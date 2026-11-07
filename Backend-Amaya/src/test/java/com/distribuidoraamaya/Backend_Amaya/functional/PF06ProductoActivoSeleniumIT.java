package com.distribuidoraamaya.Backend_Amaya.functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

class PF06ProductoActivoSeleniumIT extends SeleniumTestBase {

    @Test
    void productoActivo_muestraStockYPrecioDeVenta() throws IOException {
        WebDriverWait espera = prepararFormularioVenta();
        WebElement linea = lineaPrincipal();
        String opcion = seleccionarProducto(espera, linea);

        assertTrue(stockDeOpcion(opcion).compareTo(BigDecimal.ZERO) > 0);
        WebElement precio = linea.findElement(By.cssSelector("input[formControlName='precioUnitario']"));
        espera.until(d -> !precio.getAttribute("value").isBlank());
        assertEquals(0, PRECIO_ESPERADO.compareTo(new BigDecimal(precio.getAttribute("value"))));
        guardarCaptura("PF-06-producto-stock-precio.png");
    }
}
