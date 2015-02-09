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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.openflow.protocol.OFMatch20;

import com.huawei.ipr.pof.gui.comm.GUITools.EDIT_STATUS;
import com.huawei.ipr.pof.gui.swing.HomeMain;
import com.huawei.ipr.pof.gui.swing.SwingUIPanel;
import com.huawei.ipr.pof.gui.swing.comutil.ImageUtils;
import com.huawei.ipr.pof.manager.IPMService;

@SuppressWarnings("serial")
public class ProtocalItemImageButton extends JButton {
	//the field button inside the protocol rectangle

	public final String blue = "image/gui/swing/di-blue.png";
	public final String green = "image/gui/swing/di-green.png";

	public int guilength;
	public int logicLength;
	private OFMatch20 matchField;
	public double onePercentWidthDouble;	

	protected ProtocolCC packetCC;
	protected boolean isDefColor = true;
	
	protected Image bg;
	protected TexturePaint bgPaint;
	protected TexturePaint bgPaint2;
	
	public EDIT_STATUS editStatus = EDIT_STATUS.ES_READONLY;


	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		return new Dimension((int)(onePercentWidthDouble * guilength), (int) d.getHeight());
	}

	public static Rectangle getTextBound(String text, Font font) {
		FontRenderContext frc = new FontRenderContext(null, true, true);
		GlyphVector glyphVector = font.createGlyphVector(frc, text);
		Rectangle2D rect = glyphVector.getLogicalBounds();
		return new Rectangle(0, 0, (int) rect.getWidth(), (int) rect.getHeight());
	}

	public OFMatch20 getMatchField() {
		return matchField;
	}

	public void setMatchField(OFMatch20 matchField) {
		this.matchField = matchField;
		
		if(matchField != null){
			if(matchField.getFieldId() == -1){	//metadata
				this.setToolTipText(matchField.getFieldName() + "(o=" + matchField.getOffset() + ";l=" + matchField.getLength() + ")");
			}else{								//protocol
				this.setToolTipText(matchField.getFieldName() + "(id=" + matchField.getFieldId() + ";o=" + matchField.getOffset() + ";l=" + matchField.getLength() + ")");
			}
		}else{
			this.setToolTipText(null);
		}
	}

	protected void paintComponent(Graphics g) {
		// super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		if (isDefColor) {
			g2d.setPaint(bgPaint);
		} else {
			g2d.setPaint(bgPaint2);
		}
		Dimension d = getPreferredSize();

		Rectangle2D.Double rectbg = new Rectangle2D.Double(0, 0, d.getWidth() - 1, d.getHeight());
		g2d.fill(rectbg);
		// g2d.fillRect(0, 0, width, (int) d.getHeight());
		String text = this.getText();

		Rectangle rect = getTextBound(text, g.getFont());
		if (!"".equals(text)) {
			g.setColor(HomeMain.packetLabelColor);
			if (rect.width > d.getWidth()) {
				text = text.substring(0, 2) + "...";
			}
			if (text.length() == 1 && guilength == 1) {
				g.drawString(text, 2, 18);
			} else {
				if (d.getWidth() <= 10) {
					g.drawString(text, 0, 18);
				} else {
					g.drawString(text, 10, 18);
				}
			}
		}
	}

	public static TexturePaint createTexturePaint(Image image) {
		int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);
		BufferedImage bi = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = bi.createGraphics();
		g2d.drawImage(image, 0, 0, null);
		g2d.dispose();
		return new TexturePaint(bi, new Rectangle(0, 0, imageWidth, imageHeight));
	}

	public ProtocalItemImageButton(final ProtocolCC packetCC, final String text, final int length) {
		super(text);
//		this.setToolTipText(text);
		ImageIcon bgIcon = ImageUtils.getImageIcon("image/gui/swing/di-blue.png");
		this.setIcon(bgIcon);
		bg = bgIcon.getImage();
		this.setBorder(null);
		this.setBorderPainted(false);
		this.setContentAreaFilled(false);
		this.setOpaque(false);

		setLength(length);
		logicLength = length;
		this.packetCC = packetCC;
		this.setBorder(null);
		this.setMargin(new Insets(0, 0, 0, 0));

		ImageIcon bgIcon2 = ImageUtils.getImageIcon(green);
		bgPaint = createTexturePaint(bg);
		bgPaint2 = createTexturePaint(bgIcon2.getImage());
		this.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				int button = e.getButton();
				if (button == MouseEvent.BUTTON3) {
					if(packetCC.protocolPanel.currentProtocolButton.isUsed == true
							|| packetCC.protocolPanel.currentProtocolButton.editStatus == EDIT_STATUS.ES_READONLY
							|| matchField == null){
						return;
					}

					JPopupMenu popMenu = new JPopupMenu();
					JMenuItem modifyItem = new JMenuItem("modify");
					popMenu.add(modifyItem);
					modifyItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if(packetCC.protocolPanel.currentProtocolButton.isUsed == true
									|| packetCC.protocolPanel.currentProtocolButton.editStatus == EDIT_STATUS.ES_READONLY
									|| matchField == null){
								return;
							}
							
							modifyItem();
						}
					});

					//delete field 
					JMenuItem deleteItem = new JMenuItem("delete");
					popMenu.add(deleteItem);
                   
					deleteItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if(packetCC.protocolPanel.currentProtocolButton.isUsed == true
									|| packetCC.protocolPanel.currentProtocolButton.editStatus == EDIT_STATUS.ES_READONLY){
								return;
							}
							
							deleteitem();
						}
					});
					
					popMenu.show(ProtocalItemImageButton.this, e.getX(), e.getY());
				} else {
					if (button == 1) {
						int count = e.getClickCount();
						if (count == 2 
								&& true == HomeMain.contentPane.getSwitchPanel().middlePanel.isSelectFieldAbove()) {	//double click to add field into selected list and then used for matchfile in new flowtable
							doubleclickAction();

							changeItemsColor();
						}						
					}
				}
			}
		});
	}

    
	public void setLength(int length) {
		this.guilength = length;
	}
	
	private void modifyItem(){
		packetCC.protocolPanel.fieldInfoInputPanel.namefield.setText(matchField.getFieldName());
		packetCC.protocolPanel.fieldInfoInputPanel.offsetfield.setText(String.valueOf(matchField.getOffset()));
		packetCC.protocolPanel.fieldInfoInputPanel.lengthfield.setInt(matchField.getLength());
		packetCC.protocolPanel.fieldInfoInputPanel.offset = matchField.getOffset();
		
		packetCC.protocolPanel.fieldInfoInputPanel.setEditTarget(this);
		
		editStatus = EDIT_STATUS.ES_MODIFYING;
	}

	private void deleteitem() {
		if(matchField != null
				&& matchField.getFieldId() != IPMService.FIELDID_INVALID){
			SwingUIPanel.pofManager.iDelField(matchField.getFieldId());
		}
		
		updateLeftItems();
		
		if (packetCC.getComponentCount() == 1) {
			packetCC.removeAll();
		} else {
			packetCC.remove(this);
		}
		
		packetCC.protocolPanel.protocolFieldDisplayTable.showDefaultPane();
		
//		packetCC.validate();
////		packetCC.invalidate();
//		packetCC.doLayout();
//		packetCC.repaint();
	}
	
	private void updateLeftItems(){
		//update the left match fields
		if(null != packetCC.protocolPanel.currentProtocolButton.newPacketFieldList){
			packetCC.protocolPanel.currentProtocolButton.newPacketFieldList.remove(matchField);
			short offset = 0;
			for(OFMatch20 field : packetCC.protocolPanel.currentProtocolButton.newPacketFieldList){
				if(field.getOffset() != offset){
					field.setOffset(offset);
					SwingUIPanel.pofManager.iModifyField(field.getFieldId(), field.getFieldName(), field.getLength(), offset);
				}
				
				offset += field.getLength();
			}
			
			packetCC.protocolPanel.fieldInfoInputPanel.offsetfield.setText(String.valueOf(offset));
			packetCC.protocolPanel.fieldInfoInputPanel.offset = offset;
		}
	}
	
	protected void doubleclickAction() {
		if(null == matchField){
			return;
		}
		
		if (packetCC.protocolPanel.currentProtocolButton.editStatus == EDIT_STATUS.ES_READONLY) {
			List<OFMatch20> fieldList =  packetCC.protocolPanel.selectedMatchFieldList;
			if(null != fieldList){
				if(isDefColor){
					//check reduplicated item
					for(OFMatch20 field : fieldList){
						if(matchField.getFieldId() == field.getFieldId()){
							return;
						}
					}
					//add
					fieldList.add(matchField);
				}else{
					fieldList.remove(matchField);
				}
			}
		}
	}

	protected void changeColor() {
		if (packetCC.changeColorFlag) {
			isDefColor = !isDefColor;
		}
	}
	
	private void changeItemsColor() {
		List<ProtocalItemImageButton> items = packetCC.itemButtonList;

		if(null != items){
			for(ProtocalItemImageButton item : items){
				if(item.matchField.getFieldName().equals(matchField.getFieldName())){
					item.changeColor();
				}
			}
			packetCC.repaint();
		}
	}
}
