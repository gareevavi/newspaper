package tech.itpark.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import tech.itpark.bodyconverter.BodyConverter;
import tech.itpark.dto.newspaper.NewspaperRemoveRequestDto;
import tech.itpark.dto.newspaper.NewspaperSaveRequestDto;
import tech.itpark.dto.newspaper.NewspaperUpdateRequestDto;
import tech.itpark.http.ContentTypes;
import tech.itpark.security.HttpServletRequestAuthToken;
import tech.itpark.service.NewspaperService;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class NewspaperControllerImpl implements NewspaperController {
    private final NewspaperService service;
    private final List<BodyConverter> converters;

    @Override
    public void getAll(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final var responseDto = service.getAllNewspaper();
        write(responseDto, ContentTypes.APPLICATION_JSON, response);
    }

    @Override
    public void create(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final var auth = HttpServletRequestAuthToken.auth(request);
        final var requestDto = read(NewspaperSaveRequestDto.class, request);
        final var responseDto = service.createNewspaper(auth, requestDto);
        write(responseDto, ContentTypes.APPLICATION_JSON, response);
    }

    @Override
    public void update(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final var auth = HttpServletRequestAuthToken.auth(request);
        final var requestDto = read(NewspaperUpdateRequestDto.class, request);
        final var responseDto = service.updateNewspaper(auth, requestDto);
        write(responseDto, ContentTypes.APPLICATION_JSON, response);
    }

    @Override
    public void remove(HttpServletRequest request, HttpServletResponse response) {
        final var auth = HttpServletRequestAuthToken.auth(request);
        final var requestDto = read(NewspaperRemoveRequestDto.class, request);
        final var responseDto = service.removeByIdNewspaper(auth, requestDto);
        write(responseDto, ContentTypes.APPLICATION_JSON, response);
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
