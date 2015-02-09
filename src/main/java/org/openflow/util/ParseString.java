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
package org.openflow.util;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.jboss.netty.buffer.ChannelBuffer;
import org.openflow.protocol.OFGlobal;

/**
 * Convert String-to-byte and byte-to-String methods
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 */
public class ParseString {
    /**
     * Read the name ascii byte[] from channel and convert to String.
     * @param data  current channel buffer
     * @return name string
     */
    public static String NameByteToString(ChannelBuffer data){
        byte[] name = new byte[OFGlobal.OFP_NAME_MAX_LENGTH];
        data.readBytes(name);
        return ParseString.ByteToString(name);
    }
    
    /**
     * Convert the ascii byte[] to String. Could be used for reading a String from channel
     * @param byteString
     * @return string
     */
    public static String ByteToString(byte[] byteString){
        int index = 0;
        for (byte b : byteString) {
            if (0 == b)
                break;
            ++index;
        }

        return new String(Arrays.copyOf(byteString, index),
                            Charset.forName("ascii"));
    }
    
    /**
     * Convert a name string to ascii bytes. Could be used for writing a name string to channel
     * @param string
     * @return byte[] array
     */
    public static byte[] NameStringToBytes(String string){
        return StringToBytes(string, OFGlobal.OFP_NAME_MAX_LENGTH);
    }
    
    /**
     * Convert a String to ascii bytes. Could be used for writing a string to channel.
     * @param string
     * @param maxLength
     * @return byte[] array
     */
    public static byte[] StringToBytes(String string, int maxLength){
        byte[] returnBytes = new byte[maxLength];
        try {
             byte[] byteString = string.getBytes("ASCII");
            if (byteString.length < maxLength) {
                System.arraycopy(byteString, 0, returnBytes, 0, byteString.length);
            } else {
                System.arraycopy(byteString, 0, returnBytes, 0, maxLength - 1);
                returnBytes[maxLength - 1] = 0;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return returnBytes;
    }
    
}
