package com.kodilla.bungenics.mapper;

import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.domain.rabbit.RabbitStatus;
import com.kodilla.bungenics.domain.rabbit.RabbitTrait;
import com.kodilla.bungenics.dto.RabbitDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RabbitMapper {

    private final SecondaryStatsMapper secondaryStatsMapper;

    public RabbitDto mapToRabbitDto(Rabbit rabbit) {
        if (rabbit == null) return null;

        Set<String> traitNames = rabbit.getTraits() == null ? Set.of() :
                rabbit.getTraits().stream()
                        .map(RabbitTrait::name)
                        .collect(Collectors.toSet());

        return new RabbitDto(
                rabbit.getId(),
                rabbit.getPlayerId(),
                rabbit.getName(),
                rabbit.getBreed(),
                rabbit.getSex(),
                rabbit.getWeight(),
                rabbit.getAdultWeight(),
                rabbit.getNutritionLevel(),
                rabbit.getLife(),
                rabbit.getStress(),
                rabbit.getAge(),
                rabbit.getMaxLifetime(),
                rabbit.getMotherId(),
                rabbit.getFatherId(),
                rabbit.getStatus() != null ? rabbit.getStatus() : null,
                secondaryStatsMapper.mapToSecondaryStatsDto(rabbit.getSecondaryStats()),
                traitNames,
                rabbit.getBreedingEndTime(),
                rabbit.getAdventureEndTime(),
                rabbit.getVetEndTime(),
                rabbit.getTrainingEndTime(),
                rabbit.getRestEndTime(),
                rabbit.getTrainingEnhancedFood()
        );
    }

    public Rabbit mapToRabbit(RabbitDto rabbitDto) {
        if (rabbitDto == null) return null;

        Set<RabbitTrait> enumTraits = rabbitDto.getTraits() == null ? Set.of() :
                rabbitDto.getTraits().stream()
                        .map(t -> {
                            try {
                                return RabbitTrait.valueOf(t);
                            } catch (IllegalArgumentException e) {
                                return null;
                            }
                        })
                        .filter(t -> t != null)
                        .collect(Collectors.toSet());

        RabbitStatus statusEnum = null;
        if (rabbitDto.getStatus() != null) {
            try {
                statusEnum = RabbitStatus.valueOf(rabbitDto.getStatus().toString().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        return Rabbit.builder()
                .id(rabbitDto.getId())
                .playerId(rabbitDto.getPlayerId())
                .name(rabbitDto.getName())
                .breed(rabbitDto.getBreed())
                .sex(rabbitDto.getSex())
                .weight(rabbitDto.getWeight())
                .adultWeight(rabbitDto.getAdultWeight())
                .nutritionLevel(rabbitDto.getNutritionLevel())
                .life(rabbitDto.getLife())
                .stress(rabbitDto.getStress())
                .age(rabbitDto.getAge())
                .maxLifetime(rabbitDto.getMaxLifetime())
                .motherId(rabbitDto.getMotherId())
                .fatherId(rabbitDto.getFatherId())
                .status(statusEnum)
                .secondaryStats(secondaryStatsMapper.mapToSecondaryStats(rabbitDto.getSecondaryStats()))
                .traits(enumTraits)
                .breedingEndTime(rabbitDto.getBreedingEndTime())
                .adventureEndTime(rabbitDto.getAdventureEndTime())
                .vetEndTime(rabbitDto.getVetEndTime())
                .trainingEndTime(rabbitDto.getTrainingEndTime())
                .restEndTime(rabbitDto.getRestEndTime())
                .trainingEnhancedFood(rabbitDto.getTrainingEnhancedFood())
                .build();
    }

    public List<RabbitDto> mapToRabbitDtoList(List<Rabbit> rabbitList) {
        if (rabbitList == null) return List.of();
        return rabbitList.stream()
                .map(this::mapToRabbitDto)
                .collect(Collectors.toList());
    }
}