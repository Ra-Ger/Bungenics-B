package com.kodilla.bungenics.controller;

import com.kodilla.bungenics.domain.rabbit.FeedingRecord;
import com.kodilla.bungenics.dto.FeedingDto;
import com.kodilla.bungenics.mapper.FeedingMapper;
import com.kodilla.bungenics.service.FeedingService;
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
    private final FeedingService feedingService;

    @GetMapping(value = "/{feedingRecordId}")
    public ResponseEntity<FeedingDto> getFeeding(@PathVariable("feedingRecordId") Long feedingId) {
        return ResponseEntity.ok(feedingMapper.mapToFeedingDto(feedingService.getFeedingRecordById(feedingId)));
    }

    @GetMapping
    public List<FeedingDto> getFeedings(@PathVariable Long rabbitId) {
        return feedingMapper.mapToFeedingDtoList(feedingService.getFeedingsForRabbit(rabbitId));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FeedingDto> addFeeding(@PathVariable Long rabbitId, @RequestBody FeedingDto feedingDto) {
        FeedingRecord feeding = feedingService.saveFeedingRecord(rabbitId, feedingMapper.mapToFeeding(feedingDto));
        return ResponseEntity.status(201).body(feedingMapper.mapToFeedingDto(feeding));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeedingDto> updateFeeding(@PathVariable Long rabbitId, @PathVariable Long id, @RequestBody FeedingDto feedingDto) {
        FeedingRecord feedingData = feedingMapper.mapToFeeding(new FeedingDto(id, rabbitId, feedingDto.getFoodType(), feedingDto.getAmount(), feedingDto.getFeedingTime()));
        FeedingRecord savedFeeding = feedingService.saveFeedingRecord(rabbitId, feedingData);
        return ResponseEntity.ok(feedingMapper.mapToFeedingDto(savedFeeding));
    }
}