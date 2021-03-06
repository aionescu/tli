package com.aionescu.tli.view.gui;

import java.util.function.Function;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;

import com.aionescu.tli.utils.Pair;

public interface GUIWindow {
  static final String _darkThemeStylesheet = "file:JavaFXResources/modena-dark.css";

  Parent view();

  static void showErrorAlert(String title, String content) {
    var alert = new Alert(AlertType.ERROR);
    alert.getDialogPane().getStylesheets().add(_darkThemeStylesheet);

    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);

    alert.showAndWait();
  }

  static Label mkLabel(String text) {
    var label = new Label(text);
    label.setFont(new Font("Fira Code Regular", 14));

    return label;
  }

  static Button mkButton(String text, Runnable handler) {
    var button = new Button(text);
    button.setFont(new Font("Fira Code Regular", 14));
    button.setOnAction(e -> handler.run());

    return button;
  }

  @SuppressWarnings("unchecked")
  static <A> ListView<A> mkListView(boolean enableSelection) {
    var list = new ListView<A>();

    if (!enableSelection)
      list.setSelectionModel(new IgnoreSelectionModel<>());

    list.setCellFactory(p -> (ListCell<A>)new ListCell<Object>() {
      @Override
      protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);
        setFont(new Font("Fira Code Regular", 16));
        setText(empty ? "" : item.toString());
      }
    });

    return list;
  }

  @SuppressWarnings("unchecked")
  private static <T, C> Callback<TableColumn<T, C>, TableCell<T, C>> _tableViewCellFactory() {
    return p -> (TableCell<T, C>)new TableCell<Object, Object>() {
      @Override
      public void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);
        setFont(new Font("Fira Code Regular", 16));
        setText(empty ? "" : item.toString());
      }
    };
  }

  static <T> TableView<T> mkTableView() {
    var table = new TableView<T>();

    table.setSelectionModel(new IgnoreTableViewSelectionModel<>(table));
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    return table;
  }

  static <T, C> void addColumn(TableView<T> table, String columnName, Function<T, C> f) {
    var col = new TableColumn<T, C>(columnName);

    col.setCellValueFactory(v -> new ReadOnlyObjectWrapper<>(f.apply(v.getValue())));
    col.setCellFactory(_tableViewCellFactory());

    table.getColumns().add(col);
  }

  static <A, B> TableView<Pair<A, B>> mkPairTableView(String fstName, String sndName) {
    TableView<Pair<A, B>> table = mkTableView();

    addColumn(table, fstName, Pair::fst_);
    addColumn(table, sndName, Pair::snd_);

    return table;
  }

  static void runChild(Stage parentStage, Function<Stage, ? extends GUIWindow> ctor) {
    var childStage = new Stage();
    childStage.initOwner(parentStage);

    var window = ctor.apply(childStage);
    var view = window.view();

    var scene = new Scene(view);
    scene.getStylesheets().add(_darkThemeStylesheet);

    childStage.setScene(scene);
    childStage.showAndWait();
  }
}
