package com.kodilla.bungenics.service;

import com.kodilla.bungenics.domain.adventure.Adventure;
import com.kodilla.bungenics.domain.adventure.AdventureEvent;
import com.kodilla.bungenics.domain.player.Player;
import com.kodilla.bungenics.domain.player.RabbitFarm;
import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.domain.rabbit.RabbitStatus;
import com.kodilla.bungenics.domain.rabbit.RabbitTrait;
import com.kodilla.bungenics.exception.ResourceNotFoundException;
import com.kodilla.bungenics.game.gameSetup.BasicConstants;
import com.kodilla.bungenics.game.strategy.AdventureStrategy;
import com.kodilla.bungenics.repository.AdventureRepository;
import com.kodilla.bungenics.repository.RabbitFarmRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdventureService {

    private final AdventureRepository adventureRepository;
    private final RabbitService rabbitService;
    private final PlayerService playerService;
    private final StructureService structureService;
    private final RabbitFarmRepository rabbitFarmRepository;
    private final List<AdventureStrategy> strategies;

    public AdventureService(
            AdventureRepository adventureRepository,
            RabbitService rabbitService,
            PlayerService playerService,
            StructureService structureService,
            RabbitFarmRepository rabbitFarmRepository,
            List<AdventureStrategy> strategies) {
        this.adventureRepository = adventureRepository;
        this.rabbitService = rabbitService;
        this.playerService = playerService;
        this.structureService = structureService;
        this.rabbitFarmRepository = rabbitFarmRepository;
        this.strategies = strategies;
    }

    public List<Adventure> getCompletedAdventures(Long playerId) {
        return adventureRepository.findAll().stream()
                .filter(a -> a.getPlayerId().equals(playerId))
                .filter(a -> "COMPLETED".equalsIgnoreCase(a.getStatus()))
                .toList();
    }

    @Transactional
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

    @Transactional
    public void sendRabbitOnAdventure(Long playerId, Long rabbitId, String type) {
        Rabbit rabbit = rabbitService.getRabbitById(rabbitId);
        if (!RabbitStatus.IDLE.equals(rabbit.getStatus())) {
            throw new IllegalStateException("Rabbit must be IDLE to depart on an expedition!");
        }

        long durationSeconds = BasicConstants.ADVENTURE_DURATION_MINUTES * 60L;

        if (rabbit.hasTrait(RabbitTrait.LAZY)) {
            durationSeconds = Math.round(durationSeconds * 1.20f);
        }

        LocalDateTime endTime = LocalDateTime.now().plusSeconds(durationSeconds);

        rabbit.setStatus(RabbitStatus.ADVENTURE);
        rabbit.setAdventureEndTime(endTime);
        rabbitService.updateRabbit(rabbit.getId(), rabbit);

        Adventure adventure = new Adventure();
        adventure.setName("Expedition to " + type.toUpperCase());
        adventure.setPlayerId(playerId);
        adventure.setRabbitId(rabbitId);
        adventure.setType(type.toUpperCase());
        adventure.setEndTime(endTime);
        adventure.setStatus("IN_PROGRESS");
        adventure.setAdventureEvents(new ArrayList<>());

        adventureRepository.save(adventure);
    }

    @Transactional
    public void resolveCompletedAdventures() {
        List<Adventure> inProgress = adventureRepository.findAll().stream()
                .filter(a -> "IN_PROGRESS".equals(a.getStatus()))
                .filter(a -> a.getEndTime() != null && a.getEndTime().isBefore(LocalDateTime.now()))
                .toList();

        for (Adventure adventure : inProgress) {
            try {
                Rabbit rabbit = rabbitService.getRabbitById(adventure.getRabbitId());

                AdventureStrategy strategy = strategies.stream()
                        .filter(s -> s.getAdventureType().equalsIgnoreCase(adventure.getType()))
                        .findFirst()
                        .orElse(strategies.isEmpty() ? null : strategies.get(0));

                if (strategy == null) {
                    throw new RuntimeException("No strategy found for " + adventure.getType());
                }

                List<AdventureEvent> events = strategy.executeAdventure(rabbit);

                if (RabbitStatus.DEAD.equals(rabbit.getStatus()) || (rabbit.getLife() != null && rabbit.getLife() <= 0f)) {
                    rabbit.setStatus(RabbitStatus.DEAD);
                    rabbit.setLife(0f);
                    rabbit.setAdventureEndTime(null);
                    rabbitService.updateRabbit(rabbit.getId(), rabbit);
                } else {
                    rabbit.setStatus(RabbitStatus.RESTING);
                    rabbit.setAdventureEndTime(null);

                    long restSeconds = BasicConstants.RESTING_DURATION_MINUTES * 60L;
                    if (rabbit.hasTrait(RabbitTrait.LAZY)) {
                        restSeconds = Math.round(restSeconds * 1.20f);
                    }
                    rabbit.setRestEndTime(LocalDateTime.now().plusSeconds(restSeconds));
                    rabbitService.updateRabbit(rabbit.getId(), rabbit);
                    structureService.tryAutoAssignToWarren(rabbit, adventure.getPlayerId());
                }

                BigDecimal totalGold = events.stream()
                        .map(e -> e.getGoldReward() != null ? e.getGoldReward() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (totalGold.compareTo(BigDecimal.ZERO) > 0) {
                    Player player = playerService.getPlayerById(adventure.getPlayerId());
                    BigDecimal currentMoney = player.getMoney() != null ? player.getMoney() : BigDecimal.ZERO;
                    player.setMoney(currentMoney.add(totalGold));
                    playerService.updatePlayer(player.getId(), player);
                }

                float totalCarrots = events.stream()
                        .map(e -> e.getCarrotReward() != null ? e.getCarrotReward() : 0f)
                        .reduce(0f, Float::sum);
                float totalLettuce = events.stream()
                        .map(e -> e.getLettuceReward() != null ? e.getLettuceReward() : 0f)
                        .reduce(0f, Float::sum);
                float totalSpinach = events.stream()
                        .map(e -> e.getSpinachReward() != null ? e.getSpinachReward() : 0f)
                        .reduce(0f, Float::sum);

                if (totalCarrots > 0f || totalLettuce > 0f || totalSpinach > 0f) {
                    RabbitFarm farm = rabbitFarmRepository.findByPlayerId(adventure.getPlayerId()).orElse(null);
                    if (farm != null) {
                        if (totalCarrots > 0f) {
                            farm.setCarrotAmount((farm.getCarrotAmount() != null ? farm.getCarrotAmount() : 0f) + totalCarrots);
                        }
                        if (totalLettuce > 0f) {
                            farm.setLettuceAmount((farm.getLettuceAmount() != null ? farm.getLettuceAmount() : 0f) + totalLettuce);
                        }
                        if (totalSpinach > 0f) {
                            farm.setSpinachAmount((farm.getSpinachAmount() != null ? farm.getSpinachAmount() : 0f) + totalSpinach);
                        }
                        rabbitFarmRepository.save(farm);
                    }
                }

                adventure.setStatus("COMPLETED");
                if (adventure.getAdventureEvents() == null) {
                    adventure.setAdventureEvents(new ArrayList<>());
                }

                for (AdventureEvent event : events) {
                    event.setAdventure(adventure);
                }
                adventure.getAdventureEvents().addAll(events);
                adventureRepository.save(adventure);
            } catch (Exception e) {
                System.err.println("Error resolving adventure #" + adventure.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}