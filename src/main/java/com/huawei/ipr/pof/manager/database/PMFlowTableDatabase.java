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

package com.huawei.ipr.pof.manager.database;

import java.io.BufferedReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatchX;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionType;
import org.openflow.protocol.instruction.OFInstruction;
import org.openflow.protocol.instruction.OFInstructionApplyActions;
import org.openflow.protocol.instruction.OFInstructionType;
import org.openflow.protocol.table.OFTableType;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.huawei.ipr.pof.manager.IPMService;
import com.huawei.ipr.pof.manager.PofManager;

/**
 * SMFlowTableDatabase is a part of SMSwitchDatabase, it stores flow entries belonged a table.
 * 
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 *
 */
public class PMFlowTableDatabase {
    protected byte flowTableId;							//globalID
    protected Map<Integer, OFFlowMod> flowEntriesMap;	//<entryId, flowEntry>
    protected Integer flowEntryNo;
    protected List<Integer> freeFlowEntryIDList;
    
    protected Map<String, Integer> matchKeyMap;			//<keyString, entryId>
    
    public PMFlowTableDatabase(byte flowTableId){
        this.flowTableId = flowTableId;
        flowEntriesMap = new ConcurrentHashMap <Integer, OFFlowMod>();
        flowEntryNo = IPMService.FLOWENTRYID_START;
        freeFlowEntryIDList = Collections.synchronizedList(new ArrayList<Integer>());
        
        matchKeyMap = new ConcurrentHashMap<String, Integer>();
    }
    
    public OFFlowMod getFlowEntry(int index){
        return flowEntriesMap.get(index);
    }
    
    public void putFlowEntry(int index, OFFlowMod flowEntry){
        flowEntriesMap.put(index, flowEntry);
    }
    
    public void putMatchKey(String keyString, int entryId){
    	matchKeyMap.put(keyString, entryId);
    }
    
    public Integer getMatchKeyIndex(String keyString){
    	return matchKeyMap.get(keyString);
    }
    
    public void deleteMatchKey(String keyString){
    	matchKeyMap.remove(keyString);
    }

    public byte getFlowTableId() {
        return flowTableId;
    }
    
    public OFFlowMod deleteFlowEntry(int index){
        OFFlowMod flowEntry = flowEntriesMap.remove(index);
        freeFlowEntryIDList.add(index);
        return flowEntry;
    }

    public void setFlowTableId(byte flowTableId) {
        this.flowTableId = flowTableId;
    }

    public Map<Integer, OFFlowMod> getFlowEntriesMap() {
        return flowEntriesMap;
    }
    
    public int getNewFlowEntryID() {
        int newFlowEntryID;
        if (0 == freeFlowEntryIDList.size()) {
            newFlowEntryID = flowEntryNo;
            flowEntryNo++;
        } else {
            newFlowEntryID = freeFlowEntryIDList.remove(0);
        }
        return newFlowEntryID;
    }
    
