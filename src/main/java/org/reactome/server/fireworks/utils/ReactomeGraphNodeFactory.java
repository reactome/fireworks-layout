package org.reactome.server.fireworks.utils;

import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.model.Species;
import org.reactome.server.graph.service.TopLevelPathwayService;
import org.reactome.server.graph.utils.ReactomeGraphCore;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
public class ReactomeGraphNodeFactory {

    private final Map<Long, GraphNode> map = new HashMap<>();

    private final GraphNode graph;

    public ReactomeGraphNodeFactory(Species species) {
        TopLevelPathwayService tlpService = ReactomeGraphCore.getService(TopLevelPathwayService.class);

        this.graph = new GraphNode(species.getDbId(), species.getDisplayName());
        for (Pathway pNode : tlpService.getTopLevelPathways(species.getTaxId())) {
            GraphNode gNode = getOrCreateGraphNode(pNode);
            this.graph.addChild(gNode);
            buildBranch(pNode, gNode);
        }
    }

    public GraphNode getGraphNode() {
        return graph;
    }

    private void buildBranch(Pathway pathwayNode, GraphNode graphNode) {
        for (Event event : pathwayNode.getHasEvent()) {
            if (event instanceof Pathway) {
                Pathway pNode = (Pathway) event;
                GraphNode gNode = getOrCreateGraphNode(pNode);
                graphNode.addChild(gNode);
                buildBranch(pNode, gNode);
            }
        }
    }

    private GraphNode getOrCreateGraphNode(Pathway node) {
        GraphNode rtn = this.map.get(node.getDbId());
        if (rtn == null) {
            rtn = new GraphNode(node);
            this.map.put(node.getDbId(), rtn);
        }
        return rtn;
    }
}
