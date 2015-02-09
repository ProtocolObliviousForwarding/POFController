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
 * Modify field value with +increment value. could be negative (complement) to minus
 * 
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 *
 */
public class OFActionModifyField extends OFAction {
    public static int MINIMUM_LENGTH = OFAction.MINIMUM_LENGTH + OFMatch20.MINIMUM_LENGTH + 8;
    
    protected OFMatch20 matchField;
    protected int increment;
    
    public OFActionModifyField(){
        super.setType(OFActionType.MODIFY_FIELD);
        super.setLength((short) MINIMUM_LENGTH);
    }
    
    public void readFrom(ChannelBuffer data){
        super.readFrom(data);
        matchField = new OFMatch20();
        matchField.readFrom(data);
        this.increment = data.readInt();
        data.readBytes(4);
    }
    
    public void writeTo(ChannelBuffer data){
        super.writeTo(data);
        matchField.writeTo(data);
        data.writeInt(increment);
        data.writeZero(4);
    }
    
    public String toBytesString(){
        return super.toBytesString() +
                matchField.toBytesString() +
                HexString.toHex(increment) +
                HexString.ByteZeroEnd(4);
    }
    
    public String toString(){
        return super.toString() +
                ";fm=" + matchField.toString() +
                ";inc=" + increment;
    }

    public OFMatch20 getMatchField() {
        return matchField;
    }

    public void setMatchField(OFMatch20 fieldMatch) {
        this.matchField = fieldMatch;
    }

    public int getIncrement() {
        return increment;
    }

    public void setIncrement(int increment) {
        this.increment = increment;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((matchField == null) ? 0 : matchField.hashCode());
        result = prime * result + increment;
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
        OFActionModifyField other = (OFActionModifyField) obj;
        if (matchField == null) {
            if (other.matchField != null)
                return false;
        } else if (!matchField.equals(other.matchField))
            return false;
        if (increment != other.increment)
            return false;
        return true;
    }
    
    @Override
    public OFActionModifyField clone() throws CloneNotSupportedException {
        OFActionModifyField action = (OFActionModifyField) super.clone();
        if(null != matchField){
            action.setMatchField(matchField.clone());
        }
        return action;
    }    
}
