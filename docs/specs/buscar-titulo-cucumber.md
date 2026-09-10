# Buscar "cucumber" en Google y validar el título de resultados

**Estado:** Listo para implementar
**Feature file objetivo:** src/test/java/com/demo/features/google.feature
**Tag propuesto:** @BuscarTituloCucumber

## Historial de iteraciones

| Versión | Cambio |
|---|---|
| v1 | Borrador inicial a partir de la solicitud: "busque 'cucumber' en Google y valide que el título de la página lo contenga". Solo cubría el `Then` de título, sin criterio de mayúsculas/minúsculas ni manejo de resultados cero. |
| v2 | Revisión: se preguntó qué pasa si Google no devuelve resultados o redirige a una página de corrección ortográfica. Se decidió **no** cubrir ese caso aquí (ya lo cubre `@BuscarPalabra` como precondición general) y se dejó explícito en "Alcance". Se resolvió la comparación de título como *case-insensitive* porque Chrome puede capitalizar el `<title>` de forma distinta según la palabra buscada. |
| v3 | Refinamiento final: se ajustó el criterio de aceptación para no acoplar el escenario al conteo de resultados (eso ya es responsabilidad de `@BuscarPalabra`); este escenario se enfoca únicamente en el contenido del `<title>`. Se añadieron notas de implementación sobre qué step reutilizar (`ir al navegador de Google Chrome`, `digitar la palabra {string} en el buscador`, `dar enter para que se inicie la busqueda`) para no duplicar steps existentes, y se marcó **Listo para implementar**. |

## Objetivo

Verificar que, al buscar la palabra "cucumber" en Google, el título de la página de resultados contenga esa palabra — confirmando que la búsqueda realmente se ejecutó y que Google no redirigió a una página distinta (ej. una sugerencia ortográfica o una página de error).

## Alcance

- **Incluye:** navegar a Google, escribir "cucumber" en el buscador, ejecutar la búsqueda, y validar que `driver.getTitle()` contenga la palabra buscada (comparación case-insensitive).
- **No incluye:**
  - Validar el conteo de resultados — eso ya lo cubre el escenario existente `@BuscarPalabra`/`@BuscarSelenium` y no se duplica aquí.
  - Manejar el caso en que Google no encuentre resultados o redirija a una página de corrección ortográfica ("Showing results for...") — se asume, igual que en los escenarios existentes, que "cucumber" siempre devuelve resultados normales. Si eso deja de ser cierto, se necesita un spec aparte.
  - Cualquier cambio a los steps `Given`/`When` existentes; este escenario solo agrega un `Then` nuevo sobre un flujo ya cubierto.

## Criterios de aceptación

1. Dado que se navegó a Google, se escribió "cucumber" en el buscador y se presionó enter, el título de la página resultante (`driver.getTitle()`) debe contener la subcadena "cucumber", sin distinguir mayúsculas/minúsculas.
2. El escenario debe poder ejecutarse de forma aislada vía `-Dcucumber.filter.tags="@BuscarTituloCucumber"`, igual que los escenarios existentes.
3. El navegador debe cerrarse (`driver.quit()`) al finalizar el escenario, sin dejar procesos huérfanos, siguiendo el patrón ya usado en `MyStepDefinitions`.

## Escenario(s) Gherkin (borrador -> final)

```gherkin
  @BuscarTituloCucumber
  Scenario: Verificar que el título de resultados contenga la palabra buscada
    Given ir al navegador de Google Chrome
    When digitar la palabra "cucumber" en el buscador
    And dar enter para que se inicie la busqueda
    Then validar que el título de la página contenga la palabra "cucumber"
```

Los tres primeros steps ya existen (reutilizados tal cual de `google.feature`); solo el `Then` final es nuevo.

## Casos borde considerados

- **Título con mayúsculas distintas** (ej. "Cucumber - Google Search" vs. "cucumber - Búsqueda de Google"): en alcance — la comparación debe ser case-insensitive.
- **Google sin resultados / redirección a corrección ortográfica**: fuera de alcance para este spec (ver "Alcance"); se asume que no ocurre para la palabra "cucumber". Deferido explícitamente, no resuelto por omisión.
- **Idioma de la interfaz de Google** (título podría variar según locale, ej. "Búsqueda de Google" vs. "Google Search"): fuera de alcance porque el criterio solo exige que el título contenga "cucumber", no una cadena completa — es robusto al idioma de la UI por diseño.

## Preguntas abiertas

Ninguna pendiente. Las dos preguntas que surgieron en la revisión (manejo de resultados cero, sensibilidad a mayúsculas) quedaron resueltas en v2/v3 arriba.

## Notas de implementación

- Reutilizar los steps `Given`/`When`/`And` existentes de `MyStepDefinitions` — no crear steps duplicados con texto ligeramente distinto.
- El nuevo `Then` puede implementarse directamente sobre `driver.getTitle()` sin necesitar un nuevo helper en `Utils` (a diferencia de `@BuscarPalabra`, que sí depende de `Utils.extractTotalResults`).
- Seguir la convención de driver actual de este sandbox documentada en `CLAUDE.md`/`MyStepDefinitions` (Chrome/Chromium vía `ChromeDriver`, headless), no el patrón Firefox+geckodriver descrito como default general del proyecto.
- Próximo paso: entregar este spec a `cucumber-selenium-tester` para que agregue el escenario a `google.feature` y el step definition correspondiente.
