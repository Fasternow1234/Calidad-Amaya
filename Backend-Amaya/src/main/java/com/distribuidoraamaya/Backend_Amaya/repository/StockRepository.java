package com.distribuidoraamaya.Backend_Amaya.repository;

import com.distribuidoraamaya.Backend_Amaya.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByProductoIdAndAlmacenId(Long idProducto, Long idAlmacen);
    List<Stock> findByProductoId(Long idProducto);
    List<Stock> findByAlmacenId(Long idAlmacen);

    @Query("SELECT COUNT(DISTINCT s.producto.id) FROM Stock s WHERE s.almacen.id = :idAlmacen AND s.cantidad > 0")
    long contarProductosConStock(@Param("idAlmacen") Long idAlmacen);
}