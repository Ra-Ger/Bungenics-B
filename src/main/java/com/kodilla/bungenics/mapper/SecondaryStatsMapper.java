package com.kodilla.bungenics.mapper;

import com.kodilla.bungenics.domain.rabbit.SecondaryStats;
import com.kodilla.bungenics.dto.SecondaryStatsDto;
import org.springframework.stereotype.Component;

@Component
public class SecondaryStatsMapper {

    public SecondaryStats mapToSecondaryStats(SecondaryStatsDto dto) {
        if (dto == null) return null;
        return new SecondaryStats(
                dto.getId(),
                dto.getWeight(),
                dto.getNutritionLevel(),
                dto.getLife(),
                dto.getStress(),
                dto.getAge(),
                dto.getStrength(),
                dto.getAgility(),
                dto.getIntelligence(),
                dto.getPreferredAttack()
        );
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
                stats.getPreferredAttack()
        );
    }
}