	public boolean saveAllDataIntoFile(OutputStream out) {
        Gson gson = new Gson();
        String string;
        java.lang.reflect.Type type;

        try {
            // save tableid
            string = gson.toJson(flowTableId);
            out.write(string.getBytes());
            out.write('\n');
            
            //save freeFlowEntryIDList
            type = new TypeToken<List<Integer>>(){}.getType();
            string = gson.toJson(freeFlowEntryIDList, type);
            out.write(string.getBytes());
            out.write('\n');
            
            //save flowEntriesMap
            Iterator<Integer> flowEntryIdItor  = flowEntriesMap.keySet().iterator();
            int flowId;
            OFFlowMod flowEntry;
            String tableIdFlagString;
            while(flowEntryIdItor.hasNext()){
            	flowId = flowEntryIdItor.next();
            	flowEntry = flowEntriesMap.get(flowId);
            	
            	tableIdFlagString = "#flowid#" + flowId;
            	out.write(tableIdFlagString.getBytes());
            	out.write('\n');
            	
            	if(false == saveFlowEntryIntoFile(out, flowEntry)){
            		return false;
            	}            	
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        
        return true;
	}
	
	private boolean saveFlowEntryIntoFile(OutputStream out, OFFlowMod flowEntry) {
        Gson gson = new Gson();
        String string;
        java.lang.reflect.Type type;

        try {
            string = gson.toJson(flowEntry.getCommand(), byte.class);
            out.write(string.getBytes());
            out.write('\n');
            
            string = gson.toJson(flowEntry.getCounterId(), int.class);
            out.write(string.getBytes());
            out.write('\n');
            
            string = gson.toJson(flowEntry.getCookie(), long.class);
            out.write(string.getBytes());
            out.write('\n');
            
            string = gson.toJson(flowEntry.getCookieMask(), long.class);
            out.write(string.getBytes());
            out.write('\n');
            
            
            string = gson.toJson(flowEntry.getTableId(), byte.class);
            out.write(string.getBytes());
            out.write('\n');
            
            string = gson.toJson(flowEntry.getTableType().toString(), String.class);
            out.write(string.getBytes());
            out.write('\n');
            
            string = gson.toJson(flowEntry.getIdleTimeout(), short.class);
            out.write(string.getBytes());
            out.write('\n');
            
            string = gson.toJson(flowEntry.getHardTimeout(), short.class);
            out.write(string.getBytes());
            out.write('\n');
            
            string = gson.toJson(flowEntry.getPriority(), short.class);
            out.write(string.getBytes());
            out.write('\n');
            
            
            string = gson.toJson(flowEntry.getIndex(), int.class);
            out.write(string.getBytes());
            out.write('\n');
            
            //save matchList
            type = new TypeToken<List<OFMatchX>>(){}.getType();
            string = gson.toJson(flowEntry.getMatchList(), type);
            out.write(string.getBytes());
            out.write('\n');
            
            //save instructionList
            for(OFInstruction ins : flowEntry.getInstructionList()){
            	if(ins instanceof OFInstructionApplyActions){
            		OFInstructionApplyActions ofiaa = (OFInstructionApplyActions)ins;
            		List<OFAction> actionList = ofiaa.getActionList();
            		string = "#instruction#";
	                out.write(string.getBytes());
	                out.write('\n');
            		for(OFAction action : actionList){
    	            	//type = new TypeToken<OFAction>(){}.getType();
    	                string = "#action." + action.getType().toString() + "#" + gson.toJson(action, action.getType().toClass());
    	                out.write(string.getBytes());
    	                out.write('\n');
            		}
            	}else{
	            	//type = new TypeToken<OFInstruction>(){}.getType();
	                string = "#instruction." + ins.getType().toString() + "#" + gson.toJson(ins, ins.getType().toClass());
	                out.write(string.getBytes());
	                out.write('\n');
            	}
            }            

            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        
        return true;
	}
	
	public boolean loadAllDataFromFile(byte tableId, BufferedReader br, List<String> returnedCurLineString){ 
        Gson gson = new Gson();
        String lineString;
        java.lang.reflect.Type type;
        
        try {
            //read flowTableId
            lineString = br.readLine();
            flowTableId = gson.fromJson(lineString, byte.class);
            
            //read freeFlowEntryIDList
            lineString = br.readLine();
            type = new TypeToken<List<Integer>>(){}.getType();
            freeFlowEntryIDList = gson.fromJson(lineString, type);
            
            //read flowEntriesMap
            if(null == flowEntriesMap){
            	flowEntriesMap = new ConcurrentHashMap<Integer, OFFlowMod>();
            }else{
            	flowEntriesMap.clear();
            }
            int flowid;
            lineString = br.readLine();
            
            while( null != lineString && lineString.contains("#flowid#") ){
            	flowid = Integer.parseInt( lineString.substring("#flowid#".length()));
            	OFFlowMod flowEntry = new OFFlowMod();
            	flowEntry.setLengthU(OFFlowMod.MAXIMAL_LENGTH);
            	flowEntry.setIndex(flowid);
            	
            	flowEntriesMap.put(flowid, flowEntry);
            	if(false == loadFlowEntryFromFile(tableId, br, flowEntry, returnedCurLineString)){
            		return false;
            	}
            	
            	if(0 != flowEntry.getMatchFieldNum()){
                    String keyString = PofManager.calcMatchKey(flowEntry.getMatchList());
                    putMatchKey(keyString, flowEntry.getIndex());
            	}
            	
            	lineString = returnedCurLineString.get(0);
            	
            }
            flowEntryNo = flowEntriesMap.size();
            
        }catch (Exception e) {
            
            e.printStackTrace();
            return false;
        }

        returnedCurLineString.add(0, lineString);
        return true;
	}
	
	private boolean loadFlowEntryFromFile(byte tableId, BufferedReader br, OFFlowMod flowEntry, List<String> returnedCurLineString){ 
        Gson gson = new Gson();
        String lineString;
        java.lang.reflect.Type type;
        
        try {
            //read command
            lineString = br.readLine();
            flowEntry.setCommand( gson.fromJson(lineString, byte.class) );
            
            //read counterId
            lineString = br.readLine();
            flowEntry.setCounterId( gson.fromJson(lineString, int.class) );
            
            //read cook
            lineString = br.readLine();
            flowEntry.setCookie( gson.fromJson(lineString, long.class) );

            //read counterId
            lineString = br.readLine();
            flowEntry.setCookieMask( gson.fromJson(lineString, long.class) );

            //read tableId
            lineString = br.readLine();
            flowEntry.setTableId(gson.fromJson(lineString, byte.class));
   
            //read tableType
            lineString = br.readLine();
            flowEntry.setTableType( OFTableType.valueOf(gson.fromJson(lineString, String.class)) );

            //read idleTimeout
            lineString = br.readLine();
            flowEntry.setIdleTimeout( gson.fromJson(lineString, short.class) );
            
            //read idleTimeout
            lineString = br.readLine();
            flowEntry.setHardTimeout( gson.fromJson(lineString, short.class) );

            //read priority
            lineString = br.readLine();
            flowEntry.setPriority( gson.fromJson(lineString, short.class) );
            
            //read flowId/index
            lineString = br.readLine();
            flowEntry.setIndex( gson.fromJson(lineString, int.class) );
            
            //read matchList
            type = new TypeToken<List<OFMatchX>>(){}.getType();
            lineString = br.readLine();
            List<OFMatchX> matchList = gson.fromJson(lineString, type);
            flowEntry.setMatchList( matchList );
            
            if(null != matchList){
            	flowEntry.setMatchFieldNum( (byte)(matchList.size()) );
            }
            
            
            //read instructions and actions
            String typeString;
            String instructionString;
            String actionString;
            int pos;
            OFInstructionType instructionType;
            OFInstruction instruction;
            
            OFActionType actionType;
            OFAction action;
            
            lineString = br.readLine();
            
            List<OFInstruction> insList = flowEntry.getInstructionList();
    		if(null == insList){
    			insList = new ArrayList<OFInstruction>();
    			flowEntry.setInstructionList(insList);
    		}
    		
    		
            while(null != lineString && lineString.contains("#instruction") ){

            	if(lineString.contains("#instruction.")){//instruction
            		pos = lineString.indexOf("#", "#instruction.".length());
            		typeString = lineString.substring("#instruction.".length(), pos);
            		instructionString = lineString.substring(pos + 1);
            		
            		instructionType = OFInstructionType.valueOf(typeString);
            		instruction = gson.fromJson(instructionString, instructionType.toClass());
            		
            		insList.add(instruction);
            		
            		lineString = br.readLine();
            	}else if(lineString.contains("#instruction#")){//actions
            		lineString = br.readLine();
            		
            		instruction = new OFInstructionApplyActions();
            		insList.add(instruction);
            		
            		List<OFAction> actionList = new ArrayList<OFAction>();

            		while(null != lineString && lineString.contains("#action.")){
            			pos = lineString.indexOf("#", "#action.".length());
                		typeString = lineString.substring("#action.".length(), pos);
                		actionString = lineString.substring(pos + 1);
                		
                		actionType = OFActionType.valueOf(typeString);
                		action = gson.fromJson(actionString, actionType.toClass());
                		
                		actionList.add(action);
                		
                		lineString = br.readLine();
            		}
            		if(null == ((OFInstructionApplyActions)instruction).getActionList()){
            			((OFInstructionApplyActions)instruction).setActionList(actionList);
            		}
            		((OFInstructionApplyActions)instruction).setActionNum( (byte)(actionList.size()) );
            		
            	}else{
            		break;
            	}            	
            }
            
            if(null != insList){
            	flowEntry.setInstructionNum( (byte)(insList.size()) );
            }
            
        }catch (Exception e) {
            
            e.printStackTrace();
            return false;
        }
        returnedCurLineString.add(0, lineString);
        return true;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((flowEntriesMap == null) ? 0 : flowEntriesMap.hashCode());
        result = prime * result
                + ((flowEntryNo == null) ? 0 : flowEntryNo.hashCode());
        result = prime * result + flowTableId;
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
        PMFlowTableDatabase other = (PMFlowTableDatabase) obj;
        if (flowEntriesMap == null) {
            if (other.flowEntriesMap != null)
                return false;
        } else if (!flowEntriesMap.equals(other.flowEntriesMap))
            return false;
        if (flowEntryNo == null) {
            if (other.flowEntryNo != null)
                return false;
        } else if (!flowEntryNo.equals(other.flowEntryNo))
            return false;
        if (flowTableId != other.flowTableId)
            return false;
        return true;
    }
    
    
}
