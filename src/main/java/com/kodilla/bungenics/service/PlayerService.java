package com.kodilla.bungenics.service;

import com.kodilla.bungenics.domain.player.Player;
import com.kodilla.bungenics.domain.player.RabbitFarm;
import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.domain.rabbitFarm.Room;
import com.kodilla.bungenics.domain.rabbitFarm.Structure;
import com.kodilla.bungenics.exception.ResourceNotFoundException;
import com.kodilla.bungenics.repository.AdventureRepository;
import com.kodilla.bungenics.repository.AdventuresRecordRepository;
import com.kodilla.bungenics.repository.PlayerRepository;
import com.kodilla.bungenics.repository.RabbitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final AdventureRepository adventureRepository;
    private final AdventuresRecordRepository adventuresRecordRepository;
    private final RabbitRepository rabbitRepository;

    @Transactional
    public Player createPlayer(Player player) {
        if (player.getMoney() == null) {
            player.setMoney(new BigDecimal(100));
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

    @Transactional
    public Player updatePlayer(Long id, Player playerDetails) {
        Player existingPlayer = getPlayerById(id);
        existingPlayer.setName(playerDetails.getName());
        existingPlayer.setLocation(playerDetails.getLocation());
        if (playerDetails.getMoney() != null) {
            existingPlayer.setMoney(playerDetails.getMoney());
        }
        return playerRepository.save(existingPlayer);
    }

    @Transactional
    public void deletePlayer(Long id) {
        Player player = getPlayerById(id);

        adventureRepository.findAll().stream()
                .filter(a -> id.equals(a.getPlayerId()))
                .forEach(adventureRepository::delete);

        RabbitFarm farm = player.getRabbitFarm();
        if (farm != null) {
            if (farm.getStructures() != null) {
                for (Structure structure : farm.getStructures()) {
                    if (structure.getRooms() != null) {
                        for (Room room : structure.getRooms()) {
                            if (room.getRabbits() != null) {
                                for (Rabbit rabbit : room.getRabbits()) {
                                    adventuresRecordRepository.findByRabbitId(rabbit.getId())
                                            .ifPresent(adventuresRecordRepository::delete);
                                }
                            }
                        }
                    }
                }
            }
        }

        playerRepository.delete(player);
        System.out.println("[DB CLEANUP] Successfully wiped player ID " + id + " and all associated farm infrastructure.");
    }
}