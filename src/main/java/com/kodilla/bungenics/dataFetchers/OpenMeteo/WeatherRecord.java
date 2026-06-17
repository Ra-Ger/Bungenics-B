package com.kodilla.bungenics.dataFetchers.OpenMeteo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class WeatherRecord {
    private final double temperature;
    private final double humidity;
    private final double windSpeed;
    private final int weatherCode;
    private final String weatherDescription;
    private final String timestamp;
    private final String location;

    @Override
    public String toString() {
        return String.format("📍 %s | 🕐 %s | 🌡️ %.1f°C | 💧 %.1f%% | 🌬️ %.1f km/h | %s",
                location, timestamp, temperature, humidity, windSpeed, weatherDescription);
    }
}
