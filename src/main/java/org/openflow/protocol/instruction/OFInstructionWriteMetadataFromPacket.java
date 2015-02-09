/**
 * Copyright (c) 2012, 2013, Huawei Technologies Co., Ltd.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.openflow.protocol.instruction;

import org.jboss.netty.buffer.ChannelBuffer;
import org.openflow.util.HexString;

/**
 * Write metadata at {@link #metadataOffset} with {@link #writeLength}, 
 * the value from packet at {@link #packetOffset} with {@link #writeLength}
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 */
public class OFInstructionWriteMetadataFromPacket extends OFInstruction {
    public static int MINIMUM_LENGTH = OFInstruction.MINIMUM_LENGTH + 8;
    
    protected short metadataOffset;     //bit
    protected short packetOffset;       //bit
    protected short writeLength;        //bit
    
    public OFInstructionWriteMetadataFromPacket(){
        super.setType(OFInstructionType.WRITE_METADATA_FROM_PACKET);
        super.setLength((short)MINIMUM_LENGTH);
    }
    
    @Override
    public void readFrom(ChannelBuffer data){
        super.readFrom(data);
        metadataOffset = data.readShort();
        packetOffset = data.readShort();
        writeLength = data.readShort();
        data.readBytes(2);
    }
    
    @Override
    public void writeTo(ChannelBuffer data){
        super.writeTo(data);
        data.writeShort(metadataOffset);
        data.writeShort(packetOffset);
        data.writeShort(writeLength);
        data.writeZero(2);        
    }
    
    @Override
    public String toBytesString(){
        return super.toBytesString() +
                HexString.toHex(metadataOffset) +
                HexString.toHex(packetOffset) +
                " " +
                HexString.toHex(writeLength) +
                HexString.ByteZeroEnd(2);
    }
    
    @Override
    public String toString(){
        return super.toString() +
                ";mos=" + metadataOffset +
                ";pos=" + packetOffset +
                ";wl" + writeLength;
    }

    public short getMetadataOffset() {
        return metadataOffset;
    }

    public void setMetadataOffset(short metadataOffset) {
        this.metadataOffset = metadataOffset;
    }

    public short getPacketOffset() {
        return packetOffset;
    }

    public void setPacketOffset(short packetOffset) {
        this.packetOffset = packetOffset;
    }

    public short getWriteLength() {
        return writeLength;
    }

    public void setWriteLength(short writeLength) {
        this.writeLength = writeLength;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + writeLength;
        result = prime * result + metadataOffset;
        result = prime * result + packetOffset;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        OFInstructionWriteMetadataFromPacket other = (OFInstructionWriteMetadataFromPacket) obj;
        if (writeLength != other.writeLength)
            return false;
        if (metadataOffset != other.metadataOffset)
            return false;
        if (packetOffset != other.packetOffset)
            return false;
        return true;
    }    
}
