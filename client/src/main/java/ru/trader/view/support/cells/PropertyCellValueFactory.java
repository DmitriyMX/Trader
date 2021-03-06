package ru.trader.view.support.cells;

import com.sun.javafx.property.PropertyReference;
import javafx.beans.NamedArg;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import ru.trader.view.support.PropertyFactory;

public abstract class PropertyCellValueFactory<S,T,V> extends PropertyFactory<S, T> implements Callback<TableColumn.CellDataFeatures<T, V>, ObservableValue<V>> {

    public PropertyCellValueFactory(@NamedArg("property") String property) {
        super(property);
    }



    @Override
    public ObservableValue<V> call(TableColumn.CellDataFeatures<T, V> param) {
        return getCellValue(param.getValue());
    }

    abstract ObservableValue<V> format(ObservableValue<S> value);

    private ObservableValue<V> getCellValue(final T rowData){
        ObservableValue<S> value = null;
        final PropertyReference<S> prop = getPropertyRef(rowData);
        if (prop!=null){
            if (prop.hasProperty()) value = prop.getProperty(rowData);
            else if (prop.isWritable()) {
                value = new SimpleObjectProperty<>(prop.get(rowData));
                value.addListener((ov , o, n) -> prop.set(rowData, n));
            } else
                value = new ReadOnlyObjectWrapper<>(prop.get(rowData));
        }
        if (value == null) return null;
        return format(value);
    }

}