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

package com.huawei.ipr.pof.gui.swing.com;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.util.List;

import org.openflow.protocol.OFMatch20;

import com.huawei.ipr.pof.gui.comm.GUITools.EDIT_STATUS;
import com.huawei.ipr.pof.gui.swing.com.menu.MenuPanel;
import com.huawei.ipr.pof.gui.swing.com.protocal.ProtocolPanel;
import com.huawei.ipr.pof.gui.swing.com.switcher.SwitchPanel;
import com.huawei.ipr.pof.gui.swing.comutil.NOPanel;
import com.huawei.ipr.pof.gui.swing.comutil.PPanel;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
@SuppressWarnings("serial")
public class MainPanel extends PPanel {
	
	protected ProtocolPanel protocolPanel;
	protected SwitchPanel switchPanel;
	protected MenuPanel menuPanel;
	
	public final ProtocolPanel getProtocolPanel(){
		return protocolPanel;
	}
	
	public final SwitchPanel getSwitchPanel(){
		return switchPanel;
	}
	
	public final MenuPanel getMenuPanel(){
		return menuPanel;
	}

	public List<OFMatch20> getMetadataList() {
		return protocolPanel.metadataButton.newPacketFieldList;
	}

	public void setMetadataList(List<OFMatch20> metadataList) {
		if(null != metadataList && 0 != metadataList.size()){
			protocolPanel.metadataButton.editStatus = EDIT_STATUS.ES_READONLY;
		}else{
			protocolPanel.metadataButton.editStatus = EDIT_STATUS.ES_ADDING;
		}

		protocolPanel.metadataButton.reloadField(metadataList);
	}

	public MainPanel() {
		BorderLayout bl = new BorderLayout();
		this.setLayout(bl);

		init();
	}

	private void init() {
		menuPanel = new MenuPanel(this);
		this.add(menuPanel, BorderLayout.NORTH);


		BorderLayout bl = new BorderLayout();
		NOPanel contentPanel = new NOPanel(bl);

		protocolPanel = new ProtocolPanel(this);
		switchPanel = new SwitchPanel(this);
		
		contentPanel.add(protocolPanel, BorderLayout.NORTH);
		contentPanel.add(switchPanel, BorderLayout.CENTER);
		
		this.add(contentPanel, BorderLayout.CENTER);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
	}
}
