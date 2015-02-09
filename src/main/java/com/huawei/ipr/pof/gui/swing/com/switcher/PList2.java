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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch20;
import org.openflow.protocol.table.OFTableType;

import com.huawei.ipr.pof.gui.comm.GUITools;
import com.huawei.ipr.pof.gui.comm.GUITools.EDIT_STATUS;
import com.huawei.ipr.pof.gui.swing.HomeMain;
import com.huawei.ipr.pof.gui.swing.SwingUIPanel;
import com.huawei.ipr.pof.gui.swing.comutil.FlowEntry;
import com.huawei.ipr.pof.gui.swing.comutil.ImageUtils;
import com.huawei.ipr.pof.gui.swing.comutil.PJComboBox;
import com.huawei.ipr.pof.manager.IPMService;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */

@SuppressWarnings("serial")
public class PList2 extends JTable {
	public final Image def_img = ImageUtils.getImageIconByCaches("image/gui/swing/listbg.png").getImage();
	
	public static final String FLOWENTRY_STRING = "Flow Entry";

	protected final Map<EDIT_STATUS, String> FLOWENTRY_STRING_MAP; 	//ADDING, MODIFYING, READONLY
	
	public static final String FLOWENTRY_FIRSTLING_STRING = "Flow Entries";
	public static final String fieldString[] = {"Field < ", " >"};
	
	protected String[] data = {};

	protected List<OFMatch20> matchFieldList;

	protected JTextField textfield = new JTextField();

	protected String[] boxdata = new String[] { OFTableType.values()[0].toString(),
										OFTableType.values()[1].toString(),
										OFTableType.values()[2].toString(),
										OFTableType.values()[3].toString() };
	protected PJComboBox tableTypeCombo = new PJComboBox(boxdata);
	
	protected DefaultTableModel dm;

	protected PList1 list1;
	protected PList3 list3;

	protected boolean isCellEditable = true;
	
	protected FlowEntry currentFlowEntry;

	protected List<FlowEntry> flowEntryList;
	
	protected int selectedIndexInList1;
	
	private Vector<Vector<String>> strArray2Vector(String[] str) {
		Vector<Vector<String>> vector = new Vector<Vector<String>>();
		for (int i = 0; i < str.length; i++) {
			Vector<String> v = new Vector<String>();
			v.addElement(str[i]);
			vector.addElement(v);
		}
		return vector;
	}
	
	public void setOtherList(PList1 list1, PList3 list3) {
		this.list1 = list1;
		this.list3 = list3;
	}

	public PList2() {
		super();
		setOpaque(false);
		this.putClientProperty(GUITools.TERMINATE_EDIT_ON_FOCUS_LOST, Boolean.TRUE);
		
		FLOWENTRY_STRING_MAP = new HashMap<EDIT_STATUS, String>();
		FLOWENTRY_STRING_MAP.put(EDIT_STATUS.ES_ADDING, FLOWENTRY_STRING + "(N)");
		FLOWENTRY_STRING_MAP.put(EDIT_STATUS.ES_MODIFYING, FLOWENTRY_STRING + "(M)");
		FLOWENTRY_STRING_MAP.put(EDIT_STATUS.ES_READONLY, FLOWENTRY_STRING);

		dm = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return isCellEditable;
			}
			
