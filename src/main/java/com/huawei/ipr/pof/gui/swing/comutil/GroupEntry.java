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

package com.huawei.ipr.pof.gui.swing.comutil;

import org.openflow.protocol.OFGroupMod;

import com.huawei.ipr.pof.gui.comm.GUITools.EDIT_STATUS;
import com.huawei.ipr.pof.manager.IPMService;

/** 
 * GroupEntry contains an OFGroupMod and an EDIT_STATUS, could used in GUI to easily
 * operate and pass the ofGroupMod and its ins/action GroupList-to-GroupList
 * 
 * @author Song Jian (jack.songjian@huawei.com),  Huawei Technologies Co., Ltd.
 */
public class GroupEntry{
	public OFGroupMod ofGroupMod;
	public EDIT_STATUS editStatus;
	
	public static GroupEntry getNewInstance(OFGroupMod ofGroupMod){
		GroupEntry groupEntry;
		groupEntry = new GroupEntry();
		if(null == ofGroupMod){			
			groupEntry.ofGroupMod = new OFGroupMod();
			groupEntry.ofGroupMod.setGroupId(IPMService.GROUPID_INVALID);
			groupEntry.editStatus = EDIT_STATUS.ES_ADDING;
		}else{
			groupEntry.ofGroupMod = ofGroupMod;
			groupEntry.editStatus = EDIT_STATUS.ES_READONLY;
		}

		return groupEntry;
	}
}
