package com.extremis.dao;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

/**
 * Universal Database Connection and Schema Initializer supporting:
 * - SQLite (default for local development: extremis.db)
 * - PostgreSQL (Aiven / Cloud deployment via DATABASE_URL or DB_HOST)
 */
public class DBConnection {

    private static volatile String activeJdbcUrl = null;
    private static volatile String dbUser = null;
    private static volatile String dbPassword = null;
    private static volatile boolean isPostgres = false;
    private static volatile boolean schemaInitialized = false;

    static {
        try {
            configureDatabase();
            initSchema();
        } catch (Exception e) {
            System.err.println("DBConnection static init warning: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        initSchema();
        System.out.println("SCHEMA_INITIALIZED_SUCCESS (isPostgres=" + isPostgres + ")");
    }

    public static synchronized void configureDatabase() {
        String envDbUrl = System.getenv("DATABASE_URL");
        if (envDbUrl == null || envDbUrl.trim().isEmpty()) {
            envDbUrl = System.getenv("JDBC_DATABASE_URL");
        }
        if (envDbUrl == null || envDbUrl.trim().isEmpty()) {
            envDbUrl = System.getenv("DB_URL");
        }
        if (envDbUrl == null || envDbUrl.trim().isEmpty()) {
            envDbUrl = System.getProperty("db.url");
        }

        String envHost = System.getenv("DB_HOST");
        if (envHost == null || envHost.trim().isEmpty()) {
            envHost = System.getProperty("db.host");
        }

        if (envDbUrl != null && !envDbUrl.trim().isEmpty()) {
            envDbUrl = envDbUrl.trim();
            if (envDbUrl.startsWith("postgres://") || envDbUrl.startsWith("postgresql://")) {
                try {
                    URI uri = new URI(envDbUrl);
                    String host = uri.getHost();
                    int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                    String path = uri.getPath();
                    String dbName = (path != null && path.length() > 1) ? path.substring(1) : "defaultdb";
                    String query = uri.getQuery();

                    String userInfo = uri.getUserInfo();
                    if (userInfo != null && !userInfo.isEmpty()) {
                        String[] parts = userInfo.split(":", 2);
                        dbUser = parts[0];
                        if (parts.length > 1) {
                            dbPassword = parts[1];
                        }
                    }

                    activeJdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName +
                            (query != null && !query.isEmpty() ? "?" + query : "?sslmode=require");
                    isPostgres = true;
                } catch (Exception e) {
                    activeJdbcUrl = envDbUrl.replaceFirst("^(postgres|postgresql)://", "jdbc:postgresql://");
                    isPostgres = true;
                }
            } else if (envDbUrl.startsWith("jdbc:postgresql:")) {
                activeJdbcUrl = envDbUrl;
                isPostgres = true;
            } else {
                activeJdbcUrl = envDbUrl;
                isPostgres = envDbUrl.toLowerCase().contains("postgres");
            }
        } else if (envHost != null && !envHost.trim().isEmpty()) {
            String port = System.getenv("DB_PORT");
            if (port == null || port.isEmpty()) port = "21581";
            String dbName = System.getenv("DB_NAME");
            if (dbName == null || dbName.isEmpty()) dbName = "defaultdb";
            String ssl = System.getenv("DB_SSLMODE");
            if (ssl == null || ssl.isEmpty()) ssl = "require";
            activeJdbcUrl = "jdbc:postgresql://" + envHost.trim() + ":" + port.trim() + "/" + dbName.trim() + "?sslmode=" + ssl.trim();
            isPostgres = true;
        }

        if (isPostgres) {
            if (dbUser == null) {
                dbUser = System.getenv("DB_USER");
                if (dbUser == null) dbUser = System.getProperty("db.user");
            }
            if (dbPassword == null) {
                dbPassword = System.getenv("DB_PASSWORD");
                if (dbPassword == null) dbPassword = System.getProperty("db.password");
            }
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("PostgreSQL JDBC driver not found", e);
            }
        } else {
            activeJdbcUrl = "jdbc:sqlite:extremis.db";
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("SQLite JDBC driver not found", e);
            }
        }
    }

    public static synchronized void setDatabaseUrl(String url, String user, String pass) {
        activeJdbcUrl = url;
        dbUser = user;
        dbPassword = pass;
        isPostgres = url != null && url.toLowerCase().contains("postgres");
        schemaInitialized = false;
        try {
            if (isPostgres) Class.forName("org.postgresql.Driver");
            else Class.forName("org.sqlite.JDBC");
        } catch (Exception ignored) {}
    }

    public static Connection getConnection() throws SQLException {
        if (activeJdbcUrl == null) {
            configureDatabase();
        }
        if (isPostgres && dbUser != null && dbPassword != null) {
            return DriverManager.getConnection(activeJdbcUrl, dbUser, dbPassword);
        }
        return DriverManager.getConnection(activeJdbcUrl);
    }

    public static synchronized void initSchema() {
        if (activeJdbcUrl == null) {
            configureDatabase();
        }
        try (Connection conn = getConnection()) {
            if (isPostgres) {
                initPostgresSchema(conn);
            } else {
                initSqliteSchema(conn);
            }
            schemaInitialized = true;
        } catch (SQLException e) {
            System.err.println("initSchema notice: " + e.getMessage());
        }
    }

