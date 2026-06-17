package com.kodilla.bungenics.service;

import com.kodilla.bungenics.domain.rabbitFarm.Structure;
import com.kodilla.bungenics.exception.ResourceNotFoundException;
import com.kodilla.bungenics.repository.StructureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StructureService {

    private final StructureRepository structureRepository;

    public Structure createStructure(Structure structure) {
        return structureRepository.save(structure);
    }

    public Structure getStructureById(Long id) {
        return structureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Structure with id " + id + " not found"));
    }

    public List<Structure> getAllStructures() {
        return structureRepository.findAll();
    }

    public Structure updateStructure(Long id, Structure structureDetails) {
        Structure existingStructure = getStructureById(id);
        existingStructure.setSlots(structureDetails.getSlots());
        existingStructure.setStructureType(structureDetails.getStructureType());
        return structureRepository.save(existingStructure);
    }

    public void deleteStructure(Long id) {
        structureRepository.deleteById(id);
    }
}