package com.distribuidoraamaya.Backend_Amaya.controller;

import com.distribuidoraamaya.Backend_Amaya.dto.request.ComprobanteRequest;
import com.distribuidoraamaya.Backend_Amaya.dto.response.ComprobanteResponse;
import com.distribuidoraamaya.Backend_Amaya.service.ComprobanteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comprobantes")
@RequiredArgsConstructor
public class ComprobanteController {

    private final ComprobanteService comprobanteService;

    @GetMapping
    public ResponseEntity<List<ComprobanteResponse>> listar() {
        return ResponseEntity.ok(comprobanteService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComprobanteResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(comprobanteService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<ComprobanteResponse> crear(@Valid @RequestBody ComprobanteRequest request) {
        return ResponseEntity.ok(comprobanteService.crear(request));
    }

    @PatchMapping("/{id}/anular")
    public ResponseEntity<Void> anular(@PathVariable Long id) {
        comprobanteService.anular(id);
        return ResponseEntity.noContent().build();
    }
}