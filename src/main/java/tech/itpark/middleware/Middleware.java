package tech.itpark.middleware;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@FunctionalInterface
public interface Middleware {
  boolean process(HttpServletRequest request, HttpServletResponse response);
}