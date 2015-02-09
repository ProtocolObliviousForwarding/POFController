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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.table.DefaultTableModel;

import org.openflow.protocol.OFMatch20;
import org.openflow.protocol.OFMatchX;
import org.openflow.protocol.OFMeterMod;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.instruction.OFInstruction;
import org.openflow.protocol.instruction.OFInstructionApplyActions;
import org.openflow.protocol.instruction.OFInstructionGotoDirectTable;
import org.openflow.protocol.instruction.OFInstructionGotoTable;
import org.openflow.protocol.instruction.OFInstructionMeter;
import org.openflow.protocol.instruction.OFInstructionType;
import org.openflow.protocol.instruction.OFInstructionWriteMetadata;
import org.openflow.protocol.instruction.OFInstructionWriteMetadataFromPacket;
import org.openflow.util.HexString;

import com.huawei.ipr.pof.gui.comm.GUITools.EDIT_STATUS;
import com.huawei.ipr.pof.gui.swing.HomeMain;
import com.huawei.ipr.pof.gui.swing.SwingUIPanel;
import com.huawei.ipr.pof.gui.swing.comutil.BackgroundImageListCellRenderer;
import com.huawei.ipr.pof.gui.swing.comutil.FlowEntry;
import com.huawei.ipr.pof.gui.swing.comutil.ImageUtils;
import com.huawei.ipr.pof.manager.IPMService;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
@SuppressWarnings("serial")
public class PList5 extends PList {
	public static final OFInstructionType[] INSTRUCTIONS = IPMService.INSTRUCTIONS;
	
	public static final String[] insGotoTableString = { "Next Table ID", "Packet Offset(byte)" };
	public static final String[] insGotoDirectTableString = { "Next Table Id", "Entry Index", "Packet Offset(byte)" };
	public static final String[] insMeterString = { "MeterID(kbps)" };
	public static final String[] insWriteMetadataString = { "Metadata Name", "Metadata Offset(bit)", "Write Length(bit)", "Value" };
	public static final String[] insWriteMetadataFromPacketString = { "Metadata Name", "Metadata Offset(bit)", "Write Length(bit)", "Packet Offset(bit)" };

	public static final String ACTIONS_STRING = "Actions:";
	
	public static final String[] matchFieldString = {"Name", "Offset", "Length", "Value(0x)", "Mask(0x)"};
	
	
	protected PList4 list4;
	protected PList6 list6;
	
	protected int instructionIndex;
	
	protected int matchFieldIndex;
	
	protected FlowEntry currentFlowEntry;
	
