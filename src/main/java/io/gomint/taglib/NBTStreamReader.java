package io.gomint.taglib;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

/**
 * @author geNAZt
 * @version 1.0
 */
public class NBTStreamReader {

    protected ByteBuf in;
    protected ByteOrder byteOrder;

    private boolean useVarint;
    private int allocateLimit = -1;

    protected NBTStreamReader( ByteBuf in, ByteOrder byteOrder ) {
        this.in = in;
        this.byteOrder = byteOrder;
    }

    public boolean varint() {
        return this.useVarint;
    }

    public void setUseVarint( boolean useVarint ) {
        this.useVarint = useVarint;
    }

    public void setAllocateLimit( int allocateLimit ) {
        this.allocateLimit = allocateLimit;
    }

    protected byte readByteValue() throws IOException, AllocationLimitReachedException {
        this.expectInput( 1, "Invalid NBT Data: Expected byte" );
        return this.in.readByte();
    }

    protected String readStringValue() throws IOException, AllocationLimitReachedException {
        int length = this.useVarint ? VarInt.readUnsignedVarInt( this ) : this.readShortValue();
        this.expectInput( length, "Invalid NBT Data: Expected string bytes" );

        byte[] data = new byte[length];
        this.in.readBytes( data );

        return StringUtil.fromUTF8Bytes( data, 0, data.length );
    }

    protected short readShortValue() throws IOException, AllocationLimitReachedException {
        this.expectInput( 2, "Invalid NBT Data: Expected short" );

        if (this.byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return this.in.readShortLE();
        } else {
            return this.in.readShort();
        }
    }

    protected int readIntValue() throws IOException, AllocationLimitReachedException {
        if ( this.useVarint ) {
            return VarInt.readSignedVarInt( this );
        }

        this.expectInput( 4, "Invalid NBT Data: Expected int" );

        if (this.byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return this.in.readIntLE();
        } else {
            return this.in.readInt();
        }
    }

    protected long readLongValue() throws IOException, AllocationLimitReachedException {
        if ( this.useVarint ) {
            return VarInt.readSignedVarLong( this ).longValue();
        } else {
            this.expectInput( 8, "Invalid NBT Data: Expected long" );

            if (this.byteOrder == ByteOrder.LITTLE_ENDIAN) {
                return this.in.readLongLE();
            } else {
                return this.in.readLong();
            }
        }
    }

    protected float readFloatValue() throws IOException, AllocationLimitReachedException {
        this.expectInput( 4, "Invalid NBT Data: Expected float" );

        if (this.byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return this.in.readFloatLE();
        }

        return this.in.readFloat();
    }

    protected double readDoubleValue() throws IOException, AllocationLimitReachedException {
        this.expectInput( 8, "Invalid NBT Data: Expected double" );

        if (this.byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return this.in.readDoubleLE();
        }

        return this.in.readDouble();
    }

    protected byte[] readByteArrayValue() throws IOException, AllocationLimitReachedException {
        int size = this.readIntValue();
        this.expectInput( size, "Invalid NBT Data: Expected byte array data" );
        byte[] data = new byte[size];
        this.in.readBytes( data );
        return data;
    }

    protected int[] readIntArrayValue() throws IOException, AllocationLimitReachedException {
        int size = this.readIntValue();
        this.expectInput( this.varint() ? size : size * 4, "Invalid NBT Data: Expected int array data" );
        int[] result = new int[size];
        for ( int i = 0; i < size; ++i ) {
            result[i] = this.readIntValue();
        }
        return result;
    }

    protected void expectInput( int remaining, String message ) throws IOException, AllocationLimitReachedException {
        this.expectInput( remaining, message, true );
    }

    protected void expectInput( int remaining, String message, boolean alterAllocationLimit ) throws IOException, AllocationLimitReachedException {
        if ( alterAllocationLimit ) {
            this.alterAllocationLimit( remaining );
        }

        if ( this.in.readableBytes() < remaining ) {
            throw new IOException( message );
        }
    }

    void alterAllocationLimit( int remaining ) throws AllocationLimitReachedException {
        if ( this.allocateLimit != -1 ) {
            if ( this.allocateLimit - remaining < 0 ) {
                throw new AllocationLimitReachedException( "Could not allocate more bytes due to reaching the set limit" );
            } else {
                this.allocateLimit -= remaining;
            }
        }
    }

}
