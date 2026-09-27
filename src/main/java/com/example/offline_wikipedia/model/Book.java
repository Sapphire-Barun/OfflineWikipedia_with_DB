/**
 * Represents a physical book item within the library inventory.
 * Each book is uniquely identified by its ISBN and maintains an availability status.
 */

package com.example.offline_wikipedia.model;

public class Book {

    private String isbn;
    private String title;
    private String author;
    private BookStatus status;

    /**
     * Creates a new book entry in the catalog.
     * New books default to AVAILABLE status upon entry into inventory.
     */
    public Book(String isbn, String title, String author) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.status = BookStatus.AVAILABLE;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public BookStatus getStatus() {
        return status;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Book [isbn=" + isbn + ", title=" + title + ", author=" + author + ", status=" + status + "]";
    }
}
