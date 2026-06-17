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
public class MountainAdventureStrategy implements AdventureStrategy {

    private final WeatherFetcher weatherFetcher;
    private final PlayerRepository playerRepository;
    private final Random random = new Random();

    public MountainAdventureStrategy(WeatherFetcher weatherFetcher, PlayerRepository playerRepository) {
        this.weatherFetcher = weatherFetcher;
        this.playerRepository = playerRepository;
    }

    @Override
    public String getAdventureType() {
        return "MOUNTAIN";
    }

    @Override
    public List<AdventureEvent> executeAdventure(Rabbit rabbit) {
        List<AdventureEvent> events = new ArrayList<>();
        String name = rabbit.getName() != null ? rabbit.getName() : "Rabbit #" + rabbit.getId();

        String location = getPlayerLocation(rabbit, "Zakopane");
        WeatherRecord weatherRecord = weatherFetcher.fetchCurrentWeatherForCity(location);
        Weather weather = mapWeatherRecordToWeather(weatherRecord);

        events.add(createEvent("Expedition Departure",
                "Rabbit " + name + " sets out into the Misty Mountains! [" + weatherRecord.toString() + "]"));

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
                events.add(createEvent("Expedition Interrupted", "The rabbit has gone missing in the mountains..."));
                return events;
            }
        }

        events.add(createEvent("Expedition Conclusion", "The mountain wind gets chilly! Time to head back home!"));
        events.add(createEvent("Expedition Return", "Rabbit " + name + " returns safely from the Mountain expedition!"));

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

        list.add(() -> createEvent("Mountain Echo",
                "Rabbit " + name + " leaps onto a high rock and thumps its foot loudly. The echo thumps back five times!"));

        list.add(() -> createEvent("Alpine View",
                "Rabbit " + name + " gazes at the majestic snowy peaks. High above, eagles soar gracefully."));

        list.add(() -> createEvent("Warm Sunpatch",
                "Rabbit " + name + " finds a warm flat rock heated by the sun and takes a cozy bunny loaf rest."));

        list.add(() -> createEvent("Edible Edelweiss",
                "Rabbit " + name + " sniffs rare alpine flowers blooming between rocks and savors a tiny leaf."));

        if (weather == Weather.SNOW) {
            list.add(() -> createEvent("Blizzard Ridge",
                    "Icy gusts howl around high cliffs, but Rabbit " + name + " pulls its whiskers close and trudges forward."));
            list.add(() -> createEvent("Icy Stalactite View",
                    "Rabbit " + name + " stops to admire glittering frozen stalactites inside a mountain crevice."));
        } else if (weather == Weather.STORM) {
            list.add(() -> createEvent("Mountain Lightning",
                    "Dark storm clouds swirl around the rocky summit, lighting up the grey skies with electric flashes!"));
            list.add(() -> createEvent("Rock Cave Haven",
                    "Rabbit " + name + " rests comfortably inside a secluded dry mountain cave while tempests rage outside."));
        } else if (weather == Weather.HEAT) {
            list.add(() -> createEvent("Sun-Baked Crags",
                    "The scorching mountain sun warms the granite crags, making every hop warm on Rabbit " + name + "'s paws."));
            list.add(() -> createEvent("Glacial Stream Refresh",
                    "Rabbit " + name + " stoops down at a crystal-clear glacial stream and sips pure mountain ice-water."));
        }

        if (record != null && record.getWindSpeed() > 20.0) {
            list.add(() -> createEvent("Alpine Gale Gusts",
                    "High altitude wind gusts (" + String.format(java.util.Locale.US, "%.1f", record.getWindSpeed()) + " km/h) whistle through the mountain pass as Rabbit " + name + " holds tight to the rocks."));
        }

        if (record != null && record.getHumidity() > 75.0) {
            list.add(() -> createEvent("Mountain Ridge Fog",
                    "Dense clouds envelope the peak with high humidity (" + String.format(java.util.Locale.US, "%.1f", record.getHumidity()) + "%), shrouding the rocky ledges in mysterious white fog."));
        }

        if (record != null && (record.getWeatherCode() == 0 || record.getWeatherCode() == 1)) {
            list.add(() -> createEvent("Sunlit Peak Panorama",
                    "Unobscured high-mountain sunlight reveals a stunning 360-degree vista where Rabbit " + name + " rests happily."));
        }

        return list;
    }

    private List<Supplier<AdventureEvent>> getSkillTestEvents(String name, Rabbit rabbit, Weather weather, WeatherRecord record) {
        List<Supplier<AdventureEvent>> list = new ArrayList<>();

        list.add(() -> {
            float agi = (rabbit.getSecondaryStats() != null && rabbit.getSecondaryStats().getAgility() != null)
                    ? rabbit.getSecondaryStats().getAgility() : 5f;
            float dc = 13f;
            AdventureHelper.SkillTestResult res = AdventureHelper.performSkillTest(dc, agi);

            if (res.isSuccess()) {
                return createEvent("Agility Test - Falling Pebble",
                        "A loose pebble rolls down! [Agility Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " leaps sideways and spots a shiny coin!",
                        BigDecimal.valueOf(20), 0f,0f,0f);
            } else {
                return createEvent("Agility Test - Falling Pebble",
                        "A loose rock tumbles near Rabbit " + name + "! [Agility Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit dodges awkwardly.");
            }
        });

        list.add(() -> {
            float str = (rabbit.getSecondaryStats() != null && rabbit.getSecondaryStats().getStrength() != null)
                    ? rabbit.getSecondaryStats().getStrength() : 5f;
            float dc = 12f;
            AdventureHelper.SkillTestResult res = AdventureHelper.performSkillTest(dc, str);

            if (res.isSuccess()) {
                rabbit.setNutritionLevel(Math.min(100f, rabbit.getNutritionLevel() + 20f));
                return createEvent("Strength Test - Heavy Slate",
                        "Rabbit " + name + " sees something under slate rock. [Strength Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Heaves the slate and finds sweet wild carrots! (+20 Nutrition, +4 Carrots)",
                        BigDecimal.valueOf(10), 4.0f,0f,0f);
            } else {
                return createEvent("Strength Test - Heavy Slate",
                        "Rabbit " + name + " tries to nudge heavy stone. [Strength Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Stone doesn't budge.");
            }
        });

        list.add(() -> {
            float intel = (rabbit.getSecondaryStats() != null && rabbit.getSecondaryStats().getIntelligence() != null)
                    ? rabbit.getSecondaryStats().getIntelligence() : 5f;
            float dc = 14f;
            AdventureHelper.SkillTestResult res = AdventureHelper.performSkillTest(dc, intel);

            if (res.isSuccess()) {
                rabbit.setStress(Math.max(0f, rabbit.getStress() - 20f));
                return createEvent("Intelligence Test - Cliffside Path",
                        "A tricky path splits into ledges. [Intelligence Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " finds a safe shortcut with coins!",
                        BigDecimal.valueOf(30), 0f,0f,0f);
            } else {
                return createEvent("Intelligence Test - Cliffside Path",
                        "The cliff edge looks confusing. [Intelligence Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " backtracks.");
            }
        });

        if (weather == Weather.SNOW) {
            list.add(() -> {
                float agi = (rabbit.getSecondaryStats() != null && rabbit.getSecondaryStats().getAgility() != null) ? rabbit.getSecondaryStats().getAgility() : 5f;
                float dc = 14f;
                AdventureHelper.SkillTestResult res = AdventureHelper.performSkillTest(dc, agi);
                if (res.isSuccess()) {
                    return createEvent("Agility Test - Icy Ledge Crossing",
                            "The cliff ledge is covered in slick ice! [Agility Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " tiptoes with claws extended and retrieves lost coins!", BigDecimal.valueOf(25), 0f,0f,0f);
                } else {
                    return createEvent("Agility Test - Icy Ledge Crossing",
                            "The cliff ledge is covered in slick ice! [Agility Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " slips and retreats to safer ground.");
                }
            });
        } else if (weather == Weather.STORM) {
            list.add(() -> {
                float agi = (rabbit.getSecondaryStats() != null && rabbit.getSecondaryStats().getAgility() != null) ? rabbit.getSecondaryStats().getAgility() : 5f;
                float dc = 14f;
                AdventureHelper.SkillTestResult res = AdventureHelper.performSkillTest(dc, agi);
                if (res.isSuccess()) {
                    return createEvent("Agility Test - Rockslide Avoidance",
                            "Rain dislodges a rockslide! [Agility Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " leaps between falling boulders and finds miner loot!", BigDecimal.valueOf(35), 0f,0f,0f);
                } else {
                    return createEvent("Agility Test - Rockslide Avoidance",
                            "Rain dislodges a rockslide! [Agility Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " scrambles back terrified.");
                }
            });
        } else if (weather == Weather.HEAT) {
            list.add(() -> {
                float str = (rabbit.getSecondaryStats() != null && rabbit.getSecondaryStats().getStrength() != null) ? rabbit.getSecondaryStats().getStrength() : 5f;
                float dc = 13f;
                AdventureHelper.SkillTestResult res = AdventureHelper.performSkillTest(dc, str);
                if (res.isSuccess()) {
                    return createEvent("Strength Test - Scorching Summit Climb",
                            "Stifling mountain heat drains energy! [Strength Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " pushes through to the peak and finds wild carrots!", BigDecimal.ZERO, 5f,0f,0f);
                } else {
                    return createEvent("Strength Test - Scorching Summit Climb",
                            "Stifling mountain heat drains energy! [Strength Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " sits down exhausted.");
                }
            });
        }

        if (record != null && record.getWindSpeed() > 25.0) {
            list.add(() -> {
                float agi = (rabbit.getSecondaryStats() != null && rabbit.getSecondaryStats().getAgility() != null) ? rabbit.getSecondaryStats().getAgility() : 5f;
                float dc = 15f;
                AdventureHelper.SkillTestResult res = AdventureHelper.performSkillTest(dc, agi);
                if (res.isSuccess()) {
                    return createEvent("Agility Test - Peak Gale Balance",
                            "Powerful mountain winds threaten to push travelers off balance! [Agility Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " stays low to the ground and finds an old mountaineer pouch!", BigDecimal.valueOf(28), 0f,0f,0f);
                } else {
                    return createEvent("Agility Test - Peak Gale Balance",
                            "Powerful mountain winds threaten to push travelers off balance! [Agility Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " gets blown back into a rock recess.");
                }
            });
        }

        if (record != null && record.getHumidity() > 80.0) {
            list.add(() -> {
                float intel = (rabbit.getSecondaryStats() != null && rabbit.getSecondaryStats().getIntelligence() != null) ? rabbit.getSecondaryStats().getIntelligence() : 5f;
                float dc = 13f;
                AdventureHelper.SkillTestResult res = AdventureHelper.performSkillTest(dc, intel);
                if (res.isSuccess()) {
                    return createEvent("Intelligence Test - Mist-Shrouded Ravine",
                            "Thick mountain clouds obscure the narrow bridge! [Intelligence Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " carefully tests footholds and finds wild spinach!", BigDecimal.ZERO, 0f, 0f, 5f);
                } else {
                    return createEvent("Intelligence Test - Mist-Shrouded Ravine",
                            "Thick mountain clouds obscure the narrow bridge! [Intelligence Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " decides not to risk crossing blindly.");
                }
            });
        }

        return list;
    }

    private List<Supplier<AdventureEvent>> getCombatEvents(String name, Rabbit rabbit) {
        List<Supplier<AdventureEvent>> list = new ArrayList<>();
        list.add(() -> executeHawkCombat(name, rabbit));
        list.add(() -> executeGoatCombat(name, rabbit));
        return list;
    }

    private AdventureEvent executeHawkCombat(String name, Rabbit rabbit) {
        float dc = 13f;
        AdventureHelper.AttackModifier attMod = AdventureHelper.getAttackModifier(rabbit);
        AdventureHelper.CombatTestResult res = AdventureHelper.performCombatTest(dc, attMod.statValue());

        String logPrefix = "Combat vs Hungry Mountain Hawk [Roll " + (int)res.roll() + " (Range 0 - " + (int)res.maxRoll() + ") vs DC " + (int)dc + "]: ";

        return switch (res.resultType()) {
            case CRITICAL_VICTORY -> {
                rabbit.setStress(Math.max(0f, rabbit.getStress() - 20f));
                yield createEvent("Combat - Hungry Mountain Hawk",
                        logPrefix + "CRITICAL VICTORY! Rabbit " + name + " dazzled hawk with " + attMod.attackType() + "! Hawk dropped coins!",
                        BigDecimal.valueOf(25), 0f,0f,0f);
            }
            case NORMAL_VICTORY -> {
                rabbit.setStress(Math.max(0f, rabbit.getStress() - 5f));
                yield createEvent("Combat - Hungry Mountain Hawk",
                        logPrefix + "VICTORY! Rabbit " + name + " pacified hawk using " + attMod.attackType() + ".");
            }
            case NORMAL_DEFEAT -> {
                rabbit.setNutritionLevel(Math.max(0f, rabbit.getNutritionLevel() - 15f));
                rabbit.setStress(Math.min(100f, rabbit.getStress() + 15f));
                yield createEvent("Combat - Hungry Mountain Hawk",
                        logPrefix + "DEFEAT! Rabbit " + name + "'s " + attMod.attackType() + " was ineffective!");
            }
            case CRITICAL_DEFEAT -> {
                float baseDamage = 30f;
                float damage = AdventureHelper.calculateCombatDamage(rabbit, baseDamage);
                rabbit.setLife(Math.max(0f, (rabbit.getLife() != null ? rabbit.getLife() : 100f) - damage));
                rabbit.setStress(Math.min(100f, rabbit.getStress() + 30f));
                yield createEvent("Combat - Hungry Mountain Hawk",
                        logPrefix + "CRITICAL DEFEAT! Hawk swooped down and scratched Rabbit " + name + " (-" + (int)damage + " HP)!");
            }
        };
    }

    private AdventureEvent executeGoatCombat(String name, Rabbit rabbit) {
        float dc = 12f;
        AdventureHelper.AttackModifier attMod = AdventureHelper.getAttackModifier(rabbit);
        AdventureHelper.CombatTestResult res = AdventureHelper.performCombatTest(dc, attMod.statValue());

        String logPrefix = "Combat vs Territorial Mountain Goat [Roll " + (int)res.roll() + " (Range 0 - " + (int)res.maxRoll() + ") vs DC " + (int)dc + "]: ";

        return switch (res.resultType()) {
            case CRITICAL_VICTORY -> {
                rabbit.setStress(Math.max(0f, rabbit.getStress() - 15f));
                yield createEvent("Combat - Territorial Mountain Goat",
                        logPrefix + "CRITICAL VICTORY! Rabbit " + name + " charmed goat with " + attMod.attackType() + "! Goat shared coins!",
                        BigDecimal.valueOf(20), 0f,0f,0f);
            }
            case NORMAL_VICTORY -> {
                rabbit.setStress(Math.max(0f, rabbit.getStress() - 5f));
                yield createEvent("Combat - Territorial Mountain Goat",
                        logPrefix + "VICTORY! Rabbit " + name + " calmed goat using " + attMod.attackType() + ".");
            }
            case NORMAL_DEFEAT -> {
                rabbit.setStress(Math.min(100f, rabbit.getStress() + 15f));
                yield createEvent("Combat - Territorial Mountain Goat",
                        logPrefix + "DEFEAT! Goat snorted loudly and blocked path.");
            }
            case CRITICAL_DEFEAT -> {
                float baseDamage = 25f;
                float damage = AdventureHelper.calculateCombatDamage(rabbit, baseDamage);
                rabbit.setLife(Math.max(0f, (rabbit.getLife() != null ? rabbit.getLife() : 100f) - damage));
                rabbit.setStress(Math.min(100f, rabbit.getStress() + 25f));
                yield createEvent("Combat - Territorial Mountain Goat",
                        logPrefix + "CRITICAL DEFEAT! Goat headbutted Rabbit " + name + " off ledge (-" + (int)damage + " HP)!");
            }
        };
    }
}