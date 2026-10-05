package io.github.yasmramos.tailwindfx.testing;

import io.github.yasmramos.tailwindfx.ToolkitBootstrap;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 extension that guarantees the JavaFX toolkit is started before any test class runs.
 *
 * <p>Registered globally via {@code junit-platform.properties} (auto-detection), so plain unit
 * tests can instantiate JavaFX controls (Button, Label, ...) without extending a TestFX {@code
 * ApplicationTest}.
 */
public final class JavaFxToolkitExtension implements BeforeAllCallback {

  @Override
  public void beforeAll(ExtensionContext context) {
    ToolkitBootstrap.ensureStarted();
  }
}
