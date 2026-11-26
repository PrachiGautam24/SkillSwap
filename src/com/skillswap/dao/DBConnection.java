package com.skillswap.dao;

import com.skillswap.util.PasswordUtil;
import java.sql.*;

public class DBConnection {
    private static final String LOCAL_URL = "jdbc:h2:~/skillswapdb;MODE=MySQL";
    private static final String SHARED_URL = LOCAL_URL + ";AUTO_SERVER=TRUE";
    private static volatile String url = System.getProperty("skillswap.db.url", SHARED_URL);
    private static final String USER = "sa";
    private static final String PASS = "";

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("H2 JDBC Driver not found in classpath!", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        String requested = url;
        try {
            return DriverManager.getConnection(requested, USER, PASS);
        } catch (SQLException e) {
            // H2 90061 means the optional automatic server could not open a port.
            if (!SHARED_URL.equals(requested) || e.getErrorCode() != 90061) throw e;
            Connection local = DriverManager.getConnection(LOCAL_URL, USER, PASS);
            url = LOCAL_URL;
            return local;
        }
    }

    public static void initializeTables() {
        String[] ddlStatements = {
            "CREATE TABLE IF NOT EXISTS users (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "name VARCHAR(100) NOT NULL, " +
            "email VARCHAR(100) UNIQUE NOT NULL, " +
            "password VARCHAR(255) NOT NULL, " +
            "student_id VARCHAR(50), " +
            "department VARCHAR(100), " +
            "semester VARCHAR(20), " +
            "skills VARCHAR(255), " +
            "skills_to_learn VARCHAR(255), " +
            "learning_mode VARCHAR(50), " +
            "preferred_time VARCHAR(100), " +
            "bio TEXT)",

            "CREATE TABLE IF NOT EXISTS sessions (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "tutor_id INT NOT NULL, " +
            "title VARCHAR(150) NOT NULL, " +
            "description TEXT, " +
            "skill VARCHAR(100) NOT NULL, " +
            "scheduled_at VARCHAR(100), " +
            "status VARCHAR(30) DEFAULT 'open', " +
            "session_type VARCHAR(30) NOT NULL, " +
            "max_capacity INT DEFAULT 10, " +
            "FOREIGN KEY (tutor_id) REFERENCES users(id) ON DELETE CASCADE)",

            "CREATE TABLE IF NOT EXISTS bookings (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "session_id INT NOT NULL, " +
            "learner_id INT NOT NULL, " +
            "status VARCHAR(30) DEFAULT 'pending', " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE, " +
            "FOREIGN KEY (learner_id) REFERENCES users(id) ON DELETE CASCADE)",

            "CREATE TABLE IF NOT EXISTS ratings (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "booking_id INT UNIQUE NOT NULL, " +
            "rating INT NOT NULL, " +
            "review TEXT, " +
            "rated_by_learner BOOLEAN DEFAULT TRUE, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE)",

            "CREATE TABLE IF NOT EXISTS exchange_requests (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "from_user_id INT NOT NULL, " +
            "to_user_id INT NOT NULL, " +
            "skill_offering VARCHAR(100) NOT NULL, " +
            "skill_requesting VARCHAR(100) NOT NULL, " +
            "message TEXT, " +
            "status VARCHAR(30) DEFAULT 'pending', " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "FOREIGN KEY (from_user_id) REFERENCES users(id) ON DELETE CASCADE, " +
            "FOREIGN KEY (to_user_id) REFERENCES users(id) ON DELETE CASCADE)",

            "CREATE TABLE IF NOT EXISTS announcements (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "title VARCHAR(150) NOT NULL, " +
            "content TEXT NOT NULL, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",

            "CREATE TABLE IF NOT EXISTS messages (" +
            "id INT AUTO_INCREMENT PRIMARY KEY, " +
            "sender_id INT NOT NULL, " +
            "receiver_id INT NOT NULL, " +
            "exchange_request_id INT, " +
            "content TEXT NOT NULL, " +
            "is_read BOOLEAN DEFAULT FALSE, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE, " +
            "FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE, " +
            "FOREIGN KEY (exchange_request_id) REFERENCES exchange_requests(id) ON DELETE CASCADE)"
        };

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            for (String sql : ddlStatements) {
                stmt.execute(sql);
            }
            seedDemoData(conn);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not initialize the SkillSwap database.", e);
        }
    }

    private static void seedDemoData(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) > 0) return;
        }

        String defaultPass = PasswordUtil.hash("pass123");
        String insertUser = "INSERT INTO users (name, email, password, student_id, department, semester, skills, skills_to_learn, learning_mode, preferred_time, bio) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertUser)) {
            Object[][] users = {
                {"Alice Johnson", "alice@uni.edu", defaultPass, "STU101", "Computer Science", "5th", "Java, Swing, SQL", "Python, Machine Learning", "Online", "Evenings", "CS junior passionate about backend systems and UI design."},
                {"Bob Smith", "bob@uni.edu", defaultPass, "STU102", "Data Science", "6th", "Python, Machine Learning, Pandas", "Java, SQL", "Online", "Weekends", "Data enthusiast happy to help with Python & ML fundamentals."},
                {"Clara Oswald", "clara@uni.edu", defaultPass, "STU103", "Design", "4th", "Figma, UI/UX, Graphic Design", "HTML, CSS, Java", "In-Person", "Afternoons", "Design student looking to build technical prototyping skills."},
                {"David Kim", "david@uni.edu", defaultPass, "STU104", "Mathematics", "7th", "Calculus, Linear Algebra, R", "Python, Data Visualization", "Hybrid", "Mornings", "Math major tutoring statistics and calculus."},
                {"Eva Martinez", "eva@uni.edu", defaultPass, "STU105", "Software Eng", "3rd", "C++, Git, Linux", "React, UI/UX", "Online", "Evenings", "Open-source contributor teaching Git workflows."}
            };
            for (Object[] u : users) {
                for (int i = 0; i < u.length; i++) ps.setObject(i + 1, u[i]);
                ps.addBatch();
            }
            ps.executeBatch();
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO sessions (tutor_id, title, description, skill, scheduled_at, status, session_type) VALUES " +
                    "(1, 'Java Swing & JDBC Workshop', 'Build desktop GUIs from scratch.', 'Java', '2026-11-20 18:00', 'open', 'offering')," +
                    "(2, 'Python Data Analysis 101', 'Intro to NumPy and Pandas.', 'Python', '2026-11-21 15:00', 'open', 'offering')," +
                    "(3, 'Looking for Java OOP Mentor', 'Need help preparing for midterms.', 'Java', '2026-11-22 14:00', 'open', 'requesting')," +
                    "(4, 'Linear Algebra Study Group', 'Eigenvalues and matrix decompositions.', 'Linear Algebra', '2026-11-23 10:00', 'open', 'offering')");

            stmt.execute("INSERT INTO bookings (session_id, learner_id, status) VALUES (2, 1, 'completed'), (1, 2, 'completed')");
            stmt.execute("INSERT INTO ratings (booking_id, rating, review) VALUES (1, 5, 'Bob explained Pandas super clearly!'), (2, 5, 'Alice helped me master JDBC!')");

            stmt.execute("INSERT INTO announcements (title, content) VALUES " +
                    "('Welcome to SkillSwap Campus Hub!', 'Connect with peers, trade knowledge, and earn leaderboard badges.')," +
                    "('Exam Prep Week Matchmaking', 'Update your Skills to Learn in your profile to get 100% match recommendations.')," +
                    "('Session Etiquette Reminder', 'Please confirm or cancel pending session bookings at least 12 hours in advance.')");
        }
    }
}
