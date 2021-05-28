package tech.itpark.repository;

import tech.itpark.model.Newspaper;

import java.util.List;

public interface NewspaperRepository {
    List<Newspaper> getAll();

    Newspaper create(Newspaper newspaper);

    Newspaper update(Newspaper newspaper);

    void remove(Long id, Boolean removed);
}
