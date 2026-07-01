# Edigen - Emulator Disassembler Generator
[![Edigen Build](https://github.com/emustudio/edigen/actions/workflows/build.yml/badge.svg)](https://github.com/emustudio/edigen/actions/workflows/build.yml)
![Maven Central Version](https://img.shields.io/maven-central/v/net.emustudio/edigen)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

Edigen is a command-line tool which generates Java source code of:
 * instruction decoder of an emulator
 * and disassembler

from a text file written in its own domain-specific language.

Writing disassemblers and emulator instruction decoders by hand is tedious and error-prone. The code is often
unreadable and unmaintainable. *Edigen* takes a file containing the description of instruction formats, operation
codes and mnemonics as an input. It generates Java classes compatible with
[emuStudio](http://github.com/emustudio/emuStudio) platform, i.e. implementing interfaces from [emuLib](http://github.com/emustudio/emuLib).

For system architecture and detailed technical documentation, visit https://www.emustudio.net/edigen/

## Usage

The best way to use Edigen either with:

- [edigen-gradle-plugin](https://github.com/emustudio/edigen-gradle-plugin)
- or [edigen-maven-plugin](https://github.com/emustudio/edigen-maven-plugin)
  
Alternative way is to manually execute Edigen through a command-line interface. To see all command line options,
execute the Edigen JAR without any arguments.

## Specification format

Edigen files have the `.eds` extension and are split into a decoder and a
disassembler part, separated by `%%`. A small taste:

```
# decoder
root: instruction, data;

instruction =
    "add": 0xE dst_reg(2) src_reg(2) |
    "sub": 0xF dst_reg(2) immediate(10);

data =
    data: data(32);

src_reg, dst_reg =
    "X": 00 |
    "Y": 01;

%%

# disassembler
"%s %s, %s" = instruction dst_reg src_reg;
"%s %s, %d" = instruction dst_reg immediate;
"%0x"       = data;
```

For the full syntax and semantics, see the *EDS Language* chapter of the
[Edigen documentation](docs/index.adoc) (also published at
https://www.emustudio.net/edigen/).
