package ru.otus.hw09.controllers;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import ru.otus.hw09.dto.AuthorDto;
import ru.otus.hw09.exceptions.IntegrityViolationException;
import ru.otus.hw09.exceptions.NotFoundException;
import ru.otus.hw09.services.AuthorService;
import ru.otus.hw09.services.AuthorServiceImpl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings({"unchecked", "ConstantConditions"})
@AutoConfigureDataJpa
@AutoConfigureTestDatabase
@WebMvcTest({AuthorController.class, AuthorServiceImpl.class})
@DisplayName("Класс AuthorController должен:")
class AuthorControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    AuthorService authorService;

    private static final Long AUTHOR_ID_WITH_BOOKS = 1L;
    private static final Long AUTHOR_ID_WITHOUT_BOOKS = 3L;


    @Test
    @DisplayName("Обновляет автора")
    void updateAuthorSuccess() throws Exception {
        val expectedAuthor = new AuthorDto(AUTHOR_ID_WITH_BOOKS, "test1", "test2", "test3");
        mockMvc.perform(post("/saveAuthor")
                        .param("id", expectedAuthor.getId().toString())
                        .param("firstName", expectedAuthor.getFirstName())
                        .param("middleName", expectedAuthor.getMiddleName())
                        .param("lastName", expectedAuthor.getLastName()))
                .andExpect(status().isFound())
                .andReturn();
        val actualAuthor = authorService.findById(AUTHOR_ID_WITH_BOOKS).orElseThrow(() -> new NotFoundException("Автор с ID=" + AUTHOR_ID_WITH_BOOKS));
        assertThat(actualAuthor)
                .usingRecursiveComparison()
                .isEqualTo(expectedAuthor);
    }


    @Test
    @DisplayName("Формирует ошибки заполнения полей при редактировании/создании автора")
    void addAuthorFieldsError() throws Exception {
        val author = new AuthorDto(0L, "", "1", "2");
        val expectedErrorCodes = List.of("Size", "NotBlank", "Size", "Size");
        val result = mockMvc.perform(post("/saveAuthor")
                        .param("id", author.getId().toString())
                        .param("firstName", author.getFirstName())
                        .param("middleName", author.getMiddleName())
                        .param("lastName", author.getLastName()))
                .andExpect(status().isOk())
                .andReturn();
        val bindingResult = (BindingResult) result.getModelAndView().getModel().get("org.springframework.validation.BindingResult.author");
        val actualErrorCodes = bindingResult.getFieldErrors().stream()
                .map(FieldError::getCode)
                .collect(Collectors.toList());
        assertThat(actualErrorCodes)
                .hasSize(4)
                .containsExactlyInAnyOrderElementsOf(expectedErrorCodes);
    }

    @Test
    @DisplayName("Добавлять нового автора")
    void addAuthorSuccess() throws Exception {
        val expectedAuthor = new AuthorDto(0L, "test1", "test2", "test3");
        val result = mockMvc.perform(post("/saveAuthor")
                        .param("id", expectedAuthor.getId().toString())
                        .param("firstName", expectedAuthor.getFirstName())
                        .param("middleName", expectedAuthor.getMiddleName())
                        .param("lastName", expectedAuthor.getLastName()))
                .andExpect(status().isFound())
                .andReturn();
        val resultPageNewAuthor = mockMvc.perform(get(result.getResponse().getRedirectedUrl())).andReturn();
        val actualAuthor = (AuthorDto) resultPageNewAuthor.getModelAndView().getModel().get("author");
        assertThat(actualAuthor)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(expectedAuthor);
    }


    @Test
    @DisplayName("Не удалять автора с книгами - бросать IntegrityViolationException.")
    void deleteAuthorFail() throws Exception {
        val result = mockMvc.perform(get("/deleteAuthor").param("id", AUTHOR_ID_WITH_BOOKS.toString()))
                .andExpect(status().isConflict())
                .andReturn();
        val deletedAuthor = authorService.findById(AUTHOR_ID_WITH_BOOKS);
        assertThat(deletedAuthor.isPresent()).as("Author didn't delete").isTrue();
        assertThat(result.getResolvedException()).isInstanceOf(IntegrityViolationException.class).hasMessage("Нельзя удалить автора c ID=" + AUTHOR_ID_WITH_BOOKS + ". У автора есть книги.");
    }

    @Test
    @DisplayName("Удалять автора с указанным ID")
    void deleteAuthorSuccess() throws Exception {
        mockMvc.perform(get("/deleteAuthor").param("id", AUTHOR_ID_WITHOUT_BOOKS.toString()))
                .andExpect(status().isFound())
                .andReturn();
        val deletedAuthor = authorService.findById(AUTHOR_ID_WITHOUT_BOOKS);
        assertThat(deletedAuthor.isEmpty()).as("Author deleted").isTrue();
    }

    @Test
    @DisplayName("Добавлять в модель автора, при запросе страницы с автором")
    void author() throws Exception {
        val expectedAuthor = authorService.findById(AUTHOR_ID_WITH_BOOKS).orElseThrow(() -> new NotFoundException("Автор с ID=" + AUTHOR_ID_WITH_BOOKS));
        val result = mockMvc.perform(get("/author").param("id", AUTHOR_ID_WITH_BOOKS.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("author"))
                .andReturn();
        val actualAuthor = (AuthorDto) Objects.requireNonNull(result.getModelAndView()).getModel().get("author");
        assertThat(actualAuthor)
                .usingRecursiveComparison()
                .isEqualTo(expectedAuthor);
    }


    @Test
    @DisplayName("Добавлять в модель всех авторов, при запросе списка авторов")
    void authors() throws Exception {
        List<AuthorDto> expectedAuthors = authorService.findAll();
        val result = mockMvc.perform(get("/authors"))
                .andExpect(status().isOk())
                .andExpect(view().name("authors"))
                .andReturn();
        List<AuthorDto> actualAuthors = (List<AuthorDto>) Objects.requireNonNull(result.getModelAndView()).getModel().get("authors");
        assertThat(actualAuthors)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expectedAuthors);
    }
}