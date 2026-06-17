package com.kodilla.bungenics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PlayerDto {
    private Long id;
    private String name;
    private String location;
    private BigDecimal money;
}
