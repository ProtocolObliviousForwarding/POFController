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

package com.huawei.ipr.pof.bypassmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;

import org.openflow.protocol.OFCounterReply;
import org.openflow.protocol.OFError;
import org.openflow.protocol.OFFeaturesReply;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPortStatus;
import org.openflow.protocol.OFType;
import org.openflow.protocol.table.OFFlowTableResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.ipr.pof.manager.IPMService;

/**
 * OFMessagBypassManager provides IOFMessagBypassManagerService. 
 * <p>
 * OFMessagBypassManager is a bridge from controller to POF modules.
 * OFMessagBypassManager does not process OFMessage by itself, it provides a
 * bridge to let the OFMessages bypass and to be processed by pofManager. <br>
 * It listens the OFMessage type include:
 *      PACKET_IN, ERROR, FEATURES_REPLY, PORT_STATUS, RESOURCE_REPORT, COUNTER_REPLY
 * 
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 *
 */
public class OFMessagBypassManager 
                implements IOFMessagBypassManagerService, IOFMessageListener, IFloodlightModule{
    
    protected static Logger log = LoggerFactory.getLogger(OFMessagBypassManager.class);
    protected IFloodlightProviderService floodlightProvider = null;
    protected IPMService pofManager = null;
    
    @Override
    public String getName() {
       return "messageBypassManager";
    }

    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        return false;
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        return false;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> l = 
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IOFMessagBypassManagerService.class);
        return l;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>,
        IFloodlightService> m = 
            new HashMap<Class<? extends IFloodlightService>,
                        IFloodlightService>();
        m.put(IOFMessagBypassManagerService.class, this);
        
        return m;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l = 
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        l.add(IPMService.class);

        return l;
    }

    @Override
    public void init(FloodlightModuleContext context)
            throws FloodlightModuleException {
        this.floodlightProvider = 
                context.getServiceImpl(IFloodlightProviderService.class);
        this.pofManager = 
                context.getServiceImpl(IPMService.class);
    }

    @Override
    public void startUp(FloodlightModuleContext context) {
        floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
        
        floodlightProvider.addOFMessageListener(OFType.ERROR, this);
        floodlightProvider.addOFMessageListener(OFType.FEATURES_REPLY, this);
        floodlightProvider.addOFMessageListener(OFType.PORT_STATUS, this);
        floodlightProvider.addOFMessageListener(OFType.RESOURCE_REPORT, this);
        
        floodlightProvider.addOFMessageListener(OFType.COUNTER_REPLY, this);
    }

    @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
        switch(msg.getType()){
            case FEATURES_REPLY:
                processOFMessage((OFFeaturesReply)msg, sw);
                break;
            case PORT_STATUS:
                processOFMessage((OFPortStatus)msg, sw);
                break;
            case RESOURCE_REPORT:
                processOFMessage((OFFlowTableResource)msg, sw);
                break;
            case COUNTER_REPLY:
                processOFMessage((OFCounterReply)msg, sw);
                break;
            case ERROR:
                processOFMessage((OFError)msg, sw);
                break;
            case PACKET_IN:
                processOFMessage((OFPacketIn)msg, sw);
                break;
            default:
                processOFMessage(msg, sw);
                break;
        }

        return Command.CONTINUE;
    }
    
   
    private void processOFMessage(OFMessage msg, IOFSwitch sw){
        log.info("get msgType[{}] from {}, but NO processing", msg.getType(), sw.getId());
    }
    
    private void processOFMessage(OFFeaturesReply msg, IOFSwitch sw){
        log.info("set OFFeaturesReply[{}] from {}", msg.toString(), sw.getId());
        pofManager.iSetFeatures((int)sw.getId(), msg);
    }
    
    private void processOFMessage(OFPortStatus msg, IOFSwitch sw){
        log.info("set OFPortStatus[{}] from {}", msg.toString(), sw.getId());
        pofManager.iSetPortStatus((int)sw.getId(), msg);        
    }
    
    private void processOFMessage(OFFlowTableResource msg, IOFSwitch sw){
        log.info("set OFResourceReport[{}] from {}", msg.toString(), sw.getId());
        pofManager.iSetResourceReport((int)sw.getId(), msg);        
    }
    
    private void processOFMessage(OFCounterReply msg, IOFSwitch sw){
        log.info("set OFCounterReply[{}] from {}, xid=" + msg.getXid(), msg.toString(), sw.getId());
        pofManager.processCounterReply(sw, msg);        
    }
    
    private void processOFMessage(OFError msg, IOFSwitch sw){
        log.info("set OFError[{}] from {}", msg.toString(), sw.getId());
        pofManager.iProcessOFError((int)sw.getId(), msg);
    }
    
    private void processOFMessage(OFPacketIn msg, IOFSwitch sw){
        log.info("set OFPacketIn[{}] from {}", msg.toString(), sw.getId());
        pofManager.iProcessOFPacketIn((int)sw.getId(), msg);
    }

}
