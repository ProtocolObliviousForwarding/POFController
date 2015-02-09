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

package com.huawei.ipr.pof.gui.swing.com.switcher;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch20;
import org.openflow.protocol.OFMatchX;
import org.openflow.protocol.OFMeterMod;
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
import org.openflow.protocol.instruction.OFInstruction;
import org.openflow.protocol.instruction.OFInstructionApplyActions;
import org.openflow.protocol.instruction.OFInstructionGotoDirectTable;
import org.openflow.protocol.instruction.OFInstructionGotoTable;
import org.openflow.protocol.instruction.OFInstructionMeter;
import org.openflow.protocol.instruction.OFInstructionWriteMetadata;
import org.openflow.protocol.instruction.OFInstructionWriteMetadataFromPacket;
import org.openflow.protocol.table.OFFlowTable;
import org.openflow.protocol.table.OFTableType;
import org.openflow.util.HexString;

import com.huawei.ipr.pof.gui.comm.GUITools;
import com.huawei.ipr.pof.gui.comm.GUITools.EDIT_STATUS;
import com.huawei.ipr.pof.gui.swing.HomeMain;
import com.huawei.ipr.pof.gui.swing.SwingUIPanel;
import com.huawei.ipr.pof.gui.swing.comutil.FlowEntry;
import com.huawei.ipr.pof.gui.swing.comutil.ImageUtils;
import com.huawei.ipr.pof.gui.swing.comutil.PJComboBox;
import com.huawei.ipr.pof.manager.IPMService;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
@SuppressWarnings("serial")
public class PList6 extends JTable {

	public static final OFActionType[] ACTIONS = IPMService.ACTIONS;
	
	public static final String[] actionOutputString = { "Output Port ID", "Metadata Offset(bit)", "Metadata Length(bit)", "Packet Offset(byte)" };
	public static final String[] actionSetFieldString = { "Field Name", "Value(0x)", "Mask(0x)" };
	public static final String[] actionSetFieldFromMetadataString = { "Field Name", "Metadata Offset(bit)" };
	public static final String[] actionModFieldString = { "Field Name", "Increment" };
	public static final String[] actionAddFieldString = { "New Field Name", "Existed Field Id", "Field Position", "Field Length(bit)", "Field Value" };
	public static final String[] actionDelFieldString = { "Offset(bit)", "Length(bit)" };
	public static final String[] actionCalCheckSumString = { "Checksum Position", "Checksum Length", "Calc Start Position", "Calc Length" };
	public static final String[] actionGroupString = { "Group ID" };
	public static final String[] actionDropString = { "Reason" };
	public static final String[] actionPacketInString = { "Reason" };
	public static final String[] actionCounterString = { "Counter ID" };
	
	public static final String ACTIONS_STRING = PList5.ACTIONS_STRING;

	protected final Image def_img = ImageUtils.getImageIconByCaches("image/gui/swing/listbg.png").getImage();
	
	protected PList5 list5;
	protected PList7 list7;
	
	protected boolean isCellEditable = true;
	protected String[] data = {};
	protected JTextField textfield = new JTextField();
	protected JComboBox comboBox = new PJComboBox();
	
	protected DefaultTableModel dm;
	
	protected int instructionIndex;
	protected int actionIndex;
	
	protected FlowEntry currentFlowEntry;
	
	protected Boolean displayComboBox = null;
	

	public void setOtherList(PList5 list5, PList7 list7) {
		this.list5 = list5;
		this.list7 = list7;
	}

	private Vector<Vector<String>> strArray2Vector(String[] str) {
		Vector<Vector<String>> vector = new Vector<Vector<String>>();
		for (int i = 0; i < str.length; i++) {
			Vector<String> v = new Vector<String>();
			v.addElement(str[i]);
			vector.addElement(v);
		}
		return vector;
	}

	public PList6() {
		super();
		setOpaque(false);
		putClientProperty(GUITools.TERMINATE_EDIT_ON_FOCUS_LOST, Boolean.TRUE);

		dm = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return isCellEditable;
			}
			
