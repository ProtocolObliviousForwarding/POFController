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

import java.util.List;
import java.util.Queue;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.IFloodlightService;

import org.jboss.netty.channel.Channel;
import org.openflow.protocol.OFCounter;
import org.openflow.protocol.OFCounterReply;
import org.openflow.protocol.OFError;
import org.openflow.protocol.OFFeaturesReply;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFGroupMod;
import org.openflow.protocol.OFMatch20;
import org.openflow.protocol.OFMatchX;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFMeterMod;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPortStatus;
import org.openflow.protocol.OFProtocol;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionType;
import org.openflow.protocol.instruction.OFInstruction;
import org.openflow.protocol.instruction.OFInstructionType;
import org.openflow.protocol.table.OFFlowTable;
import org.openflow.protocol.table.OFFlowTableResource;
import org.openflow.protocol.table.OFTableType;

/**
 * ISMService extends IFloodlightService. 
 * <p>
 * The implementation of ISMService should be a connector to interconnect with:
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
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 *
 */
public interface IPMService extends IFloodlightService{
	public static final String POF_VERSION = "1.1.7";
	
    public static final int FIRST_ENTRY_TABLE_ID = 0;    
    
    public static final String FIRST_ENTRY_TABLE_NAME = "FirstEntryTable";
    
    public static final String DEFAULT_SAVE_FILE_NAME = "Database.db";
    
    public static final int SWITCHID_INVALID = 0;
    
    public static final short OFPROTOCOLID_INVALID = 0;
    public static final short FIELDID_INVALID = 0;
    
    public static final short OFPROTOCOLID_START = 1;
    public static final short FIELDID_START = 1;
    
    public static final int FLOWENTRYID_INVALID = -1;
    public static final byte FLOWTABLEID_INVALID = -1;
    
    public static final int FLOWENTRYID_START = 0;
    public static final byte FLOWTABLEID_START = 0;
    
    public static final int COUNTERID_INVALID = 0;
    public static final int GROUPID_INVALID = 0;
    public static final int METER_INVALID = 0;
    
    public static final int COUNTERID_START = 1;
    public static final int GROUPID_START = 1;
    public static final int METER_START = 1;
    
	public static final OFInstructionType[] INSTRUCTIONS = {OFInstructionType.GOTO_TABLE,
															OFInstructionType.GOTO_DIRECT_TABLE,
															OFInstructionType.METER,
															OFInstructionType.WRITE_METADATA,
															OFInstructionType.WRITE_METADATA_FROM_PACKET,
															OFInstructionType.APPLY_ACTIONS};
	
	public static final OFActionType[] ACTIONS = {OFActionType.OUTPUT,
													OFActionType.SET_FIELD,
													OFActionType.SET_FIELD_FROM_METADATA,
													OFActionType.MODIFY_FIELD,
													OFActionType.ADD_FIELD,
													OFActionType.DELETE_FIELD,
													OFActionType.CALCULATE_CHECKSUM,
													OFActionType.GROUP,
													OFActionType.DROP,
													OFActionType.PACKET_IN,
													OFActionType.COUNTER};
    
	/**
	 * Add a new switch to POFController.
	 * (Recommend to use after the controller get the GET_CONFIG_REPLY OFMessage).
	 * @param switchId
	 * @param sw	describe the switch 
	 */
    public boolean addSwitch(long switchId, IOFSwitch sw);
    
    public void removeSwitch(long switchId);
    
    public void addSwitchChannel(long switchId, Channel channel);
    
    public void removeSwitchChannel(long switchId);
    
    //interfaces to GUI        
    public List<Integer> iGetAllSwitchID();   
    
    /**
     * Set the port openflow Enabled/Disabled
     * @param deviceId
     * @param portId
     * @param onoff		0: disabled;  1: enabled
     */
    public void iSetPortOpenFlowEnable(int deviceId, int portId, byte onoff);
    
    public List<Integer> iGetAllPortId(int switchId);
    
    //field
    /**
     * @return new field ID, FIELDID_INVALID means failed
     */
    public short iNewField(String fieldName, short fieldLength, short fieldOffset);
    
    /**
     * modify the field with the new name/length/offset.
     * NOTES: modify/delete a field is allowed ONLY BEFORE anyone used it in case user entered the wrong
     * 		  information when he/her create the field. It is illegal to modify/delete a field after 
     * 		  anyone used it in flow table/entry already.
     * @param fieldId
     * @param fieldName
     * @param fieldLength
     * @param fieldOffset
     * @return modified successfully or failed
     */
    public boolean iModifyField(short fieldId, String fieldName, short fieldLength, short fieldOffset);

