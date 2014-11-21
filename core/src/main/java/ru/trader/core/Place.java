package ru.trader.core;

import ru.trader.graph.Connectable;

import java.util.Collection;

public interface Place extends Connectable<Place> {

    String getName();
    void setName(String name);

    double getX();
    double getY();
    double getZ();
    void setPosition(double x, double y, double z);

    Collection<Vendor> get();
    void add(Vendor vendor);
    Vendor addVendor(String name);
    void remove(Vendor vendor);

    default boolean isEmpty(){
        return get().isEmpty();
    }

    @Override
    default boolean canRefill() {
        return !isEmpty();
    }

    @Override
    default double getDistance(Place other){
        return getDistance(other.getX(), other.getY(), other.getZ());
    }

    default double getDistance(double x, double y, double z){
        return Math.sqrt(Math.pow(x - getX(), 2) + Math.pow(y - getY(), 2) + Math.pow(z - getZ(), 2));
    }

}