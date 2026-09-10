# cucumberselenium

[![Tests](https://github.com/maicolparrado93/TestSimetrik1/actions/workflows/tests.yml/badge.svg)](https://github.com/maicolparrado93/TestSimetrik1/actions/workflows/tests.yml)

Proyecto de automatización de pruebas: escenarios BDD en Cucumber (Gherkin en español) que manejan un navegador real vía Selenium WebDriver, sobre un bootstrap mínimo de Spring Boot 3.1.5 (Java 17). No hay lógica de negocio — el propósito único del repo es este pipeline de pruebas end-to-end.

## Qué demuestra este repo

- **BDD real, no solo unit tests**: escenarios Gherkin legibles por no-programadores, ejecutando contra `https://www.google.com` con Selenium headless.
- **Setup de WebDriver limpio y portable**: cero binarios de driver commiteados. Selenium Manager detecta el navegador y descarga el `chromedriver` correcto, igual en local que en CI — sin mantenimiento manual de versiones.
- **CI real**: cada push corre la suite completa en GitHub Actions y publica el reporte HTML de Cucumber y las capturas de pantalla de cada escenario como artifacts del build (ver el badge arriba).
- **Planeación asistida por IA integrada al flujo**: un subagente de Claude Code (`planning-subagent`) conectado a un skill (`feature-spec-planning`) convierte cada idea de escenario en un spec iterado (`docs/specs/`) antes de tocar código — ver [Flujo de planeación](#flujo-de-planeación-con-ia) abajo.

## Cómo correr

Con el wrapper de Maven, sin instalar nada más:

```bash
./mvnw clean test                 # suite completa
./mvnw test -Dtest=RunCucumberTest -Dcucumber.filter.tags="@BuscarPalabra"   # un escenario por tag
```

En un entorno donde Chrome/Chromium no esté en una ruta estándar del sistema (p. ej. un sandbox), se puede fijar explícitamente:

```bash
./mvnw test -Dtest=RunCucumberTest -Dchrome.binary.path=/ruta/al/binario
```

El reporte HTML de Cucumber queda en `target/cucumber-reports/`; las capturas de pantalla de cada escenario, en `target/screenshots/`.

## Escenarios

| Tag | Qué valida |
|---|---|
| `@BuscarPalabra` | Buscar "simetrik" en Google devuelve más de cero resultados |
| `@BuscarSelenium` | Buscar "selenium" en Google devuelve más de cero resultados |
| `@BuscarTituloCucumber` | El `<title>` de la página de resultados contiene la palabra buscada |
| `@CampoBusquedaVisible` | El campo de búsqueda de Google es visible al cargar la página |

## Arquitectura

```
src/main/java/com/demo/cucumberselenium/
  CucumberseleniumApplication.java   # bootstrap Spring Boot, sin lógica propia

src/test/java/com/demo/
  cucumberselenium/RunCucumberTest.java   # entry point JUnit + Cucumber (@CucumberOptions)
  features/google.feature                 # escenarios Gherkin (español)
  steps/MyStepDefinitions.java             # step definitions sobre Selenium WebDriver
  utils/Utils.java                         # helpers de parsing (ej. extraer conteo de resultados)

docs/specs/                          # specs de features, iterados antes de implementar
.claude/agents/                      # subagentes de Claude Code
.claude/skills/                      # skills reutilizables de Claude Code
.github/workflows/tests.yml          # CI
```

Ver [`CLAUDE.md`](./CLAUDE.md) para el detalle completo pensado para trabajar con Claude Code en este repo (convenciones, comandos, gotchas de dependencias).

## Flujo de planeación con IA

Antes de agregar un escenario no trivial, la idea pasa por una etapa de planeación explícita en vez de saltar directo al código:

1. **`.claude/skills/feature-spec-planning/SKILL.md`** define la plantilla del spec, dónde vive (`docs/specs/<slug>.md`) y la regla de que un spec no cuenta como "iterado" si solo tuvo una versión.
2. **`.claude/agents/planning-subagent.md`** es el subagente que ejecuta esa etapa: siempre invoca el skill anterior, nunca toca `.feature`/step definitions directamente, y entrega el spec terminado al agente `cucumber-selenium-tester` para implementación.
3. **[`docs/specs/buscar-titulo-cucumber.md`](./docs/specs/buscar-titulo-cucumber.md)** es un ejemplo real de ese ciclo completo: cuatro iteraciones documentadas (v1 a v4) desde la idea original hasta el escenario `@BuscarTituloCucumber` ya implementado en `google.feature`.
