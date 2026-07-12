package io.github.yasmramos.tailwindfx.theme;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.prefs.Preferences;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

/**
 * Panel de personalización de temas en tiempo real para TailwindFX.
 *
 * <p>Permite a los usuarios: - Cambiar colores primarios/secundarios dinámicamente - Ajustar
 * valores de tema (radius, spacing, etc.) - Ver preview en tiempo real - Guardar/cargar
 * configuraciones personalizadas - Exportar configuración como archivo JSON/properties
 *
 * @author yasmramos
 * @version 0.1.1
 */
public class ThemeCustomizationPanel extends VBox {

  private static final String PREFS_NODE = "tailwindfx/theme/custom";

  // Secciones del panel
  private ColorPicker primaryColorPicker;
  private ColorPicker secondaryColorPicker;
  private ColorPicker backgroundColorPicker;
  private ColorPicker textColorPicker;

  private Slider radiusSlider;
  private Slider spacingSlider;
  private Slider shadowIntensitySlider;

  private Label previewLabel;
  private Button previewButton;
  private StackPane previewBox;

  private ThemeManager themeManager;
  private Preferences prefs;

  /** Crea un nuevo panel de personalización de temas. */
  public ThemeCustomizationPanel() {
    this(null);
  }

  /**
   * Crea un nuevo panel con un ThemeManager específico.
   *
   * @param themeManager el gestor de temas a utilizar
   */
  public ThemeCustomizationPanel(ThemeManager themeManager) {
    this.themeManager = themeManager;
    this.prefs = Preferences.userRoot().node(PREFS_NODE);

    initializeUI();
    loadSavedConfiguration();
    setupListeners();
  }

  /** Inicializa la interfaz de usuario del panel. */
  private void initializeUI() {
    setSpacing(20);
    setPadding(new Insets(20));
    setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");

    // Título
    Label titleLabel = new Label("🎨 Theme Customization");
    titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

    // Sección de Colores
    VBox colorsSection = createColorsSection();

    // Sección de Propiedades
    VBox propertiesSection = createPropertiesSection();

    // Sección de Preview
    VBox previewSection = createPreviewSection();

    // Sección de Acciones
    HBox actionsSection = createActionsSection();

    getChildren()
        .addAll(
            titleLabel,
            new Separator(),
            colorsSection,
            new Separator(),
            propertiesSection,
            new Separator(),
            previewSection,
            new Separator(),
            actionsSection);
  }

  /**
   * Crea la sección de selección de colores.
   *
   * @return VBox con los controles de color
   */
  private VBox createColorsSection() {
    VBox section = new VBox(15);
    section.setPadding(new Insets(10));

    Label title = new Label("🌈 Colors");
    title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #475569;");

    GridPane grid = new GridPane();
    grid.setHgap(15);
    grid.setVgap(15);

    // Primary Color
    Label primaryLabel = createCustomLabel("Primary Color:");
    primaryColorPicker = createColorPicker(Color.valueOf("#3b82f6")); // blue-500
    grid.add(primaryLabel, 0, 0);
    grid.add(primaryColorPicker, 1, 0);

    // Secondary Color
    Label secondaryLabel = createCustomLabel("Secondary Color:");
    secondaryColorPicker = createColorPicker(Color.valueOf("#10b981")); // emerald-500
    grid.add(secondaryLabel, 0, 1);
    grid.add(secondaryColorPicker, 1, 1);

    // Background Color
    Label bgLabel = createCustomLabel("Background Color:");
    backgroundColorPicker = createColorPicker(Color.valueOf("#ffffff"));
    grid.add(bgLabel, 0, 2);
    grid.add(backgroundColorPicker, 1, 2);

    // Text Color
    Label textLabel = createCustomLabel("Text Color:");
    textColorPicker = createColorPicker(Color.valueOf("#1e293b"));
    grid.add(textLabel, 0, 3);
    grid.add(textColorPicker, 1, 3);

    section.getChildren().addAll(title, grid);
    return section;
  }

