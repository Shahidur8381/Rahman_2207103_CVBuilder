package com.example._207103_project;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:cv_database.db";
    private static DatabaseManager instance;

    private DatabaseManager() {
        initializeDatabase();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // Enable foreign keys for CASCADE to work
            stmt.execute("PRAGMA foreign_keys = ON");

            String createCVTable = """
                CREATE TABLE IF NOT EXISTS cv (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    full_name TEXT NOT NULL,
                    email TEXT,
                    phone TEXT,
                    address TEXT,
                    photo_path TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
            stmt.execute(createCVTable);

            String createEducationTable = """
                CREATE TABLE IF NOT EXISTS education (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    cv_id INTEGER NOT NULL,
                    entry TEXT NOT NULL,
                    FOREIGN KEY (cv_id) REFERENCES cv(id) ON DELETE CASCADE
                )
                """;
            stmt.execute(createEducationTable);

            String createSkillsTable = """
                CREATE TABLE IF NOT EXISTS skills (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    cv_id INTEGER NOT NULL,
                    skill TEXT NOT NULL,
                    FOREIGN KEY (cv_id) REFERENCES cv(id) ON DELETE CASCADE
                )
                """;
            stmt.execute(createSkillsTable);

            String createExperienceTable = """
                CREATE TABLE IF NOT EXISTS experience (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    cv_id INTEGER NOT NULL,
                    entry TEXT NOT NULL,
                    FOREIGN KEY (cv_id) REFERENCES cv(id) ON DELETE CASCADE
                )
                """;
            stmt.execute(createExperienceTable);

            String createProjectsTable = """
                CREATE TABLE IF NOT EXISTS projects (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    cv_id INTEGER NOT NULL,
                    project TEXT NOT NULL,
                    FOREIGN KEY (cv_id) REFERENCES cv(id) ON DELETE CASCADE
                )
                """;
            stmt.execute(createProjectsTable);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int saveCV(CVModel cv) {
        String insertCV = "INSERT INTO cv (full_name, email, phone, address, photo_path) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(insertCV, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, cv.getFullName());
                pstmt.setString(2, cv.getEmail());
                pstmt.setString(3, cv.getPhone());
                pstmt.setString(4, cv.getAddress());
                pstmt.setString(5, cv.getPhotoPath());
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int cvId = rs.getInt(1);
                    cv.setId(cvId);

                    // Save education entries
                    saveListEntries(conn, cvId, cv.getEducation(), "education", "entry");

                    // Save skills
                    saveListEntries(conn, cvId, cv.getSkills(), "skills", "skill");

                    // Save experience entries
                    saveListEntries(conn, cvId, cv.getExperience(), "experience", "entry");

                    // Save projects
                    saveListEntries(conn, cvId, cv.getProjects(), "projects", "project");

                    conn.commit();
                    return cvId;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void saveListEntries(Connection conn, int cvId, List<String> entries, String tableName, String columnName) throws SQLException {
        if (entries == null || entries.isEmpty()) return;
        
        String sql = "INSERT INTO " + tableName + " (cv_id, " + columnName + ") VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (String entry : entries) {
                if (entry != null && !entry.isBlank()) {
                    pstmt.setInt(1, cvId);
                    pstmt.setString(2, entry);
                    pstmt.executeUpdate();
                }
            }
        }
    }

    public List<CVModel> getAllCVs() {
        List<CVModel> cvList = new ArrayList<>();
        String query = "SELECT id, full_name, email, phone, address, photo_path FROM cv ORDER BY created_at DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                CVModel cv = new CVModel();
                cv.setId(rs.getInt("id"));
                cv.setFullName(rs.getString("full_name"));
                cv.setEmail(rs.getString("email"));
                cv.setPhone(rs.getString("phone"));
                cv.setAddress(rs.getString("address"));
                cv.setPhotoPath(rs.getString("photo_path"));
                cvList.add(cv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return cvList;
    }

    public CVModel getCVById(int id) {
        String query = "SELECT id, full_name, email, phone, address, photo_path FROM cv WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                CVModel cv = new CVModel();
                cv.setId(rs.getInt("id"));
                cv.setFullName(rs.getString("full_name"));
                cv.setEmail(rs.getString("email"));
                cv.setPhone(rs.getString("phone"));
                cv.setAddress(rs.getString("address"));
                cv.setPhotoPath(rs.getString("photo_path"));
                
                // Load education
                cv.getEducation().addAll(loadListEntries(id, "education", "entry"));
                
                // Load skills
                cv.getSkills().addAll(loadListEntries(id, "skills", "skill"));
                
                // Load experience
                cv.getExperience().addAll(loadListEntries(id, "experience", "entry"));
                
                // Load projects
                cv.getProjects().addAll(loadListEntries(id, "projects", "project"));
                
                return cv;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    private List<String> loadListEntries(int cvId, String tableName, String columnName) throws SQLException {
        List<String> entries = new ArrayList<>();
        String query = "SELECT " + columnName + " FROM " + tableName + " WHERE cv_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, cvId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                entries.add(rs.getString(columnName));
            }
        }
        
        return entries;
    }

    public void deleteCV(int id) {
        String deleteCV = "DELETE FROM cv WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Enable foreign keys to ensure CASCADE works
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }

            // CASCADE will automatically delete related records
            try (PreparedStatement pstmt = conn.prepareStatement(deleteCV)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
