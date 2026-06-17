package com.kodilla.bungenics.controller;

import com.kodilla.bungenics.domain.adventure.AdventureEvent;
import com.kodilla.bungenics.dto.AdventureEventDto;
import com.kodilla.bungenics.mapper.AdventureEventMapper;
import com.kodilla.bungenics.service.AdventureEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/adventure-events")
public class AdventureEventController {

    private final AdventureEventMapper eventMapper;
    private final AdventureEventService eventService;

    @GetMapping
    public List<AdventureEventDto> getEvents() {
        return eventMapper.mapToAdventureEventDtoList(eventService.getAllEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdventureEventDto> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventMapper.mapToAdventureEventDto(eventService.getEventById(id)));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdventureEventDto> createEvent(@RequestBody AdventureEventDto eventDto) {
        AdventureEvent event = eventService.createEvent(eventMapper.mapToAdventureEvent(eventDto));
        return ResponseEntity.status(201).body(eventMapper.mapToAdventureEventDto(event));
    }
}