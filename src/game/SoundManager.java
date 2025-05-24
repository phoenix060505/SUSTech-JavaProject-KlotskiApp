package game;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;

public class SoundManager {

    private MediaPlayer moveSoundPlayer;

    public SoundManager() {
        // 构造函数可以留空，或者用于初始化多个声音效果
    }
    public MediaPlayer loadSound(String soundFilePath) {
        try {
            File file = new File(soundFilePath);
            if (!file.exists()) {
                System.err.println("错误：声音文件未找到 - " + soundFilePath);
                return null;
            }
            Media sound = new Media(file.toURI().toString());
            return new MediaPlayer(sound);
        } catch (Exception e) {
            System.err.println("加载声音时出错: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static void playSound(MediaPlayer player) {
        if (player != null) {
            player.stop(); // 如果正在播放，则停止
            player.play(); // 从头开始播放
        }
    }
    
    public static void stopSound(MediaPlayer player) {
    if (player != null) {
      player.stop();
    }
  }
  public static void setVolume(MediaPlayer player, double value) {
    if (player != null) {
      player.setVolume(Math.max(0, Math.min(1, value)));
    }
  }
}
