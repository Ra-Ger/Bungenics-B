package com.kodilla.bungenics.game.factory;

import com.kodilla.bungenics.domain.rabbit.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class RabbitFactoryTest {

    private RabbitFactory factory;
    private Random seededRandom;

    @BeforeEach
    void setUp() throws Exception {
        factory = new RabbitFactory();
        seededRandom = new Random(42L);
        // inject seeded random via reflection
        Field randomField = RabbitFactory.class.getDeclaredField("random");
        randomField.setAccessible(true);
        randomField.set(factory, seededRandom);
    }

    // ---------- createRandomRabbit ----------
    @Nested
    class CreateRandomRabbit {

        @Test
        void shouldCreateRabbitWithGivenBreedAndSex() {
            Rabbit rabbit = factory.createRandomRabbit(1L, "ANGORA", "MALE");

            assertThat(rabbit).isNotNull();
            assertThat(rabbit.getPlayerId()).isEqualTo(1L);
            assertThat(rabbit.getBreed()).isEqualTo("ANGORA");
            assertThat(rabbit.getSex()).isEqualTo("MALE");
            assertThat(rabbit.getStatus()).isEqualTo(RabbitStatus.IDLE);
            assertThat(rabbit.getLife()).isGreaterThan(0f);
            assertThat(rabbit.getStress()).isEqualTo(0f);
            assertThat(rabbit.getAge()).isEqualTo(1.0f);
            assertThat(rabbit.getNutritionLevel()).isEqualTo(100f);

            SecondaryStats stats = rabbit.getSecondaryStats();
            assertThat(stats).isNotNull();
            assertThat(stats.getWeight())
                    .isBetween(Breed.ANGORA.getMinWeight(), Breed.ANGORA.getMaxWeight());
            assertThat(stats.getLife()).isEqualTo(Breed.ANGORA.getMaxLife());
            assertThat(stats.getStress()).isEqualTo(Breed.ANGORA.getMaxStress());
            assertThat(stats.getBasicStrength()).isBetween(
                    Breed.ANGORA.getBaseStrength(), Breed.ANGORA.getBaseStrength() + 3.0f);
            assertThat(stats.getBasicAgility()).isBetween(
                    Breed.ANGORA.getBaseAgility(), Breed.ANGORA.getBaseAgility() + 3.0f);
            assertThat(stats.getBasicIntelligence()).isBetween(
                    Breed.ANGORA.getBaseIntelligence(), Breed.ANGORA.getBaseIntelligence() + 3.0f);

            Set<RabbitTrait> traits = rabbit.getTraits();
            assertThat(traits).isNotEmpty();
            // at least one positive trait
            assertThat(traits.stream().anyMatch(t -> t.getType() == RabbitTrait.TraitType.POSITIVE)).isTrue();
            // name generated
            assertThat(rabbit.getName()).isNotBlank();
        }

        @Test
        void shouldAssignRandomBreedWhenNull() {
            Rabbit rabbit = factory.createRandomRabbit(2L, null, "FEMALE");
            assertThat(rabbit.getBreed()).isNotNull().isNotBlank();
            assertThat(Breed.parseOrDefault(rabbit.getBreed())).isNotEqualTo(Breed.WHITE_DWARF); // not default if exists
        }

        @Test
        void shouldAssignRandomBreedWhenBlank() {
            Rabbit rabbit = factory.createRandomRabbit(2L, "  ", "MALE");
            assertThat(rabbit.getBreed()).isNotNull().isNotBlank();
        }

        @Test
        void shouldAssignRandomSexWhenNull() {
            Rabbit rabbit = factory.createRandomRabbit(3L, "LIONHEAD", null);
            assertThat(rabbit.getSex()).isIn("MALE", "FEMALE");
        }

        @Test
        void shouldAssignRandomSexWhenBlank() {
            Rabbit rabbit = factory.createRandomRabbit(3L, "LIONHEAD", "");
            assertThat(rabbit.getSex()).isIn("MALE", "FEMALE");
        }

        @Test
        void shouldGenerateWeightWithinBreedRange() {
            Rabbit rabbit = factory.createRandomRabbit(4L, "FUZZY_LOP", "FEMALE");
            float weight = rabbit.getSecondaryStats().getWeight();
            assertThat(weight).isBetween(1.60f, 1.80f);
        }

        @Test
        void shouldSetStatsCorrectly() {
            Rabbit rabbit = factory.createRandomRabbit(5L, "HARLEQUIN", "MALE");
            SecondaryStats stats = rabbit.getSecondaryStats();
            assertEquals(Breed.HARLEQUIN.getMaxLife(), stats.getLife());
            assertEquals(Breed.HARLEQUIN.getMaxStress(), stats.getStress());
            assertThat(stats.getBasicStrength()).isBetween(4f, 7f);
            assertThat(stats.getBasicAgility()).isBetween(9f, 12f);
            assertThat(stats.getBasicIntelligence()).isBetween(8f, 11f);
        }

        @Test
        void shouldAssignPreferredAttackRandomly() {
            Rabbit rabbit = factory.createRandomRabbit(6L, "GIANT", "MALE");
            assertThat(rabbit.getSecondaryStats().getPreferredAttack()).isIn(AttackType.values());
        }

        @Test
        void shouldIncludeAtLeastOnePositiveTrait() {
            Rabbit rabbit = factory.createRandomRabbit(7L, "DALMATIAN", "FEMALE");
            assertThat(rabbit.getTraits().stream().anyMatch(t -> t.getType() == RabbitTrait.TraitType.POSITIVE))
                    .isTrue();
        }

        @Test
        void shouldPossiblyIncludeNegativeTrait() {
            // Run multiple times to ensure sometimes a negative trait appears
            boolean negativeFound = false;
            for (int i = 0; i < 20; i++) {
                Rabbit rabbit = factory.createRandomRabbit(8L + i, "WHITE_DWARF", "FEMALE");
                if (rabbit.getTraits().stream().anyMatch(t -> t.getType() == RabbitTrait.TraitType.NEGATIVE)) {
                    negativeFound = true;
                    break;
                }
            }
            assertThat(negativeFound).isTrue();
        }

        @Test
        void shouldGenerateNameBasedOnSex() {
            Rabbit male = factory.createRandomRabbit(9L, "FOX", "MALE");
            assertThat(male.getName()).isIn("Thumper", "Donald", "Soarin", "Oreo", "Bugs", "Jasper", "Gizmo", "Barnaby", "Oliver", "Felix", "Leszczynek", "Czubak");

            Rabbit female = factory.createRandomRabbit(10L, "FOX", "FEMALE");
            assertThat(female.getName()).isIn("Bella", "Niu-Nia", "Sweetie", "Twilight", "Daisy", "Lola", "Luna", "Coco", "Rosie", "Lily", "Molly");
        }

        @Test
        void shouldSetMaxLifetimeBetween14and18() {
            Rabbit rabbit = factory.createRandomRabbit(11L, "CHINCHILLA", "MALE");
            assertThat(rabbit.getMaxLifetime()).isBetween(14.0f, 18.0f);
        }
    }

    // ---------- createKit ----------
    @Nested
    class CreateKit {

        private Rabbit mother;
        private Rabbit father;

        @BeforeEach
        void setUpParents() {
            // Build deterministic parents with specific traits and stats
            mother = Rabbit.builder()
                    .id(100L)
                    .playerId(1L)
                    .breed("ANGORA")
                    .sex("FEMALE")
                    .secondaryStats(SecondaryStats.builder()
                            .basicStrength(7.0f)
                            .basicAgility(5.0f)
                            .basicIntelligence(14.0f)
                            .strength(7.0f)
                            .agility(5.0f)
                            .intelligence(14.0f)
                            .build())
                    .traits(Set.of(RabbitTrait.HARDY, RabbitTrait.GLUTTON))
                    .build();

            father = Rabbit.builder()
                    .id(200L)
                    .playerId(1L)
                    .breed("LIONHEAD")
                    .sex("MALE")
                    .secondaryStats(SecondaryStats.builder()
                            .basicStrength(5.0f)
                            .basicAgility(9.0f)
                            .basicIntelligence(3.0f)
                            .strength(5.0f)
                            .agility(9.0f)
                            .intelligence(3.0f)
                            .build())
                    .traits(Set.of(RabbitTrait.HARDY, RabbitTrait.CALM))
                    .build();
        }

        @Test
        void shouldCreateKitWithCorrectParentReferences() {
            Rabbit kit = factory.createKit(mother, father);
            assertThat(kit.getMotherId()).isEqualTo(100L);
            assertThat(kit.getFatherId()).isEqualTo(200L);
            assertThat(kit.getPlayerId()).isEqualTo(1L);
            assertThat(kit.getStatus()).isEqualTo(RabbitStatus.KIT);
            assertThat(kit.getAge()).isEqualTo(0f);
            assertThat(kit.getLife()).isGreaterThan(0f);
            assertThat(kit.getStress()).isEqualTo(0f);
            assertThat(kit.getNutritionLevel()).isEqualTo(100f);
        }

        @Test
        void shouldInheritBreedFromRandomParent() {
            Rabbit kit = factory.createKit(mother, father);
            assertThat(kit.getBreed()).isIn("ANGORA", "LIONHEAD");
        }

        @Test
        void shouldAssignRandomSex() {
            Rabbit kit = factory.createKit(mother, father);
            assertThat(kit.getSex()).isIn("MALE", "FEMALE");
        }

        @Test
        void shouldHaveInitialWeightProportionalToAdultWeight() {
            Rabbit kit = factory.createKit(mother, father);
            float breedMin, breedMax;
            if (kit.getBreed().equals("ANGORA")) {
                breedMin = Breed.ANGORA.getMinWeight();
                breedMax = Breed.ANGORA.getMaxWeight();
            } else {
                breedMin = Breed.LIONHEAD.getMinWeight();
                breedMax = Breed.LIONHEAD.getMaxWeight();
            }
            float adultWeight = kit.getAdultWeight();
            assertThat(adultWeight).isBetween(breedMin, breedMax);
            assertThat(kit.getWeight()).isEqualTo(Math.max(0.15f, adultWeight * 0.08f));
        }

        @Test
        void shouldInheritStatsBetweenParents() {
            Rabbit kit = factory.createKit(mother, father);

            SecondaryStats stats = kit.getSecondaryStats();
            float minStr = Math.min(5.0f, 7.0f);
            float maxStr = Math.max(5.0f, 7.0f);
            assertThat(stats.getStrength()).isBetween(minStr, maxStr);
            assertThat(stats.getBasicStrength()).isBetween(minStr, maxStr);

            float minAgi = Math.min(9.0f, 5.0f);
            float maxAgi = Math.max(9.0f, 5.0f);
            assertThat(stats.getAgility()).isBetween(minAgi, maxAgi);
            assertThat(stats.getBasicAgility()).isBetween(minAgi, maxAgi);

            float minIntel = Math.min(14.0f, 3.0f);
            float maxIntel = Math.max(14.0f, 3.0f);
            assertThat(stats.getIntelligence()).isBetween(minIntel, maxIntel);
            assertThat(stats.getBasicIntelligence()).isBetween(minIntel, maxIntel);
        }

        @Test
        void shouldInheritTraitsDeterministically() {
            // With seed 42, we can check exact inherited traits.
            Rabbit kit = factory.createKit(mother, father);
            Set<RabbitTrait> traits = kit.getTraits();
            // HARDY is in both → always inherited.
            assertThat(traits).contains(RabbitTrait.HARDY);
        }

        @Test
        void shouldHandleNullParentStatsGracefully() {
            Rabbit motherNoStats = Rabbit.builder()
                    .id(300L).playerId(1L).breed("ANGORA").sex("FEMALE")
                    .secondaryStats(null).traits(Collections.emptySet()).build();
            Rabbit fatherNoStats = Rabbit.builder()
                    .id(400L).playerId(1L).breed("LIONHEAD").sex("MALE")
                    .secondaryStats(null).traits(Collections.emptySet()).build();

            Rabbit kit = factory.createKit(motherNoStats, fatherNoStats);
            assertThat(kit).isNotNull();
            // Should use default breed base stats for inheritance
            SecondaryStats stats = kit.getSecondaryStats();
            // InheritStatBetweenParents uses default values if parent stat is missing.
            // For ANGORA baseStr = 5, LIONHEAD baseStr = 5 => min 5, max 5 => slight variation
            assertThat(stats.getBasicStrength()).isBetween(4.7f, 5.3f); // because of 0.6f variation
        }

        @Test
        void shouldSetPreferredAttackRandomlyBetweenCuddlingAndPetting() {
            Rabbit kit = factory.createKit(mother, father);
            assertThat(kit.getSecondaryStats().getPreferredAttack()).isIn(AttackType.CUDDLING, AttackType.PETTING);
        }

        @Test
        void shouldNameKitWithPrefix() {
            Rabbit kit = factory.createKit(mother, father);
            assertThat(kit.getName()).startsWith("Kit ");
        }
    }
}