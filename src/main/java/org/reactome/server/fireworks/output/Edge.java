package org.reactome.server.fireworks.output;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
@SuppressWarnings("unused")
public class Edge {

    private final Long from;
    private final Long to;

    public Edge(Long from, Long to) {
        this.from = from;
        this.to = to;
    }

    public Long getFrom() {
        return from;
    }

    public Long getTo() {
        return to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Edge edge = (Edge) o;

        //noinspection SimplifiableIfStatement
        if (from != null ? !from.equals(edge.from) : edge.from != null) return false;
        return to != null ? to.equals(edge.to) : edge.to == null;
    }

    @Override
    public int hashCode() {
        int result = from != null ? from.hashCode() : 0;
        result = 31 * result + (to != null ? to.hashCode() : 0);
        return result;
    }
}
