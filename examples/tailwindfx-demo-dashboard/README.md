# TailwindFX Demo Dashboard

A comprehensive example application that exercises the public APIs of the
TailwindFX framework: JIT style compilation, arbitrary values, dark mode,
responsive breakpoints, batching, metrics, and all component utilities
(buttons, badges, alerts, avatars, form controls, data tables, accordions,
effects, and animations).

## Requirements

- Java 17+
- Maven 3.8+

## Build

From the repository root, build the framework modules together with the
examples (the `examples` profile is disabled by default so it never leaks
into the Maven Central publication):

```bash
mvn -Dexamples install
```

## Run

```bash
mvn -pl examples/tailwindfx-demo-dashboard javafx:run
```

A window opens with an admin-style layout: a left sidebar (brand, navigation
with icons, user profile pinned to the bottom), a top header with a search
box, dark-mode toggle and user avatar, a KPI stat row with four trend cards,
and a scrollable grid of cards demonstrating each feature area.

## What it demonstrates

| Section | API covered |
| --- | --- |
| KPI stats | `TwGridPane` with breakpoint-driven columns, `TwCard` stat tiles, `TwAvatar` |
| Buttons | `TwButton.primary/secondary/outline/ghost/danger/icon` |
| Badges | `TwBadge.create/pill/outline`, `TwBadgeDot` |
| Alerts | `TwAlert` with `AlertType.INFO/SUCCESS/WARNING/ERROR` |
| Avatars | Initials avatars, `TwAvatarGroup`, `TwAvatarWithStatus` |
| Form | `TwInput`, `TwSelect`, `TwCheckbox` inside `TwCard.withTitle(...)` |
| Data | `TwProgressBar`, `TwSpinner`, `TwDataTable` |
| Accordion | `TwAccordion` with `TwTitledPane` |
| Style utilities | `TwStyle.apply` tokens (`p-*`, `m-*`, `bg-*-500`, `text-*`, `rounded-*`, `shadow-*`), `hover:`/`focus:`/`dark:` variants, JIT arbitrary values |
| Effects & responsive | `TwEffect`, `TwAnimation` (fade-in), `TwResponsive` / `BreakpointManager` |
| Batching & metrics | `TwBatch`, `TwMetrics` / `TailwindFXMetrics` |

The app registers styles through `TwInstall.install(scene)` and applies the
AOT-generated stylesheet produced by `tailwindfx-maven-plugin` via
`TwInstall.installGenerated(scene, "css/tailwindfx-generated.css")`.
