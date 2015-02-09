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
 * OFCounter, used in OFCounterMod, OFCounterReply, OFCounterRequest.
 * 
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 *
 */
public class OFCounter {
    public static int MINIMUM_LENGTH = 16;
    
    public enum OFCounterModCmd {
        OFPCC_ADD,
        OFPCC_DELETE,
        OFPCC_CLEAR,
        OFPCC_QUERY
    }
    
    protected OFCounterModCmd command;
    protected int counterId;
    protected long value;
    
    public void readFrom(ChannelBuffer data) {
        command = OFCounterModCmd.values()[ data.readByte() ];
        data.readBytes(3);
        counterId = data.readInt();
        value = data.readLong();
    }

    public void writeTo(ChannelBuffer data) {
        data.writeByte((byte)command.ordinal());
        data.writeZero(3);
        data.writeInt(counterId);
        data.writeLong(value);
    }
    
    public String toBytesString(){
        return HexString.toHex((byte)command.ordinal()) +
                HexString.ByteZeroEnd(3)+
                HexString.toHex(counterId) +
                HexString.toHex(value);
    }
    
    public String toString(){
        return "cmd=" + command +
                ";cid=" + counterId +
                ";value=" + value;
    }
    
    public OFCounterModCmd getCommand() {
        return command;
    }
    public void setCommand(OFCounterModCmd command) {
        this.command = command;
    }
    public int getCounterId() {
        return counterId;
    }
    public void setCounterId(int counterId) {
        this.counterId = counterId;
    }
    public long getValue() {
        return value;
    }
    public void setValue(long value) {
        this.value = value;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((command == null) ? 0 : command.hashCode());
        result = prime * result + counterId;
        result = prime * result + (int) (value ^ (value >>> 32));
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
        OFCounter other = (OFCounter) obj;
        if (command != other.command)
            return false;
        if (counterId != other.counterId)
            return false;
        if (value != other.value)
            return false;
        return true;
    }
    
    
}