    private static void initPostgresSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // 0. Compatibility functions for datetime
            try {
                stmt.execute("CREATE OR REPLACE FUNCTION datetime(t text DEFAULT 'now') RETURNS timestamp AS $$\n" +
                        "BEGIN\n" +
                        "    IF t IS NULL OR t = 'now' THEN\n" +
                        "        RETURN CURRENT_TIMESTAMP;\n" +
                        "    ELSE\n" +
                        "        RETURN t::timestamp;\n" +
                        "    END IF;\n" +
                        "EXCEPTION WHEN OTHERS THEN\n" +
                        "    RETURN CURRENT_TIMESTAMP;\n" +
                        "END;\n" +
                        "$$ LANGUAGE plpgsql;");

                stmt.execute("CREATE OR REPLACE FUNCTION datetime(t text, modifier text) RETURNS timestamp AS $$\n" +
                        "BEGIN\n" +
                        "    IF t = 'now' AND modifier = '-2 days' THEN\n" +
                        "        RETURN CURRENT_TIMESTAMP - INTERVAL '2 days';\n" +
                        "    ELSIF t = 'now' THEN\n" +
                        "        RETURN CURRENT_TIMESTAMP;\n" +
                        "    ELSE\n" +
                        "        RETURN t::timestamp;\n" +
                        "    END IF;\n" +
                        "EXCEPTION WHEN OTHERS THEN\n" +
                        "    RETURN CURRENT_TIMESTAMP;\n" +
                        "END;\n" +
                        "$$ LANGUAGE plpgsql;");
            } catch (SQLException ignored) {}

            // 1. Students Table
            stmt.execute("CREATE TABLE IF NOT EXISTS students (" +
                    "id SERIAL PRIMARY KEY, " +
                    "roll_no VARCHAR(100) UNIQUE NOT NULL, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(255) NOT NULL, " +
                    "branch VARCHAR(100) NOT NULL, " +
                    "section VARCHAR(100) NOT NULL, " +
                    "password_hash VARCHAR(255) NOT NULL, " +
                    "session_token TEXT, " +
                    "permissions_accepted INTEGER DEFAULT 0, " +
                    "class_id INTEGER, " +
                    "batch_id INTEGER)");

            // 2. Attendance Table
            stmt.execute("CREATE TABLE IF NOT EXISTS attendance (" +
                    "id SERIAL PRIMARY KEY, " +
                    "student_id INTEGER NOT NULL, " +
                    "date VARCHAR(50) NOT NULL, " +
                    "check_in_time VARCHAR(50), " +
                    "check_out_time VARCHAR(50), " +
                    "checkin_lat DOUBLE PRECISION, " +
                    "checkin_lng DOUBLE PRECISION, " +
                    "checkin_accuracy DOUBLE PRECISION, " +
                    "checkin_photo_path TEXT, " +
                    "checkout_lat DOUBLE PRECISION, " +
                    "checkout_lng DOUBLE PRECISION, " +
                    "checkout_photo_path TEXT, " +
                    "status VARCHAR(50) NOT NULL, " +
                    "is_override INTEGER DEFAULT 0, " +
                    "override_reason TEXT, " +
                    "overridden_by VARCHAR(255), " +
                    "UNIQUE(student_id, date))");

            // 3. Notices Table
            stmt.execute("CREATE TABLE IF NOT EXISTS notices (" +
                    "id SERIAL PRIMARY KEY, " +
                    "category VARCHAR(100) NOT NULL, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "message TEXT NOT NULL, " +
                    "created_at VARCHAR(100) NOT NULL, " +
                    "is_read INTEGER DEFAULT 0)");

            // 4. Faculties Table
            stmt.execute("CREATE TABLE IF NOT EXISTS faculties (" +
                    "id SERIAL PRIMARY KEY, " +
                    "faculty_id VARCHAR(100) UNIQUE NOT NULL, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(255) NOT NULL, " +
                    "branch VARCHAR(100) NOT NULL, " +
                    "section VARCHAR(100) NOT NULL, " +
                    "password_hash VARCHAR(255) NOT NULL, " +
                    "session_token TEXT, " +
                    "is_active INTEGER DEFAULT 1, " +
                    "department VARCHAR(100))");

            // 5. Admins Table
            stmt.execute("CREATE TABLE IF NOT EXISTS admins (" +
                    "id SERIAL PRIMARY KEY, " +
                    "username VARCHAR(100) UNIQUE NOT NULL, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(255) NOT NULL, " +
                    "password_hash VARCHAR(255) NOT NULL, " +
                    "session_token TEXT)");

            // 6. Classes Table
            stmt.execute("CREATE TABLE IF NOT EXISTS classes (" +
                    "id SERIAL PRIMARY KEY, " +
                    "code VARCHAR(100) UNIQUE NOT NULL, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "department VARCHAR(100) NOT NULL, " +
                    "semester VARCHAR(100), " +
                    "is_active INTEGER DEFAULT 1)");

            // 7. Batches Table
            stmt.execute("CREATE TABLE IF NOT EXISTS batches (" +
                    "id SERIAL PRIMARY KEY, " +
                    "class_id INTEGER NOT NULL, " +
                    "code VARCHAR(100) NOT NULL, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "is_active INTEGER DEFAULT 1, " +
                    "UNIQUE(class_id, code))");

            // 8. Teacher Assignments Table
            stmt.execute("CREATE TABLE IF NOT EXISTS teacher_assignments (" +
                    "id SERIAL PRIMARY KEY, " +
                    "faculty_id INTEGER NOT NULL, " +
                    "class_id INTEGER NOT NULL, " +
                    "batch_id INTEGER, " +
                    "subject VARCHAR(255), " +
                    "created_at VARCHAR(100) NOT NULL)");

            // 9. Attendance Overrides Table
            stmt.execute("CREATE TABLE IF NOT EXISTS attendance_overrides (" +
                    "id SERIAL PRIMARY KEY, " +
                    "attendance_id INTEGER, " +
                    "student_id INTEGER NOT NULL, " +
                    "faculty_id INTEGER NOT NULL, " +
                    "original_status VARCHAR(50) NOT NULL, " +
                    "updated_status VARCHAR(50) NOT NULL, " +
                    "reason TEXT NOT NULL, " +
                    "attendance_date VARCHAR(50) NOT NULL, " +
                    "override_time VARCHAR(100) NOT NULL)");

