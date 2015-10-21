package ru.trader;

import javafx.beans.property.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Market;
import ru.trader.core.MarketFilter;
import ru.trader.core.Profile;
import ru.trader.core.Ship;

import java.io.*;
import java.util.Locale;
import java.util.Properties;

public class Settings {
    private final static Logger LOG = LoggerFactory.getLogger(Settings.class);

    private final Properties values = new Properties();
    private final File file;
    private Profile profile;
    private final EDCESettings edce;


    public Settings() {
        this.file = null;
        profile = new Profile(new Ship());
        edce = new EDCESettings();
    }

    public Settings(File file) {
        this.file = file;
        profile = new Profile(new Ship());
        edce = new EDCESettings();
    }

    public void load(Market market) {
        try (InputStream is = new FileInputStream(file)) {
            values.load(is);
        } catch (FileNotFoundException e) {
            LOG.warn("File {} not found", file);
        } catch (IOException e) {
            LOG.error("Error on load settings", e);
        }
        profile = Profile.readFrom(values, market);
        edce.readFrom(values);
    }

    public void save(){
        try (OutputStream os = new FileOutputStream(file)) {
            profile.writeTo(values);
            edce.writeTo(values);
            values.store(os, "settings");
        } catch (IOException e) {
            LOG.error("Error on load settings", e);
        }
    }

    public void setLocale(Locale locale){
        values.setProperty("locale", locale.toLanguageTag());
    }

    public Locale getLocale(){
        String locale = values.getProperty("locale");
        return locale != null ? Locale.forLanguageTag(locale): null;
    }

    public void setEMDNActive(boolean active){
        values.setProperty("emdn.active", active ? "1":"0");
    }

    public boolean getEMDNActive(){
        return !"0".equals(values.getProperty("emdn.active","0"));
    }

    public void setEMDNSub(String address){
        values.setProperty("emdn.sub", address);
    }

    public String getEMDNSub(){
        return values.getProperty("emdn.sub","tcp://firehose.elite-market-data.net:9050");
    }

    public void setEMDNUpdateOnly(boolean updateOnly){
        values.setProperty("emdn.updateOnly", updateOnly ? "1":"0");
    }

    public boolean getEMDNUpdateOnly(){
        return !"0".equals(values.getProperty("emdn.updateOnly","1"));
    }

    public void setEMDNAutoUpdate(long autoUpdate){
        values.setProperty("emdn.auto", String.valueOf(autoUpdate));
    }

    public long getEMDNAutoUpdate(){
        return Long.valueOf(values.getProperty("emdn.auto", "0"));
    }

    public void setBalance(double balance){
        profile.setBalance(balance);
    }

    public double getBalance(){
        return profile.getBalance();
    }

    public void setCargo(int cargo){
        profile.getShip().setCargo(cargo);
    }

    public int getCargo(){
        return profile.getShip().getCargo();
    }

    public void setTank(double tank){
        profile.getShip().setTank(tank);
    }

    public double getTank(){
        return profile.getShip().getTank();
    }

    public void setJumps(int jumps){
        profile.setJumps(jumps);
    }

    public int getJumps(){
        return profile.getJumps();
    }

    public void setRoutesCount(int routesCount){
        profile.setRoutesCount(routesCount);
    }

    public int getRoutesCount(){
        return profile.getRoutesCount();
    }

    public MarketFilter getFilter(Market market){
        return MarketFilter.buildFilter(values, market);
    }

    public void setFilter(MarketFilter filter){
        filter.writeTo(values);
    }

    public Profile getProfile() {
        return profile;
    }

    public EDCESettings getEdce(){
        return edce;
    }

    public final class EDCESettings {
        private final BooleanProperty active;
        private final StringProperty email;
        private final IntegerProperty interval;

        public EDCESettings() {
            interval = new SimpleIntegerProperty();
            email = new SimpleStringProperty();
            active = new SimpleBooleanProperty();
        }

        public boolean isActive() {
            return active.get();
        }

        public BooleanProperty activeProperty() {
            return active;
        }

        public void setActive(boolean active) {
            this.active.set(active);
        }

        public String getEmail() {
            return email.get();
        }

        public StringProperty emailProperty() {
            return email;
        }

        public void setEmail(String email) {
            this.email.set(email);
        }

        public int getInterval() {
            return interval.get();
        }

        public IntegerProperty intervalProperty() {
            return interval;
        }

        public void setInterval(int interval) {
            this.interval.set(interval);
        }

        public void readFrom(Properties values){
            setActive(!"0".equals(values.getProperty("edce.active", "0")));
            setEmail(values.getProperty("edce.mail", "example@mail.com"));
            setInterval(Integer.valueOf(values.getProperty("edce.interval", "20")));
        }

        public void writeTo(Properties values){
            values.setProperty("edce.active", isActive() ? "1":"0");
            values.setProperty("edce.mail", getEmail());
            values.setProperty("edce.interval", String.valueOf(getInterval()));
        }


    }
}
