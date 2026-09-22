/*
 * Copyright 2026 Yasmany Ramos García (yasmramos).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.yasmramos.tailwindfx;

import io.github.yasmramos.tailwindfx.style.StyleToken;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.Parent;

/**
 * Utility class for processing JavaFX scene graphs with Tailwind CSS classes, including support for
 * JIT/arbitrary values that require inline style compilation.
 *
 * <p>This class provides mechanisms to apply Tailwind utilities to nodes that were assigned via
 * {@code getStyleClass().addAll(...)} or declared in FXML with {@code styleClass="..."}, ensuring
 * that arbitrary/JIT tokens (e.g., {@code bg-[#ff0000]/80}, {@code w-[320px]}) are properly
 * compiled to inline styles.
 *
 * <h2>Usage Patterns</h2>
 *
 * <h3>1. One-time processing after FXML loading</h3>
 *
 * <pre>{@code
 * Parent root = FXMLLoader.load(getClass().getResource("view.fxml"));
 * TwFXML.process(root);
 * // Now all nodes have their JIT tokens compiled to inline styles
 * }</pre>
 *
 * <h3>2. Dynamic processing with automatic JIT compilation</h3>
 *
 * <pre>{@code
 * Parent root = FXMLLoader.load(getClass().getResource("view.fxml"));
 * TwFXML.enableAutoJit(root);
 * // Now any class added via getStyleClass().add(...) will be auto-compiled if it's a JIT token
 * }</pre>
 *
 * @author yasmramos
 * @since 1.0.0
 */
public final class TwFXML {

  private static final String AUTO_JIT_ENABLED_KEY = "twfxml.autoJitEnabled";

  private TwFXML() {
    // Prevent instantiation
  }

  /**
   * Processes a JavaFX scene graph recursively, applying Tailwind styles to all nodes.
   *
   * <p>This method traverses the node tree starting from the given root node and calls {@link
   * TwStyle#apply(Node, String...)} for each node that has style classes. This ensures that
   * JIT/arbitrary tokens are compiled to inline styles, while utility classes are resolved with
   * proper conflict detection.
   *
   * <p>Static utility classes (e.g., {@code text-xl}, {@code font-bold}) that are present in the
   * base stylesheet will remain as style classes. JIT/arbitrary tokens (e.g., {@code
   * bg-[#ff0000]/80}, {@code w-[320px]}) will be compiled to inline styles.
   *
   * @param root the root node of the scene graph to process
   * @throws IllegalArgumentException if root is null
   */
  public static void process(Parent root) {
    if (root == null) {
      throw new IllegalArgumentException("Root node cannot be null");
    }
    processNode(root);
    traverseAndProcess(root);
  }

  /**
   * Enables automatic JIT compilation for style classes added dynamically.
   *
   * <p>This method installs a {@link javafx.collections.ListChangeListener} on each node's style
   * class list. When a JIT/arbitrary token is added (e.g., via {@code
   * node.getStyleClass().add("bg-[#ff0000]/80")}), it is automatically compiled to an inline style.
   *
   * <p>The listener uses a flag stored in {@link Node#getProperties()} to prevent infinite
   * recursion when the framework itself modifies style classes.
   *
   * <p><strong>Note:</strong> This is an opt-in feature due to performance considerations. For most
   * use cases, calling {@link #process(Parent)} once after FXML loading is sufficient.
   *
   * @param root the root node of the scene graph to enable auto-JIT on
   * @throws IllegalArgumentException if root is null
   * @see #disableAutoJit(Parent)
   * @see TwConfig#autoJitFromStyleClass(boolean)
   */
  public static void enableAutoJit(Parent root) {
    if (root == null) {
      throw new IllegalArgumentException("Root node cannot be null");
    }
    enableAutoJitOnNode(root);
    traverseAndEnableAutoJit(root);
  }

  /**
   * Disables automatic JIT compilation previously enabled by {@link #enableAutoJit(Parent)}.
   *
   * <p>This removes all listeners installed by {@code enableAutoJit} and clears the auto-JIT flag
   * from all nodes in the scene graph.
   *
   * @param root the root node of the scene graph to disable auto-JIT on
   * @throws IllegalArgumentException if root is null
   */
  public static void disableAutoJit(Parent root) {
    if (root == null) {
      throw new IllegalArgumentException("Root node cannot be null");
    }
    disableAutoJitOnNode(root);
    traverseAndDisableAutoJit(root);
  }

