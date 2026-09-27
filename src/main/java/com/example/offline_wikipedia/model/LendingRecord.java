package com.example.offline_wikipedia.model;

import java.time.LocalDate;

/**
 * Represents an audit transaction slip for a book checkout event.
 * Tracks the lending timeline: issue date, due date (14-day standard window), and return date.
 */

public class LendingRecord {

    private int recordId;
    private Book book;
    private Member member;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate; // null indicates the book is currently checked out

    /**
     * Creates a transaction record with a specified issue date.
     * Due date is automatically computed as 14 days after issue.
     */
    public LendingRecord(int recordId, Book book, Member member, LocalDate issueDate) {
        this.recordId = recordId;
        this.book = book;
        this.member = member;
        this.issueDate = issueDate;
        this.dueDate = issueDate.plusDays(14); 
        this.returnDate = null;           
    }

    /**
     * Convenience constructor that automatically defaults issue date to today.
     */
    public LendingRecord(int recordId, Book book, Member member) {
        this(recordId, book, member, LocalDate.now());
    }

    /**
     * Concludes the loan by stamping the actual date of return.
     */
    public void markReturned(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    /**
     * Evaluates whether the loan has exceeded the agreed due date.
     */
    public boolean isOverdue() {
        if (returnDate == null) {
            return LocalDate.now().isAfter(dueDate);
        }
        return returnDate.isAfter(dueDate);
    }

    public int getRecordId() { 
        return recordId; 
    }

    public Book getBook() { 
        return book; 
    }

    public Member getMember() {
         return member; 
    }

    public LocalDate getIssueDate() { 
        return issueDate; 
    }

    public LocalDate getDueDate() {
         return dueDate; 
    }

    public LocalDate getReturnDate() { 
        return returnDate; 
    }

    @Override
    public String toString() {
        return "LendingRecord [ID=" + recordId 
                + ", Book=" + book.getTitle() 
                + ", Member=" + member.getName() 
                + ", Issued=" + issueDate 
                + ", Due=" + dueDate 
                + ", Returned=" + (returnDate != null ? returnDate : "Not yet returned") 
                + "]";
    }
}
