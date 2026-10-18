package com.distribuidoraamaya.Backend_Amaya.controller;

import com.distribuidoraamaya.Backend_Amaya.dto.request.LoginRequest;
import com.distribuidoraamaya.Backend_Amaya.dto.response.LoginResponse;
import com.distribuidoraamaya.Backend_Amaya.dto.response.OpcionResponse;
import com.distribuidoraamaya.Backend_Amaya.entity.RolOpcion;
import com.distribuidoraamaya.Backend_Amaya.entity.Usuario;
import com.distribuidoraamaya.Backend_Amaya.repository.RolOpcionRepository;
import com.distribuidoraamaya.Backend_Amaya.repository.UsuarioRepository;
import com.distribuidoraamaya.Backend_Amaya.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

        private final AuthenticationManager authenticationManager;
        private final UserDetailsService userDetailsService;
        private final JwtService jwtService;
        private final UsuarioRepository usuarioRepository;
        private final RolOpcionRepository rolOpcionRepository;

        @PostMapping("/login")
        public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getNombreUsuario(),
                                                request.getPassword()));

                UserDetails userDetails = userDetailsService.loadUserByUsername(request.getNombreUsuario());
                Usuario usuario = usuarioRepository.findByNombreUsuario(request.getNombreUsuario()).orElseThrow();

                String token = jwtService.generateToken(userDetails);

                List<OpcionResponse> opciones = rolOpcionRepository
                                .findByRolIdConOpcion(usuario.getRol().getId())
                                .stream()
                                .map(RolOpcion::getOpcion)
                                .filter(o -> o.getEstado() == 1)
                                .map(o -> OpcionResponse.builder()
                                                .id(o.getId())
                                                .codigo(o.getCodigo())
                                                .nombre(o.getNombre())
                                                .orden(o.getOrden())
                                                .build())
                                .toList();

                return ResponseEntity.ok(LoginResponse.builder()
                                .accessToken(token)
                                .tokenType("Bearer")
                                .expiresIn(jwtService.getExpirationMs() / 1000)
                                .usuario(LoginResponse.UsuarioInfo.builder()
                                                .id(usuario.getId())
                                                .nombreUsuario(usuario.getNombreUsuario())
                                                .nombreEmpleado(usuario.getNombreEmpleado())
                                                .email(usuario.getEmail())
                                                .estado(usuario.getEstado())
                                                .rol(usuario.getRol().getNombre())
                                                .opciones(opciones)
                                                .build())
                                .build());
        }

        @GetMapping("/me")
        public ResponseEntity<LoginResponse.UsuarioInfo> me(@AuthenticationPrincipal UserDetails userDetails) {

                Usuario usuario = usuarioRepository.findByNombreUsuario(userDetails.getUsername()).orElseThrow();

                List<OpcionResponse> opciones = rolOpcionRepository
                                .findByRolIdConOpcion(usuario.getRol().getId())
                                .stream()
                                .map(RolOpcion::getOpcion)
                                .filter(o -> o.getEstado() == 1)
                                .map(o -> OpcionResponse.builder()
                                                .id(o.getId())
                                                .codigo(o.getCodigo())
                                                .nombre(o.getNombre())
                                                .orden(o.getOrden())
                                                .build())
                                .toList();

                return ResponseEntity.ok(LoginResponse.UsuarioInfo.builder()
                                .id(usuario.getId())
                                .nombreUsuario(usuario.getNombreUsuario())
                                .nombreEmpleado(usuario.getNombreEmpleado())
                                .email(usuario.getEmail())
                                .estado(usuario.getEstado())
                                .rol(usuario.getRol().getNombre())
                                .opciones(opciones)
                                .build());
        }
}