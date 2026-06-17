package com.kodilla.bungenics.mapper;

import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.dto.RabbitDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RabbitMapper {

    private final TraitMapper traitMapper;
    private final SecondaryStatsMapper secondaryStatsMapper;

    public Rabbit mapToRabbit(RabbitDto rabbitDto) {
        Rabbit rabbit = new Rabbit();
        rabbit.setId(rabbitDto.getId());
        rabbit.setName(rabbitDto.getName());
        rabbit.setBreed(rabbitDto.getBreed());
        rabbit.setWeight(rabbitDto.getWeight());
        rabbit.setNutritionLevel(rabbitDto.getNutritionLevel());
        rabbit.setLife(rabbitDto.getLife());
        rabbit.setStress(rabbitDto.getStress());
        rabbit.setAge(rabbitDto.getAge());
        rabbit.setMother(rabbitDto.getMotherId());
        rabbit.setFather(rabbitDto.getFatherId());
        rabbit.setStatus(rabbitDto.getStatus());

        if (rabbitDto.getTraits() != null) {
            rabbit.setTraits(traitMapper.mapToTraitList(rabbitDto.getTraits()));
            // Powiązujemy cechy z tym konkretnym królikiem
            rabbit.getTraits().forEach(t -> t.setRabbit(rabbit));
        }

        if (rabbitDto.getSecondaryStats() != null) {
            rabbit.setSecondaryStats(secondaryStatsMapper.mapToSecondaryStats(rabbitDto.getSecondaryStats()));
        }

        return rabbit;
    }

    public RabbitDto mapToRabbitDto(Rabbit rabbit) {
        return new RabbitDto(
                rabbit.getId(),
                rabbit.getName(),
                rabbit.getBreed(),
                rabbit.getWeight(),
                rabbit.getNutritionLevel(),
                rabbit.getLife(),
                rabbit.getStress(),
                rabbit.getAge(),
                rabbit.getMother(),
                rabbit.getFather(),
                rabbit.getStatus(),
                traitMapper.mapToTraitDtoList(rabbit.getTraits()),
                secondaryStatsMapper.mapToSecondaryStatsDto(rabbit.getSecondaryStats())
        );
    }

    public List<RabbitDto> mapToRabbitDtoList(List<Rabbit> rabbits) {
        return rabbits.stream().map(this::mapToRabbitDto).toList();
    }
}