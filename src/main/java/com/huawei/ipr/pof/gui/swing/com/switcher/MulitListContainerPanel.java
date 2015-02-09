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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.openflow.protocol.OFGlobal;
import org.openflow.protocol.OFMatch20;

import com.huawei.ipr.pof.gui.comm.GUITools;
import com.huawei.ipr.pof.gui.swing.HomeMain;
import com.huawei.ipr.pof.gui.swing.com.protocal.ProtocolPanel;
import com.huawei.ipr.pof.gui.swing.comutil.ImageButton;
import com.huawei.ipr.pof.gui.swing.comutil.ImageUtils;
import com.huawei.ipr.pof.gui.swing.comutil.NOPanel;
import com.huawei.ipr.pof.gui.swing.comutil.PScrollPane;
import com.huawei.ipr.pof.gui.swing.comutil.TableLayoutEx;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
@SuppressWarnings("serial")
public class MulitListContainerPanel extends NOPanel {

	public final Image arr_img = ImageUtils.getImageIconByCaches("image/gui/swing/mulitliste_arr.png").getImage();
	public PList2 list2 ;
	public PList1 list1;
	public PList3 list3;
	public PList4 list4;
	public PList5 list5;
	public PList6 list6;
	public PList7 list7;
	
	private PScrollPane jsPane1;
	private PScrollPane jsPane2;
	private PScrollPane jsPane3;
	private PScrollPane jsPane4;
	private PScrollPane jsPane5;
	private PScrollPane jsPane6;
	private PScrollPane jsPane7;
	
	private TableLayoutEx layout;
	
	private SelectFieldAbovePane selectFieldAbovePane;
	private BorderLayout selectFieldPaneLayout;
	
	protected SwitchPanel switchPanel;
	protected ProtocolPanel packetPanel;
	protected DiamondTablesPanel diamondTablesPanel;
	
	public boolean isSelectFieldAbove(){
		return selectFieldAbovePane.isVisible();
	}
	
	public MulitListContainerPanel(SwitchPanel switchPanel) {
		this.switchPanel = switchPanel;
		initDefault();
	}

	private void initDefault() {
		layout = new TableLayoutEx(new double[] { TableLayoutEx.FILL, TableLayoutEx.FILL, 
													TableLayoutEx.FILL, TableLayoutEx.FILL, 
													TableLayoutEx.FILL, TableLayoutEx.FILL, 
													TableLayoutEx.FILL  }, 
									new double[] { TableLayoutEx.FILL }, 
									0, 0);
		setLayout(layout);
		
		this.setBorder(BorderFactory.createCompoundBorder(new LineBorder(  
				new Color(0, 128, 255)), BorderFactory.createEmptyBorder(11, 1, 1, 1)));  
		
		list2 = new PList2();
		list1 = new PList1(switchPanel, this);
		list3 = new PList3();
		list4 = new PList4();
		list5 = new PList5();
		list6 = new PList6();
		list7 = new PList7();
		
		list1.setOtherList(list2);
		list2.setOtherList(list1,list3);
		list3.setOtherList(list2,list4);
		list4.setOtherList(list3,list5);
		list5.setOtherList(list4,list6);
		list6.setOtherList(list5,list7);
		list7.setOtherList(list6);
		
		jsPane1 = new PScrollPane(list1);
		jsPane2 = new PScrollPane(list2);
		jsPane3 = new PScrollPane(list3);
		jsPane4 = new PScrollPane(list4);
		jsPane5 = new PScrollPane(list5);
		jsPane6 = new PScrollPane(list6);	
		jsPane7 = new PScrollPane(list7);		
		
		jsPane2.setOpaque(false);
		jsPane2.setColumnHeader(null);		

		jsPane4.setOpaque(false);
		jsPane4.setColumnHeader(null);		

		jsPane6.setOpaque(false);
		jsPane6.setColumnHeader(null);

		jsPane7.setOpaque(false);
		jsPane7.setColumnHeader(null);
		
		selectFieldAbovePane = new SelectFieldAbovePane();
		selectFieldPaneLayout = new BorderLayout();
		
		showDefaultPanel();
	}
	
	protected void paintBorder(Graphics g) {
//		super.paintBorder(g);
		int w = this.getWidth();
		int h = this.getHeight();
		
		g.setColor(new Color(0, 128, 255));
		g.drawRoundRect(0, 10, w - 2, h - 11, 5, 5);
		
		if(diamondTablesPanel != null){
			int x = diamondTablesPanel.getSelectedX();
	 
			g.drawImage(arr_img, x + 58 , 0, null);
		}
	}
	
