package com.kodilla.bungenics.service;

import com.kodilla.bungenics.domain.player.Player;
import com.kodilla.bungenics.domain.player.RabbitFarm;
import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.domain.rabbit.RabbitStatus;
import com.kodilla.bungenics.domain.rabbit.SecondaryStats;
import com.kodilla.bungenics.domain.rabbitFarm.Room;
import com.kodilla.bungenics.domain.rabbitFarm.Structure;
import com.kodilla.bungenics.repository.PlayerRepository;
import com.kodilla.bungenics.repository.RabbitFarmRepository;
import com.kodilla.bungenics.repository.RabbitRepository;
import com.kodilla.bungenics.repository.RoomRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ShopServiceTest {

    @Autowired
    private ShopService shopService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private RabbitRepository rabbitRepository;

    @Autowired
    private RabbitFarmRepository rabbitFarmRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void testBuyFoodWithValidationAndIntegration() {
        // Given
        Player player = new Player();
        player.setName("TestPlayer");
        BigDecimal startingMoney = new BigDecimal("100.00");
        player.setMoney(startingMoney);

        RabbitFarm farm = new RabbitFarm();
        farm.setCarrotAmount(0f);
        farm.setPlayer(player);
        player.setRabbitFarm(farm);

        playerRepository.save(player);
        Long playerId = player.getId();

        // When
        shopService.buyFood(playerId, "CARROT", 10f);

        // Then
        Player updatedPlayer = playerRepository.findById(playerId).orElseThrow();

        assertTrue(updatedPlayer.getMoney().compareTo(startingMoney) < 0, "Money should be deducted after purchase");
        assertEquals(10f, updatedPlayer.getRabbitFarm().getCarrotAmount());
    }

    @Test
    void testBuyFoodValidationThrowsException() {
        // Given
        Player player = new Player();
        player.setName("PoorPlayer");
        player.setMoney(new BigDecimal("0.00"));
        playerRepository.save(player);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            shopService.buyFood(player.getId(), "CARROT", -5f);
        });
    }

    @Test
    void testSellRabbitIntegration() {
        // Given
        Player player = new Player();
        player.setName("RabbitOwner");
        player.setMoney(new BigDecimal("0.00"));
        playerRepository.save(player);

        Rabbit rabbit = new Rabbit();
        rabbit.setName("TestRabbit");
        rabbit.setWeight(3.5f);
        rabbit.setLife(100f);
        rabbit.setStatus(RabbitStatus.IDLE);
        rabbit.setPlayerId(player.getId()); // Fixed: Set rabbit ownership to current player

        SecondaryStats maxStats = new SecondaryStats();
        maxStats.setLife(100f);
        rabbit.setSecondaryStats(maxStats);

        rabbitRepository.save(rabbit);
        Long rabbitId = rabbit.getId();

        // Calculate expected value before selling
        BigDecimal expectedVal = shopService.calculateRabbitSellValue(rabbitId);

        // When
        shopService.sellRabbit(player.getId(), rabbitId);

        // Then
        Player updatedPlayer = playerRepository.findById(player.getId()).orElseThrow();

        assertEquals(0, expectedVal.compareTo(updatedPlayer.getMoney()), "Player money should match the rabbit sell value");
        assertFalse(rabbitRepository.findById(rabbitId).isPresent(), "Rabbit should be removed from database after sell");
    }

    @Test
    void testSellRabbitNotOwnedThrowsException() {
        // Given
        Player owner = new Player();
        owner.setName("OwnerPlayer");
        owner.setMoney(new BigDecimal("100.00"));
        playerRepository.save(owner);

        Player nonOwner = new Player();
        nonOwner.setName("NonOwnerPlayer");
        nonOwner.setMoney(new BigDecimal("100.00"));
        playerRepository.save(nonOwner);

        Rabbit rabbit = new Rabbit();
        rabbit.setName("UnownedRabbit");
        rabbit.setStatus(RabbitStatus.IDLE);
        rabbit.setPlayerId(owner.getId()); // Owned by 'owner', not 'nonOwner'
        rabbitRepository.save(rabbit);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            shopService.sellRabbit(nonOwner.getId(), rabbit.getId());
        });

        assertEquals("You do not own this rabbit!", exception.getMessage());
    }

    @Test
    void testSellRabbitNotIdleThrowsException() {
        // Given
        Player player = new Player();
        player.setName("BusyRabbitOwner");
        player.setMoney(new BigDecimal("100.00"));
        playerRepository.save(player);

        Rabbit rabbit = new Rabbit();
        rabbit.setName("BusyRabbit");
        rabbit.setStatus(RabbitStatus.ADVENTURE); // Rabbit is on an adventure
        rabbit.setPlayerId(player.getId());
        rabbitRepository.save(rabbit);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            shopService.sellRabbit(player.getId(), rabbit.getId());
        });

        assertTrue(exception.getMessage().contains("Only IDLE adult rabbits can be sold!"));
    }

    @Test
    void testBuyRabbitSuccess() {
        // Given
        Player player = new Player();
        player.setName("BuyerPlayer");
        player.setMoney(new BigDecimal("1000.00"));

        RabbitFarm farm = new RabbitFarm();
        farm.setPlayer(player);
        player.setRabbitFarm(farm);

        Structure structure = new Structure();
        Room room = new Room();
        room.setSlots(5);
        room.setRabbits(new ArrayList<>());
        structure.setRooms(List.of(room));
        farm.setStructures(List.of(structure));

        playerRepository.save(player);

        Rabbit marketRabbit = new Rabbit();
        marketRabbit.setName("MarketBunny");
        marketRabbit.setWeight(2.0f);
        marketRabbit.setLife(50f);
        marketRabbit.setStatus(RabbitStatus.MARKET);
        SecondaryStats stats = new SecondaryStats();
        stats.setLife(50f);
        marketRabbit.setSecondaryStats(stats);
        rabbitRepository.save(marketRabbit);

        // When
        shopService.buyRabbit(player.getId(), marketRabbit.getId());

        // Then
        Player updatedPlayer = playerRepository.findById(player.getId()).orElseThrow();
        Rabbit boughtRabbit = rabbitRepository.findById(marketRabbit.getId()).orElseThrow();

        assertEquals(player.getId(), boughtRabbit.getPlayerId(), "Purchased rabbit should belong to buyer");
        assertEquals(RabbitStatus.IDLE, boughtRabbit.getStatus(), "Status should be IDLE after purchase");
        assertTrue(updatedPlayer.getMoney().compareTo(new BigDecimal("1000.00")) < 0, "Player balance should decrease");
    }

    @Test
    void testBuyRabbitNotAvailableOnMarketThrowsException() {
        // Given
        Player player = new Player();
        player.setName("BuyerPlayer2");
        player.setMoney(new BigDecimal("1000.00"));
        playerRepository.save(player);

        Rabbit idleRabbit = new Rabbit();
        idleRabbit.setName("IdleBunny");
        idleRabbit.setStatus(RabbitStatus.IDLE);
        rabbitRepository.save(idleRabbit);

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            shopService.buyRabbit(player.getId(), idleRabbit.getId());
        });
    }

    @Test
    void testEstimateFoodCost() {
        // Given
        String foodType = "CARROT";
        Float amount = 10.0f;

        // When
        BigDecimal cost = shopService.estimateFoodCost(foodType, amount);

        // Then
        assertNotNull(cost);
        assertTrue(cost.compareTo(BigDecimal.ZERO) > 0, "Food cost estimate should be greater than zero");
    }
}