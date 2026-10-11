package com.distribuidoraamaya.Backend_Amaya.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RucResponse {
    private String ruc;
    private String razonSocial;
    private String nombreComercial;
    private String direccion;
    private String estado;
    private String condicion;
}