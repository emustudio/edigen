---
title: Templates
---

# Templates

Edigen ships two text templates:

- `src/main/resources/Decoder.edt`
- `src/main/resources/Disassembler.edt`

They define the common Java runtime scaffold around the generated fragments.

## Template Engine Basics

Edigen uses the `Template` helper, not a third-party templating library.
Variables are written as `%name%`.

There are two replacement modes:

### Inline variables

Inline variables are replaced in place.

Example:

```text
package %decoder_package%;
```

### Block variables

If a line contains only indentation plus one variable, the variable is treated as a block and every inserted line is
indented to match.

Example:

```text
    %decoder_methods%
```

This is how the templates inject multi-line Java source without manual indentation logic in the template itself.

## Decoder.edt

`Decoder.edt` defines the generated decoder class shell.

### Variables used by the template

The current template expects:

- `%auto_gen_warning%`
- `%decoder_package%`
- `%decoder_class%`
- `%decoder_fields%`
- `%root_rule%`
- `%decoder_methods%`

### What the template provides

The decoder template contributes the shared runtime pieces that every generated decoder gets:

- imports and class declaration
- byte buffer for the current instruction image
- memory context reference
- decode state fields such as `memoryPosition`, `loadedInstructionBytes`, `unit`, and `bitsRead`
- verify-on-read LRU cache of decoded instructions
- helper methods for byte copying and capacity growth
- `readBits(...)`
- the top-level `decode(...)` method

### How `decode(...)` works

The template-level `decode(...)` method handles runtime mechanics, not pattern matching itself.

Its flow is:

1. set the current memory position
2. check the address-based cache
3. on cache hit, re-read the cached image bytes and compare them
4. if bytes still match, return the cached `DecodedInstruction`
5. otherwise initialize a fresh `DecodedInstruction`
6. execute `%root_rule%`
7. finalize and store the instruction image
8. cache and return the decoded instruction

The actual decode logic starts only at step 6, where `%root_rule%` expands to the first generated rule method call.

### How generated rule methods plug in

`%decoder_methods%` inserts the output of `GenerateMethodsVisitor`.
Those methods expect the helper API defined by the template:

- `readBits(...)`
- mutable `instruction`
- mutable `unit`
- `InvalidInstructionException`

That means the template and the generator are tightly coordinated.
Neither side makes sense in isolation.

### How to read the generated decoder methods

A generated rule method usually looks like this structurally:

```java
private void ruleName(int start) throws InvalidInstructionException {
    unit = readBits(start + offset, length);

    switch (unit & mask) {
    case pattern:
        instruction.add(...);
        childRule(...);
        break;
    default:
        throw new InvalidInstructionException();
    }
}
```

The exact nesting depends on the normalized AST, but the mapping is consistent:

- one method per rule
- one masked switch per mask node
- one case per pattern node
- one block of `instruction.add(...)` calls per matched variant

### `readBits(...)`

The template-level `readBits(...)` implementation is the core bit-extraction primitive.
It:

- ensures enough instruction bytes have been loaded
- reads the relevant byte span
- assembles it into a `long`
- applies a shift and mask
- returns an `int`

The transformation pipeline exists partly to guarantee that generated rule methods can stay within this read model.

## Disassembler.edt

`Disassembler.edt` defines the generated disassembler class shell.

### Variables used by the template

The current template expects:

- `%auto_gen_warning%`
- `%disasm_package%`
- `%disasm_class%`
- `%decoder_name%`
- `%disasm_formats%`
- `%disasm_parameters%`

### What the template provides

The disassembler template contributes:

- imports and class declaration
- static import of decoder constants
- small runtime data holders `MnemonicFormat` and `Parameter`
- a `Strategy` constant holder
- a static `formatMap`
- decode and render caches
- mnemonic rendering logic
- byte-image formatting helpers

### How formats are materialized

The generator does not emit the whole disassembler logic.
Instead it emits two arrays:

- `String[] formats`
- `Parameter[][] parameters`

The template combines them into `formatMap`, keyed by `Set<Integer>` of decoder rule codes.

That design means disassembler format lookup is based on the set of keys present in a decoded instruction, not on value
order in the original format declaration.

### `disassemble(...)`

The template-level `disassemble(...)` method does four things:

1. obtain a decoded instruction through `cachedDecode(...)`
2. reuse the last rendered mnemonic if both the address and decoded instruction object match
3. look up the matching format by the instruction key set
4. render either the mnemonic or fallback text

Fallback behavior is part of the template contract:

- no matching mnemonic format yields `"N/A"`
- invalid decode yields `"unknown"` and formats only the first byte

### `getNextInstructionPosition(...)`

This method also uses `cachedDecode(...)`.
That cache exists to avoid calling `decoder.decode(...)` twice for the common pair:

- `disassemble(address)`
- `getNextInstructionPosition(address)`

### `createMnemonic(...)`

Mnemonic rendering is left-to-right placeholder substitution over the format string.

For each parameter:

1. find the next `%`
2. fetch the corresponding decoded value
3. if the value is numeric, apply strategies and call `Formatter`
4. otherwise read the string value from `DecodedInstruction`
5. replace the two-character placeholder with the rendered text

The format string can itself come from a string-returning decoder rule, which is how rule names containing format
specifiers work with the default template.

### Strategy handling

The shipped template currently implements these strategy constants:

- `reverse_bytes`
- `bit_reverse`
- `absolute`
- `shift_left`
- `shift_right`

The `Value` AST node can carry any strategy names, but the default template only understands the constants it defines.
If a custom template wants more strategies, it must implement them itself.

### Byte-image formatting

The template also renders the raw instruction bytes as uppercase hexadecimal pairs separated by spaces.
This logic is not generated per ISA; it is shared runtime code for every generated disassembler.

## Decoder and Disassembler Template Relationship

The two templates are coupled through decoder key constants.

The decoder template publishes integer constants for rule names and string-returning variants.
The disassembler template imports those constants and uses them in generated `Parameter` objects and format lookup.

That relationship is why the disassembler generator needs the decoder's fully qualified class name.

## Practical Rules for Custom Template Authors

If you replace the shipped templates, keep these contracts in mind.

### Decoder template contract

Your template must provide everything that generated decoder fragments expect:

- a mutable `instruction`
- a mutable `unit`
- `readBits(...)`
- `decode(...)` entry point
- the exception model used by generated methods

### Disassembler template contract

Your template must provide everything that generated format and parameter fragments expect:

- access to decoder key constants
- a runtime type compatible with the emitted `new Parameter(...)` expressions
- formatting logic for string and numeric values

## Why the Templates Matter Architecturally

The templates are not a cosmetic last step.
They define the runtime contract that the generators target.

From an architectural point of view:

- the AST passes define the logical decision tree
- the generation visitors turn that tree into Java fragments
- the templates define the executable environment those fragments live in

That is why understanding `Decoder.edt` and `Disassembler.edt` is essential to understanding Edigen as a whole.
