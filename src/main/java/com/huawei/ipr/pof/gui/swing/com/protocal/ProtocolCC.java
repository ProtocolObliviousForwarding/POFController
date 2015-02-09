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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.openflow.protocol.OFMatch20;

import com.huawei.ipr.pof.gui.swing.HomeMain;
import com.huawei.ipr.pof.gui.swing.comutil.ImageUtils;
import com.huawei.ipr.pof.gui.swing.comutil.RoundedFieldUI;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
@SuppressWarnings("serial")
public class ProtocolCC extends JPanel {
	//the big rectangle in the middle of protocol panel
	public Image def_img = ImageUtils.getImageIconByCaches("image/gui/swing/listbg.png").getImage();
	protected BufferedImage glassimage;
	
	public EnterPane enterPane;

	protected int _width = this.getWidth();
	protected int _height = this.getHeight();
	
	public List<ProtocalItemImageButton> itemButtonList = new ArrayList<ProtocalItemImageButton>();

	class EnterPane extends JPanel {
		JLabel label = new JLabel("Enter Protocol Name");
		public JTextField field = new JTextField(40);

		public EnterPane() {
			setOpaque(false);
			field.setUI(new RoundedFieldUI());
			this.setLayout(null);
			label.setFont(new Font(HomeMain.systemFontName, Font.BOLD, 20));
			label.setForeground(HomeMain.systemLabelColor);

			this.add(label);
			this.add(field);
		}

		public void doLayout() {
			super.doLayout();
			label.setBounds(_width / 2 - 95, 20, 300, 20);
			field.setBounds(_width / 2 - 95, 80, 200, 22);
		}
	}

	JPanel metaDataTopRightPane;
	public int maxHeight = 150;
	public ProtocolPanel protocolPanel;

	public ProtocolCC(JPanel metaDataTopRightPane, ProtocolPanel protocolPanel) {
		this.protocolPanel = protocolPanel;
		initDef();
		// this.setPreferredSize(new Dimension(100, maxHeight));
		this.metaDataTopRightPane = metaDataTopRightPane;
	}

	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		Component[] children = this.getComponents();
		if (children == null || children.length == 0) {
			return d;
		}

		int maxY = getHeight(children);
		maxY = maxY + 60;

