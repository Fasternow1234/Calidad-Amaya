package com.distribuidoraamaya.Backend_Amaya.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolOpcionId implements Serializable {

    private Long idRol;
    private Long idOpcion;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RolOpcionId that = (RolOpcionId) o;
        return Objects.equals(idRol, that.idRol) && Objects.equals(idOpcion, that.idOpcion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idRol, idOpcion);
    }
}