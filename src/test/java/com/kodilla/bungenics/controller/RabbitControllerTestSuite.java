package com.kodilla.bungenics.controller;

import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.dto.RabbitDto;
import com.kodilla.bungenics.mapper.RabbitMapper;
import com.kodilla.bungenics.service.RabbitService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitControllerTestSuite {

    @Mock
    private RabbitMapper rabbitMapper;

    @Mock
    private RabbitService rabbitService;

    @InjectMocks
    private RabbitController controller;

    @Test
    void shouldGetRabbitById() {
        Long rabbitId = 1L;
        Rabbit rabbit = new Rabbit();
        RabbitDto dto = mock(RabbitDto.class);
        when(rabbitService.getRabbitById(rabbitId)).thenReturn(rabbit);
        when(rabbitMapper.mapToRabbitDto(rabbit)).thenReturn(dto);

        ResponseEntity<RabbitDto> response = controller.getRabbit(rabbitId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void shouldGetAllRabbits() {
        Rabbit rabbit = new Rabbit();
        RabbitDto dto = mock(RabbitDto.class);
        when(rabbitService.getAllRabbits()).thenReturn(List.of(rabbit));
        when(rabbitMapper.mapToRabbitDtoList(anyList())).thenReturn(List.of(dto));

        List<RabbitDto> result = controller.getRabbits();
        assertEquals(1, result.size());
        verify(rabbitService).getAllRabbits();
    }

    @Test
    void shouldAddRabbit() {
        RabbitDto inputDto = mock(RabbitDto.class);
        Rabbit mapped = new Rabbit();
        Rabbit created = new Rabbit();
        RabbitDto outputDto = mock(RabbitDto.class);
        when(rabbitMapper.mapToRabbit(inputDto)).thenReturn(mapped);
        when(rabbitService.createRabbit(mapped)).thenReturn(created);
        when(rabbitMapper.mapToRabbitDto(created)).thenReturn(outputDto);

        ResponseEntity<RabbitDto> response = controller.addRabbit(inputDto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(outputDto, response.getBody());
        verify(rabbitService).createRabbit(mapped);
    }

    @Test
    void shouldUpdateRabbit() {
        Long id = 1L;
        RabbitDto inputDto = mock(RabbitDto.class);
        Rabbit mapped = new Rabbit();
        Rabbit saved = new Rabbit();
        RabbitDto outputDto = mock(RabbitDto.class);
        when(rabbitMapper.mapToRabbit(inputDto)).thenReturn(mapped);
        when(rabbitService.updateRabbit(id, mapped)).thenReturn(saved);
        when(rabbitMapper.mapToRabbitDto(saved)).thenReturn(outputDto);

        ResponseEntity<RabbitDto> response = controller.updateRabbit(id, inputDto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(outputDto, response.getBody());
        verify(rabbitService).updateRabbit(id, mapped);
    }

    @Test
    void shouldUpdateRabbitStatus() {
        Long id = 1L;
        String status = "RESTING";

        doNothing().when(rabbitService).updateRabbitStatus(id, status);

        ResponseEntity<Void> response = controller.updateRabbitStatus(id, status);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(rabbitService).updateRabbitStatus(id, status);
    }

    @Test
    void shouldDeleteRabbit() {
        Long rabbitId = 1L;
        doNothing().when(rabbitService).deleteRabbit(rabbitId);

        ResponseEntity<Void> response = controller.deleteRabbit(rabbitId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(rabbitService).deleteRabbit(rabbitId);
    }
}