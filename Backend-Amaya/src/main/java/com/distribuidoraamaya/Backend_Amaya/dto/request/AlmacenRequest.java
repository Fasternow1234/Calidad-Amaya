package com.distribuidoraamaya.Backend_Amaya.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AlmacenRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String nombre;

    @Size(max = 200, message = "Máximo 200 caracteres")
    private String direccion;
}