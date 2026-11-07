package com.distribuidoraamaya.Backend_Amaya.service;

import com.distribuidoraamaya.Backend_Amaya.entity.Almacen;
import com.distribuidoraamaya.Backend_Amaya.entity.Movimiento;
import com.distribuidoraamaya.Backend_Amaya.entity.Producto;
import com.distribuidoraamaya.Backend_Amaya.entity.Stock;
import com.distribuidoraamaya.Backend_Amaya.entity.TipoMovimiento;
import com.distribuidoraamaya.Backend_Amaya.repository.AlmacenRepository;
import com.distribuidoraamaya.Backend_Amaya.repository.MovimientoRepository;
import com.distribuidoraamaya.Backend_Amaya.repository.ProductoRepository;
import com.distribuidoraamaya.Backend_Amaya.repository.StockRepository;
import com.distribuidoraamaya.Backend_Amaya.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceTest {

    @Mock MovimientoRepository movimientoRepository;
    @Mock StockRepository stockRepository;
    @Mock ProductoRepository productoRepository;
    @Mock AlmacenRepository almacenRepository;
    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks MovimientoService movimientoService;

    @AfterEach
    void limpiarSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registrarEntrada_sumaCantidadYGuardaMovimiento() {
        Producto producto = producto();
        Almacen almacen = almacen();
        Stock stock = Stock.builder().producto(producto).almacen(almacen)
                .cantidad(new BigDecimal("5.00")).build();
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(almacenRepository.findById(2L)).thenReturn(Optional.of(almacen));
        when(stockRepository.findByProductoIdAndAlmacenId(1L, 2L)).thenReturn(Optional.of(stock));
        when(movimientoRepository.save(any(Movimiento.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Movimiento resultado = movimientoService.registrar(
                1L, 2L, TipoMovimiento.ENTRADA, new BigDecimal("3.00"), " Compra ");

        assertEquals(0, new BigDecimal("8.00").compareTo(stock.getCantidad()));
        assertEquals(0, new BigDecimal("5.00").compareTo(resultado.getStockAnterior()));
        assertEquals(0, new BigDecimal("8.00").compareTo(resultado.getStockNuevo()));
        assertEquals("Compra", resultado.getObservacion());
        verify(stockRepository).save(stock);
    }

    @Test
    void registrarSalida_conStockInsuficiente_rechazaSinModificarStock() {
        Producto producto = producto();
        Almacen almacen = almacen();
        Stock stock = Stock.builder().producto(producto).almacen(almacen)
                .cantidad(new BigDecimal("5.00")).build();
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(almacenRepository.findById(2L)).thenReturn(Optional.of(almacen));
        when(stockRepository.findByProductoIdAndAlmacenId(1L, 2L)).thenReturn(Optional.of(stock));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> movimientoService.registrar(
                        1L, 2L, TipoMovimiento.SALIDA, new BigDecimal("6.00"), null));

        assertTrue(error.getMessage().contains("Stock insuficiente"));
        assertEquals(0, new BigDecimal("5.00").compareTo(stock.getCantidad()));
        verify(stockRepository, never()).save(any());
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void registrar_conCantidadCero_rechazaAntesDeConsultarRepositorios() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> movimientoService.registrar(
                        1L, 2L, TipoMovimiento.ENTRADA, BigDecimal.ZERO, null));

        assertTrue(error.getMessage().contains("mayor a 0"));
        verify(productoRepository, never()).findById(any());
    }

    private Producto producto() {
        return Producto.builder().id(1L).codigo("PRD-00001").nombre("Ariel")
                .unidadMedida("UNIDAD").estado(1).build();
    }

    private Almacen almacen() {
        return Almacen.builder().id(2L).nombre("Central").estado(1).build();
    }
}
