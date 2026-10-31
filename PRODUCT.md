# Edigen Product Context

<!-- impeccable:product-schema 1 -->

## Platform

JVM command-line tool, with developer documentation rendered as HTML. The confirmed
scope is the existing CLI and developer documentation.

## Users

CPU emulator developers who describe processor instruction sets, configure source
generation during a build, and integrate generated Java classes into emuStudio CPU
plugins. Contributors and integrators also use the language reference and architecture
documentation to understand and maintain the generator.

## Product Purpose

Generate Java instruction decoders and disassemblers from a plain-text `.eds`
specification. Edigen reduces the tedious, error-prone work of maintaining these
classes by hand. Success means a developer can describe an instruction set and
generate classes that implement the emuLib decoder and disassembler contracts.

## Positioning

Edigen combines an instruction-set DSL with Java source generation for the emuStudio
ecosystem. One specification describes instruction formats, operation codes, decoded
fields, and mnemonic formatting.

## Operating Context

- Developers write UTF-8 `.eds` files with decoder and disassembler sections separated
  by `%%`.
- The recommended workflow invokes Edigen through the separate Gradle or Maven build
  plugins, then compiles the generated Java sources into a CPU plugin.
- Developers can also invoke the generator directly as a JAR; running it without
  arguments displays the command-line options.
- The documentation covers both the EDS language and the compiler architecture.
  `./gradlew doc` renders it into `docs/output`.

## Capabilities and Constraints

- Edigen parses and validates specifications, transforms an AST through ordered
  visitor passes, and expands templates into Java decoder and disassembler classes.
- Generated classes implement `net.emustudio.emulib.plugins.cpu.Decoder` and
  `net.emustudio.emulib.plugins.cpu.Disassembler`.
- Custom decoder and disassembler templates are supported through `-dt` and `-at`.
  Debug mode (`-d`) exposes the AST after each pass.
- This repository owns the EDS language, parser, and generated code shape. Build
  integration, shared runtime APIs, and bundled CPU plugins belong to their respective
  sibling repositories, as described in `AGENTS.md`.
- Documentation must distinguish the generator, the build plugins, and the runtime
  that consumes generated code.

## Brand Commitments

Use the existing name **Edigen**, expanded as **Emulator Disassembler Generator**,
and preserve the established names EDS, emuStudio, and emuLib. No additional voice
or visual direction has been established in this initialization.

## Evidence on Hand

- `README.md`: purpose, invocation options, and a small EDS example.
- `docs/index.adoc` and `docs/_c4-*.adoc`: architecture and integration workflow.
- `docs/_eds-language.adoc`: language syntax and semantics.
- `src/main/resources/Decoder.edt` and `Disassembler.edt`: generation templates.
- `src/test`: executable verification examples for the parser and generation.

## Product Principles

- Keep instruction descriptions in the specification and generate the repetitive Java.
- Treat the emuLib interfaces as the contract for generated output.
- Explain language concepts and workflows before compiler internals.
- Ground documentation examples and capability claims in the implemented language
  and generator.

## Open Decisions

No product-specific accessibility requirements or additional audiences have been
established. Future work should confirm these when relevant.
