package com.restaurant.system.restaurantmanagingsystem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.List;

public class HelloController {

    @FXML private TableView<MenuItem> menuTable;
    @FXML private TableColumn<MenuItem, String> nameColumn;
    @FXML private TableColumn<MenuItem, Double> priceColumn;
    @FXML private TableColumn<MenuItem, String> categoryColumn;

    @FXML private TextField searchField; // Keresőmező

    // MEZŐK AZ ÚJ ÉTELHEZ
    @FXML private TextField newNameField;
    @FXML private TextField newPriceField;
    @FXML private ComboBox<Category> newCategoryComboBox;

    // MEZŐK A SZERKESZTÉSHEZ
    @FXML private TextField editNameField;
    @FXML private TextField editPriceField;
    @FXML private ComboBox<Category> editCategoryComboBox;

    // STATISZTIKA LABEL-EK
    @FXML private Label totalItemsLabel;
    @FXML private Label avgPriceLabel;
    @FXML private Label maxPriceLabel;

    @FXML private ListView<Category> categoryListView;
    @FXML private TextField categoryNameField;
    @FXML private ComboBox<Category> parentCategoryComboBox;

    // Ez a lista tárolja az összes adatot a memóriában
    private ObservableList<MenuItem> masterData = FXCollections.observableArrayList();

    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        ObservableList<Category> categories = FXCollections.observableArrayList(DatabaseConnection.getAllCategories());
        newCategoryComboBox.setItems(categories);
        editCategoryComboBox.setItems(categories);

