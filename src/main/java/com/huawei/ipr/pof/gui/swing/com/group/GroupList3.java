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

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openflow.protocol.OFGroupMod;
import org.openflow.protocol.OFGroupMod.OFGroupType;

import com.huawei.ipr.pof.gui.swing.SwingUIPanel;
import com.huawei.ipr.pof.gui.swing.com.switcher.PList;
import com.huawei.ipr.pof.gui.swing.com.switcher.SwitchPanel;
import com.huawei.ipr.pof.gui.swing.comutil.GroupEntry;
import com.huawei.ipr.pof.manager.IPMService;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
@SuppressWarnings("serial")
public class GroupList3 extends PList{
	
	public static final String[] groupString = new String[] {"SwitchID", "Group Type", "Group ID", "Counter", "Actions"};
	protected GroupList2 grouplist2;
	protected GroupList4 grouplist4;
	protected GroupEntry currentGroupEntry;
	
	public GroupList3() {
		super();
		
		addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()){
					selectedValueChanged();
				}
			}
		});
	}
	
	public void setOtherList(GroupList2 grouplist2, GroupList4 grouplist4) {
		this.grouplist2 = grouplist2;
		this.grouplist4 = grouplist4;
	}
	
	public void clearNextList(){
		grouplist4.clearData();
	}

	protected void selectedValueChanged() {		
		grouplist4.clearData();
		
		int index = this.getSelectedIndex();
		if(-1 == index){			
			return ;
		}
		
		switch(index){
			case 1:
				grouplist4.showGroupType(currentGroupEntry);
				break;
			case 3:
				showCounter();
				break;
			case 4:
				grouplist4.showActions(currentGroupEntry);
				break;
			default:
				break;
		}
		
		validate();
		repaint();
	}

	private void showCounter() {
		int counterId = currentGroupEntry.ofGroupMod.getCounterId();
		if(counterId == IPMService.COUNTERID_INVALID){
			return;
		}
		long counterValue = SwingUIPanel.pofManager.iQueryCounterValue(SwitchPanel.switchID, counterId);
		if(counterValue == -1){
			counterValue = 0;
		}
		listModel.setElementAt(groupString[3] + ": " + counterValue, 3);
	}
	
	public void showGroupEntry(final GroupEntry currentGroupEntry) {
		this.currentGroupEntry = currentGroupEntry;

		clearData();
		
		OFGroupMod ofGroupMod = currentGroupEntry.ofGroupMod;

		listModel.addElement(groupString[0] + ": " + Integer.toHexString(SwitchPanel.switchID));
		listModel.addElement(groupString[1] + ": " + OFGroupType.values()[ofGroupMod.getGroupType()].name());
		if(ofGroupMod.getGroupId() == IPMService.GROUPID_INVALID){
			listModel.addElement(groupString[2]);
		}else{
			listModel.addElement(groupString[2] + ": " + ofGroupMod.getGroupId());
		}
		listModel.addElement(groupString[3]);
		
		int actionNum = ofGroupMod.getActionNum();
		listModel.addElement(groupString[4] + ( (actionNum == 0) ? "" : "(" + actionNum + ")" ));
		
		validate();
		repaint();
	}
}
