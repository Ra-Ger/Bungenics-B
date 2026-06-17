package com.kodilla.bungenics.service;

import com.kodilla.bungenics.domain.adventure.AdventureEvent;
import com.kodilla.bungenics.exception.ResourceNotFoundException;
import com.kodilla.bungenics.repository.AdventureEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdventureEventService {

    private final AdventureEventRepository adventureEventRepository;

    public AdventureEvent createEvent(AdventureEvent event) {
        return adventureEventRepository.save(event);
    }

    public AdventureEvent getEventById(Long id) {
        return adventureEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Adventure Event with id " + id + " not found"));
    }

    public List<AdventureEvent> getAllEvents() {
        return adventureEventRepository.findAll();
    }
}