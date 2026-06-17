package com.kodilla.bungenics.service;

import com.kodilla.bungenics.domain.adventure.Adventure;
import com.kodilla.bungenics.exception.ResourceNotFoundException;
import com.kodilla.bungenics.repository.AdventureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdventureService {

    private final AdventureRepository adventureRepository;

    public Adventure createAdventure(Adventure adventure) {
        return adventureRepository.save(adventure);
    }

    public Adventure getAdventureById(Long id) {
        return adventureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Adventure with id " + id + " not found"));
    }

    public List<Adventure> getAllAdventures() {
        return adventureRepository.findAll();
    }
}