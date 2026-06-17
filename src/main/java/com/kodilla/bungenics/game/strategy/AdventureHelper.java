package com.kodilla.bungenics.game.strategy;

import com.kodilla.bungenics.domain.adventure.AdventureEvent;
import com.kodilla.bungenics.domain.rabbit.AttackType;
import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.domain.rabbit.RabbitTrait;
import com.kodilla.bungenics.domain.rabbit.SecondaryStats;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class AdventureHelper {

    private static final Random RANDOM = new Random();

    public record SkillTestResult(
            boolean isSuccess,
            float roll,
            float dc,
            float statValue,
            float maxRoll
    ) {}

    public enum CombatResultType {
        CRITICAL_DEFEAT,
        NORMAL_DEFEAT,
        NORMAL_VICTORY,
        CRITICAL_VICTORY
    }

    public record CombatTestResult(
            CombatResultType resultType,
            float roll,
            float dc,
            float statValue,
            float maxRoll
    ) {}

    public static SkillTestResult performSkillTest(float dc, float statValue) {
        float maxRoll = dc + statValue;
        float roll = RANDOM.nextFloat() * maxRoll;
        roll = (float) Math.floor(roll);

        boolean isSuccess = roll > dc;
        return new SkillTestResult(isSuccess, roll, dc, statValue, maxRoll);
    }

    public static CombatTestResult performCombatTest(float dc, float statValue) {
        float maxRoll = dc + statValue;
        float roll = RANDOM.nextFloat() * maxRoll;
        roll = (float) Math.floor(roll);

        float critDefeatThreshold = dc / 3.0f;
        float critVictoryThreshold = 2.0f * dc;

        CombatResultType type;
        if (roll <= critDefeatThreshold) {
            type = CombatResultType.CRITICAL_DEFEAT;
        } else if (roll <= dc) {
            type = CombatResultType.NORMAL_DEFEAT;
        } else if (roll <= critVictoryThreshold) {
            type = CombatResultType.NORMAL_VICTORY;
        } else {
            type = CombatResultType.CRITICAL_VICTORY;
        }

        return new CombatTestResult(type, roll, dc, statValue, maxRoll);
    }

    public static <T> T drawRandomAndRemove(List<T> pool, Random random) {
        if (pool == null || pool.isEmpty()) return null;
        int index = random.nextInt(pool.size());
        return pool.remove(index);
    }

    public static List<Supplier<AdventureEvent>> selectEventsWithExclusion(
            Random random,
            List<Supplier<AdventureEvent>> neutralPool,
            List<Supplier<AdventureEvent>> skillTestPool,
            List<Supplier<AdventureEvent>> combatPool) {

        List<Supplier<AdventureEvent>> selected = new ArrayList<>();

        Supplier<AdventureEvent> n1 = drawRandomAndRemove(neutralPool, random);
        if (n1 != null) selected.add(n1);

        Supplier<AdventureEvent> s1 = drawRandomAndRemove(skillTestPool, random);
        if (s1 != null) selected.add(s1);

        Supplier<AdventureEvent> n2 = drawRandomAndRemove(neutralPool, random);
        if (n2 != null) selected.add(n2);

        Supplier<AdventureEvent> c1 = drawRandomAndRemove(combatPool, random);
        if (c1 != null) selected.add(c1);

        Supplier<AdventureEvent> s2 = drawRandomAndRemove(skillTestPool, random);
        if (s2 != null) selected.add(s2);

        Supplier<AdventureEvent> n3 = drawRandomAndRemove(neutralPool, random);
        if (n3 != null) selected.add(n3);

        return selected;
    }

    public static AdventureEvent createEvent(String name, String result) {
        return createEvent(name, result, BigDecimal.ZERO, 0f, 0f, 0f);
    }

    public static AdventureEvent createEvent(String name, String result, BigDecimal gold, Float carrotReward, Float lettuceReward, Float spinachReward) {
        AdventureEvent event = new AdventureEvent();
        event.setName(name);
        event.setResult(result);
        event.setGoldReward(gold != null ? gold : BigDecimal.ZERO);
        event.setCarrotReward(carrotReward != null ? carrotReward : 0f);
        event.setLettuceReward(lettuceReward != null ? lettuceReward : 0f);
        event.setSpinachReward(spinachReward != null ? spinachReward : 0f);
        return event;
    }

    public static AttackModifier getAttackModifier(Rabbit rabbit) {
        SecondaryStats stats = rabbit != null ? rabbit.getSecondaryStats() : null;
        float statValue = 5.0f;
        AttackType attack = AttackType.CUDDLING;

        if (stats != null) {
            attack = stats.getPreferredAttack() != null ? stats.getPreferredAttack() : AttackType.CUDDLING;
            statValue = switch (attack) {
                case CUDDLING -> stats.getStrength() != null ? stats.getStrength() : 5f;
                case PETTING -> stats.getAgility() != null ? stats.getAgility() : 5f;
                case COMPLIMENTING -> stats.getIntelligence() != null ? stats.getIntelligence() : 5f;
            };
        }
        return new AttackModifier(attack, statValue);
    }

    public static float calculateCombatDamage(Rabbit rabbit, float baseDamage) {
        float damage = baseDamage;
        if (rabbit != null) {
            if (rabbit.hasTrait(RabbitTrait.HARDY)) {
                damage *= 0.80f;
            }
            if (rabbit.hasTrait(RabbitTrait.FRAGILE)) {
                damage *= 1.20f;
            }
        }
        return Math.round(damage);
    }

    public record AttackModifier(AttackType attackType, float statValue) {}
}