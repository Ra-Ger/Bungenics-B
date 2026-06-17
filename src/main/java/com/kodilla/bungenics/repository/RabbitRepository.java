package com.kodilla.bungenics.repository;

import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.domain.rabbit.RabbitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RabbitRepository extends JpaRepository<Rabbit, Long> {
    List<Rabbit> findByPlayerId(long playerId);
    List<Rabbit> findByStatus(RabbitStatus rabbitStatus);
}
