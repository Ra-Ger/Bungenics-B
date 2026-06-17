package com.kodilla.bungenics.dto;

import com.kodilla.bungenics.domain.rabbit.RabbitTrait;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TraitDto {
    private Long id;
    private Long rabbitId;
    private RabbitTrait rabbitTrait;
}
