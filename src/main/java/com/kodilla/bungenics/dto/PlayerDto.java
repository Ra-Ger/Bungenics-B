package com.kodilla.bungenics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlayerDto {
    private Long id;
    private String name;
    private String location;
    private Float money;
}
