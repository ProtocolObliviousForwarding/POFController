/**
*    Copyright 2011, Big Switch Networks, Inc. 
*    Originally created by David Erickson & Rob Sherwood, Stanford University
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

package org.openflow.experimenter.nicira;

import org.openflow.protocol.Instantiable;
import org.openflow.protocol.experimenter.OFExperimenterData;

/**
 * Subclass of OFVendorData representing the vendor data associated with
 * a role reply vendor extension.
 * 
 * @author Rob Vaterlaus (rob.vaterlaus@bigswitch.com)
 */
public class OFNiciraRoleReplyExperimenterData extends OFNiciraRoleExperimenterData {

    protected static Instantiable<OFExperimenterData> instantiable =
            new Instantiable<OFExperimenterData>() {
                public OFExperimenterData instantiate() {
                    return new OFNiciraRoleReplyExperimenterData();
                }
            };

    /**
     * @return a subclass of Instantiable<OFVendorData> that instantiates
     *         an instance of OFRoleReplyVendorData.
     */
    public static Instantiable<OFExperimenterData> getInstantiable() {
        return instantiable;
    }

    /**
     * The data type value for a role reply
     */
    public static final int NXT_ROLE_REPLY = 11;

    /**
     * Construct a role reply vendor data with an unspecified role value.
     */
    public OFNiciraRoleReplyExperimenterData() {
        super(NXT_ROLE_REPLY);
    }
    
    /**
     * Construct a role reply vendor data with the specified role value.
     * @param role the role value for the role reply. Should be one of
     *      NX_ROLE_OTHER, NX_ROLE_MASTER or NX_ROLE_SLAVE.
     */
    public OFNiciraRoleReplyExperimenterData(int role) {
        super(NXT_ROLE_REPLY, role);
    }
}
