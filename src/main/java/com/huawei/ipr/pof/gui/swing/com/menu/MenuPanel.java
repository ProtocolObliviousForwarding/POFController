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

package com.huawei.ipr.pof.gui.swing.com.menu;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.huawei.ipr.pof.gui.comm.GUITools;
import com.huawei.ipr.pof.gui.swing.com.MainPanel;
import com.huawei.ipr.pof.gui.swing.com.group.GroupPane;
import com.huawei.ipr.pof.manager.IPMService;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
@SuppressWarnings("serial")
public class MenuPanel extends JPanel {

	public final MenuLabel fileMenuLabel = new MenuLabel("File");
	public final MenuLabel groupMenuLabel = new MenuLabel("Group");
	public final MenuLabel meterMenuLabel = new MenuLabel("Meter");
	public final MenuLabel helpMenuLabel = new MenuLabel("Help");
	
    protected JEditorPane ep;
	
	protected final FilePane filePane = new FilePane(this);
	protected final GroupPane groupPane = new GroupPane(this);	
	protected final MeterPane meterPane = new MeterPane(this);
	
	protected final MainPanel mainPanel;
	
	protected int w = 0;
	protected int h = 0;
	
	public final GroupPane getGroupPane(){
		return groupPane;
	}
	
	public final MainPanel getMainPanel(){
		return mainPanel;
	}
	
	public MenuPanel(final MainPanel mainPanel) {
		this.mainPanel = mainPanel;
		
		FlowLayout ly = new FlowLayout(FlowLayout.LEFT, 12, 5);
		ly.setAlignment(FlowLayout.LEFT);
		this.setLayout(ly);
		
		this.add(fileMenuLabel);
		this.add(groupMenuLabel);
		this.add(meterMenuLabel);
		this.add(helpMenuLabel);
		
		this.setOpaque(false);
		this.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
		
		fileMenuLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				JFrame win = (JFrame) SwingUtilities.getWindowAncestor(MenuPanel.this);
				Component c = win.getGlassPane();
				if(c != null && c.isVisible()){
					c.setVisible(false);
					if(c == filePane){
						return;
					}
				}
				showFilePane();
			}
		});
		
		groupMenuLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				JFrame win = (JFrame) SwingUtilities.getWindowAncestor(MenuPanel.this);
				Component c = win.getGlassPane();
				if(c != null && c.isVisible()){
					c.setVisible(false);
					if(c == groupPane){
						return;
					}
				}
				showGroupPane();
			}
		});

		meterMenuLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				final JFrame win = (JFrame) SwingUtilities.getWindowAncestor(MenuPanel.this);
				Component c = win.getGlassPane();
				if(c != null && c.isVisible()){
					c.setVisible(false);
					if(c == meterPane){
						return;
					}
				}
				showMenuPane();
			}
		});
		
		helpMenuLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				final JFrame win = (JFrame) SwingUtilities.getWindowAncestor(MenuPanel.this);
				Component c = win.getGlassPane();
				if(c != null && c.isVisible()){
					c.setVisible(false);
				}
				showHelpPane();
			}
		});
		
		fileMenuLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		groupMenuLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		meterMenuLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		helpMenuLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		
		initHelpEp();
	}
	
	protected void showFilePane() {
		JFrame win = (JFrame) SwingUtilities.getWindowAncestor(this);
		win.setGlassPane(filePane);
		filePane.setVisible(true);  
	}

	protected void showGroupPane() {
		JFrame win = (JFrame) SwingUtilities.getWindowAncestor(this);
		win.setGlassPane(groupPane);
		groupPane.setVisible(true);
	}

	protected void showMenuPane() {
		JFrame win = (JFrame) SwingUtilities.getWindowAncestor(this);
		win.setGlassPane(meterPane);
		meterPane.setVisible(true);
	}
	
	protected void showHelpPane(){	
		GUITools.messageDialog(MenuPanel.this, ep);
	}
	
	protected void initHelpEp(){
	    ep = new JEditorPane("text/html",   "<html>" +
												"<body>" +
													"Version " + IPMService.POF_VERSION + 
													"<br>" +
													"Please visit POF website: " +
														"<br>" +
															"<center>" +
																"<a href=\"http://www.poforwarding.org/\">" +
																	"http://www.poforwarding.org" +
																"</a>" +
															"</center>" +
														"</br>" +
												"</body>" +
											"</html>");
		
	    ep.addHyperlinkListener(new HyperlinkListener(){
	        @Override
	        public void hyperlinkUpdate(HyperlinkEvent e){
	            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)){
	            	try {
						Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
	            }
	        }
	    });
	    ep.setEditable(false);
	}
	
	public void reloadMenu() {
		groupPane.reloadGroup();
		
		meterPane.reloadAllMeters();
	}
	
	public void removeSwitch(final int switchID) {
		groupPane.removeSwitch(switchID);
		
		meterPane.reloadAllMeters();
	}

	public void doLayout() {
		super.doLayout();
		w = this.getWidth();
		h = this.getHeight();
	}
	
	public int getW(){
		return w;
	}
}
