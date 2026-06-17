package com.kodilla.bungenics.controller;

import com.kodilla.bungenics.dataFetchers.OpenMeteo.WeatherFetcher;
import com.kodilla.bungenics.dataFetchers.OpenMeteo.WeatherRecord;
import com.kodilla.bungenics.dto.WeatherDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherFetcher weatherFetcher;

    @GetMapping
    public ResponseEntity<WeatherDto> getWeather(@RequestParam String city) {
        try {
            WeatherRecord record = weatherFetcher.fetchCurrentWeatherForCity(city);
            WeatherDto dto = new WeatherDto(
                    record.getTemperature(),
                    record.getHumidity(),
                    record.getWindSpeed(),
                    record.getWeatherCode(),
                    record.getWeatherDescription(),
                    record.getLocation()
            );
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            System.err.println("[BACKEND WEATHER ERROR] Failed to fetch weather for " + city + ": " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}