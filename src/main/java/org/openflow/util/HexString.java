/**
 *    Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior
 *    University
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 **/

/**
 * Modified by Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 * Add function:
 *      public static String Zero(int length)
 *      public static String ZeroEnd(int length)
 *      public static String ByteZero(int byteLenth)
 *      public static String ByteZeroEnd(int byteLenth)
 *      public static String toHex(byte byteVar)
 *      public static String toHex(short shortVar)
 *      public static String toHex(int intVar)
 *      public static String toHex(long longVar)
 *      public static String toHex(String stringVar, int maxLength)
 *      public static String NameToHex(String stringVar)
 *      public static String toHex(byte[] byteArray)
 *      public static String toHex(byte[] byteArray, int length)
 */

package org.openflow.util;

import java.math.BigInteger;

import org.openflow.protocol.OFGlobal;

public class HexString {
    /**
     * Convert a string of bytes to a ':' separated hex string
     * @param bytes
     * @return "0f:ca:fe:de:ad:be:ef"
     */
    public static String toHexString(byte[] bytes) {
        int i;
        String ret = "";
        String tmp;
        for(i=0; i< bytes.length; i++) {
            if(i> 0)
                ret += ":";
            tmp = Integer.toHexString(U8.f(bytes[i]));
            if (tmp.length() == 1)
                ret += "0";
            ret += tmp; 
        }
        return ret;
    }
    
    public static String toHexString(long val, int padTo) {
        char arr[] = Long.toHexString(val).toCharArray();
        String ret = "";
        // prepend the right number of leading zeros
        int i = 0;
        for (; i < (padTo * 2 - arr.length); i++) {
            ret += "0";
            if ((i % 2) == 1)
                ret += ":";
        }
        for (int j = 0; j < arr.length; j++) {
            ret += arr[j];
            if ((((i + j) % 2) == 1) && (j < (arr.length - 1)))
                ret += ":";
        }
        return ret;        
    }
   
    public static String toHexString(long val) {
        return toHexString(val, 8);
    }
    
    
    /**
     * Convert a string of hex values into a string of bytes
     * @param values "0f:ca:fe:de:ad:be:ef"
     * @return [15, 5 ,2, 5, 17] 
     * @throws NumberFormatException If the string can not be parsed
     */ 
    public static byte[] fromHexString(String values) throws NumberFormatException {
        String[] octets = values.split(":");
        byte[] ret = new byte[octets.length];
        
        for(int i = 0; i < octets.length; i++) {
            if (octets[i].length() > 2)
                throw new NumberFormatException("Invalid octet length");
            ret[i] = Integer.valueOf(octets[i], 16).byteValue();
        }
        return ret;
    }
    
    public static long toLong(String values) throws NumberFormatException {
        // Long.parseLong() can't handle HexStrings with MSB set. Sigh. 
        BigInteger bi = new BigInteger(values.replaceAll(":", ""),16);
        if (bi.bitLength() > 64) 
            throw new NumberFormatException("Input string too big to fit in long: " + values);
        return bi.longValue();
    }
    
    /**
     * Get a "00000..." string.
     * Zero(3) will get "000"
     * @param zeroNumber
     * @return "000000..."
     */
    public static String Zero(int zeroNumber) {
        String ret = "";
        for (int i = 0; i < zeroNumber; i++) {
            ret += "0";
        }

        return ret;
    }
    
    /**
     * Get a "0000..." string and the end with blank space ' '.
     * ZeroEnd(3) will get "000 "
     * @param zeroNumber
     * @return "000000... "
     */
    public static String ZeroEnd(int zeroNumber){
        return Zero(zeroNumber) + " ";
    }
    
    /**
     * Get a "0000..." string, the number of '0' is (2 * byteLength). 
     * (a byte 255's hex value is 0xff, so a byte 0's hex value is 00). 
     * ByteZero(3) will get "000000"
     * @param byteLenth
     * @return "000000..."
     */
    public static String ByteZero(int byteLenth){
        return Zero(2 * byteLenth);
    }
    
    /**
     * Get a "0000... " string, the number of '0' is (2 * byteLength). 
     * (a byte 255's hex value is 0xff, so a byte 0's hex value is 00). 
     * ByteZeroEnd(3) will get "000000 "
     * @param byteLenth
     * @return "000000..."
     */
    public static String ByteZeroEnd(int byteLenth){
        return Zero(2 * byteLenth) + " ";
    }
    
    /**
     * Get a byte's hex value string. 
     * e.g. toHex((byte)254) will get "fe", toHex((byte)3) will get "03". 
     * @param byteVar
     *              byte variable
     * @return a string of the byte variable's hex value
     */
    public static String toHex(byte byteVar){
        return Integer.toHexString((byteVar & 0x000000FF) | 0xFFFFFF00).substring(6);
    }
    
    /**
     * Get a short's hex value string. 
     * e.g. toHex((short)254) will get "00fe". 
     * @param shortVar
     *              short variable
     * @return a string of the short variable's hex value
     */
    public static String toHex(short shortVar){
        return Integer.toHexString((shortVar & 0x0000FFFF) | 0xFFFF0000).substring(4);
    }
    
    /**
     * Get a int's hex value string, and end with ' '. 
     * e.g. toHex(254) will get "000000fe ". 
     * @param intVar
     *              int variable
     * @return a string of the int variable's hex value
     */
    public static String toHex(int intVar){
        char arr[] = Integer.toHexString(intVar).toCharArray();
        String ret = "";
        // prepend the right number of leading zeros
        ret += Zero(8 - arr.length);

        ret += Integer.toHexString(intVar);
        
        ret += " ";
        
        return ret;
    }
    
    /**
     * Get a long's hex value string, and end with ' '. 
     * e.g. toHex((long)254) will get "00000000 000000fe ". 
     * @param longVar
     *              long variable
     * @return a string of the long variable's hex value
     */
    public static String toHex(long longVar){
        return  toHex((int)(longVar >> 32)) + toHex((int)(longVar & 0xFFFFFFFF));
    }
    
    /**
     * Convert a String to hex string
     * @param stringVar
     * @param maxLength
     * @return hex string
     */
    public static String toHex(String stringVar, int maxLength){
        if(null == stringVar){
            return "";
        }
        byte[] byteArray = ParseString.StringToBytes(stringVar, maxLength);
        
        return toHex(byteArray);
    }
    
    /**
     * Convert a name string in open flow message to a hex string
     * @param stringVar
     * @return hex string
     */
    public static String NameToHex(String stringVar){
        if(null == stringVar){
            return "";
        }
        return toHex(stringVar, OFGlobal.OFP_NAME_MAX_LENGTH) + " ";
    }

    /**
     * Convert the byte[] array stream to hex string (without any separation symbol).
     * @param byteArray
     * @return hex string
     */
    public static String toHex(byte[] byteArray) {
        if(null == byteArray
                || 0 == byteArray.length){
            return "";
        }
        
        String ret = "";
        for(byte b : byteArray){
            ret += toHex(b);
        }
        return ret;
    }
    
    /**
     * Convert the byte[] array stream to hex string.
     * @param byteArray
     * @param start 
     *              start index of byteArray
     * @param length
     * @return hex string
     */
    public static String toHex(byte[] byteArray, int start, int length) {
        int arrayLength = byteArray.length;
        if(null == byteArray
                || 0 == arrayLength
                || start < 0 
                || (start + length) > arrayLength){
            return "";
        }
        
        String ret = "";

        for(int i = start; i < (start + length); i++){
            ret += toHex(byteArray[i]);
        }

        return ret;
    }

}
