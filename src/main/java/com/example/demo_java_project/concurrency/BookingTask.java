package com.example.demo_java_project.concurrency;

import javafx.concurrent.Task;

import java.util.concurrent.Callable;

/** A reusable background Task: runs any booking-related work off the JavaFX thread. */
public class BookingTask<T> extends Task<T> {

    private final Callable<T> work;

    public BookingTask(Callable<T> work) {
        this.work = work;
    }

    @Override
    protected T call() throws Exception {
        return work.call();
    }
}