# Minefield Object File Layout

A computer is detail-oriented machine.  The more details we give it, the better it can do
what we want it to do.  Since a Minefield object file describes the steps we want the
computer to perform, we'll add some details to the file to help the computer.

Broadly speaking, there are three parts to a minefield object file:
* The header: contains the signature, version, and pointer to the constant pool;
* The byte code: the actual instructions for the virtual machine;
* The constant pool: this is where the initialized data is stored;

The minefield object file layout described in this document applies to the following build
versions:
* zero

## Header
The header is 14 bytes long and starts at the beginning of the file. It contains the
signature, the build version, the offset of the first instruction, and the offset of the
constant pool.

### Signature [0x00:0x09]
The signature is the frist 9 bytes of the header.  It is the ASCII character sequence
"minefield" (or the byte sequence 0x6d, 0x69, 0x6e, 0x65, 0x66, 0x69, 0x65, 0x6c, 0x64).

### Build Version [0x0a:0x0d]
The build version is represented by the 4 bytes following the signature. The bytes
represent an integer in big-endian order. The build version, basically, represents what
the byte codes mean. The meaning is not described here.

### Constant Pool [0x0e:0x11]
The 4 bytes after the build version represent an integer offset from the beginning of the
file to the constant pool.

## Byte Code [0x12:...]
The actual VM byte codes start immediately after the header. Execution starts at the
beginning of this section.

## Constant Pool
The constant pool is where initialized data that does not fit into 4 bytes is stored.  For
now, this is string data.  The strings are zero terminated.


    The minefield programming language
    Copyright 2018 Eric J. Deiman

    This file is part of the minefield programming language.

    The minefield programming language is free software: you can redistribute it
    and/or modify it under the terms of the GNU General Public License as published by the
    Free Software Foundation, either version 3 of the License, or (at your option) any
    later version.

    The minefield programming language is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along with the
    minefield programming language. If not, see <https://www.gnu.org/licenses/>
