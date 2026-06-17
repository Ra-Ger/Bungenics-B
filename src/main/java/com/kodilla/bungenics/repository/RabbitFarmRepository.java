package com.kodilla.bungenics.repository;

import com.kodilla.bungenics.domain.player.RabbitFarm;
import com.kodilla.bungenics.domain.rabbit.Rabbit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RabbitFarmRepository extends JpaRepository<RabbitFarm, Long> {
    Optional<RabbitFarm> findByPlayerId(long playerId);
}