# New Features Added to TailwindFX

## 1. Gradient Support (from-*, to-*, via-*, bg-gradient-to-*)

### Problem Solved
Previously, gradient tokens like `from-blue-500`, `to-purple-600`, and `bg-gradient-to-b` were unrecognized by the JIT compiler, causing warnings.

### Solution Implemented
Modified `JitCompiler.compileBatch()` to:
- Detect gradient direction tokens (`bg-gradient-to-t`, `bg-gradient-to-b`, `bg-gradient-to-r`, etc.)
- Detect gradient color stops (`from-*`, `via-*`, `to-*`)
- Automatically resolve Tailwind color names to hex values
- Build proper `linear-gradient()` CSS expressions
- Suppress warnings for gradient-related tokens

### Example Usage
```java
// Header with gradient background
TailwindFX.jit(header, "p-5", "bg-gradient-to-b", "from-gray-800", "to-gray-900");

// Logo with gradient
TailwindFX.jit(logo, "bg-gradient-to-br", "from-blue-500", "to-purple-600", "rounded-xl");

// Three-color gradient
TailwindFX.jit(banner, "bg-gradient-to-r", "from-blue-500", "via-purple-500", "to-pink-500");
```

### Supported Directions
- `bg-gradient-to-t` (top)
- `bg-gradient-to-tr` (top right)
- `bg-gradient-to-r` (right)
- `bg-gradient-to-br` (bottom right)
- `bg-gradient-to-b` (bottom)
- `bg-gradient-to-bl` (bottom left)
- `bg-gradient-to-l` (left)
- `bg-gradient-to-tl` (top left)

## 2. Overflow Utilities

Added support for overflow utilities (Note: JavaFX handles overflow differently with ScrollPane):
- `overflow-auto`
- `overflow-hidden`
- `overflow-scroll`
- `overflow-x-auto`
- `overflow-y-auto`

## 3. Display Utilities

- `block`
- `inline`
- `inline-block`
- `hidden` (sets `-fx-visible: false`)
- `contents`

## 4. Position Utilities

Note: JavaFX uses layout panes instead of CSS positioning. These tokens provide guidance:
- `static`
- `relative`
- `absolute`
- `fixed`
- `sticky`

## 5. Backdrop Filter Utilities

Note: JavaFX requires Java Effects for backdrop filters:
- `backdrop-blur-none`
- `backdrop-blur-sm`
- `backdrop-blur`
- `backdrop-blur-md`
- `backdrop-blur-lg`
- `backdrop-blur-xl`
- `backdrop-blur-2xl`
- `backdrop-blur-3xl`

## 6. Ring Utilities

Border ring utilities with default gray color:
- `ring-0` (0px)
- `ring-1` (1px)
- `ring-2` (2px)
- `ring` (3px)
- `ring-4` (4px)
- `ring-8` (8px)

With colors:
- `ring-blue-500`
- `ring-red-500`
- etc.

## 7. Border Style Utilities

- `border-solid`
- `border-dashed`
- `border-dotted`
- `border-none`
- `border-0`
- `border` (1px solid #E5E7EB)
- `border-2`
- `border-4`
- `border-8`

## 8. Transition Utilities

Note: JavaFX uses Animation API instead of CSS transitions:
- `transition-none`
- `transition-all`
- `transition`
- `transition-colors`
- `transition-opacity`
- `transition-shadow`
- `transition-transform`

## 9. Enhanced Gradient Support in Arbitrary Values

Enhanced `bg-gradient-[...]` arbitrary value syntax:
```java
TailwindFX.jit(node, "bg-gradient-[to_right,blue-500,purple-600]");
TailwindFX.jit(node, "bg-gradient-[to_bottom,#3b82f6,#8b5cf6]");
```

## 10. Arbitrary Ring Colors

Support for custom ring colors:
```java
TailwindFX.jit(node, "ring-[#3b82f6]");
```

## Summary

### Total New Utilities Added
- 8 gradient direction utilities
- 3 gradient color stop utilities (from, via, to)
- 5 overflow utilities
- 5 display utilities
- 5 position utilities
- 8 backdrop blur utilities
- 6 ring utilities
- 8 border style utilities
- 7 transition utilities

### Total: ~57 new utility tokens supported!

### Warnings Eliminated
✅ No more warnings for `from-*`, `to-*`, `via-*` tokens
✅ No more warnings for `bg-gradient-to-*` tokens
✅ Gradient colors properly resolved from Tailwind color palette

### Files Modified
- `src/main/java/tailwindfx/JitCompiler.java`
  - Enhanced `compileBatch()` method with gradient detection
  - Added `resolveGradientColor()` helper method
  - Added `buildGradient()` helper method
  - Added support in `compileNamed()` for new utilities
  - Added support in `compileArbitrary()` for ring and gradient colors

### Backward Compatibility
✅ All existing utilities continue to work
✅ Gradient utilities work seamlessly with existing color system
✅ No breaking changes
