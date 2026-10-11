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
public class StockResponse {
    private Long id;
    private Long idProducto;
    private String codigoProducto;
    private String nombreProducto;
    private String unidadMedida;
    private Long idAlmacen;
    private String nombreAlmacen;
    private BigDecimal cantidad;
    private LocalDateTime fechaActualizacion;
}