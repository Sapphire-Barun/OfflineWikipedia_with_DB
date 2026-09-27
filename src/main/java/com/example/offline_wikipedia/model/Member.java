package com.example.offline_wikipedia.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a library patron eligible to borrow books.
 * Tracks the member's account standing and currently borrowed inventory.
 */

public class Member extends Person {

    private AccountStatus status;
    private List<Book> borrowedBooks;

    public Member(int id, String name, String email) {
        super(id, name, email);
        this.status = AccountStatus.ACTIVE;
        // Defensively initialize collection to prevent NullPointerExceptions on borrow
        this.borrowedBooks = new ArrayList<>();
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public List<Book> getBorrowedBooks() {
        return borrowedBooks;
    }

    public void setBorrowedBooks(List<Book> borrowedBooks) {
        this.borrowedBooks = borrowedBooks;
    }

    /**
     * Adds a book to the member's active borrowed list and transitions the book status to BORROWED.
     */
    public void borrowBook(Book book) {
        borrowedBooks.add(book);
        book.setStatus(BookStatus.BORROWED);
        System.out.println(book.getTitle() + " has been borrowed by " + getName() + ".");
    }

    /**
     * Removes a book from the member's active borrowed list and restores the book status to AVAILABLE.
     */
    public void returnBook(Book book) {
        borrowedBooks.remove(book);
        book.setStatus(BookStatus.AVAILABLE);
        System.out.println(book.getTitle() + " has been returned by " + getName() + ".");
    }

    @Override
    public String toString() {
        return "Member [id=" + getId() + ", name=" + getName() + ", email=" + getEmail()
                + ", status=" + status + ", borrowedBooks=" + borrowedBooks + "]";
    }
}
