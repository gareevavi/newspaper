package tech.itpark.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.itpark.dto.newspaper.*;
import tech.itpark.exception.PermissionDeniedException;

import tech.itpark.model.Newspaper;
import tech.itpark.repository.NewspaperRepository;
import tech.itpark.security.Auth;

import java.util.List;



@Service
@RequiredArgsConstructor
public class NewspaperService {
    private final NewspaperRepository repository;

    public List<Newspaper> getAllNewspaper() {
        return repository.getAll();
    }

    public NewspaperSaveResponseDto createNewspaper(Auth auth, NewspaperSaveRequestDto dto) {
        if (auth.isAnonymous()) {
            throw new PermissionDeniedException();
        }
        final var saved = repository.create(new Newspaper(
                dto.getId(),
                auth.getId(),
                dto.getAuthorId(),
                dto.getTitle()
        ));
        return new NewspaperSaveResponseDto(saved.getId(), saved.getAuthorId(), saved.getTitle());
    }

    public NewspaperUpdateResponseDto updateNewspaper(Auth auth, NewspaperUpdateRequestDto dto) {
        if (!auth.hasAnyRole("ROLE_ADMIN", "ROLE_MODERATOR")) {
            throw new PermissionDeniedException();
        }
        final var update = repository.update(new Newspaper(
                dto.getId(),
                dto.getAuthorId(),
                dto.getTitle(),
                dto.getContent()
        ));
        return new NewspaperUpdateResponseDto(update.getId(), update.getAuthorId(), update.getTitle(), update.getContent());
    }

    public NewspaperRemoveResponseDto removeByIdNewspaper(Auth auth, NewspaperRemoveRequestDto dto) {
       final var removedId = dto.getId();
        if (auth.isAnonymous()) {
            throw new PermissionDeniedException();
        }
        repository.remove(removedId, true);
        return new NewspaperRemoveResponseDto(dto.getId());
    }


}
