package com.distribuidoraamaya.Backend_Amaya.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClienteRequest {

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Pattern(regexp = "DNI|RUC", message = "El tipo de documento debe ser DNI o RUC")
    private String tipoDocumento;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 11, message = "Máximo 11 caracteres")
    private String numeroDocumento;

    @Size(max = 100, message = "Máximo 100 caracteres")
    private String nombres;

    @Size(max = 100, message = "Máximo 100 caracteres")
    private String apellidoPaterno;

    @Size(max = 100, message = "Máximo 100 caracteres")
    private String apellidoMaterno;

    @Size(max = 200, message = "Máximo 200 caracteres")
    private String razonSocial;

    @Size(max = 200, message = "Máximo 200 caracteres")
    private String nombreComercial;

    @Size(max = 250, message = "Máximo 250 caracteres")
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