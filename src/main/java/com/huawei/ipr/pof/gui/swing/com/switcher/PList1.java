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

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openflow.protocol.table.OFFlowTable;
import org.openflow.protocol.table.OFTableType;

import com.huawei.ipr.pof.gui.swing.SwingUIPanel;
import com.huawei.ipr.pof.gui.swing.com.protocal.ProtocalPacketCCNewFieldEnterPane;
import com.huawei.ipr.pof.manager.IPMService;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
@SuppressWarnings("serial")
public class PList1 extends PList{
	
	public static final String tableItems[] = {"Table ID: ", "Table Name: ", "SwitchID: ", "Table Size: ", "Table Type: ",
												"Key Length: ", "Table Field: ", "Flow Entry: "};
	protected final MulitListContainerPanel middlePanel;
	protected boolean choosedNewMatchField = false;
	protected PList2 list2;
	protected ProtocalPacketCCNewFieldEnterPane fieldPanel;
	protected final SwitchPanel switchPanel;
	protected OFFlowTable ofTable;
	protected OFFlowTable newTempOFTable;
	protected byte globalTableID = IPMService.FLOWTABLEID_INVALID;
	
	public void setOtherList(PList2 list2) {
		this.list2 = list2;
	}
	
	public PList1(final SwitchPanel switchPanel, final MulitListContainerPanel middlePanel) {
		super();
		
		this.switchPanel = switchPanel;
		this.middlePanel = middlePanel;
		
		for(int i = 0; i < tableItems.length - 1; i++){
			listModel.addElement(tableItems[i]);
		}
		
		newTempOFTable = new OFFlowTable();
		newTempOFTable.setTableId(IPMService.FLOWTABLEID_INVALID);
		
		this.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()){
					selectedValueChanged();
				}
			}
		});
	}
	
	public OFFlowTable getOFTable(){
		return ofTable;
	}
	
	public void DisplayList1(OFFlowTable ofTable, int switchID, String tableName){
		this.ofTable = ofTable;

		clearData();
		
		if(null != ofTable){
			globalTableID = SwingUIPanel.pofManager.parseToGlobalTableId(switchID, ofTable.getTableType().getValue(), ofTable.getTableId());
			int matchFieldNumber = ofTable.getMatchFieldNum();
			int entryNumber = SwingUIPanel.pofManager.iGetFlowEntryNumber(switchID, globalTableID);
			
			listModel.add(0, tableItems[0] + globalTableID);
			listModel.add(1, tableItems[1] + ofTable.getTableName());
			listModel.add(2, tableItems[2] + Integer.toHexString(switchID));
			listModel.add(3, tableItems[3] + ofTable.getTableSize());
			listModel.add(4, tableItems[4] + String.valueOf(ofTable.getTableType()));
			listModel.add(5, tableItems[5] + ofTable.getKeyLength());
			listModel.add(6, tableItems[6] + ((matchFieldNumber == 0) ? "" : "(" + matchFieldNumber + ")") );
			listModel.add(7, tableItems[7] + ((entryNumber == 0) ? "" : "(" + entryNumber + ")") );
		}else{
			globalTableID = IPMService.FLOWTABLEID_INVALID;
			
			listModel.add(0, tableItems[0]);
			
			if(null != tableName){
				listModel.add(1, tableItems[1] + tableName);
				newTempOFTable.setTableName(tableName);
			}else{
				listModel.add(1, tableItems[1]);
			}
			if(0 != switchID){
				listModel.add(2, tableItems[2] + Integer.toHexString(switchID));
			}else{
				listModel.add(2, tableItems[2]);
			}
			
			listModel.add(3, tableItems[3]);
			listModel.add(4, tableItems[4]);			
			listModel.add(5, tableItems[5]);
			listModel.add(6, tableItems[6]);
//			listModel.add(7, tableItems[7]);
		}
		
		validate();
		repaint();
	}

	protected void selectedValueChanged() {
		list2.clearData();
		
		int index = this.getSelectedIndex();
		if(-1 == index){			
			return ;
		}
		if(switchPanel.comboxSwitchID.getSelectedIndex() == -1 
				|| (SwitchPanel.switchID == 0)
				|| (null == switchPanel.middlePanel.diamondTablesPanel.getSelected())){
			return;
		}
		
		switch (index) {
			case 1:			//table name
				if(ofTable == null 
					&& !((String)this.getModel().getElementAt(1)).endsWith(IPMService.FIRST_ENTRY_TABLE_NAME)){
					list2.showTextField(index);			    
				}
				break;
			case 3:			//table size
				if(ofTable == null){
					list2.showTextField(index);			    
				}
				break;
			case 4:			//table type
				if(ofTable == null){
					list2.showTypeCombo();				
				}
				break;
			case 6:			//table field
				if(ofTable == null){
					if(newTempOFTable != null
							&& null != newTempOFTable.getTableType()
							&& true == newTempOFTable.getTableType().equals(OFTableType.OF_LINEAR_TABLE)){
						return;						
					}
					
					if(choosedNewMatchField == false
							|| switchPanel.protocolPanel.selectedMatchFieldList.size() == 0 ){
						showSelectFieldAbove();					
					}
					else{
						list2.showField(switchPanel.protocolPanel.selectedMatchFieldList);
					}
				}else{
					list2.showField(ofTable.getMatchFieldList());				
				}
				break;
			case 7:			//flow entries
				if(ofTable != null){
					list2.showFlowEntry();
				}
				break;
	  
			default:
				break;
		}
		
		validate();
		repaint();
	}
	
	private void showSelectFieldAbove() {
		middlePanel.showSelectFieldAbovePane();
	}
	
	@Override
	public void clearNextList(){
		list2.clearData();
	}
}
