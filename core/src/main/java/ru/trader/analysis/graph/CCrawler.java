package ru.trader.analysis.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Profile;
import ru.trader.core.Ship;
import ru.trader.graph.Connectable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class CCrawler<T extends Connectable<T>> extends Crawler<T> {
    private final static Logger LOG = LoggerFactory.getLogger(CCrawler.class);

    public CCrawler(ConnectibleGraph<T> graph, Consumer<List<Edge<T>>> onFoundFunc) {
        super(graph, onFoundFunc);
    }

    private Ship getShip(){
        return ((ConnectibleGraph<T>)graph).getProfile().getShip();
    }

    private Profile getProfile(){
        return ((ConnectibleGraph<T>)graph).getProfile();
    }

    @Override
    protected CostTraversalEntry start(Vertex<T> vertex) {
        double fuel = getShip().getTank();
        return new CCostTraversalEntry(new ArrayList<>(), vertex, fuel);
    }

    @Override
    protected CostTraversalEntry travers(CostTraversalEntry entry, List<Edge<T>> head, Edge<T> edge, T target) {
        T source = entry.vertex.getEntry();
        double distance = source.getDistance(edge.target.getEntry());
        double fuelCost = getShip().getFuelCost(((CCostTraversalEntry)entry).fuel, distance);
        double nextLimit = getProfile().withRefill() ? ((CCostTraversalEntry)entry).fuel - fuelCost : getShip().getTank();
        boolean refill = nextLimit < 0 && source.canRefill();
        if (refill) {
            LOG.trace("Refill");
            refill = true;
            fuelCost = getShip().getFuelCost(distance);
            nextLimit = getShip().getTank() - fuelCost;
        }
        edge = new ConnectibleEdge<>(edge.getSource(), edge.getTarget(), refill, fuelCost);
        return new CCostTraversalEntry(head, edge, entry.getWeight(), nextLimit);
    }

    protected class CCostTraversalEntry extends CostTraversalEntry {
        private final double fuel;

        protected CCostTraversalEntry(List<Edge<T>> head, Vertex<T> vertex, double fuel) {
            super(head, vertex);
            this.fuel = fuel;
        }

        protected CCostTraversalEntry(List<Edge<T>> head, Edge<T> edge, double cost, double fuel) {
            super(head, edge, cost);
            this.fuel = fuel;
        }

        public double getFuel() {
            return fuel;
        }

        @Override
        protected Iterator<Edge<T>> getIteratorInstance() {
            return vertex.getEdges().stream().filter(e -> {
                ConnectibleEdge<T> edge = (ConnectibleEdge<T>) e;
                return edge.getFuel() <= fuel || e.getSource().getEntry().canRefill();
            }).iterator();
        }
    }
}
