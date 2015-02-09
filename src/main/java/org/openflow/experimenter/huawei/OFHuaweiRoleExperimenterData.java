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

/**
 * Class that represents the vendor data in the role request
 * extension implemented by Open vSwitch to support high availability.
 * 
 * @author Rob Vaterlaus (rob.vaterlaus@bigswitch.com)
 */
public class OFHuaweiRoleExperimenterData extends OFHuaweiExperimenterData {
    
    /**
     * Role value indicating that the controller is in the OTHER role.
     */
    public static final int NX_ROLE_OTHER = 0;
    
    /**
     * Role value indicating that the controller is in the MASTER role.
     */
    public static final int NX_ROLE_MASTER = 1;
    
    /**
     * Role value indicating that the controller is in the SLAVE role.
     */
    public static final int NX_ROLE_SLAVE = 2;

    protected int role;

    /** 
     * Construct an uninitialized OFRoleVendorData
     */
    public OFHuaweiRoleExperimenterData() {
        super();
    }
    
    /**
     * Construct an OFRoleVendorData with the specified data type
     * (i.e. either request or reply) and an unspecified role.
     * @param dataType
     */
    public OFHuaweiRoleExperimenterData(int dataType) {
        super(dataType);
    }
    
    /**
     * Construct an OFRoleVendorData with the specified data type
     * (i.e. either request or reply) and role (i.e. one of of
     * master, slave, or other).
     * @param dataType either role request or role reply data type
     */
    public OFHuaweiRoleExperimenterData(int dataType, int role) {
        super(dataType);
        this.role = role;
    }
    /**
     * @return the role value of the role vendor data
     */
    public int getRole() {
        return role;
    }
    
    /**
     * @param role the role value of the role vendor data
     */
    public void setRole(int role) {
        this.role = role;
    }

    /**
     * @return the total length of the role vendor data
     */
    @Override
    public int getLength() {
        return super.getLength() + 4;
    }
    
    /**
     * Read the role vendor data from the ChannelBuffer
     * @param data the channel buffer from which we're deserializing
     * @param length the length to the end of the enclosing message
     */
    public void readFrom(ChannelBuffer data, int length) {
        super.readFrom(data, length);
        role = data.readInt();
    }

    /**
     * Write the role vendor data to the ChannelBuffer
     * @param data the channel buffer to which we're serializing
     */
    public void writeTo(ChannelBuffer data) {
        super.writeTo(data);
        data.writeInt(role);
    }
}
