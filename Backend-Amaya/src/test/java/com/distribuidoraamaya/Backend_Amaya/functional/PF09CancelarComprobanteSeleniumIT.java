package com.distribuidoraamaya.Backend_Amaya.functional;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

class PF09CancelarComprobanteSeleniumIT extends SeleniumTestBase {

    @Test
    void cancelarNuevoComprobante_cierraSinRegistrar() throws IOException {
        WebDriverWait espera = prepararFormularioVenta();
        driver.findElement(By.xpath(
                "//app-nuevo-comprobante//button[@type='button'][normalize-space()='Cancelar']"))
                .click();
        espera.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("app-nuevo-comprobante")));

        assertTrue(driver.findElements(By.cssSelector("app-nuevo-comprobante")).isEmpty());
        guardarCaptura("PF-09-cancelar-comprobante.png");
    }
}
