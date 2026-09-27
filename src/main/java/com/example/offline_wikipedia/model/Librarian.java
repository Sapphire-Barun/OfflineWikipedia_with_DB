/**
 * Represents library staff with administrative privileges.
 * Librarians can manage member account standing and add inventory to the catalog.
 */

package com.example.offline_wikipedia.model;

import com.example.offline_wikipedia.service.Library;

public class Librarian extends Person {

    private String staffId;

    public Librarian(int id, String name, String email, String staffId) {
        super(id, name, email);
        this.staffId = staffId;
    }

    public Librarian(int id, String name, String email) {
        this(id, name, email, "LIB-" + id);
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    /**
     * Suspends/closes a member's borrowing privileges (e.g., due to lost books or unpaid fines).
     */
    public void closeMemberAccount(Member member) {
        member.setStatus(AccountStatus.CLOSED);
        System.out.println("Librarian " + getName() + " (Staff ID: " + staffId + ") CLOSED account for member: " + member.getName());
    }

    /**
     * Reinstates an active status for a previously closed member account.
     */
    public void reopenMemberAccount(Member member) {
        member.setStatus(AccountStatus.ACTIVE);
        System.out.println("Librarian " + getName() + " (Staff ID: " + staffId + ") REOPENED account for member: " + member.getName());
    }

    /**
     * Adds an authorized physical book to the library catalog.
     */
    public void addNewBook(Library library, Book book) {
        library.addBook(book);
        System.out.println("Librarian " + getName() + " added '" + book.getTitle() + "' to the library catalog.");
    }

    @Override
    public String toString() {
        return "Librarian [id=" + getId() + ", name=" + getName() + ", email=" + getEmail() 
                + ", staffId=" + staffId + "]";
    }
}
