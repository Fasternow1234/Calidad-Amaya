package com.distribuidoraamaya.Backend_Amaya.controller;

import com.distribuidoraamaya.Backend_Amaya.dto.request.UsuarioRequest;
import com.distribuidoraamaya.Backend_Amaya.dto.response.RolResponse;
import com.distribuidoraamaya.Backend_Amaya.dto.response.UsuarioResponse;
import com.distribuidoraamaya.Backend_Amaya.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RolResponse>> listarRoles() {
        return ResponseEntity.ok(usuarioService.listarRoles());
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequest request,
            @AuthenticationPrincipal UserDetails usuarioActual) {
        return ResponseEntity.ok(usuarioService.actualizar(id, request, usuarioActual.getUsername()));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<UsuarioResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam Integer estado,
            @AuthenticationPrincipal UserDetails usuarioActual) {
        if (estado != 0 && estado != 1) {
            throw new IllegalArgumentException("El estado debe ser 0 (inactivo) o 1 (activo)");
        }
        return ResponseEntity.ok(usuarioService.cambiarEstado(id, estado, usuarioActual.getUsername()));
    }
}
