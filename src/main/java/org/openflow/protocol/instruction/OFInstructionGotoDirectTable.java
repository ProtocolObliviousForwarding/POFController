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
 * Goto next <B>direct</B> table.<br>
 * If next table is not direct table, use {@link OFInstructionGotoTable} instead.
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 */
public class OFInstructionGotoDirectTable extends OFInstruction {
    public static final int MINIMUM_LENGTH = OFInstruction.MINIMUM_LENGTH + 8;
    
    protected byte nextTableId;
    protected int tableEntryIndex;
    protected short packetOffset;       //byte

    public OFInstructionGotoDirectTable(){
        super.setType(OFInstructionType.GOTO_DIRECT_TABLE);
        super.setLength((short) MINIMUM_LENGTH);
    }
    
    @Override
    public void readFrom(ChannelBuffer data){
        super.readFrom(data);
        this.nextTableId = data.readByte();
        data.readBytes(1);
        this.packetOffset = data.readShort();
        this.tableEntryIndex = data.readInt();
    }
    
    @Override
    public void writeTo(ChannelBuffer data){
        super.writeTo(data);
        data.writeByte(nextTableId);
        data.writeZero(1);
        data.writeShort(packetOffset);
        data.writeInt(tableEntryIndex);
    }
    
    @Override
    public String toBytesString(){
        return super.toBytesString() +
                HexString.toHex(nextTableId) +
                HexString.ByteZeroEnd(1) +
                HexString.toHex(packetOffset) +
                " " +
                HexString.toHex(tableEntryIndex);
    }
    
    @Override
    public String toString(){
        return super.toString() +
                ";ntid=" + nextTableId +
                ";poff=" + packetOffset +
                ";tei=" + tableEntryIndex;
    }

    public byte getNextTableId() {
        return nextTableId;
    }

    public void setNextTableId(byte nextTableId) {
        this.nextTableId = nextTableId;
    }

    public int getTableEntryIndex() {
        return tableEntryIndex;
    }

    public void setTableEntryIndex(int tableEntryIndex) {
        this.tableEntryIndex = tableEntryIndex;
    }

    public short getPacketOffset() {
        return packetOffset;
    }

    public void setPacketOffset(short packetOffset) {
        this.packetOffset = packetOffset;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + nextTableId;
        result = prime * result + packetOffset;
        result = prime * result + tableEntryIndex;
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
        OFInstructionGotoDirectTable other = (OFInstructionGotoDirectTable) obj;
        if (nextTableId != other.nextTableId)
            return false;
        if (packetOffset != other.packetOffset)
            return false;
        if (tableEntryIndex != other.tableEntryIndex)
            return false;
        return true;
    }    
}
