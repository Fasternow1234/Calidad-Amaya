package com.distribuidoraamaya.Backend_Amaya.service;

import com.distribuidoraamaya.Backend_Amaya.dto.response.DniResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Service
@Slf4j
public class DniService {

    private static final String BASE_URL = "https://api.decolecta.com";

    @Value("${app.apis.token}")
    private String token;

    private final RestClient restClient;

    public DniService() {
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .build();
    }

    public DniResponse consultar(String dni) {
        if (dni == null || !dni.matches("^\\d{8}$")) {
            throw new IllegalArgumentException("El DNI debe tener 8 dígitos numéricos");
        }

        try {
            Map<String, Object> json = restClient.get()
                    .uri("/v1/reniec/dni?numero={dni}", dni)
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            log.info("Respuesta Decolecta RENIEC para DNI {}: {}", dni, json);

            if (json == null) {
                throw new IllegalArgumentException("No se encontraron datos para el DNI proporcionado.");
            }

            String nombres = primeraNoNula(json, "first_name", "nombres");
            String apellidoPaterno = primeraNoNula(json, "first_last_name", "apellido_paterno");
            String apellidoMaterno = primeraNoNula(json, "second_last_name", "apellido_materno");
            String nombreCompleto = primeraNoNula(json, "full_name", "nombre_completo");

            if (nombres == null && apellidoPaterno == null && nombreCompleto == null) {
                throw new IllegalArgumentException("No se encontraron datos para el DNI proporcionado.");
            }

            if (nombreCompleto == null) {
                StringBuilder sb = new StringBuilder();
                if (nombres != null) sb.append(nombres);
                if (apellidoPaterno != null) {
                    if (sb.length() > 0) sb.append(' ');
                    sb.append(apellidoPaterno);
                }
                if (apellidoMaterno != null) {
                    if (sb.length() > 0) sb.append(' ');
                    sb.append(apellidoMaterno);
                }
                nombreCompleto = sb.toString();
            }

            return DniResponse.builder()
                    .numero(primeraNoNula(json, "document_number", "numero"))
                    .nombres(nombres)
                    .apellidoPaterno(apellidoPaterno)
                    .apellidoMaterno(apellidoMaterno)
                    .nombreCompleto(nombreCompleto)
                    .build();

        } catch (RestClientResponseException e) {
            log.error("Error HTTP de la API RENIEC. Status: {} | Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalArgumentException("Error de la API (" + e.getStatusCode() + "): "
                    + e.getResponseBodyAsString());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error consultando DNI {}: {}", dni, e.getMessage(), e);
            throw new IllegalArgumentException("No se pudo consultar el DNI: " + e.getMessage());
        }
    }

    private String primeraNoNula(Map<String, Object> json, String... keys) {
        for (String k : keys) {
            String v = texto(json.get(k));
            if (v != null) return v;
        }
        return null;
    }

    private String texto(Object valor) {
        if (valor == null) return null;
        String t = valor.toString().trim();
        return t.isEmpty() || t.equalsIgnoreCase("null") ? null : t;
    }
}