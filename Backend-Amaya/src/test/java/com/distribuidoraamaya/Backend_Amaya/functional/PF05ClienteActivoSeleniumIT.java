package com.distribuidoraamaya.Backend_Amaya.functional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

class PF05ClienteActivoSeleniumIT extends SeleniumTestBase {

    @Test
    void clienteActivo_puedeSeleccionarseEnLaVenta() throws IOException {
        WebDriverWait espera = prepararFormularioVenta();
        WebElement selector = espera.until(ExpectedConditions.presenceOfElementLocated(By.id("idCliente")));
        String opcion = seleccionarOpcionQueContiene(espera, selector, DNI_CLIENTE, "cliente");

        assertTrue(opcion.contains(DNI_CLIENTE));
        assertFalse(new Select(selector).getFirstSelectedOption().getAttribute("value").equals("0"));
        guardarCaptura("PF-05-cliente-activo.png");
    }
}
