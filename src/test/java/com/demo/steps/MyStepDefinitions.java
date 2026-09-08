package com.demo.steps;

import com.demo.utils.Utils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

import static org.junit.Assert.assertNotEquals;

public class MyStepDefinitions {

    private WebDriver driver;

    @Given("ir al navegador de Google Chrome")
    public void iAmOnGoogleSearchPage() {
        // DESVIACION DELIBERADA del patron documentado en CLAUDE.md (Firefox +
        // geckodriver). Este step normalmente lanza FirefoxDriver apuntando al
        // geckodriver checked-in en src/test/java/com/demo/drivers/geckodriver.
        // En este sandbox de ejecucion no existe forma de obtener un Firefox real:
        // no hay apt/snap funcional (snapd no corre en el contenedor) y todos los
        // dominios de descarga de Mozilla (download.mozilla.org, ftp.mozilla.org,
        // archive.mozilla.org, download-installer.cdn.mozilla.net) estan bloqueados
        // por la politica de egress del proxy corporativo (403, no reintentable).
        // El geckodriver checked-in SI se reemplazo por un binario real linux64
        // (v0.35.0, descargado de github.com/mozilla/geckodriver), pero sin un
        // Firefox real no sirve de nada.
        // Como plan B se usa Chromium (binario real ya presente en la imagen base
        // en /opt/pw-browsers/chromium, version 141.0.7390.37) junto con un
        // chromedriver real de Linux de esa MISMA version exacta, descargado desde
        // Chrome for Testing (storage.googleapis.com) y checked-in en
        // src/test/java/com/demo/drivers/chromedriver. Se corre en modo headless
        // porque el sandbox no tiene display X11 real.
        System.setProperty("webdriver.chrome.driver", "src/test/java/com/demo/drivers/chromedriver");

        ChromeOptions options = new ChromeOptions();
        // Ruta del binario de Chromium especifica de este sandbox; en un entorno
        // con Google Chrome/Chromium instalado de forma estandar no haria falta
        // fijar setBinary. Se deja como override por system property para no
        // hardcodear un path no portable en otros entornos.
        String chromeBinary = System.getProperty("chrome.binary.path", "/opt/pw-browsers/chromium");
        options.setBinary(chromeBinary);
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
}
