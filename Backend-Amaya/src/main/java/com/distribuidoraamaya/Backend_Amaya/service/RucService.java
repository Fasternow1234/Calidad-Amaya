package com.distribuidoraamaya.Backend_Amaya.service;

import com.distribuidoraamaya.Backend_Amaya.dto.response.RucResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Service
@Slf4j
public class RucService {

    private static final String BASE_URL = "https://api.decolecta.com";

    @Value("${app.apis.token}")
    private String token;

    private final RestClient restClient;

    public RucService() {
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .build();
    }

    public RucResponse consultar(String ruc) {
        if (ruc == null || !ruc.matches("^(10|15|17|20)\\d{9}$")) {
            throw new IllegalArgumentException("El RUC debe tener 11 dígitos y empezar con 10, 15, 17 o 20");
        }
        try {
            Map<String, Object> json = restClient.get()
                    .uri("/v1/sunat/ruc?numero={ruc}", ruc)
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            log.info("Respuesta Decolecta para RUC {}: {}", ruc, json);

            if (json == null || !json.containsKey("razon_social")) {
                throw new IllegalArgumentException("No se encontraron datos para el RUC proporcionado.");
            }

            return RucResponse.builder()
                    .ruc(texto(json.get("numero_documento")))
                    .razonSocial(texto(json.get("razon_social")))
                    .nombreComercial(texto(json.get("nombre_comercial")))
                    .direccion(texto(json.get("direccion")))
                    .estado(texto(json.get("estado")))
                    .condicion(texto(json.get("condicion")))
                    .build();
        } catch (RestClientResponseException e) {
            log.error("Error HTTP de la API. Status: {} | Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalArgumentException("Error de la API (" + e.getStatusCode() + "): " + e.getResponseBodyAsString());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error consultando RUC {}: {}", ruc, e.getMessage(), e);
            throw new IllegalArgumentException("No se pudo consultar el RUC: " + e.getMessage());
        }
    }

    private String texto(Object valor) {
        if (valor == null) return null;
        String t = valor.toString().trim();
        return t.isEmpty() || t.equalsIgnoreCase("null") ? null : t;
    }
}