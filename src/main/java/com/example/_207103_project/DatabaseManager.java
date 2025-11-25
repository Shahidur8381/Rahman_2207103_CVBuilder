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
                    saveListEntries(conn, cvId, cv.getEducation(), "education", "entry");
                    saveListEntries(conn, cvId, cv.getSkills(), "skills", "skill");
                    saveListEntries(conn, cvId, cv.getExperience(), "experience", "entry");
                    saveListEntries(conn, cvId, cv.getProjects(), "projects", "project");
                    conn.commit();
                    return cvId;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
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
                cv.getEducation().addAll(loadListEntries(id, "education", "entry"));
                cv.getSkills().addAll(loadListEntries(id, "skills", "skill"));
                cv.getExperience().addAll(loadListEntries(id, "experience", "entry"));
                cv.getProjects().addAll(loadListEntries(id, "projects", "project"));
                return cv;
            }
        } catch (SQLException e) {
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
    public void updateCV(CVModel cv) {
        String updateCVQuery = "UPDATE cv SET full_name = ?, email = ?, phone = ?, address = ?, photo_path = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);
            try {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON");
                }
                try (PreparedStatement pstmt = conn.prepareStatement(updateCVQuery)) {
                    pstmt.setString(1, cv.getFullName());
                    pstmt.setString(2, cv.getEmail());
                    pstmt.setString(3, cv.getPhone());
                    pstmt.setString(4, cv.getAddress());
                    pstmt.setString(5, cv.getPhotoPath());
                    pstmt.setInt(6, cv.getId());
                    pstmt.executeUpdate();
                }
                deleteListEntries(conn, cv.getId(), "education");
                deleteListEntries(conn, cv.getId(), "skills");
                deleteListEntries(conn, cv.getId(), "experience");
                deleteListEntries(conn, cv.getId(), "projects");
                saveListEntries(conn, cv.getId(), cv.getEducation(), "education", "entry");
                saveListEntries(conn, cv.getId(), cv.getSkills(), "skills", "skill");
                saveListEntries(conn, cv.getId(), cv.getExperience(), "experience", "entry");
                saveListEntries(conn, cv.getId(), cv.getProjects(), "projects", "project");
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
        }
    }
    private void deleteListEntries(Connection conn, int cvId, String tableName) throws SQLException {
        String sql = "DELETE FROM " + tableName + " WHERE cv_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cvId);
            pstmt.executeUpdate();
        }
    }
    public void deleteCV(int id) {
        String deleteCV = "DELETE FROM cv WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            try (PreparedStatement pstmt = conn.prepareStatement(deleteCV)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
        }
    }
}

