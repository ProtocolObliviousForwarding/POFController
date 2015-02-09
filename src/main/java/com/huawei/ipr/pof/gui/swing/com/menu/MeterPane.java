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

package com.huawei.ipr.pof.gui.swing.com.menu;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openflow.protocol.OFMeterMod;

import com.huawei.ipr.pof.gui.swing.HomeMain;
import com.huawei.ipr.pof.gui.swing.SwingUIPanel;
import com.huawei.ipr.pof.gui.swing.com.switcher.SwitchPanel;
import com.huawei.ipr.pof.gui.swing.comutil.ImageButton;
import com.huawei.ipr.pof.gui.swing.comutil.ImageUtils;
import com.huawei.ipr.pof.gui.swing.comutil.RoundedFieldUI;
import com.huawei.ipr.pof.manager.IPMService;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
@SuppressWarnings("serial")
public class MeterPane extends JPanel {
	
	public final Image imgBG = ImageUtils.getImageIcon("image/gui/swing/menu_glass_bg.png").getImage();
	public final Image imgBGArr = ImageUtils.getImageIcon("image/gui/swing/menu_arr.png").getImage();
	
	public final ImageButton plusdef = new ImageButton("image/gui/swing/menu_glass_plus_button.png");	
	public final ImageButton save_button = new ImageButton("image/gui/swing/save.png");
	public final ImageButton Cancel_bbb = new ImageButton("image/gui/swing/Cancel_bbb.png");
	
	protected int meterID;
	
	protected JLabel label1 = new JLabel("SwitchID");
	protected JLabel label2 = new JLabel("MeterID");
	protected JLabel label3 = new JLabel("MeterRate(kbps)");
	
	protected JLabel label11 = new JLabel("");
	protected JLabel label22 = new JLabel("");
		
	protected JTextField field = new JTextField(15);
	protected List<ComponentsCol> extComponents = new ArrayList<ComponentsCol>();
	
	protected int realHeight = 300;
	
	protected MenuPanel menuPanel;

