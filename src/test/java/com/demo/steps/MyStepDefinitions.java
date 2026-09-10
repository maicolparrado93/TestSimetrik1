package com.demo.steps;

import com.demo.utils.Utils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class MyStepDefinitions {

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
    public void verificarResultadosNoCero() throws InterruptedException {
        // Encontrar el elemento que muestra el total de resultados
        Thread.sleep(1000);
        WebElement resultsStats = driver.findElement(By.id("result-stats"));

        // Obtener el texto y extraer el número de resultados
        String resultsText = resultsStats.getText();
        int totalResults = Utils.extractTotalResults(resultsText);

        // Verificar que el total de resultados no sea cero
        assertNotEquals(0, totalResults);

        // Cerrar el navegador
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
