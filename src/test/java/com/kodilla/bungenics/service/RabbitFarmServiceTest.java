// ---------- RabbitFarmServiceTest ----------
package com.kodilla.bungenics.service;

import com.kodilla.bungenics.domain.player.RabbitFarm;
import com.kodilla.bungenics.exception.ResourceNotFoundException;
import com.kodilla.bungenics.repository.RabbitFarmRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitFarmServiceTest {

    @Mock
    private RabbitFarmRepository rabbitFarmRepository;
    @InjectMocks
    private RabbitFarmService rabbitFarmService;

    @Test
    void shouldCreateFarmAndSetDefaultFoodAmounts() {
        RabbitFarm farm = new RabbitFarm();
        when(rabbitFarmRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RabbitFarm result = rabbitFarmService.createRabbitFarm(farm);
        assertThat(result.getHayAmount()).isEqualTo(0f);
        assertThat(result.getSpinachAmount()).isEqualTo(0f);
        assertThat(result.getCarrotAmount()).isEqualTo(0f);
        assertThat(result.getLettuceAmount()).isEqualTo(0f);
        verify(rabbitFarmRepository).save(farm);
    }

    @Test
    void shouldNotOverrideExistingFoodAmounts() {
        RabbitFarm farm = new RabbitFarm();
        farm.setCarrotAmount(50f);
        when(rabbitFarmRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RabbitFarm result = rabbitFarmService.createRabbitFarm(farm);
        assertThat(result.getCarrotAmount()).isEqualTo(50f);
        assertThat(result.getHayAmount()).isEqualTo(0f); // null -> 0
    }

    @Test
    void shouldGetFarmById() {
        RabbitFarm farm = RabbitFarm.builder().id(1L).build();
        when(rabbitFarmRepository.findById(1L)).thenReturn(Optional.of(farm));
        assertThat(rabbitFarmService.getRabbitFarmById(1L)).isSameAs(farm);
    }

    @Test
    void shouldThrowWhenFarmNotFound() {
        when(rabbitFarmRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> rabbitFarmService.getRabbitFarmById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldGetAllFarms() {
        when(rabbitFarmRepository.findAll()).thenReturn(List.of(new RabbitFarm(), new RabbitFarm()));
        assertThat(rabbitFarmService.getAllRabbitFarms()).hasSize(2);
    }

    @Test
    void shouldUpdateFarmFoodAmounts() {
        RabbitFarm existing = RabbitFarm.builder().id(1L).hayAmount(10f).spinachAmount(20f)
                .carrotAmount(30f).lettuceAmount(40f).build();
        RabbitFarm details = RabbitFarm.builder().hayAmount(100f).spinachAmount(200f)
                .carrotAmount(300f).lettuceAmount(400f).build();

        when(rabbitFarmRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(rabbitFarmRepository.save(existing)).thenReturn(existing);

        RabbitFarm updated = rabbitFarmService.updateRabbitFarm(1L, details);
        assertThat(updated.getHayAmount()).isEqualTo(100f);
        assertThat(updated.getSpinachAmount()).isEqualTo(200f);
        assertThat(updated.getCarrotAmount()).isEqualTo(300f);
        assertThat(updated.getLettuceAmount()).isEqualTo(400f);
        verify(rabbitFarmRepository).save(existing);
    }
}