	public MeterPane(MenuPanel menuPanel) {
		this.menuPanel = menuPanel;
		
		setOpaque(false);
		setLayout(null);
		field.setUI(new RoundedFieldUI());
		
		init();
		
		label11.setForeground(HomeMain.LabelColor);
		label22.setForeground(HomeMain.LabelColor);
		this.add(label11);
		this.add(label22);
		this.add(field);
		
		this.add(plusdef);
		
	
		save_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(SwitchPanel.switchID == IPMService.SWITCHID_INVALID){
					return;
				}
				String fieldString = field.getText();
				if(fieldString == null || !fieldString.matches("[1-9]\\d*")){
					field.setText("");
					return;
				}
				meterID = SwingUIPanel.pofManager.iAddMeterEntry(SwitchPanel.switchID, 
																Short.parseShort(field.getText()));
				doNewMenuMeterPaneAction();
				field.setText("0");
				label11.setText(Integer.toHexString(SwitchPanel.switchID));
				doMenuMeterPaneAction();
			}
		});
		
		plusdef.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(SwitchPanel.switchID == IPMService.SWITCHID_INVALID){
					return;
				}
				String fieldString = field.getText();
				if(fieldString == null || !fieldString.matches("[1-9]\\d*")){
					field.setText("");
					return;
				}
				meterID = SwingUIPanel.pofManager.iAddMeterEntry(SwitchPanel.switchID, 
																	Short.parseShort(field.getText()));
				doNewMenuMeterPaneAction();
				SwingUIPanel.pofManager.iGetMeter(SwitchPanel.switchID, meterID);
				field.setText("0");
				label11.setText(Integer.toHexString(SwitchPanel.switchID));
			}
		});
		
		Cancel_bbb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doMenuMeterPaneAction();
			}
		});
	}
	
	protected void init(){
		this.add(label1);
		this.add(label2);
		this.add(label3);
		
		this.add(save_button);
		this.add(Cancel_bbb);
		plusdef.setCursor(new Cursor(Cursor.HAND_CURSOR));
		Cancel_bbb.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}
	
	protected void doMenuMeterPaneAction() {
		setVisible(false);
	}
	
	private void doNewMenuMeterPaneAction() {
		addNewRow();
	}
	

	public void addNewRow() {
		addNewRow(SwitchPanel.switchID, meterID, (short)Integer.parseInt(field.getText()) );
	}
	
	protected void addNewRow(int switchID, int meterID, short rate) {
		final ComponentsCol col = new ComponentsCol(Integer.toHexString(switchID),
													String.valueOf(meterID),
													String.valueOf(rate));
		this.add(col.label1);
		this.add(col.label2);
		this.add(col.label3);
//		this.add(col.plus);
		this.add(col.delete);
		
		col.delete.setCursor(new Cursor(Cursor.HAND_CURSOR));
		
		col.delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int switchid = (int)Long.parseLong(col.label1.getText(), 16);
				int meterid = (int)Long.parseLong(col.label2.getText(), 16);
				
				extComponents.remove(col);
				
				MeterPane.this.remove(col.label1);
				MeterPane.this.remove(col.label2);
				MeterPane.this.remove(col.label3);
				MeterPane.this.remove(col.delete);
				
				MeterPane.this.invalidate();
				MeterPane.this.validate();
		
				MeterPane.this.repaint();
				
				MeterPane.this.doLayout();
				
				SwingUIPanel.pofManager.iFreeMeter(switchid, meterid);
			}
		});
		extComponents.add(col);
		
		this.doLayout();
	}
	
	public void reloadAllMeters(){
		this.removeAll();
		
		init();
		
		List<Integer> switchIDList = SwingUIPanel.pofManager.iGetAllSwitchID();
		if(switchIDList != null){
			for(int switchID: switchIDList){
				List<OFMeterMod> meterList = SwingUIPanel.pofManager.iGetAllMeters(switchID);
				if(meterList != null){
					for(OFMeterMod meter : meterList){
						addNewRow(switchID, meter.getMeterId(), meter.getRate());
					}
				}
			}
		}
		
		this.add(label11);
		this.add(label22);
		this.add(field);
		
		this.add(plusdef);
	}


	public void doLayout() {
		super.doLayout();
		int width = menuPanel.getW();
		int height = realHeight;
		
		int startX = width / 6;
		int startY = 50;
		label1.setBounds(startX, startY + 10, 100, 24);
		
		startX = width / 2 - 50;
		label2.setBounds(startX, startY + 10, 100, 24);
		
		startX = width - width / 6 - 60;
		label3.setBounds(startX, startY + 10, 100, 24);
		
		save_button.setBounds(width - width / 6, height, 56, 25);
		Cancel_bbb.setBounds(width - width / 6 + 57, height, 56, 25);
		
		int startY1 = 50;
		int extSize = extComponents.size();
		if(extSize > 0 ){
			int index = 0;
			for(ComponentsCol col : extComponents){
				
				col.label1.setBounds(width / 6 + 5, 24 * index + startY1 + 34,100, 24);
				col.label2.setBounds(width / 2 - 50 + 5, 24 * index + startY1 + 34,100, 24);
				col.label3.setBounds(width - width / 6 - 60, 24 * index + startY1 + 34,100, 24);
				
//				col.plus.setBounds(width - width / 6 + 40, 24 * index + startY1 + 34,22, 22);
				col.delete.setBounds(width - width / 6 + 65, 24 * index + startY1 + 34,22, 22);
				index ++;
			}
		}
		
		startX = width / 6;
		startY = extSize * 24 +50;
		label11.setBounds(startX + 5, startY + 34, 100, 24);
		
		startX = width / 2 - 50;
		label22.setBounds(startX + 5, startY + 34, 100, 24);
		
		startX = width - width / 6 - 60;
		field.setBounds(startX, startY + 34, 93, 24);
		
		plusdef.setBounds(startX + 125, startY + 34, 22, 22);
	}

	@Override
	protected void paintComponent(Graphics g) {
		int w = menuPanel.getW();
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		
		g2d.drawImage(imgBG, 10, 49, w , realHeight,null);
		g2d.drawImage(imgBGArr, 120, 40, 12 , 9,null);
//		g2d.drawImage(imgBGArr, 100, 40, 12 , 9,null);
		
		g2d.setColor(HomeMain.LabelColor);
		int xx = w / 3;
		g2d.drawLine(xx, 80, xx, 80 + 200);
		
		xx = w / 3 * 2;
		g2d.drawLine(xx, 80, xx, 80 + 200);
		
	}
	
	class ComponentsCol {
		public JLabel label1 = new JLabel();
		public JLabel label2 = new JLabel();
		public JLabel label3 = new JLabel();
		
//		public ImageButton plus = new ImageButton("image/gui/swing/menu_glass_plus_button.png");
		public ImageButton delete = new ImageButton("image/gui/swing/menu_glass_delete_button.png");
		
		public ComponentsCol(String string, String string2, String text) {
			label1.setText(string);
			label2.setText(string2);
			label3.setText(text);
		}
	}
}
