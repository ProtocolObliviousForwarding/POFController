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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.openflow.protocol.table.OFFlowTable;

import com.huawei.ipr.pof.gui.comm.GUITools;
import com.huawei.ipr.pof.gui.comm.GUITools.EDIT_STATUS;
import com.huawei.ipr.pof.gui.swing.HomeMain;
import com.huawei.ipr.pof.gui.swing.SwingUIPanel;
import com.huawei.ipr.pof.gui.swing.comutil.ImageButton;
import com.huawei.ipr.pof.gui.swing.comutil.ImageUtils;
import com.huawei.ipr.pof.gui.swing.comutil.NOPanel;
import com.huawei.ipr.pof.manager.IPMService;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
@SuppressWarnings("serial")
public class SingleDiamondTablePanel extends NOPanel {
	public ImageButton diamondButton = new ImageButton("image/gui/swing/diamond_black.png");
	protected SwitchPanel switchPanel;
	protected JLabel label = new JLabel("");
	
	protected EDIT_STATUS tableEditStatus = EDIT_STATUS.ES_ADDING;
	
	protected OFFlowTable ofTable;
   
	protected DefaultListModel listModel;
	protected MulitListContainerPanel middlePanel;
	
	protected boolean isSelected = false;
	
	public SingleDiamondTablePanel(final SwitchPanel switchPanel, final MulitListContainerPanel middlePanel, final OFFlowTable ofFlowTable) {
		this.switchPanel = switchPanel;
		this.ofTable = ofFlowTable;
		
		if(ofTable != null){
			label.setText(ofTable.getTableName());
			tableEditStatus = EDIT_STATUS.ES_READONLY;
		}else{
			int tableNum = switchPanel.middlePanel.diamondTablesPanel.getComponents().length;
			if(0 == tableNum){
				label.setText(IPMService.FIRST_ENTRY_TABLE_NAME);
			}else{
				label.setText("NewTable");
			}			
		}
		
		this.middlePanel = middlePanel;
		diamondButton.setPreferredSize(new Dimension(70, 50));
		label.setForeground(HomeMain.LabelColor);
		FlowLayout fl = new FlowLayout(FlowLayout.LEADING);
		fl.setHgap(10);
		this.setLayout(fl);
		
		diamondButton.addMouseListener(new MouseAdapter(){
			public void mouseReleased(MouseEvent e) {
				int button = e.getButton();
				
				if(button == MouseEvent.BUTTON1){
					diamondButtonAction();
				}else if(button == MouseEvent.BUTTON3){
					if(isSelected == true){
						JPopupMenu popMenu = new JPopupMenu();
						
						JMenuItem delItem = new JMenuItem("delete");
						popMenu.add(delItem);
						popMenu.show(diamondButton, e.getX(), e.getY());

						delItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								deleteFlowTable();
							}
						});
					}
				}
			}
		});
		
		this.add(diamondButton);
		this.add(label);
	}
	
	private void deleteFlowTable() {
		if(null != ofTable 
				&& ofTable.getTableName() == IPMService.FIRST_ENTRY_TABLE_NAME
				&& this.getDiamondTableCounter() > 1){
			GUITools.messageDialog(this, "Can not delete " + IPMService.FIRST_ENTRY_TABLE_NAME);
			return;
		}
		
		int opt = GUITools.confirmDialog(diamondButton, 
											"Delete this flow table and all subentries?", 
											"Delete all?", 
											JOptionPane.YES_NO_CANCEL_OPTION,
											JOptionPane.QUESTION_MESSAGE);

		if (opt == JOptionPane.YES_OPTION) {
			if (null != ofTable) {
				byte globalTableID = SwingUIPanel.pofManager
						.parseToGlobalTableId(SwitchPanel.switchID, ofTable.getTableType().getValue(), ofTable.getTableId());
				if (IPMService.FLOWTABLEID_INVALID != globalTableID) {
					SwingUIPanel.pofManager.iDelFlowTableAndAllSubEntries(SwitchPanel.switchID, globalTableID);
				}

				ofTable = null;
			}

			switchPanel.reloadSwitchPanel();
		}
	}
	
	public void diamondButtonAction(){
		PList1 list1 = middlePanel.list1;
		
		if(EDIT_STATUS.ES_READONLY == tableEditStatus){
			label.setText(ofTable.getTableName());
			
			list1.DisplayList1(ofTable, SwitchPanel.switchID, ofTable.getTableName());		
		}else{
			switchPanel.protocolPanel.selectedMatchFieldList.clear();
			
			list1.DisplayList1(null, SwitchPanel.switchID, label.getText());
		}

		middlePanel.repaint();
		disableOther();
		setSelected(true);
	}
	
	public void setText(String str){
		label.setText(str);
	}
	
	public void setSelected(boolean isSelected){
		this.isSelected = isSelected;
		if(isSelected){
			diamondButton.setIcon(ImageUtils.getImageIcon("image/gui/swing/diamond_blue.png"));
			switchPanel.middlePanel.diamondTablesPanel.setSelected(this);
		}else{
			diamondButton.setIcon(ImageUtils.getImageIcon("image/gui/swing/diamond_black.png"));
			switchPanel.middlePanel.diamondTablesPanel.setSelected(null);
		}
	}
	
	private void disableOther() {
		JPanel parentPane = (JPanel) this.getParent();
		Component[] children = parentPane.getComponents();
	    for(Component c:children){
	    	if(c instanceof SingleDiamondTablePanel){
	    		((SingleDiamondTablePanel)c).setSelected(false);
	    	}
	    }
	}
	
	private int getDiamondTableCounter(){
		int counter = 0;
		JPanel parentPane = (JPanel) this.getParent();
		Component[] children = parentPane.getComponents();
	    for(Component c:children){
	    	if(c instanceof SingleDiamondTablePanel){
	    		counter++;
	    	}
	    }
	    return counter;
	}
}
