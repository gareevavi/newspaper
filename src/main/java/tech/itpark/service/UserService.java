package tech.itpark.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.itpark.crypto.PasswordHasher;
import tech.itpark.crypto.TokenGenerator;
import tech.itpark.dto.user.*;
import tech.itpark.exception.AuthErrorException;
import tech.itpark.exception.PermissionDeniedException;
import tech.itpark.model.TokenAuth;
import tech.itpark.model.User;
import tech.itpark.repository.UserRepository;
import tech.itpark.security.AuthProvider;
import tech.itpark.security.Auth;

@Service
@RequiredArgsConstructor
public class UserService implements AuthProvider {
  private final UserRepository repository;
  private final PasswordHasher passwordHasher;
  private final TokenGenerator tokenGenerator;

  @Override
  public Auth provide(String token) {
    // BL -> Optional.empty -> Anonymous
    return repository.getByToken(token)
            .map(o -> (Auth) o)
            .orElse(Auth.anonymous())
            ;
  }

  public RegistrationResponseDto register(RegistrationRequestDto request) {

    if (request.getLogin() == null) {
      throw new RuntimeException("login can't be null");
    }
    if (!request.getLogin().matches("^[a-z0-9]{5,10}$")) {
      throw new RuntimeException("bad login");
    }
    if (request.getPassword() == null) {
      throw new RuntimeException("password can't be null");
    }
    if (request.getPassword().length() < 5) {
      throw new RuntimeException("minimal length of password must be greater than 5");
    }
    if (request.getSecret() == null) {
      throw new RuntimeException("secret can't be null");
    }
    if (request.getSecret().length() < 5) {
      throw new RuntimeException("minimal length of secret must be greater than 5");
    }

    final var passwordHash = passwordHasher.hash(request.getPassword());
    final var secretHash = passwordHasher.hash(request.getSecret());

    final var saved = repository.save(
        new User(0, request.getLogin(), passwordHash, secretHash, false, request.getRoles())
    );

    long saved_id = saved.getId();
    if (saved_id != 0){
      repository.saveRoles(saved_id, saved.getRoles());
    }
    return new RegistrationResponseDto(saved.getId());
  }

  public UnregisterResponseDto delete(UnregisterRequestDto requestDto, String token) {
    final var user = repository.getByToken(token)
            .orElseThrow(() -> new RuntimeException("user not found"));

    if (!passwordHasher.matches(user.getPassword(), requestDto.getPassword())) {
      throw new RuntimeException("passwords not match");
    }
    repository.remove(user, true);
    return new UnregisterResponseDto(user.getId());
  }

  public RestoreResponseDto restore(RestoreRequestDto requestDto) {
    final var user = repository.getByLogin(requestDto.getLogin())
            .orElseThrow(() -> new RuntimeException("User not found!"));

    if (!passwordHasher.matches(user.getPassword(), requestDto.getPassword())) {
      throw new AuthErrorException("Passwords not match!");
    }

    if (!passwordHasher.matches(user.getSecret(), requestDto.getSecret())) {
      throw new AuthErrorException("Secret not match!");
    }

    repository.remove(user, false);
    return new RestoreResponseDto(user.getId());
  }


  public LoginResponseDto login(LoginRequestDto request) {
    final var user = repository.getByLogin(request.getLogin())
        .orElseThrow(() -> new AuthErrorException("User not found!"));

    if (!passwordHasher.matches(user.getPassword(), request.getPassword())) {
      throw new AuthErrorException("Passwords not match!");
    }

    if (user.getRemoved()){
      throw new AuthErrorException("Users deleted!");
    }

    final var token = tokenGenerator.generate();
    repository.saveToken(new TokenAuth(user.getId(), token));
    return new LoginResponseDto(token);
  }

  public LogoutResponseDto logout(Auth auth, String token) {
    User user = repository.getByToken(token).orElseThrow(() -> new RuntimeException("Wrong token!"));
    long id = auth.getId();
    if (id != user.getId()){
      throw new AuthErrorException("Wrong authorization !"); // it's impossible
    }
    repository.deleteToken(new TokenAuth(id, token));
    return new LogoutResponseDto(id);
  }

  public UpdatePasswordResponseDto updatePassword(UpdatePasswordRequestDto requestDto) {
    User user = repository.getByLogin(requestDto.getLogin()).orElseThrow(() -> new RuntimeException("wrong login!"));
    if (!passwordHasher.matches(user.getSecret(), requestDto.getSecret())){
      throw new PermissionDeniedException("wrong secret!");
    }
    user.setPassword(passwordHasher.hash(requestDto.getPassword()));
    repository.updatePassword(user);
    return new UpdatePasswordResponseDto(user.getId());
  }

  public UpdateSecretResponseDto updateSecret(UpdateSecretRequestDto requestDto) {
    User user = repository.getByLogin(requestDto.getLogin()).orElseThrow(() -> new RuntimeException("wrong login!"));
    if (!passwordHasher.matches(user.getPassword(), requestDto.getPassword())){
      throw new PermissionDeniedException("wrong password!");
    }
    user.setSecret(passwordHasher.hash(requestDto.getSecret()));
    repository.updateSecret(user);
    return new UpdateSecretResponseDto(user.getId());
  }
}
