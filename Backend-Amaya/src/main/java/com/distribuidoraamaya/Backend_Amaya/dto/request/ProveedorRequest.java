package com.distribuidoraamaya.Backend_Amaya.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProveedorRequest {

    @NotBlank(message = "El RUC es obligatorio")
    @Pattern(
            regexp = "^(10|15|17|20)\\d{9}$",
            message = "RUC inválido. Debe empezar con 10, 15, 17 o 20 y tener 11 dígitos"
    )
    private String ruc;

    @NotBlank(message = "La razón social es obligatoria")
    @Size(max = 150, message = "Máximo 150 caracteres")
    private String razonSocial;

    @Size(max = 150, message = "Máximo 150 caracteres")
    private String nombreComercial;

    @Size(max = 200, message = "Máximo 200 caracteres")
    private String direccion;

    @Pattern(
            regexp = "^$|^\\d{6,15}$",
            message = "El teléfono debe tener entre 6 y 15 dígitos numéricos"
    )
    private String telefono;

    @Email(message = "Formato de email inválido")
    @Size(max = 150, message = "Máximo 150 caracteres")
    private String email;
}