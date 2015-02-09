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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;

import org.openflow.protocol.OFCounter;
import org.openflow.protocol.OFFeaturesReply;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFGroupMod;
import org.openflow.protocol.OFMatch20;
import org.openflow.protocol.OFMatchX;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFMeterMod;
import org.openflow.protocol.OFPortStatus;
import org.openflow.protocol.OFPortStatus.OFPortReason;
import org.openflow.protocol.OFProtocol;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.instruction.OFInstruction;
import org.openflow.protocol.table.OFFlowTable;
import org.openflow.protocol.table.OFFlowTableResource;
import org.openflow.protocol.table.OFTableResource;
import org.openflow.protocol.table.OFTableType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.huawei.ipr.pof.manager.IPMService;

/**
 * SMDatabase implements IFloodlightModule and ISMDatabaseService.
 * <p>
 * SMDatabase stores data and provides method interfaces to set and get.
 * <br>
 * SMDatabase stores:
 * <ul>
 *  <li>protocols</li>
 *  <li>fields</li>
 *  <li>packet types</li>
 *  <li>data for each switches, stored in {@link PMSwitchDatabase}</li>
 * </ul>
 * 
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 *
 */
public class PMDatabase implements IFloodlightModule, IPMDatabaseService{
	protected static Logger log = LoggerFactory.getLogger(PMDatabase.class);
	
    protected Map<Integer, PMSwitchDatabase> switchDatabaseMap;
    
    protected List<OFMatch20> metadataList;
    
    protected Map<String, Short> ofProtocolNameMap; //<name, protocolId>
    protected Map<Short, OFProtocol> ofProtocolMap; //<protocolId, protocol>
    protected short ofProtocolNo;
    
    protected Map<Short, OFMatch20> fieldDatabase; //<fieldId, field>
    protected short fieldIdNo;
    
    public List<Integer> iGetAllSwitchID(){
        List<Integer> switchIdList = Collections.synchronizedList(new ArrayList<Integer>());
        
        if(null != switchDatabaseMap){
	        Iterator<Integer> switchIdItor =  switchDatabaseMap.keySet().iterator();
	        
	        if(null != switchIdItor){
		        while(switchIdItor.hasNext()){
		            switchIdList.add(switchIdItor.next());
		        }
	        }
        }
	        
	    return switchIdList;
    }

    @Override
    public void iSetPortOpenFlowEnable(int switchId, int portId, byte onoff){
        OFPortStatus portStatus = iGetPort(switchId, portId);
        if(portStatus != null && portStatus.getDesc() != null){
        	portStatus.getDesc().setOpenflowEnable(onoff);
        }
    }
    @Override
    public OFPortStatus iGetPort(int switchId, int portId){
    	PMSwitchDatabase switchDB = getSwitchDatabase(switchId);
    	if(null != switchDB){
	        OFPortStatus port = switchDB.getPort(portId);
	        return port;
    	}else{
    		return null;
    	}
    }
    
	public class IntegerComparator<T> implements Comparator<Integer> {
		public int compare(Integer int1, Integer int2) {
			if(int1 == null || int2 == null){
				return 0;
			}
			return int1 - int2;
		}
	}
	
	IntegerComparator<Integer> intComp = new IntegerComparator<Integer>();

    
    @Override
    public List<Integer> iGetAllPortId(int switchId){
        List<Integer> portIdList = Collections.synchronizedList(new ArrayList<Integer>());
        
        PMSwitchDatabase switchDB = getSwitchDatabase(switchId);
        if(null != switchDB){
	        Map<Integer, OFPortStatus> portsMap= switchDB.getPortsMap();
	        if(null != portsMap){
		        Iterator<Integer> iter = portsMap.keySet().iterator();
		        
		        OFPortStatus portStatus;
		        int portId;
		        
		        if(null != iter){
			        while(iter.hasNext()){
			        	portId = iter.next();
			        	portStatus = portsMap.get(portId);
			        	if(portStatus.getReason() != OFPortReason.OFPPR_DELETE.ordinal()){
			        		portIdList.add(portId);
			        	}            
			        }
			        Collections.sort(portIdList, intComp);
		        }
	        }
        }
        
        return portIdList;
    }
    
    @Override
    public boolean iModifyMetadata(List<OFMatch20> metadataList){
    	if(metadataList == null){
    		return false;
    	}
    	
    	this.metadataList.clear();
    	
    	for(OFMatch20 field : metadataList){
    		this.metadataList.add(field);
    	}
    	
    	return true;
    }
    
    @Override
    public List<OFMatch20> iGetMetadata(){
    	return metadataList;
    }
    
