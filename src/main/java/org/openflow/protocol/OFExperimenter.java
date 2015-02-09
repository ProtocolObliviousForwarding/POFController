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

package org.openflow.protocol;

import org.jboss.netty.buffer.ChannelBuffer;
import org.openflow.util.U16;
import org.openflow.protocol.experimenter.OFExperimenterData;
import org.openflow.protocol.factory.OFExperimenterDataFactory;
import org.openflow.protocol.factory.OFExperimenterDataFactoryAware;

/**
 * Represents ofp_vendor_header
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class OFExperimenter extends OFMessage implements OFExperimenterDataFactoryAware {
    public static int MINIMUM_LENGTH = 12;

    protected int experimenter;
    protected OFExperimenterData experimenterData;
    protected OFExperimenterDataFactory experimenterDataFactory;

    public OFExperimenter() {
        super();
        this.type = OFType.EXPERIMENTER;
        this.length = U16.t(MINIMUM_LENGTH);
    }

    /**
     * @return the vendor
     */
    public int getExperimenter() {
        return experimenter;
    }

    /**
     * @param experimenter the experimenter to set
     */
    public void setExperimenter(int experimenter) {
        this.experimenter = experimenter;
    }

    /**
     * @return the data
     */
    public OFExperimenterData getExperimenterData() {
        return experimenterData;
    }

    /**
     * @param experimenterDataData the data to set
     */
    public void setExperimenterData(OFExperimenterData experimenterDataData) {
        this.experimenterData = experimenterDataData;
    }

    @Override
    public void setExperimenterDataFactory(OFExperimenterDataFactory experimenterDataFactory) {
        this.experimenterDataFactory = experimenterDataFactory;
    }
      
    @Override
    public void readFrom(ChannelBuffer data) {
        super.readFrom(data);
        this.experimenter = data.readInt();
        if (experimenterDataFactory == null)
            throw new RuntimeException("OFVendorDataFactory not set");
            
        this.experimenterData = experimenterDataFactory.parseExperimenterData(experimenter,
                data, super.getLengthU() - MINIMUM_LENGTH);
    }

    @Override
    public void writeTo(ChannelBuffer data) {
        super.writeTo(data);
        data.writeInt(this.experimenter);
        if (experimenterData != null)
            experimenterData.writeTo(data);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 337;
        int result = super.hashCode();
        result = prime * result + experimenter;
        if (experimenterData != null)
            result = prime * result + experimenterData.hashCode();
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        OFExperimenter other = (OFExperimenter) obj;
        if (experimenter != other.experimenter)
            return false;
        if (experimenterData == null) {
            if (other.experimenterData != null) {
                return false;
            }
        } else if (!experimenterData.equals(other.experimenterData)) {
            return false;
        }
        return true;
    }
}
