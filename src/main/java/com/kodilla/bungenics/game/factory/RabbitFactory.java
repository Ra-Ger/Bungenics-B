package com.kodilla.bungenics.game.factory;

import com.kodilla.bungenics.domain.rabbit.Breed;
import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.domain.rabbit.SecondaryStats;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class RabbitFactory {

    List<String> names = List.of("Judy", "Beri", "Lori","Bugs","Whisker");

    private final Random random = new Random();

    public Rabbit createRandomRabbit() {
        Rabbit rabbit = new Rabbit();

        Breed[] allBreeds = Breed.values();
        Breed randomBreed = allBreeds[random.nextInt(allBreeds.length)];
        rabbit.setBreed(randomBreed);

        rabbit.setName(names.get(random.nextInt(names.size())) + " " + random.nextInt(9999));

        float baseWeight = generateWeightForBreed(randomBreed);
        rabbit.setWeight(baseWeight);

        rabbit.setNutritionLevel(100f);
        rabbit.setLife(100f);
        rabbit.setStress(0f);
        rabbit.setAge(1f);
        rabbit.setStatus("IDLE");

        SecondaryStats stats = new SecondaryStats();
        stats.setWeight(baseWeight * 1.5f);
        stats.setNutritionLevel(100f);
        stats.setLife(100f);
        stats.setStress(100f);
        stats.setAge(12f);
        stats.setStrength(1f + random.nextFloat() * 10f);
        stats.setAgility(1f + random.nextFloat() * 10f);
        stats.setIntelligence(1f + random.nextFloat() * 10f);

        rabbit.setSecondaryStats(stats);

        return rabbit;
    }

    private float generateWeightForBreed(Breed breed) {
        return switch (breed) {
            case GIANT -> 5.0f + random.nextFloat() * 3.0f; // 5 - 8 kg
            case WHITE_DWARF, LIONHEAD -> 1.0f + random.nextFloat() * 1.5f; // 1 - 2.5 kg
            default -> 2.5f + random.nextFloat() * 2.0f; // 2.5 - 4.5 kg
        };
    }
}
