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
package com.huawei.ipr.pof.gui.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openflow.protocol.OFError;
import org.openflow.protocol.OFFeaturesReply;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPortStatus;
import org.openflow.protocol.table.OFFlowTableResource;
import org.openflow.util.HexString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.ipr.pof.gui.comm.IMainPanel;
import com.huawei.ipr.pof.manager.IPMService;
/**
 * ConsoleUIMainPanel module contains command line console UIs. 
 * It could be launched either stand alone or with other UI (e.g. SwingUI), 
 * when there is "console" in 
 * com.huawei.ipr.pof.gui.comm.POFGUI.mainpanel of floodlightdefault.properties file 
 * <p>
 * If launched pof controller in eclipse, user could input the command line in Console view in eclipse 
 * <p>
 * If launched the executable pof controller file in terminal (linux/unix/mac) or in cmd (windows), user could 
 * input the commmand line in the terminal/cmd window.
 * 
 * 
 * @see ConsoleUI
 * 
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 *
 */
public class ConsoleUIMainPanel extends Thread implements IMainPanel {
	protected static Logger log = LoggerFactory.getLogger(ConsoleUIMainPanel.class);
	
	public static IPMService pofManager;
	
	protected List<ConsoleUI> consoleList = Collections.synchronizedList(new ArrayList<ConsoleUI>());
	
	public ConsoleUIMainPanel(IPMService pofManager){
		ConsoleUIMainPanel.pofManager = pofManager;
	}
	
	public void addNewConsoleUI(final ConsoleUI consoleUI){
		consoleList.add(consoleUI);
	}
	
	@Override
	public void run() {
		try {
			open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void open() {
		ConsoleUI consoleUI = ConsoleUI.launchConsoleUI();
		ConsoleUI.setConsoleUIMainPanel(this);
		addNewConsoleUI(consoleUI);
		consoleUI.start();
	}

	@Override
	public void addSwitch(int switchId) {
		for(ConsoleUI consoleUI : consoleList){
			consoleUI.addSwitch(switchId);
		}
		
		String string = "Switch " + HexString.toHex(switchId) + " connected";
		log.info(string);
	}

	@Override
	public void displayDeviceInfo(int switchId, OFFeaturesReply featureReply) {
		for(ConsoleUI consoleUI : consoleList){
			consoleUI.processDeviceInfo(switchId, featureReply);
		}
		
		String string = "Receive featureReply from switch[" + HexString.toHex(switchId) + "]";
		log.info(string);
	}

	@Override
	public void displayPortInfo(int switchId, OFPortStatus portStatus) {
		for(ConsoleUI consoleUI : consoleList){
			consoleUI.processDeviceInfo(switchId, portStatus);
		}
		
		String string = "Receive portInfo[" + portStatus.getDesc().getName() + "] from switch[" + HexString.toHex(switchId) + "]";
		log.info(string);
	}

	@Override
	public void displayResourceReport(int switchId, OFFlowTableResource flowTableResource) {
		for(ConsoleUI consoleUI : consoleList){
			consoleUI.processDeviceInfo(switchId, flowTableResource);
		}
		
		String string = "Receive resourceReport from switch[" + HexString.toHex(switchId) + "]";
		log.info(string);

	}

	@Override
	public void displayOFError(int switchId, OFError error) {
		for(ConsoleUI consoleUI : consoleList){
			consoleUI.processDeviceInfo(switchId, error);
		}
		
		String string = "Receive errorMsg[" + error.getErrorType() + "/" + error.getErrorCode() + " from switch[" + HexString.toHex(switchId) + "]";
		log.info(string);

	}

	@Override
	public void displayPacketIn(int switchId, OFPacketIn packetIn) {
		for(ConsoleUI consoleUI : consoleList){
			consoleUI.processDeviceInfo(switchId, packetIn);
		}
		
		String string = "Receive packetIn from switch[" + HexString.toHex(switchId) + "]";
		log.info(string);
	}

	@Override
	public void removeSwitch(int switchId) {
		for(ConsoleUI consoleUI : consoleList){
			consoleUI.removeSwitch(switchId);
		}
		
		String string = "Switch[" + HexString.toHex(switchId) + "] disconnected";
		log.info(string);

	}

	@Override
	public void rollBack(int switchId, OFMessage sendedMsg) {
		for(ConsoleUI consoleUI : consoleList){
			consoleUI.rollBack(switchId, sendedMsg);
		}
		
		String string = "Switch[" + HexString.toHex(switchId) + "] RollBack " + sendedMsg.getType() + " data.";
		log.info(string);
	}

	@Override
	public void reloadUI() {
		for(ConsoleUI consoleUI : consoleList){
			consoleUI.reloadUI();
		}
		
		String string = "reloadUI.";
		log.info(string);
		
	}
}
