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


package com.huawei.ipr.pof.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
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
import java.util.concurrent.TimeUnit;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.OFCounterFuture;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.threadpool.IThreadPoolService;

import org.jboss.netty.channel.Channel;
import org.openflow.protocol.OFCounter;
import org.openflow.protocol.OFCounter.OFCounterModCmd;
import org.openflow.protocol.OFCounterMod;
import org.openflow.protocol.OFCounterReply;
import org.openflow.protocol.OFCounterRequest;
import org.openflow.protocol.OFError;
import org.openflow.protocol.OFFeaturesReply;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFFlowMod.OFFlowEntryCmd;
import org.openflow.protocol.OFGroupMod;
import org.openflow.protocol.OFGroupMod.OFGroupModCmd;
import org.openflow.protocol.OFMatch20;
import org.openflow.protocol.OFMatchX;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFMeterMod;
import org.openflow.protocol.OFMeterMod.OFMeterModCmd;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPortStatus;
import org.openflow.protocol.OFPortStatus.OFPortReason;
import org.openflow.protocol.OFProtocol;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionAddField;
import org.openflow.protocol.action.OFActionCalculateCheckSum;
import org.openflow.protocol.action.OFActionCounter;
import org.openflow.protocol.action.OFActionDeleteField;
import org.openflow.protocol.action.OFActionDrop;
import org.openflow.protocol.action.OFActionGroup;
import org.openflow.protocol.action.OFActionModifyField;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.protocol.action.OFActionPacketIn;
import org.openflow.protocol.action.OFActionSetField;
import org.openflow.protocol.action.OFActionSetFieldFromMetadata;
import org.openflow.protocol.factory.BasicFactory;
import org.openflow.protocol.instruction.OFInstruction;
import org.openflow.protocol.instruction.OFInstructionApplyActions;
import org.openflow.protocol.instruction.OFInstructionGotoDirectTable;
import org.openflow.protocol.instruction.OFInstructionGotoTable;
import org.openflow.protocol.instruction.OFInstructionMeter;
import org.openflow.protocol.instruction.OFInstructionWriteMetadata;
import org.openflow.protocol.instruction.OFInstructionWriteMetadataFromPacket;
import org.openflow.protocol.table.OFFlowTable;
import org.openflow.protocol.table.OFFlowTableResource;
import org.openflow.protocol.table.OFTableMod;
import org.openflow.protocol.table.OFTableMod.OFTableModCmd;
import org.openflow.protocol.table.OFTableType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.huawei.ipr.pof.gui.comm.IGUIService;
import com.huawei.ipr.pof.manager.database.IPMDatabaseService;
import com.huawei.ipr.pof.manager.database.PMSwitchDatabase;

/**
 * PofManager implements ISMService. 
 * <p>
 * PofManager is a connector to interconnect with:
 * <ul>
 *  <li> OFMessageBypassManager for getting OFMessages from controller and to process OFMessages;</li>
 *  <li> POFGUI to provide process methods to response user's input, and to display information in POFGUI</li>
 *  <li> SMDatabase to store the data</li>
 * </ul>
 * 
 * <p>
 * All the methods and parameters are name-self-explained.
 * <p>
 * For the detail of OFMessages/OFInstructions/OFActions,
 * please check POF white paper, OpenFlow documents and the source code.
 * 
 * <p>
 * TODO For now POFGUI invokes PofManager direct. 
 * However, PofManager is better to be a language independent module
 * so that PofManager could be connected with other applications.
 * TODO One way is using TCP to connect with other application (such as POFGUI).
 *      Or, PofManager could be published as web service module 
 *      so that any other application could invoke its interfaces. 
 * 
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 *
 */
public class PofManager implements IFloodlightModule, IPMService {
    protected static final int BATCH_MAX_SIZE = 100;

    protected static Logger log = LoggerFactory.getLogger(PofManager.class);

    
    protected IFloodlightProviderService floodlightProvider;
    protected IThreadPoolService threadPool;  
    protected BasicFactory factory;
    
    protected IPMDatabaseService database;
    protected IGUIService gui;
    
    
    protected boolean connected = false;

    /**
     * switchID -> tcpChannel mapping
     */
    protected Map<Integer, Channel> ofChannels;
    
    /**
     * switchID -> IOFSwitch mapping
     */
    protected Map<Integer, IOFSwitch> switches;
    
    /**
     * counter.xid -> OFCounterFuture mapping, used to get the matched counter reply from the counter request
     */
    protected Map<Integer, OFCounterFuture> counterFutureMap;
    
    protected static final ThreadLocal< Map<Integer,List<OFMessage>> > local_of_msg_buffer =
            new ThreadLocal<Map<Integer,List<OFMessage>>>() {
                @Override
                protected Map<Integer,List<OFMessage>> initialValue() {
                    return new HashMap<Integer,List<OFMessage>>();
                }
            };

//    /*
//     * TODO 
//     * for now support invoke directly only.
//     */
//    public enum SMClientConnectMode{
//        SMCCM_DIRECT,
//        SMCCM_TCP,
//        SMCCM_SSL
//    }
    
    public Map<Integer, Channel> getOfChannels() {
        return ofChannels;
    }

    public void setOfChannels(Map<Integer, Channel> ofChannels) {
        this.ofChannels = ofChannels;
    }

    public synchronized boolean isConnected() {
        return connected;
    }

    public synchronized void setConnected(boolean connected) {
        this.connected = connected;
    }
    
    public void setFloodlightProvider(IFloodlightProviderService floodlightProvider) {
        this.floodlightProvider = floodlightProvider;
    }
    
    public IFloodlightProviderService getFloodlightProvider() {
        return floodlightProvider;
    }
    
    @Override
    public boolean addSwitch(long switchId, IOFSwitch sw){
    	try{
	    	if(false == database.addSwitchDatabase((int)switchId)){
	    		return false;
	    	}
	    	
	        switches.put((int)switchId, sw);
	        
	        addSwitchChannel(switchId, sw.getChannel());
	        
	        gui.addSwitch((int)switchId);
	        
	        iSetFeatures((int)switchId, sw.getFeaturesReply());
	        
	        return true;
    	}catch(Exception e){
    		e.printStackTrace();
    		return false;
    	}
    }
    
