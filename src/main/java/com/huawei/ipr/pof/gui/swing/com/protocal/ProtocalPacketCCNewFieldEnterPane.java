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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openflow.protocol.OFGlobal;
import org.openflow.protocol.OFMatch20;

import com.huawei.ipr.pof.gui.comm.GUITools;
import com.huawei.ipr.pof.gui.comm.GUITools.EDIT_STATUS;
import com.huawei.ipr.pof.gui.swing.SwingUIPanel;
import com.huawei.ipr.pof.gui.swing.comutil.ImageButton;
import com.huawei.ipr.pof.gui.swing.comutil.ImageUtils;
import com.huawei.ipr.pof.gui.swing.comutil.IntegerField;
import com.huawei.ipr.pof.gui.swing.comutil.RoundedFieldUI;
import com.huawei.ipr.pof.manager.IPMService;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
@SuppressWarnings("serial")
public class ProtocalPacketCCNewFieldEnterPane extends JPanel {
	//the bar panel which user could input file name/offset/length when add/modify the field in protocol panel
	
	public Image background = ImageUtils.getImageIcon("image/gui/swing/newfield_bg.png").getImage();
	protected ImageButton okButton = new ImageButton("image/gui/swing/ok.png");
	protected JTextField namefield = new JTextField(6);
	protected IntegerField lengthfield = new IntegerField(6);
	public IntegerField offsetfield = new IntegerField(6);
	public int offset = 0;
	private ProtocolPanel protocolPanel;

	protected ProtocolCC packetCCtable;
	protected ProtocalItemImageButton protocalItemImageButton;
	
