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

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.factory.OFActionFactory;
import org.openflow.protocol.factory.OFActionFactoryAware;
import org.openflow.util.HexString;
import org.openflow.util.U16;

/**
 * Represents an ofp_group_mod message
 * 
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 *
 */
public class OFGroupMod extends OFMessage implements OFActionFactoryAware, Cloneable{
    public static final int MINIMUM_LENGTH = OFMessage.MINIMUM_LENGTH + 16;
    public static final int MAXIMAL_LENGTH = OFMessage.MINIMUM_LENGTH + 16 + OFGlobal.OFP_MAX_ACTION_NUMBER_PER_GROUP * OFAction.MAXIMAL_LENGTH;
    
    public enum OFGroupModCmd {
        OFPGC_ADD,
        OFPGC_MODIFY,
        OFPGC_DELETE
    }
    
    public enum OFGroupType{
        OFPGT_ALL,
        OFPGT_SELECT,
        OFPGT_INDIRECT,
        OFPGT_FF
    }
    
    protected byte command;
    protected byte groupType;
    protected byte actionNum;
    protected int groupId;
    
    protected int counterId;    
    
    protected List<OFAction> actionList;
    
    protected OFActionFactory actionFactory;
    
    public OFGroupMod(){
        super();
        this.type = OFType.GROUP_MOD;
        this.length = U16.t(MINIMUM_LENGTH);
    }
    
    @Override
    public int getLengthU() {
        return MAXIMAL_LENGTH;
    }
    
    @Override
    public void readFrom(ChannelBuffer data) {
        super.readFrom(data);
        
        command = data.readByte();
        groupType = data.readByte();
        actionNum = data.readByte();
        data.readByte();        
        groupId = data.readInt();
        
        counterId = data.readInt();
        data.readBytes(4);
        
        this.actionList = this.actionFactory.parseActions(data, OFGlobal.OFP_MAX_ACTION_NUMBER_PER_GROUP * OFAction.MAXIMAL_LENGTH);
    }

    @Override
    public void writeTo(ChannelBuffer data) {
        super.writeTo(data);
        data.writeByte(command);
        data.writeByte(groupType);
        data.writeByte(actionNum);
        data.writeByte(0);
        data.writeInt(groupId);
        
        data.writeInt(counterId);
        data.writeZero(4);
        
        if(actionList == null){
            data.writeZero(OFGlobal.OFP_MAX_ACTION_NUMBER_PER_GROUP * OFAction.MAXIMAL_LENGTH);
        }else{
            OFAction action;
            
            if(actionNum > actionList.size()){
                throw new RuntimeException("actionNum " + actionNum + " > actionList.size()" + actionList.size());
            }
            
            int i;
            for(i = 0; i < actionNum && i < OFGlobal.OFP_MAX_ACTION_NUMBER_PER_GROUP; i++){
                action = actionList.get(i);
                if(action == null){
                    data.writeZero(OFAction.MAXIMAL_LENGTH);
                }else{
                    action.writeTo(data);
                    if(action.getLength() < OFAction.MAXIMAL_LENGTH ){
                        data.writeZero(OFAction.MAXIMAL_LENGTH - action.getLength());
                    }
                }
            }
            if(i < OFGlobal.OFP_MAX_ACTION_NUMBER_PER_GROUP){
                data.writeZero( (OFGlobal.OFP_MAX_ACTION_NUMBER_PER_GROUP - i) * OFAction.MAXIMAL_LENGTH);
            }
        }
    }
    
