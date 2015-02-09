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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import org.openflow.protocol.OFError;
import org.openflow.protocol.OFError.OFErrorType;
import org.openflow.protocol.OFFeaturesReply;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPhysicalPort;
import org.openflow.protocol.OFPortStatus;
import org.openflow.protocol.OFPortStatus.OFPortReason;
import org.openflow.protocol.table.OFFlowTableResource;
import org.openflow.protocol.table.OFTableResource;
import org.openflow.protocol.table.OFTableType;
import org.openflow.util.HexString;
import org.openflow.util.ParseString;

import com.huawei.ipr.pof.gui.comm.GUITools;
import com.huawei.ipr.pof.gui.swing.HomeMain;
import com.huawei.ipr.pof.gui.swing.SwingUIPanel;
import com.huawei.ipr.pof.gui.swing.comutil.ImageUtils;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
@SuppressWarnings("serial")
public class DeviceInfoTable extends JTable {

	public Image imgBG = ImageUtils.getImageIcon("image/gui/swing/menu_glass_bg.png").getImage();
	
	public final static String stringDeviceString[][] = {
			{ "Device ID", "Total Port Num", "Connected Port Num", "Total Table Num", "Capabilities", 
							"Experimenter", "Forward Engine", "Lookup Engine" },
							
			{ "Device ID", "Port ID", "HW Address", "Name", "Config", 
							"State", "Current Feature", "Advertised", "Suportted", "Peer", 
							"Current Speed", "Max Speed",  "OpenFlow Enabled" },
	            			
			{ "Device ID", "Type", "Counter Num", "Meter Num", "Group Num", 
							"Table Type/Num/KeyLength/TotalSize", 
							"Table Type/Num/KeyLength/TotalSize", 
							"Table Type/Num/KeyLength/TotalSize", 
	            			"Table Type/Num/KeyLength/TotalSize" },
	            			
	        { "Device ID", "Time", "ErrorType", "ErrorCode", "Error String" },
	        
	        { "Device ID", "Time", "Buffer ID", "Total Length", "Reason", 
	        				"Table ID", "Data" }
	};
	
	protected DefaultTableModel[] dtms = new DefaultTableModel[stringDeviceString.length];
	protected int currentActiveIndex = 0;
	protected final EnablePOFEditor pofEditor = new EnablePOFEditor();
	
	public DeviceInfoTable() {
		super();
		this.setOpaque(false);
		this.putClientProperty(GUITools.TERMINATE_EDIT_ON_FOCUS_LOST, Boolean.TRUE);
		
		int i;
		int dtmsSize = dtms.length;
		for(i = 0; i < dtmsSize; i++){
			if(i != 1){
				dtms[i] = new DefaultTableModel(){
					public boolean isCellEditable(int row, int column) {
						return false;
					}
				};
			}else{
				dtms[i] = new DefaultTableModel(){
					public boolean isCellEditable(int row, int column) {
						if (column != stringDeviceString[1].length - 1) {
							return false;
						} else {
							return true;
						}
					}
				};
			}
			
			Vector<String> dummyHeader = new Vector<String>();
			for(String stringElement : stringDeviceString[i]){
				dummyHeader.addElement(stringElement);
			}

			dtms[i].setDataVector(null, dummyHeader);
		}
		
		getTableHeader().setBackground(new Color(229, 229, 229));
		getTableHeader().setPreferredSize(new Dimension(200, 26));
		getTableHeader().setReorderingAllowed(false);


		this.setModel(dtms[0]);

		this.setBackground(Color.WHITE);
		this.setBorder(BorderFactory.createLineBorder(HomeMain.LabelColor));

		this.setRowHeight(26);
	}
	
	public Object getValueAt(int dtmIndex, int row, int column){
		if(null != dtms && dtmIndex < dtms.length && null != dtms[dtmIndex]){
			return dtms[dtmIndex].getValueAt(row, column);
		}
		return null;
	}

	protected void paintComponent(Graphics g) {
		JViewport viewport = (JViewport) this.getParent();
		g.drawRoundRect(0, 0, viewport.getWidth(), viewport.getHeight(), 10, 10);
		super.paintComponent(g);
	}

//	public Dimension getPreferredSize() {
//		JViewport viewport = (JViewport) this.getParent();
//		return new Dimension(viewport.getWidth(), viewport.getHeight());
//	}
	
	public void setActive(int tabIndex) {
		currentActiveIndex = tabIndex;
		if(tabIndex < stringDeviceString.length){
			this.setModel(dtms[tabIndex]);
			
			dtms[tabIndex].fireTableChanged(null);
						
			if(1 == tabIndex){
				TableColumn column = this.getColumnModel().getColumn(stringDeviceString[1].length - 1);
				column.setCellEditor(pofEditor);
			}
		}
	}
	
