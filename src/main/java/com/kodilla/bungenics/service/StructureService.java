package com.kodilla.bungenics.service;

import com.kodilla.bungenics.domain.player.Player;
import com.kodilla.bungenics.domain.player.RabbitFarm;
import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.domain.rabbit.RabbitStatus;
import com.kodilla.bungenics.domain.rabbit.RabbitTrait;
import com.kodilla.bungenics.domain.rabbit.SecondaryStats;
import com.kodilla.bungenics.domain.rabbitFarm.Room;
import com.kodilla.bungenics.domain.rabbitFarm.Structure;
import com.kodilla.bungenics.domain.rabbitFarm.StructureType;
import com.kodilla.bungenics.game.factory.RabbitFactory;
import com.kodilla.bungenics.game.gameSetup.BasicConstants;
import com.kodilla.bungenics.repository.PlayerRepository;
import com.kodilla.bungenics.repository.RabbitFarmRepository;
import com.kodilla.bungenics.repository.RabbitRepository;
import com.kodilla.bungenics.repository.RoomRepository;
import com.kodilla.bungenics.repository.StructureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class StructureService {

    private final StructureRepository structureRepository;
    private final RoomRepository roomRepository;
    private final RabbitRepository rabbitRepository;
    private final RabbitFarmRepository rabbitFarmRepository;
    private final PlayerRepository playerRepository;
    private final RabbitFactory rabbitFactory;

    public StructureService(
            StructureRepository structureRepository,
            RoomRepository roomRepository,
            RabbitRepository rabbitRepository,
            RabbitFarmRepository rabbitFarmRepository,
            PlayerRepository playerRepository,
            RabbitFactory rabbitFactory) {
        this.structureRepository = structureRepository;
        this.roomRepository = roomRepository;
        this.rabbitRepository = rabbitRepository;
        this.rabbitFarmRepository = rabbitFarmRepository;
        this.playerRepository = playerRepository;
        this.rabbitFactory = rabbitFactory;
    }

    public void sanitizeRabbitFields(Rabbit rabbit) {
        if (rabbit == null) return;

        if (rabbit.getMaxLifetime() == null) {
            rabbit.setMaxLifetime(16.0f);
        }
        if (rabbit.getAge() == null) {
            rabbit.setAge(0.0f);
        }
        if (rabbit.getLife() == null) {
            rabbit.setLife(rabbit.getMaxHp());
        }
        if (rabbit.getStress() == null) {
            rabbit.setStress(0.0f);
        }
        if (rabbit.getWeight() == null) {
            rabbit.setWeight(1.0f);
        }
        if (rabbit.getNutritionLevel() == null) {
            rabbit.setNutritionLevel(100.0f);
        }
        if (rabbit.getTraits() == null) {
            rabbit.setTraits(new HashSet<RabbitTrait>());
        }
    }

    public void tryAutoAssignToWarren(Rabbit rabbit, Long playerId) {
        if (rabbit == null || playerId == null) return;

        List<Room> allRooms = roomRepository.findAll();
        boolean alreadyAssigned = allRooms.stream()
                .anyMatch(r -> r.getRabbits() != null && r.getRabbits().stream().anyMatch(rab -> rab != null && rabbit.getId().equals(rab.getId())));

        if (alreadyAssigned) return;

        List<Structure> structures = structureRepository.findAll();
        for (Structure struct : structures) {
            if (struct.getRabbitFarm() != null && struct.getRabbitFarm().getPlayer() != null && playerId.equals(struct.getRabbitFarm().getPlayer().getId())) {
                if (StructureType.WARREN.equals(struct.getStructureType()) && struct.getRooms() != null) {
                    for (Room room : struct.getRooms()) {
                        int occupantCount = room.getRabbits() != null ? room.getRabbits().size() : 0;
                        int maxSlots = room.getSlots() != null ? room.getSlots() : 0;
                        if (occupantCount < maxSlots) {
                            if (room.getRabbits() == null) room.setRabbits(new ArrayList<>());
                            room.getRabbits().add(rabbit);
                            roomRepository.save(room);
                            return;
                        }
                    }
                }
            }
        }
    }

    public boolean areRelated(Rabbit r1, Rabbit r2) {
        if (r1 == null || r2 == null) return false;

        if (Objects.equals(r1.getId(), r2.getId())) return true;
        if (Objects.equals(r1.getId(), r2.getMotherId()) || Objects.equals(r1.getId(), r2.getFatherId())) return true;
        if (Objects.equals(r2.getId(), r1.getMotherId()) || Objects.equals(r2.getId(), r1.getFatherId())) return true;
        if (r1.getMotherId() != null && Objects.equals(r1.getMotherId(), r2.getMotherId())) return true;
        if (r1.getFatherId() != null && Objects.equals(r1.getFatherId(), r2.getFatherId())) return true;

        return false;
    }

    @Transactional
    public Rabbit completeBreedingProcess(Rabbit parent) {
        if (parent == null) return null;
        sanitizeRabbitFields(parent);

        if (!RabbitStatus.BREEDING.equals(parent.getStatus())) {
            return parent;
        }

        List<Room> rooms = roomRepository.findAll();
        Room parentRoom = null;

        for (Room r : rooms) {
            if (r.getRabbits() != null && r.getRabbits().stream().anyMatch(rab -> rab != null && parent.getId().equals(rab.getId()))) {
                parentRoom = r;
                break;
            }
        }

        if (parentRoom != null) {
            Rabbit female = null;
            Rabbit male = null;

            for (Rabbit r : parentRoom.getRabbits()) {
                if (r != null && RabbitStatus.BREEDING.equals(r.getStatus())) {
                    sanitizeRabbitFields(r);
                    if ("FEMALE".equalsIgnoreCase(r.getSex())) female = r;
                    if ("MALE".equalsIgnoreCase(r.getSex())) male = r;
                }
            }

            if (female != null && male != null) {
                LocalDateTime restEnd = LocalDateTime.now().plusMinutes(BasicConstants.RESTING_DURATION_MINUTES);

                female.setStatus(RabbitStatus.RESTING);
                female.setBreedingEndTime(null);
                female.setRestEndTime(restEnd);

                male.setStatus(RabbitStatus.RESTING);
                male.setBreedingEndTime(null);
                male.setRestEndTime(restEnd);

                rabbitRepository.save(female);
                rabbitRepository.save(male);

                Rabbit kit = rabbitFactory.createKit(female, male);
                sanitizeRabbitFields(kit);
                kit.setStatus(RabbitStatus.KIT);
                Rabbit savedKit = rabbitRepository.save(kit);

                applyWeakGenesIfPresent(female, male, savedKit);

                // FERTILE: 25% chance of twin pregnancy for female
                if (female.hasTrait(RabbitTrait.FERTILE) && new Random().nextFloat() < 0.25f) {
                    Rabbit twinKit = rabbitFactory.createKit(female, male);
                    sanitizeRabbitFields(twinKit);
                    twinKit.setStatus(RabbitStatus.KIT);
                    Rabbit savedTwin = rabbitRepository.save(twinKit);
                    applyWeakGenesIfPresent(female, male, savedTwin);
                    tryAutoAssignToWarren(savedTwin, savedTwin.getPlayerId());
                }

                final Long femaleId = female.getId();
                final Long maleId = male.getId();
                parentRoom.getRabbits().removeIf(r -> r != null && (r.getId().equals(femaleId) || r.getId().equals(maleId)));
                roomRepository.save(parentRoom);

                tryAutoAssignToWarren(female, female.getPlayerId());
                tryAutoAssignToWarren(male, male.getPlayerId());
                tryAutoAssignToWarren(savedKit, savedKit.getPlayerId());

                return savedKit;
            } else {
                parent.setStatus(RabbitStatus.RESTING);
                parent.setBreedingEndTime(null);
                parent.setRestEndTime(LocalDateTime.now().plusMinutes(BasicConstants.RESTING_DURATION_MINUTES));
                Rabbit savedParent = rabbitRepository.save(parent);

                parentRoom.getRabbits().removeIf(r -> r != null && r.getId().equals(parent.getId()));
                roomRepository.save(parentRoom);
                tryAutoAssignToWarren(parent, parent.getPlayerId());

                return savedParent;
            }
        } else {
            parent.setStatus(RabbitStatus.RESTING);
            parent.setBreedingEndTime(null);
            parent.setRestEndTime(LocalDateTime.now().plusMinutes(BasicConstants.RESTING_DURATION_MINUTES));
            return rabbitRepository.save(parent);
        }
    }

    private void applyWeakGenesIfPresent(Rabbit female, Rabbit male, Rabbit kit) {
        boolean femaleWeak = female != null && female.hasTrait(RabbitTrait.WEAK_GENES);
        boolean maleWeak = male != null && male.hasTrait(RabbitTrait.WEAK_GENES);

        if ((femaleWeak || maleWeak) && new Random().nextFloat() < 0.20f) {
            List<RabbitTrait> negativePool = List.of(
                    RabbitTrait.GLUTTON,
                    RabbitTrait.SKITTISH,
                    RabbitTrait.LAZY,
                    RabbitTrait.WEAK_GENES
            );
            List<RabbitTrait> available = negativePool.stream()
                    .filter(t -> kit.getTraits() == null || !kit.getTraits().contains(t))
                    .toList();
            if (!available.isEmpty()) {
                RabbitTrait extraNegative = available.get(new Random().nextInt(available.size()));
                if (kit.getTraits() == null) {
                    kit.setTraits(new HashSet<>());
                }
                kit.getTraits().add(extraNegative);
                rabbitRepository.save(kit);
            }
        }
    }

    @Transactional
    public List<Structure> getAllStructures() {
        processFinishedBreedings();
        processFinishedTrainings();
        return structureRepository.findAll();
    }

    @Transactional
    public Structure getStructureById(Long id) {
        processFinishedBreedings();
        processFinishedTrainings();
        return structureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Structure not found with id: " + id));
    }

    @Transactional
    public Structure buildStructure(Long farmId, StructureType type, Integer gridIndex) {
        RabbitFarm farm = rabbitFarmRepository.findById(farmId)
                .orElseThrow(() -> new RuntimeException("Farm not found with id: " + farmId));

        Player player = playerRepository.findById(farm.getPlayer().getId())
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + farm.getPlayer().getId()));

        BigDecimal cost = BasicConstants.STRUCTURE_BUILD_COST;
        if (player.getMoney() == null || player.getMoney().compareTo(cost) < 0) {
            throw new IllegalStateException("Not enough money to build structure! Required: " + cost + " Gold.");
        }

        player.setMoney(player.getMoney().subtract(cost));
        playerRepository.save(player);

        Structure structure = Structure.builder()
                .rabbitFarm(farm)
                .rabbitFarmId(farmId)
                .structureType(type)
                .gridIndex(gridIndex)
                .slots(4)
                .rooms(new ArrayList<>())
                .build();

        Room defaultRoom = Room.builder()
                .slots(2)
                .structure(structure)
                .rabbits(new ArrayList<>())
                .build();
        structure.getRooms().add(defaultRoom);

        return structureRepository.save(structure);
    }

    @Transactional
    public Structure addRoomToStructure(Long structureId) {
        Structure structure = getStructureById(structureId);

        Player player = playerRepository.findById(structure.getRabbitFarm().getPlayer().getId())
                .orElseThrow(() -> new RuntimeException("Player not found"));

        BigDecimal cost = BasicConstants.ROOM_BUILD_COST;
        if (player.getMoney() == null || player.getMoney().compareTo(cost) < 0) {
            throw new IllegalStateException("Not enough money to add a room! Required: " + cost + " Gold.");
        }

        player.setMoney(player.getMoney().subtract(cost));
        playerRepository.save(player);

        Room room = Room.builder()
                .slots(2)
                .structure(structure)
                .build();
        structure.getRooms().add(room);
        return structureRepository.save(structure);
    }

    @Transactional
    public Room expandRoomSlots(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (room.getStructure() != null && StructureType.TRYSTHOUSE.equals(room.getStructure().getStructureType())) {
            throw new IllegalStateException("Rooms in a TRYSTHOUSE cannot be expanded!");
        }

        Player player = playerRepository.findById(room.getStructure().getRabbitFarm().getPlayer().getId())
                .orElseThrow(() -> new RuntimeException("Player not found"));

        BigDecimal cost = BasicConstants.ROOM_EXPAND_COST;
        if (player.getMoney() == null || player.getMoney().compareTo(cost) < 0) {
            throw new IllegalStateException("Not enough money to expand room! Required: " + cost + " Gold.");
        }

        player.setMoney(player.getMoney().subtract(cost));
        playerRepository.save(player);

        room.setSlots(room.getSlots() + 2);
        return roomRepository.save(room);
    }

    @Transactional
    public Room startTrainingInRoom(Long roomId, Long rabbitId, String enhancedFoodType) {
        processFinishedTrainings();

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        if (room.getStructure() == null || room.getStructure().getStructureType() != StructureType.TRAINING_GROUND) {
            throw new RuntimeException("Training can only take place in a TRAINING_GROUND!");
        }

        Rabbit rabbit = rabbitRepository.findById(rabbitId)
                .orElseThrow(() -> new RuntimeException("Rabbit not found with id: " + rabbitId));

        if (room.getRabbits() == null || room.getRabbits().stream().noneMatch(r -> r != null && r.getId().equals(rabbitId))) {
            throw new RuntimeException("Rabbit is not in this training room!");
        }

        if (!RabbitStatus.IDLE.equals(rabbit.getStatus())) {
            throw new IllegalStateException("Rabbit must be IDLE to start training! Resting or busy rabbits cannot train.");
        }

        sanitizeRabbitFields(rabbit);

        SecondaryStats stats = rabbit.getSecondaryStats();
        float str = (stats != null && stats.getStrength() != null) ? stats.getStrength() : 10.0f;
        float agi = (stats != null && stats.getAgility() != null) ? stats.getAgility() : 10.0f;
        float intel = (stats != null && stats.getIntelligence() != null) ? stats.getIntelligence() : 10.0f;

        float totalStats = str + agi + intel;
        BigDecimal goldCost = BigDecimal.valueOf(totalStats * BasicConstants.TRAINING_COST_MULTIPLIER);

        Long playerId = rabbit.getPlayerId();
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + playerId));

        if (player.getMoney() == null || player.getMoney().compareTo(goldCost) < 0) {
            throw new IllegalStateException("Not enough gold for training! Required: " + goldCost.setScale(2, RoundingMode.HALF_UP) + " Gold.");
        }

        if (enhancedFoodType != null && !enhancedFoodType.isBlank() && !"NONE".equalsIgnoreCase(enhancedFoodType)) {
            RabbitFarm farm = rabbitFarmRepository.findByPlayerId(playerId).orElse(null);
            if (farm == null) {
                throw new IllegalStateException("Farm not found for player!");
            }
            float weight = rabbit.getWeight() != null ? rabbit.getWeight() : 1.0f;
            float requiredFood = weight * BasicConstants.FOOD_USED_PER_WEIGHT;

            switch (enhancedFoodType.toUpperCase()) {
                case "SPINACH" -> {
                    float avail = farm.getSpinachAmount() != null ? farm.getSpinachAmount() : 0.0f;
                    if (avail < requiredFood) throw new IllegalStateException("Not enough Spinach! Required: " + String.format(Locale.US, "%.2f", requiredFood) + " kg");
                    farm.setSpinachAmount(avail - requiredFood);
                }
                case "CARROT" -> {
                    float avail = farm.getCarrotAmount() != null ? farm.getCarrotAmount() : 0.0f;
                    if (avail < requiredFood) throw new IllegalStateException("Not enough Carrots! Required: " + String.format(Locale.US, "%.2f", requiredFood) + " kg");
                    farm.setCarrotAmount(avail - requiredFood);
                }
                case "LETTUCE" -> {
                    float avail = farm.getLettuceAmount() != null ? farm.getLettuceAmount() : 0.0f;
                    if (avail < requiredFood) throw new IllegalStateException("Not enough Lettuce! Required: " + String.format(Locale.US, "%.2f", requiredFood) + " kg");
                    farm.setLettuceAmount(avail - requiredFood);
                }
                default -> throw new IllegalArgumentException("Unknown enhanced food type: " + enhancedFoodType);
            }
            rabbit.setTrainingEnhancedFood(enhancedFoodType.toUpperCase());
            rabbitFarmRepository.save(farm);
        } else {
            rabbit.setTrainingEnhancedFood(null);
        }

        player.setMoney(player.getMoney().subtract(goldCost));
        playerRepository.save(player);

        rabbit.setStatus(RabbitStatus.TRAINING);

        // LAZY: Completes tasks 20% slower
        long duration = BasicConstants.TRAINING_DURATION_MINUTES;
        if (rabbit.hasTrait(RabbitTrait.LAZY)) {
            duration = Math.round(duration * 1.20f);
        }

        rabbit.setTrainingEndTime(LocalDateTime.now().plusMinutes(duration));
        rabbitRepository.save(rabbit);

        return roomRepository.save(room);
    }

    @Transactional
    public Room assignRabbitToRoom(Long roomId, Long rabbitId) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));
        Rabbit rabbit = rabbitRepository.findById(rabbitId).orElseThrow(() -> new RuntimeException("Rabbit not found"));

        sanitizeRabbitFields(rabbit);

        Long farmOwnerId = room.getStructure() != null && room.getStructure().getRabbitFarm() != null
                ? room.getStructure().getRabbitFarm().getPlayer().getId()
                : null;

        if (farmOwnerId != null && rabbit.getPlayerId() != null && !farmOwnerId.equals(rabbit.getPlayerId())) {
            throw new IllegalStateException("Cannot assign a rabbit owned by another player!");
        }

        if (RabbitStatus.MARKET.equals(rabbit.getStatus())) {
            throw new IllegalStateException("Cannot assign a market rabbit to a room!");
        }

        if (RabbitStatus.BREEDING.equals(rabbit.getStatus())) {
            throw new IllegalStateException("Cannot assign a breeding rabbit to another room!");
        }

        if (RabbitStatus.ADVENTURE.equals(rabbit.getStatus())) {
            throw new IllegalStateException("Cannot assign a rabbit that is on an adventure!");
        }

        if (RabbitStatus.ON_VET.equals(rabbit.getStatus())) {
            throw new IllegalStateException("Cannot assign a rabbit that is currently at the Vet Clinic!");
        }

        if (room.getStructure() != null && StructureType.PLAYHOUSE.equals(room.getStructure().getStructureType())) {
            if (rabbit.getStress() == null || rabbit.getStress() <= 0.0f) {
                throw new IllegalStateException("Only stressed rabbits (stress > 0) can be assigned to the Playhouse!");
            }
        }

        if (room.getRabbits().size() >= room.getSlots()) {
            throw new RuntimeException("Room is full!");
        }

        roomRepository.findByRabbitsContaining(rabbit).ifPresent(oldRoom -> {
            if (!oldRoom.getId().equals(roomId)) {
                oldRoom.getRabbits().remove(rabbit);
                roomRepository.save(oldRoom);
            }
        });

        if (!room.getRabbits().contains(rabbit)) {
            room.getRabbits().add(rabbit);
        }
        return roomRepository.save(room);
    }

    @Transactional
    public Room removeRabbitFromRoom(Long roomId, Long rabbitId) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));
        Rabbit rabbit = rabbitRepository.findById(rabbitId).orElseThrow(() -> new RuntimeException("Rabbit not found"));

        if (RabbitStatus.MARKET.equals(rabbit.getStatus())) {
            throw new IllegalStateException("Cannot remove a market rabbit from a room!");
        }

        if (RabbitStatus.BREEDING.equals(rabbit.getStatus())) {
            throw new RuntimeException("Cannot remove rabbit while breeding in progress!");
        }

        room.getRabbits().removeIf(r -> r.getId().equals(rabbitId));
        roomRepository.save(room);

        tryAutoAssignToWarren(rabbit, rabbit.getPlayerId());
        return room;
    }

    @Transactional
    public Room startBreedingInRoom(Long roomId) {
        processFinishedBreedings();

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (room.getStructure() == null || room.getStructure().getStructureType() != StructureType.TRYSTHOUSE) {
            throw new RuntimeException("Breeding can only take place in a TRYSTHOUSE!");
        }

        List<Rabbit> idleRabbits = room.getRabbits().stream()
                .filter(r -> r != null && (RabbitStatus.IDLE.equals(r.getStatus()) || RabbitStatus.RESTING.equals(r.getStatus())))
                .toList();

        Rabbit female = idleRabbits.stream().filter(r -> "FEMALE".equalsIgnoreCase(r.getSex())).findFirst().orElse(null);
        Rabbit male = idleRabbits.stream().filter(r -> "MALE".equalsIgnoreCase(r.getSex())).findFirst().orElse(null);

        if (female == null || male == null) {
            throw new RuntimeException("Breeding requires at least 1 adult male and 1 adult female rabbit in the room!");
        }

        if (areRelated(female, male)) {
            throw new IllegalArgumentException("Inbreeding prevented! Cannot breed siblings or parent with offspring.");
        }

        // LAZY: Completes tasks 20% slower if female or male is lazy
        long duration = BasicConstants.BREEDING_DURATION_MINUTES;
        if (female.hasTrait(RabbitTrait.LAZY) || male.hasTrait(RabbitTrait.LAZY)) {
            duration = Math.round(duration * 1.20f);
        }

        LocalDateTime endTime = LocalDateTime.now().plusMinutes(duration);

        sanitizeRabbitFields(female);
        sanitizeRabbitFields(male);

        female.setStatus(RabbitStatus.BREEDING);
        female.setBreedingEndTime(endTime);
        male.setStatus(RabbitStatus.BREEDING);
        male.setBreedingEndTime(endTime);

        rabbitRepository.save(female);
        rabbitRepository.save(male);

        return roomRepository.save(room);
    }

    @Transactional
    public Rabbit completeTrainingProcess(Rabbit rabbit) {
        if (rabbit == null) return null;
        sanitizeRabbitFields(rabbit);

        if (!RabbitStatus.TRAINING.equals(rabbit.getStatus())) {
            return rabbit;
        }

        SecondaryStats stats = rabbit.getSecondaryStats();
        if (stats == null) {
            stats = SecondaryStats.builder()
                    .strength(10.0f)
                    .agility(10.0f)
                    .intelligence(10.0f)
                    .build();
            rabbit.setSecondaryStats(stats);
        }

        float str = stats.getStrength() != null ? stats.getStrength() : 10.0f;
        float agi = stats.getAgility() != null ? stats.getAgility() : 10.0f;
        float intel = stats.getIntelligence() != null ? stats.getIntelligence() : 10.0f;

        Random rng = new Random();
        for (int i = 0; i < 2; i++) {
            int roll = rng.nextInt(3);
            if (roll == 0) str += 1.0f;
            else if (roll == 1) agi += 1.0f;
            else intel += 1.0f;
        }

        String food = rabbit.getTrainingEnhancedFood();
        if (food != null) {
            switch (food.toUpperCase()) {
                case "SPINACH" -> str += 2.0f;
                case "CARROT" -> agi += 2.0f;
                case "LETTUCE" -> intel += 2.0f;
            }
        }

        stats.setStrength(str);
        stats.setAgility(agi);
        stats.setIntelligence(intel);

        rabbit.setStatus(RabbitStatus.RESTING);
        rabbit.setTrainingEndTime(null);
        rabbit.setTrainingEnhancedFood(null);
        rabbit.setRestEndTime(LocalDateTime.now().plusMinutes(BasicConstants.RESTING_DURATION_MINUTES));

        Rabbit savedRabbit = rabbitRepository.save(rabbit);

        List<Room> rooms = roomRepository.findAll();
        for (Room r : rooms) {
            if (r.getRabbits() != null && r.getRabbits().stream().anyMatch(rab -> rab != null && rabbit.getId().equals(rab.getId()))) {
                r.getRabbits().removeIf(rab -> rab != null && rab.getId().equals(rabbit.getId()));
                roomRepository.save(r);
                break;
            }
        }

        tryAutoAssignToWarren(savedRabbit, savedRabbit.getPlayerId());

        return savedRabbit;
    }

    @Transactional
    public void processFinishedTrainings() {
        List<Rabbit> trainingRabbits = rabbitRepository.findByStatus(RabbitStatus.TRAINING);
        if (trainingRabbits == null || trainingRabbits.isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();
        List<Rabbit> expired = trainingRabbits.stream()
                .filter(r -> r != null && r.getTrainingEndTime() != null && now.isAfter(r.getTrainingEndTime()))
                .toList();

        for (Rabbit r : expired) {
            completeTrainingProcess(r);
        }
    }

    @Transactional
    public void processFinishedBreedings() {
        List<Rabbit> breedingRabbits = rabbitRepository.findByStatus(RabbitStatus.BREEDING);
        if (breedingRabbits == null || breedingRabbits.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        List<Rabbit> expiredRabbits = breedingRabbits.stream()
                .filter(r -> r != null && r.getBreedingEndTime() != null && now.isAfter(r.getBreedingEndTime()))
                .toList();

        for (Rabbit rabbit : expiredRabbits) {
            sanitizeRabbitFields(rabbit);
            completeBreedingProcess(rabbit);
        }
    }

    public void deleteStructure(Long id) {
        structureRepository.deleteById(id);
    }
}