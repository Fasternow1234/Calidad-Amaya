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
public class MovimientoResponse {
    private Long id;
    private Long idProducto;
    private String codigoProducto;
    private String nombreProducto;
    private String unidadMedida;
    private Long idAlmacen;
    private String nombreAlmacen;
    private String tipo;
    private BigDecimal cantidad;
    private BigDecimal stockAnterior;
    private BigDecimal stockNuevo;
    private String observacion;
    private LocalDateTime fecha;
    private String usuario;
}