	class EnablePOFEditor extends AbstractCellEditor implements TableCellEditor {
		protected JCheckBox checkBox;
		
		protected JTable jTable;
		protected int row;
		protected int column;
		
		protected Map<String, Boolean> lastValueMap;	//<mac, lastValue>
		
		public EnablePOFEditor(){
			super();
			checkBox = new JCheckBox();
			checkBox.setText("POFEnable");
			
			lastValueMap = new HashMap<String, Boolean>();
		}
		
		public boolean stopCellEditing() {
			Boolean curValue = (Boolean)getCellEditorValue();
			byte onoff = (curValue.booleanValue() == true) ? (byte)1 : (byte)0;
			String macString = (String) jTable.getValueAt(row, 2);
			if(null != SwingUIPanel.pofManager){
				Boolean lastValue =  lastValueMap.get(macString);
				if(null == lastValue
						|| curValue != lastValue){
					int deviceID = (int)Long.parseLong( (String)jTable.getValueAt(row, 0), 16);
					int portID = (int)Long.parseLong( (String)jTable.getValueAt(row, 1));
					
					SwingUIPanel.pofManager.iSetPortOpenFlowEnable(deviceID, portID, onoff);
				}
			}

            fireEditingStopped();
            
            lastValueMap.put(macString, curValue);
            return true;  
		} 
		
		@Override
		public Object getCellEditorValue() {
			return Boolean.valueOf(checkBox.isSelected());
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			this.jTable = table;
			this.row = row;
			this.column = column;
			if(null != value){
				if (value instanceof Boolean) {
					checkBox.setSelected(((Boolean) value).booleanValue());
				}
			}
			
			return checkBox;
		}
		
		public void removeLastValue(final String macString){
			lastValueMap.put(macString, null);
		}
	}
	
	public synchronized void processDeviceInfo(final int switchId, final OFMessage ofmsg) {
		switch(ofmsg.getType()){
	        case FEATURES_REPLY:
	            processOFMessage(switchId, (OFFeaturesReply)ofmsg, dtms[0]);
	            break;
	        case PORT_STATUS:
	            processOFMessage(switchId, (OFPortStatus)ofmsg, dtms[1]);
	            break;
	        case RESOURCE_REPORT:
	            processOFMessage(switchId, (OFFlowTableResource)ofmsg, dtms[2]);
	            break;
	        case ERROR:
	            processOFMessage(switchId, (OFError)ofmsg, dtms[3]);
	            break;
	        case PACKET_IN:
	            processOFMessage(switchId, (OFPacketIn)ofmsg, dtms[4]);
	            break;
	        default:
	        	break;
        }		
	}
	
	private void processOFMessage(final int switchId, final OFFeaturesReply featureReply, final DefaultTableModel dtm) {
		dtm.addRow(
				new Object[] { 
						Integer.toHexString(featureReply.getDeviceId()), 
						String.valueOf(featureReply.getPortNum()), 
						"0",
						String.valueOf(featureReply.getTableNum()), 
						HexString.toHex(featureReply.getCapabilities()), 
						featureReply.getExperimenterName(),
						featureReply.getDeviceForwardEngineName(), 
						featureReply.getDeviceLookupEngineName()
				}
		);
	}
	
