package com.kodilla.bungenics.service;

import com.kodilla.bungenics.domain.adventure.Adventure;
import com.kodilla.bungenics.domain.adventure.AdventureEvent;
import com.kodilla.bungenics.domain.player.Player;
import com.kodilla.bungenics.domain.player.RabbitFarm;
import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.domain.rabbit.RabbitStatus;
import com.kodilla.bungenics.domain.rabbit.RabbitTrait;
import com.kodilla.bungenics.exception.ResourceNotFoundException;
import com.kodilla.bungenics.game.gameSetup.BasicConstants;
import com.kodilla.bungenics.game.strategy.AdventureStrategy;
import com.kodilla.bungenics.repository.AdventureRepository;
import com.kodilla.bungenics.repository.RabbitFarmRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdventureServiceTest {

    @Mock private AdventureRepository adventureRepository;
    @Mock private RabbitService rabbitService;
    @Mock private PlayerService playerService;
    @Mock private StructureService structureService;
    @Mock private RabbitFarmRepository rabbitFarmRepository;

    @Mock private AdventureStrategy forestStrategy;
    @Mock private AdventureStrategy mountainStrategy;

    @InjectMocks
    private AdventureService adventureService;

    private Rabbit rabbit;
    private Player player;
    private Adventure inProgressAdventure;
    private List<AdventureEvent> events;

    @BeforeEach
    void setUp() {
        // Standard rabbit (IDLE, healthy)
        rabbit = Rabbit.builder()
                .id(10L)
                .playerId(1L)
                .status(RabbitStatus.IDLE)
                .life(100f)
                .stress(10f)
                .traits(new HashSet<>())
                .build();

        player = new Player();
        player.setId(1L);
        player.setMoney(BigDecimal.valueOf(1000));

        inProgressAdventure = new Adventure();
        inProgressAdventure.setId(100L);
        inProgressAdventure.setPlayerId(1L);
        inProgressAdventure.setRabbitId(10L);
        inProgressAdventure.setType("FOREST");
        inProgressAdventure.setStatus("IN_PROGRESS");
        inProgressAdventure.setEndTime(LocalDateTime.now().minusMinutes(1));
        inProgressAdventure.setAdventureEvents(new ArrayList<>());

        events = new ArrayList<>();
        events.add(createEvent("Event1", "Success", BigDecimal.TEN, 5f, 3f, 2f));
        events.add(createEvent("Event2", "Failure", BigDecimal.ONE, 0f, 0f, 0f));

        // domyślne ustawienia strategii
        lenient().when(forestStrategy.getAdventureType()).thenReturn("FOREST");
        lenient().when(mountainStrategy.getAdventureType()).thenReturn("MOUNTAIN");
        adventureService = new AdventureService(
                adventureRepository,
                rabbitService,
                playerService,
                structureService,
                rabbitFarmRepository,
                List.of(forestStrategy, mountainStrategy)
        );
    }

    private AdventureEvent createEvent(String name, String result, BigDecimal gold, float carrots, float lettuce, float spinach) {
        AdventureEvent event = new AdventureEvent();
        event.setName(name);
        event.setResult(result);
        event.setGoldReward(gold);
        event.setCarrotReward(carrots);
        event.setLettuceReward(lettuce);
        event.setSpinachReward(spinach);
        return event;
    }

    // ---------- getCompletedAdventures ----------
    @Test
    void shouldReturnCompletedAdventuresForPlayer() {
        Adventure completed = new Adventure();
        completed.setPlayerId(1L);
        completed.setStatus("COMPLETED");
        Adventure inProgress = new Adventure();
        inProgress.setPlayerId(1L);
        inProgress.setStatus("IN_PROGRESS");
        when(adventureRepository.findAll()).thenReturn(List.of(completed, inProgress));

        List<Adventure> result = adventureService.getCompletedAdventures(1L);
        assertThat(result).hasSize(1).containsExactly(completed);
    }

    // ---------- createAdventure ----------
    @Test
    void shouldCreateAdventure() {
        Adventure adv = new Adventure();
        when(adventureRepository.save(adv)).thenReturn(adv);
        assertThat(adventureService.createAdventure(adv)).isSameAs(adv);
    }

    // ---------- getAdventureById ----------
    @Test
    void shouldGetAdventureById() {
        Adventure adv = new Adventure();
        adv.setId(5L);
        when(adventureRepository.findById(5L)).thenReturn(Optional.of(adv));
        assertThat(adventureService.getAdventureById(5L)).isEqualTo(adv);
    }

    @Test
    void shouldThrowWhenAdventureNotFound() {
        when(adventureRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> adventureService.getAdventureById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldGetAllAdventures() {
        when(adventureRepository.findAll()).thenReturn(List.of(new Adventure(), new Adventure()));
        assertThat(adventureService.getAllAdventures()).hasSize(2);
    }

    // ---------- sendRabbitOnAdventure ----------
    @Nested
    class SendRabbitOnAdventure {

        @Test
        void shouldSendIdleRabbit() {
            when(rabbitService.getRabbitById(10L)).thenReturn(rabbit);

            adventureService.sendRabbitOnAdventure(1L, 10L, "FOREST");

            assertEquals(RabbitStatus.ADVENTURE, rabbit.getStatus());
            assertNotNull(rabbit.getAdventureEndTime());
            verify(rabbitService).updateRabbit(rabbit.getId(), rabbit);
            verify(adventureRepository).save(any(Adventure.class));
        }

        @Test
        void shouldThrowIfNotIdle() {
            rabbit.setStatus(RabbitStatus.RESTING);
            when(rabbitService.getRabbitById(10L)).thenReturn(rabbit);

            assertThatThrownBy(() -> adventureService.sendRabbitOnAdventure(1L, 10L, "FOREST"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("IDLE");
            verify(adventureRepository, never()).save(any());
        }

        @Test
        void shouldExtendDurationIfLazy() {
            rabbit.getTraits().add(RabbitTrait.LAZY);
            when(rabbitService.getRabbitById(10L)).thenReturn(rabbit);

            adventureService.sendRabbitOnAdventure(1L, 10L, "FOREST");

            long baseSeconds = BasicConstants.ADVENTURE_DURATION_MINUTES * 60L;
            long expected = Math.round(baseSeconds * 1.20f);
            // Sprawdzamy, czy czas zakończenia jest w przyszłości o ~expected sekund (z tolerancją)
            LocalDateTime now = LocalDateTime.now();
            long diff = java.time.Duration.between(now, rabbit.getAdventureEndTime()).getSeconds();
            assertThat(diff).isBetween((long)(expected * 0.9), (long)(expected * 1.1));
        }
    }

    // ---------- resolveCompletedAdventures ----------
    @Nested
    class ResolveCompletedAdventures {

        @Test
        void shouldCompleteAdventureAndGiveRewards() {
            // Arrange
            when(adventureRepository.findAll()).thenReturn(List.of(inProgressAdventure));
            when(rabbitService.getRabbitById(10L)).thenReturn(rabbit);
            when(forestStrategy.executeAdventure(rabbit)).thenReturn(events);
            when(playerService.getPlayerById(1L)).thenReturn(player);
            // Farm exists with some amounts
            RabbitFarm farm = new RabbitFarm();
            farm.setCarrotAmount(0f);
            farm.setLettuceAmount(0f);
            farm.setSpinachAmount(0f);
            when(rabbitFarmRepository.findByPlayerId(1L)).thenReturn(Optional.of(farm));

            // Act
            adventureService.resolveCompletedAdventures();

            // Assert
            assertEquals(RabbitStatus.RESTING, rabbit.getStatus());
            assertNull(rabbit.getAdventureEndTime());
            verify(structureService).tryAutoAssignToWarren(rabbit, 1L);

            // Gold: 10 + 1 = 11
            verify(playerService).updatePlayer(eq(1L), argThat(p ->
                    p.getMoney().compareTo(BigDecimal.valueOf(1011)) == 0));

            // Farm rewards added
            verify(rabbitFarmRepository).save(argThat(f ->
                    f.getCarrotAmount() == 5f && f.getLettuceAmount() == 3f && f.getSpinachAmount() == 2f));

            // Adventure status updated
            assertEquals("COMPLETED", inProgressAdventure.getStatus());
            assertThat(inProgressAdventure.getAdventureEvents()).hasSize(2);
            verify(adventureRepository).save(inProgressAdventure);
        }

        @Test
        void shouldHandleRabbitDeathDuringAdventure() {
            rabbit.setLife(0f);
            rabbit.setStatus(RabbitStatus.DEAD);

            List<AdventureEvent> noFoodEvents = List.of(
                    createEvent("Ev", "Res", BigDecimal.TEN, 0f, 0f, 0f)
            );

            when(adventureRepository.findAll()).thenReturn(List.of(inProgressAdventure));
            when(rabbitService.getRabbitById(10L)).thenReturn(rabbit);
            when(forestStrategy.executeAdventure(rabbit)).thenReturn(noFoodEvents);
            when(playerService.getPlayerById(1L)).thenReturn(player);

            adventureService.resolveCompletedAdventures();

            assertEquals(RabbitStatus.DEAD, rabbit.getStatus());
            assertEquals(0f, rabbit.getLife());
            assertNull(rabbit.getAdventureEndTime());
            verify(structureService, never()).tryAutoAssignToWarren(any(), anyLong());
            verify(playerService).updatePlayer(eq(1L), any());
            assertEquals("COMPLETED", inProgressAdventure.getStatus());
        }

        @Test
        void shouldHandleNullFarmGracefully() {
            when(adventureRepository.findAll()).thenReturn(List.of(inProgressAdventure));
            when(rabbitService.getRabbitById(10L)).thenReturn(rabbit);
            when(forestStrategy.executeAdventure(rabbit)).thenReturn(events);
            when(playerService.getPlayerById(1L)).thenReturn(player);
            when(rabbitFarmRepository.findByPlayerId(1L)).thenReturn(Optional.empty());

            assertThatCode(() -> adventureService.resolveCompletedAdventures()).doesNotThrowAnyException();
            verify(rabbitFarmRepository, never()).save(any());
        }

        @Test
        void shouldFallbackToFirstStrategyIfTypeNotFound() {
            inProgressAdventure.setType("UNKNOWN");
            when(adventureRepository.findAll()).thenReturn(List.of(inProgressAdventure));
            when(rabbitService.getRabbitById(10L)).thenReturn(rabbit);
            when(forestStrategy.executeAdventure(rabbit)).thenReturn(events);
            when(playerService.getPlayerById(1L)).thenReturn(player);

            assertThatCode(() -> adventureService.resolveCompletedAdventures()).doesNotThrowAnyException();
            verify(forestStrategy).executeAdventure(rabbit);
        }

        @Test
        void shouldThrowIfNoStrategiesAvailable() {
            AdventureService emptyService = new AdventureService(
                    adventureRepository, rabbitService, playerService,
                    structureService, rabbitFarmRepository, Collections.emptyList()
            );
            inProgressAdventure.setType("FOREST");
            when(adventureRepository.findAll()).thenReturn(List.of(inProgressAdventure));
            when(rabbitService.getRabbitById(10L)).thenReturn(rabbit);

            assertThatCode(() -> emptyService.resolveCompletedAdventures()).doesNotThrowAnyException();
            verify(adventureRepository, never()).save(inProgressAdventure);
        }

        @Test
        void shouldNotProcessIfNotInProgress() {
            inProgressAdventure.setStatus("COMPLETED");
            when(adventureRepository.findAll()).thenReturn(List.of(inProgressAdventure));
            adventureService.resolveCompletedAdventures();
            verify(rabbitService, never()).getRabbitById(anyLong());
        }
    }
}