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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an OFProtocol. 
 * OFProtocol is just used in GUI and controller and do not be sent to switch.
 * OFProtocol is formed by several fields, 
 * e.g. Ethernet protocol is formed by three field: DesMac, SrcMac, EthType
 * 
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 *
 */
public class OFProtocol implements Cloneable{
    public static int MINIMUM_LENGTH = 40;
    public static int MAXIMAL_LENGTH = 360;
    
    protected String protocolName;
    protected short protocolId;
    protected short totalLength;		//bit
    protected List<OFMatch20> fieldList;
    
    public OFProtocol() {
        super();
    }
    
    public OFMatch20 getField(short fieldId) {
    	if(null != fieldList){
	    	for(OFMatch20 field : fieldList){
	    		if(field.getFieldId() == fieldId){
	    			return field;
	    		}
	    	}
    	}
        return null;
    }    
   
    public List<OFMatch20> getAllField() {
        return fieldList;
    }
    public void setFieldList(List<OFMatch20> fieldList) {
        this.fieldList = fieldList;
    }
    public String getProtocolName() {
        return protocolName;
    }
    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }
    public short getProtocolId() {
        return protocolId;
    }
    public void setProtocolId(short protocolId) {
        this.protocolId = protocolId;
    }
    public short getTotalLength() {
        return totalLength;
    }
    public void setTotalLength(short totalLength) {
        this.totalLength = totalLength;
    }
    public int getFieldNum() {
        return fieldList.size();
    }  

    @Override
    public String toString(){
        String string = "name=" + this.protocolName +
			                ";id=" + this.protocolId +
			                ";tl=" + this.totalLength;
        
        if(this.fieldList != null){
            string += ";field(" + fieldList.size() + "):";
            for(OFMatch20 field : fieldList){
                string += field.toString() + ", ";
            }
        }else{
            string += ";field=null";
        }
        
        return string;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((fieldList == null) ? 0 : fieldList.hashCode());
        result = prime * result + protocolId;
        result = prime * result
                + ((protocolName == null) ? 0 : protocolName.hashCode());
        result = prime * result + totalLength;
        return result;
    }    
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OFProtocol other = (OFProtocol) obj;
        if (fieldList == null) {
            if (other.fieldList != null)
                return false;
        } else if (!fieldList.equals(other.fieldList))
            return false;
        if (protocolId != other.protocolId)
            return false;
        if (protocolName == null) {
            if (other.protocolName != null)
                return false;
        } else if (!protocolName.equals(other.protocolName))
            return false;
        if (totalLength != other.totalLength)
            return false;
        return true;
    }
    
    @Override
    public OFProtocol clone() throws CloneNotSupportedException {
        OFProtocol protocol = (OFProtocol) super.clone();
        
        if(null != fieldList 
                && 0 != fieldList.size()){
            List<OFMatch20> neoMatchList = new ArrayList<OFMatch20>();
            for(OFMatch20 match20: fieldList){
            	neoMatchList.add((OFMatch20) match20.clone());
            }
            protocol.setFieldList(neoMatchList);
        }

        return protocol;
    }
}
