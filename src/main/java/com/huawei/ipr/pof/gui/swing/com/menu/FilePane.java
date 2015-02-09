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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.huawei.ipr.pof.gui.comm.GUITools;
import com.huawei.ipr.pof.gui.swing.HomeMain;
import com.huawei.ipr.pof.gui.swing.SwingUIPanel;
import com.huawei.ipr.pof.gui.swing.comutil.ImageButton;
import com.huawei.ipr.pof.gui.swing.comutil.ImageUtils;
/** 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
@SuppressWarnings("serial")
public class FilePane extends JPanel{

	public final Image imgBG = ImageUtils.getImageIcon("image/gui/swing/menu_glass_bg.png").getImage();
	public final Image imgBGArr = ImageUtils.getImageIcon("image/gui/swing/menu_arr.png").getImage();
	
	public final ImageButton save_button = new ImageButton("image/gui/swing/savefile.png");
	public final ImageButton load_button = new ImageButton("image/gui/swing/load.png");
	public final ImageButton max_button;
	public final ImageButton exit_button = new ImageButton("image/gui/swing/exit.png");
	
	public final Icon fullScreen = ImageUtils.getImageIcon("image/gui/swing/fullScreen.png");
	public final Icon maxScreen = ImageUtils.getImageIcon("image/gui/swing/maxScreen.png");

	protected MenuPanel menuPanel;
		
	public FilePane(MenuPanel menuPanel) {
		this.menuPanel = menuPanel;
		
		setOpaque(false);
		
		this.setLayout(null);
		
		if(HomeMain.fullScreen == true){
			max_button = new ImageButton("image/gui/swing/maxScreen.png"); 
		}else{
			max_button = new ImageButton("image/gui/swing/fullScreen.png"); 
		}
		
		this.add(save_button);
		this.add(load_button);
		this.add(max_button);
		this.add(exit_button);
		
		save_button.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				
				saveAllData();
			}
		});
		
		load_button.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				
				loadAllData();
			}
		});
		
		max_button.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				
				changeFullScreen();
			}
		});
		
		exit_button.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				System.exit(1);
			}
		});
	}
	
	protected void saveAllData(){
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));
		chooser.setFileFilter(new FileNameExtensionFilter(".db", "db"));
		int result = chooser.showSaveDialog(this);
		if(result == JFileChooser.APPROVE_OPTION){
			String fileName = chooser.getSelectedFile().getPath();
			if(null != fileName && 0 != fileName.length()){
				if(!fileName.endsWith(".db")){
					fileName = fileName + ".db";
				}
	            File file = new File(fileName);
	            if(true == file.exists()){
	            	int opt = GUITools.confirmDialog(this, 
	            										fileName + " is already existed, overwrite it?", 
				            							"Overwrite exitsed file", 
				            							JOptionPane.YES_NO_CANCEL_OPTION, 
				            							JOptionPane.QUESTION_MESSAGE);
	            	if(opt != JOptionPane.YES_OPTION){
	            		return;
	            	}
	            }
	            
	            if(true == SwingUIPanel.pofManager.saveAllDataIntoFile(fileName) ){
	            	GUITools.messageDialog(this, "Successfully saved all data into file: \n" + fileName);
	            }else{
	            	GUITools.messageDialog(this, "Ooops, save failed, please check log info.");
	            }
			}
		}		
	}
	
	protected void loadAllData(){
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));
		chooser.setFileFilter(new FileNameExtensionFilter(".db", "db"));
		int result = chooser.showOpenDialog(this);
		if(result == JFileChooser.APPROVE_OPTION){
			String fileName = chooser.getSelectedFile().getPath();
			if(null != fileName && 0 != fileName.length()){
				int opt = GUITools.confirmDialog(this, 
												"Clear ALL and Load the database from \n" + fileName + "?", 
												"clear and load?", 
												JOptionPane.YES_NO_CANCEL_OPTION,
												JOptionPane.QUESTION_MESSAGE);
				if(opt == JOptionPane.YES_OPTION){
					opt = GUITools.confirmDialog(this, 
												"Write messages to switches?",
												"write to switches?", 
												JOptionPane.YES_NO_CANCEL_OPTION,
												JOptionPane.QUESTION_MESSAGE);
					
					if(opt != JOptionPane.CANCEL_OPTION && opt != JOptionPane.CLOSED_OPTION){
						if(true == SwingUIPanel.pofManager.loadAllDataFromFile(fileName)){
							if(opt == JOptionPane.YES_OPTION){
								if(false == SwingUIPanel.pofManager.sendAllOFMessagesBasedOnDatabase()){
									GUITools.messageDialog(this, "Ooops, send OFMessage failed, please check log.");
								}else{
									GUITools.messageDialog(this, "Successfully Load all data from \n" + fileName + " \nand Sent all OFMessage to switches.");
								}
							}else{
								GUITools.messageDialog(this, "Successfully Load all data from \n" + fileName + ".");
							}
						}
						
						HomeMain.reloadUI();
					}
				}
			}
		}
	}
	
	protected void changeFullScreen(){
		JFrame win = (JFrame) SwingUtilities.getWindowAncestor(menuPanel);
		if(null != win){
			if (HomeMain.fullScreen == false) {
				win.dispose();
				win.setUndecorated(true);
				win.setVisible(true);

				win.setExtendedState(JFrame.MAXIMIZED_BOTH);

				HomeMain.fullScreen = true;
				
				max_button.setIcon(maxScreen);
			} else {
				win.dispose();
				win.setUndecorated(false);

				win.setVisible(true);

				HomeMain.fullScreen = false;
				
				max_button.setIcon(fullScreen);
			}
			setVisible(false);
		}
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
//		g2d.drawImage(imgBG, 10, 49, 140 , 26*4,null);
		g2d.drawImage(imgBGArr, 30, 40, 12 , 9,null);
		
		save_button.setBounds(10, 49, 82, 26);
		load_button.setBounds(10, 75, 82, 25);
		max_button.setBounds(10, 100, 82, 25);
		exit_button.setBounds(10, 125, 82, 26);
	}

	public void doLayout() {
		super.doLayout();
	}
	
	
}