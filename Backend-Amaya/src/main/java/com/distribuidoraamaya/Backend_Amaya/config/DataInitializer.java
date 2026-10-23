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
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final OpcionRepository opcionRepository;
    private final RolOpcionRepository rolOpcionRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlmacenRepository almacenRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            RolRepository rolRepository,
            OpcionRepository opcionRepository,
            RolOpcionRepository rolOpcionRepository,
            UsuarioRepository usuarioRepository,
            AlmacenRepository almacenRepository,
            PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.opcionRepository = opcionRepository;
        this.rolOpcionRepository = rolOpcionRepository;
        this.usuarioRepository = usuarioRepository;
        this.almacenRepository = almacenRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
        Opcion usuarios     = crearOpcionSiNoExiste("usuarios",     "Usuarios",     9);
        Opcion kardex       = crearOpcionSiNoExiste("kardex",       "Kardex",      10);

        Rol rolAdmin = rolRepository.findByNombre("ADMINISTRADOR")
                .orElseGet(() -> rolRepository.save(
                        Rol.builder()
                                .nombre("ADMINISTRADOR")
                                .descripcion("Administrador del sistema")
                                .build()
                ));

        asignarOpciones(rolAdmin, inicio, proveedores, categorias, productos, almacenes,
                movimientos, comprobantes, clientes, usuarios, kardex);

        Rol rolAlmacenero = crearRolSiNoExiste("ALMACENERO", "Gestión de almacenes e inventario");
        asignarOpciones(rolAlmacenero, inicio, proveedores, categorias, productos, almacenes, movimientos, kardex);

        Rol rolVendedor = crearRolSiNoExiste("VENDEDOR", "Gestión de clientes y ventas");
        asignarOpciones(rolVendedor, inicio, productos, comprobantes, clientes);

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

    private Rol crearRolSiNoExiste(String nombre, String descripcion) {
        return rolRepository.findByNombre(nombre)
                .orElseGet(() -> rolRepository.save(Rol.builder()
                        .nombre(nombre)
                        .descripcion(descripcion)
                        .build()));
    }

    private void asignarOpciones(Rol rol, Opcion... opciones) {
        for (Opcion opcion : opciones) {
            crearRolOpcionSiNoExiste(rol.getId(), opcion.getId());
        }
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
