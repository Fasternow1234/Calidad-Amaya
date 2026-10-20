package com.distribuidoraamaya.Backend_Amaya.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(max = 50, message = "El nombre de usuario no puede superar 50 caracteres")
    private String nombreUsuario;

    // Obligatoria al crear; vacía al actualizar conserva la contraseña actual.
    @Size(max = 72, message = "La contraseña no puede superar 72 caracteres")
    private String password;

    @NotBlank(message = "El nombre del empleado es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombreEmpleado;

    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 150, message = "El correo no puede superar 150 caracteres")
    private String email;

    @NotNull(message = "Debes seleccionar un rol")
    private Long idRol;
}
