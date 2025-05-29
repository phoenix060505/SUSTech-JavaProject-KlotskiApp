package game;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import java.net.URL;

public class SoundManager {

    private MediaPlayer moveSoundPlayer;

    public SoundManager() {
        // 构造函数可以留空，或者用于初始化多个声音效果
    }
    public MediaPlayer loadSound(String soundResourcePath) { //例如: "/sounds/moveSound.wav"
        try {
            URL resource = getClass().getResource(soundResourcePath);
            if (resource == null) {
                // 当资源未找到时，soundResourcePath 将是您传入的相对路径，如 "/sounds/moveSound.wav"
                System.err.println("错误：声音资源未找到 从类路径 - " + soundResourcePath);
                return null;
            }
            Media sound = new Media(resource.toExternalForm());
            return new MediaPlayer(sound);
        } catch (Exception e) {
            System.err.println("加载声音时出错 (" + soundResourcePath + "): " + e.getMessage());
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