    /**
     * delete the field
     * NOTES: modify/delete a field is allowed ONLY BEFORE anyone used it in case user entered the wrong
     * 		  information when he/her create the field. It is illegal to modify/delete a field after 
     * 		  anyone used it in flow table/entry already.
     * @param fieldId
     */
    public void iDelField(short fieldId);    
    
    public List<OFMatch20> iGetAllField();
    
    public OFMatch20 iGetMatchField(short fieldId);
    
    
    /**
     * modify the metadata with the new metadataList.
     * NOTES: modify metadata is allowed ONLY BEFORE anyone used it in case user entered the wrong
     * 		  information when he/her create the metadata. It is illegal to modify metadata after 
     * 		  anyone used it in flow table/entry already.
     * @param metadataList
     * @return modified successfully or failed
     */
	public boolean iModifyMetadata(List<OFMatch20> metadataList);
	
	/**
	 * @return metadata list
	 */
	public List<OFMatch20> iGetMetadata();
    
    //protocol
    /**
     * @return new protocol ID, OFPROTOCOLID_INVALID means failed
     */
    public short iAddProtocol(String protocolName, List<OFMatch20> fieldList);
    
    /**
     * modify the protocol with the new fieldsList.
     * NOTES: modify/delete a protocol is allowed ONLY BEFORE anyone used it in case user entered the wrong
     * 		  information when he/her create the protocol. It is illegal to modify/delete a protocol after 
     * 		  anyone used it in flow table/entry already.
     * @param protocolID
     * @param fieldList
     * @return modified successfully or failed
     */
    public boolean iModifyProtocol(short protocolID, List<OFMatch20> fieldList);
    
    /**
     * delete the protocol
     * NOTES: modify/delete a protocol is allowed ONLY BEFORE anyone used it in case user entered the wrong
     * 		  information when he/her create the protocol. It is illegal to modify/delete a protocol after 
     * 		  anyone used it in flow table/entry already.
     * @param protocolID
     */
    public void iDelProtocol(short protocolID);
    
    public OFProtocol iGetProtocol(short protocolID);

    public OFProtocol iGetProtocol(String protocolName);
    
    public List<OFProtocol> iGetAllProtocol();
    
    /**
     * get the ofprotocol who contains the field with fieldId
     * @param fieldId
     * @return a clone object of OFFProtocol who contain the OFMatch20 with fieldId
     */
    public OFProtocol iGetBelongedProtocol(short fieldId);
    
  
    //flow table
    /**
     * create a flow table
     * @return new flow table ID. <br>
     * NOTES: the returned flow table id is a unique global table id.
     *        the OFFlowTable.getTableID() is not unique globally 
     *        but unique within its tableType (small table Id). 
     *        Use parseToSmallTableId() and parseToGlobalTableId()
     *        to parse the global and small id if needed.
     * @return new flow table ID (global table id). -1 means failed
     */
    public byte iAddFlowTable(int switchId, String tableName,
                                byte tableType, short keyLength, int tableSize,
                                byte fieldNum, List<OFMatch20> matchFieldList);
    

    /**
     * NOTES: the OFFlowTable.getTableID() is small table id. 
     */
    public List<OFFlowTable> iGetAllFlowTable(int switchId);
    
    /**
     * NOTES: the OFFlowTable.getTableID() is small table id. 
     *          the input parameter is globalTableId
     */
    public OFFlowTable iGetFlowTable(int switchId, byte globalTableId);
    
    /**
     * NOTES: the OFFlowTable.getTableID() is small table id. 
     *          the input parameter is globalTableId
     */
    public boolean iDelEmptyFlowTable(int switchId, byte globalTableId);
    /**
     * NOTES: the OFFlowTable.getTableID() is small table id. 
     *          the input parameter is globalTableId
     */
    public void iDelFlowTableAndAllSubEntries(int switchId, byte globalTableId);
    
    public byte iGetFlowTableNumberingBase(int switchId, byte tableType);
    
    /**
     * @param switchId
     * @param globalTableId
     * @return small table id
     */
    public byte parseToSmallTableId(int switchId, byte globalTableId);

    /**
     * @param switchId
     * @param tableType
     * @param smallTableId
     * @return global table id
     */
    public byte parseToGlobalTableId(int switchId, byte tableType, byte smallTableId);

    //flow entry
    /**
     * @return new flow entry ID. -1 means failed.
     */
    public int iAddFlowEntry(int switchId, byte globalTableId,
                            byte matchFieldNum, List<OFMatchX> matchXList,
                            byte instructionNum, List<OFInstruction> instructionList,
                            short priority);
    
    public List<OFFlowMod> iGetAllFlowEntry(int switchId, byte globalTableId);
    