    @Override
    public short iAddProtocol(String protocolName, List<OFMatch20> fieldList){
    	if(null == protocolName || protocolName.length() == 0
    			|| null == fieldList){
    		return IPMService.OFPROTOCOLID_INVALID;
    	}
    	
        short protocolId = ofProtocolNo;
        
        short totalLength = 0;
        
    	for(OFMatch20 field : fieldList){
    		totalLength += field.getLength();
    	}
        
        OFProtocol newProtocol = new OFProtocol();
        newProtocol.setProtocolName(protocolName);
        newProtocol.setProtocolId(protocolId);
        newProtocol.setTotalLength(totalLength);
        newProtocol.setFieldList(fieldList);
        
        ofProtocolNameMap.put(protocolName, protocolId);
        
        ofProtocolMap.put(protocolId, newProtocol);
        
        ofProtocolNo++;
        
        return protocolId;
    }
    
    @Override
    public boolean iModifyProtocol(OFProtocol protocol, List<OFMatch20> fieldList){
    	if(null == protocol || null == fieldList){
    		return false;
    	}
    	
    	short totalLength = 0;
    	
    	for(OFMatch20 field : fieldList){
    		totalLength += field.getLength();
    	}
        
    	protocol.setTotalLength(totalLength);
    	protocol.setFieldList(fieldList);
    	
    	return true;
    }
    
    @Override
    public void iDelProtocol(OFProtocol protocol){
    	if(protocol != null){
    		ofProtocolNameMap.remove(protocol.getProtocolName());
        
    		ofProtocolMap.remove(protocol.getProtocolId());
    	}
    }
    
    @Override
    public Map<Short, OFProtocol> iGetProtocolMap(){
        return ofProtocolMap;
    }
    
    @Override
    public Map<String,Short> iGetProtocolNameMap(){
        return ofProtocolNameMap;
    }

    @Override
    public short iNewField(String fieldName, short fieldLength, short fieldOffset){
        short fieldId = fieldIdNo;
        
        OFMatch20 matchFiled = new OFMatch20();
        matchFiled.setFieldName(fieldName);
        matchFiled.setFieldId(fieldId);
        matchFiled.setLength(fieldLength);
        matchFiled.setOffset(fieldOffset);
        
        fieldDatabase.put(fieldId, matchFiled);
        
        fieldIdNo++;
        
        return fieldId;
    }
    
    @Override
    public boolean iModifyField(short fieldId, String fieldName, short fieldLength, short fieldOffset){
    	OFMatch20 matchField = fieldDatabase.get(fieldId);
    	if(null != matchField){
    		matchField.setFieldName(fieldName);
	    	matchField.setLength(fieldLength);
	    	matchField.setOffset(fieldOffset);
	    	
	    	return true;
    	}
    	return false;
    }
    
    @Override
    public void iDelField(short fieldId){
        fieldDatabase.remove(fieldId);
    }
    
    @Override
    public OFMatch20 iGetMatchField(short fieldId){
    	try{
    		OFMatch20 field = fieldDatabase.get(fieldId);
    		if(field != null){
    			return fieldDatabase.get(fieldId).clone();
    		}
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}
    	return null;
    }
    
	public class FieldIDComparator implements Comparator<OFMatch20> {
		@Override
		public int compare(OFMatch20 field1, OFMatch20 field2) {
			if(field1 == null || field2 == null){
				return 0;
			}
			int id1 = field1.getFieldId();
			int id2 = field2.getFieldId();
			
			return id1 - id2;
		}
	}
	
	private Comparator<OFMatch20> fieldIDComp = new FieldIDComparator();
	
