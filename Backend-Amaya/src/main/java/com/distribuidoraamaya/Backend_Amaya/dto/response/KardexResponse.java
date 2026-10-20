package com.distribuidoraamaya.Backend_Amaya.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class KardexResponse {
    private Long idProducto;
    private String codigoProducto;
    private String nombreProducto;
    private String unidadMedida;
    private Long idAlmacen;
    private String nombreAlmacen;
    private LocalDate desde;
    private LocalDate hasta;
    private BigDecimal saldoInicial;
    private BigDecimal totalEntradas;
    private BigDecimal totalSalidas;
    private BigDecimal saldoFinal;
    private List<KardexMovimientoResponse> movimientos;
}
