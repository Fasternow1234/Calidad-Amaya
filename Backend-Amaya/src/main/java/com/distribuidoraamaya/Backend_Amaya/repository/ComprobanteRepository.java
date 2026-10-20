package com.distribuidoraamaya.Backend_Amaya.repository;

import com.distribuidoraamaya.Backend_Amaya.entity.Comprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {

    boolean existsByNumero(String numero);

    @Query("SELECT DISTINCT c FROM Comprobante c " +
           "LEFT JOIN FETCH c.proveedor p " +
           "LEFT JOIN FETCH c.cliente cl " +
           "LEFT JOIN FETCH c.usuario u " +
           "ORDER BY c.fechaRegistro DESC, c.id DESC")
    List<Comprobante> findAllConCabecera();

    @Query("SELECT c FROM Comprobante c " +
           "LEFT JOIN FETCH c.proveedor p " +
           "LEFT JOIN FETCH c.cliente cl " +
           "LEFT JOIN FETCH c.usuario u " +
           "LEFT JOIN FETCH c.detalles d " +
           "LEFT JOIN FETCH d.producto pr " +
           "LEFT JOIN FETCH d.almacen al " +
           "WHERE c.id = :id")
    Optional<Comprobante> findByIdConDetalle(Long id);
}
