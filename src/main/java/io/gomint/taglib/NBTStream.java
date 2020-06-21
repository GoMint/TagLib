package io.gomint.taglib;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author geNAZt
 * @version 1.0
 */
public class NBTStream extends NBTStreamReader {

    private enum StreamState {
        INIT,
        PARSING,
    }

    private StreamState state;
    private NBTStreamListener nbtStreamListener;
    private Function<String, Boolean> nbtCompoundAcceptor;

    public NBTStream( ByteBuf in, ByteOrder byteOrder ) {
        super( in, byteOrder );
        this.state = StreamState.INIT;
    }

    public void addCompountAcceptor( Function<String, Boolean> acceptor ) {
        if ( state == StreamState.INIT ) {
            this.nbtCompoundAcceptor = acceptor;
        }
    }

    public void addListener( NBTStreamListener listener ) {
        if ( state == StreamState.INIT ) {
            this.nbtStreamListener = listener;
        }
    }

    public void parse() throws Exception {
        state = StreamState.PARSING;

        if ( this.nbtStreamListener == null ) {
            return;
        }

        this.expectInput( 2, "Invalid NBT Data: Not enough data to read new tag", false );
        if ( this.readByteValue() != NBTDefinitions.TAG_COMPOUND ) {
            throw new IOException( "Invalid NBT Data: No list tag found" );
        }

        // Start reading the compound
        this.readTagCompoundValue( this.readStringValue(), "", false );
    }

    private NBTTagCompound readTagCompoundValue( String path, String tagName, boolean readAsCompound ) throws Exception {
        boolean manual = false;
        if ( !readAsCompound && this.nbtCompoundAcceptor != null ) {
            // Ask the acceptor if he wants the compound as a whole or not
            readAsCompound = manual = this.nbtCompoundAcceptor.apply( path );
        }

        this.expectInput( 1, "Invalid NBT Data: Expected Tag ID in compound tag" );
        byte tagID = this.readByteValue();

        NBTTagCompound compound = ( readAsCompound ) ? new NBTTagCompound( tagName ) : null;
        while ( tagID != NBTDefinitions.TAG_END ) {
            String name = this.readStringValue();
            String currentPath = path + "." + name;

            switch ( tagID ) {
                case NBTDefinitions.TAG_BYTE:
                    if ( compound != null ) {
                        compound.addValue( name, this.readByteValue() );
                        break;
                    }

                    this.nbtStreamListener.onNBTValue( currentPath, this.readByteValue() );
                    break;
                case NBTDefinitions.TAG_SHORT:
                    if ( compound != null ) {
                        compound.addValue( name, this.readShortValue() );
                        break;
                    }

                    this.nbtStreamListener.onNBTValue( currentPath, this.readShortValue() );
                    break;
                case NBTDefinitions.TAG_INT:
                    if ( compound != null ) {
                        compound.addValue( name, this.readIntValue() );
                        break;
                    }

                    this.nbtStreamListener.onNBTValue( currentPath, this.readIntValue() );
                    break;
                case NBTDefinitions.TAG_LONG:
                    if ( compound != null ) {
                        compound.addValue( name, this.readLongValue() );
                        break;
                    }

                    this.nbtStreamListener.onNBTValue( currentPath, this.readLongValue() );
                    break;
                case NBTDefinitions.TAG_FLOAT:
                    if ( compound != null ) {
                        compound.addValue( name, this.readFloatValue() );
                        break;
                    }

                    this.nbtStreamListener.onNBTValue( currentPath, this.readFloatValue() );
                    break;
                case NBTDefinitions.TAG_DOUBLE:
                    if ( compound != null ) {
                        compound.addValue( name, this.readDoubleValue() );
                        break;
                    }

                    this.nbtStreamListener.onNBTValue( currentPath, this.readDoubleValue() );
                    break;
                case NBTDefinitions.TAG_BYTE_ARRAY:
                    if ( compound != null ) {
                        compound.addValue( name, this.readByteArrayValue() );
                        break;
                    }

                    this.nbtStreamListener.onNBTValue( currentPath, this.readByteArrayValue() );
                    break;
                case NBTDefinitions.TAG_STRING:
                    if ( compound != null ) {
                        compound.addValue( name, this.readStringValue() );
                        break;
                    }

                    this.nbtStreamListener.onNBTValue( currentPath, this.readStringValue() );
                    break;
                case NBTDefinitions.TAG_LIST:
                    if ( compound != null ) {
                        compound.addValue( name, this.readTagListValue( currentPath, true ) );
                        break;
                    }

                    this.readTagListValue( currentPath, false );
                    break;
                case NBTDefinitions.TAG_COMPOUND:
                    if ( compound != null ) {
                        compound.addValue( name, this.readTagCompoundValue( currentPath, name, true ) );
                        break;
                    }

                    this.readTagCompoundValue( currentPath, name, false );
                    break;
                case NBTDefinitions.TAG_INT_ARRAY:
                    this.nbtStreamListener.onNBTValue( currentPath, this.readIntArrayValue() );
                    break;
                default:
                    throw new IOException( "Invalid NBT Data: Unknown tag <" + tagID + ">" );
            }

            this.expectInput( 1, "Invalid NBT Data: Expected tag ID in tag compound" );
            tagID = this.readByteValue();
        }

        if ( manual ) {
            this.nbtStreamListener.onNBTValue( path, compound );
        }

        return compound;
    }

