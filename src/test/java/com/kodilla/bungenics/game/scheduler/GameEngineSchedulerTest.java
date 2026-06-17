package com.kodilla.bungenics.game.scheduler;

import com.kodilla.bungenics.domain.player.Player;
import com.kodilla.bungenics.domain.player.RabbitFarm;
import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.domain.rabbit.RabbitStatus;
import com.kodilla.bungenics.domain.rabbit.RabbitTrait;
import com.kodilla.bungenics.domain.rabbit.SecondaryStats;
import com.kodilla.bungenics.domain.rabbitFarm.Room;
import com.kodilla.bungenics.domain.rabbitFarm.Structure;
import com.kodilla.bungenics.domain.rabbitFarm.StructureType;
import com.kodilla.bungenics.game.gameSetup.BasicConstants;
import com.kodilla.bungenics.repository.*;
import com.kodilla.bungenics.service.AdventureService;
import com.kodilla.bungenics.service.StructureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameEngineSchedulerTest {

    @Mock private PlayerRepository playerRepository;
    @Mock private RabbitRepository rabbitRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private RabbitFarmRepository rabbitFarmRepository;
    @Mock private AdventureService adventureService;
    @Mock private StructureService structureService;

    @Spy
    @InjectMocks
    private GameEngineScheduler scheduler;

    private Player player;
    private RabbitFarm farm;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 5, 12, 0);

    @BeforeEach
    void setUp() {
        player = new Player();
        player.setId(1L);
        farm = new RabbitFarm();
        farm.setHayAmount(100f);
        farm.setCarrotAmount(0f);
        farm.setSpinachAmount(0f);
        farm.setLettuceAmount(0f);

        lenient().doNothing().when(structureService).sanitizeRabbitFields(any());
        lenient().doNothing().when(structureService).tryAutoAssignToWarren(any(), anyLong());
        lenient().doNothing().when(adventureService).resolveCompletedAdventures();

        lenient().doReturn(1).when(scheduler).getGrowDaysForRabbit(any());
    }

    private Rabbit createRabbit(Long id, RabbitStatus status, float age, float life, float stress,
                                float nutrition, boolean inRoom) {
        Rabbit rabbit = Rabbit.builder()
                .id(id)
                .playerId(1L)
                .status(status)
                .life(life)
                .stress(stress)
                .nutritionLevel(nutrition)
                .age(age)
                .weight(2.0f)
                .adultWeight(2.0f)
                .traits(new HashSet<>())
                .secondaryStats(SecondaryStats.builder().life(100f).stress(100f).build())
                .build();
        if (inRoom) {
            Room room = Room.builder().id(10L).rabbits(new ArrayList<>(List.of(rabbit)))
                    .structure(Structure.builder().structureType(StructureType.WARREN).build())
                    .build();
            when(roomRepository.findByRabbitsContaining(rabbit)).thenReturn(Optional.of(room));
        } else {
            when(roomRepository.findByRabbitsContaining(rabbit)).thenReturn(Optional.empty());
        }
        return rabbit;
    }

    // ---------- getGrowDaysForRabbit ----------
    @Nested
    class GetGrowDays {
        @BeforeEach
        void resetSpy() {
            doCallRealMethod().when(scheduler).getGrowDaysForRabbit(any());
        }

        @Test
        void shouldReturnSmallBreedDays() {
            Rabbit small = Rabbit.builder().adultWeight(1.5f).traits(new HashSet<>()).build();
            assertThat(scheduler.getGrowDaysForRabbit(small)).isEqualTo(BasicConstants.SMALL_RABBITS_GROW_DAYS);
        }

        @Test
        void shouldReturnAverageBreedDays() {
            Rabbit avg = Rabbit.builder().adultWeight(3.0f).traits(new HashSet<>()).build();
            assertThat(scheduler.getGrowDaysForRabbit(avg)).isEqualTo(BasicConstants.AVERAGE_RABBITS_GROW_DAYS);
        }

        @Test
        void shouldReturnLargeBreedDays() {
            Rabbit large = Rabbit.builder().adultWeight(6.0f).traits(new HashSet<>()).build();
            assertThat(scheduler.getGrowDaysForRabbit(large)).isEqualTo(BasicConstants.LARGE_RABBITS_GROW_DAYS);
        }

        @Test
        void shouldHalveDaysForQuickGrower() {
            Rabbit quick = Rabbit.builder().adultWeight(3.0f).traits(Set.of(RabbitTrait.QUICK_GROWER)).build();
            int expected = Math.max(1, Math.round(BasicConstants.AVERAGE_RABBITS_GROW_DAYS * 0.5f));
            assertThat(scheduler.getGrowDaysForRabbit(quick)).isEqualTo(expected);
        }
    }

    // ---------- processGameTick ----------
    @Nested
    class ProcessGameTick {

        private float daysPerTick;

        @BeforeEach
        void computeDaysPerTick() {
            float realSecondsPerDay = BasicConstants.MINUTES_PER_DAY * 60.0f;
            daysPerTick = (BasicConstants.SCHEDULER_TICK_RATE_MS / 1000.0f) / realSecondsPerDay;
        }

        @Test
        void shouldAgeAndFeedRabbitInWarren() {
            Rabbit rabbit = createRabbit(1L, RabbitStatus.IDLE, 1.0f, 90f, 20f, 80f, true);
            when(playerRepository.findAll()).thenReturn(List.of(player));
            when(rabbitFarmRepository.findByPlayerId(1L)).thenReturn(Optional.of(farm));
            when(rabbitRepository.findByPlayerId(1L)).thenReturn(List.of(rabbit));

            scheduler.processGameTick();

            assertThat(rabbit.getAge()).isEqualTo(1.0f + daysPerTick);
            float expectedNutrition = 80f - BasicConstants.HUNGER_DECAY_PER_DAY * daysPerTick;
            assertThat(rabbit.getNutritionLevel()).isEqualTo(expectedNutrition);
            assertThat(rabbit.getLife()).isEqualTo(90f + BasicConstants.WARREN_LIFE_REGEN_PER_TICK);
            assertThat(rabbit.getStress()).isEqualTo(20f - BasicConstants.WARREN_STRESS_REDUCTION_PER_TICK);
            verify(rabbitRepository).save(rabbit);
        }

        @Test
        void shouldFeedHungryRabbitWithHay() {
            Rabbit rabbit = createRabbit(2L, RabbitStatus.IDLE, 0f, 100f, 10f, 49.5f, true);
            farm.setHayAmount(50f);
            when(playerRepository.findAll()).thenReturn(List.of(player));
            when(rabbitFarmRepository.findByPlayerId(1L)).thenReturn(Optional.of(farm));
            when(rabbitRepository.findByPlayerId(1L)).thenReturn(List.of(rabbit));

            scheduler.processGameTick();

            assertThat(rabbit.getNutritionLevel()).isEqualTo(100f);
            assertThat(farm.getHayAmount()).isEqualTo(50f - 0.1f);
        }

        @Test
        void shouldStarveAndTakeDamageIfNoHay() {
            Rabbit rabbit = createRabbit(3L, RabbitStatus.IDLE, 0f, 30f, 10f, 30f, false); // bezdomny
            farm.setHayAmount(0f);
            when(playerRepository.findAll()).thenReturn(List.of(player));
            when(rabbitFarmRepository.findByPlayerId(1L)).thenReturn(Optional.of(farm));
            when(rabbitRepository.findByPlayerId(1L)).thenReturn(List.of(rabbit));

            scheduler.processGameTick();

            assertThat(rabbit.getLife()).isEqualTo(28f);
            verify(rabbitRepository).save(rabbit);
        }

        @Test
        void shouldKillRabbitIfStarvedToZeroHp() {
            Rabbit rabbit = createRabbit(4L, RabbitStatus.IDLE, 0f, 2f, 0f, 30f, true);
            farm.setHayAmount(0f);
            when(playerRepository.findAll()).thenReturn(List.of(player));
            when(rabbitFarmRepository.findByPlayerId(1L)).thenReturn(Optional.of(farm));
            when(rabbitRepository.findByPlayerId(1L)).thenReturn(List.of(rabbit));

            scheduler.processGameTick();

            assertThat(rabbit.getStatus()).isEqualTo(RabbitStatus.DEAD);
        }

        @Test
        void shouldIncreaseStressForHomelessRabbit() {
            Rabbit rabbit = createRabbit(5L, RabbitStatus.IDLE, 0f, 100f, 30f, 80f, false);
            when(playerRepository.findAll()).thenReturn(List.of(player));
            when(rabbitFarmRepository.findByPlayerId(1L)).thenReturn(Optional.of(farm));
            when(rabbitRepository.findByPlayerId(1L)).thenReturn(List.of(rabbit));

            scheduler.processGameTick();

            assertThat(rabbit.getStress()).isEqualTo(30f + BasicConstants.HOMELESS_STRESS_INCREASE_PER_TICK);
        }

        @Test
        void shouldApplyCalmAndSkittishModifiersForHomelessStress() {
            Rabbit rabbit = createRabbit(6L, RabbitStatus.IDLE, 0f, 100f, 30f, 80f, false);
            rabbit.getTraits().add(RabbitTrait.CALM);
            rabbit.getTraits().add(RabbitTrait.SKITTISH); // -0.2+0.2 = 1.0
            when(playerRepository.findAll()).thenReturn(List.of(player));
            when(rabbitFarmRepository.findByPlayerId(1L)).thenReturn(Optional.of(farm));
            when(rabbitRepository.findByPlayerId(1L)).thenReturn(List.of(rabbit));

            scheduler.processGameTick();

            float expectedStress = 30f + BasicConstants.HOMELESS_STRESS_INCREASE_PER_TICK * 1.0f;
            assertThat(rabbit.getStress()).isEqualTo(expectedStress);
        }

        @Test
        void shouldReduceStressInPlayhouseAndAutoAssignWhenZero() {
            Rabbit rabbit = createRabbit(7L, RabbitStatus.IDLE, 0f, 100f, 5f, 80f, true);
            Room playroom = Room.builder().id(20L).rabbits(new ArrayList<>(List.of(rabbit)))
                    .structure(Structure.builder().structureType(StructureType.PLAYHOUSE).build()).build();
            when(roomRepository.findByRabbitsContaining(rabbit)).thenReturn(Optional.of(playroom));
            when(playerRepository.findAll()).thenReturn(List.of(player));
            when(rabbitFarmRepository.findByPlayerId(1L)).thenReturn(Optional.of(farm));
            when(rabbitRepository.findByPlayerId(1L)).thenReturn(List.of(rabbit));

            scheduler.processGameTick();

            assertThat(rabbit.getStress()).isEqualTo(5f - BasicConstants.PLAYHOUSE_STRESS_REDUCTION_PER_TICK);
            verify(roomRepository, never()).save(playroom);
            verify(structureService, never()).tryAutoAssignToWarren(any(), anyLong());
        }

        @Test
        void shouldRemoveFromPlayhouseWhenStressZero() {
            Rabbit rabbit = createRabbit(8L, RabbitStatus.IDLE, 0f, 100f, 0.4f, 80f, true);
            Room playroom = Room.builder().id(20L).rabbits(new ArrayList<>(List.of(rabbit)))
                    .structure(Structure.builder().structureType(StructureType.PLAYHOUSE).build()).build();
            when(roomRepository.findByRabbitsContaining(rabbit)).thenReturn(Optional.of(playroom));
            when(playerRepository.findAll()).thenReturn(List.of(player));
            when(rabbitFarmRepository.findByPlayerId(1L)).thenReturn(Optional.of(farm));
            when(rabbitRepository.findByPlayerId(1L)).thenReturn(List.of(rabbit));

            scheduler.processGameTick();

            assertThat(rabbit.getStress()).isEqualTo(0f);
            assertThat(playroom.getRabbits()).doesNotContain(rabbit);
            verify(roomRepository).save(playroom);
            verify(structureService).tryAutoAssignToWarren(rabbit, 1L);
        }

        @Test
        void shouldKillRabbitIfStressExceeds90Percent() {
            Rabbit rabbit = createRabbit(9L, RabbitStatus.IDLE, 0f, 5f, 95f, 80f, false); // bezdomny
            when(playerRepository.findAll()).thenReturn(List.of(player));
            when(rabbitFarmRepository.findByPlayerId(1L)).thenReturn(Optional.of(farm));
            when(rabbitRepository.findByPlayerId(1L)).thenReturn(List.of(rabbit));

            scheduler.processGameTick();

            assertThat(rabbit.getLife()).isEqualTo(3f);
        }

        @Test
        void shouldMatureKitToRestingWhenOldEnough() {
            Rabbit kit = createRabbit(10L, RabbitStatus.KIT, 0.992f, 100f, 0f, 80f, true);
            kit.setAdultWeight(2.0f);
            when(playerRepository.findAll()).thenReturn(List.of(player));
            when(rabbitFarmRepository.findByPlayerId(1L)).thenReturn(Optional.of(farm));
            when(rabbitRepository.findByPlayerId(1L)).thenReturn(List.of(kit));

            scheduler.processGameTick();

            assertThat(kit.getStatus()).isEqualTo(RabbitStatus.RESTING);
            assertThat(kit.getRestEndTime()).isNotNull();
            verify(structureService).tryAutoAssignToWarren(kit, 1L);
        }

        @Test
        void shouldCompleteRestingIfTimePassed() {
            Rabbit resting = createRabbit(11L, RabbitStatus.RESTING, 0f, 100f, 10f, 80f, true);
            resting.setRestEndTime(NOW.minusMinutes(1));
            try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class)) {
                mockedTime.when(LocalDateTime::now).thenReturn(NOW);

                when(playerRepository.findAll()).thenReturn(List.of(player));
                when(rabbitFarmRepository.findByPlayerId(1L)).thenReturn(Optional.of(farm));
                when(rabbitRepository.findByPlayerId(1L)).thenReturn(List.of(resting));

                scheduler.processGameTick();
            }

            assertThat(resting.getStatus()).isEqualTo(RabbitStatus.IDLE);
            assertThat(resting.getRestEndTime()).isNull();
        }

        @Test
        void shouldCompleteVetStayIfTimePassed() {
            Rabbit vetRabbit = createRabbit(12L, RabbitStatus.ON_VET, 0f, 80f, 30f, 80f, true);
            vetRabbit.setVetEndTime(NOW.minusMinutes(1));
            try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class)) {
                mockedTime.when(LocalDateTime::now).thenReturn(NOW);

                when(playerRepository.findAll()).thenReturn(List.of(player));
                when(rabbitFarmRepository.findByPlayerId(1L)).thenReturn(Optional.of(farm));
                when(rabbitRepository.findByPlayerId(1L)).thenReturn(List.of(vetRabbit));

                scheduler.processGameTick();
            }

            assertThat(vetRabbit.getStatus()).isEqualTo(RabbitStatus.IDLE);
            assertThat(vetRabbit.getLife()).isEqualTo(100f);
            assertThat(vetRabbit.getStress()).isEqualTo(0f);
        }

        @Test
        void shouldTriggerBreedingCompletionIfTimePassed() {
            Rabbit breeding = createRabbit(13L, RabbitStatus.BREEDING, 0f, 100f, 10f, 80f, true);
            breeding.setBreedingEndTime(NOW.minusMinutes(1));
            try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class)) {
                mockedTime.when(LocalDateTime::now).thenReturn(NOW);

                when(playerRepository.findAll()).thenReturn(List.of(player));
                when(rabbitFarmRepository.findByPlayerId(1L)).thenReturn(Optional.of(farm));
                when(rabbitRepository.findByPlayerId(1L)).thenReturn(List.of(breeding));

                scheduler.processGameTick();
            }

            verify(structureService).completeBreedingProcess(breeding);
        }

        @Test
        void shouldTriggerTrainingCompletionIfTimePassed() {
            Rabbit training = createRabbit(14L, RabbitStatus.TRAINING, 0f, 100f, 10f, 80f, true);
            training.setTrainingEndTime(NOW.minusMinutes(1));
            try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class)) {
                mockedTime.when(LocalDateTime::now).thenReturn(NOW);

                when(playerRepository.findAll()).thenReturn(List.of(player));
                when(rabbitFarmRepository.findByPlayerId(1L)).thenReturn(Optional.of(farm));
                when(rabbitRepository.findByPlayerId(1L)).thenReturn(List.of(training));

                scheduler.processGameTick();
            }

            verify(structureService).completeTrainingProcess(training);
        }

        @Test
        void shouldResolveAdventuresAtEndOfTick() {
            when(playerRepository.findAll()).thenReturn(Collections.emptyList());

            scheduler.processGameTick();

            verify(adventureService).resolveCompletedAdventures();
        }
    }
}