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

## CI

`.github/workflows/tests.yml` runs `./mvnw clean test` on every push to `main` and on every PR, using GitHub Actions' preinstalled Chrome. It uploads the Cucumber HTML report (`target/cucumber-reports`) and scenario screenshots (`target/screenshots`) as build artifacts, always (even on failure), for debugging and as portfolio evidence.

**The test step runs with `continue-on-error: true`, deliberately.** Google shows a reCAPTCHA to GitHub Actions' shared runner IPs instead of real search results — confirmed by inspecting the page HTML in a CI run (`<div id="recaptcha" class="g-recaptcha">` appears instead of `#result-stats`/organic results). This makes `@BuscarPalabra`, `@BuscarSelenium` and `@BuscarTituloCucumber` fail deterministically in CI; it is not a selector bug or a flake, and there is no code fix for it (working around Google's bot-detection is out of scope, deliberately). The `Report suite outcome` step writes the real pass/fail to the run's Job Summary regardless, so the failure stays visible without turning the PR check red. `@CampoBusquedaVisible` doesn't search, so it isn't affected. All four scenarios pass locally from a normal (non-datacenter) IP.

## Architecture

- **`RunCucumberTest`** (`src/test/java/com/demo/cucumberselenium/RunCucumberTest.java`) is the JUnit entry point that wires Cucumber to JUnit via `@CucumberOptions`. It points `features` at `src/test/java/com/demo/features` and `glue` at the `com.demo` package (so step definitions can live in any sub-package under `com.demo`, e.g. `com.demo.steps`).
- **Feature files** live under `src/test/java/com/demo/features/*.feature` and are written in Spanish (Gherkin `Given`/`When`/`Then`).
- **Step definitions** live under `src/test/java/com/demo/steps/` (e.g. `MyStepDefinitions.java`) and implement the Gherkin steps directly against a Selenium `WebDriver`. Each step-definition class owns its own `WebDriver` instance rather than sharing one through a common hook/context.
- **`Utils`** (`src/test/java/com/demo/utils/Utils.java`) holds small parsing helpers used by step definitions (e.g. extracting the result count out of Google's "About X results" text).
- **WebDriver setup is a single, consistent Chrome/Chromium pattern**: `MyStepDefinitions` launches `ChromeDriver` headless. No driver binary is checked into the repo — Selenium Manager (bundled in `selenium-java`) detects the installed browser and downloads/caches a matching `chromedriver` automatically, both locally and in CI, so there's no version to keep in sync by hand. The only override point is the optional `chrome.binary.path` system property, needed solely in sandboxes where Chrome/Chromium isn't on a standard system path (e.g. `-Dchrome.binary.path=/opt/pw-browsers/chromium`); outside those, leave it unset. `CucumberseleniumApplication.main` is a plain, unmodified Spring Boot bootstrap (`SpringApplication.run(...)`) — it no longer contains ad-hoc Selenium/driver code.
- **`pom.xml` pins `<selenium.version>` explicitly.** `spring-boot-starter-parent:3.1.5`'s own dependency management pins that property to an old `4.8.3`, which silently downgrades Selenium's submodules (`selenium-remote-driver`, `selenium-chrome-driver`, etc.) even when `selenium-java` itself is declared at a newer version — the submodules resolve via the *nearest* dependency-management entry, not `selenium-java`'s own POM. If you bump the Selenium version, update the `selenium.version` property, not just the `selenium-java` dependency's `<version>`, or you'll get a silent Selenium Manager / ChromeDriver mismatch.
- **`docs/specs/`** holds iterated feature specs produced by the `planning-subagent` (`.claude/agents/planning-subagent.md`) via the `feature-spec-planning` skill (`.claude/skills/feature-spec-planning/SKILL.md`) — write a spec there and get it to "Listo para implementar" before adding new `.feature`/step-definition code for anything beyond a trivial fix.
- **`serenity.conf`** (`src/test/resources/serenity.conf`) configures a Chrome/headless profile (screenshots on failure, headless mode, sandbox-disabling Chrome args), but no Serenity dependency is present in `pom.xml` and no code currently reads this config — it does not affect how `RunCucumberTest` actually runs today.
