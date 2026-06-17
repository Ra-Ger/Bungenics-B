package com.kodilla.bungenics.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kodilla.bungenics.domain.rabbit.RabbitStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Backend Data Transfer Object for transferring Rabbit state across services and REST endpoints.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RabbitDto {

    private Long id;
    private Long playerId;
    private String name;
    private String breed;
    private String sex;
    private Float weight;
    private Float adultWeight;
    private Float nutritionLevel;
    private Float life;
    private Float stress;
    private Float age;
    private Float maxLifetime;
    private Long motherId;
    private Long fatherId;
    private RabbitStatus status;
    private SecondaryStatsDto secondaryStats;
    private Set<String> traits;

    private LocalDateTime breedingEndTime;
    private LocalDateTime adventureEndTime;
    private LocalDateTime vetEndTime;
    private LocalDateTime trainingEndTime;
    private LocalDateTime restEndTime;

    private String trainingEnhancedFood;
}