package util;

import model.GameState;
import user.User;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.zip.CRC32; // Import for checksum calculation
import java.io.ByteArrayOutputStream; // Import for checksum calculation

public class GameFileManager {
    private static final String USER_FILE = "users.dat";//用户数据文件路径
    private static final String SAVE_DIR = "saves/";//游戏存档目录
    private ScheduledExecutorService scheduler;//定时器
    private static final long SAVE_INTERVAL = 5; // 5 seconds interval
    private ScheduledFuture<?> currentAutoSaveTask;//当前自动保存任务
    //将游戏状态保存到文件
    public GameFileManager() {
        //如果保存文件的目录不存在则创建目录
        File saveDir = new File(SAVE_DIR);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        //创建了一个以时间为轴的线性执行器，打开，运行，关闭任务
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }
    private static class UserDataWrapper implements Serializable {
        private static final long serialVersionUID = 2L; // Use a new serialVersionUID
        private Map<String, User> users;
        private long checksum;

        public UserDataWrapper(Map<String, User> users, long checksum) {
            this.users = users;
            this.checksum = checksum;
        }

        public Map<String, User> getUsers() {
            return users;
        }

        public long getChecksum() {
            return checksum;
        }
    }
    private long calculateChecksum(Map<String, User> usersObject) {
        if (usersObject == null) {
            return 0L; // Or some other default for null input
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(usersObject);
            byte[] bytes = baos.toByteArray();
            CRC32 crc32 = new CRC32();
            crc32.update(bytes);
            return crc32.getValue();
        } catch (IOException e) {
            // This should ideally not happen with ByteArrayOutputStream
            System.err.println("Error calculating checksum: " + e.getMessage());
            e.printStackTrace();
            return -1L; // Indicate an error in checksum calculation
        }
    }
    //保存文件方法，要求输入username和GameState state(GameState的定义在GameState文件当中，包含board，username，currentLevel，moveHistory，isGameWon)
    public boolean saveGame(String username, GameState state) {
        if (username == null || state == null) {
            //Java标准的错误输出流，通常用于打印错误信息或调试信息
            System.err.println("Cannot save game: username or state is null");
            return false;
        }
        //try-catch异常处理块，如创建文件，写入文件的时候可能会出现各种原因
        // （如磁盘已满，没有写入权限，文件路径无效等）失败，使用try-catch块可以避免程序崩溃
        try {
            //创建一个向文件写入数据的输出流
            FileOutputStream fileOut = new FileOutputStream(SAVE_DIR + username + ".save");
            //将Java对象转换为字节序列，这个过程称之为序列化，并写入输出流
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(state);//state就是传入的GameState
            out.close();//关闭ObjectOutPutStream
            fileOut.close();//关闭FileOutputStream
            return true;//如果都么有抛出异常则返回
        } catch (IOException e) {//如果try代码块中的任何代码抛出了IOException异常或其子类型的异常，程序就会跳转执行这个模块
            System.err.println("Error saving game for user " + username + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

        //从文件中加载GameState
        public GameState loadGame(String username) {
        try {//和上面一样
            FileInputStream fileIn = new FileInputStream(SAVE_DIR + username + ".save");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            GameState state = (GameState) in.readObject();
            in.close();
            fileIn.close();
            return state;
        } catch (IOException | ClassNotFoundException e) {//报错信息
            System.err.println("Error loading game for user " + username + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // 将User存储到User_File
    public boolean saveUsers(Map<String, User> users) {
        if (users == null) {
            System.err.println("Cannot save null users map.");
            return false;
        }
        long checksum = calculateChecksum(users);
        UserDataWrapper wrapper = new UserDataWrapper(users, checksum);

        try (FileOutputStream fileOut = new FileOutputStream(USER_FILE);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(wrapper);
            System.out.println("User data saved successfully with checksum.");
            return true;
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Inside GameFileManager.java

    @SuppressWarnings("unchecked")
    public Map<String, User> loadUsers() {
        File file = new File(USER_FILE);
        if (!file.exists()) {
            System.out.println("User file not found. Returning new empty map.");
            return new HashMap<>(); // No file, return empty map
        }

        try (FileInputStream fileIn = new FileInputStream(USER_FILE);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            Object readObject = in.readObject();

            if (readObject instanceof UserDataWrapper) {
                UserDataWrapper wrapper = (UserDataWrapper) readObject;
                Map<String, User> users = wrapper.getUsers();
                long storedChecksum = wrapper.getChecksum();

                // It's crucial that calculateChecksum uses the exact same serialization
                // process as what was used to generate the original bytes for the stored checksum.
                long calculatedChecksum = calculateChecksum(users);

                if (storedChecksum == calculatedChecksum) {
                    System.out.println("User data loaded successfully. Checksum matches.");
                    return users != null ? users : new HashMap<>();
                } else {
                    System.err.println("User data checksum mismatch. File may be corrupted or tampered with.");
                    System.err.println("Stored: " + storedChecksum + ", Calculated: " + calculatedChecksum);
                    // Optionally, you could rename or delete the corrupted file here:
                    // file.renameTo(new File(USER_FILE + ".corrupted-" + System.currentTimeMillis()));
                    System.out.println("Loading default empty user data due to checksum mismatch.");
                    return new HashMap<>(); // Checksum mismatch
                }
            } else {
                // This could be an old file format or a severely corrupted file
                System.err.println("User data file is in an unexpected format or severely corrupted.");
                // Optionally, rename or delete:
                // file.renameTo(new File(USER_FILE + ".unknownformat-" + System.currentTimeMillis()));
                System.out.println("Loading default empty user data due to unexpected format.");
                return new HashMap<>();
            }
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            System.err.println("Error loading or validating user data: " + e.getMessage());
            e.printStackTrace();
            // Optionally, rename or delete the corrupted file here:
            // file.renameTo(new File(USER_FILE + ".corrupted-exception-" + System.currentTimeMillis()));
            System.out.println("Loading default empty user data due to exception during load.");
            return new HashMap<>(); // Error during deserialization or casting
        }
    }
    //自动保存方法
    public void startAutoSave(User user, Supplier<GameState> stateSupplier) {
        //去除任何已经保存过的文件
        stopAutoSave();
        //报错信息
        if (user == null || user.getUsername() == null || user.getUsername().isEmpty()) {
            System.err.println("Cannot start auto-save: user is null or has no username");
            return;
        }
        //基于ScheduledExecutorService实现自动保存方法
        currentAutoSaveTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                GameState currentState = stateSupplier.get();
                if (currentState != null) {
                    if (saveGame(user.getUsername(), currentState)) {
                        System.out.println("Game auto-saved for user: " + user.getUsername());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error during auto-save: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, SAVE_INTERVAL, TimeUnit.SECONDS);
    }

    //停止自动保存方法
    public void stopAutoSave() {
        if (currentAutoSaveTask != null) {
            currentAutoSaveTask.cancel(false);
            currentAutoSaveTask = null;
            System.out.println("Auto-save stopped");
        }
    }

    //停止整个时间轴的线程
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            stopAutoSave();
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("GameFileManager scheduler shutdown");
        }
    }
}