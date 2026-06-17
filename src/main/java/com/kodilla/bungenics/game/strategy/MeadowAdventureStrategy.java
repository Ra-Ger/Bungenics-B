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
public class MeadowAdventureStrategy implements AdventureStrategy {

    private final WeatherFetcher weatherFetcher;
    private final PlayerRepository playerRepository;
    private final Random random = new Random();

    public MeadowAdventureStrategy(WeatherFetcher weatherFetcher, PlayerRepository playerRepository) {
        this.weatherFetcher = weatherFetcher;
        this.playerRepository = playerRepository;
    }

    @Override
    public String getAdventureType() {
        return "MEADOW";
    }

    @Override
    public List<AdventureEvent> executeAdventure(Rabbit rabbit) {
        List<AdventureEvent> events = new ArrayList<>();
        String name = rabbit.getName() != null ? rabbit.getName() : "Rabbit #" + rabbit.getId();

        String location = getPlayerLocation(rabbit, "Pęcice");
        WeatherRecord weatherRecord = weatherFetcher.fetchCurrentWeatherForCity(location);
        Weather weather = mapWeatherRecordToWeather(weatherRecord);

        events.add(createEvent("Expedition Departure",
                "Rabbit " + name + " sets out across the Blooming Meadows! [" + weatherRecord.toString() + "]"));

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
                events.add(createEvent("Expedition Interrupted", "The rabbit has gone missing in the meadow..."));
                return events;
            }
        }

        events.add(createEvent("Expedition Conclusion", "Sun is setting over the meadow! Time to head back home!"));
        events.add(createEvent("Expedition Return", "Rabbit " + name + " returns happily from the Meadow expedition!"));

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

        // Base neutral events
        list.add(() -> createEvent("Dandelion Breeze",
                "Rabbit " + name + " blows on a giant dandelion head and watches fluffy seeds dance away."));

        list.add(() -> createEvent("Clover Patch",
                "Rabbit " + name + " finds a luscious patch of sweet clover and eats until its belly is round."));

        list.add(() -> createEvent("Butterfly Chase",
                "Rabbit " + name + " playfully hops after a yellow swallowtail butterfly across the field."));

        list.add(() -> createEvent("Streamside Sip",
                "Rabbit " + name + " stops at a clear meadow creek for a refreshing drink of cool water."));

        if (weather == Weather.SNOW) {
            list.add(() -> createEvent("Snowy Meadow Blanket",
                    "A pristine blanket of fresh snow covers the green meadow while Rabbit " + name + " leaves cute paw prints behind."));
            list.add(() -> createEvent("Frosted Clover Patch",
                    "Rabbit " + name + " uncovers crisp, frozen clover blades buried under a light dust of winter snow."));
        } else if (weather == Weather.STORM) {
            list.add(() -> createEvent("Meadow Tempest",
                    "Dark storm clouds sweep across open fields as wind bends tall meadow grasses in dramatic waves."));
            list.add(() -> createEvent("Burrow Shelter",
                    "Rabbit " + name + " snuggles deep inside an abandoned burrow to stay dry while rain hammers the field."));
        } else if (weather == Weather.HEAT) {
            list.add(() -> createEvent("Summer Heatwave",
                    "Warm golden heat bathes the meadow, filling the air with sweet scents of blooming wild flowers."));
            list.add(() -> createEvent("Shaded Tree Shade",
                    "Rabbit " + name + " reposes lazily in the shade of a lonely meadow willow tree."));
        }

        // Additional bonus events based on WeatherRecord
        if (record != null && record.getWindSpeed() > 20.0) {
            list.add(() -> createEvent("Meadow Wind Rustle",
                    "Fresh breezes (" + String.format(java.util.Locale.US, "%.1f", record.getWindSpeed()) + " km/h) send waves through the tall meadow grass, carrying scents of wild clover to Rabbit " + name + "."));
        }

        if (record != null && record.getHumidity() > 75.0) {
            list.add(() -> createEvent("Morning Dew Drops",
                    "High meadow humidity (" + String.format(java.util.Locale.US, "%.1f", record.getHumidity()) + "%) covers grass blades in sparkling dew droplets that refresh Rabbit " + name + "'s paws."));
        }

        if (record != null && (record.getWeatherCode() == 0 || record.getWeatherCode() == 1)) {
            list.add(() -> createEvent("Golden Meadow Sunbeam",
                    "Bright unbroken sunlight warms the flower-filled pasture where Rabbit " + name + " basks happily."));
        }

        return list;
    }

    private List<Supplier<AdventureEvent>> getSkillTestEvents(String name, Rabbit rabbit, Weather weather, WeatherRecord record) {
        List<Supplier<AdventureEvent>> list = new ArrayList<>();

        list.add(() -> {
            float agi = (rabbit.getSecondaryStats() != null && rabbit.getSecondaryStats().getAgility() != null)
                    ? rabbit.getSecondaryStats().getAgility() : 5f;
            float dc = 10f;
            AdventureHelper.SkillTestResult res = AdventureHelper.performSkillTest(dc, agi);

            if (res.isSuccess()) {
                return createEvent("Agility Test - Creek Leap",
                        "A creek blocks path! [Agility Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " leaps across and finds shiny coins!",
                        BigDecimal.valueOf(18), 0f,0f,0f);
            } else {
                return createEvent("Agility Test - Creek Leap",
                        "A creek blocks path! [Agility Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " gets paws wet.");
            }
        });

        list.add(() -> {
            float str = (rabbit.getSecondaryStats() != null && rabbit.getSecondaryStats().getStrength() != null)
                    ? rabbit.getSecondaryStats().getStrength() : 5f;
            float dc = 11f;
            AdventureHelper.SkillTestResult res = AdventureHelper.performSkillTest(dc, str);

            if (res.isSuccess()) {
                rabbit.setNutritionLevel(Math.min(100f, rabbit.getNutritionLevel() + 30f));
                return createEvent("Strength Test - Digging Soil",
                        "Rabbit " + name + " senses carrots under soil. [Strength Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Digs and unearths golden carrots! (+30 Nutrition, +6 Carrots)",
                        BigDecimal.ZERO, 6.0f,0f,0f);
            } else {
                return createEvent("Strength Test - Digging Soil",
                        "Rabbit " + name + " tries to dig clay soil. [Strength Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Gets tired.");
            }
        });

        list.add(() -> {
            float intel = (rabbit.getSecondaryStats() != null && rabbit.getSecondaryStats().getIntelligence() != null)
                    ? rabbit.getSecondaryStats().getIntelligence() : 5f;
            float dc = 12f;
            AdventureHelper.SkillTestResult res = AdventureHelper.performSkillTest(dc, intel);

            if (res.isSuccess()) {
                rabbit.setStress(Math.max(0f, rabbit.getStress() - 15f));
                return createEvent("Intelligence Test - Scarecrow Mystery",
                        "Scary figure looms! [Intelligence Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " realizes it's a scarecrow with spinach! (+6 Spinach)",
                        BigDecimal.ZERO, 0f, 0f, 6.0f);
            } else {
                return createEvent("Intelligence Test - Scarecrow Mystery",
                        "Tall figure stands silent. [Intelligence Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " keeps away.");
            }
        });

        if (weather == Weather.SNOW) {
            list.add(() -> {
                float str = (rabbit.getSecondaryStats() != null && rabbit.getSecondaryStats().getStrength() != null) ? rabbit.getSecondaryStats().getStrength() : 5f;
                float dc = 12f;
                AdventureHelper.SkillTestResult res = AdventureHelper.performSkillTest(dc, str);
                if (res.isSuccess()) {
                    return createEvent("Strength Test - Snowy Burrow Dig",
                            "Frozen soil covers underground roots! [Strength Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " breaks icy crust and digs up carrots!", BigDecimal.ZERO, 5f,0f,0f);
                } else {
                    return createEvent("Strength Test - Snowy Burrow Dig",
                            "Frozen soil covers underground roots! [Strength Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - The frozen soil is too hard to break.");
                }
            });
        } else if (weather == Weather.STORM) {
            list.add(() -> {
                float agi = (rabbit.getSecondaryStats() != null && rabbit.getSecondaryStats().getAgility() != null) ? rabbit.getSecondaryStats().getAgility() : 5f;
                float dc = 12f;
                AdventureHelper.SkillTestResult res = AdventureHelper.performSkillTest(dc, agi);
                if (res.isSuccess()) {
                    return createEvent("Agility Test - Windy Field Sprint",
                            "Powerful storm gusts sweep the meadow! [Agility Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " leans into the wind and snatches blowing coins!", BigDecimal.valueOf(20), 0f,0f,0f);
                } else {
                    return createEvent("Agility Test - Windy Field Sprint",
                            "Powerful storm gusts sweep the meadow! [Agility Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Strong wind blows Rabbit " + name + " tumbling back.");
                }
            });
        } else if (weather == Weather.HEAT) {
            list.add(() -> {
                float intel = (rabbit.getSecondaryStats() != null && rabbit.getSecondaryStats().getIntelligence() != null) ? rabbit.getSecondaryStats().getIntelligence() : 5f;
                float dc = 12f;
                AdventureHelper.SkillTestResult res = AdventureHelper.performSkillTest(dc, intel);
                if (res.isSuccess()) {
                    return createEvent("Intelligence Test - Sun-Dried Herb Search",
                            "Summer heat concentrates herbal scents! [Intelligence Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " identifies medicinal herbs and collects lettuce!", BigDecimal.ZERO, 0f, 6f, 0f);
                } else {
                    return createEvent("Intelligence Test - Sun-Dried Herb Search",
                            "Summer heat concentrates herbal scents! [Intelligence Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " cannot distinguish dried plants.");
                }
            });
        }

        if (record != null && record.getWindSpeed() > 25.0) {
            list.add(() -> {
                float agi = (rabbit.getSecondaryStats() != null && rabbit.getSecondaryStats().getAgility() != null) ? rabbit.getSecondaryStats().getAgility() : 5f;
                float dc = 13f;
                AdventureHelper.SkillTestResult res = AdventureHelper.performSkillTest(dc, agi);
                if (res.isSuccess()) {
                    return createEvent("Agility Test - Gusty Seed Catch",
                            "Brisk meadow winds scatter wild seeds everywhere! [Agility Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " leaps deftly and snatches a pouch dropped by a farmer!", BigDecimal.valueOf(22), 0f,0f,0f);
                } else {
                    return createEvent("Agility Test - Gusty Seed Catch",
                            "Brisk meadow winds scatter wild seeds everywhere! [Agility Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Swirling winds throw off Rabbit " + name + "'s jump.");
                }
            });
        }

        if (record != null && record.getHumidity() > 80.0) {
            list.add(() -> {
                float intel = (rabbit.getSecondaryStats() != null && rabbit.getSecondaryStats().getIntelligence() != null) ? rabbit.getSecondaryStats().getIntelligence() : 5f;
                float dc = 11f;
                AdventureHelper.SkillTestResult res = AdventureHelper.performSkillTest(dc, intel);
                if (res.isSuccess()) {
                    return createEvent("Intelligence Test - Moist Dew Foraging",
                            "Heavy dew makes wild greenery extra tender! [Intelligence Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " selects the juiciest leaves and gathers fresh lettuce!", BigDecimal.ZERO, 0f, 5f, 0f);
                } else {
                    return createEvent("Intelligence Test - Moist Dew Foraging",
                            "Heavy dew makes wild greenery extra tender! [Intelligence Test: Rolled " + (int)res.roll() + " vs DC " + (int)dc + "] - Rabbit " + name + " picks wet weeds by mistake.");
                }
            });
        }

        return list;
    }

    private List<Supplier<AdventureEvent>> getCombatEvents(String name, Rabbit rabbit) {
        List<Supplier<AdventureEvent>> list = new ArrayList<>();
        list.add(() -> executeCrowCombat(name, rabbit));
        list.add(() -> executeDogCombat(name, rabbit));
        return list;
    }

    private AdventureEvent executeCrowCombat(String name, Rabbit rabbit) {
        float dc = 11f;
        AdventureHelper.AttackModifier attMod = AdventureHelper.getAttackModifier(rabbit);
        AdventureHelper.CombatTestResult res = AdventureHelper.performCombatTest(dc, attMod.statValue());

        String logPrefix = "Combat vs Angry Crow [Roll " + (int)res.roll() + " (Range 0 - " + (int)res.maxRoll() + ") vs DC " + (int)dc + "]: ";

        return switch (res.resultType()) {
            case CRITICAL_VICTORY -> {
                rabbit.setStress(Math.max(0f, rabbit.getStress() - 15f));
                yield createEvent("Combat - Angry Crow",
                        logPrefix + "CRITICAL VICTORY! Rabbit " + name + " charmed crow with " + attMod.attackType() + "! Crow dropped coins!",
                        BigDecimal.valueOf(18), 0f,0f,0f);
            }
            case NORMAL_VICTORY -> {
                rabbit.setStress(Math.max(0f, rabbit.getStress() - 5f));
                yield createEvent("Combat - Angry Crow",
                        logPrefix + "VICTORY! Rabbit " + name + " won using " + attMod.attackType() + ".");
            }
            case NORMAL_DEFEAT -> {
                rabbit.setStress(Math.min(100f, rabbit.getStress() + 10f));
                yield createEvent("Combat - Angry Crow",
                        logPrefix + "DEFEAT! Rabbit " + name + "'s " + attMod.attackType() + " was ignored!");
            }
            case CRITICAL_DEFEAT -> {
                float baseDamage = 15f;
                float damage = AdventureHelper.calculateCombatDamage(rabbit, baseDamage);
                rabbit.setLife(Math.max(0f, (rabbit.getLife() != null ? rabbit.getLife() : 100f) - damage));
                rabbit.setStress(Math.min(100f, rabbit.getStress() + 25f));
                yield createEvent("Combat - Angry Crow",
                        logPrefix + "CRITICAL DEFEAT! Crow pecked Rabbit " + name + " (-" + (int)damage + " HP)!");
            }
        };
    }

    private AdventureEvent executeDogCombat(String name, Rabbit rabbit) {
        float dc = 12f;
        AdventureHelper.AttackModifier attMod = AdventureHelper.getAttackModifier(rabbit);
        AdventureHelper.CombatTestResult res = AdventureHelper.performCombatTest(dc, attMod.statValue());

        String logPrefix = "Combat vs Curious Farm Dog [Roll " + (int)res.roll() + " (Range 0 - " + (int)res.maxRoll() + ") vs DC " + (int)dc + "]: ";

        return switch (res.resultType()) {
            case CRITICAL_VICTORY -> {
                rabbit.setStress(Math.max(0f, rabbit.getStress() - 20f));
                yield createEvent("Combat - Curious Farm Dog",
                        logPrefix + "CRITICAL VICTORY! Rabbit " + name + " bewildered dog with " + attMod.attackType() + "! Dog dropped coins!",
                        BigDecimal.valueOf(20), 0f,0f,0f);
            }
            case NORMAL_VICTORY -> {
                rabbit.setStress(Math.max(0f, rabbit.getStress() - 5f));
                yield createEvent("Combat - Curious Farm Dog",
                        logPrefix + "VICTORY! Rabbit " + name + " calmed dog using " + attMod.attackType() + ".");
            }
            case NORMAL_DEFEAT -> {
                rabbit.setStress(Math.min(100f, rabbit.getStress() + 15f));
                yield createEvent("Combat - Curious Farm Dog",
                        logPrefix + "DEFEAT! Dog barked loudly and chased Rabbit " + name + " away!");
            }
            case CRITICAL_DEFEAT -> {
                float baseDamage = 20f;
                float damage = AdventureHelper.calculateCombatDamage(rabbit, baseDamage);
                rabbit.setLife(Math.max(0f, (rabbit.getLife() != null ? rabbit.getLife() : 100f) - damage));
                rabbit.setStress(Math.min(100f, rabbit.getStress() + 30f));
                yield createEvent("Combat - Curious Farm Dog",
                        logPrefix + "CRITICAL DEFEAT! Dog bit Rabbit " + name + " on tail (-" + (int)damage + " HP)!");
            }
        };
    }
}