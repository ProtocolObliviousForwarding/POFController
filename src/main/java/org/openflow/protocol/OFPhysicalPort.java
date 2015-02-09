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

package org.openflow.protocol;

import java.util.Arrays;


import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.jboss.netty.buffer.ChannelBuffer;
import org.openflow.protocol.serializers.OFPhysicalPortJSONSerializer;
import org.openflow.util.HexString;
import org.openflow.util.ParseString;

/**
 * Modified by Song Jian (jack.songjian@huawei.com), Huawei Technologies Co., Ltd.
 *      Delete items in enum OFPortConfig
 *          delete OFPPC_NO_STP, OFPPC_NO_RECV_STP, OFPPC_NO_FLOOD
 *      Delete items in enum OFPortState
 *          delete OFPPS_STP_LISTEN, OFPPS_STP_LEARN, OFPPS_STP_FORWARD, OFPPS_STP_BLOCK, OFPPS_STP_MASK
 *          add     OFPPS_BLOCKED, OFPPS_LIVE
 *      Add items in OFPortFeatures
 *          add     OFPPF_40GB_FD, OFPPF_100GB_FD, OFPPF_1TB_FD, OFPPF_OTHER
 *      
 *      Modify class member short portNumber to int portId
 *      Add class member devicedId
 *      Add class member currentSpeed
 *      Add class member maxSpeed
 *      Add class member openflowEnable
 *      And add get/set methods of above class members
 *      Modify the readFrom()/writeTo() members to match the class member change.
 *      Add setAll() methods to set an OFPhysicalPort object
 *          
 */

/**
 * Represents ofp_phy_port
 * @author David Erickson (daviderickson@cs.stanford.edu) - Mar 25, 2010
 */
@JsonSerialize(using=OFPhysicalPortJSONSerializer.class)
public class OFPhysicalPort {
    public static int MINIMUM_LENGTH = 88;
    public static int OFP_ETH_ALEN = 6;

    public enum OFPortConfig {
        OFPPC_PORT_DOWN    (1 << 0) {
            public String toString() {
                return "port-down (0x1)";
            }
        },
//        OFPPC_NO_STP       (1 << 1) {
//            public String toString() {
//                return "no-stp (0x2)";
//            }
//        },
        OFPPC_NO_RECV      (1 << 2) {
            public String toString() {
                return "no-recv (0x4)";
            }
        },
//        OFPPC_NO_RECV_STP  (1 << 3) {
//            public String toString() {
//                return "no-recv-stp (0x8)";
//            }
//        },
//        OFPPC_NO_FLOOD     (1 << 4) {
//            public String toString() {
//                return "no-flood (0x10)";
//            }
//        },
        OFPPC_NO_FWD       (1 << 5) {
            public String toString() {
                return "no-fwd (0x20)";
            }
        },
        OFPPC_NO_PACKET_IN (1 << 6) {
            public String toString() {
                return "no-pkt-in (0x40)";
            }
        };

        protected int value;

        private OFPortConfig(int value) {
            this.value = value;
        }

        /**
         * @return the value
         */
        public int getValue() {
            return value;
        }
    }

    public enum OFPortState {
        OFPPS_LINK_DOWN     (1 << 0) {
            public String toString() {
                return "link-down (0x1)";
            }
        },
        /*
        OFPPS_STP_LISTEN  (0 << 8) {
            public String toString() {
                return "listen (0x0)";
            }
        },
        OFPPS_STP_LEARN   (1 << 8) {
            public String toString() {
                return "learn-no-relay (0x100)";
            }
        },
        OFPPS_STP_FORWARD (2 << 8) {
            public String toString() {
                return "forward (0x200)";
            }
        },
        OFPPS_STP_BLOCK   (3 << 8) {
            public String toString() {
                return "block-broadcast (0x300)";
            }
        },
        OFPPS_STP_MASK    (3 << 8) {
            public String toString() {
                return "block-broadcast (0x300)";
            }
        };*/
        
        OFPPS_BLOCKED       (1 << 1) {
            public String toString() {
                return "blocked (0x2)";
            }
        },
        OFPPS_LIVE          (1 << 2) {
            public String toString() {
                return "live (0x4)";
            }
        };

        protected int value;

        private OFPortState(int value) {
            this.value = value;
        }

        /**
         * @return the value
         */
        public int getValue() {
            return value;
        }
    }

