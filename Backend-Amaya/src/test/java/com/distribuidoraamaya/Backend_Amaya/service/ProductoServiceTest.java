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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock ProductoRepository productoRepository;
    @Mock CategoriaRepository categoriaRepository;
    @Mock AlmacenRepository almacenRepository;
    @Mock StockRepository stockRepository;
    @InjectMocks ProductoService productoService;

    @Test
    void crear_conDatosValidos_guardaProductoYStockInicialCero() {
        ProductoRequest request = productoValido();
        Categoria categoria = Categoria.builder().id(2L).nombre("Limpieza").estado(1).build();
        Almacen almacen = Almacen.builder().id(3L).nombre("Central").estado(1).build();
        when(productoRepository.existsByNombreIgnoreCase("Ariel")).thenReturn(false);
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria));
        when(almacenRepository.findById(3L)).thenReturn(Optional.of(almacen));
        when(productoRepository.obtenerUltimoNumeroCodigo()).thenReturn(2);
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> {
            Producto producto = invocation.getArgument(0);
            producto.setId(10L);
            return producto;
        });
        when(stockRepository.findByProductoId(10L)).thenReturn(List.of());

        ProductoResponse response = productoService.crear(request);

        assertEquals("PRD-00003", response.getCodigo());
        assertEquals("Ariel", response.getNombre());
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getStockTotal()));
        verify(stockRepository).save(any(Stock.class));
    }

    @Test
    void crear_conProductoDuplicado_rechazaSinGuardar() {
        ProductoRequest request = productoValido();
        when(productoRepository.existsByNombreIgnoreCase("Ariel")).thenReturn(true);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class, () -> productoService.crear(request));

        assertTrue(error.getMessage().contains("Ya existe"));
        verify(productoRepository, never()).save(any());
        verify(stockRepository, never()).save(any());
    }

    @Test
    void crear_conCategoriaInactiva_rechazaSinGuardar() {
        ProductoRequest request = productoValido();
        Categoria inactiva = Categoria.builder().id(2L).nombre("Limpieza").estado(0).build();
        when(productoRepository.existsByNombreIgnoreCase("Ariel")).thenReturn(false);
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(inactiva));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class, () -> productoService.crear(request));

        assertTrue(error.getMessage().contains("inactiva"));
        verify(productoRepository, never()).save(any());
    }

    private ProductoRequest productoValido() {
        ProductoRequest request = new ProductoRequest();
        request.setNombre("Ariel");
        request.setDescripcion("Detergente");
        request.setPrecioCompra(new BigDecimal("4.00"));
        request.setPrecioVenta(new BigDecimal("5.00"));
        request.setIdCategoria(2L);
        request.setIdAlmacen(3L);
        request.setUnidadMedida("UNIDAD");
        return request;
    }
}
