package com.kodilla.bungenics.service;

import com.kodilla.bungenics.domain.adventure.Adventure;
import com.kodilla.bungenics.domain.adventure.AdventuresRecord;
import com.kodilla.bungenics.domain.player.Player;
import com.kodilla.bungenics.domain.player.RabbitFarm;
import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.domain.rabbitFarm.Room;
import com.kodilla.bungenics.domain.rabbitFarm.Structure;
import com.kodilla.bungenics.exception.ResourceNotFoundException;
import com.kodilla.bungenics.repository.AdventureRepository;
import com.kodilla.bungenics.repository.AdventuresRecordRepository;
import com.kodilla.bungenics.repository.PlayerRepository;
import com.kodilla.bungenics.repository.RabbitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock private PlayerRepository playerRepository;
    @Mock private AdventureRepository adventureRepository;
    @Mock private AdventuresRecordRepository adventuresRecordRepository;
    @Mock private RabbitRepository rabbitRepository; // nieużywane bezpośrednio, ale wymagane przez konstruktor

    @InjectMocks
    private PlayerService playerService;

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player();
        player.setId(1L);
        player.setName("TestPlayer");
        player.setLocation("Warsaw");
        player.setMoney(BigDecimal.valueOf(200));
    }

    @Nested
    class CreatePlayer {

        @Test
        void shouldSetDefaultMoneyWhenNull() {
            Player newPlayer = new Player();
            newPlayer.setMoney(null);
            when(playerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Player result = playerService.createPlayer(newPlayer);
            assertThat(result.getMoney()).isEqualByComparingTo(BigDecimal.valueOf(100));
            verify(playerRepository).save(newPlayer);
        }

        @Test
        void shouldKeepExistingMoney() {
            player.setMoney(BigDecimal.valueOf(300));
            when(playerRepository.save(any())).thenReturn(player);

            Player result = playerService.createPlayer(player);
            assertThat(result.getMoney()).isEqualByComparingTo(BigDecimal.valueOf(300));
        }
    }

    @Nested
    class GetPlayer {

        @Test
        void shouldReturnPlayerIfExists() {
            when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
            assertThat(playerService.getPlayerById(1L)).isSameAs(player);
        }

        @Test
        void shouldThrowIfNotFound() {
            when(playerRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> playerService.getPlayerById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Test
    void shouldGetAllPlayers() {
        when(playerRepository.findAll()).thenReturn(List.of(player, new Player()));
        assertThat(playerService.getAllPlayers()).hasSize(2);
    }

    @Nested
    class UpdatePlayer {

        @Test
        void shouldUpdateNameLocationAndMoney() {
            Player details = new Player();
            details.setName("NewName");
            details.setLocation("Berlin");
            details.setMoney(BigDecimal.valueOf(500));

            when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
            when(playerRepository.save(player)).thenReturn(player);

            Player updated = playerService.updatePlayer(1L, details);
            assertThat(updated.getName()).isEqualTo("NewName");
            assertThat(updated.getLocation()).isEqualTo("Berlin");
            assertThat(updated.getMoney()).isEqualByComparingTo(BigDecimal.valueOf(500));
            verify(playerRepository).save(player);
        }

        @Test
        void shouldNotUpdateMoneyIfNull() {
            Player details = new Player();
            details.setName("NewName");
            details.setMoney(null);
            player.setMoney(BigDecimal.valueOf(200));

            when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
            when(playerRepository.save(player)).thenReturn(player);

            Player updated = playerService.updatePlayer(1L, details);
            assertThat(updated.getMoney()).isEqualByComparingTo(BigDecimal.valueOf(200));
        }
    }

    @Nested
    class DeletePlayer {

        private RabbitFarm farm;
        private Structure structure;
        private Room room;
        private Rabbit rabbit;

        @BeforeEach
        void setUpFarmStructure() {
            // build a sample farm with structure -> room -> rabbits
            rabbit = Rabbit.builder().id(10L).build();
            room = Room.builder().id(100L).rabbits(new ArrayList<>(List.of(rabbit))).build();
            structure = Structure.builder().id(1000L).rooms(new ArrayList<>(List.of(room))).build();
            farm = RabbitFarm.builder().id(5000L).structures(new ArrayList<>(List.of(structure))).build();
            player.setRabbitFarm(farm);
        }

        @Test
        void shouldDeletePlayerAndCascadeAdventuresAndRecords() {
            // given
            when(playerRepository.findById(1L)).thenReturn(Optional.of(player));

            Adventure adventure = mock(Adventure.class);
            when(adventure.getPlayerId()).thenReturn(1L);
            when(adventureRepository.findAll()).thenReturn(List.of(adventure));

            AdventuresRecord record = mock(AdventuresRecord.class);
            when(adventuresRecordRepository.findByRabbitId(10L)).thenReturn(Optional.of(record));

            // when
            playerService.deletePlayer(1L);

            // then
            verify(adventureRepository).delete(adventure);
            verify(adventuresRecordRepository).findByRabbitId(10L);
            verify(adventuresRecordRepository).delete(record);
            verify(playerRepository).delete(player);
        }

        @Test
        void shouldHandleNoFarmGracefully() {
            player.setRabbitFarm(null);
            when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
            when(adventureRepository.findAll()).thenReturn(List.of());

            assertThatCode(() -> playerService.deletePlayer(1L)).doesNotThrowAnyException();
            verify(playerRepository).delete(player);
        }

        @Test
        void shouldThrowIfPlayerNotFound() {
            when(playerRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> playerService.deletePlayer(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(playerRepository, never()).delete(any());
        }
    }
}