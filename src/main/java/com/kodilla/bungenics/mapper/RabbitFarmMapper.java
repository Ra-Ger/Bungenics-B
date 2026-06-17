package com.kodilla.bungenics.mapper;

import com.kodilla.bungenics.domain.player.Player;
import com.kodilla.bungenics.domain.player.RabbitFarm;
import com.kodilla.bungenics.dto.RabbitFarmDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RabbitFarmMapper {

    public RabbitFarm mapToRabbitFarm(RabbitFarmDto dto) {
        RabbitFarm farm = new RabbitFarm();
        farm.setId(dto.getId());

        if (dto.getPlayerId() != null) {
            Player player = new Player();
            player.setId(dto.getPlayerId());
            farm.setPlayer(player);
        }

        farm.setHayAmount(dto.getHayAmount());
        farm.setSpinachAmount(dto.getSpinachAmount());
        farm.setCarrotAmount(dto.getCarrotAmount());
        farm.setLettuceAmount(dto.getLettuceAmount());

        return farm;
    }

    public RabbitFarmDto mapToRabbitFarmDto(RabbitFarm farm) {
        return new RabbitFarmDto(
                farm.getId(),
                farm.getPlayer() != null ? farm.getPlayer().getId() : null,
                farm.getHayAmount(),
                farm.getSpinachAmount(),
                farm.getCarrotAmount(),
                farm.getLettuceAmount()
        );
    }

    public List<RabbitFarmDto> mapToRabbitFarmDtoList(List<RabbitFarm> farms) {
        return farms.stream().map(this::mapToRabbitFarmDto).toList();
    }
}