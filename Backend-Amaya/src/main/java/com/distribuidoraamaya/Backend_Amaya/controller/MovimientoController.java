package com.distribuidoraamaya.Backend_Amaya.controller;

import com.distribuidoraamaya.Backend_Amaya.dto.response.MovimientoResponse;
import com.distribuidoraamaya.Backend_Amaya.dto.response.KardexResponse;
import com.distribuidoraamaya.Backend_Amaya.service.KardexService;
import com.distribuidoraamaya.Backend_Amaya.service.MovimientoService;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/movimientos")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENERO')")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;
    private final KardexService kardexService;

    @GetMapping
    public ResponseEntity<List<MovimientoResponse>> listar() {
        return ResponseEntity.ok(movimientoService.listar());
    }

    @GetMapping("/kardex/{idProducto}")
    public ResponseEntity<KardexResponse> consultarKardex(
            @PathVariable Long idProducto,
            @RequestParam(required = false) Long idAlmacen,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {
        return ResponseEntity.ok(kardexService.consultar(idProducto, idAlmacen, desde, hasta));
    }
}
