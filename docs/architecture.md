---
title: Architecture
---

# Architecture

Edigen is a source-to-source compiler for a small DSL.
Its input is an `.eds` file and its output is Java source code for:

- a decoder implementing `net.emustudio.emulib.plugins.cpu.Decoder`
- a disassembler implementing `net.emustudio.emulib.plugins.cpu.Disassembler`

## High-Level Structure

```text
CLI
  Edigen
    ->
Translation orchestration
  Translator
    ->
Parsing
  Parser (generated from Grammar.jj)
    ->
AST
  Specification
    |- Decoder subtree
    `- Disassembler subtree
    ->
Visitor passes
  semantic checks + structural rewrites
    ->
Generators
  DecoderGenerator / DisassemblerGenerator
    ->
Template rendering
  Template + PrettyPrinter + *.edt
    ->
Generated Java source
```

## Entry Points

### CLI layer

`net.emustudio.edigen.Edigen` is the command-line entry point.
Its responsibilities are small by design:

- print startup and error messages
- parse command-line arguments
- create `Translator`
- terminate with a non-zero exit code on failure

This keeps the actual compiler pipeline out of the CLI.

### Translation layer

`net.emustudio.edigen.Translator` is the real application core.
It performs three steps:

1. parse the input file into a `Specification`
2. run the fixed visitor pipeline over the AST
3. run the decoder and disassembler generators

That class is the best place to understand the implementation order, because `transform(...)` defines the exact pass
sequence used in production.

## Parsing Layer

The parser grammar is defined in `src/main/javacc/Grammar.jj`.
JavaCC generates `net.emustudio.edigen.parser.Parser`.

The parser is intentionally lightweight:

- it recognizes decoder and disassembler syntax
- it creates AST nodes
- it attaches source line numbers
- it does not attempt to resolve names or normalize structure

That separation matters because later visitors need to reason over the tree as a whole, including forward references
and cross-links between decoder and disassembler sections.

## AST Model

The root node is `Specification`, which always contains two subtrees:

- `Decoder`
- `Disassembler`

All AST nodes extend `TreeNode`.
`TreeNode` provides:

- parent/child relationships
- insertion-order child storage
- in-place mutation helpers such as `addChild(...)` and `remove()`
- deep copying via `copy()`
- tree dumping via `dump(...)`
- source line association

The important domain nodes are:

- `Rule`
  A decoder rule, possibly with multiple names.
- `Variant`
  One branch of a rule.
- `Subrule`
  A reference to another rule, or a value-capturing field.
- `Pattern`
  A bit pattern.
- `Mask`
  A bit mask used during matching.
- `Format`
  One disassembler output format string.
- `Value`
  One disassembler parameter, bound to a decoder rule name.

## Visitor Model

All analysis, validation, transformation, and emission logic is expressed as subclasses of `Visitor`.

This is the central extension mechanism in the project.
The base `Visitor` implementation simply traverses children, so each specialized visitor overrides only the node types
it cares about.

That leads to a consistent structure:

- semantic passes mutate or validate the same AST in place
- generator visitors render source from the normalized AST
- adding a new pass usually does not require changing node classes

## Why the AST Is Mutated In Place

Edigen does not build a separate IR for every compiler phase.
Instead, the original AST is progressively rewritten into a shape that is closer to code generation.

This has two practical benefits:

- passes can reuse the same node types and tree utilities
- generated source visitors can be simple because the hard structural work is already done

The tradeoff is that pass ordering is important and part of the architecture.

## Generation Layer

The generation package has two public generators:

- `DecoderGenerator`
- `DisassemblerGenerator`

Both inherit from `Generator`, which handles:

- package/class name splitting
- template selection
- output file creation
- common template variable setup

The actual generated fragments come from dedicated visitors:

- `GenerateFieldsVisitor`
- `GenerateMethodsVisitor`
- `GenerateFormatsVisitor`
- `GenerateParametersVisitor`

## Template Layer

The final Java file is not assembled manually with string concatenation.
Instead, a generator fills a `Template` object and writes into a `.edt` file.

Two helpers are important here:

- `Template`
  Replaces `%name%` variables, with special handling for block variables so inserted code keeps indentation.
- `PrettyPrinter`
  Adds indentation while generator visitors emit Java line-by-line.

The shipped templates are:

- `src/main/resources/Decoder.edt`
- `src/main/resources/Disassembler.edt`

## Generated Runtime Model

The generated decoder and disassembler are not just thin wrappers around emitted switch statements.
The templates also define the runtime support code that every generated class shares.

The decoder template currently provides:

- memory-backed bit reading
- instruction image buffering
- a verify-on-read LRU cache keyed by memory address
- generated rule methods

The disassembler template currently provides:

- format lookup by decoded rule set
- constant-decoding strategies
- a last-position decode cache
- a last-render cache for mnemonic and byte string reuse

## Extension Points

The architecture exposes two supported extension points without changing Java code:

- custom decoder template via `-dt`
- custom disassembler template via `-at`

Because the generators produce template variables rather than final files directly, template replacement is a first-class
way to customize the emitted runtime while keeping the AST pipeline unchanged.

## Debugging the Pipeline

`Translator` supports a debug mode that dumps the AST after each pass.
Architecturally, this is important because the transformation pipeline is where most of the real compiler logic lives.
If generated output looks wrong, the fastest way to reason about it is usually to inspect the post-pass tree rather than
the final Java.
