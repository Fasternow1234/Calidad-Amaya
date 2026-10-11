package com.distribuidoraamaya.Backend_Amaya.service;

import com.distribuidoraamaya.Backend_Amaya.dto.request.AlmacenRequest;
import com.distribuidoraamaya.Backend_Amaya.dto.response.AlmacenResponse;
import com.distribuidoraamaya.Backend_Amaya.entity.Almacen;
import com.distribuidoraamaya.Backend_Amaya.entity.Stock;
import com.distribuidoraamaya.Backend_Amaya.repository.AlmacenRepository;
import com.distribuidoraamaya.Backend_Amaya.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlmacenService {

    private final AlmacenRepository almacenRepository;
    private final StockRepository stockRepository;

    public List<AlmacenResponse> listarActivos() {
        return almacenRepository.findByEstadoOrderByNombreAsc(1).stream()
                .map(this::toResponse).toList();
    }

    public List<AlmacenResponse> listarTodos() {
        return almacenRepository.findAllByOrderByNombreAsc().stream()
                .map(this::toResponse).toList();
    }

    public AlmacenResponse obtener(Long id) {
        Almacen a = almacenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Almacén no encontrado"));
        return toResponse(a);
    }

    @Transactional
    public AlmacenResponse crear(AlmacenRequest request) {
        String nombre = request.getNombre().trim();
        if (almacenRepository.existsByNombreIgnoreCase(nombre)) {
            throw new IllegalArgumentException("Ya existe un almacén con ese nombre");
        }
        Almacen almacen = Almacen.builder()
                .nombre(nombre)
                .direccion(limpiar(request.getDireccion()))
                .estado(1)
                .build();
        return toResponse(almacenRepository.save(almacen));
    }

    @Transactional
    public AlmacenResponse actualizar(Long id, AlmacenRequest request) {
        Almacen almacen = almacenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Almacén no encontrado"));
        String nombre = request.getNombre().trim();
        almacenRepository.findByNombreIgnoreCase(nombre).ifPresent(existente -> {
            if (!existente.getId().equals(id)) {
                throw new IllegalArgumentException("Ya existe otro almacén con ese nombre");
            }
        });
        almacen.setNombre(nombre);
        almacen.setDireccion(limpiar(request.getDireccion()));
        return toResponse(almacenRepository.save(almacen));
    }

    @Transactional
    public void desactivar(Long id) {
        Almacen almacen = almacenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Almacén no encontrado"));
        List<Stock> stocks = stockRepository.findByAlmacenId(id);
        List<Stock> conStock = stocks.stream()
                .filter(s -> s.getCantidad() != null
                        && s.getCantidad().compareTo(BigDecimal.ZERO) > 0)
                .toList();
        if (!conStock.isEmpty()) {
            int cantidadProductos = conStock.size();
            String mensaje = cantidadProductos == 1
                    ? "No se puede eliminar el almacén porque tiene 1 producto con stock. Primero transfiere o retira la mercadería."
                    : "No se puede eliminar el almacén porque tiene " + cantidadProductos + " productos con stock. Primero transfiere o retira la mercadería.";
            throw new IllegalArgumentException(mensaje);
        }
        almacen.setEstado(0);
        almacenRepository.save(almacen);
    }

    @Transactional
    public AlmacenResponse reactivar(Long id) {
        Almacen almacen = almacenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Almacén no encontrado"));
        almacen.setEstado(1);
        return toResponse(almacenRepository.save(almacen));
    }

    private String limpiar(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private AlmacenResponse toResponse(Almacen a) {
        return AlmacenResponse.builder()
                .id(a.getId())
                .nombre(a.getNombre())
                .direccion(a.getDireccion())
                .estado(a.getEstado())
                .fechaRegistro(a.getFechaRegistro())
                .totalProductos(stockRepository.contarProductosConStock(a.getId()))
                .build();
    }
}