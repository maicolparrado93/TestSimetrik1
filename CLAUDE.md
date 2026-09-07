# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Spring Boot 3.1.5 (Java 17) project whose only real purpose is to run Cucumber BDD scenarios that drive Selenium WebDriver browser automation. There is no application logic beyond a Spring Boot bootstrap class — this is a test-automation project (`cucumberselenium`).

## Commands

Build and run tests (Maven wrapper, no local Maven install needed):

```bash
./mvnw clean test          # run all tests, including the Cucumber suite
./mvnw clean install       # build the jar and run tests
```

Run only the Cucumber suite (it's a normal JUnit test class):

```bash
./mvnw test -Dtest=RunCucumberTest
```

Run a single Cucumber scenario by tag (tags are defined in `.feature` files, e.g. `@BuscarPalabra`):

```bash
./mvnw test -Dtest=RunCucumberTest -Dcucumber.filter.tags="@BuscarPalabra"
```

There is no linter configured in this project.

## Architecture

- **`RunCucumberTest`** (`src/test/java/com/demo/cucumberselenium/RunCucumberTest.java`) is the JUnit entry point that wires Cucumber to JUnit via `@CucumberOptions`. It points `features` at `src/test/java/com/demo/features` and `glue` at the `com.demo` package (so step definitions can live in any sub-package under `com.demo`, e.g. `com.demo.steps`).
- **Feature files** live under `src/test/java/com/demo/features/*.feature` and are written in Spanish (Gherkin `Given`/`When`/`Then`).
- **Step definitions** live under `src/test/java/com/demo/steps/` (e.g. `MyStepDefinitions.java`) and implement the Gherkin steps directly against a Selenium `WebDriver`. Each step-definition class owns its own `WebDriver` instance rather than sharing one through a common hook/context.
- **`Utils`** (`src/test/java/com/demo/utils/Utils.java`) holds small parsing helpers used by step definitions (e.g. extracting the result count out of Google's "About X results" text).
- **WebDriver setup is currently inconsistent across the codebase**: `MyStepDefinitions` launches Firefox via `FirefoxDriver`, pointing `webdriver.gecko.driver` at the checked-in binary `src/test/java/com/demo/drivers/geckodriver`; `CucumberseleniumApplication.main` (unused by the test suite) instead launches Chrome via a hardcoded Windows path to `chromedriver.exe`. When adding new steps/scenarios, prefer following the pattern already used by the step definitions (Firefox + relative driver path) unless deliberately switching browsers.
- **`serenity.conf`** (`src/test/resources/serenity.conf`) configures a Chrome/headless profile (screenshots on failure, headless mode, sandbox-disabling Chrome args), but no Serenity dependency is present in `pom.xml` and no code currently reads this config — it does not affect how `RunCucumberTest` actually runs today.
