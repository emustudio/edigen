---
title: Code Generation
---

# Code Generation

Once the AST has been normalized, Edigen switches from analysis to rendering.
The generation layer is deliberately thin: most of its job is to walk the normalized tree and fill the shipped templates.

## Main Generator Classes

### Generator

`net.emustudio.edigen.generation.Generator` is the common base class.
It handles:

- package/class name extraction
- template selection
- output file creation
- opening the template and output streams
- common template variable setup such as `%auto_gen_warning%`

Subclasses only need to provide the template-specific variables.

### DecoderGenerator

`DecoderGenerator` fills these decoder-specific template variables:

- `%decoder_package%`
- `%decoder_class%`
- `%root_rule%`
- `%decoder_fields%`
- `%decoder_methods%`

It delegates the actual code fragments to visitors:

- `GenerateFieldsVisitor`
- `GenerateMethodsVisitor`

### DisassemblerGenerator

`DisassemblerGenerator` fills:

- `%disasm_package%`
- `%disasm_class%`
- `%decoder_name%`
- `%disasm_formats%`
- `%disasm_parameters%`

It delegates fragment generation to:

- `GenerateFormatsVisitor`
- `GenerateParametersVisitor`

## Supporting Rendering Utilities

### Template

`Template` is a minimal placeholder engine.
It understands two kinds of variables:

- inline variables
  replaced directly inside a line
- block variables
  a whole line consisting only of indentation and `%name%`

Block variables are important for code generation because they preserve indentation for multi-line inserted source.

### PrettyPrinter

Generator visitors emit Java one line at a time.
`PrettyPrinter` adds indentation heuristically based on braces and labels.

It is not a general formatter.
It assumes the generator already emits sensible Java line structure.

## Decoder Code Generation

### Field constants

`GenerateFieldsVisitor` walks the decoder subtree and emits integer constants for:

- rules that can return something
- string-returning variants

Those constants become the decoder keys used by both generated classes.

The decoder template exposes them as `public static final int` fields so the disassembler can import them with:

```java
import static <decoder_name>.*;
```

### Rule methods

`GenerateMethodsVisitor` is the core decoder source emitter.

It generates one private method per rule.
The normalized AST maps naturally onto Java control flow:

- `Rule`
  becomes a private method
- `Mask`
  becomes `readBits(...)` plus a `switch`
- `Pattern`
  becomes `case ...:` or `default:`
- `Variant`
  emits `instruction.add(...)`
- `Subrule`
  becomes a recursive method call into another generated rule method

The generated methods are tree-driven, not interpreter-driven.
They directly encode the normalized decision tree built by the passes.

### Root rule invocation

`DecoderGenerator` computes `%root_rule%` from the first resolved root rule.
If the root rule has multiple names, the emitted invocation includes the decoder key constant that identifies the root
alias used at the call site.

That keeps multi-name rules working without duplicating generated methods.

### Fallback across root rules

If multiple root rules exist, `GenerateMethodsVisitor` wires them into the generated `default` path.
If the first root rule fails to match, the next root rule is invoked, preserving declaration order from the source file.

## Disassembler Code Generation

### Format strings

`GenerateFormatsVisitor` writes the static `String[] formats` initializer used by `Disassembler.edt`.
Each AST `Format` node contributes its literal format string.

### Parameter descriptors

`GenerateParametersVisitor` emits the `Parameter[][]` initializer.
Each emitted `Parameter` contains:

- the decoder key constant for a rule
- the strategy list to apply before formatting numeric values

This means the template does not need to know anything about the AST.
It consumes a simple, already-materialized runtime description.

## Code Generation Contract

The normalized AST and the templates have a clear contract:

- passes must produce a decoder tree that can be rendered as nested masks, patterns, variants, and subrule calls
- generators must translate that tree into Java fragments
- templates provide the shared runtime shell around those fragments

This split keeps responsibilities narrow:

- passes decide structure
- visitors decide Java syntax for the structure
- templates decide the common runtime scaffold

## Generated Output Shape

At a high level, Edigen produces:

```text
generated decoder class
  shared runtime helpers from Decoder.edt
  +
  generated constants and rule methods

generated disassembler class
  shared runtime helpers from Disassembler.edt
  +
  generated format arrays and parameter arrays
```

## Why Generation Is Template-Based

Using templates instead of constructing entire files in Java gives Edigen two practical advantages:

- the generated runtime scaffold is easy to inspect and customize
- advanced users can replace the default templates without changing the AST or visitor pipeline

The result is a pipeline where the compiler core and the emitted runtime are loosely coupled but still strongly typed
through the template variable contract.
