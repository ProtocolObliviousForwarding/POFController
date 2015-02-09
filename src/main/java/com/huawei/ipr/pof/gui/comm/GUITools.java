/**
 * Copyright (c) 2012, 2013, Huawei Technologies Co., Ltd.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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

package com.huawei.ipr.pof.gui.comm;

import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 * 
 */
public class GUITools {
	public enum EDIT_STATUS{
		ES_ADDING,
		ES_MODIFYING,
		ES_READONLY
	}
	
	public static final String RE_HEX = "^[0-9a-fA-F]+$";
	public static final String RE_0xHEX = "^[0][x|X][0-9a-fA-F]+$";
	public static final String RE_DEC = "^[0-9]+$";
	public static final String RE_PMDEC = "^[-]?[0-9]+$";
	
	public static String TERMINATE_EDIT_ON_FOCUS_LOST = "terminateEditOnFocusLost";
	
	protected static Logger log = LoggerFactory.getLogger(GUITools.class);

	/**
	 * parse the hex string text to hex bytes.
	 * 
	 * @param valueString
	 *            the hex string
	 * @return hex byte array
	 */
	private static byte[] parseTextToHexBytes(String valueString) {
		if (null == valueString) {
			return null;
		}

		if ((valueString.length() % 2) != 0) {
			valueString = valueString + "0";
		}

		byte[] valueBytes = new byte[valueString.length() / 2];
		for (int index = 0; index < valueBytes.length; index++) {
			valueBytes[index] = (byte) Short.parseShort(valueString.substring(index * 2, index * 2 + 2).trim(), 16);
		}

		return valueBytes;
	}
	
	/**
	 * get a hex mask string from bit length
	 * used for generate mask from length automatically
	 * @param bitLength
	 *            the hex string
	 * @return hex string<br>
	 * 				f(0) = "0" <br>
	 * 				f(1) = "8" <br>
	 * 				f(2) = "c" <br>
	 *				f(3) = "e" <br>
	 *  			f(4) = "f" <br>
	 *  			f(8) = "ff" <br>
	 *  			f(31) = "fffffffe" <br>
	 *  			f(32) = "ffffffff" <br>
	 *  			f(33) = "ffffffff8"
	 */
	public static String getMaskStringFromBitLength(int bitLength){
		if(bitLength == 0){
			return "0";
		}
		String result = "";
		if(bitLength >= Integer.SIZE){
			String f32 = "ffffffff";
			int f32Num = bitLength / Integer.SIZE;
			for(int i = 0; i < f32Num; i++){
				result += f32;
			}
		}
		
		bitLength = bitLength % Integer.SIZE;
		
		if(bitLength == 0){
			return result;
		}
		
		int value32;
		value32 = (-1) << (3 - ((bitLength-1) % 4));

		String value32Result = Integer.toHexString( value32 );
		result += value32Result.substring(value32Result.length() - 1 - (bitLength-1)/4);
		return result;
	}

	/**
	 * parse the hex string text to hex byte[]
	 * 
	 * @param swingParentComponent
	 * @param textName
	 * @param valueString
	 * @param maxBitLength
	 * @return hex byte[]
	 */
	public static byte[] parseTextToHexBytes(Component swingParentComponent, String textName, String valueString, int maxBitLength) {
		if (null == valueString || 0 == maxBitLength) {
			return null;
		}

		if (valueString.length() > 1 + (maxBitLength - 1) / 4) {
			messageDialog(swingParentComponent, textName + " length incorrect! MAX bit length is " + maxBitLength);
			return null;
		}

		if (!valueString.matches(RE_HEX)) {
			return null;
		}

		return parseTextToHexBytes(valueString);
	}

	/**
	 * remove the tail zeros of the string
	 * 
	 * @param valueString
	 * @return a new string that not end with zero
	 */
	public static String removeTailZeros(String valueString) {
		int i = valueString.length();
		for (; i > 0; i--) {
			if (false == valueString.substring(i - 1, i).equals("0")) {
				break;
			}
		}
		return valueString.substring(i);
	}
	
	public static void removeAllListeners(JButton button) {
		ActionListener[] als = button.getActionListeners();
		if (null != als && 0 != als.length) {
			for (ActionListener l : als) {
				button.removeActionListener(l);
			}
		}
	}

	public static void messageDialog(Component parentComponent, Object message) {
		if(parentComponent != null){
			JOptionPane.showMessageDialog(parentComponent, message);
		}else{
			System.err.println(message);
		}
	}

	public static int confirmDialog(Component parentComponent, Object message, String title, int optionType, int messageType) {
		return JOptionPane.showConfirmDialog(parentComponent, message, title, optionType, messageType);
	}
	
	public static void logInfo(Object obj, String methodName, String string){
		log.info(obj.getClass().getSimpleName() + "." + methodName + ": " + string);
	}
	
	public static void logError(Object obj, String methodName, String string){
		log.error(obj.getClass().getSimpleName() + "." + methodName + ": " + string);
	}
	
	public static void logDebug(Object obj, String methodName, String string){
		log.debug(obj.getClass().getSimpleName() + "." + methodName+ ": " + string);
	}
}
