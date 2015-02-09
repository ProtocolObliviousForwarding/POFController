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

import org.jboss.netty.buffer.ChannelBuffer;

/** * 
 * Represents an ofp_counter_mod message
 * 
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 *
 */
public class OFCounterMod extends OFMessage {
    public static int MINIMUM_LENGTH = OFMessage.MINIMUM_LENGTH + OFCounter.MINIMUM_LENGTH;   

    protected OFCounter counter;
    
    public OFCounterMod(){
        super();
        super.setType(OFType.COUNTER_MOD);
        super.setLength((short)MINIMUM_LENGTH);
        if(counter == null){
            counter = new OFCounter();
        }
    }
    
    public void readFrom(ChannelBuffer data) {
        super.readFrom(data);
        if(counter == null){
            counter = new OFCounter();
        }
        counter.readFrom(data);
    }
    
    public String toBytesString(){
        return super.toBytesString() + counter.toBytesString();
    }
    
    public String toString(){
        return "CounterMod:" + counter.toString();
    }

    @Override
    public void writeTo(ChannelBuffer data) {
        super.writeTo(data);
        if(counter == null){
            counter = new OFCounter();
        }
        counter.writeTo(data);
    }

    public OFCounter getCounter() {
        return counter;
    }

    public void setCounter(OFCounter counter) {
        this.counter = counter;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((counter == null) ? 0 : counter.hashCode());
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
        OFCounterMod other = (OFCounterMod) obj;
        if (counter == null) {
            if (other.counter != null)
                return false;
        } else if (!counter.equals(other.counter))
            return false;
        return true;
    }
}
