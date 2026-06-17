package com.kodilla.bungenics.mapper;

import com.kodilla.bungenics.domain.rabbit.SecondaryStats;
import com.kodilla.bungenics.dto.SecondaryStatsDto;
import org.springframework.stereotype.Component;

@Component
public class SecondaryStatsMapper {

    public SecondaryStats mapToSecondaryStats(SecondaryStatsDto dto) {
        if (dto == null) return null;
        return SecondaryStats.builder()
                .id(dto.getId())
                .weight(dto.getWeight())
                .nutritionLevel(dto.getNutritionLevel())
                .life(dto.getLife())
                .stress(dto.getStress())
                .age(dto.getAge())
                .strength(dto.getStrength())
                .agility(dto.getAgility())
                .intelligence(dto.getIntelligence())
                .basicStrength(dto.getBasicStrength())
                .basicAgility(dto.getBasicAgility())
                .basicIntelligence(dto.getBasicIntelligence())
                .preferredAttack(dto.getPreferredAttack())
                .build();
    }

    public SecondaryStatsDto mapToSecondaryStatsDto(SecondaryStats stats) {
        if (stats == null) return null;
        return new SecondaryStatsDto(
                stats.getId(),
                stats.getWeight(),
                stats.getNutritionLevel(),
                stats.getLife(),
                stats.getStress(),
                stats.getAge(),
                stats.getStrength(),
                stats.getAgility(),
                stats.getIntelligence(),
                stats.getBasicStrength(),
                stats.getBasicAgility(),
                stats.getBasicIntelligence(),
                stats.getPreferredAttack()
        );
    }
}