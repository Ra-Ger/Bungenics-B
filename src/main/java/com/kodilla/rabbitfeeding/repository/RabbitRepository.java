package com.kodilla.rabbitfeeding.repository;

import com.kodilla.rabbitfeeding.domain.Rabbit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RabbitRepository extends JpaRepository<Rabbit, Long> {
    // nie trzeba nadpisywać findAll() to i tak działa
    //    @Override
//    List<Rabbit> findAll();
}
