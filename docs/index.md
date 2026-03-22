---
title: Edigen Internals
---

# Edigen Internals

This site documents the current Edigen implementation from parser to generated Java source.
It focuses on:

- system architecture
- AST shape and visitor model
- the transformation pipeline in `Translator`
- decoder and disassembler code generation
- the shipped `Decoder.edt` and `Disassembler.edt` templates

## Reading Order

1. [Architecture](architecture.md)
2. [AST Transformation Pipeline](ast-transformation.md)
3. [Code Generation](code-generation.md)
4. [Templates](templates.md)

## End-to-End Pipeline

```text
.eds specification
    ->
JavaCC parser (`Grammar.jj`)
    ->
AST (`Specification`)
    ->
semantic + structural visitor passes
    ->
generator visitors
    ->
template expansion (`Decoder.edt`, `Disassembler.edt`)
    ->
generated Java classes
```

## Main Runtime Components

The implementation is centered around these layers:

- `net.emustudio.edigen.Edigen`
  CLI entry point.
- `net.emustudio.edigen.Translator`
  Orchestrates parsing, AST transformation, and code generation.
- `net.emustudio.edigen.parser.Parser`
  Parses the `.eds` source into an AST.
- `net.emustudio.edigen.nodes.*`
  AST model shared by all passes and generators.
- `net.emustudio.edigen.passes.*`
  In-place AST analysis and normalization visitors.
- `net.emustudio.edigen.generation.*`
  Generator classes and source-emitting visitors.
- `src/main/resources/Decoder.edt`
  Decoder template.
- `src/main/resources/Disassembler.edt`
  Disassembler template.

## Design Summary

Edigen does not generate code directly from the parser. It first builds a generic AST, then mutates that AST through
a fixed sequence of visitors until the tree has a shape that is easy to emit as Java source. The decoder and
disassembler generators then walk that normalized tree and inject generated fragments into text templates.

This split is the key architectural idea in the project:

- the parser is responsible for syntax only
- semantic meaning is attached by visitors
- decoder-specific optimizations happen as tree rewrites
- code generation is mostly a rendering step over a normalized tree

## Notes About Current Behavior

The documentation here follows the current implementation, including a few places where the code is more authoritative
than the README:

- root declarations use `root instruction;` syntax in the current grammar
- the default disassembler template implements strategy constants `reverse_bytes`, `bit_reverse`, `absolute`,
  `shift_left`, and `shift_right`

If you need the detail behind those statements, the later pages point to the relevant classes and template sections.
