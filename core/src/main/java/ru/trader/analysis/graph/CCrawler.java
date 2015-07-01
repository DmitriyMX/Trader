package ru.trader.analysis.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Profile;
import ru.trader.core.Ship;
import ru.trader.graph.Connectable;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CCrawler<T extends Connectable<T>> extends Crawler<T> {
    private final static Logger LOG = LoggerFactory.getLogger(CCrawler.class);
    private double startFuel;

    public CCrawler(Graph<T> graph, Function<List<Edge<T>>, Boolean> onFoundFunc) {
        super(graph, onFoundFunc);
        startFuel = getShip().getTank();
    }

    public CCrawler(Graph<T> graph, Function<Edge<T>, Boolean> isFoundFunc, Function<List<Edge<T>>, Boolean> onFoundFunc) {
        super(graph, isFoundFunc, onFoundFunc);
        startFuel = getShip().getTank();
    }


    protected Ship getShip(){
        return ((ConnectibleGraph<T>)graph).getShip();
    }

    protected Profile getProfile(){
        return ((ConnectibleGraph<T>)graph).getProfile();
    }

    public void setStartFuel(double startFuel) {
        this.startFuel = startFuel;
    }

    @Override
    protected CCostTraversalEntry start(Vertex<T> vertex) {
        double fuel = startFuel;
        return new CCostTraversalEntry(vertex, fuel);
    }

    @Override
    protected CCostTraversalEntry travers(CostTraversalEntry entry, Edge<T> edge) {
        @SuppressWarnings("unchecked")
        CCostTraversalEntry cEntry = (CCostTraversalEntry)entry;
        ConnectibleEdge<T> cEdge = (ConnectibleEdge<T>) edge;
        double nextLimit;
        if (cEdge.isRefill()) {
            LOG.trace("Refill");
            nextLimit = getShip().getTank() - cEdge.getFuel();
        } else {
            nextLimit = getProfile().withRefill() ? cEntry.getFuel() - cEdge.getFuel() : getShip().getTank();
        }
        return new CCostTraversalEntry(cEntry, edge, nextLimit);
    }

    protected class CCostTraversalEntry extends CostTraversalEntry {
        private final double fuel;

        protected CCostTraversalEntry(Vertex<T> vertex, double fuel) {
            super(vertex);
            this.fuel = fuel;
        }

        protected CCostTraversalEntry(CCostTraversalEntry head, Edge<T> edge, double fuel) {
            super(head, edge);
            this.fuel = fuel;
        }

        public double getFuel() {
            return fuel;
        }

        @Override
        public List<Edge<T>> collect(Collection<Edge<T>> src) {
            return src.stream().filter(this::check).map(this::wrap).collect(Collectors.toList());
        }

        protected boolean check(Edge<T> e){
            ConnectibleEdge<T> edge = (ConnectibleEdge<T>) e;
            return edge.getFuel() <= fuel || edge.getSource().getEntry().canRefill();
        }

        protected ConnectibleEdge<T> wrap(Edge<T> edge){
            T source = edge.source.getEntry();
            double distance = source.getDistance(edge.target.getEntry());
            double fuelCost = getShip().getFuelCost(fuel, distance);
            double nextLimit = getProfile().withRefill() ? fuel - fuelCost : getShip().getTank();
            boolean refill = nextLimit < 0 && source.canRefill();
            if (refill) {
                refill = true;
                fuelCost = getShip().getFuelCost(distance);
            }
            return new ConnectibleEdge<>(edge.getSource(), edge.getTarget(), refill, fuelCost);
        }
    }
}
