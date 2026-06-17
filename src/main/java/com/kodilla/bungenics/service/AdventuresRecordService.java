package com.kodilla.bungenics.service;

import com.kodilla.bungenics.domain.adventure.AdventuresRecord;
import com.kodilla.bungenics.exception.ResourceNotFoundException;
import com.kodilla.bungenics.repository.AdventuresRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdventuresRecordService {

    private final AdventuresRecordRepository adventuresRecordRepository;

    public AdventuresRecord createRecord(AdventuresRecord record) {
        return adventuresRecordRepository.save(record);
    }

    public AdventuresRecord getRecordById(Long id) {
        return adventuresRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Adventures Record with id " + id + " not found"));
    }

    public List<AdventuresRecord> getAllRecords() {
        return adventuresRecordRepository.findAll();
    }
}