/**
*    Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior
*    University
* 
*    Licensed under the Apache License, Version 2.0 (the "License"); you may
*    not use this file except in compliance with the License. You may obtain
*    a copy of the License at
*
*         http://www.apache.org/licenses/LICENSE-2.0
*
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
*    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
*    License for the specific language governing permissions and limitations
*    under the License.
**/

/**
 * @author David Erickson (daviderickson@cs.stanford.edu) - Mar 11, 2010
 */
package org.openflow.protocol.action;


import org.jboss.netty.buffer.ChannelBuffer;
import org.openflow.util.HexString;

/**
 * Modified by Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 * Modified based on POF white paper.
 *      portID using int instead short
 *      add metadata offset and length in case adding the metadata in front of the packet
 *      add packet offset in case cut some fields what are not needed
 *      delete maxlength
 */

/**
 * @author David Erickson (daviderickson@cs.stanford.edu) - Mar 11, 2010
 * @author Rob Sherwood (rob.sherwood@stanford.edu)
 */
public class OFActionOutput extends OFAction{
    public static int MINIMUM_LENGTH = OFAction.MINIMUM_LENGTH + 16;

    protected int portId;
    protected short metadataOffset;     //bit
    protected short metadataLength;     //bit
    
    protected short packetOffset;       //byte
    
    //protected int maxLength;

    public OFActionOutput() {
        super.setType(OFActionType.OUTPUT);
        super.setLength((short) MINIMUM_LENGTH);
    }

    /**
     * Create an Output Action sending packets out the specified
     * OpenFlow port.
     *
     * This is the most common creation pattern for OFActions.
     *
     * @param portId
     */

    public OFActionOutput(int portId) {
        this(portId, 0xffffffff);
    }

    /**
     * Create an Output Action specifying both the port AND
     * the snaplen of the packet to send out that port.
     * The length field is only meaningful when port == OFPort.OFPP_CONTROLLER
     * @param port
     * @param maxLength The maximum number of bytes of the packet to send.
     * Most hardware only supports this value for OFPP_CONTROLLER
     */

    public OFActionOutput(int port, int maxLength) {
        super();
        super.setType(OFActionType.OUTPUT);
        super.setLength((short) MINIMUM_LENGTH);
        this.portId = port;
    }

    /**
     * Get the output port
     * @return portId
     */
    public int getPortId() {
        return this.portId;
    }

    /**
     * Set the output port
     * @param portId
     */
    public OFActionOutput setPortId(int portId) {
        this.portId = portId;
        return this;
    }

    public short getMetadataOffset() {
        return metadataOffset;
    }

    public void setMetadataOffset(short metadataOffset) {
        this.metadataOffset = metadataOffset;
    }

    public short getMetadataLength() {
        return metadataLength;
    }

    public void setMetadataLength(short metadataLength) {
        this.metadataLength = metadataLength;
    }

    public short getPacketOffset() {
        return packetOffset;
    }

    public void setPacketOffset(short packetOffset) {
        this.packetOffset = packetOffset;
    }

    @Override
    public void readFrom(ChannelBuffer data) {
        super.readFrom(data);
        this.portId = data.readInt();
        this.metadataOffset = data.readShort();
        this.metadataLength = data.readShort();
        
        this.packetOffset = data.readShort();
        data.readBytes(6);
        //this.maxLength = data.readShort();
    }

    @Override
    public void writeTo(ChannelBuffer data) {
        super.writeTo(data);
        data.writeInt(portId);
        data.writeShort(metadataOffset);
        data.writeShort(metadataLength);
        
        data.writeShort(packetOffset);
        data.writeZero(6);        
    }
    
    public String toBytesString(){
        return super.toBytesString() +
                HexString.toHex(portId) +
                HexString.toHex(metadataOffset) +
                HexString.toHex(metadataLength) +
                " " +
                HexString.toHex(packetOffset) +
                HexString.ByteZeroEnd(6);
    }
    
    public String toString(){
        return super.toString() +
                ";pid=" + portId +
                ";mos=" + metadataOffset +
                ";mlen=" + metadataLength +
                ";pos=" + packetOffset;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + metadataLength;
        result = prime * result + metadataOffset;
        result = prime * result + packetOffset;
        result = prime * result + portId;
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
        OFActionOutput other = (OFActionOutput) obj;
        if (metadataLength != other.metadataLength)
            return false;
        if (metadataOffset != other.metadataOffset)
            return false;
        if (packetOffset != other.packetOffset)
            return false;
        if (portId != other.portId)
            return false;
        return true;
    }    
}