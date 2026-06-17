package com.kodilla.bungenics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RoomDto {
    private Long id;
    private Integer slots;
    private List<RabbitDto> rabbits;
}