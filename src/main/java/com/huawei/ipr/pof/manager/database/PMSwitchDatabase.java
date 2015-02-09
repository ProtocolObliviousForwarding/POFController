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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.openflow.protocol.OFCounter;
import org.openflow.protocol.OFCounter.OFCounterModCmd;
import org.openflow.protocol.OFFeaturesReply;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFGroupMod;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFMeterMod;
import org.openflow.protocol.OFPortStatus;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionType;
import org.openflow.protocol.table.OFDataTable;
import org.openflow.protocol.table.OFFlowTable;
import org.openflow.protocol.table.OFFlowTableResource;
import org.openflow.protocol.table.OFTableType;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.huawei.ipr.pof.manager.IPMService;

/**
 * SMSwitchDatabase is a part of SMDatabase, it stores data belonged to a switch.
 * <br>
 * SMSwitchDatabase stores:
 * <ul>
 *  <li>Ports</li>
 *  <li>TableResource</li>
 *  <li>Flow Tables</li>
 *  <li>Flow Entries (stored in {@link PMFlowTableDatabase})</li>
 *  <li>Counters</li>
 *  <li>GroupMods</li>
 *  <li>MeterMods</li>
 *  <li>LoggedMessages</li>
 *  <li>SentMessage (for roll back)</li>
 * </ul>
 * 
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 *
 */
public class PMSwitchDatabase {
    protected int deviceId;
    protected OFFeaturesReply switchFeatures;
    
    protected Map<Integer,OFPortStatus> portsMap;
    
    protected OFFlowTableResource flowTableResource;
    
    protected Map<Byte, OFFlowTable> flowTablesMap; //<globalTableId, table>
    protected Map<Byte, PMFlowTableDatabase> flowTableDatabaseMap; //<globaltableId, SMFlowTableDatabase>
    
    protected Map<OFTableType, Byte> flowTableNoBaseMap;    //<tableType, NumberBase>
    protected Map<OFTableType, Byte> flowTableNoMap;        //<tableType, globalTableId>
    protected Map<OFTableType, List<Byte>> freeFlowTableIDListMap;
    
    protected OFDataTable<OFCounter> counterTable;  //<id, counterEntry>
    protected OFDataTable<OFGroupMod> groupTable;      //<id, groupEntry>
    protected OFDataTable<OFMeterMod> meterTable;      //<id, meterEntry>
    
    protected Queue<OFMessage> ofmInLogList;
    protected Map<OFType, Queue<OFMessage>> ofmInLogMap;
    
    protected Queue<OFMessage> ofmOutLogList;
    protected Map<OFType, Queue<OFMessage>> ofmOutLogMap;
    
    protected Queue<OFMessage> sendedOfmQueue;    
    protected Map<Integer, OFMessage> oldBackupOfmMap; //<sended_Ofm_xid, oldOFMessage>
    
    public final static int QUEUE_LOG_SIZE_MAXIMAL = 100;
    
    public final static int QUEUE_SENDED_SIZE_MAXIMAL = 20;
    
    public PMSwitchDatabase(int switchId) {
        deviceId = switchId;
        
        portsMap = new ConcurrentHashMap<Integer,OFPortStatus>();
        
        flowTablesMap = new ConcurrentHashMap<Byte, OFFlowTable>();
        flowTableDatabaseMap = new ConcurrentHashMap<Byte, PMFlowTableDatabase>();
        
        flowTableNoBaseMap = new ConcurrentHashMap<OFTableType, Byte>();
        flowTableNoMap = new ConcurrentHashMap<OFTableType, Byte>();
        freeFlowTableIDListMap = new ConcurrentHashMap<OFTableType, List<Byte>>();
        
        counterTable = new OFDataTable<OFCounter>(IPMService.COUNTERID_START);
        groupTable = new OFDataTable<OFGroupMod>(IPMService.GROUPID_START);
        meterTable = new OFDataTable<OFMeterMod>(IPMService.COUNTERID_START);
        
        ofmInLogList = new LinkedBlockingQueue<OFMessage>(QUEUE_LOG_SIZE_MAXIMAL);
        ofmOutLogList = new LinkedBlockingQueue<OFMessage>(QUEUE_LOG_SIZE_MAXIMAL);
        
        ofmInLogMap = new ConcurrentHashMap<OFType, Queue<OFMessage>>();
        ofmOutLogMap = new ConcurrentHashMap<OFType, Queue<OFMessage>>();
        
        sendedOfmQueue = new LinkedBlockingQueue<OFMessage>(QUEUE_SENDED_SIZE_MAXIMAL);
        oldBackupOfmMap = new ConcurrentHashMap<Integer, OFMessage>();
    }
    