    @Override
    public List<OFMatch20> iGetAllField(){
        List<OFMatch20> matchFieldList = Collections.synchronizedList(new ArrayList<OFMatch20>());
        
        if(null != fieldDatabase){
	        matchFieldList.addAll(fieldDatabase.values());
	        
	        //return the sorted field list
	        Collections.sort(matchFieldList, fieldIDComp);
        }
        
        return matchFieldList;
    }
    
    
     @Override
    public byte parseToSmallTableId(int switchId, byte globalTableId){
        PMSwitchDatabase switchDB = this.getSwitchDatabase(switchId);
		if (null == switchDB) {
			return IPMService.FLOWTABLEID_INVALID;
		}
		
		OFFlowTable flowTable = switchDB.getFlowTable(globalTableId);
		if (flowTable != null) {
			return flowTable.getTableId();
		}

		for (byte tableType = (byte) (OFTableType.OF_MAX_TABLE_TYPE.getValue() - 1); tableType >= 0; tableType--) {
			byte flowTableNoBase = switchDB.getFlowTableNoBase(tableType);
			if(flowTableNoBase == IPMService.FLOWTABLEID_INVALID){
				return flowTableNoBase;
			}
			
			if (globalTableId >= flowTableNoBase) {
				return (byte) (globalTableId - flowTableNoBase);
			}
		}

		return IPMService.FLOWTABLEID_INVALID;
    }
    @Override
    public byte parseToGlobalTableId(int switchId, byte tableType, byte smallTableId){
        PMSwitchDatabase switchDB = this.getSwitchDatabase(switchId);
		if (null == switchDB) {
			return IPMService.FLOWTABLEID_INVALID;
		}
		
		byte flowTableNoBase = switchDB.getFlowTableNoBase(tableType);
		if(flowTableNoBase == IPMService.FLOWTABLEID_INVALID){
			return flowTableNoBase;
		}
		
        return (byte) (flowTableNoBase + smallTableId);
    }
    
    
    @Override
    public byte iAddFlowTable(int switchId, String tableName,
                                byte tableType, short keyLength, int tableSize,
                                byte fieldNum, List<OFMatch20> matchFieldList){
        
        byte globalFlowTableId = IPMService.FLOWTABLEID_INVALID;
        try{
        	PMSwitchDatabase switchDB = getSwitchDatabase(switchId);
        	if(switchDB == null){
        		return IPMService.FLOWTABLEID_INVALID;
        	}
        	
            globalFlowTableId = this.getSwitchDatabase(switchId)
                                            .getNewFlowTableID(tableType);
            
            if(tableName.equalsIgnoreCase(IPMService.FIRST_ENTRY_TABLE_NAME)
            		&& (tableType != OFTableType.OF_MM_TABLE.ordinal()
            				|| globalFlowTableId != 0) ){
            	return IPMService.FLOWTABLEID_INVALID;
            }
            
            if(globalFlowTableId == 0
            		&& (!tableName.equalsIgnoreCase(IPMService.FIRST_ENTRY_TABLE_NAME)
            			|| tableType != OFTableType.OF_MM_TABLE.ordinal()) ){
            	return IPMService.FLOWTABLEID_INVALID;
            }
            
            byte smallFlowTableId = parseToSmallTableId(switchId, globalFlowTableId);
            
            OFFlowTable newFlowTable = new OFFlowTable(tableName, smallFlowTableId);
            newFlowTable.setTableType(OFTableType.values() [ tableType ]);
            newFlowTable.setKeyLength(keyLength);
            newFlowTable.setMatchFieldNum(fieldNum);
            newFlowTable.setTableSize(tableSize);
            newFlowTable.setMatchFieldList(matchFieldList);
            
            switchDB.putFlowTable(globalFlowTableId, newFlowTable);
            
            switchDB.putNewFlowTableDatabse(globalFlowTableId);
        }catch (Exception e) {
            e.printStackTrace();
            
            if(-1 != globalFlowTableId){
                //TODO
            }
            
            globalFlowTableId = IPMService.FLOWTABLEID_INVALID;
        }
        
        return globalFlowTableId;
    }
    @Override
    public void iPutFlowTable(int switchId, byte flowTableId, OFFlowTable flowTable){
    	PMSwitchDatabase switchDB = this.getSwitchDatabase(switchId);
    	if(null != switchDB){
    		switchDB.putFlowTable(flowTableId, flowTable);
    	}
    } 
    @Override
    public OFFlowTable iGetFlowTable(int switchId, byte flowTableId){
    	PMSwitchDatabase switchDB = this.getSwitchDatabase(switchId);
    	if(null != switchDB){
    		return switchDB.getFlowTable(flowTableId);
    	}
    	return null;
    }
    @Override
    public Map<Byte, OFFlowTable> iGetFlowTableMap(int switchId){ 
    	PMSwitchDatabase switchDB = this.getSwitchDatabase(switchId);
    	if(null != switchDB){
    		return switchDB.getFlowTablesMap();
    	}
    	return null;
    }
    @Override
    public void iDelFlowTable(int switchId, byte tableType, byte globalTableId) {
    	PMSwitchDatabase switchDB = this.getSwitchDatabase(switchId);
    	if(null != switchDB){
    		switchDB.removeFlowTable(globalTableId); 
	        
    		switchDB.removeFlowTableDatabse(globalTableId);
	        
    		switchDB.addFreeFlowTableID(tableType, globalTableId);
    	}
    }
    @Override
    public byte iGetFlowTableNumberingBase(int switchId, byte tableType) {
    	PMSwitchDatabase switchDB = this.getSwitchDatabase(switchId);
    	if(null != switchDB){
    		return switchDB.flowTableNoBaseMap
                        .get(OFTableType.values()[tableType]);
    	}
    	
    	return IPMService.FLOWTABLEID_INVALID;
    }

