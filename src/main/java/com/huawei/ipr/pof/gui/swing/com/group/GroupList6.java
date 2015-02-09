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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigInteger;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultListModel;
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
import org.openflow.util.HexString;

import com.huawei.ipr.pof.gui.comm.GUITools;
import com.huawei.ipr.pof.gui.comm.GUITools.EDIT_STATUS;
import com.huawei.ipr.pof.gui.swing.HomeMain;
import com.huawei.ipr.pof.gui.swing.SwingUIPanel;
import com.huawei.ipr.pof.gui.swing.com.switcher.PList6;
import com.huawei.ipr.pof.gui.swing.com.switcher.SwitchPanel;
import com.huawei.ipr.pof.gui.swing.comutil.GroupEntry;
import com.huawei.ipr.pof.gui.swing.comutil.ImageUtils;
import com.huawei.ipr.pof.manager.IPMService;

/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
@SuppressWarnings("serial")
public class GroupList6 extends JTable{
	protected Image def_img = ImageUtils.getImageIconByCaches("image/gui/swing/listbg.png").getImage();
	
	protected boolean isCellEditable = false;
	protected String[] data = {};
	protected JTextField textfield = new JTextField();
	protected boolean displayTextfield = false;
   
	protected DefaultTableModel dm;	

	protected GroupList5 grouplist5;
	
	protected GroupEntry currentGroupEntry;
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
	
	public void setOtherList(GroupList5 grouplist5) {
		this.grouplist5 = grouplist5;
	}

	public GroupList6() {
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
		tc.setCellRenderer(new GroupList6CellRenderer());
		tc.setCellEditor(new GroupList6CellEditor());
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
	}
	
	protected void selectedValueChanged() {
		int selectedRow = this.getSelectedRow();
		
		if(selectedRow == -1){
			return;
		}
		
		if(currentGroupEntry.editStatus == EDIT_STATUS.ES_READONLY){
			return;
		}
		
		if(displayTextfield == true){
			return;
		}
		
		String value = String.valueOf( getValueAt(selectedRow, 0) );
		
		if(value != null && !value.isEmpty()){
			setValueFromSelection(selectedRow, value);
		}
		
		validate();
		repaint();
	}
	
	protected void setValueFromSelection(final int selectedIndex, final String selectedString){
		OFAction action = currentGroupEntry.ofGroupMod.getActionList().get(actionIndex);
		
		switch(action.getType()){
			case OUTPUT:
				setValueFromSelection((OFActionOutput)action, selectedString);
				break;
			case SET_FIELD:
				setValueFromSelection((OFActionSetField)action, selectedString);
				break;
			case SET_FIELD_FROM_METADATA:
				setValueFromSelection((OFActionSetFieldFromMetadata)action, selectedString);
				break;
			case MODIFY_FIELD:
				setValueFromSelection((OFActionModifyField)action, selectedString);
				break;
			case ADD_FIELD:
				setValueFromSelection((OFActionAddField)action, selectedString);
				break;
			case GROUP:
				setValueFromSelection((OFActionGroup)action, selectedString);
				break;
			case DROP:
				setValueFromSelection((OFActionDrop)action, selectedIndex, selectedString);
				break;
			case PACKET_IN:
				setValueFromSelection((OFActionPacketIn)action, selectedIndex, selectedString);
				break;
				
			case DELETE_FIELD:
			case CALCULATE_CHECKSUM:
			case COUNTER:
			default:
				return;
		}
	}
	

	private void setValueFromSelection(final OFActionOutput action, final String comboSelectedString) {
		if(comboSelectedString.matches(GUITools.RE_DEC)){
			int portID = Integer.parseInt(comboSelectedString);
			action.setPortId(portID);
			
			DefaultListModel list5Model = (DefaultListModel)grouplist5.getModel();
			list5Model.setElementAt(GroupList5.actionOutputString[0] + ": " + portID, 0);
		}
	}
	
