package com.distribuidoraamaya.Backend_Amaya.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class KardexMovimientoResponse {
    private Long id;
    private LocalDateTime fecha;
    private String tipo;
    private BigDecimal entrada;
    private BigDecimal salida;
    private BigDecimal saldo;
    private String nombreAlmacen;
    private String referencia;
    private String usuario;
}
