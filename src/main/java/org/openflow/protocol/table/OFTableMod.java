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
package org.openflow.protocol.table;

import org.jboss.netty.buffer.ChannelBuffer;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;
import org.openflow.util.U16;

/**
 * OFTableMod<br>
 * {<br>
 *      {@link OFMessage} header;<br>
 *      {@link OFFlowTable} flowTable;<br>
 * } //sizeof()= 8 + {@link OFFlowTable#MINIMUM_LENGTH}
 * 
 * @see OFFlowTable
 *      
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 *
 */
public class OFTableMod extends OFMessage implements Cloneable{
    public static final int MINIMUM_LENGTH = OFMessage.MINIMUM_LENGTH + OFFlowTable.MAXIMAL_LENGTH;
    
    protected OFFlowTable flowTable;
    
    public enum OFTableModCmd {
        OFPTC_ADD,
        OFPTC_MODIFY,
        OFPTC_DELETE
    }
    
    public OFTableMod(){
        super();
        this.type = OFType.TABLE_MOD;
        this.length = U16.t(MINIMUM_LENGTH);
    }    

    @Override
    public void readFrom(ChannelBuffer data) {
        super.readFrom(data);
        if(flowTable == null){
            flowTable = new OFFlowTable();
        }
        flowTable.readFrom(data);
    }
    
    @Override
    public void writeTo(ChannelBuffer data) {
        super.writeTo(data);
        if(flowTable == null){
            data.writeZero(OFFlowTable.MAXIMAL_LENGTH);
        }else{
            flowTable.writeTo(data);
        }
    }
    
    public String toBytesString(){
        return super.toBytesString() +
                flowTable.toBytesString();
    }
    
    public String toString(){
        return super.toString() +
                "; TableMod:" +
                flowTable.toString();
    }

    public OFFlowTable getFlowTable() {
        return flowTable;
    }

    public void setFlowTable(OFFlowTable flowTable) {
        this.flowTable = flowTable;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((flowTable == null) ? 0 : flowTable.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        OFTableMod other = (OFTableMod) obj;
        if (flowTable == null) {
            if (other.flowTable != null)
                return false;
        } else if (!flowTable.equals(other.flowTable))
            return false;
        return true;
    }
    
    @Override
    public OFTableMod clone() throws CloneNotSupportedException {
        OFTableMod neoTableMod= (OFTableMod) super.clone();
        if(null != flowTable){
            neoTableMod.setFlowTable(flowTable.clone());
        }

        return neoTableMod;
    }    
}
