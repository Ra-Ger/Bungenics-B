package com.kodilla.bungenics.service;

import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.exception.ResourceNotFoundException;
import com.kodilla.bungenics.repository.RabbitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RabbitService {

    private final RabbitRepository rabbitRepository;

    public Rabbit createRabbit(Rabbit rabbit) {
        if (rabbit.getLife() == null) rabbit.setLife(100f);
        if (rabbit.getStress() == null) rabbit.setStress(0f);
        if (rabbit.getNutritionLevel() == null) rabbit.setNutritionLevel(100f);
        if (rabbit.getAge() == null) rabbit.setAge(0f);
        if (rabbit.getStatus() == null) rabbit.setStatus("IDLE");

        return rabbitRepository.save(rabbit);
    }

    public Rabbit updateRabbit(Long id, Rabbit rabbitDetails) {
        Rabbit existingRabbit = getRabbitById(id);

        existingRabbit.setName(rabbitDetails.getName());
        existingRabbit.setBreed(rabbitDetails.getBreed());
        existingRabbit.setWeight(rabbitDetails.getWeight());

        return rabbitRepository.save(existingRabbit);
    }

    public Rabbit getRabbitById(long rabbitId) {
        return rabbitRepository.findById(rabbitId)
                .orElseThrow(() -> new ResourceNotFoundException("Rabbit with id " + rabbitId + " not found"));
    }

    public List<Rabbit> getAllRabbits() {
        return rabbitRepository.findAll();
    }

    public void deleteRabbit(Long id) {
        rabbitRepository.deleteById(id);
    }
}