	int w,h;
	public void doLayout() {
		super.doLayout();
		w = this.getWidth();
		h = this.getHeight();
	}
	
//select field	
	class SelectFieldAbovePane extends JPanel{
		JLabel label = new JLabel("Now select field above");
		ImageButton sub = new ImageButton("image/gui/swing/submit_button.png");
		ImageButton cancel = new ImageButton("image/gui/swing/cancel_button.png");
		public SelectFieldAbovePane() {
			setOpaque(false);
			this.setLayout(null);
			label.setFont(new Font(HomeMain.systemFontName, Font.PLAIN, 18));
			label.setForeground(HomeMain.systemLabelColor);
//			tempTableFieldList.clear();
			sub.setCursor(new Cursor(Cursor.HAND_CURSOR));
			cancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			
			this.add(label);
			this.add(sub);
			this.add(cancel);
			
			sub.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					List<OFMatch20> matchFileList = packetPanel.selectedMatchFieldList;
					
					if(matchFileList.size() > OFGlobal.OFP_MAX_MATCH_FIELD_NUM){
						GUITools.messageDialog(SelectFieldAbovePane.this, "Max match field num is " + OFGlobal.OFP_MAX_MATCH_FIELD_NUM + " (<+ " + matchFileList.size() + ").");
						return;
					}
					showDefaultPanel();
//					selectFieldAbovePane.setVisible(false);
					
					packetPanel.protocolFieldDisplayTable.changeColorFlag = false;
					list1.choosedNewMatchField = true;
					
					list1.newTempOFTable.setMatchFieldList(matchFileList);
					list1.newTempOFTable.setMatchFieldNum((byte)matchFileList.size());
					short keyLength = 0;
					for(OFMatch20 matchField : matchFileList){
						keyLength += matchField.getLength();
					}
					list1.newTempOFTable.setKeyLength(keyLength);
					
					DefaultListModel list1Model = (DefaultListModel) list1.getModel();
					
					list1Model.set(5, "Key Length: " + list1.newTempOFTable.getKeyLength());
				}
			});
			
			cancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					showDefaultPanel();
//					selectFieldAbovePane.setVisible(false);
					packetPanel.protocolFieldDisplayTable.changeColorFlag = false;					
				}
			});
		}
		
		public void doLayout() {
			super.doLayout();
			label.setBounds(w / 2 - 90, h / 2 - 70, 200, 24);
			sub.setBounds(w / 2 - 110, h / 2 - 20 , 102, 31);
			cancel.setBounds(w / 2 + 7, h / 2 - 20 , 102, 31);
		}
		
		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			super.paintComponent(g2d);
		}
	}
	
	private void showDefaultPanel() {
		selectFieldAbovePane.setVisible(false);		
		this.remove(selectFieldAbovePane);
		
		this.setLayout(layout);	
		
		jsPane1.setVisible(true);
		jsPane2.setVisible(true);
		jsPane3.setVisible(true);
		jsPane4.setVisible(true);
		jsPane5.setVisible(true);
		jsPane6.setVisible(true);
		jsPane7.setVisible(true);
		
		this.add(jsPane1, "0,0");
		this.add(jsPane2, "1,0");
		this.add(jsPane3, "2,0");
		this.add(jsPane4, "3,0");
		this.add(jsPane5, "4,0");
		this.add(jsPane6, "5,0");
		this.add(jsPane7, "6,0");
		 
		validate();
		repaint();
	}

	public void showSelectFieldAbovePane() {
		jsPane1.setVisible(false);
		jsPane2.setVisible(false);
		jsPane3.setVisible(false);
		jsPane4.setVisible(false);
		jsPane5.setVisible(false);
		jsPane6.setVisible(false);
		jsPane7.setVisible(false);
		
		this.remove(jsPane1);
		this.remove(jsPane2);
		this.remove(jsPane3);
		this.remove(jsPane4);
		this.remove(jsPane5);
		this.remove(jsPane6);
		this.remove(jsPane7);
		
		this.setLayout(selectFieldPaneLayout);
		
		selectFieldAbovePane.setVisible(true);
		this.add(selectFieldAbovePane);

		validate();
		repaint();
		
		packetPanel.protocolFieldDisplayTable.changeColorFlag = true;
	}

	public void setProtocolPanel(ProtocolPanel packetPanel) {
		this.packetPanel = packetPanel;
	}

	public void setDiamondTablesPanel(DiamondTablesPanel diamondTablesPanel) {
		this.diamondTablesPanel = diamondTablesPanel;
	}
}
