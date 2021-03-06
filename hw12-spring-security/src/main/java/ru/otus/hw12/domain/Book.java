package ru.otus.hw12.domain;

import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor
@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;
    @ManyToOne(targetEntity = Author.class)
    @JoinColumn(name = "author_id", foreignKey = @ForeignKey(name = "fk_book_author"), nullable = false)
    private Author author;
    @Column(name = "title")
    private String title;
    @Column(name = "short_content")
    private String shortContent;

    @Fetch(FetchMode.SUBSELECT)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "books_genres",
            joinColumns = @JoinColumn(name = "book_id"),
            foreignKey = @ForeignKey(name = "fk_book_genre"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"),
            inverseForeignKey = @ForeignKey(name = "fk_genre"))
    private List<Genre> genres;


    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "book")
    private List<Comment> comments;

    public Book(long id, Author author, String title, String shortContent, List<Genre> genres, List<Comment> comments) {
        this.id = id;
        this.author = Objects.requireNonNull(author);
        this.title = title;
        this.shortContent = shortContent;
        this.genres = genres;
        this.comments = comments;
    }


    public Book(Author author, String title, String shortContent, List<Genre> genres, List<Comment> comments) {
        this(0L, author, title, shortContent, genres, comments);
    }

    public long getId() {
        return this.id;
    }

    public Author getAuthor() {
        return this.author;
    }

    public String getTitle() {
        return this.title;
    }

    public String getShortContent() {
        return this.shortContent;
    }

    public List<Genre> getGenres() {
        return this.genres;
    }

    public List<Comment> getComments() {
        return this.comments;
    }
}
