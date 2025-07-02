package org.reactome.server.fireworks.output;

import org.reactome.server.fireworks.utils.GraphNode;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@SuppressWarnings("unused")
public class Graph {

    private final Long speciesId;
    private final String speciesName;

    private final Collection<Node> nodes;
    private final Collection<Edge> edges;

    public Graph(GraphNode graph) {
        this.speciesId = graph.getDbId();
        this.speciesName = graph.getName();

        this.nodes = new HashSet<>(graph.getNodes());
        this.edges = new HashSet<>(graph.getEdges());
    }

    public Long getSpeciesId() {
        return speciesId;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public Collection<Node> getNodes() {
        return nodes;
    }

    public Collection<Edge> getEdges() {
        return edges;
    }


}
