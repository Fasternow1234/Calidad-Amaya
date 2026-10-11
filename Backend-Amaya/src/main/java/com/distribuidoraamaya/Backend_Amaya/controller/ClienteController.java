package com.distribuidoraamaya.Backend_Amaya.controller;

import com.distribuidoraamaya.Backend_Amaya.dto.request.ClienteRequest;
import com.distribuidoraamaya.Backend_Amaya.dto.response.ClienteResponse;
import com.distribuidoraamaya.Backend_Amaya.dto.response.DniResponse;
import com.distribuidoraamaya.Backend_Amaya.dto.response.RucResponse;
import com.distribuidoraamaya.Backend_Amaya.service.ClienteService;
import com.distribuidoraamaya.Backend_Amaya.service.DniService;
import com.distribuidoraamaya.Backend_Amaya.service.RucService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final DniService dniService;
    private final RucService rucService;

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listarActivos() {
        return ResponseEntity.ok(clienteService.listarActivos());
    }

    @GetMapping("/todos")
    public ResponseEntity<List<ClienteResponse>> listarTodos() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ClienteResponse>> buscar(@RequestParam String q) {
        return ResponseEntity.ok(clienteService.buscar(q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.obtener(id));
    }

    @GetMapping("/dni/{dni}")
    public ResponseEntity<DniResponse> consultarDni(@PathVariable String dni) {
        return ResponseEntity.ok(dniService.consultar(dni));
    }

    @GetMapping("/ruc/{ruc}")
    public ResponseEntity<RucResponse> consultarRuc(@PathVariable String ruc) {
        return ResponseEntity.ok(rucService.consultar(ruc));
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(clienteService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequest request
    ) {
        return ResponseEntity.ok(clienteService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        clienteService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<ClienteResponse> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.reactivar(id));
    }
}