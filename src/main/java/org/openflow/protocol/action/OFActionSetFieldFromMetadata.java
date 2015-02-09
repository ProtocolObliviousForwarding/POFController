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
import org.openflow.protocol.OFMatch20;
import org.openflow.util.HexString;

/**
 * Set field value from metadata. <br>
 * Write Src: metadata value start from {@link #metadataOffset}, length = {@link #fieldSetting}.length <br>
 * Write Des: field start from {@link #fieldSetting}.offset, length = {@link #fieldSetting}.length <br>
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 *
 */
public class OFActionSetFieldFromMetadata extends OFAction {
    public static int MINIMUM_LENGTH = OFAction.MINIMUM_LENGTH + OFMatch20.MINIMUM_LENGTH + 8;
    
    protected OFMatch20 fieldSetting;
    protected short     metadataOffset;     //bit
    
    public OFActionSetFieldFromMetadata(){
        super.setType(OFActionType.SET_FIELD_FROM_METADATA);
        super.setLength((short) MINIMUM_LENGTH);
    }
    
    public void readFrom(ChannelBuffer data){
        super.readFrom(data);
        fieldSetting = new OFMatch20();
        fieldSetting.readFrom(data);
        this.metadataOffset = data.readShort();
        data.readBytes(6);
    }
    
    public void writeTo(ChannelBuffer data){
        super.writeTo(data);
        fieldSetting.writeTo(data);
        data.writeShort(metadataOffset);
        data.writeZero(6);
    }
    
    public String toBytesString(){
        return super.toBytesString() +
                fieldSetting.toBytesString() +
                HexString.toHex(metadataOffset) +
                HexString.ByteZeroEnd(6);
    }
    
    public String toString(){
        return super.toString() +
                ";fs=" + fieldSetting.toString() +
                ";mos=" + metadataOffset;
    }

    public OFMatch20 getFieldSetting() {
        return fieldSetting;
    }

    public void setFieldSetting(OFMatch20 fieldSetting) {
        this.fieldSetting = fieldSetting;
    }

    public short getMetadataOffset() {
        return metadataOffset;
    }

    public void setMetadataOffset(short metadataOffset) {
        this.metadataOffset = metadataOffset;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((fieldSetting == null) ? 0 : fieldSetting.hashCode());
        result = prime * result + metadataOffset;
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
        OFActionSetFieldFromMetadata other = (OFActionSetFieldFromMetadata) obj;
        if (fieldSetting == null) {
            if (other.fieldSetting != null)
                return false;
        } else if (!fieldSetting.equals(other.fieldSetting))
            return false;
        if (metadataOffset != other.metadataOffset)
            return false;
        return true;
    }
    
    @Override
    public OFActionSetFieldFromMetadata clone() throws CloneNotSupportedException {
        OFActionSetFieldFromMetadata action = (OFActionSetFieldFromMetadata) super.clone();
        if(null != fieldSetting){
            action.setFieldSetting(fieldSetting.clone());
        }
        return action;
    } 
    
    
}
