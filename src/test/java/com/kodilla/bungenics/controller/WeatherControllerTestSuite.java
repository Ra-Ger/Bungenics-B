package com.kodilla.bungenics.controller;

import com.kodilla.bungenics.dataFetchers.OpenMeteo.WeatherFetcher;
import com.kodilla.bungenics.dataFetchers.OpenMeteo.WeatherRecord;
import com.kodilla.bungenics.dto.WeatherDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherControllerTestSuite {

    @Mock
    private WeatherFetcher weatherFetcher;

    @InjectMocks
    private WeatherController controller;

    @Test
    void shouldReturnWeatherDataWhenFetchSucceeds() {
        String city = "Warsaw";
        WeatherRecord record = new WeatherRecord(
                22.5, 60.0, 12.3, 800,
                "Clear sky", "2025-01-01T12:00", "Warsaw"
        );
        when(weatherFetcher.fetchCurrentWeatherForCity(city)).thenReturn(record);

        ResponseEntity<WeatherDto> response = controller.getWeather(city);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        WeatherDto dto = response.getBody();
        assertNotNull(dto);
        assertEquals(22.5, dto.getTemperature(), 0.001);
        assertEquals(60.0, dto.getHumidity(), 0.001);
        assertEquals(12.3, dto.getWindSpeed(), 0.001);
        assertEquals(800, dto.getWeatherCode());
        assertEquals("Clear sky", dto.getWeatherDescription());
        assertEquals("Warsaw", dto.getLocation());
    }

    @Test
    void shouldReturnBadRequestWhenFetchFails() {
        String city = "InvalidCity";
        when(weatherFetcher.fetchCurrentWeatherForCity(city))
                .thenThrow(new RuntimeException("City not found"));

        ResponseEntity<WeatherDto> response = controller.getWeather(city);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }
}