package com.example.demo_java_project.concurrency;

import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * One shared thread pool for all background work in the app (DB calls,
 * API calls, file I/O). Instead of creating a brand-new OS thread every
 * time something runs in the background ("new Thread(...)"), tasks are
 * queued on this fixed-size pool and reused threads pick them up.
 */
public final class AppExecutor {

    private static final int POOL_SIZE = 6;

    private static final ThreadFactory FACTORY = runnable -> {
        Thread t = new Thread(runnable, "app-pool-worker");
        t.setDaemon(true);   // let the JVM exit even if a task is still running
        return t;
    };

    private static final ExecutorService POOL = Executors.newFixedThreadPool(POOL_SIZE, FACTORY);

    private AppExecutor() { }

    /** Submits a JavaFX Task to the shared pool. onSucceeded/onFailed still run on the FX thread. */
    public static void submit(Task<?> task) {
        POOL.execute(task);
    }

    /** Called once when the application exits. */
    public static void shutdown() {
        POOL.shutdown();
    }
}