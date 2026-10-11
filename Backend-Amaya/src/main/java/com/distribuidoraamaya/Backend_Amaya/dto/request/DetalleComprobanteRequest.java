package com.distribuidoraamaya.Backend_Amaya.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DetalleComprobanteRequest {

    @NotNull(message = "El producto es obligatorio")
    private Long idProducto;

    // Ya no se usa. El almacén lo determina el producto y el tipo de comprobante.
    private Long idAlmacen;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
    private BigDecimal cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio unitario no puede ser negativo")
    private BigDecimal precioUnitario;
}