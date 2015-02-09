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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

import com.huawei.ipr.pof.gui.swing.HomeMain;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 */
public class PJComboBoxUI extends BasicComboBoxUI {

	Image leftButton = ImageUtils.getImageIcon("image/gui/swing/combox_text.png").getImage();

	private JButton arrow;
	private boolean boundsLight = false;
//	private static final int ARCWIDTH = 15;
//	private static final int ARCHEIGHT = 15;

	public PJComboBoxUI() {
		super();
	}

	protected JButton createArrowButton() {
		arrow = new JButton();
		arrow.setIcon(ImageUtils.getImageIcon("image/gui/swing/combox_array.png"));
		arrow.setRolloverEnabled(true);
		arrow.setBorder(null);
		arrow.setOpaque(true);
		arrow.setContentAreaFilled(false);
		return arrow;
	}

	public void paint(Graphics g, JComponent c) {
		hasFocus = comboBox.hasFocus();
		Graphics2D g2 = (Graphics2D) g;
		if (!comboBox.isEditable()) {
			Rectangle r = rectangleForCurrentValue();

			paintCurrentValueBackground(g2, r, hasFocus);
			paintCurrentValue(g2, r, hasFocus);
		}

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (comboBox.isFocusable()) {
			g2.setColor(new Color(150, 207, 254));
		}
	}

	public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
		g.drawImage(leftButton, bounds.x, bounds.y, bounds.width, bounds.height, null);
	}

	public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
		ListCellRenderer renderer = comboBox.getRenderer();
		Component c;

		if (hasFocus && !isPopupVisible(comboBox)) {
			c = renderer.getListCellRendererComponent(listBox, comboBox.getSelectedItem(), -1, true, false);
		} else {
			c = renderer.getListCellRendererComponent(listBox, comboBox.getSelectedItem(), -1, false, false);
			c.setBackground(UIManager.getColor("ComboBox.background"));
		}
		c.setFont(comboBox.getFont());
		c.setForeground(HomeMain.LabelColor);

		// Fix for 4238829: should lay out the JPanel.
		boolean shouldValidate = false;
		if (c instanceof JPanel) {
			shouldValidate = true;
		}

		int x = bounds.x, y = bounds.y, w = bounds.width, h = bounds.height;

		currentValuePane.paintComponent(g, c, comboBox, x, y, w, h, shouldValidate);
	}

	public Dimension getPreferredSize(JComponent c) {
		return super.getPreferredSize(c);
	}

	public boolean isBoundsLight() {
		return boundsLight;
	}

	public void setBoundsLight(boolean boundsLight) {
		this.boundsLight = boundsLight;
	}

	@SuppressWarnings("serial")
	protected ComboPopup createPopup() {
		ComboPopup popup = new BasicComboPopup(comboBox) {
			public void paintBorder(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(new Color(150, 207, 254));
				g2.drawRoundRect(0, -arrow.getHeight(), getWidth() - 1, getHeight() + arrow.getHeight() - 1, 0, 0);
			}
		};
		return popup;
	}
}
