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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch20;
import org.openflow.protocol.OFMatchX;

import com.huawei.ipr.pof.gui.comm.GUITools.EDIT_STATUS;
import com.huawei.ipr.pof.gui.swing.HomeMain;
import com.huawei.ipr.pof.gui.swing.SwingUIPanel;
import com.huawei.ipr.pof.gui.swing.com.protocal.ProtocalPacketCCNewFieldEnterPane;
import com.huawei.ipr.pof.gui.swing.comutil.FlowEntry;
import com.huawei.ipr.pof.gui.swing.comutil.ImageUtils;
import com.huawei.ipr.pof.manager.IPMService;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
@SuppressWarnings("serial")
public class PList3 extends PList {
	public static final String flowEntryEleString[] = {"FlowEntry ID: ", "Priority: ", "Match Field: ", "Instruction: ", "Counter: "};
	public static final String fieldEleString[] = {"Name: ", "Offset: ", "Length: "};
	
	protected PList4 list4;
	protected PList2 list2;
	protected ProtocalPacketCCNewFieldEnterPane fieldPanel ;
	
	protected FlowEntry currentFlowEntry;
	
	public void setOtherList(PList2 list2, PList4 list4) {
		this.list4 = list4;
		this.list2 = list2;
	}
	
	public PList3() {
		super();
		addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()){
					selectedValueChanged();
				}
			}
		});

		setCellRenderer(new PList3Renderer());
	}
	
	protected void selectedValueChanged() {		
		list4.clearData();
		
		int index = this.getSelectedIndex();
		if(-1 == index){			
			return;
		}
		
	    String value = String.valueOf(listModel.getElementAt(index));
		if (null == value || 0 == value.length()) {
			return;
		}
	    
		switch (index) {
			case 1:			//priority
				if(value.startsWith(flowEntryEleString[index])){
					list4.showPriority(currentFlowEntry);
				}
				break;
			case 2:			//field
				if(value.startsWith(flowEntryEleString[index])){
					list4.showFields(currentFlowEntry);
				}
				break;
			case 3:		//instruction
				if(value.startsWith(flowEntryEleString[index])){
					list4.showInstruction(currentFlowEntry);
				}
				break;
			case 4:		//counter
				if((value.startsWith(flowEntryEleString[index]))){
					showCounter();
				}
				break;
			default:
				break;
		}
	}

	private void showCounter() {
		if(currentFlowEntry.ofFlowMod.getIndex() == IPMService.FLOWENTRYID_INVALID){
			return;
		}
		int counterID = currentFlowEntry.ofFlowMod.getCounterId();
		if(counterID != IPMService.COUNTERID_INVALID){
			long counterValue = SwingUIPanel.pofManager.iQueryCounterValue(SwitchPanel.switchID, counterID);
			if(counterValue == -1){
				counterValue = 0;
			}
			listModel.set(4, flowEntryEleString[4] + counterValue);
		}
		
		validate();
		repaint();
	}
	
	public void showFlowEntry(final FlowEntry currentFlowEntry) {
		this.currentFlowEntry = currentFlowEntry;
		
		clearData();
		
		//add default matchX if adding a new flow entry
		if(currentFlowEntry.editStatus == EDIT_STATUS.ES_ADDING 
				&& currentFlowEntry.ofFlowMod.getMatchList() == null){
			List<OFMatchX> matchXList = new ArrayList<OFMatchX>();
			List<OFMatch20> matchList = list2.list1.ofTable.getMatchFieldList();
			if(matchList != null){
				for(OFMatch20 matchField : matchList){
					 OFMatchX fieldSetting = SwingUIPanel
												.pofManager
												.iNewMatchX(matchField, 
															new byte[] {0}, 
															new byte[] {0});
					 matchXList.add(fieldSetting);
				}
				currentFlowEntry.ofFlowMod.setMatchList(matchXList);
				currentFlowEntry.ofFlowMod.setMatchFieldNum((byte)matchXList.size());
			}
		}
		
		OFFlowMod ofFlowMod = currentFlowEntry.ofFlowMod;

		int id = ofFlowMod.getIndex();
    	int fieldNumber = ofFlowMod.getMatchFieldNum();
    	int insNumber = ofFlowMod.getInstructionNum();
    	
		listModel.addElement(flowEntryEleString[0] + ( (id == IPMService.FLOWENTRYID_INVALID) ? "" : id) );
		listModel.addElement(flowEntryEleString[1] + ofFlowMod.getPriority());
		listModel.addElement(flowEntryEleString[2] + ( (fieldNumber == 0) ? "" : "(" + fieldNumber + ")") );
		listModel.addElement(flowEntryEleString[3] + ( (insNumber == 0) ? "" : "(" + insNumber + ")") );
		listModel.addElement(flowEntryEleString[4]);
		
		validate();
		repaint();
	}

	public void showField(OFMatch20 field) {
		clearData();

		listModel.addElement(fieldEleString[0] + field.getFieldName());
		listModel.addElement(fieldEleString[1] + field.getOffset());
		listModel.addElement(fieldEleString[2] + field.getLength());
		
		validate();
		repaint();
	}
	
	@Override
	public void clearNextList(){
		list4.clearData();
	}

	class PList3Renderer extends DefaultListCellRenderer {
		public final Image select_img = ImageUtils.getImageIconByCaches("image/gui/swing/list_select.png").getImage();
		protected int row = -1;
		protected Object value;
		protected boolean isSelected = false;
		
		public PList3Renderer() {
			super();
			this.setOpaque(false);
		}

		public Dimension getPreferredSize() {
			Dimension d = super.getPreferredSize();
			return new Dimension((int) d.getWidth(), 24);
		}

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			this.value = value;
			this.row = index;
			this.isSelected = isSelected;
			Component c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			return c;
		}

		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			if (!isSelected) {
				g2d.drawImage(def_img, 0, 0, this.getWidth(), this.getHeight(), null);
			} else {
				g2d.drawImage(select_img, 0, 0, this.getWidth(), this.getHeight(), null);
			}

			g2d.setColor(HomeMain.LabelColor);
			g2d.drawString("  " + value, 10, 2 + 15);
		}
	}
}
