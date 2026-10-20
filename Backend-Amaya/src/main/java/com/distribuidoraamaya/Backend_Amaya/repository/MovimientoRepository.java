package com.distribuidoraamaya.Backend_Amaya.repository;

import com.distribuidoraamaya.Backend_Amaya.entity.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    @Query("SELECT m FROM Movimiento m " +
           "JOIN FETCH m.producto p " +
           "JOIN FETCH m.almacen a " +
           "LEFT JOIN FETCH m.usuario u " +
           "ORDER BY m.fecha DESC, m.id DESC")
    List<Movimiento> findAllConDetalle();

    List<Movimiento> findByProductoIdOrderByFechaAscIdAsc(Long idProducto);

    List<Movimiento> findByProductoIdAndAlmacenIdOrderByFechaAscIdAsc(Long idProducto, Long idAlmacen);
}
