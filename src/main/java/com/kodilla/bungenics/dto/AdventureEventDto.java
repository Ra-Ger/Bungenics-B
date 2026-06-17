package com.kodilla.bungenics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class AdventureEventDto {
    private Long id;
    private String name;
    private String result;
    private BigDecimal goldReward;
    private Float carrotReward;
    private Float lettuceReward;
    private Float spinachReward;
}