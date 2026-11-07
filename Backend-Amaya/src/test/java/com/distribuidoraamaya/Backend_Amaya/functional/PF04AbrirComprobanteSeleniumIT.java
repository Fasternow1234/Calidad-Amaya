package com.distribuidoraamaya.Backend_Amaya.functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

class PF04AbrirComprobanteSeleniumIT extends SeleniumTestBase {

    @Test
    void nuevoComprobante_abreFormularioDeVenta() throws IOException {
        WebDriverWait espera = iniciarNavegador();
        iniciarSesion(espera);
        abrirNuevoComprobante(espera);

        Select tipoOperacion = new Select(driver.findElement(By.id("tipo")));
        tipoOperacion.selectByValue("VENTA");

        assertEquals("VENTA", tipoOperacion.getFirstSelectedOption().getAttribute("value"));
        assertTrue(driver.findElement(By.id("idCliente")).isDisplayed());
        guardarCaptura("PF-04-formulario-venta.png");
    }
}
