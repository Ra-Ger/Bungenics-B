package com.kodilla.bungenics.repository;

import com.kodilla.bungenics.domain.player.RabbitFarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RabbitFarmRepository extends JpaRepository<RabbitFarm, Long> {
}