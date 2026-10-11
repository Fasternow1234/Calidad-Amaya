package com.distribuidoraamaya.Backend_Amaya.config;

import com.distribuidoraamaya.Backend_Amaya.entity.Almacen;
import com.distribuidoraamaya.Backend_Amaya.entity.Opcion;
import com.distribuidoraamaya.Backend_Amaya.entity.Rol;
import com.distribuidoraamaya.Backend_Amaya.entity.RolOpcion;
import com.distribuidoraamaya.Backend_Amaya.entity.Usuario;
import com.distribuidoraamaya.Backend_Amaya.repository.AlmacenRepository;
import com.distribuidoraamaya.Backend_Amaya.repository.OpcionRepository;
import com.distribuidoraamaya.Backend_Amaya.repository.RolOpcionRepository;
import com.distribuidoraamaya.Backend_Amaya.repository.RolRepository;
import com.distribuidoraamaya.Backend_Amaya.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final OpcionRepository opcionRepository;
    private final RolOpcionRepository rolOpcionRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlmacenRepository almacenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Opcion inicio       = crearOpcionSiNoExiste("inicio",       "Inicio",       1);
        Opcion proveedores  = crearOpcionSiNoExiste("proveedores",  "Proveedores",  2);
        Opcion categorias   = crearOpcionSiNoExiste("categorias",   "Categorías",   3);
        Opcion productos    = crearOpcionSiNoExiste("productos",    "Productos",    4);
        Opcion almacenes    = crearOpcionSiNoExiste("almacenes",    "Almacenes",    5);
        Opcion movimientos  = crearOpcionSiNoExiste("movimientos",  "Movimientos",  6);
        Opcion comprobantes = crearOpcionSiNoExiste("comprobantes", "Comprobantes", 7);
        Opcion clientes     = crearOpcionSiNoExiste("clientes",     "Clientes",     8);

        Rol rolAdmin = rolRepository.findByNombre("ADMINISTRADOR")
                .orElseGet(() -> rolRepository.save(
                        Rol.builder()
                                .nombre("ADMINISTRADOR")
                                .descripcion("Administrador del sistema")
                                .build()
                ));

        crearRolOpcionSiNoExiste(rolAdmin.getId(), inicio.getId());
        crearRolOpcionSiNoExiste(rolAdmin.getId(), proveedores.getId());
        crearRolOpcionSiNoExiste(rolAdmin.getId(), categorias.getId());
        crearRolOpcionSiNoExiste(rolAdmin.getId(), productos.getId());
        crearRolOpcionSiNoExiste(rolAdmin.getId(), almacenes.getId());
        crearRolOpcionSiNoExiste(rolAdmin.getId(), movimientos.getId());
        crearRolOpcionSiNoExiste(rolAdmin.getId(), comprobantes.getId());
        crearRolOpcionSiNoExiste(rolAdmin.getId(), clientes.getId());

        if (!usuarioRepository.existsByNombreUsuario("admin")) {
            usuarioRepository.save(
                    Usuario.builder()
                            .nombreUsuario("admin")
                            .password(passwordEncoder.encode("admin123"))
                            .nombreEmpleado("Administrador Amaya")
                            .email("admin@amaya.com")
                            .estado(1)
                            .rol(rolAdmin)
                            .build()
            );
        }

        crearAlmacenSiNoExiste("Almacén Casa", "Primer piso de la vivienda familiar");
        crearAlmacenSiNoExiste("Almacén Unicachi", "Mercado Unicachi");
    }

    private Opcion crearOpcionSiNoExiste(String codigo, String nombre, int orden) {
        return opcionRepository.findByCodigo(codigo)
                .orElseGet(() -> opcionRepository.save(
                        Opcion.builder()
                                .codigo(codigo)
                                .nombre(nombre)
                                .orden(orden)
                                .estado(1)
                                .build()
                ));
    }

    private void crearRolOpcionSiNoExiste(Long idRol, Long idOpcion) {
        boolean existe = rolOpcionRepository.findByRolIdConOpcion(idRol).stream()
                .anyMatch(ro -> ro.getIdOpcion().equals(idOpcion));
        if (!existe) {
            rolOpcionRepository.save(
                    RolOpcion.builder()
                            .idRol(idRol)
                            .idOpcion(idOpcion)
                            .build()
            );
        }
    }

    private void crearAlmacenSiNoExiste(String nombre, String direccion) {
        boolean existe = almacenRepository.findByNombreIgnoreCase(nombre).isPresent();
        if (!existe) {
            almacenRepository.save(
                    Almacen.builder()
                            .nombre(nombre)
                            .direccion(direccion)
                            .estado(1)
                            .build()
            );
        }
    }
}