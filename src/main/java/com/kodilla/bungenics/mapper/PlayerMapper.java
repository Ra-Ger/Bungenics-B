package com.kodilla.bungenics.mapper;

import com.kodilla.bungenics.domain.player.Player;
import com.kodilla.bungenics.dto.PlayerDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlayerMapper {

    public Player mapToPlayer(PlayerDto playerDto) {
        Player player = new Player();
        player.setId(playerDto.getId());
        player.setName(playerDto.getName());
        player.setLocation(playerDto.getLocation());
        player.setMoney(playerDto.getMoney());
        return player;
    }

    public PlayerDto mapToPlayerDto(Player player) {
        return new PlayerDto(
                player.getId(),
                player.getName(),
                player.getLocation(),
                player.getMoney()
        );
    }

    public List<PlayerDto> mapToPlayerDtoList(List<Player> players) {
        return players.stream().map(this::mapToPlayerDto).toList();
    }
}
