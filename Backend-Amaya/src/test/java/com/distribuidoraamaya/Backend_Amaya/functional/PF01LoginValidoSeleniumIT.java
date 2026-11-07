package com.distribuidoraamaya.Backend_Amaya.functional;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

class PF01LoginValidoSeleniumIT extends SeleniumTestBase {

    @Test
    void loginValido_muestraPanelPrincipal() throws IOException {
        WebDriverWait espera = iniciarNavegador();
        iniciarSesion(espera);

        WebElement menuComprobantes = espera.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                "//button[contains(@class,'nav-item')][.//span[normalize-space()='Comprobantes']]")));
        assertTrue(menuComprobantes.isDisplayed(), "Debe mostrarse el menu principal.");
        guardarCaptura("PF-01-login-valido.png");
    }
}
