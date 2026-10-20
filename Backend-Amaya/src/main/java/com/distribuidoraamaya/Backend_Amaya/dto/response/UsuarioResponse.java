package com.distribuidoraamaya.Backend_Amaya.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UsuarioResponse {
    private Long id;
    private String nombreUsuario;
    private String nombreEmpleado;
    private String email;
    private Integer estado;
    private LocalDateTime fechaRegistro;
    private Long idRol;
    private String rol;
}
