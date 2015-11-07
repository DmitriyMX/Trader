package ru.trader.view.support;

import com.sun.javafx.scene.control.skin.TableViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;

public class ViewUtils {

    //Scroll to row if invisible
    public static void show(TableView tableView, int index){
        ObservableList kids = ((TableViewSkin) tableView.getSkin()).getChildren();
        if (kids == null || kids.isEmpty()) { return; }
        VirtualFlow flow = (VirtualFlow)kids.get(1);
        flow.show(index);
    }

    // Edit next cell
    public static <S> void editNext(TableView<S> tableView){
        TableView.TableViewSelectionModel<S> sm = tableView.getSelectionModel();
        if (!sm.isCellSelectionEnabled()) return;
        sm.selectNext();
        ObservableList<TablePosition> pos = sm.getSelectedCells();
        for (TablePosition p : pos) {
            if (p.getTableColumn().isEditable()) {
                show(tableView, p.getRow() > 0 ? p.getRow() : 0);
                //noinspection unchecked
                tableView.edit(p.getRow(), p.getTableColumn());
                return;
            }
        }
        editNext(tableView);
    }

    public static <T extends Event> EventHandler<T> joinHandlers(EventHandler<T> first, EventHandler<T> second){
        return (e -> {
            first.handle(e);
            second.handle(e);
        });
    }

    public static  <S,T> void addOnEditCommit(TableColumn<S, T> column, EventHandler<TableColumn.CellEditEvent<S, T>> eventHandler){
        final EventHandler<TableColumn.CellEditEvent<S, T>> oldHandler = column.getOnEditCommit();
        column.setOnEditCommit(joinHandlers(oldHandler, eventHandler));
    }

    public static void doFX(Runnable runnable){
        if (Platform.isFxApplicationThread()){
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

    public static String timeToString(long time){
        return String.format("%d:%02d:%02d", time/3600, (time%3600)/60, (time%60));
    }

    public static String distanceToString(double distance){
        if (distance < 0.01) return String.format("%.0f Ls", distance / 0.00000003169);
        return String.format("%.2f LY", distance);
    }

    public static String stationDistanceToString(double distance){
        return String.format("%.2f Ls", distance);
    }
}
