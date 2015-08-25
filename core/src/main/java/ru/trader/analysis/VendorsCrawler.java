package ru.trader.analysis;

import org.jetbrains.annotations.NotNull;
import ru.trader.analysis.graph.*;
import ru.trader.core.Order;
import ru.trader.core.Vendor;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class VendorsCrawler extends Crawler<Vendor> {
    private double startFuel;
    private double startBalance;
    private final VendorsCrawlerSpecification specification;

    public VendorsCrawler(VendorsGraph graph, VendorsCrawlerSpecification specification, AnalysisCallBack callback) {
        super(graph, specification, callback);
        this.specification = specification;
        startFuel = graph.getProfile().getShip().getTank();
        startBalance = graph.getProfile().getBalance();
    }

    private Scorer getScorer(){
        return ((VendorsGraph)graph).getScorer();
    }

    public void setStartFuel(double startFuel) {
        this.startFuel = startFuel;
    }

    public void setStartBalance(double startBalance) {
        this.startBalance = startBalance;
    }

    @Override
    protected VendorsTraversalEntry start(Vertex<Vendor> vertex) {
        return new VendorsTraversalEntry(super.start(vertex), startFuel, startBalance);
    }

    @Override
    protected VendorsTraversalEntry travers(final CostTraversalEntry entry, final Edge<Vendor> edge) {
        VendorsTraversalEntry vEntry = (VendorsTraversalEntry)entry;
        VendorsEdge vEdge = (VendorsEdge) edge;
        return new VendorsTraversalEntry(vEntry, vEdge);
    }

    protected class VendorsTraversalEntry extends CostTraversalEntry {
        private final double fuel;
        private final double balance;

        protected VendorsTraversalEntry(CostTraversalEntry entry, double fuel, double balance) {
            super(entry.getTarget());
            this.fuel = fuel;
            this.balance = balance;
        }

        protected VendorsTraversalEntry(VendorsTraversalEntry head, VendorsEdge edge) {
            super(head, edge);
            this.balance = head.balance + edge.getProfit();
            this.fuel = edge.getRemain();
        }

        @Override
        public List<Edge<Vendor>> collect(Collection<Edge<Vendor>> src) {
            return src.stream().filter(this::check).map(this::wrap).filter(e -> e != null).collect(Collectors.toList());
        }

        protected boolean check(Edge<Vendor> e){
            VendorsGraph.VendorsBuildEdge edge = (VendorsGraph.VendorsBuildEdge) e;
            return fuel <= edge.getMaxFuel() && (fuel >= edge.getMinFuel() || edge.getSource().getEntry().canRefill())
                   && (edge.getProfit() > 0 || VendorsCrawler.this.isFound(edge, this));
        }

        protected VendorsEdge wrap(Edge<Vendor> e) {
            VendorsGraph.VendorsBuildEdge edge = (VendorsGraph.VendorsBuildEdge) e;
            Path<Vendor> path = edge.getPath(fuel);
            if (path == null) return null;
            VendorsEdge res = new VendorsEdge(edge.getSource(), edge.getTarget(), new TransitPath(path, fuel));
            res.setOrders(MarketUtils.getStack(edge.getOrders(), balance, getScorer().getProfile().getShip().getCargo()));
            return res;
        }

        @Override
        public double getWeight() {
            if (weight == null){
                weight = specification.computeWeight(this);
            }
            return weight;
        }
    }

    public class VendorsEdge extends ConnectibleEdge<Vendor> {
        private TransitPath  path;
        private List<Order> orders;
        private Double profitByTonne;
        private Long time;

        protected VendorsEdge(Vertex<Vendor> source, Vertex<Vendor> target, TransitPath path) {
            super(source, target);
            if (path == null) throw new IllegalArgumentException("Path must be no-null");
            this.path = path;
        }

        protected void setOrders(List<Order> orders){
            this.orders = orders;
        }

        public double getProfit(){
            return getOrders().stream().mapToDouble(Order::getProfit).sum();
        }

        public List<Order> getOrders(){
            if (orders == null){
                Vendor seller = source.getEntry();
                Vendor buyer = target.getEntry();
                orders = MarketUtils.getOrders(seller, buyer);
            }
            return orders;
        }

        public double getRemain() {
            return path.getRemain();
        }

        @Override
        public boolean isRefill() {
            return path.isRefill();
        }

        @Override
        public double getFuelCost() {
            if (path != null){
                return path.getFuelCost();
            }
            return super.getFuelCost();
        }

        public TransitPath getPath() {
            return path;
        }

        public double getProfitByTonne() {
            if (profitByTonne == null){
                profitByTonne = computeProfit();
            }
            return profitByTonne;
        }

        public long getTime() {
            if (time == null){
                time = computeTime();
            }
            return time;
        }

        @Override
        public int compareTo(@NotNull Edge other) {
            double w = getWeight();
            double ow = other.getWeight();
            if (ow >= 0 && w >= 0) return super.compareTo(other);
            if (w < 0 && ow < 0) return Double.compare(Math.abs(w), Math.abs(ow));
            return w < 0 ? 1 : -1;
        }

        protected double computeProfit(){
            return getScorer().getProfitByTonne(getProfit(), getFuelCost());
        }

        protected long computeTime(){
            int jumps = source.getEntry().getPlace().equals(target.getEntry().getPlace())? 0 : 1;
            int lands = 1;
            if (path != null){
                jumps = path.size();
                lands += path.getRefillCount();
                //not lands if refuel on this station
                if (path.isRefill()) lands--;
            } else {
                lands += isRefill() ? 1 :0;
            }
            return getScorer().getTime(target.getEntry().getDistance(), jumps, lands);
        }

        @Override
        protected double computeWeight() {
            return specification.computeWeight(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof VendorsEdge)) return false;
            if (!super.equals(o)) return false;
            VendorsEdge edge = (VendorsEdge) o;
            return !(path != null ? !path.equals(edge.path) : edge.path != null);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (path != null ? path.hashCode() : 0);
            return result;
        }

    }
}
