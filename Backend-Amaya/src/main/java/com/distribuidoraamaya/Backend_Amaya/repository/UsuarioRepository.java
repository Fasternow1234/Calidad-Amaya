package com.distribuidoraamaya.Backend_Amaya.repository;

import com.distribuidoraamaya.Backend_Amaya.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
    Optional<Usuario> findByEmail(String email);
    boolean existsByNombreUsuario(String nombreUsuario);
    boolean existsByEmail(String email);
    List<Usuario> findAllByOrderByNombreEmpleadoAsc();
    long countByRol_NombreAndEstado(String nombreRol, Integer estado);
}