	private void processOFMessage(final int switchId, final OFPortStatus portStatus, final DefaultTableModel dtm) {
		OFPhysicalPort portDesc = portStatus.getDesc();
		Boolean openflowEnable = (portDesc.getOpenflowEnable() == 1) ? true : false;
		
		
		int rowCount = dtm.getRowCount();
		for(int row = 0; row < rowCount; row++){
			if( Integer.toHexString(portDesc.getDeviceId()).equals((String) dtm.getValueAt(row, 0))
					&& Integer.toString(portDesc.getPortId()).equals((String) dtm.getValueAt(row, 1)) ){
				String macString = (String) dtm.getValueAt(row, 2);
				dtm.removeRow(row);
				if(portStatus.getReason() == OFPortReason.OFPPR_DELETE.ordinal()){
					pofEditor.removeLastValue(macString);
					return;
				}
				break;
			}
		}
		
		dtm.addRow(
				new Object[] { 
						Integer.toHexString(portDesc.getDeviceId()), 
						Integer.toString(portDesc.getPortId()), 
                		HexString.toHex(portDesc.getHardwareAddress()), 
                		portDesc.getName(),
                        HexString.toHex(portDesc.getConfig()), 
                        HexString.toHex(portDesc.getState()),
                        HexString.toHex(portDesc.getCurrentFeatures()), 
                        HexString.toHex(portDesc.getAdvertisedFeatures()),
                        HexString.toHex(portDesc.getSupportedFeatures()),
                        HexString.toHex(portDesc.getPeerFeatures()),
                        HexString.toHex(portDesc.getCurrentSpeed()), 
                        HexString.toHex(portDesc.getMaxSpeed()),
                        openflowEnable
				}
		);
		
		//update port info at deviceinfo tab
		if(null != dtms[1]){
			byte reason = portStatus.getReason();
			if(reason == OFPortReason.OFPPR_DELETE.ordinal()
					|| reason == OFPortReason.OFPPR_ADD.ordinal()){
				rowCount = dtms[0].getRowCount();
				for(int row = 0; row < rowCount; row++){
					if( Integer.toHexString(portDesc.getDeviceId()).equals((String) dtms[0].getValueAt(row, 0)) ){
						try{
							int connectedPortNum = Integer.parseInt( (String)dtms[0].getValueAt(row, 2) );
							if(reason == OFPortReason.OFPPR_DELETE.ordinal() && connectedPortNum > 0){
								dtms[0].setValueAt(String.valueOf(connectedPortNum - 1), row, 2);
							}else if(reason == OFPortReason.OFPPR_ADD.ordinal()){
								dtms[0].setValueAt(String.valueOf(connectedPortNum + 1), row, 2);
							}
							break;
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	private void processOFMessage(final int switchId, final OFFlowTableResource flowTableResource, final DefaultTableModel dtm) {
		List<Object> stringList = new ArrayList<Object>();
		
		stringList.add(Integer.toHexString(switchId));
		stringList.add(flowTableResource.getResourceType().toString());
		stringList.add(flowTableResource.getCounterNum());
		stringList.add(flowTableResource.getMeterNum());
		stringList.add(flowTableResource.getGroupNum());
		
        OFTableResource tableResource;
        String[] stringTableResources = new String[OFTableType.MAX_TABLE_TYPE];
        for (int i = 0; i < OFTableType.MAX_TABLE_TYPE; i++) {
        	stringTableResources[i] = "";
            tableResource = flowTableResource.getTableResourcesMap().get(OFTableType.values()[i]);

            stringTableResources[i] += tableResource.getTableType().toString();
            stringTableResources[i] += "/";
            stringTableResources[i] += tableResource.getTableNum();
            stringTableResources[i] += "/";
            stringTableResources[i] += tableResource.getKeyLength();
            stringTableResources[i] += "/";
            stringTableResources[i] += tableResource.getTotalSize();
            
            stringList.add(stringTableResources[i]);
        }
        
        Object[] stringArray = stringList.toArray();

        dtm.addRow( stringArray );
	}
	
	private void processOFMessage(final int switchId, final OFError error, final DefaultTableModel dtm) {
        SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateTime = tempDate.format(new java.util.Date());

        dtm.addRow(
				new Object[] {
						Integer.toHexString(error.getDeviceId()), 
						dateTime,
						OFErrorType.values()[error.getErrorType()].toString(),
						error.getErrorCodeString(),
						ParseString.ByteToString(error.getError())
				}
		);		
	}
	
	private void processOFMessage(final int switchId, final OFPacketIn packetIn, final DefaultTableModel dtm) {
        SimpleDateFormat tempDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateTime = tempDate.format(new java.util.Date());
        String packetData = HexString.toHex(packetIn.getPacketData());

        dtm.addRow(
				new Object[] { 
						Integer.toHexString(packetIn.getDeviceId()), 
						dateTime,
						packetIn.getBufferId(),
						packetIn.getTotalLength(),
						packetIn.getReason().toString(),
						packetIn.getTableId(),
						packetData
				}
		);
		
	}

	public void removeSwitch(final int switchId) {
		int rowCount;
		int rowIndex;
		String stringDeviceId;
		if(null != dtms){
			int dtmNum = dtms.length - 2;	//do not clear all //dtms.length;
			int dtmIndex;
			for(dtmIndex = 0; dtmIndex < dtmNum; dtmIndex++){
				if(null != dtms[dtmIndex]){

					rowCount = dtms[dtmIndex].getRowCount();
					for(rowIndex = 0; rowIndex < rowCount; rowIndex++){
						stringDeviceId = (String)dtms[dtmIndex].getValueAt(rowIndex, 0);
						if( stringDeviceId.equalsIgnoreCase( Integer.toHexString(switchId) ) ){
							
							if(dtmIndex == 1){	//remove last value in port tab
								String macString = (String)dtms[dtmIndex].getValueAt(rowIndex, 2);
								pofEditor.removeLastValue(macString);
							}
							
							dtms[dtmIndex].removeRow(rowIndex);
							
							//avoid ArrayIndexOutOfBoundsException 
							rowIndex--;
							rowCount = dtms[dtmIndex].getRowCount();
						}
					}//for rowIndex loop
				}
			}//for dtmIndex loop
		}		
	}
}
