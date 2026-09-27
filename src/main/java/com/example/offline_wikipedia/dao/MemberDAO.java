package com.example.offline_wikipedia.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.offline_wikipedia.model.AccountStatus;
import com.example.offline_wikipedia.model.Member;
import com.example.offline_wikipedia.util.DBConnection;

/**
 * Data Access Object (DAO) for the 'members' table.
 * Handles database persistence and retrieval for library patrons.
 */

public class MemberDAO {

    /**
     * Inserts a new member into PostgreSQL, or updates existing details if the ID already exists.
     */
    
    public void save(Member member) {

        String sql = "INSERT INTO members (id, name, email, status) VALUES (?, ?, ?, ?) " +
                     "ON CONFLICT (id) DO UPDATE SET " +
                     "name = EXCLUDED.name, " +
                     "email = EXCLUDED.email, " +
                     "status = EXCLUDED.status";
                     
        try(Connection conn = DBConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, member.getId());
            stmt.setString(2, member.getName());
            stmt.setString(3, member.getEmail());
            stmt.setString(4, member.getStatus().name());

            stmt.executeUpdate();

            System.out.println("Member saved successfully: " + member.getName());
        }
        catch(SQLException e) {
            System.err.println("Error saving member (" + member.getId() + "): " + e.getMessage());
        }
    }

    /**
     * Finds a member by their primary key ID.
     * Returns null if no member with this ID exists.
     */

    public Member findById(int id) {

        String sql = "SELECT id, name, email, status FROM members WHERE id = ?";

        try(Connection conn = DBConnection.getConnection(); 
             PreparedStatement stmt  = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if(rs.next()) {
                    Member member = new Member(rs.getInt("id"), rs.getString("name"), rs.getString("email")); 
                    member.setStatus(AccountStatus.valueOf(rs.getString("status")));
                    
                    return member;
                }
            }
        }
        catch(SQLException e) {
            System.err.println("Error finding member by ID (" + id + "): " + e.getMessage());
        }
        return null;
    }

    /**
     * Updates an existing member's account status (ACTIVE <-> CLOSED).
     */

    public void updateStatus(int id, AccountStatus status) {

        String sql = "UPDATE members SET status = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status.name());
            stmt.setInt(2, id);
            stmt.executeUpdate();
            
            System.out.println("Member ID " + id + " status updated to " + status);
        } 
        catch (SQLException e) {
            System.err.println("Error updating member status: " + e.getMessage());
        }
    }

    /**
     * Returns all registered members, ordered alphabetically by name.
     */
    public List<Member> findAll() {
        List<Member> list = new ArrayList<>();
        String sql = "SELECT id, name, email, status FROM members ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
                while (rs.next()) {
                Member member = new Member(rs.getInt("id"), rs.getString("name"), rs.getString("email"));
                member.setStatus(AccountStatus.valueOf(rs.getString("status")));
                
                list.add(member);
            }
        } 
        catch (SQLException e) {
            System.err.println("Error fetching all members: " + e.getMessage());
        }
        return list;
    }
}