    public void iAddSendedOFMessage(OFMessage message){
        if(sendedOfmQueue.size() >= QUEUE_LOG_SIZE_MAXIMAL){
            OFMessage polledMsg = sendedOfmQueue.poll();
            oldBackupOfmMap.remove( polledMsg.getXid() );
        }
        sendedOfmQueue.offer(message);
    }
    
    public Queue<OFMessage> iGetSendedOFMessageQueue(){
        return sendedOfmQueue;
    }
    
    public OFMessage iGetSendedOFMessage(int xid){
        for(OFMessage message : sendedOfmQueue){
            if(message.getXid() == xid){
                return message;
            }            
        }
        return null;
    }
    
    public void iDelSendedOFMessage(OFMessage message){
        sendedOfmQueue.remove(message);        
    }
    
    public void iAddOldBackupMessage(int sended_msg_xid, OFMessage message){
        oldBackupOfmMap.put(sended_msg_xid, message);
    }
    
    public OFMessage iGetOldBackupMessage(int xid){
        return oldBackupOfmMap.get(xid);
    }
    
    public void iDelOldBackupMessage(int xid){
        oldBackupOfmMap.remove(xid);
    }
    
    
    
    public void iLogOFMessageIn(OFMessage message){
        if(ofmInLogList.size() >= QUEUE_LOG_SIZE_MAXIMAL){
            ofmInLogList.poll();
        }
        ofmInLogList.offer(message);
    }
    
    public void iLogOFMessageIn(OFType type, OFMessage message){
        if(false == ofmInLogMap.containsKey(type)){
            ofmInLogMap.put(type, new LinkedBlockingQueue<OFMessage>(QUEUE_LOG_SIZE_MAXIMAL));
        }
        Queue<OFMessage> queue = ofmInLogMap.get(type);
        if(queue.size() >= QUEUE_LOG_SIZE_MAXIMAL){
            queue.poll();
        }
        queue.offer(message);
    }
    
    public void iLogOFMessageOut(OFMessage message){
        if(ofmOutLogList.size() >= QUEUE_LOG_SIZE_MAXIMAL){
            ofmOutLogList.poll();
        }
        ofmOutLogList.offer(message);
    }
    
    public void iLogOFMessageOut(OFType type, OFMessage message){
        if(false == ofmOutLogMap.containsKey(type)){
            ofmOutLogMap.put(type, new LinkedBlockingQueue<OFMessage>(QUEUE_LOG_SIZE_MAXIMAL));
        }
        Queue<OFMessage> queue = ofmOutLogMap.get(type);
        if(queue.size() >= QUEUE_LOG_SIZE_MAXIMAL){
            queue.poll();
        }
        queue.offer(message);
    }
    
    public Queue<OFMessage> iGetLogOFMessageInAll(){
        return ofmInLogList;
    }
    
    public Queue<OFMessage> iGetLogOFMessageIn(OFType type){
        return ofmInLogMap.get(type);        
    }
    
    public Queue<OFMessage> iGetLogOFMessageOutAll(){
        return ofmOutLogList;
    }
    
    public Queue<OFMessage> iGetLogOFMessageOut(OFType type){
        return ofmOutLogMap.get(type);        
    }
        
    public OFPortStatus getPort(int portId){
        return portsMap.get(portId);
    }
    
    public void putPort(int portId, OFPortStatus port){
        portsMap.put(portId, port);
    }
    
    public OFFlowTable getFlowTable(byte globalTableId){
        return flowTablesMap.get(globalTableId);
    }
    
