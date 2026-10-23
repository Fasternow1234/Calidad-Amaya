package com.distribuidoraamaya.Backend_Amaya.functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Prueba funcional de HU-08. Requiere frontend, backend y MySQL en ejecucion.
 *
 * Se llama IT para evitar que Maven la ejecute junto con las pruebas unitarias.
 * La ejecucion registra una venta real en el ambiente configurado y descuenta
 * una unidad del stock mediante el flujo normal de la aplicacion.
 */
class RegistroComprobanteVentaSeleniumIT {

    private static final String URL_BASE = valor("selenium.baseUrl", "SELENIUM_BASE_URL", "http://localhost:4200");
    private static final String USUARIO = valor("selenium.usuario", "SELENIUM_USUARIO", "admin");
    private static final String PASSWORD = valor("selenium.password", "SELENIUM_PASSWORD", "");
    private static final String DNI_CLIENTE = valor("selenium.clienteDni", "SELENIUM_CLIENTE_DNI", "98645128");
    private static final String PRODUCTO = valor("selenium.producto", "SELENIUM_PRODUCTO", "Ariel");
    private static final String STOCK_ESPERADO = valor("selenium.stockEsperado", "SELENIUM_STOCK_ESPERADO", "10");
    private static final BigDecimal PRECIO_ESPERADO = new BigDecimal(
            valor("selenium.precioEsperado", "SELENIUM_PRECIO_ESPERADO", "5.00"));
    private static final long PAUSA_DEMOSTRACION_MS = Long.parseLong(
            valor("selenium.pausaMs", "SELENIUM_PAUSA_MS", "0"));

    private WebDriver driver;

    @AfterEach
    void cerrarNavegador() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void registrarVenta_deUnaUnidad_muestraConfirmacionYComprobante() throws IOException {
        // Preparar
        assertFalse(PASSWORD.isBlank(),
                "Define SELENIUM_PASSWORD antes de ejecutar; la contrasena no se guarda en el codigo.");

        EdgeOptions opciones = new EdgeOptions();
        opciones.addArguments("--start-maximized");
        driver = new EdgeDriver(opciones);
        WebDriverWait espera = new WebDriverWait(driver, Duration.ofSeconds(20));
        String correlativo = String.format("%08d", System.currentTimeMillis() % 100_000_000L);
        String numeroEsperado = "B001-" + correlativo;

        try {
            // Ejecutar: iniciar sesion y abrir el formulario de comprobantes.
            driver.get(URL_BASE + "/login");
            espera.until(ExpectedConditions.visibilityOfElementLocated(By.id("nombreUsuario")));
            espera.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
            escribir(By.id("nombreUsuario"), USUARIO);
            escribir(By.id("password"), PASSWORD);
            driver.findElement(By.cssSelector("form button[type='submit']")).click();

            WebElement menuComprobantes = espera.until(ExpectedConditions.elementToBeClickable(By.xpath(
                    "//button[contains(@class,'nav-item')][.//span[normalize-space()='Comprobantes']]")));
            pausarDemostracion();
            menuComprobantes.click();

            espera.until(ExpectedConditions.elementToBeClickable(By.xpath(
                    "//button[contains(@class,'btn-primary')][contains(normalize-space(.),'Nuevo comprobante')]"))).click();
            espera.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("app-nuevo-comprobante")));
            pausarDemostracion();

            // Ejecutar: completar la venta con los datos controlados.
            new Select(driver.findElement(By.id("tipo"))).selectByValue("VENTA");
            espera.until(ExpectedConditions.elementToBeClickable(By.xpath(
                    "//app-nuevo-comprobante//button[contains(@class,'segmento-btn')][.//strong[normalize-space()='Boleta']]")))
                    .click();
            escribir(By.id("correlativo"), correlativo);

            WebElement selectorCliente = espera.until(ExpectedConditions.presenceOfElementLocated(By.id("idCliente")));
            seleccionarOpcionQueContiene(espera, selectorCliente, DNI_CLIENTE, "cliente");