    public OFFlowMod iGetFlowEntry(int switchId, byte globalTableId, int flowEntryId);
    
    public boolean iModFlowEntry(int switchId, byte globalTableId, int flowEntryId,
                                byte matchFieldNum, List<OFMatchX> matchList,
                                byte instructionNum, List<OFInstruction> instructionList,
                                short priority);
    
    public void iDelFlowEntry(int switchId, byte globalTableId, int index);
    
    /**
     * Will be deleted below @Deprecated methods.
     */
    //create the instruction, return the OFInstruction, 
    //see more details in the POF white paper, OpenFlow documents and the specific OFInstruction source code
    //@Deprecated , please new a OFInstructionXXX by yourself and set the parameters value
    @Deprecated
    public OFInstruction iNewInstructionGotoTable(int switchId, byte nextGlobalTableId, short packetOffset);
    @Deprecated
    public OFInstruction iNewInstructionGotoDirectTable(byte nextGlobalTableId, int tableEntryIndex, short packetOffset);
    @Deprecated
    public OFInstruction iNewInstructionMeter(int meterId);
    @Deprecated
    public OFInstruction iNewInstructionWriteMetadata(short metadataOffset, short writeLength, int value);
    @Deprecated
    public OFInstruction iNewInstructionWriteMetadataFromPacket(short metadataOffset, short packetOffset, short writeLength);
    @Deprecated 
    public OFInstruction iNewInstructionApplyActions(byte actionNum, List<OFAction> actionList);
    
    
    //create the action, return the OFAction, 
    //see more details in the POF white paper, OpenFlow documents and the specific OFAction source code
	//@Deprecated , please new a OFActionXXX by yourself and set the parameters value
    @Deprecated
    public OFAction iNewActionSetField(OFMatchX fieldSetting);
    @Deprecated
    public OFAction iNewActionSetFieldFromMetadata(OFMatch20 fieldSetting, short metadataOffset);
    @Deprecated
    public OFAction iNewActionModifyField(OFMatch20 fieldMatch, int increment);
    @Deprecated
    public OFAction iNewActionAddField(short fieldId, short fieldPosition, int fieldLength, long fieldValue);
    @Deprecated
    public OFAction iNewActionDeleteField(short fieldPosition, int fieldLength);
    @Deprecated
    public OFAction iNewActionOutput(int outputPortId, short metadataOffset, short metadataLength, short packetOffset);
    @Deprecated
    public OFAction iNewActionCalculateCheckSum(short checksumPosition, short checksumLength, short calcStartPosition, short calcLength);
    @Deprecated
    public OFAction iNewActionCounter(int counterId);
    @Deprecated
    public OFAction iNewActionGroup(int groupId);
    @Deprecated
    public OFAction iNewActionPacketIn(int reason);
    @Deprecated
    public OFAction iNewActionDrop(int reason);
    /**
     * Will be deleted above @Deprecated methods.
     */
    
    //matchX
    /**
     * create a MatchX of the field using value/mask.
     * OFMatchX is a match field with the field value/mask.
     * OFMatchX could be used in a specific flowEntry match information, 
     * 								setting field value in SET_FIELD action, 
     * 								modify field value in MODIFY_FIELD action
     * @param fieldId
     * @param value
     * @param mask
     * @return OFMatchX
     */
    public OFMatchX iNewMatchX(short fieldId, byte[] value, byte[] mask);

    /**
     * create a MatchX of the OFMatch20 using value/mask.
     * OFMatchX is a match field with the field value/mask.
     * OFMatchX could be used in a specific flowEntry match information, 
     * 								setting field value in SET_FIELD action, 
     * 								modify field value in MODIFY_FIELD action
     * @param match
     * @param value
     * @param mask
     * @return OFMatchX
     */
    public OFMatchX iNewMatchX(OFMatch20 match, byte[] value, byte[] mask);    


    
    /**
     * @param switchId
     * @param tableId
     * @param flowEntryId
     * @param instructionIndex
     * @return the instruction of the flow entry
     */
    public OFInstruction iGetInstruction(int switchId, byte tableId, int flowEntryId,
                                            int instructionIndex);
    
    public OFInstruction iGetInstruction(OFFlowMod flowEntry, int instructionIndex);
    
    /**
     * @param switchId
     * @param tableId
     * @param flowEntryId
     * @param instructionIndex
     * @param actionIndex
     * @return the action of the instruction
     */
    public OFAction iGetActionFromInstruction(int switchId, byte tableId, int flowEntryId,
                                                int instructionIndex, int actionIndex);
    /**
     * @param switchId
     * @param groupId
     * @param actionIndex
     * @return the action of the group
     */
    public OFAction iGetActionFromGroup(int switchId, int groupId, int actionIndex);
    
    
    //counter    
    public long iQueryCounterValue(int switchId, int counterId);
    
