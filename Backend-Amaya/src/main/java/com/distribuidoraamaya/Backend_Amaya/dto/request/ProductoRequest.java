package com.distribuidoraamaya.Backend_Amaya.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "Máximo 150 caracteres")
    private String nombre;

    @Size(max = 300, message = "Máximo 300 caracteres")
    private String descripcion;

    @NotNull(message = "El precio de compra es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio de compra debe ser mayor a 0")
    private BigDecimal precioCompra;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio de venta debe ser mayor a 0")
    private BigDecimal precioVenta;

    @NotNull(message = "La categoría es obligatoria")
    private Long idCategoria;

    @NotBlank(message = "La unidad de medida es obligatoria")
    @Pattern(
            regexp = "UNIDAD|KILO|LITRO|GRAMO",
            message = "La unidad base debe ser UNIDAD, KILO, LITRO o GRAMO"
    )
    private String unidadMedida;

    @NotNull(message = "El almacén es obligatorio")
    private Long idAlmacen;
}