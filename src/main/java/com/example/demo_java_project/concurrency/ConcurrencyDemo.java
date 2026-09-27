package com.example.demo_java_project.concurrency;

import com.example.demo_java_project.model.Role;
import com.example.demo_java_project.model.User;
import com.example.demo_java_project.service.BookingService;
import com.example.demo_java_project.session.SessionManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DEV / DEMO ONLY — proves that even with 10 threads racing to book
 * the SAME slot at the SAME time, only ONE booking succeeds.
 * Run this class's main() directly (not through the JavaFX app).
 */
public class ConcurrencyDemo {

    public static void main(String[] args) throws InterruptedException {

        // Fake a logged-in user so BookingService.book() has someone to book for.
        // Change the id (1) to match a real user id in your `users` table.
        SessionManager.login(new User(3, "Hasirun Mahin Ullash", "student@slotsync.com", null,
                "x", Role.STUDENT, null));

        BookingService bookingService = new BookingService();
        AtomicInteger success = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();

        int threadCount = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        // Change resourceId (1) and the date/time below to match a resource
        // and an open slot in your database (must be within its open/close hours,
        // and not already booked).
        int resourceId = 1;
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime time = LocalTime.of(10, 0);

        System.out.println("Launching " + threadCount + " threads, all booking resource "
                + resourceId + " on " + date + " at " + time + " ...");

        for (int i = 0; i < threadCount; i++) {
            final int threadNum = i + 1;
            pool.submit(() -> {
                try {
                    bookingService.book(resourceId, date, time);
                    success.incrementAndGet();
                    System.out.println("Thread " + threadNum + ": SUCCESS");
                } catch (Exception e) {
                    failed.incrementAndGet();
                    System.out.println("Thread " + threadNum + ": REJECTED - " + e.getMessage());
                }
            });
        }

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println();
        System.out.println("✅ Successful bookings: " + success.get());
        System.out.println("❌ Rejected (slot already taken): " + failed.get());
    }
}