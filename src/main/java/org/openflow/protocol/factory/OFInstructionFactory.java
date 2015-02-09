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

package org.openflow.protocol.factory;

import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.openflow.protocol.instruction.OFInstruction;
import org.openflow.protocol.instruction.OFInstructionType;

/**
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 *
 */
public interface OFInstructionFactory {
    /**
     * Retrieves an OFInstruction instance corresponding to the specified
     * OFInstructionType
     * @param t the type of the OFInstruction to be retrieved
     * @return an OFInstruction instance
     */
    public OFInstruction getInstruction(OFInstructionType t);
    
    /**
     * Attempts to parse and return all OFInstructions contained in the given
     * ByteBuffer, beginning at the ByteBuffer's position, and ending at
     * position+length.
     * @param data the ChannelBuffer to parse for OpenFlow actions
     * @param length the number of Bytes to examine for OpenFlow instructions
     * @return a list of OFInstruction instances
     */
    public List<OFInstruction> parseInstructions(ChannelBuffer data, int length);
    
    /**
     * Attempts to parse and return all OFInstructions contained in the given
     * ByteBuffer, beginning at the ByteBuffer's position, and ending at
     * position+length.
     * @param data the ChannelBuffer to parse for OpenFlow instructions
     * @param length the number of Bytes to examine for OpenFlow instructions
     * @param limit the maximum number of messages to return, 0 means no limit
     * @return a list of OFInstruction instances
     */
    public List<OFInstruction> parseInstructions(ChannelBuffer data, int length, int limit);

}
