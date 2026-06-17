package com.kodilla.bungenics.controller;

import com.kodilla.bungenics.domain.player.Player;
import com.kodilla.bungenics.dto.PlayerDto;
import com.kodilla.bungenics.mapper.PlayerMapper;
import com.kodilla.bungenics.service.PlayerService;
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
class PlayerControllerTestSuite {

    @Mock
    private PlayerMapper playerMapper;

    @Mock
    private PlayerService playerService;

    @InjectMocks
    private PlayerController controller;

    @Test
    void shouldGetAllPlayers() {
        Player player = new Player();
        PlayerDto dto = mock(PlayerDto.class);
        when(playerService.getAllPlayers()).thenReturn(List.of(player));
        when(playerMapper.mapToPlayerDtoList(anyList())).thenReturn(List.of(dto));

        List<PlayerDto> result = controller.getPlayers();
        assertEquals(1, result.size());
        verify(playerService).getAllPlayers();
    }

    @Test
    void shouldGetPlayerById() {
        Long id = 1L;
        Player player = new Player();
        PlayerDto dto = mock(PlayerDto.class);
        when(playerService.getPlayerById(id)).thenReturn(player);
        when(playerMapper.mapToPlayerDto(player)).thenReturn(dto);

        ResponseEntity<PlayerDto> response = controller.getPlayer(id);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void shouldCreatePlayer() {
        PlayerDto inputDto = mock(PlayerDto.class);
        Player mapped = new Player();
        Player created = new Player();
        PlayerDto outputDto = mock(PlayerDto.class);
        when(playerMapper.mapToPlayer(inputDto)).thenReturn(mapped);
        when(playerService.createPlayer(mapped)).thenReturn(created);
        when(playerMapper.mapToPlayerDto(created)).thenReturn(outputDto);

        ResponseEntity<PlayerDto> response = controller.createPlayer(inputDto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(outputDto, response.getBody());
        verify(playerService).createPlayer(mapped);
    }

    @Test
    void shouldUpdatePlayer() {
        Long id = 1L;
        PlayerDto inputDto = mock(PlayerDto.class);
        Player mapped = new Player();
        Player updated = new Player();
        PlayerDto outputDto = mock(PlayerDto.class);
        when(playerMapper.mapToPlayer(inputDto)).thenReturn(mapped);
        when(playerService.updatePlayer(id, mapped)).thenReturn(updated);
        when(playerMapper.mapToPlayerDto(updated)).thenReturn(outputDto);

        ResponseEntity<PlayerDto> response = controller.updatePlayer(id, inputDto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(outputDto, response.getBody());
        verify(playerService).updatePlayer(id, mapped);
    }

    @Test
    void shouldDeletePlayer() {
        Long id = 1L;
        doNothing().when(playerService).deletePlayer(id);

        ResponseEntity<Void> response = controller.deletePlayer(id);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(playerService).deletePlayer(id);
    }
}