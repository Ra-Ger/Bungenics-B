package com.kodilla.bungenics.service;

import com.kodilla.bungenics.domain.player.Player;
import com.kodilla.bungenics.exception.ResourceNotFoundException;
import com.kodilla.bungenics.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    public Player createPlayer(Player player) {
        if (player.getMoney() == null) {
            player.setMoney(100f);
        }
        return playerRepository.save(player);
    }

    public Player getPlayerById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player with id " + id + " not found"));
    }

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    public Player updatePlayer(Long id, Player playerDetails) {
        Player existingPlayer = getPlayerById(id);
        existingPlayer.setName(playerDetails.getName());
        existingPlayer.setLocation(playerDetails.getLocation());
        if (playerDetails.getMoney() != null) {
            existingPlayer.setMoney(playerDetails.getMoney());
        }
        return playerRepository.save(existingPlayer);
    }
}
