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

package com.huawei.ipr.pof.gui.swing.com.protocal;

import java.awt.*;
/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 */
@SuppressWarnings("serial")
public class ModifiedFlowLayout extends FlowLayout {

	public ModifiedFlowLayout() {
		super();
	}

	public ModifiedFlowLayout(int align) {
		super(align);
	}

	public ModifiedFlowLayout(int align, int hgap, int vgap) {
		super(align, hgap, vgap);
	}

	public Dimension minimumLayoutSize(Container target) {
		return computeSize(target, false);
	}

	public Dimension preferredLayoutSize(Container target) {
		return computeSize(target, true);
	}

	private Dimension computeSize(Container target, boolean minimum) {
		synchronized (target.getTreeLock()) {
			int hgap = getHgap();
			int vgap = getVgap();
			int w = target.getWidth();

			if (w == 0) {
				w = Integer.MAX_VALUE;
			}

			Insets insets = target.getInsets();
			if (insets == null) {
				insets = new Insets(0, 0, 0, 0);
			}
			int reqdWidth = 0;

			int maxwidth = w - (insets.left + insets.right + hgap * 2);
			int n = target.getComponentCount();
			int x = 0;
			int y = insets.top;
			int rowHeight = 0;

			for (int i = 0; i < n; i++) {
				Component c = target.getComponent(i);
				if (c.isVisible()) {
					Dimension d = minimum ? c.getMinimumSize() : c.getPreferredSize();
					if ((x == 0) || ((x + d.width) <= maxwidth)) {
						if (x > 0) {
							x += hgap;
						}
						x += d.width;
						rowHeight = Math.max(rowHeight, d.height);
					} else {
						x = d.width;
						y += vgap + rowHeight;
						rowHeight = d.height;
					}
					reqdWidth = Math.max(reqdWidth, x);
				}
			}
			y += rowHeight;
			return new Dimension(reqdWidth + insets.left + insets.right, y);
		}
	}
}
