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

package com.huawei.ipr.pof.gui.swing.com.group;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.border.LineBorder;

import com.huawei.ipr.pof.gui.swing.comutil.ImageUtils;
import com.huawei.ipr.pof.gui.swing.comutil.NOPanel;
import com.huawei.ipr.pof.gui.swing.comutil.PScrollPane;
import com.huawei.ipr.pof.gui.swing.comutil.TableLayoutEx;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
@SuppressWarnings("serial")
public class GroupSetPanel extends NOPanel {
	public static final Image arr_img = ImageUtils.getImageIconByCaches("image/gui/swing/mulitliste_arr.png").getImage();
	protected GroupList1 grouplist1;
	protected GroupList2 grouplist2;
	protected GroupList3 grouplist3;
	protected GroupList4 grouplist4;
	protected GroupList5 grouplist5;
	protected GroupList6 grouplist6;
	
	private PScrollPane jsGroupPane1;
	private PScrollPane jsGroupPane2;
	private PScrollPane jsGroupPane3;
	private PScrollPane jsGroupPane4;
	private PScrollPane jsGroupPane5;
	private PScrollPane jsGroupPane6;
	
	protected int w;
	protected int h;
	
	private TableLayoutEx layout;
	
	protected GroupPane groupPane;
	
	public GroupSetPanel(final GroupPane groupPane) {
		this.groupPane = groupPane;
		initDefault();
	}

	private void initDefault() {
		setOpaque(false);
		
		layout = new TableLayoutEx(new double[] { TableLayoutEx.FILL, TableLayoutEx.FILL, 
													TableLayoutEx.FILL, TableLayoutEx.FILL, 
													TableLayoutEx.FILL, TableLayoutEx.FILL}, 
									new double[] { TableLayoutEx.FILL }, 
									0, 0);
		setLayout(layout);
		
		this.setBorder(BorderFactory.createCompoundBorder(new LineBorder( new Color(0, 128, 255)), 
														BorderFactory.createEmptyBorder(11, 1, 1, 1)));  
		
		grouplist2 = new GroupList2();
		grouplist1 = new GroupList1(groupPane, grouplist2);
		grouplist3 = new GroupList3();
		grouplist4 = new GroupList4();
		grouplist5 = new GroupList5();
		grouplist6 = new GroupList6();
		
		grouplist2.setOtherList(grouplist1,grouplist3);
		grouplist3.setOtherList(grouplist2,grouplist4);
		grouplist4.setOtherList(grouplist3,grouplist5);
		grouplist5.setOtherList(grouplist4,grouplist6);
		grouplist6.setOtherList(grouplist5);
		
		jsGroupPane1 = new PScrollPane(grouplist1);
		jsGroupPane2 = new PScrollPane(grouplist2);
		jsGroupPane3 = new PScrollPane(grouplist3);
		jsGroupPane4 = new PScrollPane(grouplist4);
		jsGroupPane5 = new PScrollPane(grouplist5);
		jsGroupPane6 = new PScrollPane(grouplist6);	
		
		jsGroupPane6.setOpaque(false);
		jsGroupPane6.setColumnHeader(null);

		showDefaultPanel();
	}
	
	protected void paintBorder(Graphics g) {
		int w = this.getWidth();
		int h = this.getHeight();
		
		g.setColor(new Color(0, 128, 255));
		g.drawRoundRect(0, 10, w - 2, h - 11, 5, 5);
	}
	
	public void doLayout() {
		super.doLayout();
		w = this.getWidth();
		h = this.getHeight();
	}
	
	private void showDefaultPanel() {
		this.setLayout(layout);	
		
		jsGroupPane1.setVisible(true);
		jsGroupPane2.setVisible(true);
		jsGroupPane3.setVisible(true);
		jsGroupPane4.setVisible(true);
		jsGroupPane5.setVisible(true);
		jsGroupPane6.setVisible(true);
		
		this.add(jsGroupPane1, "0,0");
		this.add(jsGroupPane2, "1,0");
		this.add(jsGroupPane3, "2,0");
		this.add(jsGroupPane4, "3,0");
		this.add(jsGroupPane5, "4,0");
		this.add(jsGroupPane6, "5,0");
		
		validate();
		repaint();
	}
}
