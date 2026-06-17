package com.kodilla.bungenics.controller;

import com.kodilla.bungenics.domain.player.RabbitFarm;
import com.kodilla.bungenics.dto.RabbitFarmDto;
import com.kodilla.bungenics.mapper.RabbitFarmMapper;
import com.kodilla.bungenics.service.RabbitFarmService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitFarmControllerTestSuite {

    @Mock
    private RabbitFarmMapper rabbitFarmMapper;

    @Mock
    private RabbitFarmService rabbitFarmService;

    @InjectMocks
    private RabbitFarmController controller;

    @Test
    void shouldGetAllFarms() {
        RabbitFarm farm = new RabbitFarm();
        RabbitFarmDto dto = mock(RabbitFarmDto.class);
        when(rabbitFarmService.getAllRabbitFarms()).thenReturn(List.of(farm));
        when(rabbitFarmMapper.mapToRabbitFarmDtoList(anyList())).thenReturn(List.of(dto));

        List<RabbitFarmDto> result = controller.getFarms();
        assertEquals(1, result.size());
        verify(rabbitFarmService).getAllRabbitFarms();
    }

    @Test
    void shouldGetFarmById() {
        Long id = 1L;
        RabbitFarm farm = new RabbitFarm();
        RabbitFarmDto dto = mock(RabbitFarmDto.class);
        when(rabbitFarmService.getRabbitFarmById(id)).thenReturn(farm);
        when(rabbitFarmMapper.mapToRabbitFarmDto(farm)).thenReturn(dto);

        ResponseEntity<RabbitFarmDto> response = controller.getFarm(id);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void shouldCreateFarm() {
        RabbitFarmDto inputDto = mock(RabbitFarmDto.class);
        RabbitFarm mapped = new RabbitFarm();
        RabbitFarm created = new RabbitFarm();
        RabbitFarmDto outputDto = mock(RabbitFarmDto.class);
        when(rabbitFarmMapper.mapToRabbitFarm(inputDto)).thenReturn(mapped);
        when(rabbitFarmService.createRabbitFarm(mapped)).thenReturn(created);
        when(rabbitFarmMapper.mapToRabbitFarmDto(created)).thenReturn(outputDto);

        ResponseEntity<RabbitFarmDto> response = controller.createFarm(inputDto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(outputDto, response.getBody());
        verify(rabbitFarmService).createRabbitFarm(mapped);
    }

    @Test
    void shouldUpdateFarm() {
        Long id = 1L;
        RabbitFarmDto inputDto = mock(RabbitFarmDto.class);
        RabbitFarm mapped = new RabbitFarm();
        RabbitFarm updated = new RabbitFarm();
        RabbitFarmDto outputDto = mock(RabbitFarmDto.class);
        when(rabbitFarmMapper.mapToRabbitFarm(inputDto)).thenReturn(mapped);
        when(rabbitFarmService.updateRabbitFarm(id, mapped)).thenReturn(updated);
        when(rabbitFarmMapper.mapToRabbitFarmDto(updated)).thenReturn(outputDto);

        ResponseEntity<RabbitFarmDto> response = controller.updateFarm(id, inputDto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(outputDto, response.getBody());
        verify(rabbitFarmService).updateRabbitFarm(id, mapped);
    }
}