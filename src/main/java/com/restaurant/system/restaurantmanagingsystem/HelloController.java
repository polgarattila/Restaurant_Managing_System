package com.restaurant.system.restaurantmanagingsystem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextField;

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

    @FXML
    private TextField nameField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField categoryIdField;

    @FXML
    private ComboBox<Category> categoryComboBox;

    public void initialize() {
        // Megmondjuk az oszlopoknak, hogy a MenuItem melyik mezőjét mutassák
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        categoryComboBox.setItems(FXCollections.observableArrayList(DatabaseConnection.getAllCategories()));

        // Adatok betöltése az adatbázisból
        loadMenuItems();
    }

    private void loadMenuItems() {
        List<MenuItem> items = DatabaseConnection.getMenuItemsForGUI();
        ObservableList<MenuItem> observableList = FXCollections.observableArrayList(items);
        menuTable.setItems(observableList);
    }

    @FXML
    protected void onAddButtonClick() {
        try {
            String name = nameField.getText();
            double price = Double.parseDouble(priceField.getText());

            // Itt kérjük le a kiválasztott kategória objektumot
            Category selectedCategory = categoryComboBox.getValue();

            if (selectedCategory != null) {
                DatabaseConnection.addMenuItem(name, price, selectedCategory.getId());

                nameField.clear();
                priceField.clear();
                categoryComboBox.setValue(null);
                loadMenuItems();
            }
        } catch (NumberFormatException e) {
            System.out.println("Hiba az adatok formátumában!");
        }
    }
}