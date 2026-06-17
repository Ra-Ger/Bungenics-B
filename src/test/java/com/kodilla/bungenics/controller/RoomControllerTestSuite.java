package com.kodilla.bungenics.controller;

import com.kodilla.bungenics.domain.rabbitFarm.Room;
import com.kodilla.bungenics.dto.RoomDto;
import com.kodilla.bungenics.mapper.RoomMapper;
import com.kodilla.bungenics.service.RoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomControllerTestSuite {

    @Mock
    private RoomMapper roomMapper;

    @Mock
    private RoomService roomService;

    @InjectMocks
    private RoomController controller;

    @Test
    void shouldGetAllRooms() {
        Room room = new Room();
        RoomDto dto = mock(RoomDto.class);
        when(roomService.getAllRooms()).thenReturn(List.of(room));
        when(roomMapper.mapToRoomDtoList(anyList())).thenReturn(List.of(dto));

        List<RoomDto> result = controller.getRooms();
        assertEquals(1, result.size());
        verify(roomService).getAllRooms();
    }

    @Test
    void shouldGetRoomById() {
        Long id = 1L;
        Room room = new Room();
        RoomDto dto = mock(RoomDto.class);
        when(roomService.getRoomById(id)).thenReturn(room);
        when(roomMapper.mapToRoomDto(room)).thenReturn(dto);

        ResponseEntity<RoomDto> response = controller.getRoom(id);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void shouldCreateRoom() {
        RoomDto inputDto = mock(RoomDto.class);
        Room mapped = new Room();
        Room created = new Room();
        RoomDto outputDto = mock(RoomDto.class);
        when(roomMapper.mapToRoom(inputDto)).thenReturn(mapped);
        when(roomService.createRoom(mapped)).thenReturn(created);
        when(roomMapper.mapToRoomDto(created)).thenReturn(outputDto);

        ResponseEntity<RoomDto> response = controller.createRoom(inputDto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(outputDto, response.getBody());
        verify(roomService).createRoom(mapped);
    }

    @Test
    void shouldUpdateRoom() {
        Long id = 1L;
        RoomDto inputDto = mock(RoomDto.class);
        Room mapped = new Room();
        Room updated = new Room();
        RoomDto outputDto = mock(RoomDto.class);
        when(roomMapper.mapToRoom(inputDto)).thenReturn(mapped);
        when(roomService.updateRoom(id, mapped)).thenReturn(updated);
        when(roomMapper.mapToRoomDto(updated)).thenReturn(outputDto);

        ResponseEntity<RoomDto> response = controller.updateRoom(id, inputDto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(outputDto, response.getBody());
        verify(roomService).updateRoom(id, mapped);
    }
}