    @Override
    public int iAddFlowEntry(int switchId, byte globalTableId,
                            byte matchFieldNum, List<OFMatchX> matchList,
                            byte instructionNum, List<OFInstruction> instructionList,
                            short priority){
    	int flowEntryId = IPMService.FLOWENTRYID_INVALID;
    	try{
	    	PMSwitchDatabase switchDB = this.getSwitchDatabase(switchId);
	    	if(null == switchDB){
	    		return IPMService.FLOWENTRYID_INVALID;
	    	}
	    	
	        flowEntryId = switchDB.getFlowTableDatabase(globalTableId)
	                                .getNewFlowEntryID();
	        
	        OFTableType tableType = switchDB.getFlowTable(globalTableId)
	                                    .getTableType();
	        
	        byte smallFlowTableId = parseToSmallTableId(switchId, globalTableId);
	        
	        OFFlowMod newFlowEntry = new OFFlowMod();
	        newFlowEntry.setTableId(smallFlowTableId);
	        newFlowEntry.setTableType(tableType);
	        newFlowEntry.setIndex(flowEntryId);
	        
	        newFlowEntry.setMatchFieldNum(matchFieldNum);
	        newFlowEntry.setMatchList(matchList);
	        newFlowEntry.setInstructionNum(instructionNum);
	        newFlowEntry.setInstructionList(instructionList);
	        newFlowEntry.setPriority(priority);
	        
	        int newCounterID = switchDB.allocCounterId();
	        newFlowEntry.setCounterId(newCounterID);
	        
	        newFlowEntry.setLengthU(OFFlowMod.MAXIMAL_LENGTH);
	        
	        switchDB.getFlowTableDatabase(globalTableId)
	            	.putFlowEntry(flowEntryId, newFlowEntry);
    	}catch(Exception e){
    		e.printStackTrace();
    		flowEntryId = IPMService.FLOWENTRYID_INVALID;
    	}
        
        return flowEntryId;
    }
    @Override
    public Map<Integer, OFFlowMod> iGetFlowEntriesMap(int switchId, byte globalTableId){
    	PMSwitchDatabase switchDB = this.getSwitchDatabase(switchId);
    	if(null == switchDB){
    		return null;
    	}

        return switchDB.getFlowEntriesMap(globalTableId);
    }
    @Override
    public OFFlowMod iGetFlowEntry(int switchId, byte globalTableId, int flowEntryId){
    	PMSwitchDatabase switchDB = this.getSwitchDatabase(switchId);
    	if(null == switchDB){
    		return null;
    	}
    	
    	PMFlowTableDatabase  tableDB = switchDB.getFlowTableDatabase(globalTableId);
    	
    	if(null == tableDB){
    		return null;
    	}
    	
        return tableDB.getFlowEntry(flowEntryId);  
    }
    @Override
    public boolean iModFlowEntry(int switchId, byte globalTableId, int flowEntryId,
                                byte matchFieldNum, List<OFMatchX> matchList,
                                byte instructionNum, List<OFInstruction> instructionList,
                                short priority){
    	try{
	        OFFlowMod flowEntryFlowMod = this.getSwitchDatabase(switchId)
	                                        .getFlowTableDatabase(globalTableId)
	                                        .getFlowEntry(flowEntryId);
	        
	        if(flowEntryFlowMod == null){
	        	return false;
	        }
	        
	        flowEntryFlowMod.setMatchFieldNum(matchFieldNum);
	        flowEntryFlowMod.setMatchList(matchList);
	        flowEntryFlowMod.setInstructionNum(instructionNum);
	        flowEntryFlowMod.setInstructionList(instructionList);
	        flowEntryFlowMod.setPriority(priority);
	        
	        return true;
    	}catch(Exception e){
    		e.printStackTrace();
    		return false;
    	}
    }
    
    @Override
    public OFFlowMod iDelFlowEntry(int switchId, byte globalTableId, int index){
    	try{
	        OFFlowMod flowEntry =  getSwitchDatabase(switchId)
	                                    .getFlowTableDatabase(globalTableId)
	                                    .deleteFlowEntry(index);
	        
	        if(flowEntry.getCounterId() != IPMService.COUNTERID_INVALID){
	        	this.iFreeCounter(switchId, flowEntry.getCounterId());
	        }
	        return flowEntry;
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}
    }
    
    @Override
    public void putMatchKey(int switchId, byte globalTableId, String keyString, int entryId){
    	try{
    		getSwitchDatabase(switchId).getFlowTableDatabase(globalTableId).putMatchKey(keyString, entryId);
    	}catch(Exception e){
    		e.printStackTrace();
    		return ;
    	}
    }
    
    @Override
    public Integer getFlowEntryIndexByMatchKey(int switchId, byte globalTableId, String keyString){
    	try{
    		return getSwitchDatabase(switchId).getFlowTableDatabase(globalTableId).getMatchKeyIndex(keyString);
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}
    }
    
    @Override
    public void deleteMatchKey(int switchId, byte globalTableId, String keyString){
    	try{
    		getSwitchDatabase(switchId).getFlowTableDatabase(globalTableId).deleteMatchKey(keyString);
    	}catch(Exception e){
    		e.printStackTrace();
    		return ;
    	}
    }

    
    @Override
    public int iAllocateCounter(int switchId){
    	try{
	        return this.getSwitchDatabase(switchId)
	                    .allocCounterId();
    	}catch(Exception e){
    		e.printStackTrace();
    		return IPMService.COUNTERID_INVALID;
	    }
    }
    @Override
    public OFCounter iFreeCounter(int switchId, int counterId){
    	try{
	        return getSwitchDatabase(switchId)
	                .removeCounter(counterId);
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
	    }
    }
    @Override
    public OFCounter iGetCounter(int switchId, int counterId){
    	try{
    		return this.getSwitchDatabase(switchId)
    					.getCounter(counterId);
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
	    }
                        
    }
    @Override
    public boolean iResetCounter(int switchId, int counterId) {
        OFCounter counter = iGetCounter(switchId, counterId);
        if(counter == null){
        	return false;
        }
        counter.setValue(0);
        return true;
    }
    
