package tech.itpark.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import tech.itpark.exception.PermissionDeniedException;
import tech.itpark.security.Auth;
import tech.itpark.security.AuthProvider;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class AuthFilter extends HttpFilter {

  @Override
  protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
    final var token = req.getHeader("Authorization");
    req.setAttribute("TOKEN", token);

    final var context = (ApplicationContext) getServletContext().getAttribute("CONTEXT");
    AuthProvider provider = context.getBean(AuthProvider.class);

    final var auth = provider.provide(token);
    req.setAttribute("AUTH", auth);


    final var servletPath = req.getServletPath();
    final var accessRoles = (Map<String, Set<String>>) context.getBean("accessRoles");
    if (accessRoles.containsKey(servletPath)){
      final var roles = accessRoles.get(servletPath).toArray(String[]::new);
      if (!auth.hasAnyRole(roles)){
        throw new PermissionDeniedException("OPERATION NOT ALLOWED !!!");
      }
    }

    chain.doFilter(req, res);
  }
}
