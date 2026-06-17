package com.kodilla.bungenics.service;

import com.kodilla.bungenics.domain.player.RabbitFarm;
import com.kodilla.bungenics.exception.ResourceNotFoundException;
import com.kodilla.bungenics.repository.RabbitFarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RabbitFarmService {

    private final RabbitFarmRepository rabbitFarmRepository;

    public RabbitFarm createRabbitFarm(RabbitFarm rabbitFarm) {
        if(rabbitFarm.getHayAmount() == null) rabbitFarm.setHayAmount(0f);
        if(rabbitFarm.getSpinachAmount() == null) rabbitFarm.setSpinachAmount(0f);
        if(rabbitFarm.getCarrotAmount() == null) rabbitFarm.setCarrotAmount(0f);
        if(rabbitFarm.getLettuceAmount() == null) rabbitFarm.setLettuceAmount(0f);

        return rabbitFarmRepository.save(rabbitFarm);
    }

    public RabbitFarm getRabbitFarmById(Long id) {
        return rabbitFarmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farm with id " + id + " not found"));
    }

    public List<RabbitFarm> getAllRabbitFarms() {
        return rabbitFarmRepository.findAll();
    }

    public RabbitFarm updateRabbitFarm(Long id, RabbitFarm farmDetails) {
        RabbitFarm existingFarm = getRabbitFarmById(id);

        existingFarm.setHayAmount(farmDetails.getHayAmount());
        existingFarm.setSpinachAmount(farmDetails.getSpinachAmount());
        existingFarm.setCarrotAmount(farmDetails.getCarrotAmount());
        existingFarm.setLettuceAmount(farmDetails.getLettuceAmount());

        return rabbitFarmRepository.save(existingFarm);
    }
}