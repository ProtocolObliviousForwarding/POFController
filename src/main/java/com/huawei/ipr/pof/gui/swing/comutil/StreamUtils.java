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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/** 
 * @author Chai Zhi, Huawei Technologies Co., Ltd. 
 */
public class StreamUtils {

	private static ClassLoader CLASSLOADER = StreamUtils.class.getClassLoader();

	private static URL CLASSPATH_ROOT_PATH_URL = Thread.currentThread().getContextClassLoader().getResource("");
	private static String CLASSPATH_ROOT_PATH;

	private static String USER_DIR = System.getProperty("user.dir").replaceAll("\\\\", "/");

	public static final String FILE_PROTOCOL_START = "file:";
	public static final String FILE_PROTOCOL = FILE_PROTOCOL_START + File.separator;

	static {
		if (CLASSPATH_ROOT_PATH_URL != null) {
			String classPathAbsolutePath = CLASSPATH_ROOT_PATH_URL.getPath();
			
			if (classPathAbsolutePath.startsWith("/")) {
				classPathAbsolutePath = classPathAbsolutePath.substring(1);
			}
			if (classPathAbsolutePath.endsWith("/")) {
				classPathAbsolutePath = classPathAbsolutePath.substring(0, classPathAbsolutePath.length() - 1);
			}
			
			int _index = classPathAbsolutePath.lastIndexOf("/");
			CLASSPATH_ROOT_PATH = classPathAbsolutePath.substring(0, _index);
		}
	}

	/**
	 * 
	 * @param relativePath
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static InputStream getRelativePathStream(String relativePath) throws MalformedURLException, IOException {
		URL url = getRelativePathURL(relativePath);
		if (url == null)
			return null;
		return url.openStream();
	}

	/**
	 * 
	 * @param relativePath
	 * @throws MalformedURLException
	 */
	public static URL getRelativePathURL(String relativePath) throws MalformedURLException {
		if (!relativePath.contains("../")) {
			return CLASSLOADER.getResource(relativePath);
		} else {
			relativePath = relativePath.substring(2);
			if (CLASSPATH_ROOT_PATH == null) {
				String resourceAbsolutePath = "file:///" + USER_DIR + relativePath;
				return new URL(resourceAbsolutePath);
			} else {
				String resourceAbsolutePath = "file:///" + CLASSPATH_ROOT_PATH + relativePath;
				return new URL(resourceAbsolutePath);
			}
		}
	}

	/**
	 * 
	 * @param relativePath
	 * @throws MalformedURLException
	 */
	public static String getRelativeFileName(String relativePath) throws MalformedURLException {
		URL fileURL = getRelativePathURL(relativePath);
		String fileName = fileURL.getFile();
		if (fileName.startsWith("/")) {
			fileName.substring(1);
		}
		return fileName;
	}

	/**
	 * 
	 * @param urlstr
	 * @throws IOException
	 */
	public static InputStream getAbsolutePathStream(String urlstr) throws IOException {
		URL url = new URL(urlstr);
		return url.openStream();
	}

	/**
	 * 
	 * @param path
	 * @throws IOException
	 */
	public static InputStream getPathStream(String path) throws IOException {
		if (!"".equals(path)) {
			if (path.length() > 7) {
				String subStr = path.substring(0, 6);
				if (subStr.contains(":")) {
					return getAbsolutePathStream(path);
				} else {
					return getRelativePathStream(path);
				}
			} else {
				return getRelativePathStream(path);
			}
		} else {
			return null;
		}
	}

	public static void copyFile(String oldPath, String newPath) throws IOException {
		int byteread = 0;
		File oldfile = new File(oldPath);
		InputStream inStream = null;
		FileOutputStream outStream = null;
		if (oldfile.exists()) {
			try {
				inStream = new FileInputStream(oldPath);
				outStream = new FileOutputStream(newPath);
				byte[] buffer = new byte[1024];
				while ((byteread = inStream.read(buffer)) != -1) {
					outStream.write(buffer, 0, byteread);
				}
			} catch (IOException ioe) {
                throw ioe;
			} finally {
				try {
					if (inStream != null)
						inStream.close();
					if (outStream != null)
						outStream.close();
				} catch (IOException e) {
					  throw e;
				}
			}
		}
	}
}
