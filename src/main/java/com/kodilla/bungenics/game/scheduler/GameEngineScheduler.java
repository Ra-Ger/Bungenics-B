package com.kodilla.bungenics.game.scheduler;

import com.kodilla.bungenics.domain.player.Player;
import com.kodilla.bungenics.domain.player.RabbitFarm;
import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.domain.rabbit.RabbitStatus;
import com.kodilla.bungenics.domain.rabbit.RabbitTrait;
import com.kodilla.bungenics.domain.rabbitFarm.Room;
import com.kodilla.bungenics.domain.rabbitFarm.StructureType;
import com.kodilla.bungenics.game.gameSetup.BasicConstants;
import com.kodilla.bungenics.repository.PlayerRepository;
import com.kodilla.bungenics.repository.RabbitFarmRepository;
import com.kodilla.bungenics.repository.RabbitRepository;
import com.kodilla.bungenics.repository.RoomRepository;
import com.kodilla.bungenics.service.AdventureService;
import com.kodilla.bungenics.service.StructureService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class GameEngineScheduler {

    private final PlayerRepository playerRepository;
    private final RabbitRepository rabbitRepository;
    private final RoomRepository roomRepository;
    private final RabbitFarmRepository rabbitFarmRepository;
    private final AdventureService adventureService;
    private final StructureService structureService;

    public GameEngineScheduler(
            PlayerRepository playerRepository,
            RabbitRepository rabbitRepository,
            RoomRepository roomRepository,
            RabbitFarmRepository rabbitFarmRepository,
            AdventureService adventureService,
            StructureService structureService) {
        this.playerRepository = playerRepository;
        this.rabbitRepository = rabbitRepository;
        this.roomRepository = roomRepository;
        this.rabbitFarmRepository = rabbitFarmRepository;
        this.adventureService = adventureService;
        this.structureService = structureService;
    }

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void processGameTick() {
        List<Player> players = playerRepository.findAll();

        float realSecondsPerDay = BasicConstants.MINUTES_PER_DAY * 60.0f;
        float daysPerTick = (BasicConstants.SCHEDULER_TICK_RATE_MS / 1000.0f) / realSecondsPerDay;

        for (Player player : players) {
            RabbitFarm farm = rabbitFarmRepository.findByPlayerId(player.getId()).orElse(null);
            List<Rabbit> playerRabbits = rabbitRepository.findByPlayerId(player.getId());

            for (Rabbit rabbit : playerRabbits) {
                structureService.sanitizeRabbitFields(rabbit);

                if (RabbitStatus.DEAD.equals(rabbit.getStatus()) || RabbitStatus.MARKET.equals(rabbit.getStatus())) {
                    continue;
                }

                float currentAge = rabbit.getAge() != null ? rabbit.getAge() : 0.0f;
                rabbit.setAge(currentAge + daysPerTick);

                Optional<Room> roomOpt = roomRepository.findByRabbitsContaining(rabbit);
                boolean isHomeless = roomOpt.isEmpty();
                StructureType structureType = roomOpt.map(r -> r.getStructure() != null ? r.getStructure().getStructureType() : null).orElse(null);

                boolean isInWarren = StructureType.WARREN.equals(structureType);
                boolean isInPlayhouse = StructureType.PLAYHOUSE.equals(structureType);

                processHungerAndFeeding(rabbit, farm, daysPerTick, isHomeless);

                if (RabbitStatus.DEAD.equals(rabbit.getStatus())) {
                    rabbitRepository.save(rabbit);
                    continue;
                }

                if (isHomeless) {
                    float baseStressGain = BasicConstants.HOMELESS_STRESS_INCREASE_PER_TICK;
                    float stressModifier = 1.0f;

                    if (rabbit.hasTrait(RabbitTrait.CALM)) {
                        stressModifier -= 0.20f;
                    }
                    if (rabbit.hasTrait(RabbitTrait.SKITTISH)) {
                        stressModifier += 0.20f;
                    }

                    float currentStress = rabbit.getStress() != null ? rabbit.getStress() : 0.0f;
                    rabbit.setStress(Math.min(rabbit.getMaxStress(), currentStress + (baseStressGain * stressModifier)));
                } else if (isInWarren && rabbit.getNutritionLevel() != null && rabbit.getNutritionLevel() > 20.0f) {
                    float currentLife = rabbit.getLife() != null ? rabbit.getLife() : rabbit.getMaxHp();
                    float currentStress = rabbit.getStress() != null ? rabbit.getStress() : 0.0f;

                    rabbit.setLife(Math.min(rabbit.getMaxHp(), currentLife + BasicConstants.WARREN_LIFE_REGEN_PER_TICK));
                    rabbit.setStress(Math.max(0.0f, currentStress - BasicConstants.WARREN_STRESS_REDUCTION_PER_TICK));
                } else if (isInPlayhouse) {
                    float currentStress = rabbit.getStress() != null ? rabbit.getStress() : 0.0f;
                    if (currentStress > 0.0f) {
                        float newStress = Math.max(0.0f, currentStress - BasicConstants.PLAYHOUSE_STRESS_REDUCTION_PER_TICK);
                        rabbit.setStress(newStress);

                        if (newStress <= 0.0f && roomOpt.isPresent()) {
                            Room playhouseRoom = roomOpt.get();
                            playhouseRoom.getRabbits().removeIf(r -> r != null && r.getId().equals(rabbit.getId()));
                            roomRepository.save(playhouseRoom);

                            structureService.tryAutoAssignToWarren(rabbit, player.getId());
                        }
                    }
                }

                float maxStress = rabbit.getMaxStress();
                float currentStress = rabbit.getStress() != null ? rabbit.getStress() : 0.0f;
                if (maxStress > 0.0f && (currentStress / maxStress) > 0.90f) {
                    float currentLife = rabbit.getLife() != null ? rabbit.getLife() : rabbit.getMaxHp();
                    currentLife = Math.max(0.0f, currentLife - 2.0f);
                    rabbit.setLife(currentLife);

                    if (currentLife <= 0.0f) {
                        rabbit.setStatus(RabbitStatus.DEAD);
                        rabbitRepository.save(rabbit);
                        continue;
                    }
                }

                if (RabbitStatus.KIT.equals(rabbit.getStatus())) {
                    int requiredGrowDays = getGrowDaysForRabbit(rabbit);

                    if (rabbit.getAge() >= requiredGrowDays) {
                        rabbit.setStatus(RabbitStatus.RESTING);
                        rabbit.setRestEndTime(LocalDateTime.now().plusMinutes(BasicConstants.RESTING_DURATION_MINUTES));
                        structureService.tryAutoAssignToWarren(rabbit, player.getId());

                        if (rabbit.getNutritionLevel() != null && rabbit.getNutritionLevel() > 20.0f) {
                            if (rabbit.getAdultWeight() != null) {
                                rabbit.setWeight(rabbit.getAdultWeight());
                            }
                        }
                    }
                }

                if (RabbitStatus.RESTING.equals(rabbit.getStatus())) {
                    if (rabbit.getRestEndTime() != null && LocalDateTime.now().isAfter(rabbit.getRestEndTime())) {
                        rabbit.setStatus(RabbitStatus.IDLE);
                        rabbit.setRestEndTime(null);
                    } else if (isHomeless) {
                        structureService.tryAutoAssignToWarren(rabbit, player.getId());
                    }
                }

                if (RabbitStatus.ON_VET.equals(rabbit.getStatus())) {
                    if (rabbit.getVetEndTime() != null && LocalDateTime.now().isAfter(rabbit.getVetEndTime())) {
                        rabbit.setStatus(RabbitStatus.IDLE);
                        rabbit.setVetEndTime(null);
                        rabbit.setRestEndTime(null);
                        rabbit.setLife(rabbit.getMaxHp());
                        rabbit.setStress(0.0f);
                        structureService.tryAutoAssignToWarren(rabbit, player.getId());
                    } else {
                        float maxHp = rabbit.getMaxHp();
                        if (rabbit.getLife() != null) {
                            rabbit.setLife(Math.min(maxHp, rabbit.getLife() + 5.0f));
                        }
                        if (rabbit.getStress() != null) {
                            rabbit.setStress(Math.max(0.0f, rabbit.getStress() - 5.0f));
                        }
                    }
                }

                if (RabbitStatus.BREEDING.equals(rabbit.getStatus()) && rabbit.getBreedingEndTime() != null) {
                    if (LocalDateTime.now().isAfter(rabbit.getBreedingEndTime())) {
                        structureService.completeBreedingProcess(rabbit);
                    }
                }

                if (RabbitStatus.TRAINING.equals(rabbit.getStatus()) && rabbit.getTrainingEndTime() != null) {
                    if (LocalDateTime.now().isAfter(rabbit.getTrainingEndTime())) {
                        structureService.completeTrainingProcess(rabbit);
                    }
                }

                rabbitRepository.save(rabbit);
            }

            if (farm != null) {
                rabbitFarmRepository.save(farm);
            }
        }

        try {
            adventureService.resolveCompletedAdventures();
        } catch (Exception ignored) {}
    }

    public int getGrowDaysForRabbit(Rabbit rabbit) {
        float adultWeight = (rabbit != null && rabbit.getAdultWeight() != null) ? rabbit.getAdultWeight() : 3.0f;
        int baseDays;

        if (adultWeight < 2.5f) {
            baseDays = BasicConstants.SMALL_RABBITS_GROW_DAYS;
        } else if (adultWeight < 5.0f) {
            baseDays = BasicConstants.AVERAGE_RABBITS_GROW_DAYS;
        } else {
            baseDays = BasicConstants.LARGE_RABBITS_GROW_DAYS;
        }

        if (rabbit != null && rabbit.hasTrait(RabbitTrait.QUICK_GROWER)) {
            return Math.max(1, Math.round(baseDays * 0.5f));
        }

        return baseDays;
    }

    private void processHungerAndFeeding(Rabbit rabbit, RabbitFarm farm, float daysPerTick, boolean isHomeless) {
        if (rabbit == null) return;

        float currentHunger = rabbit.getNutritionLevel() != null ? rabbit.getNutritionLevel() : 100.0f;

        float nutritionLoss = BasicConstants.HUNGER_DECAY_PER_DAY * daysPerTick;
        if (rabbit.hasTrait(RabbitTrait.GLUTTON)) {
            nutritionLoss *= 1.20f;
        }
        currentHunger = Math.max(0.0f, currentHunger - nutritionLoss);
        rabbit.setNutritionLevel(currentHunger);

        float bodyWeight = rabbit.getWeight() != null ? rabbit.getWeight() : 1.0f;
        float consumptionMultiplier = isHomeless ? BasicConstants.HOMELESS_FOOD_CONSUMPTION_MULTIPLIER : 1.0f;

        if (rabbit.hasTrait(RabbitTrait.GLUTTON)) {
            consumptionMultiplier *= 1.20f;
        }

        float foodConsumedPerTick = bodyWeight * BasicConstants.FOOD_USED_PER_WEIGHT * daysPerTick * consumptionMultiplier;

        if (currentHunger < 50.0f && farm != null) {
            float hay = farm.getHayAmount() != null ? farm.getHayAmount() : 0.0f;
            float neededHay = Math.max(0.1f, foodConsumedPerTick * 10.0f);

            if (hay >= neededHay) {
                farm.setHayAmount(hay - neededHay);
                rabbit.setNutritionLevel(100.0f);
            } else if (hay > 0.0f) {
                farm.setHayAmount(0.0f);
                float restoredPercentage = (hay / neededHay) * 50.0f;
                rabbit.setNutritionLevel(Math.min(100.0f, currentHunger + restoredPercentage));
            } else {
                float currentLife = rabbit.getLife() != null ? rabbit.getLife() : 100.0f;
                currentLife = Math.max(0.0f, currentLife - 2.0f);
                rabbit.setLife(currentLife);

                if (currentLife <= 0.0f) {
                    rabbit.setStatus(RabbitStatus.DEAD);
                }
            }
        }
    }
}