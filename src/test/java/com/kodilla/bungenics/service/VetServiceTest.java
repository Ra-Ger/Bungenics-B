package com.kodilla.bungenics.service;

import com.kodilla.bungenics.domain.player.Player;
import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.domain.rabbit.RabbitStatus;
import com.kodilla.bungenics.exception.ResourceNotFoundException;
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
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VetServiceTest {

    @Mock
    private RabbitRepository rabbitRepository;
    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private VetService vetService;

    private Rabbit rabbit;
    private Player player;

    @BeforeEach
    void setUp() {
        rabbit = Rabbit.builder()
                .id(1L)
                .playerId(100L)
                .status(RabbitStatus.IDLE)
                .life(80f)
                .stress(10f)
                .build(); // domyślne maxHp = 100f

        player = new Player();
        player.setId(100L);
        player.setMoney(new BigDecimal("200.00"));
    }

    @Nested
    class GetMaxHp {
        @Test
        void shouldReturnDefaultWhenRabbitIsNull() {
            assertEquals(100.0f, vetService.getMaxHp(null));
        }

        @Test
        void shouldReturnRabbitsMaxHp() {
            // Zależne od implementacji getMaxHp() – domyślnie 100f
            assertEquals(100.0f, vetService.getMaxHp(rabbit));
        }
    }

    @Nested
    class AdmitToVet {

        @Test
        void shouldAdmitSuccessfullyAndDeductGold() {
            when(rabbitRepository.findById(1L)).thenReturn(Optional.of(rabbit));
            when(playerRepository.findById(100L)).thenReturn(Optional.of(player));
            when(rabbitRepository.save(any(Rabbit.class))).thenAnswer(inv -> inv.getArgument(0));

            Rabbit result = vetService.admitToVet(1L);

            assertEquals(RabbitStatus.ON_VET, result.getStatus());
            assertNotNull(result.getVetEndTime());
            // Koszt: 50 + (100-80)/2 = 60
            assertThat(player.getMoney()).isEqualByComparingTo(new BigDecimal("140.00"));
            verify(playerRepository).save(player);
            verify(rabbitRepository).save(rabbit);
        }

        @Test
        void shouldAdmitIfHealthyButStressed() {
            rabbit.setLife(100f);
            rabbit.setStress(30f);
            when(rabbitRepository.findById(1L)).thenReturn(Optional.of(rabbit));
            when(playerRepository.findById(100L)).thenReturn(Optional.of(player));
            when(rabbitRepository.save(any(Rabbit.class))).thenAnswer(inv -> inv.getArgument(0));

            Rabbit result = vetService.admitToVet(1L);
            assertEquals(RabbitStatus.ON_VET, result.getStatus());
            // Koszt: 50 + 0 = 50
            assertThat(player.getMoney()).isEqualByComparingTo(new BigDecimal("150.00"));
        }

        @Test
        void shouldThrowWhenRabbitIsCompletelyHealthy() {
            rabbit.setLife(100f);
            rabbit.setStress(0f);
            when(rabbitRepository.findById(1L)).thenReturn(Optional.of(rabbit));
            when(playerRepository.findById(100L)).thenReturn(Optional.of(player));

            assertThatThrownBy(() -> vetService.admitToVet(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("completely healthy");
            verify(rabbitRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenNotEnoughMoney() {
            player.setMoney(BigDecimal.TEN);
            when(rabbitRepository.findById(1L)).thenReturn(Optional.of(rabbit));
            when(playerRepository.findById(100L)).thenReturn(Optional.of(player));

            assertThatThrownBy(() -> vetService.admitToVet(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not enough money");
            verify(rabbitRepository, never()).save(any());
        }

        @Test
        void shouldThrowWhenRabbitNotFound() {
            when(rabbitRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> vetService.admitToVet(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void shouldThrowWhenPlayerNotFound() {
            when(rabbitRepository.findById(1L)).thenReturn(Optional.of(rabbit));
            when(playerRepository.findById(100L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> vetService.admitToVet(1L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class DischargeFromVet {

        @Test
        void shouldFullyHealAndSetIdle() {
            rabbit.setStatus(RabbitStatus.ON_VET);
            rabbit.setLife(50f);
            rabbit.setStress(70f);
            rabbit.setVetEndTime(LocalDateTime.now().plusMinutes(5));
            when(rabbitRepository.findById(1L)).thenReturn(Optional.of(rabbit));
            when(rabbitRepository.save(any(Rabbit.class))).thenAnswer(inv -> inv.getArgument(0));

            Rabbit result = vetService.dischargeFromVet(1L);

            assertEquals(100.0f, result.getLife()); // maxHp = 100f
            assertEquals(0.0f, result.getStress());
            assertNull(result.getVetEndTime());
            assertEquals(RabbitStatus.IDLE, result.getStatus());
            verify(rabbitRepository).save(rabbit);
        }

        @Test
        void shouldThrowWhenRabbitNotFound() {
            when(rabbitRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> vetService.dischargeFromVet(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}