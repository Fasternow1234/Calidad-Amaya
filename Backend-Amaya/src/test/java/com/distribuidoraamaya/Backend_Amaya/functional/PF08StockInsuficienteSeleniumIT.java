package com.distribuidoraamaya.Backend_Amaya.functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

class PF08StockInsuficienteSeleniumIT extends SeleniumTestBase {

    @Test
    void stockInsuficiente_muestraValidacionYNoRegistra() throws IOException {
        WebDriverWait espera = prepararFormularioVenta();
        WebElement selectorCliente = espera.until(ExpectedConditions.presenceOfElementLocated(By.id("idCliente")));
        seleccionarOpcionQueContiene(espera, selectorCliente, DNI_CLIENTE, "cliente");

        WebElement linea = lineaPrincipal();
        BigDecimal stock = stockDeOpcion(seleccionarProducto(espera, linea));
        escribir(linea.findElement(By.cssSelector("input[formControlName='cantidad']")),
                stock.add(BigDecimal.ONE).stripTrailingZeros().toPlainString());

        WebElement validacion = espera.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                "//app-nuevo-comprobante//small[@role='alert'][contains(normalize-space(.),'Excede el stock')]")));
        assertEquals("Excede el stock disponible", validacion.getText().trim());

        driver.findElement(By.xpath(
                "//app-nuevo-comprobante//button[@type='submit'][contains(normalize-space(.),'Registrar comprobante')]"))
                .click();
        WebElement mensaje = espera.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("app-nuevo-comprobante .alert-error")));
        assertTrue(mensaje.getText().contains("Stock insuficiente"));
        guardarCaptura("PF-08-stock-insuficiente.png");
    }
}
