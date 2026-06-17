package com.kodilla.bungenics.controller;

import com.kodilla.bungenics.domain.rabbitFarm.Room;
import com.kodilla.bungenics.domain.rabbitFarm.Structure;
import com.kodilla.bungenics.domain.rabbitFarm.StructureType;
import com.kodilla.bungenics.dto.RoomDto;
import com.kodilla.bungenics.dto.StructureDto;
import com.kodilla.bungenics.mapper.RoomMapper;
import com.kodilla.bungenics.mapper.StructureMapper;
import com.kodilla.bungenics.service.StructureService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StructureControllerTestSuite {

    @Mock
    private StructureMapper structureMapper;

    @Mock
    private RoomMapper roomMapper;

    @Mock
    private StructureService structureService;

    @InjectMocks
    private StructureController controller;

    @Test
    void shouldGetAllStructures() {
        Structure structure = new Structure();
        StructureDto dto = mock(StructureDto.class);
        when(structureService.getAllStructures()).thenReturn(List.of(structure));
        when(structureMapper.mapToStructureDtoList(anyList())).thenReturn(List.of(dto));

        List<StructureDto> result = controller.getStructures();
        assertEquals(1, result.size());
        verify(structureService).getAllStructures();
    }

    @Test
    void shouldGetStructureById() {
        Long id = 1L;
        Structure structure = new Structure();
        StructureDto dto = mock(StructureDto.class);
        when(structureService.getStructureById(id)).thenReturn(structure);
        when(structureMapper.mapToStructureDto(structure)).thenReturn(dto);

        ResponseEntity<StructureDto> response = controller.getStructure(id);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void shouldBuildStructure() {
        Long farmId = 1L;
        StructureType type = StructureType.WARREN;
        Integer gridIndex = 0;
        Structure built = new Structure();
        StructureDto dto = mock(StructureDto.class);
        when(structureService.buildStructure(farmId, type, gridIndex)).thenReturn(built);
        when(structureMapper.mapToStructureDto(built)).thenReturn(dto);

        ResponseEntity<StructureDto> response = controller.buildStructure(farmId, type, gridIndex);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(structureService).buildStructure(farmId, type, gridIndex);
    }

    @Test
    void shouldAddRoomToStructure() {
        Long structureId = 1L;
        Structure updated = new Structure();
        StructureDto dto = mock(StructureDto.class);
        when(structureService.addRoomToStructure(structureId)).thenReturn(updated);
        when(structureMapper.mapToStructureDto(updated)).thenReturn(dto);

        ResponseEntity<StructureDto> response = controller.addRoom(structureId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(structureService).addRoomToStructure(structureId);
    }

    @Test
    void shouldExpandRoom() {
        Long roomId = 2L;
        Room expanded = new Room();
        RoomDto dto = mock(RoomDto.class);
        when(structureService.expandRoomSlots(roomId)).thenReturn(expanded);
        when(roomMapper.mapToRoomDto(expanded)).thenReturn(dto);

        ResponseEntity<RoomDto> response = controller.expandRoom(roomId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(structureService).expandRoomSlots(roomId);
    }

    @Test
    void shouldAssignRabbitToRoom() {
        Long roomId = 3L;
        Long rabbitId = 10L;
        Room updated = new Room();
        RoomDto dto = mock(RoomDto.class);
        when(structureService.assignRabbitToRoom(roomId, rabbitId)).thenReturn(updated);
        when(roomMapper.mapToRoomDto(updated)).thenReturn(dto);

        ResponseEntity<RoomDto> response = controller.assignRabbit(roomId, rabbitId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(structureService).assignRabbitToRoom(roomId, rabbitId);
    }

    @Test
    void shouldRemoveRabbitFromRoom() {
        Long roomId = 3L;
        Long rabbitId = 10L;
        Room updated = new Room();
        RoomDto dto = mock(RoomDto.class);
        when(structureService.removeRabbitFromRoom(roomId, rabbitId)).thenReturn(updated);
        when(roomMapper.mapToRoomDto(updated)).thenReturn(dto);

        ResponseEntity<RoomDto> response = controller.removeRabbit(roomId, rabbitId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(structureService).removeRabbitFromRoom(roomId, rabbitId);
    }

    @Test
    void shouldDeleteStructure() {
        Long id = 1L;
        doNothing().when(structureService).deleteStructure(id);

        ResponseEntity<Void> response = controller.deleteStructure(id);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(structureService).deleteStructure(id);
    }
}