            WebElement linea = driver.findElement(By.cssSelector(".linea-detalle"));
            WebElement selectorProducto = linea.findElement(By.cssSelector("select[formControlName='idProducto']"));
            String textoProducto = seleccionarOpcionQueContiene(espera, selectorProducto, PRODUCTO, "producto");
            assertTrue(textoProducto.contains("Stock: " + STOCK_ESPERADO),
                    "El producto no tiene el stock inicial esperado. Opcion encontrada: " + textoProducto);

            WebElement cantidad = linea.findElement(By.cssSelector("input[formControlName='cantidad']"));
            cantidad.clear();
            cantidad.sendKeys("1");

            WebElement precio = linea.findElement(By.cssSelector("input[formControlName='precioUnitario']"));
            espera.until(d -> !precio.getAttribute("value").isBlank());
            assertEquals(0, PRECIO_ESPERADO.compareTo(new BigDecimal(precio.getAttribute("value"))),
                    "El precio cargado para el producto no coincide con el dato de prueba.");

            WebElement total = driver.findElement(By.cssSelector(".total-linea"));
            espera.until(ExpectedConditions.textToBePresentInElement(total, "5.00"));
            assertTrue(total.getText().contains("S/ 5.00"), "El total mostrado debe ser S/ 5.00.");
            pausarDemostracion();

            driver.findElement(By.xpath(
                    "//app-nuevo-comprobante//button[@type='submit'][contains(normalize-space(.),'Registrar comprobante')]"))
                    .click();

            // Comprobar: mensaje de exito y numero nuevo visible en el listado.
            WebElement mensaje = espera.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".toast.toast-success .toast-mensaje")));
            assertEquals("Comprobante registrado correctamente", mensaje.getText().trim());
            espera.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), numeroEsperado));

            guardarCaptura("venta-aprobada-" + numeroEsperado.replace('-', '_') + ".png");
            pausarDemostracion();
        } catch (RuntimeException | AssertionError error) {
            guardarCaptura("venta-fallida.png");
            throw error;
        }
    }

    private void escribir(By localizador, String texto) {
        WebElement campo = driver.findElement(localizador);
        campo.clear();
        campo.sendKeys(texto);
    }

    private String seleccionarOpcionQueContiene(
            WebDriverWait espera, WebElement selector, String textoBuscado, String tipoDato) {
        espera.until(d -> new Select(selector).getOptions().stream()
                .anyMatch(opcion -> opcion.getText().contains(textoBuscado)));

        Select select = new Select(selector);
        WebElement opcion = select.getOptions().stream()
                .filter(elemento -> elemento.getText().contains(textoBuscado))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se encontro el " + tipoDato + ": " + textoBuscado));
        String textoCompleto = opcion.getText();
        select.selectByVisibleText(textoCompleto);
        return textoCompleto;
    }

    private void guardarCaptura(String nombreArchivo) throws IOException {
        if (!(driver instanceof TakesScreenshot captura)) {
            return;
        }
        Path directorio = Path.of("target", "selenium-evidencias");
        Files.createDirectories(directorio);
        Path destino = directorio.resolve(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-")) + nombreArchivo);
        Files.copy(captura.getScreenshotAs(OutputType.FILE).toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
    }

    private void pausarDemostracion() {
        if (PAUSA_DEMOSTRACION_MS <= 0) {
            return;
        }
        try {
            Thread.sleep(PAUSA_DEMOSTRACION_MS);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("La pausa de demostracion fue interrumpida.", error);
        }
    }

    private static String valor(String propiedad, String variableEntorno, String valorPredeterminado) {
        String desdePropiedad = System.getProperty(propiedad);
        if (desdePropiedad != null && !desdePropiedad.isBlank()) {
            return desdePropiedad;
        }
        String desdeEntorno = System.getenv(variableEntorno);
        return desdeEntorno == null || desdeEntorno.isBlank() ? valorPredeterminado : desdeEntorno;
    }
}