            // 10. Random Check-in Settings Table
            stmt.execute("CREATE TABLE IF NOT EXISTS random_checkin_settings (" +
                    "id INTEGER PRIMARY KEY CHECK (id = 1), " +
                    "is_enabled INTEGER DEFAULT 1, " +
                    "is_paused INTEGER DEFAULT 0, " +
                    "earliest_time VARCHAR(50) DEFAULT '09:15', " +
                    "latest_time VARCHAR(50) DEFAULT '15:30', " +
                    "duration_minutes INTEGER DEFAULT 10, " +
                    "min_interval_minutes INTEGER DEFAULT 45, " +
                    "max_interval_minutes INTEGER DEFAULT 90, " +
                    "challenges_per_day INTEGER DEFAULT 2, " +
                    "target_type VARCHAR(50) DEFAULT 'ALL', " +
                    "target_value VARCHAR(50) DEFAULT 'ALL', " +
                    "updated_at VARCHAR(100))");

            stmt.execute("INSERT INTO random_checkin_settings " +
                    "(id, is_enabled, is_paused, earliest_time, latest_time, duration_minutes, min_interval_minutes, max_interval_minutes, challenges_per_day, target_type, target_value, updated_at) " +
                    "VALUES (1, 1, 0, '09:15', '15:30', 10, 45, 90, 2, 'ALL', 'ALL', '2026-10-23 00:00:00') " +
                    "ON CONFLICT (id) DO NOTHING");

            // 10b. Attendance Timer Settings Table
            stmt.execute("CREATE TABLE IF NOT EXISTS attendance_timer_settings (" +
                    "id INTEGER PRIMARY KEY CHECK (id = 1), " +
                    "checkin_timer_enabled INTEGER DEFAULT 1, " +
                    "checkin_start_time VARCHAR(50) DEFAULT '08:50', " +
                    "checkin_end_time VARCHAR(50) DEFAULT '09:05', " +
                    "checkin_duration_minutes INTEGER DEFAULT 15, " +
                    "checkout_timer_enabled INTEGER DEFAULT 1, " +
                    "checkout_start_time VARCHAR(50) DEFAULT '15:40', " +
                    "checkout_end_time VARCHAR(50) DEFAULT '18:00', " +
                    "checkout_duration_minutes INTEGER DEFAULT 140, " +
                    "updated_at VARCHAR(100))");

            stmt.execute("INSERT INTO attendance_timer_settings " +
                    "(id, checkin_timer_enabled, checkin_start_time, checkin_end_time, checkin_duration_minutes, " +
                    "checkout_timer_enabled, checkout_start_time, checkout_end_time, checkout_duration_minutes, updated_at) " +
                    "VALUES (1, 1, '08:50', '09:05', 15, 1, '15:40', '18:00', 140, '2026-10-23 00:00:00') " +
                    "ON CONFLICT (id) DO NOTHING");

            // 11. Random Challenges Table
            stmt.execute("CREATE TABLE IF NOT EXISTS random_challenges (" +
                    "id SERIAL PRIMARY KEY, " +
                    "challenge_code VARCHAR(100) UNIQUE NOT NULL, " +
                    "scheduled_time VARCHAR(100) NOT NULL, " +
                    "deadline_time VARCHAR(100) NOT NULL, " +
                    "duration_minutes INTEGER NOT NULL, " +
                    "status VARCHAR(50) NOT NULL, " +
                    "target_type VARCHAR(50) NOT NULL, " +
                    "target_value VARCHAR(50) NOT NULL, " +
                    "is_manual INTEGER DEFAULT 0, " +
                    "created_at VARCHAR(100) NOT NULL, " +
                    "created_by VARCHAR(100) NOT NULL)");

            // 12. Student Random Challenges Table
            stmt.execute("CREATE TABLE IF NOT EXISTS student_random_challenges (" +
                    "id SERIAL PRIMARY KEY, " +
                    "challenge_id INTEGER NOT NULL, " +
                    "student_id INTEGER NOT NULL, " +
                    "status VARCHAR(50) NOT NULL, " +
                    "responded_at VARCHAR(100), " +
                    "lat DOUBLE PRECISION, " +
                    "lng DOUBLE PRECISION, " +
                    "accuracy DOUBLE PRECISION, " +
                    "photo_path TEXT, " +
                    "UNIQUE(challenge_id, student_id))");

            // 13. Random Location Checks Table
            stmt.execute("CREATE TABLE IF NOT EXISTS random_location_checks (" +
                    "id SERIAL PRIMARY KEY, " +
                    "student_id INTEGER NOT NULL, " +
                    "attendance_id INTEGER, " +
                    "check_date VARCHAR(50) NOT NULL, " +
                    "scheduled_time VARCHAR(100) NOT NULL, " +
                    "window_end_time VARCHAR(100) NOT NULL, " +
                    "status VARCHAR(50) NOT NULL, " +
                    "verification_result VARCHAR(100), " +
                    "lat DOUBLE PRECISION, " +
                    "lng DOUBLE PRECISION, " +
                    "accuracy DOUBLE PRECISION, " +
                    "distance_meters DOUBLE PRECISION, " +
                    "server_timestamp VARCHAR(100), " +
                    "client_timestamp VARCHAR(100), " +
                    "failure_reason TEXT, " +
                    "flagged_for_review INTEGER DEFAULT 0, " +
                    "created_at VARCHAR(100) NOT NULL, " +
                    "faculty_review_status VARCHAR(50), " +
                    "faculty_review_notes TEXT, " +
                    "faculty_reviewed_by VARCHAR(100), " +
                    "faculty_reviewed_at VARCHAR(100))");

