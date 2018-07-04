/*
  The minefield programming language
  Copyright 2018 Eric J. Deiman

  This file is part of the minefield programming language.

  The minefield programming language is free software: you can redistribute it
  and/ormodify it under the terms of the GNU General Public License as published by the
  Free Software Foundation, either version 3 of the License, or (at your option) any
  later version.

  The minefield programming language is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along with the
  minefield programming language. If not, see <https://www.gnu.org/licenses/>
*/

package common;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

/**
 * <p>Store and manipulate bytes in big-endian format. In a way there are 2 interfaces
 * here: one for sequential access; and, one for random access. Each is described
 * below.</p>
 *
 * <p>When generating byte code, it is often useful to store them in a sequence, one right
 * after the other. It will be convient to use chained method calls for that. This means
 * there needs to be a way for the CodeBuffer to know where the insertion point is. This
 * point is called the pointer. The methods prefixed with read and write automatically
 * update the pointer. The pointer position can be gotten and set. </p>
 *
 * <p>It is also useful to be able to access the VMImageBuffer randomly, so the methods
 * prefixed with put and get write anywhere into the VMImageBuffer. These calls do not change
 * the pointer.</p>
 *
 * <p>This class also hides some bit-twiddling.</p>
 */
public class VMImageBuffer {

    public VMImageBuffer() {
        core = new Vector<Byte>(1024, 1024);
        pointer = 0;
    }

    public int getByte(int at) {
        return 0x000000ff & ((int)core.elementAt(at));
    }

    public int getShort(int at) {
        int info2 = 0x000000ff & ((int)core.elementAt(at));
        int info1 = 0x000000ff & ((int)core.elementAt(at + 1));
        info2 = info2 << 8 | info1;
        return info2;
    }

    public int getInteger(int at) {
        int info4 = 0x000000ff & ((int)core.elementAt(at));
        int info3 = 0x000000ff & ((int)core.elementAt(at + 1));
        int info2 = 0x000000ff & ((int)core.elementAt(at + 2));
        int info1 = 0x000000ff & ((int)core.elementAt(at + 3));
        info4 = info4 << 8 | info3;
        info4 = info4 << 8 | info2;
        info4 = info4 << 8 | info1;
        return info4;
    }

    public void putByte(int data, int at) {
        byte info = (byte)data;
        data = data >>> 8;
        if(data > 0) {
            throw new Error("in VMImageBuffer.putByte: data contains more than 8-bits " +
                            "of info.");
        }
        core.set(at, info);
    }

    public void putShort(int data, int at) {
        assert (at & 0x1) == 0 : "at must be aligned to 2 bytes" ;
        byte info1 = (byte)data;
        data = data >>> 8;
        byte info2 = (byte)data;
        data = data >>> 8;
        if(data > 0) {
            throw new Error("in VMImageBuffer.putShort: data contains more than 16-bits " +
                            "of info.");
        }
        core.set(at, info2);
        core.set(at + 1, info1);
    }

    public void putInteger(int data, int at) {
        assert (at & 0x2) == 0 : "at must be aligned to 4 bytes" ;
        byte info1 = (byte)data;
        data = data >>> 8;
        byte info2 = (byte)data;
        data = data >>> 8;
        byte info3 = (byte)data;
        data = data >>> 8;
        byte info4 = (byte)data;
        core.set(at, info4);
        core.set(at + 1, info3);
        core.set(at + 2, info2);
        core.set(at + 3, info1);
    }

    //------------------------------------------------------------------------------

    public Integer getPointer() {
        return pointer;
    }

    public Integer setPointer(Integer newPointer) {
        Integer oldPointer = pointer;
        pointer = newPointer;
        return oldPointer;
    }

    public int readByte() {
        return 0x000000ff & ((int)core.elementAt(pointer++));
    }

    public int readShort() {
        int info2 = 0x000000ff & ((int)core.elementAt(pointer++));
        int info1 = 0x000000ff & ((int)core.elementAt(pointer++));
        info2 = info2 << 8 | info1;
        return info2;
    }

    public int readInteger() {
        int info4 = 0x000000ff & ((int)core.elementAt(pointer++));
        int info3 = 0x000000ff & ((int)core.elementAt(pointer++));
        int info2 = 0x000000ff & ((int)core.elementAt(pointer++));
        int info1 = 0x000000ff & ((int)core.elementAt(pointer++));
        info4 = info4 << 8 | info3;
        info4 = info4 << 8 | info2;
        info4 = info4 << 8 | info1;
        return info4;
    }

    public VMImageBuffer writeByte(ByteCodes.Codes op) {
        return writeByte(op.ordinal());
    }

    public VMImageBuffer writeByte(int data) {
        byte info = (byte)data;
        data = data >>> 8;
        if(data > 0) {
            throw new Error("in VMImageBuffer.writeByte: data contains more than 8-bits " +
                            "of info.");
        }
        core.add(pointer++, info);
        return this;
    }

    public VMImageBuffer writeShort(int data) {
        byte info1 = (byte)data;
        data = data >>> 8;
        byte info2 = (byte)data;
        data = data >>> 8;
        if(data > 0) {
            throw new Error("in VMImageBuffer.writeShort: data contains more than 16-bits " +
                            "of info.");
        }
        core.add(pointer++, info2);
        core.add(pointer++, info1);
        return this;
    }

    public VMImageBuffer writeInteger(int data) {
        byte info1 = (byte)data;
        data = data >>> 8;
        byte info2 = (byte)data;
        data = data >>> 8;
        byte info3 = (byte)data;
        data = data >>> 8;
        byte info4 = (byte)data;
        core.add(pointer++, info4);
        core.add(pointer++, info3);
        core.add(pointer++, info2);
        core.add(pointer++, info1);
        return this;
    }

    public VMImageBuffer writeString(String data) {
        for(byte b : data.getBytes()) {
            core.add(pointer++, b);
        }
        return this;
    }

    //------------------------------------------------------------------------------

    public Boolean readFrom(String fileName) {
        try {
            FileInputStream fi = new FileInputStream(fileName);
            Boolean result = readFrom(fi);
            fi.close();
            return result;
        }
        catch(Exception e) {
            throw new Error(e);
        }
    }

    public Boolean readFrom(InputStream in) {
        try {
            byte[] buffer = new byte[1024];
            int amountRead = in.read(buffer);
            while(amountRead > 0) {
                for(int i = 0; i < amountRead; i++) {
                    core.add(buffer[i]);
                }
                amountRead = in.read(buffer);
            }
            return true;
        }
        catch(Exception e) {
            throw new Error(e);
        }
    }

    public Boolean writeTo(String fileName) {
        try {
            FileOutputStream fo = new FileOutputStream(fileName);
            Boolean result = writeTo(fo);
            fo.close();
            return result;
        }
        catch(Exception e) {
            throw new Error(e);
        }

    }
    public Boolean writeTo(OutputStream out) {
        try {
            byte[] coreArray = new byte[core.size()];
            int i = 0;
            for(Byte b : core) {
                coreArray[i++] = b;
            }
            out.write(coreArray);
        }
        catch(Exception e) {
            throw new Error(e);
        }
        return true;
    }

    public Integer size() {
        return core.size();
    }

    //------------------------------------------------------------------------------

    public String dumpToString() {
        StringBuilder out = new StringBuilder();
        for(Byte b : core) {
            out.append(String.format("0x%02x\n", b));
        }
        return out.toString();
    }

    //------------------------------------------------------------------------------
    private Integer pointer;
    private Vector<Byte> core;
}
