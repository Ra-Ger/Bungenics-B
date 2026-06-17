package com.kodilla.bungenics.dataFetchers.OpenMeteo;

import java.util.HashMap;
import java.util.Map;

public class WeatherCodeMapper {

    private static final Map<Integer, String> WEATHER_MAP = new HashMap<>();

    static {
        // Clear sky / Cloud cover
        WEATHER_MAP.put(0, "Clear sky");
        WEATHER_MAP.put(1, "Mainly clear");
        WEATHER_MAP.put(2, "Partly cloudy");
        WEATHER_MAP.put(3, "Overcast");

        // Fog
        WEATHER_MAP.put(45, "Fog");
        WEATHER_MAP.put(48, "Depositing rime fog");

        // Drizzle
        WEATHER_MAP.put(51, "Light drizzle");
        WEATHER_MAP.put(53, "Moderate drizzle");
        WEATHER_MAP.put(55, "Dense drizzle");
        WEATHER_MAP.put(56, "Light freezing drizzle");
        WEATHER_MAP.put(57, "Dense freezing drizzle");

        // Rain
        WEATHER_MAP.put(61, "Light rain");
        WEATHER_MAP.put(63, "Moderate rain");
        WEATHER_MAP.put(65, "Heavy rain");
        WEATHER_MAP.put(66, "Light freezing rain");
        WEATHER_MAP.put(67, "Heavy freezing rain");

        // Snow
        WEATHER_MAP.put(71, "Light snow");
        WEATHER_MAP.put(73, "Moderate snow");
        WEATHER_MAP.put(75, "Heavy snow");
        WEATHER_MAP.put(77, "Snow grains");

        // Showers
        WEATHER_MAP.put(80, "Light rain showers");
        WEATHER_MAP.put(81, "Moderate rain showers");
        WEATHER_MAP.put(82, "Violent rain showers");
        WEATHER_MAP.put(85, "Light snow showers");
        WEATHER_MAP.put(86, "Heavy snow showers");

        // Thunderstorms
        WEATHER_MAP.put(95, "Thunderstorm");
        WEATHER_MAP.put(96, "Thunderstorm with hail");
        WEATHER_MAP.put(99, "Heavy thunderstorm with hail");
    }

    public static String getDescription(int code) {
        return WEATHER_MAP.getOrDefault(code, "Unknown weather (code: " + code + ")");
    }
}