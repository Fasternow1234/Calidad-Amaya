package com.distribuidoraamaya.Backend_Amaya.service;

import com.distribuidoraamaya.Backend_Amaya.dto.request.ComprobanteRequest;
import com.distribuidoraamaya.Backend_Amaya.dto.request.DetalleComprobanteRequest;
import com.distribuidoraamaya.Backend_Amaya.dto.response.ComprobanteResponse;
import com.distribuidoraamaya.Backend_Amaya.entity.Almacen;
import com.distribuidoraamaya.Backend_Amaya.entity.Cliente;
import com.distribuidoraamaya.Backend_Amaya.entity.Comprobante;
import com.distribuidoraamaya.Backend_Amaya.entity.DetalleComprobante;
import com.distribuidoraamaya.Backend_Amaya.entity.Producto;
import com.distribuidoraamaya.Backend_Amaya.entity.Stock;
import com.distribuidoraamaya.Backend_Amaya.entity.TipoComprobante;
import com.distribuidoraamaya.Backend_Amaya.entity.TipoMovimiento;
import com.distribuidoraamaya.Backend_Amaya.entity.Usuario;
import com.distribuidoraamaya.Backend_Amaya.repository.AlmacenRepository;
import com.distribuidoraamaya.Backend_Amaya.repository.ClienteRepository;
import com.distribuidoraamaya.Backend_Amaya.repository.ComprobanteRepository;
import com.distribuidoraamaya.Backend_Amaya.repository.ProductoRepository;
import com.distribuidoraamaya.Backend_Amaya.repository.ProveedorRepository;
import com.distribuidoraamaya.Backend_Amaya.repository.StockRepository;
import com.distribuidoraamaya.Backend_Amaya.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComprobanteServiceTest {

    private static final Long ID_CLIENTE = 10L;
    private static final Long ID_PRODUCTO = 20L;
    private static final Long ID_ALMACEN = 30L;
    private static final String NUMERO_COMPROBANTE = "F001-000001";
    private static final LocalDate FECHA_EMISION = LocalDate.of(2026, 10, 22);
    private static final BigDecimal PRECIO_UNITARIO = new BigDecimal("25.50");

    @Mock
    private ComprobanteRepository comprobanteRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private AlmacenRepository almacenRepository;

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private MovimientoService movimientoService;

    @InjectMocks
    private ComprobanteService comprobanteService;

    @AfterEach
    void limpiarContextoDeSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void crearVenta_valida_guardaComprobanteYRegistraSalida() {
        // Preparar
        simularAutenticacion();
        Cliente cliente = clienteActivo();
        Producto producto = productoActivo();
        Almacen almacen = almacen();
        Stock stock = stock(producto, almacen, "19.00");
        ComprobanteRequest request = solicitudVenta("1.00");

        when(comprobanteRepository.existsByNumero(NUMERO_COMPROBANTE)).thenReturn(false);
        when(clienteRepository.findById(ID_CLIENTE)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findByNombreUsuario("vendedor")).thenReturn(Optional.of(usuario()));
        when(productoRepository.findById(ID_PRODUCTO)).thenReturn(Optional.of(producto));
        when(stockRepository.findByProductoId(ID_PRODUCTO)).thenReturn(List.of(stock));
        when(comprobanteRepository.save(any(Comprobante.class))).thenAnswer(invocation -> {
            Comprobante comprobante = invocation.getArgument(0);
            comprobante.setId(100L);
            return comprobante;
        });

        // Ejecutar
        ComprobanteResponse resultado = comprobanteService.crear(request);

        // Comprobar
        ArgumentCaptor<Comprobante> captor = ArgumentCaptor.forClass(Comprobante.class);
        verify(comprobanteRepository).save(captor.capture());
        Comprobante guardado = captor.getValue();

        assertEquals(TipoComprobante.VENTA, guardado.getTipo());
        assertEquals(NUMERO_COMPROBANTE, guardado.getNumero());
        assertEquals(FECHA_EMISION, guardado.getFechaEmision());
        assertSame(cliente, guardado.getCliente());
        assertEquals(0, new BigDecimal("25.50").compareTo(guardado.getTotal()));
        assertEquals(1, guardado.getDetalles().size());

        DetalleComprobante detalle = guardado.getDetalles().get(0);
        assertSame(producto, detalle.getProducto());
        assertSame(almacen, detalle.getAlmacen());
        assertEquals(0, new BigDecimal("1.00").compareTo(detalle.getCantidad()));
        assertEquals(0, PRECIO_UNITARIO.compareTo(detalle.getPrecioUnitario()));

        verify(movimientoService).registrar(
                ID_PRODUCTO,
                ID_ALMACEN,
                TipoMovimiento.SALIDA,
                new BigDecimal("1.00"),
                "VENTA " + NUMERO_COMPROBANTE
        );
        assertEquals(0, new BigDecimal("25.50").compareTo(resultado.getTotal()));
        assertEquals("VENTA", resultado.getTipo());
    }

    @Test
    void crearVenta_conStockInsuficiente_rechazaSinGuardarNiRegistrarMovimiento() {
        // Preparar
        simularAutenticacion();
        Cliente cliente = clienteActivo();
        Producto producto = productoActivo();
        Stock stock = stock(producto, almacen(), "19.00");
        ComprobanteRequest request = solicitudVenta("20.00");

        when(comprobanteRepository.existsByNumero(NUMERO_COMPROBANTE)).thenReturn(false);
        when(clienteRepository.findById(ID_CLIENTE)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findByNombreUsuario("vendedor")).thenReturn(Optional.of(usuario()));
        when(productoRepository.findById(ID_PRODUCTO)).thenReturn(Optional.of(producto));
        when(stockRepository.findByProductoId(ID_PRODUCTO)).thenReturn(List.of(stock));

        // Ejecutar
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> comprobanteService.crear(request)
        );

        // Comprobar
        assertTrue(excepcion.getMessage().contains("No hay stock suficiente"));
        verify(comprobanteRepository, never()).save(any(Comprobante.class));
        verifyNoInteractions(movimientoService);
    }

    @Test
    void crearVenta_conClienteInactivo_rechazaAntesDeGuardarORegistrarMovimiento() {
        // Preparar
        ComprobanteRequest request = solicitudVenta("1.00");
        Cliente clienteInactivo = clienteActivo();
        clienteInactivo.setEstado(0);

        when(comprobanteRepository.existsByNumero(NUMERO_COMPROBANTE)).thenReturn(false);
        when(clienteRepository.findById(ID_CLIENTE)).thenReturn(Optional.of(clienteInactivo));

        // Ejecutar
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> comprobanteService.crear(request)
        );

        // Comprobar
        assertTrue(excepcion.getMessage().contains("cliente seleccionado")
                && excepcion.getMessage().contains("inactivo"));
        verify(comprobanteRepository, never()).save(any(Comprobante.class));
        verifyNoInteractions(productoRepository, stockRepository, movimientoService);
    }

    @Test
    void crearVenta_conProductoInactivo_rechazaAntesDeGuardarORegistrarMovimiento() {
        // Preparar
        simularAutenticacion();
        Producto productoInactivo = productoActivo();
        productoInactivo.setEstado(0);
        ComprobanteRequest request = solicitudVenta("1.00");

        when(comprobanteRepository.existsByNumero(NUMERO_COMPROBANTE)).thenReturn(false);
        when(clienteRepository.findById(ID_CLIENTE)).thenReturn(Optional.of(clienteActivo()));
        when(usuarioRepository.findByNombreUsuario("vendedor")).thenReturn(Optional.of(usuario()));
        when(productoRepository.findById(ID_PRODUCTO)).thenReturn(Optional.of(productoInactivo));

        // Ejecutar
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> comprobanteService.crear(request)
        );

        // Comprobar
        assertTrue(excepcion.getMessage().contains("Producto de prueba")
                && excepcion.getMessage().contains("inactivo"));
        verify(comprobanteRepository, never()).save(any(Comprobante.class));
        verifyNoInteractions(stockRepository, movimientoService);
    }

    @Test
    void crearVenta_conNumeroDuplicado_rechazaSinGuardarNiRegistrarMovimiento() {
        // Preparar
        ComprobanteRequest request = solicitudVenta("1.00");
        when(comprobanteRepository.existsByNumero(NUMERO_COMPROBANTE)).thenReturn(true);

        // Ejecutar
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> comprobanteService.crear(request)
        );

        // Comprobar
        assertTrue(excepcion.getMessage().contains("Ya existe un comprobante"));
        verify(comprobanteRepository, never()).save(any(Comprobante.class));
        verifyNoInteractions(clienteRepository, productoRepository, stockRepository, movimientoService);
    }

    @Test
    void crearVenta_conCantidadIgualAlStock_aceptaLaVenta() {
        // Preparar
        simularAutenticacion();
        Cliente cliente = clienteActivo();
        Producto producto = productoActivo();
        Almacen almacen = almacen();
        Stock stock = stock(producto, almacen, "19.00");
        ComprobanteRequest request = solicitudVenta("19.00");

        when(comprobanteRepository.existsByNumero(NUMERO_COMPROBANTE)).thenReturn(false);
        when(clienteRepository.findById(ID_CLIENTE)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findByNombreUsuario("vendedor")).thenReturn(Optional.of(usuario()));
        when(productoRepository.findById(ID_PRODUCTO)).thenReturn(Optional.of(producto));
        when(stockRepository.findByProductoId(ID_PRODUCTO)).thenReturn(List.of(stock));
        when(comprobanteRepository.save(any(Comprobante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecutar
        ComprobanteResponse resultado = comprobanteService.crear(request);

        // Comprobar
        verify(comprobanteRepository).save(any(Comprobante.class));
        verify(movimientoService).registrar(
                ID_PRODUCTO,
                ID_ALMACEN,
                TipoMovimiento.SALIDA,
                new BigDecimal("19.00"),
                "VENTA " + NUMERO_COMPROBANTE
        );
        assertEquals(0, new BigDecimal("484.50").compareTo(resultado.getTotal()));
    }

    private void simularAutenticacion() {
        UsernamePasswordAuthenticationToken autenticacion = UsernamePasswordAuthenticationToken.authenticated(
                "vendedor",
                "credencial-no-real",
                List.of(new SimpleGrantedAuthority("ROLE_VENDEDOR"))
        );
        SecurityContextHolder.getContext().setAuthentication(autenticacion);
    }

    private ComprobanteRequest solicitudVenta(String cantidad) {
        DetalleComprobanteRequest detalle = new DetalleComprobanteRequest();
        detalle.setIdProducto(ID_PRODUCTO);
        detalle.setCantidad(new BigDecimal(cantidad));
        detalle.setPrecioUnitario(PRECIO_UNITARIO);

        ComprobanteRequest request = new ComprobanteRequest();
        request.setTipo("VENTA");
        request.setNumero(NUMERO_COMPROBANTE);
        request.setFechaEmision(FECHA_EMISION);
        request.setIdCliente(ID_CLIENTE);
        request.setObservacion("Venta unitaria de prueba");
        request.setDetalles(List.of(detalle));
        return request;
    }

    private Cliente clienteActivo() {
        return Cliente.builder()
                .id(ID_CLIENTE)
                .tipoDocumento("DNI")
                .numeroDocumento("12345678")
                .nombres("Cliente")
                .apellidoPaterno("Prueba")
                .estado(1)
                .build();
    }

    private Producto productoActivo() {
        return Producto.builder()
                .id(ID_PRODUCTO)
                .codigo("PROD-TEST")
                .nombre("Producto de prueba")
                .unidadMedida("unidad")
                .estado(1)
                .build();
    }

    private Almacen almacen() {
        return Almacen.builder()
                .id(ID_ALMACEN)
                .nombre("Almacen de prueba")
                .estado(1)
                .build();
    }

    private Stock stock(Producto producto, Almacen almacen, String cantidad) {
        return Stock.builder()
                .id(40L)
                .producto(producto)
                .almacen(almacen)
                .cantidad(new BigDecimal(cantidad))
                .build();
    }

    private Usuario usuario() {
        return Usuario.builder()
                .id(50L)
                .nombreUsuario("vendedor")
                .estado(1)
                .build();
    }
}