			@Override
			public void setValueAt(Object aValue, int row, int column) {
				if(null != aValue 
						&& aValue.toString().length() > 0 
						&& row < dm.getRowCount() 
						&& column < dm.getColumnCount()){	//else will occur exception
					super.setValueAt(aValue, row, column);
				}
			}
		};

		Vector<String> dummyHeader = new Vector<String>();
		dummyHeader.addElement("");
		dm.setDataVector(strArray2Vector(data), dummyHeader);

		this.setModel(dm);

		setShowGrid(false);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		TableColumn tc = getColumnModel().getColumn(0);
		tc.setCellRenderer(new PList6CellRenderer());
		tc.setCellEditor(new PList6CellEditor());
		
		setRowHeight(24);

		dm.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				int t = e.getType();
				if (TableModelEvent.UPDATE == t) {
					changeTextValue();
				}
			}
		});
		

		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				selectedValueChanged();
			}
		});

		comboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				if (arg0.getStateChange() == ItemEvent.SELECTED) {
					if (arg0.getSource() == comboBox
							&& comboBox.getSelectedIndex() != 0
							&& comboBox.getSelectedIndex() != -1 ) {						
						setValueFromCombo(comboBox.getSelectedIndex(), String.valueOf(comboBox.getSelectedItem()));
					}
				}
			}
		});
	}
	
	public void clearData() {
		int count = dm.getRowCount();
		for (int i = 0; i < count; i++) {
			dm.removeRow(0);
		}
		
		clearSelection();
		
		textfield.setText("");
		comboBox.removeAllItems();
		
		comboBox.setVisible(false);
		textfield.setVisible(false);
		
		displayComboBox = null;
		
		list7.clearData();
		
		validate();
		repaint();
	}
	
	private void selectedValueChanged(){
		list7.clearData();

		int index = getSelectedRow();
		
		if (-1 == index) {
			return;
		}
		
		if(currentFlowEntry.editStatus == EDIT_STATUS.ES_READONLY){
			return;
		}
		
		if(displayComboBox != null && displayComboBox == true){
			return;
		}
		
		String value = String.valueOf(getValueAt(index, 0));
		
		if(value != null && !value.isEmpty()){
			DefaultListModel list5Model = (DefaultListModel) list5.getModel();
			if(list5Model.getElementAt(0).equals(ACTIONS_STRING)){
				if(dm.getValueAt(0, 0).equals(ACTIONS[0].name())){	//add a new action
					list5Model.addElement( value );
					
					addNewAction( OFActionType.valueOf(value) );
					
					clearData();
				}else{												//display action value
					list7.showAction(currentFlowEntry, instructionIndex, actionIndex);
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
		
		OFInstructionApplyActions ins = (OFInstructionApplyActions)(currentFlowEntry.ofFlowMod.getInstructionList().get(instructionIndex));
		ins.getActionList().add(newAction);
		ins.setActionNum((byte)(ins.getActionList().size()));
	}
	
	
	private void setValueFromCombo(final int comboSelectedIndex, final String comboSelectedString){
		OFInstruction ins = currentFlowEntry.ofFlowMod.getInstructionList().get(instructionIndex);
		switch(ins.getType()){
			case GOTO_TABLE:
				setValueFromCombo((OFInstructionGotoTable)ins, comboSelectedString);
				break;
			case GOTO_DIRECT_TABLE:
				setValueFromCombo((OFInstructionGotoDirectTable)ins, comboSelectedString, list5.getSelectedIndex());
				break;
			case METER:
				setValueFromCombo((OFInstructionMeter)ins, comboSelectedString);
				break;
			case WRITE_METADATA:
				setValueFromCombo((OFInstructionWriteMetadata)ins, comboSelectedIndex);
				break;
			case WRITE_METADATA_FROM_PACKET:
				setValueFromCombo((OFInstructionWriteMetadataFromPacket)ins, comboSelectedIndex);
				break;
			default:
				return;
		}
		
		validate();
		repaint();
	}
	
	private void setValueFromCombo(final OFInstructionGotoTable ins, final String comboSelectedString) {
		String tableIDString = getSubString(comboSelectedString, ".");
		
		if(tableIDString != null && tableIDString.matches(GUITools.RE_DEC)){
			byte globalTableID = (byte)Integer.parseInt(tableIDString);
			ins.setNextTableId(globalTableID);
			OFFlowTable table = SwingUIPanel.pofManager.iGetFlowTable(SwitchPanel.switchID, globalTableID);
			ins.setMatchFieldNum(table.getMatchFieldNum());
			ins.setMatchList(table.getMatchFieldList());
			
			DefaultListModel list5Model = (DefaultListModel) list5.getModel();
			list5Model.setElementAt(PList5.insGotoTableString[0] + ": " + globalTableID, 0);
		}
	}

	private void setValueFromCombo(final OFInstructionGotoDirectTable ins, final String comboSelectedString, final int selectIndex) {
		DefaultListModel list5Model = (DefaultListModel) list5.getModel();
		if(selectIndex == 0){
			String tableIDString = getSubString(comboSelectedString, ".");
			
			if(tableIDString != null && tableIDString.matches(GUITools.RE_DEC)){
				byte globalTableID = (byte)Integer.parseInt(tableIDString);
				ins.setNextTableId(globalTableID);
				
				list5Model.setElementAt(PList5.insGotoDirectTableString[0] + ": " + globalTableID, 0);
			}
		}else if(selectIndex == 1 && comboSelectedString.matches(GUITools.RE_DEC)){
			int entryIndex = Integer.parseInt(comboSelectedString);
			ins.setTableEntryIndex(entryIndex);
			
			list5Model.setElementAt(PList5.insGotoDirectTableString[1] + ": " + entryIndex, 1);

		}
	}

	private void setValueFromCombo(final OFInstructionMeter ins, final String comboSelectedString) {
		String meterIDString = getSubString(comboSelectedString, ".");
		
		if( meterIDString != null && meterIDString.matches(GUITools.RE_DEC) ){
			int meterID = Integer.parseInt(meterIDString);
			
			if(meterID != IPMService.METER_INVALID){
				ins.setMeterId(meterID);
				
				OFMeterMod meter = SwingUIPanel.pofManager.iGetMeter(SwitchPanel.switchID, meterID);
				
				DefaultListModel list5Model = (DefaultListModel) list5.getModel();
				list5Model.setElementAt(PList5.insMeterString[0] + ": " + meterID + "." + meter.getRate(),  0);
			}
		}
	}

	private void setValueFromCombo(final OFInstructionWriteMetadata ins, final int comboSelectedIndex) {
		List<OFMatch20> metadataList = HomeMain.contentPane.getProtocolPanel().metadataButton.newPacketFieldList;
		OFMatch20 metadataField = metadataList.get(comboSelectedIndex - 1);
		
		ins.setMetadataOffset(metadataField.getOffset());
		ins.setWriteLength(metadataField.getLength());
		
		DefaultListModel list5Model = (DefaultListModel) list5.getModel();
		list5Model.setElementAt(PList5.insWriteMetadataString[0] + ": " + metadataField.getFieldId() + "." + metadataField.getFieldName(), 0);
		list5Model.setElementAt(PList5.insWriteMetadataString[1] + ": " + metadataField.getOffset(), 1);
		list5Model.setElementAt(PList5.insWriteMetadataString[2] + ": " + metadataField.getLength(), 2);

	}
	
	private void setValueFromCombo(final OFInstructionWriteMetadataFromPacket ins, final int comboSelectedIndex) {
		List<OFMatch20> metadataList = HomeMain.contentPane.getProtocolPanel().metadataButton.newPacketFieldList;
		OFMatch20 metadataField = metadataList.get(comboSelectedIndex - 1);
		
		ins.setMetadataOffset(metadataField.getOffset());
		ins.setWriteLength(metadataField.getLength());
		
		DefaultListModel list5Model = (DefaultListModel) list5.getModel();
		list5Model.setElementAt(PList5.insWriteMetadataFromPacketString[0] + ": " + metadataField.getFieldId() + "." + metadataField.getFieldName(), 0);
		list5Model.setElementAt(PList5.insWriteMetadataFromPacketString[1] + ": " + metadataField.getOffset(), 1);
		list5Model.setElementAt(PList5.insWriteMetadataFromPacketString[2] + ": " + metadataField.getLength(), 2);
	}
	
	
	private void showTextField(String textValue) {
		displayComboBox = false;
		
		comboBox.setVisible(false);
		textfield.setVisible(true);
		
		isCellEditable = true;
		
		if(textValue == null){
			textfield.setText("");
			dm.addRow(new String[] { "" });
		}else{
			textfield.setText(textValue);
			dm.addRow(new String[] { textValue });
		}
	}
	
	private void showTextField() {
		showTextField(null);
	}
	
	private void showComboBox() {
		displayComboBox = true;
		
		comboBox.setVisible(true);
		textfield.setVisible(false);
		
		isCellEditable = true;

		dm.addRow(new Object[]{comboBox});
	}
	
	private void notShowComboBoxOrTextField() {
		displayComboBox = null;
		
		comboBox.setVisible(false);
		textfield.setVisible(false);
		
		isCellEditable = false;
	}
	
	
	public void showInstruction(final FlowEntry currentFlowEntry, final int instructionIndex) {
		this.currentFlowEntry = currentFlowEntry;
		this.instructionIndex = instructionIndex;
		
		clearData();
		
		isCellEditable = true;
		
		OFInstruction ins = currentFlowEntry.ofFlowMod.getInstructionList().get(instructionIndex);
		
		int selectedIndexInList5 = list5.getSelectedIndex();
		switch(ins.getType()){
			case GOTO_TABLE:
				showInstruction((OFInstructionGotoTable)ins, selectedIndexInList5);
				break;
			case GOTO_DIRECT_TABLE:
				showInstruction((OFInstructionGotoDirectTable)ins, selectedIndexInList5);
				break;
			case METER:
				showInstruction((OFInstructionMeter)ins, selectedIndexInList5);
				break;
			case WRITE_METADATA:
				showInstruction((OFInstructionWriteMetadata)ins, selectedIndexInList5);
				break;
			case WRITE_METADATA_FROM_PACKET:
				showInstruction((OFInstructionWriteMetadataFromPacket)ins, selectedIndexInList5);
				break;
			default:
				return;
		}
		
		validate();
		repaint();
	}
	
	private void showInstruction(final OFInstructionGotoTable ins, final int selectedIndex) {
		if(selectedIndex == 0){
			comboBox.addItem("");
			List<OFFlowTable> allTableList = SwingUIPanel.pofManager.iGetAllFlowTable(SwitchPanel.switchID);
			for (OFFlowTable flowTable : allTableList) {
				byte nextTableID = SwingUIPanel
										.pofManager
										.parseToGlobalTableId(SwitchPanel.switchID, 
																	flowTable.getTableType().getValue(), 
																	flowTable.getTableId());
				if (flowTable.getTableType() != OFTableType.OF_LINEAR_TABLE) {
					comboBox.addItem(nextTableID + "." +flowTable.getTableName());
				}
			}
			
			showComboBox();
		}else{
			showTextField();
		}
	}

	private void showInstruction(final OFInstructionGotoDirectTable ins, final int selectedIndex) {
		if(selectedIndex == 0){
			comboBox.addItem("");
			List<OFFlowTable> allTableList = SwingUIPanel.pofManager.iGetAllFlowTable(SwitchPanel.switchID);
			for (OFFlowTable flowTable : allTableList) {
				byte globalTableId = SwingUIPanel
										.pofManager
										.parseToGlobalTableId(SwitchPanel.switchID, 
																	flowTable.getTableType().getValue(), 
																	flowTable.getTableId());
				if (flowTable.getTableType() == OFTableType.OF_LINEAR_TABLE) {
					comboBox.addItem(globalTableId + "." +flowTable.getTableName());
				}
			}
			
			showComboBox();
		}else if(selectedIndex == 1){
			comboBox.addItem("");
			
			byte globalTableId = ins.getNextTableId();
			
			OFFlowTable flowTable = SwingUIPanel.pofManager.iGetFlowTable(SwitchPanel.switchID, globalTableId);
			List<OFFlowMod> flowEntryList = SwingUIPanel.pofManager.iGetAllFlowEntry(SwitchPanel.switchID, globalTableId);
			if (null != flowEntryList) {
				if (flowTable.getTableType() == OFTableType.OF_LINEAR_TABLE) {
					for (OFFlowMod flowEntry : flowEntryList) {
						comboBox.addItem(String.valueOf(flowEntry.getIndex()));
					}
				}
			}
			
			showComboBox();
		}else{
			showTextField();
		}
	}

	private void showInstruction(final OFInstructionMeter ins, final int selectedIndex) {
		comboBox.addItem("");
		List<OFMeterMod> meterList = SwingUIPanel.pofManager.iGetAllMeters(SwitchPanel.switchID);
		for (OFMeterMod ofMeter : meterList) {
			comboBox.addItem((ofMeter.getMeterId() + "." + ofMeter.getRate()));
		}
		
		showComboBox();
	}

	private void showInstruction(final OFInstructionWriteMetadata ins, final int selectedIndex) {
		if(selectedIndex == 0){
			comboBox.addItem("");
			List<OFMatch20> metadataList = HomeMain.contentPane.getProtocolPanel().metadataButton.newPacketFieldList;
			for (OFMatch20 metadataField : metadataList) {
				comboBox.addItem((metadataField.getFieldId() + "." + metadataField.getFieldName()));
			}
			
			showComboBox();
		}else if(selectedIndex == 3){
			showTextField();
		}
	}
	
	private void showInstruction(final OFInstructionWriteMetadataFromPacket ins, final int selectedIndex) {
		if(selectedIndex == 0){
			comboBox.addItem("");
			List<OFMatch20> metadataList = HomeMain.contentPane.getProtocolPanel().metadataButton.newPacketFieldList;
			for (OFMatch20 metadataField : metadataList) {
				comboBox.addItem((metadataField.getFieldId() + "." + metadataField.getFieldName()));
			}
			
			showComboBox();
		}else if(selectedIndex == 3){
			showTextField();
		}
	}


	protected void changeTextValue() {
		if(displayComboBox == null || displayComboBox == true){
			return;
		}
		
		String value = (String)dm.getValueAt(0, 0);
		
		if (null == value || 0 == value.length()) {
			return;
		}
		
		DefaultListModel list5Model = (DefaultListModel) list5.getModel();

		if(list5Model.getElementAt(0).equals(ACTIONS_STRING)){
			return;
		}
		
		String valueInList5 = String.valueOf(list5.getSelectedValue());
		int selectedIndexInList5 = list5.getSelectedIndex();
		int selectedIndexInList4 = list5.list4.getSelectedRow();
		
		if(valueInList5.startsWith(PList5.matchFieldString[3])){
			if(!value.matches(GUITools.RE_HEX)){
				if(value.matches(GUITools.RE_0xHEX)){
					value = value.substring(2);
				}else{
					GUITools.messageDialog(this, "Please input correct value."); 
					return;
				}
			}

			byte[] valueBytes = GUITools.parseTextToHexBytes(this, "Value", value, currentFlowEntry.ofFlowMod.getMatchList().get(selectedIndexInList4).getLength());
			
			if(null != valueBytes){
				list5Model.set(selectedIndexInList5, PList5.matchFieldString[3] + ": " + value);
				currentFlowEntry.ofFlowMod.getMatchList().get(selectedIndexInList4).setValue(valueBytes);
			}
		}else if(valueInList5.startsWith(PList5.matchFieldString[4])){
			if(!value.matches(GUITools.RE_HEX)){
				if(value.matches(GUITools.RE_0xHEX)){
					value = value.substring(2);
				}else{
					GUITools.messageDialog(this, "Please input correct mask value."); 
					return;
				}
			}
			byte[] maskBytes = GUITools.parseTextToHexBytes(this, "Mask", value, currentFlowEntry.ofFlowMod.getMatchList().get(selectedIndexInList4).getLength());
			
			if(null != maskBytes){
				list5Model.set(selectedIndexInList5, PList5.matchFieldString[4] + ": " + value);
				currentFlowEntry.ofFlowMod.getMatchList().get(selectedIndexInList4).setMask(maskBytes);
			}
		}else{
			setInstructionValue(value);
		}
		
		validate();
		repaint();
	}


	private void setInstructionValue(String newValue) {
		OFInstruction ins = currentFlowEntry.ofFlowMod.getInstructionList().get(instructionIndex);
		switch(ins.getType()){
			case GOTO_TABLE:
				setInstructionValue((OFInstructionGotoTable)ins, newValue);
				break;
			case GOTO_DIRECT_TABLE:
				setInstructionValue((OFInstructionGotoDirectTable)ins, newValue);
				break;
			case WRITE_METADATA:
				setInstructionValue((OFInstructionWriteMetadata)ins, newValue);
				break;
			case WRITE_METADATA_FROM_PACKET:
				setInstructionValue((OFInstructionWriteMetadataFromPacket)ins, newValue);
				break;
			default:
				return;
		}
	}

	private void setInstructionValue(final OFInstructionGotoTable ins, final String newValue) {
		if(!newValue.matches(GUITools.RE_DEC)){
			GUITools.messageDialog(this, "Please input correct offset value.");
			return;
		}
		
		DefaultListModel list5Model = (DefaultListModel) list5.getModel();
		list5Model.setElementAt(PList5.insGotoTableString[1] + ": " + newValue, 1);
		
		ins.setPacketOffset(Short.parseShort(newValue));
	}
	
	private void setInstructionValue(final OFInstructionGotoDirectTable ins, final String newValue) {
		if(!newValue.matches(GUITools.RE_DEC)){
			GUITools.messageDialog(this, "Please input correct offset value.");
			return;
		}
		DefaultListModel list5Model = (DefaultListModel) list5.getModel();
		list5Model.setElementAt(PList5.insGotoDirectTableString[2] + ": " + newValue, 2);
		
		ins.setPacketOffset(Short.parseShort(newValue));
	}

	private void setInstructionValue(final OFInstructionWriteMetadata ins, final String newValue) {
		int value;
		if(newValue.matches(GUITools.RE_0xHEX)){
			value = (int)Long.parseLong(newValue.substring(2), 16);
		}else if(newValue.matches(GUITools.RE_PMDEC)){
			value = (int)Long.parseLong(newValue);
		}else{
			GUITools.messageDialog(this, "Please input correct value."); 
			return;
		}
		
		DefaultListModel list5Model = (DefaultListModel) list5.getModel();
		list5Model.setElementAt(PList5.insWriteMetadataString[3] + ": " + value, 3);
		
		ins.setValue(value);
	}
	
	private void setInstructionValue(final OFInstructionWriteMetadataFromPacket ins, final String newValue) {
		if(!newValue.matches(GUITools.RE_DEC)){
			GUITools.messageDialog(this, "Please input correct packet offset."); 
			return;
		}
		
		short value = (short)Integer.parseInt(newValue);
		
		DefaultListModel list5Model = (DefaultListModel) list5.getModel();
		list5Model.setElementAt(PList5.insWriteMetadataFromPacketString[3] + ": " + newValue, 3);
		
		ins.setPacketOffset(value);
	}
	
	
	public void setValueOrMask(final FlowEntry currentFlowEntry) {
		this.currentFlowEntry = currentFlowEntry;
		
		String valueInList5 = String.valueOf(list5.getSelectedValue());
		int selectedIndexInList4 = list5.list4.getSelectedRow();
		
		String value = null;
		
		if(valueInList5.startsWith(PList5.matchFieldString[3])){
			value = HexString.toHex( currentFlowEntry.ofFlowMod.getMatchList().get(selectedIndexInList4).getValue() );
		}else if(valueInList5.startsWith(PList5.matchFieldString[4])){
			value = HexString.toHex( currentFlowEntry.ofFlowMod.getMatchList().get(selectedIndexInList4).getMask() );
			if(value == null || value.length() == 0 || value.equals("00")){
				String hexValueString = HexString.toHex( currentFlowEntry.ofFlowMod.getMatchList().get(selectedIndexInList4).getValue() );
				if(!hexValueString.equals("00")){
					hexValueString = (String)list5.listModel.getElementAt(3);
					int hexValueStartPos = (PList5.matchFieldString[3] + ": ").length();
					hexValueString = hexValueString.substring(hexValueStartPos);
					if(hexValueString.matches(GUITools.RE_HEX)){
						value = GUITools.getMaskStringFromBitLength(hexValueString.length() * 4);
						
						byte[] maskBytes = GUITools.parseTextToHexBytes(this, "Mask", value, currentFlowEntry.ofFlowMod.getMatchList().get(selectedIndexInList4).getLength());
						currentFlowEntry.ofFlowMod.getMatchList().get(selectedIndexInList4).setMask(maskBytes);
					}
				}
			}
			DefaultListModel list5Model = (DefaultListModel) list5.getModel();
			list5Model.set(list5.getSelectedIndex(), PList5.matchFieldString[4] + ": " + value);
		}
		showTextField(value);
	}
	
	public void showAllDefaultActions(final FlowEntry currentFlowEntry, final int instructionIndex) {
		this.currentFlowEntry = currentFlowEntry;
		this.instructionIndex = instructionIndex;
		
		notShowComboBoxOrTextField();
		
		clearData();
		
		DefaultListModel list5Model = (DefaultListModel) list5.getModel();
		if (list5Model.size() > 0) {
			for (OFActionType actionType : ACTIONS) {
				dm.addRow(new String[] { actionType.name() });
			}
		}
		
		validate();
		repaint();
	}
	
	public void showAction(final FlowEntry currentFlowEntry, final int instructionIndex, final int actionIndex) {
		this.currentFlowEntry = currentFlowEntry;		
		this.instructionIndex = instructionIndex;
		this.actionIndex = actionIndex;
		
		notShowComboBoxOrTextField();
		
		clearData();
		
		OFInstruction ins = currentFlowEntry.ofFlowMod.getInstructionList().get(instructionIndex);
		OFAction action = ((OFInstructionApplyActions)ins).getActionList().get(actionIndex);
		
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
		dm.addRow(new String[] { actionOutputString[0] + ": " + action.getPortId() });
		dm.addRow(new String[] { actionOutputString[1] + ": " + action.getMetadataOffset() });
		dm.addRow(new String[] { actionOutputString[2] + ": " + action.getMetadataLength() });
		dm.addRow(new String[] { actionOutputString[3] + ": " + action.getPacketOffset() });
	}
	
	private void showAction(final OFActionSetField action) {
		OFMatchX fieldX = action.getFieldSetting();

		if(fieldX.getFieldId() == IPMService.FIELDID_INVALID){
			dm.addRow(new String[] { actionSetFieldString[0] });
			dm.addRow(new String[] { actionSetFieldString[1] });
			dm.addRow(new String[] { actionSetFieldString[2] });
		}else{
			String value = HexString.toHex( fieldX.getValue() );
			String mask = HexString.toHex( fieldX.getMask() );
			dm.addRow(new String[] { actionSetFieldString[0] + ": " + fieldX.getFieldId() + "." + fieldX.getFieldName() });
			dm.addRow(new String[] { actionSetFieldString[1] + ": " + value });
			dm.addRow(new String[] { actionSetFieldString[2] + ": " + mask });
		}
	}
	
	private void showAction(final OFActionSetFieldFromMetadata action) {
		OFMatch20 field = action.getFieldSetting();
		if(field.getFieldId() == IPMService.FIELDID_INVALID){
			dm.addRow(new String[] { actionSetFieldFromMetadataString[0] });
		}else{
			dm.addRow(new String[] { actionSetFieldFromMetadataString[0] + ": " + field.getFieldId() + "." + field.getFieldName() });
		}
		dm.addRow(new String[] { actionSetFieldFromMetadataString[1] + ": " + action.getMetadataOffset() });
	}
	
	private void showAction(final OFActionModifyField action) {
		OFMatch20 field = action.getMatchField();
		if(field.getFieldId() == IPMService.FIELDID_INVALID){
			dm.addRow(new String[] { actionModFieldString[0] });
		}else{
			dm.addRow(new String[] { actionModFieldString[0] + ": " + field.getFieldId() + "." + field.getFieldName() });
		}
		dm.addRow(new String[] { actionModFieldString[1] + ": " + action.getIncrement() });
	}
	
	private void showAction(final OFActionAddField action) {
		short fieldID = action.getFieldId();
		if(fieldID == IPMService.FIELDID_INVALID){
			dm.addRow(new String[] { actionAddFieldString[0] });
			dm.addRow(new String[] { actionAddFieldString[1] });
		}else{
			String fieldName = SwingUIPanel.pofManager.iGetMatchField(fieldID).getFieldName();
			dm.addRow(new String[] { actionAddFieldString[0] + ": " + fieldID + "." + fieldName });
			dm.addRow(new String[] { actionAddFieldString[1] + ": " + fieldID});
		}

		dm.addRow(new String[] { actionAddFieldString[2] + ": " + action.getFieldPosition() });
		dm.addRow(new String[] { actionAddFieldString[3] + ": " + action.getFieldLength() });
		dm.addRow(new String[] { actionAddFieldString[4] + ": " + action.getFieldValue() });
	}
	
	private void showAction(final OFActionDeleteField action) {
		dm.addRow(new String[] { actionDelFieldString[0] + ": " + action.getFieldPosition() });
		dm.addRow(new String[] { actionDelFieldString[1] + ": " + action.getFieldLength() });
	}
	
	private void showAction(final OFActionCalculateCheckSum action) {
		dm.addRow(new String[] { actionCalCheckSumString[0] + ": " + action.getChecksumPosition() });
		dm.addRow(new String[] { actionCalCheckSumString[1] + ": " + action.getChecksumLength() });
		dm.addRow(new String[] { actionCalCheckSumString[2] + ": " + action.getCalcStartPosition() });
		dm.addRow(new String[] { actionCalCheckSumString[3] + ": " + action.getCalcLength() });
	}
	
	private void showAction(final OFActionGroup action) {
		int groupId = action.getGroupId();
		if(groupId == IPMService.GROUPID_INVALID){
			dm.addRow(new String[] { actionGroupString[0] });
		}else{
			dm.addRow(new String[] { actionGroupString[0] + ": " + action.getGroupId() });
		}
	}
	
	private void showAction(final OFActionDrop action) {
		dm.addRow(new String[] { actionDropString[0] + ": " + OFDropReason.values()[action.getReason()].name() });
	}

	private void showAction(final OFActionPacketIn action) {
		dm.addRow(new String[] { actionPacketInString[0] + ": " + OFPacketInReason.values()[action.getReason()] });
	}
	
	private void showAction(final OFActionCounter action) {
		int counterId = action.getCounterId();
		if(counterId == IPMService.COUNTERID_INVALID){
			counterId = SwingUIPanel.pofManager.iAllocateCounter(SwitchPanel.switchID);
			action.setCounterId(counterId);
		}
		dm.addRow(new String[] { actionCounterString[0] + ": " + counterId });
	}
	

	public static String getSubString(final String str, final String indexString){
		int n_pos;
		 
		n_pos = str.indexOf(indexString);
		
		if(n_pos == -1){
			return null;
		}
		 
		return str.substring(0, n_pos);
	}
	

	@Override
	protected void paintComponent(Graphics g) {
		JViewport viewport = (JViewport) this.getParent();
		g.drawImage(def_img, 0, 0, viewport.getWidth(), viewport.getHeight(),null);
		super.paintComponent(g);
	}

	@Override
	public Dimension getPreferredSize() {
		JViewport viewport = (JViewport) this.getParent();
		return new Dimension(viewport.getWidth(), viewport.getHeight());
	}

	@Override
	protected void paintBorder(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		int w = this.getWidth();
		int h = this.getHeight();

		g2d.setPaint(new Color(120, 120, 120));
		g2d.drawLine(w - 1, 0, w - 1, h);
	}
	
	class PList6CellEditor extends AbstractCellEditor implements TableCellEditor {

		@Override
		public Object getCellEditorValue() {
			if(displayComboBox == null){
				return null;
			}else if(displayComboBox == true){
				return comboBox.getSelectedItem();
			}else{
				return textfield.getText();
			}
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			if(displayComboBox == null){
				comboBox.setVisible(false);
				textfield.setVisible(false);
				
				return null;
			}else if(displayComboBox == true){
				comboBox.setVisible(true);
				textfield.setVisible(false);
				
				comboBox.setSelectedItem(value);
				return comboBox;
			}else{
				comboBox.setVisible(false);
				textfield.setVisible(true);
				
				textfield.setText(value == null ? "" : value.toString());
				return textfield;
			}
		}
	}
	
	class PList6CellRenderer extends DefaultTableCellRenderer {
		protected Image select_img = ImageUtils.getImageIconByCaches("image/gui/swing/list_select.png").getImage();
		protected Image def_img = ImageUtils.getImageIconByCaches("image/gui/swing/listbg.png").getImage();
		
		protected int row = -1;
		protected Object value;
		protected boolean isSelected = false;
		
		public PList6CellRenderer() {
			setOpaque(false);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			this.value = value;
			this.isSelected = isSelected;
			this.row = row;
			
			if(displayComboBox == null){
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}else if(displayComboBox == true){
				return comboBox;
			}else{
				return textfield;
			}
		}

		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			if (!isSelected) {
				g2d.drawImage(def_img, 0, 0, this.getWidth(), this.getHeight(), null);
			} else {
				g2d.drawImage(select_img, 0, 0, this.getWidth(), this.getHeight(), null);
			}
			int w = this.getWidth();
			int h = this.getHeight();
			g2d.setColor(HomeMain.packetLabelColor);
			g2d.drawRoundRect(2, 1, w - 5, h - 2, 3, 3);
			g2d.setColor(HomeMain.LabelColor);
			
			if(displayComboBox == null || displayComboBox == false){
				g2d.drawString("    " + value, 10, 2 + 15);
			}else {
				g2d.drawString("    " + comboBox.getSelectedItem(), 10, 2 + 15);
			}
		}
	}
}