            try { stmt.execute("CREATE INDEX IF NOT EXISTS idx_rlc_student_date ON random_location_checks(student_id, check_date)"); } catch (SQLException ignored) {}
            try { stmt.execute("CREATE INDEX IF NOT EXISTS idx_rlc_status ON random_location_checks(status)"); } catch (SQLException ignored) {}
        }

        seedClassesAndBatches(conn);
        seedStudents(conn);
        cleanupDemoAccounts(conn);
        seedFaculties(conn);
        seedTeacherAssignments(conn);
        seedAdmins(conn);
        seedNotices(conn);
        normalizeAndSeedHistoricalAttendance(conn);
    }

    private static void initSqliteSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {

            // 1. Students Table
            stmt.execute("CREATE TABLE IF NOT EXISTS students (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "roll_no TEXT UNIQUE NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "email TEXT NOT NULL, " +
                    "branch TEXT NOT NULL, " +
                    "section TEXT NOT NULL, " +
                    "password_hash TEXT NOT NULL, " +
                    "session_token TEXT, " +
                    "permissions_accepted INTEGER DEFAULT 0)");

            // 2. Attendance Table
            stmt.execute("CREATE TABLE IF NOT EXISTS attendance (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "student_id INTEGER NOT NULL, " +
                    "date TEXT NOT NULL, " +
                    "check_in_time TEXT, " +
                    "check_out_time TEXT, " +
                    "checkin_lat REAL, " +
                    "checkin_lng REAL, " +
                    "checkin_accuracy REAL, " +
                    "checkin_photo_path TEXT, " +
                    "checkout_lat REAL, " +
                    "checkout_lng REAL, " +
                    "checkout_photo_path TEXT, " +
                    "status TEXT NOT NULL, " +
                    "UNIQUE(student_id, date), " +
                    "FOREIGN KEY(student_id) REFERENCES students(id))");

            // 3. Notices Table
            stmt.execute("CREATE TABLE IF NOT EXISTS notices (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "category TEXT NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "message TEXT NOT NULL, " +
                    "created_at TEXT NOT NULL, " +
                    "is_read INTEGER DEFAULT 0)");

            // 4. Faculties Table
            stmt.execute("CREATE TABLE IF NOT EXISTS faculties (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "faculty_id TEXT UNIQUE NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "email TEXT NOT NULL, " +
                    "branch TEXT NOT NULL, " +
                    "section TEXT NOT NULL, " +
                    "password_hash TEXT NOT NULL, " +
                    "session_token TEXT)");

            // 5. Admins Table
            stmt.execute("CREATE TABLE IF NOT EXISTS admins (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "email TEXT NOT NULL, " +
                    "password_hash TEXT NOT NULL, " +
                    "session_token TEXT)");

            // 6. Classes Table
            stmt.execute("CREATE TABLE IF NOT EXISTS classes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "code TEXT UNIQUE NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "department TEXT NOT NULL, " +
                    "semester TEXT, " +
                    "is_active INTEGER DEFAULT 1)");

            // 7. Batches Table
            stmt.execute("CREATE TABLE IF NOT EXISTS batches (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "class_id INTEGER NOT NULL, " +
                    "code TEXT NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "is_active INTEGER DEFAULT 1, " +
                    "UNIQUE(class_id, code), " +
                    "FOREIGN KEY(class_id) REFERENCES classes(id) ON DELETE CASCADE)");

            // 8. Teacher Assignments Table
            stmt.execute("CREATE TABLE IF NOT EXISTS teacher_assignments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "faculty_id INTEGER NOT NULL, " +
                    "class_id INTEGER NOT NULL, " +
                    "batch_id INTEGER, " +
                    "subject TEXT, " +
                    "created_at TEXT NOT NULL, " +
                    "FOREIGN KEY(faculty_id) REFERENCES faculties(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(class_id) REFERENCES classes(id) ON DELETE CASCADE)");

            // 9. Attendance Override Audit Table (Immutable History)
            stmt.execute("CREATE TABLE IF NOT EXISTS attendance_overrides (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "attendance_id INTEGER, " +
                    "student_id INTEGER NOT NULL, " +
                    "faculty_id INTEGER NOT NULL, " +
                    "original_status TEXT NOT NULL, " +
                    "updated_status TEXT NOT NULL, " +
                    "reason TEXT NOT NULL, " +
                    "attendance_date TEXT NOT NULL, " +
                    "override_time TEXT NOT NULL, " +
                    "FOREIGN KEY(student_id) REFERENCES students(id), " +
                    "FOREIGN KEY(faculty_id) REFERENCES faculties(id))");

            // 10. Random Check-In Settings Table
            stmt.execute("CREATE TABLE IF NOT EXISTS random_checkin_settings (" +
                    "id INTEGER PRIMARY KEY CHECK (id = 1), " +
                    "is_enabled INTEGER DEFAULT 1, " +
                    "is_paused INTEGER DEFAULT 0, " +
                    "earliest_time TEXT DEFAULT '09:15', " +
                    "latest_time TEXT DEFAULT '15:30', " +
                    "duration_minutes INTEGER DEFAULT 10, " +
                    "min_interval_minutes INTEGER DEFAULT 45, " +
                    "max_interval_minutes INTEGER DEFAULT 90, " +
                    "challenges_per_day INTEGER DEFAULT 2, " +
                    "target_type TEXT DEFAULT 'ALL', " +
                    "target_value TEXT DEFAULT 'ALL', " +
                    "updated_at TEXT)");

            stmt.execute("INSERT OR IGNORE INTO random_checkin_settings " +
                    "(id, is_enabled, is_paused, earliest_time, latest_time, duration_minutes, min_interval_minutes, max_interval_minutes, challenges_per_day, target_type, target_value, updated_at) " +
                    "VALUES (1, 1, 0, '09:15', '15:30', 10, 45, 90, 2, 'ALL', 'ALL', datetime('now'))");

            // 10b. Attendance Timer Settings Table (Checkin & Checkout Timers + Duration Slots)
            stmt.execute("CREATE TABLE IF NOT EXISTS attendance_timer_settings (" +
                    "id INTEGER PRIMARY KEY CHECK (id = 1), " +
                    "checkin_timer_enabled INTEGER DEFAULT 1, " +
                    "checkin_start_time TEXT DEFAULT '08:50', " +
                    "checkin_end_time TEXT DEFAULT '09:05', " +
                    "checkin_duration_minutes INTEGER DEFAULT 15, " +
                    "checkout_timer_enabled INTEGER DEFAULT 1, " +
                    "checkout_start_time TEXT DEFAULT '15:40', " +
                    "checkout_end_time TEXT DEFAULT '18:00', " +
                    "checkout_duration_minutes INTEGER DEFAULT 140, " +
                    "updated_at TEXT)");

            stmt.execute("INSERT OR IGNORE INTO attendance_timer_settings " +
                    "(id, checkin_timer_enabled, checkin_start_time, checkin_end_time, checkin_duration_minutes, " +
                    "checkout_timer_enabled, checkout_start_time, checkout_end_time, checkout_duration_minutes, updated_at) " +
                    "VALUES (1, 1, '08:50', '09:05', 15, 1, '15:40', '18:00', 140, datetime('now'))");

            // 11. Random Challenges Table
            stmt.execute("CREATE TABLE IF NOT EXISTS random_challenges (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "challenge_code TEXT UNIQUE NOT NULL, " +
                    "scheduled_time TEXT NOT NULL, " +
                    "deadline_time TEXT NOT NULL, " +
                    "duration_minutes INTEGER NOT NULL, " +
                    "status TEXT NOT NULL, " +
                    "target_type TEXT NOT NULL, " +
                    "target_value TEXT NOT NULL, " +
                    "is_manual INTEGER DEFAULT 0, " +
                    "created_at TEXT NOT NULL, " +
                    "created_by TEXT NOT NULL)");

            // 12. Student Random Challenges Table
            stmt.execute("CREATE TABLE IF NOT EXISTS student_random_challenges (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "challenge_id INTEGER NOT NULL, " +
                    "student_id INTEGER NOT NULL, " +
                    "status TEXT NOT NULL, " +
                    "responded_at TEXT, " +
                    "lat REAL, " +
                    "lng REAL, " +
                    "accuracy REAL, " +
                    "photo_path TEXT, " +
                    "UNIQUE(challenge_id, student_id), " +
                    "FOREIGN KEY(challenge_id) REFERENCES random_challenges(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE)");

            // 13. Random Location Verification Checks Table
            stmt.execute("CREATE TABLE IF NOT EXISTS random_location_checks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "student_id INTEGER NOT NULL, " +
                    "attendance_id INTEGER, " +
                    "check_date TEXT NOT NULL, " +
                    "scheduled_time TEXT NOT NULL, " +
                    "window_end_time TEXT NOT NULL, " +
                    "status TEXT NOT NULL, " +
                    "verification_result TEXT, " +
                    "lat REAL, " +
                    "lng REAL, " +
                    "accuracy REAL, " +
                    "distance_meters REAL, " +
                    "server_timestamp TEXT, " +
                    "client_timestamp TEXT, " +
                    "failure_reason TEXT, " +
                    "flagged_for_review INTEGER DEFAULT 0, " +
                    "created_at TEXT NOT NULL, " +
                    "FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(attendance_id) REFERENCES attendance(id) ON DELETE CASCADE)");
            try { stmt.execute("CREATE INDEX IF NOT EXISTS idx_rlc_student_date ON random_location_checks(student_id, check_date)"); } catch (SQLException ignored) {}
            try { stmt.execute("CREATE INDEX IF NOT EXISTS idx_rlc_status ON random_location_checks(status)"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE random_location_checks ADD COLUMN faculty_review_status TEXT"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE random_location_checks ADD COLUMN faculty_review_notes TEXT"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE random_location_checks ADD COLUMN faculty_reviewed_by TEXT"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE random_location_checks ADD COLUMN faculty_reviewed_at TEXT"); } catch (SQLException ignored) {}

            try { stmt.execute("ALTER TABLE notices ADD COLUMN is_read INTEGER DEFAULT 0"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE students ADD COLUMN permissions_accepted INTEGER DEFAULT 0"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE students ADD COLUMN class_id INTEGER"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE students ADD COLUMN batch_id INTEGER"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE faculties ADD COLUMN is_active INTEGER DEFAULT 1"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE faculties ADD COLUMN department TEXT"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE attendance ADD COLUMN is_override INTEGER DEFAULT 0"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE attendance ADD COLUMN override_reason TEXT"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE attendance ADD COLUMN overridden_by TEXT"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE attendance ADD COLUMN checkin_lat REAL"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE attendance ADD COLUMN checkin_lng REAL"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE attendance ADD COLUMN checkin_accuracy REAL"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE attendance ADD COLUMN checkin_photo_path TEXT"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE attendance ADD COLUMN checkout_lat REAL"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE attendance ADD COLUMN checkout_lng REAL"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE attendance ADD COLUMN checkout_photo_path TEXT"); } catch (SQLException ignored) {}

            // Seed sample classes and batches
            seedClassesAndBatches(conn);

            // Seed sample students (strictly Bhavya Kumar Bohra demo + 5 students)
            seedStudents(conn);

            // Clean up unnecessary demo/test accounts
            cleanupDemoAccounts(conn);

            // Seed sample faculties
            seedFaculties(conn);

            // Seed sample teacher assignments
            seedTeacherAssignments(conn);

            // Seed sample admins
            ResultSet rsAdm = stmt.executeQuery("SELECT COUNT(*) FROM admins");
            if (rsAdm.next() && rsAdm.getInt(1) == 0) {
                seedAdmins(conn);
            }

            // Seed sample notices if notices table is empty
            ResultSet rsN = stmt.executeQuery("SELECT COUNT(*) FROM notices");
            if (rsN.next() && rsN.getInt(1) == 0) {
                seedNotices(conn);
            }

            // Seed sample attendance
            normalizeAndSeedHistoricalAttendance(conn);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void normalizeAndSeedHistoricalAttendance(Connection conn) {
        // 1. Clean legacy epoch timestamps
        try (Statement s = conn.createStatement()) {
            s.executeUpdate("UPDATE attendance SET date = '2026-10-01' WHERE date = '1790793000000'");
            s.executeUpdate("UPDATE attendance SET date = '2026-10-03' WHERE date = '1790965800000'");
            s.executeUpdate("UPDATE attendance SET date = '2026-10-04' WHERE date = '1791052200000'");
            s.executeUpdate("UPDATE attendance SET date = '2026-10-05' WHERE date = '1791138600000'");
            s.executeUpdate("UPDATE attendance SET date = '2026-10-06' WHERE date = '1791225000000'");
            s.executeUpdate("UPDATE attendance SET date = '2026-10-07' WHERE date = '1791311400000'");
            s.executeUpdate("UPDATE attendance SET date = '2026-10-08' WHERE date = '1791397800000'");
            s.executeUpdate("UPDATE attendance SET date = '2026-10-09' WHERE date = '1791484200000'");
            s.executeUpdate("UPDATE attendance SET date = '2026-10-10' WHERE date = '1791570600000'");

            s.executeUpdate("UPDATE students SET class_id = 1, batch_id = 1 WHERE branch = 'AI' AND (class_id IS NULL OR class_id != 1)");
            s.executeUpdate("UPDATE students SET class_id = 2, batch_id = 3 WHERE branch = 'CS' AND (class_id IS NULL OR class_id != 2)");
            s.executeUpdate("UPDATE students SET class_id = 3, batch_id = 5 WHERE branch = 'ECE' AND (class_id IS NULL OR class_id != 3)");
        } catch (SQLException ignored) {}

        // 2. Conducted dates in October 2026
        String[] dates = {"2026-10-01", "2026-10-02", "2026-10-03", "2026-10-05", "2026-10-06", "2026-10-07", "2026-10-08", "2026-10-09", "2026-10-10", "2026-10-12"};

        String insertAtt = "INSERT INTO attendance (student_id, date, check_in_time, check_out_time, checkin_lat, checkin_lng, checkin_accuracy, status, is_override, override_reason, overridden_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (student_id, date) DO NOTHING";

        try (PreparedStatement ps = conn.prepareStatement(insertAtt);
             Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT id, roll_no, class_id FROM students ORDER BY id")) {

            java.util.List<Long> studentIds = new java.util.ArrayList<>();
            while (rs.next()) {
                studentIds.add(rs.getLong("id"));
            }

            for (Long sId : studentIds) {
                for (int i = 0; i < dates.length; i++) {
                    String d = dates[i];
                    long hash = (sId * 31 + i);

                    String status;
                    String cin = "08:50:00";
                    String cout = "15:45:00";
                    int isOverride = 0;
                    String reason = null;
                    String by = null;

                    // Distribute statuses realistically
                    if (sId == 8L) {
                        status = (i % 2 == 0) ? "PRESENT" : "ABSENT";
                    } else if (sId == 6L) {
                        if (i == 2 || i == 5 || i == 7) status = "ABSENT";
                        else if (i == 4) {
                            status = "PRESENT";
                            isOverride = 1;
                            reason = "Medical emergency verified by faculty";
                            by = "Dr. Rajesh Sharma (FAC001)";
                        } else status = "PRESENT";
                    } else if (sId == 7L) {
                        if (i == 3) status = "EXCUSED";
                        else status = "PRESENT";
                    } else if (sId == 1L || sId == 2L) {
                        if (i == dates.length - 1) {
                            status = "LATE";
                            cin = "09:22:00";
                            isOverride = 1;
                            reason = "College bus delay confirmed";
                            by = "Dr. Rajesh Sharma (FAC001)";
                        } else status = "PRESENT";
                    } else {
                        if (hash % 10 == 0) {
                            status = "ABSENT";
                            cin = null;
                            cout = null;
                        } else if (hash % 8 == 0) {
                            status = "LATE";
                            cin = "09:18:00";
                        } else {
                            status = "PRESENT";
                        }
                    }

                    ps.setLong(1, sId);
                    ps.setString(2, d);
                    ps.setString(3, cin);
                    ps.setString(4, cout);
                    ps.setDouble(5, 26.9131);
                    ps.setDouble(6, 75.8089);
                    ps.setDouble(7, 15.0);
                    ps.setString(8, status);
                    ps.setInt(9, isOverride);
                    ps.setString(10, reason);
                    ps.setString(11, by);
                    ps.executeUpdate();
                }
            }

            seedSampleOverrideAudits(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void seedSampleOverrideAudits(Connection conn) {
        try (Statement s = conn.createStatement()) {
            ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM attendance_overrides");
            if (rs.next() && rs.getInt(1) > 0) return;
        } catch (SQLException ignored) {}

        String sql = "INSERT INTO attendance_overrides (attendance_id, student_id, faculty_id, original_status, updated_status, reason, attendance_date, override_time) " +
                     "VALUES (?, ?, 1, ?, ?, ?, ?, datetime('now', '-2 days'))";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, 1L);
            ps.setLong(2, 6L);
            ps.setString(3, "ABSENT");
            ps.setString(4, "PRESENT");
            ps.setString(5, "Medical emergency certificate verified by department");
            ps.setString(6, "2026-10-06");
            ps.executeUpdate();

            ps.setLong(1, 2L);
            ps.setLong(2, 1L);
            ps.setString(3, "NOT_MARKED");
            ps.setString(4, "LATE");
            ps.setString(5, "College bus breakdown verified with transport office");
            ps.setString(6, "2026-10-12");
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    private static void seedClassesAndBatches(Connection conn) throws SQLException {
        try (Statement s = conn.createStatement()) {
            ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM classes");
            if (rs.next() && rs.getInt(1) > 0) return;
        }

        String insertClass = "INSERT INTO classes (code, name, department, semester, is_active) VALUES (?, ?, ?, ?, 1) ON CONFLICT (code) DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(insertClass)) {
            ps.setString(1, "AI-A");
            ps.setString(2, "Artificial Intelligence - Section A");
            ps.setString(3, "AI");
            ps.setString(4, "5th Semester");
            ps.executeUpdate();

            ps.setString(1, "CS-A");
            ps.setString(2, "Computer Science - Section A");
            ps.setString(3, "CS");
            ps.setString(4, "5th Semester");
            ps.executeUpdate();

            ps.setString(1, "ECE-A");
            ps.setString(2, "Electronics & Comm - Section A");
            ps.setString(3, "ECE");
            ps.setString(4, "5th Semester");
            ps.executeUpdate();
        }

        Long aiClassId = null, csClassId = null, eceClassId = null;
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT id, code FROM classes")) {
            while (rs.next()) {
                String code = rs.getString("code");
                if ("AI-A".equals(code)) aiClassId = rs.getLong("id");
                else if ("CS-A".equals(code)) csClassId = rs.getLong("id");
                else if ("ECE-A".equals(code)) eceClassId = rs.getLong("id");
            }
        }

        String insertBatch = "INSERT INTO batches (class_id, code, name, is_active) VALUES (?, ?, ?, 1) ON CONFLICT (class_id, code) DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(insertBatch)) {
            if (aiClassId != null) {
                ps.setLong(1, aiClassId);
                ps.setString(2, "A1");
                ps.setString(3, "Batch A1 (Roll 1-30)");
                ps.executeUpdate();

                ps.setLong(1, aiClassId);
                ps.setString(2, "A2");
                ps.setString(3, "Batch A2 (Roll 31-60)");
                ps.executeUpdate();
            }
            if (csClassId != null) {
                ps.setLong(1, csClassId);
                ps.setString(2, "C1");
                ps.setString(3, "Batch C1 (Roll 1-30)");
                ps.executeUpdate();

                ps.setLong(1, csClassId);
                ps.setString(2, "C2");
                ps.setString(3, "Batch C2 (Roll 31-60)");
                ps.executeUpdate();
            }
            if (eceClassId != null) {
                ps.setLong(1, eceClassId);
                ps.setString(2, "E1");
                ps.setString(3, "Batch E1 (Roll 1-30)");
                ps.executeUpdate();

                ps.setLong(1, eceClassId);
                ps.setString(2, "E2");
                ps.setString(3, "Batch E2 (Roll 31-60)");
                ps.executeUpdate();
            }
        }

        try (Statement s = conn.createStatement()) {
            if (aiClassId != null) {
                s.executeUpdate("UPDATE students SET class_id = " + aiClassId + " WHERE branch = 'AI'");
            }
            if (csClassId != null) {
                s.executeUpdate("UPDATE students SET class_id = " + csClassId + " WHERE branch = 'CS'");
            }
            if (eceClassId != null) {
                s.executeUpdate("UPDATE students SET class_id = " + eceClassId + " WHERE branch = 'ECE'");
            }
            s.executeUpdate("UPDATE students SET batch_id = (SELECT id FROM batches WHERE batches.class_id = students.class_id ORDER BY id ASC LIMIT 1) WHERE batch_id IS NULL");
        }
    }

    private static void seedTeacherAssignments(Connection conn) throws SQLException {
        try (Statement s = conn.createStatement()) {
            ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM teacher_assignments");
            if (rs.next() && rs.getInt(1) > 0) return;
        }

        String insertAssign = "INSERT INTO teacher_assignments (faculty_id, class_id, batch_id, subject, created_at) " +
                "VALUES ((SELECT id FROM faculties WHERE faculty_id = ?), " +
                "(SELECT id FROM classes WHERE code = ?), " +
                "(SELECT id FROM batches WHERE code = ? LIMIT 1), ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(insertAssign)) {
            ps.setString(1, "FAC001");
            ps.setString(2, "AI-A");
            ps.setString(3, "A1");
            ps.setString(4, "Deep Learning");
            ps.setString(5, LocalDate.now().toString());
            ps.executeUpdate();

            ps.setString(1, "FAC001");
            ps.setString(2, "AI-A");
            ps.setString(3, "A2");
            ps.setString(4, "Machine Learning Lab");
            ps.setString(5, LocalDate.now().toString());
            ps.executeUpdate();

            ps.setString(1, "FAC002");
            ps.setString(2, "CS-A");
            ps.setString(3, "C1");
            ps.setString(4, "Operating Systems");
            ps.setString(5, LocalDate.now().toString());
            ps.executeUpdate();

            ps.setString(1, "FAC003");
            ps.setString(2, "ECE-A");
            ps.setString(3, "E1");
            ps.setString(4, "VLSI Design");
            ps.setString(5, LocalDate.now().toString());
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    private static void seedFaculties(Connection conn) throws SQLException {
        String facPassHash = hashPassword("FacultyPassword123!");
        String insertFac = "INSERT INTO faculties (faculty_id, name, email, branch, section, password_hash) VALUES (?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT(faculty_id) DO UPDATE SET password_hash = excluded.password_hash, name = excluded.name, is_active = 1";
        try (PreparedStatement ps = conn.prepareStatement(insertFac)) {
            ps.setString(1, "FAC001");
            ps.setString(2, "Dr. Rajesh Sharma");
            ps.setString(3, "rajesh.sharma@aryacollege.edu.in");
            ps.setString(4, "AI");
            ps.setString(5, "Section A");
            ps.setString(6, facPassHash);
            ps.executeUpdate();

            ps.setString(1, "FAC002");
            ps.setString(2, "Prof. Priya Verma");
            ps.setString(3, "priya.verma@aryacollege.edu.in");
            ps.setString(4, "CS");
            ps.setString(5, "Section A");
            ps.setString(6, facPassHash);
            ps.executeUpdate();

            ps.setString(1, "FAC003");
            ps.setString(2, "Dr. Amit Patel");
            ps.setString(3, "amit.patel@aryacollege.edu.in");
            ps.setString(4, "ECE");
            ps.setString(5, "Section A");
            ps.setString(6, facPassHash);
            ps.executeUpdate();
        }
    }

    private static void seedAdmins(Connection conn) throws SQLException {
        String adminPassHash = hashPassword("AdminPassword123!");
        String insertAdm = "INSERT INTO admins (username, name, email, password_hash) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT(username) DO UPDATE SET password_hash = excluded.password_hash, name = excluded.name";
        try (PreparedStatement ps = conn.prepareStatement(insertAdm)) {
            ps.setString(1, "admin");
            ps.setString(2, "Chief Administrator");
            ps.setString(3, "admin@aryacollege.edu.in");
            ps.setString(4, adminPassHash);
            ps.executeUpdate();
        }
    }

    private static void seedStudents(Connection conn) throws SQLException {
        String studentPassHash = hashPassword("StudentPassword123!");
        String insertStudent = "INSERT INTO students (roll_no, name, email, branch, section, password_hash, permissions_accepted, class_id, batch_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, 1, ?, ?) " +
                "ON CONFLICT(roll_no) DO UPDATE SET password_hash = excluded.password_hash, permissions_accepted = 1, class_id = excluded.class_id, batch_id = excluded.batch_id";

        Object[][] students = {
            {"24EARAD031", "Bhavya Kumar Bohra", "bhavya.24earad031@aryacollege.edu.in", "AI", "Section A", 1L, 1L},
            {"24EARAD001", "Aarav Sharma", "aarav.24earad001@aryacollege.edu.in", "AI", "Section A", 1L, 1L},
            {"24EARAD002", "Ananya Gupta", "ananya.24earad002@aryacollege.edu.in", "AI", "Section A", 1L, 1L},
            {"24ECS001", "Ishan Verma", "ishan.24ecs001@aryacollege.edu.in", "CS", "Section A", 2L, 3L},
            {"24ECS002", "Diya Joshi", "diya.24ecs002@aryacollege.edu.in", "CS", "Section A", 2L, 3L},
            {"24ECE001", "Aditya Roy", "aditya.24ece001@aryacollege.edu.in", "ECE", "Section A", 3L, 5L}
        };

        try (PreparedStatement ps = conn.prepareStatement(insertStudent)) {
            for (Object[] s : students) {
                ps.setString(1, (String) s[0]);
                ps.setString(2, (String) s[1]);
                ps.setString(3, (String) s[2]);
                ps.setString(4, (String) s[3]);
                ps.setString(5, (String) s[4]);
                ps.setString(6, studentPassHash);
                ps.setLong(7, (Long) s[5]);
                ps.setLong(8, (Long) s[6]);
                ps.executeUpdate();
            }
        }
    }

    private static void cleanupDemoAccounts(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM students WHERE roll_no NOT IN (" +
                    "'24EARAD031', '24EARAD001', '24EARAD002', '24ECS001', '24ECS002', '24ECE001')");

            stmt.executeUpdate("DELETE FROM faculties WHERE faculty_id LIKE 'TCH_TEST_%'");

            stmt.executeUpdate("UPDATE students SET class_id = 1, batch_id = 1 WHERE branch = 'AI'");
            stmt.executeUpdate("UPDATE students SET class_id = 2, batch_id = 3 WHERE branch = 'CS'");
            stmt.executeUpdate("UPDATE students SET class_id = 3, batch_id = 5 WHERE branch = 'ECE'");
            stmt.executeUpdate("UPDATE students SET permissions_accepted = 1");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void seedNotices(Connection conn) throws SQLException {
        String insertNotice = "INSERT INTO notices (category, title, message, created_at, is_read) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertNotice)) {
            ps.setString(1, "IMPORTANT");
            ps.setString(2, "Important Notice");
            ps.setString(3, "Midterm exam form submission\nlast date: 15 Oct 2026.");
            ps.setString(4, "2026-10-10 10:00:00");
            ps.setInt(5, 0);
            ps.executeUpdate();

            ps.setString(1, "GENERAL");
            ps.setString(2, "General");
            ps.setString(3, "College will remain closed on\n12 Oct 2026.");
            ps.setString(4, "2026-10-08 09:30:00");
            ps.setInt(5, 1);
            ps.executeUpdate();

            ps.setString(1, "ACADEMIC");
            ps.setString(2, "Academic");
            ps.setString(3, "Regular classes will resume\nfrom 13 Oct 2026.");
            ps.setString(4, "2026-10-05 11:00:00");
            ps.setInt(5, 1);
            ps.executeUpdate();

            ps.setString(1, "GENERAL");
            ps.setString(2, "General");
            ps.setString(3, "Review your attendance regularly.");
            ps.setString(4, "2026-10-01 08:30:00");
            ps.setInt(5, 1);
            ps.executeUpdate();
        }
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }
}
