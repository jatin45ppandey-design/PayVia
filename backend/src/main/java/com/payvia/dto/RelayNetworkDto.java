package com.payvia.dto;

import java.util.List;

public class RelayNetworkDto {
    private List<RelayNodeDto> nodes;
    private List<RelayEdgeDto> edges;

    public List<RelayNodeDto> getNodes() { return nodes; }
    public void setNodes(List<RelayNodeDto> nodes) { this.nodes = nodes; }

    public List<RelayEdgeDto> getEdges() { return edges; }
    public void setEdges(List<RelayEdgeDto> edges) { this.edges = edges; }
}
