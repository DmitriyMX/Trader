package ru.trader.store.berkeley.entities;

import com.sleepycat.persist.model.*;
import ru.trader.core.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

@Entity(version = 3)
public class BDBVendor {

    @PrimaryKey(sequence = "V_ID")
    private long id;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE,
                  relatedEntity = BDBPlace.class, onRelatedEntityDelete = DeleteAction.CASCADE)
    private long placeId;
    private String name;
    private FACTION faction;
    private GOVERNMENT government;
    private STATION_TYPE type;
    private ECONOMIC_TYPE economic;
    private ECONOMIC_TYPE subEconomic;
    private long modified;
    private double distance;

    @SecondaryKey(relate=Relationship.MANY_TO_MANY)
    Collection<SERVICE_TYPE> services = new HashSet<>();

    private BDBVendor() {
    }

    public BDBVendor(String name, long placeId) {
        this.name = name;
        this.placeId = placeId;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FACTION getFaction() {
        return faction;
    }

    public void setFaction(FACTION faction) {
        this.faction = faction;
    }

    public GOVERNMENT getGovernment() {
        return government;
    }

    public void setGovernment(GOVERNMENT government) {
        this.government = government;
    }

    public STATION_TYPE getType() {
        return type;
    }

    public void setType(STATION_TYPE type) {
        this.type = type;
    }

    public ECONOMIC_TYPE getEconomic() {
        return economic;
    }

    public void setEconomic(ECONOMIC_TYPE economic) {
        this.economic = economic;
    }

    public ECONOMIC_TYPE getSubEconomic() {
        return subEconomic;
    }

    public void setSubEconomic(ECONOMIC_TYPE subEconomic) {
        this.subEconomic = subEconomic;
    }

    public long getPlaceId() {
        return placeId;
    }

    public void setPlace(long placeId){
        this.placeId = placeId;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void add(SERVICE_TYPE service){
        services.add(service);
    }

    public void remove(SERVICE_TYPE service){
        services.remove(service);
    }

    public boolean has(SERVICE_TYPE service){
        return services.contains(service);
    }

    public Collection<SERVICE_TYPE> getServices() {
        return services;
    }

    public long getModified() {
        return modified;
    }

    public void setModified(long modified) {
        this.modified = modified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BDBVendor)) return false;

        BDBVendor bdbVendor = (BDBVendor) o;

        if (Double.compare(bdbVendor.distance, distance) != 0) return false;
        if (id != bdbVendor.id) return false;
        if (placeId != bdbVendor.placeId) return false;
        if (!name.equals(bdbVendor.name)) return false;
        if (!services.equals(bdbVendor.services)) return false;

        return Objects.equals(faction, bdbVendor.faction)
               && Objects.equals(government, bdbVendor.government)
               && Objects.equals(type, bdbVendor.type)
               && Objects.equals(economic, bdbVendor.economic)
               && Objects.equals(subEconomic, bdbVendor.subEconomic)
               && Objects.equals(modified, bdbVendor.modified);
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
