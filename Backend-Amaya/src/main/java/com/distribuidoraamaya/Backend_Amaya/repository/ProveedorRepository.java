package com.distribuidoraamaya.Backend_Amaya.repository;

import com.distribuidoraamaya.Backend_Amaya.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    List<Proveedor> findByEstadoOrderByRazonSocialAsc(Integer estado);

    List<Proveedor> findAllByOrderByRazonSocialAsc();

    Optional<Proveedor> findByRuc(String ruc);

    boolean existsByRuc(String ruc);
}