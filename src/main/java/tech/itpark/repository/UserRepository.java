package tech.itpark.repository;

import tech.itpark.model.TokenAuth;
import tech.itpark.model.User;

import java.util.Optional;
import java.util.Set;

public interface UserRepository {
    User save(User user);

    void remove(User user, Boolean removed);

    Optional<User> getByToken(String token);

    Optional<User> getByLogin(String login);

    void saveRoles(long id, Set<String> roles);

    void updatePassword(User user);

    void updateSecret(User user);

    void saveToken(TokenAuth auth);

    void deleteToken(TokenAuth auth);
}