    /**
     * allocate a new counter
     * @param switchId
     * @return the new couter's id
     */
    public int iAllocateCounter(int switchId);
    
    /**
     * @return a cloned object of the counter
     */
    public OFCounter iFreeCounter(int switchId, int counterId);
    
    /**
     * reset the counter value to 0
     * @param switchId
     * @param counterId
     * @param writeToSwitch  write the counter.value=0 to switch or not
     */
    public boolean iResetCounter(int switchId, int counterId, boolean writeToSwitch);
    
    
    //meter
    public int iAddMeterEntry(int switchId, short rate);
    
    /**
     * @return a cloned object of the meter
     */
    public OFMeterMod iFreeMeter(int switchId, int meterId);
    
    public OFMeterMod iGetMeter(int switchId, int meterId);
    
    public boolean iModifyMeter(int switchId, int meterId, short rate);
    
    public List<OFMeterMod> iGetAllMeters(int switchId);

    
    //group
    public int iAddGroupEntry(int switchId, byte groupType, byte actionNum, List<OFAction> actionList);
    
    /**
     * @return  a cloned object of the group
     */
    public OFGroupMod iFreeGroupEntry(int switchId, int groupId);
    
    public OFGroupMod iGetGroupEntry(int switchId, int groupId);
    
    public boolean iModifyGroupEntry(int switchId, int groupId, byte groupType, byte actionNum, List<OFAction> actionList);

    public List<OFGroupMod> iGetAllGroups(int switchId);
    

    
    public int iGetFlowEntryNumber(int switchId, byte globalTableId);

    public int iGetTableNumber(int switchId, OFTableType tableType);

    public int iGetUsedCounterNumber(int switchId);

    public int iGetUsedGroupNumber(int switchId);

    public int iGetUsedMeterNumber(int switchId);

    public int iGetAllTableNumber(int switchId);
    
    //
    public void reloadUI();
    

    /**
     * send a OFMessage to switch
     * @param switchId
     * @param message
     */
    public void iAddSendedOFMessage(int switchId, OFMessage message);

    public Queue<OFMessage> iGetSendedOFMessageQueue(int switchId);

    public OFMessage iGetSendedOFMessage(int switchId, int xid);

    public void iDelSendedOFMessage(int switchId, OFMessage message);

    public boolean saveAllDataIntoFile(String fileName);

    public boolean loadAllDataFromFile(String fileName);

    /**
     * Send all open flow messages which stored in database to the switches.
     * @return successful or failed
     */
    public boolean sendAllOFMessagesBasedOnDatabase();
    
    
    //interface to OFMessageBypassManager
    /**
     * process when received a feature reply ofmessage from switch
     * @param switchId
     * @param featureReply
     */
    public void iSetFeatures(int switchId, OFFeaturesReply featureReply);
    

	public OFFeaturesReply iGetFeature(int switchId);

    /**
     * process when received a updated port status ofmessage from switch
     * @param switchId
     * @param portStatus
     */
    public void iSetPortStatus(int switchId, OFPortStatus portStatus);
    
    
    public OFPortStatus iGetPortStatus(int switchId, int portId);

    /**
     * process when received table resource ofmessage from switch
     * @param switchId
     * @param flowTableResource
     */
    public void iSetResourceReport(int switchId, OFFlowTableResource flowTableResource);
    
    /**
     * return table resource 
     * @param switchId
     * @return flow table resource
     */
    public OFFlowTableResource iGetResourceReport(int switchId);
    
    /**
     * process when received counter reply ofmessage from switch
     * @param sw
     * @param counterReply
     */
    public void processCounterReply(IOFSwitch sw, OFCounterReply counterReply);
    
    /**
     * process when receive error ofmessage from switch
     * @param switchId
     * @param errorMsg
     */
    public void iProcessOFError(int switchId, OFError errorMsg);

    /**
     * process when receive packetin ofmessage from switch
     * @param switchId
     * @param packetinMsg
     */
    public void iProcessOFPacketIn(int switchId, OFPacketIn packetinMsg);

    
    //log
    public void iLogOFMessageIn(int switchId, OFMessage message);
    
    public void iLogErrorMessageIn(int switchId, OFError errMessage);
    
    public void iLogPacketInMessageIn(int switchId, OFPacketIn packetIn);
    
    public void iLogOFMessageOut(int switchId, OFMessage message);
    
    public void iLogErrorMessageOut(int switchId, OFError errMessage);
    
    public void iLogPacketInMessageOut(int switchId, OFPacketIn packetIn);

}