		return new Dimension((int) d.getWidth(), maxY);
	}

	private int getHeight(Component[] children) {
		int maxY = 0;
		for (Component child : children) {
			int y = child.getY();
			if (maxY < y) {
				maxY = y;
			}
		}
		return maxY;

	}

	private void initDef() {
		ModifiedFlowLayout layout = new ModifiedFlowLayout(FlowLayout.LEFT, 0, 1);
		this.setLayout(layout);

		this.setBorder(null);
		setOpaque(false);
	}

	public void addItem(OFMatch20 matchField, boolean addData) {
		if(null == matchField){
			return;
		}
		LastW last = getLastRowLength();
		int lastRowTotalLength = last.lastTotalLength;
		int lastTotalwidth = last.lastTotalwidth;
		String name = matchField.getFieldName();
		int length = matchField.getLength();

		double onePercentWidthDouble= 0;
		if(lastRowTotalLength == 128){
			onePercentWidthDouble = (double)(_width - 2) / 128;
		}else{
			onePercentWidthDouble = ((double)_width - 2 - lastTotalwidth) / (128 - lastRowTotalLength);
		}

		if ((lastRowTotalLength + length) > 128) {
			int firstLength = 128 - lastRowTotalLength;

			final ProtocalItemImageButton firstLengthitem = new ProtocalItemImageButton(this, name, firstLength);
			firstLengthitem.onePercentWidthDouble = onePercentWidthDouble;
			firstLengthitem.logicLength = length;
			addItem(firstLengthitem);
			
			firstLengthitem.setMatchField(matchField);

			int count = (length - firstLength) / 128;
			if ((length - firstLength) / 128 < 1) {
				int lastLength = length - firstLength;
				ProtocalItemImageButton lastLengthitem = new ProtocalItemImageButton(this, name, lastLength);
				lastLengthitem.onePercentWidthDouble = onePercentWidthDouble;
				lastLengthitem.logicLength = length;
				addItem(lastLengthitem);
				
				lastLengthitem.setMatchField(matchField);
			} else {
				for (int i = 0; i < count; i++) {
					ProtocalItemImageButton countLengthitem = new ProtocalItemImageButton(this, name, 128);
					countLengthitem.onePercentWidthDouble = onePercentWidthDouble;
					countLengthitem.logicLength = length;
					addItem(countLengthitem);
					
					countLengthitem.setMatchField(matchField);
				}
				int _last = length - firstLength - count * 128;
				if (_last > 0) {
					ProtocalItemImageButton lastitem = new ProtocalItemImageButton(this, name, _last);
					lastitem.onePercentWidthDouble = onePercentWidthDouble;
					lastitem.logicLength = length;
					addItem(lastitem);
					lastitem.setMatchField(matchField);
				}
				// 128 * count
			}
		} else {
			final ProtocalItemImageButton item = new ProtocalItemImageButton(this, name, length);
			item.onePercentWidthDouble = onePercentWidthDouble;
			item.logicLength = length;
			addItem(item);
			
			item.setMatchField(matchField);
		}

		protocolPanel.validate();
		protocolPanel.invalidate();
	}

	private LastW getLastRowLength() {
		Component[] children = this.getComponents();
		if (children.length == 0) {
			return new LastW();
		}
		Component[] cs = (Component[]) this.getComponents();
		int maxY = getHeight(cs);
		int maxX = 0;
		for (Component c : cs) {
			if (c.getY() == maxY) {
				if (maxX < c.getX()) {
					maxX = c.getX();
				}
			}
		}

		LastW last = new LastW();
		for (Component c : cs) {
			if (c.getY() == maxY) {
				last.lastTotalLength = last.lastTotalLength + ((ProtocalItemImageButton) c).guilength;
				last.lastTotalwidth = last.lastTotalwidth + c.getWidth();
			}
		}
		return last;
	}

	public boolean changeColorFlag = false;

	private void addItem(ProtocalItemImageButton item) {
		this.add(item);
		
		itemButtonList.add(item);
	}

	public void doLayout() {
		super.doLayout();
		_width = this.getWidth();
		_height = this.getHeight();

		JScrollBar bar = scrollPane.getVerticalScrollBar();
		if(bar.isShowing()){
			_width = _width + bar.getWidth();
		}
	}

	Image top_img = ImageUtils.getImageIconByCaches("image/gui/swing/bg.png").getImage();

	public void paintComponent(Graphics g) {
		// g.drawImage(top_img, 0, 0, this.getWidth(), 3, null);
		g.drawImage(def_img, 0, 0, this.getWidth(), this.getHeight(), null);
		super.paintComponent(g);
	}

	protected void paintBorder(Graphics g) {
		super.paintBorder(g);
		Graphics2D g2d = (Graphics2D) g;
		int w = this.getWidth();
		int h = this.getHeight();

		g2d.setPaint(new Color(120, 120, 120));
		g2d.drawLine(0, h - 1, w, h - 1);
	}

	public void showEnterPane() {
		this.removeAll();
		enterPane = new EnterPane();
		this.setLayout(new BorderLayout());
		this.add(enterPane);

		validate();
		repaint();
	}

	public void showDefaultPane() {
		itemButtonList.clear();
		this.removeAll();
		initDef();
		
		if(null != protocolPanel.currentProtocolButton){
			List<OFMatch20> matchFieldList = protocolPanel.currentProtocolButton.newPacketFieldList;

			if(matchFieldList != null){
				for (OFMatch20 field : matchFieldList) {
					addItem(field, false);
				}
			}
		}

		
		validate();
		repaint();
	}

	public void showEmptyPane() {
		itemButtonList.clear();
		this.removeAll();
		initDef();
		validate();
		repaint();
	}

	class LastW {
		int lastTotalLength = 0;
		int lastTotalwidth = 0;
		
		@Override
		public String toString() {
			return "lastTotalLength:" + lastTotalLength + ",w:" + lastTotalwidth;
		}
	}

	JScrollPane scrollPane;
	public void setJScrollPane(JScrollPane scrollPane) {
		this.scrollPane = scrollPane;
	}

}
