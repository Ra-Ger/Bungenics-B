package com.kodilla.bungenics.repository;

import com.kodilla.bungenics.domain.rabbit.Rabbit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RabbitRepository extends JpaRepository<Rabbit, Long> {
}
