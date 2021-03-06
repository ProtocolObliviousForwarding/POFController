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

package net.floodlightcontroller.packet;

/**
*
* @author David Erickson (daviderickson@cs.stanford.edu)
*/
public interface IPacket {
    /**
     * 
     * @return payload
     */
    public IPacket getPayload();

    /**
     * 
     * @param packet
     */
    public IPacket setPayload(IPacket packet);

    /**
     * 
     * @return parent
     */
    public IPacket getParent();

    /**
     * 
     * @param packet
     */
    public IPacket setParent(IPacket packet);

    /**
     * Sets all payloads parent packet if applicable, then serializes this 
     * packet and all payloads
     * @return a byte[] containing this packet and payloads
     */
    public byte[] serialize();

    /**
     * Deserializes this packet layer and all possible payloads
     * @param data
     * @param offset offset to start deserializing from
     * @param length length of the data to deserialize
     * @return the deserialized data
     */
    public IPacket deserialize(byte[] data, int offset, int length);
}
