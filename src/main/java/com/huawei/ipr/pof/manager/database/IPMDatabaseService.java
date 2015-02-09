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
import java.util.List;
import java.util.Map;
import java.util.Queue;

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
import org.openflow.protocol.OFProtocol;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.instruction.OFInstruction;
import org.openflow.protocol.table.OFFlowTable;
import org.openflow.protocol.table.OFFlowTableResource;
import org.openflow.protocol.table.OFTableType;

/**
 * ISMDatabaseService extends IFloodlightService. 
 * <p>
 * The implementation of ISMDatabaseService should provide database to store
 * all OFMessages and related information used in GUI and POFManager. 
 * <p>
 * POFGUI does not store anything almost, so anything POFGUI displayed has got
 * from database via methods interfaces provided by ISMDatabaseService.
 * 
 * <p>
 * All the methods and parameters are name-self-explained.
 * <p>
 * For the detail of OFMessages/OFInstructions/OFActions,
 * please check POF white paper, OpenFlow documents and the source code.
 * 
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 *
 */
public interface IPMDatabaseService extends IFloodlightService {
    public List<Integer> iGetAllSwitchID();
    
    //port
    public void iSetPortOpenFlowEnable(int deviceId, int portId, byte onoff);
    
    public List<Integer> iGetAllPortId(int switchId);
    
    public OFPortStatus iGetPort(int deviceId, int portId);
    

    //metadata
    public boolean iModifyMetadata(List<OFMatch20> metadataList);
    
    public List<OFMatch20> iGetMetadata();
    
	
    //protocol
    public short iAddProtocol(String protocolName, List<OFMatch20> fieldList);    

	public boolean iModifyProtocol(OFProtocol protocol, List<OFMatch20> fieldList);
	
	public void iDelProtocol(OFProtocol protocol);
	
    public Map<Short, OFProtocol> iGetProtocolMap();    

    public Map<String, Short> iGetProtocolNameMap();
    

    //field
    public short iNewField(String fieldName, short fieldLength, short fieldOffset);
    
	public void iDelField(short fieldId);

	public boolean iModifyField(short fieldId, String fieldName, short fieldLength, short fieldOffset);

    public OFMatch20 iGetMatchField(short fieldId);    

    public List<OFMatch20> iGetAllField();
    
    
    //table
    public byte iAddFlowTable(int switchId, String tableName,
                                byte tableType, short keyLength, int tableSize,
                                byte fieldNum, List<OFMatch20> matchFieldList);
    
    public void iPutFlowTable(int switchId, byte flowTableId, OFFlowTable flowTable);
    
    public OFFlowTable iGetFlowTable(int switchId, byte flowTableId);
    
    public void iDelFlowTable(int switchId, byte tableType, byte globalTableId);
    
    public byte iGetFlowTableNumberingBase(int switchId, byte tableType);
    
    public byte parseToSmallTableId(int switchId, byte globalTableId);

    public byte parseToGlobalTableId(int switchId, byte tableType, byte smallTableId);
 
    public Map<Byte, OFFlowTable> iGetFlowTableMap(int switchId);

    //flow entry
    public int iAddFlowEntry(int switchId, byte globalTableId,
                            byte matchFieldNum, List<OFMatchX> matchList,
                            byte instructionNum, List<OFInstruction> instructionList,
                            short priority);
    
    public OFFlowMod iGetFlowEntry(int switchId, byte globalTableId, int flowEntryId);
    
    public boolean iModFlowEntry(int switchId, byte globalTableId, int flowEntryId,
                                byte matchFieldNum, List<OFMatchX> matchList,
                                byte instructionNum, List<OFInstruction> instructionList,
                                short priority);
    
    public OFFlowMod iDelFlowEntry(int switchId, byte globalTableId, int index);
    
    public Map<Integer, OFFlowMod> iGetFlowEntriesMap(int switchId, byte globalTableId);
    
    //match key for check reduplication
    public void putMatchKey(int switchId, byte globalTableId, String keyString, int entryId);

