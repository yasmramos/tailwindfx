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

import io.github.yasmramos.tailwindfx.style.StylePerf;

/**
 * TwBatch — Batch operations facade for performance optimization.
 *
 * <pre>
 * TwBatch.run(() -> {
 *     TwStyle.apply(node1, "p-4", "bg-blue-500");
 *     TwStyle.apply(node2, "m-2", "text-white");
 * });
 * </pre>
 */
public final class TwBatch {

  private TwBatch() {}

  /** Executes a batch of style operations efficiently. */
  public static void run(Runnable action) {
    StylePerf.batch(action);
  }

  /** Executes a batch of style operations asynchronously. */
  public static void runAsync(Runnable action) {
    StylePerf.batchAsync(action);
  }
}
