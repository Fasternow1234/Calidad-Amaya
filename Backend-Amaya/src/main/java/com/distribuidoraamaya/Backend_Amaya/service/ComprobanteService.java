package com.distribuidoraamaya.Backend_Amaya.service;

import com.distribuidoraamaya.Backend_Amaya.dto.request.ComprobanteRequest;
import com.distribuidoraamaya.Backend_Amaya.dto.request.DetalleComprobanteRequest;
import com.distribuidoraamaya.Backend_Amaya.dto.response.ComprobanteResponse;
import com.distribuidoraamaya.Backend_Amaya.dto.response.DetalleComprobanteResponse;
import com.distribuidoraamaya.Backend_Amaya.entity.*;
import com.distribuidoraamaya.Backend_Amaya.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComprobanteService {

    private final ComprobanteRepository comprobanteRepository;
    private final ProductoRepository productoRepository;
    private final AlmacenRepository almacenRepository;
    private final ProveedorRepository proveedorRepository;
    private final UsuarioRepository usuarioRepository;
    private final StockRepository stockRepository;
    private final MovimientoService movimientoService;

    public List<ComprobanteResponse> listar() {
        return comprobanteRepository.findAllConCabecera().stream()
                .map(this::toResponseSinDetalle)
                .toList();
    }

    public ComprobanteResponse obtener(Long id) {
        Comprobante c = comprobanteRepository.findByIdConDetalle(id)
                .orElseThrow(() -> new IllegalArgumentException("Comprobante no encontrado"));
        return toResponseConDetalle(c);
    }

    @Transactional
    public ComprobanteResponse crear(ComprobanteRequest request) {
        if (comprobanteRepository.existsByNumero(request.getNumero().trim())) {
            throw new IllegalArgumentException("Ya existe un comprobante con el número " + request.getNumero());
        }

        TipoComprobante tipo;
        try {
            tipo = TipoComprobante.valueOf(request.getTipo().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Tipo de comprobante inválido: " + request.getTipo());
        }

        Proveedor proveedor = null;
        if (tipo == TipoComprobante.COMPRA) {
            if (request.getIdProveedor() == null) {
                throw new IllegalArgumentException("Debe seleccionar un proveedor para la compra");
            }
            proveedor = proveedorRepository.findById(request.getIdProveedor())
                    .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
            if (proveedor.getEstado() == null || proveedor.getEstado() != 1) {
                throw new IllegalArgumentException("El proveedor seleccionado está inactivo");
            }
        }

        // ✅ Se construye el comprobante SIN guardarlo aún
        Comprobante comprobante = Comprobante.builder()
                .tipo(tipo)
                .numero(request.getNumero().trim())
                .fechaEmision(request.getFechaEmision())
                .proveedor(proveedor)
                .observacion(limpiar(request.getObservacion()))
                .total(BigDecimal.ZERO)
                .estado(1)
                .usuario(usuarioActual())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        // ✅ Se itera sobre TODOS los detalles agregándolos a la MISMA lista
        //    (no se reemplaza la referencia, se usa .add() sobre la lista original)
        for (DetalleComprobanteRequest d : request.getDetalles()) {
            Producto producto = productoRepository.findById(d.getIdProducto())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado (id=" + d.getIdProducto() + ")"));
            if (producto.getEstado() == null || producto.getEstado() != 1) {
                throw new IllegalArgumentException("El producto " + producto.getNombre() + " está inactivo");
            }

            Almacen almacen = tipo == TipoComprobante.COMPRA
                    ? resolverAlmacenCompra(producto)
                    : resolverAlmacenVenta(producto, d.getCantidad());

            DetalleComprobante detalle = DetalleComprobante.builder()
                    .comprobante(comprobante)
                    .producto(producto)
                    .almacen(almacen)
                    .cantidad(d.getCantidad())
                    .precioUnitario(d.getPrecioUnitario())
                    .build();

            comprobante.getDetalles().add(detalle);   // ✅ .add() en la lista original

            movimientoService.registrar(
                    producto.getId(),
                    almacen.getId(),
                    tipo == TipoComprobante.COMPRA ? TipoMovimiento.ENTRADA : TipoMovimiento.SALIDA,
                    d.getCantidad(),
                    tipo + " " + comprobante.getNumero()
            );

            BigDecimal subtotal = d.getCantidad().multiply(d.getPrecioUnitario());
            total = total.add(subtotal);
        }

        comprobante.setTotal(total);

        // ✅ Un solo save: Hibernate hace cascade ALL sobre los detalles
        comprobante = comprobanteRepository.save(comprobante);

        return toResponseConDetalle(comprobante);
    }

    @Transactional
    public void anular(Long id) {
        Comprobante c = comprobanteRepository.findByIdConDetalle(id)
                .orElseThrow(() -> new IllegalArgumentException("Comprobante no encontrado"));
        if (c.getEstado() != null && c.getEstado() == 0) {
            throw new IllegalArgumentException("El comprobante ya está anulado");
        }
        for (DetalleComprobante d : c.getDetalles()) {
            movimientoService.registrar(
                    d.getProducto().getId(),
                    d.getAlmacen().getId(),
                    c.getTipo() == TipoComprobante.COMPRA ? TipoMovimiento.SALIDA : TipoMovimiento.ENTRADA,
                    d.getCantidad(),
                    "Anulación " + c.getNumero()
            );
        }
        c.setEstado(0);
        comprobanteRepository.save(c);
    }

    private Almacen resolverAlmacenCompra(Producto producto) {
        Almacen principal = producto.getAlmacenPrincipal();
        if (principal == null) {
            principal = stockRepository.findByProductoId(producto.getId()).stream()
                    .min(Comparator.comparing(Stock::getId))
                    .map(Stock::getAlmacen)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "El producto " + producto.getNombre() + " no tiene almacén principal asignado"));
        }
        return principal;
    }

    private Almacen resolverAlmacenVenta(Producto producto, BigDecimal cantidad) {
        List<Stock> stocks = stockRepository.findByProductoId(producto.getId());
        return stocks.stream()
                .filter(s -> s.getCantidad() != null && s.getCantidad().compareTo(cantidad) >= 0)
                .max(Comparator.comparing(Stock::getCantidad))
                .map(Stock::getAlmacen)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No hay stock suficiente de " + producto.getNombre() +
                        " en ningún almacén. Verifique el inventario."));
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

    private ComprobanteResponse toResponseSinDetalle(Comprobante c) {
        return ComprobanteResponse.builder()
                .id(c.getId())
                .tipo(c.getTipo() != null ? c.getTipo().name() : null)
                .numero(c.getNumero())
                .fechaEmision(c.getFechaEmision())
                .idProveedor(c.getProveedor() != null ? c.getProveedor().getId() : null)
                .razonSocialProveedor(c.getProveedor() != null ? c.getProveedor().getRazonSocial() : null)
                .rucProveedor(c.getProveedor() != null ? c.getProveedor().getRuc() : null)
                .observacion(c.getObservacion())
                .total(c.getTotal())
                .estado(c.getEstado())
                .usuario(c.getUsuario() != null ? c.getUsuario().getNombreUsuario() : null)
                .fechaRegistro(c.getFechaRegistro())
                .detalles(null)
                .build();
    }

    private ComprobanteResponse toResponseConDetalle(Comprobante c) {
        List<DetalleComprobanteResponse> detalles = c.getDetalles().stream()
                .map(d -> DetalleComprobanteResponse.builder()
                        .id(d.getId())
                        .idProducto(d.getProducto().getId())
                        .codigoProducto(d.getProducto().getCodigo())
                        .nombreProducto(d.getProducto().getNombre())
                        .unidadMedida(d.getProducto().getUnidadMedida())
                        .idAlmacen(d.getAlmacen().getId())
                        .nombreAlmacen(d.getAlmacen().getNombre())
                        .cantidad(d.getCantidad())
                        .precioUnitario(d.getPrecioUnitario())
                        .subtotal(d.getCantidad().multiply(d.getPrecioUnitario()))
                        .build())
                .toList();
        ComprobanteResponse r = toResponseSinDetalle(c);
        r.setDetalles(detalles);
        return r;
    }
}