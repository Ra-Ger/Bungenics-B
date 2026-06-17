package com.kodilla.bungenics.controller;

import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.dto.RabbitDto;
import com.kodilla.bungenics.mapper.RabbitMapper;
import com.kodilla.bungenics.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;
    private final RabbitMapper rabbitMapper;

    @GetMapping("/food-price")
    public ResponseEntity<BigDecimal> getFoodPrice(@RequestParam String foodType, @RequestParam Float amount) {
        return ResponseEntity.ok(shopService.estimateFoodCost(foodType, amount));
    }

    @GetMapping("/rabbit-value/{rabbitId}")
    public ResponseEntity<BigDecimal> getRabbitValue(@PathVariable Long rabbitId) {
        return ResponseEntity.ok(shopService.calculateRabbitSellValue(rabbitId));
    }

    @GetMapping("/market")
    public ResponseEntity<List<RabbitDto>> getMarketRabbits() {
        List<Rabbit> marketBunnies = shopService.getMarketRabbits();
        return ResponseEntity.ok(rabbitMapper.mapToRabbitDtoList(marketBunnies));
    }

    @PostMapping("/buy-rabbit")
    public ResponseEntity<String> buyRabbit(@RequestParam Long playerId, @RequestParam Long rabbitId) {
        try {
            shopService.buyRabbit(playerId, rabbitId);
            return ResponseEntity.ok("Rabbit purchased successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<String> sellRabbit(@RequestParam Long playerId, @RequestParam Long rabbitId) {
        try {
            shopService.sellRabbit(playerId, rabbitId);
            return ResponseEntity.ok("Rabbit sold successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/buy")
    public ResponseEntity<String> buyFood(@RequestParam Long playerId, @RequestParam String foodType, @RequestParam Float amount) {
        try {
            shopService.buyFood(playerId, foodType, amount);
            return ResponseEntity.ok("Food purchased successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
