/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 GoMint, BlackyPaw and geNAZt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.gomint.taglib;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;

/**
 * @author BlackyPaw
 * @version 1.0
 */
public class NBTWriter {

  private static final int MAX_SIZE = 10 * 1024 * 1024;

  private ByteOrder order;
  private ByteBuf buf;

  private boolean useVarint;

  public NBTWriter(final ByteBuf out, final ByteOrder byteOrder) {
    this.buf = out;
    this.order = byteOrder;
  }

  public void setUseVarint(boolean useVarint) {
    this.useVarint = useVarint;
  }

  public void write(List<Object> list) throws IOException {
    this.writeTagHeader(NBTDefinitions.TAG_LIST, "");
    this.writeListValue(list);
  }

  public void write(NBTTagCompound compound) throws IOException {
    this.writeTagHeader(NBTDefinitions.TAG_COMPOUND, compound.getName());
    this.writeCompoundValue(compound);
  }

  private void writeTagHeader(byte type, String name) throws IOException {
    this.writeByteValue(type);
    this.writeStringValue(name);
  }

  private void writeStringValue(String value) throws IOException {
    if (value != null) {
      byte[] utf8Bytes = StringUtil.getUTF8Bytes(value);
      if (this.useVarint) {
        VarInt.writeUnsignedVarInt(this, utf8Bytes.length);
      } else {
        this.writeShortValue((short) utf8Bytes.length);
      }

      this.ensureCapacity(utf8Bytes.length);
      this.buf.writeBytes(utf8Bytes);
    } else {
      if (this.useVarint) {
        this.writeByteValue((byte) 0);
      } else {
        this.writeShortValue((short) 0);
      }
    }
  }

  void writeByteValue(byte value) throws IOException {
    this.ensureCapacity(1);
    this.buf.writeByte(value);
  }

  private void writeShortValue(short value) throws IOException {
    this.ensureCapacity(2);
    if (this.order == ByteOrder.LITTLE_ENDIAN) {
      this.buf.writeShortLE(value);
    } else {
      this.buf.writeShort(value);
    }
  }

  private void writeIntegerValue(int value) throws IOException {
    if (this.useVarint) {
      VarInt.writeSignedVarInt(this, value);
    } else {
      this.ensureCapacity(4);

      if (this.order == ByteOrder.LITTLE_ENDIAN) {
        this.buf.writeIntLE(value);
      } else {
        this.buf.writeInt(value);
      }
    }
  }

  private void writeLongValue(long value) throws IOException {
    if (this.useVarint) {
      VarInt.writeSignedVarLong(this, value);
    } else {
      this.ensureCapacity(8);

      if (this.order == ByteOrder.LITTLE_ENDIAN) {
        this.buf.writeLongLE(value);
      } else {
        this.buf.writeLong(value);
      }
    }
  }

  private void writeFloatValue(float value) throws IOException {
    this.ensureCapacity(4);

    if (this.order == ByteOrder.LITTLE_ENDIAN) {
      this.buf.writeFloatLE(value);
    } else {
      this.buf.writeFloat(value);
    }
  }

  private void writeDoubleValue(double value) throws IOException {
    this.ensureCapacity(8);

    if (this.order == ByteOrder.LITTLE_ENDIAN) {
      this.buf.writeDoubleLE(value);
    } else {
      this.buf.writeDouble(value);
    }
  }

  private void writeByteArrayValue(byte[] value) throws IOException {
    this.ensureCapacity(value.length + 4);

    if (this.order == ByteOrder.LITTLE_ENDIAN) {
      this.buf.writeIntLE(value.length);
    } else {
      this.buf.writeInt(value.length);
    }

    this.buf.writeBytes(value);
  }

  @SuppressWarnings("unchecked")
  private void writeListValue(List<Object> value) throws IOException {
    this.ensureCapacity(5);
    if (value.size() > 0) {
      byte listNbtType = this.getNBTTypeFromValue(value.get(0));
      this.writeByteValue(listNbtType);
      this.writeIntegerValue(value.size());
      for (Object rawValue : value) {
        switch (listNbtType) {
          case NBTDefinitions.TAG_BYTE:
            this.writeByteValue((Byte) rawValue);
            break;
          case NBTDefinitions.TAG_SHORT:
            this.writeShortValue((Short) rawValue);
            break;
          case NBTDefinitions.TAG_INT:
            this.writeIntegerValue((Integer) rawValue);
            break;
          case NBTDefinitions.TAG_LONG:
            this.writeLongValue((Long) rawValue);
            break;
          case NBTDefinitions.TAG_FLOAT:
            this.writeFloatValue((Float) rawValue);
            break;
          case NBTDefinitions.TAG_DOUBLE:
            this.writeDoubleValue((Double) rawValue);
            break;
          case NBTDefinitions.TAG_BYTE_ARRAY:
            this.writeByteArrayValue((byte[]) rawValue);
            break;
          case NBTDefinitions.TAG_STRING:
            this.writeStringValue((String) rawValue);
            break;
          case NBTDefinitions.TAG_LIST:
            this.writeListValue((List<Object>) rawValue);
            break;
          case NBTDefinitions.TAG_COMPOUND:
            this.writeCompoundValue((NBTTagCompound) rawValue);
            break;
          case NBTDefinitions.TAG_INT_ARRAY:
            this.writeIntegerArrayValue((int[]) rawValue);
            break;
        }
      }
    } else {
      this.writeByteValue(NBTDefinitions.TAG_BYTE);
      this.writeIntegerValue(0);
    }
  }

