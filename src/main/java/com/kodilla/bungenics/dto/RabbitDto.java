package com.kodilla.bungenics.dto;

import com.kodilla.bungenics.domain.rabbit.Breed;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RabbitDto {
    private Long id;
    private String name;
    private Breed breed;
    private Float weight;
    private Float nutritionLevel;
    private Float life;
    private Float stress;
    private Float age;
    private Long motherId;
    private Long fatherId;
    private String status;
    private List<TraitDto> traits;
    private SecondaryStatsDto secondaryStats;
}
