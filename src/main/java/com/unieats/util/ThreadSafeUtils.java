package com.unieats.util;

import javafx.application.Platform;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * Utility class for thread-safe operations in JavaFX applications
 */
public class ThreadSafeUtils {
    
    private static final ExecutorService backgroundExecutor = Executors.newFixedThreadPool(4);
    
    /**
     * Execute a task in the background and update UI on JavaFX thread
     */
    public static <T> CompletableFuture<T> runAsync(Supplier<T> backgroundTask) {
        return CompletableFuture.supplyAsync(backgroundTask, backgroundExecutor);
    }
    
    /**
     * Execute a task in the background and run UI update on JavaFX thread
     */
    public static void runAsync(Runnable backgroundTask, Runnable uiUpdate) {
        CompletableFuture.runAsync(backgroundTask, backgroundExecutor)
            .thenRun(() -> Platform.runLater(uiUpdate));
    }
    
    /**
     * Execute a task in the background with UI update and error handling
     */
    public static void runAsyncWithErrorHandling(Runnable backgroundTask, Runnable uiUpdate, 
                                               java.util.function.Consumer<Exception> errorHandler) {
        CompletableFuture.runAsync(backgroundTask, backgroundExecutor)
            .thenRun(() -> Platform.runLater(uiUpdate))
            .exceptionally(throwable -> {
                Platform.runLater(() -> errorHandler.accept((Exception) throwable.getCause()));
                return null;
            });
    }
    
    /**
     * Ensure a task runs on the JavaFX Application Thread
     */
    public static void runOnFXThread(Runnable task) {
        if (Platform.isFxApplicationThread()) {
            task.run();
        } else {
            Platform.runLater(task);
        }
    }
    
    /**
     * Shutdown the background executor (call on application exit)
     */
    public static void shutdown() {
        backgroundExecutor.shutdown();
    }
}
