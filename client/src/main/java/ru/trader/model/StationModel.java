package ru.trader.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.OFFER_TYPE;
import ru.trader.core.Offer;
import ru.trader.core.Vendor;

import java.util.*;
import java.util.stream.Collectors;

public class StationModel {
    private final static Logger LOG = LoggerFactory.getLogger(StationModel.class);
    private final Vendor station;
    private final MarketModel market;

    private OfferModel asModel(Offer offer){
        return market.getModeler().get(offer);
    }

    private OfferModel asModel(Offer offer, ItemModel item){
        return market.getModeler().get(offer, item);
    }

    StationModel(Vendor station, MarketModel market) {
        this.station = station;
        this.market = market;
    }

    Vendor getStation() {
        return station;
    }

    public String getName() {return station.getName();}

    public void setName(String value) {
        if (getName().equals(value)) return;
        LOG.info("Change name station {} to {}", station, value);
        station.setName(value);
    }

    public double getDistance(){
        return station.getDistance();
    }

    public void setDistance(double value){
        if (getDistance() == value) return;
        LOG.info("Change distance station {} to {}", station, value);
        station.setDistance(value);
    }

    public SystemModel getSystem(){
        return market.getModeler().get(station.getPlace());
    }

    public List<OfferModel> getSells() {
        return station.getAllSellOffers().stream().map(this::asModel).collect(Collectors.toList());
    }

    public List<OfferModel> getBuys() {
        return station.getAllBuyOffers().stream().map(this::asModel).collect(Collectors.toList());
    }

    public OfferModel add(OFFER_TYPE type, ItemModel item, double price, long count){
        OfferModel offer = asModel(station.addOffer(type, item.getItem(), price, count), item);
        LOG.info("Add offer {} to station {}", offer, station);
        offer.refresh();
        market.getNotificator().sendAdd(offer);
        return offer;
    }

    public void remove(OfferModel offer) {
        LOG.info("Remove offer {} from station {}", offer, station);
        station.remove(offer.getOffer());
        offer.refresh();
        market.getNotificator().sendRemove(offer);
    }

    public boolean hasSell(ItemModel item) {
        return station.hasSell(item.getItem());
    }

    public boolean hasBuy(ItemModel item) {
        return station.hasBuy(item.getItem());
    }

    public double getDistance(StationModel other){
        return station.getPlace().getDistance(other.station.getPlace());
    }

    @Override
    public String toString() {
        if (LOG.isTraceEnabled()){
            final StringBuilder sb = new StringBuilder("StationModel{");
            sb.append(station.toString());
            sb.append('}');
            return sb.toString();
        }
        return station.toString();
    }

}
