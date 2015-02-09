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

package com.huawei.ipr.pof.gui.swing.com.protocal;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch20;
import org.openflow.protocol.OFMatchX;
import org.openflow.protocol.OFProtocol;
import org.openflow.protocol.table.OFFlowTable;

import com.huawei.ipr.pof.gui.comm.GUITools;
import com.huawei.ipr.pof.gui.comm.GUITools.EDIT_STATUS;
import com.huawei.ipr.pof.gui.swing.HomeMain;
import com.huawei.ipr.pof.gui.swing.SwingUIPanel;
import com.huawei.ipr.pof.gui.swing.com.MainPanel;
import com.huawei.ipr.pof.gui.swing.comutil.ImageButton;
import com.huawei.ipr.pof.gui.swing.comutil.NOPanel;
import com.huawei.ipr.pof.gui.swing.comutil.TableLayoutEx;
import com.huawei.ipr.pof.manager.IPMService;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
@SuppressWarnings("serial")
public class ProtocolPanel extends NOPanel{
	//the whole protocol part panel
 
	protected JLabel lbPacket = new JLabel("Protocol");
	protected ImageButton plusButton = new ImageButton("image/gui/swing/plusbutton_small.png");
	protected JLabel lbAddNew = new JLabel("Add New Protocol");
	
	protected ImageButton saveButton = new ImageButton("image/gui/swing/save_button.png");
	protected boolean saveButton_WaitingInputName = false;
	
	protected JPanel metaDataTopRightPane = new NOPanel();
	protected JPanel bottomPane;
	public ProtocalPacketCCNewFieldEnterPane fieldInfoInputPanel;
	
	protected SaveNewFieldListener saveFieldListener = new SaveNewFieldListener();
	protected SaveMetaDataListener saveMeteListener = new SaveMetaDataListener();
	protected SaveEditProtocolListener saveEditProtocolListener = new SaveEditProtocolListener();
	
	public List<MetaDataButton> protocolButtonList = new ArrayList<MetaDataButton>();	//not include metadataButton
	
	public List<OFMatch20> selectedMatchFieldList = new ArrayList<OFMatch20>();
	
	public MetaDataButton currentProtocolButton;	//just a reference;
	
	public ProtocolCC protocolFieldDisplayTable = new ProtocolCC(metaDataTopRightPane,this);
	
	public MetaDataButton metadataButton;
	
	protected final MainPanel mainPanel;
	
	public void setEPViewable(boolean isV) {
		if(isV){
			lbPacket.setText("Packet");
			plusButton.setVisible(false);
			lbAddNew.setVisible(false);
			metaDataTopRightPane.setVisible(false);
			
			Component[] cs = protocolFieldDisplayTable.getComponents();
			for(Component c:cs){
				c.setVisible(false);
			}
			
		}else{
			lbPacket.setText("Protocol");
			plusButton.setVisible(true);
			lbAddNew.setVisible(true);
			metaDataTopRightPane.setVisible(true);
			
			Component[] cs = protocolFieldDisplayTable.getComponents();
			for(Component c:cs){
				c.setVisible(true);
			}
		}
	}
	public ProtocolPanel(final MainPanel mainPanel) {
		super();
		this.setLayout(new BorderLayout());
		
		this.mainPanel = mainPanel;
		
		metadataButton = new MetaDataButton("Metadata", protocolFieldDisplayTable);
		metadataButton.isMetadata = true;
		metadataButton.newPacketFieldList = SwingUIPanel.pofManager.iGetMetadata();
		currentProtocolButton = metadataButton;
		
		initTop();
		initMiddle();
		initBottom();
	}
	
	private void initBottom() {
		bottomPane = new NOPanel();
		bottomPane.setLayout(new BorderLayout());
		
		JPanel newFieldEnterPaneCon = new JPanel();
		TableLayoutEx layout = new TableLayoutEx(
				new double[]
						{ TableLayoutEx.FILL, 
							TableLayoutEx.PREFERRED, 
							TableLayoutEx.FILL }, 
				new double[]
						{ TableLayoutEx.PREFERRED }, 
				0, 
				0);
		newFieldEnterPaneCon.setLayout(layout);
		newFieldEnterPaneCon.setOpaque(false);
		
		fieldInfoInputPanel = new ProtocalPacketCCNewFieldEnterPane(this);
		fieldInfoInputPanel.setTargetTableCC(protocolFieldDisplayTable);
		
		
		newFieldEnterPaneCon.add(fieldInfoInputPanel,"1,0");
		bottomPane.add(newFieldEnterPaneCon,BorderLayout.CENTER);
		
		JPanel saveNewFiledButtonPane = new NOPanel();
		saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		saveButton.setPreferredSize(new Dimension(100,29));
		saveNewFiledButtonPane.add(saveButton);
		
		bottomPane.add(saveNewFiledButtonPane,BorderLayout.SOUTH);
		saveButton.addActionListener(saveFieldListener);
		this.add(bottomPane,BorderLayout.SOUTH);
		
		bottomPane.setVisible(false);
	}

