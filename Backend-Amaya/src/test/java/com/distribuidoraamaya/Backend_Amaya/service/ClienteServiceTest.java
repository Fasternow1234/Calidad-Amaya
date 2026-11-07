package com.distribuidoraamaya.Backend_Amaya.service;

import com.distribuidoraamaya.Backend_Amaya.dto.request.ClienteRequest;
import com.distribuidoraamaya.Backend_Amaya.dto.response.ClienteResponse;
import com.distribuidoraamaya.Backend_Amaya.entity.Cliente;
import com.distribuidoraamaya.Backend_Amaya.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock ClienteRepository clienteRepository;
    @InjectMocks ClienteService clienteService;

    @Test
    void crear_conDniValido_guardaClienteActivo() {
        ClienteRequest request = clienteDni("12345678");
        when(clienteRepository.existsByNumeroDocumento("12345678")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente cliente = invocation.getArgument(0);
            cliente.setId(1L);
            return cliente;
        });

        ClienteResponse response = clienteService.crear(request);

        assertEquals(1L, response.getId());
        assertEquals("12345678", response.getNumeroDocumento());
        assertEquals("Alex Hurtado Gonzales", response.getNombreMostrar());
        assertEquals(1, response.getEstado());
    }

    @Test
    void crear_conDniInvalido_rechazaSinGuardar() {
        ClienteRequest request = clienteDni("1234");

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class, () -> clienteService.crear(request));

        assertTrue(error.getMessage().contains("DNI"));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void crear_conDocumentoDuplicado_rechazaSinGuardar() {
        ClienteRequest request = clienteDni("12345678");
        when(clienteRepository.existsByNumeroDocumento("12345678")).thenReturn(true);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class, () -> clienteService.crear(request));

        assertTrue(error.getMessage().contains("Ya existe"));
        verify(clienteRepository, never()).save(any());
    }

    private ClienteRequest clienteDni(String dni) {
        ClienteRequest request = new ClienteRequest();
        request.setTipoDocumento("DNI");
        request.setNumeroDocumento(dni);
        request.setNombres("Alex");
        request.setApellidoPaterno("Hurtado");
        request.setApellidoMaterno("Gonzales");
        return request;
    }
}
