module com.restaurant.system.restaurantmanagingsystem {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.restaurant.system.restaurantmanagingsystem to javafx.fxml;
    exports com.restaurant.system.restaurantmanagingsystem;
}