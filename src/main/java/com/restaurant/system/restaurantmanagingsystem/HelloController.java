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

    @FXML private TextField searchField;

    @FXML private TextField newNameField;
    @FXML private TextField newPriceField;
    @FXML private ComboBox<Category> newCategoryComboBox;

    @FXML private TextField editNameField;
    @FXML private TextField editPriceField;
    @FXML private ComboBox<Category> editCategoryComboBox;

    @FXML private Label totalItemsLabel;
    @FXML private Label avgPriceLabel;
    @FXML private Label maxPriceLabel;

    @FXML private ListView<Category> categoryListView;
    @FXML private TextField categoryNameField;
    @FXML private ComboBox<Category> parentCategoryComboBox;

    @FXML private TableView<MenuItem> orderMenuTable;
    @FXML private TableColumn<MenuItem, String> orderMenuNameColumn;
    @FXML private TableColumn<MenuItem, Double> orderMenuPriceColumn;
    @FXML private ListView<BasketItem> basketListView;
    @FXML private Spinner<Integer> tableNumberSpinner;
    @FXML private Label orderTotalLabel;

    @FXML private TableView<Order> activeOrdersTable;
    @FXML private TableColumn<Order, Integer> orderIdColumn;
    @FXML private TableColumn<Order, Integer> orderTableColumn;
    @FXML private TableColumn<Order, String> orderTimeColumn;
    @FXML private TableColumn<Order, Double> orderTotalColumn;
    @FXML private ListView<String> activeOrderDetailsList;
    @FXML private TextField orderSearchField;

    private ObservableList<BasketItem> basket = FXCollections.observableArrayList();
    private ObservableList<MenuItem> masterData = FXCollections.observableArrayList();

    public void initialize() {
        // --- 1. CELLA GYÁRAK BEÁLLÍTÁSA ---
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        orderMenuNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        orderMenuPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        orderTableColumn.setCellValueFactory(new PropertyValueFactory<>("tableNumber"));
        orderTimeColumn.setCellValueFactory(new PropertyValueFactory<>("formattedTime"));
        orderTotalColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        // --- 2. KERESÉS ÉS SZŰRÉS LOGIKA (Menedzser fül) ---
        FilteredList<MenuItem> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(menuItem -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return menuItem.getName().toLowerCase().contains(lowerCaseFilter) ||
                        menuItem.getCategoryName().toLowerCase().contains(lowerCaseFilter);
            });
            refreshStatistics();
        });
        SortedList<MenuItem> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(menuTable.comparatorProperty());
        menuTable.setItems(sortedData);

        // --- 3. KERESÉS ÉS SZŰRÉS LOGIKA (Rendelésfelvétel fül) ---
        FilteredList<MenuItem> orderFilteredData = new FilteredList<>(masterData, p -> true);
        orderSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            orderFilteredData.setPredicate(menuItem -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return menuItem.getName().toLowerCase().contains(lowerCaseFilter) ||
                        menuItem.getCategoryName().toLowerCase().contains(lowerCaseFilter);
            });
        });
        SortedList<MenuItem> orderSortedData = new SortedList<>(orderFilteredData);
        orderSortedData.comparatorProperty().bind(orderMenuTable.comparatorProperty());
        orderMenuTable.setItems(orderSortedData); // Itt kötjük össze a szűrt listával!

        // --- 4. FIGYELŐK ÉS EGYÉB BEÁLLÍTÁSOK ---
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

        categoryListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                categoryNameField.setText(newSelection.getName());
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

        activeOrdersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                List<String> details = DatabaseConnection.getOrderDetails(newSelection.getId());
                activeOrderDetailsList.setItems(FXCollections.observableArrayList(details));
            } else {
                activeOrderDetailsList.getItems().clear();
            }
        });

        tableNumberSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 1));
        basketListView.setItems(basket);
        setupNumberValidation(newPriceField);
        setupNumberValidation(editPriceField);

        // --- 5. ADATOK BETÖLTÉSE ---
        loadMenuItems();
        refreshActiveOrders();
        refreshCategoryLists();
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
        masterData.setAll(items); // A TableView-k automatikusan frissülnek a SortedList-en keresztül
        refreshStatistics();
    }

    private void refreshOrderMenu() {
        loadMenuItems(); // Csak a masterData-t frissítjük
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
        double sum = masterData.stream().mapToDouble(MenuItem::getPrice).sum();
        MenuItem expensive = masterData.stream().max((a, b) -> Double.compare(a.getPrice(), b.getPrice())).orElse(masterData.get(0));

        totalItemsLabel.setText(String.valueOf(total));
        avgPriceLabel.setText(String.format("%.0f Ft", sum / total));
        maxPriceLabel.setText(expensive.getName() + " (" + (int)expensive.getPrice() + " Ft)");
    }

    @FXML
    protected void onAddCategoryClick() {
        String name = categoryNameField.getText();
        Category parent = parentCategoryComboBox.getValue();
        if (!name.isEmpty()) {
            DatabaseConnection.addCategory(name, (parent != null ? parent.getId() : null));
            categoryNameField.clear();
            refreshCategoryLists();
        }
    }

    @FXML
    protected void onUpdateCategoryClick() {
        Category selected = categoryListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Figyelem", "Válassz ki egy kategóriát!", Alert.AlertType.WARNING);
            return;
        }
        String newName = categoryNameField.getText();
        Category parent = parentCategoryComboBox.getValue();
        Integer parentId = (parent != null) ? parent.getId() : null;

        if (!newName.isEmpty() && (parentId == null || parentId != selected.getId())) {
            DatabaseConnection.updateCategory(selected.getId(), newName, parentId);
            refreshCategoryLists();
            showAlert("Siker", "Kategória frissítve!", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    protected void onDeleteCategoryClick() {
        Category selected = categoryListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Törlöd a kategóriát?", ButtonType.OK, ButtonType.CANCEL);
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (DatabaseConnection.deleteCategory(selected.getId())) {
                refreshCategoryLists();
                showAlert("Siker", "Törölve!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Hiba", "Vannak függő elemek!", Alert.AlertType.ERROR);
            }
        }
    }

    private void refreshCategoryLists() {
        List<Category> allCategories = DatabaseConnection.getAllCategories();
        List<Category> hierarchicList = new ArrayList<>();
        sortCategoriesHierarchical(allCategories, null, hierarchicList);
        ObservableList<Category> categories = FXCollections.observableArrayList(hierarchicList);
        categoryListView.setItems(categories);
        parentCategoryComboBox.setItems(categories);
        newCategoryComboBox.setItems(categories);
        editCategoryComboBox.setItems(categories);
    }

    private void sortCategoriesHierarchical(List<Category> all, Integer parentId, List<Category> result) {
        for (Category c : all) {
            if ((parentId == null && c.getParentId() == null) || (parentId != null && parentId.equals(c.getParentId()))) {
                result.add(c);
                sortCategoriesHierarchical(all, c.getId(), result);
            }
        }
    }

    @FXML
    protected void onAddToBasketClick() {
        MenuItem selected = orderMenuTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean found = false;
            for (BasketItem bi : basket) {
                if (bi.getItem().getId() == selected.getId()) {
                    bi.addOne();
                    found = true;
                    break;
                }
            }
            if (!found) basket.add(new BasketItem(selected, 1));
            basketListView.refresh();
            updateOrderTotal();
        }
    }

    private void updateOrderTotal() {
        double total = basket.stream().mapToDouble(bi -> bi.getItem().getPrice() * bi.getQuantity()).sum();
        orderTotalLabel.setText("Összesen: " + (int)total + " Ft");
    }

    @FXML
    protected void onPlaceOrderClick() {
        if (basket.isEmpty()) {
            showAlert("Hiba", "Üres a kosár!", Alert.AlertType.WARNING);
            return;
        }
        int tableNum = tableNumberSpinner.getValue();
        double total = basket.stream().mapToDouble(bi -> bi.getItem().getPrice() * bi.getQuantity()).sum();
        DatabaseConnection.saveOrder(tableNum, new ArrayList<>(basket), total);
        showAlert("Siker", "Rendelés rögzítve!", Alert.AlertType.INFORMATION);
        basket.clear();
        updateOrderTotal();
        refreshActiveOrders();
    }

    @FXML
    public void refreshActiveOrders() {
        List<Order> orders = DatabaseConnection.getActiveOrders();
        activeOrdersTable.setItems(FXCollections.observableArrayList(orders));
    }

    @FXML
    protected void onFinalizeOrderClick() {
        Order selected = activeOrdersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Lezárod a rendelést?", ButtonType.OK, ButtonType.CANCEL);
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            DatabaseConnection.updateOrderStatus(selected.getId(), "FIZETVE");
            refreshActiveOrders();
        }
    }
}