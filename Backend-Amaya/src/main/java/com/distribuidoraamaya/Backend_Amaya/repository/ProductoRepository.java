package com.distribuidoraamaya.Backend_Amaya.repository;

import com.distribuidoraamaya.Backend_Amaya.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByEstadoOrderByNombreAsc(Integer estado);
    List<Producto> findAllByOrderByNombreAsc();
    Optional<Producto> findByCodigo(String codigo);
    Optional<Producto> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByCodigo(String codigo);

    @Query("SELECT p FROM Producto p WHERE p.estado = 1 AND (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', :q, '%'))) ORDER BY p.nombre")
    List<Producto> buscar(@Param("q") String q);

    @Query("SELECT MAX(CAST(SUBSTRING(p.codigo, 5) AS int)) FROM Producto p WHERE p.codigo LIKE 'PRD-%'")
    Integer obtenerUltimoNumeroCodigo();
}