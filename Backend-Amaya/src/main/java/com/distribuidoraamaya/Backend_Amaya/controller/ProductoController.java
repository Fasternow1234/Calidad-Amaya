package com.distribuidoraamaya.Backend_Amaya.controller;

import com.distribuidoraamaya.Backend_Amaya.dto.request.ProductoRequest;
import com.distribuidoraamaya.Backend_Amaya.dto.response.ProductoResponse;
import com.distribuidoraamaya.Backend_Amaya.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENERO', 'VENDEDOR')")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<List<ProductoResponse>> listarActivos(@AuthenticationPrincipal UserDetails usuario) {
        return ResponseEntity.ok(aplicarPrivacidadCosto(productoService.listarActivos(), usuario));
    }

    @GetMapping("/todos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENERO')")
    public ResponseEntity<List<ProductoResponse>> listarTodos(@AuthenticationPrincipal UserDetails usuario) {
        return ResponseEntity.ok(aplicarPrivacidadCosto(productoService.listarTodos(), usuario));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoResponse>> buscar(@RequestParam String q,
                                                          @AuthenticationPrincipal UserDetails usuario) {
        return ResponseEntity.ok(aplicarPrivacidadCosto(productoService.buscar(q), usuario));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtener(@PathVariable Long id,
                                                     @AuthenticationPrincipal UserDetails usuario) {
        return ResponseEntity.ok(aplicarPrivacidadCosto(productoService.obtener(id), usuario));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENERO')")
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(productoService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENERO')")
    public ResponseEntity<ProductoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(productoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENERO')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        productoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ALMACENERO')")
    public ResponseEntity<ProductoResponse> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.reactivar(id));
    }

    private List<ProductoResponse> aplicarPrivacidadCosto(List<ProductoResponse> productos, UserDetails usuario) {
        return productos.stream().map(producto -> aplicarPrivacidadCosto(producto, usuario)).toList();
    }

    private ProductoResponse aplicarPrivacidadCosto(ProductoResponse producto, UserDetails usuario) {
        boolean esVendedor = usuario.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_VENDEDOR".equals(authority.getAuthority()));
        if (!esVendedor) return producto;

        return ProductoResponse.builder()
                .id(producto.getId())
                .codigo(producto.getCodigo())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .precioVenta(producto.getPrecioVenta())
                .idCategoria(producto.getIdCategoria())
                .categoriaNombre(producto.getCategoriaNombre())
                .unidadMedida(producto.getUnidadMedida())
                .stockTotal(producto.getStockTotal())
                .idAlmacenDefault(producto.getIdAlmacenDefault())
                .nombreAlmacenDefault(producto.getNombreAlmacenDefault())
                .estado(producto.getEstado())
                .fechaRegistro(producto.getFechaRegistro())
                .build();
    }
}
