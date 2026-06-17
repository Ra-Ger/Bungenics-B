package com.kodilla.bungenics.repository;

import com.kodilla.bungenics.domain.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
}
