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
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
public class SwingUIPanel extends Thread implements IMainPanel {
	protected static Logger log = LoggerFactory.getLogger(SwingUIPanel.class);
	
	public static IPMService pofManager;
	
	public SwingUIPanel(IPMService pofManager){
		SwingUIPanel.pofManager = pofManager;
	}
	
	@Override
	public void run() {
		try {
			open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			//System.exit(1);
		}
	}

	public void open() {
		HomeMain.launchHomeMain();		
	}

	@Override
	public void addSwitch(int switchId) {
		HomeMain.addSwitch(switchId);
		
		String string = "Switch " + HexString.toHex(switchId) + " connected";
		log.info(string);
	}

	@Override
	public void displayDeviceInfo(int switchId, OFFeaturesReply featureReply) {
		HomeMain.processDeviceInfo(switchId, featureReply);
		
		String string = "Receive featureReply from switch[" + HexString.toHex(switchId) + "]";
		log.info(string);
	}

	@Override
	public void displayPortInfo(int switchId, OFPortStatus portStatus) {
		HomeMain.processDeviceInfo(switchId, portStatus);
		
		String string = "Receive portInfo[" + portStatus.getDesc().getName() + "] from switch[" + HexString.toHex(switchId) + "]";
		log.info(string);
	}

	@Override
	public void displayResourceReport(int switchId, OFFlowTableResource flowTableResource) {
		HomeMain.processDeviceInfo(switchId, flowTableResource);
		
		String string = "Receive resourceReport from switch[" + HexString.toHex(switchId) + "]";
		log.info(string);

	}

	@Override
	public void displayOFError(int switchId, OFError error) {
		HomeMain.processDeviceInfo(switchId, error);
		
		String string = "Receive errorMsg[" + error.getErrorType() + "/" + error.getErrorCode() + " from switch[" + HexString.toHex(switchId) + "]";
		log.info(string);

	}

	@Override
	public void displayPacketIn(int switchId, OFPacketIn packetIn) {
		HomeMain.processDeviceInfo(switchId, packetIn);
		
		String string = "Receive packetIn from switch[" + HexString.toHex(switchId) + "]";
		log.info(string);

	}

	@Override
	public void removeSwitch(int switchId) {
		HomeMain.removeSwitch(switchId);
		
		String string = "Switch[" + HexString.toHex(switchId) + "] disconnected";
		log.info(string);

	}

	@Override
	public void rollBack(int switchId, OFMessage sendedMsg) {
		HomeMain.rollBack(switchId, sendedMsg);
		String string = "Switch[" + HexString.toHex(switchId) + "] RollBack " + sendedMsg.getType() + " data.";
		log.info(string);
	}
	
	@Override
	public void reloadUI(){
		HomeMain.reloadUI();
		String string = "reload UI.";
		log.info(string);
	}
}
