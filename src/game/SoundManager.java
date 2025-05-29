package game;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import java.net.URL;

public class SoundManager {

    private MediaPlayer moveSoundPlayer;

    public SoundManager() {
    }

    public MediaPlayer loadSound(String soundUrl) { // 参数soundUrl应该是一个完整的URL字符串
        if (soundUrl == null || soundUrl.isEmpty()) {
            System.err.println("错误：提供的声音URL为空或无效。");
            return null;
        }
        try {
            // Media构造函数可以直接接受一个格式正确的URL字符串
            Media sound = new Media(soundUrl);
            return new MediaPlayer(sound);
        } catch (Exception e) {
            // 明确打印出导致问题的URL，有助于调试
            System.err.println("加载声音媒体时出错 (URL: " + soundUrl + "): " + e.getMessage());
            // e.printStackTrace(); // 可以暂时注释掉，如果错误信息太冗余
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
