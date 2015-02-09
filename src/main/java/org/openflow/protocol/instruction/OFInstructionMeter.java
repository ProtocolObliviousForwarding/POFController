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
 * Using the Meter( {@link #meterId}) to limit the flow rate
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 */
public class OFInstructionMeter extends OFInstruction {
    public static int MINIMUM_LENGTH = OFInstruction.MINIMUM_LENGTH + 8;
    
    protected int meterId;

    public OFInstructionMeter(){
        super.setType(OFInstructionType.METER);
        super.setLength((short)MINIMUM_LENGTH);
    }
    
    @Override
    public void readFrom(ChannelBuffer data){
        super.readFrom(data);
        meterId = data.readInt();
        data.readBytes(4);
    }
    
    @Override
    public void writeTo(ChannelBuffer data){
        super.writeTo(data);
        data.writeInt(meterId);
        data.writeZero(4);
    }
    
    @Override
    public String toBytesString(){
        return super.toBytesString() +
                HexString.toHex(meterId) +
                HexString.ByteZeroEnd(4);
    }
    
    @Override
    public String toString(){
        return super.toString() +
                ";mid=" + meterId;
    }

    public int getMeterId() {
        return meterId;
    }

    public void setMeterId(int meterId) {
        this.meterId = meterId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + meterId;
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
        OFInstructionMeter other = (OFInstructionMeter) obj;
        if (meterId != other.meterId)
            return false;
        return true;
    }

}
