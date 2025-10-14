package com.unieats.util;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Utility class for consistent UI animations and transitions
 */
public class UIUtils {
    
    /**
     * Apply fade in animation to a node
     */
    public static void fadeIn(Node node, Duration duration) {
        node.setOpacity(0);
        FadeTransition fadeTransition = new FadeTransition(duration, node);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();
    }
    
    /**
     * Apply fade out animation to a node
     */
    public static void fadeOut(Node node, Duration duration) {
        FadeTransition fadeTransition = new FadeTransition(duration, node);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.play();
    }
    
    /**
     * Apply slide in animation from left
     */
    public static void slideInFromLeft(Node node, Duration duration) {
        node.setTranslateX(-20);
        TranslateTransition slideTransition = new TranslateTransition(duration, node);
        slideTransition.setFromX(-20);
        slideTransition.setToX(0);
        slideTransition.play();
    }
    
    /**
     * Apply slide in animation from right
     */
    public static void slideInFromRight(Node node, Duration duration) {
        node.setTranslateX(20);
        TranslateTransition slideTransition = new TranslateTransition(duration, node);
        slideTransition.setFromX(20);
        slideTransition.setToX(0);
        slideTransition.play();
    }
    
    /**
     * Apply slide in animation from top
     */
    public static void slideInFromTop(Node node, Duration duration) {
        node.setTranslateY(-20);
        TranslateTransition slideTransition = new TranslateTransition(duration, node);
        slideTransition.setFromY(-20);
        slideTransition.setToY(0);
        slideTransition.play();
    }
    
    /**
     * Apply slide in animation from bottom
     */
    public static void slideInFromBottom(Node node, Duration duration) {
        node.setTranslateY(20);
        TranslateTransition slideTransition = new TranslateTransition(duration, node);
        slideTransition.setFromY(20);
        slideTransition.setToY(0);
        slideTransition.play();
    }
    
    /**
     * Apply a gentle bounce animation
     */
    public static void bounce(Node node, Duration duration) {
        node.setScaleX(0.95);
        node.setScaleY(0.95);
        
        javafx.animation.ScaleTransition scaleTransition = new javafx.animation.ScaleTransition(duration, node);
        scaleTransition.setFromX(0.95);
        scaleTransition.setFromY(0.95);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setCycleCount(2);
        scaleTransition.play();
    }
    
    /**
     * Apply a pulse animation
     */
    public static void pulse(Node node, Duration duration) {
        javafx.animation.ScaleTransition scaleTransition = new javafx.animation.ScaleTransition(duration, node);
        scaleTransition.setFromX(1.0);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(1.05);
        scaleTransition.setToY(1.05);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setCycleCount(2);
        scaleTransition.play();
    }
    
    /**
     * Apply a shake animation for error states
     */
    public static void shake(Node node, Duration duration) {
        TranslateTransition shakeTransition = new TranslateTransition(Duration.millis(100), node);
        shakeTransition.setFromX(0);
        shakeTransition.setToX(-10);
        shakeTransition.setAutoReverse(true);
        shakeTransition.setCycleCount(6);
        shakeTransition.play();
    }
    
    /**
     * Apply a success checkmark animation
     */
    public static void successAnimation(Node node, Duration duration) {
        node.setScaleX(0);
        node.setScaleY(0);
        
        javafx.animation.ScaleTransition scaleTransition = new javafx.animation.ScaleTransition(duration, node);
        scaleTransition.setFromX(0);
        scaleTransition.setFromY(0);
        scaleTransition.setToX(1.2);
        scaleTransition.setToY(1.2);
        
        scaleTransition.setOnFinished(e -> {
            javafx.animation.ScaleTransition scaleBack = new javafx.animation.ScaleTransition(Duration.millis(200), node);
            scaleBack.setFromX(1.2);
            scaleBack.setFromY(1.2);
            scaleBack.setToX(1.0);
            scaleBack.setToY(1.0);
            scaleBack.play();
        });
        
        scaleTransition.play();
    }
    
    /**
     * Apply a loading spinner animation
     */
    public static void loadingSpinner(Node node, Duration duration) {
        javafx.animation.RotateTransition rotateTransition = new javafx.animation.RotateTransition(duration, node);
        rotateTransition.setFromAngle(0);
        rotateTransition.setToAngle(360);
        rotateTransition.setCycleCount(javafx.animation.Animation.INDEFINITE);
        rotateTransition.play();
    }
    
    /**
     * Apply a smooth transition for page changes
     */
    public static void pageTransition(Node oldNode, Node newNode, Duration duration) {
        // Fade out old node
        fadeOut(oldNode, duration);
        
        // After fade out, show new node and fade in
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(duration.divide(2), e -> {
                newNode.setVisible(true);
                fadeIn(newNode, duration);
            })
        );
        timeline.play();
    }
    
    /**
     * Apply a smooth transition for card appearance
     */
    public static void cardAppear(Node card, Duration duration) {
        card.setOpacity(0);
        card.setScaleX(0.9);
        card.setScaleY(0.9);
        
        FadeTransition fadeTransition = new FadeTransition(duration, card);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        
        javafx.animation.ScaleTransition scaleTransition = new javafx.animation.ScaleTransition(duration, card);
        scaleTransition.setFromX(0.9);
        scaleTransition.setFromY(0.9);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        
        fadeTransition.play();
        scaleTransition.play();
    }
    
    /**
     * Apply a smooth transition for card disappearance
     */
    public static void cardDisappear(Node card, Duration duration) {
        FadeTransition fadeTransition = new FadeTransition(duration, card);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        
        javafx.animation.ScaleTransition scaleTransition = new javafx.animation.ScaleTransition(duration, card);
        scaleTransition.setFromX(1.0);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(0.9);
        scaleTransition.setToY(0.9);
        
        fadeTransition.play();
        scaleTransition.play();
    }
}
