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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import com.huawei.ipr.pof.gui.swing.HomeMain;
import com.huawei.ipr.pof.gui.swing.comutil.ImageButton;
import com.huawei.ipr.pof.gui.swing.comutil.TableImageButton;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 */
@SuppressWarnings("serial")
public class PDiagramPanel extends JPanel {

	ImageButton blueButton1 = new TableImageButton("image/gui/swing/diamond_blue.png");
	ImageButton blueButton2 = new TableImageButton("image/gui/swing/diamond_blue.png");
	ImageButton blueButton3 = new TableImageButton("image/gui/swing/diamond_blue.png");
	ImageButton blueButton4 = new TableImageButton("image/gui/swing/diamond_blue.png");
	ImageButton blueButton5 = new TableImageButton("image/gui/swing/diamond_blue.png");
	
	ImageButton blackButton1 = new TableImageButton("image/gui/swing/diamond_black.png");
	ImageButton blackButton2 = new TableImageButton("image/gui/swing/diamond_black.png");
	ImageButton blackButton3 = new TableImageButton("image/gui/swing/diamond_black.png");
	
	SwitchPanel switchPanel;
	public PDiagramPanel(SwitchPanel switchPanel) {
		this.switchPanel = switchPanel;
		this.setOpaque(false);
		
		this.setLayout(null);
		
		this.add(blueButton1);
		this.add(blueButton2);
		this.add(blueButton3);
		this.add(blueButton4);
		this.add(blueButton5);
		
		this.add(blackButton1);
		this.add(blackButton2);
		this.add(blackButton3);
		
		blueButton1.addMouseListener(new ButtonMouseListener());
		blueButton2.addMouseListener(new ButtonMouseListener());
		blueButton3.addMouseListener(new ButtonMouseListener());
		blueButton4.addMouseListener(new ButtonMouseListener());
		blueButton5.addMouseListener(new ButtonMouseListener());
		blackButton1.addMouseListener(new ButtonMouseListener());
		blackButton2.addMouseListener(new ButtonMouseListener());
		blackButton3.addMouseListener(new ButtonMouseListener());		
	}
	
	class ButtonMouseListener extends MouseAdapter{
		public void mouseClicked(MouseEvent e) {
			super.mouseClicked(e);
			int count = e.getClickCount();
			if(count == 2){
				switchPanel.showE();
			}
		}
	}
	
	
	public void doLayout() {
		super.doLayout();
		int w = this.getWidth();
		int h = this.getHeight();
	    
		int wgap = w / 6;
		int hgap = h / 4;
		
		int startX = 20;
		int startY = 0;
		int imagew1 = 126;
		int imageh1 = 90;
		blueButton1.setBounds(startX, startY, imagew1, imageh1);
		
		startX = startX + wgap;
		blueButton2.setBounds(startX, startY, imagew1, imageh1);
		
		startX = startX + wgap;
		blueButton3.setBounds(startX, startY, imagew1, imageh1);
		
		startX = startX + wgap;
		blackButton1.setBounds(startX, startY, imagew1, imageh1);
 
		startX = 20;
		startY = hgap;
		startX = startX + wgap;
		blackButton2.setBounds(startX, startY, imagew1, imageh1);
		
		startX = startX + wgap * 2;
		blueButton4.setBounds(startX, startY, imagew1, imageh1);
		
		startX = startX + wgap ;
		blueButton5.setBounds(startX, startY, imagew1, imageh1);
		
		startX = 20;
		startX = startX + wgap * 2 ;
		startY = hgap * 2;
		blackButton3.setBounds(startX, startY, imagew1, imageh1);
	}
	
	private final static float[][] dash = {
		{ 8F },
		{ 4F },
		{ 2F },
		{ 1F },
		{ 6F, 1F, 1F, 6F },
		{ 10F, 2F },
		{ 1F, 4F, 1F, 8F },
		{ 1F, 1F, 1F, 1F, 1F, 1F, 1F, 1F, 8F, 4F },
		{ 7F, 7F, 2F },
        { 3F, 6F },					//MIT_DASH1
        { 5F, 5F },					//MIT_DASH2
        { 7F, 5F },					//MIT_DASH3
        { 9F, 5F, 3F, 5F },			//MIT_DASH4
        { 20F, 5F, 6F, 5F }			//MIT_DASH5
  };

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		
		int w = this.getWidth();
		int h = this.getHeight();
		int wgap = w / 6;
		int hgap = h / 4;
		
		int startX = 20;
		int startY = 0;
		
		int imagew1 = 126;
		int imageh1 = 90;
		
		int textStartX = imagew1 - 75;
		int textStartY = imageh1 - 46;
		
		g2d.setColor(HomeMain.blueColor);
		g2d.drawLine(startX +textStartX , startY + textStartY , startX +textStartX + wgap  , startY + textStartY);
		
		g2d.drawLine(startX +textStartX + wgap , startY + textStartY , startX +textStartX + wgap * 2 , startY + textStartY);
		
		g2d.drawLine(startX +textStartX + wgap * 2 + 8 , startY + textStartY , startX +textStartX + wgap * 2 + 8 , startY + textStartY + hgap);
		g2d.drawLine(startX +textStartX + wgap * 2 + 8 , startY + textStartY + hgap , startX +textStartX + wgap * 3 + 8 , startY + textStartY + hgap);
		
		g2d.drawLine(startX +textStartX + wgap * 3 + 8 , startY + textStartY + hgap , startX +textStartX + wgap * 4 + 8 , startY + textStartY + hgap);
		
		
		g2d.setColor(new Color(100, 100, 100));
		
		BasicStroke stroke = new BasicStroke((float) 1, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER, 10F, dash[2], 0F);
		
		g2d.setStroke(stroke);
		g2d.drawLine(startX +textStartX + wgap * 2 , startY + textStartY , startX +textStartX + wgap * 3 , startY + textStartY);
		
		g2d.drawLine(startX +textStartX + 8 , startY + textStartY , startX +textStartX  + 8 , startY + textStartY + hgap);
		g2d.drawLine(startX +textStartX  + 8 , startY + textStartY + hgap , startX +textStartX  + wgap + 8 , startY + textStartY + hgap);
		
		g2d.drawLine(startX +textStartX  + wgap + 8 , startY + textStartY + hgap , startX +textStartX  + wgap + 8 , startY + textStartY + hgap * 2);
		g2d.drawLine( startX +textStartX  + wgap + 8 , startY + textStartY + hgap * 2 , startX +textStartX  + wgap * 2 + 8 , startY + textStartY + hgap * 2);
		
		g2d.drawLine(  startX +textStartX  + wgap * 2 + 8 , startY + textStartY + hgap * 2 , startX +textStartX  + wgap * 2 + 8 , startY + textStartY + hgap * 3);
	}
}
