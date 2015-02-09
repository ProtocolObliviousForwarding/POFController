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

package org.openflow.protocol;

/**
 * Define global constant values
 * 
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd. *
 */
public class OFGlobal {    
    /**
     * Define invalid value. {@value}.
     */
    public static final int OFP_INVALID_VALUE = 0xFFFFFFFF;
    
    /**
     * Define the length of device name. {@value}.
     */
    public static final int OFP_NAME_MAX_LENGTH = 64;

    /**
     * Define the max length of error string. {@value}.
     */
    public static final int OFP_ERROR_STRING_MAX_LENGTH = 256;

    /**
     * Define the max length of packetin. {@value}.
     */
    public static final int OFP_PACKET_IN_MAX_LENGTH = 2048;

    /**
     * Define the max length in byte unit of match field. {@value}.
     */
    public static final int OFP_MAX_FIELD_LENGTH_IN_BYTE = 16;

    /**
     * Define the max number of match field in one flow entry. {@value}.
     */
    public static final int OFP_MAX_MATCH_FIELD_NUM = 8;

    /**
     * Define the max instruction number of one flow entry. {@value}.
     */
    public static final int OFP_MAX_INSTRUCTION_NUM = 4;

    /**
     * Define the max field number of one protocol. {@value}.
     */
    public static final int OFP_MAX_PROTOCOL_FIELD_NUM  = 8;

    /**
     * Define the max action number in one instruction. {@value}.
     */
    public static final int OFP_MAX_ACTION_NUMBER_PER_INSTRUCTION = 4;

    /**
     * Define the max action number in one group. {@value}.
     */
    public static final int OFP_MAX_ACTION_NUMBER_PER_GROUP = 4;

    /**
     * Define the max action length in unit of byte. {@value}.
     */
    public static final int OFP_MAX_ACTION_LENGTH = 44;
    
    /**
     * Define the max instruction length in unit of byte. {@value}.
     */
    public static final int OFP_MAX_INSTRUCTION_LENGTH = (8 + 8 + OFP_MAX_ACTION_NUMBER_PER_INSTRUCTION * (OFP_MAX_ACTION_LENGTH + 4));

}
