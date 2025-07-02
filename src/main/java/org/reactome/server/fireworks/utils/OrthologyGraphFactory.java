package org.reactome.server.fireworks.utils;


import org.reactome.server.graph.domain.model.Event;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.domain.model.Species;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.utils.ReactomeGraphCore;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
public class OrthologyGraphFactory {

    private final Long mainSpecies;
    private final GraphNode speciesGraph;

    private final Map<Long, GraphNode> dbId2MainSpeciesNode = new HashMap<>();
    private final Map<Long, GraphNode> dbId2SpeciesNode = new HashMap<>();


    private final DatabaseObjectService objectService = ReactomeGraphCore.getService(DatabaseObjectService.class);

    public OrthologyGraphFactory(GraphNode mainSpeciesGraph, GraphNode speciesGraph) {
        this.mainSpecies = mainSpeciesGraph.getDbId();
        this.speciesGraph = speciesGraph;

        initMap(dbId2MainSpeciesNode, mainSpeciesGraph);
        initMap(dbId2SpeciesNode, speciesGraph);

        setLayout();
    }

    public GraphNode getGraph() {
        return speciesGraph;
    }

    private void setLayout() {
        for (GraphNode speciesNode : dbId2SpeciesNode.values()) {
            GraphNode hsNode = getMainSpeciesGraphNode(speciesNode);
            if (hsNode != null && !speciesNode.hasLayoutData()) {
                speciesNode.setLayoutParameters(hsNode);
            }
        }
    }

    private GraphNode getMainSpeciesGraphNode(GraphNode node) {
        if (node.getStId().equals("SPECIES_NODE")) return null;
        Pathway pathway = objectService.findById(node.getDbId());
        if (pathway.getInferredFrom() != null) {
            for (Event event : pathway.getInferredFrom()) {
                for (Species s : event.getSpecies()) {
                    if (s.getDbId().equals(this.mainSpecies)) {
                        return dbId2MainSpeciesNode.get(event.getDbId());
                    }
                }
            }
        }
        if (pathway.getOrthologousEvent() != null) {
            for (Event event : pathway.getOrthologousEvent()) {
                for (Species s : event.getSpecies()) {
                    if (s.getDbId().equals(this.mainSpecies)) {
                        return dbId2MainSpeciesNode.get(event.getDbId());
                    }
                }
            }
        }
        return null;
    }

    private void initMap(Map<Long, GraphNode> map, GraphNode node) {
        map.put(node.getDbId(), node);
        for (GraphNode gNode : node.getChildren()) {
            initMap(map, gNode);
        }
    }
}
