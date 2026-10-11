package com.distribuidoraamaya.Backend_Amaya.service;

import com.distribuidoraamaya.Backend_Amaya.dto.response.MovimientoResponse;
import com.distribuidoraamaya.Backend_Amaya.entity.*;
import com.distribuidoraamaya.Backend_Amaya.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final StockRepository stockRepository;
    private final ProductoRepository productoRepository;
    private final AlmacenRepository almacenRepository;
    private final UsuarioRepository usuarioRepository;

    public List<MovimientoResponse> listar() {
        return movimientoRepository.findAllConDetalle().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public Movimiento registrar(Long idProducto,
                                Long idAlmacen,
                                TipoMovimiento tipo,
                                BigDecimal cantidad,
                                String observacion) {

        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }

        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        Almacen almacen = almacenRepository.findById(idAlmacen)
                .orElseThrow(() -> new IllegalArgumentException("Almacén no encontrado"));

        Stock stock = stockRepository
                .findByProductoIdAndAlmacenId(idProducto, idAlmacen)
                .orElseGet(() -> Stock.builder()
                        .producto(producto)
                        .almacen(almacen)
                        .cantidad(BigDecimal.ZERO)
                        .build());

        BigDecimal anterior = stock.getCantidad() != null
                ? stock.getCantidad()
                : BigDecimal.ZERO;

        BigDecimal nuevo = tipo == TipoMovimiento.ENTRADA
                ? anterior.add(cantidad)
                : anterior.subtract(cantidad);

        if (nuevo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Stock insuficiente en " + almacen.getNombre() +
                    ". Disponible: " + anterior + " " + producto.getUnidadMedida()
            );
        }

        stock.setCantidad(nuevo);
        stockRepository.save(stock);

        Movimiento movimiento = Movimiento.builder()
                .producto(producto)
                .almacen(almacen)
                .tipo(tipo)
                .cantidad(cantidad)
                .stockAnterior(anterior)
                .stockNuevo(nuevo)
                .observacion(limpiar(observacion))
                .usuario(usuarioActual())
                .build();

        return movimientoRepository.save(movimiento);
    }

    private String limpiar(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return usuarioRepository.findByNombreUsuario(auth.getName()).orElse(null);
    }

    private MovimientoResponse toResponse(Movimiento m) {
        return MovimientoResponse.builder()
                .id(m.getId())
                .idProducto(m.getProducto().getId())
                .codigoProducto(m.getProducto().getCodigo())
                .nombreProducto(m.getProducto().getNombre())
                .unidadMedida(m.getProducto().getUnidadMedida())
                .idAlmacen(m.getAlmacen().getId())
                .nombreAlmacen(m.getAlmacen().getNombre())
                .tipo(m.getTipo() != null ? m.getTipo().name() : null)
                .cantidad(m.getCantidad())
                .stockAnterior(m.getStockAnterior())
                .stockNuevo(m.getStockNuevo())
                .observacion(m.getObservacion())
                .fecha(m.getFecha())
                .usuario(m.getUsuario() != null ? m.getUsuario().getNombreUsuario() : null)
                .build();
    }
}