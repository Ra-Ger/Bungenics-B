package com.kodilla.rabbitfeeding.dto;

import com.kodilla.rabbitfeeding.domain.Rabbit;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class FeedingDto {
    private Long id;
    private Rabbit rabbit;
    private String foodType;
    private Float amount;
    private LocalDateTime feedingTime;
}
