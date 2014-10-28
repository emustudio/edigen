Edigen - Emulator Disassembler Generator
========================================
[![Build Status](https://travis-ci.org/sulir/edigen.png)](https://travis-ci.org/sulir/edigen)

Edigen is a command-line tool which generates Java source code of:
 * an instruction decoder of an emulator
 * a disassembler

from a text file written in its own domain-specific language.

Writing disassemblers and emulator instruction decoders by hand is tedious and error-prone. The code is often unreadable and unmaintainable. *Edigen* takes a file containing the description of instruction formats, operation codes and mnemonics as an input. It generates Java classes compatible with the [emuStudio](http://github.com/vbmacher/emuStudio) platform, i.e. implementing interfaces from [emuLib](http://github.com/vbmacher/emuLib).

Input file example
------------------

	# comment
	root = instruction;

	instruction =
		"add": 0x1e arith_operands |
		"sub": 0x1f arith_operands;

	arith_operands =
		0 dst_reg(3) 0 src_reg |
		0 dst_reg(3) 10 src_mem |
		1 dst_reg(3) 0 immediate;

	src_reg, dst_reg =
		"eax": 000 |
		"ebx": 001;

	src_mem = 
		mem: 00 mem(16);

	immediate =
		imm: 000 imm(8) |
		imm: 001 imm(16);

	%%

	"%s %s, %s" = instruction dst_reg src_reg;
	"%s %s, [%X]" = instruction dst_reg src_mem;
	"%s %s, %X" = instruction dst_reg immediate;

Notes for developers
--------------------

The project now uses Maven 3. Dependencies (like JavaCC) are automatically downloaded.
