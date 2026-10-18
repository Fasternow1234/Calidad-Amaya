package com.distribuidoraamaya.Backend_Amaya.controller;

import com.distribuidoraamaya.Backend_Amaya.dto.request.ComprobanteRequest;
import com.distribuidoraamaya.Backend_Amaya.dto.response.ComprobanteResponse;
import com.distribuidoraamaya.Backend_Amaya.service.ComprobanteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comprobantes")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'VENDEDOR')")
@RequiredArgsConstructor
public class ComprobanteController {

    private final ComprobanteService comprobanteService;

    @GetMapping
    public ResponseEntity<List<ComprobanteResponse>> listar(@AuthenticationPrincipal UserDetails usuario) {
        List<ComprobanteResponse> comprobantes = comprobanteService.listar();
        if (esVendedor(usuario)) {
            comprobantes = comprobantes.stream().filter(c -> "VENTA".equals(c.getTipo())).toList();
        }
        return ResponseEntity.ok(comprobantes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComprobanteResponse> obtener(@PathVariable Long id,
                                                       @AuthenticationPrincipal UserDetails usuario) {
        ComprobanteResponse comprobante = comprobanteService.obtener(id);
        validarAccesoOperacion(comprobante.getTipo(), usuario);
        return ResponseEntity.ok(comprobante);
    }

    @PostMapping
    public ResponseEntity<ComprobanteResponse> crear(@Valid @RequestBody ComprobanteRequest request,
                                                      @AuthenticationPrincipal UserDetails usuario) {
        validarAccesoOperacion(request.getTipo(), usuario);
        return ResponseEntity.ok(comprobanteService.crear(request));
    }

    @PatchMapping("/{id}/anular")
    public ResponseEntity<Void> anular(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails usuario) {
        validarAccesoOperacion(comprobanteService.obtener(id).getTipo(), usuario);
        comprobanteService.anular(id);
        return ResponseEntity.noContent().build();
    }

    private boolean esVendedor(UserDetails usuario) {
        return usuario.getAuthorities().stream().anyMatch(a -> "ROLE_VENDEDOR".equals(a.getAuthority()));
    }

    private void validarAccesoOperacion(String tipo, UserDetails usuario) {
        if (esVendedor(usuario) && !"VENTA".equalsIgnoreCase(tipo)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "El rol VENDEDOR solo puede consultar y registrar comprobantes de venta");
        }
    }
}
