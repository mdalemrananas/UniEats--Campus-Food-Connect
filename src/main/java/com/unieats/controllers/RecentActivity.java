package com.unieats.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RecentActivity {
    private final StringProperty time;
    private final StringProperty type;
    private final StringProperty description;

    public RecentActivity(String type, String description) {
        this.time = new SimpleStringProperty(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        this.type = new SimpleStringProperty(type);
        this.description = new SimpleStringProperty(description);
    }

    public RecentActivity(String time, String type, String description) {
        this.time = new SimpleStringProperty(time);
        this.type = new SimpleStringProperty(type);
        this.description = new SimpleStringProperty(description);
    }

    public String getTime() { return time.get(); }
    public void setTime(String time) { this.time.set(time); }
    public StringProperty timeProperty() { return time; }

    public String getType() { return type.get(); }
    public void setType(String type) { this.type.set(type); }
    public StringProperty typeProperty() { return type; }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }
    public StringProperty descriptionProperty() { return description; }
}
