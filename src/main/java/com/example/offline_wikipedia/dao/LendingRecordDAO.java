package com.example.offline_wikipedia.dao;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.offline_wikipedia.model.AccountStatus;
import com.example.offline_wikipedia.model.Book;
import com.example.offline_wikipedia.model.BookStatus;
import com.example.offline_wikipedia.model.LendingRecord;
import com.example.offline_wikipedia.model.Member;
import com.example.offline_wikipedia.util.DBConnection;


/**
 * Data Access Object (DAO) for the 'lending_records' table.
 * Manages loan transactions, returns, and history audit trails.
 */
public class LendingRecordDAO {

    /**
     * Records a new book loan transaction in PostgreSQL.
     */
    public void save(String isbn, int memberId, LocalDate issueDate, LocalDate dueDate) {
        
        String sql = "INSERT INTO lending_records (isbn, member_id, issue_date, due_date) VALUES (?,?,?,?)";

        try(Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);
            stmt.setInt(2, memberId);
            stmt.setDate(3, Date.valueOf(issueDate));
            stmt.setDate(4, Date.valueOf(dueDate));

            stmt.executeUpdate();

            System.out.println("Lending transaction recorded for ISBN " + isbn + " to Member " + memberId);

        }
        catch(SQLException e) {
            System.out.println("Error saving lending record: " + e.getMessage());
        }
    }

    /**
     * Marks an active loan as returned by stamping the return date.
     * Uses a subquery to target the most recent unreturned loan for this book and member.
     */
    public void markReturned(String isbn, int memberId, LocalDate returnDate) {

        String sql = "UPDATE lending_records SET return_date = ? " +
                     "WHERE record_id = (SELECT record_id FROM lending_records " +
                     "                   WHERE isbn = ? AND member_id = ? AND return_date IS NULL " +
                     "                   ORDER BY issue_date DESC LIMIT 1)";

        try(Connection conn = DBConnection.getConnection(); 
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(returnDate));
            stmt.setString(2, isbn);
            stmt.setInt(3, memberId);

            int updated = stmt.executeUpdate();
            if (updated > 0) {
                System.out.println("Loan record updated: Book returned.");
            } 
            else {
                System.out.println("No active checkout found for ISBN " + isbn + " and Member " + memberId);
            }
        }
        catch(SQLException e) {
            System.err.println("Error updating return record: " + e.getMessage());
        }             
    }

    /**
     * Counts how many books a member currently has checked out (unreturned).
     */
    public int countActiveLoans(int memberId) {
        
        String sql = "SELECT COUNT(*) FROM lending_records WHERE member_id = ? AND return_date IS NULL";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
                stmt.setInt(1, memberId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } 
        catch (SQLException e) {
            System.err.println("Error counting active loans for member " + memberId + ": " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retrieves the entire lending audit history by joining lending_records with books and members.
     */
    public List<LendingRecord> findAll() {
        
        List<LendingRecord> history = new ArrayList<>();
        
        String sql = "SELECT lr.record_id, lr.issue_date, lr.due_date, lr.return_date, " +
                     "       b.isbn, b.title, b.author, b.status AS book_status, " +
                     "       m.id AS member_id, m.name AS member_name, m.email AS member_email, m.status AS member_status " +
                     "FROM lending_records lr " +
                     "JOIN books b ON lr.isbn = b.isbn " +
                     "JOIN members m ON lr.member_id = m.id " +
                     "ORDER BY lr.issue_date DESC";
        
        try (Connection conn = DBConnection.getConnection(); 
             Statement stmt = conn.createStatement(); 
             ResultSet rs = stmt.executeQuery(sql)) {
        
            while (rs.next()) {
                // 1. Reconstruct Book object
                Book book = new Book(rs.getString("isbn"), rs.getString("title"), rs.getString("author"));
                book.setStatus(BookStatus.valueOf(rs.getString("book_status")));
                
                // 2. Reconstruct Member object
                Member member = new Member(rs.getInt("member_id"), rs.getString("member_name"), rs.getString("member_email"));
                member.setStatus(AccountStatus.valueOf(rs.getString("member_status")));
                
                // 3. Reconstruct LendingRecord object
                int recordId = rs.getInt("record_id");
                LocalDate issueDate = rs.getDate("issue_date").toLocalDate();
                LendingRecord record = new LendingRecord(recordId, book, member, issueDate);
                Date retDate = rs.getDate("return_date");
                if (retDate != null) {
                    record.markReturned(retDate.toLocalDate());
                }
                history.add(record);
            }
        } 
        catch (SQLException e) {
            System.err.println("Error fetching lending history: " + e.getMessage());
        }
        return history;
    }
}
