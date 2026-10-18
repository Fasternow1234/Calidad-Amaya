package com.distribuidoraamaya.Backend_Amaya.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RolResponse {
    private Long id;
    private String nombre;
    private String descripcion;
}
