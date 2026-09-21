package com.example.demo_java_project.exception;

/** Business-rule or data problem whose message is safe to show to the user. */
public class ServiceException extends Exception {

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}