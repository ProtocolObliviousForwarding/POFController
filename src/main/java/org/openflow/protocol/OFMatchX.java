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

import java.util.Arrays;

import org.jboss.netty.buffer.ChannelBuffer;
import org.openflow.util.HexString;

/**
 * Represents an ofp_matchX_msg. 
 * <p>
 * OFMatchX is a {@link OFMatch20} with value/mask. <br>
 * Value with mask presents an exact value. value and make
 * are byte[] array stream, e.g. a ip prefix (192.168.*.*)
 * in LPM table should presents as value=c0a8 and mask=ffff,
 * OFMatchX.writeTo() method will add the '0's tail. 
 * 
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 *
 */
public class OFMatchX implements Cloneable{
    public static final int MINIMUM_LENGTH = 40;
    
    protected String fieldName;
    protected short fieldId;
    protected short offset;     //bit
    protected short length;     //bit
    
    protected byte[] value;
    protected byte[] mask;
    
    public OFMatchX(){}
    
    public OFMatchX(OFMatch20 match, byte[] value, byte[] mask){
    	this.fieldName = match.getFieldName();
        this.fieldId = match.getFieldId();
        this.offset  = match.getOffset();
        this.length  = match.getLength();
        
        this.value = value;
        this.mask = mask;
    }
    
    public void readFrom(ChannelBuffer data){
        this.fieldId = data.readShort();
        this.offset  = data.readShort();
        this.length  = data.readShort();
        data.readShort();
        
        value = new byte[OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE];
        data.readBytes(value);
        
        mask = new byte[OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE];
        data.readBytes(mask);
    }
    
    public void writeTo(ChannelBuffer data){
        data.writeShort(fieldId);
        data.writeShort(offset);
        data.writeShort(length);
        data.writeShort(0);
        
        if(value == null){
            data.writeZero(OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE);
        }else{
            if(value.length > OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE){
                data.writeBytes(value, OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE - value.length, OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE);
            }else{
                data.writeBytes(value);
                data.writeZero(OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE - value.length);                
            }
        }
        
        if(mask == null){
            data.writeZero(OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE);
        }else{
            if(mask.length > OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE){
                data.writeBytes(mask, OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE - mask.length, OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE);
            }else{
                data.writeBytes(mask);
                data.writeZero(OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE - mask.length);                
            }
        }
    }
    
    public String toBytesString(){
        String string = HexString.toHex(fieldId) +
                        HexString.toHex(offset) +
                        HexString.toHex(length) +
                        HexString.ByteZeroEnd(2);

        
        if(value == null){
            string += HexString.ByteZeroEnd(OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE);
        }else{
            if(value.length > OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE){
                string += HexString.toHex(value, 0, OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE);
                string += HexString.ZeroEnd(0);
            }else{
                //string += HexString.ZeroEnd(0);
                string += HexString.toHex(value);
                //string += HexString.ZeroEnd(0);
                string += HexString.ByteZeroEnd(OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE - value.length );
            }
        }
        
        if(mask == null){
            string += HexString.ByteZeroEnd(OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE);
        }else{
            if(mask.length > OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE){
                string += HexString.toHex(mask, 0, OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE);
                string += HexString.ZeroEnd(0);
            }else{
                //string += HexString.ZeroEnd(0);
                string += HexString.toHex(mask);
                //string += HexString.ZeroEnd(0);
                string += HexString.ByteZeroEnd(OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE - mask.length);
            }
        }
        
        return string;
        
    }
    
    public String toString(){
        return ";fid=" + fieldId +
                ";ofst=" + offset +
                ";len=" + length +
                ";val=" + HexString.toHex(value) +
                ";mask=" + HexString.toHex(mask);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + fieldId;
        result = prime * result + length;
        result = prime * result + Arrays.hashCode(mask);
        result = prime * result + offset;
        result = prime * result + Arrays.hashCode(value);
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OFMatchX other = (OFMatchX) obj;
        if (fieldId != other.fieldId)
            return false;
        if (length != other.length)
            return false;
        if (!Arrays.equals(mask, other.mask))
            return false;
        if (offset != other.offset)
            return false;
        if (!Arrays.equals(value, other.value))
            return false;
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

    public byte[] getValue() {
        return value;
    }
    
    public void setValue(byte[] value) {
        this.value = value;
    }

    public byte[] getMask() {
        return mask;
    }
    
    public void setMask(byte[] mask) {
        this.mask = mask;
    }
    
    public String getFullHexValue(){
        String fullHexValueString = "";
        if(value == null){
            fullHexValueString += HexString.Zero((length - 1)/8 + 1);
        }else{
            if(value.length >= OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE){
                fullHexValueString += HexString.toHex(value, 0, OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE);
            }else{
                fullHexValueString += HexString.toHex(value);
                //fullHexValueString += HexString.Zero(OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE - value.length);
            }
        }
        
        return fullHexValueString;
    }
    
    public String getFullHexMask(){
        String fullHexMaskString = "";
        if(mask == null){
            fullHexMaskString += HexString.Zero((length - 1)/8 + 1);
        }else{
            if(mask.length >= OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE){
                fullHexMaskString += HexString.toHex(mask, 0, OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE);
            }else{
                fullHexMaskString += HexString.toHex(mask);
                //fullHexMaskString += HexString.Zero(OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE - mask.length);
            }
        }
        
        return fullHexMaskString;
    }
    
    @Override
    public OFMatchX clone() throws CloneNotSupportedException {
        OFMatchX matchX = (OFMatchX) super.clone();
        if(null != value){
            matchX.setValue(value.clone());
        }
        if(null != mask){
            matchX.setMask(mask.clone());
        }
        return matchX;
    }    
    
}
