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
public class ClienteResponse {
    private Long id;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String razonSocial;
    private String nombreComercial;
    private String direccion;
    private String telefono;
    private String email;
    private Integer estado;
    private LocalDateTime fechaRegistro;

    /**
     * Nombre para mostrar en el frontend.
     * - EMPRESA → razonSocial
     * - PERSONA → "nombres apellidoPaterno apellidoMaterno"
     */
    private String nombreMostrar;
}