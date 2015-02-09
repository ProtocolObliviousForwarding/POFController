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

package org.openflow.protocol.action;

import org.jboss.netty.buffer.ChannelBuffer;
import org.openflow.util.HexString;

/**
 * Delete a field with field position and length.
 * 
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 *
 */
public class OFActionDeleteField extends OFAction {
    public static int MINIMUM_LENGTH = OFAction.MINIMUM_LENGTH + 8;
    
    protected short fieldPosition;  //bit
    protected int fieldLength;      //bit
    
    public OFActionDeleteField(){
        super.setType(OFActionType.DELETE_FIELD);
        super.setLength((short) MINIMUM_LENGTH);
    }
    
    public void readFrom(ChannelBuffer data){
        super.readFrom(data);
        this.fieldPosition = data.readShort();
        data.readBytes(2);
        this.fieldLength = data.readInt();
    }
    
    public void writeTo(ChannelBuffer data){
        super.writeTo(data);
        data.writeShort(fieldPosition);
        data.writeZero(2);
        data.writeInt(fieldLength);
    }
    
    public String toBytesString(){
        return super.toBytesString() +
                HexString.toHex(fieldPosition) +
                HexString.ByteZeroEnd(2) +
                HexString.toHex(fieldLength);
    }
    
    public String toString(){
        return super.toString() +
                ";fpos=" + fieldPosition +
                ";flen=" + fieldLength;
    }

    public short getFieldPosition() {
        return fieldPosition;
    }

    public void setFieldPosition(short fieldPosition) {
        this.fieldPosition = fieldPosition;
    }

    public int getFieldLength() {
        return fieldLength;
    }

    public void setFieldLength(int fieldLength) {
        this.fieldLength = fieldLength;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + fieldLength;
        result = prime * result + fieldPosition;
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
        OFActionDeleteField other = (OFActionDeleteField) obj;
        if (fieldLength != other.fieldLength)
            return false;
        if (fieldPosition != other.fieldPosition)
            return false;
        return true;
    }
    
    
}
