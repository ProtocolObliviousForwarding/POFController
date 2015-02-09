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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.openflow.protocol.OFMatch20;
import org.openflow.protocol.OFProtocol;

import com.huawei.ipr.pof.gui.comm.GUITools;
import com.huawei.ipr.pof.gui.comm.GUITools.EDIT_STATUS;
import com.huawei.ipr.pof.gui.swing.HomeMain;
import com.huawei.ipr.pof.gui.swing.SwingUIPanel;
import com.huawei.ipr.pof.gui.swing.comutil.ImageUtils;
import com.huawei.ipr.pof.manager.IPMService;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
@SuppressWarnings("serial")
public class MetaDataButton extends JButton {

	protected ProtocolCC packetCC;
	protected String text1;
	
	public List<OFMatch20> newPacketFieldList = new ArrayList<OFMatch20>();

	public boolean isMetadata = false;
	public EDIT_STATUS editStatus = EDIT_STATUS.ES_ADDING;
	
	public boolean isUsed = false;	

	public MetaDataButton(final String text, final ProtocolCC packetCC) {
		initi("image/gui/swing/metadata_bg1.png");
		this.text1 = text;
		this.packetCC = packetCC;
		this.setPreferredSize(new Dimension(80, 26));

		addMouseListener(new MouseAdapter() {
		    public void mouseReleased(MouseEvent e) {
		    	MetaDataButton cur = packetCC.protocolPanel.currentProtocolButton;
		    	if(cur != null
		    			&& cur != MetaDataButton.this
		    			&& cur.isMetadata == false
		    			&& cur.editStatus != EDIT_STATUS.ES_READONLY){
		    		GUITools.messageDialog(MetaDataButton.this, "Need to finish current one!!");
		    		return;
		    	}
		    	
		    	packetCC.protocolPanel.currentProtocolButton = MetaDataButton.this;
		    	
		    	packetCC.showDefaultPane();
		    	
		    	doAction();
		    	
		    	int button = e.getButton();
		    	
		    	if(button == MouseEvent.BUTTON1
		    			&& isMetadata == true
		    			&& isUsed == false
		    			&& e.getClickCount() >= 2
		    			&& packetCC.protocolPanel.bottomPane.isVisible() == false){
					MetaDataButton.this.editStatus = EDIT_STATUS.ES_MODIFYING;
					
					packetCC.protocolPanel.doShowEditMetaDataPane();
		    	}
		    	
		    	if(button == MouseEvent.BUTTON3){
		    		if(isUsed == true){
		    			return;
		    		}
		    		
		    		JPopupMenu popMenu = new JPopupMenu();
		    		JMenuItem editItem = new JMenuItem("edit");
		    		popMenu.add(editItem);
		    		//edit protocol
		    		editItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
				    		if(isUsed == true){
				    			return;
				    		}
				    		
							MetaDataButton.this.editStatus = EDIT_STATUS.ES_MODIFYING;
							
							if(true == isMetadata){
								packetCC.protocolPanel.doShowEditMetaDataPane();
							}else{
								packetCC.protocolPanel.doShowEditProtocolPane();
							}
						}
		    		});
			    		
		    		if(isMetadata == true){
		    			popMenu.show(MetaDataButton.this, e.getX(), e.getY());
		    			return;
		    		}
			    		
		    		JMenuItem delItem = new JMenuItem("delete");
		    		popMenu.add(delItem);
		    		popMenu.show(MetaDataButton.this, e.getX(), e.getY());
		    		delItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
				    		if(isUsed == true || isMetadata == true){
				    			return;
				    		}
				    		
							//delete all fields
							if(null != newPacketFieldList){
								for(OFMatch20 field : newPacketFieldList){
									if(IPMService.FIELDID_INVALID != field.getFieldId()){
										SwingUIPanel.pofManager.iDelField(field.getFieldId());
									}
								}
							}
							
							//delete protocol
							if(null != MetaDataButton.this.text1){
								OFProtocol protocol = SwingUIPanel.pofManager.iGetProtocol(MetaDataButton.this.text1);
								if(null != protocol){
									SwingUIPanel.pofManager.iDelProtocol(protocol.getProtocolId());
								}
							}
							
							newPacketFieldList.clear();
							
							//remove swing components
							packetCC.protocolPanel.protocolButtonList.remove(MetaDataButton.this);
							packetCC.protocolPanel.metaDataTopRightPane.remove(MetaDataButton.this);
							
							packetCC.protocolPanel.currentProtocolButton = null;
							packetCC.protocolPanel.protocolFieldDisplayTable.showEmptyPane();
							
							//repaint
							packetCC.protocolPanel.validate();
							packetCC.protocolPanel.repaint();
						}
		    		});
		    	}	    	
		    }
		});
	}
	
	public  void setTextttt(String tt) {
		text1 = tt;
	}
	
	public String getName(){
		return text1;
	}

	private void initi(String img) {
		this.setIcon(ImageUtils.getImageIcon(img));
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setBorderPainted(false);
		this.setContentAreaFilled(false);
		this.setOpaque(false);
	}

	Color cc = HomeMain.LabelColor;
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(cc);
		int w = 80;
//		int h = 26;
		Font font = g.getFont();
		FontRenderContext frc = new FontRenderContext(null, true, true);
		GlyphVector glyphVector = font.createGlyphVector(frc, text1);
		Rectangle2D rect = glyphVector.getLogicalBounds();
		
		g.drawString(text1, (int)(w - rect.getWidth()) / 2, 18);
	}

	public void doAction() {
		Container parent = this.getParent();
		Component[] comps = parent.getComponents();
		for (Component c : comps) {
			if (c instanceof MetaDataButton) {
				MetaDataButton bu = (MetaDataButton) c;
				bu.disImage();
			}
		}
		reSet();
	}

	public void disImage() {
		this.setIcon(ImageUtils.getImageIcon("image/gui/swing/metadata_bg_b.png"));
		cc = HomeMain.packetLabelColor;
	}

	public void reSet() {
		this.setIcon(ImageUtils.getImageIcon("image/gui/swing/metadata_bg1.png"));
		cc = HomeMain.LabelColor;
	}

	public void reloadField(List<OFMatch20> allFieldList) {
		newPacketFieldList.clear();
		try {
			if(allFieldList != null){
				for(OFMatch20 field : allFieldList){	
					newPacketFieldList.add(field.clone());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
