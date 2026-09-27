package com.example.offline_wikipedia.service;

import java.time.LocalDate;
import java.util.List;

import com.example.offline_wikipedia.dao.BookDAO;
import com.example.offline_wikipedia.dao.LendingRecordDAO;
import com.example.offline_wikipedia.dao.MemberDAO;
import com.example.offline_wikipedia.model.AccountStatus;
import com.example.offline_wikipedia.model.Book;
import com.example.offline_wikipedia.model.BookStatus;
import com.example.offline_wikipedia.model.LendingRecord;
import com.example.offline_wikipedia.model.Member;

/**
 * Central management service facade for the library system.
 * Coordinates business rules, validation guards, and delegates persistence
 * to PostgreSQL via the DAO layer.
 */
public class Library {

    private final BookDAO bookDAO;
    private final MemberDAO memberDAO;
    private final LendingRecordDAO lendingRecordDAO;

    /**
     * Default constructor initializing active DAOs for PostgreSQL connectivity.
     */
    public Library() {
        this(new BookDAO(), new MemberDAO(), new LendingRecordDAO());
    }

    /**
     * Dependency injection constructor (useful for custom configurations or unit testing).
     */
    public Library(BookDAO bookDAO, MemberDAO memberDAO, LendingRecordDAO lendingRecordDAO) {
        this.bookDAO = bookDAO;
        this.memberDAO = memberDAO;
        this.lendingRecordDAO = lendingRecordDAO;
    }

    /**
     * Persists a new or updated book in PostgreSQL.
     */
    public void addBook(Book book) {
        bookDAO.save(book);
    }

    /**
     * Registers a new member or updates an existing record in PostgreSQL.
     */
    public void registerMember(Member member) {
        memberDAO.save(member);
    }

    /**
     * Queries PostgreSQL for a book matching the given ISBN.
     */
    public Book searchBookByIsbn(String isbn) {
        return bookDAO.findByIsbn(isbn);
    }

    /**
     * Queries PostgreSQL for a member with the matching numeric ID.
     */
    public Member findMemberById(int id) {
        return memberDAO.findById(id);
    }

    /**
     * Executes a book checkout transaction with business rule validations:
     * 1. Book must exist in PostgreSQL.
     * 2. Member must be registered.
     * 3. Member account must not be CLOSED.
     * 4. Book must not already be BORROWED.
     * 5. Member cannot exceed borrowing limit (max 5 books).
     */
    public void issueBook(String isbn, int memberId) {
        Book book = searchBookByIsbn(isbn);
        if (book == null) {
            System.out.println("Cannot issue: Book with ISBN " + isbn + " not found!");
            return;
        }

        Member member = findMemberById(memberId);
        if (member == null) {
            System.out.println("Cannot issue: Member with ID " + memberId + " not registered!");
            return;
        }

        if (member.getStatus() == AccountStatus.CLOSED) {
            System.out.println("Cannot issue book: Account for member " + member.getName() + " is closed.");
            return;
        }

        if (book.getStatus() == BookStatus.BORROWED) {
            System.out.println("Cannot issue book: '" + book.getTitle() + "' is already borrowed.");
            return;
        }

        int activeLoans = lendingRecordDAO.countActiveLoans(memberId);
        if (activeLoans >= 5) {
            System.out.println("Cannot issue book: " + member.getName() + " has reached the limit of 5 active borrowed books.");
            return;
        }

        // 1. Update book availability in PostgreSQL
        bookDAO.updateStatus(isbn, BookStatus.BORROWED);

        // 2. Persist the lending transaction
        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(14);
        lendingRecordDAO.save(isbn, memberId, issueDate, dueDate);

        System.out.println("Success: '" + book.getTitle() + "' issued to " + member.getName() 
                + " (Due Date: " + dueDate + ").");
    }

    /**
     * Processes a book return transaction:
     * 1. Validates book and member exist.
     * 2. Verifies the book is actually currently borrowed.
     * 3. Sets book status back to AVAILABLE in PostgreSQL.
     * 4. Stamps return_date on the active loan record in PostgreSQL.
     */
    public void returnBook(String isbn, int memberId) {
        Book book = searchBookByIsbn(isbn);
        if (book == null) {
            System.out.println("Cannot return: Book with ISBN " + isbn + " not found!");
            return;
        }

        Member member = findMemberById(memberId);
        if (member == null) {
            System.out.println("Cannot return: Member with ID " + memberId + " not registered!");
            return;
        }

        if (book.getStatus() != BookStatus.BORROWED) {
            System.out.println("Cannot return: '" + book.getTitle() + "' is not currently borrowed.");
            return;
        }

        // 1. Restore book status to AVAILABLE
        bookDAO.updateStatus(isbn, BookStatus.AVAILABLE);

        // 2. Conclude the loan record in PostgreSQL
        lendingRecordDAO.markReturned(isbn, memberId, LocalDate.now());

        System.out.println("Success: '" + book.getTitle() + "' returned by " + member.getName() + ".");
    }

    /**
     * Retrieves all currently active (unreturned) loans for a specific member from PostgreSQL.
     */
    public List<LendingRecord> getActiveLoansByMember(int memberId) {
        return lendingRecordDAO.findAll().stream()
                .filter(r -> r.getMember().getId() == memberId && r.getReturnDate() == null)
                .toList();
    }

    public List<Book> getCatalog() {
        return bookDAO.findAll();
    }

    public List<Member> getMembers() {
        return memberDAO.findAll();
    }

    public List<LendingRecord> getLendingHistory() {
        return lendingRecordDAO.findAll();
    }
}