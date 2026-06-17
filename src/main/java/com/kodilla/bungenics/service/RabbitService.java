package com.kodilla.bungenics.service;

import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.domain.rabbit.RabbitStatus;
import com.kodilla.bungenics.exception.ResourceNotFoundException;
import com.kodilla.bungenics.repository.RabbitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RabbitService {

    private final RabbitRepository rabbitRepository;

    @Transactional
    public Rabbit createRabbit(Rabbit rabbit) {
        if (rabbit.getSex() == null || rabbit.getSex().isBlank()) {
            rabbit.setSex(Math.random() > 0.5 ? "FEMALE" : "MALE");
        }
        if (rabbit.getStatus() == null) {
            rabbit.setStatus(RabbitStatus.IDLE);
        }
        return rabbitRepository.save(rabbit);
    }

    public Rabbit getRabbitById(Long id) {
        return rabbitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rabbit with id " + id + " not found"));
    }

    public List<Rabbit> getAllRabbits() {
        return rabbitRepository.findAll();
    }

    @Transactional
    public Rabbit updateRabbit(Long id, Rabbit rabbitDetails) {
        Rabbit existing = getRabbitById(id);
        existing.setName(rabbitDetails.getName());
        existing.setBreed(rabbitDetails.getBreed());
        if (rabbitDetails.getSex() != null) existing.setSex(rabbitDetails.getSex());
        if (rabbitDetails.getWeight() != null) existing.setWeight(rabbitDetails.getWeight());
        if (rabbitDetails.getNutritionLevel() != null) existing.setNutritionLevel(rabbitDetails.getNutritionLevel());
        if (rabbitDetails.getLife() != null) existing.setLife(rabbitDetails.getLife());
        if (rabbitDetails.getStress() != null) existing.setStress(rabbitDetails.getStress());
        if (rabbitDetails.getStatus() != null) existing.setStatus(rabbitDetails.getStatus());
        if (rabbitDetails.getPlayerId() != null) existing.setPlayerId(rabbitDetails.getPlayerId());
        return rabbitRepository.save(existing);
    }

    @Transactional
    public void renameRabbit(Long id, String newName) {
        Rabbit existing = getRabbitById(id);
        if (newName != null && !newName.isBlank()) {
            existing.setName(newName.trim());
            rabbitRepository.save(existing);
        }
    }

    @Transactional
    public void updateRabbitStatus(Long id, String statusStr) {
        Rabbit rabbit = getRabbitById(id);
        RabbitStatus newStatus = RabbitStatus.valueOf(statusStr.toUpperCase());

        if (newStatus == RabbitStatus.ON_VET) {
            rabbit.setStatus(RabbitStatus.ON_VET);
            rabbit.setVetEndTime(java.time.LocalDateTime.now().plusMinutes(5));
        } else if (newStatus == RabbitStatus.IDLE) {
            if (RabbitStatus.ON_VET.equals(rabbit.getStatus()) || rabbit.getVetEndTime() != null) {
                rabbit.setLife(rabbit.getMaxHp());
                rabbit.setStress(0.0f);
                rabbit.setVetEndTime(null);
            }
            rabbit.setStatus(newStatus);
        } else {
            rabbit.setStatus(newStatus);
        }
        rabbitRepository.save(rabbit);
    }

    @Transactional
    public void deleteRabbit(Long id) {
        Rabbit rabbit = getRabbitById(id);
        rabbitRepository.delete(rabbit);
    }
}