package com.distribuidoraamaya.Backend_Amaya.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteResponse {
    private Long id;
    private String tipo;
    private String numero;
    private LocalDate fechaEmision;
    private Long idProveedor;
    private String razonSocialProveedor;
    private String rucProveedor;
    private Long idCliente;
    private String nombreCliente;
    private String documentoCliente;
    private String observacion;
    private BigDecimal total;
    private Integer estado;
    private String usuario;
    private LocalDateTime fechaRegistro;
    private List<DetalleComprobanteResponse> detalles;
}
