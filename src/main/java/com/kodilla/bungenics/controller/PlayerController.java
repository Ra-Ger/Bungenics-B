package com.kodilla.bungenics.controller;

import com.kodilla.bungenics.domain.player.Player;
import com.kodilla.bungenics.dto.PlayerDto;
import com.kodilla.bungenics.mapper.PlayerMapper;
import com.kodilla.bungenics.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/players")
public class PlayerController {

    private final PlayerMapper playerMapper;
    private final PlayerService playerService;

    @GetMapping
    public List<PlayerDto> getPlayers() {
        return playerMapper.mapToPlayerDtoList(playerService.getAllPlayers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerDto> getPlayer(@PathVariable Long id) {
        return ResponseEntity.ok(playerMapper.mapToPlayerDto(playerService.getPlayerById(id)));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PlayerDto> createPlayer(@RequestBody PlayerDto playerDto) {
        Player player = playerService.createPlayer(playerMapper.mapToPlayer(playerDto));
        return ResponseEntity.status(201).body(playerMapper.mapToPlayerDto(player));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlayerDto> updatePlayer(@PathVariable Long id, @RequestBody PlayerDto playerDto) {
        Player playerDetails = playerMapper.mapToPlayer(playerDto);
        Player updatedPlayer = playerService.updatePlayer(id, playerDetails);
        return ResponseEntity.ok(playerMapper.mapToPlayerDto(updatedPlayer));
    }
}