        // --- KERESÉS ÉS SZŰRÉS LOGIKA ---
        FilteredList<MenuItem> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(menuItem -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (menuItem.getName().toLowerCase().contains(lowerCaseFilter)) return true;
                return menuItem.getCategoryName().toLowerCase().contains(lowerCaseFilter);
            });
            refreshStatistics(); // Szűrésnél is frissülhet a stat (opcionális)
        });
        SortedList<MenuItem> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(menuTable.comparatorProperty());
        menuTable.setItems(sortedData);

        // Kiválasztás figyelése
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

        setupNumberValidation(newPriceField);
        setupNumberValidation(editPriceField);

        // Kategória lista kijelölésének figyelése
        categoryListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                categoryNameField.setText(newSelection.getName());
                // Beállítjuk a szülőt a ComboBox-ban (ha van)
                if (newSelection.getParentId() != null) {
                    for (Category cat : parentCategoryComboBox.getItems()) {
                        if (cat.getId() == newSelection.getParentId()) {
                            parentCategoryComboBox.setValue(cat);
                            break;
                        }
                    }
                } else {
                    parentCategoryComboBox.setValue(null);
                }
            }
        });

        refreshCategoryLists();

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
        masterData.setAll(items); // Frissítjük a központi listát
        refreshStatistics();      // Automatikus statisztika frissítés
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
            showAlert("Hiba", "Hiba történt!", Alert.AlertType.ERROR);
        }
    }

    @FXML
    protected void onUpdateButtonClick() {
        MenuItem selectedItem = menuTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("Hiba", "Válassz ki egy elemet!", Alert.AlertType.WARNING);
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
            showAlert("Hiba", "Minden mező kötelező!", Alert.AlertType.WARNING);
            return false;
        }
        try {
            if (Double.parseDouble(priceText) <= 0) {
                showAlert("Hiba", "Az árnak pozitívnak kell lennie!", Alert.AlertType.WARNING);
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    @FXML
    protected void onDeleteButtonClick() {
        MenuItem selectedItem = menuTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Törlöd: " + selectedItem.getName() + "?", ButtonType.OK, ButtonType.CANCEL);
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            DatabaseConnection.deleteMenuItem(selectedItem.getId());
            loadMenuItems();
        }
    }

    @FXML
    public void refreshStatistics() {
        if (masterData.isEmpty()) {
            totalItemsLabel.setText("0");
            avgPriceLabel.setText("0 Ft");
            maxPriceLabel.setText("-");
            return;
        }

        int total = masterData.size();
        double sum = 0;
        MenuItem expensive = masterData.get(0);

        for (MenuItem item : masterData) {
            sum += item.getPrice();
            if (item.getPrice() > expensive.getPrice()) {
                expensive = item;
            }
        }

        totalItemsLabel.setText(String.valueOf(total));
        avgPriceLabel.setText(String.format("%.0f Ft", sum / total));
        maxPriceLabel.setText(expensive.getName() + " (" + (int)expensive.getPrice() + " Ft)");
    }

    @FXML
    protected void onAddCategoryClick() {
        String name = categoryNameField.getText();
        Category parent = parentCategoryComboBox.getValue(); // Lehet null!

        if (!name.isEmpty()) {
            DatabaseConnection.addCategory(name, (parent != null ? parent.getId() : null));
            categoryNameField.clear();
            refreshCategoryLists();
        }
    }

    @FXML
    protected void onUpdateCategoryClick() {
        Category selected = categoryListView.getSelectionModel().getSelectedItem();

        // 1. Ellenőrizzük, van-e kijelölés
        if (selected == null) {
            showAlert("Figyelem", "Kérlek, válassz ki egy kategóriát a listából a módosításhoz!", Alert.AlertType.WARNING);
            return;
        }

        String newName = categoryNameField.getText();
        Category parent = parentCategoryComboBox.getValue();
        Integer parentId = (parent != null) ? parent.getId() : null;

        // 2. Üres név ellenőrzése
        if (newName.isEmpty()) {
            showAlert("Hiba", "A kategória neve nem lehet üres!", Alert.AlertType.ERROR);
            return;
        }

        // 3. Önhivatkozás elleni védelem (ne lehessen önmaga szülője)
        if (parentId != null && parentId == selected.getId()) {
            showAlert("Logikai hiba", "Egy kategória nem lehet önmaga szülője! Kérlek, válassz másik szülőt vagy hagyd üresen.", Alert.AlertType.WARNING);
            return;
        }

        // 4. Módosítás végrehajtása
        DatabaseConnection.updateCategory(selected.getId(), newName, parentId);
        refreshCategoryLists();
        showAlert("Siker", "A kategória (" + newName + ") sikeresen frissítve lett!", Alert.AlertType.INFORMATION);
    }

    @FXML
    protected void onDeleteCategoryClick() {
        Category selected = categoryListView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Figyelem", "Válassz ki egy kategóriát a törléshez!", Alert.AlertType.WARNING);
            return;
        }

        // Megerősítés kérése
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Törlés megerősítése");
        confirm.setHeaderText("Kategória törlése: " + selected.getName());
        confirm.setContentText("Biztosan törölni akarod? Ha vannak hozzárendelt ételek vagy alkategóriák, a törlés sikertelen lesz.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            // Itt hívjuk meg a boolean visszatérésű metódust
            boolean success = DatabaseConnection.deleteCategory(selected.getId());

            if (success) {
                refreshCategoryLists();
                showAlert("Siker", "Kategória eltávolítva.", Alert.AlertType.INFORMATION);
            } else {
                // Ez fut le, ha a RESTRICT szabály miatt a MySQL nem engedte a törlést
                showAlert("Hiba", "Nem sikerült a törlés! \n\nIndok: Vannak ételek vagy alkategóriák, amik ehhez a kategóriához tartoznak. Előbb ezeket helyezd át vagy töröld!", Alert.AlertType.ERROR);
            }
        }
    }

    private void refreshCategoryLists() {
        List<Category> allCategories = DatabaseConnection.getAllCategories();

        // 1. Hierarchikus sorrendbe rendezzük a listát
        List<Category> hierarchicList = new ArrayList<>();
        sortCategoriesHierarchical(allCategories, null, hierarchicList);

        ObservableList<Category> categories = FXCollections.observableArrayList(hierarchicList);

        // 2. Beállítjuk a listát
        categoryListView.setItems(categories);

        // 3. A ComboBoxoknál is fontos a hierarchikus sorrend a könnyebb átláthatóság miatt
        parentCategoryComboBox.setItems(categories);
        newCategoryComboBox.setItems(categories);
        editCategoryComboBox.setItems(categories);
    }

    // Segédmetódus a rekurzív rendezéshez
    private void sortCategoriesHierarchical(List<Category> all, Integer parentId, List<Category> result) {
        for (Category c : all) {
            // Megkeressük azokat, akiknek a parentId-ja megegyezik az éppen keresettel
            if ((parentId == null && c.getParentId() == null) ||
                    (parentId != null && parentId.equals(c.getParentId()))) {
                result.add(c);
                // Rekurzívan megkeressük ennek a kategóriának a gyerekeit is
                sortCategoriesHierarchical(all, c.getId(), result);
            }
        }
    }

}