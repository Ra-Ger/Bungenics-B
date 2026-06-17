package com.kodilla.bungenics.domain.rabbit;

import lombok.Getter;

@Getter
public enum Breed {
    WHITE_DWARF(0.50f, 1.13f, 80f, 60f, 2f, 12f, 4f),
    LIONHEAD(1.36f, 1.70f, 220f, 100f, 5f, 6f, 3f),
    FUZZY_LOP(1.60f, 1.80f, 150f, 110f, 4f, 7f, 2f),
    ANGORA(2.00f, 5.50f, 130f, 100f, 5f, 3f, 12f),
    FOX(2.50f, 3.20f, 140f, 130f, 6f, 9f, 5f),
    DALMATIAN(2.70f, 3.60f, 160f, 160f, 8f, 6f, 3f),
    HARLEQUIN(2.70f, 3.60f, 90f, 120f, 4f, 9f, 8f),
    CHINCHILLA(4.50f, 7.30f, 130f, 190f, 12f, 6f, 3f),
    GIANT(6.40f, 11.30f, 140f, 350f, 8f, 2f, 5f);

    private final float minWeight;
    private final float maxWeight;
    private final float maxStress;
    private final float maxLife;
    private final float baseStrength;
    private final float baseAgility;
    private final float baseIntelligence;

    Breed(float minWeight, float maxWeight, float maxStress, float maxLife, float baseStrength, float baseAgility, float baseIntelligence) {
        this.minWeight = minWeight;
        this.maxWeight = maxWeight;
        this.maxStress = maxStress;
        this.maxLife = maxLife;
        this.baseStrength = baseStrength;
        this.baseAgility = baseAgility;
        this.baseIntelligence = baseIntelligence;
    }

    public static Breed parseOrDefault(String breedName) {
        if (breedName == null || breedName.isBlank()) {
            return WHITE_DWARF;
        }
        try {
            return Breed.valueOf(breedName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return WHITE_DWARF;
        }
    }
}
