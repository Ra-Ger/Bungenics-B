package com.kodilla.bungenics.repository;

import com.kodilla.bungenics.domain.rabbitFarm.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
}