    @Override
    public int iAddMeterEntry(int switchId, short rate){
    	try{
	        int meterId = this.getSwitchDatabase(switchId)
	                        .allocMeterId();
	        OFMeterMod newMeter = new OFMeterMod();
	        newMeter.setMeterId(meterId);
	        newMeter.setRate(rate);
	        
	        this.getSwitchDatabase(switchId)
	            .putMeter(meterId, newMeter);
	        
	        return meterId;
    	}catch(Exception e){
    		e.printStackTrace();
    		return IPMService.METER_INVALID;
	    }
    }
    @Override
    public OFMeterMod iFreeMeter(int switchId, int meterId){
    	try{
	        return getSwitchDatabase(switchId)
	                .removeMeter(meterId);
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
	    }
    }
    @Override
    public OFMeterMod iGetMeter(int switchId, int meterId){
    	try{
	        return this.getSwitchDatabase(switchId)
	                                .getMeter(meterId);
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
	    }
    }
    @Override
    public boolean iModifyMeter(int switchId, int meterId, short rate) {
    	try{
	        OFMeterMod meter = iGetMeter(switchId, meterId);
	        if(meter == null){
	        	return false;
	        }
	        meter.setRate(rate);
	        return true;
    	}catch(Exception e){
    		e.printStackTrace();
    		return false;
	    }
    }
    @Override
    public List<OFMeterMod> iGetAllMeters(int switchId){
    	try{
    		return this.getSwitchDatabase(switchId).getAllMeterList();
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
	    }
    }
    @Override
    public int iAddGroupEntry(int switchId, byte groupType, byte actionNum, List<OFAction> actionList){
    	try{
	        int groupId = this.getSwitchDatabase(switchId)
	                            .allocGroupId();
	        OFGroupMod newGroup = new OFGroupMod();
	        newGroup.setGroupId(groupId);
	        newGroup.setGroupType(groupType);
	        newGroup.setActionNum(actionNum);
	        newGroup.setActionList(actionList);
	        
	        int newCounterID = this.getSwitchDatabase(switchId).allocCounterId();
	        newGroup.setCounterId(newCounterID);
	        
	        newGroup.setLengthU(OFGroupMod.MAXIMAL_LENGTH);
	        
	        this.getSwitchDatabase(switchId)
	            .putGroup(groupId, newGroup);
	        
	        return groupId;
    	}catch(Exception e){
    		e.printStackTrace();
    		return IPMService.GROUPID_INVALID;
	    }
    }
    @Override
    public OFGroupMod iFreeGroupEntry(int switchId, int groupId){
    	try{
	        OFGroupMod groupMod = getSwitchDatabase(switchId)
	                                        .removeGroup(groupId);
	        
	        if(groupMod.getCounterId() != IPMService.COUNTERID_INVALID){
	        	this.iFreeCounter(switchId, groupMod.getCounterId());
	        }
	        return groupMod;
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
	    }
    }
    @Override
    public OFGroupMod iGetGroupEntry(int switchId, int groupId){
    	try{
	        return this.getSwitchDatabase(switchId)
	                   .getGroup(groupId);
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
	    }
    }
    @Override
    public boolean iModifyGroupEntry(int switchId, int groupId, byte groupType, byte actionNum, List<OFAction> actionList) {
    	try{
	        OFGroupMod group = iGetGroupEntry(switchId, groupId);
	        if(group == null){
	        	return false;
	        }
	        group.setGroupType(groupType);
	        group.setActionNum(actionNum);
	        group.setActionList(actionList); 
	        return true;
    	}catch(Exception e){
    		e.printStackTrace();
    		return false;
	    }
    }
    @Override
    public List<OFGroupMod> iGetAllGroups(int switchId){
    	try{
    		return this.getSwitchDatabase(switchId).getAllGroupList();
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
	    }
    }
    
    
    @Override
    public void iSetFeatures(int switchId, OFFeaturesReply featureReply){
    	try{
    		this.getSwitchDatabase(switchId)
    			.setSwitchFeatures(featureReply);
    	}catch(Exception e){
    		e.printStackTrace();
    		return ;
	    }
            
    }
    
	@Override
	public OFFeaturesReply iGetFeatures(int switchId) {
		try{
			return this.getSwitchDatabase(switchId).getSwitchFeatures();
		}catch(Exception e){
			e.printStackTrace();
    		return null;
	    }
	}


