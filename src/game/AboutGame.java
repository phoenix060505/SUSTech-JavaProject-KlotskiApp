package game;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class AboutGame {
    public static void applyFadeTransition(Button label) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.5), label);
        fadeTransition.setFromValue(0.0);    // 起始透明度
        fadeTransition.setToValue(1.0);     // 结束透明度
        fadeTransition.setCycleCount(1);     // 只播放一次
        fadeTransition.setInterpolator(Interpolator.EASE_IN); // 缓动效果
        // 保证Label可见性
        label.setVisible(true);
        fadeTransition.play();
    }

    public void applyFadeTransition(Label label) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.1), label);
        fadeTransition.setFromValue(0.0);    // 起始透明度
        fadeTransition.setToValue(1.0);     // 结束透明度
        fadeTransition.setCycleCount(1);     // 只播放一次
        fadeTransition.setInterpolator(Interpolator.EASE_IN); // 缓动效果
        //动画参数定制化
        //fadeTransition.setDelay(Duration.seconds(0.5)); // 添加0.5秒延迟出现
        fadeTransition.setRate(0.8); // 调节播放速度
        // 保证Label可见性
        label.setVisible(true);
        fadeTransition.play();
    }
}
