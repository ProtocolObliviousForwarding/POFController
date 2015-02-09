/**
*    Copyright 2011, Big Switch Networks, Inc. 
*    Originally created by David Erickson, Stanford University
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

package org.openflow.protocol.serializers;

/**
 * Modified by Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 * Modify public void serialize() based on new OFFeaturesReply format.
 */

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.openflow.protocol.OFFeaturesReply;

public class OFFeaturesReplyJSONSerializer extends JsonSerializer<OFFeaturesReply> {
    
    /**
     * Performs the serialization of a OFFeaturesReply object
     */
    @Override
    public void serialize(OFFeaturesReply reply, JsonGenerator jGen, SerializerProvider serializer) throws IOException, JsonProcessingException {
        jGen.writeStartObject();
        jGen.writeNumberField("capabilities", reply.getCapabilities());
        jGen.writeStringField("deviceForwardEngineName", reply.getDeviceForwardEngineName());
        jGen.writeStringField("deviceLookupEngineName", reply.getDeviceLookupEngineName());
        jGen.writeNumberField("deviceId", reply.getDeviceId());
        jGen.writeStringField("experimenterName", reply.getExperimenterName());
        jGen.writeNumberField("length", reply.getLength());
        jGen.writeNumberField("portNum", reply.getPortNum());
        jGen.writeNumberField("tableNum", reply.getTableNum());
        jGen.writeStringField("type", reply.getType().toString());
        jGen.writeNumberField("version", reply.getVersion());
        jGen.writeNumberField("xid", reply.getXid());
        jGen.writeEndObject();
    }

    /**
     * Tells SimpleModule that we are the serializer for OFFeaturesReply
     */
    @Override
    public Class<OFFeaturesReply> handledType() {
        return OFFeaturesReply.class;
    }
}
