package com.example.ChronoTask.security;


import jakarta.servlet.http.HttpServletRequest;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static boolean isFrameworkInternalRequest(HttpServletRequest request) {
        return request.getParameter("v-r") != null;
    }
}