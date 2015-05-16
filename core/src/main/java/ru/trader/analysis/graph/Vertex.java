package ru.trader.analysis.graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Vertex<T> {
    private final ArrayList<Edge<T>> edges = new ArrayList<>();
    private final T entry;
    private volatile int level = -1;

    public Vertex(T entry) {
        this.entry = entry;
    }

    public T getEntry() {
        return entry;
    }

    public boolean isEntry(T entry){
        return this.entry.equals(entry);
    }

    void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public void connect(Edge<T> edge){
        assert this == edge.getSource();
        synchronized (edges){
            edges.add(edge);
        }
    }

    public List<Edge<T>> getEdges() {
        return edges;
    }

    public Optional<Edge<T>> getEdge(Vertex<T> target) {
        return getEdge(target.entry);
    }

    public Optional<Edge<T>> getEdge(T target) {
        return edges.stream().filter((e) -> e.isConnect(target)).findFirst();
    }

    public void sortEdges(){
        edges.sort(Comparator.<Edge<T>>naturalOrder());
    }

    public boolean isConnected(T other){
        return getEdge(other).isPresent();
    }

    public boolean isSingle(){
        return edges.size() == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex vertex = (Vertex) o;
        return entry.equals(vertex.entry);
    }

    @Override
    public int hashCode() {
        return entry.hashCode();
    }

    @Override
    public String toString() {
        return "Vertex{" + entry + ", lvl=" + level + '}';
    }

}