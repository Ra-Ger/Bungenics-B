package com.kodilla.bungenics.game.strategy;

import com.kodilla.bungenics.dataFetchers.OpenMeteo.WeatherFetcher;
import com.kodilla.bungenics.dataFetchers.OpenMeteo.WeatherRecord;
import com.kodilla.bungenics.domain.adventure.AdventureEvent;
import com.kodilla.bungenics.domain.player.Player;
import com.kodilla.bungenics.domain.rabbit.*;
import com.kodilla.bungenics.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.function.Supplier;

import static com.kodilla.bungenics.game.strategy.AdventureHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeadowAdventureStrategyTest {

    @Mock
    private WeatherFetcher weatherFetcher;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private MeadowAdventureStrategy strategy;

    private Rabbit rabbit;

    @BeforeEach
    void setUp() {
        rabbit = Rabbit.builder()
                .id(1L)
                .name("Fluffy")
                .playerId(1L)
                .life(100f)
                .stress(30f)
                .nutritionLevel(50f)
                .secondaryStats(SecondaryStats.builder()
                        .strength(7f)
                        .agility(8f)
                        .intelligence(9f)
                        .build())
                .traits(new HashSet<>())
                .build();
    }

    @Test
    void shouldReturnMeadowAdventureType() {
        assertEquals("MEADOW", strategy.getAdventureType());
    }

    // ---------- executeAdventure ----------
    @Nested
    class ExecuteAdventure {

        @Test
        void shouldBuildFullSuccessfulAdventure() {
            WeatherRecord record = mock(WeatherRecord.class);
            when(record.toString()).thenReturn("Clear, 20°C");
            when(record.getTemperature()).thenReturn(20.0);
            when(record.getWindSpeed()).thenReturn(10.0);
            when(record.getHumidity()).thenReturn(50.0);
            when(record.getWeatherCode()).thenReturn(0);
            when(weatherFetcher.fetchCurrentWeatherForCity("Warsaw")).thenReturn(record);

            Player player = new Player();
            player.setLocation("Warsaw");
            when(playerRepository.findById(1L)).thenReturn(Optional.of(player));

            try (MockedStatic<AdventureHelper> helper = mockStatic(AdventureHelper.class, CALLS_REAL_METHODS)) {
                helper.when(() -> selectEventsWithExclusion(any(), anyList(), anyList(), anyList()))
                        .thenReturn(Collections.emptyList());

                List<AdventureEvent> events = strategy.executeAdventure(rabbit);

                assertEquals(3, events.size());
                assertEquals("Expedition Departure", events.get(0).getName());
                assertEquals("Expedition Conclusion", events.get(1).getName());
                assertEquals("Expedition Return", events.get(2).getName());
                assertTrue(events.get(0).getResult().contains("Clear, 20°C"));

                verify(weatherFetcher).fetchCurrentWeatherForCity("Warsaw");
                verify(playerRepository).findById(1L);
            }
        }

        @Test
        void shouldUseDefaultLocationWhenPlayerNotFound() {
            WeatherRecord record = mock(WeatherRecord.class);
            when(record.toString()).thenReturn("Mock");
            when(weatherFetcher.fetchCurrentWeatherForCity("Pęcice")).thenReturn(record);
            when(playerRepository.findById(1L)).thenReturn(Optional.empty());

            try (MockedStatic<AdventureHelper> helper = mockStatic(AdventureHelper.class, CALLS_REAL_METHODS)) {
                helper.when(() -> selectEventsWithExclusion(any(), anyList(), anyList(), anyList()))
                        .thenReturn(Collections.emptyList());

                strategy.executeAdventure(rabbit);
                verify(weatherFetcher).fetchCurrentWeatherForCity("Pęcice");
            }
        }

        @Test
        void shouldHandleNullRabbitNameGracefully() {
            Rabbit unnamed = Rabbit.builder().id(99L).life(100f).build();
            WeatherRecord record = mock(WeatherRecord.class);
            when(record.toString()).thenReturn("Mock");
            when(weatherFetcher.fetchCurrentWeatherForCity(anyString())).thenReturn(record);

            try (MockedStatic<AdventureHelper> helper = mockStatic(AdventureHelper.class, CALLS_REAL_METHODS)) {
                helper.when(() -> selectEventsWithExclusion(any(), anyList(), anyList(), anyList()))
                        .thenReturn(Collections.emptyList());

                List<AdventureEvent> events = strategy.executeAdventure(unnamed);
                assertFalse(events.isEmpty(), "Should contain at least departure event");
                assertNotNull(events.get(0), "First event must not be null");
                assertTrue(events.get(0).getResult().contains("Rabbit #99"));
            }
        }

        @Test
        void shouldInterruptAdventureWhenRabbitDies() {
            rabbit.setLife(30f);
            WeatherRecord record = mock(WeatherRecord.class);
            when(record.toString()).thenReturn("Mock");
            when(weatherFetcher.fetchCurrentWeatherForCity(anyString())).thenReturn(record);

            Player player = new Player();
            player.setLocation("City");
            when(playerRepository.findById(1L)).thenReturn(Optional.of(player));

            try (MockedStatic<AdventureHelper> helper = mockStatic(AdventureHelper.class, CALLS_REAL_METHODS)) {
                ArgumentCaptor<List<Supplier<AdventureEvent>>> combatCaptor =
                        ArgumentCaptor.forClass(List.class);

                helper.when(() -> selectEventsWithExclusion(
                                any(Random.class), anyList(),
                                anyList(), combatCaptor.capture()))
                        .thenAnswer(inv -> {
                            List<Supplier<AdventureEvent>> combat = combatCaptor.getValue();
                            return List.of(combat.get(0)); // first combat
                        });

                CombatTestResult combatRes = new CombatTestResult(
                        CombatResultType.CRITICAL_DEFEAT, 4f, 11f, 7f, 18f);
                helper.when(() -> performCombatTest(anyFloat(), anyFloat()))
                        .thenReturn(combatRes);
                helper.when(() -> calculateCombatDamage(eq(rabbit), anyFloat()))
                        .thenReturn(40f); // 30 - 40 = 0
                helper.when(() -> getAttackModifier(rabbit))
                        .thenReturn(new AttackModifier(AttackType.CUDDLING, 7f));

                List<AdventureEvent> events = strategy.executeAdventure(rabbit);

                assertEquals(0f, rabbit.getLife());
                assertEquals(RabbitStatus.DEAD, rabbit.getStatus());
                assertTrue(events.stream().anyMatch(e -> "Expedition Interrupted".equals(e.getName())));
                assertTrue(events.stream().noneMatch(e -> "Expedition Conclusion".equals(e.getName())));
                assertTrue(events.stream().noneMatch(e -> "Expedition Return".equals(e.getName())));
            }
        }
    }

    // ---------- weather mapping & pools ----------
    @Nested
    class WeatherMappingAndPools {

        private Player createPlayer(String location) {
            Player p = new Player();
            p.setLocation(location);
            return p;
        }

        @Test
        void shouldBuildCorrectPoolsForHeatWithAllTriggers() {
            WeatherRecord record = mock(WeatherRecord.class);
            when(record.toString()).thenReturn("Hot");
            when(record.getTemperature()).thenReturn(30.0);
            when(record.getWindSpeed()).thenReturn(30.0);
            when(record.getHumidity()).thenReturn(90.0);
            when(record.getWeatherCode()).thenReturn(0);

            when(weatherFetcher.fetchCurrentWeatherForCity(anyString())).thenReturn(record);
            when(playerRepository.findById(anyLong())).thenReturn(Optional.of(createPlayer("X")));

            try (MockedStatic<AdventureHelper> helper = mockStatic(AdventureHelper.class, CALLS_REAL_METHODS)) {
                SkillTestResult successSkill = new SkillTestResult(true, 15f, 12f, 8f, 20f);
                helper.when(() -> performSkillTest(anyFloat(), anyFloat())).thenReturn(successSkill);

                ArgumentCaptor<List<Supplier<AdventureEvent>>> neutralCaptor =
                        ArgumentCaptor.forClass(List.class);
                ArgumentCaptor<List<Supplier<AdventureEvent>>> skillCaptor =
                        ArgumentCaptor.forClass(List.class);
                ArgumentCaptor<List<Supplier<AdventureEvent>>> combatCaptor =
                        ArgumentCaptor.forClass(List.class);

                helper.when(() -> selectEventsWithExclusion(
                                any(Random.class), neutralCaptor.capture(),
                                skillCaptor.capture(), combatCaptor.capture()))
                        .thenReturn(Collections.emptyList());

                strategy.executeAdventure(rabbit);

                // Neutral pool: 4 base + 2 heat + 1 wind + 1 humidity + 1 clear sky = 9
                List<Supplier<AdventureEvent>> neutralPool = neutralCaptor.getValue();
                assertEquals(9, neutralPool.size());
                List<String> neutralNames = new ArrayList<>();
                neutralPool.forEach(s -> neutralNames.add(s.get().getName()));
                assertTrue(neutralNames.contains("Summer Heatwave"));
                assertTrue(neutralNames.contains("Shaded Tree Shade"));
                assertTrue(neutralNames.contains("Meadow Wind Rustle"));
                assertTrue(neutralNames.contains("Morning Dew Drops"));
                assertTrue(neutralNames.contains("Golden Meadow Sunbeam"));

                // Skill pool: 3 core + 1 heat + 1 wind(>25) + 1 humidity(>80) = 6
                List<Supplier<AdventureEvent>> skillPool = skillCaptor.getValue();
                assertEquals(6, skillPool.size());
                List<String> skillNames = new ArrayList<>();
                skillPool.forEach(s -> skillNames.add(s.get().getName()));
                assertTrue(skillNames.contains("Intelligence Test - Sun-Dried Herb Search"));
                assertTrue(skillNames.contains("Agility Test - Gusty Seed Catch"));
                assertTrue(skillNames.contains("Intelligence Test - Moist Dew Foraging"));

                assertEquals(2, combatCaptor.getValue().size());
            }
        }

        @Test
        void shouldNotAddExtraEventsWhenWeatherIsClearAndNoTriggers() {
            WeatherRecord record = mock(WeatherRecord.class);
            when(record.toString()).thenReturn("Clear, 10°C");
            when(record.getTemperature()).thenReturn(10.0);
            when(record.getWindSpeed()).thenReturn(10.0);
            when(record.getHumidity()).thenReturn(50.0);
            when(record.getWeatherCode()).thenReturn(2);

            when(weatherFetcher.fetchCurrentWeatherForCity(anyString())).thenReturn(record);
            when(playerRepository.findById(anyLong())).thenReturn(Optional.of(createPlayer("X")));

            try (MockedStatic<AdventureHelper> helper = mockStatic(AdventureHelper.class, CALLS_REAL_METHODS)) {
                ArgumentCaptor<List<Supplier<AdventureEvent>>> neutralCaptor =
                        ArgumentCaptor.forClass(List.class);
                helper.when(() -> selectEventsWithExclusion(any(), neutralCaptor.capture(), anyList(), anyList()))
                        .thenReturn(Collections.emptyList());

                strategy.executeAdventure(rabbit);

                List<Supplier<AdventureEvent>> neutralPool = neutralCaptor.getValue();
                assertEquals(4, neutralPool.size());
            }
        }

        @Test
        void shouldHandleSnowWeather() {
            WeatherRecord record = mock(WeatherRecord.class);
            when(record.toString()).thenReturn("Snowy");
            when(record.getTemperature()).thenReturn(-5.0);
            when(record.getWindSpeed()).thenReturn(15.0);
            when(record.getHumidity()).thenReturn(60.0);
            when(record.getWeatherCode()).thenReturn(71);

            when(weatherFetcher.fetchCurrentWeatherForCity(anyString())).thenReturn(record);
            when(playerRepository.findById(anyLong())).thenReturn(Optional.of(createPlayer("X")));

            try (MockedStatic<AdventureHelper> helper = mockStatic(AdventureHelper.class, CALLS_REAL_METHODS)) {
                ArgumentCaptor<List<Supplier<AdventureEvent>>> neutralCaptor =
                        ArgumentCaptor.forClass(List.class);
                helper.when(() -> selectEventsWithExclusion(any(), neutralCaptor.capture(), anyList(), anyList()))
                        .thenReturn(Collections.emptyList());

                strategy.executeAdventure(rabbit);

                List<Supplier<AdventureEvent>> neutralPool = neutralCaptor.getValue();
                assertEquals(6, neutralPool.size()); // 4 base + 2 snow
                List<String> names = new ArrayList<>();
                neutralPool.forEach(s -> names.add(s.get().getName()));
                assertTrue(names.contains("Snowy Meadow Blanket"));
                assertTrue(names.contains("Frosted Clover Patch"));
            }
        }

        @Test
        void shouldHandleStormWeather() {
            WeatherRecord record = mock(WeatherRecord.class);
            when(record.toString()).thenReturn("Stormy");
            when(record.getTemperature()).thenReturn(10.0);
            when(record.getWindSpeed()).thenReturn(35.0);   // >20 and >30
            when(record.getHumidity()).thenReturn(95.0);    // >75
            when(record.getWeatherCode()).thenReturn(95);   // storm

            when(weatherFetcher.fetchCurrentWeatherForCity(anyString())).thenReturn(record);
            when(playerRepository.findById(anyLong())).thenReturn(Optional.of(createPlayer("X")));

            try (MockedStatic<AdventureHelper> helper = mockStatic(AdventureHelper.class, CALLS_REAL_METHODS)) {
                ArgumentCaptor<List<Supplier<AdventureEvent>>> neutralCaptor =
                        ArgumentCaptor.forClass(List.class);
                helper.when(() -> selectEventsWithExclusion(any(), neutralCaptor.capture(), anyList(), anyList()))
                        .thenReturn(Collections.emptyList());

                strategy.executeAdventure(rabbit);

                List<Supplier<AdventureEvent>> neutralPool = neutralCaptor.getValue();
                // 4 base + 2 storm + 1 wind + 1 humidity = 8
                assertEquals(8, neutralPool.size());
                List<String> names = new ArrayList<>();
                neutralPool.forEach(s -> names.add(s.get().getName()));
                assertTrue(names.contains("Meadow Tempest"));
                assertTrue(names.contains("Burrow Shelter"));
                assertTrue(names.contains("Meadow Wind Rustle"));
                assertTrue(names.contains("Morning Dew Drops"));
            }
        }
    }
}