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

package com.huawei.ipr.pof.gui.comm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;

import org.openflow.protocol.OFError;
import org.openflow.protocol.OFFeaturesReply;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPortStatus;
import org.openflow.protocol.table.OFFlowTableResource;

import com.huawei.ipr.pof.manager.IPMService;
/** 
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd. 
 */
public class POFGUI implements IFloodlightModule, IGUIService {
    
    protected IPMService pofManager;

    private List<IMainPanel> mainPanelList = Collections.synchronizedList(new ArrayList<IMainPanel>());
    
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> l = 
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IGUIService.class);
        return l;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>,
        IFloodlightService> m = 
            new HashMap<Class<? extends IFloodlightService>,
                        IFloodlightService>();
        // We are the class that implements the service
        m.put(IGUIService.class, this);
        
        return m;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        return null;
    }

    @Override
    public void init(FloodlightModuleContext context)
            throws FloodlightModuleException {
        this.pofManager = context.getServiceImpl(IPMService.class);
    }

    @Override
    public void startUp(FloodlightModuleContext context) {
		try {
	        Map<String, String> configOptins = context.getConfigParams(this);
	        
	        String portNumString = configOptins.get("mainpanel");
	        if(null != portNumString){
	        	//could add other gui, or console, if like
	        	if ( portNumString.contains("swing") ){
	        		IMainPanel swingUIMainPanel = new com.huawei.ipr.pof.gui.swing.SwingUIPanel(pofManager);
	        		mainPanelList.add(swingUIMainPanel);
	        		swingUIMainPanel.start();
		        }
	        	
	        	if ( portNumString.contains("console") ){
	        		IMainPanel consoleUIMainPanel = new com.huawei.ipr.pof.gui.console.ConsoleUIMainPanel(pofManager);
	        		mainPanelList.add(consoleUIMainPanel);
	        		consoleUIMainPanel.start();
		        }
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}        
    }
    
    @Override
    public void addSwitch(int switchId){
    	for(IMainPanel mainPanel : mainPanelList){
    		mainPanel.addSwitch(switchId);
    	}
    }

    @Override
    public void displayDeviceInfo(int switchId, OFFeaturesReply featureReply) {
    	for(IMainPanel mainPanel : mainPanelList){
    		mainPanel.displayDeviceInfo(switchId, featureReply);
    	}
    }

    @Override
    public void displayPortInfo(int switchId, OFPortStatus portStatus) {
    	for(IMainPanel mainPanel : mainPanelList){
    		mainPanel.displayPortInfo(switchId, portStatus);
    	}
    }

    @Override
    public void displayResourceReport(int switchId, OFFlowTableResource flowTableResource) {
    	for(IMainPanel mainPanel : mainPanelList){
    		mainPanel.displayResourceReport(switchId, flowTableResource);
    	}
    }

    @Override
    public void displayOFError(int switchId, OFError error) {
    	for(IMainPanel mainPanel : mainPanelList){
    		mainPanel.displayOFError(switchId, error);
    	}
    }

    @Override
    public void displayPacketIn(int switchId, OFPacketIn packetIn) {
    	for(IMainPanel mainPanel : mainPanelList){
    		mainPanel.displayPacketIn(switchId, packetIn);
    	}
    }
    
    @Override
    public void removeSwitch(int switchId){
    	for(IMainPanel mainPanel : mainPanelList){
    		mainPanel.removeSwitch(switchId);
    	}
    }
    
    @Override
    public void rollBack(int switchId, OFMessage sendedMsg) {
    	for(IMainPanel mainPanel : mainPanelList){
    		mainPanel.rollBack(switchId, sendedMsg);
    	}
    }
    
    @Override
    public void reloadUI(){
    	for(IMainPanel mainPanel : mainPanelList){
    		mainPanel.reloadUI();
    	}
    }
}
