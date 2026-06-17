package com.kodilla.bungenics.game.scheduler;

import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.game.factory.RabbitFactory;
import com.kodilla.bungenics.game.utils.Printers;
import com.kodilla.bungenics.service.RabbitService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GameEngineScheduler {

    private final RabbitService rabbitService;
    private final RabbitFactory rabbitFactory;



    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void gameTick() {
        System.out.println("--- GAME TICK ---");

        List<Rabbit> allRabbits = rabbitService.getAllRabbits();
        if(allRabbits.isEmpty()) {
            for(int i = 0; i < 3; i++) {
                Rabbit rabbit = rabbitFactory.createRandomRabbit();
                rabbitService.createRabbit(rabbit);
            }
            allRabbits = rabbitService.getAllRabbits();
        }

        for (Rabbit rabbit : allRabbits) {
            if (rabbit.getNutritionLevel() != null && rabbit.getNutritionLevel() > 0) {
                float newNutrition = Math.max(0f, rabbit.getNutritionLevel() - 1.0f);
                rabbit.setNutritionLevel(newNutrition);

                if (newNutrition < 20.0f) {
                    float currentStress = rabbit.getStress() != null ? rabbit.getStress() : 0f;
                    rabbit.setStress(Math.min(100f, currentStress + 2.0f));
                }

                rabbitService.updateRabbit(rabbit.getId(), rabbit);
            }
        }
        System.out.println("--- Bunnies ---");
        for (Rabbit rabbit : allRabbits) {
            Printers.printRabbit(rabbit);
        }
    }
}