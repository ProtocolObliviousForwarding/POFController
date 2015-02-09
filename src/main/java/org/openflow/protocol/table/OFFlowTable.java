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

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.openflow.protocol.OFGlobal;
import org.openflow.protocol.OFMatch20;
import org.openflow.protocol.table.OFTableMod.OFTableModCmd;
import org.openflow.util.HexString;
import org.openflow.util.ParseString;

/**
 * OFFlowTable describes the openflow table information <br>
 * <P>
 * OFFlowTable<br>
 * {<br>
 *      int8    command;<br>
 *      int8    tableId;<br>
 *      int8    tableType;<br>
 *      int8    matchFieldNum;<br>
 *      int32   tableSize;<br>
 *      <br>
 *      int16   keyLength;<br>
 *      int8    reserve[6];<br>
 *      <br>
 *      int8    tableName[{@link org.openflow.protocol.OFGlobal#OFP_NAME_MAX_LENGTH}];<br>
 *      <br>
 *      List<{@link OFMatch20}> matchFieldList; <br>
 *  } //sizeof() = 16 + {@link org.openflow.protocol.OFGlobal#OFP_NAME_MAX_LENGTH}<br>
 *  
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 */

public class OFFlowTable implements Cloneable{
    public static final int MINIMUM_LENGTH = OFGlobal.OFP_NAME_MAX_LENGTH + 16;
    public static final int MAXIMAL_LENGTH = OFGlobal.OFP_NAME_MAX_LENGTH + 16  + OFGlobal.OFP_MAX_MATCH_FIELD_NUM * OFMatch20.MINIMUM_LENGTH;
    
    protected OFTableModCmd command;
    protected byte tableId;
    protected OFTableType tableType;
    protected byte matchFieldNum;    
    protected int tableSize;
    
    protected short keyLength;    
    
    protected String tableName;
    protected List<OFMatch20> matchFieldList;
    
    public OFFlowTable(String tableName, byte tableId) {
        super();
        this.tableName = tableName;
        this.tableId = tableId;
        matchFieldList = new ArrayList<OFMatch20>();
    }

    public OFFlowTable() {
        super();
    }
    
    public void readFrom(ChannelBuffer data){
        command = OFTableModCmd.values()[data.readByte()];
        tableId = data.readByte();
        tableType = OFTableType.values()[ data.readByte() ]; 
        matchFieldNum = data.readByte();
        tableSize = data.readInt();        
        
        keyLength = data.readShort();
        data.readBytes(6);
        
        tableName = ParseString.NameByteToString(data);
        
        OFMatch20 matchField;
        for(int i = 0; i < OFGlobal.OFP_MAX_MATCH_FIELD_NUM; i++){
        	matchField = new OFMatch20();
        	matchField.readFrom(data);
        	matchFieldList.add(matchField);
        }
    }
    
    public void writeTo(ChannelBuffer data){
        data.writeByte(command.ordinal());
        data.writeByte(tableId);
        data.writeByte(tableType.getValue());
        data.writeByte(matchFieldNum);        
        data.writeInt(tableSize);
        
        data.writeShort(keyLength);
        data.writeZero(6);
        
        data.writeBytes( ParseString.NameStringToBytes(tableName) );
        
        if (this.matchFieldList == null){
            data.writeZero(OFGlobal.OFP_MAX_MATCH_FIELD_NUM * OFMatch20.MINIMUM_LENGTH);
        }else{
        	OFMatch20 matchField;
            
            if(matchFieldNum > matchFieldList.size()){
                throw new RuntimeException("matchFieldNum " + matchFieldNum + " > matchFieldList.size()" + matchFieldList.size());
            }
            
            int i = 0;
            for(; i < matchFieldNum && i < OFGlobal.OFP_MAX_MATCH_FIELD_NUM; i++){
            	matchField = matchFieldList.get(i);
                if(matchFieldList == null){
                    data.writeZero(OFMatch20.MINIMUM_LENGTH);
                }else{
                	matchField.writeTo(data);
                }
            }
            if(i < OFGlobal.OFP_MAX_MATCH_FIELD_NUM){
                data.writeZero( (OFGlobal.OFP_MAX_MATCH_FIELD_NUM - i) * OFMatch20.MINIMUM_LENGTH);
            }
        } 
    }
    
