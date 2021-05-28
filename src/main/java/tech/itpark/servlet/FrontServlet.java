package tech.itpark.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import tech.itpark.http.Handler;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// Servlet
public class FrontServlet extends HttpServlet {
  private ConfigurableApplicationContext context;
  private Map<String, Handler> linkTrace;
  private Map<String, Set<String>> accessRoles;

  private final Handler notFoundHandler = (request, response) -> response.sendError(404, "Page not found");

  @Override
  public void init() throws ServletException {
    super.init();
    try {
      final var basePackage = getInitParameter("base-package");
      context = new AnnotationConfigApplicationContext(basePackage);

      final var servletContext = getServletContext();
      servletContext.setAttribute("CONTEXT", context);

      linkTrace = (Map<String, Handler>) context.getBean("linkTrace");
      accessRoles = (Map<String, Set<String>>) context.getBean("accessRoles");

    } catch (Exception e) {
      throw new UnavailableException(e.getMessage());
    }
  }

  @Override
  public void destroy() {
    context.close();
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    final var servletPath = request.getServletPath();
    try {
      Optional.ofNullable(linkTrace.get(servletPath))
          .orElse(notFoundHandler)
          .handle(request, response);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
