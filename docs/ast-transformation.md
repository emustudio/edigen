---
title: AST Transformation Pipeline
---

# AST Transformation Pipeline

The parser does not build the final code-generation tree directly.
Instead, `Translator.transform(...)` runs a fixed sequence of visitors over the AST until the decoder subtree becomes a
decision tree that the generators can emit with very little extra logic.

## Pass Order

The production order is:

1. `ResolveNamesVisitor`
2. `DetectRootRulesVisitor`
3. `DetectUnusedRulesVisitor`
4. `SemanticCheckVisitor`
5. `MergePatternsVisitor`
6. `SortVisitor`
7. `SplitVisitor`
8. `PushDownVariantsVisitor`
9. `GroupVisitor`
10. `DetectAmbiguousVisitor`
11. `NarrowMasksVisitor`
12. `RemoveUnreachablePatternsVisitor`
13. `DetectUnreachableFormatsVisitor`

If the `IGNORE_UNUSED_RULES` setting is enabled, step 3 is replaced by a no-op visitor.

## Parsed AST Shape

Immediately after parsing, the decoder side is still close to the source text.
A rule contains variants, and each variant contains constants and subrules in source order.

```text
Specification
  Decoder
    Rule
      Variant
        Pattern      <- parsed constant
        Subrule
        Pattern
  Disassembler
    Format
      Value
      Value
```

That shape is easy to parse but not ideal for code generation.
The later passes convert it into a tree of masks, patterns, and leaf variants.

## Detailed Passes

### 1. ResolveNamesVisitor

This pass attaches names to actual objects.

It performs four jobs:

- collect all rule names
- resolve `Subrule` references to their `Rule`
- resolve a variant's returned subrule to the actual child `Subrule` node
- infer implicit rules for typed subrules that reference no explicit rule

The implicit-rule behavior is important.
If a subrule has a fixed length but no explicit rule definition, Edigen synthesizes a rule that simply returns the raw
bits. That keeps the rest of the pipeline uniform.

### 2. DetectRootRulesVisitor

This pass binds the declared root rule names stored in `Decoder` to actual `Rule` objects.

It also:

- preserves the declared root order
- marks rules as root rules
- records the root alias used to reach a multi-name rule
- rejects undefined roots
- rejects cases where the same logical rule would be tried multiple times under different root aliases

### 3. DetectUnusedRulesVisitor

This pass performs reachability analysis starting from the root rules.
Rules not reachable from that graph are treated as errors unless the user explicitly disabled the check.

The pass is intentionally early because unreachable rules are usually authoring mistakes, not code generation issues.

### 4. SemanticCheckVisitor

This pass validates semantic constraints that do not belong to name resolution.

It checks:

- a subrule without an explicit length may appear only at the end of a variant
- disassembler formats must not reuse the same set of values
- every value referenced by the disassembler must come from a rule that can actually return something

This is the pass that ties decoder-returning behavior to disassembler legality.

### 5. MergePatternsVisitor

This is the first major structural rewrite.

For each variant it:

- concatenates all constant fragments into one `Mask` plus one `Pattern`
- computes bit offsets for child subrules
- inserts zero-mask spans for variable-width subrules
- removes subrules that exist only as returned raw values

After this pass, each variant has a single consolidated bit-matching description instead of many separate constant nodes.

### 6. SortVisitor

This pass reorders variants by mask length before deeper restructuring.
Its purpose is to keep decode order compatible with instruction-length constraints and avoid reading beyond the current
instruction earlier than necessary.

The exact tree shape still remains variant-centric at this point.

### 7. SplitVisitor

The decoder operates on units up to `Decoder.UNIT_SIZE_BITS`, which is currently 32.

If a merged mask or pattern is longer than that, this pass splits it into a vertical chain of pieces:

```text
Variant
  Mask <= 32 bits, start 0
    Pattern
      Mask <= 32 bits, start 32
        Pattern
          ...
```

This allows the generated decoder to handle long instructions without requiring a larger primitive read unit.

### 8. PushDownVariantsVisitor

Before this pass, variants still sit near the top of the decoder tree.
After this pass, variants become leaves.

Conceptually, it changes the structure from:

```text
Rule
  Variant
    Mask
      Pattern
        ...
```

to:

```text
Rule
  Mask
    Pattern
      ...
        Variant
```

That makes the decoder tree look like a real decision tree:

- internal nodes test bits
- leaf nodes commit to a variant

### 9. GroupVisitor

This pass merges sibling masks or patterns with identical bit sequences.

Without grouping, two variants that share a common prefix would still produce duplicated branches.
After grouping, shared prefixes collapse into common decision nodes.

This pass is one of the key reasons the generated decoder becomes compact and tree-shaped instead of variant-shaped.

### 10. DetectAmbiguousVisitor

Once grouping is done, Edigen can detect ambiguity in the actual decision structure.

It checks for:

- variant ambiguity
  one pattern leading to more than one leaf variant
- path ambiguity
  different masks and patterns that become indistinguishable under the common masked bits

This pass is deliberately placed after grouping, because ambiguity is easiest to reason about on the grouped decision
tree rather than on the raw variant list.

### 11. NarrowMasksVisitor

At any rule or pattern node, only one child mask should remain on the main path.

If there are multiple child masks, this pass:

- keeps the first mask in place
- moves the other masks under a synthetic empty `Pattern`

That synthetic empty pattern acts like a default branch.
It models "if the earlier mask did not match, try the next mask."

This is how later code generation can produce nested `switch` plus `default` blocks without extra control-flow analysis.

### 12. RemoveUnreachablePatternsVisitor

If a mask contains only zero bits, then any corresponding pattern comparison is trivially true.
This pass removes that now-useless pattern layer and splices its child mask upward.

The effect is small but important:

- less dead structure in the AST
- less dead control flow in the generated decoder

### 13. DetectUnreachableFormatsVisitor

This pass operates across both decoder and disassembler concerns.

It computes the sets of decoder keys that can actually be produced by reachable decode paths, then compares those sets
with the value sets referenced by disassembler formats.

It reports:

- unreachable formats as errors
- missing formats as informational output

The analysis is iterative so recursive rule references can participate in the reachable-set computation.

## Resulting Decoder Tree Shape

After the full pipeline, the decoder subtree is close to this:

```text
Decoder
  Rule
    Mask
      Pattern
        Mask
          Pattern
            Variant
```

That is the shape expected by `GenerateMethodsVisitor`.

## Why This Pipeline Works Well

The pass sequence gradually moves from meaning to structure:

- early passes resolve names and validate intent
- middle passes normalize variants into bit-test nodes
- later passes compact the tree and remove invalid or useless branches
- the final tree is simple enough that generation is mostly formatting

This is why the generators themselves stay relatively small.
Most of the real compiler work happens before code emission starts.
