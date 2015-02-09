package com.huawei.ipr.pof.gui.swing.com.switcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.huawei.ipr.pof.gui.swing.comutil.ImageButton;
import com.huawei.ipr.pof.gui.swing.comutil.ImageUtils;
import com.huawei.ipr.pof.gui.swing.comutil.NOPanel;
import com.huawei.ipr.pof.gui.swing.comutil.PScrollPane;

@SuppressWarnings("serial")
public class DeviceInfoTablePanel extends NOPanel {
	public final String stringDeviceTabString[] = {"Device", "Port", "Table Resource", "Last 10 Error Msg", "Last 10 PacketIn Msg"};
	
	public final Image imgBG = ImageUtils.getImageIcon("image/gui/swing/menu_glass_bg.png").getImage();
	public final Image imgBGArr = ImageUtils.getImageIcon("image/gui/swing/deviceinfo_pane_arr.png").getImage();

	public final String stringDefTabImage = "image/gui/swing/deviceinfo_table_deftab.png";
	public final String stringCurTabImage = "image/gui/swing/deviceinfo_table_curtab.png";
	
	protected ImageButton tabs[] = new ImageButton[stringDeviceTabString.length];
	
	protected DeviceInfoTable deviceInfoTable = new DeviceInfoTable();
	protected PScrollPane scrollPane;
	
	protected int i;
	
	protected final SwitchPanel switchPanel;
	
	public DeviceInfoTable getDeviceInfoTable(){
		return deviceInfoTable;
	}
	
	public DeviceInfoTablePanel(SwitchPanel switchPanel) {
		this.switchPanel = switchPanel;
		
		i = 0;
		int tabSize = tabs.length;
		for(i = 0; i < tabSize; i++){
			tabs[i] = new ImageButton(stringDeviceTabString[i], stringDefTabImage);				
			tabs[i].addActionListener(new ActionListener() {
				final int tabIndex = i;
				public void actionPerformed(ActionEvent e) {
					setActive(tabIndex);
				}
			});
			
			this.add(tabs[i]);
		}
		
		//default display tabs[0]
		tabs[0].setIcon(ImageUtils.getImageIcon(stringCurTabImage));
		deviceInfoTable.setActive(0);
		
		setLayout(null);
		
		
		scrollPane = new PScrollPane(deviceInfoTable);
//		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		this.add(scrollPane, BorderLayout.CENTER);
		scrollPane.setBackground(Color.WHITE);
		scrollPane.getViewport().setBackground(Color.WHITE);
	}

	
	protected void setActive(int tabIndex) {
		int curIndex = 0;
		for(ImageButton tab : tabs){
			if(curIndex != tabIndex){
				tab.setIcon(ImageUtils.getImageIcon(stringDefTabImage));
			}else{
				tab.setIcon(ImageUtils.getImageIcon(stringCurTabImage));
			}
			curIndex++;
		}
		deviceInfoTable.setActive(tabIndex);
	}

	public void doLayout() {
		super.doLayout();

		int w = switchPanel.getWidth();
		int h = switchPanel.getHeight() / 2;
		int x = switchPanel.getX() + 10;
		int y = switchPanel.getY() + h + 5;

		int curIndex = 0;
		for(ImageButton tab : tabs){
			tab.setBounds(x + 20 + 147 * curIndex, y + 30, 147, 26);
			curIndex++;
		}
//		tab1.setBounds(x + 20, y + 30, 147, 26);
//		tab2.setBounds(x + 167, y + 30, 147, 26);
		
		scrollPane.setBounds(x + 20, y + 56, w - 40, h - 90);
		scrollPane.repaint();
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		int w = switchPanel.getWidth();
		int h = switchPanel.getHeight() / 2;
		int x = switchPanel.getX() + 10;
		int y = switchPanel.getY() + h + 5;

		g2d.drawImage(imgBG, x, y, w, h, null);
		g2d.drawImage(imgBGArr, x + w - 20, y + h, 12, 9, null);

	}
}
