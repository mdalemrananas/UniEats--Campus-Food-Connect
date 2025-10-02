package com.unieats.util;

import javafx.beans.binding.Bindings;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public final class ResponsiveSceneFactory {
    private ResponsiveSceneFactory() {}

    public static Scene createResponsiveScene(Parent contentRoot, double baseWidth, double baseHeight) {
        contentRoot.setManaged(true);

        Group scalableGroup = new Group(contentRoot);
        StackPane container = new StackPane(scalableGroup);
        // Start smaller to fit small screens. We'll scale down further if needed.
        Scene scene = new Scene(container, Math.min(baseWidth, 320), Math.min(baseHeight, 560), Color.WHITE);

        var scaleBinding = Bindings.min(
            scene.widthProperty().divide(baseWidth),
            scene.heightProperty().divide(baseHeight)
        );

        scalableGroup.scaleXProperty().bind(scaleBinding);
        scalableGroup.scaleYProperty().bind(scaleBinding);

        contentRoot.prefWidth(baseWidth);
        contentRoot.prefHeight(baseHeight);

        return scene;
    }
}

