# Edigen - Emulator Disassembler Generator
![Edigen Build](https://github.com/emustudio/edigen/workflows/Edigen%20Build/badge.svg)

Edigen is a command-line tool which generates Java source code of:
 * instruction decoder of an emulator
 * and disassembler

from a text file written in its own domain-specific language.

Writing disassemblers and emulator instruction decoders by hand is tedious and error-prone. The code is often
unreadable and unmaintainable. *Edigen* takes a file containing the description of instruction formats, operation
codes and mnemonics as an input. It generates Java classes compatible with
[emuStudio](http://github.com/emustudio/emuStudio) platform, i.e. implementing interfaces from [emuLib](http://github.com/emustudio/emuLib).

## License

This project is released under [GNU GPL v2](https://www.gnu.org/licenses/gpl-2.0.html) license.

## Usage

The best way to use Edigen either with:

- [edigen-gradle-plugin](https://github.com/emustudio/edigen-gradle-plugin)
- or [edigen-maven-plugin](https://github.com/emustudio/edigen-maven-plugin)
  
Alternative way is to manually execute Edigen through a command-line interface. To see all command line options,
execute the Edigen JAR without any arguments.

## Specification format

Edigen files have `.eds` extension. This is an example of an input file:

```
# decoder
instruction =
    "add": 0xE dst_reg(2) src_reg(2) |
    "sub": 0xF dst_reg(2) immediate(10);

src_reg, dst_reg =
    "X": 00 |
    "Y": 01;

%%

# disassembler
"%s %s, %s" = instruction dst_reg src_reg;
"%s %s, %d" = instruction dst_reg immediate;
```

The file is divided into two parts: decoder and disassembler, separated by `%%`. The character `#` denotes one-line comments.

### Decoder

The decoder part consist of a set of rules. Decoding always starts with the first rule (in this case, `instruction`).
Each rule has one or more variants. A variant consists of a mixture of constants and subrules.

A constant can be hexadecimal (e.g., `0xF`) or binary (`01`). Constants are used to unambiguously match exactly one
variant for each rule.

A subrule has a name and length in bits -- e.g., `dst_reg(2)`. The bits located at the position of a subrule are passed
to the corresponding rule.

A variant can return a value - either a constant string (`"add"`), or a binary value taken from a subrule (`imm`).

The result of decoding is an associative array in the form {rule: value, ...}.

For example, let us decode the instruction "1111 0100 0000 0011", The second variant of the rule `instruction` is
matched, since `0xF` is 1111. So far, the result is {instruction: "sub"}. The following bits (01) are passed to the
rule `dst_reg`, where the second variant matches, so the result is updated: {instruction: "sub", dst_reg: "Y"}.

Finally, the bits `00 0000 0011` are passed to the rule `immediate`, which just returns the passed binary value.
The final result is {instruction: "sub", dst_reg: "Y", immediate: 3}.

### Disassembler

Disassembler part matches a set of rules (on the right side of `=`) to a formatting string (on the left side).
The first set of rules which is a subset of the result rule-set is selected. The corresponding formatting string is used,
substituting the formats with concrete values.

In our example, the first rule-set cannot be used, since our result does not contain `src_reg`. However, our result
contains all rules specified in the second rule-set (`instruction dst_reg immediate`), so the disassembled instruction
is "sub Y, 3".

By default, these format specifiers are available:
 * `%c` - one character, in the platform's default charset
 * `%d` - arbitrarily long signed integer, decimal
 * `%f` - a 4-byte of 8-byte floating point number
 * `%s` - a string (typically used for string constants returned from variants)
 * `%x` - arbitrarily long unsigned integer, hexadecimal, lowercase
 * `%X` - arbitrarily long unsigned integer, hexadecimal, uppercase
 * `%%` - a percent sign
 
 The rule-set on the right side of `=` can take decoding strategy as a parameter in brackets `()`. The following
 decoding strategies are available:
 
 * `bit_reverse` - reverses the bits
 * `big_endian` - decodes the bits as they are stored in big-endian format
 * `little_endian` - decodes the bits as they are stored in little-endian format
 * `absolute` - decodes the bits as stored in 2's complement if they are negative; the negative sign is then thrown away
 * `shift_left` - shifts the number to the left by 1 bit. Does it in "big endian" way. Meaning bytes `[0] = 1, [1] = 2`
   will result in `[0] = 2, [1] = 4`
 * `shift_right` - shifts the number to the right by 1 bit. Dies it in "big endian" way. Meaning bytes `[0] = 1, [1] = 2`
   will result in `[0] = 0, [1] = 0x81`   
 
The strategies can be combined. Multiple strategies will be applied in the left-to-right order.
For example (grammar of [SSEM](http://curation.cs.manchester.ac.uk/computer50/www.computer50.org/mark1/prog98/ssemref.html) machine):

```
instruction = "JMP": line(5)     ignore8(8) 000 ignore16(16) |
              "JPR": line(5)     ignore8(8) 100 ignore16(16) |
              "LDN": line(5)     ignore8(8) 010 ignore16(16) |
              "STO": line(5)     ignore8(8) 110 ignore16(16) |
              "SUB": line(5)     ignore8(8) 001 ignore16(16) |
              "CMP": 00000       ignore8(8) 011 ignore16(16) |
              "STP": 00000       ignore8(8) 111 ignore16(16);

line = arg: arg(5);
ignore5 = arg: arg(5);
ignore8 = arg: arg(8);
ignore16 = arg: arg(16);

%%

"%s %X" = instruction line(shift_left, shift_left, shift_left, bit_reverse, absolute) ignore8 ignore16;
"%s" = instruction ignore8 ignore16;
```