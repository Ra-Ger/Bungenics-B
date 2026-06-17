package com.kodilla.bungenics.game.strategy;

import com.kodilla.bungenics.domain.adventure.AdventureEvent;
import com.kodilla.bungenics.domain.rabbit.AttackType;
import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.domain.rabbit.RabbitTrait;
import com.kodilla.bungenics.domain.rabbit.SecondaryStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdventureHelperTest {

    // --------------- performSkillTest ---------------
    @Nested
    class PerformSkillTest {

        @RepeatedTest(100)
        void shouldHaveConsistentMaxRollAndSuccessLogic() {
            float dc = 5.0f;
            float stat = 3.0f;
            AdventureHelper.SkillTestResult result = AdventureHelper.performSkillTest(dc, stat);

            assertEquals(dc + stat, result.maxRoll(), "maxRoll must be dc + statValue");
            assertTrue(result.roll() >= 0 && result.roll() < result.maxRoll(),
                    "roll must be in [0, maxRoll)");
            assertEquals(result.roll() > result.dc(), result.isSuccess(),
                    "success is true exactly when roll > dc");
        }

        @Test
        void shouldHandleZeroDcAndStat() {
            AdventureHelper.SkillTestResult result = AdventureHelper.performSkillTest(0f, 0f);
            assertEquals(0f, result.maxRoll());
            assertEquals(0f, result.roll()); // floor(0 * nextFloat) = 0
            assertFalse(result.isSuccess()); // 0 not > 0
        }
    }

    // --------------- performCombatTest ---------------
    @Nested
    class PerformCombatTest {

        @RepeatedTest(100)
        void shouldHaveConsistentThresholdsAndResultType() {
            float dc = 30.0f;
            float stat = 10.0f;
            AdventureHelper.CombatTestResult result = AdventureHelper.performCombatTest(dc, stat);

            assertEquals(dc + stat, result.maxRoll());
            assertTrue(result.roll() >= 0 && result.roll() < result.maxRoll());

            float critDefeat = dc / 3.0f;
            float critVictory = 2.0f * dc;

            AdventureHelper.CombatResultType expectedType;
            if (result.roll() <= critDefeat) {
                expectedType = AdventureHelper.CombatResultType.CRITICAL_DEFEAT;
            } else if (result.roll() <= dc) {
                expectedType = AdventureHelper.CombatResultType.NORMAL_DEFEAT;
            } else if (result.roll() <= critVictory) {
                expectedType = AdventureHelper.CombatResultType.NORMAL_VICTORY;
            } else {
                expectedType = AdventureHelper.CombatResultType.CRITICAL_VICTORY;
            }
            assertEquals(expectedType, result.resultType());
        }

        @Test
        void zeroDcShouldMapOnlyCritDefeatAndCritVictory() {
            float dc = 0f;
            float stat = 10f;
            AdventureHelper.CombatTestResult result = AdventureHelper.performCombatTest(dc, stat);
            if (result.roll() == 0) {
                assertEquals(AdventureHelper.CombatResultType.CRITICAL_DEFEAT, result.resultType());
            } else {
                assertEquals(AdventureHelper.CombatResultType.CRITICAL_VICTORY, result.resultType());
            }
        }
    }

    // --------------- drawRandomAndRemove ---------------
    @Nested
    class DrawRandomAndRemove {

        private Random seededRandom;

        @BeforeEach
        void setUp() {
            seededRandom = new Random(12345); // deterministic
        }

        @Test
        void shouldReturnNullWhenPoolIsNull() {
            assertNull(AdventureHelper.drawRandomAndRemove(null, seededRandom));
        }

        @Test
        void shouldReturnNullWhenPoolIsEmpty() {
            List<String> empty = new ArrayList<>();
            assertNull(AdventureHelper.drawRandomAndRemove(empty, seededRandom));
        }

        @Test
        void shouldRemoveAndReturnElement() {
            List<String> pool = new ArrayList<>(List.of("A", "B", "C"));
            int initialSize = pool.size();

            String drawn = AdventureHelper.drawRandomAndRemove(pool, seededRandom);
            assertNotNull(drawn);
            assertEquals(initialSize - 1, pool.size());
            assertFalse(pool.contains(drawn));
        }

        @Test
        void shouldUseGivenRandomToChooseIndex() {
            List<Integer> pool = new ArrayList<>(List.of(10, 20, 30));
            int initialSize = pool.size();
            Integer removed = AdventureHelper.drawRandomAndRemove(pool, seededRandom);
            assertEquals(initialSize - 1, pool.size());
            assertNotNull(removed);
        }
    }

    // --------------- selectEventsWithExclusion ---------------
    @Nested
    class SelectEventsWithExclusion {

        private Random seededRandom;
        private Supplier<AdventureEvent> eventA, eventB, eventC, eventD, eventE, eventF;

        @BeforeEach
        void setUp() {
            seededRandom = new Random(42);
            eventA = () -> new AdventureEvent();
            eventB = () -> new AdventureEvent();
            eventC = () -> new AdventureEvent();
            eventD = () -> new AdventureEvent();
            eventE = () -> new AdventureEvent();
            eventF = () -> new AdventureEvent();
        }

        @Test
        void shouldSelectInCorrectOrderAndRemoveFromPools() {
            List<Supplier<AdventureEvent>> neutral = new ArrayList<>(List.of(eventA, eventB, eventC));
            List<Supplier<AdventureEvent>> skill = new ArrayList<>(List.of(eventD, eventE));
            List<Supplier<AdventureEvent>> combat = new ArrayList<>(List.of(eventF));

            List<Supplier<AdventureEvent>> selected =
                    AdventureHelper.selectEventsWithExclusion(seededRandom, neutral, skill, combat);

            assertEquals(6, selected.size());
            assertEquals(6, selected.stream().distinct().count());
            assertEquals(0, neutral.size());
            assertEquals(0, skill.size());
            assertEquals(0, combat.size());
        }

        @Test
        void shouldOmitEventsWhenPoolsBecomeEmpty() {
            // Given
            List<Supplier<AdventureEvent>> neutral = new ArrayList<>(List.of(eventA));
            List<Supplier<AdventureEvent>> skill = new ArrayList<>();           // empty
            List<Supplier<AdventureEvent>> combat = new ArrayList<>(List.of(eventF));

            // When
            List<Supplier<AdventureEvent>> selected =
                    AdventureHelper.selectEventsWithExclusion(seededRandom, neutral, skill, combat);

            // Then
            assertEquals(2, selected.size(), "Should contain exactly 2 successfully drawn events");
            assertSame(eventA, selected.get(0), "First drawn must be from neutral pool");
            assertSame(eventF, selected.get(1), "Second drawn must be from combat pool");

            // All pools should be exhausted after the draws
            assertTrue(neutral.isEmpty());
            assertTrue(skill.isEmpty());
            assertTrue(combat.isEmpty());
        }
    }

    // --------------- createEvent ---------------
    @Nested
    class CreateEvent {

        @Test
        void shouldCreateEventWithDefaults() {
            AdventureEvent event = AdventureHelper.createEvent("name", "result");
            assertEquals("name", event.getName());
            assertEquals("result", event.getResult());
            assertEquals(BigDecimal.ZERO, event.getGoldReward());
            assertEquals(0f, event.getCarrotReward());
            assertEquals(0f, event.getLettuceReward());
            assertEquals(0f, event.getSpinachReward());
        }

        @Test
        void shouldHandleNullRewardsAsZero() {
            AdventureEvent event = AdventureHelper.createEvent("x", "y", null, null, null, null);
            assertEquals(BigDecimal.ZERO, event.getGoldReward());
            assertEquals(0f, event.getCarrotReward());
            assertEquals(0f, event.getLettuceReward());
            assertEquals(0f, event.getSpinachReward());
        }

        @Test
        void shouldSetAllFieldsCorrectly() {
            AdventureEvent event = AdventureHelper.createEvent(
                    "Quest", "Completed",
                    new BigDecimal("10.5"), 2.5f, 1.0f, 3.0f);
            assertEquals("Quest", event.getName());
            assertEquals("Completed", event.getResult());
            assertEquals(new BigDecimal("10.5"), event.getGoldReward());
            assertEquals(2.5f, event.getCarrotReward());
            assertEquals(1.0f, event.getLettuceReward());
            assertEquals(3.0f, event.getSpinachReward());
        }
    }

    // --------------- getAttackModifier ---------------
    @Nested
    class GetAttackModifier {

        @Test
        void shouldReturnCuddlingDefaultWhenRabbitNull() {
            AdventureHelper.AttackModifier mod = AdventureHelper.getAttackModifier(null);
            assertEquals(AttackType.CUDDLING, mod.attackType());
            assertEquals(5.0f, mod.statValue());
        }

        @Test
        void shouldUseDefaultWhenStatsMissing() {
            Rabbit rabbit = mock(Rabbit.class);
            when(rabbit.getSecondaryStats()).thenReturn(null);
            AdventureHelper.AttackModifier mod = AdventureHelper.getAttackModifier(rabbit);
            assertEquals(AttackType.CUDDLING, mod.attackType());
            assertEquals(5.0f, mod.statValue());
        }

        @ParameterizedTest
        @CsvSource({
                "CUDDLING, STRENGTH, 7.0",
                "PETTING, AGILITY, 8.5",
                "COMPLIMENTING, INTELLIGENCE, 9.2"
        })
        void shouldUseStatBasedOnPreferredAttack(AttackType attack, String statField, float expectedValue) {
            SecondaryStats stats = mock(SecondaryStats.class);
            when(stats.getPreferredAttack()).thenReturn(attack);
            // Symulujemy odpowiednie gettery
            switch (attack) {
                case CUDDLING -> when(stats.getStrength()).thenReturn(expectedValue);
                case PETTING -> when(stats.getAgility()).thenReturn(expectedValue);
                case COMPLIMENTING -> when(stats.getIntelligence()).thenReturn(expectedValue);
            }

            Rabbit rabbit = mock(Rabbit.class);
            when(rabbit.getSecondaryStats()).thenReturn(stats);

            AdventureHelper.AttackModifier mod = AdventureHelper.getAttackModifier(rabbit);
            assertEquals(attack, mod.attackType());
            assertEquals(expectedValue, mod.statValue());
        }

        @Test
        void shouldFallbackToDefaultWhenPreferredAttackHasNullStat() {
            SecondaryStats stats = mock(SecondaryStats.class);
            when(stats.getPreferredAttack()).thenReturn(AttackType.PETTING);
            when(stats.getAgility()).thenReturn(null); // brak statystyki

            Rabbit rabbit = mock(Rabbit.class);
            when(rabbit.getSecondaryStats()).thenReturn(stats);

            AdventureHelper.AttackModifier mod = AdventureHelper.getAttackModifier(rabbit);
            assertEquals(AttackType.PETTING, mod.attackType());
            assertEquals(5.0f, mod.statValue()); // domyślna wartość
        }
    }

    // --------------- calculateCombatDamage ---------------
    @Nested
    class CalculateCombatDamage {

        @Test
        void shouldReturnBaseDamageWhenRabbitNull() {
            assertEquals(100, AdventureHelper.calculateCombatDamage(null, 100));
        }

        @Test
        void shouldApplyHardyTraitReduction() {
            Rabbit rabbit = mock(Rabbit.class);
            when(rabbit.hasTrait(RabbitTrait.HARDY)).thenReturn(true);
            when(rabbit.hasTrait(RabbitTrait.FRAGILE)).thenReturn(false);

            float result = AdventureHelper.calculateCombatDamage(rabbit, 100);
            assertEquals(80, result); // 100 * 0.8, zaokrąglone
        }

        @Test
        void shouldApplyFragileTraitIncrease() {
            Rabbit rabbit = mock(Rabbit.class);
            when(rabbit.hasTrait(RabbitTrait.HARDY)).thenReturn(false);
            when(rabbit.hasTrait(RabbitTrait.FRAGILE)).thenReturn(true);

            float result = AdventureHelper.calculateCombatDamage(rabbit, 100);
            assertEquals(120, result); // 100 * 1.2
        }

        @Test
        void shouldCombineTraits() {
            Rabbit rabbit = mock(Rabbit.class);
            when(rabbit.hasTrait(RabbitTrait.HARDY)).thenReturn(true);
            when(rabbit.hasTrait(RabbitTrait.FRAGILE)).thenReturn(true);

            // 100 * 0.8 * 1.2 = 96
            float result = AdventureHelper.calculateCombatDamage(rabbit, 100);
            assertEquals(96, result);
        }

        @Test
        void shouldRoundResultToWholeNumber() {
            Rabbit rabbit = mock(Rabbit.class);
            when(rabbit.hasTrait(RabbitTrait.HARDY)).thenReturn(false);
            when(rabbit.hasTrait(RabbitTrait.FRAGILE)).thenReturn(false);

            assertEquals(3, AdventureHelper.calculateCombatDamage(rabbit, 3.2f));
            assertEquals(4, AdventureHelper.calculateCombatDamage(rabbit, 3.6f));
        }
    }
}