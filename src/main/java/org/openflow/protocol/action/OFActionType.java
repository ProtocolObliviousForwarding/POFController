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

/**
 * Modified by Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 * Modified Based on POF white paper.
 *      Original OFActionType include:
 *          OUTPUT,
 *          SET_VLAN_ID,
 *          SET_VLAN_PCP,
 *          STRIP_VLAN,
 *          SET_DL_SRC,
 *          SET_DL_DST,
 *          SET_NW_SRC,
 *          SET_NW_DST,
 *          SET_NW_TOS,
 *          SET_TP_SRC,
 *          SET_TP_DST,
 *          OPAQUE_ENQUEUE,
 *          VENDOR
 *          
 *      Based on POF white paper, OFActionType include:
 *          OUTPUT,
 *          SET_FIELD,
 *          SET_FIELD_FROM_METADATA,
 *          MODIFY_FIELD,
 *          ADD_FIELD,
 *          DELETE_FIELD,
 *          CALCULATE_CHECKSUM
 *          GROUP,
 *          DROP,
 *          PACKET_IN,
 *          COUNTER,
 *          EXPERMENTER
 *  
 */

import java.lang.reflect.Constructor;

import org.openflow.protocol.Instantiable;

/**
 * List of OpenFlow Action types and mappings to wire protocol value and
 * derived classes
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public enum OFActionType {
    OUTPUT              (0, OFActionOutput.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionOutput();
                            }}),
    SET_FIELD	        (1, OFActionSetField.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionSetField();
                            }}),
    SET_FIELD_FROM_METADATA        (2, OFActionSetFieldFromMetadata.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionSetFieldFromMetadata();
                            }}),
    MODIFY_FIELD        (3, OFActionModifyField.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionModifyField();
                            }}),
    ADD_FIELD             (4, OFActionAddField.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionAddField();
                            }}),
    DELETE_FIELD          (5, OFActionDeleteField.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionDeleteField();
                            }}),
    CALCULATE_CHECKSUM  (6, OFActionCalculateCheckSum.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionCalculateCheckSum();
                            }}),
    GROUP               (7, OFActionGroup.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionGroup();
                            }}),
    DROP                (8, OFActionDrop.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionDrop();
                            }}),
    PACKET_IN           (9, OFActionPacketIn.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionPacketIn();
                            }}), 
    COUNTER             (10, OFActionCounter.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionCounter();
                            }}),
    EXPERIMENTER        (0xffff, OFActionExterimenter.class, new Instantiable<OFAction>() {
                            @Override
                            public OFAction instantiate() {
                                return new OFActionExterimenter();
                            }});

    protected static OFActionType[] mapping;

    protected Class<? extends OFAction> clazz;
    protected Constructor<? extends OFAction> constructor;
    protected Instantiable<OFAction> instantiable;
    protected int minLen;
    protected short type;

    /**
     * Store some information about the OpenFlow Action type, including wire
     * protocol type number, length, and derrived class
     *
     * @param type Wire protocol number associated with this OFType
     * @param clazz The Java class corresponding to this type of OpenFlow Action
     * @param instantiable the instantiable for the OFAction this type represents
     */
    OFActionType(int type, Class<? extends OFAction> clazz, Instantiable<OFAction> instantiable) {
        this.type = (short) type;
        this.clazz = clazz;
        this.instantiable = instantiable;
        try {
            this.constructor = clazz.getConstructor(new Class[]{});
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failure getting constructor for class: " + clazz, e);
        }
        OFActionType.addMapping(this.type, this);
    }

    /**
     * Adds a mapping from type value to OFActionType enum
     *
     * @param i OpenFlow wire protocol Action type value
     * @param t type
     */
    static public void addMapping(short i, OFActionType t) {
        if (mapping == null)
            mapping = new OFActionType[16];
        // bring higher mappings down to the edge of our array
        if (i < 0)
            i = (short) (16 + i);
        OFActionType.mapping[i] = t;
    }

    /**
     * Given a wire protocol OpenFlow type number, return the OFType associated
     * with it
     *
     * @param i wire protocol number
     * @return OFType enum type
     */

    static public OFActionType valueOf(short i) {
        if (i < 0)
            i = (short) (16+i);
        return OFActionType.mapping[i];
    }

    /**
     * @return Returns the wire protocol value corresponding to this
     *         OFActionType
     */
    public short getTypeValue() {
        return this.type;
    }

    /**
     * @return return the OFAction subclass corresponding to this OFActionType
     */
    public Class<? extends OFAction> toClass() {
        return clazz;
    }

    /**
     * Returns the no-argument Constructor of the implementation class for
     * this OFActionType
     * @return the constructor
     */
    public Constructor<? extends OFAction> getConstructor() {
        return constructor;
    }

    /**
     * Returns a new instance of the OFAction represented by this OFActionType
     * @return the new object
     */
    public OFAction newInstance() {
        return instantiable.instantiate();
    }

    /**
     * @return the instantiable
     */
    public Instantiable<OFAction> getInstantiable() {
        return instantiable;
    }

    /**
     * @param instantiable the instantiable to set
     */
    public void setInstantiable(Instantiable<OFAction> instantiable) {
        this.instantiable = instantiable;
    }
}
