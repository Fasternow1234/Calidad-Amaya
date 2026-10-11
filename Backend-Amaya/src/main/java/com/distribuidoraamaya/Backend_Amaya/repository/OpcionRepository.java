package com.distribuidoraamaya.Backend_Amaya.repository;

import com.distribuidoraamaya.Backend_Amaya.entity.Opcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OpcionRepository extends JpaRepository<Opcion, Long> {
    Optional<Opcion> findByCodigo(String codigo);
}