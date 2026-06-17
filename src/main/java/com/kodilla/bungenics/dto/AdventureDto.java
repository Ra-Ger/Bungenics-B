package com.kodilla.bungenics.dto;

import com.kodilla.bungenics.domain.adventure.AdventureEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class AdventureDto {
    private Long id;
    private String name;
    private Long playerId;
    private Long rabbitId;
    private String type;
    private LocalDateTime endTime;
    private String status;
    private List<AdventureEvent> adventureEvents;
}
