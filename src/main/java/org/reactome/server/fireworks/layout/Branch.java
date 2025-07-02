package org.reactome.server.fireworks.layout;

import org.apache.commons.math3.util.Precision;
import org.reactome.server.fireworks.utils.GraphNode;

import java.util.Collections;
import java.util.List;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
class Branch {
    static final double RADIUS_DELTA = 12d;

    final double minRadius;
    double minAngle;
    double maxAngle;
    double angle;

    Burst burst;

    List<GraphNode> nodes;
    double length;
    double radius;

    Branch(Burst burst, double minRadius, double minAngle, double maxAngle, List<GraphNode> nodes) {
        this.burst = burst;
        this.minRadius = minRadius;
        this.minAngle = minAngle;
        this.maxAngle = maxAngle;
        this.angle = Math.abs(maxAngle - minAngle);
        this.nodes = nodes;
        if (burst.getDirection().equals(Direction.ANTICLOCKWISE)) {
            Collections.reverse(this.nodes);
        }
        this.setLength();
        this.setRadius();
    }

    double getRadius() {
        return this.radius;
    }

    void setNodesPosition() {
        double lAvailable = angle * this.radius;                    // first we calculate the space we have
        double angle = minAngle;
        for (GraphNode node : nodes) {
            if (!node.hasLayoutData() || node.getRadius() > this.radius) {
                double p = length / node.getSize();                 // p is the proportion factor
                double size = lAvailable / p;                       // a normalised size depending on the factor
                double minAngle = angle;
                double nodeAngle = angle + (size / 2d) / this.radius; // the angle where the node has to be drawn
                double x = this.burst.getCenterX() + this.radius * Math.cos(nodeAngle);
                double y = this.burst.getCenterY() + this.radius * Math.sin(nodeAngle);
                angle += size / this.radius;

                node.setLayoutParameters(x, y, this.radius, nodeAngle, minAngle, angle);
            }
        }
    }

    private void setLength() {
        length = 0;
        for (GraphNode node : nodes) {
            length += Precision.round(node.getSize(), 2);
        }
    }

    private void setRadius() {
        double r = length / angle;
        this.radius = minRadius + RADIUS_DELTA;
        if (r > radius) {
            //When this happens the nodes are way too close in the outer levels of the ring
            //Widening up the available range to laid them up is normally OK
            minAngle = this.minAngle - Math.toRadians(4);
            maxAngle = this.maxAngle + Math.toRadians(4);
            angle = Math.abs(maxAngle - minAngle);

//            double arc = angle * radius;
//            double factor = arc / getEuclidean(minAngle, maxAngle);
//            double neededAngle = factor * length / radius;
//            double angleDelta = Math.abs(neededAngle - angle) / 2d;
//            minAngle = minAngle - angleDelta;
//            maxAngle = maxAngle + angleDelta;
//            angle = Math.abs(maxAngle - minAngle);
        }
    }

    private double getEuclidean(double a1, double a2) {
        double x1 = this.burst.getCenterX() + this.radius * Math.cos(a1);
        double y1 = this.burst.getCenterY() + this.radius * Math.sin(a1);

        double x2 = this.burst.getCenterX() + this.radius * Math.cos(a2);
        double y2 = this.burst.getCenterY() + this.radius * Math.sin(a2);

        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
}
