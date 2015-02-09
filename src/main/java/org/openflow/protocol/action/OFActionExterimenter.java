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

package org.openflow.protocol.action;


import org.jboss.netty.buffer.ChannelBuffer;

/**
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class OFActionExterimenter extends OFAction {
    public static int MINIMUM_LENGTH = OFAction.MINIMUM_LENGTH + 8;

    protected int exterimenter;

    public OFActionExterimenter() {
        super();
        super.setType(OFActionType.EXPERIMENTER);
        super.setLength((short) MINIMUM_LENGTH);
    }

    /**
     * @return the vendor
     */
    public int getExterimenter() {
        return exterimenter;
    }

    /**
     * @param exterimenter the exterimenter to set
     */
    public void setExterimenter(int exterimenter) {
        this.exterimenter = exterimenter;
    }

    @Override
    public void readFrom(ChannelBuffer data) {
        super.readFrom(data);
        this.exterimenter = data.readInt();
    }

    @Override
    public void writeTo(ChannelBuffer data) {
        super.writeTo(data);
        data.writeInt(this.exterimenter);
    }

    @Override
    public int hashCode() {
        final int prime = 379;
        int result = super.hashCode();
        result = prime * result + exterimenter;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof OFActionExterimenter)) {
            return false;
        }
        OFActionExterimenter other = (OFActionExterimenter) obj;
        if (exterimenter != other.exterimenter) {
            return false;
        }
        return true;
    }
}
