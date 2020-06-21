package io.gomint.taglib;

import java.io.IOException;
import java.math.BigInteger;

/**
 * @author geNAZt
 * @version 1.0
 */
class VarInt {

    private static final BigInteger UNSIGNED_LONG_MAX_VALUE = new BigInteger( "FFFFFFFFFFFFFFFF", 16 );

    private static long encodeZigZag32( int v ) {
        return (long) ( v << 1 ^ v >> 31 );
    }

    private static int decodeZigZag32( long v ) {
        return (int) ( v >> 1 ) ^ -( (int) ( v & 1L ) );
    }

    private static BigInteger decodeZigZag64( BigInteger v ) {
        BigInteger left = v.shiftRight( 1 );
        BigInteger right = v.and( BigInteger.ONE ).negate();
        return left.xor( right );
    }

    private static BigInteger encodeZigZag64( long v ) {
        BigInteger origin = BigInteger.valueOf( v );
        BigInteger left = origin.shiftLeft( 1 );
        BigInteger right = origin.shiftRight( 63 );
        return left.xor( right );
    }

    static BigInteger readSignedVarLong( NBTStreamReader reader ) throws IOException, AllocationLimitReachedException {
        BigInteger val = readVarNumber( reader );
        return decodeZigZag64( val );
    }

    static void writeSignedVarLong( NBTWriter writer, long value ) throws IOException {
        BigInteger signedLong = encodeZigZag64( value );
        writeVarBigInteger( writer, signedLong );
    }

    private static BigInteger readVarNumber( NBTStreamReader reader ) throws IOException, AllocationLimitReachedException {
        BigInteger result = BigInteger.ZERO;
        int offset = 0;
        int b;

        do {
            if ( offset >= 10 ) {
                throw new IllegalArgumentException( "Var Number too big" );
            }

            b = reader.readByteValue();
            result = result.or( BigInteger.valueOf( ( b & 0x7f ) << ( offset * 7 ) ) );
            offset++;
        } while ( ( b & 0x80 ) > 0 );

        return result;
    }

    private static void writeVarBigInteger( NBTWriter writer, BigInteger value ) throws IOException {
        if ( value.compareTo( UNSIGNED_LONG_MAX_VALUE ) > 0 ) {
            throw new IllegalArgumentException( "The value is too big" );
        }

        value = value.and( UNSIGNED_LONG_MAX_VALUE );
        BigInteger i = BigInteger.valueOf( -128 );
        BigInteger BIX7F = BigInteger.valueOf( 0x7f );
        BigInteger BIX80 = BigInteger.valueOf( 0x80 );
        while ( !value.and( i ).equals( BigInteger.ZERO ) ) {
            writer.writeByteValue( value.and( BIX7F ).or( BIX80 ).byteValue() );
            value = value.shiftRight( 7 );
        }

        writer.writeByteValue( value.byteValue() );
    }

    static void writeUnsignedVarInt( NBTWriter writer, int value ) throws IOException {
        while ( ( value & -128 ) != 0 ) {
            writer.writeByteValue( (byte) ( value & 127 | 128 ) );
            value >>>= 7;
        }

        writer.writeByteValue( (byte) value );
    }

    static int readUnsignedVarInt( NBTStreamReader reader ) throws IOException, AllocationLimitReachedException {
        int out = 0;
        int bytes = 0;
        byte in;

        do {
            in = reader.readByteValue();
            out |= ( in & 0x7F ) << ( bytes++ * 7 );

            if ( bytes > 6 ) {
                throw new RuntimeException( "VarInt too big" );
            }
        } while ( ( in & 0x80 ) == 0x80 );

        return out;
    }


    private static void writeUnsignedVarLong( NBTWriter buffer, long value ) throws IOException {
        while ( ( value & -128L ) != 0L ) {
            buffer.writeByteValue( (byte) ( (int) ( value & 127L | 128L ) ) );
            value >>>= 7;
        }

        buffer.writeByteValue( (byte) ( (int) value ) );
    }

    private static long readUnsignedVarLong( NBTStreamReader buffer ) throws IOException, AllocationLimitReachedException {
        long out = 0L;
        int bytes = 0;

        byte in;
        do {
            in = buffer.readByteValue();
            out |= (long) ( ( in & 127 ) << bytes++ * 7 );
            if ( bytes > 7 ) {
                throw new RuntimeException( "VarInt too big" );
            }
        } while ( ( in & 128 ) == 128 );

        return out;
    }

    static int readSignedVarInt( NBTStreamReader buffer ) throws IOException, AllocationLimitReachedException {
        long val = readUnsignedVarLong( buffer );
        return decodeZigZag32( val );
    }

    static void writeSignedVarInt( NBTWriter buffer, int value ) throws IOException {
        long signedValue = encodeZigZag32( value );
        writeUnsignedVarLong( buffer, signedValue );
    }

}
