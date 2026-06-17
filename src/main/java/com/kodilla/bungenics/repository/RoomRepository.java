package com.kodilla.bungenics.repository;

import com.kodilla.bungenics.domain.rabbit.Rabbit;
import com.kodilla.bungenics.domain.rabbitFarm.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRabbitsContaining(Rabbit rabbit);
    Optional<Room> findByRabbits_Id(Long rabbitId);
}