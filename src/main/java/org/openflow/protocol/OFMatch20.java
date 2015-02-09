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

package org.openflow.protocol;

import org.jboss.netty.buffer.ChannelBuffer;
import org.openflow.util.HexString;

/**
 * Represents an ofp_match_msg. 
 * <p>
 * Using OFMatch20 instead of OFMatch in POF. OFMatch20 describes a field of 
 * a protocol or a field of a packet. Different with OFMatch message which 
 * contains certain protocol information such as mac/ip/port, OFMatch20 just 
 * uses fieldId, offset and length to present a field. So users could use 
 * several OFMatch20 to create a total new protocol or packet type.
 * 
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 *
 */
public class OFMatch20 implements Cloneable{
	public static final int MINIMUM_LENGTH = 8;
    
    public static final short METADATA_FIELD_ID = (short) 0xffff;
    
    protected String fieldName;	//just store, do not send
    protected short fieldId;
    protected short offset;     //bit
    protected short length;     //bit
    
    public OFMatch20(){}
    
    public void readFrom(ChannelBuffer data){
        this.fieldId = data.readShort();
        this.offset  = data.readShort();
        this.length  = data.readShort();
        data.readShort();
    }
    
    public void writeTo(ChannelBuffer data){
        data.writeShort(fieldId);
        data.writeShort(offset);
        data.writeShort(length);
        data.writeShort(0);
    }
    
    public String toBytesString(){
        return HexString.toHex(fieldId) +
                HexString.toHex(offset) +
                HexString.toHex(length) +
                HexString.ByteZeroEnd(2);
    }
    
    public String toString(){
        return "fid=" + fieldId +
        		((fieldName==null) ? "" : (";name=" + fieldName)) +
                ";ofst=" + offset +
                ";len=" + length;
    }
    
    public int hashCode(){
        final int prime = 641;
        int result = 1;
        
        result = prime * result + fieldId;
        result = prime * result + offset;
        result = prime * result + length;
        
        return result;
    }
    
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof OFMatch20)) {
            return false;
        }
        OFMatch20 other = (OFMatch20) obj;
        
        if (fieldId != other.fieldId) {
            return false;
        }
        if (offset != other.offset) {
            return false;
        }
        if (length != other.length) {
            return false;
        }
        
        return true;
    }
    
    public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public short getFieldId() {
        return fieldId;
    }
    public void setFieldId(short fieldId) {
        this.fieldId = fieldId;
    }
    public short getOffset() {
        return offset;
    }
    public void setOffset(short offset) {
        this.offset = offset;
    }
    public short getLength() {
        return length;
    }
    public void setLength(short length) {
        this.length = length;
    }
    
    @Override
    public OFMatch20 clone() throws CloneNotSupportedException {
        return (OFMatch20) super.clone();
    }
}