    public String toBytesString(){
        String string = HexString.toHex((byte)command.ordinal());
        string += HexString.toHex(tableId);
        string += HexString.toHex((byte)tableType.ordinal());
        string += HexString.toHex(matchFieldNum);
        string += " ";
        
        string += HexString.toHex(tableSize);
        
        string += HexString.toHex(keyLength);
        string += HexString.ByteZeroEnd(6);
        
        string += HexString.NameToHex(tableName);
        
        return string;
    }
    
    public String toString(){
    	String string = "cmd=" + command +
			                ";stid=" + tableId +
			                ";tt=" + tableType +
			                ";mn=" + matchFieldNum +
			                ";size=" + tableSize +
			                ";kl=" + keyLength +
			                ";tn=" + tableName;
        if(this.matchFieldList != null){
            string += ";match(" + matchFieldList.size() + ")=";
            for(OFMatch20 match : matchFieldList){
                string += match.toString() + ",";
            }
        }else{
            string += ";match=null";
        }
        
        return string;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((command == null) ? 0 : command.hashCode());
        result = prime * result + keyLength;
        result = prime * result
                + ((matchFieldList == null) ? 0 : matchFieldList.hashCode());
        result = prime * result + matchFieldNum;
        result = prime * result + tableId;
        result = prime * result
                + ((tableName == null) ? 0 : tableName.hashCode());
        result = prime * result + tableSize;
        result = prime * result
                + ((tableType == null) ? 0 : tableType.hashCode());
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
        OFFlowTable other = (OFFlowTable) obj;
        if (command != other.command)
            return false;
        if (keyLength != other.keyLength)
            return false;
        if (matchFieldList == null) {
            if (other.matchFieldList != null)
                return false;
        } else if (!matchFieldList.equals(other.matchFieldList))
            return false;
        if (matchFieldNum != other.matchFieldNum)
            return false;
        if (tableId != other.tableId)
            return false;
        if (tableName == null) {
            if (other.tableName != null)
                return false;
        } else if (!tableName.equals(other.tableName))
            return false;
        if (tableSize != other.tableSize)
            return false;
        if (tableType != other.tableType)
            return false;
        return true;
    }
    
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public byte getTableId() {
        return tableId;
    }

    public void setTableId(byte tableId) {
        this.tableId = tableId;
    }

    public OFTableType getTableType() {
        return tableType;
    }

    public void setTableType(OFTableType type) {
        this.tableType = type;
    }

    public short getKeyLength() {
        return keyLength;
    }

    public void setKeyLength(short keyLength) {
        this.keyLength = keyLength;
    }

    public int getTableSize() {
        return tableSize;
    }

    public void setTableSize(int tableSize) {
        this.tableSize = tableSize;
    }

    public byte getMatchFieldNum() {
        return matchFieldNum;
    }

    public void setMatchFieldNum(byte matchFieldNum) {
        this.matchFieldNum = matchFieldNum;
    }

    public List<OFMatch20> getMatchFieldList() {
        return matchFieldList;
    }

    public void setMatchFieldList(List<OFMatch20> matchFieldList) {
        this.matchFieldList = matchFieldList;
    }

    public OFTableModCmd getCommand() {
        return command;
    }

    public void setCommand(OFTableModCmd command) {
        this.command = command;
    }
    
    @Override
    public OFFlowTable clone() throws CloneNotSupportedException {
        OFFlowTable neoFlowTable= (OFFlowTable) super.clone();
        
        if(null != matchFieldList 
                && 0 != matchFieldList.size()
                && 0 != matchFieldNum){
            List<OFMatch20> neoMatchList = new ArrayList<OFMatch20>();
            for(OFMatch20 matchField: this.matchFieldList){
                neoMatchList.add((OFMatch20) matchField.clone());
            }
            neoFlowTable.setMatchFieldList(neoMatchList); 
        }

        return neoFlowTable;
    }
    
    
}
