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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
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
import javax.swing.table.TableColumn;

import org.openflow.protocol.OFMatchX;
import org.openflow.protocol.instruction.OFInstruction;
import org.openflow.protocol.instruction.OFInstructionType;

import com.huawei.ipr.pof.gui.comm.GUITools;
import com.huawei.ipr.pof.gui.comm.GUITools.EDIT_STATUS;
import com.huawei.ipr.pof.gui.swing.HomeMain;
import com.huawei.ipr.pof.gui.swing.comutil.FlowEntry;
import com.huawei.ipr.pof.gui.swing.comutil.ImageUtils;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
@SuppressWarnings("serial")
public class PList4 extends JTable {
	public final Image def_img = ImageUtils.getImageIconByCaches("image/gui/swing/listbg.png").getImage();
	public static final String INSTRUCTION_ITEM_STRING = "Instruction item:";
	public static final String fieldString[] = {"Field < ", " >"};
	
	protected boolean isCellEditable = true;
	protected JTextField textfield = new JTextField();
	protected DefaultCellEditor editor = new DefaultCellEditor(textfield);
	protected String[] data = {};
	protected DefaultTableModel dm;
	
	protected PList5 list5;
	protected PList3 list3;
	
	protected FlowEntry currentFlowEntry;
	
	private Vector<Vector<String>> strArray2Vector(String[] str) {
		Vector<Vector<String>> vector = new Vector<Vector<String>>();
		for (int i = 0; i < str.length; i++) {
			Vector<String> v = new Vector<String>();
			v.addElement(str[i]);
			vector.addElement(v);
		}
		return vector;
	}

