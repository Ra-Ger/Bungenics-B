package com.kodilla.bungenics.service;

import com.kodilla.bungenics.domain.adventure.AdventuresRecord;
import com.kodilla.bungenics.exception.ResourceNotFoundException;
import com.kodilla.bungenics.repository.AdventuresRecordRepository;
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
class AdventuresRecordServiceTest {

    @Mock
    private AdventuresRecordRepository adventuresRecordRepository;
    @InjectMocks
    private AdventuresRecordService service;

    @Test
    void shouldCreateRecord() {
        AdventuresRecord record = new AdventuresRecord();
        when(adventuresRecordRepository.save(record)).thenReturn(record);
        assertThat(service.createRecord(record)).isSameAs(record);
        verify(adventuresRecordRepository).save(record);
    }

    @Test
    void shouldGetRecordById() {
        AdventuresRecord record = new AdventuresRecord();
        record.setId(1L);
        when(adventuresRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        assertThat(service.getRecordById(1L)).isEqualTo(record);
    }

    @Test
    void shouldThrowWhenRecordNotFound() {
        when(adventuresRecordRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getRecordById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void shouldGetAllRecords() {
        when(adventuresRecordRepository.findAll()).thenReturn(List.of(new AdventuresRecord(), new AdventuresRecord()));
        assertThat(service.getAllRecords()).hasSize(2);
    }
}