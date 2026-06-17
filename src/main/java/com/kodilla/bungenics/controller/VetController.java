package com.kodilla.bungenics.controller;

import com.kodilla.bungenics.dto.RabbitDto;
import com.kodilla.bungenics.mapper.RabbitMapper;
import com.kodilla.bungenics.service.VetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vet")
@RequiredArgsConstructor
public class VetController {

    private final VetService vetService;
    private final RabbitMapper rabbitMapper;

    @PostMapping("/{id}/admit")
    public ResponseEntity<RabbitDto> admitToVet(@PathVariable Long id) {
        return ResponseEntity.ok(rabbitMapper.mapToRabbitDto(vetService.admitToVet(id)));
    }

    @PostMapping("/{id}/discharge")
    public ResponseEntity<RabbitDto> dischargeFromVet(@PathVariable Long id) {
        return ResponseEntity.ok(rabbitMapper.mapToRabbitDto(vetService.dischargeFromVet(id)));
    }
}