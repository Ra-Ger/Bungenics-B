package com.kodilla.bungenics.service;

import com.kodilla.bungenics.domain.adventure.AdventureEvent;
import com.kodilla.bungenics.exception.ResourceNotFoundException;
import com.kodilla.bungenics.repository.AdventureEventRepository;
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
class AdventureEventServiceTest {

    @Mock
    private AdventureEventRepository adventureEventRepository;
    @InjectMocks
    private AdventureEventService service;

    @Test
    void shouldCreateEvent() {
        AdventureEvent event = new AdventureEvent();
        when(adventureEventRepository.save(event)).thenReturn(event);
        assertThat(service.createEvent(event)).isSameAs(event);
        verify(adventureEventRepository).save(event);
    }

    @Test
    void shouldGetEventById() {
        AdventureEvent event = new AdventureEvent();
        event.setId(1L);
        when(adventureEventRepository.findById(1L)).thenReturn(Optional.of(event));
        assertThat(service.getEventById(1L)).isEqualTo(event);
    }

    @Test
    void shouldThrowWhenEventNotFound() {
        when(adventureEventRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getEventById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void shouldGetAllEvents() {
        when(adventureEventRepository.findAll()).thenReturn(List.of(new AdventureEvent(), new AdventureEvent()));
        assertThat(service.getAllEvents()).hasSize(2);
    }
}