    public enum OFPortFeatures {
        OFPPF_10MB_HD    (1 << 0) {
            public String toString() {
                return "10mb-hd (0x1)";
            }
        },
        OFPPF_10MB_FD    (1 << 1) {
            public String toString() {
                return "10mb-fd (0x2)";
            }
        },
        OFPPF_100MB_HD   (1 << 2) {
            public String toString() {
                return "100mb-hd (0x4)";
            }
        },
        OFPPF_100MB_FD   (1 << 3) {
            public String toString() {
                return "100mb-fd (0x8)";
            }
        },
        OFPPF_1GB_HD     (1 << 4) {
            public String toString() {
                return "1gb-hd (0x10)";
            }
        },
        OFPPF_1GB_FD     (1 << 5) {
            public String toString() {
                return "1gb-fd (0x20)";
            }
        },
        OFPPF_10GB_FD    (1 << 6) {
            public String toString() {
                return "10gb-fd (0x40)";
            }
        },
        OFPPF_40GB_FD    (1 << 7) {
            public String toString() {
                return "40gb-fd (0x80)";
            }
        },
        OFPPF_100GB_FD   (1 << 8) {
            public String toString() {
                return "100gb-fd (0x100)";
            }
        },
        OFPPF_1TB_FD     (1 << 9) {
            public String toString() {
                return "40gb-fd (0x200)";
            }
        },
        OFPPF_OTHER      (1 << 10) {
            public String toString() {
                return "40gb-fd (0x400)";
            }
        },
        OFPPF_COPPER     (1 << 11) {
            public String toString() {
                return "copper (0x800)";
            }
        },
        OFPPF_FIBER      (1 << 12) {
            public String toString() {
                return "fiber (0x1000)";
            }
        },
        OFPPF_AUTONEG    (1 << 13) {
            public String toString() {
                return "autoneg (0x2000)";
            }
        },
        OFPPF_PAUSE      (1 << 14) {
            public String toString() {
                return "pause (0x4000)";
            }
        },
        OFPPF_PAUSE_ASYM (1 << 15) {
            public String toString() {
                return "pause-asym (0x8000)";
            }
        };

        protected int value;

        private OFPortFeatures(int value) {
            this.value = value;
        }

        /**
         * @return the value
         */
        public int getValue() {
            return value;
        }
    }

    protected int portId;
    protected int deviceId;
    
    protected byte[] hardwareAddress;
    
    protected String name;              //32B
    
    protected int config;
    protected int state;
    
    protected int currentFeatures;
    protected int advertisedFeatures;
    
    protected int supportedFeatures;
    protected int peerFeatures;
    
    protected int currentSpeed;
    protected int maxSpeed;
    
    protected byte openflowEnable;
    
    

