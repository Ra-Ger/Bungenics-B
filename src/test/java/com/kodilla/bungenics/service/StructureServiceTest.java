package com.kodilla.bungenics.service;

import com.kodilla.bungenics.domain.player.Player;
import com.kodilla.bungenics.domain.player.RabbitFarm;
import com.kodilla.bungenics.domain.rabbit.*;
import com.kodilla.bungenics.domain.rabbitFarm.Room;
import com.kodilla.bungenics.domain.rabbitFarm.Structure;
import com.kodilla.bungenics.domain.rabbitFarm.StructureType;
import com.kodilla.bungenics.game.factory.RabbitFactory;
import com.kodilla.bungenics.game.gameSetup.BasicConstants;
import com.kodilla.bungenics.repository.*;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StructureServiceTest {

    @Mock private StructureRepository structureRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private RabbitRepository rabbitRepository;
    @Mock private RabbitFarmRepository rabbitFarmRepository;
    @Mock private PlayerRepository playerRepository;
    @Mock private RabbitFactory rabbitFactory;

    @InjectMocks
    private StructureService structureService;

    // ---------- sanitizeRabbitFields ----------
    @Nested
    class SanitizeRabbitFields {

        @Test
        void shouldSetDefaultsForNullFields() {
            Rabbit rabbit = new Rabbit();
            structureService.sanitizeRabbitFields(rabbit);

            assertEquals(16.0f, rabbit.getMaxLifetime());
            assertEquals(0.0f, rabbit.getAge());
            assertEquals(100.0f, rabbit.getNutritionLevel());
            assertEquals(0.0f, rabbit.getStress());
            assertEquals(1.0f, rabbit.getWeight());
            assertNotNull(rabbit.getTraits());
            assertTrue(rabbit.getTraits().isEmpty());
            // life uses getMaxHp(), which depends on secondary stats or breed. With no stats, getMaxHp() returns 100.0f
            assertEquals(100.0f, rabbit.getLife());
        }

        @Test
        void shouldNotOverrideExistingFields() {
            Rabbit rabbit = Rabbit.builder()
                    .maxLifetime(20f)
                    .age(5f)
                    .life(80f)
                    .stress(10f)
                    .weight(2.5f)
                    .nutritionLevel(90f)
                    .traits(Set.of(RabbitTrait.HARDY))
                    .build();
            structureService.sanitizeRabbitFields(rabbit);
            assertEquals(20f, rabbit.getMaxLifetime());
            assertEquals(5f, rabbit.getAge());
            assertEquals(80f, rabbit.getLife());
            assertEquals(10f, rabbit.getStress());
            assertEquals(2.5f, rabbit.getWeight());
            assertEquals(90f, rabbit.getNutritionLevel());
            assertEquals(1, rabbit.getTraits().size());
        }
    }

    // ---------- areRelated ----------
    @Nested
    class AreRelated {

        @Test
        void shouldReturnTrueForSameId() {
            Rabbit r1 = Rabbit.builder().id(1L).build();
            assertTrue(structureService.areRelated(r1, r1));
        }

        @Test
        void shouldReturnTrueForParentChild() {
            Rabbit parent = Rabbit.builder().id(1L).build();
            Rabbit child = Rabbit.builder().id(2L).motherId(1L).build();
            assertTrue(structureService.areRelated(parent, child));
            assertTrue(structureService.areRelated(child, parent));
        }

        @Test
        void shouldReturnTrueForSiblings() {
            Rabbit r1 = Rabbit.builder().id(1L).motherId(10L).fatherId(20L).build();
            Rabbit r2 = Rabbit.builder().id(2L).motherId(10L).fatherId(20L).build();
            assertTrue(structureService.areRelated(r1, r2));
        }

        @Test
        void shouldReturnFalseForUnrelated() {
            Rabbit r1 = Rabbit.builder().id(1L).motherId(10L).fatherId(20L).build();
            Rabbit r2 = Rabbit.builder().id(2L).motherId(30L).fatherId(40L).build();
            assertFalse(structureService.areRelated(r1, r2));
        }
    }

    // ---------- tryAutoAssignToWarren ----------
    @Nested
    class TryAutoAssignToWarren {

        private Rabbit rabbit;
        private Long playerId = 1L;
        private Player player;
        private RabbitFarm farm;
        private Structure warren;
        private Room room1, room2;

        @BeforeEach
        void setUp() {
            rabbit = Rabbit.builder().id(10L).playerId(playerId).build();
            player = new Player();
            player.setId(playerId);
            farm = RabbitFarm.builder().id(100L).player(player).build();

            warren = Structure.builder()
                    .id(1L)
                    .structureType(StructureType.WARREN)
                    .rabbitFarm(farm)
                    .rooms(new ArrayList<>())
                    .build();

            room1 = Room.builder().id(1L).slots(2).structure(warren).rabbits(new ArrayList<>()).build();
            room2 = Room.builder().id(2L).slots(2).structure(warren).rabbits(new ArrayList<>()).build();
            warren.getRooms().add(room1);
            warren.getRooms().add(room2);
        }

        @Test
        void shouldAssignToFirstAvailableRoom() {
            when(roomRepository.findAll()).thenReturn(List.of(room1, room2));
            when(structureRepository.findAll()).thenReturn(List.of(warren));

            structureService.tryAutoAssignToWarren(rabbit, playerId);

            assertThat(room1.getRabbits()).contains(rabbit);
            verify(roomRepository).save(room1);
        }

        @Test
        void shouldSkipIfAlreadyAssigned() {
            room1.getRabbits().add(rabbit);
            when(roomRepository.findAll()).thenReturn(List.of(room1, room2));

            structureService.tryAutoAssignToWarren(rabbit, playerId);

            verify(roomRepository, never()).save(any());
        }

        @Test
        void shouldNotAssignIfNoFreeSlots() {
            room1.setSlots(1);
            room1.getRabbits().add(new Rabbit());
            room2.setSlots(1);
            room2.getRabbits().add(new Rabbit());

            when(roomRepository.findAll()).thenReturn(List.of(room1, room2));
            when(structureRepository.findAll()).thenReturn(List.of(warren));

            structureService.tryAutoAssignToWarren(rabbit, playerId);

            verify(roomRepository, never()).save(any());
        }

        @Test
        void shouldIgnoreNonWarrenStructures() {
            Structure nonWarren = Structure.builder()
                    .structureType(StructureType.TRAINING_GROUND)
                    .rabbitFarm(farm)
                    .rooms(new ArrayList<>())
                    .build();
            when(roomRepository.findAll()).thenReturn(List.of(room1));
            when(structureRepository.findAll()).thenReturn(List.of(nonWarren, warren));

            structureService.tryAutoAssignToWarren(rabbit, playerId);

            verify(roomRepository).save(room1);
        }
    }

    // ---------- completeBreedingProcess ----------
    @Nested
    class CompleteBreedingProcess {

        private Rabbit female, male;
        private Room room;
        private Rabbit kit;

        @BeforeEach
        void setUp() {
            female = Rabbit.builder().id(1L).playerId(1L).status(RabbitStatus.BREEDING).sex("FEMALE").traits(new HashSet<>()).build();
            male = Rabbit.builder().id(2L).playerId(1L).status(RabbitStatus.BREEDING).sex("MALE").traits(new HashSet<>()).build();
            room = Room.builder().id(1L).slots(4).rabbits(new ArrayList<>(List.of(female, male))).structure(
                    Structure.builder().id(10L).structureType(StructureType.TRYSTHOUSE).build()
            ).build();
            kit = Rabbit.builder().id(3L).playerId(1L).status(RabbitStatus.KIT).traits(new HashSet<>()).build();
            lenient().when(rabbitFactory.createKit(female, male)).thenReturn(kit);
            lenient().when(rabbitRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            lenient().when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        }

        @Test
        void shouldCompleteBreedingAndCreateKit() {
            when(roomRepository.findAll()).thenReturn(List.of(room));

            Rabbit result = structureService.completeBreedingProcess(female);

            // parents become resting
            assertEquals(RabbitStatus.RESTING, female.getStatus());
            assertEquals(RabbitStatus.RESTING, male.getStatus());
            assertNull(female.getBreedingEndTime());
            assertNotNull(female.getRestEndTime());
            assertNotNull(male.getRestEndTime());

            // kit saved and returned
            verify(rabbitRepository, atLeastOnce()).save(kit);
            assertEquals(kit, result);
            // parents removed from room
            assertThat(room.getRabbits()).isEmpty();
            verify(roomRepository).save(room);
        }

        @Test
        void shouldNotProcessIfStatusNotBreeding() {
            female.setStatus(RabbitStatus.IDLE);
            Rabbit result = structureService.completeBreedingProcess(female);
            assertSame(female, result);
            verify(rabbitFactory, never()).createKit(any(), any());
        }

        @Test
        void shouldHandleMissingPartner() {
            room.getRabbits().remove(male);
            when(roomRepository.findAll()).thenReturn(List.of(room));

            Rabbit result = structureService.completeBreedingProcess(female);
            assertEquals(RabbitStatus.RESTING, female.getStatus());
            assertNull(female.getBreedingEndTime());
            verify(rabbitFactory, never()).createKit(any(), any());
        }

        @Test
        void shouldHandleRabbitNotInAnyRoom() {
            when(roomRepository.findAll()).thenReturn(Collections.emptyList());
            Rabbit result = structureService.completeBreedingProcess(female);
            assertEquals(RabbitStatus.RESTING, female.getStatus());
            verify(rabbitRepository).save(female);
        }
    }

    // ---------- buildStructure ----------
    @Nested
    class BuildStructure {

        private Player player;
        private RabbitFarm farm;

        @BeforeEach
        void setUp() {
            player = new Player();
            player.setId(1L);
            player.setMoney(BigDecimal.valueOf(1000));
            farm = RabbitFarm.builder().id(10L).player(player).build();
            lenient().when(rabbitFarmRepository.findById(10L)).thenReturn(Optional.of(farm));
            lenient().when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
        }

        @Test
        void shouldDeductGoldAndCreateStructure() {
            when(structureRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Structure result = structureService.buildStructure(10L, StructureType.WARREN, 0);

            assertEquals(BigDecimal.valueOf(1000).subtract(BasicConstants.STRUCTURE_BUILD_COST), player.getMoney());
            assertNotNull(result);
            assertEquals(StructureType.WARREN, result.getStructureType());
            assertEquals(1, result.getRooms().size());
            verify(playerRepository).save(player);
            verify(structureRepository).save(any());
        }

        @Test
        void shouldThrowIfNotEnoughMoney() {
            player.setMoney(BigDecimal.ZERO);
            assertThatThrownBy(() -> structureService.buildStructure(10L, StructureType.WARREN, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough money");
        }
    }

    // ---------- addRoomToStructure ----------
    @Nested
    class AddRoomToStructure {

        @Test
        void shouldAddRoomAndDeductGold() {
            Player player = new Player();
            player.setId(1L);
            player.setMoney(BigDecimal.valueOf(500));
            RabbitFarm farm = RabbitFarm.builder().id(10L).player(player).build();
            Structure structure = Structure.builder().id(1L).rabbitFarm(farm).rooms(new ArrayList<>()).build();

            when(structureRepository.findById(1L)).thenReturn(Optional.of(structure));
            when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
            when(structureRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Structure result = structureService.addRoomToStructure(1L);

            assertEquals(1, result.getRooms().size());
            assertEquals(BigDecimal.valueOf(500).subtract(BasicConstants.ROOM_BUILD_COST), player.getMoney());
            verify(structureRepository).save(structure);
        }
    }

    // ---------- expandRoomSlots ----------
    @Nested
    class ExpandRoomSlots {

        @Test
        void shouldIncreaseSlotsByTwo() {
            Player player = new Player();
            player.setId(1L);
            player.setMoney(BigDecimal.valueOf(300));
            RabbitFarm farm = RabbitFarm.builder().id(10L).player(player).build();
            Structure structure = Structure.builder().id(1L).structureType(StructureType.WARREN).rabbitFarm(farm).build();
            Room room = Room.builder().id(1L).slots(2).structure(structure).rabbits(new ArrayList<>()).build();

            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
            when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
            when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Room result = structureService.expandRoomSlots(1L);

            assertEquals(4, result.getSlots());
            verify(roomRepository).save(room);
        }

        @Test
        void shouldThrowForTrysthouse() {
            Structure structure = Structure.builder().id(1L).structureType(StructureType.TRYSTHOUSE).build();
            Room room = Room.builder().id(1L).slots(2).structure(structure).build();

            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

            assertThatThrownBy(() -> structureService.expandRoomSlots(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("cannot be expanded");
        }
    }

    // ---------- startTrainingInRoom ----------
    @Nested
    class StartTrainingInRoom {

        private Room room;
        private Rabbit rabbit;
        private Player player;

        @BeforeEach
        void setUp() {
            player = new Player();
            player.setId(1L);
            player.setMoney(BigDecimal.valueOf(500));

            room = Room.builder().id(1L).slots(2).rabbits(new ArrayList<>()).structure(
                    Structure.builder().id(10L).structureType(StructureType.TRAINING_GROUND)
                            .rabbitFarm(RabbitFarm.builder().id(20L).player(player).build()).build()
            ).build();

            rabbit = Rabbit.builder().id(1L).playerId(1L).status(RabbitStatus.IDLE).weight(2.0f)
                    .secondaryStats(SecondaryStats.builder().strength(5f).agility(5f).intelligence(5f).build())
                    .traits(new HashSet<>()).build();
            room.getRabbits().add(rabbit);
        }

        @Test
        void shouldStartTrainingSuccessfully() {
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
            when(rabbitRepository.findById(1L)).thenReturn(Optional.of(rabbit));
            when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
            when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RabbitFarm farm = RabbitFarm.builder()
                    .id(20L)
                    .player(player)
                    .carrotAmount(100f)
                    .build();
            when(rabbitFarmRepository.findByPlayerId(1L)).thenReturn(Optional.of(farm));

            structureService.startTrainingInRoom(1L, 1L, "CARROT");

            assertEquals(RabbitStatus.TRAINING, rabbit.getStatus());
            verify(rabbitRepository).save(rabbit);
            verify(playerRepository).save(player);
        }

        @Test
        void shouldThrowIfNotTrainingGround() {
            room.getStructure().setStructureType(StructureType.WARREN);
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

            assertThatThrownBy(() -> structureService.startTrainingInRoom(1L, 1L, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("TRAINING_GROUND");
        }

        @Test
        void shouldThrowIfRabbitNotInRoom() {
            room.getRabbits().clear();
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
            when(rabbitRepository.findById(1L)).thenReturn(Optional.of(rabbit));

            assertThatThrownBy(() -> structureService.startTrainingInRoom(1L, 1L, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not in this training room");
        }

        @Test
        void shouldThrowIfNotEnoughGold() {
            player.setMoney(BigDecimal.ONE);
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
            when(rabbitRepository.findById(1L)).thenReturn(Optional.of(rabbit));
            when(playerRepository.findById(1L)).thenReturn(Optional.of(player));

            assertThatThrownBy(() -> structureService.startTrainingInRoom(1L, 1L, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough gold");
        }
    }

    // ---------- assignRabbitToRoom ----------
    @Nested
    class AssignRabbitToRoom {

        private Room room;
        private Rabbit rabbit;

        @BeforeEach
        void setUp() {
            room = Room.builder().id(1L).slots(2).rabbits(new ArrayList<>()).structure(
                    Structure.builder().rabbitFarm(RabbitFarm.builder().player(new Player()).build()).build()).build();
            rabbit = Rabbit.builder().id(1L).playerId(1L).status(RabbitStatus.IDLE).build();
            room.getStructure().getRabbitFarm().getPlayer().setId(1L);
        }

        @Test
        void shouldAssignSuccessfully() {
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
            when(rabbitRepository.findById(1L)).thenReturn(Optional.of(rabbit));
            when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Room result = structureService.assignRabbitToRoom(1L, 1L);
            assertThat(result.getRabbits()).contains(rabbit);
        }

        @Test
        void shouldThrowIfRabbitBelongsToAnotherPlayer() {
            room.getStructure().getRabbitFarm().getPlayer().setId(2L);
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
            when(rabbitRepository.findById(1L)).thenReturn(Optional.of(rabbit));

            assertThatThrownBy(() -> structureService.assignRabbitToRoom(1L, 1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("another player");
        }

        @Test
        void shouldThrowIfMarketRabbit() {
            rabbit.setStatus(RabbitStatus.MARKET);
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
            when(rabbitRepository.findById(1L)).thenReturn(Optional.of(rabbit));

            assertThatThrownBy(() -> structureService.assignRabbitToRoom(1L, 1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("market");
        }

        @Test
        void shouldThrowIfAdventureRabbit() {
            rabbit.setStatus(RabbitStatus.ADVENTURE);
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
            when(rabbitRepository.findById(1L)).thenReturn(Optional.of(rabbit));

            assertThatThrownBy(() -> structureService.assignRabbitToRoom(1L, 1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("adventure");
        }

        @Test
        void shouldThrowIfFullRoom() {
            room.setSlots(1);
            room.getRabbits().add(new Rabbit());
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
            when(rabbitRepository.findById(1L)).thenReturn(Optional.of(rabbit));

            assertThatThrownBy(() -> structureService.assignRabbitToRoom(1L, 1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("full");
        }

        @Test
        void shouldRemoveFromPreviousRoom() {
            Room previousRoom = Room.builder().id(2L).rabbits(new ArrayList<>(List.of(rabbit))).build();
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
            when(rabbitRepository.findById(1L)).thenReturn(Optional.of(rabbit));
            when(roomRepository.findByRabbitsContaining(rabbit)).thenReturn(Optional.of(previousRoom));
            when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            structureService.assignRabbitToRoom(1L, 1L);
            assertThat(previousRoom.getRabbits()).doesNotContain(rabbit);
            verify(roomRepository).save(previousRoom);
        }

        @Test
        void shouldThrowIfPlayhouseAndRabbitNotStressed() {
            room.getStructure().setStructureType(StructureType.PLAYHOUSE);
            rabbit.setStress(0f);
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
            when(rabbitRepository.findById(1L)).thenReturn(Optional.of(rabbit));

            assertThatThrownBy(() -> structureService.assignRabbitToRoom(1L, 1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("stress > 0");
        }

        @Test
        void shouldAllowPlayhouseIfStressed() {
            room.getStructure().setStructureType(StructureType.PLAYHOUSE);
            rabbit.setStress(10f);
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
            when(rabbitRepository.findById(1L)).thenReturn(Optional.of(rabbit));
            when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            assertDoesNotThrow(() -> structureService.assignRabbitToRoom(1L, 1L));
        }
    }

    // ---------- removeRabbitFromRoom ----------
    @Nested
    class RemoveRabbitFromRoom {

        @Test
        void shouldRemoveAndAutoAssign() {
            Room room = Room.builder().id(1L).rabbits(new ArrayList<>(List.of(
                    Rabbit.builder().id(1L).status(RabbitStatus.IDLE).playerId(1L).build()
            ))).build();
            Rabbit rabbit = room.getRabbits().get(0);

            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
            when(rabbitRepository.findById(1L)).thenReturn(Optional.of(rabbit));
            when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // Mock autowarren method by checking interactions
            structureService.removeRabbitFromRoom(1L, 1L);
            assertThat(room.getRabbits()).isEmpty();
            verify(roomRepository).save(room);
        }
    }

    // ---------- startBreedingInRoom ----------
    @Nested
    class StartBreedingInRoom {

        @Test
        void shouldStartBreedingSuccessfully() {
            Room room = Room.builder().id(1L).rabbits(new ArrayList<>()).structure(
                    Structure.builder().structureType(StructureType.TRYSTHOUSE).build()).build();
            Rabbit female = Rabbit.builder().id(1L).status(RabbitStatus.IDLE).sex("FEMALE").traits(new HashSet<>()).build();
            Rabbit male = Rabbit.builder().id(2L).status(RabbitStatus.IDLE).sex("MALE").traits(new HashSet<>()).build();
            room.getRabbits().add(female);
            room.getRabbits().add(male);

            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
            when(rabbitRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            structureService.startBreedingInRoom(1L);

            assertEquals(RabbitStatus.BREEDING, female.getStatus());
            assertEquals(RabbitStatus.BREEDING, male.getStatus());
            assertNotNull(female.getBreedingEndTime());
            assertNotNull(male.getBreedingEndTime());
        }

        @Test
        void shouldThrowIfNotTrysthouse() {
            Room room = Room.builder().id(1L).structure(
                    Structure.builder().structureType(StructureType.WARREN).build()).build();
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

            assertThatThrownBy(() -> structureService.startBreedingInRoom(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("TRYSTHOUSE");
        }

        @Test
        void shouldThrowIfInbreeding() {
            Room room = Room.builder().id(1L).structure(
                    Structure.builder().structureType(StructureType.TRYSTHOUSE).build()).rabbits(new ArrayList<>()).build();
            Rabbit female = Rabbit.builder().id(1L).status(RabbitStatus.IDLE).sex("FEMALE").traits(new HashSet<>()).build();
            Rabbit male = Rabbit.builder().id(2L).status(RabbitStatus.IDLE).sex("MALE").motherId(1L).traits(new HashSet<>()).build();
            room.getRabbits().add(female);
            room.getRabbits().add(male);

            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

            assertThatThrownBy(() -> structureService.startBreedingInRoom(1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Inbreeding");
        }
    }

    // ---------- completeTrainingProcess ----------
    @Nested
    class CompleteTrainingProcess {

        @Test
        void shouldIncrementStatsAndEndTraining() {
            Rabbit rabbit = Rabbit.builder().id(1L).status(RabbitStatus.TRAINING).playerId(1L).traits(new HashSet<>())
                    .secondaryStats(SecondaryStats.builder().strength(10f).agility(10f).intelligence(10f).build())
                    .trainingEnhancedFood(null).build();
            when(rabbitRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Rabbit result = structureService.completeTrainingProcess(rabbit);

            SecondaryStats stats = result.getSecondaryStats();
            assertTrue(stats.getStrength() >= 10f && stats.getStrength() <= 12f); // up to 2 increments
            assertTrue(stats.getAgility() >= 10f && stats.getAgility() <= 12f);
            assertTrue(stats.getIntelligence() >= 10f && stats.getIntelligence() <= 12f);
            assertEquals(RabbitStatus.RESTING, result.getStatus());
            assertNull(result.getTrainingEndTime());
        }
    }

    // ---------- processFinishedBreedings ----------
    @Nested
    class ProcessFinishedBreedings {

        @Test
        void shouldCompleteExpiredBreedings() {
            Rabbit expired = Rabbit.builder().id(1L).status(RabbitStatus.BREEDING)
                    .breedingEndTime(LocalDateTime.now().minusMinutes(1)).traits(new HashSet<>()).build();
            when(rabbitRepository.findByStatus(RabbitStatus.BREEDING)).thenReturn(List.of(expired));
            when(roomRepository.findAll()).thenReturn(Collections.emptyList()); // no room → rest

            structureService.processFinishedBreedings();

            assertEquals(RabbitStatus.RESTING, expired.getStatus());
            verify(rabbitRepository).save(expired);
        }
    }

    // ---------- processFinishedTrainings ----------
    @Nested
    class ProcessFinishedTrainings {

        @Test
        void shouldCompleteExpiredTrainings() {
            Rabbit expired = Rabbit.builder().id(1L).status(RabbitStatus.TRAINING)
                    .trainingEndTime(LocalDateTime.now().minusMinutes(1)).traits(new HashSet<>())
                    .secondaryStats(SecondaryStats.builder().strength(5f).agility(5f).intelligence(5f).build()).build();
            when(rabbitRepository.findByStatus(RabbitStatus.TRAINING)).thenReturn(List.of(expired));
            when(rabbitRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            structureService.processFinishedTrainings();

            assertEquals(RabbitStatus.RESTING, expired.getStatus());
            verify(rabbitRepository).save(expired);
        }
    }
}