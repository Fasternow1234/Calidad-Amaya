package com.distribuidoraamaya.Backend_Amaya.service;

import com.distribuidoraamaya.Backend_Amaya.dto.request.ClienteRequest;
import com.distribuidoraamaya.Backend_Amaya.dto.response.ClienteResponse;
import com.distribuidoraamaya.Backend_Amaya.entity.Cliente;
import com.distribuidoraamaya.Backend_Amaya.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public List<ClienteResponse> listarActivos() {
        return clienteRepository.findByEstadoOrderByNumeroDocumentoAsc(1).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ClienteResponse> listarTodos() {
        return clienteRepository.findAllByOrderByNumeroDocumentoAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ClienteResponse> buscar(String q) {
        if (q == null || q.trim().isEmpty()) return listarActivos();
        return clienteRepository.buscar(q.trim()).stream()
                .map(this::toResponse)
                .toList();
    }

    public ClienteResponse obtener(Long id) {
        Cliente c = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        return toResponse(c);
    }

    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        String tipoDoc = request.getTipoDocumento().trim().toUpperCase();
        String numeroDoc = request.getNumeroDocumento().trim();

        validarDocumento(tipoDoc, numeroDoc);
        validarCamposSegunTipo(tipoDoc, request);

        if (clienteRepository.existsByNumeroDocumento(numeroDoc)) {
            throw new IllegalArgumentException("Ya existe un cliente con el documento " + numeroDoc);
        }

        Cliente cliente = Cliente.builder()
                .tipoDocumento(tipoDoc)
                .numeroDocumento(numeroDoc)
                .nombres(tipoDoc.equals("DNI") ? limpiar(request.getNombres()) : null)
                .apellidoPaterno(tipoDoc.equals("DNI") ? limpiar(request.getApellidoPaterno()) : null)
                .apellidoMaterno(tipoDoc.equals("DNI") ? limpiar(request.getApellidoMaterno()) : null)
                .razonSocial(tipoDoc.equals("RUC") ? limpiar(request.getRazonSocial()) : null)
                .nombreComercial(tipoDoc.equals("RUC") ? limpiar(request.getNombreComercial()) : null)
                .direccion(limpiar(request.getDireccion()))
                .telefono(limpiar(request.getTelefono()))
                .email(limpiar(request.getEmail()))
                .estado(1)
                .build();

        return toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        String tipoDoc = request.getTipoDocumento().trim().toUpperCase();
        String numeroDoc = request.getNumeroDocumento().trim();

        validarDocumento(tipoDoc, numeroDoc);
        validarCamposSegunTipo(tipoDoc, request);

        clienteRepository.findByNumeroDocumento(numeroDoc).ifPresent(existente -> {
            if (!existente.getId().equals(id)) {
                throw new IllegalArgumentException("Ya existe otro cliente con el documento " + numeroDoc);
            }
        });

        cliente.setTipoDocumento(tipoDoc);
        cliente.setNumeroDocumento(numeroDoc);
        cliente.setNombres(tipoDoc.equals("DNI") ? limpiar(request.getNombres()) : null);
        cliente.setApellidoPaterno(tipoDoc.equals("DNI") ? limpiar(request.getApellidoPaterno()) : null);
        cliente.setApellidoMaterno(tipoDoc.equals("DNI") ? limpiar(request.getApellidoMaterno()) : null);
        cliente.setRazonSocial(tipoDoc.equals("RUC") ? limpiar(request.getRazonSocial()) : null);
        cliente.setNombreComercial(tipoDoc.equals("RUC") ? limpiar(request.getNombreComercial()) : null);
        cliente.setDireccion(limpiar(request.getDireccion()));
        cliente.setTelefono(limpiar(request.getTelefono()));
        cliente.setEmail(limpiar(request.getEmail()));

        return toResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public void desactivar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        cliente.setEstado(0);
        clienteRepository.save(cliente);
    }

    @Transactional
    public ClienteResponse reactivar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        cliente.setEstado(1);
        return toResponse(clienteRepository.save(cliente));
    }

    // ---------- Validaciones ----------

    private void validarDocumento(String tipoDoc, String numeroDoc) {
        if (tipoDoc.equals("DNI")) {
            if (!numeroDoc.matches("^\\d{8}$")) {
                throw new IllegalArgumentException("El DNI debe tener 8 dígitos numéricos");
            }
        } else if (tipoDoc.equals("RUC")) {
            if (!numeroDoc.matches("^(10|20)\\d{9}$")) {
                throw new IllegalArgumentException("El RUC debe tener 11 dígitos y empezar con 10 o 20");
            }
        } else {
            throw new IllegalArgumentException("Tipo de documento inválido: " + tipoDoc);
        }
    }

    private void validarCamposSegunTipo(String tipoDoc, ClienteRequest request) {
        if (tipoDoc.equals("DNI")) {
            if (esVacio(request.getNombres())) {
                throw new IllegalArgumentException("Los nombres son obligatorios para DNI");
            }
            if (esVacio(request.getApellidoPaterno())) {
                throw new IllegalArgumentException("El apellido paterno es obligatorio para DNI");
            }
            if (esVacio(request.getApellidoMaterno())) {
                throw new IllegalArgumentException("El apellido materno es obligatorio para DNI");
            }
        } else if (tipoDoc.equals("RUC")) {
            if (esVacio(request.getRazonSocial())) {
                throw new IllegalArgumentException("La razón social es obligatoria para RUC");
            }
        }
    }

    private boolean esVacio(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String limpiar(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    // ---------- Mapping ----------

    private ClienteResponse toResponse(Cliente c) {
        return ClienteResponse.builder()
                .id(c.getId())
                .tipoDocumento(c.getTipoDocumento())
                .numeroDocumento(c.getNumeroDocumento())
                .nombres(c.getNombres())
                .apellidoPaterno(c.getApellidoPaterno())
                .apellidoMaterno(c.getApellidoMaterno())
                .razonSocial(c.getRazonSocial())
                .nombreComercial(c.getNombreComercial())
                .direccion(c.getDireccion())
                .telefono(c.getTelefono())
                .email(c.getEmail())
                .estado(c.getEstado())
                .fechaRegistro(c.getFechaRegistro())
                .nombreMostrar(construirNombreMostrar(c))
                .build();
    }

    private String construirNombreMostrar(Cliente c) {
        if ("RUC".equals(c.getTipoDocumento()) && c.getRazonSocial() != null) {
            return c.getRazonSocial();
        }
        StringBuilder sb = new StringBuilder();
        if (c.getNombres() != null) sb.append(c.getNombres());
        if (c.getApellidoPaterno() != null) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(c.getApellidoPaterno());
        }
        if (c.getApellidoMaterno() != null) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(c.getApellidoMaterno());
        }
        return sb.length() > 0 ? sb.toString() : c.getNumeroDocumento();
    }
}