    public void setAll(int portId, int deviceId, byte[] hardwareAddress,
            String name, int config, int state, int currentFeatures,
            int advertisedFeatures, int supportedFeatures, int peerFeatures,
            int currentSpeed, int maxSpeed, byte openflowEnable) {
        this.portId = portId;
        this.deviceId = deviceId;
        this.hardwareAddress = hardwareAddress;
        this.name = name;
        this.config = config;
        this.state = state;
        this.currentFeatures = currentFeatures;
        this.advertisedFeatures = advertisedFeatures;
        this.supportedFeatures = supportedFeatures;
        this.peerFeatures = peerFeatures;
        this.currentSpeed = currentSpeed;
        this.maxSpeed = maxSpeed;
        this.openflowEnable = openflowEnable;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(int currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(int maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public byte getOpenflowEnable() {
        return openflowEnable;
    }

    public void setOpenflowEnable(byte openflowEnable) {
        this.openflowEnable = openflowEnable;
    }

    /**
     * @return the portNumber
     */
    public int getPortId() {
        return portId;
    }

    /**
     * @param portId the portId to set
     */
    public void setPortId(int portId) {
        this.portId = portId;
    }
    
    public void setPortId(OFPort portId) {
        this.portId = portId.getValue();
    }

    /**
     * @return the hardwareAddress
     */
    public byte[] getHardwareAddress() {
        return hardwareAddress;
    }

    /**
     * @param hardwareAddress the hardwareAddress to set
     */
    public void setHardwareAddress(byte[] hardwareAddress) {
        if (hardwareAddress.length != OFP_ETH_ALEN)
            throw new RuntimeException("Hardware address must have length "
                    + OFP_ETH_ALEN);
        this.hardwareAddress = hardwareAddress;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the config
     */
    public int getConfig() {
        return config;
    }

    /**
     * @param config the config to set
     */
    public void setConfig(int config) {
        this.config = config;
    }

    /**
     * @return the state
     */
    public int getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(int state) {
        this.state = state;
    }

    /**
     * @return the currentFeatures
     */
    public int getCurrentFeatures() {
        return currentFeatures;
    }

    /**
     * @param currentFeatures the currentFeatures to set
     */
    public void setCurrentFeatures(int currentFeatures) {
        this.currentFeatures = currentFeatures;
    }

    /**
     * @return the advertisedFeatures
     */
    public int getAdvertisedFeatures() {
        return advertisedFeatures;
    }

    /**
     * @param advertisedFeatures the advertisedFeatures to set
     */
    public void setAdvertisedFeatures(int advertisedFeatures) {
        this.advertisedFeatures = advertisedFeatures;
    }

    /**
     * @return the supportedFeatures
     */
    public int getSupportedFeatures() {
        return supportedFeatures;
    }

    /**
     * @param supportedFeatures the supportedFeatures to set
     */
    public void setSupportedFeatures(int supportedFeatures) {
        this.supportedFeatures = supportedFeatures;
    }

    /**
     * @return the peerFeatures
     */
    public int getPeerFeatures() {
        return peerFeatures;
    }

    /**
     * @param peerFeatures the peerFeatures to set
     */
    public void setPeerFeatures(int peerFeatures) {
        this.peerFeatures = peerFeatures;
    }
    
    

    /**
     * Read this message off the wire from the specified ByteBuffer
     * @param data
     */
    public void readFrom(ChannelBuffer data) {
        this.portId = data.readInt();
        this.deviceId = data.readInt();
        
        if (this.hardwareAddress == null)
            this.hardwareAddress = new byte[OFP_ETH_ALEN];
        data.readBytes(this.hardwareAddress);
        data.readShort();
        
        this.name = ParseString.NameByteToString(data);

        this.config = data.readInt();
        this.state = data.readInt();
        
        this.currentFeatures = data.readInt();
        this.advertisedFeatures = data.readInt();
        this.supportedFeatures = data.readInt();
        this.peerFeatures = data.readInt();
        
        this.currentSpeed = data.readInt();
        this.maxSpeed = data.readInt();
        
        this.openflowEnable = data.readByte();
        data.readBytes(7);
    }

    /**
     * Write this message's binary format to the specified ByteBuffer
     * @param data
     */
    public void writeTo(ChannelBuffer data) {
        data.writeInt(this.portId);
        data.writeInt(this.deviceId);
        data.writeBytes(this.hardwareAddress);
        data.writeZero(2);
        
        data.writeBytes(ParseString.NameStringToBytes(this.name));

        data.writeInt(this.config);
        data.writeInt(this.state);
        
        data.writeInt(this.currentFeatures);
        data.writeInt(this.advertisedFeatures);
        data.writeInt(this.supportedFeatures);
        data.writeInt(this.peerFeatures);
        
        data.writeInt(this.currentSpeed);
        data.writeInt(this.maxSpeed);
        
        data.writeByte(this.openflowEnable);
        data.writeZero(7);
    }
    
    public String toBytesString(){
        String bytesString = HexString.toHex(portId);
        
        bytesString += HexString.toHex(deviceId);
        
        bytesString += HexString.toHex(hardwareAddress);
        bytesString += HexString.ByteZeroEnd(2);
        
        bytesString += HexString.NameToHex(name);
        
        bytesString += HexString.toHex(config);
        
        bytesString += HexString.toHex(state);
        
        bytesString += HexString.toHex(currentFeatures);
        
        bytesString += HexString.toHex(advertisedFeatures);
        
        bytesString += HexString.toHex(supportedFeatures);
        
        bytesString += HexString.toHex(peerFeatures);
        
        bytesString += HexString.toHex(currentSpeed);
        
        bytesString += HexString.toHex(maxSpeed);
        
        bytesString += HexString.toHex(openflowEnable);
        bytesString += HexString.ByteZeroEnd(7);
        
        return bytesString;
        
    }
    
    public String toString(){
        String string = 
                ";pid=" + this.portId +
                ";did=" + this.deviceId +
                ";addr=" + HexString.toHex(this.hardwareAddress) +
                ";name=" + this.name +
                ";cfg=" + this.config +
                ";sta=" + this.state +
                ";curf=" + this.currentFeatures +
                ";adv=" + this.advertisedFeatures +
                ";sup=" + this.supportedFeatures +
                ";per=" + this.peerFeatures +
                ";curs=" + this.currentSpeed +
                ";maxs=" + this.maxSpeed +
                ";ofe=" + this.openflowEnable;
        
        return string;
    }

    @Override
    public int hashCode() {
        final int prime = 307;
        int result = 1;
        result = prime * result + portId;
        result = prime * result + deviceId;
        result = prime * result + Arrays.hashCode(hardwareAddress);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        
        result = prime * result + config;
        result = prime * result + state;
        
        result = prime * result + currentFeatures;
        result = prime * result + advertisedFeatures;
        result = prime * result + supportedFeatures;
        result = prime * result + peerFeatures;
        
        result = prime * result + currentSpeed;
        result = prime * result + maxSpeed;
        
        result = prime * result + openflowEnable;
        
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof OFPhysicalPort)) {
            return false;
        }
        OFPhysicalPort other = (OFPhysicalPort) obj;
        if (portId != other.portId) {
            return false;
        }
        if (deviceId != other.deviceId) {
            return false;
        }
        if (!Arrays.equals(hardwareAddress, other.hardwareAddress)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        
        if (config != other.config) {
            return false;
        }
        if (state != other.state) {
            return false;
        }

        if (currentFeatures != other.currentFeatures) {
            return false;
        }
        if (advertisedFeatures != other.advertisedFeatures) {
            return false;
        }
        if (supportedFeatures != other.supportedFeatures) {
            return false;
        }
        if (peerFeatures != other.peerFeatures) {
            return false;
        }
        
        if (currentSpeed != other.currentSpeed) {
            return false;
        }
        if (maxSpeed != other.maxSpeed) {
            return false;
        }
        
        if (openflowEnable != other.openflowEnable) {
            return false;
        }
        
        return true;
    }
}
