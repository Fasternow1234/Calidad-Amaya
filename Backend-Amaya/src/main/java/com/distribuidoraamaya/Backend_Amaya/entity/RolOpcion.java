package com.distribuidoraamaya.Backend_Amaya.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rol_opcion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(RolOpcionId.class)
public class RolOpcion {

    @Id
    @Column(name = "id_rol")
    private Long idRol;

    @Id
    @Column(name = "id_opcion")
    private Long idOpcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_opcion", insertable = false, updatable = false)
    private Opcion opcion;
}