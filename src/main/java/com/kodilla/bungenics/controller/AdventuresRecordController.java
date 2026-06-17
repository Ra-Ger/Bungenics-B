package com.kodilla.bungenics.controller;

import com.kodilla.bungenics.domain.adventure.AdventuresRecord;
import com.kodilla.bungenics.dto.AdventuresRecordDto;
import com.kodilla.bungenics.mapper.AdventuresRecordMapper;
import com.kodilla.bungenics.service.AdventuresRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/adventure-records")
public class AdventuresRecordController {

    private final AdventuresRecordMapper recordMapper;
    private final AdventuresRecordService recordService;

    @GetMapping
    public List<AdventuresRecordDto> getRecords() {
        return recordMapper.mapToAdventuresRecordDtoList(recordService.getAllRecords());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdventuresRecordDto> getRecord(@PathVariable Long id) {
        return ResponseEntity.ok(recordMapper.mapToAdventuresRecordDto(recordService.getRecordById(id)));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AdventuresRecordDto> createRecord(@RequestBody AdventuresRecordDto recordDto) {
        AdventuresRecord record = recordService.createRecord(recordMapper.mapToAdventuresRecord(recordDto));
        return ResponseEntity.status(201).body(recordMapper.mapToAdventuresRecordDto(record));
    }
}