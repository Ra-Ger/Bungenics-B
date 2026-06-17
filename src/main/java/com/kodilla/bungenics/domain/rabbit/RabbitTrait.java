package com.kodilla.bungenics.domain.rabbit;

import lombok.Getter;

@Getter
public enum RabbitTrait {
    // Positives
    HARDY("Hardy", TraitType.POSITIVE, "Loses 20% less life in combat."),
    QUICK_GROWER("Quick Grower", TraitType.POSITIVE, "Matures 50% faster."),
    LUCKY("Lucky", TraitType.POSITIVE, "Finds 10% of extra loot on adventures."),
    CALM("Calm", TraitType.POSITIVE, "Stress increase reduced by 20%."),
    FERTILE("Fertile", TraitType.POSITIVE, "25% chance of twin pregnancy."),

    // Negatives
    GLUTTON("Glutton", TraitType.NEGATIVE, "Consumes 20% more food."),
    FRAGILE("Fragile", TraitType.NEGATIVE, "Loses 20% more life in combat."),
    SKITTISH("Skittish", TraitType.NEGATIVE, "Stress increase increased by 20%."),
    LAZY("Lazy", TraitType.NEGATIVE, "Completes tasks 20% slower."),
    WEAK_GENES("Weak Genes", TraitType.NEGATIVE, "20% chance for offspring to receive an additional random negative trait.");

    public enum TraitType {
        POSITIVE,
        NEGATIVE
    }

    private final String displayName;
    private final TraitType type;
    private final String description;

    RabbitTrait(String displayName, TraitType type, String description) {
        this.displayName = displayName;
        this.type = type;
        this.description = description;
    }
}