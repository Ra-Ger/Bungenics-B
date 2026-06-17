package com.kodilla.bungenics.service;

import com.kodilla.bungenics.domain.player.Player;
import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.domain.rabbit.RabbitStatus;
import com.kodilla.bungenics.exception.ResourceNotFoundException;
import com.kodilla.bungenics.repository.PlayerRepository;
import com.kodilla.bungenics.repository.RabbitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VetService {

    private final RabbitRepository rabbitRepository;
    private final PlayerRepository playerRepository;

    public float getMaxHp(Rabbit rabbit) {
        if (rabbit == null) return 100.0f;
        return rabbit.getMaxHp();
    }

    @Transactional
    public Rabbit admitToVet(Long rabbitId) {
        Rabbit rabbit = rabbitRepository.findById(rabbitId)
                .orElseThrow(() -> new ResourceNotFoundException("Rabbit not found with id: " + rabbitId));

        Player player = playerRepository.findById(rabbit.getPlayerId())
                .orElseThrow(() -> new ResourceNotFoundException("Player not found"));

        float maxHp = rabbit.getMaxHp();
        float currentHp = rabbit.getLife() != null ? rabbit.getLife() : maxHp;

        if (currentHp >= maxHp && (rabbit.getStress() == null || rabbit.getStress() <= 0.0f)) {
            throw new IllegalStateException("Rabbit is completely healthy and not stressed!");
        }
        
        double cost = 50.0 + (maxHp - currentHp) / 2.0;
        BigDecimal costBd = BigDecimal.valueOf(cost);

        if (player.getMoney().compareTo(costBd) < 0) {
            throw new IllegalStateException("Not enough money! Required: " + costBd + " Gold.");
        }

        player.setMoney(player.getMoney().subtract(costBd));
        playerRepository.save(player);

        rabbit.setStatus(RabbitStatus.ON_VET);
        rabbit.setVetEndTime(LocalDateTime.now().plusMinutes(5));

        return rabbitRepository.save(rabbit);
    }

    @Transactional
    public Rabbit dischargeFromVet(Long rabbitId) {
        Rabbit rabbit = rabbitRepository.findById(rabbitId)
                .orElseThrow(() -> new ResourceNotFoundException("Rabbit not found with id: " + rabbitId));

        rabbit.setLife(rabbit.getMaxHp());
        rabbit.setStress(0.0f);
        rabbit.setVetEndTime(null);
        rabbit.setStatus(RabbitStatus.IDLE);

        return rabbitRepository.save(rabbit);
    }
}