    @Override
    public void iSetPortStatus(int switchId, OFPortStatus portStatus){
    	try{
	        int portId = portStatus.getDesc().getPortId();
	        this.getSwitchDatabase(switchId)
	            .putPort(portId, portStatus);
    	}catch(Exception e){
    		e.printStackTrace();
    		return ;
	    }
    }
    
	@Override
	public OFPortStatus iGetPortStatus(int switchId, int portId) {
		try{
			return this.getSwitchDatabase(switchId).getPort(portId);
		}catch(Exception e){
			e.printStackTrace();
    		return null;
	    }
	}

    @Override
    public void iSetResourceReport(int switchId, OFFlowTableResource resourceReport){
    	try{
	        switch(resourceReport.getResourceType()){
	            case OFRRT_FLOW_TABLE:
	                this.getSwitchDatabase(switchId)
	                    .setFlowTableResource(resourceReport);
	                SetFlowTableNoBase(switchId, resourceReport.getTableResourcesMap());
	                break;
	            default:
	                break;
	        }
    	}catch(Exception e){
    		e.printStackTrace();
    		return ;
	    }
    }
    
    @Override
    public OFFlowTableResource iGetResourceReport(int switchId){
    	try{
	        return this.getSwitchDatabase(switchId)
	                        .getFlowTableResource();
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
	    }
    }
    
    private void SetFlowTableNoBase(int switchId, Map<OFTableType, OFTableResource> flowTableReourceMap){
        byte base = 0;
        OFTableResource tableResource;
        
        try{
        	if(flowTableReourceMap == null){
        		return;
        	}
        	
	        for(byte tableType = 0; tableType < OFTableType.OF_MAX_TABLE_TYPE.getValue(); tableType++){
	            tableResource = flowTableReourceMap.get(OFTableType.values()[ tableType ]);
	
	            this.getSwitchDatabase(switchId).setFlowTableNo(tableType, base);
	            this.getSwitchDatabase(switchId).setFlowTableNoBase(tableType, base);
	            base += tableResource.getTableNum();
	        }
        }catch(Exception e){
        	e.printStackTrace();
    		return ;
	    }
    }
    @Override
    public void iLogOFMessageIn(int switchId, OFMessage message){
    	try{
    		getSwitchDatabase(switchId).iLogOFMessageIn(message);
    	}catch(Exception e){
    		e.printStackTrace();
    		return ;
    	}
    }
    @Override
    public void iLogOFMessageIn(int switchId, OFType type, OFMessage message){
    	try{
    		getSwitchDatabase(switchId).iLogOFMessageIn(type, message);
    	}catch(Exception e){
    		e.printStackTrace();
    		return ;
    	}
    }
    @Override
    public void iLogOFMessageOut(int switchId, OFMessage message){
    	try{
    		getSwitchDatabase(switchId).iLogOFMessageOut(message);
    	}catch(Exception e){
    		e.printStackTrace();
    		return ;
    	}
    }
    @Override
    public void iLogOFMessageOut(int switchId, OFType type, OFMessage message){
    	try{
    		getSwitchDatabase(switchId).iLogOFMessageOut(type, message);
    	}catch(Exception e){
    		e.printStackTrace();
    		return ;
    	}
    }
    @Override
    public List<OFMessage> iGetLogOFMessageInAll(int switchId){
    	try{
    		return new ArrayList<OFMessage>(getSwitchDatabase(switchId).iGetLogOFMessageInAll());
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}
    }
    @Override
    public List<OFMessage> iGetLogOFMessageOutAll(int switchId){
    	try{
    		return new ArrayList<OFMessage>(getSwitchDatabase(switchId).iGetLogOFMessageOutAll());
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}
    }
    @Override
    public List<OFMessage> iGetLogOFMessageIn(int switchId, OFType type){
    	try{
    		return new ArrayList<OFMessage>(getSwitchDatabase(switchId).iGetLogOFMessageIn(type));
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}
    }
    @Override
    public List<OFMessage> iGetLogOFMessageOut(int switchId, OFType type){
    	try{
    		return new ArrayList<OFMessage>(getSwitchDatabase(switchId).iGetLogOFMessageOut(type));
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}
    }

    @Override
    public int iGetTableNumber(int switchId, OFTableType tableType){
    	try{
    		return this.getSwitchDatabase(switchId).getTableNumber(tableType);
    	}catch(Exception e){
    		e.printStackTrace();
    		return 0;
    	}
    }
    @Override
    public int iGetAllTableNumber(int switchId){
    	try{
    		return this.getSwitchDatabase(switchId).getAllTableNumber();
    	}catch(Exception e){
    		e.printStackTrace();
    		return 0;
    	}
    }
    @Override
    public int iGetUsedCounterNumber(int switchId){
    	try{
    		return this.getSwitchDatabase(switchId).getUsedCounterNumber();
    	}catch(Exception e){
    		e.printStackTrace();
    		return 0;
    	}
    }
    @Override
    public int iGetUsedGroupNumber(int switchId){
    	try{
    		return this.getSwitchDatabase(switchId).getUsedGroupNumber();
    	}catch(Exception e){
    		e.printStackTrace();
    		return 0;
    	}
    }
    @Override
    public int iGetUsedMeterNumber(int switchId){
    	try{
    		return this.getSwitchDatabase(switchId).getUsedMeterNumber();
    	}catch(Exception e){
    		e.printStackTrace();
    		return 0;
    	}
    }
    
