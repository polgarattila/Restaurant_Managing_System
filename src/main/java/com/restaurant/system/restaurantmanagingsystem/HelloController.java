package com.restaurant.system.restaurantmanagingsystem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

    @FXML
    private TextField nameField;
    @FXML
    private TextField priceField;
    @FXML
    private ComboBox<Category> categoryComboBox;

    public void initialize() {
        // Oszlopok összekötése a MenuItem mezőivel
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        // ComboBox feltöltése kategóriákkal
        categoryComboBox.setItems(FXCollections.observableArrayList(DatabaseConnection.getAllCategories()));

        // Táblázat feltöltése ételekkel
        loadMenuItems();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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
            String priceText = priceField.getText();
            Category selectedCategory = categoryComboBox.getValue();

            if (name.isEmpty() || priceText.isEmpty() || selectedCategory == null) {
                showAlert("Hiba", "Kérlek tölts ki minden mezőt!", Alert.AlertType.WARNING);
                return;
            }

            double price = Double.parseDouble(priceText);
            boolean success = DatabaseConnection.addMenuItem(name, price, selectedCategory.getId());

            if (success) {
                showAlert("Siker", "Étel sikeresen hozzáadva!", Alert.AlertType.INFORMATION);
                nameField.clear();
                priceField.clear();
                categoryComboBox.setValue(null);
                loadMenuItems();
            } else {
                showAlert("Hiba", "Nem sikerült a mentés az adatbázisba!", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            showAlert("Hiba", "Az ár formátuma nem megfelelő!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    protected void onDeleteButtonClick() {
        MenuItem selectedItem = menuTable.getSelectionModel().getSelectedItem();

        // 1. Ellenőrizzük, van-e kijelölés
        if (selectedItem == null) {
            showAlert("Figyelem", "Válassz ki egy elemet a törléshez!", Alert.AlertType.WARNING);
            return;
        }

        // 2. Megerősítő kérő ablak (CONFIRMATION)
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Törlés megerősítése");
        alert.setHeaderText(selectedItem.getName() + " törlése");
        alert.setContentText("Biztosan el akarod távolítani ezt a tételt az étlapról?");

        // 3. Megvárjuk a gombnyomást
        var result = alert.showAndWait();

        // 4. Csak akkor törlünk, ha az OK gombra kattintott
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DatabaseConnection.deleteMenuItem(selectedItem.getId());
            showAlert("Siker", "Az elem törölve lett!", Alert.AlertType.INFORMATION);
            loadMenuItems();
        }
    }


}