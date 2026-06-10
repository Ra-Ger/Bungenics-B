package com.kodilla.rabbitfeeding.mapper;

import com.kodilla.rabbitfeeding.domain.Rabbit;
import com.kodilla.rabbitfeeding.dto.RabbitDto;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RabbitMapper {

    public Rabbit mapToRabbit(RabbitDto rabbitDto) {
        return new Rabbit(rabbitDto.getId(),
                rabbitDto.getName(),
                rabbitDto.getBreed(),
                rabbitDto.getWeight());
    }

    public RabbitDto mapToRabbitDto(Rabbit rabbit) {
        return new RabbitDto(rabbit.getId(),
                rabbit.getName(),
                rabbit.getBreed(),
                rabbit.getWeight());
    }

    public List<RabbitDto> mapToRabbitDtoList(List<Rabbit> rabbits) {
        return rabbits.stream().map(this::mapToRabbitDto).toList();
    }

}
