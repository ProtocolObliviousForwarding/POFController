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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OFDataTable is a kind of list, stores the data with:
 * 1. when delete an element, save the index into the freeList 
 * 2. alloc a new index from the freeList first; if freeList is empty, use the entryIdNo (then entryIdNo++).
 * 
 * @author Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 */
public class OFDataTable<V> {
    protected Map<Integer, V> dataTable;
    protected List<Integer> freeIdList;
    protected int entryIdNo;
    protected int maxNumber;
    protected int startNo;
    
    /**
     * @param startNo
     *          should set startNo at initialization, in case anyone want to set 0 as invalid value.
     */    
    public OFDataTable(int startNo){
        if(startNo < 0 || startNo >= Integer.MAX_VALUE){
            throw new ArrayIndexOutOfBoundsException(startNo);
        }
        
        dataTable = new ConcurrentHashMap<Integer, V>();
        freeIdList = Collections.synchronizedList(new ArrayList<Integer>());
        entryIdNo = startNo;
        this.startNo = startNo;
        this.maxNumber = Integer.MAX_VALUE;
    }
    
    
    
    public OFDataTable(Map<Integer, V> dataTable, List<Integer> freeIdList, int entryIdNo, int maxNumber, int startNo) {
		super();
		this.dataTable = dataTable;
		this.freeIdList = freeIdList;
		this.entryIdNo = entryIdNo;
		this.maxNumber = maxNumber;
		this.startNo = startNo;
	}


	public void setMaxNumber(int maxNumber){
        if(maxNumber >= 0 && maxNumber > startNo){
            this.maxNumber = maxNumber;
        }
    }
    
    public V get(int index){
        if(index >= 0 && index >= startNo && index <= maxNumber){
            return dataTable.get(index);
        }else{
            return null;
        }
    }
    
    public Map<Integer, V> getAllData(){
        return dataTable;
    }
    
    /**
     * Get the first value's index
     * @param value
     * @return the index, -1 means not found.
     */
    public int getFirstValueIndex(V value){
        Iterator<Integer> iter = dataTable.keySet().iterator();
        int index;
        while(iter.hasNext()){
            index = iter.next();
            if( value.equals(dataTable.get(index)) ){
                return index;
            }
        }
        return -1;
    }
    
    public synchronized void put(int index, V value){
        if(index < 0 || index < startNo || index > maxNumber){
            throw new ArrayIndexOutOfBoundsException(index);
        }
        dataTable.put(index, value);
    }
    
    public synchronized V remove(int index){
        V value = null;
        if(index >= 0 && index >= startNo && index <= maxNumber){
            value = dataTable.remove(index);
            freeIdList.add(index);
        }
        
        return value;
    }
    
    /**
     * remove all elements which equals value
     * @param value
     */
    public synchronized void removeValue(V value){
        Iterator<Integer> iter = dataTable.keySet().iterator();
        int index;
        while(iter.hasNext()){
            index = iter.next();
            if( value.equals(dataTable.get(index)) ){
                iter.remove();
                freeIdList.add(index);
            }
        }
    }
    
    /**
     * put the value into table.
     * @param value
     * @return the index which store the value. -1 means table is full.
     */
    public synchronized int put(V value){
        int key = alloc();
        if(key == -1){
            return key;
        }
        dataTable.put(key, value);
        
        return key;
    }
    
    
    /**
     * alloc an index from freeList first, otherwise alloc an new index
     * @return the index which store the value. -1 means table is full.
     */
    public synchronized int alloc(){
        int index;
        if(freeIdList.isEmpty() == false){
            index = freeIdList.remove(0);
        }else{
            index = allocNew();
        }
        
        return index;
    }
    
    /**
     * direct alloc an new index, do not check the freeList
     * @return the index which store the value. -1 means table is full.
     */
    public synchronized int allocNew(){
        if(entryIdNo > maxNumber){
            return -1;
        }
        int index = entryIdNo;            
        entryIdNo++;
        
        return index;
    }
    
    public int usedSize(){
        return dataTable.size();
    }

	public List<Integer> getFreeIdList() {
		return freeIdList;
	}

	public int getEntryIdNo() {
		return entryIdNo;
	}

	public int getMaxNumber() {
		return maxNumber;
	}

	public int getStartNo() {
		return startNo;
	}

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataTable == null) ? 0 : dataTable.hashCode());
        result = prime * result + entryIdNo;
        result = prime * result + ((freeIdList == null) ? 0 : freeIdList.hashCode());
        result = prime * result + maxNumber;
        result = prime * result + startNo;
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
        OFDataTable<?> other = (OFDataTable<?>) obj;
        if (dataTable == null) {
            if (other.dataTable != null)
                return false;
        } else if (!dataTable.equals(other.dataTable))
            return false;
        if (entryIdNo != other.entryIdNo)
            return false;
        if (freeIdList == null) {
            if (other.freeIdList != null)
                return false;
        } else if (!freeIdList.equals(other.freeIdList))
            return false;
        if (maxNumber != other.maxNumber)
            return false;
        if (startNo != other.startNo)
            return false;
        return true;
    }    
}
