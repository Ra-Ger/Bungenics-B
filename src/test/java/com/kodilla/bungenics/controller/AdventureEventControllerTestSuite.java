package com.kodilla.bungenics.controller;

import com.kodilla.bungenics.domain.adventure.AdventureEvent;
import com.kodilla.bungenics.dto.AdventureEventDto;
import com.kodilla.bungenics.mapper.AdventureEventMapper;
import com.kodilla.bungenics.service.AdventureEventService;
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
class AdventureEventControllerTestSuite {

    @Mock
    private AdventureEventMapper eventMapper;

    @Mock
    private AdventureEventService eventService;

    @InjectMocks
    private AdventureEventController controller;

    @Test
    void shouldGetAllEvents() {
        AdventureEvent event = new AdventureEvent();
        AdventureEventDto dto = mock(AdventureEventDto.class);
        when(eventService.getAllEvents()).thenReturn(List.of(event));
        when(eventMapper.mapToAdventureEventDtoList(anyList())).thenReturn(List.of(dto));

        List<AdventureEventDto> result = controller.getEvents();
        assertEquals(1, result.size());
        verify(eventService).getAllEvents();
    }

    @Test
    void shouldGetEventById() {
        Long id = 1L;
        AdventureEvent event = new AdventureEvent();
        AdventureEventDto dto = mock(AdventureEventDto.class);
        when(eventService.getEventById(id)).thenReturn(event);
        when(eventMapper.mapToAdventureEventDto(event)).thenReturn(dto);

        ResponseEntity<AdventureEventDto> response = controller.getEvent(id);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void shouldCreateEvent() {
        AdventureEventDto inputDto = mock(AdventureEventDto.class);
        AdventureEvent mapped = new AdventureEvent();
        AdventureEvent created = new AdventureEvent();
        AdventureEventDto outputDto = mock(AdventureEventDto.class);
        when(eventMapper.mapToAdventureEvent(inputDto)).thenReturn(mapped);
        when(eventService.createEvent(mapped)).thenReturn(created);
        when(eventMapper.mapToAdventureEventDto(created)).thenReturn(outputDto);

        ResponseEntity<AdventureEventDto> response = controller.createEvent(inputDto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(outputDto, response.getBody());
        verify(eventService).createEvent(mapped);
    }
}