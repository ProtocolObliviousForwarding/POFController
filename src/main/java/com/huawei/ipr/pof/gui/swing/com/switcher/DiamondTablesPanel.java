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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import com.huawei.ipr.pof.gui.swing.comutil.ImageButton;
import com.huawei.ipr.pof.gui.swing.comutil.NOPanel;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
@SuppressWarnings("serial")
public class DiamondTablesPanel extends NOPanel {
	private SingleDiamondTablePanel currentSelected = null;

	public DiamondTablesPanel() {
		FlowLayout fl = new FlowLayout(FlowLayout.LEADING);
		fl.setHgap(5);
		this.setLayout(fl);
		
	}
	
	public void setSelected(SingleDiamondTablePanel currentSelected){
		if(null != currentSelected && currentSelected.isSelected == true){
			this.currentSelected = currentSelected;
		}else{
			this.currentSelected = null;
		}
	}
	
	public SingleDiamondTablePanel getSelected(){
		if(null != currentSelected && currentSelected.isSelected == true){
			return currentSelected;
		}else{
			return null;
		}
	}

	public int getSelectedX() {
		Component[] children = getComponents();
		for (Component c : children) {
			if (c instanceof SingleDiamondTablePanel) {
				if (((SingleDiamondTablePanel) c).isSelected) {
					int v = scrollPane.getHorizontalScrollBar().getValue();
					return ((SingleDiamondTablePanel) c).getX() - v;
				}
			}
		}
		return 0;
	}
	
	public void doLayout() {
		super.doLayout();
		Window win = SwingUtilities.windowForComponent(this);
		if(win!= null){
			int w = win.getWidth();
			int visibleWidth = w - 220;
			int contentWidth = this.getWidth();
			if((visibleWidth - contentWidth) < 0){
				scrollPane.setPreferredSize(new Dimension(visibleWidth, this.getHeight()));
			}
		}
 
	}
	
 

	public JScrollPane scrollPane;
	public void setLeftAndRight(ImageButton leftButton, ImageButton rightButton, final JScrollPane scrollPane) {
		this.scrollPane = scrollPane;
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER );
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER  );
		
		rightButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int v = scrollPane.getHorizontalScrollBar().getValue();
				scrollPane.getHorizontalScrollBar().setValue(v + 50);
			}
		});
		
		leftButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int v = scrollPane.getHorizontalScrollBar().getValue();
				scrollPane.getHorizontalScrollBar().setValue(v - 50);
			}
		});
	}
}
