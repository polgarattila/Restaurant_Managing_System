module com.restaurant.system.restaurantmanagingsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.restaurant.system.restaurantmanagingsystem to javafx.fxml;
    exports com.restaurant.system.restaurantmanagingsystem;

}