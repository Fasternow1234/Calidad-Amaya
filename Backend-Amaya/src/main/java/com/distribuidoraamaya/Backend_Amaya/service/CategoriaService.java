package com.distribuidoraamaya.Backend_Amaya.service;

import com.distribuidoraamaya.Backend_Amaya.dto.request.CategoriaRequest;
import com.distribuidoraamaya.Backend_Amaya.dto.response.CategoriaResponse;
import com.distribuidoraamaya.Backend_Amaya.entity.Categoria;
import com.distribuidoraamaya.Backend_Amaya.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public List<CategoriaResponse> listarActivas() {
        return categoriaRepository.findByEstadoOrderByNombreAsc(1).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<CategoriaResponse> listarTodas() {
        return categoriaRepository.findAllByOrderByNombreAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoriaResponse obtener(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
        return toResponse(categoria);
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        String nombre = request.getNombre().trim();

        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre");
        }

        Categoria categoria = Categoria.builder()
                .nombre(nombre)
                .estado(1)
                .build();

        return toResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        String nombre = request.getNombre().trim();

        categoriaRepository.findByNombreIgnoreCase(nombre).ifPresent(existente -> {
            if (!existente.getId().equals(id)) {
                throw new IllegalArgumentException("Ya existe otra categoría con ese nombre");
            }
        });

        categoria.setNombre(nombre);
        return toResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public void desactivar(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
        categoria.setEstado(0);
        categoriaRepository.save(categoria);
    }

    @Transactional
    public CategoriaResponse reactivar(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
        categoria.setEstado(1);
        return toResponse(categoriaRepository.save(categoria));
    }

    private CategoriaResponse toResponse(Categoria c) {
        return CategoriaResponse.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .estado(c.getEstado())
                .fechaRegistro(c.getFechaRegistro())
                .build();
    }
}