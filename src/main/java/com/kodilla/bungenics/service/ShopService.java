package com.kodilla.bungenics.service;

import com.kodilla.bungenics.dataFetchers.NASS.CommodityCode;
import com.kodilla.bungenics.dataFetchers.NASS.NASSLatestDataFetcher;
import com.kodilla.bungenics.domain.player.Player;
import com.kodilla.bungenics.domain.player.RabbitFarm;
import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.domain.rabbit.RabbitStatus;
import com.kodilla.bungenics.domain.rabbitFarm.Room;
import com.kodilla.bungenics.domain.rabbitFarm.Structure;
import com.kodilla.bungenics.game.factory.RabbitFactory;
import com.kodilla.bungenics.game.gameSetup.BasicConstants;
import com.kodilla.bungenics.repository.PlayerRepository;
import com.kodilla.bungenics.repository.RabbitFarmRepository;
import com.kodilla.bungenics.repository.RabbitRepository;
import com.kodilla.bungenics.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final PlayerRepository playerRepository;
    private final RabbitRepository rabbitRepository;
    private final RabbitFarmRepository rabbitFarmRepository;
    private final RoomRepository roomRepository;
    private final RabbitFactory rabbitFactory;
    private final NASSLatestDataFetcher nassFetcher;

    private final Map<String, BigDecimal> foodPriceCache = new ConcurrentHashMap<>();

    public BigDecimal estimateFoodCost(String foodType, Float amount) {
        if (amount == null || amount <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal pricePerKg = getPriceForFoodType(foodType);
        return pricePerKg.multiply(BigDecimal.valueOf(amount)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateRabbitSellValue(Long rabbitId) {
        Rabbit rabbit = rabbitRepository.findById(rabbitId)
                .orElseThrow(() -> new RuntimeException("Rabbit not found with id: " + rabbitId));
        return getRabbitValue(rabbit);
    }

    private BigDecimal getRabbitValue(Rabbit rabbit) {
        BigDecimal weight = BigDecimal.valueOf(rabbit.getWeight() != null ? rabbit.getWeight() : 1.0f);
        BigDecimal life = BigDecimal.valueOf(rabbit.getLife() != null ? rabbit.getLife() : rabbit.getMaxHp());
        BigDecimal maxLife = BigDecimal.valueOf(rabbit.getMaxHp());

        BigDecimal healthMultiplier = maxLife.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ONE
                : life.divide(maxLife, 2, RoundingMode.HALF_UP);

        double baseVal = 50.0 + (weight.doubleValue() * 20.0) + (healthMultiplier.doubleValue() * 25.0);
        return BigDecimal.valueOf(Math.round(baseVal * 100.0) / 100.0);
    }

    @Transactional
    public List<Rabbit> getMarketRabbits() {
        List<Rabbit> marketRabbits = new ArrayList<>(rabbitRepository.findByStatus(RabbitStatus.MARKET));

        if (marketRabbits.size() < 3) {
            int needed = 3 - marketRabbits.size();
            for (int i = 0; i < needed; i++) {
                Rabbit newRabbit = rabbitFactory.createRandomRabbit(null, null, null);
                newRabbit.setStatus(RabbitStatus.MARKET);

                newRabbit.setLife(newRabbit.getMaxHp());
                newRabbit.setStress(0.0f);

                Rabbit savedRabbit = rabbitRepository.save(newRabbit);
                marketRabbits.add(savedRabbit);
            }
        }
        return marketRabbits;
    }

    @Transactional
    public void buyRabbit(Long playerId, Long rabbitId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + playerId));
        Rabbit rabbit = rabbitRepository.findById(rabbitId)
                .orElseThrow(() -> new RuntimeException("Rabbit not found with id: " + rabbitId));

        if (rabbit.getStatus() != RabbitStatus.MARKET) {
            throw new IllegalStateException("Rabbit is not available on the market!");
        }

        BigDecimal price = getRabbitValue(rabbit).multiply(BigDecimal.valueOf(1.2)).setScale(2, RoundingMode.HALF_UP);
        if (player.getMoney().compareTo(price) < 0) {
            throw new RuntimeException("Not enough money! Required: " + price + " Gold.");
        }

        RabbitFarm farm = rabbitFarmRepository.findByPlayerId(playerId)
                .orElseThrow(() -> new IllegalStateException("Player does not own a rabbit farm!"));

        Room freeRoom = null;
        if (farm.getStructures() != null) {
            for (Structure struct : farm.getStructures()) {
                if (struct.getRooms() != null) {
                    for (Room room : struct.getRooms()) {
                        int currentOccupants = room.getRabbits() != null ? room.getRabbits().size() : 0;
                        int maxSlots = room.getSlots() != null ? room.getSlots() : 0;
                        if (currentOccupants < maxSlots) {
                            freeRoom = room;
                            break;
                        }
                    }
                }
                if (freeRoom != null) break;
            }
        }

        if (freeRoom == null) {
            throw new RuntimeException("No free space in farm rooms! Build or expand rooms first.");
        }

        rabbit.setLife(rabbit.getMaxHp());
        rabbit.setStress(0.0f);

        player.setMoney(player.getMoney().subtract(price));
        rabbit.setPlayerId(playerId);
        rabbit.setStatus(RabbitStatus.IDLE);

        playerRepository.save(player);
        rabbitRepository.save(rabbit);

        if (freeRoom.getRabbits() == null) {
            freeRoom.setRabbits(new ArrayList<>());
        }
        freeRoom.getRabbits().add(rabbit);
        roomRepository.save(freeRoom);
    }

    @Transactional
    public void sellRabbit(Long playerId, Long rabbitId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + playerId));
        Rabbit rabbit = rabbitRepository.findById(rabbitId)
                .orElseThrow(() -> new RuntimeException("Rabbit not found with id: " + rabbitId));

        if (!playerId.equals(rabbit.getPlayerId())) {
            throw new IllegalStateException("You do not own this rabbit!");
        }

        if (!RabbitStatus.IDLE.equals(rabbit.getStatus())) {
            throw new IllegalStateException("Only IDLE adult rabbits can be sold! (Busy or young rabbits cannot be sold)");
        }

        BigDecimal sellValue = getRabbitValue(rabbit);
        player.setMoney(player.getMoney().add(sellValue));

        roomRepository.findByRabbitsContaining(rabbit).ifPresent(room -> {
            room.getRabbits().remove(rabbit);
            roomRepository.save(room);
        });

        rabbitRepository.delete(rabbit);
        playerRepository.save(player);
    }

    @Transactional
    public void buyFood(Long playerId, String foodType, Float amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero!");
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + playerId));
        RabbitFarm farm = rabbitFarmRepository.findByPlayerId(playerId)
                .orElseThrow(() -> new RuntimeException("Farm not found for player: " + playerId));

        BigDecimal totalCost = estimateFoodCost(foodType, amount);
        if (player.getMoney().compareTo(totalCost) < 0) {
            throw new RuntimeException("Not enough money! Required: " + totalCost + " Gold.");
        }

        player.setMoney(player.getMoney().subtract(totalCost));

        switch (foodType.toUpperCase()) {
            case "HAY" -> farm.setHayAmount((farm.getHayAmount() != null ? farm.getHayAmount() : 0f) + amount);
            case "CARROT", "CARROTS" -> farm.setCarrotAmount((farm.getCarrotAmount() != null ? farm.getCarrotAmount() : 0f) + amount);
            case "LETTUCE" -> farm.setLettuceAmount((farm.getLettuceAmount() != null ? farm.getLettuceAmount() : 0f) + amount);
            case "SPINACH" -> farm.setSpinachAmount((farm.getSpinachAmount() != null ? farm.getSpinachAmount() : 0f) + amount);
            default -> throw new IllegalArgumentException("Unknown food type: " + foodType);
        }

        playerRepository.save(player);
        rabbitFarmRepository.save(farm);
    }

    private BigDecimal getPriceForFoodType(String foodType) {
        String key = foodType.toUpperCase();
        if (foodPriceCache.containsKey(key)) {
            return foodPriceCache.get(key);
        }
        BigDecimal price = fetchPriceFromNass(key);
        foodPriceCache.put(key, price);
        return price;
    }

    private BigDecimal fetchPriceFromNass(String foodType) {
        try {
            switch (foodType) {
                case "HAY":
                    BigDecimal rawHayPrice = nassFetcher.fetchLatestPriceForCommodity(CommodityCode.HAY).getPrice();
                    if (rawHayPrice != null && rawHayPrice.compareTo(BigDecimal.ZERO) > 0) {
                        return rawHayPrice.divide(BigDecimal.valueOf(BasicConstants.TON), 4, RoundingMode.HALF_UP);
                    }
                    return BasicConstants.DEFAULT_HAY_PRICE_PER_KG;
                case "CARROT", "CARROTS":
                    BigDecimal rawCarrotPrice = nassFetcher.fetchLatestPriceForCommodity(CommodityCode.CARROTS).getPrice();
                    if (rawCarrotPrice != null && rawCarrotPrice.compareTo(BigDecimal.ZERO) > 0) {
                        return rawCarrotPrice.divide(BigDecimal.valueOf(BasicConstants.CETNAR), 4, RoundingMode.HALF_UP);
                    }
                    return BasicConstants.DEFAULT_CARROT_PRICE_PER_KG;
                case "LETTUCE":
                    BigDecimal rawLettucePrice = nassFetcher.fetchLatestPriceForCommodity(CommodityCode.LETTUCE).getPrice();
                    if (rawLettucePrice != null && rawLettucePrice.compareTo(BigDecimal.ZERO) > 0) {
                        return rawLettucePrice.divide(BigDecimal.valueOf(BasicConstants.CETNAR), 4, RoundingMode.HALF_UP);
                    }
                    return BasicConstants.DEFAULT_LETTUCE_PRICE_PER_KG;
                case "SPINACH":
                    BigDecimal rawSpinachPrice = nassFetcher.fetchLatestPriceForCommodity(CommodityCode.SPINACH).getPrice();
                    if (rawSpinachPrice != null && rawSpinachPrice.compareTo(BigDecimal.ZERO) > 0) {
                        return rawSpinachPrice.divide(BigDecimal.valueOf(BasicConstants.CETNAR), 4, RoundingMode.HALF_UP);
                    }
                    return BasicConstants.DEFAULT_SPINACH_PRICE_PER_KG;
                default:
                    return BigDecimal.valueOf(3.0);
            }
        } catch (Exception e) {
            return switch (foodType) {
                case "HAY" -> BasicConstants.DEFAULT_HAY_PRICE_PER_KG;
                case "CARROT", "CARROTS" -> BasicConstants.DEFAULT_CARROT_PRICE_PER_KG;
                case "LETTUCE" -> BasicConstants.DEFAULT_LETTUCE_PRICE_PER_KG;
                case "SPINACH" -> BasicConstants.DEFAULT_SPINACH_PRICE_PER_KG;
                default -> BigDecimal.valueOf(3.0);
            };
        }
    }
}