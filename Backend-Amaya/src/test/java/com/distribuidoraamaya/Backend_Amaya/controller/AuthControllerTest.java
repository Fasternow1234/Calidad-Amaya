package com.distribuidoraamaya.Backend_Amaya.controller;

import com.distribuidoraamaya.Backend_Amaya.dto.request.LoginRequest;
import com.distribuidoraamaya.Backend_Amaya.dto.response.LoginResponse;
import com.distribuidoraamaya.Backend_Amaya.entity.Opcion;
import com.distribuidoraamaya.Backend_Amaya.entity.Rol;
import com.distribuidoraamaya.Backend_Amaya.entity.RolOpcion;
import com.distribuidoraamaya.Backend_Amaya.entity.Usuario;
import com.distribuidoraamaya.Backend_Amaya.repository.RolOpcionRepository;
import com.distribuidoraamaya.Backend_Amaya.repository.UsuarioRepository;
import com.distribuidoraamaya.Backend_Amaya.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock UserDetailsService userDetailsService;
    @Mock JwtService jwtService;
    @Mock UsuarioRepository usuarioRepository;
    @Mock RolOpcionRepository rolOpcionRepository;
    @InjectMocks AuthController authController;

    @Test
    void login_conCredencialesValidas_devuelveTokenRolYOpcionesActivas() {
        LoginRequest request = new LoginRequest();
        request.setNombreUsuario("vendedor");
        request.setPassword("credencial-de-prueba");

        Rol rol = Rol.builder().id(3L).nombre("VENDEDOR").build();
        Usuario usuario = Usuario.builder()
                .id(10L).nombreUsuario("vendedor").nombreEmpleado("Usuario de prueba")
                .email("prueba@local.test").estado(1).rol(rol).build();
        Opcion activa = Opcion.builder().id(1L).codigo("VENTAS").nombre("Comprobantes")
                .orden(1).estado(1).build();
        Opcion inactiva = Opcion.builder().id(2L).codigo("OCULTA").nombre("Oculta")
                .orden(2).estado(0).build();

        when(userDetailsService.loadUserByUsername("vendedor")).thenReturn(usuario);
        when(usuarioRepository.findByNombreUsuario("vendedor")).thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(usuario)).thenReturn("token-de-prueba");
        when(jwtService.getExpirationMs()).thenReturn(3_600_000L);
        when(rolOpcionRepository.findByRolIdConOpcion(3L)).thenReturn(List.of(
                RolOpcion.builder().opcion(activa).build(),
                RolOpcion.builder().opcion(inactiva).build()));

        ResponseEntity<LoginResponse> response = authController.login(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("token-de-prueba", response.getBody().getAccessToken());
        assertEquals("VENDEDOR", response.getBody().getUsuario().getRol());
        assertEquals(1, response.getBody().getUsuario().getOpciones().size());
        assertEquals("VENTAS", response.getBody().getUsuario().getOpciones().get(0).getCodigo());
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void login_conCredencialesInvalidas_rechazaLaAutenticacion() {
        LoginRequest request = new LoginRequest();
        request.setNombreUsuario("vendedor");
        request.setPassword("incorrecta");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        BadCredentialsException error = assertThrows(
                BadCredentialsException.class, () -> authController.login(request));

        assertTrue(error.getMessage().contains("Credenciales"));
    }
}