	public ProtocalPacketCCNewFieldEnterPane(ProtocolPanel protoPanel) {
		this.protocolPanel = protoPanel;
		namefield.setUI(new RoundedFieldUI());
		lengthfield.setUI(new RoundedFieldUI());
		offsetfield.setUI(new RoundedFieldUI());
		offsetfield.setEditable(false);
		okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		setOpaque(false);
		this.setLayout(null);
		this.add(namefield);
		this.add(lengthfield);
		this.add(okButton);
		this.add(offsetfield);
		offsetfield.setInt(0);

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okButtonSubmit();
			}
		});
		
	
		lengthfield.addKeyListener(new java.awt.event.KeyAdapter() { 
			public void keyPressed(java.awt.event.KeyEvent evt) { 
				if (evt.getKeyCode() == (char)KeyEvent.VK_ENTER) {
					okButtonSubmit();
				}
			}
		});
	}
	
	private void okButtonSubmit(){
		if(protocolPanel.currentProtocolButton.editStatus != EDIT_STATUS.ES_READONLY
				&& protocalItemImageButton == null){
			OFMatch20 newFieldNode = createField();
			if(newFieldNode != null){
				packetCCtable.addItem(newFieldNode, true);
				
				offset = offset + Integer.parseInt(lengthfield.getText());
				
				namefield.setText("");
				lengthfield.setText("");
				offsetfield.setText(String.valueOf(offset));
			}else{
				return;
			}
		}else if(protocalItemImageButton != null && protocalItemImageButton.editStatus == EDIT_STATUS.ES_MODIFYING){
			modField();
		}
		packetCCtable.validate();
		namefield.requestFocus();
	}
	
	public OFMatch20 createField(){
		OFMatch20 newFieldNode = null;
		
		if (namefield.getText().equals("")) {
			GUITools.messageDialog(this, "please input field name."); 
			return null;
		}
		
		if (lengthfield.getText().equals("")) {
			GUITools.messageDialog(this, "please input field length."); 
			return null;
		}
		
		short fieldLength = Short.parseShort(lengthfield.getText());
		short offsetLength = Short.parseShort(offsetfield.getText());
		
		if(fieldLength > OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE * 8){
			GUITools.messageDialog(this, "Max field length is " + OFGlobal.OFP_MAX_FIELD_LENGTH_IN_BYTE * 8 + "bit."); 
			return null;
		}
		
		List<OFMatch20> fieldList = protocolPanel.currentProtocolButton.newPacketFieldList;
		if(null == fieldList){
			return null;
		}
		String name = namefield.getText();
		for(OFMatch20 field : fieldList){	//check field name confliction
			if(field.getFieldName().equalsIgnoreCase(name)){
				GUITools.messageDialog(this, "field name conflicted!");
				return null;
			}
		}
		
		if(protocolPanel.currentProtocolButton.isMetadata == false){	//protocol
			OFMatch20 matchNode = new OFMatch20();
			matchNode.setFieldName(name);
			matchNode.setLength(fieldLength);
			matchNode.setOffset(offsetLength);
			fieldList.add(matchNode);
			
			newFieldNode = matchNode;
		}else{	//metadata
			OFMatch20 metadataNode = new OFMatch20();
			metadataNode.setFieldId(OFMatch20.METADATA_FIELD_ID);
			metadataNode.setFieldName(name);
			metadataNode.setLength(fieldLength);
			metadataNode.setOffset(offsetLength);
			
			fieldList.add(metadataNode);
			
			newFieldNode = metadataNode;
		}
		
		return newFieldNode;
	}
	
	public void modField(){
		if(0 == lengthfield.getInt() 
				|| null == namefield.getText() 
				|| "" == namefield.getText()
				|| protocalItemImageButton == null){
			return;
		}
		
		protocalItemImageButton.setText(namefield.getText());
		protocalItemImageButton.setLength(lengthfield.getInt());
		
		OFMatch20 matchField = protocalItemImageButton.getMatchField();

		String name = namefield.getText();
		short length = (short)lengthfield.getInt();
		
		if(name != matchField.getFieldName()
				|| length != matchField.getLength()){
			matchField.setFieldName(name);
			matchField.setLength(length);
			
			if(matchField.getFieldId() != IPMService.FIELDID_INVALID){
				SwingUIPanel.pofManager.iModifyField(matchField.getFieldId(), matchField.getFieldName(), matchField.getLength(), matchField.getOffset());
			}
			
			short currentOffset = 0;
			for(OFMatch20 field : protocolPanel.currentProtocolButton.newPacketFieldList){
				if(field.getOffset() != currentOffset){
					field.setOffset(currentOffset);
					if(matchField.getFieldId() != IPMService.FIELDID_INVALID){
						SwingUIPanel.pofManager.iModifyField(field.getFieldId(), field.getFieldName(), field.getLength(), currentOffset);
					}
				}
				
				currentOffset += field.getLength();
			}
			
			offsetfield.setText(String.valueOf(currentOffset));
			offset = currentOffset;
			
			namefield.setText("");
			lengthfield.setText("");
			
			protocolPanel.protocolFieldDisplayTable.showDefaultPane();
			
			protocalItemImageButton.editStatus = EDIT_STATUS.ES_READONLY;
			protocalItemImageButton = null;
		}
		
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(366, 40);
	}

	protected void paintComponent(Graphics g) {
		g.drawImage(background, 0, 0, null);
		super.paintComponent(g);

		namefield.setBounds(53, 8, 55, 22);
		lengthfield.setBounds(260, 8, 55, 22);
		okButton.setBounds(320, 2, 32, 32);
		offsetfield.setBounds(165, 8, 45, 22);
		g.setColor(new Color(120, 120, 120));
		g.setFont(new Font("Tahoma", Font.PLAIN, 15));
		g.drawString("Name",12, 24);
		g.drawString("Offset", 122, 24);
		g.drawString("Length", 213, 24);
	}

	public void setTargetTableCC(ProtocolCC packetCCtable) {
		this.packetCCtable = packetCCtable;
	}

	public void setEditTarget(ProtocalItemImageButton protocalItemImageButton) {
		this.protocalItemImageButton = protocalItemImageButton;
	}
}