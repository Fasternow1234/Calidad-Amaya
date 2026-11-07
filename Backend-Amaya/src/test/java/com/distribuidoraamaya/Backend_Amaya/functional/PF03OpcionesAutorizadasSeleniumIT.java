package com.distribuidoraamaya.Backend_Amaya.functional;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.WebDriverWait;

class PF03OpcionesAutorizadasSeleniumIT extends SeleniumTestBase {

    @Test
    void usuarioAutenticado_visualizaOpcionesAutorizadas() throws IOException {
        WebDriverWait espera = iniciarNavegador();
        iniciarSesion(espera);

        assertTrue(menu("Comprobantes").isDisplayed());
        assertTrue(menu("Movimientos").isDisplayed());
        assertTrue(menu("Clientes").isDisplayed());
        guardarCaptura("PF-03-opciones-autorizadas.png");
    }
}
