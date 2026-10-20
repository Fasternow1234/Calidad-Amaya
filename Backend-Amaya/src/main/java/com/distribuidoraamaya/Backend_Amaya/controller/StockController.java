package com.distribuidoraamaya.Backend_Amaya.controller;

import com.distribuidoraamaya.Backend_Amaya.dto.response.StockResponse;
import com.distribuidoraamaya.Backend_Amaya.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENERO', 'VENDEDOR')")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping("/almacen/{idAlmacen}")
    public ResponseEntity<List<StockResponse>> porAlmacen(@PathVariable Long idAlmacen) {
        return ResponseEntity.ok(stockService.listarPorAlmacen(idAlmacen));
    }

    @GetMapping("/producto/{idProducto}")
    public ResponseEntity<List<StockResponse>> porProducto(@PathVariable Long idProducto) {
        return ResponseEntity.ok(stockService.listarPorProducto(idProducto));
    }
}
