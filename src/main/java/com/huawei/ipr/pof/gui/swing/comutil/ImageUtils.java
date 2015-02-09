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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 */
public class ImageUtils {

	private static Map<String, ImageIcon> imagesMap = new HashMap<String, ImageIcon>();// string -->ImageIcon

	/**
	 * from java class url
	 * 
	 * @param imageURL
	 * @throws MalformedURLException
	 */
	public static ImageIcon getImageIcon(String imageURL)  {
		URL url;
		try {
			url = StreamUtils.getRelativePathURL(imageURL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
		return new ImageIcon(url);
	}

	public static synchronized ImageIcon getImageIconByCaches(String imageURL) {
		ImageIcon image = (ImageIcon) imagesMap.get(imageURL);
		if (image == null) {
			URL url = null;
			try {
				url = StreamUtils.getRelativePathURL(imageURL);
			} catch (MalformedURLException e) {
				return null;
			}
			image = new ImageIcon(url);
			imagesMap.put(imageURL, image);
		}
		return image;
	}

	 
}
