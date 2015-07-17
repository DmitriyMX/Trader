package ru.trader.controllers;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import ru.trader.Main;
import ru.trader.World;
import ru.trader.maddavo.Parser;
import ru.trader.model.*;
import ru.trader.view.support.Localization;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class MainController {
    private final static Logger LOG = LoggerFactory.getLogger(MainController.class);

    private static MarketModel world = new MarketModel(World.getMarket());
    private static MarketModel market = world;


    @FXML
    private BorderPane mainPane;

    @FXML
    private Menu langs;

    @FXML
    private OffersController offersController;
    @FXML
    private ItemsController itemsController;
    @FXML
    private RouterController routerController;

    @FXML
    private void initialize() {
        fillLangs();
    }

    private void fillLangs() {
        ToggleGroup toggleGroup = new ToggleGroup();
        for (Locale locale : Localization.getLocales()) {
            ResourceBundle rb = Localization.getResources(locale);
            RadioMenuItem mi = new RadioMenuItem(rb.getString("main.menu.settings.language.item"));
            mi.setToggleGroup(toggleGroup);
            mi.setUserData(locale);
            if (locale.equals(Localization.getCurrentLocale())) mi.setSelected(true);
            langs.getItems().add(mi);
        }
        toggleGroup.selectedToggleProperty().addListener((cb, o, n) -> {
            try {
                if (n != null) {
                    Main.changeLocale((Locale) n.getUserData());
                    world.refresh();
                }
            } catch (IOException e) {
                LOG.error("Error on change locale to {}", n.getUserData());
                LOG.error("",e);
            }
        });
    }


    public OffersController getOffersController() {
        return offersController;
    }

    public BorderPane getMainPane(){
        return mainPane;
    }

    public static MarketModel getMarket() {
        return market;
    }
    public static MarketModel getWorld() {
        return world;
    }

    public void setMarket(MarketModel market) {
        MainController.market = market;
        Screeners.reinitAll();
    }

    void init(){
        itemsController.init();
        offersController.init();
        routerController.init();
    }

    public void save(ActionEvent actionEvent) {
        try {
            World.save();
        } catch (FileNotFoundException | UnsupportedEncodingException | XMLStreamException e) {
            LOG.error("Error on save file",e);
        }
    }

    public void importWorld(ActionEvent actionEvent) {
        try {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML bd files (*.xml)", "*.xml");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setInitialDirectory(new File("."));
            File file = fileChooser.showOpenDialog(null);
            if (file !=null) {
                World.impXml(file);
                reload();
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOG.error("Error on import file", e);
        }
    }

    public void exportWorld(ActionEvent actionEvent) {
        try {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML bd files (*.xml)", "*.xml");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setInitialDirectory(new File("."));
            File file = fileChooser.showSaveDialog(null);
            if (file !=null) {
                World.saveTo(file);
                reload();
            }
        } catch (FileNotFoundException | UnsupportedEncodingException | XMLStreamException e) {
            LOG.error("Error on save as file", e);
        }
    }

    public void clear(ActionEvent actionEvent){
        Action res = Screeners.showConfirm(String.format(Localization.getString("dialog.confirm.remove"), Localization.getString("market.all")));
        if (res == Dialog.ACTION_YES) {
            market.clear();
            reload();
        }
    }

    public void clearOffers(ActionEvent actionEvent){
        Action res = Screeners.showConfirm(String.format(Localization.getString("dialog.confirm.remove"), Localization.getString("market.offers")));
        if (res == Dialog.ACTION_YES) {
            market.clearOffers();
            reload();
        }
    }

    public void clearStations(ActionEvent actionEvent){
        Action res = Screeners.showConfirm(String.format(Localization.getString("dialog.confirm.remove"), Localization.getString("market.stations")));
        if (res == Dialog.ACTION_YES) {
            market.clearStations();
            reload();
        }
    }

    public void clearSystems(ActionEvent actionEvent){
        Action res = Screeners.showConfirm(String.format(Localization.getString("dialog.confirm.remove"), Localization.getString("market.systems")));
        if (res == Dialog.ACTION_YES) {
            market.clearSystems();
            reload();
        }
    }

    public void clearItems(ActionEvent actionEvent){
        Action res = Screeners.showConfirm(String.format(Localization.getString("dialog.confirm.remove"), Localization.getString("market.items")));
        if (res == Dialog.ACTION_YES) {
            market.clearItems();
            reload();
        }
    }

    public void clearGroups(ActionEvent actionEvent){
        Action res = Screeners.showConfirm(String.format(Localization.getString("dialog.confirm.remove"), Localization.getString("market.groups")));
        if (res == Dialog.ACTION_YES) {
            market.clearGroups();
            reload();
        }
    }

    public Optional<GroupModel> addGroup(){
        GroupModel group = Screeners.showAddGroup(market);
        return Optional.ofNullable(group);
    }

    public Optional<ItemModel> addItem(){
        ItemModel item = Screeners.showAddItem(market);
        return Optional.ofNullable(item);
    }


    public void addSystem(ActionEvent actionEvent){
        Screeners.showSystemsEditor(null);
    }

    public void editSystem(ActionEvent actionEvent){
        SystemModel system = offersController.getSystem();
        if (system != null) {
            Screeners.showSystemsEditor(system);
        }
    }

    public void removeSystem(ActionEvent actionEvent){
        SystemModel system = offersController.getSystem();
        if (system != null) {
            Action res = Screeners.showConfirm(String.format(Localization.getString("dialog.confirm.remove"), system.getName()));
            if (res == Dialog.ACTION_YES) {
                market.remove(system);
            }
        }
    }

    public void addStation(ActionEvent actionEvent) {
        SystemModel system = offersController.getSystem();
        if (system != null){
            Screeners.showAddStation(offersController.getSystem());
        }
    }

    public void editStation(ActionEvent actionEvent) {
        StationModel station = offersController.getStation();
        if (station != null) {
            Screeners.showEditStation(offersController.getStation());
        }
    }

    public void removeStation(ActionEvent actionEvent){
        StationModel station = offersController.getStation();
        if (station != null) {
            Action res = Screeners.showConfirm(String.format(Localization.getString("dialog.confirm.remove"), station.getName()));
            if (res == Dialog.ACTION_YES) {
                station.getSystem().remove(station);
            }
        }
    }

    public void editSettings(){
        Screeners.showSettings();
    }

    public void editFilter(){
        //TODO: implement
        //Screeners.showFilter(market.getFilter());
    }

    public void impMadSystems(ActionEvent actionEvent) {
        chooseFile(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"), file -> {
            try {
                Parser.parseSystems(file, World.getMarket());
                reload();
            } catch (IOException e) {
                LOG.error("Error on import file", e);
            }
        });
    }

    public void impMadStations(ActionEvent actionEvent) {
        chooseFile(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"), file -> {
            try {
                Parser.parseStations(file, World.getMarket());
                reload();
            } catch (IOException e) {
                LOG.error("Error on import file", e);
            }
        });
    }

    public void impMadOffers(ActionEvent actionEvent) {
        chooseFile(new FileChooser.ExtensionFilter("Prices files (*.prices)", "*.prices"), file -> {
            try {
                Parser.parsePrices(file, World.getMarket());
                reload();
            } catch (IOException e) {
                LOG.error("Error on import file", e);
            }
        });
    }

    private void chooseFile(FileChooser.ExtensionFilter filter, Consumer<File> action) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(filter);
        fileChooser.setInitialDirectory(new File("."));
        File file = fileChooser.showOpenDialog(null);
        if (file !=null) {
            action.accept(file);
        }
    }



    private void reload(){
        if (world != null) world.getModeler().clear();
        world = new MarketModel(World.getMarket());
        market = world;
        Screeners.reinitAll();
    }
}
