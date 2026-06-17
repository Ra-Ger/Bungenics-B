package com.kodilla.bungenics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class FeedingDto {
    private Long id;
    private Long rabbitId;
    private String foodType;
    private Float amount;
    private LocalDateTime feedingTime;
}
