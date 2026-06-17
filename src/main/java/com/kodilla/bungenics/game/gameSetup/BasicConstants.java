package com.kodilla.bungenics.game.gameSetup;

import java.math.BigDecimal;

public class BasicConstants {

    // Food & Weight
    public static final float TON = 1000.0f;      // Hay is in metric tons, other crops in cwt (cetnar)
    public static final float CETNAR = 45.4f;
    public static final float FOOD_USED_PER_WEIGHT = 0.1f; // Rabbit consumes 100g (0.1kg) food per 1kg body weight per day

    // Default prices per KG (in Gold)
    public static final BigDecimal DEFAULT_HAY_PRICE_PER_KG = new BigDecimal("0.30");
    public static final BigDecimal DEFAULT_SPINACH_PRICE_PER_KG = new BigDecimal("3.00");
    public static final BigDecimal DEFAULT_CARROT_PRICE_PER_KG = new BigDecimal("1.50");
    public static final BigDecimal DEFAULT_LETTUCE_PRICE_PER_KG = new BigDecimal("2.00");

    // Time ratio
    public static final int MINUTES_PER_DAY = 10;    // 10 real-world minutes = 1 in-game day (600 seconds)
    public static final long SCHEDULER_TICK_RATE_MS = 5000L; // Game tick every 5 seconds (5000 ms)

    // Growth / Maturation days (In-game days until Kit becomes adult IDLE)
    public static final int SMALL_RABBITS_GROW_DAYS = 90;
    public static final int AVERAGE_RABBITS_GROW_DAYS = 120;
    public static final int LARGE_RABBITS_GROW_DAYS = 150;

    // Rabbit
    public static final float BASIC_RABBIT_PRICE = 10.0f;
    public static final float DEFAULT_MAX_LIFETIME_DAYS = 1825.0f;

    // Hunger Decay Rate (% loss per in-game day)
    public static final float HUNGER_DECAY_PER_DAY = 100.0f;

    // Action & Cooldown Durations (in real-world minutes)
    public static final int BREEDING_DURATION_MINUTES = 5;
    public static final int ADVENTURE_DURATION_MINUTES = 1;
    public static final int RESTING_DURATION_MINUTES = 5; // Resting duration post-expedition/breeding/training
    public static final long TRAINING_DURATION_MINUTES = 5L;

    // Warren & Homelessness Mechanics
    public static final float HOMELESS_FOOD_CONSUMPTION_MULTIPLIER = 2.0f; // Homeless rabbits eat 2x hay
    public static final float HOMELESS_STRESS_INCREASE_PER_TICK = 1.5f;    // Stress increase per tick when homeless
    public static final float WARREN_LIFE_REGEN_PER_TICK = 0.5f;

    // Stress reduction constants & multipliers
    public static final float BASE_STRESS_REDUCTION_PER_TICK = 0.5f;
    public static final float WARREN_STRESS_REGEN_MULTIPLIER = 0.1f;    // 10x slower stress reduction in Warren (reduced 10-fold)
    public static final float PLAYHOUSE_STRESS_REGEN_MULTIPLIER = 1.0f; // Full stress reduction rate in Playhouse (10x faster than Warren)

    public static final float WARREN_STRESS_REDUCTION_PER_TICK = BASE_STRESS_REDUCTION_PER_TICK * WARREN_STRESS_REGEN_MULTIPLIER;
    public static final float PLAYHOUSE_STRESS_REDUCTION_PER_TICK = BASE_STRESS_REDUCTION_PER_TICK * PLAYHOUSE_STRESS_REGEN_MULTIPLIER;

    // Structure costs
    public static final BigDecimal STRUCTURE_BUILD_COST = new BigDecimal("100.00");
    public static final BigDecimal ROOM_BUILD_COST = new BigDecimal("50.00");
    public static final BigDecimal ROOM_EXPAND_COST = new BigDecimal("40.00");

    public static final double TRAINING_COST_MULTIPLIER = 1.5;


}