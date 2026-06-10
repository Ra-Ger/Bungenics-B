package com.kodilla.rabbitfeeding.controller;

import com.kodilla.rabbitfeeding.domain.FeedingRecord;
import com.kodilla.rabbitfeeding.domain.Rabbit;
import com.kodilla.rabbitfeeding.dto.FeedingDto;
import com.kodilla.rabbitfeeding.dto.RabbitDto;
import com.kodilla.rabbitfeeding.mapper.FeedingMapper;
import com.kodilla.rabbitfeeding.mapper.RabbitMapper;
import com.kodilla.rabbitfeeding.service.RabbitFeedingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/rabbits/{rabbitId}/feedings")
public class FeedingController {

    private final FeedingMapper feedingMapper;
    private final RabbitFeedingService rabbitFeedingService;

    @GetMapping(value = "/{feedingRecordId}")
    public ResponseEntity<FeedingDto> getFeeding(@PathVariable("feedingRecordId") Long feedingId) {
        return ResponseEntity.ok(feedingMapper.mapToFeedingDto(rabbitFeedingService.getFeedingRecordById(feedingId)));
    }

    @GetMapping
    public List<FeedingDto> getFeedings() {
        return feedingMapper.mapToFeedingDtoList(rabbitFeedingService.getAllFeedingRecords());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FeedingDto> addFeeding(@RequestBody FeedingDto feedingDto) {
        FeedingRecord feeding = rabbitFeedingService.addFeedingRecord(feedingDto.getId(),feedingMapper.mapToFeeding(feedingDto));
        return ResponseEntity.status(201).body(feedingMapper.mapToFeedingDto(feeding));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeedingDto> updateFeeding(@PathVariable Long id, @RequestBody FeedingDto feedingDto) {
        FeedingRecord feeding = feedingMapper.mapToFeeding(new FeedingDto(id,feedingDto.getRabbit(),feedingDto.getFoodType(), feedingDto.getAmount(),feedingDto.getFeedingTime()));
        FeedingRecord savedFeeding = rabbitFeedingService.addFeedingRecord(feeding.getRabbit().getId(),feeding);
        return ResponseEntity.ok(feedingMapper.mapToFeedingDto(savedFeeding));
    }
}
