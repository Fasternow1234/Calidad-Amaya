package com.distribuidoraamaya.Backend_Amaya.repository;

import com.distribuidoraamaya.Backend_Amaya.entity.RolOpcion;
import com.distribuidoraamaya.Backend_Amaya.entity.RolOpcionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RolOpcionRepository extends JpaRepository<RolOpcion, RolOpcionId> {

    @Query("SELECT ro FROM RolOpcion ro JOIN FETCH ro.opcion WHERE ro.idRol = :idRol ORDER BY ro.opcion.orden")
    List<RolOpcion> findByRolIdConOpcion(@Param("idRol") Long idRol);
}