    @Override
    public void removeSwitch(long switchId){
    	try{
	        this.switches.remove((int)switchId);
	        
	        removeSwitchChannel(switchId);
	        
	        //TODO NOTES: what if switch reconnect again, clear the database or not?
	        database.removeSwitchDatabase((int)switchId);
	        
	        gui.removeSwitch((int)switchId);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    @Override
    public void addSwitchChannel(long switchId, Channel channel){
        this.ofChannels.put((int)switchId, channel);
    }
    
    @Override
    public void removeSwitchChannel(long switchId){
        this.ofChannels.remove((int)switchId);
    }
    
	public void writeOf(int switchDeviceId, OFMessage ofm){
		try{
	        Map<Integer, List<OFMessage>> of_msg_buffer_map = local_of_msg_buffer.get();
	        List<OFMessage> of_msg_buffer = of_msg_buffer_map.get(switchDeviceId);
	        if(of_msg_buffer == null){
	            of_msg_buffer = new ArrayList<OFMessage>();
	            of_msg_buffer_map.put(switchDeviceId, of_msg_buffer);
	        }
	        of_msg_buffer.add(ofm);
	        
	        //write ofm immediately
	        if(true){
	            this.writeOf(switchDeviceId, of_msg_buffer);
	            of_msg_buffer.clear();
	        }
		}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    private void writeOf(int switchDeviceId, List<OFMessage> of_msg_buffer) {
    	Channel channel = ofChannels.get(switchDeviceId);
    	if(null != channel){
    		channel.write(of_msg_buffer);
    	}
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> l = 
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IPMService.class);
        return l;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>,
        IFloodlightService> m = 
            new HashMap<Class<? extends IFloodlightService>,
                        IFloodlightService>();
        m.put(IPMService.class, this);
        
        return m;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l = 
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        l.add(IPMDatabaseService.class);
        l.add(IThreadPoolService.class);
        l.add(IGUIService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context)
            throws FloodlightModuleException {
        this.floodlightProvider = 
                context.getServiceImpl(IFloodlightProviderService.class);
        this.database = 
                context.getServiceImpl(IPMDatabaseService.class);
        this.threadPool = 
                context.getServiceImpl(IThreadPoolService.class);
        this.gui =
                context.getServiceImpl(IGUIService.class);
    }

    @Override
    public void startUp(FloodlightModuleContext context) {        
        factory = floodlightProvider.getOFMessageFactory();
        
        ofChannels = new ConcurrentHashMap <Integer, Channel>();
        switches = new ConcurrentHashMap <Integer, IOFSwitch>();
        counterFutureMap = new ConcurrentHashMap<Integer, OFCounterFuture>();
    }

    
    //interfaces to GUI
    @Override
    public List<Integer> iGetAllSwitchID(){
    	if(null == database){
    		return null;
    	}
        return database.iGetAllSwitchID();
    }
    @Override
    public void iSetPortOpenFlowEnable(int deviceId, int portId, byte onoff){
    	if(null == database){
    		return;
    	}
    	
        database.iSetPortOpenFlowEnable(deviceId, portId, onoff);
        
        OFPortStatus port = database.iGetPort(deviceId, portId);
        if(port != null){
	        port.setReason((byte)OFPortReason.OFPPR_MODIFY.ordinal());
	        port.setType(OFType.PORT_MOD);
	        
	        writeOf(deviceId, port);
        }
        
        
    }
    @Override
    public boolean iModifyMetadata(List<OFMatch20> metadataList){
    	if(null == database){
    		return false;
    	}
    	
    	return database.iModifyMetadata(metadataList);
    }
    @Override
    public List<OFMatch20> iGetMetadata(){
    	if(null == database){
    		return null;
    	}
    	
    	return database.iGetMetadata();
    }
    
    @Override
    public List<Integer> iGetAllPortId(int switchId){
    	if(null == database){
    		return null;
    	}
    	
        return database.iGetAllPortId(switchId);
    }
    
    
    @Override
    public short iAddProtocol(String protocolName, List<OFMatch20> fieldList){
    	if(null == fieldList){
    		log.error("addProtocol(name={}) failed: fieldList == null.", protocolName);
    		return OFPROTOCOLID_INVALID;
    	}
    	
    	if(false == checkFieldList(fieldList)){
    		log.error("addProtocol(name={}) failed: field info error.", protocolName);
    		return OFPROTOCOLID_INVALID;
    	}
    	
    	if(null == database){
    		return OFPROTOCOLID_INVALID;
    	}

    	short protocolID = database.iAddProtocol(protocolName, fieldList);
    	
        return protocolID;
    }
    
    @Override
    public OFProtocol iGetProtocol(short protocolID){
    	if(null == database){
    		return null;
    	}
    	
    	Map<Short, OFProtocol> protocolMap = database.iGetProtocolMap();
    	if(null == protocolMap){
    		return null;
    	}
        return protocolMap.get(protocolID);
    }
    
    @Override
    public OFProtocol iGetProtocol(String protocolName){
    	if(null == database){
    		return null;
    	}
    	
    	Map<String,Short> protocolNameMap = database.iGetProtocolNameMap();
    	if(null != protocolNameMap){
    		Short protocolID = protocolNameMap.get(protocolName);
    		if(protocolID != null){
    			return iGetProtocol(protocolID);
    		}
    	}
    	
        return null;
    }
    
    /**
     * check the fields' offset/length value of a protocol
     */
    private boolean checkFieldList(List<OFMatch20> fieldList){
    	if(null == fieldList){
    		return false;
    	}
    	    	
    	int previousFieldOffset = 0;
    	int previousFieldLength = 0;

    	for(OFMatch20 field : fieldList){
    		if(field.getOffset() < previousFieldOffset + previousFieldLength){
    			return false;
    		}
    		previousFieldOffset = field.getOffset();
    		previousFieldLength = field.getLength();
    	}
    	return true;
    }
    
	public class ProtocolComparator<T> implements Comparator<OFProtocol> {
		public int compare(OFProtocol p1, OFProtocol p2) {
			if(p1 == null || p2 == null){
				return 0;
			}
			
			short id1 = p1.getProtocolId();
			short id2 = p2.getProtocolId();;
			
			return id1 - id2;
		}
	}
	
	ProtocolComparator<OFProtocol> protocolComp = new ProtocolComparator<OFProtocol>();

    
    @Override
    public List<OFProtocol> iGetAllProtocol(){
    	if(null == database){
    		return null;
    	}
    	
        List<OFProtocol> protocolList = new ArrayList<OFProtocol>();
        
        Map<Short, OFProtocol> protocolMap = database.iGetProtocolMap();
        
        if(null != protocolMap){
	        Iterator<Short> protocolIdIter = protocolMap.keySet().iterator();
	        short protocolId;
	        OFProtocol protocol;
	        
	        if(null != protocolIdIter){
		        while(protocolIdIter.hasNext()){
		            protocolId = protocolIdIter.next();
		            protocol = protocolMap.get(protocolId);
		            protocolList.add(protocol);
		        }
		        
		        Collections.sort(protocolList, protocolComp);
	        }
        }
        
        return protocolList;
    }
    
    @Override
    public boolean iModifyProtocol(short protocolID, List<OFMatch20> newFieldList){
    	if(null == database){
    		return false;
    	}
    	
    	if(null == newFieldList){
    		log.error("modifyProtocol(id={}) failed: fieldList == null.", protocolID);
    		return false;
    	}
    	
    	if(false == checkFieldList(newFieldList)){
    		log.error("modifyProtocol(id={}) failed: field info error.", protocolID);
    		return false;
    	}
    	
    	OFProtocol protocol = iGetProtocol(protocolID);
    	
    	if(null == protocol){
    		log.error("modifyProtocol(id={}) failed: no such protocol", protocolID);
    		return false;
    	}
    	
    	return database.iModifyProtocol(protocol, newFieldList);
    }
    
    @Override
    public void iDelProtocol(short protocolID){
    	if(null == database){
    		return ;
    	}
    	
    	OFProtocol protocol = iGetProtocol(protocolID);
    	if(null == protocol){
    		log.error("delProtocol(id={}) failed: no such protocol", protocolID);
    		return ;
    	}
    	
    	List<OFMatch20> fieldList = protocol.getAllField();
    	if(null != fieldList){
	    	for(OFMatch20 field : fieldList){
	    		iDelField(field.getFieldId());
	    	}
    	}
    	database.iDelProtocol(protocol);
    }
    
    
    @Override
    public short iNewField(String fieldName, short fieldLength, short fieldOffset){
    	if(null == database){
    		return FIELDID_INVALID;
    	}
    	
        return database.iNewField(fieldName, fieldLength, fieldOffset);
    }
    
    @Override
    public boolean iModifyField(short fieldId, String fieldName, short fieldLength, short fieldOffset){
    	if(null == database){
    		return false;
    	}
    	
    	return database.iModifyField(fieldId, fieldName, fieldLength, fieldOffset);
    }
    
    @Override
    public void iDelField(short fieldId){
    	if(null == database){
    		return ;
    	}
    	
    	database.iDelField(fieldId);
    }
    
    @Override
    public OFProtocol iGetBelongedProtocol(short fieldId){
		try {
	    	if(null == database){
	    		return null;
	    	}
	    	
	        Map<Short, OFProtocol> protocolMap = database.iGetProtocolMap();
	        Iterator<Short> protocolIdIter = null;
	        if(null != protocolMap){
	        	protocolIdIter = protocolMap.keySet().iterator();
	        }
	        List<OFMatch20> fieldList;
	        short protocolId;
	        OFProtocol protocol;
        
			if(protocolIdIter != null){
		        while(protocolIdIter.hasNext()){
		            protocolId = protocolIdIter.next();
		            protocol = protocolMap.get(protocolId);
		            if(null != protocol){
		            	fieldList =  protocol.getAllField();
		            	if(null != fieldList){
		            		for(OFMatch20 matchField : fieldList){
		            			if(matchField.getFieldId() == fieldId){
									return protocol.clone();
		            			}
		            		}
		            	}
		            }
		        }
			}
        }catch (Exception e) {
			e.printStackTrace();
		}
        
        return null;
    }

    @Override
    public byte iAddFlowTable(int switchId, String tableName,
                                byte tableType, short keyLength, int tableSize,
                                byte fieldNum, List<OFMatch20> matchFieldList){
        return addFlowTable(switchId, tableName, tableType, keyLength, tableSize, fieldNum, matchFieldList, true);
    }
    
    private byte addFlowTable(int switchId, String tableName,
                                byte tableType, short keyLength, int tableSize,
                                byte fieldNum, List<OFMatch20> matchFieldList,
                                boolean writeToSwitch){
        byte globalTableId;
        try{
        	//check parameters
        	if(false == switches.containsKey(switchId)
            		|| tableName == null || tableName.length() == 0
            		|| tableSize == 0
            		|| tableType < 0 || tableType >= OFTableType.OF_MAX_TABLE_TYPE.getValue()
            		|| (tableType == OFTableType.OF_LINEAR_TABLE.getValue() && fieldNum != 0 && matchFieldList != null && matchFieldList.size() != 0)
            		|| (tableType != OFTableType.OF_LINEAR_TABLE.getValue() && (fieldNum == 0 || matchFieldList == null || matchFieldList.size() != fieldNum)) ){
        		return FLOWTABLEID_INVALID;
        	}
        	
        	if(null != matchFieldList){
        		int totalFiledLength = 0;
        		for(OFMatch20 field : matchFieldList){
        			if(field == null){
        				return FLOWTABLEID_INVALID;
        			}
        			totalFiledLength += field.getLength();
        		}
        		if(totalFiledLength != keyLength){
        			return FLOWTABLEID_INVALID;
        		}
        	}
        	
        	//add flow table
            globalTableId = database.iAddFlowTable(switchId, 
                                                        tableName, 
                                                        tableType, 
                                                        keyLength, 
                                                        tableSize, 
                                                        fieldNum, 
                                                        matchFieldList);

            if(FLOWTABLEID_INVALID == globalTableId){
            	return FLOWTABLEID_INVALID;
            }
            if(true == writeToSwitch){
                OFFlowTable flowTable = database.iGetFlowTable(switchId, globalTableId);
                if(flowTable == null){
                	return FLOWTABLEID_INVALID;
                }
                flowTable = flowTable.clone();
                flowTable.setCommand(OFTableModCmd.OFPTC_ADD);
                
                OFTableMod tableMod = (OFTableMod)(factory.getOFMessage(OFType.TABLE_MOD));
                tableMod.setFlowTable(flowTable);
                
                writeOf(switchId, tableMod); 
                
                //for roll back
                this.iAddSendedOFMessage(switchId, tableMod);
            }
        } catch (Exception e) {
            e.printStackTrace();            
            globalTableId = FLOWTABLEID_INVALID;
        }       
        
        return globalTableId;
    }
    
	public class FlowTableGlobalIDComparator<T> implements Comparator<OFFlowTable> {
		int switchId;
		public void setSwitchID(int switchId){
			this.switchId = switchId;
		}
		public int compare(OFFlowTable table1, OFFlowTable table2) {
			if(table1 == null || table2 == null){
				return 0;
			}
			byte id1 = PofManager.this.parseToGlobalTableId(switchId, table1.getTableType().getValue(), table1.getTableId());
			byte id2 = PofManager.this.parseToGlobalTableId(switchId, table2.getTableType().getValue(), table2.getTableId());
			
			return id1 - id2;
		}

	}
	
	FlowTableGlobalIDComparator<OFFlowTable> flowTableIdComp = new FlowTableGlobalIDComparator<OFFlowTable>();
    
    @Override
    public List<OFFlowTable> iGetAllFlowTable(int switchId){
    	List<OFFlowTable> tableList = new ArrayList<OFFlowTable>();
    	
    	try{
	    	if(null == database){
	    		return null;
	    	}
	    	
	        Map<Byte, OFFlowTable> flowTableMap = database.iGetFlowTableMap(switchId);
	        
	        if(flowTableMap != null){
		        Iterator<Byte> tableIdIter = flowTableMap.keySet().iterator();
		        byte tableID;
		        OFFlowTable ofTable;
		        
		        if(null != tableIdIter){
			        while(tableIdIter.hasNext()){
			            tableID = tableIdIter.next();
			            ofTable = flowTableMap.get(tableID);
			            tableList.add(ofTable);
			        }
			        
			        //return the sorted flow table list
			        flowTableIdComp.setSwitchID(switchId);
			        Collections.sort(tableList, flowTableIdComp);
		        }
	        }
    	}catch (Exception e) {
            e.printStackTrace();            
        }   

        return tableList;
    }

    @Override
    public OFFlowTable iGetFlowTable(int switchId, byte globalTableId) {
    	if(null == database){
    		return null;
    	}
    	
        return database.iGetFlowTable(switchId, globalTableId);
    }
    
    @Override
    public boolean iDelEmptyFlowTable(int switchId, byte globalTableId){       
    	return iDelEmptyFlowTable(switchId, globalTableId, true);
    }
    
	private boolean iDelEmptyFlowTable(int switchId, byte globalTableId, boolean writeToSwitch) {
        if(null == database){
            return true;
        }
        try{
            Map<Integer, OFFlowMod> flowEntryMap = database.iGetFlowEntriesMap(switchId, globalTableId);
            if(null != flowEntryMap && flowEntryMap.size() != 0){
            	return false;
            }
            
            OFFlowTable ofTable = iGetFlowTable(switchId, globalTableId);
            
            if(ofTable == null){
            	return true;
            }
            
            ofTable = ofTable.clone();
            //del database
            database.iDelFlowTable(switchId, ofTable.getTableType().getValue(), globalTableId);
            
            if(true == writeToSwitch){
	            //write the delete OFFlowTable
	            ofTable.setCommand(OFTableModCmd.OFPTC_DELETE);
	            
	            OFTableMod tableMod = (OFTableMod)(factory.getOFMessage(OFType.TABLE_MOD));
	            tableMod.setFlowTable(ofTable);	
	            
	            writeOf(switchId, tableMod);
	            //for roll back
	            this.iAddSendedOFMessage(switchId, tableMod);
            }
            
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
        return true;
		
	}
    
    @Override
    public void iDelFlowTableAndAllSubEntries(int switchId, byte globalTableId){       
        delFlowTableAndAllSubEntries(switchId, globalTableId, true);
    }
    
    private void delFlowTableAndAllSubEntries(int switchId, byte globalTableId, boolean writeToSwitch){
        if(null == database){
            return;
        }
        try{
            int flowID;
            OFFlowMod flowEntry;  
            
            Map<Integer, OFFlowMod> flowEntryMap = database.iGetFlowEntriesMap(switchId, globalTableId);
            if(null == flowEntryMap){
            	return;
            }
            Iterator<Integer> tableIdIter = flowEntryMap.keySet().iterator();
            List<Integer> flowIdList = new ArrayList<Integer>();
            //get all flow entries in this table
            while(tableIdIter.hasNext()){
                flowID = tableIdIter.next();
                
                if(true == writeToSwitch){
                	flowEntry = database.iGetFlowEntry(switchId, globalTableId, flowID);
                	if(flowEntry == null){
                		return;
                	}
                	
                	flowEntry = flowEntry.clone();
                	flowEntry.setCommand((byte)OFFlowEntryCmd.OFPFC_DELETE.ordinal());                
                
                	//write the delete OFFlowMod
                	writeOf(switchId, flowEntry);
                }
                flowIdList.add(flowID);
            }
            for(int delflowID: flowIdList){
            	database.iDelFlowEntry(switchId, globalTableId, delflowID);
            }
            
            OFFlowTable ofTable = iGetFlowTable(switchId, globalTableId);
            if(ofTable == null){
            	return;
            }
            
            ofTable = ofTable.clone();
            
            //del database
            database.iDelFlowTable(switchId, ofTable.getTableType().getValue(), globalTableId);
            
            if(true == writeToSwitch){
	            //write the delete OFFlowTable
	            ofTable.setCommand(OFTableModCmd.OFPTC_DELETE);
	            
	            OFTableMod tableMod = (OFTableMod)(factory.getOFMessage(OFType.TABLE_MOD));
	            tableMod.setFlowTable(ofTable);	
	            
	            writeOf(switchId, tableMod);
	            //for roll back
	            this.iAddSendedOFMessage(switchId, tableMod);
            }
            
        }catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
    
    @Override
    public byte iGetFlowTableNumberingBase(int switchId, byte tableType) {
    	if(null == database){
    		return FLOWTABLEID_INVALID;
    	}
    	
        return database.iGetFlowTableNumberingBase(switchId, tableType);
    }
    
    @Override
    public byte parseToSmallTableId(int switchId, byte globalTableId){
    	if(null == database){
    		return FLOWTABLEID_INVALID;
    	}
    	
    	return database.parseToSmallTableId(switchId, globalTableId);
    }
    
    @Override
    public byte parseToGlobalTableId(int switchId, byte tableType, byte smallTableId){
    	if(null == database){
    		return FLOWTABLEID_INVALID;
    	}
    	
    	return database.parseToGlobalTableId(switchId, tableType, smallTableId);
    }
    
    @Override
    public int iAddFlowEntry(int switchId, byte globalTableId,
                            byte matchFieldNum, List<OFMatchX> matchXList,
                            byte instructionNum, List<OFInstruction> instructionList,
                            short priority){
        return addFlowEntry(switchId, globalTableId, matchFieldNum, matchXList, instructionNum, instructionList, priority, true);
    }
    
    private int addFlowEntry(int switchId, byte globalTableId,
                            byte matchFieldNum, List<OFMatchX> matchXList,
                            byte instructionNum, List<OFInstruction> instructionList,
                            short priority,
                            boolean writeToSwitch){
        int flowEntryId;
        try {
        	if(null == database){
        		return FLOWENTRYID_INVALID;
        	}
        	
        	//check the parameters
        	if(false == switches.containsKey(switchId)){
            	return FLOWENTRYID_INVALID;
        	}
        	
        	OFFlowTable flowTable = this.iGetFlowTable(switchId, globalTableId);
        	
        	if(null == flowTable){
        		return FLOWENTRYID_INVALID;
        	}
        	OFTableType tableType = flowTable.getTableType();
        	
        	if((tableType == OFTableType.OF_LINEAR_TABLE && matchFieldNum != 0 && matchXList != null && matchXList.size() != 0)
            		|| (tableType != OFTableType.OF_LINEAR_TABLE && (matchFieldNum == 0 || matchXList == null || matchXList.size() != matchFieldNum))
            		|| instructionNum == 0 || instructionList == null || instructionList.size() != instructionNum){
            	return FLOWENTRYID_INVALID;
        	}
        	
        	if(null != matchXList){
        		int totalFiledLength = 0;
        		for(OFMatchX matchX : matchXList){
        			if(matchX == null){
        				return FLOWENTRYID_INVALID;
        			}
        			totalFiledLength += matchX.getLength();
        		}
        		if(totalFiledLength != flowTable.getKeyLength()){
        			return FLOWENTRYID_INVALID;
        		}
        	}
        	
        	if(0 != matchFieldNum){
            	//check reduplication
            	if(true == checkFlowEntryReduplication(switchId, globalTableId, FLOWENTRYID_INVALID, matchXList)){
            		log.info("flow entry is reduplicate (check matchX value/mask).");
            		return FLOWENTRYID_INVALID;
            	}
        	}

        	
        	//add flow entry
            flowEntryId = database.iAddFlowEntry(switchId, globalTableId, matchFieldNum, matchXList, instructionNum, instructionList, priority);
            if(FLOWENTRYID_INVALID == flowEntryId){
            	return flowEntryId;
            }
            
            if(true == writeToSwitch){
                OFFlowMod flowMod = database.iGetFlowEntry(switchId, globalTableId, flowEntryId);
                if(flowMod == null){
                	return FLOWENTRYID_INVALID;
                }
                flowMod = flowMod.clone();
                flowMod.setCommand((byte)OFFlowEntryCmd.OFPFC_ADD.ordinal());
                writeOf(switchId, flowMod);
                
                //for roll back
                this.iAddSendedOFMessage(switchId, flowMod);
            }
            
        	if(0 != matchFieldNum){
                String keyString = calcMatchKey(matchXList);
                database.putMatchKey(switchId, globalTableId, keyString, flowEntryId);
        	}
        } catch (Exception e) {
            e.printStackTrace();
            flowEntryId = FLOWENTRYID_INVALID;
        }
        
        return flowEntryId;
    }
    
    public static String calcMatchKey(final List<OFMatchX> matchXList){
		int key = 0;
		if(null != matchXList){
			for(final OFMatchX matchX : matchXList){
				key += matchX.hashCode();
			}			
		}
		return String.valueOf(key);
    }
    
	protected boolean checkFlowEntryReduplication(int switchId, byte globalTableId, int flowEntryID, List<OFMatchX> matchXList) {
		try{
			if(null == database){
	            return true;
	        }
			
			String keyString = calcMatchKey(matchXList);
			
			Integer entryIndex = database.getFlowEntryIndexByMatchKey(switchId, globalTableId, keyString);
	        
			if(null == entryIndex){
				return false;
			}
			
			if(flowEntryID != FLOWENTRYID_INVALID && flowEntryID == entryIndex){
				return false;
			}
			
			List<OFMatchX> flowEntryMatchXList = database.iGetFlowEntry(switchId, globalTableId, entryIndex).getMatchList();
			if(matchXList == flowEntryMatchXList){
				return true;
			}
			
			if(null != flowEntryMatchXList){
				if(flowEntryMatchXList.size() == matchXList.size()){
					for(int index = 0; index < matchXList.size(); index++){
						if(!matchXList.get(index).getFullHexValue().equals(flowEntryMatchXList.get(index).getFullHexValue())
								|| !matchXList.get(index).getFullHexMask().equals(flowEntryMatchXList.get(index).getFullHexMask())){
							return false;
						}
					}
					return true;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return false;
	}

	public class FlowEntryGloablIDComparator implements Comparator<OFFlowMod> {
		@Override
		public int compare(OFFlowMod entry1, OFFlowMod entry2) {
			if(entry1 == null || entry2 == null){
				return 0;
			}
			
			int id1 = entry1.getIndex();
			int id2 = entry2.getIndex();
			
			return id1 - id2;
		}
	}
	
	private Comparator<OFFlowMod> flowEntryIDComp = new FlowEntryGloablIDComparator();
	 
    @Override
    public List<OFFlowMod> iGetAllFlowEntry(int switchId, byte globalTableId){
    	List<OFFlowMod> flowEntryList = new ArrayList<OFFlowMod>();
    	
    	try{
	        if(null == database){
	            return null;
	        }
	        
	        Map<Integer, OFFlowMod> flowEntryMap = database.iGetFlowEntriesMap(switchId, globalTableId);
	        
	        if(null != flowEntryMap){
		        Iterator<Integer> tableIdIter = flowEntryMap.keySet().iterator();
		        int flowID;
		        OFFlowMod flowEntry;
		        
		        if(null != tableIdIter){
			        while(tableIdIter.hasNext()){
			            flowID = tableIdIter.next();
			            flowEntry = flowEntryMap.get(flowID);
			            flowEntryList.add(flowEntry);
			        }
			        
			        Collections.sort(flowEntryList, flowEntryIDComp); 
		        }
	        }
    	}catch(Exception e){
    		e.printStackTrace();
    	}
        
        return flowEntryList;
    }
    @Override
    public OFFlowMod iGetFlowEntry(int switchId, byte globalTableId, int flowEntryId){
        if(null == database){
            return null;
        }
        
        return database.iGetFlowEntry(switchId, globalTableId, flowEntryId);  
    }
    @Override
    public boolean iModFlowEntry(int switchId, byte globalTableId, int flowEntryId,
                                byte matchFieldNum, List<OFMatchX> matchXList,
                                byte instructionNum, List<OFInstruction> instructionList,
                                short priority){        
        return modFlowEntry(switchId, globalTableId, flowEntryId, 
	                        matchFieldNum, matchXList, instructionNum, instructionList, priority,
	                        true);
    }
    
    private boolean modFlowEntry(int switchId, byte globalTableId, int flowEntryId,
                                byte matchFieldNum, List<OFMatchX> matchXList,
                                byte instructionNum, List<OFInstruction> instructionList,
                                short priority,
                                boolean writeToSwitch){        
        try {
            if(null == database){
                return false;
            }
            
        	//check the parameters
        	if(false == switches.containsKey(switchId)){
            	return false;
        	}
        	
        	OFFlowTable flowTable = this.iGetFlowTable(switchId, globalTableId);

        	if(null == flowTable){
        		return false;
        	}
        		
        	OFTableType tableType = flowTable.getTableType();
        	
        	if((tableType == OFTableType.OF_LINEAR_TABLE && matchFieldNum != 0 && matchXList != null && matchXList.size() != 0)
            		|| (tableType != OFTableType.OF_LINEAR_TABLE && (matchFieldNum == 0 || matchXList == null || matchXList.size() != matchFieldNum))
            		|| instructionNum == 0 || instructionList == null || instructionList.size() != instructionNum){
            	return false;
        	}
        	
        	if(null != matchXList){
        		int totalFiledLength = 0;
        		for(OFMatchX matchX : matchXList){
        			if(matchX == null){
        				return false;
        			}
        			totalFiledLength += matchX.getLength();
        		}
        		if(totalFiledLength != flowTable.getKeyLength()){
        			return false;
        		}
        	}
        	
        	OFFlowMod oldFlowMod = database.iGetFlowEntry(switchId, globalTableId, flowEntryId);
        	if(null == oldFlowMod){
        		return false;
        	}
        	oldFlowMod = oldFlowMod.clone();
        	
        	if(0 != matchFieldNum){
            	//check reduplication
            	if(true == checkFlowEntryReduplication(switchId, globalTableId, oldFlowMod.getIndex(), matchXList)){
            		log.info("flow entry is reduplicate (check matchX value/mask).");
            		return false;
            	}
        	}
        	
        	//modify entry
            database.iModFlowEntry(switchId, globalTableId, flowEntryId, 
                                    matchFieldNum, matchXList, instructionNum, instructionList, priority);
            
            if(true == writeToSwitch){
                OFFlowMod flowMod = database.iGetFlowEntry(switchId, globalTableId, flowEntryId);
            	if(null == flowMod){
            		return false;
            	}
            	flowMod = flowMod.clone();
                flowMod.setCommand((byte)OFFlowEntryCmd.OFPFC_MODIFY.ordinal());
                
                writeOf(switchId, flowMod); 
                
                //for roll back
                this.iAddSendedOFMessage(switchId, flowMod);            
                this.addOldBackupOFMessage(switchId, flowMod.getXid(), oldFlowMod);
            }
            
        	if(0 != matchFieldNum){
                String keyString = calcMatchKey(matchXList);
                database.putMatchKey(switchId, globalTableId, keyString, flowEntryId);
        	}
            
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public void iDelFlowEntry(int switchId, byte globalTableId, int index){
        delFlowEntry(switchId, globalTableId, index, true);
    }
    
    private OFFlowMod delFlowEntry(int switchId, byte globalTableId, int index, boolean writeToSwitch){ 
        OFFlowMod flowMod;
        try {
            if(null == database){
                return null;
            }
            
            flowMod = database.iGetFlowEntry(switchId, globalTableId, index);
            if(flowMod == null){
            	return null;
            }
            
            flowMod = flowMod.clone();
            
            //this.iResetCounter(switchId, flowMod.getCounterId(), writeToSwitch);
            
            database.iDelFlowEntry(switchId, globalTableId, index);
            
            if(true == writeToSwitch){
                flowMod.setCommand((byte)OFFlowEntryCmd.OFPFC_DELETE.ordinal());
                
                writeOf(switchId, flowMod);
                
                //for roll back
                this.iAddSendedOFMessage(switchId, flowMod);
            }
            
        	if(0 != flowMod.getMatchFieldNum()){
        		String keyString = calcMatchKey(flowMod.getMatchList());
        		database.deleteMatchKey(switchId, globalTableId, keyString);
        	}
        }catch (Exception e) {
            e.printStackTrace();
            flowMod = null;
        }
        
        return flowMod;
    }
    
    /**
     * Will be deleted below @Deprecated methods.
     */
    @Override
    @Deprecated
    public OFInstruction iNewInstructionGotoTable(int switchId, byte nextGlobalTableId, short packetOffset) {
        if(null == database){
            return null;
        }
        
        OFFlowTable nextFlowTable = database.iGetFlowTable(switchId, nextGlobalTableId);
        if(null != nextFlowTable){
	        byte matchFieldNum = nextFlowTable.getMatchFieldNum();
	        
	        List<OFMatch20> matchFieldList =  nextFlowTable.getMatchFieldList();
	        
	        OFInstructionGotoTable instruction = new OFInstructionGotoTable();
	        instruction.setNextTableId(nextGlobalTableId);
	        instruction.setMatchFieldNum(matchFieldNum);
	        instruction.setPacketOffset(packetOffset);
	        instruction.setMatchList(matchFieldList);
	        
	        return instruction;
        }

        return null;
    }

    @Override
    @Deprecated
    public OFInstruction iNewInstructionGotoDirectTable(byte nextGlobalTableId, int tableEntryIndex, short packetOffset){
        OFInstructionGotoDirectTable instruction = new OFInstructionGotoDirectTable();
        instruction.setNextTableId(nextGlobalTableId);
        instruction.setTableEntryIndex(tableEntryIndex);
        instruction.setPacketOffset(packetOffset);
        
        return instruction;
    }
    
    @Override
    @Deprecated
    public OFInstruction iNewInstructionMeter(int meterId){
        OFInstructionMeter instruction = new OFInstructionMeter();
        instruction.setMeterId(meterId);
        
        return instruction;
    }
    
    @Override
    @Deprecated
    public OFInstruction iNewInstructionWriteMetadata(short metadataOffset, short writeLength, int value){
        OFInstructionWriteMetadata instruction = new OFInstructionWriteMetadata();
        instruction.setMetadataOffset(metadataOffset);
        instruction.setWriteLength(writeLength);
        instruction.setValue(value);
        
        return instruction;
    }
    
    @Override
    @Deprecated
    public OFInstruction iNewInstructionWriteMetadataFromPacket(short metadataOffset, short packetOffset, short writeLength){
        OFInstructionWriteMetadataFromPacket instruction = new OFInstructionWriteMetadataFromPacket();
        instruction.setMetadataOffset(metadataOffset);
        instruction.setPacketOffset(packetOffset);
        instruction.setWriteLength(writeLength);
        
        return instruction;
    }
    
    @Override 
    @Deprecated
    public OFInstruction iNewInstructionApplyActions(byte actionNum, List<OFAction> actionList){
        OFInstructionApplyActions instruction = new OFInstructionApplyActions();
        instruction.setActionNum(actionNum);
        instruction.setActionFactory(factory);
        instruction.setActionList(actionList);
        
        return instruction;
    }
    
    @Override
    @Deprecated
    public OFAction iNewActionSetField(OFMatchX fieldSetting){
        OFActionSetField action = new OFActionSetField();
        action.setFieldSetting(fieldSetting);
        
        return action;        
    }
    
    @Override
    @Deprecated
    public OFAction iNewActionSetFieldFromMetadata(OFMatch20 fieldSetting, short metadataOffset){
        OFActionSetFieldFromMetadata action  = new OFActionSetFieldFromMetadata();
        action.setFieldSetting(fieldSetting);
        action.setMetadataOffset(metadataOffset);
        
        return action;
    }
    
    @Override
    @Deprecated
    public OFAction iNewActionModifyField(OFMatch20 fieldMatch, int increment){
        OFActionModifyField action = new OFActionModifyField();
        action.setMatchField(fieldMatch);
        action.setIncrement(increment);
        
        return action;        
    }
    
    @Override
    @Deprecated
    public OFAction iNewActionAddField(short fieldId, short fieldPosition, int fieldLength, long fieldValue){
        OFActionAddField action = new OFActionAddField();
        action.setFieldId(fieldId);
        action.setFieldPosition(fieldPosition);
        action.setFieldLength(fieldLength);
        action.setFieldValue(fieldValue);
        
        return action;
    }
    
    @Override
    @Deprecated
    public OFAction iNewActionDeleteField(short fieldPosition, int fieldLength){
        OFActionDeleteField action = new OFActionDeleteField();
        action.setFieldPosition(fieldPosition);
        action.setFieldLength(fieldLength);
        
        return action;
    }
    
    @Override
    @Deprecated
    public OFAction iNewActionOutput(int outputPortId, short metadataOffset, short metadataLength, short packetOffset){
        OFActionOutput action = new OFActionOutput();
        action.setPortId(outputPortId);
        action.setMetadataOffset(metadataOffset);
        action.setMetadataLength(metadataLength);
        action.setPacketOffset(packetOffset);
        
        return action;
    }
    
    @Override
    @Deprecated
    public OFAction iNewActionCalculateCheckSum(short checksumPosition, short checksumLength, short calcStartPosition, short calcLength){
        OFActionCalculateCheckSum action = new OFActionCalculateCheckSum();
        action.setChecksumPosition(checksumPosition);
        action.setChecksumLength(checksumLength);
        action.setCalcStartPosition(calcStartPosition);
        action.setCalcLength(calcLength);
        
        return action;
    }
    
    @Override
    @Deprecated
    public OFAction iNewActionCounter(int counterId){
        OFActionCounter action = new OFActionCounter();
        action.setCounterId(counterId);
        
        return action;
    }
    
    @Override
    @Deprecated
    public OFAction iNewActionGroup(int groupId){
        OFActionGroup action = new OFActionGroup();
        action.setGroupId(groupId);
        
        return action;
    }
    
    @Override
    @Deprecated
    public OFAction iNewActionPacketIn(int reason){
        OFActionPacketIn action = new OFActionPacketIn();
        action.setReason(reason);
        
        return action;
    }
    
    @Override
    @Deprecated
    public OFAction iNewActionDrop(int reason){
        OFActionDrop action = new OFActionDrop();
        action.setReason(reason);
        
        return action;
    }    
    /**
     * Will be deleted above @Deprecated methods.
     */
    
    
    @Override
    public OFMatch20 iGetMatchField(short fieldId){
        if(null == database){
            return null;
        }
        return database.iGetMatchField(fieldId);
    }
    @Override
    public List<OFMatch20> iGetAllField(){
        if(null == database){
            return null;
        }
        return database.iGetAllField();
    }
    @Override
    public OFMatchX iNewMatchX(short fieldId, byte[] value, byte[] mask){
        if(null == database){
            return null;
        }
        OFMatch20 match = iGetMatchField(fieldId);
        if(value == null || mask == null || match == null){
        	return null;
        }
        return iNewMatchX(match, value, mask);
    }
    @Override
    public OFMatchX iNewMatchX(OFMatch20 match, byte[] value, byte[] mask){
        if(value == null || mask == null || match == null){
        	return null;
        }
        return new OFMatchX(match, value, mask);
    }
    
    @Override
    public OFInstruction iGetInstruction(int switchId, byte globalTableId, int flowEntryId,
                                            int instructionIndex){
        OFFlowMod flowEntry = iGetFlowEntry(switchId, globalTableId, flowEntryId);
        if(flowEntry == null){
        	return null;
        }
        return iGetInstruction(flowEntry, instructionIndex);        
    }
    @Override
    public OFInstruction iGetInstruction(OFFlowMod flowEntry, int instructionIndex){
        if(flowEntry == null || flowEntry.getInstructionList() == null){
        	return null;
        }
        return flowEntry.getInstructionList().get(instructionIndex);        
    }
    @Override
    public OFAction iGetActionFromInstruction(int switchId, byte globalTableId, int flowEntryId,
                                                int instructionIndex, int actionIndex){
        OFInstruction instruction = iGetInstruction(switchId, globalTableId, flowEntryId, instructionIndex);
        if(instruction == null){
        	return null;
        }
        
        if(instruction instanceof OFInstructionApplyActions){
            OFInstructionApplyActions instructionApplyActions = (OFInstructionApplyActions)instruction;
            List<OFAction> actionList = instructionApplyActions.getActionList();
            if(actionList == null){
            	return null;
            }
            return actionList.get(actionIndex);
        }
        return null;
    }
    @Override
    public OFAction iGetActionFromGroup(int switchId, int groupId, int actionIndex){
        OFGroupMod groupMod = iGetGroupEntry(switchId, groupId);
        if(groupMod == null){
        	return null;
        }
        
        List<OFAction> actionList = groupMod.getActionList();
        if(actionList == null){
        	return null;
        }
        
        return actionList.get(actionIndex);
    }
    
    @Override
    public long iQueryCounterValue(int switchId, int counterId){
    	long value = COUNTERID_INVALID;
    	IOFSwitch sw = null;
    	
        try {
	    	if(database == null){
	    		return COUNTERID_INVALID;
	    	}
	    	
	    	if( null == database.iGetCounter(switchId, counterId)){
	    		return COUNTERID_INVALID;
	    	}
	        List<OFCounterReply> counterReplyList;
	        
	        sw = switches.get(switchId);
	        
	        OFCounterRequest counterReq = (OFCounterRequest)factory.getOFMessage(OFType.COUNTER_REQUEST);
	        counterReq.getCounter().setCounterId(counterId);
	        counterReq.getCounter().setCommand(OFCounterModCmd.OFPCC_QUERY);
	
	        writeOf(switchId, counterReq);
	       
	        OFCounterFuture future = new OFCounterFuture(threadPool, sw, counterReq.getXid());        
	        this.counterFutureMap.put(counterReq.getXid(), future);
        

            counterReplyList = future.get(2, TimeUnit.SECONDS);
            if(counterReplyList == null || counterReplyList.size() == 0){
                value = 0;
            }else{
                value = counterReplyList.get(0).getCounter().getValue();
            }
            
            //save to database;
            database.iGetCounter(switchId, counterId).setValue(value);
            
        } catch (Exception e) {
            log.error("Failure retrieving counter from switch {}", sw);
        } 
        
        return value;
    }
    
    @Override
    public void processCounterReply(IOFSwitch sw, OFCounterReply counterReply){
    	if(sw == null || counterReply == null){
    		return;
    	}
    	
        OFCounterFuture future = this.counterFutureMap.get(counterReply.getXid());

        if(future != null){
            future.deliverFuture(sw, counterReply);
        }else{
            log.error("CounterReply[id={},cid={}] NOT find the mached request, drop.", counterReply.getXid(), counterReply.getCounter().getCounterId());
        }
    }

    @Override
    public int iAllocateCounter(int switchId){
    	if(database == null){
    		return COUNTERID_INVALID;
    	}
    	
        return database.iAllocateCounter(switchId);
    }
    @Override
    public OFCounter iFreeCounter(int switchId, int counterId){
    	if(database == null){
    		return null;
    	}
    	
        return database.iFreeCounter(switchId, counterId);
    }
    @Override
    public boolean iResetCounter(int switchId, int counterId, boolean writeToSwitch) {
        if(database == null || false == database.iResetCounter(switchId, counterId) ){
        	return false;
        }
        
        if(true == writeToSwitch){
        	OFCounter counter = database.iGetCounter(switchId, counterId);
        	if(counter != null){
	            OFCounterMod counterMod = (OFCounterMod)factory.getOFMessage(OFType.COUNTER_MOD);
	            counter.setCommand(OFCounterModCmd.OFPCC_CLEAR);
	            counterMod.setCounter(counter);
	            
	            writeOf(switchId, counterMod);
        	}else{
        		return false;
        	}
        }
        return true;
    }
    @Override
    public int iAddMeterEntry(int switchId, short rate){
    	if(database == null){
    		return METER_INVALID;
    	}
    	
        int meterId = database.iAddMeterEntry(switchId, rate);
        
        OFMeterMod meterMod = iGetMeter(switchId, meterId);
        if(meterMod != null){
	        meterMod.setCommand(OFMeterModCmd.OFPMC_ADD);
	        
	        writeOf(switchId, meterMod);
        }
        
        return meterId;
    }
    @Override
    public OFMeterMod iFreeMeter(int switchId, int meterId){
    	if(database == null){
    		return null;
    	}
    	
        OFMeterMod meterMod = database.iFreeMeter(switchId, meterId);
        
        if(meterMod != null){
	        meterMod.setCommand(OFMeterModCmd.OFPMC_DELETE);
	        
	        writeOf(switchId, meterMod);
        }
        
        return meterMod;
    }
    @Override
    public OFMeterMod iGetMeter(int switchId, int meterId){
    	if(database == null){
    		return null;
    	}
    	
        return database.iGetMeter(switchId, meterId);
    }
    
    @Override
    public List<OFMeterMod> iGetAllMeters(int switchId){
    	if(database == null){
    		return null;
    	}
    	
        return database.iGetAllMeters(switchId);
    }
    
    @Override
    public boolean iModifyMeter(int switchId, int meterId, short rate) {
        if(database == null || false == database.iModifyMeter(switchId, meterId, rate)){
        	return false;
        }
        
        OFMeterMod meterMod = iGetMeter(switchId, meterId);
        
        if(meterMod != null){
	        meterMod.setCommand(OFMeterModCmd.OFPMC_MODIFY);
	        
	        writeOf(switchId, meterMod);
	        
	        return true;
        }
        
        return false;
    }
    @Override
    public int iAddGroupEntry(int switchId, byte groupType, byte actionNum, List<OFAction> actionList){
        return addGroupEntry(switchId, groupType, actionNum, actionList, true);
    }
    
    private int addGroupEntry(int switchId, byte groupType, byte actionNum, List<OFAction> actionList, boolean writeToSwitch){
        int groupId;
        try{
        	if(database == null){
        		return GROUPID_INVALID;
        	}
        	
            groupId = database.iAddGroupEntry(switchId, groupType, actionNum, actionList);
            
            if(true == writeToSwitch){
                OFGroupMod groupMod = iGetGroupEntry(switchId, groupId);
                if(groupMod == null){
                	return GROUPID_INVALID;
                }
                groupMod = groupMod.clone();
                groupMod.setCommand(OFGroupModCmd.OFPGC_ADD);
                
                writeOf(switchId, groupMod);
                
                //for roll back
                this.iAddSendedOFMessage(switchId, groupMod);
            }
            
        }catch (Exception e) {
            e.printStackTrace();
            groupId = GROUPID_INVALID;
        }
        
        return groupId;
    }
    @Override
    public OFGroupMod iFreeGroupEntry(int switchId, int groupId){       
        return freeGroupEntry(switchId, groupId, true);
    }
    
    private OFGroupMod freeGroupEntry(int switchId, int groupId, boolean writeToSwitch){
        OFGroupMod groupMod = null;
        try{
        	if(database == null){
        		return null;
        	}
        	
            groupMod = database.iFreeGroupEntry(switchId, groupId);
            if(groupMod == null){
            	return null;
            }
            groupMod = groupMod.clone();
            
            if(true == writeToSwitch){            
                groupMod.setCommand(OFGroupModCmd.OFPGC_DELETE);
                writeOf(switchId, groupMod);
                
                //for roll back
                this.iAddSendedOFMessage(switchId, groupMod);
            }
            
        }catch (Exception e) {
            e.printStackTrace();
        }
        
        return groupMod;
    }
    @Override
    public OFGroupMod iGetGroupEntry(int switchId, int groupId){
    	if(database == null){
    		return null;
    	}
        return database.iGetGroupEntry(switchId, groupId);
    }
    @Override
    public boolean iModifyGroupEntry(int switchId, int groupId, byte groupType, byte actionNum, List<OFAction> actionList){
        return modifyGroupEntry(switchId, groupId, groupType, actionNum, actionList, true);
    }
    
    private boolean modifyGroupEntry(int switchId, int groupId, 
                                    byte groupType, byte actionNum, List<OFAction> actionList,
                                    boolean writeToSwitch){
        try{
        	if(database == null){
        		return false;
        	}
        	
        	if(false == switches.containsKey(switchId)){
            	return false;
        	}
        	if(groupId == GROUPID_INVALID){
        		return false;
        	}
        	
        	if(actionNum == 0){
        		if(actionList != null && actionList.size() != 0){
        			return false;
        		}
        	}else{
        		if(actionList == null || actionList.size() != actionNum){
        			return false;
        		}
        	}
        	
            OFGroupMod oldGroupMod = database.iGetGroupEntry(switchId, groupId);
            if(oldGroupMod == null){
            	return false;
            }
            oldGroupMod = oldGroupMod.clone();
            
            database.iModifyGroupEntry(switchId, groupId, groupType, actionNum, actionList);
            
            if(true == writeToSwitch){
                OFGroupMod groupMod = iGetGroupEntry(switchId, groupId);
                if(groupMod == null){
                	return false;
                }
                groupMod = groupMod.clone();
                groupMod.setCommand(OFGroupModCmd.OFPGC_MODIFY);
                
                writeOf(switchId, groupMod);
                
                //for roll back
                this.iAddSendedOFMessage(switchId, groupMod);                
                this.addOldBackupOFMessage(switchId, groupMod.getXid(), oldGroupMod);
            }
            
            return true;
            
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public List<OFGroupMod> iGetAllGroups(int switchId){
    	if(database == null){
    		return null;
    	}
    	
        return database.iGetAllGroups(switchId);
    }
    
    @Override
    public void iLogOFMessageIn(int switchId, OFMessage message){
    	if(database == null || message == null){
    		return ;
    	}
        database.iLogOFMessageIn(switchId, message);
    }
    @Override
    public void iLogErrorMessageIn(int switchId, OFError errMessage){
    	if(database == null || errMessage == null){
    		return ;
    	}
    	
        database.iLogOFMessageIn(switchId, errMessage.getType(), errMessage);
    }
    @Override
    public void iLogPacketInMessageIn(int switchId, OFPacketIn packetIn){
    	if(database == null || packetIn == null){
    		return ;
    	}
        database.iLogOFMessageIn(switchId, packetIn.getType(), packetIn);
    }
    @Override
    public void iLogOFMessageOut(int switchId, OFMessage message){
    	if(database == null || message == null){
    		return ;
    	}
        database.iLogOFMessageOut(switchId, message);
    }
    @Override
    public void iLogErrorMessageOut(int switchId, OFError errMessage){
    	if(database == null || errMessage == null){
    		return ;
    	}
        database.iLogOFMessageOut(switchId, errMessage.getType(), errMessage);
    }
    @Override
    public void iLogPacketInMessageOut(int switchId, OFPacketIn packetIn){
    	if(database == null || packetIn == null){
    		return ;
    	}
        database.iLogOFMessageOut(switchId, packetIn.getType(), packetIn);
    }
    
    @Override
    public int iGetFlowEntryNumber(int switchId, byte globalTableId){
        if(null == database){
            return 0;
        }
        if(null == database.iGetFlowTable(switchId, globalTableId)){
            return 0;
        }
        Map<Integer, OFFlowMod> flowEntryMap = database.iGetFlowEntriesMap(switchId, globalTableId);
        if(null == flowEntryMap){
            return 0;
        }
        
        return flowEntryMap.size();
    }
    @Override
    public int iGetTableNumber(int switchId, OFTableType tableType){
        if(null == database){
            return 0;
        }
        return database.iGetTableNumber(switchId, tableType);
    }
    @Override
    public int iGetUsedCounterNumber(int switchId){
        if(null == database){
            return 0;
        }
        return database.iGetUsedCounterNumber(switchId);
    }
    @Override
    public int iGetUsedGroupNumber(int switchId){
        if(null == database){
            return 0;
        }
        return database.iGetUsedGroupNumber(switchId);
    }
    @Override
    public int iGetUsedMeterNumber(int switchId){
        if(null == database){
            return 0;
        }
        return database.iGetUsedMeterNumber(switchId);
    }
    @Override
    public int iGetAllTableNumber(int switchId){
        if(null == database){
            return 0;
        }
        return database.iGetAllTableNumber(switchId);
    }
    
    
    @Override
    public void iAddSendedOFMessage(int switchId, OFMessage message){
        if(null == database){
            return ;
        }
        //log.debug("AddSendedOFMsg: [T=" + message.getType() + ", xid=" +message.getXid() + "]");
        database.iAddSendedOFMessage(switchId, message);
    }
    @Override
    public Queue<OFMessage> iGetSendedOFMessageQueue(int switchId){
        if(null == database){
            return null;
        }
        return database.iGetSendedOFMessageQueue(switchId);
    }
    
    @Override
    public OFMessage iGetSendedOFMessage(int switchId, int xid){
        if(null == database){
            return null;
        }
        return database.iGetSendedOFMessage(switchId, xid);
    }
    
    @Override
    public void iDelSendedOFMessage(int switchId, OFMessage message){
        if(null == database){
            return ;
        }
        database.iDelSendedOFMessage(switchId, message);
    }
    
    private void addOldBackupOFMessage(int switchId, int sended_msg_xid, OFMessage oldMsg){
        if(null == database){
            return ;
        }
        database.iAddOldBackupOFMessage(switchId, sended_msg_xid, oldMsg);
    }
    
    private OFMessage getOldBackupOFMessage(int switchId, int sended_msg_xid){
        if(null == database){
            return null;
        }
        return database.iGetOldBackupOFMessage(switchId, sended_msg_xid);
    }
    
    
    //interface to OFMessageBypassManager
    @Override
    public void iSetFeatures(int switchId, OFFeaturesReply featureReply){
        if(null == database){
            return ;
        }
        
        database.iSetFeatures(switchId, featureReply);
        
        if(null != gui){
        	gui.displayDeviceInfo(switchId, featureReply);
        }
    }
    @Override
    public OFFeaturesReply iGetFeature(int switchId){
        if(null == database){
            return null;
        }
    	return database.iGetFeatures(switchId);
    }
    
    @Override
    public void iSetPortStatus(int switchId, OFPortStatus portStatus){
        if(null == database){
            return ;
        }
        database.iSetPortStatus(switchId, portStatus);
        
        if(null != gui){
        	gui.displayPortInfo(switchId, portStatus);
        }
    }
    
	@Override
	public OFPortStatus iGetPortStatus(int switchId, int portId) {
        if(null == database){
            return null;
        }
		return database.getSwitchDatabase(switchId).getPort(portId);
	}
	
    @Override
    public void iSetResourceReport(int switchId, OFFlowTableResource flowTableResource){
        if(null == database){
            return ;
        }
        database.iSetResourceReport(switchId, flowTableResource);
        
        if(null != gui){
        	gui.displayResourceReport(switchId, flowTableResource);
        }
    }
    
    @Override
    public OFFlowTableResource iGetResourceReport(int switchId){
        if(null == database){
            return null;
        }
        return database.iGetResourceReport(switchId);
    }
    
    @Override
    public void iProcessOFError(int switchId, OFError msg) {
        if(null == database){
            return ;
        }
        
        this.iLogErrorMessageIn(switchId, msg);
        
        if(null != gui){
        	gui.displayOFError(switchId, msg);
        }
        
        //roll back if get the error
        this.rollBack(switchId, msg.getXid());
    }

    @Override
    public void iProcessOFPacketIn(int switchId, OFPacketIn msg) {
        if(null == database){
            return ;
        }
        
        this.iLogPacketInMessageIn(switchId, msg);
        
        if(null != gui){
        	gui.displayPacketIn(switchId, msg);
        }
    }
    
    private void rollBack(int switchId, int xid){
        //TODO need more tests for the roll back process
    	
    	try{
	        if(null == database){
	            return ;
	        }
	        
	        OFMessage sendedMsg = this.iGetSendedOFMessage(switchId, xid);
	        if(null == sendedMsg){
	            log.error("RollBack fail! SendedMsg(xid=" + xid + ") is missing.");
	            return;
	        }
	        if(null != sendedMsg){
	            switch(sendedMsg.getType()){
	                case TABLE_MOD:
	                    rollBack(switchId, (OFTableMod)sendedMsg);
	                    break;
	                case FLOW_MOD:
	                    rollBack(switchId, (OFFlowMod)sendedMsg);
	                    break;
	                case GROUP_MOD:
	                    rollBack(switchId, (OFGroupMod)sendedMsg);
	                    break;
	                default:
	                    log.info("Unhandled rollback ofType: " + sendedMsg.getType());
	                    return;
	            }
	            
	            if(null != gui){
	            	gui.rollBack(switchId, sendedMsg);
	            }
	        }
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    private void rollBack(int switchId, OFTableMod sendedMsg) {
        switch(sendedMsg.getFlowTable().getCommand()){
            case OFPTC_ADD:
        		byte globaltableID = parseToGlobalTableId(switchId, sendedMsg.getFlowTable().getTableType().getValue(), sendedMsg.getFlowTable().getTableId());

                this.delFlowTableAndAllSubEntries(switchId, globaltableID, false);
                break;
            case OFPTC_MODIFY:
            case OFPTC_DELETE:  //TODO roll back from delete table is too complex, do not support yet..
            default:
                log.info("Unhandled table rollback ofType: " + sendedMsg.getFlowTable().getCommand());
                break;
        }        
    }
    
    private void rollBack(int switchId, OFFlowMod sendedMsg) {
    	byte globaltableID = parseToGlobalTableId(switchId, sendedMsg.getTableType().getValue(), sendedMsg.getTableId());
        switch(OFFlowMod.OFFlowEntryCmd.values()[ sendedMsg.getCommand() ]){
            case OFPFC_ADD:
                this.delFlowEntry(switchId, globaltableID, sendedMsg.getIndex(), false);
                break;
            case OFPFC_MODIFY:
                OFFlowMod oldFlowModMsg = (OFFlowMod)getOldBackupOFMessage(switchId, sendedMsg.getXid());
                if(null == oldFlowModMsg){
                    log.error("RollBack OFFlowMod fail! OldBackupMsg(xid=" + sendedMsg.getXid() + ") is missing.");
                }
                this.modFlowEntry(switchId, 
                					globaltableID, 
                                    oldFlowModMsg.getIndex(),
                                    oldFlowModMsg.getMatchFieldNum(),
                                    oldFlowModMsg.getMatchList(),
                                    oldFlowModMsg.getInstructionNum(),
                                    oldFlowModMsg.getInstructionList(),
                                    oldFlowModMsg.getPriority(),
                                    false);
                break;
            case OFPFC_DELETE:
                this.addFlowEntry(switchId, 
                					globaltableID, 
                                    sendedMsg.getMatchFieldNum(),
                                    sendedMsg.getMatchList(),
                                    sendedMsg.getInstructionNum(),
                                    sendedMsg.getInstructionList(),
                                    sendedMsg.getPriority(),
                                    false);
                break;
            case OFPFC_MODIFY_STRICT:
            case OFPFC_DELETE_STRICT:
            default:
                log.info("Unhandled flow rollback ofType: " + OFFlowMod.OFFlowEntryCmd.values()[ sendedMsg.getCommand() ]);
                break;                
        }        
    }


    
    private void rollBack(int switchId, OFGroupMod sendedMsg) {
        switch(OFGroupModCmd.values()[sendedMsg.getCommand()]){
            case OFPGC_ADD:
                this.freeGroupEntry(switchId, sendedMsg.getGroupId(), false);
                break;
            case OFPGC_MODIFY:
                OFGroupMod oldGroupModMsg = (OFGroupMod)getOldBackupOFMessage(switchId, sendedMsg.getXid());
                if(null == oldGroupModMsg){
                    log.error("RollBack OFGroupMod fail! OldBackupMsg(xid=" + sendedMsg.getXid() + ") is missing.");
                }
                this.modifyGroupEntry(switchId, oldGroupModMsg.getGroupId(), 
                                        oldGroupModMsg.getGroupType(),
                                        oldGroupModMsg.getActionNum(),
                                        oldGroupModMsg.getActionList(),
                                        false);
                break;
            case OFPGC_DELETE:
                this.addGroupEntry(switchId, 
                                    sendedMsg.getGroupType(),
                                    sendedMsg.getActionNum(),
                                    sendedMsg.getActionList(),
                                    false);
                break;
            default:
                log.info("Unhandled group rollback ofType: " + sendedMsg.getCommand());
                break;
        }
        
    }
    
    @Override
    public boolean saveAllDataIntoFile(String fileName){
        File file = null;
        FileOutputStream out = null;
        boolean ret = false;
        if(null == fileName){
            fileName = PofManager.DEFAULT_SAVE_FILE_NAME;
        }
        
        try{
            file = new File(fileName);
            if(false == file.exists()){
                file.createNewFile();
            }
            if(false == file.canWrite()){
                log.error("file {} can NOT write.", fileName);
                return false;
            }
            out = new FileOutputStream(file);
            
            //write metadataList;
            ret = this.saveMetadataIntoFile(out);
            
            if(true == ret){
            	//write all infomation in database;
            	ret = database.saveAllDataIntoFile(out);
            }
            
            out.close();
        }catch (Exception e) {
            e.printStackTrace();
            if(out != null){
                try {
                    out.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            file.deleteOnExit();
            ret = false;
        }
        
        return ret;
    }
    
    private boolean saveMetadataIntoFile(FileOutputStream out) {
        Gson gson = new Gson();
        String string;
        java.lang.reflect.Type type;

        try {
            List<OFMatch20> metadataList = database.iGetMetadata();
            type = new TypeToken<List<OFMatch20>>(){}.getType();
            string = gson.toJson(metadataList, type);
            out.write(string.getBytes());
            out.write('\n');
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } 
        return true;
	}

	@Override
    public boolean loadAllDataFromFile(String fileName){
        File file = null;
        BufferedReader br = null;
        boolean ret = false;
        if(null == fileName){
            fileName = PofManager.DEFAULT_SAVE_FILE_NAME;
        }
        try{
            file = new File(fileName);
            if(false == file.exists()
                    || true == file.isDirectory()
                    || false == file.canRead()){
                log.error("Read file {} fail.", fileName);
                return false;
            }
            br = new BufferedReader(new FileReader(file));
            
            ret = this.loadMetadataFromFile(br);
            if(true == ret){
            	ret = database.loadAllDataFromFile(br);
            }
            
            if(true == ret){
                List<Integer> switchIDList = this.iGetAllSwitchID();
                if(null != switchIDList){
	                for(int switchID: switchIDList){
	                    if(false == switches.containsKey(switchID)){
	                        log.info("Switch[id={}] is not connected, remove from database", switchID);
	                        database.removeSwitchDatabase(switchID);
	                    }
	                }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            ret = false;
        }finally{
            try {
            	if(null != br){
            		br.close();
            	}
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return ret;
    }
    
    private boolean loadMetadataFromFile(BufferedReader br) {
        Gson gson = new Gson();
        String lineString;
        java.lang.reflect.Type type;
        
        try {
            lineString = br.readLine();
            type = new TypeToken<ArrayList<OFMatch20>>(){}.getType();
            List<OFMatch20> metadataList = gson.fromJson(lineString, type);
            
            database.iModifyMetadata(metadataList);
            
        }catch (Exception e) {
        	database.iGetMetadata().clear();
            
            e.printStackTrace();
            return false;
        }

        return true;
	}

	@Override
    public boolean sendAllOFMessagesBasedOnDatabase(){
        try{
            PMSwitchDatabase switchDB;
            int switchId;
            byte tableId;
            
            List<OFMeterMod> allMeters;
            List<OFGroupMod> allGroups;
            List<OFFlowTable> allTables;
            List<OFFlowMod> allFlowEntries;
            
            OFTableMod tableMod;
            
            //get all switchDatabse;
            Iterator<PMSwitchDatabase> switchDBIterator =  database.getSwitchDatabaseMap().values().iterator();
            while(switchDBIterator.hasNext()){
                switchDB = switchDBIterator.next();
                switchId = switchDB.getDeviceId();                
                
                //get all ofMeter and write
                allMeters = this.iGetAllMeters(switchId);
                if(null != allMeters && 0 != allMeters.size()){
	                for(OFMeterMod ofMeter : allMeters){
	                    ofMeter.setCommand(OFMeterModCmd.OFPMC_ADD);
	                    
	                    writeOf(switchId, ofMeter); 
	                }
                }
                
                //get all ofGroup and write
                allGroups = this.iGetAllGroups(switchId);
                if(null != allGroups && 0 != allGroups.size()){
	                for(OFGroupMod ofGroup : allGroups){
	                    ofGroup.setCommand(OFGroupModCmd.OFPGC_ADD);
	                    
	                    writeOf(switchId, ofGroup); 
	                }
                }
                
                //get all ofTable in the switch and write
                allTables = this.iGetAllFlowTable(switchId);
                if(null != allTables && 0 != allTables.size()){
	                for(OFFlowTable ofFlowTable : allTables){
	                    ofFlowTable.setCommand(OFTableModCmd.OFPTC_ADD);
	                    
	                    tableMod = (OFTableMod)(factory.getOFMessage(OFType.TABLE_MOD));
	                    tableMod.setFlowTable(ofFlowTable);
	                    
	                    writeOf(switchId, tableMod);            
	                    this.iAddSendedOFMessage(switchId, tableMod);                    
	                }
	                
	                for(OFFlowTable ofFlowTable : allTables){                    
	                    tableId = parseToGlobalTableId(switchId, ofFlowTable.getTableType().getValue(), ofFlowTable.getTableId());
	                    
	                    //get all flowEntries in the table and write
	                    allFlowEntries = this.iGetAllFlowEntry(switchId, tableId);
	                    
	                    if(null != allFlowEntries && 0 != allFlowEntries.size()){
		                    for(OFFlowMod ofFlowMod : allFlowEntries){
		                    	ofFlowMod.setCommand((byte)OFFlowEntryCmd.OFPFC_ADD.ordinal());
		                        writeOf(switchId, ofFlowMod);
		                        this.iAddSendedOFMessage(switchId, ofFlowMod);
		                    }
	                    }
	                }
                }
            }//end while(switchDBIterator.hasNext())
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
	
	public void reloadUI(){
		gui.reloadUI();
	}
}
