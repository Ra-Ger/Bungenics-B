package com.kodilla.rabbitfeeding.controller;

import com.kodilla.rabbitfeeding.domain.Rabbit;
import com.kodilla.rabbitfeeding.dto.RabbitDto;
import com.kodilla.rabbitfeeding.mapper.RabbitMapper;
import com.kodilla.rabbitfeeding.service.RabbitFeedingService;
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
    private final RabbitFeedingService rabbitFeedingService;

    @GetMapping(value = "/{rabbitId}")
    public ResponseEntity<RabbitDto> getRabbit(@PathVariable("rabbitId") Long rabbitId) {
        return ResponseEntity.ok(rabbitMapper.mapToRabbitDto(rabbitFeedingService.getRabbitById(rabbitId)));
    }

    @GetMapping
    public List<RabbitDto> getRabbits() {
        return rabbitMapper.mapToRabbitDtoList(rabbitFeedingService.getAllRabbits());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RabbitDto> addRabbit(@RequestBody RabbitDto rabbitDto) {
        Rabbit rabbit = rabbitFeedingService.createRabbit(rabbitMapper.mapToRabbit(rabbitDto));
        return ResponseEntity.status(201).body(rabbitMapper.mapToRabbitDto(rabbit));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RabbitDto> updateRabbit(@PathVariable Long id, @RequestBody RabbitDto rabbitDto) {
        Rabbit rabbit = rabbitMapper.mapToRabbit(new RabbitDto(id,rabbitDto.getName(),rabbitDto.getBreed(), rabbitDto.getWeight()));
        Rabbit savedRabbit = rabbitFeedingService.createRabbit(rabbit);
        return ResponseEntity.ok(rabbitMapper.mapToRabbitDto(savedRabbit));
    }
}
