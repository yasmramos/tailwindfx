# Basic TailwindFX Example App

A beginner-friendly example project built with **TailwindFX** and **JavaFX**, demonstrating how to use TailwindFX utility classes to create modern, clean UI interfaces.

---

## Included Examples

| Example | Description |
|---|---|
| **Basic Login Form** | Email, password, login button, forgot password |
| **Basic Dashboard** | Sidebar, stats cards, accordion/collapse sections |

---

## Screenshots

### Login Example
![Login Example](https://raw.githubusercontent.com/keshavsharma0614-blip/tailwindfx/main/examples/basic-app/login-example.png)

### Dashboard Example
![Dashboard Example](https://raw.githubusercontent.com/keshavsharma0614-blip/tailwindfx/main/examples/basic-app/dashboard-example.png)

---

## Project Structure

```
examples/basic-app/
├── README.md
├── login-example.png
└── dashboard-example.png

src/main/java/io/github/yasmramos/tailwindfx/examples/
├── BasicLoginExample.java
└── BasicDashboardExample.java
```

---

## Requirements

- Java JDK 17 or higher
- JavaFX SDK
- IntelliJ IDEA (recommended) or any Java-supported IDE

---

## How to Run

### Step 1 — Clone the Repository
```bash
git clone https://github.com/yasmramos/tailwindfx.git
cd tailwindfx
```

### Step 2 — Configure JavaFX in Your IDE

In IntelliJ IDEA, go to **Run → Edit Configurations → Modify Options → Add VM Options** and paste:

```
--module-path "path/to/javafx-sdk/lib" --add-modules javafx.controls,javafx.fxml
```

Replace `path/to/javafx-sdk/lib` with your actual JavaFX SDK path, for example:
```
--module-path "C:\javafx-sdk-26\lib" --add-modules javafx.controls,javafx.fxml
```

### Step 3 — Run the Examples

**Login Example:**
```
src/main/java/io/github/yasmramos/tailwindfx/examples/BasicLoginExample.java
```
Right-click → **Run 'BasicLoginExample.main()'**

**Dashboard Example:**
```
src/main/java/io/github/yasmramos/tailwindfx/examples/BasicDashboardExample.java
```
Right-click → **Run 'BasicDashboardExample.main()'**

---

## TailwindFX Integration

These examples use TailwindFX the correct way — no inline `setStyle()`:

```java
// Install TailwindFX styles on the scene
TailwindFX.install(scene);

// Apply utility classes to nodes
TailwindFX.apply(loginButton, "btn-primary", "rounded-lg");
TailwindFX.apply(root, "bg-gray-50", "border", "rounded-xl");
TailwindFX.apply(title, "text-2xl", "font-bold");
```

---

## Troubleshooting

**JavaFX runtime components are missing**
→ Make sure your VM Options include `--module-path` and `--add-modules javafx.controls,javafx.fxml`

**TailwindFX styles not applying**
→ Confirm `TailwindFX.install(scene)` is called after `new Scene(...)` and before `stage.show()`

**Old UI still showing after code changes**
→ Save all files (Ctrl+S), then go to **Build → Rebuild Project**, and re-run

---

## Purpose

This project helps developers:
- Understand how to set up and install TailwindFX
- Learn utility-first styling in JavaFX applications
- See real working examples of login and dashboard layouts
- Use `TailwindFX.apply()` instead of traditional inline styles

---

*Part of the [TailwindFX](https://github.com/yasmramos/tailwindfx) open-source project.*
