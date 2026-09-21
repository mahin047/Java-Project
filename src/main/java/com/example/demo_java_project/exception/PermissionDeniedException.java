package com.example.demo_java_project.exception;

public class PermissionDeniedException extends ServiceException {

    public PermissionDeniedException(String message) {
        super(message);
    }
}