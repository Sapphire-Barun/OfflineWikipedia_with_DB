package com.example.offline_wikipedia.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.offline_wikipedia.model.Book;
import com.example.offline_wikipedia.model.BookStatus;
import com.example.offline_wikipedia.util.DBConnection;

public class BookDAO {
    
    public void save(Book book) {
        
        String sql = "INSERT INTO books (isbn, title, author, status) VALUES (?, ?, ?, ?)" +
        "ON CONFLICT (isbn)  DO UPDATE SET title = EXCLUDED.title, author = EXCLUDED.author, status = EXCLUDED.status";

        try (Connection conn = DBConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, book.getStatus().name());

            stmt.executeUpdate();
        }
        catch(SQLException e) {
            System.err.println("Error saving book: " + e.getMessage());
        }
    }

    public Book findByIsbn(String isbn) {
        String sql = "SELECT * FROM books WHERE isbn = ?";

        try(Connection conn = DBConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, isbn);

            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                Book book = new Book(rs.getString("isbn"), rs.getString("title"), rs.getString("author"));

                book.setStatus(BookStatus.valueOf(rs.getString("status")));
                return book;
            }
        }
        catch (SQLException e) {
            System.err.println("Error finding book: " + e.getMessage());
        }
        return null;
    }

    public void updateStatus(String isbn, BookStatus status) {
        String sql = "UPDATE books SET status = ? where isbn = ?";

        try(Connection conn = DBConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.name());
            stmt.setString(2, isbn);

            stmt.executeUpdate();
        }
        catch(SQLException e) {
            System.err.println("Error updating book status: " + e.getMessage());
        }
    }

    public List<Book> findAll() {
        List<Book> list = new ArrayList<>();

        String sql = "SELECT * FROM books ORDER BY title";

        try(Connection conn = DBConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql); 
             ResultSet rs = stmt.executeQuery()) {
            
            while(rs.next()) {
                Book book = new Book(rs.getString("isbn"), rs.getString("title"), rs.getString("author"));

                book.setStatus(BookStatus.valueOf(rs.getString("status")));
                list.add(book);
            }
        }
        catch(SQLException e) {
            System.err.println("Error fetching all books: " + e.getMessage());
        }
        return list;
    }
}
