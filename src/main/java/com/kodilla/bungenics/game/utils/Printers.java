package com.kodilla.bungenics.game.utils;

import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.domain.rabbit.SecondaryStats;
import com.kodilla.bungenics.domain.rabbit.Trait;

import java.util.List;

public class Printers {
    public static void printRabbit(Rabbit rabbit) {
        if (rabbit == null) {
            System.out.println("Rabbit is null");
            return;
        }

        System.out.println("========== RABBIT DETAILS ==========");
        System.out.printf("ID:             %d%n", rabbit.getId());
        System.out.printf("Name:           %s%n", rabbit.getName());
        System.out.printf("Breed:          %s%n", rabbit.getBreed());
        System.out.printf("Weight:         %.2f%n", rabbit.getWeight());
        System.out.printf("Nutrition Lvl:  %.2f%n", rabbit.getNutritionLevel());
        System.out.printf("Life:           %.2f%n", rabbit.getLife());
        System.out.printf("Stress:         %.2f%n", rabbit.getStress());
        System.out.printf("Age:            %.2f%n", rabbit.getAge());
        System.out.printf("Status:         %s%n", rabbit.getStatus());
        System.out.printf("Mother ID:      %s%n",
                rabbit.getMother() != null ? rabbit.getMother() : "N/A");
        System.out.printf("Father ID:      %s%n",
                rabbit.getFather() != null ? rabbit.getFather() : "N/A");

        SecondaryStats stats = rabbit.getSecondaryStats();
        if (stats != null) {
            System.out.println("--------- Secondary Stats ---------");
            printSecondaryStats(stats);
        } else {
            System.out.println("Secondary Stats: N/A");
        }

        List<Trait> traits = rabbit.getTraits();
        if (traits != null && !traits.isEmpty()) {
            System.out.println("--------- Traits ---------");
            for (int i = 0; i < traits.size(); i++) {
                Trait t = traits.get(i);
                System.out.printf("  %d. Type: %s%n", i + 1,
                        t.getTraitType() != null ? t.getTraitType() : "UNKNOWN");
            }
        } else {
            System.out.println("Traits: N/A");
        }

        System.out.println("====================================\n");
    }

    private static void printSecondaryStats(SecondaryStats stats) {
        System.out.printf("  Max Weight:        %.2f%n", stats.getWeight());
        System.out.printf("  Max Nutrition Lvl: %.2f%n", stats.getNutritionLevel());
        System.out.printf("  Max Life:          %.2f%n", stats.getLife());
        System.out.printf("  Max Stress:        %.2f%n", stats.getStress());
        System.out.printf("  Max Age:           %.2f%n", stats.getAge());
        System.out.printf("  Strength:          %.2f%n", stats.getStrength());
        System.out.printf("  Agility:           %.2f%n", stats.getAgility());
        System.out.printf("  Intelligence:      %.2f%n", stats.getIntelligence());
        System.out.printf("  Preferred Attack:  %s%n",
                stats.getPreferredAttack() != null ? stats.getPreferredAttack() : "N/A");
    }
}

