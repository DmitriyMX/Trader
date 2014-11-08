package ru.trader.model;

import javafx.beans.property.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Item;
import ru.trader.core.OFFER_TYPE;
import ru.trader.view.support.Localization;

import java.util.List;

public class ItemModel {
    private final static Logger LOG = LoggerFactory.getLogger(ItemModel.class);

    private final Item item;
    private StringProperty name;

    private final ItemStatModel statSell;
    private final ItemStatModel statBuy;

    ItemModel(Item item, MarketModel market) {
        this.item = item;
        this.statSell = new ItemStatModel(market.getStat(OFFER_TYPE.SELL, item), market);
        this.statBuy = new ItemStatModel(market.getStat(OFFER_TYPE.BUY, item), market);
    }

    Item getItem(){
        return item;
    }

    public String getId() {return item.getName();}

    public String getName() {return name != null ? name.get() : item.getName();}

    public void setName(String value) {
        LOG.info("Change name of item {} to {}", item, value);
        item.setName(value);
        if (name != null) name.set(value);
    }

    public ReadOnlyStringProperty nameProperty() {
        if (name == null) {
            String lName = Localization.getString("item." + item.getName(), item.getName());
            name = new SimpleStringProperty(lName);
        }
        return name;
    }

    public ReadOnlyDoubleProperty avgBuyProperty() {
        return statBuy.avgProperty();
    }

    public ReadOnlyObjectProperty<OfferModel> minBuyProperty() {
        return statBuy.minProperty();
    }

    public ReadOnlyObjectProperty<OfferModel> maxBuyProperty() {
        return statBuy.maxProperty();
    }

    public ReadOnlyObjectProperty<OfferModel> bestBuyProperty() {
        return statBuy.bestProperty();
    }

    public ReadOnlyDoubleProperty avgSellProperty() {
        return statSell.avgProperty();
    }

    public ReadOnlyObjectProperty<OfferModel> minSellProperty() {
        return statSell.minProperty();
    }

    public ReadOnlyObjectProperty<OfferModel> maxSellProperty() {
        return statSell.maxProperty();
    }

    public ReadOnlyObjectProperty<OfferModel> bestSellProperty() {
        return statSell.bestProperty();
    }

    public List<OfferModel> getSeller() {
        return statSell.getOffers();
    }

    public List<OfferModel> getBuyer() {
        return statBuy.getOffers();
    }

    public boolean isMarketItem(){
        return item.getGroup() != null && item.getGroup().isMarket();
    }

    public void refresh(){
        LOG.trace("Refresh stats of itemDesc {}", this);
        statBuy.refresh();
        statSell.refresh();
    }

    public void refresh(OFFER_TYPE type){
        LOG.trace("Refresh {} stat of itemDesc {}", type, this);
        switch (type) {
            case SELL: statSell.refresh();
                break;
            case BUY: statBuy.refresh();
                break;
        }
    }

    @Override
    public String toString() {
        if (LOG.isTraceEnabled()){
            final StringBuilder sb = new StringBuilder("ItemModel{");
            sb.append("nameProp=").append(name);
            sb.append(", item=").append(item.toString());
            sb.append(", statSell=").append(statSell.toString());
            sb.append(", statBuy=").append(statBuy.toString());
            sb.append('}');
            return sb.toString();
        }
        return item.toString();
    }
}

