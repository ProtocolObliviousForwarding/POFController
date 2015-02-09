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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.JTextComponent;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 */
public class RoundedFieldUI extends BasicTextFieldUI {
	private int round = 5;
	private int shadeWidth = 2;
	private int textSpacing = 3;

	public void installUI(JComponent c) {
		super.installUI(c);

		c.setOpaque(false);

		int s = shadeWidth + 1 + textSpacing;
		c.setBorder(BorderFactory.createEmptyBorder(s, s, s, s));
	}

	protected void paintSafely(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Shape border = getBorderShape();

		Stroke os = g2d.getStroke();
		g2d.setStroke(new BasicStroke(shadeWidth * 2));
		g2d.setPaint(new Color(224, 224, 224));
		g2d.draw(border);
		g2d.setStroke(os);

		g2d.setPaint(Color.WHITE);
		g2d.fill(border);

		g2d.setPaint(new Color(224, 224, 224));
		g2d.draw(border);

		super.paintSafely(g);
	}

	private Shape getBorderShape() {
		JTextComponent component = getComponent();
		if (round > 0) {
			return new RoundRectangle2D.Double(shadeWidth, shadeWidth
					, component.getWidth() - shadeWidth * 2 - 1, component.getHeight() - shadeWidth * 2 - 1
					, round * 2, round * 2);
		} else {
			return new Rectangle2D.Double(shadeWidth, shadeWidth
					, component.getWidth() - shadeWidth * 2 - 1, component.getHeight() - shadeWidth * 2 - 1);
		}
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();

		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
		frame.add(panel);

		panel.add(new JLabel("Field:"), BorderLayout.NORTH);

		JTextField field = new JTextField(20);
		field.setUI(new RoundedFieldUI());
		panel.add(field);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}