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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFGlobal;
import org.openflow.protocol.OFMatch20;
import org.openflow.protocol.OFMatchX;
import org.openflow.protocol.OFProtocol;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionAddField;
import org.openflow.protocol.action.OFActionModifyField;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.protocol.action.OFActionSetField;
import org.openflow.protocol.action.OFActionSetFieldFromMetadata;
import org.openflow.protocol.instruction.OFInstruction;
import org.openflow.protocol.instruction.OFInstructionApplyActions;
import org.openflow.protocol.instruction.OFInstructionType;
import org.openflow.protocol.table.OFFlowTable;
import org.openflow.protocol.table.OFTableType;

import com.huawei.ipr.pof.gui.comm.GUITools;
import com.huawei.ipr.pof.gui.comm.GUITools.EDIT_STATUS;
import com.huawei.ipr.pof.gui.swing.HomeMain;
import com.huawei.ipr.pof.gui.swing.SwingUIPanel;
import com.huawei.ipr.pof.gui.swing.com.MainPanel;
import com.huawei.ipr.pof.gui.swing.com.protocal.MetaDataButton;
import com.huawei.ipr.pof.gui.swing.com.protocal.ProtocolPanel;
import com.huawei.ipr.pof.gui.swing.comutil.FlowEntry;
import com.huawei.ipr.pof.gui.swing.comutil.ImageButton;
import com.huawei.ipr.pof.gui.swing.comutil.ImageUtils;
import com.huawei.ipr.pof.gui.swing.comutil.NOPanel;
import com.huawei.ipr.pof.gui.swing.comutil.PJComboBox;
import com.huawei.ipr.pof.manager.IPMService;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
@SuppressWarnings("serial")
public class SwitchPanel extends NOPanel {
	
	
	public static final byte FLOWTABLEID_INVALID = IPMService.FLOWTABLEID_INVALID;
	
	protected JLabel lbPacket = new JLabel("Switch");
    public static int switchID = IPMService.SWITCHID_INVALID;
    protected JComboBox comboxSwitchID;
    
    public final ImageButton leftButton = new ImageButton("image/gui/swing/dbx-left.png");
    public final ImageButton rightButton = new ImageButton("image/gui/swing/dbx-right.png");
    public final ImageButton addNewTableButton = new ImageButton("image/gui/swing/plusbutton.png");
    public final ImageButton eButton = new ImageButton("image/gui/swing/E.png");
    public final ImageButton pButton = new ImageButton("image/gui/swing/P.png");
    public final ImageButton submitButton = new ImageButton("image/gui/swing/submit.png");
    
	protected DeviceInfoButton displayDeviceInfoButton = new DeviceInfoButton(this);
	protected JPanel topPane = new NOPanel(new BorderLayout());
	public MulitListContainerPanel middlePanel ;
	protected PDiagramPanel pdiagramPanel;
	
	protected DiamondTablesPanel diamondTablesPanel;
	protected JPanel top2CenterPane;
	protected JScrollPane scrollPane;
	protected final ProtocolPanel protocolPanel;
	
	protected final MainPanel mainPanel;
	
	protected Map<Integer, Map<Byte, List<FlowEntry>>> switch_flowTable_flowEntryListMap;	//<switchID, flowTable_flowEntryListMap>	
	
	protected Map<Byte, List<FlowEntry>> flowTable_flowEntryListMap;	//<globalTableID, FlowEntry>	
	
	public SwitchPanel(final MainPanel mainPanel) {
		super();
		this.setLayout(new BorderLayout());

		this.mainPanel = mainPanel;
		this.protocolPanel = mainPanel.getProtocolPanel();
		
		addNewTableButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		eButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		pButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		initTop();
		initMiddle();
		initbottom();
		
		switch_flowTable_flowEntryListMap = new HashMap<Integer, Map<Byte, List<FlowEntry>>>();
	}
	
	public DeviceInfoButton getDeviceInfoButton(){
		return displayDeviceInfoButton;
	}
	
	public void addSwitch(int switchID){
		if(null != comboxSwitchID){
			String stringSwitchID = Integer.toHexString(switchID);
			comboxSwitchID.removeItem(stringSwitchID);
			comboxSwitchID.addItem(stringSwitchID);			
		}
	}
	