	public PList4() {
		super();
		setOpaque(false);
		this.putClientProperty(GUITools.TERMINATE_EDIT_ON_FOCUS_LOST, Boolean.TRUE);

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

		TableColumn tc = getColumn("");
		
		tc.setCellRenderer(new PList4CellRenderer());
		
		setRowHeight(24);

		addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				int button = e.getButton();
				if(button == MouseEvent.BUTTON1){
					selectedValueChanged();
				}else if (button == MouseEvent.BUTTON3) {
					addDelete(e);
				}
			}
		});
		
		dm.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				int t = e.getType();
				if (TableModelEvent.UPDATE == t) {
					String value = "" + dm.getValueAt(0, 0);
					if (((String)list3.getSelectedValue()).startsWith(PList3.flowEntryEleString[1])
							&& currentFlowEntry.editStatus != EDIT_STATUS.ES_READONLY
							&& value.matches(GUITools.RE_DEC)){
						DefaultListModel list3Model = (DefaultListModel) list3.getModel();
						list3Model.set(1, PList3.flowEntryEleString[1] + value);
						currentFlowEntry.ofFlowMod.setPriority(Short.valueOf(value));
					}
				}
			}
		});
	}
		
	private void addDelete(MouseEvent e){
		final int selectedRow = getSelectedRow();
		if (currentFlowEntry.editStatus != EDIT_STATUS.ES_READONLY
				&& selectedRow > 0 
				&& this.getValueAt(0, 0).equals(INSTRUCTION_ITEM_STRING)) {	//flow entry
			
			JPopupMenu popMenu = new JPopupMenu();
			//delete
			JMenuItem delItem = new JMenuItem("Delete");
			popMenu.add(delItem);						
	
			delItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					currentFlowEntry.ofFlowMod.getInstructionList().remove(selectedRow - 1);
					
					int insNum = currentFlowEntry.ofFlowMod.getInstructionList().size();
					currentFlowEntry.ofFlowMod.setInstructionNum((byte)insNum);
					
					DefaultListModel list3Model = (DefaultListModel)list3.getModel();
					list3Model.setElementAt(PList3.flowEntryEleString[3] + ( (insNum == 0) ? "" : "(" + insNum + ")"), 3);
					
					clearData();
				}
			});
		
			popMenu.show(this, e.getX(), e.getY());
		}
	}

	public void setOtherList(PList3 list3, PList5 list5) {
		this.list5 = list5;
		this.list3 = list3;
	}
	
	protected void selectedValueChanged() {
		list5.clearData();

		int index = this.getSelectedRow();

		if (-1 == index) {
			return;
		}
		
		String value = String.valueOf(this.getModel().getValueAt(index, 0));
		
		if (null == value || 0 == value.length()) {
			return;
		}
		
		if( value.startsWith(fieldString[0]) ){										//display matchXfield
			list5.showField(currentFlowEntry, index);
		}else if(index == 0 
				&& value.equals(INSTRUCTION_ITEM_STRING)	){			//display all Instructions in list5
			if(currentFlowEntry.editStatus != EDIT_STATUS.ES_READONLY){
				list5.showAllDefaultInstructions(currentFlowEntry);
			}
		}else if (index != 0){													//display an ins
			list5.showInstruction(currentFlowEntry, index - 1);
		}
		
		validate();
		repaint();
	}
	
	public void showPriority(final FlowEntry currentFlowEntry) {
		this.currentFlowEntry = currentFlowEntry;
		clearData();
		
		if(currentFlowEntry.editStatus == EDIT_STATUS.ES_READONLY){
			return;
		}
		
		isCellEditable = true;
		dm.addRow(new String[] { String.valueOf(currentFlowEntry.ofFlowMod.getPriority()) });
	
		validate();
		repaint();
	}
	
	public void showFields(final FlowEntry currentFlowEntry) {
		this.currentFlowEntry = currentFlowEntry;
		
		isCellEditable = false;
		clearData();
		
		List<OFMatchX> matchXList = currentFlowEntry.ofFlowMod.getMatchList();

		if(matchXList != null){
			for (OFMatchX field : matchXList){
				dm.addRow(new String[] { fieldString[0] + field.getFieldId() + fieldString[1] });
			}
		}
		
		validate();
		repaint();
	}
	
	public void showInstruction(final FlowEntry currentFlowEntry) {
		this.currentFlowEntry = currentFlowEntry;
		clearData();

		dm.addRow(new String[] { INSTRUCTION_ITEM_STRING });
		
		isCellEditable = false;
		
		List<OFInstruction> insList = currentFlowEntry.ofFlowMod.getInstructionList();
		if(currentFlowEntry.ofFlowMod.getInstructionList() == null){
			insList = new ArrayList<OFInstruction>();
			currentFlowEntry.ofFlowMod.setInstructionList(insList);
			
			
		}
		for (OFInstruction instruction : insList){
			dm.addRow(new String[] { instruction.getType().toString() });
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
		
		list5.clearData();
		
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
	
	class PList4CellRenderer extends DefaultTableCellRenderer {
		public Image icon = ImageUtils.getImageIcon("image/gui/swing/list_plus_icon.png").getImage();
		public Image select_img = ImageUtils.getImageIconByCaches("image/gui/swing/list_select.png").getImage();
		public Image add_end_img = ImageUtils.getImageIconByCaches("image/gui/swing/plusbutton_small.png").getImage();
		
		protected Object value;
		protected boolean isSelected = false;
		protected int row = -1;
		
		public PList4CellRenderer() {
			setOpaque(false);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			this.value = value;
			this.isSelected = isSelected;
			this.row = row;
			return c;
		}

		@Override
		public Dimension getPreferredSize() {
			Dimension d = super.getPreferredSize();
			return new Dimension((int) d.getWidth(), 24);
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
			
			if (value.equals(INSTRUCTION_ITEM_STRING)
					&& row == 0 
					&& currentFlowEntry.editStatus != EDIT_STATUS.ES_READONLY) {
				g2d.drawImage(icon, 3, 3, null);
			}
			
			g2d.setColor(HomeMain.packetLabelColor);
			g2d.drawRoundRect(2, 1, w - 5, h - 2, 3, 3);
			g2d.setColor(HomeMain.LabelColor);
			g2d.drawString("    " + value, 10, 2 + 15);
			
			
			if (value.equals(OFInstructionType.APPLY_ACTIONS.toString())
					&& currentFlowEntry.editStatus != EDIT_STATUS.ES_READONLY) {
				g2d.drawImage(add_end_img, this.getWidth() - 25, 0, 24, 24, null);
			}
		}
	}
}
