package com.kodilla.bungenics.mapper;

import com.kodilla.bungenics.domain.rabbitFarm.Room;
import com.kodilla.bungenics.dto.RoomDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoomMapper {

    public Room mapToRoom(RoomDto dto) {
        Room room = new Room();
        room.setId(dto.getId());
        room.setSlots(dto.getSlots());
        return room;
    }

    public RoomDto mapToRoomDto(Room room) {
        return new RoomDto(
                room.getId(),
                room.getSlots()
        );
    }

    public List<RoomDto> mapToRoomDtoList(List<Room> rooms) {
        return rooms.stream().map(this::mapToRoomDto).toList();
    }
}