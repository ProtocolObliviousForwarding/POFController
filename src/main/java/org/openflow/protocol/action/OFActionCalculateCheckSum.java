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
 * re-calculate the checksum start from {@link #calcStartPosition} with length {@link #calcLength},
 * and write the result to {@link #checksumPosition} with length {@link #checksumLength} 
 * 
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 *
 */
public class OFActionCalculateCheckSum extends OFAction {
    public static int MINIMUM_LENGTH = OFAction.MINIMUM_LENGTH + 8;
    
    protected short checksumPosition;   //bit
    protected short checksumLength;     //bit
    protected short calcStartPosition;  //bit
    protected short calcLength;         //bit
    
    public OFActionCalculateCheckSum(){
        super.setType(OFActionType.CALCULATE_CHECKSUM);
        super.setLength((short) MINIMUM_LENGTH);
    }
    
    public void readFrom(ChannelBuffer data){
        super.readFrom(data);
        checksumPosition = data.readShort();
        checksumLength = data.readShort();
        calcStartPosition = data.readShort();
        calcLength = data.readShort();        
    }
    
    public void writeTo(ChannelBuffer data){
        super.writeTo(data);
        data.writeShort(checksumPosition);
        data.writeShort(checksumLength);
        data.writeShort(calcStartPosition);
        data.writeShort(calcLength);
    }
    
    public String toBytesString(){
        return super.toBytesString() +
                HexString.toHex(checksumPosition) +
                HexString.toHex(checksumLength) +
                " " +
                HexString.toHex(calcStartPosition) +                
                HexString.toHex(calcLength) +
                " ";
    }
    
    public String toString(){
        return super.toString() +
                ";ckpos=" + checksumPosition +
                ";cklen=" + checksumLength +
                ";clpos=" + calcStartPosition +
                ";cllen=" + calcLength;
    }

    public short getChecksumPosition() {
        return checksumPosition;
    }

    public void setChecksumPosition(short checksumPosition) {
        this.checksumPosition = checksumPosition;
    }

    public short getChecksumLength() {
        return checksumLength;
    }

    public void setChecksumLength(short checksumLength) {
        this.checksumLength = checksumLength;
    }

    public short getCalcStartPosition() {
        return calcStartPosition;
    }

    public void setCalcStartPosition(short calcStartPosition) {
        this.calcStartPosition = calcStartPosition;
    }

    public short getCalcLength() {
        return calcLength;
    }

    public void setCalcLength(short calcLength) {
        this.calcLength = calcLength;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + calcLength;
        result = prime * result + calcStartPosition;
        result = prime * result + checksumLength;
        result = prime * result + checksumPosition;
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
        OFActionCalculateCheckSum other = (OFActionCalculateCheckSum) obj;
        if (calcLength != other.calcLength)
            return false;
        if (calcStartPosition != other.calcStartPosition)
            return false;
        if (checksumLength != other.checksumLength)
            return false;
        if (checksumPosition != other.checksumPosition)
            return false;
        return true;
    }
    
    
}