  /**
   * Crea la sección de ajuste de propiedades.
   *
   * @return VBox con los sliders de propiedades
   */
  private VBox createPropertiesSection() {
    VBox section = new VBox(15);
    section.setPadding(new Insets(10));

    Label title = new Label("⚙️ Properties");
    title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #475569;");

    VBox controls = new VBox(12);

    // Border Radius
    VBox radiusBox = createSliderControl("Border Radius:", 0, 24, 8, "px");
    radiusSlider = (Slider) ((HBox) radiusBox.getChildren().get(1)).getChildren().get(0);
    controls.getChildren().add(radiusBox);

    // Spacing Scale
    VBox spacingBox = createSliderControl("Spacing Scale:", 0.5, 2.0, 1.0, "x");
    spacingSlider = (Slider) ((HBox) spacingBox.getChildren().get(1)).getChildren().get(0);
    controls.getChildren().add(spacingBox);

    // Shadow Intensity
    VBox shadowBox = createSliderControl("Shadow Intensity:", 0, 100, 50, "%");
    shadowIntensitySlider = (Slider) ((HBox) shadowBox.getChildren().get(1)).getChildren().get(0);
    controls.getChildren().add(shadowBox);

    section.getChildren().addAll(title, controls);
    return section;
  }

  /**
   * Crea un control slider con etiqueta y valor.
   *
   * @param label texto de la etiqueta
   * @param min valor mínimo
   * @param max valor máximo
   * @param value valor inicial
   * @param unit unidad de medida
   * @return VBox con el control completo
   */
  private VBox createSliderControl(
      String label, double min, double max, double value, String unit) {
    VBox container = new VBox(5);

    Label nameLabel = new Label(label);
    nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b;");

    HBox sliderBox = new HBox(10);
    sliderBox.setAlignment(Pos.CENTER_LEFT);

    Slider slider = new Slider(min, max, value);
    slider.setPrefWidth(200);
    slider.setStyle("-fx-control-inner-background: #3b82f6;");

    Label valueLabel = new Label(String.format("%.1f %s", value, unit));
    valueLabel.setMinWidth(60);
    valueLabel.setStyle("-fx-text-fill: #475569; -fx-font-family: monospace;");

    // Actualizar etiqueta al cambiar slider
    slider
        .valueProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              valueLabel.setText(String.format("%.1f %s", newVal.doubleValue(), unit));
              applyLiveChanges();
            });

    sliderBox.getChildren().addAll(slider, valueLabel);
    container.getChildren().addAll(nameLabel, sliderBox);

    return container;
  }

  /**
   * Crea la sección de preview en vivo.
   *
   * @return VBox con el preview
   */
  private VBox createPreviewSection() {
    VBox section = new VBox(15);
    section.setPadding(new Insets(10));

    Label title = new Label("👁️ Live Preview");
    title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #475569;");

    // Área de preview (usamos Pane en lugar de Region para tener getChildren() público)
    StackPane previewContainer = new StackPane();
    previewContainer.setPrefHeight(150);
    previewContainer.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 8px;");
    this.previewBox = previewContainer;

    // Elementos de preview
    previewLabel = new Label("Sample Text Preview");
    previewLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #1e293b;");

    previewButton = new Button("Preview Button");
    previewButton.setStyle(
        """
            -fx-background-color: #3b82f6;
            -fx-text-fill: white;
            -fx-background-radius: 8px;
            -fx-padding: 8 16;
            -fx-font-weight: bold;
        """);

    VBox previewContent = new VBox(15, previewLabel, previewButton);
    previewContent.setAlignment(Pos.CENTER);
    previewContent.setPadding(new Insets(20));

    previewContainer.getChildren().add(previewContent);

    section.getChildren().addAll(title, previewBox);
    return section;
  }

  /**
   * Crea la sección de botones de acción.
   *
   * @return HBox con los botones
   */
  private HBox createActionsSection() {
    HBox box = new HBox(10);
    box.setAlignment(Pos.CENTER);

    Button saveBtn = new Button("💾 Save Configuration");
    saveBtn.setStyle(getButtonStyle("#10b981"));
    saveBtn.setOnAction(e -> saveConfiguration());

    Button loadBtn = new Button("📂 Load Configuration");
    loadBtn.setStyle(getButtonStyle("#3b82f6"));
    loadBtn.setOnAction(e -> loadSavedConfiguration());

    Button exportBtn = new Button("📤 Export to File");
    exportBtn.setStyle(getButtonStyle("#f59e0b"));
    exportBtn.setOnAction(e -> exportToFile());

    Button importBtn = new Button("📥 Import from File");
    importBtn.setStyle(getButtonStyle("#8b5cf6"));
    importBtn.setOnAction(e -> importFromFile());

    Button resetBtn = new Button("🔄 Reset to Defaults");
    resetBtn.setStyle(getButtonStyle("#ef4444"));
    resetBtn.setOnAction(e -> resetToDefaults());

    box.getChildren().addAll(saveBtn, loadBtn, exportBtn, importBtn, resetBtn);
    return box;
  }

  /**
   * Crea un ColorPicker estilizado.
   *
   * @param defaultColor color por defecto
   * @return ColorPicker configurado
   */
  private ColorPicker createColorPicker(Color defaultColor) {
    ColorPicker picker = new ColorPicker(defaultColor);
    picker.setPrefWidth(200);
    picker.setOnAction(e -> applyLiveChanges());
    picker.setStyle("-fx-background-radius: 6px;");
    return picker;
  }

  /**
   * Crea una etiqueta personalizada.
   *
   * @param text texto de la etiqueta
   * @return Label estilizada
   */
  private Label createCustomLabel(String text) {
    Label label = new Label(text);
    label.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b;");
    return label;
  }

  /**
   * Obtiene el estilo para botones.
   *
   * @param colorHex color en hexadecimal
   * @return string de estilo CSS
   */
  private String getButtonStyle(String colorHex) {
    return String.format(
        """
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-background-radius: 6px;
            -fx-padding: 8 16;
            -fx-font-weight: bold;
            -fx-cursor: hand;
        """,
        colorHex);
  }

  /** Configura los listeners para cambios en tiempo real. */
  private void setupListeners() {
    primaryColorPicker.valueProperty().addListener(e -> applyLiveChanges());
    secondaryColorPicker.valueProperty().addListener(e -> applyLiveChanges());
    backgroundColorPicker.valueProperty().addListener(e -> applyLiveChanges());
    textColorPicker.valueProperty().addListener(e -> applyLiveChanges());
  }

  /** Aplica los cambios en tiempo real al preview. */
  private void applyLiveChanges() {
    try {
      // Aplicar colores al preview
      Color primary = primaryColorPicker.getValue();
      Color secondary = secondaryColorPicker.getValue();
      Color bg = backgroundColorPicker.getValue();
      Color text = textColorPicker.getValue();

      double radius = radiusSlider.getValue();
      double spacing = spacingSlider.getValue();
      double shadow = shadowIntensitySlider.getValue();

      // Actualizar preview box
      previewBox.setStyle(
          String.format(
              """
                -fx-background-color: derive(%s, 90%%);
                -fx-background-radius: %.0f;
                -fx-border-color: %s;
                -fx-border-radius: %.0f;
                -fx-border-width: 2;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,%.2f), %.0f, 0, 0, 0);
            """,
              toCssColor(bg), radius, toCssColor(primary), radius, shadow / 100.0, shadow / 5.0));

      // Actualizar botón de preview
      previewButton.setStyle(
          String.format(
              """
                -fx-background-color: %s;
                -fx-text-fill: white;
                -fx-background-radius: %.0f;
                -fx-padding: %.0f %.0f;
                -fx-font-weight: bold;
                -fx-font-size: %.1f;
            """,
              toCssColor(primary), radius, 8 * spacing, 16 * spacing, 14 * spacing));

      // Actualizar etiqueta de preview
      previewLabel.setStyle(
          String.format(
              """
                -fx-font-size: %.1f;
                -fx-text-fill: %s;
                -fx-font-weight: bold;
            """,
              18 * spacing, toCssColor(text)));

      // Actualizar tema global si está disponible
      if (themeManager != null) {
        updateThemeColors(primary, secondary, bg, text);
      }

    } catch (Exception e) {
      System.err.println("Error applying live changes: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Convierte un Color JavaFX a formato CSS.
   *
   * @param color color a convertir
   * @return string en formato CSS rgb/rgba
   */
  private String toCssColor(Color color) {
    if (color == null) {
      return "#000000";
    }
    int r = (int) (color.getRed() * 255);
    int g = (int) (color.getGreen() * 255);
    int b = (int) (color.getBlue() * 255);
    return String.format("#%02x%02x%02x", r, g, b);
  }

  /**
   * Actualiza los colores del tema global.
   *
   * @param primary color primario
   * @param secondary color secundario
   * @param bg color de fondo
   * @param text color de texto
   */
  private void updateThemeColors(Color primary, Color secondary, Color bg, Color text) {
    try {
      // Aquí se podría integrar con el sistema de temas existente
      // Por ahora, solo actualizamos las variables CSS si están disponibles
      System.out.println("Theme colors updated:");
      System.out.println("  Primary: " + toCssColor(primary));
      System.out.println("  Secondary: " + toCssColor(secondary));
      System.out.println("  Background: " + toCssColor(bg));
      System.out.println("  Text: " + toCssColor(text));
    } catch (Exception e) {
      System.err.println("Error updating theme colors: " + e.getMessage());
    }
  }

  /** Guarda la configuración actual en Preferences. */
  public void saveConfiguration() {
    try {
      prefs.putDouble("primary.r", primaryColorPicker.getValue().getRed());
      prefs.putDouble("primary.g", primaryColorPicker.getValue().getGreen());
      prefs.putDouble("primary.b", primaryColorPicker.getValue().getBlue());

      prefs.putDouble("secondary.r", secondaryColorPicker.getValue().getRed());
      prefs.putDouble("secondary.g", secondaryColorPicker.getValue().getGreen());
      prefs.putDouble("secondary.b", secondaryColorPicker.getValue().getBlue());

      prefs.putDouble("background.r", backgroundColorPicker.getValue().getRed());
      prefs.putDouble("background.g", backgroundColorPicker.getValue().getGreen());
      prefs.putDouble("background.b", backgroundColorPicker.getValue().getBlue());

      prefs.putDouble("text.r", textColorPicker.getValue().getRed());
      prefs.putDouble("text.g", textColorPicker.getValue().getGreen());
      prefs.putDouble("text.b", textColorPicker.getValue().getBlue());

      prefs.putDouble("radius", radiusSlider.getValue());
      prefs.putDouble("spacing", spacingSlider.getValue());
      prefs.putDouble("shadow", shadowIntensitySlider.getValue());

      showAlert("Success", "Configuration saved successfully!");

    } catch (Exception e) {
      showAlert("Error", "Failed to save configuration: " + e.getMessage());
    }
  }

  /** Carga la configuración guardada desde Preferences. */
  public void loadSavedConfiguration() {
    try {
      if (prefs.getDouble("primary.r", -1) != -1) {
        primaryColorPicker.setValue(
            new Color(
                prefs.getDouble("primary.r", 0.23),
                prefs.getDouble("primary.g", 0.51),
                prefs.getDouble("primary.b", 0.96),
                1.0));

        secondaryColorPicker.setValue(
            new Color(
                prefs.getDouble("secondary.r", 0.06),
                prefs.getDouble("secondary.g", 0.73),
                prefs.getDouble("secondary.b", 0.51),
                1.0));

        backgroundColorPicker.setValue(
            new Color(
                prefs.getDouble("background.r", 1.0),
                prefs.getDouble("background.g", 1.0),
                prefs.getDouble("background.b", 1.0),
                1.0));

        textColorPicker.setValue(
            new Color(
                prefs.getDouble("text.r", 0.12),
                prefs.getDouble("text.g", 0.16),
                prefs.getDouble("text.b", 0.23),
                1.0));

        radiusSlider.setValue(prefs.getDouble("radius", 8.0));
        spacingSlider.setValue(prefs.getDouble("spacing", 1.0));
        shadowIntensitySlider.setValue(prefs.getDouble("shadow", 50.0));

        applyLiveChanges();
        showAlert("Success", "Configuration loaded successfully!");
      }
    } catch (Exception e) {
      showAlert("Error", "Failed to load configuration: " + e.getMessage());
    }
  }

  /** Exporta la configuración a un archivo. */
  public void exportToFile() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Export Theme Configuration");
    fileChooser
        .getExtensionFilters()
        .add(new FileChooser.ExtensionFilter("Properties Files", "*.properties"));
    fileChooser.setInitialFileName("theme-config.properties");

    File file = fileChooser.showSaveDialog(getScene() != null ? getScene().getWindow() : null);
    if (file != null) {
      try (FileWriter writer = new FileWriter(file)) {
        writer.write("# TailwindFX Theme Configuration\n");
        writer.write("# Generated by ThemeCustomizationPanel\n\n");

        writer.write(
            String.format("primary.color=%s\n", toCssColor(primaryColorPicker.getValue())));
        writer.write(
            String.format("secondary.color=%s\n", toCssColor(secondaryColorPicker.getValue())));
        writer.write(
            String.format("background.color=%s\n", toCssColor(backgroundColorPicker.getValue())));
        writer.write(String.format("text.color=%s\n", toCssColor(textColorPicker.getValue())));

        writer.write(String.format("border.radius=%.1f\n", radiusSlider.getValue()));
        writer.write(String.format("spacing.scale=%.2f\n", spacingSlider.getValue()));
        writer.write(String.format("shadow.intensity=%.1f\n", shadowIntensitySlider.getValue()));

        showAlert("Success", "Configuration exported to:\n" + file.getAbsolutePath());

      } catch (IOException e) {
        showAlert("Error", "Failed to export configuration: " + e.getMessage());
      }
    }
  }

  /** Importa la configuración desde un archivo. */
  public void importFromFile() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Import Theme Configuration");
    fileChooser
        .getExtensionFilters()
        .add(new FileChooser.ExtensionFilter("Properties Files", "*.properties"));

    File file = fileChooser.showOpenDialog(getScene() != null ? getScene().getWindow() : null);
    if (file != null) {
      try {
        String content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
        parseAndApplyConfig(content);
        showAlert("Success", "Configuration imported from:\n" + file.getAbsolutePath());

      } catch (IOException e) {
        showAlert("Error", "Failed to import configuration: " + e.getMessage());
      }
    }
  }

  /**
   * Parsea y aplica configuración desde contenido de archivo.
   *
   * @param content contenido del archivo
   */
  private void parseAndApplyConfig(String content) {
    try {
      String[] lines = content.split("\n");
      for (String line : lines) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) continue;

        String[] parts = line.split("=");
        if (parts.length != 2) continue;

        String key = parts[0].trim();
        String value = parts[1].trim();

        switch (key) {
          case "primary.color":
            primaryColorPicker.setValue(Color.web(value));
            break;
          case "secondary.color":
            secondaryColorPicker.setValue(Color.web(value));
            break;
          case "background.color":
            backgroundColorPicker.setValue(Color.web(value));
            break;
          case "text.color":
            textColorPicker.setValue(Color.web(value));
            break;
          case "border.radius":
            radiusSlider.setValue(Double.parseDouble(value));
            break;
          case "spacing.scale":
            spacingSlider.setValue(Double.parseDouble(value));
            break;
          case "shadow.intensity":
            shadowIntensitySlider.setValue(Double.parseDouble(value));
            break;
        }
      }
      applyLiveChanges();

    } catch (Exception e) {
      throw new RuntimeException("Error parsing config: " + e.getMessage(), e);
    }
  }

  /** Resetea la configuración a los valores por defecto. */
  public void resetToDefaults() {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Reset Configuration");
    alert.setHeaderText("Reset to Defaults");
    alert.setContentText("Are you sure you want to reset all settings to default values?");

    alert
        .showAndWait()
        .ifPresent(
            response -> {
              if (response == ButtonType.OK) {
                primaryColorPicker.setValue(Color.valueOf("#3b82f6"));
                secondaryColorPicker.setValue(Color.valueOf("#10b981"));
                backgroundColorPicker.setValue(Color.valueOf("#ffffff"));
                textColorPicker.setValue(Color.valueOf("#1e293b"));

                radiusSlider.setValue(8.0);
                spacingSlider.setValue(1.0);
                shadowIntensitySlider.setValue(50.0);

                applyLiveChanges();
                showAlert("Success", "Configuration reset to defaults!");
              }
            });
  }

  /**
   * Muestra una alerta al usuario.
   *
   * @param title título de la alerta
   * @param message mensaje a mostrar
   */
  private void showAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  /**
   * Obtiene la configuración actual como string JSON.
   *
   * @return JSON con la configuración actual
   */
  public String getConfigAsJson() {
    return String.format(
        """
        {
            "colors": {
                "primary": "%s",
                "secondary": "%s",
                "background": "%s",
                "text": "%s"
            },
            "properties": {
                "borderRadius": %.1f,
                "spacingScale": %.2f,
                "shadowIntensity": %.1f
            }
        }
        """,
        toCssColor(primaryColorPicker.getValue()),
        toCssColor(secondaryColorPicker.getValue()),
        toCssColor(backgroundColorPicker.getValue()),
        toCssColor(textColorPicker.getValue()),
        radiusSlider.getValue(),
        spacingSlider.getValue(),
        shadowIntensitySlider.getValue());
  }
}
