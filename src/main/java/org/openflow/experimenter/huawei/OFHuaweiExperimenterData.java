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

package org.openflow.experimenter.huawei;

import org.jboss.netty.buffer.ChannelBuffer;
import org.openflow.protocol.experimenter.OFExperimenterData;

/**
 * Base class for vendor data corresponding to a Huawei vendor extension.
 * Huawei vendor data always starts with a 4-byte integer data type value.
 * 
 * @author Song Jian (jack.songjian@huawei.com)
 */
public class OFHuaweiExperimenterData implements OFExperimenterData {

    public static final int NX_EXPERIMENTER_ID = 0x00004877;
    /**
     * The value of the integer data type at the beginning of the vendor data
     */
    protected int dataType;
    
    /**
     * Construct empty (i.e. unspecified data type) Huawei vendor data.
     */
    public OFHuaweiExperimenterData() {
    }
    
    /**
     * Contruct Huawei vendor data with the specified data type
     * @param dataType the data type value at the beginning of the vendor data.
     */
    public OFHuaweiExperimenterData(int dataType) {
        this.dataType = dataType;
    }
    
    /**
     * Get the data type value at the beginning of the vendor data
     * @return the integer data type value
     */
    public int getDataType() {
        return dataType;
    }
    
    /**
     * Set the data type value
     * @param dataType the integer data type value at the beginning of the
     *     vendor data.
     */
    public void setDataType(int dataType) {
        this.dataType = dataType;
    }
    
    /**
     * Get the length of the vendor data. This implementation will normally
     * be the superclass for another class that will override this to return
     * the overall vendor data length. This implementation just returns the 
     * length of the part that includes the 4-byte integer data type value
     * at the beginning of the vendor data.
     */
    @Override
    public int getLength() {
        return 4;
    }

    /**
     * Read the vendor data from the ChannelBuffer
     * @param data the channel buffer from which we're deserializing
     * @param length the length to the end of the enclosing message
     */
    @Override
    public void readFrom(ChannelBuffer data, int length) {
        dataType = data.readInt();
    }

    /**
     * Write the vendor data to the ChannelBuffer
     * @param data the channel buffer to which we're serializing
     */
    @Override
    public void writeTo(ChannelBuffer data) {
        data.writeInt(dataType);
    }
}
