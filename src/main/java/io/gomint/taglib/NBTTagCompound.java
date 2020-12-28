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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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

  private String[] keys = new String[0];
  private Object[] values = new Object[0];
  private int size;

  /**
   * Constructs a new NBTTagCompound given its name. If no name is specified, i.e. name == null, the
   * NBTTagCompound is considered to be member of a list.
   *
   * @param name The name of the tag
   */
  public NBTTagCompound(final String name) {
    super();
    this.name = name;
    this.ensureCapacity(8);
  }

  private void ensureCapacity(int numOfElements) {
    if (this.keys.length < numOfElements) {
      String[] copy = new String[numOfElements * 2];
      System.arraycopy(this.keys, 0, copy, 0, this.keys.length);
      this.keys = copy;
    }

    if (this.values.length < numOfElements) {
      Object[] copy = new Object[numOfElements * 2];
      System.arraycopy(this.values, 0, copy, 0, this.values.length);
      this.values = copy;
    }
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
    this.set(name, value);
  }

  private <V> V get(String key, V defaultValue) {
    int keyIndex = this.getKeyIndex(key);
    if (keyIndex != -1) {
      return (V) this.values[keyIndex];
    }

    return defaultValue;
  }

  private void set(String key, Object value) {
    // Check if the key already is known
    int keyIndex = this.getKeyIndex(key);
    if (keyIndex != -1) {
      this.values[keyIndex] = value;
      return;
    }

    // Store new value
    this.ensureCapacity(this.size + 1);

    // Check for first null
    for (int i = 0; i < this.keys.length; i++) {
      if (this.keys[i] == null) {
        this.keys[i] = key;
        this.values[i] = value;
        break;
      }
    }

    this.size++;
  }

  private int getKeyIndex(String key) {
    for (int i = 0; i < this.keys.length; i++) {
      if (key.equals(this.keys[i])) {
        return i;
      }
    }

    return -1;
  }

  /**
   * Adds the specified value to the compound given the name used to store it.
   *
   * @param name  The name of the value
   * @param value The value to be stored
   */
  public void addValue(String name, short value) {
    this.set(name, value);
  }

  /**
   * Adds the specified value to the compound given the name used to store it.
   *
   * @param name  The name of the value
   * @param value The value to be stored
   */
  public void addValue(String name, int value) {
    this.set(name, value);
  }

  /**
   * Adds the specified value to the compound given the name used to store it.
   *
   * @param name  The name of the value
   * @param value The value to be stored
   */
  public void addValue(String name, long value) {
    this.set(name, value);
  }

  /**
   * Adds the specified value to the compound given the name used to store it.
   *
   * @param name  The name of the value
   * @param value The value to be stored
   */
  public void addValue(String name, float value) {
    this.set(name, value);
  }

  /**
   * Adds the specified value to the compound given the name used to store it.
   *
   * @param name  The name of the value
   * @param value The value to be stored
   */
  public void addValue(String name, byte[] value) {
    this.set(name, value);
  }

  /**
   * Adds the specified value to the compound given the name used to store it.
   *
   * @param name  The name of the value
   * @param value The value to be stored
   */
  public void addValue(String name, String value) {
    this.set(name, value);
  }

  /**
   * Adds the specified value to the compound given the name used to store it.
   *
   * @param name  The name of the value
   * @param value The value to be stored
   */
  public void addValue(String name, double value) {
    this.set(name, value);
  }

  /**
   * Adds the specified value to the compound given the name used to store it.
   *
   * @param name  The name of the value
   * @param value The value to be stored
   */
  public void addValue(String name, int[] value) {
    this.set(name, value);
  }

  /**
   * Adds the specified value to the compound given the name used to store it.
   *
   * @param name  The name of the value
   * @param value The value to be stored
   */
  public void addValue(String name, List value) {
    this.set(name, value);
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
    this.set(name, value);
  }

  /**
   * Adds the specified value to the compound given the name used to store it.
   *
   * @param name  The name of the value
   * @param value The value to be stored
   */
  public void addValue(String name, Object value) {
    this.set(name, value);
  }

  /**
   * Adds the specified tag as a child tag of this compound. This method is effectively the same as
   * calling {@link #addValue(String, NBTTagCompound)} and specified tag.getName() as the name.
   *
   * @param tag The tag to be added as a child
   */
  public void addChild(NBTTagCompound tag) {
    this.set(tag.getName(), tag);
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
    return this.get(name, defaultValue);
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
    return this.get(name, defaultValue);
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
    return this.get(name, defaultValue);
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
    return this.get(name, defaultValue);
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
    return this.get(name, defaultValue);
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
    return this.get(name, defaultValue);
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
    return this.get(name, defaultValue);
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
    return this.get(name, defaultValue);
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
    return this.get(name, defaultValue);
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
    List<Object> backingList = this.get(name, null);
    if (backingList != null) {
      return backingList;
    }

    if (insert) {
      backingList = new ArrayList<>(0);
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
    NBTTagCompound compound = this.get(name, null);
    if (compound != null) {
      return compound;
    }

    if (insert) {
      compound = new NBTTagCompound(name);
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
    Set<Map.Entry<String, Object>> sets = new HashSet<>();
    for (int i = 0; i < this.keys.length; i++) {
      String key = this.keys[i];
      if (key != null) {
        sets.add(new AbstractMap.SimpleImmutableEntry<>(key, this.values[i]));
      }
    }

    return sets;
  }

  /**
   * Checks whether or not the compound contains a child tag with the specified name.
   *
   * @param key The name of the child tag
   * @return Whether or not the compound contains a child tag with the specified name
   */
  public boolean containsKey(String key) {
    return this.getKeyIndex(key) != -1;
  }

  /**
   * Removes given child
   *
   * @param key The name of the child tag
   * @return The object which has been removed or null when nothing has been removed
   */
  public Object remove(String key) {
    int keyIndex = this.getKeyIndex(key);
    if (keyIndex == -1) {
      return null;
    }

    Object value = this.values[keyIndex];
    this.keys[keyIndex] = null;
    this.values[keyIndex] = null;
    this.size--;
    return value;
  }

  /**
   * Get the amount of children in this compound
   *
   * @return amount of children
   */
  public int size() {
    return this.size;
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
    compound.keys = Arrays.copyOf(this.keys, this.keys.length);
    compound.values = Arrays.copyOf(this.values, this.values.length);
    return compound;
  }

  NBTTagCompound() {
    super();
    this.name = null;
    this.ensureCapacity(8);
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
            Arrays.equals(keys, compound.keys) &&
            Arrays.equals(values, compound.values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, keys, values);
  }

  @Override
  public String toString() {
    return "{\"_class\":\"NBTTagCompound\", " +
            "\"name\":" + (name == null ? "null" : "\"" + name + "\"") + ", " +
            "\"keys\":" + Arrays.toString(keys) + ", " +
            "\"values\":" + Arrays.toString(values) + ", " +
            "\"size\":\"" + size + "\"" +
            "}";
  }

}
