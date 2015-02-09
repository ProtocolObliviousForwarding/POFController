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

package com.huawei.ipr.pof.gui.swing.comutil;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.border.Border;

import com.huawei.ipr.pof.gui.swing.HomeMain;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 */
@SuppressWarnings("serial")
public class BackgroundImageListCellRenderer extends DefaultListCellRenderer {
	public Image def_img = ImageUtils.getImageIconByCaches("image/gui/swing/listbg.png").getImage();
	public Image select_img = ImageUtils.getImageIconByCaches("image/gui/swing/list_select.png").getImage();

	protected boolean isSelected = false;

	public BackgroundImageListCellRenderer() {
		this.setForeground(HomeMain.systemLabelColor);
		this.setHorizontalAlignment(JLabel.LEFT);
		setOpaque(false);
	}

	public Component getListCellRendererComponent(JList list, Object value, int row, boolean isSelected, boolean cellHasFocus) {
		setComponentOrientation(list.getComponentOrientation());
		this.isSelected = isSelected;

		this.setPreferredSize(new Dimension((int)this.getSize().getWidth(),24));
		
		if (value instanceof Icon) {
			setIcon((Icon) value);
			setText("");
		} else {
			setIcon(null);
			setText((value == null) ? "" : "  " + value.toString());
		}

		setEnabled(list.isEnabled());
		setFont(list.getFont());

		Border border = BorderFactory.createEmptyBorder();
		setBorder(border);

		return this;
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		if (!isSelected) {
			g2d.drawImage(def_img, 0, 0, this.getWidth(), this.getHeight(), null);
		} else {
			g2d.drawImage(select_img, 0, 0, this.getWidth(), this.getHeight(), null);
		}
		super.paintComponent(g);
	}
}
