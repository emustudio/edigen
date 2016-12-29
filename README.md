# Edigen - Emulator Disassembler Generator
[![Build Status](https://travis-ci.org/sulir/edigen.svg?branch=master)](https://travis-ci.org/sulir/edigen)

Edigen is a command-line tool which generates Java source code of:
 * an instruction decoder of an emulator
 * and a disassembler

from a text file written in its own domain-specific language.

Writing disassemblers and emulator instruction decoders by hand is tedious and error-prone. The code is often unreadable and unmaintainable. *Edigen* takes a file containing the description of instruction formats, operation codes and mnemonics as an input. It generates Java classes compatible with the [emuStudio](http://github.com/vbmacher/emuStudio) platform, i.e. implementing interfaces from [emuLib](http://github.com/vbmacher/emuLib).

## Usage

The best way to use Edigen is through its Maven plugin -- see the [basic example](https://github.com/sulir/edigen-maven-plugin#basic-use) and [reference](https://github.com/sulir/edigen-maven-plugin#reference).

An alternative is to manually execute Edigen through a command-line interface. To see all command line options, execute the Edigen JAR without any arguments.

## Specification format

This is an example of an input file:

```
# decoder
instruction =
    "add": 0xE dst_reg(2) src_reg(2) |
    "sub": 0xF dst_reg(2) immediate(10);

src_reg, dst_reg =
    "X": 00 |
    "Y": 01;

immediate = imm: imm(10);

%%

# disassembler
"%s %s, %s" = instruction dst_reg src_reg;
"%s %s, %d" = instruction dst_reg immediate;
```

The file is divided into two parts: decoder and disassembler, separated by `%%`. The character `#` denotes one-line comments.

### Decoder

The decoder part consist of a set of rules. Decoding always starts with the first rule (in this case, `instruction`). Each rule has one or more variants. A variant consists of a mixture of constants and subrules.

A constant can be hexadecimal (e.g., `0xF`) or binary (`01`). Constants are used to unambiguously match exactly one variant for each rule.

A subrule has a name and length in bits -- e.g., `dst_reg(2)`. The bits located at the position of a subrule are passed to the corresponding rule.

A variant can return a value - either a constant string (`"add"`), or a binary value taken from a subrule (`imm`).

The result of decoding is an associative array in the form {rule: value, ...}.

For example, let us decode the instruction "1111 0100 0000 0011", The second variant of the rule `instruction` is matched, since `0xF` is 1111. So far, the result is {instruction: "sub"}. The following bits (01) are passed to the rule `dst_reg`, where the second variant matches, so the result is updated: {instruction: "sub", dst_reg: "Y"}. Finally, the bits 00 0000 0011 are passed to the rule `immediate`, which just returns the passed binary value. The final result is {instruction: "sub", dst_reg: "Y", immediate: 3}.

### Disassembler

The disassembler part matches a set of rules (on the right side of `=`) to the formatting string (on the left side). The first set of rules which is a subset of the result rule-set is selected. The corresponding formatting string is used, substituting the formats with concrete values.

In our example, the first rule-set cannot be used, since our result does not contain `src_reg`. However, our result contains all rules specified in the second rule-set (`instruction dst_reg immediate`), so the disassembled instruction is "sub Y, 3".

By default, these format specifiers are available:
 * `%c` - one character, in the platform's default charset
 * `%d` - arbitrarily long signed integer, decimal
 * `%f` - a 4-byte of 8-byte floating point number
 * `%s` - a string (typically used for string constants returned from variants)
 * `%x` - arbitrarily long unsigned integer, hexadecimal, lowercase
 * `%X` - arbitrarily long unsigned integer, hexadecimal, uppercase
 * `%%` - a percent sign