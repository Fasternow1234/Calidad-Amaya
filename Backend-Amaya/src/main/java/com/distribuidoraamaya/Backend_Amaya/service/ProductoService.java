package com.distribuidoraamaya.Backend_Amaya.service;

import com.distribuidoraamaya.Backend_Amaya.dto.request.ProductoRequest;
import com.distribuidoraamaya.Backend_Amaya.dto.response.ProductoResponse;
import com.distribuidoraamaya.Backend_Amaya.entity.Almacen;
import com.distribuidoraamaya.Backend_Amaya.entity.Categoria;
import com.distribuidoraamaya.Backend_Amaya.entity.Producto;
import com.distribuidoraamaya.Backend_Amaya.entity.Stock;
import com.distribuidoraamaya.Backend_Amaya.repository.AlmacenRepository;
import com.distribuidoraamaya.Backend_Amaya.repository.CategoriaRepository;
import com.distribuidoraamaya.Backend_Amaya.repository.ProductoRepository;
import com.distribuidoraamaya.Backend_Amaya.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductoService {

    public static final int STOCK_MAXIMO_SISTEMA = 999;

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final AlmacenRepository almacenRepository;
    private final StockRepository stockRepository;

    public List<ProductoResponse> listarActivos() {
        return productoRepository.findByEstadoOrderByNombreAsc(1).stream()
                .map(this::toResponse).toList();
    }

    public List<ProductoResponse> listarTodos() {
        return productoRepository.findAllByOrderByNombreAsc().stream()
                .map(this::toResponse).toList();
    }

    public List<ProductoResponse> buscar(String q) {
        if (q == null || q.trim().isEmpty()) return listarActivos();
        return productoRepository.buscar(q.trim()).stream()
                .map(this::toResponse).toList();
    }

    public ProductoResponse obtener(Long id) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        return toResponse(p);
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        String nombre = request.getNombre().trim();
        if (productoRepository.existsByNombreIgnoreCase(nombre)) {
            throw new IllegalArgumentException("Ya existe un producto con ese nombre");
        }

        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
        if (categoria.getEstado() != 1) {
            throw new IllegalArgumentException("La categoría seleccionada está inactiva");
        }

        Almacen almacen = almacenRepository.findById(request.getIdAlmacen())
                .orElseThrow(() -> new IllegalArgumentException("Almacén no encontrado"));
        if (almacen.getEstado() != 1) {
            throw new IllegalArgumentException("El almacén seleccionado está inactivo");
        }

        Producto producto = Producto.builder()
                .codigo(generarCodigo())
                .nombre(nombre)
                .descripcion(limpiar(request.getDescripcion()))
                .precioCompra(request.getPrecioCompra())
                .precioVenta(request.getPrecioVenta())
                .categoria(categoria)
                .almacenPrincipal(almacen)
                .unidadMedida(request.getUnidadMedida().toUpperCase())
                .estado(1)
                .build();

        Producto saved = productoRepository.save(producto);

        stockRepository.save(
                Stock.builder()
                        .producto(saved)
                        .almacen(almacen)
                        .cantidad(BigDecimal.ZERO)
                        .build()
        );

        return toResponse(saved);
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        String nombre = request.getNombre().trim();
        productoRepository.findByNombreIgnoreCase(nombre).ifPresent(existente -> {
            if (!existente.getId().equals(id)) {
                throw new IllegalArgumentException("Ya existe otro producto con ese nombre");
            }
        });

        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
        if (categoria.getEstado() == null || categoria.getEstado() != 1) {
            throw new IllegalArgumentException("La categoría seleccionada está inactiva");
        }

        if (request.getIdAlmacen() != null && request.getIdAlmacen() > 0) {
            Long almacenActualId = producto.getAlmacenPrincipal() != null
                    ? producto.getAlmacenPrincipal().getId()
                    : null;

            if (almacenActualId == null || !almacenActualId.equals(request.getIdAlmacen())) {
                Almacen nuevoAlmacen = almacenRepository.findById(request.getIdAlmacen())
                        .orElseThrow(() -> new IllegalArgumentException("Almacén no encontrado"));
                if (nuevoAlmacen.getEstado() == null || nuevoAlmacen.getEstado() != 1) {
                    throw new IllegalArgumentException("El almacén seleccionado no está activo");
                }

                if (almacenActualId != null) {
                    Stock stockActual = stockRepository
                            .findByProductoIdAndAlmacenId(producto.getId(), almacenActualId)
                            .orElse(null);

                    if (stockActual != null
                            && stockActual.getCantidad() != null
                            && stockActual.getCantidad().compareTo(BigDecimal.ZERO) > 0) {
                        Almacen almacenViejo = almacenRepository.findById(almacenActualId).orElse(null);
                        String nombreViejo = almacenViejo != null ? almacenViejo.getNombre() : "actual";
                        String cantidadFormateada = formatearCantidadConUnidad(
                                stockActual.getCantidad(),
                                producto.getUnidadMedida()
                        );
                        throw new IllegalArgumentException(
                                "El producto tiene " + cantidadFormateada + " en " + nombreViejo
                                        + ". Retira o vende el stock antes de cambiar de almacén.");
                    }
                    if (stockActual != null) {
                        stockRepository.delete(stockActual);
                    }
                }

                if (stockRepository.findByProductoIdAndAlmacenId(
                        producto.getId(), nuevoAlmacen.getId()).isEmpty()) {
                    stockRepository.save(
                            Stock.builder()
                                    .producto(producto)
                                    .almacen(nuevoAlmacen)
                                    .cantidad(BigDecimal.ZERO)
                                    .build()
                    );
                }

                producto.setAlmacenPrincipal(nuevoAlmacen);
            }
        }

        producto.setNombre(nombre);
        producto.setDescripcion(limpiar(request.getDescripcion()));
        producto.setPrecioCompra(request.getPrecioCompra());
        producto.setPrecioVenta(request.getPrecioVenta());
        producto.setCategoria(categoria);
        producto.setUnidadMedida(request.getUnidadMedida().toUpperCase());

        return toResponse(productoRepository.save(producto));
    }

    @Transactional
    public void desactivar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        BigDecimal stockTotal = calcularStockTotal(producto.getId());
        if (stockTotal != null && stockTotal.compareTo(BigDecimal.ZERO) > 0) {
            String cantidadFormateada = formatearCantidadConUnidad(
                    stockTotal,
                    producto.getUnidadMedida()
            );
            throw new IllegalArgumentException(
                    "No se puede eliminar el producto \"" + producto.getNombre()
                            + "\" porque tiene " + cantidadFormateada + " en stock. "
                            + "Vende o retira el stock antes de eliminarlo.");
        }

        producto.setEstado(0);
        productoRepository.save(producto);
    }

    @Transactional
    public ProductoResponse reactivar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        producto.setEstado(1);
        return toResponse(productoRepository.save(producto));
    }

    private String generarCodigo() {
        Integer ultimo = productoRepository.obtenerUltimoNumeroCodigo();
        int siguiente = (ultimo == null ? 0 : ultimo) + 1;
        return String.format("PRD-%05d", siguiente);
    }

    private String limpiar(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private BigDecimal calcularStockTotal(Long idProducto) {
        return stockRepository.findByProductoId(idProducto).stream()
                .map(Stock::getCantidad)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Formatea cantidad + unidad con singular/plural correcto.
     * Ej: 1 + UNIDAD -> "1 unidad" | 2 + UNIDAD -> "2 unidades"
     *     1 + KILO   -> "1 kilo"   | 3.5 + KILO -> "3.5 kilos"
     */
    private String formatearCantidadConUnidad(BigDecimal cantidad, String unidadMedida) {
        if (cantidad == null) return "0";

        BigDecimal limpia = cantidad.stripTrailingZeros();
        String num = limpia.toPlainString();

        if (unidadMedida == null || unidadMedida.isBlank()) {
            return num;
        }

        boolean singular = limpia.compareTo(BigDecimal.ONE) == 0;
        String unidad = unidadMedida.toLowerCase().trim();

        String palabra;
        if (singular) {
            palabra = unidad;
        } else {
            switch (unidad) {
                case "unidad": palabra = "unidades"; break;
                case "kilo":   palabra = "kilos";    break;
                case "litro":  palabra = "litros";   break;
                case "gramo":  palabra = "gramos";   break;
                default:       palabra = unidad;
            }
        }
        return num + " " + palabra;
    }

    private Almacen resolverAlmacenPrincipal(Producto p) {
        if (p.getAlmacenPrincipal() != null) return p.getAlmacenPrincipal();
        List<Stock> stocks = stockRepository.findByProductoId(p.getId());
        return stocks.stream()
                .min(Comparator.comparing(Stock::getId))
                .map(Stock::getAlmacen)
                .orElse(null);
    }

    private ProductoResponse toResponse(Producto p) {
        Almacen principal = resolverAlmacenPrincipal(p);
        return ProductoResponse.builder()
                .id(p.getId())
                .codigo(p.getCodigo())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .precioCompra(p.getPrecioCompra())
                .precioVenta(p.getPrecioVenta())
                .idCategoria(p.getCategoria().getId())
                .categoriaNombre(p.getCategoria().getNombre())
                .unidadMedida(p.getUnidadMedida())
                .stockTotal(calcularStockTotal(p.getId()))
                .idAlmacenDefault(principal != null ? principal.getId() : null)
                .nombreAlmacenDefault(principal != null ? principal.getNombre() : null)
                .estado(p.getEstado())
                .fechaRegistro(p.getFechaRegistro())
                .build();
    }
}