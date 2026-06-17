package com.kodilla.bungenics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoomDto {
    private Long id;
    private Integer slots;
}