package com.demo.steps;

import com.demo.utils.Utils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class MyStepDefinitions {

    // "I agree"/"Accept all" en el dialogo de consentimiento de cookies que Google
    // muestra en la primera visita desde ciertas regiones (frecuente en runners de
    // CI). El id L2AGLb es el que Google usa para ese boton desde hace anios.
    private static final By CONSENT_ACCEPT_BUTTON = By.id("L2AGLb");
    private static final By RESULT_STATS = By.id("result-stats");

    private WebDriver driver;

    @Given("ir al navegador de Google Chrome")
    public void iAmOnGoogleSearchPage() {
        ChromeOptions options = new ChromeOptions();
        // Este sandbox de ejecucion no tiene Chrome/Chromium en una ruta estandar
        // del sistema, asi que se permite fijar el binario via system property.
        // Fuera de este sandbox (dev local, CI) esta property no se define y
        // Selenium Manager (incluido en selenium-java) detecta el navegador
        // instalado automaticamente. El driver binario (chromedriver) tampoco se
        // checkea en el repo: Selenium Manager lo descarga y cachea por version,
        // igual en local que en CI, evitando el mismatch de versiones que exigia
        // mantener un binario a mano.
        String chromeBinary = System.getProperty("chrome.binary.path");
        if (chromeBinary != null && !chromeBinary.isBlank()) {
            options.setBinary(chromeBinary);
        }
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        driver.get("https://www.google.com");
        aceptarConsentimientoSiAparece();
    }

    @When("digitar la palabra {string} en el buscador")
    public void iSearchFor(String busqueda) {
        driver.findElement(By.name("q")).sendKeys(busqueda);
        driver.findElement(By.name("q")).submit();
    }

    @When("dar enter para que se inicie la busqueda")
    public void enter() {
        Actions actions = new Actions(driver);
        actions.sendKeys(Keys.ENTER).perform();
    }

    @Then("validar que el total de resultados de consulta sea diferente a cero")
    public void verificarResultadosNoCero() {
        assertNotEquals(0, obtenerTotalResultados());
        driver.quit();
    }

    @Then("validar que el título de la página contenga la palabra {string}")
    public void verificarTituloContienePalabra(String palabra) throws InterruptedException {
        Thread.sleep(1000);
        String titulo = driver.getTitle();
        guardarScreenshot("busqueda-" + palabra.toLowerCase());
        assertTrue(
                "Se esperaba que el título '" + titulo + "' contuviera '" + palabra + "'",
                titulo.toLowerCase().contains(palabra.toLowerCase())
        );
        driver.quit();
    }

    @Then("el campo de búsqueda debe estar visible")
    public void verificarCampoBusquedaVisible() {
        WebElement campoBusqueda = driver.findElement(By.name("q"));
        assertTrue("Se esperaba que el campo de búsqueda fuera visible", campoBusqueda.isDisplayed());
        driver.quit();
    }

    @Then("el título de la página de inicio debe ser {string}")
    public void verificarTituloPaginaInicial(String tituloEsperado) {
        assertEquals(tituloEsperado, driver.getTitle());
        driver.quit();
    }

    @When("escribir {string} en el campo de búsqueda sin enviar")
    public void escribirSinEnviar(String texto) {
        driver.findElement(By.name("q")).sendKeys(texto);
    }

    @Then("el campo de búsqueda debe contener el texto {string}")
    public void verificarCampoBusquedaContieneTexto(String textoEsperado) {
        String valorActual = driver.findElement(By.name("q")).getAttribute("value");
        assertEquals(textoEsperado, valorActual);
        driver.quit();
    }

    private void aceptarConsentimientoSiAparece() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.elementToBeClickable(CONSENT_ACCEPT_BUTTON))
                    .click();
        } catch (TimeoutException e) {
            // No aparecio el dialogo de consentimiento en esta region/sesion; se continua normal.
        }
    }

    private int obtenerTotalResultados() {
        try {
            WebElement resultsStats = new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.presenceOfElementLocated(RESULT_STATS));
            return Utils.extractTotalResults(resultsStats.getText());
        } catch (TimeoutException e) {
            // Google no siempre renderiza el texto "About X results" (id result-stats)
            // con ese id exacto; como respaldo se cuentan los resultados organicos en
            // el contenedor principal de resultados (id "search"), mas estable en el
            // markup de Google que el widget de conteo.
            //
            // NOTA: en runners de GitHub Actions esto tambien devuelve 0 porque Google
            // le muestra un reCAPTCHA a las IPs compartidas de GitHub Actions en vez de
            // resultados (confirmado inspeccionando el HTML de la pagina en CI) — un
            // bloqueo anti-bot deterministico, no un problema de selector. Ver
            // CLAUDE.md/README: por eso este job de CI corre con continue-on-error.
            return driver.findElements(By.cssSelector("#search a h3")).size();
        }
    }

    private void guardarScreenshot(String nombre) {
        try {
            File origen = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path destino = Path.of("target/screenshots/" + nombre + ".png");
            Files.createDirectories(destino.getParent());
            Files.copy(origen.toPath(), destino, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // Una captura fallida no debe tumbar el escenario; solo se pierde la evidencia.
            System.err.println("No se pudo guardar el screenshot: " + e.getMessage());
        }
    }
}
