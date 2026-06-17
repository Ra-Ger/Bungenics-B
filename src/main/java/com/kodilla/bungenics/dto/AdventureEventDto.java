package com.kodilla.bungenics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdventureEventDto {
    private Long id;
    private String name;
    private String result;
}