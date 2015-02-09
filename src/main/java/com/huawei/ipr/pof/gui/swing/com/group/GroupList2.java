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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.openflow.protocol.OFGroupMod;

import com.huawei.ipr.pof.gui.comm.GUITools.EDIT_STATUS;
import com.huawei.ipr.pof.gui.swing.HomeMain;
import com.huawei.ipr.pof.gui.swing.SwingUIPanel;
import com.huawei.ipr.pof.gui.swing.com.switcher.PList;
import com.huawei.ipr.pof.gui.swing.com.switcher.SwitchPanel;
import com.huawei.ipr.pof.gui.swing.comutil.GroupEntry;
import com.huawei.ipr.pof.gui.swing.comutil.ImageUtils;
import com.huawei.ipr.pof.manager.IPMService;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
@SuppressWarnings("serial")
public class GroupList2 extends PList{
	public static final String GROUPENTRY_FIRSTLING_STRING = "Group Entries";
	public static final String GROUPENTRY_STRING = "Group Entry";
	protected final Map<EDIT_STATUS, String> GROUPENTRY_STRING_MAP; 	//ADDING, MODIFYING, READONLY
	
	protected List<GroupEntry> groupEntryList;
	protected GroupList1 grouplist1;
	protected GroupList3 grouplist3;
	
	protected GroupEntry currentGroupEntry;
	
	public GroupList2() {
		super();
		
		GROUPENTRY_STRING_MAP = new HashMap<EDIT_STATUS, String>();
		GROUPENTRY_STRING_MAP.put(EDIT_STATUS.ES_ADDING, GROUPENTRY_STRING + "(N)");
		GROUPENTRY_STRING_MAP.put(EDIT_STATUS.ES_MODIFYING, GROUPENTRY_STRING + "(M)");
		GROUPENTRY_STRING_MAP.put(EDIT_STATUS.ES_READONLY, GROUPENTRY_STRING);
		
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int button = e.getButton();
				if(button == MouseEvent.BUTTON1){
					selectedValueChanged();
				}else if (button == MouseEvent.BUTTON3) {
					addModifyAndDelete(e);
				}
			}
		});

		setCellRenderer(new GroupList2Renderer());
	}
	
	private void addModifyAndDelete(MouseEvent e){
		//TODO //FIXME 	
		//why popMenu does not display correctly? It seems to display in lower panel level.
		//the same issue in GroupList4
		//probably it is the same issue that cause the combo item can not be displayed or selected in GroupList
		//for now, use mousePressed() instead of mouseClicked(), so that user could select the popMenu
		// by right-click the mouse (and DO NOT release!!!) to choose the item.
		
		final int selectedindex = this.getSelectedIndex();
		if (selectedindex > 0) {
			currentGroupEntry = groupEntryList.get(selectedindex - 1);
			
			JPopupMenu popMenu = new JPopupMenu();
			
			//modify
			if(currentGroupEntry.editStatus == EDIT_STATUS.ES_READONLY){
				JMenuItem editItem = new JMenuItem("Modify");
				popMenu.add(editItem);

				editItem.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						grouplist3.clearData();
						
						currentGroupEntry.editStatus = EDIT_STATUS.ES_MODIFYING;
						listModel.setElementAt(GROUPENTRY_STRING_MAP.get(EDIT_STATUS.ES_MODIFYING), selectedindex);
					}
				});
			}

			//delete
			JMenuItem delItem = new JMenuItem("Delete");
			popMenu.add(delItem);						

			delItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					grouplist3.clearData();
					
					if(currentGroupEntry.editStatus != EDIT_STATUS.ES_ADDING){	//need delete from DB
						SwingUIPanel.pofManager.iFreeGroupEntry(SwitchPanel.switchID, currentGroupEntry.ofGroupMod.getGroupId());
					}
					
					groupEntryList.remove(selectedindex - 1);
					
					currentGroupEntry = null;
					grouplist1.reloadGroupList1();
				}
			});
			
			popMenu.show(this, e.getX(), e.getY());
		}
	}
		
	protected void selectedValueChanged() {
		grouplist3.clearData();
		
		int index = this.getSelectedIndex();
		if(-1 == index){			
			return ;
		}
		
		String value = String.valueOf(listModel.getElementAt(index));
		
		if (null == value || 0 == value.length()) {
			return;
		}
		
		if(index == 0){
			currentGroupEntry = GroupEntry.getNewInstance(null);
			groupEntryList.add(currentGroupEntry);
			listModel.addElement(GROUPENTRY_STRING_MAP.get(EDIT_STATUS.ES_ADDING));
			clearSelection();
		}else{
			currentGroupEntry = groupEntryList.get(index - 1);
			grouplist3.showGroupEntry(currentGroupEntry);
		}
	}

	public void setOtherList(GroupList1 grouplist1, GroupList3 grouplist3) {
		this.grouplist1 = grouplist1;
		this.grouplist3 = grouplist3;
	}
	
	public void clearNextList(){
		grouplist3.clearData();
	}

	public void showGroupEntry() {
		listModel.addElement(GROUPENTRY_FIRSTLING_STRING);
		
		if(SwitchPanel.switchID != IPMService.SWITCHID_INVALID){
			groupEntryList = grouplist1.groupPane.switch_GroupEntryListMap.get(SwitchPanel.switchID);
			if(groupEntryList == null){
				groupEntryList = createGroupEntryList( SwingUIPanel.pofManager.iGetAllGroups(SwitchPanel.switchID));
				
				grouplist1.groupPane.switch_GroupEntryListMap.put(SwitchPanel.switchID, groupEntryList);
			}
			for(GroupEntry groupEntry : groupEntryList){
				listModel.addElement(GROUPENTRY_STRING_MAP.get(groupEntry.editStatus));
			}
		}
	}
	
	private List<GroupEntry> createGroupEntryList(List<OFGroupMod> allGroupEntry) {
		List<GroupEntry> newGroupEntryList = Collections.synchronizedList(new ArrayList<GroupEntry>());
		if(null ==  allGroupEntry){
			return newGroupEntryList;
		}
		
		for(OFGroupMod ofGroupMod : allGroupEntry){
			newGroupEntryList.add( GroupEntry.getNewInstance(ofGroupMod) );
		}
		
		return newGroupEntryList;
	}

	class GroupList2Renderer extends DefaultListCellRenderer {
		public Image icon = ImageUtils.getImageIcon("image/gui/swing/list_plus_icon.png").getImage();
		public Image select_img = ImageUtils.getImageIconByCaches("image/gui/swing/list_select.png").getImage();
		protected int row = -1;
		protected Object value;
		protected boolean isSelected = false;
		
		public GroupList2Renderer() {
			super();
			this.setOpaque(false);
		}

		public Dimension getPreferredSize() {
			Dimension d = super.getPreferredSize();
			return new Dimension((int) d.getWidth(), 24);
		}

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			this.value = value;
			this.row = index;
			this.isSelected = isSelected;
			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			return c;
		}

		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			
			if (!isSelected) {
				g2d.drawImage(def_img, 0, 0, this.getWidth(), this.getHeight(), null);
			} else {
				g2d.drawImage(select_img, 0, 0, this.getWidth(), this.getHeight(), null);
			}
			
			if (row == 0) {			//display "+"
				g2d.drawImage(icon, 3, 3, null);
			}

			g2d.setColor(HomeMain.LabelColor);
			g2d.drawString("    " + value, 10, 2 + 15);
		}
	}
}
