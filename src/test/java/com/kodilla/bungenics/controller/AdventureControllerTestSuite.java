package com.kodilla.bungenics.controller;

import com.kodilla.bungenics.domain.adventure.Adventure;
import com.kodilla.bungenics.dto.AdventureDto;
import com.kodilla.bungenics.mapper.AdventureMapper;
import com.kodilla.bungenics.service.AdventureService;
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
class AdventureControllerTestSuite {

    @Mock
    private AdventureMapper adventureMapper;

    @Mock
    private AdventureService adventureService;

    @InjectMocks
    private AdventureController controller;

    @Test
    void shouldGetAllAdventures() {
        Adventure adventure = new Adventure();
        AdventureDto adventureDto = mock(AdventureDto.class);
        when(adventureService.getAllAdventures()).thenReturn(List.of(adventure));
        when(adventureMapper.mapToAdventureDtoList(anyList())).thenReturn(List.of(adventureDto));

        List<AdventureDto> result = controller.getAdventures();
        assertEquals(1, result.size());
        verify(adventureService).getAllAdventures();
    }

    @Test
    void shouldGetAdventureById() {
        Long id = 1L;
        Adventure adventure = new Adventure();
        AdventureDto adventureDto = mock(AdventureDto.class);
        when(adventureService.getAdventureById(id)).thenReturn(adventure);
        when(adventureMapper.mapToAdventureDto(adventure)).thenReturn(adventureDto);

        ResponseEntity<AdventureDto> response = controller.getAdventure(id);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(adventureDto, response.getBody());
    }

    @Test
    void shouldCreateAdventure() {
        AdventureDto inputDto = mock(AdventureDto.class);
        Adventure mappedAdventure = new Adventure();
        Adventure createdAdventure = new Adventure();
        AdventureDto outputDto = mock(AdventureDto.class);
        when(adventureMapper.mapToAdventure(inputDto)).thenReturn(mappedAdventure);
        when(adventureService.createAdventure(mappedAdventure)).thenReturn(createdAdventure);
        when(adventureMapper.mapToAdventureDto(createdAdventure)).thenReturn(outputDto);

        ResponseEntity<AdventureDto> response = controller.createAdventure(inputDto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(outputDto, response.getBody());
        verify(adventureService).createAdventure(mappedAdventure);
    }

    @Test
    void shouldGetCompletedAdventuresForPlayer() {
        Long playerId = 100L;
        Adventure completed = new Adventure();
        completed.setPlayerId(playerId);
        completed.setStatus("COMPLETED");
        Adventure notCompleted = new Adventure();
        notCompleted.setPlayerId(playerId);
        notCompleted.setStatus("ACTIVE");
        Adventure otherPlayer = new Adventure();
        otherPlayer.setPlayerId(999L);
        otherPlayer.setStatus("COMPLETED");
        when(adventureService.getAllAdventures()).thenReturn(List.of(completed, notCompleted, otherPlayer));
        AdventureDto dto = mock(AdventureDto.class);
        when(adventureMapper.mapToAdventureDtoList(List.of(completed))).thenReturn(List.of(dto));

        ResponseEntity<List<AdventureDto>> response = controller.getCompletedAdventures(playerId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(adventureService).getAllAdventures();
        verify(adventureMapper).mapToAdventureDtoList(List.of(completed));
    }

    @Test
    void shouldSendRabbitOnAdventure() {
        Long playerId = 1L;
        Long rabbitId = 2L;
        String type = "EXPLORE";
        doNothing().when(adventureService).sendRabbitOnAdventure(playerId, rabbitId, type);

        ResponseEntity<Void> response = controller.sendRabbitOnAdventure(playerId, rabbitId, type);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(adventureService).sendRabbitOnAdventure(playerId, rabbitId, type);
    }
}