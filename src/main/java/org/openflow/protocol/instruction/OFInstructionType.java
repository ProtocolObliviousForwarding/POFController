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

package org.openflow.protocol.instruction;

import java.lang.reflect.Constructor;

import org.openflow.protocol.Instantiable;

/**
 * List of OpenFlow with POF Instruction types and mappings to wire protocol value and
 * derived classes
 * 
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 */
public enum OFInstructionType {
    GOTO_TABLE                  (1, OFInstructionGotoTable.class, new Instantiable<OFInstruction>() {
                                        @Override
                                        public OFInstruction instantiate() {
                                            return new OFInstructionGotoTable();
                                        }}),
    WRITE_METADATA              (2, OFInstructionWriteMetadata.class, new Instantiable<OFInstruction>() {
                                        @Override
                                        public OFInstruction instantiate() {
                                            return new OFInstructionWriteMetadata();
                                        }}),
    WRITE_ACTIONS               (3, OFInstructionWriteActions.class, new Instantiable<OFInstruction>() {
                                        @Override
                                        public OFInstruction instantiate() {
                                            return new OFInstructionWriteActions();
                                        }}),
    APPLY_ACTIONS               (4, OFInstructionApplyActions.class, new Instantiable<OFInstruction>() {
                                        @Override
                                        public OFInstruction instantiate() {
                                            return new OFInstructionApplyActions();
                                        }}),
    CLEAR_ACTIONS               (5, OFInstructionClearActions.class, new Instantiable<OFInstruction>() {
                                        @Override
                                        public OFInstruction instantiate() {
                                            return new OFInstructionClearActions();
                                        }}),
    METER                       (6, OFInstructionMeter.class, new Instantiable<OFInstruction>() {
                                        @Override
                                        public OFInstruction instantiate() {
                                            return new OFInstructionMeter();
                                        }}),
    WRITE_METADATA_FROM_PACKET  (7, OFInstructionWriteMetadataFromPacket.class, new Instantiable<OFInstruction>() {
                                        @Override
                                        public OFInstruction instantiate() {
                                            return new OFInstructionWriteMetadataFromPacket();
                                        }}),
    GOTO_DIRECT_TABLE           (8, OFInstructionGotoDirectTable.class, new Instantiable<OFInstruction>() {
                                            @Override
                                            public OFInstruction instantiate() {
                                                return new OFInstructionGotoDirectTable();
                                            }}),    
    EXPERIMENTER                (0xffff, OFInstructionExperimenter.class, new Instantiable<OFInstruction>() {
                                        @Override
                                        public OFInstruction instantiate() {
                                            return new OFInstructionExperimenter();
                                        }});
    
    
    protected static OFInstructionType[] mapping;

    protected Class<? extends OFInstruction> clazz;
    protected Constructor<? extends OFInstruction> constructor;
    protected Instantiable<OFInstruction> instantiable;
    protected short type;
    
    /**
     * Store some information about the OpenFlow Instruction type, including wire
     * protocol type number, length, and class
     *
     * @param type Wire protocol number associated with this OFInstruction
     * @param clazz The Java class corresponding to this type of OpenFlow Instruction
     * @param instantiable the instantiable for the OFInstruction this type represents
     */
    OFInstructionType(int type, Class<? extends OFInstruction> clazz, Instantiable<OFInstruction> instantiable) {
        this.type = (short) type;
        this.clazz = clazz;
        this.instantiable = instantiable;
        try {
            this.constructor = clazz.getConstructor(new Class[]{});
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failure getting constructor for class: " + clazz, e);
        }
        OFInstructionType.addMapping(this.type, this);
    }

    /**
     * Adds a mapping from type value to OFInstructionType enum
     *
     * @param i OpenFlow wire protocol Instruction type value
     * @param t type
     */
    static public void addMapping(short i, OFInstructionType t) {
        if (mapping == null)
            mapping = new OFInstructionType[16];
        // bring higher mappings down to the edge of our array
        if (i < 0)
            i = (short) (16 + i);
        OFInstructionType.mapping[i] = t;
    }

    /**
     * Given a wire protocol OpenFlow type number, return the OFInstructionType associated
     * with it
     *
     * @param i wire protocol number
     * @return OFInstructionType enum type
     */

    static public OFInstructionType valueOf(short i) {
        if (i < 0)
            i = (short) (16+i);
        return OFInstructionType.mapping[i];
    }

    /**
     * @return Returns the wire protocol value corresponding to this
     *         OFInstructionType
     */
    public short getTypeValue() {
        return this.type;
    }

    /**
     * @return return the OFInstruction subclass corresponding to this OFInstructionType
     */
    public Class<? extends OFInstruction> toClass() {
        return clazz;
    }

    /**
     * Returns the no-argument Constructor of the implementation class for
     * this OFInstructionType
     * @return the constructor
     */
    public Constructor<? extends OFInstruction> getConstructor() {
        return constructor;
    }

    /**
     * Returns a new instance of the OFInstruction represented by this OFInstructionType
     * @return the new object
     */
    public OFInstruction newInstance() {
        return instantiable.instantiate();
    }

    /**
     * @return the instantiable
     */
    public Instantiable<OFInstruction> getInstantiable() {
        return instantiable;
    }

    /**
     * @param instantiable the instantiable to set
     */
    public void setInstantiable(Instantiable<OFInstruction> instantiable) {
        this.instantiable = instantiable;
    }
}