	public PList5() {
		super();
		setCellRenderer( new PList5Renderer());
		
		addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				int button = e.getButton();
				if(button == MouseEvent.BUTTON1){
					selectedValueChanged();
				}else if (button == MouseEvent.BUTTON3) {
					addDelete(e);
				}
			}
		});
	}
    
	public void setOtherList(PList4 list4, PList6 list6) {
		this.list4 = list4;
		this.list6 = list6;
	}
	
	private void addDelete(MouseEvent e){
		final int selectedIndex = this.getSelectedIndex();
		if (currentFlowEntry.editStatus != EDIT_STATUS.ES_READONLY
				&& selectedIndex > 0 
				&& this.listModel.get(0).equals(ACTIONS_STRING)) {	//flow entry
			
			JPopupMenu popMenu = new JPopupMenu();
			//delete
			JMenuItem delItem = new JMenuItem("Delete");
			popMenu.add(delItem);						
	
			delItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					OFInstructionApplyActions ins = (OFInstructionApplyActions)currentFlowEntry.ofFlowMod.getInstructionList().get(instructionIndex);
					ins.getActionList().remove(selectedIndex - 1);
					
					int actionNum = ins.getActionList().size();
					ins.setActionNum((byte)actionNum);
					
					clearData();
				}
			});
		
			popMenu.show(this, e.getX(), e.getY());
		}
	}
	
	protected void selectedValueChanged() {
		int index = this.getSelectedIndex();

		list6.clearData();
		if (-1 == index) {
			return;
		}

		String value = String.valueOf(this.getSelectedValue());
		
		if (null == value || 0 == value.length()) {
			return;
		}
		
		if(value.startsWith(matchFieldString[3]) || value.startsWith(matchFieldString[4])){	//value/mask
			if(currentFlowEntry.editStatus != EDIT_STATUS.ES_READONLY){
				list6.setValueOrMask(currentFlowEntry);
			}
		}else if ( value.equals(ACTIONS_STRING) ){		//display all Actions in list6
			if(currentFlowEntry.editStatus != EDIT_STATUS.ES_READONLY){
				list6.showAllDefaultActions(currentFlowEntry, instructionIndex);
			}
		}else if(String.valueOf(listModel.get(0)).equals(ACTIONS_STRING)){	//display an action
			list6.showAction(currentFlowEntry, instructionIndex, index - 1);
		}else if(list4.dm.getValueAt(0, 0).equals(PList4.INSTRUCTION_ITEM_STRING)){		
			if(listModel.get(0).equals(INSTRUCTIONS[0].name())){			//add a new instruction
				DefaultTableModel list4Model = (DefaultTableModel) list4.getModel();
				list4Model.addRow(new String[] { value });
				
				addNewInstruction( OFInstructionType.valueOf(value) );
				
				clearData();
			}else if(currentFlowEntry.editStatus != EDIT_STATUS.ES_READONLY){	//display instruction value
				list6.showInstruction(currentFlowEntry, instructionIndex);
			}
		}
		
		validate();
		repaint();
	}

	private void addNewInstruction(OFInstructionType insType) {
		OFInstruction newIns = null;
		switch (insType)
		{
			case GOTO_TABLE:
				newIns = new OFInstructionGotoTable();
				((OFInstructionGotoTable)newIns).setMatchList(null);
				break;
			case GOTO_DIRECT_TABLE:
				newIns = new OFInstructionGotoDirectTable();
				break;
			case METER:
				newIns = new OFInstructionMeter();
				break;
			case WRITE_METADATA:
				newIns = new OFInstructionWriteMetadata();
				break;
			case WRITE_METADATA_FROM_PACKET:
				newIns = new OFInstructionWriteMetadataFromPacket();
				break;
			case APPLY_ACTIONS:
				List<OFAction> tempActionList = new ArrayList<OFAction>();
				newIns = new OFInstructionApplyActions();
				((OFInstructionApplyActions)newIns).setActionList(tempActionList);
				break;
			default:
				break;
		}
		
		currentFlowEntry.ofFlowMod.getInstructionList().add(newIns);
		currentFlowEntry.ofFlowMod.setInstructionNum((byte)(currentFlowEntry.ofFlowMod.getInstructionList().size()));
	}

	public void showAllDefaultInstructions(final FlowEntry currentFlowEntry) {
		this.currentFlowEntry = currentFlowEntry;
		DefaultTableModel list4Model = (DefaultTableModel) list4.getModel();
		if (list4Model.getRowCount() >= 0) {
			clearData();
			
			for(OFInstructionType insType : INSTRUCTIONS){
				listModel.addElement(insType.name());
			}
		}
		
		validate();
		repaint();
	}
	
	public void showInstruction(FlowEntry currentFlowEntry, int index) {
		this.currentFlowEntry = currentFlowEntry;
		instructionIndex = index;
		clearData();
		
		OFInstruction ins = currentFlowEntry.ofFlowMod.getInstructionList().get(index);
		
		switch(ins.getType()){
			case GOTO_TABLE:
				showInstruction((OFInstructionGotoTable)ins);
				break;
			case GOTO_DIRECT_TABLE:
				showInstruction((OFInstructionGotoDirectTable)ins);
				break;
			case METER:
				showInstruction((OFInstructionMeter)ins);
				break;
			case WRITE_METADATA:
				showInstruction((OFInstructionWriteMetadata)ins);
				break;
			case WRITE_METADATA_FROM_PACKET:
				showInstruction((OFInstructionWriteMetadataFromPacket)ins);
				break;
			case APPLY_ACTIONS:
				showInstruction((OFInstructionApplyActions)ins);
				break;
			default:
				return;
		}
		
		validate();
		repaint();
	}

	private void showInstruction(final OFInstructionGotoTable ins) {
		listModel.addElement(insGotoTableString[0] + ": " + ins.getNextTableId());
		listModel.addElement(insGotoTableString[1] + ": " + ins.getPacketOffset());
	}

	private void showInstruction(final OFInstructionGotoDirectTable ins) {
		listModel.addElement(insGotoDirectTableString[0] + ": " + ins.getNextTableId());
		listModel.addElement(insGotoDirectTableString[1] + ": " + ins.getTableEntryIndex());
		listModel.addElement(insGotoDirectTableString[2] + ": " + ins.getPacketOffset());
	}

	private void showInstruction(final OFInstructionMeter ins) {
		if(ins.getMeterId() != IPMService.METER_INVALID){
			OFMeterMod meter = SwingUIPanel.pofManager.iGetMeter(SwitchPanel.switchID, ins.getMeterId());
			listModel.addElement(insMeterString[0] + ": " + ins.getMeterId() + "." + meter.getRate());
		}else{
			listModel.addElement(insMeterString[0]);
		}
		
	}

	private void showInstruction(final OFInstructionWriteMetadata ins) {
		OFMatch20 metadataField = HomeMain.contentPane.getProtocolPanel().getBelongedMetaDataName(ins.getMetadataOffset(), ins.getWriteLength());
		if(metadataField != null ){
			listModel.addElement(insWriteMetadataString[0] + ": " + metadataField.getFieldId() + "." + metadataField.getFieldName());
		}else{
			listModel.addElement(insWriteMetadataString[0]);
		}
		
		listModel.addElement(insWriteMetadataString[1] + ": " + ins.getMetadataOffset());
		listModel.addElement(insWriteMetadataString[2] + ": " + ins.getWriteLength());
		listModel.addElement(insWriteMetadataString[3] + ": " + ins.getValue());
	}
	
	private void showInstruction(final OFInstructionWriteMetadataFromPacket ins) {
		OFMatch20 metadataField = HomeMain.contentPane.getProtocolPanel().getBelongedMetaDataName(ins.getMetadataOffset(), ins.getWriteLength());
		if(metadataField != null ){
			listModel.addElement(insWriteMetadataFromPacketString[0] + ": " + metadataField.getFieldId() + "." + metadataField.getFieldName());
		}else{
			listModel.addElement(insWriteMetadataFromPacketString[0]);
		}
		
		listModel.addElement(insWriteMetadataFromPacketString[1] + ": " + ins.getMetadataOffset());
		listModel.addElement(insWriteMetadataFromPacketString[2] + ": " + ins.getWriteLength());
		listModel.addElement(insWriteMetadataFromPacketString[3] + ": " + ins.getPacketOffset());
	}
	
	private void showInstruction(final OFInstructionApplyActions ins) {
		showAction(ins.getActionList());
	}

	private void showAction(List<OFAction> actionList) {
		clearData();
		
		listModel.addElement(ACTIONS_STRING);
		
		for (OFAction actionNode : actionList) {
			listModel.addElement(actionNode.getType().name());
		}
	}

	
	public void showField(final FlowEntry currentFlowEntry, final int index) {
		this.currentFlowEntry = currentFlowEntry;
		matchFieldIndex = index;
		clearData();
		
		OFMatchX matchX = currentFlowEntry.ofFlowMod.getMatchList().get(index);
		
		listModel.addElement(matchFieldString[0] + ": "  + matchX.getFieldName());
		listModel.addElement(matchFieldString[1] + ": " + matchX.getOffset());
		listModel.addElement(matchFieldString[2] + ": " + matchX.getLength());
		
		byte[] valueBytes = matchX.getValue();
		byte[] maskBytes = matchX.getMask();
		listModel.addElement(matchFieldString[3] + ": " + HexString.toHex(valueBytes));
		listModel.addElement(matchFieldString[4] + ": " + HexString.toHex(maskBytes));
	}
	
	@Override
	public void clearNextList(){
		list6.clearData();
	}
	
	class PList5Renderer extends BackgroundImageListCellRenderer{
		public final Image icon = ImageUtils.getImageIcon("image/gui/swing/list_plus_icon.png").getImage();
		public final Image select_img = ImageUtils.getImageIconByCaches("image/gui/swing/list_select.png").getImage();
		public final Image add_end_img = ImageUtils.getImageIconByCaches("image/gui/swing/plusbutton_small.png").getImage();
		
		protected Object value;
		protected int row = -1;
		
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int row, boolean isSelected, boolean cellHasFocus) {
			this.value = value;
			this.row = row;
			return super.getListCellRendererComponent(list, value, row, isSelected, cellHasFocus);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;

			int w = this.getWidth();
			int h = this.getHeight();
			
			if (value.equals(ACTIONS_STRING) 
					&& row == 0 
					&& currentFlowEntry.editStatus != EDIT_STATUS.ES_READONLY){
				g2d.drawImage(icon, 3, 3, null);
			}
			
			g2d.setColor(HomeMain.packetLabelColor);
			g2d.drawRoundRect(2, 1, w - 5, h - 2, 3, 3);
			g2d.setColor(HomeMain.LabelColor);
			g2d.drawString("    " + value, 10, 2 + 15);
			
			validate();
			repaint();
		}		
	}
}