	private void initMiddle() {
		protocolFieldDisplayTable.setOpaque(false);

		JScrollPane scrollPane = new JScrollPane(protocolFieldDisplayTable);
		scrollPane.setPreferredSize(new Dimension(100, protocolFieldDisplayTable.maxHeight));
		scrollPane.setBorder(null);
		
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		protocolFieldDisplayTable.setJScrollPane(scrollPane);
		
		this.add(scrollPane,BorderLayout.CENTER);
	}

	private void initTop() {
		JPanel topPane = new NOPanel();
		JPanel topLeftPane = new NOPanel();
		
		topPane.setLayout(new BorderLayout());
		
		FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
		fl.setVgap(0);
		fl.setHgap(10);
		topLeftPane.setLayout(fl);
		
		fl = new FlowLayout(FlowLayout.RIGHT);
		fl.setVgap(0);
		fl.setHgap(0);
		metaDataTopRightPane.setLayout(fl);
		
		topLeftPane.add(lbPacket);
		topLeftPane.add(plusButton);
		topLeftPane.add(lbAddNew);
		
		plusButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		
		metadataButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		metaDataTopRightPane.add(metadataButton);
		lbPacket.setForeground(HomeMain.systemLabelColor);
		lbPacket.setFont(new Font(HomeMain.systemFontName,Font.BOLD,16));
		
		plusButton.setPreferredSize(new Dimension(24, 24));
		lbPacket.setPreferredSize(new Dimension(100, 26));
		lbAddNew.setForeground(HomeMain.LabelColor);
		
		
		topPane.add(topLeftPane,BorderLayout.WEST);
		topPane.add(metaDataTopRightPane,BorderLayout.EAST);
		
		this.add(topPane,BorderLayout.NORTH);
		
		
		plusButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(currentProtocolButton.isMetadata == false
						&& currentProtocolButton.editStatus != EDIT_STATUS.ES_READONLY ){
					GUITools.messageDialog(ProtocolPanel.this, "Need to finish current one!!");
					return;
				}
				
				doShowAddNewFieldPane();
			}
		});
	}
	
	public void doShowAddNewFieldPane() {
		MetaDataButton newProtocolButton = new MetaDataButton("", protocolFieldDisplayTable);
		currentProtocolButton = newProtocolButton;
		
		newProtocolButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		metaDataTopRightPane.add(newProtocolButton);
		newProtocolButton.doAction();
		newProtocolButton.isMetadata = false;
		
		GUITools.removeAllListeners(saveButton);
		saveButton.addActionListener(saveFieldListener);
		
		protocolButtonList.add(newProtocolButton);
		
		plusButton.setVisible(false);
		lbAddNew.setVisible(false);
		
		showAddNewFieldPanel();
		protocolFieldDisplayTable.showEmptyPane();
		
		saveButton_WaitingInputName = false;
	}
	
	public void doShowEditMetaDataPane() {
		if(currentProtocolButton != metadataButton){
			GUITools.messageDialog(this, "ERROR! currentProtocolButton != metadataButton !");
			return;
		}
		
		plusButton.setVisible(false);
		lbAddNew.setVisible(false);
		
		showAddNewFieldPanel();
		
		GUITools.removeAllListeners(saveButton);
		
		saveButton.addActionListener(saveMeteListener);
	}
	
	public void doShowEditProtocolPane() {
		if(currentProtocolButton == metadataButton){
			GUITools.messageDialog(this, "ERROR! currentProtocolButton == metadataButton !");
			return;
		}
		
		plusButton.setVisible(false);
		lbAddNew.setVisible(false);
		
		showAddNewFieldPanel();
		
		GUITools.removeAllListeners(saveButton);
		
		saveButton.addActionListener(saveEditProtocolListener);
	}
	
	public void showAddNewFieldPanel() {
		bottomPane.setVisible(true);
		fieldInfoInputPanel.setVisible(true);
		
		int offset = 0;
		if(currentProtocolButton != null){
			
			 List<OFMatch20> matchFielsList = currentProtocolButton.newPacketFieldList;
			 if(null != matchFielsList){
				 for(OFMatch20 field : matchFielsList){
					 offset += field.getLength();
				 }
			 }
		}
		
		fieldInfoInputPanel.offsetfield.setText(String.valueOf(offset));
		fieldInfoInputPanel.offset = offset;
	}
	

	class SaveNewFieldListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			saveNewField();
		}
	}
	
	
	class SaveMetaDataListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			saveEditMeta();
		}
	}
	
	class SaveEditProtocolListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			saveEditProtocol();
		}
	}
	
	public void saveEditMeta() {
		if(currentProtocolButton != metadataButton){
			GUITools.messageDialog(this, "ERROR! currentProtocolButton != metadataButton !");
			return ;
		}
		
		currentProtocolButton.editStatus = EDIT_STATUS.ES_READONLY;
		
		bottomPane.setVisible(false);
		fieldInfoInputPanel.setVisible(true);
		plusButton.setVisible(true);
		lbAddNew.setVisible(true);
		
		GUITools.removeAllListeners(saveButton);
		
		fieldInfoInputPanel.offsetfield.setText("0");
		fieldInfoInputPanel.offset = 0;
	}
	
	public void saveEditProtocol() {
		IPMService pofManager = SwingUIPanel.pofManager;
		
		if(currentProtocolButton == metadataButton){
			GUITools.messageDialog(this, "ERROR! currentProtocolButton != metadataButton !");
			return ;
		}
		
		bottomPane.setVisible(false);
		fieldInfoInputPanel.setVisible(true);
		plusButton.setVisible(true);
		lbAddNew.setVisible(true);
		
		GUITools.removeAllListeners(saveButton);
		
		List<OFMatch20> fieldList = currentProtocolButton.newPacketFieldList;
		short fieldId;
		
		OFMatch20 newMatchField;
		List<OFMatch20> newFieldList = new ArrayList<OFMatch20>();
		for(OFMatch20 matchField : fieldList){
			if(IPMService.FIELDID_INVALID != matchField.getFieldId()){	//protocol
				fieldId = pofManager.iNewField(matchField.getFieldName(), matchField.getLength(), matchField.getOffset());
				matchField.setFieldId(fieldId);
				
				newMatchField = pofManager.iGetMatchField(fieldId);

				newFieldList.add(newMatchField);
			}else{	//metadata
				newFieldList.add(matchField);
			}
		}
		
		String protocolName = currentProtocolButton.text1;
		OFProtocol protocol = pofManager.iGetProtocol(protocolName);
		short protocolID = protocol.getProtocolId();
		
		//modify protocol
		pofManager.iModifyProtocol(protocolID, newFieldList);
		currentProtocolButton.newPacketFieldList = newFieldList;
		
		currentProtocolButton.editStatus = EDIT_STATUS.ES_READONLY;
		
		fieldInfoInputPanel.offsetfield.setText("0");
		fieldInfoInputPanel.offset = 0;
	}
	
	//save
	public void saveNewField() {
		if(currentProtocolButton == metadataButton){
			GUITools.messageDialog(this, "ERROR! currentProtocolButton == metadataButton !");
			return;
		}
		
		if(false == saveButton_WaitingInputName){	//waiting to input protocol name
			saveButton_WaitingInputName = true;
			
			protocolFieldDisplayTable.showEnterPane();
			fieldInfoInputPanel.setVisible(false);
		}else{	//submit the protocol
			IPMService pofManager = SwingUIPanel.pofManager;
			
			String protocolName = protocolFieldDisplayTable.enterPane.field.getText();
			if(null == protocolName || 0 == protocolName.length()){
				GUITools.messageDialog(this, "please input protocolName!");
				return ;
			}
			
			for(MetaDataButton protocolButton : protocolButtonList){
				if(protocolButton.text1.equalsIgnoreCase(protocolName)){
					GUITools.messageDialog(this, "protocol name conflicted!");
					return;
				}
			}
			
			currentProtocolButton.setTextttt(protocolName);
			
			protocolFieldDisplayTable.showDefaultPane();
			
			metaDataTopRightPane.updateUI();
			
			bottomPane.setVisible(false);
			protocolFieldDisplayTable.setVisible(true);
			plusButton.setVisible(true);
			lbAddNew.setVisible(true);
			
			saveButton_WaitingInputName = false;
			
			GUITools.removeAllListeners(saveButton);
			
			protocolButtonList.add(this.currentProtocolButton);
			
			List<OFMatch20> fieldList = currentProtocolButton.newPacketFieldList;
			short fieldId;
			
			OFMatch20 newMatchField;
			List<OFMatch20> newFieldList = new ArrayList<OFMatch20>();
			for(OFMatch20 matchField : fieldList){
				fieldId = pofManager.iNewField(matchField.getFieldName(), matchField.getLength(), matchField.getOffset());
				matchField.setFieldId(fieldId);
				
				newMatchField = pofManager.iGetMatchField(fieldId);

				newFieldList.add(newMatchField);
			}
			pofManager.iAddProtocol(protocolName, fieldList);
			currentProtocolButton.newPacketFieldList = newFieldList;
			currentProtocolButton.editStatus = EDIT_STATUS.ES_READONLY;
		}
	}
	
	public OFMatch20 getBelongedMetaDataName(final int offset, final int length){
		List<OFMatch20> fieldList = metadataButton.newPacketFieldList;
		if(fieldList == null){
			return null;
		}
		
		for(OFMatch20 field : fieldList){
			if( field.getOffset() >= offset ){
				if( field.getLength() <= length){
					return field;
				}
				return null;
			}
		}
		
		return null;
	}

	public void reloadProtocolPanel() {
		IPMService pofManager = SwingUIPanel.pofManager;
		
		//remove all metadatabutton first		
		protocolButtonList.clear();
		metaDataTopRightPane.removeAll();
		
		//metadata
		metaDataTopRightPane.add(metadataButton);
		metadataButton.newPacketFieldList = pofManager.iGetMetadata();
		metadataButton.editStatus = EDIT_STATUS.ES_READONLY;
		
		//protocols
		List<OFProtocol> protocolList =  pofManager.iGetAllProtocol();
		if(protocolList != null){
			for(OFProtocol protocol: protocolList){
				MetaDataButton newProtocolButton = new MetaDataButton(protocol.getProtocolName(), protocolFieldDisplayTable);
				
				metaDataTopRightPane.add(newProtocolButton);
				newProtocolButton.isMetadata = false;
				newProtocolButton.editStatus = EDIT_STATUS.ES_READONLY;
				
				newProtocolButton.reloadField(protocol.getAllField());
				
				protocolButtonList.add(newProtocolButton);
			}
		}
		
		recheckUsedMetadata();
		
		saveButton_WaitingInputName = false;
		bottomPane.setVisible(false);
		protocolFieldDisplayTable.setVisible(true);
		plusButton.setVisible(true);
		lbAddNew.setVisible(true);
		fieldInfoInputPanel.setVisible(false);
		
		currentProtocolButton = null;
		protocolFieldDisplayTable.showEmptyPane();
		
		validate();
		repaint();
		
	}
	private void recheckUsedMetadata() {
		IPMService pofManager = SwingUIPanel.pofManager;
		List<Integer> switchIDList = pofManager.iGetAllSwitchID();
		
		List<OFFlowTable> flowTableList;
		List<OFFlowMod> flowEntryList;
		byte globalTableId;
		List<OFMatchX> matchXList;
		
		if(null != switchIDList){
			for(int switchId : switchIDList){
				flowTableList = pofManager.iGetAllFlowTable(switchId);
				if(null != flowTableList){
					for(OFFlowTable flowTable : flowTableList){
						globalTableId = pofManager.parseToGlobalTableId(switchId, flowTable.getTableType().getValue(), flowTable.getTableId());
						flowEntryList = pofManager.iGetAllFlowEntry(switchId, globalTableId);
						if(null != flowEntryList){
							for(OFFlowMod flowEntry : flowEntryList){
								matchXList = flowEntry.getMatchList();
								if(null != matchXList){
									for(OFMatchX matchX : matchXList){
										mainPanel.getSwitchPanel().markUsedProtocol(matchX.getFieldId());
									}
								}
								
								mainPanel.getSwitchPanel().checkUsedProtocol(flowEntry.getInstructionList());
							}
						}
					}
				}
			}
		}
	}
	
}
