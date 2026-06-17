package com.kodilla.bungenics.controller;

import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.dto.RabbitDto;
import com.kodilla.bungenics.mapper.RabbitMapper;
import com.kodilla.bungenics.service.RabbitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/rabbits")
public class RabbitController {
    private final RabbitMapper rabbitMapper;
    private final RabbitService rabbitService;

    @GetMapping(value = "/{rabbitId}")
    public ResponseEntity<RabbitDto> getRabbit(@PathVariable("rabbitId") Long rabbitId) {
        return ResponseEntity.ok(rabbitMapper.mapToRabbitDto(rabbitService.getRabbitById(rabbitId)));
    }

    @GetMapping
    public List<RabbitDto> getRabbits() {
        return rabbitMapper.mapToRabbitDtoList(rabbitService.getAllRabbits());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RabbitDto> addRabbit(@RequestBody RabbitDto rabbitDto) {
        Rabbit rabbit = rabbitService.createRabbit(rabbitMapper.mapToRabbit(rabbitDto));
        return ResponseEntity.status(201).body(rabbitMapper.mapToRabbitDto(rabbit));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RabbitDto> updateRabbit(@PathVariable Long id, @RequestBody RabbitDto rabbitDto) {
        Rabbit rabbitDetails = rabbitMapper.mapToRabbit(rabbitDto);
        Rabbit savedRabbit = rabbitService.updateRabbit(id, rabbitDetails);
        return ResponseEntity.ok(rabbitMapper.mapToRabbitDto(savedRabbit));
    }

    @DeleteMapping("/{rabbitId}")
    public ResponseEntity<Void> deleteRabbit(@PathVariable Long rabbitId) {
        rabbitService.deleteRabbit(rabbitId);
        return ResponseEntity.noContent().build();
    }
}