  /**
   * Checks if a style class token is a JIT/arbitrary token that requires inline compilation.
   *
   * @param className the style class name to check
   * @return true if the token is a JIT/arbitrary token, false otherwise
   */
  static boolean isJitToken(String className) {
    if (className == null || className.isEmpty()) {
      return false;
    }
    // Arbitrary values contain [...]
    if (className.contains("[") && className.contains("]")) {
      return true;
    }
    // Opacity modifiers contain /digit
    if (className.contains("/")) {
      String[] parts = className.split("/");
      if (parts.length == 2 && parts[1].matches("\\d+")) {
        return true;
      }
    }
    // Check against StyleToken patterns for known JIT utilities
    try {
      StyleToken token = StyleToken.parse(className);
      return token != null && token.kind == StyleToken.Kind.ARBITRARY;
    } catch (Exception e) {
      return false;
    }
  }

  private static void processNode(Node node) {
    if (node == null) {
      return;
    }

    List<String> styleClasses = new ArrayList<>(node.getStyleClass());
    if (!styleClasses.isEmpty()) {
      // Separate JIT tokens from regular classes
      List<String> jitTokens = new ArrayList<>();
      List<String> regularClasses = new ArrayList<>();

      for (String cls : styleClasses) {
        if (isJitToken(cls)) {
          jitTokens.add(cls);
        } else {
          regularClasses.add(cls);
        }
      }

      // Apply JIT tokens as inline styles
      if (!jitTokens.isEmpty()) {
        TwStyle.apply(node, jitTokens.toArray(new String[0]));
      }

      // Re-add regular classes that might have been removed by TwStyle.apply
      // (TwStyle.apply may remove classes it processed, but we want to keep non-Tailwind classes)
      for (String cls : regularClasses) {
        if (!node.getStyleClass().contains(cls)) {
          node.getStyleClass().add(cls);
        }
      }
    }
  }

  private static void traverseAndProcess(Parent parent) {
    if (parent == null) {
      return;
    }

    List<Node> children = parent.getChildrenUnmodifiable();
    for (Node child : children) {
      processNode(child);
      if (child instanceof Parent) {
        traverseAndProcess((Parent) child);
      }
    }
  }

  private static void enableAutoJitOnNode(Node node) {
    if (node == null) {
      return;
    }

    // Check if already enabled to avoid duplicate listeners
    if (Boolean.TRUE.equals(node.getProperties().get(AUTO_JIT_ENABLED_KEY))) {
      return;
    }

    node.getProperties().put(AUTO_JIT_ENABLED_KEY, Boolean.TRUE);

    node.getStyleClass()
        .addListener(
            (javafx.collections.ListChangeListener<String>)
                change -> {
                  // Skip if auto-JIT is disabled or in the middle of framework modifications
                  Object enabledObj = node.getProperties().get(AUTO_JIT_ENABLED_KEY);
                  if (!Boolean.TRUE.equals(enabledObj)
                      || Boolean.TRUE.equals(node.getProperties().get("tw.internalModification"))) {
                    return;
                  }

                  // Collect added JIT tokens first to avoid ConcurrentModificationException
                  java.util.List<String> jitTokensToAdd = new java.util.ArrayList<>();
                  while (change.next()) {
                    if (change.wasAdded()) {
                      for (String addedClass : change.getAddedSubList()) {
                        if (isJitToken(addedClass)) {
                          jitTokensToAdd.add(addedClass);
                        }
                      }
                    }
                  }

                  // Process collected JIT tokens
                  if (!jitTokensToAdd.isEmpty()) {
                    // Mark internal modification to prevent recursion
                    node.getProperties().put("tw.internalModification", Boolean.TRUE);
                    try {
                      for (String jitToken : jitTokensToAdd) {
                        // Remove the JIT class and apply it as inline style
                        node.getStyleClass().remove(jitToken);
                      }
                      TwStyle.apply(node, jitTokensToAdd.toArray(new String[0]));
                    } finally {
                      node.getProperties().put("tw.internalModification", Boolean.FALSE);
                    }
                  }
                });
  }

  private static void traverseAndEnableAutoJit(Parent parent) {
    if (parent == null) {
      return;
    }

    List<Node> children = parent.getChildrenUnmodifiable();
    for (Node child : children) {
      enableAutoJitOnNode(child);
      if (child instanceof Parent) {
        traverseAndEnableAutoJit((Parent) child);
      }
    }
  }

  private static void disableAutoJitOnNode(Node node) {
    if (node == null) {
      return;
    }

    // Mark as permanently disabled so listener stops processing
    node.getProperties().put(AUTO_JIT_ENABLED_KEY, Boolean.FALSE);
  }

  private static void traverseAndDisableAutoJit(Parent parent) {
    if (parent == null) {
      return;
    }

    List<Node> children = parent.getChildrenUnmodifiable();
    for (Node child : children) {
      disableAutoJitOnNode(child);
      if (child instanceof Parent) {
        traverseAndDisableAutoJit((Parent) child);
      }
    }
  }
}
