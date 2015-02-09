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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import com.huawei.ipr.pof.gui.swing.comutil.BackgroundImageListCellRenderer;
import com.huawei.ipr.pof.gui.swing.comutil.ImageUtils;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 */
@SuppressWarnings("serial")
public class PList extends JList {
	
	protected Image def_img = ImageUtils.getImageIconByCaches("image/gui/swing/listbg.png").getImage();
	protected BackgroundImageListCellRenderer renderer = new BackgroundImageListCellRenderer();
	protected final DefaultListModel listModel;
	
	public PList() {
		super();
		setCellRenderer(renderer);
		
		listModel = new DefaultListModel();
		setModel(listModel);
		
		setOpaque(false);
	}
	
	public void clearData() {
		listModel.clear();
		
		clearSelection();
		
		clearNextList();
		
		validate();
		repaint();
	}
	
	public void clearNextList(){
		
	}
	
    protected void paintComponent(Graphics g) {
		g.drawImage(def_img, 0, 0, this.getWidth(), this.getHeight(), null);
		super.paintComponent(g);
	}
    
	protected void paintBorder(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		int w =  this.getWidth();
		int h =  this.getHeight();
 
		g2d.setPaint(new Color(120, 120, 120));
		g2d.drawLine(w - 1, 0, w - 1, h);
	}
}
