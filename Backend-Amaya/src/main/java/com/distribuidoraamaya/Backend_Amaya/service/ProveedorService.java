package com.distribuidoraamaya.Backend_Amaya.service;

import com.distribuidoraamaya.Backend_Amaya.dto.request.ProveedorRequest;
import com.distribuidoraamaya.Backend_Amaya.dto.response.ProveedorResponse;
import com.distribuidoraamaya.Backend_Amaya.entity.Proveedor;
import com.distribuidoraamaya.Backend_Amaya.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public List<ProveedorResponse> listarActivos() {
        return proveedorRepository.findByEstadoOrderByRazonSocialAsc(1).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ProveedorResponse> listarTodos() {
        return proveedorRepository.findAllByOrderByRazonSocialAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public ProveedorResponse obtener(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        return toResponse(proveedor);
    }

    @Transactional
    public ProveedorResponse crear(ProveedorRequest request) {
        String ruc = request.getRuc().trim();
        String razonSocial = request.getRazonSocial().trim();

        if (proveedorRepository.existsByRuc(ruc)) {
            throw new IllegalArgumentException("Ya existe un proveedor con el RUC " + ruc);
        }

        Proveedor proveedor = Proveedor.builder()
                .ruc(ruc)
                .razonSocial(razonSocial)
                .nombreComercial(limpiar(request.getNombreComercial()))
                .direccion(limpiar(request.getDireccion()))
                .telefono(limpiar(request.getTelefono()))
                .email(limpiar(request.getEmail()))
                .estado(1)
                .build();

        return toResponse(proveedorRepository.save(proveedor));
    }

    @Transactional
    public ProveedorResponse actualizar(Long id, ProveedorRequest request) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));

        String ruc = request.getRuc().trim();
        String razonSocial = request.getRazonSocial().trim();

        proveedorRepository.findByRuc(ruc).ifPresent(existente -> {
            if (!existente.getId().equals(id)) {
                throw new IllegalArgumentException("Ya existe otro proveedor con el RUC " + ruc);
            }
        });

        proveedor.setRuc(ruc);
        proveedor.setRazonSocial(razonSocial);
        proveedor.setNombreComercial(limpiar(request.getNombreComercial()));
        proveedor.setDireccion(limpiar(request.getDireccion()));
        proveedor.setTelefono(limpiar(request.getTelefono()));
        proveedor.setEmail(limpiar(request.getEmail()));

        return toResponse(proveedorRepository.save(proveedor));
    }

    @Transactional
    public void desactivar(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        proveedor.setEstado(0);
        proveedorRepository.save(proveedor);
    }

    @Transactional
    public ProveedorResponse reactivar(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        proveedor.setEstado(1);
        return toResponse(proveedorRepository.save(proveedor));
    }

    private String limpiar(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private ProveedorResponse toResponse(Proveedor p) {
        return ProveedorResponse.builder()
                .id(p.getId())
                .ruc(p.getRuc())
                .razonSocial(p.getRazonSocial())
                .nombreComercial(p.getNombreComercial())
                .direccion(p.getDireccion())
                .telefono(p.getTelefono())
                .email(p.getEmail())
                .estado(p.getEstado())
                .fechaRegistro(p.getFechaRegistro())
                .build();
    }
}