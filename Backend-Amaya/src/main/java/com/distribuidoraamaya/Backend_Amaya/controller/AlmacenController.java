package com.distribuidoraamaya.Backend_Amaya.controller;

import com.distribuidoraamaya.Backend_Amaya.dto.request.AlmacenRequest;
import com.distribuidoraamaya.Backend_Amaya.dto.response.AlmacenResponse;
import com.distribuidoraamaya.Backend_Amaya.service.AlmacenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/almacenes")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENERO')")
@RequiredArgsConstructor
public class AlmacenController {

    private final AlmacenService almacenService;

    @GetMapping
    public ResponseEntity<List<AlmacenResponse>> listarActivos() {
        return ResponseEntity.ok(almacenService.listarActivos());
    }

    @GetMapping("/todos")
    public ResponseEntity<List<AlmacenResponse>> listarTodos() {
        return ResponseEntity.ok(almacenService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlmacenResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(almacenService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<AlmacenResponse> crear(@Valid @RequestBody AlmacenRequest request) {
        return ResponseEntity.ok(almacenService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlmacenResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody AlmacenRequest request) {
        return ResponseEntity.ok(almacenService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        almacenService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<AlmacenResponse> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(almacenService.reactivar(id));
    }
}