    @Override
    public void iAddSendedOFMessage(int switchId, OFMessage message){
    	try{
    		this.getSwitchDatabase(switchId).iAddSendedOFMessage(message);
    	}catch(Exception e){
    		e.printStackTrace();
    		return ;
    	}
    }
    @Override
    public Queue<OFMessage> iGetSendedOFMessageQueue(int switchId){
    	try{
    		return this.getSwitchDatabase(switchId).iGetSendedOFMessageQueue();
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}
    }
    @Override
    public OFMessage iGetSendedOFMessage(int switchId, int xid){
    	try{
    		return this.getSwitchDatabase(switchId).iGetSendedOFMessage(xid);
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}
    }
    
    @Override
    public void iDelSendedOFMessage(int switchId, OFMessage message){
    	try{
    		this.getSwitchDatabase(switchId).iDelSendedOFMessage(message);
    	}catch(Exception e){
    		e.printStackTrace();
    		return ;
    	}
    }
    
    @Override
    public void iAddOldBackupOFMessage(int switchId, int sended_msg_xid, OFMessage message){
    	try{
    		this.getSwitchDatabase(switchId).iAddOldBackupMessage(sended_msg_xid, message);
    	}catch(Exception e){
    		e.printStackTrace();
    		return ;
    	}
    }
    
    @Override
    public OFMessage iGetOldBackupOFMessage(int switchId, int sended_msg_xid){
    	try{
    		return this.getSwitchDatabase(switchId).iGetOldBackupMessage(sended_msg_xid);
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}
    }    
    @Override
    public void iDelOldBackupOFMessage(int switchId, int sended_msg_xid){
    	try{
    		this.getSwitchDatabase(switchId).iDelOldBackupMessage(sended_msg_xid);
    	}catch(Exception e){
    		e.printStackTrace();
    		return ;
    	}
    }
    
    @Override
    public boolean addSwitchDatabase(int switchId){
        if(null != switchDatabaseMap.get(switchId)){
           log.error("switch [Id= " + switchId + "] already exists.");
           return false;
        }
        
        switchDatabaseMap.put(switchId, new PMSwitchDatabase(switchId));
        return true;
    }
    @Override
    public PMSwitchDatabase getSwitchDatabase(int switchId){
        return switchDatabaseMap.get(switchId);
    }
    @Override
    public void removeSwitchDatabase(int switchId){
        if(null != switchDatabaseMap.get(switchId)){
            //TODO consider: clear all database while remove the switch? how about reconnect  again?
            switchDatabaseMap.remove(switchId);
        }
    }
    
    @Override
    public Map<Integer, PMSwitchDatabase> getSwitchDatabaseMap() {
        return switchDatabaseMap;
    }
    
