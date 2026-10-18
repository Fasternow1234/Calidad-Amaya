package com.distribuidoraamaya.Backend_Amaya.service;

import com.distribuidoraamaya.Backend_Amaya.dto.request.UsuarioRequest;
import com.distribuidoraamaya.Backend_Amaya.dto.response.RolResponse;
import com.distribuidoraamaya.Backend_Amaya.dto.response.UsuarioResponse;
import com.distribuidoraamaya.Backend_Amaya.entity.Rol;
import com.distribuidoraamaya.Backend_Amaya.entity.Usuario;
import com.distribuidoraamaya.Backend_Amaya.repository.RolRepository;
import com.distribuidoraamaya.Backend_Amaya.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final Set<String> ROLES_PERMITIDOS = Set.of("ADMINISTRADOR", "ALMACENERO", "VENDEDOR");

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAllByOrderByNombreEmpleadoAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RolResponse> listarRoles() {
        return rolRepository.findAll().stream()
                .filter(rol -> List.of("ADMINISTRADOR", "ALMACENERO", "VENDEDOR").contains(rol.getNombre()))
                .sorted((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()))
                .map(rol -> RolResponse.builder()
                        .id(rol.getId()).nombre(rol.getNombre()).descripcion(rol.getDescripcion()).build())
                .toList();
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        String username = request.getNombreUsuario().trim();
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria para crear un usuario");
        }
        validarLongitudPassword(request.getPassword());
        validarUsuarioUnico(username, limpiar(request.getEmail()), null);

        Usuario usuario = Usuario.builder()
                .nombreUsuario(username)
                .password(passwordEncoder.encode(request.getPassword()))
                .nombreEmpleado(request.getNombreEmpleado().trim())
                .email(limpiar(request.getEmail()))
                .estado(1)
                .rol(obtenerRol(request.getIdRol()))
                .build();
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest request, String usuarioActual) {
        Usuario usuario = obtenerUsuario(id);
        validarNoUltimoAdministrador(usuario, request.getIdRol(), usuario.getEstado(), usuarioActual);
        String username = request.getNombreUsuario().trim();
        validarUsuarioUnico(username, limpiar(request.getEmail()), id);

        usuario.setNombreUsuario(username);
        usuario.setNombreEmpleado(request.getNombreEmpleado().trim());
        usuario.setEmail(limpiar(request.getEmail()));
        usuario.setRol(obtenerRol(request.getIdRol()));
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            validarLongitudPassword(request.getPassword());
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse cambiarEstado(Long id, Integer estado, String usuarioActual) {
        Usuario usuario = obtenerUsuario(id);
        if (estado == 0 && usuario.getNombreUsuario().equals(usuarioActual)) {
            throw new IllegalArgumentException("No puedes desactivar tu propia cuenta");
        }
        validarNoUltimoAdministrador(usuario, usuario.getRol().getId(), estado, usuarioActual);
        usuario.setEstado(estado);
        return toResponse(usuarioRepository.save(usuario));
    }

    private void validarNoUltimoAdministrador(Usuario usuario, Long nuevoIdRol, Integer nuevoEstado, String usuarioActual) {
        boolean esAdminActivo = "ADMINISTRADOR".equals(usuario.getRol().getNombre()) && usuario.getEstado() == 1;
        Rol nuevoRol = obtenerRol(nuevoIdRol);
        boolean seguiraAdminActivo = "ADMINISTRADOR".equals(nuevoRol.getNombre()) && nuevoEstado == 1;
        if (esAdminActivo && !seguiraAdminActivo
                && usuarioRepository.countByRol_NombreAndEstado("ADMINISTRADOR", 1) <= 1) {
            throw new IllegalArgumentException("Debe permanecer al menos un administrador activo");
        }
        if (usuario.getNombreUsuario().equals(usuarioActual) && !seguiraAdminActivo) {
            throw new IllegalArgumentException("No puedes quitarte el rol de administrador ni desactivar tu cuenta");
        }
    }

    private Usuario obtenerUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private Rol obtenerRol(Long id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El rol seleccionado no existe"));
        if (!ROLES_PERMITIDOS.contains(rol.getNombre())) {
            throw new IllegalArgumentException("Solo se permiten los roles definidos para el sistema");
        }
        return rol;
    }

    private void validarUsuarioUnico(String username, String email, Long idActual) {
        usuarioRepository.findByNombreUsuario(username).ifPresent(existente -> {
            if (!existente.getId().equals(idActual)) {
                throw new IllegalArgumentException("Ya existe un usuario con ese nombre de usuario");
            }
        });
        if (email != null) {
            usuarioRepository.findByEmail(email).ifPresent(existente -> {
                if (!existente.getId().equals(idActual)) {
                    throw new IllegalArgumentException("Ya existe un usuario con ese correo");
                }
            });
        }
    }

    private void validarLongitudPassword(String password) {
        if (password.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("La contraseña no puede superar 72 bytes UTF-8");
        }
    }

    private String limpiar(String valor) {
        if (valor == null || valor.isBlank()) return null;
        return valor.trim();
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombreUsuario(usuario.getNombreUsuario())
                .nombreEmpleado(usuario.getNombreEmpleado())
                .email(usuario.getEmail())
                .estado(usuario.getEstado())
                .fechaRegistro(usuario.getFechaRegistro())
                .idRol(usuario.getRol().getId())
                .rol(usuario.getRol().getNombre())
                .build();
    }
}
