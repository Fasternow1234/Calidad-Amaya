package com.distribuidoraamaya.Backend_Amaya.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DetalleComprobanteRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void crearValidador() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void cerrarValidador() {
        validatorFactory.close();
    }

    @Test
    void aceptaCantidadEnteraPositiva() {
        assertTrue(validator.validate(solicitudConCantidad("2")).isEmpty());
    }

    @Test
    void aceptaEnteroConCerosDecimalesSinTratarloComoFraccion() {
        assertTrue(validator.validate(solicitudConCantidad("2.0")).isEmpty());
    }

    @Test
    void rechazaCantidadFraccionaria() {
        assertFalse(validator.validate(solicitudConCantidad("1.5")).isEmpty());
    }

    @Test
    void rechazaCantidadCero() {
        assertFalse(validator.validate(solicitudConCantidad("0")).isEmpty());
    }

    @Test
    void rechazaCantidadNegativa() {
        assertFalse(validator.validate(solicitudConCantidad("-1")).isEmpty());
    }

    @Test
    void rechazaCantidadQueSuperaLaPrecisionPermitida() {
        assertFalse(validator.validate(solicitudConCantidad("10000000000")).isEmpty());
    }

    @Test
    void rechazaCantidadAusente() {
        assertFalse(validator.validate(solicitudConCantidad(null)).isEmpty());
    }

    private static DetalleComprobanteRequest solicitudConCantidad(String cantidad) {
        DetalleComprobanteRequest request = new DetalleComprobanteRequest();
        request.setIdProducto(1L);
        request.setCantidad(cantidad == null ? null : new BigDecimal(cantidad));
        request.setPrecioUnitario(new BigDecimal("8.70"));
        return request;
    }
}
