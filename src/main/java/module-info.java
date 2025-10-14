module com.unieats {
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

<<<<<<< HEAD
    opens com.unieats to javafx.fxml;
    opens com.unieats.util to javafx.base;

    exports com.unieats;

    opens com.unieats.controllers to javafx.fxml;
}
=======
    exports com.unieats;
    exports com.unieats.controllers;
    
    opens com.unieats to javafx.fxml, javafx.graphics;
    opens com.unieats.controllers to javafx.fxml, javafx.graphics;
}
>>>>>>> User-Panel
