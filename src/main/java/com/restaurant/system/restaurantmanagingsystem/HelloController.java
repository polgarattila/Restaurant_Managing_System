package com.restaurant.system.restaurantmanagingsystem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class HelloController {

    @FXML
    private TableView<MenuItem> menuTable;

    @FXML
    private TableColumn<MenuItem, String> nameColumn;

    @FXML
    private TableColumn<MenuItem, Double> priceColumn;

    @FXML
    private TableColumn<MenuItem, String> categoryColumn;

    public void initialize() {
        // Megmondjuk az oszlopoknak, hogy a MenuItem melyik mezőjét mutassák
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        // Adatok betöltése az adatbázisból
        loadMenuItems();
    }

    private void loadMenuItems() {
        List<MenuItem> items = DatabaseConnection.getMenuItemsForGUI();
        ObservableList<MenuItem> observableList = FXCollections.observableArrayList(items);
        menuTable.setItems(observableList);
    }
}