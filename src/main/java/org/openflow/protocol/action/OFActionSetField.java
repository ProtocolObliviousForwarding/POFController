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
import org.openflow.protocol.OFMatchX;

/**
 * Set field value using {@link #fieldSetting} (value/mask).
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 *
 */
public class OFActionSetField extends OFAction {
    public static int MINIMUM_LENGTH = OFAction.MINIMUM_LENGTH + OFMatchX.MINIMUM_LENGTH;
    protected OFMatchX fieldSetting;
    
    public OFActionSetField(){
        super.setType(OFActionType.SET_FIELD);
        super.setLength((short)MINIMUM_LENGTH);
    }
    
    public void readFrom(ChannelBuffer data){
        super.readFrom(data);
        fieldSetting = new OFMatchX();
        fieldSetting.readFrom(data);
    }
    
    public void writeTo(ChannelBuffer data){
        super.writeTo(data);
        fieldSetting.writeTo(data);
    }
    
    public String toBytesString(){
        return super.toBytesString() +
                fieldSetting.toBytesString();
    }
    
    public String toString(){
        return super.toString() +
                ";fs=" + fieldSetting.toString();
    }

    public OFMatchX getFieldSetting() {
        return fieldSetting;
    }

    public void setFieldSetting(OFMatchX fieldSetting) {
        this.fieldSetting = fieldSetting;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((fieldSetting == null) ? 0 : fieldSetting.hashCode());
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
        OFActionSetField other = (OFActionSetField) obj;
        if (fieldSetting == null) {
            if (other.fieldSetting != null)
                return false;
        } else if (!fieldSetting.equals(other.fieldSetting))
            return false;
        return true;
    }
    
    @Override
    public OFActionSetField clone() throws CloneNotSupportedException {
        OFActionSetField action = (OFActionSetField) super.clone();
        if(null != fieldSetting){
            action.setFieldSetting(fieldSetting.clone());
        }
        return action;
    }
    
    
    
}
