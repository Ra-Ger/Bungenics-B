package com.kodilla.rabbitfeeding.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RabbitDto {
    private Long id;
    private String name;
    private String breed;
    private Float weight;
}
