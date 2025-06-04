import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import user.User;
import util.GameFileManager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GameFileManagerTest {
    private static final String USER_FILE = "users.dat";

    @BeforeEach
    public void cleanFile() {
        File f = new File(USER_FILE);
        if (f.exists()) {
            f.delete();
        }
    }

    @AfterEach
    public void after() {
        File f = new File(USER_FILE);
        if (f.exists()) {
            f.delete();
        }
    }

    @Test
    public void testLoadUsersWithCorruptedFileReturnsEmptyMap() throws IOException {
        GameFileManager manager = new GameFileManager();
        Map<String, User> users = new HashMap<>();
        users.put("alice", new User("alice", "pass"));

        assertTrue(manager.saveUsers(users), "saveUsers should succeed");

        // tamper with the saved file
        RandomAccessFile raf = new RandomAccessFile(USER_FILE, "rw");
        if (raf.length() > 0) {
            raf.seek(0);
            byte b = raf.readByte();
            raf.seek(0);
            raf.writeByte(b ^ 0xFF); // flip bits of first byte
        }
        raf.close();

        GameFileManager newManager = new GameFileManager();
        Map<String, User> loaded = newManager.loadUsers();
        assertNotNull(loaded, "loadUsers should not return null");
        assertTrue(loaded.isEmpty(), "Corrupted file should result in empty map");
    }
}
