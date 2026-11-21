# Components

`tailwindfx-components` ships pre-built JavaFX controls styled with TailwindFX. Add the module to
your project:

```xml
<dependency>
    <groupId>io.github.yasmramos</groupId>
    <artifactId>tailwindfx-components</artifactId>
    <version>${tailwindfx.version}</version>
</dependency>
```

Install its stylesheet once, before showing the scene:

```java
TwInstall.install(scene);                                        // base + variables
TwInstall.installGenerated(scene, "/tailwindfx-components.css"); // component styles
```

## Contents

| Component | Extends / returns | Description |
|-----------|-------------------|-------------|
| `TwButton` | `Button` | Variants (primary, secondary, outline, ghost, danger, success, disabled, icon), colours, loading state |
| `TwCard` | `VBox` | Header / body / footer slots with matching classes |
| `TwAlert` | `HBox` | Info / success / warning / error alerts with icon and message |
| `TwBadge` | `Label` | Inline badges, pills, outlines and dot badges |
| `TwAvatar` | `StackPane` | Initials or image avatars, groups, online status |
| `TwInput` | `TextField` | Text input with error state, numeric/decimal formatters |
| `TwPasswordField` | `PasswordField` | Password input with error state |
| `TwCheckbox` | `CheckBox` | Colours, sizes and error state |
| `TwSelect` | factories | Styled `ComboBox` / `ChoiceBox`, editable and error variants |
| `TwSpinner` | factories | Styled `ProgressIndicator` spinners |
| `TwProgressBar` | `ProgressBar` | Colours, striped and indeterminate states |
| `TwAccordion` | `VBox` | Group of `TwTitledPane`s |
| `TwTitledPane` | `TitledPane` | Collapsible section |
| `TwVirtualFlow` | `Region` | Virtualised, scrollable list/grid of cells |
| `TwDataTable` | `VBox` | Sortable, filterable, paginated table with search |

## TwButton

```java
Button primary = TwButton.primary("Save");
Button outline = TwButton.outline("Cancel");
Button danger  = TwButton.danger("Delete");
Button withIcon = TwButton.icon("+", "Add");

TwButton custom = new TwButton("Send");
custom.setColor("purple");
custom.setLoading(true);   // shows a waiting cursor until the work finishes
```

## TwCard

```java
TwCard card = new TwCard();
card.setHeader(new Label("Title"));
card.setBody(new Label("Body content"));
card.setFooter(TwButton.primary("OK"));

// Or use a shorthand
TwCard titled = TwCard.withTitle("Title");
```

## TwAlert

```java
TwAlert.info("Heads up");
TwAlert.success("Saved");
TwAlert.warning("Low stock");
TwAlert.error("Payment failed");

TwAlert custom = new TwAlert(TwAlert.AlertType.WARNING, "Check the address");
```

## TwBadge

```java
TwBadge.create("New");
TwBadge.create("Sale", "red");
TwBadge.pill("3");
TwBadge.outline("Beta", "blue");
TwBadge.dot("Online", "green");     // returns a TwBadgeDot with a status label
```

## TwAvatar

```java
TwAvatar.create("YA");
TwAvatar.create("YA", "purple", "lg");
TwAvatar.fromImage(imageView);
TwAvatar.group(avatar1, avatar2, avatar3);
TwAvatar.withStatus(avatar, true);   // online indicator
```

## TwInput and TwPasswordField

```java
TwInput email = new TwInput("Email");
email.setError(true);                 // red border

TwInput quantity = TwInput.numeric(); // digits only
TwInput price = TwInput.decimal();

TwPasswordField password = new TwPasswordField("Password");
password.setError(true);
```

## TwCheckbox

```java
TwCheckbox box = new TwCheckbox("Accept terms");
box.setColor("green");
box.setSize("lg");
box.setError(true);

TwCheckbox.create("Newsletter");
TwCheckbox.checked("Remember me", true);
TwCheckbox.disabled("Unavailable");
```

## TwSelect

```java
ComboBox<String> combo = TwSelect.comboBox("One", "Two", "Three");
ChoiceBox<String> choice = TwSelect.choiceBox("Small", "Medium", "Large");
ComboBox<String> editable = TwSelect.editable("Type or pick…");
ComboBox<String> invalid = TwSelect.error("Choose a country", "Spain", "France");
```

## TwSpinner and TwProgressBar

```java
ProgressIndicator spinner = TwSpinner.create();
ProgressIndicator large = TwSpinner.largeColored("blue");

TwProgressBar bar = new TwProgressBar(0.6);
bar.setColor("green");
bar.setStriped(true);

TwProgressBar done = TwProgressBar.success(1.0);
```

## TwAccordion and TwTitledPane

```java
TwTitledPane pane = new TwTitledPane("Details", new Label("Hidden content"));
TwAccordion accordion = new TwAccordion(pane);
```

## TwVirtualFlow

```java
TwVirtualFlow<Task> flow = new TwVirtualFlow<>();
flow.setItems(tasks);
flow.setCellHeight(72);
flow.setCellUpdater((node, task) -> ((Label) node).setText(task.title()));
```

## TwDataTable

```java
TwDataTable<User> table = TwDataTable.of(User.class)
    .column("Name", User::name)
    .column("Email", User::email, true)   // sortable
    .searchable(true)
    .pageSize(20)
    .placeholder("No users")
    .build();

table.setItems(users);
table.setFilter(user -> user.active());
```

## Notes

- Every component installs its own base style class (for example `tw-card`, `tw-alert`, `btn`) so
  the stylesheet can target it without extra work.
- The stylesheet in `tailwindfx-components` also carries the dark-mode overrides; switch themes with
  `TwTheme.of(scene).dark().apply()`.
- `ComponentCssContractTest` guards the contract between the classes the components apply and the
  rules the stylesheet declares, so the two cannot drift apart unnoticed.
