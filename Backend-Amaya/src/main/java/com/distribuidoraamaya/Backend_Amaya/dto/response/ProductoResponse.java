package com.distribuidoraamaya.Backend_Amaya.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponse {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private Long idCategoria;
    private String categoriaNombre;
    private String unidadMedida;
    private BigDecimal stockTotal;
    private Long idAlmacenDefault;
    private String nombreAlmacenDefault;
    private Integer estado;
    private LocalDateTime fechaRegistro;
}