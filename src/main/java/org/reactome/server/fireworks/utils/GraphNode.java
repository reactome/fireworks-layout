package org.reactome.server.fireworks.utils;


import org.reactome.server.fireworks.output.Edge;
import org.reactome.server.fireworks.output.Node;
import org.reactome.server.graph.domain.model.Pathway;
import org.reactome.server.graph.exception.CustomQueryException;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.utils.ReactomeGraphCore;

import java.util.*;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
public class GraphNode implements Comparable<GraphNode> {

    private final Long dbId;
    private final String stId;
    private final String name;
    private Boolean disease;
    private final Double size;
    private Double angle;

    private boolean layoutData = false;
    private int level = 0;

    private double x;
    private double y;
    private double radius;
    private double minAngle;
    private double maxAngle;

    private final List<GraphNode> children;
    private final List<GraphNode> parents;

    GraphNode(Long speciesId, String name) {
        this.dbId = speciesId;
        this.stId = "SPECIES_NODE";
        this.name = name;
        this.children = new ArrayList<>();
        this.parents = new ArrayList<>();

        AdvancedDatabaseObjectService aux = ReactomeGraphCore.getService(AdvancedDatabaseObjectService.class);
        String query = "MATCH (:Species{dbId:$speciesDbId})<-[:species]-(:Pathway)-[:hasEvent|input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate|repeatedUnit|referenceEntity|proteinMarker|RNAMarker*]->(re:ReferenceEntity) " +
                "RETURN COUNT(DISTINCT re) AS size";
        Map<String, Object> parametersMap = new HashMap<>();
        parametersMap.put("speciesDbId", speciesId);
        Long size = null;
        try {
            System.out.print("Calculating background size for " + name + "...");
            size = aux.getCustomQueryResult(Long.class, query, parametersMap);
            System.out.println(" Done. [" + size.intValue() + " entities]");
        } catch (CustomQueryException e) {
            System.err.println("A problem has been found calculating the background for the species " + name);
            System.exit(0);
        }
        this.size = size.doubleValue();
    }

    GraphNode(Pathway node) {
        this.children = new ArrayList<>();
        this.parents = new ArrayList<>();
        this.dbId = node.getDbId();
        this.stId = node.getStId();
        this.disease = node.getIsInDisease();
        this.name = node.getDisplayName();

        AdvancedDatabaseObjectService aux = ReactomeGraphCore.getService(AdvancedDatabaseObjectService.class);
        String query = "MATCH (:Pathway{dbId:$dbId})-[:hasEvent|input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|hasCandidate|repeatedUnit|referenceEntity|proteinMarker|RNAMarker*]->(re:ReferenceEntity) " +
                "RETURN COUNT(DISTINCT re) AS size";
        Map<String, Object> parametersMap = new HashMap<>();
        parametersMap.put("dbId", dbId);
        Long size = null;
        try {
            size = aux.getCustomQueryResult(Long.class, query, parametersMap);
        } catch (CustomQueryException e) {
            System.err.println("A problem has been found calculating the size for the pathway " + node);
            System.exit(0);
        }
        this.size = size.doubleValue();
    }

    void addChild(GraphNode child) {
        child.setLevel(this.level + 1);
        this.children.add(child);
        child.addParent(this);
    }

    private void addParent(GraphNode parent) {
        this.parents.add(parent);
    }

    public Long getDbId() {
        return dbId;
    }

    public String getStId() {
        return stId;
    }

    public String getName() {
        return name;
    }

    public Double getRatio() {
        Double speciesSize = getRoot().getSize();
        return size / speciesSize;
    }

    public Double getSize() {
        if (this.stId.equals("SPECIES_NODE")) return size;
        return (getRatio() + 0.01) * 15;
    }

    public Double getAngle() {
        return angle;
    }

    public double getMinAngle() {
        return minAngle;
    }

    public double getMaxAngle() {
        return maxAngle;
    }

    public double getRadius() {
        return radius;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public List<GraphNode> getChildren() {
        List<GraphNode> rtn = new LinkedList<>();
        rtn.addAll(this.children);
        Collections.sort(rtn);
        return rtn;
    }

    public List<Edge> getEdges() {
        List<Edge> rtn = new LinkedList<>();
        for (GraphNode child : this.getChildren()) {
            if (!this.dbId.equals(-1L) && this.layoutData) {
                rtn.add(new Edge(this.getDbId(), child.getDbId()));
            }
            rtn.addAll(child.getEdges());
        }
        return rtn;
    }

    public List<Node> getNodes() {
        List<Node> rtn = new LinkedList<>();
        if (!this.dbId.equals(-1L) && this.layoutData) {
            rtn.add(new Node(this));
        }
        for (GraphNode child : this.children) {
            rtn.addAll(child.getNodes());
        }
        return rtn;
    }

    @SuppressWarnings("UnusedDeclaration")
    public List<GraphNode> getParents() {
        List<GraphNode> rtn = new LinkedList<>();
        rtn.addAll(this.parents);
        Collections.sort(rtn);
        return rtn;
    }

    private GraphNode getRoot() {
        if (this.stId.equals("SPECIES_NODE")) return this;
        return this.parents.get(0).getRoot();
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public int compareTo(GraphNode o) {
        return this.name.compareTo(o.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GraphNode graphNode = (GraphNode) o;

        //noinspection RedundantIfStatement
        if (dbId != null ? !dbId.equals(graphNode.dbId) : graphNode.dbId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return dbId != null ? dbId.hashCode() : 0;
    }

    public boolean hasLayoutData() {
        return layoutData;
    }

    private void setLevel(int level) {
        if (this.level < level) {
            this.level = level;
        }
    }

    public void setLayoutParameters(double x, double y, double radius, double angle, double minAngle, double maxAngle) {
        this.layoutData = true;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.angle = angle;
        this.minAngle = minAngle;
        if (Math.abs(maxAngle - minAngle) > Math.PI) {
            this.maxAngle = minAngle + Math.PI;
        } else {
            this.maxAngle = maxAngle;
        }
    }

    void setLayoutParameters(GraphNode eq) {
        this.layoutData = true;
        this.x = eq.x;
        this.y = eq.y;
        this.radius = eq.radius;
        this.angle = eq.angle;
        this.minAngle = eq.minAngle;
        this.maxAngle = eq.maxAngle;
    }

    @Override
    public String toString() {
        return "GraphNode{" +
                "dbId=" + dbId +
                ", name='" + name + '\'' +
                ", x=" + x +
                ", y=" + y +
                '}';
    }

    public Boolean getDisease() {
        return this.disease;
    }
}
