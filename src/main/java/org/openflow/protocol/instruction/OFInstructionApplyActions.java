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
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.factory.OFActionFactory;
import org.openflow.protocol.factory.OFActionFactoryAware;
import org.openflow.util.HexString;

/**
 * Apply Actions.
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 */
public class OFInstructionApplyActions extends OFInstruction implements OFActionFactoryAware, Cloneable{
    public static final int MINIMUM_LENGTH = OFInstruction.MINIMUM_LENGTH + 8;
    public static final int MANIMAL_LENGTH = OFInstruction.MINIMUM_LENGTH + 8 + OFGlobal.OFP_MAX_ACTION_NUMBER_PER_INSTRUCTION * OFAction.MAXIMAL_LENGTH;
    
    protected byte actionNum;    
    protected List<OFAction> actionList;
    
    protected OFActionFactory actionFactory;
    
    public OFInstructionApplyActions(){
        super.setType(OFInstructionType.APPLY_ACTIONS);
        super.setLength((short)MANIMAL_LENGTH);
    }
    
    public void readFrom(ChannelBuffer data){
        super.readFrom(data);
        actionNum = data.readByte();
        data.readBytes(7);
        
        if(this.actionFactory == null){
            throw new RuntimeException("OFActionFactory not set");
        }
        this.actionList = this.actionFactory.parseActions(data, OFGlobal.OFP_MAX_ACTION_NUMBER_PER_INSTRUCTION * OFAction.MAXIMAL_LENGTH);
    }
    
    public void writeTo(ChannelBuffer data){
        super.writeTo(data);
        data.writeByte(actionNum);
        data.writeZero(7);
        
        if(actionList == null){
            data.writeZero(OFGlobal.OFP_MAX_ACTION_NUMBER_PER_INSTRUCTION * OFAction.MAXIMAL_LENGTH);
        }else{
            OFAction action;
            
            if(actionNum > actionList.size()){
                throw new RuntimeException("actionNum " + actionNum + " > actionList.size()" + actionList.size());
            }
            
            int i;
            for(i = 0; i < actionNum && i < OFGlobal.OFP_MAX_ACTION_NUMBER_PER_INSTRUCTION; i++){
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
            if(i < OFGlobal.OFP_MAX_ACTION_NUMBER_PER_INSTRUCTION){
                data.writeZero( (OFGlobal.OFP_MAX_ACTION_NUMBER_PER_INSTRUCTION - i) * OFAction.MAXIMAL_LENGTH);
            }
        }
    }
    
    @Override
    public String toBytesString(){
        String string =  super.toBytesString() +
                        HexString.toHex(actionNum) +
                        HexString.ByteZeroEnd(7);
        
        if(actionList == null){
            string += HexString.ByteZeroEnd(OFGlobal.OFP_MAX_ACTION_NUMBER_PER_INSTRUCTION * OFAction.MAXIMAL_LENGTH);
        }else{
            OFAction action;
            
            if(actionNum > actionList.size()){
                throw new RuntimeException("actionNum " + actionNum + " > actionList.size()" + actionList.size());
            }
            
            int i;
            for(i = 0; i < actionNum && i < OFGlobal.OFP_MAX_ACTION_NUMBER_PER_INSTRUCTION; i++){
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
            if(i < OFGlobal.OFP_MAX_ACTION_NUMBER_PER_INSTRUCTION){
                string += HexString.ByteZeroEnd( (OFGlobal.OFP_MAX_ACTION_NUMBER_PER_INSTRUCTION - i) * OFAction.MAXIMAL_LENGTH);
            }
        }
        
        return string;
    }
    
    @Override
    public String toString(){
        String string =  super.toString() +
                            ";an=" + actionNum ;
        
        OFAction action;
        for(int i = 0; i < actionList.size(); i++){
            action = actionList.get(i);
            string += ";ac[" + i + "]=" + action.toString();
        }
        
        return string;
    }
    
    public byte getActionNum() {
        return actionNum;
    }
    public void setActionNum(byte actionNum) {
        this.actionNum = actionNum;
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
        result = prime * result
                + ((actionList == null) ? 0 : actionList.hashCode());
        result = prime * result + actionNum;
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
        OFInstructionApplyActions other = (OFInstructionApplyActions) obj;
        if (actionList == null) {
            if (other.actionList != null)
                return false;
        } else if (!actionList.equals(other.actionList))
            return false;
        if (actionNum != other.actionNum)
            return false;
        return true;
    }

    @Override
    public void setActionFactory(OFActionFactory actionFactory) {
        this.actionFactory = actionFactory;        
    }
    
    @Override
    public OFInstructionApplyActions clone() throws CloneNotSupportedException {
        OFInstructionApplyActions ins = (OFInstructionApplyActions) super.clone();
        if(null != actionList
                && 0 != actionList.size()
                && 0 != actionNum){
            ins.actionList = new ArrayList<OFAction>();
            for(OFAction action : this.actionList){
                ins.actionList.add(action.clone());
            }
        }
        return ins;
    } 
}
