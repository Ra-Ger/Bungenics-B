package com.kodilla.bungenics.dto;

import com.kodilla.bungenics.domain.rabbit.AttackType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SecondaryStatsDto {
    private Long id;
    private Float weight;
    private Float nutritionLevel;
    private Float life;
    private Float stress;
    private Float age;
    private Float strength;
    private Float agility;
    private Float intelligence;
    private AttackType preferredAttack;
}
