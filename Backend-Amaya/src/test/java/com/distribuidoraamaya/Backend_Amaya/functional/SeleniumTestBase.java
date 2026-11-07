package com.distribuidoraamaya.Backend_Amaya.functional;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
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

/** Configuracion y operaciones compartidas por las pruebas funcionales Selenium. */
abstract class SeleniumTestBase {

    private static final Pattern PATRON_STOCK = Pattern.compile("Stock:\\s*([0-9]+(?:[.,][0-9]+)?)");

    protected static final String URL_BASE = valor("selenium.baseUrl", "SELENIUM_BASE_URL", "http://localhost:4200");
    protected static final String USUARIO = valor("selenium.usuario", "SELENIUM_USUARIO", "admin");
    protected static final String PASSWORD = valor("selenium.password", "SELENIUM_PASSWORD", "admin123");
    protected static final String DNI_CLIENTE = valor("selenium.clienteDni", "SELENIUM_CLIENTE_DNI", "98645128");
    protected static final String PRODUCTO = valor("selenium.producto", "SELENIUM_PRODUCTO", "Ariel");
    protected static final String STOCK_ESPERADO = valor("selenium.stockEsperado", "SELENIUM_STOCK_ESPERADO", "6");
    protected static final BigDecimal PRECIO_ESPERADO = new BigDecimal(
            valor("selenium.precioEsperado", "SELENIUM_PRECIO_ESPERADO", "5.00"));
    private static final long PAUSA_DEMOSTRACION_MS = Long.parseLong(
            valor("selenium.pausaMs", "SELENIUM_PAUSA_MS", "3000"));

    protected WebDriver driver;

    @AfterEach
    void cerrarNavegador() {
        if (driver != null) {
            pausarDemostracion();
            driver.quit();
        }
    }

    protected WebDriverWait iniciarNavegador() {
        assertFalse(PASSWORD.isBlank(),
                "Define SELENIUM_PASSWORD antes de ejecutar; la contrasena no se guarda en el codigo.");
        EdgeOptions opciones = new EdgeOptions();
        opciones.addArguments("--start-maximized");
        driver = new EdgeDriver(opciones);
        return new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    protected void iniciarSesion(WebDriverWait espera) {
        driver.get(URL_BASE + "/login");
        espera.until(ExpectedConditions.visibilityOfElementLocated(By.id("nombreUsuario")));
        escribir(By.id("nombreUsuario"), USUARIO);
        escribir(By.id("password"), PASSWORD);
        driver.findElement(By.cssSelector("form button[type='submit']")).click();
        espera.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                "//button[contains(@class,'nav-item')][.//span[normalize-space()='Comprobantes']]")));
    }

    protected WebElement menu(String nombre) {
        return driver.findElement(By.xpath(
                "//button[contains(@class,'nav-item')][.//span[normalize-space()='" + nombre + "']]"));
    }

    protected void abrirNuevoComprobante(WebDriverWait espera) {
        espera.until(ExpectedConditions.elementToBeClickable(menu("Comprobantes"))).click();
        espera.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//button[contains(@class,'btn-primary')][contains(normalize-space(.),'Nuevo comprobante')]"))).click();
        espera.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("app-nuevo-comprobante")));
    }

    protected WebDriverWait prepararFormularioVenta() {
        WebDriverWait espera = iniciarNavegador();
        iniciarSesion(espera);
        abrirNuevoComprobante(espera);
        new Select(driver.findElement(By.id("tipo"))).selectByValue("VENTA");
        return espera;
    }

    protected WebElement lineaPrincipal() {
        return driver.findElement(By.cssSelector("app-nuevo-comprobante .linea-detalle"));
    }

    protected String seleccionarProducto(WebDriverWait espera, WebElement linea) {
        WebElement selector = linea.findElement(By.cssSelector("select[formControlName='idProducto']"));
        return seleccionarOpcionQueContiene(espera, selector, PRODUCTO, "producto");
    }

    protected BigDecimal stockDeOpcion(String textoOpcion) {
        Matcher matcher = PATRON_STOCK.matcher(textoOpcion);
        assertTrue(matcher.find(), "No se encontro el stock en la opcion: " + textoOpcion);
        return new BigDecimal(matcher.group(1).replace(',', '.'));
    }

    protected void escribir(By localizador, String texto) {
        escribir(driver.findElement(localizador), texto);
    }

    protected void escribir(WebElement campo, String texto) {
        campo.clear();
        campo.sendKeys(texto);
    }

    protected BigDecimal decimalDeCelda(WebElement celda) {
        return new BigDecimal(celda.getText().trim().replace(',', '.'));
    }

    protected String seleccionarOpcionQueContiene(
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

    protected void guardarCaptura(String nombreArchivo) throws IOException {
        if (!(driver instanceof TakesScreenshot captura)) {
            return;
        }
        Path directorio = Path.of("target", "selenium-evidencias");
        Files.createDirectories(directorio);
        Path destino = directorio.resolve(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-")) + nombreArchivo);
        Files.copy(captura.getScreenshotAs(OutputType.FILE).toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
    }

    protected void pausarDemostracion() {
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