			@Override
			public void setValueAt(Object aValue, int row, int column) {
				if(null != aValue && aValue.toString().length() > 0 
						&& row < dm.getRowCount() && column < dm.getColumnCount()){	//else will occur exception
					super.setValueAt(aValue, row, column);
				}
			}			
		};
		Vector<String> dummyHeader = new Vector<String>();
		dummyHeader.addElement("");
		dm.setDataVector(strArray2Vector(data), dummyHeader);

		this.setModel(dm);

		setShowGrid(false);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		TableColumn tc = getColumnModel().getColumn(0);
		tc.setCellRenderer( new PList2CellRenderer());
		tc.setCellEditor( new PList2CellEditor());
		
		setRowHeight(24);

		addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				int button = e.getButton();
				if(button == MouseEvent.BUTTON1){
					selectedValueChanged();
				}else if (button == MouseEvent.BUTTON3) {
					addModifyAndDelete(e);
				}
			}
		});


		dm.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				int t = e.getType();
				if (TableModelEvent.UPDATE == t) {
					changeTextValue();
				}
			}
		});

		tableTypeCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				if (arg0.getStateChange() == ItemEvent.SELECTED) {
					if (arg0.getSource() == tableTypeCombo) {
						setValueFromCombo();
					}
				}
			}
		});
	}
	
	private void setValueFromCombo(){
		int index = tableTypeCombo.getSelectedIndex();
		if (-1 == index) {
			return;
		}
		
		OFTableType tableType = OFTableType.values()[index];
		
		//FIRST_ENTRY_TABLE_NAME can be MM table only (because FIRST_ENTRY_TABLE_NAME's globalTableId must be 0)
		if(list1.newTempOFTable.getTableName().equalsIgnoreCase(IPMService.FIRST_ENTRY_TABLE_NAME)
				&& tableType != OFTableType.OF_MM_TABLE){
			GUITools.messageDialog(this, IPMService.FIRST_ENTRY_TABLE_NAME + " can be MM table only");
			tableTypeCombo.setSelectedIndex(OFTableType.OF_MM_TABLE.getValue());
			index = tableTypeCombo.getSelectedIndex();
			tableType = OFTableType.values()[index];
			return;
		}
		
		DefaultListModel list1Model = (DefaultListModel) list1.getModel();

		list1Model.set(4, PList1.tableItems[4] + tableTypeCombo.getSelectedItem());
		
		list1.newTempOFTable.setTableType(tableType);
	}

	protected void selectedValueChanged() {
		list3.clearData();

		int index = getSelectedRow();
		
		if (-1 == index) {
			return;
		}
		
		String value = String.valueOf(dm.getValueAt(index, 0));
		
		if (null == value || 0 == value.length()) {
			return;
		}
		
		if (index > 0 && value.startsWith(FLOWENTRY_STRING)) {	//display flow entry in list3
			currentFlowEntry = flowEntryList.get(index - 1);
			list3.showFlowEntry( currentFlowEntry );
		}else if (index == 0 && value.startsWith(FLOWENTRY_FIRSTLING_STRING)) {		//add a new flow entry
			dm.addRow(new String[] { FLOWENTRY_STRING_MAP.get(EDIT_STATUS.ES_ADDING) });
			currentFlowEntry = FlowEntry.getNewInstance(null);
			flowEntryList.add(currentFlowEntry);
			clearSelection();
		}else if (index >= 0 && value.startsWith(fieldString[0])) {		//display matchX
			if(matchFieldList != null){
				list3.showField(matchFieldList.get(index));
			}
		}
		
		validate();
		repaint();
	}
	
	private void addModifyAndDelete(MouseEvent e){
		final int selectedRow = getSelectedRow();
		if (selectedRow > 0 && selectedIndexInList1 == 7) {	//flow entry
			currentFlowEntry = flowEntryList.get(selectedRow - 1);
			
			JPopupMenu popMenu = new JPopupMenu();
			
			//modify
			if(currentFlowEntry.editStatus == EDIT_STATUS.ES_READONLY){
				JMenuItem editItem = new JMenuItem("Modify");
				popMenu.add(editItem);

				editItem.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						list3.clearData();
						
						currentFlowEntry.editStatus = EDIT_STATUS.ES_MODIFYING;
						dm.setValueAt(FLOWENTRY_STRING_MAP.get(EDIT_STATUS.ES_MODIFYING), selectedRow, 0);
					}
				});
			}

			//delete
			JMenuItem delItem = new JMenuItem("Delete");
			popMenu.add(delItem);						

			delItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					list3.clearData();
					
					if(currentFlowEntry.editStatus != EDIT_STATUS.ES_ADDING){	//need delete from DB
						SwingUIPanel.pofManager.iDelFlowEntry(SwitchPanel.switchID, list1.globalTableID, currentFlowEntry.ofFlowMod.getIndex());
					}
					
					flowEntryList.remove(selectedRow - 1);
					
					currentFlowEntry = null;
					list1.switchPanel.middlePanel.list1.DisplayList1(list1.getOFTable(), SwitchPanel.switchID, list1.getOFTable().getTableName());
				}
			});
			
			popMenu.show(this, e.getX(), e.getY());
		}
	}

	//input table name or table size in textfield
	private void changeTextValue() {
		String value = (String) dm.getValueAt(0, 0);
		DefaultListModel list1Model = (DefaultListModel) list1.getModel();
		
		if (null == value || 0 == value.length()) {
			return;
		}
		switch (selectedIndexInList1) {
			case 1: {// table name
				//FIRST_ENTRY_TABLE_NAME can be used for first entry table ONLY
				if(value.equalsIgnoreCase(IPMService.FIRST_ENTRY_TABLE_NAME)){
					GUITools.messageDialog(this, "Name \"" + IPMService.FIRST_ENTRY_TABLE_NAME + "\" can be used for first entry table ONLY!");
					return;
				}
				
				list1Model.set(selectedIndexInList1, PList1.tableItems[selectedIndexInList1] + value);
				
				SingleDiamondTablePanel singleDiamondTablePanel = list1.middlePanel.diamondTablesPanel.getSelected();
				singleDiamondTablePanel.setText(value);
	
				list1.newTempOFTable.setTableName(value);
	
				break;
			}
			case 3: {// table size
				if(value.matches(GUITools.RE_DEC)){
					int tableSize = Integer.parseInt(value);
					
					list1Model.set(selectedIndexInList1, PList1.tableItems[selectedIndexInList1] + value);
	
					list1.newTempOFTable.setTableSize(tableSize);
				}				
				break;
			}
		}
		
		validate();
		repaint();
	}

	public void showTextField(int index) {
		selectedIndexInList1 = index;
		
		clearData();
		
		tableTypeCombo.setVisible(false);
		textfield.setVisible(true);
		
		isCellEditable = true;
		textfield.setText("");

		dm.addRow(new String[] { "" });
	}
	
	public void showTypeCombo(){
		selectedIndexInList1 = 4;
		
		clearData();
		
		isCellEditable = true;
		
		if (null != list1.getOFTable()) {
			tableTypeCombo.setSelectedIndex(list1.getOFTable().getTableType().getValue());
		} else if (null != list1.newTempOFTable && null != list1.newTempOFTable.getTableType()) {
			tableTypeCombo.setSelectedIndex(list1.newTempOFTable.getTableType().getValue());
		}else{
			tableTypeCombo.setSelectedIndex(-1);
		}
		
		tableTypeCombo.setVisible(true);
		textfield.setVisible(false);
		
		dm.addRow(new Object[]{tableTypeCombo});
	}

	public void showFlowEntry() {
		selectedIndexInList1 = 7;

		isCellEditable = false;

		clearData();
		
		tableTypeCombo.setVisible(false);
		textfield.setVisible(false);
		
		if(list1.getOFTable() == null){
			return;
		}
		
		dm.addRow(new String[] { FLOWENTRY_FIRSTLING_STRING + ": " });
		
		flowEntryList = list1.switchPanel.flowTable_flowEntryListMap.get(list1.globalTableID);
		if(null == flowEntryList){
			flowEntryList = createFlowEntryList( SwingUIPanel.pofManager.iGetAllFlowEntry(SwitchPanel.switchID, list1.globalTableID) );
			list1.switchPanel.flowTable_flowEntryListMap.put(list1.globalTableID, flowEntryList);
		}
		
		for (FlowEntry flowEntry : flowEntryList) {
			dm.addRow(new String[] { FLOWENTRY_STRING_MAP.get(flowEntry.editStatus) });
		}
		
		validate();
		repaint();
	}	

	private List<FlowEntry> createFlowEntryList(List<OFFlowMod> allFlowEntry) {
		List<FlowEntry> newFlowEntryList = Collections.synchronizedList(new ArrayList<FlowEntry>());
		if(null ==  allFlowEntry){
			return newFlowEntryList;
		}
		
		for(OFFlowMod ofFlowMod : allFlowEntry){
			newFlowEntryList.add( FlowEntry.getNewInstance(ofFlowMod) );
		}
		
		return newFlowEntryList;
	}

	public void showField(List<OFMatch20> packetFieldList) {
		this.matchFieldList = packetFieldList;
		
		selectedIndexInList1 = 6;

		isCellEditable = false;

		clearData();
		
		tableTypeCombo.setVisible(false);
		textfield.setVisible(false);

		if(null != packetFieldList){
			for (OFMatch20 field : packetFieldList) {
				dm.addRow(new String[] { fieldString[0] + field.getFieldId() + fieldString[1] });
			}
		}

		validate();
		repaint();
	}

	public void clearData() {
		int count = dm.getRowCount();
		for (int i = 0; i < count; i++) {
			dm.removeRow(0);
		}
		
		clearSelection();
		
		list3.clearData();

		textfield.setText("");

		tableTypeCombo.setSelectedIndex(-1);

		tableTypeCombo.setVisible(false);
		textfield.setVisible(false);
		
		validate();
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		JViewport viewport = (JViewport) this.getParent();
		g.drawImage(def_img, 0, 0, viewport.getWidth(), viewport.getHeight(), null);
		super.paintComponent(g);
	}

	@Override
	public Dimension getPreferredSize() {
		JViewport viewport = (JViewport) this.getParent();
		return new Dimension(viewport.getWidth(), viewport.getHeight());
	}

	@Override
	protected void paintBorder(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		int w = this.getWidth();
		int h = this.getHeight();

		g2d.setPaint(new Color(120, 120, 120));
		g2d.drawLine(w - 1, 0, w - 1, h);
	}

	class PList2CellEditor extends AbstractCellEditor implements TableCellEditor {
		@Override
		public Object getCellEditorValue() {
			switch(selectedIndexInList1){
				case 4:	{//tableType, return combo
					return tableTypeCombo.getSelectedItem();
				}
				case 1:
				case 3:{
					return textfield.getText();
				}
				default:
					return null;
			}
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			switch(selectedIndexInList1){
				case 1:
				case 3: {
					tableTypeCombo.setVisible(false);
					textfield.setVisible(true);
					
					textfield.setText(value == null ? "" : value.toString());
					return textfield;
				}
				case 4:	{//tableType, return combo
					tableTypeCombo.setVisible(true);
					textfield.setVisible(false);
					
					tableTypeCombo.setSelectedItem(value);
					return tableTypeCombo;
				}
				default:
					return null;
			}
		}
	}

	class PList2CellRenderer extends DefaultTableCellRenderer {
		public Image icon = ImageUtils.getImageIcon("image/gui/swing/list_plus_icon.png").getImage();
		public Image select_img = ImageUtils.getImageIconByCaches("image/gui/swing/list_select.png").getImage();
		protected int row = -1;
		protected Object value;
		protected boolean isSelected = false;
		
		public PList2CellRenderer() {
			setOpaque(false);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			this.value = value;
			this.isSelected = isSelected;
			this.row = row;
			switch(selectedIndexInList1){
				case 1:
				case 3:
					return textfield;
				case 4:
					return tableTypeCombo;
				case 6:
				case 7:
					return (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				default:
					return null;
			}
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			if (!isSelected) {
				g2d.drawImage(def_img, 0, 0, this.getWidth(), this.getHeight(), null);
			} else {
				g2d.drawImage(select_img, 0, 0, this.getWidth(), this.getHeight(), null);
			}
			int w = this.getWidth();
			int h = this.getHeight();
			g2d.drawRoundRect(2, 1, w - 5, h - 2, 3, 3);
			g2d.setColor(HomeMain.LabelColor);
			if (selectedIndexInList1 == 7 && row == 0) {	//display "+"
				g2d.drawImage(icon, 3, 3, null);
			}
			
			if (selectedIndexInList1 == 4) {
				g2d.drawString("    " + tableTypeCombo.getSelectedItem(), 10, 2 + 15);
			} else {
				g2d.drawString("    " + value, 10, 2 + 15);
			}
		}
	}
}