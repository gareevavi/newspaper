package tech.itpark.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import tech.itpark.exception.DataAccessException;
import tech.itpark.jdbc.JdbcTemplate;

import tech.itpark.model.Newspaper;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


@Repository
@RequiredArgsConstructor
public class NewspaperRepositoryImpl implements NewspaperRepository {
    private final DataSource ds;
    private final JdbcTemplate template = new JdbcTemplate();

    @Override
    public List<Newspaper> getAll() {
        List<Newspaper> result = new ArrayList<>();
        try (
                final var conn = ds.getConnection();
                final var stmt = conn.prepareStatement("""
                        SELECT id, author_id, title, content FROM newspaper WHERE id = ?
                        """);
        ) {
            final var rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(new Newspaper(
                        rs.getLong("id"),
                        rs.getLong("authorId"),
                        rs.getString("title"),
                        rs.getString("content")
                ));
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
        return result;
    }

    @Override
    public Newspaper create(Newspaper newspaper) {
        try (
                final var conn = ds.getConnection();
                final var stmt = conn.prepareStatement("""
                        INSERT INTO newspaper(author_id, title, content) VALUES (?, ?, ?);
                        """, Statement.RETURN_GENERATED_KEYS);
        ) {
            var index = 0;
            stmt.setLong(++index, newspaper.getAuthorId());
            stmt.setString(++index, newspaper.getTitle());
            stmt.setString(++index, newspaper.getContent());
            stmt.executeUpdate();

            final var keys = stmt.getGeneratedKeys();
            if (!keys.next()) {
                throw new DataAccessException("no keys in result");
            }
            newspaper.setId(keys.getLong(1));

            return newspaper;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public Newspaper update(Newspaper newspaper) {
        try (
                final var stmt = ds.getConnection().prepareStatement("UPDATE newspaper SET author_id = ?, title = ?, content = ? WHERE id = ?")
        ) {
            var index = 0;
            stmt.setLong(++index, newspaper.getAuthorId());
            stmt.setString(++index, newspaper.getTitle());
            stmt.setString(++index, newspaper.getContent());
            stmt.setLong(++index, newspaper.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
        return newspaper;
    }

    @Override
    public void remove(Long id, Boolean removed) {
        try (
                final var stmt = ds.getConnection().prepareStatement("UPDATE users SET removed = ? WHERE id = ?")
        ) {
            var index = 0;
            stmt.setBoolean(++index, removed);
            stmt.setLong(++index, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
