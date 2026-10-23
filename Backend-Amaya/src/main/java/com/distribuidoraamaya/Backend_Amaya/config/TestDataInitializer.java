package com.distribuidoraamaya.Backend_Amaya.config;

import com.distribuidoraamaya.Backend_Amaya.entity.Almacen;
import com.distribuidoraamaya.Backend_Amaya.entity.Categoria;
import com.distribuidoraamaya.Backend_Amaya.entity.Cliente;
import com.distribuidoraamaya.Backend_Amaya.entity.Producto;
import com.distribuidoraamaya.Backend_Amaya.entity.Stock;
import com.distribuidoraamaya.Backend_Amaya.entity.TipoMovimiento;
import com.distribuidoraamaya.Backend_Amaya.repository.AlmacenRepository;
import com.distribuidoraamaya.Backend_Amaya.repository.CategoriaRepository;
import com.distribuidoraamaya.Backend_Amaya.repository.ClienteRepository;
import com.distribuidoraamaya.Backend_Amaya.repository.ProductoRepository;
import com.distribuidoraamaya.Backend_Amaya.repository.StockRepository;
import com.distribuidoraamaya.Backend_Amaya.service.MovimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("test")
@RequiredArgsConstructor
public class TestDataInitializer implements CommandLineRunner {

    private static final String NOMBRE_ALMACEN = "Almacén QA";
    private static final String NOMBRE_CATEGORIA = "Pruebas automatizadas";
    private static final String CODIGO_PRODUCTO = "E2E-PRD-001";
    private static final String NOMBRE_PRODUCTO = "Producto Selenium Prueba";
    private static final String DOCUMENTO_CLIENTE = "99999999";

    private final AlmacenRepository almacenRepository;
    private final CategoriaRepository categoriaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final StockRepository stockRepository;
    private final MovimientoService movimientoService;

    @Override
    public void run(String... args) {
        Almacen almacen = obtenerAlmacen();
        Categoria categoria = obtenerCategoria();
        crearClienteSiFalta();
        Producto producto = obtenerProducto(categoria, almacen);
        prepararStock(producto, almacen);
    }

    private Almacen obtenerAlmacen() {
        return almacenRepository.findByNombreIgnoreCase(NOMBRE_ALMACEN)
                .orElseGet(() -> almacenRepository.save(Almacen.builder()
                        .nombre(NOMBRE_ALMACEN)
                        .direccion("Ambiente aislado de pruebas")
                        .estado(1)
                        .build()));
    }

    private Categoria obtenerCategoria() {
        return categoriaRepository.findByNombreIgnoreCase(NOMBRE_CATEGORIA)
                .orElseGet(() -> categoriaRepository.save(Categoria.builder()
                        .nombre(NOMBRE_CATEGORIA)
                        .estado(1)
                        .build()));
    }

    private void crearClienteSiFalta() {
        if (clienteRepository.existsByNumeroDocumento(DOCUMENTO_CLIENTE)) return;

        clienteRepository.save(Cliente.builder()
                .tipoDocumento("DNI")
                .numeroDocumento(DOCUMENTO_CLIENTE)
                .nombres("Cliente")
                .apellidoPaterno("Prueba")
                .apellidoMaterno("Automatizada")
                .estado(1)
                .build());
    }

    private Producto obtenerProducto(Categoria categoria, Almacen almacen) {
        return productoRepository.findByCodigo(CODIGO_PRODUCTO)
                .orElseGet(() -> productoRepository.save(Producto.builder()
                        .codigo(CODIGO_PRODUCTO)
                        .nombre(NOMBRE_PRODUCTO)
                        .descripcion("Producto de uso exclusivo en pruebas automatizadas")
                        .precioCompra(new BigDecimal("5.00"))
                        .precioVenta(new BigDecimal("8.70"))
                        .categoria(categoria)
                        .almacenPrincipal(almacen)
                        .unidadMedida("UNIDAD")
                        .estado(1)
                        .build()));
    }

    private void prepararStock(Producto producto, Almacen almacen) {
        Stock stock = stockRepository.findByProductoIdAndAlmacenId(producto.getId(), almacen.getId())
                .orElse(null);
        if (stock == null || stock.getCantidad() == null || stock.getCantidad().compareTo(BigDecimal.ZERO) == 0) {
            movimientoService.registrar(
                    producto.getId(),
                    almacen.getId(),
                    TipoMovimiento.ENTRADA,
                    new BigDecimal("20"),
                    "Stock inicial del ambiente Selenium"
            );
        }
    }
}
