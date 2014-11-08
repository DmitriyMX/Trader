package ru.trader.model;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import ru.trader.graph.PathRoute;
import ru.trader.model.support.ModelBindings;

import java.util.List;

public class OrderModel {

    private final OfferModel offer;

    private final LongProperty count;
    private final ObjectProperty<OfferModel> buyer =  new SimpleObjectProperty<>();
    private LongProperty max;
    private DoubleProperty profit;
    private DoubleProperty distance;
    private DoubleProperty bestProfit;
    private PathRoute path;

    public OrderModel(OfferModel offer) {
        this.offer = offer;
        this.max = new SimpleLongProperty(0);
        this.count = new SimpleLongProperty(0){
            @Override
            public void setValue(Number v) {
                long limit = max.get();
                if (limit > 0 && v.longValue() > limit){
                    super.setValue(limit);
                } else {
                    super.setValue(v);
                }
            }
        };
    }

    public OrderModel(OfferModel sellOffer, OfferModel buyOffer, long max) {
        this(sellOffer);
        this.max.setValue(max);
        setBuyOffer(buyOffer);
        setCount(max);
    }

    public OrderModel(OfferModel offer, double balance, long limit) {
        this(offer);
        this.max.setValue(Math.min(offer.getCount(), Math.min(limit, (long) Math.floor(balance / offer.getPrice()))));
    }

    PathRoute getPath() {
        return path;
    }

    void setPath(PathRoute path) {
        this.path = path;
    }


    public OfferModel getOffer() {
        return offer;
    }

    public long getCount() {
        return count.get();
    }

    public void setCount(long count) {
        this.count.set(count);
    }

    public LongProperty countProperty() {
        return count;
    }

    public OfferModel getBuyOffer() {
        return buyer.get();
    }

    public void setBuyOffer(OfferModel buyer) {
        this.buyer.set(buyer);
        if (distance!=null) distance.set(getStation().getDistance(buyer.getStation()));
    }

    public ReadOnlyObjectProperty<OfferModel> buyOfferProperty() {
        return buyer;
    }

    public long getMax() {
        return max.get();
    }

    public void setMax(long max) {
        this.max.setValue(max);
    }


    public StationModel getStation() {
        return offer.getStation();
    }

    public ReadOnlyStringProperty nameProperty() {
        return offer.nameProperty();
    }

    public ReadOnlyDoubleProperty priceProperty() {
        return offer.priceProperty();
    }

    public ReadOnlyLongProperty supplyProperty() {
        return offer.countProperty();
    }

    public ReadOnlyObjectProperty<OfferModel> bestProperty(){
        return offer.bestBuyProperty();
    }

    public List<OfferModel> getBuyers(){
        return offer.getBuyer();
    }

    public StationModel getBuyer() {
        OfferModel buyOffer = getBuyOffer();
        return buyOffer != null ? buyer.get().getStation() : null;
    }

    public double getProfit() {
        return profitProperty().get();
    }

    public ObservableValue<Double> getProfit(OfferModel buyer) {
        return buyer.priceProperty().subtract(offer.priceProperty()).multiply(Bindings.min(max, buyer.countProperty())).asObject();
    }

    public ReadOnlyDoubleProperty profitProperty() {
        if (profit == null){
            profit = new SimpleDoubleProperty(0);
            profit.bind(ModelBindings.diff(buyer, offer.priceProperty()).multiply(count));
        }
        return profit;
    }

    public ReadOnlyDoubleProperty bestProfitProperty() {
        if (bestProfit == null){
            bestProfit = new SimpleDoubleProperty(0);
            bestProfit.bind(offer.profitProperty().multiply(max));
        }
        return bestProfit;
    }

    public ReadOnlyDoubleProperty distanceProperty() {
        if (distance == null){
            StationModel buyer = getBuyer();
            distance = new SimpleDoubleProperty(buyer!=null ? getStation().getDistance(buyer) : Double.NaN);
        }
        return distance;
    }

    public double getBalance(){
        return path != null ? path.getRoot().getBalance() : getMax() * offer.getPrice();
    }

}
