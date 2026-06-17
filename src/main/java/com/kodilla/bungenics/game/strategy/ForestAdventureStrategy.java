package com.kodilla.bungenics.game.strategy;

import com.kodilla.bungenics.dataFetchers.OpenMeteo.WeatherFetcher;
import com.kodilla.bungenics.dataFetchers.OpenMeteo.WeatherRecord;
import com.kodilla.bungenics.domain.adventure.AdventureEvent;
import com.kodilla.bungenics.domain.player.Player;
import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.domain.rabbit.RabbitStatus;
import com.kodilla.bungenics.repository.PlayerRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static com.kodilla.bungenics.game.strategy.AdventureHelper.createEvent;

@Component
public class ForestAdventureStrategy implements AdventureStrategy {

    private final WeatherFetcher weatherFetcher;
    private final PlayerRepository playerRepository;
    private final Random random = new Random();

    public ForestAdventureStrategy(WeatherFetcher weatherFetcher, PlayerRepository playerRepository) {
        this.weatherFetcher = weatherFetcher;
        this.playerRepository = playerRepository;
    }

    @Override
    public String getAdventureType() {
        return "FOREST";
    }

    @Override
    public List<AdventureEvent> executeAdventure(Rabbit rabbit) {
        List<AdventureEvent> events = new ArrayList<>();
        String name = rabbit.getName() != null ? rabbit.getName() : "Rabbit #" + rabbit.getId();

        String location = getPlayerLocation(rabbit, "Pęcice");
        WeatherRecord weatherRecord = weatherFetcher.fetchCurrentWeatherForCity(location);
        Weather weather = mapWeatherRecordToWeather(weatherRecord);

        events.add(createEvent("Expedition Departure",
                "Rabbit " + name + " sets out into the Deep Forest! [" + weatherRecord.toString() + "]"));

        List<Supplier<AdventureEvent>> neutralPool = new ArrayList<>(getNeutralEvents(name, weather, weatherRecord));
        List<Supplier<AdventureEvent>> skillTestPool = new ArrayList<>(getSkillTestEvents(name, rabbit, weather, weatherRecord));
        List<Supplier<AdventureEvent>> combatPool = new ArrayList<>(getCombatEvents(name, rabbit));

        List<Supplier<AdventureEvent>> selectedSequence = AdventureHelper.selectEventsWithExclusion(random, neutralPool, skillTestPool, combatPool);

        for (Supplier<AdventureEvent> stepSupplier : selectedSequence) {
            AdventureEvent event = stepSupplier.get();
            events.add(event);

            if (rabbit.getLife() != null && rabbit.getLife() <= 0f) {
                rabbit.setLife(0f);
                rabbit.setStatus(RabbitStatus.DEAD);
                events.add(createEvent("Expedition Interrupted", "The rabbit has gone missing in the forest..."));
                return events;
            }
        }

        events.add(createEvent("Expedition Conclusion", "Getting hungry! Time to head back home!"));
        events.add(createEvent("Expedition Return", "Rabbit " + name + " returns safely from the Forest expedition!"));

        return events;
    }

    private String getPlayerLocation(Rabbit rabbit, String defaultLocation) {
        if (rabbit != null && rabbit.getPlayerId() != null) {
            return playerRepository.findById(rabbit.getPlayerId())
                    .map(Player::getLocation)
                    .filter(loc -> loc != null && !loc.isBlank())
                    .orElse(defaultLocation);
        }
        return defaultLocation;
    }

    private Weather mapWeatherRecordToWeather(WeatherRecord record) {
        if (record == null) {
            return Weather.CLEAR;
        }
        double temp = record.getTemperature();
        int code = record.getWeatherCode();

        if (temp >= 25.0) {
            return Weather.HEAT;
        } else if (temp <= 2.0 || (code >= 71 && code <= 77) || code == 85 || code == 86) {
            return Weather.SNOW;
        } else if (code >= 80 || record.getWindSpeed() > 30.0) {
            return Weather.STORM;
        } else {
            return Weather.CLEAR;
        }
    }

