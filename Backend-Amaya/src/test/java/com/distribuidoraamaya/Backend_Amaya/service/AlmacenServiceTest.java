package com.distribuidoraamaya.Backend_Amaya.service;

import com.distribuidoraamaya.Backend_Amaya.dto.request.AlmacenRequest;
import com.distribuidoraamaya.Backend_Amaya.dto.response.AlmacenResponse;
import com.distribuidoraamaya.Backend_Amaya.entity.Almacen;
import com.distribuidoraamaya.Backend_Amaya.entity.Stock;
import com.distribuidoraamaya.Backend_Amaya.repository.AlmacenRepository;
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
class AlmacenServiceTest {

    @Mock AlmacenRepository almacenRepository;
    @Mock StockRepository stockRepository;
    @InjectMocks AlmacenService almacenService;

    @Test
    void crear_conNombreDisponible_guardaAlmacenActivo() {
        AlmacenRequest request = new AlmacenRequest();
        request.setNombre(" Almacén Central ");
        request.setDireccion(" Lima ");
        when(almacenRepository.existsByNombreIgnoreCase("Almacén Central")).thenReturn(false);
        when(almacenRepository.save(any(Almacen.class))).thenAnswer(invocation -> {
            Almacen almacen = invocation.getArgument(0);
            almacen.setId(1L);
            return almacen;
        });
        when(stockRepository.contarProductosConStock(1L)).thenReturn(0L);

        AlmacenResponse response = almacenService.crear(request);

        assertEquals("Almacén Central", response.getNombre());
        assertEquals("Lima", response.getDireccion());
        assertEquals(1, response.getEstado());
    }

    @Test
    void crear_conNombreDuplicado_rechazaSinGuardar() {
        AlmacenRequest request = new AlmacenRequest();
        request.setNombre("Central");
        when(almacenRepository.existsByNombreIgnoreCase("Central")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> almacenService.crear(request));
        verify(almacenRepository, never()).save(any());
    }

    @Test
    void desactivar_conProductosEnStock_rechazaLaOperacion() {
        Almacen almacen = Almacen.builder().id(1L).nombre("Central").estado(1).build();
        Stock stock = Stock.builder().cantidad(new BigDecimal("5.00")).build();
        when(almacenRepository.findById(1L)).thenReturn(Optional.of(almacen));
        when(stockRepository.findByAlmacenId(1L)).thenReturn(List.of(stock));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class, () -> almacenService.desactivar(1L));

        assertTrue(error.getMessage().contains("producto con stock"));
        assertEquals(1, almacen.getEstado());
        verify(almacenRepository, never()).save(any());
    }
}