    public void setSwitchDatabaseMap(Map<Integer, PMSwitchDatabase> switchDatabase) {
        this.switchDatabaseMap = switchDatabase;
    }
    

  
    @Override
    public boolean saveAllDataIntoFile(OutputStream out){
        Gson gson = new Gson();
        String string;
        java.lang.reflect.Type type;

        try {
            // save protocol
            type = new TypeToken<Map<String, Short>>(){}.getType();
            string = gson.toJson(ofProtocolNameMap, type);
            out.write(string.getBytes());
            out.write('\n');

            type = new TypeToken<Map<Short, OFProtocol>>(){}.getType();
            string = gson.toJson(ofProtocolMap, type);
            out.write(string.getBytes());
            out.write('\n');
            
            //save files
            type = new TypeToken<Map<Short, OFMatch20>>(){}.getType();
            string = gson.toJson(fieldDatabase, type);
            out.write(string.getBytes());
            out.write('\n');

            
            //TODO toJson issue, why gson can not be smarter?
            //save all database of each switch
//            type = new TypeToken<HashMap<Integer, SMSwitchDatabase>>(){}.getType();
//            string = gson.toJson(switchDatabaseMap, type);
//            out.write(string.getBytes());
//            out.write('\n');
            Iterator<Integer> switchDatabaseItor  = switchDatabaseMap.keySet().iterator();
            int switchId;
            PMSwitchDatabase switchDatabase;
            String switchIdFlagString;
            while(switchDatabaseItor.hasNext()){
            	switchId = switchDatabaseItor.next();
            	switchDatabase = switchDatabaseMap.get(switchId);
            	
            	switchIdFlagString = "#switchid#" + Integer.toHexString(switchId);
            	out.write(switchIdFlagString.getBytes());
            	out.write('\n');
            	
            	if(false == switchDatabase.saveAllDataIntoFile(out)){
            		return false;
            	}            	
            }
         

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        
        return true;
    }
    @Override
    public boolean loadAllDataFromFile(BufferedReader br){        
        Gson gson = new Gson();
        String lineString;
        java.lang.reflect.Type type;
        
        try {
            ofProtocolNameMap.clear();
            ofProtocolMap.clear();
            
            //read protocol
            lineString = br.readLine();
            type = new TypeToken<Map<String, Short>>(){}.getType();
            ofProtocolNameMap = gson.fromJson(lineString, type);
            
            lineString = br.readLine();
            type = new TypeToken<Map<Short, OFProtocol>>(){}.getType();
            ofProtocolMap = gson.fromJson(lineString, type);
            
            ofProtocolNo = (short) (ofProtocolMap.size() + IPMService.OFPROTOCOLID_START);
            
            //read fields
            lineString = br.readLine();
            type = new TypeToken<Map<Short, OFMatch20>>(){}.getType();
            fieldDatabase = gson.fromJson(lineString, type);
            
            fieldIdNo = (short)((fieldDatabase == null) ? 0 : fieldDatabase.size() + IPMService.FIELDID_START);
            
            int switchId;            
            PMSwitchDatabase newSwitchDatabase;
            List<String> returnedCurLineString = new ArrayList<String>(); 
            
            lineString = br.readLine();
            while( null != lineString && lineString.contains("#switchid#") ){
            	switchId = (int)Long.parseLong( lineString.substring("#switchid#".length()), 16);
            	newSwitchDatabase = switchDatabaseMap.get(switchId);
            	boolean switchConnected = true;
            	if(null == newSwitchDatabase){
            		switchConnected = false;
            		newSwitchDatabase = new PMSwitchDatabase(switchId);
            		switchDatabaseMap.put(switchId, newSwitchDatabase);
            	}
            	
            	if(false == newSwitchDatabase.loadAllDataFromFile(br, returnedCurLineString, switchConnected)){
            		return false;
            	}
            	lineString = returnedCurLineString.get(0);
            }
            
        }catch (Exception e) {
            ofProtocolNameMap.clear();
            ofProtocolMap.clear();
            switchDatabaseMap.clear();
            
            e.printStackTrace();
            return false;
        }

        return true;
    }    
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fieldDatabase == null) ? 0 : fieldDatabase.hashCode());
		result = prime * result + fieldIdNo;
		result = prime * result + ((ofProtocolMap == null) ? 0 : ofProtocolMap.hashCode());
		result = prime * result + ((ofProtocolNameMap == null) ? 0 : ofProtocolNameMap.hashCode());
		result = prime * result + ofProtocolNo;
		result = prime * result + ((switchDatabaseMap == null) ? 0 : switchDatabaseMap.hashCode());
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
		PMDatabase other = (PMDatabase) obj;
		if (fieldDatabase == null) {
			if (other.fieldDatabase != null)
				return false;
		} else if (!fieldDatabase.equals(other.fieldDatabase))
			return false;
		if (fieldIdNo != other.fieldIdNo)
			return false;
		if (ofProtocolMap == null) {
			if (other.ofProtocolMap != null)
				return false;
		} else if (!ofProtocolMap.equals(other.ofProtocolMap))
			return false;
		if (ofProtocolNameMap == null) {
			if (other.ofProtocolNameMap != null)
				return false;
		} else if (!ofProtocolNameMap.equals(other.ofProtocolNameMap))
			return false;
		if (ofProtocolNo != other.ofProtocolNo)
			return false;
		if (switchDatabaseMap == null) {
			if (other.switchDatabaseMap != null)
				return false;
		} else if (!switchDatabaseMap.equals(other.switchDatabaseMap))
			return false;
		return true;
	}

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> l = 
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IPMDatabaseService.class);
        return l;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>,
        IFloodlightService> m = 
            new HashMap<Class<? extends IFloodlightService>,
                        IFloodlightService>();
        // We are the class that implements the service
        m.put(IPMDatabaseService.class, this);
        
        return m;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        return null;
    }

    @Override
    public void init(FloodlightModuleContext context)
            throws FloodlightModuleException {
                
    }

    @Override
    public void startUp(FloodlightModuleContext context) {
        switchDatabaseMap = new ConcurrentHashMap<Integer, PMSwitchDatabase>();
        
        metadataList = Collections.synchronizedList(new ArrayList<OFMatch20>());
        
        ofProtocolNameMap = new ConcurrentHashMap<String, Short>();
        ofProtocolMap = new ConcurrentHashMap<Short, OFProtocol>();
        ofProtocolNo = IPMService.OFPROTOCOLID_START;
        
        fieldDatabase = new ConcurrentHashMap<Short, OFMatch20>();
        fieldIdNo = IPMService.FIELDID_START;        
    }
}
