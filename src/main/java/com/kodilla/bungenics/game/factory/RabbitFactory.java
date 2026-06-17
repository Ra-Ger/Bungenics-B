package com.kodilla.bungenics.game.factory;

import com.kodilla.bungenics.domain.rabbit.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RabbitFactory {

    private final Random random = new Random();

    public Rabbit createRandomRabbit(Long playerId, String breedStr, String sex) {
        Breed breedEnum = (breedStr != null && !breedStr.isBlank())
                ? Breed.parseOrDefault(breedStr)
                : Breed.values()[random.nextInt(Breed.values().length)];

        String selectedBreed = breedEnum.name();
        String selectedSex = (sex != null && !sex.isBlank())
                ? sex
                : (random.nextBoolean() ? "FEMALE" : "MALE");

        float minW = breedEnum.getMinWeight();
        float maxW = breedEnum.getMaxWeight();
        float adultWeight = minW + random.nextFloat() * (maxW - minW);

        float baseStr = breedEnum.getBaseStrength() + random.nextFloat() * 3.0f;
        float baseAgi = breedEnum.getBaseAgility() + random.nextFloat() * 3.0f;
        float baseIntel = breedEnum.getBaseIntelligence() + random.nextFloat() * 3.0f;

        Set<RabbitTrait> traits = generateRandomTraits();

        float maxHP = breedEnum.getMaxLife();
        float maxStress = breedEnum.getMaxStress();
        float maxAge = 14.0f + random.nextFloat() * 4.0f; 

        SecondaryStats stats = SecondaryStats.builder()
                .weight(adultWeight)
                .nutritionLevel(100f)
                .life(maxHP)
                .stress(maxStress)
                .age(maxAge)
                .basicStrength(baseStr)
                .basicAgility(baseAgi)
                .basicIntelligence(baseIntel)
                .strength(baseStr)
                .agility(baseAgi)
                .intelligence(baseIntel)
                .preferredAttack(AttackType.values()[random.nextInt(AttackType.values().length)])
                .build();

        return Rabbit.builder()
                .playerId(playerId)
                .name(generateRandomName(selectedSex))
                .breed(selectedBreed)
                .sex(selectedSex)
                .weight(adultWeight)
                .adultWeight(adultWeight)
                .nutritionLevel(100f)
                .life(maxHP)
                .stress(0f)
                .age(1.0f)
                .maxLifetime(maxAge)
                .status(RabbitStatus.IDLE)
                .secondaryStats(stats)
                .traits(traits)
                .build();
    }

    public Rabbit createKit(Rabbit mother, Rabbit father) {
        String sex = random.nextBoolean() ? "FEMALE" : "MALE";

        String chosenBreedStr = random.nextBoolean() ? mother.getBreed() : father.getBreed();
        Breed chosenBreed = Breed.parseOrDefault(chosenBreedStr);

        float minW = chosenBreed.getMinWeight();
        float maxW = chosenBreed.getMaxWeight();
        float adultWeight = minW + random.nextFloat() * (maxW - minW);
        float initialWeight = Math.max(0.15f, adultWeight * 0.08f);

        float baseStr = inheritStatBetweenParents(
                getStatOrDefault(mother, "STR", chosenBreed.getBaseStrength()),
                getStatOrDefault(father, "STR", chosenBreed.getBaseStrength())
        );
        float baseAgi = inheritStatBetweenParents(
                getStatOrDefault(mother, "AGI", chosenBreed.getBaseAgility()),
                getStatOrDefault(father, "AGI", chosenBreed.getBaseAgility())
        );
        float baseIntel = inheritStatBetweenParents(
                getStatOrDefault(mother, "INTEL", chosenBreed.getBaseIntelligence()),
                getStatOrDefault(father, "INTEL", chosenBreed.getBaseIntelligence())
        );

        Set<RabbitTrait> inheritedTraits = inheritTraits(mother, father);

        float maxHP = chosenBreed.getMaxLife();
        float maxStress = chosenBreed.getMaxStress();
        float maxAge = 14.0f + random.nextFloat() * 4.0f;

        SecondaryStats stats = SecondaryStats.builder()
                .weight(initialWeight)
                .nutritionLevel(100f)
                .life(maxHP)
                .stress(maxStress)
                .age(maxAge)
                .basicStrength(baseStr)
                .basicAgility(baseAgi)
                .basicIntelligence(baseIntel)
                .strength(baseStr)
                .agility(baseAgi)
                .intelligence(baseIntel)
                .preferredAttack(random.nextBoolean() ? AttackType.CUDDLING : AttackType.PETTING)
                .build();

        return Rabbit.builder()
                .playerId(mother.getPlayerId())
                .name("Kit " + generateRandomName(sex))
                .breed(chosenBreed.name())
                .sex(sex)
                .weight(initialWeight)
                .adultWeight(adultWeight)
                .nutritionLevel(100f)
                .life(maxHP)
                .stress(0f)
                .age(0f)
                .maxLifetime(maxAge)
                .motherId(mother.getId())
                .fatherId(father.getId())
                .status(RabbitStatus.KIT)
                .secondaryStats(stats)
                .traits(inheritedTraits)
                .build();
    }

    private float inheritStatBetweenParents(float statA, float statB) {
        float min = Math.min(statA, statB);
        float max = Math.max(statA, statB);
        if (Math.abs(max - min) < 0.01f) {
            return min + (random.nextFloat() * 0.6f - 0.3f);
        }
        return min + random.nextFloat() * (max - min);
    }

    private float getStatOrDefault(Rabbit parent, String statType, float defaultVal) {
        if (parent == null || parent.getSecondaryStats() == null) return defaultVal;
        SecondaryStats s = parent.getSecondaryStats();
        return switch (statType) {
            case "STR" -> s.getBasicStrength() != null ? s.getBasicStrength() : (s.getStrength() != null ? s.getStrength() : defaultVal);
            case "AGI" -> s.getBasicAgility() != null ? s.getBasicAgility() : (s.getAgility() != null ? s.getAgility() : defaultVal);
            case "INTEL" -> s.getBasicIntelligence() != null ? s.getBasicIntelligence() : (s.getIntelligence() != null ? s.getIntelligence() : defaultVal);
            default -> defaultVal;
        };
    }

    private Set<RabbitTrait> generateRandomTraits() {
        Set<RabbitTrait> traits = new HashSet<>();
        List<RabbitTrait> positive = Arrays.stream(RabbitTrait.values())
                .filter(t -> t.getType() == RabbitTrait.TraitType.POSITIVE)
                .toList();
        List<RabbitTrait> negative = Arrays.stream(RabbitTrait.values())
                .filter(t -> t.getType() == RabbitTrait.TraitType.NEGATIVE)
                .toList();

        if (!positive.isEmpty()) {
            traits.add(positive.get(random.nextInt(positive.size())));
        }
        if (random.nextBoolean() && !negative.isEmpty()) {
            traits.add(negative.get(random.nextInt(negative.size())));
        }
        return traits;
    }

    private Set<RabbitTrait> inheritTraits(Rabbit mother, Rabbit father) {
        Set<RabbitTrait> inherited = new HashSet<>();
        Set<RabbitTrait> motherTraits = mother.getTraits() != null ? mother.getTraits() : Collections.emptySet();
        Set<RabbitTrait> fatherTraits = father.getTraits() != null ? father.getTraits() : Collections.emptySet();

        Set<RabbitTrait> combined = new HashSet<>(motherTraits);
        combined.addAll(fatherTraits);

        for (RabbitTrait trait : combined) {
            boolean inMother = motherTraits.contains(trait);
            boolean inFather = fatherTraits.contains(trait);

            if (inMother && inFather) {
                inherited.add(trait);
            } else if (inMother || inFather) {
                if (random.nextBoolean()) {
                    inherited.add(trait);
                }
            }
        }
        return inherited;
    }

    private String generateRandomName(String sex) {
        List<String> femaleNames = List.of("Bella", "Niu-Nia", "Sweetie", "Twilight", "Daisy", "Lola", "Luna", "Coco", "Rosie", "Lily", "Molly");
        List<String> maleNames = List.of("Thumper", "Donald", "Soarin", "Oreo", "Bugs", "Jasper", "Gizmo", "Barnaby", "Oliver", "Felix", "Leszczynek", "Czubak");
        List<String> pool = "FEMALE".equalsIgnoreCase(sex) ? femaleNames : maleNames;
        return pool.get(random.nextInt(pool.size()));
    }
}