package com.kodilla.bungenics.controller;

import com.kodilla.bungenics.domain.adventure.AdventuresRecord;
import com.kodilla.bungenics.dto.AdventuresRecordDto;
import com.kodilla.bungenics.mapper.AdventuresRecordMapper;
import com.kodilla.bungenics.service.AdventuresRecordService;
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
class AdventuresRecordControllerTestSuite {

    @Mock
    private AdventuresRecordMapper recordMapper;

    @Mock
    private AdventuresRecordService recordService;

    @InjectMocks
    private AdventuresRecordController controller;

    @Test
    void shouldGetAllRecords() {
        AdventuresRecord record = new AdventuresRecord();
        AdventuresRecordDto dto = mock(AdventuresRecordDto.class);
        when(recordService.getAllRecords()).thenReturn(List.of(record));
        when(recordMapper.mapToAdventuresRecordDtoList(anyList())).thenReturn(List.of(dto));

        List<AdventuresRecordDto> result = controller.getRecords();
        assertEquals(1, result.size());
        verify(recordService).getAllRecords();
    }

    @Test
    void shouldGetRecordById() {
        Long id = 1L;
        AdventuresRecord record = new AdventuresRecord();
        AdventuresRecordDto dto = mock(AdventuresRecordDto.class);
        when(recordService.getRecordById(id)).thenReturn(record);
        when(recordMapper.mapToAdventuresRecordDto(record)).thenReturn(dto);

        ResponseEntity<AdventuresRecordDto> response = controller.getRecord(id);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void shouldCreateRecord() {
        AdventuresRecordDto inputDto = mock(AdventuresRecordDto.class);
        AdventuresRecord mapped = new AdventuresRecord();
        AdventuresRecord created = new AdventuresRecord();
        AdventuresRecordDto outputDto = mock(AdventuresRecordDto.class);
        when(recordMapper.mapToAdventuresRecord(inputDto)).thenReturn(mapped);
        when(recordService.createRecord(mapped)).thenReturn(created);
        when(recordMapper.mapToAdventuresRecordDto(created)).thenReturn(outputDto);

        ResponseEntity<AdventuresRecordDto> response = controller.createRecord(inputDto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(outputDto, response.getBody());
        verify(recordService).createRecord(mapped);
    }
}