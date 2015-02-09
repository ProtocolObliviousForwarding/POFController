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

package com.huawei.ipr.pof.gui.swing.comutil;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 */
public class SwingUtils {

	public static Insets DefaultWindowInsets = new Insets(0, 0, 0, 0);
	
	public static final Rectangle getScrollPaneViewportSize(JScrollPane panel) {
		Rectangle viewRect = panel.getViewportBorderBounds();
		JScrollBar vbar = panel.getVerticalScrollBar();
		if (vbar.isVisible()) {
			viewRect.width = viewRect.width + vbar.getWidth();
		}
		JScrollBar hbar = panel.getHorizontalScrollBar();
		if (hbar.isVisible()) {
			viewRect.height = viewRect.height + hbar.getHeight();
		}
		return viewRect;
	}
	
	public static void centerWindow(Component window) {
		if (window == null) {
			return;
		}
		Dimension screenSize = window.getToolkit().getScreenSize();
		Dimension mySize = window.getSize();
		GraphicsConfiguration config = window.getGraphicsConfiguration();
		Insets screenInsets;
		if (config == null) {
			screenInsets = window.getToolkit().getScreenInsets(config);
		} else {
			screenInsets = DefaultWindowInsets;
		}
		int blankHeight = screenSize.height - screenInsets.top - screenInsets.bottom;
		int blankWidth = screenSize.width - screenInsets.left - screenInsets.right;
		int y = (blankHeight - mySize.height) / 2 + screenInsets.top;
		int x = (blankWidth - mySize.width) / 2 + screenInsets.left;

		window.setLocation(x, y);
	}

	public static void fullScreen(JFrame window) {
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		screenSize.width = screenSize.width * 2/ 3;
		screenSize.height = screenSize.height * 2/ 3;
		window.setMinimumSize(screenSize);
		window.setLocation(screenSize.width * 1 / 6, screenSize.height * 1 / 6);
		window.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
	}

}
