module com.unieats {
    // Required modules
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.controlsfx.controls;
    requires java.sql;
    requires transitive javafx.graphics;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.ikonli.material2;
    requires org.java_websocket;
    requires com.google.gson;

    // Exports
    exports com.unieats;
    exports com.unieats.controllers;
    exports com.unieats.util;

    // Opens for reflection
    opens com.unieats to javafx.fxml, javafx.graphics;
    opens com.unieats.controllers to javafx.fxml, javafx.graphics;
    opens com.unieats.util to javafx.base;
    opens com.unieats.models to com.google.gson;
    opens com.unieats.websocket to com.google.gson;
}