    public void putFlowTable(byte globalTableId, OFFlowTable flowTable){
        flowTablesMap.put(globalTableId, flowTable);
    }
    
    public void removeFlowTable(byte globalTableId){
        flowTablesMap.remove(globalTableId);
    }

    public PMFlowTableDatabase getFlowTableDatabase(byte globalTableId){
        return flowTableDatabaseMap.get(globalTableId);
    }
    
    public void putNewFlowTableDatabse(byte globalTableId){
        flowTableDatabaseMap.put(globalTableId, new PMFlowTableDatabase(globalTableId));
    }
    
    public void removeFlowTableDatabse(byte globalTableId){
        flowTableDatabaseMap.remove(globalTableId);
    }
    
    public int allocCounterId(){
        int newCounterID = counterTable.alloc();
        OFCounter newCounter = new OFCounter();
        newCounter.setCommand(OFCounterModCmd.OFPCC_ADD);
        
        putCounter(newCounterID, newCounter);
        
        return newCounterID;
    }
    public OFCounter getCounter(int counterId){
        return counterTable.get(counterId);
    }
    public void putCounter(int counterId, OFCounter counter){
        counterTable.put(counterId, counter);
    }
    public OFCounter removeCounter(int counterId){
        return counterTable.remove(counterId);
    }
    
    
    public int allocGroupId(){
        return groupTable.alloc();
    }
    public OFGroupMod getGroup(int groupId){
        return groupTable.get(groupId);
    }
    public void putGroup(int groupId, OFGroupMod group){
        groupTable.put(groupId, group);
    }
    public OFGroupMod removeGroup(int groupId){
        return groupTable.remove(groupId);
    }
    
	public class GroupComparatpr<T> implements Comparator<OFGroupMod> {
		public int compare(OFGroupMod group1, OFGroupMod group2) {
			if(group1 == null || group2 == null){
				return 0;
			}
			return group1.getGroupId() - group2.getGroupId();
		}
	}
	
	GroupComparatpr<OFGroupMod> groupComp = new GroupComparatpr<OFGroupMod>();
	
    public List<OFGroupMod> getAllGroupList(){
        List<OFGroupMod> groupList = Collections.synchronizedList(new ArrayList<OFGroupMod>());
        
        try{
	        Map<Integer, OFGroupMod> hashMap =  groupTable.getAllData();
	        Iterator<Integer> iter = hashMap.keySet().iterator();
	        int index;
	        while(iter.hasNext()){
	            index = iter.next();
	            groupList.add(hashMap.get(index));
	        }
	        
	        Collections.sort(groupList, groupComp);
        }catch(Exception e){
        	e.printStackTrace();
        }
        return groupList;        
    }
    
    
    public int allocMeterId(){
        return meterTable.alloc();
    }
    public OFMeterMod getMeter(int meterId){
        return meterTable.get(meterId);
    }
    public void putMeter(int meterId, OFMeterMod meter){
        meterTable.put(meterId, meter);
    }
    public OFMeterMod removeMeter(int meterId){
        return meterTable.remove(meterId);
    }
    
	public class MeterComparatpr<T> implements Comparator<OFMeterMod> {
		public int compare(OFMeterMod meter1, OFMeterMod meter2) {
			if(meter1 == null || meter2 == null){
				return 0;
			}
			return meter1.getMeterId() - meter2.getMeterId();
		}
	}
	
	MeterComparatpr<OFMeterMod> meterComp = new MeterComparatpr<OFMeterMod>();
	
    public List<OFMeterMod> getAllMeterList(){
        List<OFMeterMod> meterList = Collections.synchronizedList(new ArrayList<OFMeterMod>());
        
        try{
	        Map<Integer, OFMeterMod> hashMap =  meterTable.getAllData();
	        Iterator<Integer> iter = hashMap.keySet().iterator();
	        int index;
	        while(iter.hasNext()){
	            index = iter.next();
	            meterList.add(hashMap.get(index));
	        }
	        
	        Collections.sort(meterList, meterComp);
        }catch(Exception e){
        	e.printStackTrace();
        }
        return meterList;        
    }
   
    
    