    private List<Supplier<AdventureEvent>> getNeutralEvents(String name, Weather weather, WeatherRecord record) {
        List<Supplier<AdventureEvent>> list = new ArrayList<>();

        list.add(() -> createEvent("Short Rest",
                "Rabbit " + name + " steps forward bravely! Walked 3 meters and decided to take a break to nibble on fresh grass."));

        list.add(() -> createEvent("Cloud Gazing",
                "Rabbit " + name + " admires beautiful clouds in the sky. One looks like cherries, another like puppies."));

        list.add(() -> createEvent("Forest Whispers",
                "Rabbit " + name + " stops to listen to the gentle rustling of pine needles and tapping of a woodpecker."));

        list.add(() -> createEvent("Wildflower Trail",
                "Rabbit " + name + " sniffs a cluster of vibrant blue forest flowers and sneezes cutely three times."));

        if (weather == Weather.SNOW) {
            list.add(() -> createEvent("Snowy Pine Canopy",
                    "Frosty snow falls gently from heavy pine branches right onto Rabbit " + name + "'s tail!"));
            list.add(() -> createEvent("Frozen Tracks",
                    "Rabbit " + name + " follows mysterious tiny animal footprints pressed deep into the fresh forest powder."));
        } else if (weather == Weather.STORM) {
            list.add(() -> createEvent("Thunder Echo",
                    "Bright lightning flashes through the dense forest canopy, shaking the ground with loud thunder!"));
            list.add(() -> createEvent("Hollow Tree Shelter",
                    "Rabbit " + name + " hides snugly inside a hollow old oak tree while torrential rain pours down."));
        } else if (weather == Weather.HEAT) {
            list.add(() -> createEvent("Shaded Fern Rest",
                    "Rabbit " + name + " sprawls out under giant green fern leaves to escape the scorching forest heat."));
            list.add(() -> createEvent("Dry Forest Creek",
                    "Rabbit " + name + " finds a cool trickling spring amidst dry leaves and quenches its thirsty throat."));
        }

        if (record != null && record.getWindSpeed() > 20.0) {
            list.add(() -> createEvent("High Wind Gusts",
                    "Brisk winds (" + String.format(java.util.Locale.US, "%.1f", record.getWindSpeed()) + " km/h) blow through the forest canopy, dropping fresh pinecones near Rabbit " + name + "."));
        }

        if (record != null && record.getHumidity() > 75.0) {
            list.add(() -> createEvent("Damp Moss Cushion",
                    "High forest humidity (" + String.format(java.util.Locale.US, "%.1f", record.getHumidity()) + "%) makes the moss exceptionally soft under Rabbit " + name + "'s paws."));
        }

        if (record != null && (record.getWeatherCode() == 0 || record.getWeatherCode() == 1)) {
            list.add(() -> createEvent("Clear Canopy Sunbeam",
                    "Unobstructed sunlight warms a quiet forest glade where Rabbit " + name + " basks comfortably."));
        }

        return list;
    }