	public void removeSwitch(int switchID){
		if(null != comboxSwitchID){
			String stringSwitchID = Integer.toHexString(switchID);
			comboxSwitchID.removeItem(stringSwitchID);
			
			if(flowTable_flowEntryListMap == switch_flowTable_flowEntryListMap.remove(switchID)){
				flowTable_flowEntryListMap = null;
			}
			
			switchID = IPMService.SWITCHID_INVALID;
			
			reloadSwitchPanel();
		}
	}

	private void initbottom() {
		JPanel bottomPane = new NOPanel(new BorderLayout());
		
		JPanel bottomCenterPane = new NOPanel();
		FlowLayout fl = new FlowLayout(FlowLayout.CENTER);
		fl.setVgap(0);
		fl.setHgap(0);
		bottomCenterPane.setLayout(fl);
		bottomCenterPane.add(submitButton);
		bottomCenterPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		
		JPanel bottomRightPane = new NOPanel();
		fl = new FlowLayout(FlowLayout.RIGHT);
		fl.setVgap(0);
		fl.setHgap(0);
		bottomRightPane.setLayout(fl);
		displayDeviceInfoButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		bottomRightPane.add(displayDeviceInfoButton);
		bottomRightPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		
		bottomPane.add(bottomCenterPane);
		bottomPane.add(bottomRightPane,BorderLayout.EAST);
		this.add(bottomPane, BorderLayout.SOUTH);
		
		submitButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				submitAction();
			}
		});
		submitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}
	
	public void markUsedProtocol(List<OFMatch20> matchFieldList){
		if(null != matchFieldList){
			for(OFMatch20 field : matchFieldList){
				markUsedProtocol(field.getFieldId());
			}
		}
	}
	
	public void markUsedMetadata(){
		if(protocolPanel.metadataButton.isUsed == false){
			protocolPanel.metadataButton.isUsed = true;
		}
	}
	
	public void markUsedProtocol(short fieldId){
		if(OFMatch20.METADATA_FIELD_ID != fieldId){
			OFProtocol protocol = SwingUIPanel.pofManager.iGetBelongedProtocol(fieldId);
			if(protocol != null && protocolPanel.protocolButtonList != null){
				for(MetaDataButton metadataButton : protocolPanel.protocolButtonList){
					if( metadataButton.isUsed == false
							&& metadataButton.getName().equalsIgnoreCase(protocol.getProtocolName()) ){
						metadataButton.isUsed = true;
					}
				}
			}
		}else{//metadata
			markUsedMetadata();
		}
	}
	
	public void markUsedProtocol(OFMatch20 field){
		if(field != null){
			markUsedProtocol(field.getFieldId());
		}
	}
	
	public void markUsedProtocol(OFMatchX fieldX){
		if(fieldX != null){
			markUsedProtocol(fieldX.getFieldId());
		}
	}

	
	public void submitAction() {
		if(IPMService.SWITCHID_INVALID == switchID ){
			return ;
		}
		OFFlowTable currentTable = middlePanel.list1.getOFTable();
		if(currentTable == null || currentTable.getTableId() == IPMService.FLOWTABLEID_INVALID){
			submitToCreateNewTable();
		}else{
			submitFlowEntry(currentTable, middlePanel.list1.globalTableID);
		}
	}
	
	private void submitToCreateNewTable(){
		//add flow table
		OFFlowTable currentTable = middlePanel.list1.newTempOFTable;
		if(null != currentTable){
			if(currentTable.getTableName() == null || currentTable.getTableName().isEmpty()){
            	GUITools.messageDialog(this, "Please input Table Name.");
				return;
			}
			if(currentTable.getTableName().equalsIgnoreCase(IPMService.FIRST_ENTRY_TABLE_NAME)
					&& currentTable.getTableType() != OFTableType.OF_MM_TABLE){
            	GUITools.messageDialog(this, IPMService.FIRST_ENTRY_TABLE_NAME + " can be MM table only");
				return;
			}
			
			if(0 == currentTable.getTableSize()){
            	GUITools.messageDialog(this, "Please input Table Size.");
				return;
			}
			
			if(currentTable.getTableType() == OFTableType.OF_LINEAR_TABLE){
				if(0 != currentTable.getMatchFieldNum()
						|| (currentTable.getMatchFieldList() != null && currentTable.getMatchFieldList().size() != 0)){
					GUITools.messageDialog(this, "OF_LINEAR_TABLE should NOT have any match field.");
					return;
				}
			}else{
				if(0 == currentTable.getMatchFieldNum()
						|| currentTable.getMatchFieldList() == null
						|| currentTable.getMatchFieldList().size() == 0){
					GUITools.messageDialog(this, "Please add match field.");
					return;
				}
				
				if(currentTable.getMatchFieldNum() > OFGlobal.OFP_MAX_MATCH_FIELD_NUM){
					GUITools.messageDialog(this, "Max match field num is " + OFGlobal.OFP_MAX_MATCH_FIELD_NUM + " (+ " + currentTable.getMatchFieldNum() + ").");
					return;
				}
			}

			byte globalTableId = SwingUIPanel.pofManager.iAddFlowTable(switchID, 
																	currentTable.getTableName(), 
																	currentTable.getTableType().getValue(), 
																	currentTable.getKeyLength(), 
																	currentTable.getTableSize(), 
																	currentTable.getMatchFieldNum(), 
																	currentTable.getMatchFieldList());
			
			if(FLOWTABLEID_INVALID == globalTableId){
				GUITools.messageDialog(this, "Add flow table failedly.");
				return;
			}
			
			//mark the protocol it is already used and can not be modified or deleted
			List<OFMatch20> fieldList = currentTable.getMatchFieldList();
			if(null != fieldList){
				markUsedProtocol(fieldList);
			}
							
			protocolPanel.selectedMatchFieldList = new ArrayList<OFMatch20>();
			
			currentTable = SwingUIPanel.pofManager.iGetFlowTable(switchID, globalTableId);
			
			diamondTablesPanel.getSelected().tableEditStatus = EDIT_STATUS.ES_READONLY;
			diamondTablesPanel.getSelected().ofTable = currentTable;
			
			middlePanel.list1.DisplayList1(currentTable, switchID, currentTable.getTableName());
			
			middlePanel.list1.newTempOFTable = new OFFlowTable();
			middlePanel.list1.newTempOFTable.setTableId(IPMService.FLOWTABLEID_INVALID);
			
			flowTable_flowEntryListMap.put(globalTableId, new ArrayList<FlowEntry>());

			middlePanel.list1.choosedNewMatchField = false;
			
			//change the item's color
			protocolPanel.protocolFieldDisplayTable.showDefaultPane();
			
			GUITools.messageDialog(this, "Add a flow table successfully."); 
		}
	}
	
	private void submitFlowEntry(final OFFlowTable currentTable, final byte globalTableID){
		Boolean successful = null;
		
		List<FlowEntry> flowEntryList = flowTable_flowEntryListMap.get(globalTableID);
		int flowEntryListSize = flowEntryList.size();
		
		for( int i = 0 ; i < flowEntryListSize; i++){
			FlowEntry flowEntry = flowEntryList.get(i);
			OFFlowMod ofEntry = flowEntry.ofFlowMod;
			
			if(ofEntry.getInstructionNum() > OFGlobal.OFP_MAX_INSTRUCTION_NUM){
            	GUITools.messageDialog(this, "instruction num " + ofEntry.getInstructionNum() + " > OFGlobal.OFP_MAX_INSTRUCTION_NUM(" + OFGlobal.OFP_MAX_INSTRUCTION_NUM + ").");
            	successful = false;
            	continue;
			}
			
			if(null != ofEntry.getInstructionList()){
				for(OFInstruction ins : ofEntry.getInstructionList()){
					if(ins.getType() == OFInstructionType.APPLY_ACTIONS){
						int actionNum = ((OFInstructionApplyActions)ins).getActionNum();
						if(actionNum > OFGlobal.OFP_MAX_ACTION_NUMBER_PER_INSTRUCTION){
			            	GUITools.messageDialog(this, "action num " + actionNum + " > OFGlobal.OFP_MAX_ACTION_NUMBER_PER_INSTRUCTION(" + OFGlobal.OFP_MAX_ACTION_NUMBER_PER_INSTRUCTION + ").");
			            	successful = false;
			            	continue;
						}
					}
				}
			}
			
			if(flowEntry.editStatus == EDIT_STATUS.ES_ADDING){	//adding
	            byte fieldNum = currentTable.getMatchFieldNum();
	            if( (fieldNum == 0 && ofEntry.getMatchList() != null && ofEntry.getMatchList().size() != 0)
	            		|| (fieldNum != 0 
	            				&& (ofEntry.getMatchList() == null || ofEntry.getMatchList().size() != fieldNum) ) ){
	            	GUITools.messageDialog(this, "field number dismatch, check the match field.");
	            	successful = false;
	            	continue;
	            }
	            int newFlowEntryId = SwingUIPanel.pofManager.iAddFlowEntry(switchID, 
													                		globalTableID, 
													                		fieldNum, 
													                		ofEntry.getMatchList(),
																			(byte) ofEntry.getInstructionNum(),
																			ofEntry.getInstructionList(), 
																			ofEntry.getPriority());
	            
	            if(newFlowEntryId != IPMService.FLOWENTRYID_INVALID){
	            	flowEntry.editStatus = EDIT_STATUS.ES_READONLY;
	            	flowEntryList.get(i).ofFlowMod = SwingUIPanel.pofManager.iGetFlowEntry(switchID, globalTableID, newFlowEntryId);
	            }
	            
	            if(newFlowEntryId != IPMService.FLOWENTRYID_INVALID && successful == null ){
	            	successful = true;
	            }else if(newFlowEntryId == IPMService.FLOWENTRYID_INVALID && (successful == null || successful == true) ){
	            	successful = false;
	            }
	            
	            checkUsedProtocol(ofEntry.getInstructionList());
			}else if(flowEntry.editStatus == EDIT_STATUS.ES_READONLY){	//display, do nothing
				continue;
			}else if(flowEntry.editStatus == EDIT_STATUS.ES_MODIFYING){	//modifying
				 byte fieldNum = currentTable.getMatchFieldNum();
				 boolean succ = SwingUIPanel.pofManager.iModFlowEntry(switchID, 
																		globalTableID, 
																		ofEntry.getIndex(),
																		fieldNum, 
																		ofEntry.getMatchList(),
																		(byte) ofEntry.getInstructionNum(),
																		ofEntry.getInstructionList(), 
																		ofEntry.getPriority());
				 
				 if(succ){
					 flowEntry.editStatus = EDIT_STATUS.ES_READONLY;
					 flowEntryList.get(i).ofFlowMod = SwingUIPanel.pofManager.iGetFlowEntry(switchID, globalTableID, ofEntry.getIndex());
				 }
				 
				 if(succ == true && successful == null){
					 successful = true;
				 }else if(succ == false && (successful == null || successful == true) ){
					 successful = false;
				 }
				 
				 checkUsedProtocol(ofEntry.getInstructionList());
			}
		}
		
		try {	//wait in case roll back
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(0 != flowTable_flowEntryListMap.get(globalTableID).size() 
				&& successful != null
				&& diamondTablesPanel.getSelected() != null){
			if(successful){
				GUITools.messageDialog(this, "Successfully add/modify flow entries."); 
			}else{
				GUITools.messageDialog(this, "Ooops! add/modify flow entries failed!\nPlease check console log."); 
			}
			
			int entryNumber = SwingUIPanel.pofManager.iGetFlowEntryNumber(switchID, globalTableID);
			middlePanel.list1.listModel.set(7, PList1.tableItems[7] + ((entryNumber == 0) ? "" : "(" + entryNumber + ")") );
		}			

		middlePanel.list2.currentFlowEntry = null;
		middlePanel.list2.clearData();
		
	}
	
	public void checkUsedProtocolInActionList(final List<OFAction> actionList){
		short fieldid;
		if(actionList != null){
			for(OFAction action : actionList){
				switch(action.getType()){
					case SET_FIELD:
						fieldid = ((OFActionSetField)action).getFieldSetting().getFieldId();
						markUsedProtocol(fieldid);
						break;
					case SET_FIELD_FROM_METADATA:
						fieldid = ((OFActionSetFieldFromMetadata)action).getFieldSetting().getFieldId();
						markUsedProtocol(fieldid);
						markUsedMetadata();
						break;
					case MODIFY_FIELD:
						fieldid = ((OFActionModifyField)action).getMatchField().getFieldId();
						markUsedProtocol(fieldid);
						break;
					case ADD_FIELD:
						fieldid = ((OFActionAddField)action).getFieldId();
						markUsedProtocol(fieldid);
						break;
					case OUTPUT:
						if( ((OFActionOutput)action).getMetadataOffset() != 0 
							|| ((OFActionOutput)action).getMetadataLength() != 0 ){
							markUsedMetadata();
						}
						break;
					default:
						break;
				}
			}
		}
	}
	
	public void checkUsedProtocol(final List<OFInstruction> insList){
        //check for used protocol
        if(null != insList){
            for(OFInstruction ins : insList){
            	switch(ins.getType()){
            		case WRITE_METADATA:
            		case WRITE_METADATA_FROM_PACKET:
            			markUsedMetadata();
            			break;
            		case APPLY_ACTIONS:
                		List<OFAction> actionList = ((OFInstructionApplyActions)ins).getActionList();
                		checkUsedProtocolInActionList(actionList);
                		break;
        			default:
        				break;
            	}
            }
        }
	}


	private void initMiddle() {
		middlePanel = new MulitListContainerPanel(this);
		this.add(middlePanel, BorderLayout.CENTER);
		
		middlePanel.setDiamondTablesPanel(diamondTablesPanel);
		middlePanel.setProtocolPanel(mainPanel.getProtocolPanel());
		
		pdiagramPanel = new PDiagramPanel(this);
		pdiagramPanel.setVisible(false);
	}
	
	public void showP() {
		middlePanel.setVisible(false);
		pdiagramPanel.setVisible(true);
		
		this.remove(middlePanel);
			
		this.add(pdiagramPanel, BorderLayout.CENTER);
		
		protocolPanel.setEPViewable(true);

	}

	public void showE() {
		pdiagramPanel.setVisible(false);
		middlePanel.setVisible(true);
		
		this.remove(pdiagramPanel);

		this.add(middlePanel, BorderLayout.CENTER);

		protocolPanel.setEPViewable(false);
	}

	private void initTop() {
		top1();
		top2();
		topPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
		this.add(topPane, BorderLayout.NORTH);
	}
	
	
	private void top2() {
		JPanel top2Pane = new NOPanel(new BorderLayout());
		diamondTablesPanel = new DiamondTablesPanel();
		scrollPane = new JScrollPane(diamondTablesPanel);
		scrollPane.setOpaque(false);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getViewport().setOpaque(false);
		top2CenterPane = new NOPanel();
		FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
		fl.setHgap(1);
		top2CenterPane.setLayout(fl);
		
		JLabel label3 = new JLabel("<html><body> Add New <br> Table <body></html>");
		label3.setForeground(HomeMain.LabelColor);

		addNewTableButton.setPreferredSize(new Dimension(40, 41));
		
		top2CenterPane.add(scrollPane);
		JLabel Label = new JLabel(ImageUtils.getImageIcon("image/gui/swing/bg.png"));
		Label.setPreferredSize(new Dimension(1, 70));
		top2CenterPane.add(Label);
		
		JPanel addNewTableButtonPane = new NOPanel();
		addNewTableButtonPane.add(addNewTableButton);
		top2CenterPane.add(addNewTableButtonPane);
		top2CenterPane.add(label3);
		// right
		leftButton.setPreferredSize(new Dimension(20, 80));
		rightButton.setPreferredSize(new Dimension(20, 80));
		
		diamondTablesPanel.setLeftAndRight(leftButton,rightButton,scrollPane);
		
		leftButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		rightButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		
		top2Pane.add(leftButton, BorderLayout.WEST);
		top2Pane.add(rightButton, BorderLayout.EAST);
		top2Pane.add(top2CenterPane, BorderLayout.CENTER);

		topPane.add(top2Pane);

		addNewTableButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(comboxSwitchID.getSelectedIndex() == -1 || (switchID == IPMService.SWITCHID_INVALID)){
					GUITools.messageDialog(SwitchPanel.this, "please choose switch first."); 
					return;
				}else{
					Component[] diamondTablesChildren = diamondTablesPanel.getComponents();
					if(null != diamondTablesChildren){
						for (Component c : diamondTablesChildren) {
							if (c instanceof SingleDiamondTablePanel) {
								if (((SingleDiamondTablePanel) c).ofTable == null) {
									GUITools.messageDialog(SwitchPanel.this, "please finish current one first."); 
									return;
								}
							}
						}
					}

					SingleDiamondTablePanel tSBXPane = new SingleDiamondTablePanel(SwitchPanel.this, middlePanel, null);
				    diamondTablesPanel.add(tSBXPane);
			    
					scrollPane.validate();
					scrollPane.doLayout();
					top2CenterPane.validate();
				}
			}
		});
	}

	private void top1() {
		JPanel top1Pane = new NOPanel();
		JPanel top1LeftPane = new NOPanel();
		JPanel top1RightPane = new NOPanel();

		FlowLayout fl = new FlowLayout(FlowLayout.RIGHT);
		fl.setVgap(0);
		fl.setHgap(0);
		top1RightPane.setLayout(fl);

		top1Pane.setLayout(new BorderLayout());
		top1LeftPane.add(lbPacket);

		lbPacket.setForeground(HomeMain.systemLabelColor);
		lbPacket.setFont(new Font(HomeMain.systemFontName, Font.BOLD, 16));
		lbPacket.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));

		comboxSwitchID = new PJComboBox(new String[]{""});
		comboxSwitchID.setCursor(new Cursor(Cursor.HAND_CURSOR));
		comboxSwitchID.setSelectedIndex(-1);
		comboxSwitchID.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				removeTabales();
				
				if(arg0.getStateChange() == ItemEvent.SELECTED) {					
					if(arg0.getSource() == comboxSwitchID) {   
						int index = comboxSwitchID.getSelectedIndex(); 
						if(index != -1  && index != 0){
							String switchIDString = (String)comboxSwitchID.getSelectedItem();
							if(switchIDString == null || switchIDString.equals("") || !switchIDString.matches(GUITools.RE_HEX)){
								switchID = IPMService.SWITCHID_INVALID;
								return;
							}else{
								switchID = (int)Long.parseLong(switchIDString, 16);
							}
							if(switchID != IPMService.SWITCHID_INVALID){
								if(null == (flowTable_flowEntryListMap = switch_flowTable_flowEntryListMap.get(switchID)) ){
									flowTable_flowEntryListMap = new HashMap<Byte, List<FlowEntry>>();
									switch_flowTable_flowEntryListMap.put(switchID, flowTable_flowEntryListMap);
								}
								
								if(SwingUIPanel.pofManager.iGetAllFlowTable(switchID) != null
										&& SwingUIPanel.pofManager.iGetAllFlowTable(switchID).size() != 0){
									List<OFFlowTable> tableList = SwingUIPanel.pofManager.iGetAllFlowTable(switchID);
									
									displayTabales(tableList);									
								}
							}
						}else{
							switchID = IPMService.SWITCHID_INVALID;
						}
					} 
				}
			}
		});
		top1LeftPane.add(comboxSwitchID);
		
		// right
		eButton.setPreferredSize(new Dimension(40, 29));
		pButton.setPreferredSize(new Dimension(40, 29));
		top1RightPane.add(eButton);
		top1RightPane.add(pButton);
		
		eButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showE();
			}
		});
		
		pButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//TODO showp
				//showP();
			}
		});

		top1Pane.add(top1LeftPane, BorderLayout.WEST);
		top1Pane.add(top1RightPane, BorderLayout.EAST);

		topPane.add(top1Pane, BorderLayout.NORTH);
	}

	public void displayTabales(final List<OFFlowTable> tableList){
		if(null == tableList){
			return;
		}
		
		for(OFFlowTable flowTable : tableList){
			SingleDiamondTablePanel tSBXPane = new SingleDiamondTablePanel(this, middlePanel, flowTable);
			diamondTablesPanel.add(tSBXPane);
		}
		
		middlePanel.list1.DisplayList1(null, 0, null);
		
		scrollPane.validate();
		scrollPane.doLayout();
		top2CenterPane.validate();
	}
	
	public void removeTabales(){
		diamondTablesPanel.removeAll();
		diamondTablesPanel.setSelected(null);
		
		middlePanel.list1.DisplayList1(null, 0, null);
		
		scrollPane.validate();
		scrollPane.doLayout();
		top2CenterPane.validate();
	}

	public void reloadSwitchPanel() {
		removeTabales();
		comboxSwitchID.setSelectedIndex(-1);
	}
	
	public void resetFlowEntryListMap(){
		switch_flowTable_flowEntryListMap.clear();
		
		flowTable_flowEntryListMap = null;

	}
}
