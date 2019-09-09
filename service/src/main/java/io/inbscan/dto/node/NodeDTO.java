package io.inbscan.dto.node;

import java.util.List;

public class NodeDTO {

    private String Address;

    private NodeInfo NodeInfo;

    private Integer Stake;

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public io.inbscan.dto.node.NodeInfo getNodeInfo() {
        return NodeInfo;
    }

    public void setNodeInfo(io.inbscan.dto.node.NodeInfo nodeInfo) {
        NodeInfo = nodeInfo;
    }

    public Integer getStake() {
        return Stake;
    }

    public void setStake(Integer stake) {
        Stake = stake;
    }
}
