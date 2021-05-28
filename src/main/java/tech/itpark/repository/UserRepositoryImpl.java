package tech.itpark.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import tech.itpark.exception.DataAccessException;
import tech.itpark.jdbc.JdbcTemplate;
import tech.itpark.model.TokenAuth;
import tech.itpark.model.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final DataSource ds;
    private final JdbcTemplate template = new JdbcTemplate();


    @Override
    public User save(User user) {
        try (
                final var connection = ds.getConnection();
                final var statement = connection.prepareStatement("""
                            INSERT INTO users(login, password, secret) VALUES (?, ?, ?);
                        """, Statement.RETURN_GENERATED_KEYS); // альтернатива RETURNING
        ) {
            var index = 0;
            statement.setString(++index, user.getLogin());
            statement.setString(++index, user.getPassword());
            statement.setString(++index, user.getSecret());
            statement.executeUpdate();

            final var keys = statement.getGeneratedKeys();
            if (!keys.next()) {
                throw new DataAccessException("no keys in result");
            }

            user.setId(keys.getLong(1));

            return user;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public void remove(User user, Boolean removed) {
        try (
                final var statement = ds.getConnection().prepareStatement("UPDATE users SET removed = ? WHERE id = ?")
        ) {
            var index = 0;
            statement.setBoolean(++index, removed);
            statement.setLong(++index, user.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public Optional<User> getByToken(String token) {
        try (
                final var connection = ds.getConnection();
                final var statement = connection.prepareStatement("""
                          SELECT u.id, u.login, '**MASKED**' AS password, '**MASKED**' AS secret, u.removed FROM users u 
                          JOIN user_roles ur ON u.id = ur.role_id
                          JOIN tokens t ON u.id = t.userid
                          WHERE t.token = ?
                        """);
        ) {
            var index = 0;
            statement.setString(++index, token);
            try (
                    final var resultSet = statement.executeQuery();
            ) {
                return resultSet.next() ? Optional.of(
                        new User(
                                resultSet.getLong("id"),
                                resultSet.getString("login"),
                                resultSet.getString("password"),
                                resultSet.getString("secret"),
                                resultSet.getBoolean("removed"),
                                Set.of((String[]) resultSet.getArray("roles").getArray())
                        )
                ) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public Optional<User> getByLogin(String login) {
        try (
                final var connection = ds.getConnection();
                final var statement = connection.prepareStatement("""
                        SELECT u.id, u.login, '**MASKED**' AS password, '**MASKED**' AS secret, u.removed FROM users u 
                        JOIN user_roles ur ON u.id = ur.role_id 
                        WHERE u.login = ?
                        """);
        ) {
            var index = 0;
            statement.setString(++index, login);
            try (
                    final var resultSet = statement.executeQuery();
            ) {
                return resultSet.next() ? Optional.of(
                        new User(
                                resultSet.getLong("id"),
                                resultSet.getString("login"),
                                resultSet.getString("password"),
                                resultSet.getString("secret"),
                                resultSet.getBoolean("removed"),
                                Set.of((String[]) resultSet.getArray("roles").getArray())
                        )
                ) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public void saveRoles(long id, Set<String> roles) {
        if (id == 0 || roles.size() == 0) {
            return;
        }
        try (
                final var connection = ds.getConnection();
                final var statement = connection.prepareStatement("""
                        INSERT INTO user_roles(user_id, role_id)
                        SELECT ?, r.id FROM roles r WHERE r.name = ANY (?)
                          """, Statement.NO_GENERATED_KEYS);
        ) {

            String[] arrayString = roles.toArray(String[]::new);
            Array arrayRoles = connection.createArrayOf("TEXT", arrayString);

            var index = 0;
            statement.setLong(++index, id);
            statement.setArray(++index, arrayRoles);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public void updatePassword(User user) {
        try (
                final var statement = ds.getConnection().prepareStatement("UPDATE users SET password = ? WHERE id = ?")
        ) {
            var index = 0;
            statement.setString(++index, user.getPassword());
            statement.setLong(++index, user.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public void updateSecret(User user) {
        try (
                final var statement = ds.getConnection().prepareStatement("UPDATE users SET secret = ? WHERE id = ?")
        ) {
            var index = 0;
            statement.setString(++index, user.getSecret());
            statement.setLong(++index, user.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public void saveToken(TokenAuth auth) {
        try (
                final var conn = ds.getConnection();
        ) {
            // language=PostgreSQL
            template.update(conn, "INSERT INTO tokens(userId, token) VALUES (?, ?) ON CONFLICT (userId) DO UPDATE SET token = ?",
                    auth.getUserId(), auth.getToken(), auth.getToken());
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public void deleteToken(TokenAuth auth) {
        try (
                final var conn = ds.getConnection();
        ) {
            // language=PostgreSQL
            template.update(conn, "DELETE FROM tokens WHERE userId = ? AND token = ?;",
                    auth.getUserId(), auth.getToken());
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