    public int getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }
    public OFFeaturesReply getSwitchFeatures() {
        return switchFeatures;
    }
    public void setSwitchFeatures(OFFeaturesReply switchFeatures) {
        this.switchFeatures = switchFeatures;
    }

    public Map<Integer, OFPortStatus> getPortsMap() {
        return portsMap;
    }

    public OFFlowTableResource getFlowTableResource() {
        return flowTableResource;
    }
    public void setFlowTableResource(OFFlowTableResource flowTableResource) {
    	if(flowTableResource == null){
    		return;
    	}
    	
        this.flowTableResource = flowTableResource;
        
        counterTable.setMaxNumber(flowTableResource.getCounterNum());
        groupTable.setMaxNumber(flowTableResource.getGroupNum());
        meterTable.setMaxNumber(flowTableResource.getMeterNum());
    }

    public Map<Byte, OFFlowTable> getFlowTablesMap() {
        return flowTablesMap;
    }
    
    public Map<Integer, OFFlowMod> getFlowEntriesMap(byte globalTableId) {
    	if(null == flowTableDatabaseMap.get(globalTableId)){
    		return null;
    	}
        return flowTableDatabaseMap.get(globalTableId).getFlowEntriesMap();
    }

    public byte getNewFlowTableID(byte tableType) {
    	byte newFlowTableID = IPMService.FLOWTABLEID_INVALID;
    	
    	try{
	        OFTableType ofTableType = OFTableType.values()[tableType];
	        if(null == freeFlowTableIDListMap
	                || null == freeFlowTableIDListMap.get(ofTableType)
	                || 0 == freeFlowTableIDListMap.get(ofTableType).size()){
	            newFlowTableID = this.flowTableNoMap.get(ofTableType);
	            
	            this.setFlowTableNo(tableType, (byte) (newFlowTableID + 1));
	        }else{
	            newFlowTableID = freeFlowTableIDListMap.get(ofTableType).remove(0);
	        }
    	}catch(Exception e){
        	e.printStackTrace();
        }
        return newFlowTableID;
    }
    
    public void setFlowTableNo(byte tableType, byte flowTableNo) {
        this.flowTableNoMap.put(OFTableType.values()[tableType], flowTableNo);
    }
    
    public byte getFlowTableNoBase(byte tableType) {
    	try{
    		return this.flowTableNoBaseMap.get(OFTableType.values()[tableType]);
    	}catch(Exception e){
    		e.printStackTrace();
    		return IPMService.FLOWTABLEID_INVALID;
    	}
        
    }
    
    public void setFlowTableNoBase(byte tableType, byte flowTableNoBase) {
        this.flowTableNoBaseMap.put(OFTableType.values()[tableType], flowTableNoBase);
        
        this.freeFlowTableIDListMap.put(OFTableType.values()[tableType], Collections.synchronizedList(new ArrayList<Byte>()));
    }
    
	public class ByteComparatpr<T> implements Comparator<Byte> {
		public int compare(Byte id1, Byte id2) {
			return id1 - id2;
		}

	}
	
	ByteComparatpr<Byte> byteComp = new ByteComparatpr<Byte>();

    
    public void addFreeFlowTableID(byte tableType, byte flowTableID){
    	List<Byte> freeIdList = freeFlowTableIDListMap.get(OFTableType.values()[tableType]);
    	freeIdList.add(flowTableID);
        
        Collections.sort(freeIdList, byteComp); 
    }
    
    
    public int getTableNumber(OFTableType tableType){
    	try{
        	return flowTableNoMap.get(tableType) - flowTableNoBaseMap.get(tableType) - freeFlowTableIDListMap.get(tableType).size();
    	}catch(Exception e){
        	e.printStackTrace();
        	return 0;
        }
    }
    
    public int getAllTableNumber(){
        return flowTablesMap.size();
    }
    
    public int getUsedCounterNumber(){
        return counterTable.usedSize();
    }
    
    public int getUsedGroupNumber(){
        return groupTable.usedSize();
    }
    
    public int getUsedMeterNumber(){
        return meterTable.usedSize();
    }
    
    private boolean saveGroupTableIntoFile(OutputStream out){
        Gson gson = new Gson();
        String string;
        java.lang.reflect.Type type;

        try {
            string = gson.toJson("#Group#", "#Group#".getClass());
            out.write(string.getBytes());
            out.write('\n');
            
            type = new TypeToken<List<Integer>>(){}.getType();
            string = gson.toJson(groupTable.getFreeIdList(), type);
            out.write(string.getBytes());
            out.write('\n');
            
            string = gson.toJson(groupTable.getEntryIdNo(), int.class);
            out.write(string.getBytes());
            out.write('\n');
            
            string = gson.toJson(groupTable.getMaxNumber(), int.class);
            out.write(string.getBytes());
            out.write('\n');
            
            string = gson.toJson(groupTable.getStartNo(), int.class);
            out.write(string.getBytes());
            out.write('\n');
            
            Iterator<Integer> groupTableItor  = groupTable.getAllData().keySet().iterator();
            Map<Integer, OFGroupMod> groupTableMap = groupTable.getAllData();
            int groupId;
            OFGroupMod ofGroup;
            String groupIdFlagString;
            while(groupTableItor.hasNext()){
            	groupId = groupTableItor.next();
            	ofGroup = groupTableMap.get(groupId);
            	
            	groupIdFlagString = "#groupid#" + groupId;
            	out.write(groupIdFlagString.getBytes());
            	out.write('\n');
            	
            	if(false == saveGroupIntoFile(out, ofGroup)){
            		return false;
            	}            	
            }
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        
        return true;
    }
    
    
    private boolean loadGroupTableFromFile(BufferedReader br, List<String> returnedCurLineString) {
        Gson gson = new Gson();
        String lineString;
        java.lang.reflect.Type type;
        
        try {
        	lineString = br.readLine();
        	if(!gson.fromJson(lineString, String.class).equals("#Group#")){
        		return false;
        	}
        	
            //read freeIdList
            lineString = br.readLine();
            type = new TypeToken<List<Integer>>(){}.getType();
            List<Integer> freeIdList = gson.fromJson(lineString, type);
            
            //read flowTableId
            lineString = br.readLine();
            int entryId = gson.fromJson(lineString, int.class); 
            
            
            //read maxNumber
            lineString = br.readLine();
            int maxNumber = gson.fromJson(lineString, int.class);  
     
            //read startNo
            lineString = br.readLine();
            int startNo = gson.fromJson(lineString, int.class);  
            
            int pos;
            int groupId;
        	String typeString;
        	String actionString;
            OFGroupMod ofGroup;
            List<OFAction> actionList;
            OFActionType actionType;
            OFAction action;
            
            Map<Integer, OFGroupMod> groupTableMap = new ConcurrentHashMap<Integer, OFGroupMod>();
            
            lineString = br.readLine();
            
            while(null != lineString && lineString.contains("#groupid#") ){
            	groupId = (int)Long.parseLong( lineString.substring("#groupid#".length()));            	
            	
            	ofGroup = new OFGroupMod();
            	ofGroup.setLengthU(OFGroupMod.MAXIMAL_LENGTH);
            	
            	//read group command
            	lineString = br.readLine();
            	ofGroup.setCommand(  gson.fromJson(lineString, byte.class) );
            	
            	//read group type
            	lineString = br.readLine();
            	ofGroup.setGroupType( gson.fromJson(lineString, byte.class) );
            	
            	//read group id
            	lineString = br.readLine();
            	ofGroup.setGroupId( gson.fromJson(lineString, int.class) );
            	
            	//read group counter id
            	lineString = br.readLine();
            	ofGroup.setCounterId( gson.fromJson(lineString, int.class) );           	
            	

            	//read ofGroup ActionList
				lineString = br.readLine();

				actionList = Collections.synchronizedList(new ArrayList<OFAction>());

				while (null != lineString && lineString.contains("#action.")) {
					pos = lineString.indexOf("#", "#action.".length());
					typeString = lineString.substring("#action.".length(), pos);
					actionString = lineString.substring(pos + 1);

					actionType = OFActionType.valueOf(typeString);
					
					action = gson.fromJson(actionString, actionType.toClass());

					actionList.add(action);

					lineString = br.readLine();
				}//while: read actions
				
				//set action list
				if (null == ofGroup.getActionList()) {
					ofGroup.setActionList(actionList);
				}
				
				//set group action num
				ofGroup.setActionNum((byte) (actionList.size()));
				
				//put the groupMod to GroupTableMap
				groupTableMap.put(groupId, ofGroup);				
            }//while: read GroupEntry
            
            groupTable = new OFDataTable<OFGroupMod>(groupTableMap, freeIdList, entryId, maxNumber, startNo);
            
            returnedCurLineString.add(0, lineString);


        }catch (Exception e) {
            
            e.printStackTrace();
            return false;
        }

        returnedCurLineString.add(0, lineString);
        return true;
	}

    private boolean saveGroupIntoFile(OutputStream out, OFGroupMod ofGroup) {
        Gson gson = new Gson();
        String string;
        //java.lang.reflect.Type type;

        try {
            string = gson.toJson(ofGroup.getCommand(), byte.class);
            out.write(string.getBytes());
            out.write('\n');
            
            string = gson.toJson(ofGroup.getGroupType(), byte.class);
            out.write(string.getBytes());
            out.write('\n');
            
            string = gson.toJson(ofGroup.getGroupId(), int.class);
            out.write(string.getBytes());
            out.write('\n');
            
            string = gson.toJson(ofGroup.getCounterId(), int.class);
            out.write(string.getBytes());
            out.write('\n');
            
    		for(OFAction action : ofGroup.getActionList()){
            	string = "#action." + action.getType().toString() + "#" + gson.toJson(action, action.getType().toClass());
                out.write(string.getBytes());
                out.write('\n');
    		}

        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
	}
    
    

	public boolean saveAllDataIntoFile(OutputStream out){
        Gson gson = new Gson();
        String string;
        java.lang.reflect.Type type;

        try {
            // save deviceId
            string = gson.toJson(deviceId, int.class);
            out.write(string.getBytes());
            out.write('\n');
            
            //save freeFlowTableIDListMap
            type = new TypeToken<ConcurrentHashMap<OFTableType, List<Byte>>>(){}.getType();
            string = gson.toJson(freeFlowTableIDListMap, type);
            out.write(string.getBytes());
            out.write('\n');
            
            //save counterTable
            type = new TypeToken<OFDataTable<OFCounter>>(){}.getType();
            string = gson.toJson(counterTable, type);
            out.write(string.getBytes());
            out.write('\n');
            
            //save groupTable
//            type = new TypeToken<OFDataTable<OFGroupMod>>(){}.getType();
//            string = gson.toJson(groupTable, type);
//            out.write(string.getBytes());
//            out.write('\n');
            //save group
        	if(false == saveGroupTableIntoFile(out)){
        		return false;
        	}
            
            //save meterTable
            type = new TypeToken<OFDataTable<OFMeterMod>>(){}.getType();
            string = gson.toJson(meterTable, type);
            out.write(string.getBytes());
            out.write('\n');
            
            //save flowTablesMap
            type = new TypeToken<ConcurrentHashMap<Byte, OFFlowTable>>(){}.getType();
            string = gson.toJson(flowTablesMap, type);
            out.write(string.getBytes());
            out.write('\n');
            
            //save flowTableDatabaseMap
            Iterator<Byte> flowTableDatabaseItor  = flowTableDatabaseMap.keySet().iterator();
            byte tableId;
            PMFlowTableDatabase flowTableDatabase;
            String tableIdFlagString;
            while(flowTableDatabaseItor.hasNext()){
            	tableId = flowTableDatabaseItor.next();
            	flowTableDatabase = flowTableDatabaseMap.get(tableId);
            	
            	tableIdFlagString = "#tableid#" + tableId;
            	out.write(tableIdFlagString.getBytes());
            	out.write('\n');
            	
            	if(false == flowTableDatabase.saveAllDataIntoFile(out)){
            		return false;
            	}            	
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean loadAllDataFromFile(BufferedReader br, List<String> returnedCurLineString, boolean switchConnected){
        Gson gson = new Gson();
        String lineString;
        java.lang.reflect.Type type;
        
        try {
            //read deviceId
            lineString = br.readLine();
            deviceId = (int)(long)gson.fromJson(lineString, long.class);
            
            //read freeFlowTableIDListMap
            lineString = br.readLine();
            type = new TypeToken<ConcurrentHashMap<OFTableType, List<Byte>>>(){}.getType();
            freeFlowTableIDListMap = gson.fromJson(lineString, type);
            
            //read conterTable
            lineString = br.readLine();
            type = new TypeToken<OFDataTable<OFCounter>>(){}.getType();
            counterTable = gson.fromJson(lineString, type);
            
            //read groupTable
            if(false == loadGroupTableFromFile(br, returnedCurLineString)){
            	return false;
            }
            
            //read meterTable
            lineString = returnedCurLineString.get(0);
            type = new TypeToken<OFDataTable<OFMeterMod>>(){}.getType();
            meterTable = gson.fromJson(lineString, type);
            
            //read flowTablesMap
            lineString = br.readLine();
            type = new TypeToken<ConcurrentHashMap<Byte, OFFlowTable>>(){}.getType();
            flowTablesMap = gson.fromJson(lineString, type);
            
            //reset logs
            ofmInLogList.clear();
            ofmOutLogList.clear();
            
            ofmInLogMap.clear();
            ofmOutLogMap.clear();
            
            sendedOfmQueue.clear();
            oldBackupOfmMap.clear();

            
            //save flowTableDatabaseMap
            if(null == flowTableDatabaseMap){
            	flowTableDatabaseMap = new ConcurrentHashMap<Byte, PMFlowTableDatabase>();
            }else{
            	flowTableDatabaseMap.clear();
            }
            byte tableId;
            lineString = br.readLine();
            PMFlowTableDatabase flowTableDatabase;
            while( null != lineString && lineString.contains("#tableid#") ){
            	tableId = Byte.parseByte( lineString.substring("#tableid#".length()));
            	
            	flowTableDatabase = flowTableDatabaseMap.get(tableId);
            	if(null == flowTableDatabase){
            		flowTableDatabase = new PMFlowTableDatabase(tableId);
            		flowTableDatabaseMap.put(tableId, flowTableDatabase);
            	}            	
            	
            	if(false == flowTableDatabase.loadAllDataFromFile(tableId, br, returnedCurLineString)){
            		return false;
            	}
            	lineString = returnedCurLineString.get(0);
            }
            
            if(switchConnected){
	            int[] tableNum = new int[OFTableType.OF_MAX_TABLE_TYPE.getValue()];
	            Iterator<Byte> tableIdIter = flowTablesMap.keySet().iterator();
	            byte tableID;
	            OFFlowTable ofTable;
	            while(tableIdIter.hasNext()){
	                tableID = tableIdIter.next();
	                ofTable = flowTablesMap.get(tableID);
	                tableNum[ofTable.getTableType().ordinal()]++;
	            }
	            
	            for(byte tableType = 0; tableType < OFTableType.OF_MAX_TABLE_TYPE.getValue(); tableType++){
	            	flowTableNoMap.put(OFTableType.values()[tableType], (byte)(flowTableNoBaseMap.get(OFTableType.values()[tableType]) + tableNum[tableType]) );
	            }
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
        result = prime * result + ((counterTable == null) ? 0 : counterTable.hashCode());
        result = prime * result + deviceId;
        result = prime * result + ((flowTableDatabaseMap == null) ? 0 : flowTableDatabaseMap.hashCode());
        result = prime * result + ((flowTableNoBaseMap == null) ? 0 : flowTableNoBaseMap.hashCode());
        result = prime * result + ((flowTableNoMap == null) ? 0 : flowTableNoMap.hashCode());
        result = prime * result + ((flowTableResource == null) ? 0 : flowTableResource.hashCode());
        result = prime * result + ((flowTablesMap == null) ? 0 : flowTablesMap.hashCode());
        result = prime * result + ((freeFlowTableIDListMap == null) ? 0 : freeFlowTableIDListMap.hashCode());
        result = prime * result + ((groupTable == null) ? 0 : groupTable.hashCode());
        result = prime * result + ((meterTable == null) ? 0 : meterTable.hashCode());
        result = prime * result + ((ofmInLogList == null) ? 0 : ofmInLogList.hashCode());
        result = prime * result + ((ofmInLogMap == null) ? 0 : ofmInLogMap.hashCode());
        result = prime * result + ((ofmOutLogList == null) ? 0 : ofmOutLogList.hashCode());
        result = prime * result + ((ofmOutLogMap == null) ? 0 : ofmOutLogMap.hashCode());
        result = prime * result + ((oldBackupOfmMap == null) ? 0 : oldBackupOfmMap.hashCode());
        result = prime * result + ((portsMap == null) ? 0 : portsMap.hashCode());
        result = prime * result + ((sendedOfmQueue == null) ? 0 : sendedOfmQueue.hashCode());
        result = prime * result + ((switchFeatures == null) ? 0 : switchFeatures.hashCode());
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
        PMSwitchDatabase other = (PMSwitchDatabase) obj;
        if (counterTable == null) {
            if (other.counterTable != null)
                return false;
        } else if (!counterTable.equals(other.counterTable))
            return false;
        if (deviceId != other.deviceId)
            return false;
        if (flowTableDatabaseMap == null) {
            if (other.flowTableDatabaseMap != null)
                return false;
        } else if (!flowTableDatabaseMap.equals(other.flowTableDatabaseMap))
            return false;
        if (flowTableNoBaseMap == null) {
            if (other.flowTableNoBaseMap != null)
                return false;
        } else if (!flowTableNoBaseMap.equals(other.flowTableNoBaseMap))
            return false;
        if (flowTableNoMap == null) {
            if (other.flowTableNoMap != null)
                return false;
        } else if (!flowTableNoMap.equals(other.flowTableNoMap))
            return false;
        if (flowTableResource == null) {
            if (other.flowTableResource != null)
                return false;
        } else if (!flowTableResource.equals(other.flowTableResource))
            return false;
        if (flowTablesMap == null) {
            if (other.flowTablesMap != null)
                return false;
        } else if (!flowTablesMap.equals(other.flowTablesMap))
            return false;
        if (freeFlowTableIDListMap == null) {
            if (other.freeFlowTableIDListMap != null)
                return false;
        } else if (!freeFlowTableIDListMap.equals(other.freeFlowTableIDListMap))
            return false;
        if (groupTable == null) {
            if (other.groupTable != null)
                return false;
        } else if (!groupTable.equals(other.groupTable))
            return false;
        if (meterTable == null) {
            if (other.meterTable != null)
                return false;
        } else if (!meterTable.equals(other.meterTable))
            return false;
        if (ofmInLogList == null) {
            if (other.ofmInLogList != null)
                return false;
        } else if (!ofmInLogList.equals(other.ofmInLogList))
            return false;
        if (ofmInLogMap == null) {
            if (other.ofmInLogMap != null)
                return false;
        } else if (!ofmInLogMap.equals(other.ofmInLogMap))
            return false;
        if (ofmOutLogList == null) {
            if (other.ofmOutLogList != null)
                return false;
        } else if (!ofmOutLogList.equals(other.ofmOutLogList))
            return false;
        if (ofmOutLogMap == null) {
            if (other.ofmOutLogMap != null)
                return false;
        } else if (!ofmOutLogMap.equals(other.ofmOutLogMap))
            return false;
        if (oldBackupOfmMap == null) {
            if (other.oldBackupOfmMap != null)
                return false;
        } else if (!oldBackupOfmMap.equals(other.oldBackupOfmMap))
            return false;
        if (portsMap == null) {
            if (other.portsMap != null)
                return false;
        } else if (!portsMap.equals(other.portsMap))
            return false;
        if (sendedOfmQueue == null) {
            if (other.sendedOfmQueue != null)
                return false;
        } else if (!sendedOfmQueue.equals(other.sendedOfmQueue))
            return false;
        if (switchFeatures == null) {
            if (other.switchFeatures != null)
                return false;
        } else if (!switchFeatures.equals(other.switchFeatures))
            return false;
        return true;
    }

}
