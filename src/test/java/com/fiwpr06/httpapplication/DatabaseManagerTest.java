package com.fiwpr06.httpapplication;

import com.fiwpr06.httpapplication.common.model.AccessLog;
import com.fiwpr06.httpapplication.server.database.AccessLogDao;
import com.fiwpr06.httpapplication.server.database.DatabaseManager;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseManagerTest {

    @Test
    public void testDatabaseConnectionAndInitialization() throws Exception {
        DatabaseManager db = DatabaseManager.getInstance();
        assertNotNull(db);
        assertNotNull(db.getActiveDbType());

        try (Connection conn = db.getConnection()) {
            assertNotNull(conn);
            assertFalse(conn.isClosed());
        }
    }

    @Test
    public void testAccessLogPersistence() {
        AccessLogDao dao = new AccessLogDao();
        AccessLog log = new AccessLog("127.0.0.1", "GET", "/test-uri", 200, 1024, "JUnit-Runner");
        dao.insertLog(log);

        List<AccessLog> logs = dao.getRecentLogs(10);
        assertNotNull(logs);
        assertFalse(logs.isEmpty());
        assertTrue(logs.stream().anyMatch(l -> "/test-uri".equals(l.getUri())));
    }

    @Test
    public void testAccessLogStatistics() {
        AccessLogDao dao = new AccessLogDao();
        long initialCount = dao.getTotalRequests();
        dao.insertLog(new AccessLog("192.168.1.100", "GET", "/api/status", 200, 500, "JUnit"));

        assertEquals(initialCount + 1, dao.getTotalRequests());
        assertTrue(dao.getTotalBytesSent() >= 500);
    }
}
