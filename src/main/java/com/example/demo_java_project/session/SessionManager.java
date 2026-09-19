package com.example.demo_java_project.session;

import com.example.demo_java_project.model.User;

/** Keeps track of who is logged in (one user per running app). */
public final class SessionManager {

    private static volatile User currentUser;

    private SessionManager() { }

    public static void login(User user)    { currentUser = user; }
    public static void logout()            { currentUser = null; }
    public static User getCurrentUser()    { return currentUser; }
    public static boolean isLoggedIn()     { return currentUser != null; }
}