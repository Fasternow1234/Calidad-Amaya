package com.distribuidoraamaya.Backend_Amaya.functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

class PF10RegistrarVentaSeleniumIT extends SeleniumTestBase {

    @Test
    void registrarVenta_deUnaUnidad_generaSalidaYReduceStock() throws IOException {
        WebDriverWait espera = iniciarNavegador();
        String correlativo = String.format("%08d", System.currentTimeMillis() % 100_000_000L);
        String numeroEsperado = "B001-" + correlativo;

        try {
            iniciarSesion(espera);
            abrirNuevoComprobante(espera);
            new Select(driver.findElement(By.id("tipo"))).selectByValue("VENTA");
            espera.until(ExpectedConditions.elementToBeClickable(By.xpath(
                    "//app-nuevo-comprobante//button[contains(@class,'segmento-btn')][.//strong[normalize-space()='Boleta']]")))
                    .click();
            escribir(By.id("correlativo"), correlativo);

            WebElement selectorCliente = espera.until(ExpectedConditions.presenceOfElementLocated(By.id("idCliente")));
            seleccionarOpcionQueContiene(espera, selectorCliente, DNI_CLIENTE, "cliente");

            WebElement linea = lineaPrincipal();
            WebElement selectorProducto = linea.findElement(By.cssSelector("select[formControlName='idProducto']"));
            String textoProducto = seleccionarOpcionQueContiene(espera, selectorProducto, PRODUCTO, "producto");
            assertTrue(textoProducto.contains("Stock: " + STOCK_ESPERADO),
                    "El producto no tiene el stock inicial esperado: " + textoProducto);

            escribir(linea.findElement(By.cssSelector("input[formControlName='cantidad']")), "1");
            WebElement precio = linea.findElement(By.cssSelector("input[formControlName='precioUnitario']"));
            espera.until(d -> !precio.getAttribute("value").isBlank());
            assertEquals(0, PRECIO_ESPERADO.compareTo(new BigDecimal(precio.getAttribute("value"))));

            WebElement total = driver.findElement(By.cssSelector(".total-linea"));
            espera.until(ExpectedConditions.textToBePresentInElement(total, PRECIO_ESPERADO.toPlainString()));

            driver.findElement(By.xpath(
                    "//app-nuevo-comprobante//button[@type='submit'][contains(normalize-space(.),'Registrar comprobante')]"))
                    .click();

            WebElement mensaje = espera.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".toast.toast-success .toast-mensaje")));
            assertEquals("Comprobante registrado correctamente", mensaje.getText().trim());
            espera.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), numeroEsperado));

            espera.until(ExpectedConditions.elementToBeClickable(menu("Movimientos"))).click();
            espera.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("app-movimientos")));
            WebElement buscador = espera.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("app-movimientos input.buscador-input")));
            escribir(buscador, numeroEsperado);

            WebElement fila = espera.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("app-movimientos table.tabla tbody tr")));
            assertEquals(PRODUCTO, fila.findElement(By.cssSelector("td.nombre-producto")).getText().trim());
            assertEquals("Salida", fila.findElement(By.cssSelector("td.col-tipo")).getText().trim());

            List<WebElement> columnasStock = fila.findElements(By.cssSelector("td.col-stock"));
            assertEquals(2, columnasStock.size());
            BigDecimal stockInicial = new BigDecimal(STOCK_ESPERADO);
            assertEquals(0, stockInicial.compareTo(decimalDeCelda(columnasStock.get(0))));
            assertEquals(0, stockInicial.subtract(BigDecimal.ONE).compareTo(decimalDeCelda(columnasStock.get(1))));

            guardarCaptura("PF-10-venta-y-salida-" + numeroEsperado.replace('-', '_') + ".png");
        } catch (RuntimeException | AssertionError error) {
            guardarCaptura("PF-10-venta-fallida.png");
            throw error;
        }
    }
}