	private void setValueFromSelection(final OFActionSetField action, final String comboSelectedString) {
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
				
				DefaultListModel list5Model = (DefaultListModel)grouplist5.getModel();
				list5Model.setElementAt(GroupList5.actionAddFieldString[0] + ": " + fieldID + "." + field.getFieldName(), 0);
			}
		}
	}

	private void setValueFromSelection(final OFActionSetFieldFromMetadata action, final String comboSelectedString) {
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
				
				DefaultListModel list5Model = (DefaultListModel)grouplist5.getModel();
				list5Model.setElementAt(GroupList5.actionSetFieldFromMetadataString[0] + ": " + fieldID + "." + field.getFieldName(), 0);
			}
		}
	}
	
	private void setValueFromSelection(final OFActionModifyField action, final String comboSelectedString) {
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
				
				DefaultListModel list5Model = (DefaultListModel)grouplist5.getModel();
				list5Model.setElementAt(GroupList5.actionModFieldString[0] + ": " + fieldID + "." + field.getFieldName(), 0);
			}
		}
	}
	
	
	private void setValueFromSelection(final OFActionAddField action, final String comboSelectedString) {
		//clear
		if(comboSelectedString.equals("CLEAR")){
			action.setFieldId(IPMService.FIELDID_INVALID);
			action.setFieldPosition((short) 0);
			action.setFieldLength(0);
			
			DefaultListModel list5Model = (DefaultListModel)grouplist5.getModel();
			list5Model.setElementAt(GroupList5.actionAddFieldString[0], 0);
			list5Model.setElementAt(GroupList5.actionAddFieldString[1], 1);
			list5Model.setElementAt(GroupList5.actionAddFieldString[2], 2);
			list5Model.setElementAt(GroupList5.actionAddFieldString[3], 3);

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
				
				DefaultListModel list5Model = (DefaultListModel)grouplist5.getModel();
				list5Model.setElementAt(GroupList5.actionAddFieldString[0], 0);
				list5Model.setElementAt(GroupList5.actionAddFieldString[1] + ": " + fieldID + "." + field.getFieldName(), 1);
				list5Model.setElementAt(GroupList5.actionAddFieldString[2] + ": " + action.getFieldPosition(), 2);
				list5Model.setElementAt(GroupList5.actionAddFieldString[3] + ": " + action.getFieldLength(), 3);
			}
		}
		
	}

	
	private void setValueFromSelection(final OFActionGroup action, final String comboSelectedString) {
		if(comboSelectedString.matches(GUITools.RE_DEC)){
			int groupID = Integer.parseInt(comboSelectedString);
			action.setGroupId(groupID);
			
			DefaultListModel list5Model = (DefaultListModel)grouplist5.getModel();
			list5Model.setElementAt(GroupList5.actionGroupString[0] + ": " + groupID, 0);
		}
	}
	
	private void setValueFromSelection(final OFActionDrop action, final int comboSelectedIndex, final String comboSelectedString) {
		action.setReason(comboSelectedIndex - 1);
		
		DefaultListModel list5Model = (DefaultListModel)grouplist5.getModel();
		list5Model.setElementAt(GroupList5.actionDropString[0] + ": " + comboSelectedString, 0);
	}
	
	private void setValueFromSelection(final OFActionPacketIn action, final int comboSelectedIndex, final String comboSelectedString) {
		action.setReason(comboSelectedIndex - 1);
		
		DefaultListModel list5Model = (DefaultListModel)grouplist5.getModel();
		list5Model.setElementAt(GroupList5.actionPacketInString[0] + ": " + comboSelectedString, 0);
	}
	
	protected void changeTextValue() {
		if(displayTextfield == false){
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
		OFAction action = currentGroupEntry.ofGroupMod.getActionList().get(actionIndex);
		
		int selectedIndexInList5 = grouplist5.getSelectedIndex();

		switch(action.getType()){
			case OUTPUT:
				setActionValue((OFActionOutput)action, newValue, selectedIndexInList5);
				break;
			case SET_FIELD:
				setActionValue((OFActionSetField)action, newValue, selectedIndexInList5);
				break;
			case SET_FIELD_FROM_METADATA:
				setActionValue((OFActionSetFieldFromMetadata)action, newValue, selectedIndexInList5);
				break;
			case MODIFY_FIELD:
				setActionValue((OFActionModifyField)action, newValue, selectedIndexInList5);
				break;
			case ADD_FIELD:
				setActionValue((OFActionAddField)action, newValue, selectedIndexInList5);
				break;
			case DELETE_FIELD:
				setActionValue((OFActionDeleteField)action, newValue, selectedIndexInList5);
				break;				
			case CALCULATE_CHECKSUM:
				setActionValue((OFActionCalculateCheckSum)action, newValue, selectedIndexInList5);
				break;
			case GROUP:
			case DROP:
			case PACKET_IN:
			case COUNTER:
			default:
				return;
		}
	}
	
	private void setActionValue(final OFActionOutput action, final String newValue, final int selectedIndexInList5) {
		if(!newValue.matches(GUITools.RE_DEC)){
			GUITools.messageDialog(this, "Please input correct value for " + GroupList5.actionOutputString[selectedIndexInList5]);
			return;
		}
		
		short value = (short)Integer.parseInt(newValue);
		
		
		if(selectedIndexInList5 == 1){
			action.setMetadataOffset(value);
		}else if(selectedIndexInList5 == 2){
			action.setMetadataLength(value);
		}else if(selectedIndexInList5 == 3){
			action.setPacketOffset(value);
		}
		
		DefaultListModel list5Model = (DefaultListModel)grouplist5.getModel();
		list5Model.setElementAt(GroupList5.actionOutputString[selectedIndexInList5] + ": " + value, selectedIndexInList5);
	}

	private void setActionValue(final OFActionSetField action, final String newValue, final int selectedIndexInList5) {
		short maxLength = action.getFieldSetting().getLength();
		
		String value = newValue;
		
		if(!value.matches(GUITools.RE_HEX)){
			if(value.matches(GUITools.RE_0xHEX)){
				value = value.substring(2);
			}else{
				GUITools.messageDialog(this, "Please input correct value for " + GroupList5.actionSetFieldString[selectedIndexInList5]); 
				return;
			}
		}
		
		if(selectedIndexInList5 == 1){
			byte[] valueBytes = GUITools.parseTextToHexBytes(this, "Value", value, maxLength);
			if(null == valueBytes){
				return;
			}
			action.getFieldSetting().setValue(valueBytes);
		}else if(selectedIndexInList5 == 2){
			byte[] maskBytes = GUITools.parseTextToHexBytes(this, "Mask", value, maxLength);
			if(null != maskBytes){
				return;
			}
			action.getFieldSetting().setMask(maskBytes);
		}
		
		DefaultListModel list5Model = (DefaultListModel)grouplist5.getModel();
		list5Model.setElementAt(GroupList5.actionSetFieldString[selectedIndexInList5] + ": " + value, selectedIndexInList5);
	}

	private void setActionValue(final OFActionSetFieldFromMetadata action, final String newValue, final int selectedIndexInList5) {
		if(!newValue.matches(GUITools.RE_DEC)){
			GUITools.messageDialog(this, "Please input correct value for " + GroupList5.actionSetFieldFromMetadataString[selectedIndexInList5]); 
			return;
		}
		
		short value = (short)Integer.parseInt(newValue);
		
		action.setMetadataOffset(value);
		
		DefaultListModel list5Model = (DefaultListModel)grouplist5.getModel();
		list5Model.setElementAt(GroupList5.actionSetFieldFromMetadataString[selectedIndexInList5] + ": " + value, selectedIndexInList5);
	}

	private void setActionValue(final OFActionModifyField action, final String newValue, final int selectedIndexInList5) {
		if(!newValue.matches(GUITools.RE_PMDEC)){
			GUITools.messageDialog(this, "Please input correct value for " + GroupList5.actionModFieldString[selectedIndexInList5]); 
			return;
		}
		
		int value = (int)Long.parseLong(newValue);
		
		action.setIncrement(value);
		
		DefaultListModel list5Model = (DefaultListModel)grouplist5.getModel();
		list5Model.setElementAt(GroupList5.actionModFieldString[selectedIndexInList5] + ": " + value, selectedIndexInList5);
	}

	private void setActionValue(final OFActionAddField action, final String newValue, final int selectedIndexInList5) {
		String value = "";
		short fieldID;
		switch (selectedIndexInList5){
			case 0:
				if(newValue == null || newValue.isEmpty()){
					GUITools.messageDialog(this, "Please input correct field name.");
					return;
				}
				
				fieldID = action.getFieldId();
				
				if(fieldID == IPMService.FIELDID_INVALID){
					fieldID = SwingUIPanel.pofManager.iNewField(newValue, (short)action.getFieldLength(), action.getFieldPosition());
					action.setFieldId(fieldID);
					
					value = fieldID + "." + newValue;
				}else{
					OFMatch20 field = SwingUIPanel.pofManager.iGetMatchField( fieldID );
					field.setFieldName(newValue);
					
					value = fieldID + "." + newValue;
				}
				break;
			case 1:
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
			case 2:
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
			case 3:
				long longValue;
				if((newValue.startsWith("0x")) || (newValue.startsWith("0X"))){
					String hexString = newValue.substring(2);
					if(hexString.matches(GUITools.RE_HEX)){
						longValue = new BigInteger(hexString, 16).longValue();
						action.setFieldValue(longValue);
					}else{
						GUITools.messageDialog(this, "Please input correct value."); 
						return;
					}
				}else if(newValue.matches(GUITools.RE_PMDEC)){
					longValue = new BigInteger(newValue).longValue();
					action.setFieldValue(longValue);
				}else{
					GUITools.messageDialog(this, "Please input correct value."); 
					return;
				}
				
				value = action.getFieldValue() + "";
				
				break;
		}
		
		DefaultListModel list5Model = (DefaultListModel)grouplist5.getModel();
		list5Model.setElementAt(GroupList5.actionAddFieldString[selectedIndexInList5] + ": " + value, selectedIndexInList5);
	}

	private void setActionValue(final OFActionDeleteField action, final String newValue, final int selectedIndexInList5) {
		if(!newValue.matches(GUITools.RE_DEC)){
			GUITools.messageDialog(this, "Please input correct value for " + GroupList5.actionDelFieldString[selectedIndexInList5]); 
			return;
		}
		
		short value = (short)Integer.parseInt(newValue);
		
		if(selectedIndexInList5 == 0){
			action.setFieldPosition(value);
		}else if(selectedIndexInList5 == 1){
			action.setFieldLength(value);
		}
		
		DefaultListModel list5Model = (DefaultListModel)grouplist5.getModel();
		list5Model.setElementAt(GroupList5.actionDelFieldString[selectedIndexInList5] + ": " + value, selectedIndexInList5);
	}

	private void setActionValue(final OFActionCalculateCheckSum action, final String newValue, final int selectedIndexInList5) {
		if(!newValue.matches(GUITools.RE_DEC)){
			GUITools.messageDialog(this, "Please input correct value for " + GroupList5.actionDelFieldString[selectedIndexInList5]); 
			return;
		}
		
		short value = (short)Integer.parseInt(newValue);
		
		if(selectedIndexInList5 == 0){
			action.setChecksumPosition(value);
		}else if(selectedIndexInList5 == 1){
			action.setChecksumLength(value);
		}else if(selectedIndexInList5 == 2){
			action.setCalcStartPosition(value);
		}else if(selectedIndexInList5 == 3){
			action.setCalcLength(value);
		}
		
		DefaultListModel list5Model = (DefaultListModel)grouplist5.getModel();
		list5Model.setElementAt(GroupList5.actionCalCheckSumString[selectedIndexInList5] + ": " + value, selectedIndexInList5);
	}
	
	private void showItems() {
		displayTextfield = false;
		
		textfield.setVisible(false);
		
		isCellEditable = false;
	}
	
	private void showTextField() {
		showTextField(null);
	}
	
	private void showTextField(String textValue) {
		displayTextfield = true;
		
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
	
	public void showAction(final GroupEntry currentGroupEntry, final int actionIndex) {
		this.currentGroupEntry = currentGroupEntry;
		this.actionIndex = actionIndex;
		clearData();
		
		OFAction action = currentGroupEntry.ofGroupMod.getActionList().get(actionIndex);
		
		int selectedIndexInList5 = grouplist5.getSelectedIndex();
		switch(action.getType()){
			case OUTPUT:
				showAction((OFActionOutput)action, selectedIndexInList5);
				break;
			case SET_FIELD:
				showAction((OFActionSetField)action, selectedIndexInList5);
				break;
			case SET_FIELD_FROM_METADATA:
				showAction((OFActionSetFieldFromMetadata)action, selectedIndexInList5);
				break;
			case MODIFY_FIELD:
				showAction((OFActionModifyField)action, selectedIndexInList5);
				break;
			case ADD_FIELD:
				showAction((OFActionAddField)action, selectedIndexInList5);
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
	
	private void showAction(final OFActionOutput action, final int selectedIndexInList5) {
		if(selectedIndexInList5 == 0){
			List<Integer> portIDList = SwingUIPanel.pofManager.iGetAllPortId(SwitchPanel.switchID);
			for (int portID : portIDList) {
				dm.addRow(new String[]{ String.valueOf(portID) });
			}
			
			showItems();
		}else{
			showTextField();
		}
	}
	
	private void showAction(final OFActionSetField action, final int selectedIndexInList5) {
		String value = null;
		
		if(selectedIndexInList5 == 0){
			for (OFMatch20 field : SwingUIPanel.pofManager.iGetAllField()) {
				short fieldID = field.getFieldId();
				dm.addRow(new String[]{ String.valueOf(fieldID) + "." + field.getFieldName() });
			}
			
			showItems();
		}else{
			DefaultListModel list5Model = (DefaultListModel)grouplist5.getModel();
			
			if(selectedIndexInList5 == 1){
				value = HexString.toHex( action.getFieldSetting().getValue() );
			}else{
				value = HexString.toHex( action.getFieldSetting().getMask() );
				if(value == null || value.length() == 0 || value.equals("00")){
					String hexValueString = HexString.toHex( action.getFieldSetting().getValue() );
					if(!hexValueString.equals("00")){
						hexValueString = String.valueOf(list5Model.get(1));
						int hexValueStartPos = (GroupList5.actionSetFieldString[1] + ": ").length();
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
			
			list5Model.setElementAt(GroupList5.actionSetFieldString[selectedIndexInList5] + ": " + value, selectedIndexInList5);
		}
	}
	
	private void showAction(final OFActionSetFieldFromMetadata action, final int selectedIndexInList5) {
		if(selectedIndexInList5 == 0){
			for (OFMatch20 field : SwingUIPanel.pofManager.iGetAllField()) {
				short fieldID = field.getFieldId();
				dm.addRow(new String[]{ String.valueOf(fieldID) + "." + field.getFieldName() });
			}
			
			showItems();
		}else{
			showTextField();
		}
	}
	
	private void showAction(final OFActionModifyField action, final int selectedIndexInList5) {
		if(selectedIndexInList5 == 0){
			for (OFMatch20 field : SwingUIPanel.pofManager.iGetAllField()) {
				short fieldID = field.getFieldId();
				dm.addRow(new String[]{ String.valueOf(fieldID) + "." + field.getFieldName() });
			}
			
			showItems();
		}else{
			showTextField();
		}
	}
	
	private void showAction(final OFActionAddField action, final int selectedIndexInList5) {
		if(selectedIndexInList5 == 1){
			dm.addRow(new String[] {"CLEAR"});
			for (OFMatch20 field : SwingUIPanel.pofManager.iGetAllField()) {
				short fieldID = field.getFieldId();
				dm.addRow(new String[]{ String.valueOf(fieldID) + "." + field.getFieldName() });
			}
			
			showItems();
		}else{
			if(selectedIndexInList5 == 4){
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
		List<OFGroupMod> ofGroupList = SwingUIPanel.pofManager.iGetAllGroups(SwitchPanel.switchID);
		for (OFGroupMod ofGroup : ofGroupList) {
			dm.addRow(new String[]{ String.valueOf(ofGroup.getGroupId()) });
		}
		
		showItems();
	}
	
	private void showAction(final OFActionDrop action) {
		for (OFDropReason reasonType : OFDropReason.values()) {
			dm.addRow(new String[]{ reasonType.toString() });
		}
		
		showItems();
	}
	
	private void showAction(final OFActionPacketIn action) {
		for (OFPacketInReason reasonType : OFPacketInReason.values()) {
			dm.addRow(new String[]{ reasonType.toString() });
		}
		
		showItems();
	}
	
	public void clearData() {
		int count = dm.getRowCount();
		for (int i = 0; i < count; i++) {
			dm.removeRow(0);
		}
		
		clearSelection();
		
		textfield.setText("");
		textfield.setVisible(false);
		
		displayTextfield = false;
		
		validate();
		repaint();
	}

	protected void paintComponent(Graphics g) {
		g.drawImage(def_img, 0, 0, this.getWidth(), this.getHeight(), null);
		super.paintComponent(g);
	}

	public Dimension getPreferredSize() {
		JViewport viewport = (JViewport) this.getParent();
		return new Dimension(viewport.getWidth(), viewport.getHeight());
	}

	protected void paintBorder(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		int w = this.getWidth();
		int h = this.getHeight();

		g2d.setPaint(new Color(120, 120, 120));
		g2d.drawLine(w - 1, 0, w - 1, h);
	}

	class GroupList6CellEditor extends AbstractCellEditor implements TableCellEditor {
		@Override
		public Object getCellEditorValue() {
			if(displayTextfield == true){
				return textfield.getText();
			}else {
				return null;
			}
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			if(displayTextfield == true){
				textfield.setVisible(true);				
				textfield.setText(value == null ? "" : value.toString());
				
				return textfield;
			}else {
				textfield.setVisible(false);
				return null;
			}
		}
	}

	class GroupList6CellRenderer extends DefaultTableCellRenderer {
		protected Image select_img = ImageUtils.getImageIconByCaches("image/gui/swing/list_select.png").getImage();
		protected Image def_img = ImageUtils.getImageIconByCaches("image/gui/swing/listbg.png").getImage();
		protected int row = -1;
		protected Object value;
		protected boolean isSelected = false;
		
		public GroupList6CellRenderer() {
			setOpaque(false);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			this.value = value;
			this.isSelected = isSelected;
			this.row = row;
			
			if(displayTextfield == true){
				return textfield;
			}else {
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
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
			
			g2d.drawString("    " + value, 10, 2 + 15);
		}
	}
}
