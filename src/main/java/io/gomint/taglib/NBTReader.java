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
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author BlackyPaw
 * @version 1.0
 */
public class NBTReader extends NBTStreamReader {

	public NBTReader( ByteBuf in, ByteOrder byteOrder ) {
		super( in, byteOrder );
	}

	public List<Object> parseList() throws IOException, AllocationLimitReachedException {
		this.expectInput( this.varint() ? 2 : 3, "Invalid NBT Data: Not enough data to read new tag", false );
		if ( this.readByteValue() != NBTDefinitions.TAG_LIST ) {
			throw new IOException( "Invalid NBT Data: No list tag found" );
		}

		this.readStringValue();
		return this.readTagListValue();
	}

	public NBTTagCompound parse() throws IOException, AllocationLimitReachedException {
		this.expectInput( this.varint() ? 2 : 3, "Invalid NBT Data: Not enough data to read new tag", false );
		if ( this.readByteValue() != NBTDefinitions.TAG_COMPOUND ) {
			throw new IOException( "Invalid NBT Data: No root tag found" );
		}

		String name = this.readStringValue();
		NBTTagCompound root = this.readTagCompoundValue();
		root.setName( name );
		return root;
	}

	private NBTTagCompound readTagCompoundValue() throws IOException, AllocationLimitReachedException {
		this.alterAllocationLimit( Allocation.COMPOUND );
		NBTTagCompound compound = new NBTTagCompound();
		this.expectInput( 1, "Invalid NBT Data: Expected Tag ID in compound tag", false );
		byte tagID = this.readByteValue();
		while ( tagID != NBTDefinitions.TAG_END ) {
			switch ( tagID ) {
				case NBTDefinitions.TAG_BYTE:
					compound.addValue( this.readStringValue(), this.readByteValue() );
					break;
				case NBTDefinitions.TAG_SHORT:
					compound.addValue( this.readStringValue(), this.readShortValue() );
					break;
				case NBTDefinitions.TAG_INT:
					compound.addValue( this.readStringValue(), this.readIntValue() );
					break;
				case NBTDefinitions.TAG_LONG:
					compound.addValue( this.readStringValue(), this.readLongValue() );
					break;
				case NBTDefinitions.TAG_FLOAT:
					compound.addValue( this.readStringValue(), this.readFloatValue() );
					break;
				case NBTDefinitions.TAG_DOUBLE:
					compound.addValue( this.readStringValue(), this.readDoubleValue() );
					break;
				case NBTDefinitions.TAG_BYTE_ARRAY:
					compound.addValue( this.readStringValue(), this.readByteArrayValue() );
					break;
				case NBTDefinitions.TAG_STRING:
					compound.addValue( this.readStringValue(), this.readStringValue() );
					break;
				case NBTDefinitions.TAG_LIST:
					compound.addValue( this.readStringValue(), this.readTagListValue() );
					break;
				case NBTDefinitions.TAG_COMPOUND:
					String name = this.readStringValue();
					NBTTagCompound child = this.readTagCompoundValue();
					child.setName( name );
					compound.addChild( child );
					break;
				case NBTDefinitions.TAG_INT_ARRAY:
					compound.addValue( this.readStringValue(), this.readIntArrayValue() );
					break;
				default:
					throw new IOException( "Invalid NBT Data: Unknown tag <" + tagID + ">" );
			}

			this.expectInput( 1, "Invalid NBT Data: Expected tag ID in tag compound", false );
			tagID = this.readByteValue();
		}
		return compound;
	}
	
	private List<Object> readTagListValue() throws IOException, AllocationLimitReachedException {
		this.expectInput( this.varint() ? 2 : 5, "Invalid NBT Data: Expected TAGList header", false );
		byte listType = this.readByteValue();
		int listLength = this.readIntValue();

		this.alterAllocationLimit( Allocation.ARRAY_LIST ); 			// 16 byte java object overhead + 4 byte integer for size
		this.alterAllocationLimit( Allocation.REFERENCE * listLength ); // reference overhead ( for the objects stored inside )

		List<Object> backingList = new ArrayList<>( listLength );

		switch( listType ) {
			case NBTDefinitions.TAG_END:
				// Not to be unseen! Seemingly Mojang cares about something after all: disk space
				listLength = 0;
				break;
			case NBTDefinitions.TAG_BYTE:
				this.expectInput( listLength, "Invalid NBT Data: Expected bytes for list" );
				for ( int i = 0; i < listLength; ++i ) {
					backingList.add( this.readByteValue() );
				}
				break;
			case NBTDefinitions.TAG_SHORT:
				this.expectInput( 2 * listLength, "Invalid NBT Data: Expected shorts for list" );
				for ( int i = 0; i < listLength; ++i ) {
					backingList.add( this.readShortValue() );
				}
				break;
			case NBTDefinitions.TAG_INT:
				this.expectInput( 4 * listLength, "Invalid NBT Data: Expected ints for list" );
				for ( int i = 0; i < listLength; ++i ) {
					backingList.add( this.readIntValue() );
				}
				break;
			case NBTDefinitions.TAG_LONG:
				this.expectInput( 8 * listLength, "Invalid NBT Data: Expected longs for list" );
				for ( int i = 0; i < listLength; ++i ) {
					backingList.add( this.readLongValue() );
				}
				break;
			case NBTDefinitions.TAG_FLOAT:
				this.expectInput( 4 * listLength, "Invalid NBT Data: Expected floats for list" );
				for ( int i = 0; i < listLength; ++i ) {
					backingList.add( this.readFloatValue() );
				}
				break;
			case NBTDefinitions.TAG_DOUBLE:
				this.expectInput( 8 * listLength, "Invalid NBT Data: Expected doubles for list" );
				for ( int i = 0; i < listLength; ++i ) {
					backingList.add( this.readDoubleValue() );
				}
				break;
			case NBTDefinitions.TAG_BYTE_ARRAY:
				for ( int i = 0; i < listLength; ++i ) {
					backingList.add( this.readByteArrayValue() );
				}
				break;
			case NBTDefinitions.TAG_STRING:
				for ( int i = 0; i < listLength; ++i ) {
					backingList.add( this.readStringValue() );
				}
				break;
			case NBTDefinitions.TAG_LIST:
				for ( int i = 0; i < listLength; ++i ) {
					backingList.add( this.readTagListValue() );
				}
				break;
			case NBTDefinitions.TAG_COMPOUND:
				for ( int i = 0; i < listLength; ++i ) {
					backingList.add( this.readTagCompoundValue() );
				}
				break;
			case NBTDefinitions.TAG_INT_ARRAY:
				for ( int i = 0; i < listLength; ++i ) {
					backingList.add( this.readIntArrayValue() );
				}
				break;
			default:
				throw new IOException( "Invalid NBT Data: Unknown tag <" + listType + ">" );
		}

		return backingList;
	}

}
