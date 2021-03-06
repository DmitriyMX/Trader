package ru.trader.store.berkeley.dao;

import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import ru.trader.store.berkeley.entities.BDBVendor;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

public class VendorDA<T> {
    private final PrimaryIndex<Long, BDBVendor> indexById;
    private final SecondaryIndex<Long, Long, BDBVendor> indexByPlaceId;
    private final Function<BDBVendor,T> convertFunc;

    public VendorDA(EntityStore store, Function<BDBVendor, T> convertFunc) {
        this.convertFunc = convertFunc;
        this.indexById = store.getPrimaryIndex(Long.class, BDBVendor.class);
        this.indexByPlaceId = store.getSecondaryIndex(indexById, Long.class, "placeId");
    }

    public T get(long id){
        return DAUtils.get(indexById, id, convertFunc);
    }

    public Collection<T> getAll(){
        return DAUtils.getAll(indexById, convertFunc);
    }

    public Collection<T> getAllByPlace(long placeId){
        return DAUtils.getAll(indexByPlaceId, placeId, convertFunc);
    }

    public Collection<BDBVendor> getAllVendors(){
        return DAUtils.getAll(indexById, v -> v);
    }

    public Collection<String> getNamesByPlace(long placeId){
        return DAUtils.getAll(indexByPlaceId, placeId, BDBVendor::getName);
    }


    public boolean contains(long placeId){
        return indexByPlaceId.contains(placeId);
    }

    public boolean contains(long placeId, Predicate<BDBVendor> filter) {
        return !DAUtils.getAll(indexByPlaceId, placeId, v -> v, filter).isEmpty();
    }

    public long count(long placeId){
        return indexByPlaceId.subIndex(placeId).count();
    }

    public BDBVendor put(BDBVendor vendor){
        return indexById.put(vendor);
    }

    public void update(BDBVendor vendor){
        indexById.putNoReturn(vendor);
    }

    public void delete(BDBVendor vendor){
        indexById.delete(vendor.getId());
    }
}
