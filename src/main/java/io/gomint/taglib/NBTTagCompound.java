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
import io.netty.buffer.PooledByteBufAllocator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a compound tag that may hold several children tags.
 *
 * @author BlackyPaw
 * @version 1.0
 */
public class NBTTagCompound implements Cloneable {

  /**
   * Reads the NBTTagCompound from the specified file. See {@link #readFrom(ByteBuf,
   * ByteOrder)} for further details.
   *
   * @param file       The file to read the NBTCompound from
   * @return The compound tag that was read from the input source
   * @throws IOException Thrown in case an I/O error occurs or invalid NBT data is encountered
   */
  public static NBTTagCompound readFrom(File file, ByteOrder byteOrder)
      throws IOException, AllocationLimitReachedException {
    try (FileInputStream in = new FileInputStream(file)) {
      ByteBuf buffer = PooledByteBufAllocator.DEFAULT.directBuffer((int) file.length());
      buffer.writeBytes(in, (int) file.length());
      NBTTagCompound compound = readFrom(buffer, byteOrder);
      buffer.release();
      return compound;
    }
  }

  /**
   * Reads the NBTTagCompound from the specified input stream. In case compressed is set to true the
   * given input stream will be wrapped in a deflating stream. The implementation is guaranteed to
   * wrap the entire stream in a BufferedInputStream so that no unbuffered I/O will ever occur.
   * Therefore it is not necessary to wrap the input in a BufferedInputStream manually. The input
   * stream is closed automatically.
   *
   * @param in         The input stream to read from
   * @return The compound tag that was read from the input source
   * @throws IOException Thrown in case an I/O error occurs or invalid NBT data is encountered
   */
  public static NBTTagCompound readFrom(ByteBuf in, ByteOrder byteOrder)
      throws IOException, AllocationLimitReachedException {
    NBTReader reader = new NBTReader(in, byteOrder);
    return reader.parse();
  }

  private String name;
  private Map<String, Object> children;

  /**
   * Constructs a new NBTTagCompound given its name. If no name is specified, i.e. name == null, the
   * NBTTagCompound is considered to be member of a list.
   *
   * @param name The name of the tag
   */
  public NBTTagCompound(final String name) {
    super();
    this.name = name;
    this.children = new HashMap<>();
  }

  /**
   * Gets the name of the tag. May be null if the tag belongs to a list.
   *
   * @return The name of the tag
   */
  public String getName() {
    return this.name;
  }

  /**
   * Adds the specified value to the compound given the name used to store it.
   *
   * @param name  The name of the value
   * @param value The value to be stored
   */
  public void addValue(String name, byte value) {
    this.children.put(name, value);
  }

  /**
   * Adds the specified value to the compound given the name used to store it.
   *
   * @param name  The name of the value
   * @param value The value to be stored
   */
  public void addValue(String name, short value) {
    this.children.put(name, value);
  }

  /**
   * Adds the specified value to the compound given the name used to store it.
   *
   * @param name  The name of the value
   * @param value The value to be stored
   */
  public void addValue(String name, int value) {
    this.children.put(name, value);
  }

  /**
   * Adds the specified value to the compound given the name used to store it.
   *
   * @param name  The name of the value
   * @param value The value to be stored
   */
  public void addValue(String name, long value) {
    this.children.put(name, value);
  }

  /**
   * Adds the specified value to the compound given the name used to store it.
   *
   * @param name  The name of the value
   * @param value The value to be stored
   */
  public void addValue(String name, float value) {
    this.children.put(name, value);
  }

  /**
   * Adds the specified value to the compound given the name used to store it.
   *
   * @param name  The name of the value
   * @param value The value to be stored
   */
  public void addValue(String name, byte[] value) {
    this.children.put(name, value);
  }

  /**
   * Adds the specified value to the compound given the name used to store it.
   *
   * @param name  The name of the value
   * @param value The value to be stored
   */
  public void addValue(String name, String value) {
    this.children.put(name, value);
  }

  /**
   * Adds the specified value to the compound given the name used to store it.
   *
   * @param name  The name of the value
   * @param value The value to be stored
   */
  public void addValue(String name, double value) {
    this.children.put(name, value);
  }

  /**
   * Adds the specified value to the compound given the name used to store it.
   *
   * @param name  The name of the value
   * @param value The value to be stored
   */
  public void addValue(String name, int[] value) {
    this.children.put(name, value);
  }

  /**
   * Adds the specified value to the compound given the name used to store it.
   *
   * @param name  The name of the value
   * @param value The value to be stored
   */
  public void addValue(String name, List value) {
    this.children.put(name, value);
  }

  /**
   * Adds the specified value to the compound given the name used to store it.
   *
   * @param name  The name of the value
   * @param value The value to be stored
   */
  public void addValue(String name, NBTTagCompound value) {
    if (!name.equals(value.getName())) {
      throw new AssertionError(
          "Failed to add NBTTagCompound with name '" + value.getName() + "' given name '" + name
              + "'");
    }
    this.children.put(name, value);
  }

