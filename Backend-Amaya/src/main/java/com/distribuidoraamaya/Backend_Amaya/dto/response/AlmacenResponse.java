package com.distribuidoraamaya.Backend_Amaya.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlmacenResponse {
    private Long id;
    private String nombre;
    private String direccion;
    private Integer estado;
    private LocalDateTime fechaRegistro;
    private Long totalProductos;
}