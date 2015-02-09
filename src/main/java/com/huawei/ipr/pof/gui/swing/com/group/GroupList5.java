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

package com.huawei.ipr.pof.gui.swing.com.group;

import javax.swing.DefaultListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openflow.protocol.OFMatch20;
import org.openflow.protocol.OFMatchX;
import org.openflow.protocol.OFPacketIn.OFPacketInReason;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionAddField;
import org.openflow.protocol.action.OFActionCalculateCheckSum;
import org.openflow.protocol.action.OFActionCounter;
import org.openflow.protocol.action.OFActionDeleteField;
import org.openflow.protocol.action.OFActionDrop;
import org.openflow.protocol.action.OFActionDrop.OFDropReason;
import org.openflow.protocol.action.OFActionGroup;
import org.openflow.protocol.action.OFActionModifyField;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.protocol.action.OFActionPacketIn;
import org.openflow.protocol.action.OFActionSetField;
import org.openflow.protocol.action.OFActionSetFieldFromMetadata;
import org.openflow.protocol.action.OFActionType;
import org.openflow.util.HexString;

import com.huawei.ipr.pof.gui.comm.GUITools.EDIT_STATUS;
import com.huawei.ipr.pof.gui.swing.SwingUIPanel;
import com.huawei.ipr.pof.gui.swing.com.switcher.PList;
import com.huawei.ipr.pof.gui.swing.com.switcher.PList6;
import com.huawei.ipr.pof.gui.swing.com.switcher.SwitchPanel;
import com.huawei.ipr.pof.gui.swing.comutil.GroupEntry;
import com.huawei.ipr.pof.manager.IPMService;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
@SuppressWarnings("serial")
public class GroupList5 extends PList{
	public static final OFActionType[] ACTIONS = IPMService.ACTIONS;

	public static final String[] actionOutputString = PList6.actionOutputString;		//{ "Output Port ID", "Metadata Offset(bit)", "Metadata Length(bit)", "Packet Offset(byte)" };
	public static final String[] actionSetFieldString = PList6.actionSetFieldString;	//{ "Field Name", "Value(0x)", "Mask(0x)" };
	public static final String[] actionSetFieldFromMetadataString = PList6.actionSetFieldFromMetadataString;	//{ "Field Name", "Metadata Offset(bit)" };
	public static final String[] actionModFieldString = PList6.actionModFieldString;	//{ "Field Name", "Increment" };
	public static final String[] actionAddFieldString = PList6.actionAddFieldString;	//{ "Field name", "Field Position", "Field Length(bit)", "Field Value" };
	public static final String[] actionDelFieldString = PList6.actionDelFieldString;	//{ "Offset(bit)", "Length(bit)" };
	public static final String[] actionCalCheckSumString = PList6.actionCalCheckSumString;	//{ "Checksum Position", "Checksum Length", "Calc Start Position", "Calc Length" };
	public static final String[] actionGroupString = PList6.actionGroupString;			//{ "Group ID" };
	public static final String[] actionDropString = PList6.actionDropString;			//{ "Reason" };
	public static final String[] actionPacketInString = PList6.actionPacketInString;	//{ "Reason" };
	public static final String[] actionCounterString = PList6.actionCounterString;					//{ "Counter ID" };
	
	public static final String ACTIONS_STRING = GroupList4.ACTIONS_STRING;
	

	protected GroupEntry currentGroupEntry;
	protected int actionIndex;
	
