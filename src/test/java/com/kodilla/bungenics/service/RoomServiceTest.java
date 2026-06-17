// ---------- RoomServiceTest ----------
package com.kodilla.bungenics.service;

import com.kodilla.bungenics.domain.rabbitFarm.Room;
import com.kodilla.bungenics.exception.ResourceNotFoundException;
import com.kodilla.bungenics.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;
    @InjectMocks
    private RoomService roomService;

    @Test
    void shouldCreateRoom() {
        Room room = Room.builder().slots(3).build();
        when(roomRepository.save(room)).thenReturn(room);

        Room result = roomService.createRoom(room);
        assertThat(result).isSameAs(room);
        verify(roomRepository).save(room);
    }

    @Test
    void shouldGetRoomById() {
        Room room = Room.builder().id(1L).slots(2).build();
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        Room result = roomService.getRoomById(1L);
        assertThat(result).isEqualTo(room);
    }

    @Test
    void shouldThrowWhenRoomNotFound() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> roomService.getRoomById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void shouldGetAllRooms() {
        List<Room> rooms = List.of(new Room(), new Room());
        when(roomRepository.findAll()).thenReturn(rooms);

        assertThat(roomService.getAllRooms()).hasSize(2);
    }

    @Test
    void shouldUpdateRoomSlots() {
        Room existing = Room.builder().id(1L).slots(2).build();
        Room details = Room.builder().slots(5).build();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(roomRepository.save(existing)).thenReturn(existing);

        Room result = roomService.updateRoom(1L, details);
        assertThat(result.getSlots()).isEqualTo(5);
        verify(roomRepository).save(existing);
    }
}