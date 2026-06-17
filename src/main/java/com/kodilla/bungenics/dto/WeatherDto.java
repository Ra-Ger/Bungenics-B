package com.kodilla.bungenics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WeatherDto {
    private double temperature;
    private double humidity;
    private double windSpeed;
    private int weatherCode;
    private String weatherDescription;
    private String location;
}