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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.openflow.protocol.OFMessage;

import com.huawei.ipr.pof.gui.swing.comutil.ImageButton;
import com.huawei.ipr.pof.gui.swing.comutil.ImageUtils;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
@SuppressWarnings("serial")
public class DeviceInfoButton extends ImageButton {

	public final Icon def = ImageUtils.getImageIcon("image/gui/swing/deviceinfo.png");

	public final Icon def1 = ImageUtils.getImageIcon("image/gui/swing/deviceinfo_mousepressed.png");
	public final Icon def2 = ImageUtils.getImageIcon("image/gui/swing/deviceinfo_mousetntered.png");
	public final Icon def3 = ImageUtils.getImageIcon("image/gui/swing/deviceinfo_tips.png");

	protected final SwitchPanel switchPanel;

	protected final DeviceInfoTablePanel deviceInfoTablePanel;
	
	public final DeviceInfoTablePanel getDeviceInfoTablePanel(){
		return deviceInfoTablePanel;
	}

	public DeviceInfoButton(final SwitchPanel switchPanel) {
		super("image/gui/swing/deviceinfo.png");

		this.switchPanel = switchPanel;
		
		deviceInfoTablePanel = new DeviceInfoTablePanel(switchPanel);
		
		deviceInfoTablePanel.setVisible(false);
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				setIcon(def1);
			}

			public void mouseReleased(MouseEvent e) {
				setIcon(def);
			}

			public void mouseEntered(MouseEvent e) {
				setIcon(def2);
			}

			public void mouseExited(MouseEvent e) {
				setIcon(def);
			}
		});

		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showTable();
			}
		});
	}
	
	public void newNotification(){
		setIcon(def3);
	}
	
	public void processDeviceInfo(final int switchId, final OFMessage ofmsg){
		if(null != deviceInfoTablePanel
				&& null != deviceInfoTablePanel.getDeviceInfoTable()){
			deviceInfoTablePanel.getDeviceInfoTable().processDeviceInfo(switchId, ofmsg);
			
			newNotification();
		}
	}
	

	public void removeSwitch(int switchId) {
		if(null != deviceInfoTablePanel
				&& null != deviceInfoTablePanel.getDeviceInfoTable()){
			deviceInfoTablePanel.getDeviceInfoTable().removeSwitch(switchId);
			
			newNotification();
		}		
	}


	protected void showTable() {
		if (deviceInfoTablePanel.isVisible()) {
			deviceInfoTablePanel.setVisible(false);
		} else {
			JFrame win = (JFrame) SwingUtilities.getWindowAncestor(this);

			Component glassPane = win.getGlassPane();
			if (glassPane != null) {
				win.setGlassPane(glassPane);
				glassPane.setVisible(false);
			}

			win.setGlassPane(deviceInfoTablePanel);
			deviceInfoTablePanel.setVisible(true);
		}
	}


}
