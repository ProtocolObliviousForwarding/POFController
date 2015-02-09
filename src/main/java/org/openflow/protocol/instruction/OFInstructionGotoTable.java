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

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.openflow.protocol.OFGlobal;
import org.openflow.protocol.OFMatch20;
import org.openflow.util.HexString;

/**
 * Goto next table. <br>
 * If next table is Linear Table (or say Direct Table), use {@link OFInstructionGotoDirectTable}  instead.
 * 
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 */
public class OFInstructionGotoTable extends OFInstruction {
    public static final int MINIMUM_LENGTH = OFInstruction.MINIMUM_LENGTH + 8;
    public static final int MAXIMAL_LENGTH = OFInstruction.MINIMUM_LENGTH + 8 + OFMatch20.MINIMUM_LENGTH * OFGlobal.OFP_MAX_MATCH_FIELD_NUM;
    
    protected byte nextTableId;
    protected byte matchFieldNum;
    protected short packetOffset;           //byte
    protected List<OFMatch20> matchList;
    
    public OFInstructionGotoTable(){
        super.setType(OFInstructionType.GOTO_TABLE);
        super.setLength((short) MAXIMAL_LENGTH);
    }
    
    @Override
    public void readFrom(ChannelBuffer data){
        super.readFrom(data);
        this.nextTableId = data.readByte();
        this.matchFieldNum = data.readByte();
        this.packetOffset = data.readShort();
        data.readBytes(4);
        
        if(matchList == null){
            matchList = new ArrayList<OFMatch20>();
        }else{
            matchList.clear();
        }
        
        OFMatch20 match;
        for(int i = 0; i < OFGlobal.OFP_MAX_MATCH_FIELD_NUM; i++){
            match = new OFMatch20();
            match.readFrom(data);
            matchList.add(match);
        }
    }
    
    @Override
    public void writeTo(ChannelBuffer data){
        super.writeTo(data);
        data.writeByte(nextTableId);
        data.writeByte(matchFieldNum);
        data.writeShort(packetOffset);
        data.writeZero(4);
        
        if(matchList == null){
            data.writeZero(OFGlobal.OFP_MAX_MATCH_FIELD_NUM * OFMatch20.MINIMUM_LENGTH);
        }else{
            OFMatch20 match;
            
            if(matchFieldNum > matchList.size()){
                throw new RuntimeException("matchFieldNum " + matchFieldNum + " > matchList.size()" + matchList.size());
            }
            
            int i;
            for(i = 0; i < matchFieldNum && i < OFGlobal.OFP_MAX_MATCH_FIELD_NUM; i++){
                match = matchList.get(i);
                if(match == null){
                    data.writeZero(OFMatch20.MINIMUM_LENGTH);
                }else{
                    match.writeTo(data);
                }                
            }
            
            if(i < OFGlobal.OFP_MAX_MATCH_FIELD_NUM){
                data.writeZero( (OFGlobal.OFP_MAX_MATCH_FIELD_NUM - i) * OFMatch20.MINIMUM_LENGTH);
            }
        }
    }
    @Override
    public String toBytesString(){
		String string= super.toBytesString() + 
						HexString.toHex(nextTableId) + 
						HexString.toHex(matchFieldNum) + 
						HexString.toHex(packetOffset) + 
						" " + 
						HexString.ByteZeroEnd(4);

		if (this.matchList == null) {
			string += HexString.ByteZeroEnd(OFGlobal.OFP_MAX_MATCH_FIELD_NUM * OFMatch20.MINIMUM_LENGTH);
		} else {
			OFMatch20 match20;

			if (matchFieldNum > matchList.size()) {
				throw new RuntimeException("matchFieldNum " + matchFieldNum + " > matchList.size()" + matchList.size());
			}

			int i = 0;
			for (; i < matchFieldNum && i < OFGlobal.OFP_MAX_MATCH_FIELD_NUM; i++) {
				match20 = matchList.get(i);
				if (match20 == null) {
					string += HexString.ByteZeroEnd(OFMatch20.MINIMUM_LENGTH);
				} else {
					string += match20.toBytesString();
				}
			}
			if (i < OFGlobal.OFP_MAX_MATCH_FIELD_NUM) {
				string += HexString.ByteZeroEnd((OFGlobal.OFP_MAX_MATCH_FIELD_NUM - i) * OFMatch20.MINIMUM_LENGTH);
			}
		}
		;
		return string;
    }
    
    @Override
    public String toString(){
        String string = super.toString() +
			                ";ntid=" + nextTableId +
			                ";fn=" + matchFieldNum+
			                ";poff=" + packetOffset;
        if(this.matchList != null){
            string += ";match(" + matchList.size() + ")=";
            for(OFMatch20 match : matchList){
                string += match.toString() + ",";
            }
        }else{
            string += ";match=null";
        }
        
        return string;
    }
    
    public byte getNextTableId() {
        return nextTableId;
    }

    public void setNextTableId(byte nextTableId) {
        this.nextTableId = nextTableId;
    }

    public short getPacketOffset() {
        return packetOffset;
    }

    public void setPacketOffset(short packetOffset) {
        this.packetOffset = packetOffset;
    }

    public byte getMatchFieldNum() {
        return matchFieldNum;
    }

    public void setMatchFieldNum(byte matchFieldNum) {
        this.matchFieldNum = matchFieldNum;
    }

    public List<OFMatch20> getMatchList() {
        return matchList;
    }

    public void setMatchList(List<OFMatch20> matchList) {
        this.matchList = matchList;
    }

    @Override
    public int hashCode() {
        final int prime = 701;
        int result = super.hashCode();
        result = prime * result + matchFieldNum;
        result = prime * result + ((matchList == null) ? 0 : matchList.hashCode());
        result = prime * result + nextTableId;
        result = prime * result + packetOffset;
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
        
        OFInstructionGotoTable other = (OFInstructionGotoTable) obj;
        if (matchFieldNum != other.matchFieldNum)
            return false;
        if (matchList == null) {
            if (other.matchList != null)
                return false;
        } else if (!matchList.equals(other.matchList))
            return false;
        if (nextTableId != other.nextTableId)
            return false;
        if (packetOffset != other.packetOffset)
            return false;
        return true;
    }
    
    @Override
    public OFInstructionGotoTable clone() throws CloneNotSupportedException {
        OFInstructionGotoTable ins = (OFInstructionGotoTable) super.clone();
        if(null != matchList
                && 0 != matchList.size()
                && 0 != matchFieldNum){
            ins.matchList = new ArrayList<OFMatch20>();
            for(OFMatch20 matchField : this.matchList){
                ins.matchList.add(matchField.clone());
            }
        }

        return ins;
    } 
    
}
