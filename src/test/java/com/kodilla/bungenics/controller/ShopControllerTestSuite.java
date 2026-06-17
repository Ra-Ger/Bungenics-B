package com.kodilla.bungenics.controller;

import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.dto.RabbitDto;
import com.kodilla.bungenics.mapper.RabbitMapper;
import com.kodilla.bungenics.service.ShopService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShopControllerTestSuite {

    @Mock
    private ShopService shopService;

    @Mock
    private RabbitMapper rabbitMapper;

    @InjectMocks
    private ShopController controller;

    @Test
    void shouldGetFoodPrice() {
        String foodType = "HAY";
        Float amount = 5.0f;
        BigDecimal expectedPrice = new BigDecimal("12.50");
        when(shopService.estimateFoodCost(foodType, amount)).thenReturn(expectedPrice);

        ResponseEntity<BigDecimal> response = controller.getFoodPrice(foodType, amount);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedPrice, response.getBody());
    }

    @Test
    void shouldGetRabbitValue() {
        Long rabbitId = 1L;
        BigDecimal value = new BigDecimal("250.00");
        when(shopService.calculateRabbitSellValue(rabbitId)).thenReturn(value);

        ResponseEntity<BigDecimal> response = controller.getRabbitValue(rabbitId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(value, response.getBody());
    }

    @Test
    void shouldGetMarketRabbits() {
        Rabbit rabbit = new Rabbit();
        RabbitDto dto = mock(RabbitDto.class);
        when(shopService.getMarketRabbits()).thenReturn(List.of(rabbit));
        when(rabbitMapper.mapToRabbitDtoList(anyList())).thenReturn(List.of(dto));

        ResponseEntity<List<RabbitDto>> response = controller.getMarketRabbits();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(dto, response.getBody().get(0));
    }

    @Test
    void shouldBuyRabbitSuccessfully() {
        Long playerId = 1L;
        Long rabbitId = 2L;
        doNothing().when(shopService).buyRabbit(playerId, rabbitId);

        ResponseEntity<String> response = controller.buyRabbit(playerId, rabbitId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Rabbit purchased successfully.", response.getBody());
    }

    @Test
    void shouldFailBuyRabbit() {
        Long playerId = 1L;
        Long rabbitId = 2L;
        doThrow(new RuntimeException("Not enough money")).when(shopService).buyRabbit(playerId, rabbitId);

        ResponseEntity<String> response = controller.buyRabbit(playerId, rabbitId);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Not enough money", response.getBody());
    }

    @Test
    void shouldSellRabbitSuccessfully() {
        Long playerId = 1L;
        Long rabbitId = 2L;
        doNothing().when(shopService).sellRabbit(playerId, rabbitId);

        ResponseEntity<String> response = controller.sellRabbit(playerId, rabbitId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Rabbit sold successfully.", response.getBody());
    }

    @Test
    void shouldFailSellRabbit() {
        Long playerId = 1L;
        Long rabbitId = 2L;
        doThrow(new RuntimeException("Rabbit not owned")).when(shopService).sellRabbit(playerId, rabbitId);

        ResponseEntity<String> response = controller.sellRabbit(playerId, rabbitId);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Rabbit not owned", response.getBody());
    }

    @Test
    void shouldBuyFoodSuccessfully() {
        Long playerId = 1L;
        String foodType = "CARROT";
        Float amount = 3.0f;
        doNothing().when(shopService).buyFood(playerId, foodType, amount);

        ResponseEntity<String> response = controller.buyFood(playerId, foodType, amount);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Food purchased successfully.", response.getBody());
    }

    @Test
    void shouldFailBuyFood() {
        Long playerId = 1L;
        String foodType = "CARROT";
        Float amount = 3.0f;
        doThrow(new RuntimeException("Insufficient funds")).when(shopService).buyFood(playerId, foodType, amount);

        ResponseEntity<String> response = controller.buyFood(playerId, foodType, amount);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Insufficient funds", response.getBody());
    }
}