package com.restaurant.system.restaurantmanagingsystem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class HelloController {

    @FXML private TableView<MenuItem> menuTable;
    @FXML private TableColumn<MenuItem, String> nameColumn;
    @FXML private TableColumn<MenuItem, Double> priceColumn;
    @FXML private TableColumn<MenuItem, String> categoryColumn;

    // MEZŐK AZ ÚJ ÉTELHEZ (Hozzáadás fül)
    @FXML private TextField newNameField;
    @FXML private TextField newPriceField;
    @FXML private ComboBox<Category> newCategoryComboBox;

    // MEZŐK A SZERKESZTÉSHEZ (Kezelés fül)
    @FXML private TextField editNameField;
    @FXML private TextField editPriceField;
    @FXML private ComboBox<Category> editCategoryComboBox;

    public void initialize() {
        // Táblázat beállítása
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        // Mindkét ComboBox feltöltése
        ObservableList<Category> categories = FXCollections.observableArrayList(DatabaseConnection.getAllCategories());
        newCategoryComboBox.setItems(categories);
        editCategoryComboBox.setItems(categories);

        // Kiválasztás figyelése: Ha kattintasz a táblázatban, a SZERKESZTŐ mezőkbe tölti az adatot
        menuTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                editNameField.setText(newSelection.getName());
                editPriceField.setText(String.valueOf(newSelection.getPrice()));
                for (Category cat : editCategoryComboBox.getItems()) {
                    if (cat.getName().equals(newSelection.getCategoryName())) {
                        editCategoryComboBox.setValue(cat);
                        break;
                    }
                }
            }
        });

        // Szám-validátor mindkét ár mezőhöz
        setupNumberValidation(newPriceField);
        setupNumberValidation(editPriceField);

        loadMenuItems();
    }

    private void setupNumberValidation(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                textField.setText(oldValue);
            }
        });
    }

    private void loadMenuItems() {
        List<MenuItem> items = DatabaseConnection.getMenuItemsForGUI();
        menuTable.setItems(FXCollections.observableArrayList(items));
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    protected void onAddButtonClick() {
        try {
            String name = newNameField.getText();
            String priceText = newPriceField.getText();
            Category selectedCategory = newCategoryComboBox.getValue();

            if (validateAndProcess(name, priceText, selectedCategory)) {
                double price = Double.parseDouble(priceText);
                if (DatabaseConnection.addMenuItem(name, price, selectedCategory.getId())) {
                    showAlert("Siker", "Étel hozzáadva!", Alert.AlertType.INFORMATION);
                    newNameField.clear();
                    newPriceField.clear();
                    newCategoryComboBox.setValue(null);
                    loadMenuItems();
                }
            }
        } catch (Exception e) {
            showAlert("Hiba", "Váratlan hiba történt!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    protected void onUpdateButtonClick() {
        MenuItem selectedItem = menuTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("Hiba", "Válassz ki egy elemet a táblázatban!", Alert.AlertType.WARNING);
            return;
        }

        String name = editNameField.getText();
        String priceText = editPriceField.getText();
        Category selectedCategory = editCategoryComboBox.getValue();

        if (validateAndProcess(name, priceText, selectedCategory)) {
            double price = Double.parseDouble(priceText);
            if (DatabaseConnection.updateMenuItem(selectedItem.getId(), name, price, selectedCategory.getId())) {
                showAlert("Siker", "Tétel frissítve!", Alert.AlertType.INFORMATION);
                loadMenuItems();
            }
        }
    }

    private boolean validateAndProcess(String name, String priceText, Category category) {
        if (name.isEmpty() || priceText.isEmpty() || category == null) {
            showAlert("Hiba", "Minden mező kitöltése kötelező!", Alert.AlertType.WARNING);
            return false;
        }
        if (Double.parseDouble(priceText) <= 0) {
            showAlert("Hiba", "Az árnak pozitívnak kell lennie!", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    @FXML
    protected void onDeleteButtonClick() {
        MenuItem selectedItem = menuTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("Figyelem", "Válassz ki egy elemet!", Alert.AlertType.WARNING);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Biztosan törlöd: " + selectedItem.getName() + "?", ButtonType.OK, ButtonType.CANCEL);
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            DatabaseConnection.deleteMenuItem(selectedItem.getId());
            loadMenuItems();
            showAlert("Siker", "Törölve!", Alert.AlertType.INFORMATION);
        }
    }
}