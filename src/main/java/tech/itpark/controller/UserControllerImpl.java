package tech.itpark.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import tech.itpark.bodyconverter.BodyConverter;
import tech.itpark.dto.user.*;
import tech.itpark.http.ContentTypes;
import tech.itpark.security.HttpServletRequestAuthToken;
import tech.itpark.service.UserService;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserControllerImpl implements UserController {
  private final UserService service;
  private final List<BodyConverter> converters;

  @Override
  public void register(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final var requestDto = read(RegistrationRequestDto.class, request);
    final var responseDto = service.register(requestDto);
    write(responseDto, ContentTypes.APPLICATION_JSON, response);
  }

  @Override
  public void delete(HttpServletRequest request, HttpServletResponse response) {
    final var token = HttpServletRequestAuthToken.token(request);
    final var requestDto = read(UnregisterRequestDto.class, request);
    final var responseDto = service.delete(requestDto, token);
    write(responseDto, ContentTypes.APPLICATION_JSON, response);
  }

  @Override
  public void restore(HttpServletRequest request, HttpServletResponse response) {
    final var requestDto = read(RestoreRequestDto.class, request);
    final var responseDto = service.restore(requestDto);
    write(responseDto, ContentTypes.APPLICATION_JSON, response);
  }

  @Override
  public void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final var requestDto = read(LoginRequestDto.class, request);
    final var responseDto = service.login(requestDto);
    write(responseDto, ContentTypes.APPLICATION_JSON, response);
  }

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response) {
    final var token = HttpServletRequestAuthToken.token(request);
    final var auth = HttpServletRequestAuthToken.auth(request);
    final var responseDto = service.logout(auth, token);
    write(responseDto, ContentTypes.APPLICATION_JSON, response);
  }

  @Override
  public void updatePassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
    write(service.updatePassword(read(UpdatePasswordRequestDto.class, request)), ContentTypes.APPLICATION_JSON, response);
  }

  @Override
  public void updateSecret(HttpServletRequest request, HttpServletResponse response) throws IOException {
    write(service.updateSecret(read(UpdateSecretRequestDto.class, request)), ContentTypes.APPLICATION_JSON, response);
  }

  public <T> T read(Class<T> clazz, HttpServletRequest request) {
    for (final var converter : converters) {
      if (!converter.canRead(request.getContentType(), clazz)) {
        continue;
      }

      try {
        return converter.read(request.getReader(), clazz);
      } catch (IOException e) {
        e.printStackTrace();
        // TODO: convert to special exception
        throw new RuntimeException(e);
      }
    }
    // TODO: convert to special exception
    throw new RuntimeException("no converters support given content type");
  }

  private void write(Object data, String contentType, HttpServletResponse response) {
    for (final var converter : converters) {
      if (!converter.canWrite(contentType, data.getClass())) {
        continue;
      }

      try {
        response.setContentType(contentType);
        converter.write(response.getWriter(), data);
        return;
      } catch (IOException e) {
        e.printStackTrace();
        // TODO: convert to special exception
        throw new RuntimeException(e);
      }
    }
    // TODO: convert to special exception
    throw new RuntimeException("no converters support given content type");
  }

}
