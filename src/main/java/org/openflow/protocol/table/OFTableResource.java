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

package org.openflow.protocol.table;

import org.jboss.netty.buffer.ChannelBuffer;
import org.openflow.util.HexString;

/**
 * OFTableResource describes the table resource information of one table type. It is a part of {@link OFFlowTableResource}
 * <P>
 * OFTableResource <br>
 * {<br>
 *      int32   deviceID;<br>
 *      int8    tableType;<br>
 *      int8    tableNum;   //how many tables of this kind of table type could be<br>
 *      int16   keyLength;<br>
 *      <br>
 *      int32   totalSize;  //how many total entries of this kind of table type could be<br>
 *      int32   reserve;<br>
 * } //sizeof() = 16<br>
 * 
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 */
public class OFTableResource {
    public static int MINIMUM_LENGTH = 16;
    
    protected int deviceId;
    protected OFTableType tableType;
    protected byte tableNum;
    protected short keyLength;
    
    protected int totalSize;
    
    public void readFrom(ChannelBuffer data) {
        this.deviceId   = data.readInt();
        this.tableType  = OFTableType.values()[data.readByte()];
        this.tableNum   = data.readByte();
        this.keyLength  = data.readShort();
        
        this.totalSize  = data.readInt();
        data.readBytes(4);
    }
    
    public void writeTo(ChannelBuffer data){
        data.writeInt(this.deviceId);
        data.writeByte(this.tableType.getValue());
        data.writeByte(this.tableNum);
        data.writeShort(this.keyLength);
        
        data.writeInt(this.totalSize);
        data.writeZero(4);
    }
    
    public String toBytesString(){
        String bytesString = HexString.toHex(deviceId);
        
        bytesString += HexString.toHex(tableType.getValue());
        bytesString += HexString.toHex(tableNum);
        bytesString += HexString.toHex(keyLength);
        bytesString += " ";
        
        bytesString += HexString.toHex(totalSize);
        bytesString += HexString.ByteZeroEnd(4);
        
        return bytesString;
    }
    
    @Override
    public String toString(){
        return "did=" + this.deviceId +
                ";tt=" + this.tableType +
                ";tn=" + this.tableNum +
                ";kl=" + this.keyLength +
                ";ts=" + this.totalSize;
    }
    
    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public OFTableType getTableType() {
        return tableType;
    }

    public void setTableType(OFTableType tableType) {
        this.tableType = tableType;
    }

    public byte getTableNum() {
        return tableNum;
    }

    public void setTableNum(byte tableNum) {
        this.tableNum = tableNum;
    }

    public short getKeyLength() {
        return keyLength;
    }

    public void setKeyLength(short keyLength) {
        this.keyLength = keyLength;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + deviceId;
        result = prime * result + keyLength;
        result = prime * result + tableNum;
        result = prime * result + ((tableType == null) ? 0 : tableType.hashCode());
        result = prime * result + totalSize;
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
        OFTableResource other = (OFTableResource) obj;
        if (deviceId != other.deviceId)
            return false;
        if (keyLength != other.keyLength)
            return false;
        if (tableNum != other.tableNum)
            return false;
        if (tableType != other.tableType)
            return false;
        if (totalSize != other.totalSize)
            return false;
        return true;
    }

}