    private List<Object> readTagListValue( String path, boolean readAsList ) throws Exception {
        boolean manual = false;
        if ( !readAsList && this.nbtCompoundAcceptor != null ) {
            // Ask the acceptor if he wants the compound as a whole or not
            readAsList = manual = this.nbtCompoundAcceptor.apply( path );
        }

        this.expectInput( 5, "Invalid NBT Data: Expected TAGList header" );
        byte listType = this.readByteValue();
        int listLength = this.readIntValue();

        List<Object> list = ( readAsList ) ? new ArrayList<>() : null;
        switch ( listType ) {
            case NBTDefinitions.TAG_END:
                // Not to be unseen! Seemingly Mojang cares about something after all: disk space
                break;
            case NBTDefinitions.TAG_BYTE:
                this.expectInput( listLength, "Invalid NBT Data: Expected bytes for list" );

                for ( int i = 0; i < listLength; ++i ) {
                    if ( list != null ) {
                        list.add( this.readByteValue() );
                    } else {
                        this.nbtStreamListener.onNBTValue( path + "." + i, this.readByteValue() );
                    }
                }

                break;
            case NBTDefinitions.TAG_SHORT:
                this.expectInput( 2 * listLength, "Invalid NBT Data: Expected shorts for list" );

                for ( int i = 0; i < listLength; ++i ) {
                    if ( list != null ) {
                        list.add( this.readShortValue() );
                    } else {
                        this.nbtStreamListener.onNBTValue( path + "." + i, this.readShortValue() );
                    }
                }

                break;
            case NBTDefinitions.TAG_INT:
                this.expectInput( 4 * listLength, "Invalid NBT Data: Expected ints for list" );

                for ( int i = 0; i < listLength; ++i ) {
                    if ( list != null ) {
                        list.add( this.readIntValue() );
                    } else {
                        this.nbtStreamListener.onNBTValue( path + "." + i, this.readIntValue() );
                    }
                }

                break;
            case NBTDefinitions.TAG_LONG:
                this.expectInput( 8 * listLength, "Invalid NBT Data: Expected longs for list" );

                for ( int i = 0; i < listLength; ++i ) {
                    if ( list != null ) {
                        list.add( this.readLongValue() );
                    } else {
                        this.nbtStreamListener.onNBTValue( path + "." + i, this.readLongValue() );
                    }
                }

                break;
            case NBTDefinitions.TAG_FLOAT:
                this.expectInput( 4 * listLength, "Invalid NBT Data: Expected floats for list" );

                for ( int i = 0; i < listLength; ++i ) {
                    if ( list != null ) {
                        list.add( this.readFloatValue() );
                    } else {
                        this.nbtStreamListener.onNBTValue( path + "." + i, this.readFloatValue() );
                    }
                }

                break;
            case NBTDefinitions.TAG_DOUBLE:
                this.expectInput( 8 * listLength, "Invalid NBT Data: Expected doubles for list" );

                for ( int i = 0; i < listLength; ++i ) {
                    if ( list != null ) {
                        list.add( this.readDoubleValue() );
                    } else {
                        this.nbtStreamListener.onNBTValue( path + "." + i, this.readDoubleValue() );
                    }
                }

                break;
            case NBTDefinitions.TAG_BYTE_ARRAY:
                for ( int i = 0; i < listLength; ++i ) {
                    if ( list != null ) {
                        list.add( this.readByteArrayValue() );
                    } else {
                        this.nbtStreamListener.onNBTValue( path + "." + i, this.readByteArrayValue() );
                    }
                }

                break;
            case NBTDefinitions.TAG_STRING:
                for ( int i = 0; i < listLength; ++i ) {
                    if ( list != null ) {
                        list.add( this.readStringValue() );
                    } else {
                        this.nbtStreamListener.onNBTValue( path + "." + i, this.readStringValue() );
                    }
                }

                break;
            case NBTDefinitions.TAG_LIST:
                for ( int i = 0; i < listLength; ++i ) {
                    if ( list != null ) {
                        list.add( this.readTagListValue( path + "." + i, true ) );
                    } else {
                        this.readTagListValue( path + "." + i, false );
                    }
                }

                break;
            case NBTDefinitions.TAG_COMPOUND:
                for ( int i = 0; i < listLength; ++i ) {
                    if ( list != null ) {
                        list.add( this.readTagCompoundValue( path + "." + i, "", true ) );
                    } else {
                        this.readTagCompoundValue( path + "." +  i, "", false );
                    }
                }

                break;
            case NBTDefinitions.TAG_INT_ARRAY:
                for ( int i = 0; i < listLength; ++i ) {
                    if ( list != null ) {
                        list.add( this.readIntArrayValue() );
                    } else {
                        this.nbtStreamListener.onNBTValue( path + "." + i, this.readIntArrayValue() );
                    }
                }

                break;
            default:
                throw new IOException( "Invalid NBT Data: Unknown tag <" + listType + ">" );
        }

        if ( manual ) {
            this.nbtStreamListener.onNBTValue( path, list );
        }

        return list;
    }

}
