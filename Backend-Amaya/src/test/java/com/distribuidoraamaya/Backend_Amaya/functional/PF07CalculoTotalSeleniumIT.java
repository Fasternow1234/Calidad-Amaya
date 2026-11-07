package com.distribuidoraamaya.Backend_Amaya.functional;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

class PF07CalculoTotalSeleniumIT extends SeleniumTestBase {

    @Test
    void cantidadDos_calculaTotalAutomaticamente() throws IOException {
        WebDriverWait espera = prepararFormularioVenta();
        WebElement linea = lineaPrincipal();
        String opcion = seleccionarProducto(espera, linea);
        assertTrue(stockDeOpcion(opcion).compareTo(new BigDecimal("2")) >= 0,
                "La prueba requiere al menos dos unidades disponibles.");

        escribir(linea.findElement(By.cssSelector("input[formControlName='cantidad']")), "2");
        BigDecimal totalEsperado = PRECIO_ESPERADO.multiply(new BigDecimal("2"));
        WebElement total = driver.findElement(By.cssSelector(".total-linea strong"));
        espera.until(d -> total.getText().contains(totalEsperado.toPlainString()));
        assertTrue(total.getText().contains(totalEsperado.toPlainString()));
        guardarCaptura("PF-07-calculo-total.png");
    }
}
