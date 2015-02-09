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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.openflow.protocol.OFGlobal;
import org.openflow.protocol.OFGroupMod;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.action.OFAction;

import com.huawei.ipr.pof.gui.comm.GUITools;
import com.huawei.ipr.pof.gui.comm.GUITools.EDIT_STATUS;
import com.huawei.ipr.pof.gui.swing.SwingUIPanel;
import com.huawei.ipr.pof.gui.swing.com.menu.MenuPanel;
import com.huawei.ipr.pof.gui.swing.com.switcher.SwitchPanel;
import com.huawei.ipr.pof.gui.swing.comutil.GroupEntry;
import com.huawei.ipr.pof.gui.swing.comutil.ImageButton;
import com.huawei.ipr.pof.gui.swing.comutil.ImageUtils;
import com.huawei.ipr.pof.gui.swing.comutil.PScrollPane;
import com.huawei.ipr.pof.manager.IPMService;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
@SuppressWarnings("serial")
public class GroupPane extends JPanel{
	protected GroupSetPanel groupSetPaneltable;
	public Image imgBG = ImageUtils.getImageIcon("image/gui/swing/menu_glass_bg.png").getImage();
	public Image imgBGArr = ImageUtils.getImageIcon("image/gui/swing/menu_arr.png").getImage();
	
	public ImageButton Save_button = new ImageButton("image/gui/swing/save.png");
	public ImageButton Cancel_button = new ImageButton("image/gui/swing/Cancel_bbb.png");
	
	protected MenuPanel menuPanel;
	protected PScrollPane jsPane;
	
	protected Map<Integer, List<GroupEntry>> switch_GroupEntryListMap;	//<switchID, List<GroupEntry>>
	
	protected int table_height;
	
	public GroupPane(final MenuPanel menuPanel) {
		this.menuPanel = menuPanel;
		setOpaque(false);
		
		this.switch_GroupEntryListMap = new HashMap<Integer, List<GroupEntry>>();	//<switchID, List<GroupEntry>>
		
		groupSetPaneltable = new GroupSetPanel(this);
		groupSetPaneltable.setPreferredSize(new Dimension(450, 300));
		
		jsPane = new PScrollPane(groupSetPaneltable);
		jsPane.setOpaque(false);
		jsPane.setColumnHeader(null);
		
		this.setLayout(null);
		
		this.add(jsPane);
		this.add(Save_button);
		this.add(Cancel_button);
		
		Save_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSaveAction();
				setVisible(false);
			}
		});
		
		Cancel_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
	}
	
	protected void doSaveAction() {
		Boolean successful = null;
		List<GroupEntry> groupEntryList = switch_GroupEntryListMap.get(SwitchPanel.switchID);
		int groupEntryListSize = groupEntryList.size();
		
		for(int i = 0; i < groupEntryListSize; i++){
			GroupEntry groupEntry = groupEntryList.get(i);
			OFGroupMod ofGroupMod = groupEntry.ofGroupMod;
			
			List<OFAction> actioinList = ofGroupMod.getActionList();
			byte actionNum = ofGroupMod.getActionNum();
			
			if(actionNum > OFGlobal.OFP_MAX_ACTION_NUMBER_PER_GROUP){
            	GUITools.messageDialog(this, "action num " + actionNum + " > OFGlobal.OFP_MAX_ACTION_NUMBER_PER_GROUP(" + OFGlobal.OFP_MAX_ACTION_NUMBER_PER_GROUP + ").");
            	successful = false;
            	continue;
			}
			
			if(groupEntry.editStatus == EDIT_STATUS.ES_ADDING){ //adding
				if( actionNum == 0
						|| actioinList == null
						|| actionNum != actioinList.size() ){
	            	GUITools.messageDialog(this, "group entry's action number dismatch, please check.");
	            	continue;
				}
				int newGroupEntryId = SwingUIPanel.pofManager.iAddGroupEntry(SwitchPanel.switchID, 
																				ofGroupMod.getGroupType(), 
																				actionNum, 
																				actioinList);
				if(newGroupEntryId != IPMService.GROUPID_INVALID){
					groupEntry.editStatus = EDIT_STATUS.ES_READONLY;
					groupEntryList.get(i).ofGroupMod = SwingUIPanel.pofManager.iGetGroupEntry(SwitchPanel.switchID, newGroupEntryId);
				}
				
	            if(newGroupEntryId != IPMService.FLOWENTRYID_INVALID && successful == null ){
	            	successful = true;
	            }else if(newGroupEntryId == IPMService.FLOWENTRYID_INVALID && (successful == null || successful == true) ){
	            	successful = false;
	            }
	            menuPanel.getMainPanel().getSwitchPanel().checkUsedProtocolInActionList(actioinList);
			}else if(groupEntry.editStatus == EDIT_STATUS.ES_READONLY){
				continue;
			}else if(groupEntry.editStatus == EDIT_STATUS.ES_MODIFYING){
				if(ofGroupMod.getGroupId() == IPMService.GROUPID_INVALID){
	            	GUITools.messageDialog(this, "invalid group ID!.");
	            	continue;
				}
				if( (actionNum == 0 && actioinList != null && actioinList.size() != 0)
						|| (actionNum != 0 && (actioinList == null || actionNum != actioinList.size())) ){
	            	GUITools.messageDialog(this, "group entry's action number dismatch, please check.");
	            	continue;
				}
				boolean succ = SwingUIPanel.pofManager.iModifyGroupEntry(SwitchPanel.switchID, 
																			ofGroupMod.getGroupId(), 
																			ofGroupMod.getGroupType(), 
																			actionNum, 
																			actioinList);
				
				if (succ) {
					groupEntry.editStatus = EDIT_STATUS.ES_READONLY;
					groupEntryList.get(i).ofGroupMod = SwingUIPanel.pofManager.iGetGroupEntry(SwitchPanel.switchID, ofGroupMod.getGroupId());
				}
				 
				if (succ == true && successful == null) {
					successful = true;
				} else if (succ == false && (successful == null || successful == true)) {
					successful = false;
				}

				menuPanel.getMainPanel().getSwitchPanel().checkUsedProtocolInActionList(actioinList);
			}
		}
		
		groupSetPaneltable.grouplist1.reloadGroupList1();
		groupSetPaneltable.grouplist2.currentGroupEntry = null;
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.drawImage(imgBG, 10, 49, menuPanel.getW() , table_height + 60,null);
		g2d.drawImage(imgBGArr, 70, 40, 12 , 9,null);
	}
	
	public void doLayout() {
		super.doLayout();
		table_height = 300;
		jsPane.setBounds(40, 60, menuPanel.getW() - 60, table_height);
		Save_button.setBounds(menuPanel.getW() - menuPanel.getW() / 6, table_height +70, 56, 25);
		Cancel_button.setBounds(menuPanel.getW() - menuPanel.getW() / 6 + 57, table_height +70, 56, 25);
	}

	public void rollBack(int switchId, OFMessage sendedMsg) {
		if(null != groupSetPaneltable){
			groupSetPaneltable.grouplist1.reloadGroupList1();
		}		
	}
	
	public void reloadGroup(){
		switch_GroupEntryListMap.clear();
		if(null != groupSetPaneltable){
			groupSetPaneltable.grouplist1.reloadGroupList1();
		}
	}
	
	public void removeSwitch(final int switchID){
		switch_GroupEntryListMap.remove(switchID);
		if(null != groupSetPaneltable){
			groupSetPaneltable.grouplist1.reloadGroupList1();
		}
	}
}
