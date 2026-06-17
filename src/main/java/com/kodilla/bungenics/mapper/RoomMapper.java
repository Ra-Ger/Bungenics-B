package com.kodilla.bungenics.mapper;

import com.kodilla.bungenics.domain.rabbitFarm.Room;
import com.kodilla.bungenics.dto.RoomDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RoomMapper {

    @Lazy
    private final RabbitMapper rabbitMapper;

    public Room mapToRoom(RoomDto dto) {
        Room room = new Room();
        room.setId(dto.getId());
        room.setSlots(dto.getSlots());
        if (dto.getRabbits() != null) {
            room.setRabbits(dto.getRabbits().stream().map(rabbitMapper::mapToRabbit).toList());
        }
        return room;
    }

    public RoomDto mapToRoomDto(Room room) {
        return new RoomDto(
                room.getId(),
                room.getSlots(),
                room.getRabbits() != null ? rabbitMapper.mapToRabbitDtoList(room.getRabbits()) : new ArrayList<>()
        );
    }

    public List<Room> mapToRoomList(List<RoomDto> dtos) {
        return dtos.stream().map(this::mapToRoom).toList();
    }

    public List<RoomDto> mapToRoomDtoList(List<Room> rooms) {
        return rooms.stream().map(this::mapToRoomDto).toList();
    }
}