    public String toBytesString(){
        String string = super.toString();
        string += HexString.toHex(command) +
                    HexString.toHex(groupType) +
                    HexString.toHex(actionNum) +
                    HexString.ByteZeroEnd(1) +
                    HexString.toHex(groupId) +
                    HexString.toHex(counterId) +
                    HexString.ByteZeroEnd(4);
        
        if(actionList == null){
            string += HexString.ByteZeroEnd(OFGlobal.OFP_MAX_ACTION_NUMBER_PER_GROUP * OFAction.MAXIMAL_LENGTH);
        }else{
            OFAction action;
            
            if(actionNum > actionList.size()){
                throw new RuntimeException("actionNum " + actionNum + " > actionList.size()" + actionList.size());
            }
            
            int i;
            for(i = 0; i < actionNum && i < OFGlobal.OFP_MAX_ACTION_NUMBER_PER_GROUP; i++){
                action = actionList.get(i);
                if(action == null){
                    string += HexString.ByteZeroEnd(OFAction.MAXIMAL_LENGTH);
                }else{
                    string += action.toBytesString();
                    if(action.getLength() < OFAction.MAXIMAL_LENGTH ){
                        string += HexString.ByteZeroEnd(OFAction.MAXIMAL_LENGTH - action.getLength());
                    }
                }
            }
            if(i < OFGlobal.OFP_MAX_ACTION_NUMBER_PER_GROUP){
                string += HexString.ByteZeroEnd( (OFGlobal.OFP_MAX_ACTION_NUMBER_PER_GROUP - i) * OFAction.MAXIMAL_LENGTH);
            }
        }
        
        return string;
    }
    
    public String toString(){
        String string = super.toString();
        string += "; GroupMod:" +
                "cmd=" + command +
                ";type=" + groupType +
                ";anum=" + actionNum +
                ";gid=" + groupId +
                ";cid=" + counterId;
        
        if(actionList == null){
            string += "actionList=null";
        }else{            
            for(OFAction action : actionList){                
                if(action != null){
                    string +=";act=" + action.toString();
                }
            }
        }
        
        return string;
    }




    public byte getCommand() {
        return command;
    }


    public void setCommand(byte command) {
        this.command = command;
    }
    
    public void setCommand(OFGroupModCmd command) {
        this.command = (byte) command.ordinal();
    }


    public byte getGroupType() {
        return groupType;
    }


    public void setGroupType(byte groupType) {
        this.groupType = groupType;
    }


    public byte getActionNum() {
        return actionNum;
    }


    public void setActionNum(byte actionNum) {
        this.actionNum = actionNum;
    }


    public int getGroupId() {
        return groupId;
    }


    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }


    public int getCounterId() {
        return counterId;
    }


    public void setCounterId(int counterId) {
        this.counterId = counterId;
    }


    public List<OFAction> getActionList() {
        return actionList;
    }


    public void setActionList(List<OFAction> actionList) {
        this.actionList = actionList;
    }


    @Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((actionFactory == null) ? 0 : actionFactory.hashCode());
		result = prime * result + ((actionList == null) ? 0 : actionList.hashCode());
		result = prime * result + actionNum;
		result = prime * result + command;
		result = prime * result + counterId;
		result = prime * result + groupId;
		result = prime * result + groupType;
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
		OFGroupMod other = (OFGroupMod) obj;
		if (actionFactory == null) {
			if (other.actionFactory != null)
				return false;
		} else if (!actionFactory.equals(other.actionFactory))
			return false;
		if (actionList == null) {
			if (other.actionList != null)
				return false;
		} else if (!actionList.equals(other.actionList))
			return false;
		if (actionNum != other.actionNum)
			return false;
		if (command != other.command)
			return false;
		if (counterId != other.counterId)
			return false;
		if (groupId != other.groupId)
			return false;
		if (groupType != other.groupType)
			return false;
		return true;
	}
    
    @Override
    public OFGroupMod clone() throws CloneNotSupportedException {
        OFGroupMod groupMod= (OFGroupMod) super.clone();
        
        if(null != actionList 
                && 0 != actionList.size()
                && 0 != actionNum){
            List<OFAction> neoActionList = new ArrayList<OFAction>();
            for(OFAction ofAction: this.actionList){
                neoActionList.add(ofAction.clone());
            }
            groupMod.setActionList(neoActionList);
        }

        return groupMod;
    }

    @Override
    public void setActionFactory(OFActionFactory actionFactory) {
        this.actionFactory = actionFactory;        
    }
    
    
}