    public Integer getFlowEntryIndexByMatchKey(int switchId, byte globalTableId, String keyString);

    public void deleteMatchKey(int switchId, byte globalTableId, String keyString);
    
    //counter
    public int iAllocateCounter(int switchId);
    
    public OFCounter iFreeCounter(int switchId, int counterId);
    
    public boolean iResetCounter(int switchId, int counterId);
    
    public OFCounter iGetCounter(int switchId, int counterId);
    
    
    //meter
    public int iAddMeterEntry(int switchId, short rate);
    
    public OFMeterMod iFreeMeter(int switchId, int meterId);
    
    public OFMeterMod iGetMeter(int switchId, int meterId);
    
    public boolean iModifyMeter(int switchId, int meterId, short rate);
    
    public List<OFMeterMod> iGetAllMeters(int switchId);
    
    
    //group
    public int iAddGroupEntry(int switchId, byte groupType, byte actionNum, List<OFAction> actionList);
    
    public OFGroupMod iFreeGroupEntry(int switchId, int groupId);
    
    public OFGroupMod iGetGroupEntry(int switchId, int groupId);
    
    public boolean iModifyGroupEntry(int switchId, int groupId, byte groupType, byte actionNum, List<OFAction> actionList);

    public List<OFGroupMod> iGetAllGroups(int switchId);
    
    
    //feature, resource
    public void iSetFeatures(int switchId, OFFeaturesReply featureReply);

	public OFFeaturesReply iGetFeatures(int switchId);
	
    public void iSetPortStatus(int switchId, OFPortStatus portStatus);

    public OFPortStatus iGetPortStatus(int switchId, int portId);
    
    public void iSetResourceReport(int switchId, OFFlowTableResource flowTableResource);
    
    public OFFlowTableResource iGetResourceReport(int switchId);

    
    //log of_message
    public void iLogOFMessageIn(int switchId, OFMessage message);

    public void iLogOFMessageIn(int switchId, OFType type, OFMessage message);
    
    public void iLogOFMessageOut(int switchId, OFMessage message);

    public void iLogOFMessageOut(int switchId, OFType type, OFMessage message);
    
    public List<OFMessage> iGetLogOFMessageInAll(int switchId);
    
    public List<OFMessage> iGetLogOFMessageOutAll(int switchId);
    
    public List<OFMessage> iGetLogOFMessageIn(int switchId, OFType type);
    
    public List<OFMessage> iGetLogOFMessageOut(int switchId, OFType type);
    
    
    //switch database
    public boolean addSwitchDatabase(int switchId);
    
    public void removeSwitchDatabase(int switchId);
    
    public PMSwitchDatabase getSwitchDatabase(int switchId);

    public Map<Integer, PMSwitchDatabase> getSwitchDatabaseMap();
    


    //get entry number
    public int iGetTableNumber(int switchId, OFTableType tableType);

    public int iGetUsedCounterNumber(int switchId);

    public int iGetUsedGroupNumber(int switchId);

    public int iGetUsedMeterNumber(int switchId);

    public int iGetAllTableNumber(int switchId);
    

    //sended of message (for roll back)
    public void iAddSendedOFMessage(int switchId, OFMessage message);

    public Queue<OFMessage> iGetSendedOFMessageQueue(int switchId);
    
    public OFMessage iGetSendedOFMessage(int switchId, int xid);

    public void iDelSendedOFMessage(int switchId, OFMessage message);
    

    //old backup of message (do roll back)
    public void iDelOldBackupOFMessage(int switchId, int sended_msg_xid);

    public OFMessage iGetOldBackupOFMessage(int switchId, int sended_msg_xid);

    public void iAddOldBackupOFMessage(int switchId, int sended_msg_xid, OFMessage oldMsg);
    

    //save/load 
    public boolean saveAllDataIntoFile(OutputStream out);
    
    public boolean loadAllDataFromFile(BufferedReader br);
}
