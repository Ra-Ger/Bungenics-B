package com.kodilla.bungenics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RabbitFarmDto {
    private Long id;
    private Long playerId;
    private Float hayAmount;
    private Float spinachAmount;
    private Float carrotAmount;
    private Float lettuceAmount;
}