package com.kodilla.bungenics.controller;

import com.kodilla.bungenics.domain.adventure.Adventure;
import com.kodilla.bungenics.dto.AdventureDto;
import com.kodilla.bungenics.mapper.AdventureMapper;
import com.kodilla.bungenics.service.AdventureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/adventures")
public class AdventureController {

    private final AdventureMapper adventureMapper;
    private final AdventureService adventureService;

    @GetMapping
    public List<AdventureDto> getAdventures() {
        return adventureMapper.mapToAdventureDtoList(adventureService.getAllAdventures());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdventureDto> getAdventure(@PathVariable Long id) {
        return ResponseEntity.ok(adventureMapper.mapToAdventureDto(adventureService.getAdventureById(id)));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdventureDto> createAdventure(@RequestBody AdventureDto adventureDto) {
        Adventure adventure = adventureService.createAdventure(adventureMapper.mapToAdventure(adventureDto));
        return ResponseEntity.status(201).body(adventureMapper.mapToAdventureDto(adventure));
    }

    @GetMapping("/completed/{playerId}")
    public ResponseEntity<List<AdventureDto>> getCompletedAdventures(@PathVariable Long playerId) {
        List<Adventure> completed = adventureService.getAllAdventures().stream()
                .filter(a -> playerId.equals(a.getPlayerId()) && "COMPLETED".equals(a.getStatus()))
                .toList();
        return ResponseEntity.ok(adventureMapper.mapToAdventureDtoList(completed));
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendRabbitOnAdventure(
            @RequestParam Long playerId,
            @RequestParam Long rabbitId,
            @RequestParam String type) {
        adventureService.sendRabbitOnAdventure(playerId, rabbitId, type);
        return ResponseEntity.ok().build();
    }
}