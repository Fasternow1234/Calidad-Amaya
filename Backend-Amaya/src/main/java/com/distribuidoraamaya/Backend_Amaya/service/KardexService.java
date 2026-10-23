package com.distribuidoraamaya.Backend_Amaya.service;

import com.distribuidoraamaya.Backend_Amaya.dto.response.KardexMovimientoResponse;
import com.distribuidoraamaya.Backend_Amaya.dto.response.KardexResponse;
import com.distribuidoraamaya.Backend_Amaya.entity.*;
import com.distribuidoraamaya.Backend_Amaya.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KardexService {

    private final ProductoRepository productoRepository;
    private final AlmacenRepository almacenRepository;
    private final MovimientoRepository movimientoRepository;
    private final StockRepository stockRepository;

    @Transactional(readOnly = true)
    public KardexResponse consultar(Long idProducto, Long idAlmacen, LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la fecha final");
        }

        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        Almacen almacenFiltro = idAlmacen == null ? null : almacenRepository.findById(idAlmacen)
                .orElseThrow(() -> new IllegalArgumentException("Almacén no encontrado"));

        List<Movimiento> movimientos = idAlmacen == null
                ? movimientoRepository.findByProductoIdOrderByFechaAscIdAsc(idProducto)
                : movimientoRepository.findByProductoIdAndAlmacenIdOrderByFechaAscIdAsc(idProducto, idAlmacen);

        // Cada almacén parte del stock anterior al primer movimiento que tenga registrado.
        Map<Long, BigDecimal> saldosPorAlmacen = new LinkedHashMap<>();
        Map<Long, String> nombresAlmacen = new LinkedHashMap<>();
        for (Movimiento movimiento : movimientos) {
            saldosPorAlmacen.putIfAbsent(movimiento.getAlmacen().getId(), cero(movimiento.getStockAnterior()));
            nombresAlmacen.putIfAbsent(movimiento.getAlmacen().getId(), movimiento.getAlmacen().getNombre());
        }

        // Si un almacén tiene stock pero aún no tiene movimientos, se presenta como saldo de apertura.
        for (Stock stock : stockRepository.findByProductoId(idProducto)) {
            if ((idAlmacen == null || stock.getAlmacen().getId().equals(idAlmacen))
                    && !saldosPorAlmacen.containsKey(stock.getAlmacen().getId())) {
                saldosPorAlmacen.put(stock.getAlmacen().getId(), cero(stock.getCantidad()));
                nombresAlmacen.put(stock.getAlmacen().getId(), stock.getAlmacen().getNombre());
            }
        }

        BigDecimal saldoInicial = sumar(saldosPorAlmacen);
        BigDecimal entradas = BigDecimal.ZERO;
        BigDecimal salidas = BigDecimal.ZERO;
        List<KardexMovimientoResponse> filas = new ArrayList<>();

        for (Movimiento movimiento : movimientos) {
            LocalDate fechaMovimiento = movimiento.getFecha().toLocalDate();
            Long idAlmacenMovimiento = movimiento.getAlmacen().getId();
            BigDecimal saldoAlmacen = saldosPorAlmacen.getOrDefault(idAlmacenMovimiento, BigDecimal.ZERO);

            if (desde != null && fechaMovimiento.isBefore(desde)) {
                saldosPorAlmacen.put(idAlmacenMovimiento, aplicar(saldoAlmacen, movimiento));
                saldoInicial = sumar(saldosPorAlmacen);
                continue;
            }
            if (hasta != null && fechaMovimiento.isAfter(hasta)) {
                continue;
            }

            BigDecimal nuevoSaldoAlmacen = aplicar(saldoAlmacen, movimiento);
            saldosPorAlmacen.put(idAlmacenMovimiento, nuevoSaldoAlmacen);

            BigDecimal entrada = movimiento.getTipo() == TipoMovimiento.ENTRADA
                    ? movimiento.getCantidad() : BigDecimal.ZERO;
            BigDecimal salida = movimiento.getTipo() == TipoMovimiento.SALIDA
                    ? movimiento.getCantidad() : BigDecimal.ZERO;
            entradas = entradas.add(entrada);
            salidas = salidas.add(salida);

            filas.add(KardexMovimientoResponse.builder()
                    .id(movimiento.getId())
                    .fecha(movimiento.getFecha())
                    .tipo(movimiento.getTipo().name())
                    .entrada(entrada)
                    .salida(salida)
                    .saldo(sumar(saldosPorAlmacen))
                    .nombreAlmacen(movimiento.getAlmacen().getNombre())
                    .referencia(movimiento.getObservacion())
                    .usuario(movimiento.getUsuario() != null ? movimiento.getUsuario().getNombreUsuario() : null)
                    .build());
        }

        return KardexResponse.builder()
                .idProducto(producto.getId())
                .codigoProducto(producto.getCodigo())
                .nombreProducto(producto.getNombre())
                .unidadMedida(producto.getUnidadMedida())
                .idAlmacen(almacenFiltro != null ? almacenFiltro.getId() : null)
                .nombreAlmacen(almacenFiltro != null ? almacenFiltro.getNombre() : "Todos los almacenes")
                .desde(desde)
                .hasta(hasta)
                .saldoInicial(saldoInicial)
                .totalEntradas(entradas)
                .totalSalidas(salidas)
                .saldoFinal(sumar(saldosPorAlmacen))
                .movimientos(filas)
                .build();
    }

    private BigDecimal aplicar(BigDecimal saldo, Movimiento movimiento) {
        return movimiento.getTipo() == TipoMovimiento.ENTRADA
                ? saldo.add(movimiento.getCantidad())
                : saldo.subtract(movimiento.getCantidad());
    }

    private BigDecimal sumar(Map<Long, BigDecimal> saldos) {
        return saldos.values().stream().reduce(BigDecimal.ZERO, (total, saldo) -> total.add(saldo));
    }

    private BigDecimal cero(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
