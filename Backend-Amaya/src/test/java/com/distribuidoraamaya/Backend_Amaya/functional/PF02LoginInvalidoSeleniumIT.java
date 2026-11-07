package com.distribuidoraamaya.Backend_Amaya.functional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

class PF02LoginInvalidoSeleniumIT extends SeleniumTestBase {

    @Test
    void loginInvalido_muestraMensajeYNoIngresa() throws IOException {
        WebDriverWait espera = iniciarNavegador();
        driver.get(URL_BASE + "/login");
        espera.until(ExpectedConditions.visibilityOfElementLocated(By.id("nombreUsuario")));
        escribir(By.id("nombreUsuario"), USUARIO);
        escribir(By.id("password"), PASSWORD + "-incorrecta");
        driver.findElement(By.cssSelector("form button[type='submit']")).click();

        WebElement mensaje = espera.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("app-login .alert-error, .login-wrapper .alert-error")));
        assertFalse(mensaje.getText().isBlank(), "Debe informarse que las credenciales no son validas.");
        assertTrue(driver.getCurrentUrl().contains("/login"), "El usuario debe permanecer en el login.");
        guardarCaptura("PF-02-login-invalido.png");
    }
}
