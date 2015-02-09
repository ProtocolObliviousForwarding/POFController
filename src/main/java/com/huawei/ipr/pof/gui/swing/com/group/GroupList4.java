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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.openflow.protocol.OFGroupMod.OFGroupType;
import org.openflow.protocol.action.OFAction;

import com.huawei.ipr.pof.gui.comm.GUITools.EDIT_STATUS;
import com.huawei.ipr.pof.gui.swing.HomeMain;
import com.huawei.ipr.pof.gui.swing.com.switcher.PList;
import com.huawei.ipr.pof.gui.swing.com.switcher.PList5;
import com.huawei.ipr.pof.gui.swing.comutil.GroupEntry;
import com.huawei.ipr.pof.gui.swing.comutil.ImageUtils;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */

@SuppressWarnings("serial")
public class GroupList4 extends PList{
	public static final String ACTIONS_STRING = PList5.ACTIONS_STRING;	//"Actions:";
	
	protected GroupList3 grouplist3;
	protected GroupList5 grouplist5;

	protected GroupEntry currentGroupEntry;
	
	public void setOtherList(GroupList3 grouplist3, GroupList5 grouplist5) {
		this.grouplist3 = grouplist3;
		this.grouplist5 = grouplist5;
	}
	
	public GroupList4() {
		super();

		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int button = e.getButton();
				if(button == MouseEvent.BUTTON1){
					selectedValueChanged();
				}else if (button == MouseEvent.BUTTON3) {
					addDelete(e);
				}
			}
		});
		
		setCellRenderer(new GroupList4Renderer());
	}
	
	public void clearNextList(){
		grouplist5.clearData();
	}
	
	private void addDelete(MouseEvent e){
		final int selectedIndex = this.getSelectedIndex();
		if (currentGroupEntry.editStatus != EDIT_STATUS.ES_READONLY
				&& selectedIndex > 0 
				&& listModel.get(0).equals(ACTIONS_STRING)) {	//flow entry
			
			JPopupMenu popMenu = new JPopupMenu();
			//delete
			JMenuItem delItem = new JMenuItem("Delete");
			popMenu.add(delItem);						
	
			delItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					List<OFAction> actionList = currentGroupEntry.ofGroupMod.getActionList();
					actionList.remove(selectedIndex - 1);
					
					int actionNum = actionList.size();
					currentGroupEntry.ofGroupMod.setActionNum((byte)actionNum);
					
					DefaultListModel list3Model = (DefaultListModel)grouplist3.getModel();
					list3Model.setElementAt(GroupList3.groupString[4] + ( (actionNum == 0) ? "" : "(" + actionNum + ")" ), 4);
					
					clearData();
				}
			});
		
			popMenu.show(this, e.getX(), e.getY());
		}
	}
	
	protected void selectedValueChanged() {
		grouplist5.clearData();
		
		int index = getSelectedIndex();
		
		if (index == -1) {
			return;
		}
		
		String value = String.valueOf(listModel.get(index));
		
		if(value == null || value.length() == 0){
			return;
		}
		
		int selectedIndexInGrouplist3 = grouplist3.getSelectedIndex();

		if(1 == selectedIndexInGrouplist3){		//groupType
			OFGroupType groupType = OFGroupType.valueOf(value);
			currentGroupEntry.ofGroupMod.setGroupType((byte)groupType.ordinal());
			
			DefaultListModel list3Model = (DefaultListModel) grouplist3.getModel();
			list3Model.setElementAt(GroupList3.groupString[1] + ": " + groupType.name(), selectedIndexInGrouplist3);
			
			clearData();
		}else if(4 == selectedIndexInGrouplist3){	//actions
			if (0 == index) {
				grouplist5.showAllDefaultActions(currentGroupEntry);
			}else{
				grouplist5.showAction(currentGroupEntry, index - 1);
			}	
		}
		
		validate();
		repaint();
	}
	
	public void showActions(GroupEntry currentGroupEntry) {
		this.currentGroupEntry = currentGroupEntry;
		
		clearData();

		listModel.addElement(ACTIONS_STRING);
		
		if(currentGroupEntry.ofGroupMod.getActionList() == null){
			currentGroupEntry.ofGroupMod.setActionList(new ArrayList<OFAction>());
		}
		
		List<OFAction> actionList = currentGroupEntry.ofGroupMod.getActionList();
		for(OFAction action : actionList){
			listModel.addElement(action.getType().name());
		}
		
		validate();
		repaint();
	}


	public void showGroupType(final GroupEntry currentGroupEntry) {
		this.currentGroupEntry = currentGroupEntry;
		
		clearData();

		for(OFGroupType groupType : OFGroupType.values()){
			listModel.addElement(groupType.name());
		}
		
		validate();
		repaint();
	}

	class GroupList4Renderer extends DefaultListCellRenderer {
		public Image icon = ImageUtils.getImageIcon("image/gui/swing/list_plus_icon.png").getImage();
		public Image select_img = ImageUtils.getImageIconByCaches("image/gui/swing/list_select.png").getImage();
		protected int row = -1;
		protected Object value;
		protected boolean isSelected = false;
		
		public GroupList4Renderer() {
			setOpaque(false);
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
			this.value = value;
			this.isSelected = isSelected;
			this.row = index;
			return super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			if (!isSelected) {
				g2d.drawImage(def_img, 0, 0, this.getWidth(), this.getHeight(), null);
			} else {
				g2d.drawImage(select_img, 0, 0, this.getWidth(), this.getHeight(), null);
			}
			int w = this.getWidth();
			int h = this.getHeight();
			g2d.setColor(HomeMain.packetLabelColor);
			g2d.drawRoundRect(2, 1, w - 5, h - 2, 3, 3);
			g2d.setColor(HomeMain.LabelColor);
			
			if (row == 0 
					&& grouplist3.getSelectedIndex() == 4 
					&& currentGroupEntry.editStatus != EDIT_STATUS.ES_READONLY) {			//display "+"
				g2d.drawImage(icon, 3, 3, null);
			}
			
			g2d.drawString("    " + value, 10, 2 + 15);
		}
	}
}
