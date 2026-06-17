package com.kodilla.bungenics.controller;

import com.kodilla.bungenics.domain.player.RabbitFarm;
import com.kodilla.bungenics.dto.RabbitFarmDto;
import com.kodilla.bungenics.mapper.RabbitFarmMapper;
import com.kodilla.bungenics.service.RabbitFarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/farms")
public class RabbitFarmController {

    private final RabbitFarmMapper rabbitFarmMapper;
    private final RabbitFarmService rabbitFarmService;

    @GetMapping
    public List<RabbitFarmDto> getFarms() {
        return rabbitFarmMapper.mapToRabbitFarmDtoList(rabbitFarmService.getAllRabbitFarms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RabbitFarmDto> getFarm(@PathVariable Long id) {
        return ResponseEntity.ok(rabbitFarmMapper.mapToRabbitFarmDto(rabbitFarmService.getRabbitFarmById(id)));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RabbitFarmDto> createFarm(@RequestBody RabbitFarmDto rabbitFarmDto) {
        RabbitFarm farm = rabbitFarmService.createRabbitFarm(rabbitFarmMapper.mapToRabbitFarm(rabbitFarmDto));
        return ResponseEntity.status(201).body(rabbitFarmMapper.mapToRabbitFarmDto(farm));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RabbitFarmDto> updateFarm(@PathVariable Long id, @RequestBody RabbitFarmDto rabbitFarmDto) {
        RabbitFarm farmDetails = rabbitFarmMapper.mapToRabbitFarm(rabbitFarmDto);
        RabbitFarm updatedFarm = rabbitFarmService.updateRabbitFarm(id, farmDetails);
        return ResponseEntity.ok(rabbitFarmMapper.mapToRabbitFarmDto(updatedFarm));
    }
}