    private List<Supplier<AdventureEvent>> getSkillTestEvents(String name, Rabbit rabbit, Weather weather, WeatherRecord record) {
        List<Supplier<AdventureEvent>> list = new ArrayList<>();

        list.add(() -> {
            float agi = (rabbit.getSecondaryStats() != null && rabbit.getSecondaryStats().getAgility() != null)
                    ? rabbit.getSecondaryStats().getAgility() : 5f;
            float dc = 12f;
            AdventureHelper.SkillTestResult res = AdventureHelper.performSkillTest(dc, agi);

            if (res.isSuccess()) {
                return createEvent("Agility Test - Magpie Coin",
                        "Rabbit " + name + " spots a shiny coin! A magpie swoops down, but [Agility Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit snatches it just in time!",
                        BigDecimal.valueOf(15), 0f,0f,0f);
            } else {
                return createEvent("Agility Test - Magpie Coin",
                        "Rabbit " + name + " spots a shiny coin, but [Agility Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit was too slow and leaves empty-handed.");
            }
        });

        list.add(() -> {
            float str = (rabbit.getSecondaryStats() != null && rabbit.getSecondaryStats().getStrength() != null)
                    ? rabbit.getSecondaryStats().getStrength() : 5f;
            float dc = 11f;
            AdventureHelper.SkillTestResult res = AdventureHelper.performSkillTest(dc, str);

            if (res.isSuccess()) {
                rabbit.setNutritionLevel(Math.min(100f, rabbit.getNutritionLevel() + 25f));
                return createEvent("Strength Test - Cardboard Box",
                        "Rabbit " + name + " finds an abandoned box. [Strength Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Tears open the box and feasts on fresh lettuce! (+25 Nutrition, +5 Lettuce to Farm)",
                        BigDecimal.ZERO, 0f, 5.0f, 0f);
            } else {
                return createEvent("Strength Test - Cardboard Box",
                        "Rabbit " + name + " finds a cardboard box, but [Strength Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - The cardboard is too thick.");
            }
        });

        list.add(() -> {
            float intel = (rabbit.getSecondaryStats() != null && rabbit.getSecondaryStats().getIntelligence() != null)
                    ? rabbit.getSecondaryStats().getIntelligence() : 5f;
            float dc = 13f;
            AdventureHelper.SkillTestResult res = AdventureHelper.performSkillTest(dc, intel);

            if (res.isSuccess()) {
                rabbit.setStress(Math.max(0f, rabbit.getStress() - 15f));
                return createEvent("Intelligence Test - Ancient Runes",
                        "Rabbit " + name + " discovers mossy stones. [Intelligence Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Deciphers the ancient riddle and unlocks coins!",
                        BigDecimal.valueOf(25),null,null,null);
            } else {
                return createEvent("Intelligence Test - Ancient Runes",
                        "Rabbit " + name + " stares confused at mysterious stone carvings. [Intelligence Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - The symbols remain a mystery.");
            }
        });

        if (weather == Weather.SNOW) {
            list.add(() -> {
                float agi = (rabbit.getSecondaryStats() != null && rabbit.getSecondaryStats().getAgility() != null) ? rabbit.getSecondaryStats().getAgility() : 5f;
                float dc = 13f;
                AdventureHelper.SkillTestResult res = AdventureHelper.performSkillTest(dc, agi);
                if (res.isSuccess()) {
                    return createEvent("Agility Test - Snowdrift Leap",
                            "Deep snow drifts block the path! [Agility Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " bounds over snowbanks and finds frozen carrots!", BigDecimal.valueOf(18), 2f,0f,0f);
                } else {
                    return createEvent("Agility Test - Snowdrift Leap",
                            "Deep snow drifts block the path! [Agility Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " plunges belly-first into cold snow.");
                }
            });
        } else if (weather == Weather.STORM) {
            list.add(() -> {
                float agi = (rabbit.getSecondaryStats() != null && rabbit.getSecondaryStats().getAgility() != null) ? rabbit.getSecondaryStats().getAgility() : 5f;
                float dc = 13f;
                AdventureHelper.SkillTestResult res = AdventureHelper.performSkillTest(dc, agi);
                if (res.isSuccess()) {
                    return createEvent("Agility Test - Slippery Mud Climb",
                            "Heavy rain turns slopes into mud! [Agility Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " scrambles up safely and spots a lost pouch!", BigDecimal.valueOf(20), 0f,0f,0f);
                } else {
                    return createEvent("Agility Test - Slippery Mud Climb",
                            "Heavy rain turns slopes into mud! [Agility Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " slips back down covered in mud.");
                }
            });
        } else if (weather == Weather.HEAT) {
            list.add(() -> {
                float intel = (rabbit.getSecondaryStats() != null && rabbit.getSecondaryStats().getIntelligence() != null) ? rabbit.getSecondaryStats().getIntelligence() : 5f;
                float dc = 13f;
                AdventureHelper.SkillTestResult res = AdventureHelper.performSkillTest(dc, intel);
                if (res.isSuccess()) {
                    return createEvent("Intelligence Test - Wilted Berry Foraging",
                            "Scorching heat wilts bushes! [Intelligence Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " searches under dense leaves and collects juicy wild spinach!", BigDecimal.ZERO, 0f, 0f, 5f);
                } else {
                    return createEvent("Intelligence Test - Wilted Berry Foraging",
                            "Scorching heat wilts bushes! [Intelligence Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " finds only dried leaves.");
                }
            });
        }

        if (record != null && record.getWindSpeed() > 25.0) {
            list.add(() -> {
                float agi = (rabbit.getSecondaryStats() != null && rabbit.getSecondaryStats().getAgility() != null) ? rabbit.getSecondaryStats().getAgility() : 5f;
                float dc = 14f;
                AdventureHelper.SkillTestResult res = AdventureHelper.performSkillTest(dc, agi);
                if (res.isSuccess()) {
                    return createEvent("Agility Test - Falling Branch Dodge",
                            "Strong winds shake branches free! [Agility Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " leaps clear of a falling bough and spots buried coins!", BigDecimal.valueOf(22), 0f,0f,0f);
                } else {
                    return createEvent("Agility Test - Falling Branch Dodge",
                            "Strong winds shake branches free! [Agility Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - A small twig bumps Rabbit " + name + "'s ear.");
                }
            });
        }

        if (record != null && record.getHumidity() > 80.0) {
            list.add(() -> {
                float intel = (rabbit.getSecondaryStats() != null && rabbit.getSecondaryStats().getIntelligence() != null) ? rabbit.getSecondaryStats().getIntelligence() : 5f;
                float dc = 12f;
                AdventureHelper.SkillTestResult res = AdventureHelper.performSkillTest(dc, intel);
                if (res.isSuccess()) {
                    return createEvent("Intelligence Test - Misty Navigation",
                            "Thick forest mist hampers vision! [Intelligence Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " uses sense of smell to uncover wild spinach!", BigDecimal.ZERO, 0f, 0f, 4f);
                } else {
                    return createEvent("Intelligence Test - Misty Navigation",
                            "Thick forest mist hampers vision! [Intelligence Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " wanders in circles for a moment.");
                }
            });
        }

        return list;
    }

    private List<Supplier<AdventureEvent>> getCombatEvents(String name, Rabbit rabbit) {
        List<Supplier<AdventureEvent>> list = new ArrayList<>();
        list.add(() -> executeBadgerCombat(name, rabbit));
        list.add(() -> executePigeonCombat(name, rabbit));
        return list;
    }

    private AdventureEvent executeBadgerCombat(String name, Rabbit rabbit) {
        float dc = 12f;
        AdventureHelper.AttackModifier attMod = AdventureHelper.getAttackModifier(rabbit);
        AdventureHelper.CombatTestResult res = AdventureHelper.performCombatTest(dc, attMod.statValue());

        String logPrefix = "Combat vs Grumpy Badger [Roll " + (int)res.roll() + " (Range 0 - " + (int)res.maxRoll() + ") vs DC " + (int)dc + "]: ";

        return switch (res.resultType()) {
            case CRITICAL_VICTORY -> {
                rabbit.setStress(Math.max(0f, rabbit.getStress() - 15f));
                yield createEvent("Combat - Grumpy Badger",
                        logPrefix + "CRITICAL VICTORY! Rabbit " + name + " overwhelmed the badger with " + attMod.attackType() + "! Badger dropped a bag of coins!",
                        BigDecimal.valueOf(20), 0f,0f,0f);
            }
            case NORMAL_VICTORY -> {
                rabbit.setStress(Math.max(0f, rabbit.getStress() - 5f));
                yield createEvent("Combat - Grumpy Badger",
                        logPrefix + "VICTORY! Rabbit " + name + " pacified the badger with " + attMod.attackType() + ".");
            }
            case NORMAL_DEFEAT -> {
                rabbit.setStress(Math.min(100f, rabbit.getStress() + 15f));
                yield createEvent("Combat - Grumpy Badger",
                        logPrefix + "DEFEAT! Rabbit " + name + "'s " + attMod.attackType() + " only annoyed the badger!");
            }
            case CRITICAL_DEFEAT -> {
                float baseDamage = 25f;
                float damage = AdventureHelper.calculateCombatDamage(rabbit, baseDamage);
                rabbit.setLife(Math.max(0f, (rabbit.getLife() != null ? rabbit.getLife() : 100f) - damage));
                rabbit.setStress(Math.min(100f, rabbit.getStress() + 25f));
                yield createEvent("Combat - Grumpy Badger",
                        logPrefix + "CRITICAL DEFEAT! The badger growled fiercely and bit Rabbit " + name + " hard on the paw (-" + (int)damage + " HP)!");
            }
        };
    }

    private AdventureEvent executePigeonCombat(String name, Rabbit rabbit) {
        float dc = 10f;
        AdventureHelper.AttackModifier attMod = AdventureHelper.getAttackModifier(rabbit);
        AdventureHelper.CombatTestResult res = AdventureHelper.performCombatTest(dc, attMod.statValue());

        String logPrefix = "Combat vs Feisty Pigeon [Roll " + (int)res.roll() + " (Range 0 - " + (int)res.maxRoll() + ") vs DC " + (int)dc + "]: ";

        return switch (res.resultType()) {
            case CRITICAL_VICTORY -> {
                rabbit.setStress(Math.max(0f, rabbit.getStress() - 10f));
                yield createEvent("Combat - Feisty Pigeon",
                        logPrefix + "CRITICAL VICTORY! Rabbit " + name + " dazzled the pigeon with " + attMod.attackType() + "! Bird dropped coins!",
                        BigDecimal.valueOf(12), 0f,0f,0f);
            }
            case NORMAL_VICTORY -> {
                rabbit.setStress(Math.max(0f, rabbit.getStress() - 5f));
                yield createEvent("Combat - Feisty Pigeon",
                        logPrefix + "VICTORY! Rabbit " + name + " soothed the pigeon using " + attMod.attackType() + ".");
            }
            case NORMAL_DEFEAT -> {
                rabbit.setStress(Math.min(100f, rabbit.getStress() + 10f));
                yield createEvent("Combat - Feisty Pigeon",
                        logPrefix + "DEFEAT! The pigeon flapped frantically, startling Rabbit " + name + "!");
            }
            case CRITICAL_DEFEAT -> {
                float baseDamage = 15f;
                float damage = AdventureHelper.calculateCombatDamage(rabbit, baseDamage);
                rabbit.setLife(Math.max(0f, (rabbit.getLife() != null ? rabbit.getLife() : 100f) - damage));
                rabbit.setStress(Math.min(100f, rabbit.getStress() + 20f));
                yield createEvent("Combat - Feisty Pigeon",
                        logPrefix + "CRITICAL DEFEAT! The pigeon pecked Rabbit " + name + " repeatedly (-" + (int)damage + " HP)!");
            }
        };
    }
}