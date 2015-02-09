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

/**
 * Modified by Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 *      Modified OFPort from short to int.
 */
package org.openflow.protocol;

public enum OFPort {
    OFPP_MAX                ((int)0xffffff00),
    OFPP_IN_PORT            ((int)0xfffffff8),
    OFPP_TABLE              ((int)0xfffffff9),
    OFPP_NORMAL             ((int)0xfffffffa),
    OFPP_FLOOD              ((int)0xfffffffb),
    OFPP_ALL                ((int)0xfffffffc),
    OFPP_CONTROLLER         ((int)0xfffffffd),
    OFPP_LOCAL              ((int)0xfffffffe),
    OFPP_ANY                ((int)0xffffffff);

    protected int value;

    private OFPort(int value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }
}
