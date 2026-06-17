// ---------- RabbitServiceTest ----------
package com.kodilla.bungenics.service;

import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.domain.rabbit.RabbitStatus;
import com.kodilla.bungenics.exception.ResourceNotFoundException;
import com.kodilla.bungenics.repository.RabbitRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitServiceTest {

    @Mock
    private RabbitRepository rabbitRepository;
    @InjectMocks
    private RabbitService rabbitService;

    @Nested
    class CreateRabbit {

        @Test
        void shouldSetSexWhenNull() {
            Rabbit rabbit = new Rabbit();
            rabbit.setSex(null);
            when(rabbitRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Rabbit result = rabbitService.createRabbit(rabbit);
            assertThat(result.getSex()).isIn("FEMALE", "MALE");
            assertThat(result.getStatus()).isEqualTo(RabbitStatus.IDLE);
        }

        @Test
        void shouldNotOverrideExistingSex() {
            Rabbit rabbit = new Rabbit();
            rabbit.setSex("MALE");
            when(rabbitRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Rabbit result = rabbitService.createRabbit(rabbit);
            assertThat(result.getSex()).isEqualTo("MALE");
        }

        @Test
        void shouldSetDefaultStatusWhenNull() {
            Rabbit rabbit = new Rabbit();
            rabbit.setStatus(null);
            when(rabbitRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Rabbit result = rabbitService.createRabbit(rabbit);
            assertThat(result.getStatus()).isEqualTo(RabbitStatus.IDLE);
        }
    }

    @Nested
    class GetRabbit {

        @Test
        void shouldReturnRabbitIfExists() {
            Rabbit rabbit = Rabbit.builder().id(1L).name("Fluffy").build();
            when(rabbitRepository.findById(1L)).thenReturn(Optional.of(rabbit));
            assertThat(rabbitService.getRabbitById(1L)).isSameAs(rabbit);
        }

        @Test
        void shouldThrowIfNotFound() {
            when(rabbitRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> rabbitService.getRabbitById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Test
    void shouldGetAllRabbits() {
        when(rabbitRepository.findAll()).thenReturn(List.of(new Rabbit(), new Rabbit()));
        assertThat(rabbitService.getAllRabbits()).hasSize(2);
    }

    @Test
    void shouldUpdateRabbitFields() {
        Rabbit existing = Rabbit.builder().id(1L).name("Old").breed("Lop").build();
        Rabbit details = Rabbit.builder()
                .name("New").breed("Angora").sex("FEMALE")
                .weight(2.5f).nutritionLevel(90f).life(95f).stress(5f)
                .status(RabbitStatus.RESTING).playerId(10L).build();

        when(rabbitRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(rabbitRepository.save(existing)).thenReturn(existing);

        Rabbit updated = rabbitService.updateRabbit(1L, details);
        assertThat(updated.getName()).isEqualTo("New");
        assertThat(updated.getBreed()).isEqualTo("Angora");
        assertThat(updated.getWeight()).isEqualTo(2.5f);
        assertThat(updated.getStatus()).isEqualTo(RabbitStatus.RESTING);
        verify(rabbitRepository).save(existing);
    }

    @Test
    void shouldRenameRabbit() {
        Rabbit rabbit = Rabbit.builder().id(1L).name("Old").build();
        when(rabbitRepository.findById(1L)).thenReturn(Optional.of(rabbit));

        rabbitService.renameRabbit(1L, "NewName");
        assertThat(rabbit.getName()).isEqualTo("NewName");
        verify(rabbitRepository).save(rabbit);
    }

    @Test
    void shouldNotRenameWithBlank() {
        Rabbit rabbit = Rabbit.builder().id(1L).name("Keep").build();
        when(rabbitRepository.findById(1L)).thenReturn(Optional.of(rabbit));

        rabbitService.renameRabbit(1L, "  ");
        assertThat(rabbit.getName()).isEqualTo("Keep");
        verify(rabbitRepository, never()).save(rabbit);
    }

    @Nested
    class UpdateRabbitStatus {

        @Test
        void shouldSetOnVetStatusAndEndTime() {
            Rabbit rabbit = Rabbit.builder().id(1L).status(RabbitStatus.IDLE).build();
            when(rabbitRepository.findById(1L)).thenReturn(Optional.of(rabbit));
            when(rabbitRepository.save(rabbit)).thenReturn(rabbit);

            rabbitService.updateRabbitStatus(1L, "ON_VET");
            assertThat(rabbit.getStatus()).isEqualTo(RabbitStatus.ON_VET);
            assertThat(rabbit.getVetEndTime()).isNotNull();
        }

        @Test
        void shouldHealWhenIdleAfterVet() {
            Rabbit rabbit = Rabbit.builder().id(1L)
                    .status(RabbitStatus.ON_VET)
                    .life(50f).stress(30f)
                    .vetEndTime(LocalDateTime.now().plusMinutes(5))
                    .build();
            // getMaxHp() returns 100f default
            when(rabbitRepository.findById(1L)).thenReturn(Optional.of(rabbit));
            when(rabbitRepository.save(rabbit)).thenReturn(rabbit);

            rabbitService.updateRabbitStatus(1L, "IDLE");
            assertThat(rabbit.getStatus()).isEqualTo(RabbitStatus.IDLE);
            assertThat(rabbit.getLife()).isEqualTo(100f);
            assertThat(rabbit.getStress()).isEqualTo(0.0f);
            assertThat(rabbit.getVetEndTime()).isNull();
        }

        @Test
        void shouldSetOtherStatusDirectly() {
            Rabbit rabbit = Rabbit.builder().id(1L).status(RabbitStatus.IDLE).build();
            when(rabbitRepository.findById(1L)).thenReturn(Optional.of(rabbit));
            when(rabbitRepository.save(rabbit)).thenReturn(rabbit);

            rabbitService.updateRabbitStatus(1L, "RESTING");
            assertThat(rabbit.getStatus()).isEqualTo(RabbitStatus.RESTING);
        }
    }

    @Test
    void shouldDeleteRabbit() {
        Rabbit rabbit = Rabbit.builder().id(1L).build();
        when(rabbitRepository.findById(1L)).thenReturn(Optional.of(rabbit));

        rabbitService.deleteRabbit(1L);
        verify(rabbitRepository).delete(rabbit);
    }
}