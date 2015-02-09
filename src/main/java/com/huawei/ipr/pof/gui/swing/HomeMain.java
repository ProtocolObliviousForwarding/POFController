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

package com.huawei.ipr.pof.gui.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.FontUIResource;

import org.openflow.protocol.OFMessage;

import com.huawei.ipr.pof.gui.swing.com.MainPanel;
import com.huawei.ipr.pof.gui.swing.comutil.ImageUtils;
import com.huawei.ipr.pof.gui.swing.comutil.SwingUtils;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
public class HomeMain {

    public static Color LabelColor = new Color(190, 190, 190);
    public static Color systemLabelColor = new Color(224, 224, 224);
    public static Color packetLabelColor = new Color(70, 70, 70);
    
    public static Color blueColor =  new Color(0, 128, 255);
    
    public static String systemFontName = "";
    
    public static ImageIcon logoImage = ImageUtils.getImageIcon("image/gui/pofLogo.png");
    
    public static boolean launchedFlag = false;
    
    public static MainPanel contentPane = null;
    
	public static boolean fullScreen = true;
    
	public static void initGlobalFontSetting(Font fnt){
	    FontUIResource fontRes = new FontUIResource(fnt);
	    for(Enumeration<Object> keys = UIManager.getDefaults().keys(); keys.hasMoreElements();){
	        Object key = keys.nextElement();
	        Object value = UIManager.get(key);
	        if(value instanceof FontUIResource){
	            UIManager.put(key, fontRes);
	        }
	    }
	}
	
	public static void launchHomeMain() {
		if(launchedFlag){
			return;
		}
		Font font = new Font(systemFontName, Font.PLAIN, 14);		
		initGlobalFontSetting(font);
		
		contentPane = new MainPanel();
		
		contentPane.setBorder(new EmptyBorder(10,10,10,10));
		
		JFrame frame = new JFrame();
		frame.setTitle("Protocol Oblivious Forwarding (POF) Configuration GUI");
		frame.setIconImage(logoImage.getImage());
		
		frame.getContentPane().add(contentPane);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		SwingUtils.fullScreen(frame);
		frame.setVisible(true);
		
		if(fullScreen){
			frame.dispose();
			frame.setUndecorated(true);
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
			frame.setVisible(true);
		}
		
		launchedFlag = true;
	}

	public static void addSwitch(int switchId) {
		if(null != contentPane
				&& null != contentPane.getSwitchPanel()
				&& null != contentPane.getSwitchPanel().getDeviceInfoButton()){
			
			contentPane.getSwitchPanel().addSwitch(switchId);
		}		
	}
	
	public static void processDeviceInfo(final int switchId, final OFMessage ofmsg){
		if(null != contentPane
				&& null != contentPane.getSwitchPanel()
				&& null != contentPane.getSwitchPanel().getDeviceInfoButton()){
			contentPane.getSwitchPanel().getDeviceInfoButton().processDeviceInfo(switchId, ofmsg);
		}
	}

	public static void removeSwitch(int switchId) {
		if(null != contentPane){
			if(null != contentPane.getMenuPanel()){
				contentPane.getMenuPanel().removeSwitch(switchId);
			}
			
			if(null != contentPane.getSwitchPanel()){
				contentPane.getSwitchPanel().removeSwitch(switchId);
				
				if(null != contentPane.getSwitchPanel().getDeviceInfoButton()){
					contentPane.getSwitchPanel().getDeviceInfoButton().removeSwitch(switchId);
				}
			}
		}		
	}

	public static void rollBack(int switchId, OFMessage sendedMsg) {
		if(null != contentPane){
			switch(sendedMsg.getType()){
				case TABLE_MOD:
				case FLOW_MOD:
					if(null != contentPane.getSwitchPanel()){
						contentPane.getSwitchPanel().reloadSwitchPanel();
					}					
					break;
				case GROUP_MOD:
					if (null != contentPane.getMenuPanel()
							&& null != contentPane.getMenuPanel().getGroupPane()) {
						contentPane.getMenuPanel().getGroupPane().rollBack(switchId, sendedMsg);
					}
					break;
				default:
					return;
			}
		}
	}

	public static void reloadUI() {
		if(null != contentPane){
			
			if(null != contentPane.getMenuPanel()){
				contentPane.getMenuPanel().reloadMenu();
			}
			
			if(null != contentPane.getProtocolPanel()){
				contentPane.getProtocolPanel().reloadProtocolPanel();
			}
			
			if(null != contentPane.getSwitchPanel()){
				contentPane.getSwitchPanel().reloadSwitchPanel();
				
				contentPane.getSwitchPanel().resetFlowEntryListMap();
			}
		}
	}
}
