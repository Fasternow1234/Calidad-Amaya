package com.distribuidoraamaya.Backend_Amaya.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ComprobanteRequest {

    @NotBlank(message = "El tipo es obligatorio (COMPRA o VENTA)")
    private String tipo;

    @NotBlank(message = "El número es obligatorio")
    @Size(max = 30, message = "Máximo 30 caracteres")
    private String numero;

    @NotNull(message = "La fecha de emisión es obligatoria")
    private LocalDate fechaEmision;

    private Long idProveedor;

    @Size(max = 300, message = "Máximo 300 caracteres")
    private String observacion;

    @NotEmpty(message = "Debe agregar al menos un producto")
    @Valid
    private List<DetalleComprobanteRequest> detalles;
}