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
import org.openflow.protocol.OFGlobal;
import org.openflow.util.HexString;

/**
 * The base class for OFInstruction. All OFInstructions must extend from this.
 * 
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 */
public class OFInstruction implements Cloneable {
    public static final int MINIMUM_LENGTH = 8;
    public static final int MAXIMAL_LENGTH = OFGlobal.OFP_MAX_INSTRUCTION_LENGTH;
    
    protected OFInstructionType type;
    protected short length;
    
    public OFInstructionType getType() {
        return type;
    }
    
    public void setType(OFInstructionType type) {
        this.type = type;
    }
    
    public short getLength() {
        return length;
    }
    
    public int getLengthU() {
        //return U16.f(length);
        return MAXIMAL_LENGTH;
    }
    
    public void setLength(short length) {
        this.length = length;
    }
    
    public String toBytesString(){
        return HexString.toHex(type.getTypeValue()) +
                HexString.toHex(length) + 
                " " +
                HexString.ByteZeroEnd(4);
    }
    
    public String toString() {
        return "ofinstruction:" +
            "t=" + this.getType() +
            ";l=" + this.getLength();
    }
    
    public void readFrom(ChannelBuffer data) {
        this.type = OFInstructionType.valueOf(data.readShort());
        this.length = data.readShort();
        data.readInt();
    }

    public void writeTo(ChannelBuffer data) {
        data.writeShort(type.getTypeValue());
        data.writeShort(length);
        data.writeInt(0);
    }

    @Override
    public int hashCode() {
        final int prime = 347;
        int result = 1;
        result = prime * result + length;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof OFInstruction)) {
            return false;
        }
        OFInstruction other = (OFInstruction) obj;
        if (length != other.length) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public OFInstruction clone() throws CloneNotSupportedException {
        return (OFInstruction) super.clone();
    } 
}