  @SuppressWarnings("unchecked")
  private void writeCompoundValue(NBTTagCompound compound) throws IOException {
    for (Map.Entry<String, Object> key : compound.entrySet()) {
      Object rawValue = key.getValue();
      byte nbtType = this.getNBTTypeFromValue(rawValue);
      this.writeTagHeader(nbtType, key.getKey());
      switch (nbtType) {
        case NBTDefinitions.TAG_BYTE:
          this.writeByteValue((Byte) rawValue);
          break;
        case NBTDefinitions.TAG_SHORT:
          this.writeShortValue((Short) rawValue);
          break;
        case NBTDefinitions.TAG_INT:
          this.writeIntegerValue((Integer) rawValue);
          break;
        case NBTDefinitions.TAG_LONG:
          this.writeLongValue((Long) rawValue);
          break;
        case NBTDefinitions.TAG_FLOAT:
          this.writeFloatValue((Float) rawValue);
          break;
        case NBTDefinitions.TAG_DOUBLE:
          this.writeDoubleValue((Double) rawValue);
          break;
        case NBTDefinitions.TAG_BYTE_ARRAY:
          this.writeByteArrayValue((byte[]) rawValue);
          break;
        case NBTDefinitions.TAG_STRING:
          this.writeStringValue((String) rawValue);
          break;
        case NBTDefinitions.TAG_LIST:
          this.writeListValue((List<Object>) rawValue);
          break;
        case NBTDefinitions.TAG_COMPOUND:
          this.writeCompoundValue((NBTTagCompound) rawValue);
          break;
        case NBTDefinitions.TAG_INT_ARRAY:
          this.writeIntegerArrayValue((int[]) rawValue);
          break;
      }
    }

    this.writeByteValue(NBTDefinitions.TAG_END);
  }

  private void writeIntegerArrayValue(int[] value) throws IOException {
    this.ensureCapacity(4 * value.length + 4);

    if (this.order == ByteOrder.LITTLE_ENDIAN) {
      this.buf.writeIntLE(value.length);
    } else {
      this.buf.writeInt(value.length);
    }

    for (int item : value) {
      if (this.order == ByteOrder.LITTLE_ENDIAN) {
        this.buf.writeIntLE(item);
      } else {
        this.buf.writeInt(item);
      }
    }
  }

  private byte getNBTTypeFromValue(Object value) throws IOException {
    if (value instanceof Byte) {
      return NBTDefinitions.TAG_BYTE;
    } else if (value instanceof Short) {
      return NBTDefinitions.TAG_SHORT;
    } else if (value instanceof Integer) {
      return NBTDefinitions.TAG_INT;
    } else if (value instanceof Long) {
      return NBTDefinitions.TAG_LONG;
    } else if (value instanceof Float) {
      return NBTDefinitions.TAG_FLOAT;
    } else if (value instanceof Double) {
      return NBTDefinitions.TAG_DOUBLE;
    } else if (value instanceof byte[]) {
      return NBTDefinitions.TAG_BYTE_ARRAY;
    } else if (value instanceof String) {
      return NBTDefinitions.TAG_STRING;
    } else if (value instanceof List) {
      return NBTDefinitions.TAG_LIST;
    } else if (value instanceof NBTTagCompound) {
      return NBTDefinitions.TAG_COMPOUND;
    } else if (value instanceof int[]) {
      return NBTDefinitions.TAG_INT_ARRAY;
    } else {
      throw new IOException(
          "Invalid NBT Data: Cannot deduce NBT type of class '" + value.getClass().getName()
              + "' (value: '" + value.toString() + "')");
    }
  }

  private void ensureCapacity(int capacity) {
    int targetCapacity = this.buf.writerIndex() + capacity;
    if (targetCapacity <= this.buf.capacity()) {
      return;
    }

    int fastWritable = this.buf.maxFastWritableBytes();
    int newCapacity = fastWritable >= capacity ? this.buf.writerIndex() + fastWritable
        : this.buf.alloc().calculateNewCapacity(targetCapacity, MAX_SIZE);
    this.buf.capacity(newCapacity);
  }

}