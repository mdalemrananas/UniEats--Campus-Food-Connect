module com.unieats {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.controlsfx.controls;
    requires java.sql;
    requires transitive javafx.graphics;

    opens com.unieats to javafx.fxml;
    exports com.unieats;
    opens com.unieats.controllers to javafx.fxml;
}