  /**
   * Adds the specified tag as a child tag of this compound. This method is effectively the same as
   * calling {@link #addValue(String, NBTTagCompound)} and specified tag.getName() as the name.
   *
   * @param tag The tag to be added as a child
   */
  public void addChild(NBTTagCompound tag) {
    this.children.put(tag.getName(), tag);
  }

  /**
   * Gets the attribute with the specified name from the compound if it exists. If not it will
   * return the default value instead.
   *
   * @param name         The name of the attribute
   * @param defaultValue The default value to return for non-existing attributes
   * @return The value of the attribute
   */
  public Byte getByte(String name, Byte defaultValue) {
    return (this.children.containsKey(name) ? (Byte) this.children.get(name) : defaultValue);
  }

  /**
   * Gets the attribute with the specified name from the compound if it exists. If not it will
   * return the default value instead.
   *
   * @param name         The name of the attribute
   * @param defaultValue The default value to return for non-existing attributes
   * @return The value of the attribute
   */
  public Short getShort(String name, Short defaultValue) {
    return (this.children.containsKey(name) ? (Short) this.children.get(name) : defaultValue);
  }

  /**
   * Gets the attribute with the specified name from the compound if it exists. If not it will
   * return the default value instead.
   *
   * @param name         The name of the attribute
   * @param defaultValue The default value to return for non-existing attributes
   * @return The value of the attribute
   */
  public Integer getInteger(String name, Integer defaultValue) {
    return (this.children.containsKey(name) ? (Integer) this.children.get(name) : defaultValue);
  }

  /**
   * Gets the attribute with the specified name from the compound if it exists. If not it will
   * return the default value instead.
   *
   * @param name         The name of the attribute
   * @param defaultValue The default value to return for non-existing attributes
   * @return The value of the attribute
   */
  public Long getLong(String name, Long defaultValue) {
    return (this.children.containsKey(name) ? (Long) this.children.get(name) : defaultValue);
  }

  /**
   * Gets the attribute with the specified name from the compound if it exists. If not it will
   * return the default value instead.
   *
   * @param name         The name of the attribute
   * @param defaultValue The default value to return for non-existing attributes
   * @return The value of the attribute
   */
  public Float getFloat(String name, Float defaultValue) {
    return (this.children.containsKey(name) ? (Float) this.children.get(name) : defaultValue);
  }

  /**
   * Gets the attribute with the specified name from the compound if it exists. If not it will
   * return the default value instead.
   *
   * @param name         The name of the attribute
   * @param defaultValue The default value to return for non-existing attributes
   * @return The value of the attribute
   */
  public Double getDouble(String name, Double defaultValue) {
    return (this.children.containsKey(name) ? (Double) this.children.get(name) : defaultValue);
  }

  /**
   * Gets the attribute with the specified name from the compound if it exists. If not it will
   * return the default value instead.
   *
   * @param name         The name of the attribute
   * @param defaultValue The default value to return for non-existing attributes
   * @return The value of the attribute
   */
  public String getString(String name, String defaultValue) {
    return (this.children.containsKey(name) ? (String) this.children.get(name) : defaultValue);
  }

  /**
   * Gets the attribute with the specified name from the compound if it exists. If not it will
   * return the default value instead.
   *
   * @param name         The name of the attribute
   * @param defaultValue The default value to return for non-existing attributes
   * @return The value of the attribute
   */
  public byte[] getByteArray(String name, byte[] defaultValue) {
    return (this.children.containsKey(name) ? (byte[]) this.children.get(name) : defaultValue);
  }

  /**
   * Gets the attribute with the specified name from the compound if it exists. If not it will
   * return the default value instead.
   *
   * @param name         The name of the attribute
   * @param defaultValue The default value to return for non-existing attributes
   * @return The value of the attribute
   */
  public int[] getIntegerArray(String name, int[] defaultValue) {
    return (this.children.containsKey(name) ? (int[]) this.children.get(name) : defaultValue);
  }

  /**
   * Gets the list stored under the specified name. In case the list does not exist and insert is
   * set to true a new and empty list with the specified name will be created. If insert is set to
   * false null will be returned instead.
   *
   * @param name   The name of the list
   * @param insert Whether or not to insert a new and empty list if the list does not exist
   * @return The list or null
   */
  @SuppressWarnings("unchecked")
  public List<Object> getList(String name, boolean insert) {
    if (this.children.containsKey(name)) {
      return (List<Object>) this.children.get(name);
    }

    if (insert) {
      List<Object> backingList = new ArrayList<>(0);
      this.addValue(name, backingList);
      return backingList;
    } else {
      return null;
    }
  }

  /**
   * Gets the compound stored under the specified name. In case the compound does not exist and
   * insert is set to true a new and empty compound with the specified name will be created. If
   * insert is set to false null will be returned instead.
   *
   * @param name   The name of the compound
   * @param insert Whether or not to insert a new and empty compound if the compound does not exist
   * @return The compound or null
   */
  public NBTTagCompound getCompound(String name, boolean insert) {
    if (this.children.containsKey(name)) {
      return (NBTTagCompound) this.children.get(name);
    }

    if (insert) {
      NBTTagCompound compound = new NBTTagCompound(name);
      this.addValue(name, compound);
      return compound;
    } else {
      return null;
    }
  }

