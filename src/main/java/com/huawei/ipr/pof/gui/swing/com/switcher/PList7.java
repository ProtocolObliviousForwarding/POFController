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

/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.math.BigInteger;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
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
import javax.swing.table.TableModel;

import org.openflow.protocol.OFGroupMod;
import org.openflow.protocol.OFMatch20;
import org.openflow.protocol.OFMatchX;
import org.openflow.protocol.OFPacketIn.OFPacketInReason;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionAddField;
import org.openflow.protocol.action.OFActionCalculateCheckSum;
import org.openflow.protocol.action.OFActionDeleteField;
import org.openflow.protocol.action.OFActionDrop;
import org.openflow.protocol.action.OFActionDrop.OFDropReason;
import org.openflow.protocol.action.OFActionGroup;
import org.openflow.protocol.action.OFActionModifyField;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.protocol.action.OFActionPacketIn;
import org.openflow.protocol.action.OFActionSetField;
import org.openflow.protocol.action.OFActionSetFieldFromMetadata;
import org.openflow.protocol.instruction.OFInstruction;
import org.openflow.protocol.instruction.OFInstructionApplyActions;
import org.openflow.util.HexString;

import com.huawei.ipr.pof.gui.comm.GUITools;
import com.huawei.ipr.pof.gui.swing.HomeMain;
import com.huawei.ipr.pof.gui.swing.SwingUIPanel;
import com.huawei.ipr.pof.gui.swing.comutil.FlowEntry;
import com.huawei.ipr.pof.gui.swing.comutil.ImageUtils;
import com.huawei.ipr.pof.gui.swing.comutil.PJComboBox;
import com.huawei.ipr.pof.manager.IPMService;

@SuppressWarnings("serial")
public class PList7 extends JTable {
	protected final Image def_img = ImageUtils.getImageIconByCaches("image/gui/swing/listbg.png").getImage();	
	
	protected DefaultTableModel dm;
	
	protected boolean isCellEditable = true;
	protected String[] data = {};
	protected JTextField textfield = new JTextField();
	protected JComboBox comboBox = new PJComboBox();
	protected Boolean displayComboBox = null;
	
	protected PList6 list6;
	
	protected FlowEntry currentFlowEntry;
	protected int instructionIndex;
	protected int actionIndex;
	
	private Vector<Vector<String>> strArray2Vector(String[] str) {
		Vector<Vector<String>> vector = new Vector<Vector<String>>();
		for (int i = 0; i < str.length; i++) {
			Vector<String> v = new Vector<String>();
			v.addElement(str[i]);
			vector.addElement(v);
		}
		return vector;
	}
	
	public void setOtherList(PList6 list6) {
		this.list6 = list6;
	}
	