	protected GroupList4 grouplist4;
	protected GroupList6 grouplist6;
	
	
	public GroupList5() {
		super();
		
		addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()){
					selectedValueChanged();
				}
			}
		});
	}
	
	public void setOtherList(GroupList4 grouplist4, GroupList6 grouplist6) {
		this.grouplist4 = grouplist4;
		this.grouplist6 = grouplist6;
	}

	protected void selectedValueChanged() {		
		grouplist6.clearData();
		
		int index = getSelectedIndex();
		if(-1 == index){			
			return;
		}
		
		if(currentGroupEntry.editStatus == EDIT_STATUS.ES_READONLY){
			return;
		}
		
		String value = String.valueOf(getSelectedValue());
		
		if(value != null && !value.isEmpty()){
			DefaultListModel list4Model = (DefaultListModel) grouplist4.getModel();
			if(list4Model.getElementAt(0).equals(ACTIONS_STRING)){
				if(listModel.getElementAt(0).equals(ACTIONS[0].name())){	//add a new action
					list4Model.addElement( value );
					
					addNewAction( OFActionType.valueOf(value) );
					
					clearData();
				}else{												//display action value
					grouplist6.showAction(currentGroupEntry, actionIndex);
				}
			}
		}
		
		validate();
		repaint();
	}

	private void addNewAction(final OFActionType actionType) {
		OFAction newAction = null;
		switch (actionType){
			case OUTPUT:
				newAction = new OFActionOutput();
				break;
			case SET_FIELD:
				newAction = new OFActionSetField();
				((OFActionSetField)newAction).setFieldSetting(new OFMatchX());
				break;
			case SET_FIELD_FROM_METADATA:
				newAction = new OFActionSetFieldFromMetadata();
				((OFActionSetFieldFromMetadata)newAction).setFieldSetting(new OFMatch20());
				break;
			case MODIFY_FIELD:
				newAction = new OFActionModifyField();
				((OFActionModifyField)newAction).setMatchField(new OFMatch20());
				break;
			case ADD_FIELD:
				newAction = new OFActionAddField();
				break;
			case DELETE_FIELD:
				newAction = new OFActionDeleteField();
				break;
			case CALCULATE_CHECKSUM:
				newAction = new OFActionCalculateCheckSum();
				break;
			case GROUP:
				newAction = new OFActionGroup();
				break;
			case DROP:
				newAction = new OFActionDrop();
				break;
			case PACKET_IN:
				newAction = new OFActionPacketIn();
				break;
			case COUNTER:
				newAction = new OFActionCounter();
				break;
			default:
				break;
		}
		
		currentGroupEntry.ofGroupMod.getActionList().add(newAction);
		currentGroupEntry.ofGroupMod.setActionNum((byte) currentGroupEntry.ofGroupMod.getActionList().size());
	}

	public void clearNextList(){
		grouplist6.clearData();
	}


	public void showAllDefaultActions(final GroupEntry currentGroupEntry) {
		this.currentGroupEntry = currentGroupEntry;
		clearData();

		for (OFActionType actionType: ACTIONS) {
			listModel.addElement(actionType.name());
		}
		
		validate();
		repaint();
	}



	public void showAction(final GroupEntry currentGroupEntry, final int actionIndex) {
		this.currentGroupEntry = currentGroupEntry;		
		this.actionIndex = actionIndex;
		
		clearData();
		
		OFAction action = currentGroupEntry.ofGroupMod.getActionList().get(actionIndex);

		switch(action.getType()){
			case OUTPUT:
				showAction((OFActionOutput)action);
				break;
			case SET_FIELD:
				showAction((OFActionSetField)action);
				break;
			case SET_FIELD_FROM_METADATA:
				showAction((OFActionSetFieldFromMetadata)action);
				break;
			case MODIFY_FIELD:
				showAction((OFActionModifyField)action);
				break;
			case ADD_FIELD:
				showAction((OFActionAddField)action);
				break;
			case DELETE_FIELD:
				showAction((OFActionDeleteField)action);
				break;				
			case CALCULATE_CHECKSUM:
				showAction((OFActionCalculateCheckSum)action);
				break;
			case GROUP:
				showAction((OFActionGroup)action);
				break;
			case DROP:
				showAction((OFActionDrop)action);
				break;
			case PACKET_IN:
				showAction((OFActionPacketIn)action);
				break;
			case COUNTER:
				showAction((OFActionCounter)action);
				break;
			default:
				return;
		}
	
		validate();
		repaint();
	}
	
	private void showAction(final OFActionOutput action) {
		listModel.addElement( actionOutputString[0] + ": " + action.getPortId() );
		listModel.addElement( actionOutputString[1] + ": " + action.getMetadataOffset() );
		listModel.addElement( actionOutputString[2] + ": " + action.getMetadataLength() );
		listModel.addElement( actionOutputString[3] + ": " + action.getPacketOffset() );
	}
	
	private void showAction(final OFActionSetField action) {
		OFMatchX fieldX = action.getFieldSetting();

		if(fieldX.getFieldId() == IPMService.FIELDID_INVALID){
			listModel.addElement( actionSetFieldString[0] );
			listModel.addElement( actionSetFieldString[1] );
			listModel.addElement( actionSetFieldString[2] );
		}else{
			String value = HexString.toHex( fieldX.getValue() );
			String mask = HexString.toHex( fieldX.getMask() );
			listModel.addElement( actionSetFieldString[0] + ": " + fieldX.getFieldId() + "." + fieldX.getFieldName() );
			listModel.addElement( actionSetFieldString[1] + ": " + value );
			listModel.addElement( actionSetFieldString[2] + ": " + mask );
		}
	}
	
	private void showAction(final OFActionSetFieldFromMetadata action) {
		OFMatch20 field = action.getFieldSetting();
		if(field.getFieldId() == IPMService.FIELDID_INVALID){
			listModel.addElement( actionSetFieldFromMetadataString[0] );
		}else{
			listModel.addElement( actionSetFieldFromMetadataString[0] + ": " + field.getFieldId() + "." + field.getFieldName() );
		}
		listModel.addElement( actionSetFieldFromMetadataString[1] + ": " + action.getMetadataOffset() );
	}
	
	private void showAction(final OFActionModifyField action) {
		OFMatch20 field = action.getMatchField();
		if(field.getFieldId() == IPMService.FIELDID_INVALID){
			listModel.addElement( actionModFieldString[0] );
		}else{
			listModel.addElement( actionModFieldString[0] + ": " + field.getFieldId() + "." + field.getFieldName() );
		}
		listModel.addElement( actionModFieldString[1] + ": " + action.getIncrement() );
	}
	
	private void showAction(final OFActionAddField action) {
		short fieldID = action.getFieldId();
		if(fieldID == IPMService.FIELDID_INVALID){
			listModel.addElement( actionAddFieldString[0] );
			listModel.addElement( actionAddFieldString[1] );
		}else{
			String fieldName = SwingUIPanel.pofManager.iGetMatchField(fieldID).getFieldName();
			listModel.addElement( actionAddFieldString[0] + ": " + fieldID + "." + fieldName );
			listModel.addElement( actionAddFieldString[1] + ": " + fieldID);
		}
		listModel.addElement( actionAddFieldString[2] + ": " + action.getFieldPosition() );
		listModel.addElement( actionAddFieldString[3] + ": " + action.getFieldLength() );
		listModel.addElement( actionAddFieldString[4] + ": " + action.getFieldValue() );
	}
	
	private void showAction(final OFActionDeleteField action) {
		listModel.addElement( actionDelFieldString[0] + ": " + action.getFieldPosition() );
		listModel.addElement( actionDelFieldString[1] + ": " + action.getFieldLength() );
	}
	
	private void showAction(final OFActionCalculateCheckSum action) {
		listModel.addElement( actionCalCheckSumString[0] + ": " + action.getChecksumPosition() );
		listModel.addElement( actionCalCheckSumString[1] + ": " + action.getChecksumLength() );
		listModel.addElement( actionCalCheckSumString[2] + ": " + action.getCalcStartPosition() );
		listModel.addElement( actionCalCheckSumString[3] + ": " + action.getCalcLength() );
	}
	
	private void showAction(final OFActionGroup action) {
		int groupId = action.getGroupId();
		if(groupId == IPMService.GROUPID_INVALID){
			listModel.addElement( actionGroupString[0] );
		}else{
			listModel.addElement( actionGroupString[0] + ": " + action.getGroupId() );
		}
	}
	
	private void showAction(final OFActionDrop action) {
		listModel.addElement( actionDropString[0] + ": " + OFDropReason.values()[action.getReason()].name() );
	}

	private void showAction(final OFActionPacketIn action) {
		listModel.addElement( actionPacketInString[0] + ": " + OFPacketInReason.values()[action.getReason()] );
	}
	
	private void showAction(final OFActionCounter action) {
		int counterId = action.getCounterId();
		if(counterId == IPMService.COUNTERID_INVALID){
			counterId = SwingUIPanel.pofManager.iAllocateCounter(SwitchPanel.switchID);
			action.setCounterId(counterId);
		}
		listModel.addElement( actionCounterString[0] + ": " + counterId );
	}
}
