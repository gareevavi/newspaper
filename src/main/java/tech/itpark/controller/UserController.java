package tech.itpark.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface UserController {
    void register(HttpServletRequest request, HttpServletResponse response) throws IOException;

    void delete(HttpServletRequest request, HttpServletResponse response);

    void restore(HttpServletRequest request, HttpServletResponse response);

    void login(HttpServletRequest request, HttpServletResponse response) throws IOException;

    void logout(HttpServletRequest request, HttpServletResponse response);

    void updatePassword(HttpServletRequest request, HttpServletResponse response) throws IOException;

    void updateSecret(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
