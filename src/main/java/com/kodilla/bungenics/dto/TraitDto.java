package com.kodilla.bungenics.dto;

import com.kodilla.bungenics.domain.rabbit.TraitType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TraitDto {
    private Long id;
    private Long rabbitId;
    private TraitType traitType;
}
