---
name: feature-spec-planning
description: Turn a raw feature request for this Cucumber/Selenium project into a structured, iterated spec document under docs/specs/ before any `.feature` file or step definition is written. Use when a new test scenario, or a change to an existing one, is proposed and needs scope, acceptance criteria, and edge cases nailed down first — not when the scenario is already clear and small enough to implement directly.
---

# Feature Spec Planning

Planning skill for this repository (`cucumberselenium`, Spring Boot + Cucumber + Selenium). Its job is to turn a one-line feature request into a spec that is complete enough to hand straight to implementation, and to make the *iteration* that got it there visible in the document itself — not just in chat history.

This skill only produces/refines the spec file. It never edits `.feature` files, step definitions, or `Utils` — that is the `cucumber-selenium-tester` agent's job, once the spec is approved.

## When to use this

- A user asks for a new Cucumber scenario that isn't a trivial one-line tweak (new page, new assertion type, new edge case).
- A user asks to change the behavior of an existing scenario in a way that affects acceptance criteria.
- Not needed for pure bug fixes to a locator or a flaky wait — those go straight to `cucumber-selenium-tester`.

## Output location and naming

Write the spec to `docs/specs/<kebab-case-slug>.md`, one file per feature idea. The slug should describe the scenario, not the ticket (e.g. `buscar-titulo-cucumber.md`, not `feature-3.md`).

## Spec template

Every spec produced by this skill has these sections, in this order:

```markdown
# <Feature title>

**Estado:** Draft | En iteración | Listo para implementar
**Feature file objetivo:** src/test/java/com/demo/features/<archivo>.feature
**Tag propuesto:** @<TagEnPascalCase>

## Historial de iteraciones
| Versión | Cambio |
|---|---|
| v1 | Borrador inicial a partir de la solicitud del usuario |
| v2 | ... ajuste tras revisión ... |

## Objetivo
One or two sentences: what user-visible behavior is being verified and why.

## Alcance
- **Incluye:** ...
- **No incluye:** ... (explicitly name what was cut, and why — this is what makes the iteration visible)

## Criterios de aceptación
Numbered, testable, in Spanish to match the project's Gherkin convention.

## Escenario(s) Gherkin (borrador -> final)
Real Gherkin, in Spanish, following the existing style in `src/test/java/com/demo/features/google.feature` (Given/When/And/Then, one `@Tag` per scenario). This must be close enough to final that `cucumber-selenium-tester` can paste it in with minimal changes.

## Casos borde considerados
List edge cases that were discussed, and for each one say whether it's in scope now or deliberately deferred, and why.

## Preguntas abiertas
Anything still unresolved. A spec with unresolved *blocking* questions cannot be marked "Listo para implementar".

## Notas de implementación
Pointers for whoever implements this: which existing step text/regex to reuse, whether a new `Utils` helper is needed, driver/browser conventions to follow (see CLAUDE.md — Chrome/Chromium in this sandbox, Firefox+geckodriver is the documented pattern elsewhere).
```

## Iteration process

1. **Draft (v1).** Read the existing `.feature` files and step definitions first (reuse existing step text instead of inventing near-duplicates). Fill in every section of the template from the user's request, even if some are rough. Mark **Estado: Draft**.
2. **Review.** Present the draft and explicitly ask about anything ambiguous: what counts as pass/fail, which edge cases matter, which browser/tag conventions apply. Do not silently guess on acceptance criteria.
3. **Refine (v2, v3, ...).** Each round of feedback becomes a new row in "Historial de iteraciones" describing what changed and why — not just an overwrite. Keep prior decisions traceable.
4. **Finalize.** Once acceptance criteria are testable, edge cases are resolved or explicitly deferred, and the Gherkin is concrete, set **Estado: Listo para implementar**. A spec is not "iterated" if it only ever had one version — the history table must show real refinement, not a single entry.

## Handoff

A spec marked "Listo para implementar" is the direct input to the `cucumber-selenium-tester` agent: hand it the spec's Gherkin and implementation notes so it can add the `.feature` scenario and step definitions.