	public PList7() {
		super();
		this.setOpaque(false);
		this.putClientProperty(GUITools.TERMINATE_EDIT_ON_FOCUS_LOST, Boolean.TRUE);

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
		tc.setCellRenderer(new PList7CellRenderer());
		tc.setCellEditor(new PList7CellEditor());
		setRowHeight(24);

		dm.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				int t = e.getType();
				if (TableModelEvent.UPDATE == t) {
					changeTextValue();
				}
			}
		});
		
		comboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				if (arg0.getStateChange() == ItemEvent.SELECTED) {
					if (arg0.getSource() == comboBox 
							&& comboBox.getSelectedIndex() != 0
							&& comboBox.getSelectedIndex() != -1) {						
						setValueFromCombo(comboBox.getSelectedIndex(), String.valueOf(comboBox.getSelectedItem()) );
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
		
		validate();
		repaint();
	}
	
	private void showTextField() {
		showTextField(null);
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
		
		validate();
		repaint();
	}
	
	private void showComboBox() {
		displayComboBox = true;
		
		comboBox.setVisible(true);
		textfield.setVisible(false);
		
		isCellEditable = true;

		dm.addRow(new Object[]{comboBox});
		
		validate();
		repaint();
	}

	private void setValueFromCombo(final int comboSelectedIndex, final String comboSelectedString){
		OFInstruction ins = currentFlowEntry.ofFlowMod.getInstructionList().get(instructionIndex);
		OFAction action = ((OFInstructionApplyActions)ins).getActionList().get(actionIndex);
		
		switch(action.getType()){
			case OUTPUT:
				setValueFromCombo((OFActionOutput)action, comboSelectedString);
				break;
			case SET_FIELD:
				setValueFromCombo((OFActionSetField)action, comboSelectedString);
				break;
			case SET_FIELD_FROM_METADATA:
				setValueFromCombo((OFActionSetFieldFromMetadata)action, comboSelectedString);
				break;
			case MODIFY_FIELD:
				setValueFromCombo((OFActionModifyField)action, comboSelectedString);
				break;
			case ADD_FIELD:
				setValueFromCombo((OFActionAddField)action, comboSelectedString);
				break;
			case GROUP:
				setValueFromCombo((OFActionGroup)action, comboSelectedString);
				break;
			case DROP:
				setValueFromCombo((OFActionDrop)action, comboSelectedIndex, comboSelectedString);
				break;
			case PACKET_IN:
				setValueFromCombo((OFActionPacketIn)action, comboSelectedIndex, comboSelectedString);
				break;
			

			case DELETE_FIELD:
			case CALCULATE_CHECKSUM:
			case COUNTER:
			default:
				return;
		}
		
		validate();
		repaint();
	}
	
	private void setValueFromCombo(final OFActionOutput action, final String comboSelectedString) {
		if(comboSelectedString.matches(GUITools.RE_DEC)){
			int portID = Integer.parseInt(comboSelectedString);
			action.setPortId(portID);
			
			TableModel list6Model = list6.getModel();
			list6Model.setValueAt(PList6.actionOutputString[0] + ": " + portID, 0, 0);
		}
	}
	
	private void setValueFromCombo(final OFActionSetField action, final String comboSelectedString) {
		String fieldIDString = PList6.getSubString(comboSelectedString, ".");
		
		if(fieldIDString != null && fieldIDString.matches(GUITools.RE_DEC)){
			byte fieldID = (byte)Integer.parseInt(fieldIDString);
			if(fieldID != IPMService.FIELDID_INVALID){
				OFMatch20 field = SwingUIPanel.pofManager.iGetMatchField(fieldID);
				OFMatchX fieldX = action.getFieldSetting();
				fieldX.setFieldId(fieldID);
				fieldX.setFieldName(field.getFieldName());
				fieldX.setOffset(field.getOffset());
				fieldX.setLength(field.getLength());
				fieldX.setValue(new byte[]{ 0 });
				fieldX.setMask(new byte[]{ 0 });
				
				TableModel list6Model = list6.getModel();
				list6Model.setValueAt(PList6.actionAddFieldString[0] + ": " + fieldID + "." + field.getFieldName(), 0, 0);
			}
		}
	}

	private void setValueFromCombo(final OFActionSetFieldFromMetadata action, final String comboSelectedString) {
		String fieldIDString = PList6.getSubString(comboSelectedString, ".");
		
		if(fieldIDString != null && fieldIDString.matches(GUITools.RE_DEC)){
			byte fieldID = (byte)Integer.parseInt(fieldIDString);
			if(fieldID != IPMService.FIELDID_INVALID){
				OFMatch20 field = SwingUIPanel.pofManager.iGetMatchField(fieldID);
				OFMatch20 fieldinaction = action.getFieldSetting();
				fieldinaction.setFieldId(fieldID);
				fieldinaction.setFieldName(field.getFieldName());
				fieldinaction.setOffset(field.getOffset());
				fieldinaction.setLength(field.getLength());
				
				TableModel list6Model = list6.getModel();
				list6Model.setValueAt(PList6.actionSetFieldFromMetadataString[0] + ": " + fieldID + "." + field.getFieldName(), 0, 0);
			}
		}
	}
	
	private void setValueFromCombo(final OFActionModifyField action, final String comboSelectedString) {
		String fieldIDString = PList6.getSubString(comboSelectedString, ".");
		
		if(fieldIDString != null && fieldIDString.matches(GUITools.RE_DEC)){
			byte fieldID = (byte)Integer.parseInt(fieldIDString);
			if(fieldID != IPMService.FIELDID_INVALID){
				OFMatch20 field = SwingUIPanel.pofManager.iGetMatchField(fieldID);
				OFMatch20 fieldinaction = action.getMatchField();
				fieldinaction.setFieldId(fieldID);
				fieldinaction.setFieldName(field.getFieldName());
				fieldinaction.setOffset(field.getOffset());
				fieldinaction.setLength(field.getLength());
				
				TableModel list6Model = list6.getModel();
				list6Model.setValueAt(PList6.actionModFieldString[0] + ": " + fieldID + "." + field.getFieldName(), 0, 0);
			}
		}
	}
	
	private void setValueFromCombo(final OFActionAddField action, final String comboSelectedString) {
		//clear
		if(comboSelectedString.equals("CLEAR")){
			action.setFieldId(IPMService.FIELDID_INVALID);
			action.setFieldPosition((short) 0);
			action.setFieldLength(0);
			
			TableModel list6Model = list6.getModel();
			list6Model.setValueAt(PList6.actionAddFieldString[0], 0, 0);
			list6Model.setValueAt(PList6.actionAddFieldString[1], 1, 0);
			list6Model.setValueAt(PList6.actionAddFieldString[2], 2, 0);
			list6Model.setValueAt(PList6.actionAddFieldString[3], 3, 0);

			return;
		}
		
		String fieldIDString = PList6.getSubString(comboSelectedString, ".");
		
		if(fieldIDString != null && fieldIDString.matches(GUITools.RE_DEC)){
			byte fieldID = (byte)Integer.parseInt(fieldIDString);
			if(fieldID != IPMService.FIELDID_INVALID){
				OFMatch20 field = SwingUIPanel.pofManager.iGetMatchField(fieldID);
				action.setFieldId(fieldID);
				action.setFieldPosition(field.getOffset());
				action.setFieldLength(field.getLength());
				
				TableModel list6Model = list6.getModel();
				list6Model.setValueAt(PList6.actionAddFieldString[0], 0, 0);
				list6Model.setValueAt(PList6.actionAddFieldString[1] + ": " + fieldID, 1, 0);
				list6Model.setValueAt(PList6.actionAddFieldString[2] + ": " + action.getFieldPosition(), 2, 0);
				list6Model.setValueAt(PList6.actionAddFieldString[3] + ": " + action.getFieldLength(), 3, 0);
			}
		}
		
	}
	
	private void setValueFromCombo(final OFActionGroup action, final String comboSelectedString) {
		if(comboSelectedString.matches(GUITools.RE_DEC)){
			int groupID = Integer.parseInt(comboSelectedString);
			action.setGroupId(groupID);
			
			TableModel list6Model = list6.getModel();
			list6Model.setValueAt(PList6.actionGroupString[0] + ": " + groupID, 0, 0);
		}
	}
	
	private void setValueFromCombo(final OFActionDrop action, final int comboSelectedIndex, final String comboSelectedString) {
		action.setReason(comboSelectedIndex - 1);
		
		TableModel list6Model = list6.getModel();
		list6Model.setValueAt(PList6.actionDropString[0] + ": " + comboSelectedString, 0, 0);
	}
	
	private void setValueFromCombo(final OFActionPacketIn action, final int comboSelectedIndex, final String comboSelectedString) {
		action.setReason(comboSelectedIndex - 1);
		
		TableModel list6Model = list6.getModel();
		list6Model.setValueAt(PList6.actionPacketInString[0] + ": " + comboSelectedString, 0, 0);
	}


	public void showAction(FlowEntry currentFlowEntry, int instructionIndex, int actionIndex) {
		this.currentFlowEntry = currentFlowEntry;
		this.instructionIndex = instructionIndex;
		this.actionIndex = actionIndex;
		
		clearData();
		
		isCellEditable = true;
		
		OFInstruction ins = currentFlowEntry.ofFlowMod.getInstructionList().get(instructionIndex);
		OFAction action = ((OFInstructionApplyActions)ins).getActionList().get(actionIndex);
		
		int selectedIndexInList6 = list6.getSelectedRow();
		switch(action.getType()){
			case OUTPUT:
				showAction((OFActionOutput)action, selectedIndexInList6);
				break;
			case SET_FIELD:
				showAction((OFActionSetField)action, selectedIndexInList6);
				break;
			case SET_FIELD_FROM_METADATA:
				showAction((OFActionSetFieldFromMetadata)action, selectedIndexInList6);
				break;
			case MODIFY_FIELD:
				showAction((OFActionModifyField)action, selectedIndexInList6);
				break;
			case ADD_FIELD:
				showAction((OFActionAddField)action, selectedIndexInList6);
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
			default:
				return;
		}
		
		validate();
		repaint();
	}
	
	private void showAction(final OFActionOutput action, final int selectedIndexInList6) {
		if(selectedIndexInList6 == 0){
			comboBox.addItem("");
			List<Integer> portIDList = SwingUIPanel.pofManager.iGetAllPortId(SwitchPanel.switchID);
			for (int portID : portIDList) {
				comboBox.addItem(String.valueOf(portID));
			}
			
			showComboBox();
		}else{
			showTextField();
		}
	}
	
	private void showAction(final OFActionSetField action, final int selectedIndexInList6) {
		String value = null;
		
		if(selectedIndexInList6 == 0){
			comboBox.addItem("");
			for (OFMatch20 field : SwingUIPanel.pofManager.iGetAllField()) {
				short fieldID = field.getFieldId();
				comboBox.addItem(String.valueOf(fieldID) + "." + field.getFieldName());
			}
			
			showComboBox();
		}else{
			if(selectedIndexInList6 == 1){
				value = HexString.toHex( action.getFieldSetting().getValue() );
			}else{
				value = HexString.toHex( action.getFieldSetting().getMask() );
				if(value == null || value.length() == 0 || value.equals("00")){
					String hexValueString = HexString.toHex( action.getFieldSetting().getValue() );
					if(!hexValueString.equals("00")){
						hexValueString = (String)list6.getValueAt(1, 0);
						int hexValueStartPos = (PList6.actionSetFieldString[1] + ": ").length();
						hexValueString = hexValueString.substring(hexValueStartPos);
						if(hexValueString.matches(GUITools.RE_HEX)){
							value = GUITools.getMaskStringFromBitLength(hexValueString.length() * 4);
							
							byte[] maskBytes = GUITools.parseTextToHexBytes(this, "Mask", value, action.getFieldSetting().getLength());
							if(maskBytes != null){
								action.getFieldSetting().setMask(maskBytes);
							}
						}
					}
				}
			}
			
			showTextField(value);
			
			TableModel list6Model = list6.getModel();
			list6Model.setValueAt(PList6.actionSetFieldString[selectedIndexInList6] + ": " + value, selectedIndexInList6, 0);
		}
	}
	
	private void showAction(final OFActionSetFieldFromMetadata action, final int selectedIndexInList6) {
		if(selectedIndexInList6 == 0){
			comboBox.addItem("");
			for (OFMatch20 field : SwingUIPanel.pofManager.iGetAllField()) {
				short fieldID = field.getFieldId();
				comboBox.addItem(String.valueOf(fieldID) + "." + field.getFieldName());
			}
			
			showComboBox();
		}else{
			showTextField();
		}
	}
	
	private void showAction(final OFActionModifyField action, final int selectedIndexInList6) {
		if(selectedIndexInList6 == 0){
			comboBox.addItem("");
			for (OFMatch20 field : SwingUIPanel.pofManager.iGetAllField()) {
				short fieldID = field.getFieldId();
				comboBox.addItem(String.valueOf(fieldID) + "." + field.getFieldName());
			}
			
			showComboBox();
		}else{
			showTextField();
		}
	}
	
	private void showAction(final OFActionAddField action, final int selectedIndexInList6) {
		if(selectedIndexInList6 == 1){
			comboBox.addItem("");
			comboBox.addItem("CLEAR");
			for (OFMatch20 field : SwingUIPanel.pofManager.iGetAllField()) {
				short fieldID = field.getFieldId();
				comboBox.addItem(String.valueOf(fieldID) + "." + field.getFieldName());
			}
			
			showComboBox();
		}else{
			if(selectedIndexInList6  == 4){
				showTextField();
			}else{
				if(action.getFieldId() == IPMService.FIELDID_INVALID){
					showTextField();
				}
			}
		}
	}
	
	private void showAction(final OFActionDeleteField action) {
		showTextField();
	}
	
	private void showAction(final OFActionCalculateCheckSum action) {
		showTextField();
	}
	
	private void showAction(final OFActionGroup action) {
		comboBox.addItem("");
		List<OFGroupMod> ofGroupList = SwingUIPanel.pofManager.iGetAllGroups(SwitchPanel.switchID);
		for (OFGroupMod ofGroup : ofGroupList) {
			comboBox.addItem(String.valueOf(ofGroup.getGroupId()));
		}
		
		showComboBox();
	}
	
	private void showAction(final OFActionDrop action) {
		comboBox.addItem("");
		for (OFDropReason reasonType : OFDropReason.values()) {
			comboBox.addItem(reasonType.toString());
		}
		
		showComboBox();
	}
	
	private void showAction(final OFActionPacketIn action) {
		comboBox.addItem("");
		for (OFPacketInReason reasonType : OFPacketInReason.values()) {
			comboBox.addItem(reasonType.toString());
		}
		
		showComboBox();
	}
	
	protected void changeTextValue() {
		if(displayComboBox == null || displayComboBox == true){
			return;
		}
		
		String value = (String)dm.getValueAt(0, 0);
		
		if (null == value || 0 == value.length()) {
			return;
		}
		
		setActionValue(value);
		
		validate();
		repaint();
	}

	private void setActionValue(String newValue) {
		OFInstruction ins = currentFlowEntry.ofFlowMod.getInstructionList().get(instructionIndex);
		OFAction action = ((OFInstructionApplyActions)ins).getActionList().get(actionIndex);
		
		int selectedIndexInList6 = list6.getSelectedRow();

		switch(action.getType()){
			case OUTPUT:
				setActionValue((OFActionOutput)action, newValue, selectedIndexInList6);
				break;
			case SET_FIELD:
				setActionValue((OFActionSetField)action, newValue, selectedIndexInList6);
				break;
			case SET_FIELD_FROM_METADATA:
				setActionValue((OFActionSetFieldFromMetadata)action, newValue, selectedIndexInList6);
				break;
			case MODIFY_FIELD:
				setActionValue((OFActionModifyField)action, newValue, selectedIndexInList6);
				break;
			case ADD_FIELD:
				setActionValue((OFActionAddField)action, newValue, selectedIndexInList6);
				break;
			case DELETE_FIELD:
				setActionValue((OFActionDeleteField)action, newValue, selectedIndexInList6);
				break;				
			case CALCULATE_CHECKSUM:
				setActionValue((OFActionCalculateCheckSum)action, newValue, selectedIndexInList6);
				break;
			case GROUP:
			case DROP:
			case PACKET_IN:
			case COUNTER:
			default:
				return;
		}
	}

	private void setActionValue(final OFActionOutput action, final String newValue, final int selectedIndexInList6) {
		if(!newValue.matches(GUITools.RE_DEC)){
			GUITools.messageDialog(this, "Please input correct value for " + PList6.actionOutputString[selectedIndexInList6]);
			return;
		}
		
		short value = (short)Integer.parseInt(newValue);
		
		
		if(selectedIndexInList6 == 1){
			action.setMetadataOffset(value);
		}else if(selectedIndexInList6 == 2){
			action.setMetadataLength(value);
		}else if(selectedIndexInList6 == 3){
			action.setPacketOffset(value);
		}
		
		TableModel list6Model = list6.getModel();
		list6Model.setValueAt(PList6.actionOutputString[selectedIndexInList6] + ": " + value, selectedIndexInList6, 0);
	}

	private void setActionValue(final OFActionSetField action, final String newValue, final int selectedIndexInList6) {
		short maxLength = action.getFieldSetting().getLength();
		
		String value = newValue;
		
		if(!value.matches(GUITools.RE_HEX)){
			if(value.matches(GUITools.RE_0xHEX)){
				value = value.substring(2);
			}else{
				GUITools.messageDialog(this, "Please input correct value for " + PList6.actionSetFieldString[selectedIndexInList6]); 
				return;
			}
		}
		
		if(selectedIndexInList6 == 1){
			byte[] valueBytes = GUITools.parseTextToHexBytes(this, "Value", value, maxLength);
			if(null == valueBytes){
				return;
			}
			action.getFieldSetting().setValue(valueBytes);
		}else if(selectedIndexInList6 == 2){
			byte[] maskBytes = GUITools.parseTextToHexBytes(this, "Mask", value, maxLength);
			if(null != maskBytes){
				return;
			}
			action.getFieldSetting().setMask(maskBytes);
		}
		
		TableModel list6Model = list6.getModel();
		list6Model.setValueAt(PList6.actionSetFieldString[selectedIndexInList6] + ": " + value, selectedIndexInList6, 0);
	}

	private void setActionValue(final OFActionSetFieldFromMetadata action, final String newValue, final int selectedIndexInList6) {
		if(!newValue.matches(GUITools.RE_DEC)){
			GUITools.messageDialog(this, "Please input correct value for " + PList6.actionSetFieldFromMetadataString[selectedIndexInList6]); 
			return;
		}
		
		short value = (short)Integer.parseInt(newValue);
		
		action.setMetadataOffset(value);
		
		TableModel list6Model = list6.getModel();
		list6Model.setValueAt(PList6.actionSetFieldFromMetadataString[selectedIndexInList6] + ": " + value, selectedIndexInList6, 0);
	}

	private void setActionValue(final OFActionModifyField action, final String newValue, final int selectedIndexInList6) {
		if(!newValue.matches(GUITools.RE_PMDEC)){
			GUITools.messageDialog(this, "Please input correct value for " + PList6.actionModFieldString[selectedIndexInList6]); 
			return;
		}
		
		int value = (int)Long.parseLong(newValue);
		
		action.setIncrement(value);
		
		TableModel list6Model = list6.getModel();
		list6Model.setValueAt(PList6.actionModFieldString[selectedIndexInList6] + ": " + value, selectedIndexInList6, 0);
	}

	private void setActionValue(final OFActionAddField action, final String newValue, final int selectedIndexInList6) {
		String value = "";
		short fieldID;
		switch (selectedIndexInList6){
			case 0:
				if(newValue == null || newValue.isEmpty()){
					GUITools.messageDialog(this, "Please input correct field name.");
					return;
				}
				
				fieldID = action.getFieldId();
				
				if(fieldID == IPMService.FIELDID_INVALID){
					if(action.getFieldLength() == 0){
						GUITools.messageDialog(this, "Please input field length, field length can not be 0.");
						return;
					}
					fieldID = SwingUIPanel.pofManager.iNewField(newValue, (short)action.getFieldLength(), action.getFieldPosition());
					action.setFieldId(fieldID);
					
					value = fieldID + "." + newValue;
				}else{
					OFMatch20 field = SwingUIPanel.pofManager.iGetMatchField( fieldID );
					field.setFieldName(newValue);
					
					value = fieldID + "." + newValue;
				}
				break;
			case 2:
				if(!newValue.matches(GUITools.RE_DEC)){
					GUITools.messageDialog(this, "Please input correct field offset.");
					return;
				}
				
				short posValue = (short)Integer.parseInt(newValue);
				
				action.setFieldPosition(posValue);
				
				fieldID = action.getFieldId();
				if(fieldID != IPMService.FIELDID_INVALID){
					OFMatch20 field = SwingUIPanel.pofManager.iGetMatchField( fieldID );
					field.setOffset(posValue);
				}
				
				value = newValue;
				break;
			case 3:
				if(!newValue.matches(GUITools.RE_DEC)){
					GUITools.messageDialog(this, "Please input correct length.");
					return;
				}
				
				short lengthValue = (short)Integer.parseInt(newValue);
				
				action.setFieldLength(lengthValue);
				
				fieldID = action.getFieldId();
				if(fieldID != IPMService.FIELDID_INVALID){
					OFMatch20 field = SwingUIPanel.pofManager.iGetMatchField( fieldID );
					field.setLength(lengthValue);
				}
				
				value = newValue;
				break;
			case 4:
				long longValue;
				if(newValue.matches(GUITools.RE_0xHEX)){
					longValue = new BigInteger(newValue.substring(2), 16).longValue();
				}else if(newValue.matches(GUITools.RE_PMDEC)){
					longValue = new BigInteger(newValue).longValue();
				}else{
					GUITools.messageDialog(this, "Please input correct value."); 
					return;
				}
				action.setFieldValue(longValue);
				value = action.getFieldValue() + "";
				
				break;
		}
		
		TableModel list6Model = list6.getModel();
		list6Model.setValueAt(PList6.actionAddFieldString[selectedIndexInList6] + ": " + value, selectedIndexInList6, 0);
	}

	private void setActionValue(final OFActionDeleteField action, final String newValue, final int selectedIndexInList6) {
		if(!newValue.matches(GUITools.RE_DEC)){
			GUITools.messageDialog(this, "Please input correct value for " + PList6.actionDelFieldString[selectedIndexInList6]); 
			return;
		}
		
		short value = (short)Integer.parseInt(newValue);
		
		if(selectedIndexInList6 == 0){
			action.setFieldPosition(value);
		}else if(selectedIndexInList6 == 1){
			action.setFieldLength(value);
		}
		
		TableModel list6Model = list6.getModel();
		list6Model.setValueAt(PList6.actionDelFieldString[selectedIndexInList6] + ": " + value, selectedIndexInList6, 0);
	}

	private void setActionValue(final OFActionCalculateCheckSum action, final String newValue, final int selectedIndexInList6) {
		if(!newValue.matches(GUITools.RE_DEC)){
			GUITools.messageDialog(this, "Please input correct value for " + PList6.actionDelFieldString[selectedIndexInList6]); 
			return;
		}
		
		short value = (short)Integer.parseInt(newValue);
		
		if(selectedIndexInList6 == 0){
			action.setChecksumPosition(value);
		}else if(selectedIndexInList6 == 1){
			action.setChecksumLength(value);
		}else if(selectedIndexInList6 == 2){
			action.setCalcStartPosition(value);
		}else if(selectedIndexInList6 == 3){
			action.setCalcLength(value);
		}
		
		TableModel list6Model = list6.getModel();
		list6Model.setValueAt(PList6.actionCalCheckSumString[selectedIndexInList6] + ": " + value, selectedIndexInList6, 0);
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
	
	class PList7CellEditor extends AbstractCellEditor implements TableCellEditor {
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

	class PList7CellRenderer extends DefaultTableCellRenderer {
		protected Image select_img = ImageUtils.getImageIconByCaches("image/gui/swing/list_select.png").getImage();
		protected Image def_img = ImageUtils.getImageIconByCaches("image/gui/swing/listbg.png").getImage();
		protected int row = -1;
		protected Object value;
		protected boolean isSelected = false;
		
		public PList7CellRenderer() {
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