  /**
   * Writes the NBTTagCompound to the specified file. See {@link #writeTo(ByteBuf,
   * ByteOrder)} for further details.
   *
   * @param file       The file to write the NBTCompound to
   * @param byteOrder  The byteorder to use
   * @throws IOException Thrown in case an I/O error occurs or invalid NBT data is encountered
   */
  public void writeTo(File file, ByteOrder byteOrder) throws IOException {
    try (FileOutputStream out = new FileOutputStream(file)) {
      ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer();
      this.writeTo(buf, byteOrder);

      byte[] data = new byte[buf.readableBytes()];
      buf.readBytes(data);
      out.write(data);
      buf.release();
    }
  }

  /**
   * Writes the NBTTagCompound to the specified output stream. In case compressed is set to true the
   * given output stream will be wrapped in an inflating stream. The implementation is guaranteed to
   * wrap the entire stream in a BufferedOutputStream so that no unbuffered I/O will ever occur.
   * Therefore it is not necessary to wrap the output in a BufferedOutputStream manually. The output
   * stream is closed automatically.
   *
   * @param out        The output stream to write to
   * @param byteOrder  The byteorder to use
   * @throws IOException Thrown in case an I/O error occurs or invalid NBT data is encountered
   */
  public void writeTo(ByteBuf out, ByteOrder byteOrder) throws IOException {
    NBTWriter writer = new NBTWriter(out, byteOrder);
    writer.write(this);
  }

  /**
   * Returns an iterable set of entries this tag compound holds.
   *
   * @return The set of entries the compound holds
   */
  public Set<Map.Entry<String, Object>> entrySet() {
    return this.children.entrySet();
  }

  /**
   * Checks whether or not the compound contains a child tag with the specified name.
   *
   * @param key The name of the child tag
   * @return Whether or not the compound contains a child tag with the specified name
   */
  public boolean containsKey(String key) {
    return this.children.containsKey(key);
  }

  /**
   * Removes given child
   *
   * @param key The name of the child tag
   * @return The object which has been removed or null when nothing has been removed
   */
  public Object remove(String key) {
    return this.children.remove(key);
  }

  /**
   * Get the amount of children in this compound
   *
   * @return amount of children
   */
  public int size() {
    return this.children.size();
  }

  /**
   * Clones the compound and all of its non-immutable elements recursively. This operation may be
   * expensive so use it only if absolutely necessary.
   *
   * @param newName New name of the root compound
   * @return The cloned tag compound.
   */
  public NBTTagCompound deepClone(String newName) {
    NBTTagCompound compound = this.deepClone0();
    compound.setName(newName);
    return compound;
  }

  private NBTTagCompound deepClone0() {
    NBTTagCompound compound = new NBTTagCompound();
    compound.name = this.name;
    compound.children = new HashMap<>(this.children.size());
    for (Map.Entry<String, Object> child : this.children.entrySet()) {
      Object value = child.getValue();
      if (value instanceof byte[]) {
        byte[] data = (byte[]) value;
        compound.addValue(child.getKey(), Arrays.copyOf(data, data.length));
      } else if (value instanceof List) {
        compound.addValue(child.getKey(), this.deepCloneList((List) value));
      } else if (value instanceof int[]) {
        int[] data = (int[]) value;
        compound.addValue(child.getKey(), Arrays.copyOf(data, data.length));
      } else if (value instanceof NBTTagCompound) {
        compound.addChild(((NBTTagCompound) value).deepClone0());
      } else {
        // Other supported types are immutable:
        compound.children.put(child.getKey(), child.getValue());
      }
    }
    return compound;
  }

  private List deepCloneList(List input) {
    List output = new ArrayList(input.size());
    for (Object value : input) {
      if (value instanceof byte[]) {
        byte[] data = (byte[]) value;
        output.add(Arrays.copyOf(data, data.length));
      } else if (value instanceof List) {
        output.add(this.deepCloneList((List) value));
      } else if (value instanceof int[]) {
        int[] data = (int[]) value;
        output.add(Arrays.copyOf(data, data.length));
      } else if (value instanceof NBTTagCompound) {
        output.add(((NBTTagCompound) value).deepClone0());
      } else {
        // Other supported types are immutable:
        output.add(value);
      }
    }
    return output;
  }

  NBTTagCompound() {
    super();
    this.name = null;
    this.children = new HashMap<>();
  }

  /**
   * @deprecated For internal use only!
   */
  void setName(String name) {
    this.name = name;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NBTTagCompound compound = (NBTTagCompound) o;
    return Objects.equals(name, compound.name) &&
            Objects.equals(children, compound.children);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, children);
  }

  @Override
  public String toString() {
    return "NBTTagCompound{" +
            "name='" + name + '\'' +
            ", children=" + children +
            '}';
  }
}
