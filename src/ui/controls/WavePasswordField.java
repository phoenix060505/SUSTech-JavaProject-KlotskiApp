package ui.controls;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition; // 确保只导入一次
import javafx.geometry.Pos;
import javafx.scene.Node; // 需要导入 Node
import javafx.scene.control.PasswordField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

/** 带浮动标签的密码框 (标签字符逐个动画) */
public class WavePasswordField extends StackPane {

    private final PasswordField input;
    private final HBox labelBox; // HBox 整体不再做 TranslateTransition
    private final Pane barPane;
    private final Rectangle centerBar;

    private double controlPrefWidth;

    // 动画常量
    private static final Duration BAR_ANIMATION_DURATION = Duration.millis(300);
    // 标签字符动画参数
    private static final Duration CHAR_ANIM_DURATION = Duration.millis(350); // 每个字符的动画时长
    private static final double CHAR_ANIM_DELAY_INCREMENT = 50; // 每个字符之间的延迟 ms
    private static final double CHAR_TRANSLATE_Y_FOCUSED = -15; // 每个字符向上移动的距离
    private static final double CHAR_TRANSLATE_Y_BLURRED = 0;   // 每个字符回到 HBox 内的原始 Y 位置 (通常是0)

    private static final double LABEL_BOX_Y_FOCUSED_ADJUST = -5; // HBox 整体的微调（如果需要）
    private static final double LABEL_BOX_Y_BLURRED_INITIAL = 10; // HBox 初始的Y偏移

    private static final Color BAR_FOCUSED_COLOR = Color.web("#5264AE");
    private static final Color BAR_DEFAULT_COLOR = Color.web("#5264AE");

    // 用于存储每个字符的原始Y位置（如果它们在HBox中有不同基线，虽然不太可能）
    // private final java.util.List<Double> originalCharYPositions = new java.util.ArrayList<>();


    public WavePasswordField(String label) {
        this(label, 200.0);
    }

    public WavePasswordField(String label, double prefWidth) {
        this.controlPrefWidth = prefWidth;

        input = new PasswordField();
        input.getStyleClass().add("wave-input");
        input.setPrefWidth(this.controlPrefWidth);

        labelBox = new HBox();
        labelBox.getStyleClass().add("wave-label");
        for (int i = 0; i < label.length(); i++) {
            Text t = new Text(String.valueOf(label.charAt(i)));
            t.getStyleClass().add("wave-label-char");
            // 不再使用内联CSS延迟，我们将通过JavaFX动画的delay属性控制
            // t.setStyle(String.format("-fx-transition-delay: %.2fs;", i * 0.05));
            labelBox.getChildren().add(t);
            // originalCharYPositions.add(t.getTranslateY()); // 记录原始Y（通常是0）
        }
        labelBox.setMouseTransparent(true);

        barPane = new Pane();
        barPane.getStyleClass().add("wave-bar");
        barPane.setPrefWidth(this.controlPrefWidth);
        barPane.setMouseTransparent(true);

        centerBar = new Rectangle(0, 2, BAR_DEFAULT_COLOR);
        centerBar.setTranslateX(this.controlPrefWidth / 2);
        barPane.getChildren().add(centerBar);

        getChildren().addAll(input, barPane, labelBox);
        StackPane.setAlignment(labelBox, Pos.TOP_LEFT); // labelBox 在 StackPane 中的对齐
        labelBox.setTranslateX(5); // labelBox 整体的 X 偏移
        labelBox.setTranslateY(LABEL_BOX_Y_BLURRED_INITIAL); // labelBox 整体的初始 Y 偏移

        barPane.translateYProperty().bind(input.heightProperty().subtract(centerBar.getHeight()));

        input.focusedProperty().addListener((o, ov, foc) -> {
            if (foc) {
                focusAnim();
            } else if (input.getText().isEmpty()) {
                blurAnim();
            } else {
                subtleBlurAnim();
            }
        });
    }

    private void focusAnim() {
        // HBox 整体可能也需要微小的Y轴调整，以配合字符的上升
        // 如果不调整，则字符的 CHAR_TRANSLATE_Y_FOCUSED 是相对于 HBox 内部的
        // 如果希望 HBox 整体也向上移动一点，可以加一个 TranslateTransition 给 labelBox
        // TranslateTransition labelBoxTransition = new TranslateTransition(Duration.millis(100), labelBox);
        // labelBoxTransition.setToY(LABEL_BOX_Y_BLURRED_INITIAL + LABEL_BOX_Y_FOCUSED_ADJUST); // 例如向上微调
        // labelBoxTransition.play();

        for (int i = 0; i < labelBox.getChildren().size(); i++) {
            Node charNode = labelBox.getChildren().get(i);
            TranslateTransition ttChar = new TranslateTransition(CHAR_ANIM_DURATION, charNode);
            ttChar.setToY(CHAR_TRANSLATE_Y_FOCUSED); // 字符相对于其在HBox中的位置向上移动
            ttChar.setDelay(Duration.millis(i * CHAR_ANIM_DELAY_INCREMENT));
            ttChar.setInterpolator(Interpolator.EASE_OUT);
            ttChar.play();
        }

        // 下划线动画保持不变
        Timeline barTimeline = new Timeline();
        KeyValue kvBarWidth = new KeyValue(centerBar.widthProperty(), this.controlPrefWidth, Interpolator.EASE_OUT);
        KeyValue kvBarTranslateX = new KeyValue(centerBar.translateXProperty(), 0, Interpolator.EASE_OUT);
        KeyValue kvBarFill = new KeyValue(centerBar.fillProperty(), BAR_FOCUSED_COLOR, Interpolator.EASE_BOTH);
        KeyFrame kf = new KeyFrame(BAR_ANIMATION_DURATION, kvBarWidth, kvBarTranslateX, kvBarFill);
        barTimeline.getKeyFrames().add(kf);
        barTimeline.play();
    }

    private void blurAnim() {
        // 如果 HBox 整体有Y轴调整，这里也需要恢复
        // TranslateTransition labelBoxTransition = new TranslateTransition(Duration.millis(100), labelBox);
        // labelBoxTransition.setToY(LABEL_BOX_Y_BLURRED_INITIAL);
        // labelBoxTransition.play();

        for (int i = 0; i < labelBox.getChildren().size(); i++) {
            Node charNode = labelBox.getChildren().get(i);
            TranslateTransition ttChar = new TranslateTransition(CHAR_ANIM_DURATION, charNode);
            // double originalY = originalCharYPositions.get(i); // 获取原始Y
            ttChar.setToY(CHAR_TRANSLATE_Y_BLURRED); // 字符回到其在HBox中的原始Y位置 (通常是0)
            ttChar.setDelay(Duration.millis(i * CHAR_ANIM_DELAY_INCREMENT)); // 可以反向延迟或同样延迟
            ttChar.setInterpolator(Interpolator.EASE_IN);
            ttChar.play();
        }

        // 下划线动画保持不变
        Timeline barTimeline = new Timeline();
        KeyValue kvBarWidth = new KeyValue(centerBar.widthProperty(), 0, Interpolator.EASE_IN);
        KeyValue kvBarTranslateX = new KeyValue(centerBar.translateXProperty(), this.controlPrefWidth / 2, Interpolator.EASE_IN);
        KeyValue kvBarFill = new KeyValue(centerBar.fillProperty(), BAR_DEFAULT_COLOR, Interpolator.EASE_BOTH);
        KeyFrame kf = new KeyFrame(BAR_ANIMATION_DURATION, kvBarWidth, kvBarTranslateX, kvBarFill);
        barTimeline.getKeyFrames().add(kf);
        barTimeline.play();
    }

    private void subtleBlurAnim() {
        // 字符保持在上方状态 (或者可以播一个非常快的动画回到上方，如果它们可能不在)
        for (int i = 0; i < labelBox.getChildren().size(); i++) {
            Node charNode = labelBox.getChildren().get(i);
            // 确保字符在上方
            if (charNode.getTranslateY() != CHAR_TRANSLATE_Y_FOCUSED) {
                // 可以选择直接设置，或者快速动画到目标位置
                // charNode.setTranslateY(CHAR_TRANSLATE_Y_FOCUSED);
                TranslateTransition ttChar = new TranslateTransition(Duration.millis(50), charNode); // 快速动画
                ttChar.setToY(CHAR_TRANSLATE_Y_FOCUSED);
                ttChar.setDelay(Duration.millis(i * CHAR_ANIM_DELAY_INCREMENT * 0.5)); // 更快的延迟
                ttChar.play();
            }
        }
        // HBox 整体Y位置也应保持在焦点调整后的位置
        // if (labelBox.getTranslateY() != LABEL_BOX_Y_BLURRED_INITIAL + LABEL_BOX_Y_FOCUSED_ADJUST) {
        //     labelBox.setTranslateY(LABEL_BOX_Y_BLURRED_INITIAL + LABEL_BOX_Y_FOCUSED_ADJUST);
        // }


        // 下划线动画保持不变 (仅颜色变化)
        Timeline barTimeline = new Timeline();
        KeyValue kvBarWidth = new KeyValue(centerBar.widthProperty(), this.controlPrefWidth, Interpolator.EASE_OUT);
        KeyValue kvBarTranslateX = new KeyValue(centerBar.translateXProperty(), 0, Interpolator.EASE_OUT);
        KeyValue kvBarFill = new KeyValue(centerBar.fillProperty(), BAR_DEFAULT_COLOR, Interpolator.EASE_BOTH);
        KeyFrame kf = new KeyFrame(BAR_ANIMATION_DURATION, kvBarWidth, kvBarTranslateX, kvBarFill);
        barTimeline.getKeyFrames().add(kf);
        barTimeline.play();
    }

    public String getText() { return input.getText(); }
    public void clear() {
        input.clear();
        if (!input.isFocused()) {
            blurAnim();
        }
    }
    public PasswordField getPasswordField() { return input; }

    // 新增 setText 方法以正确处理标签状态
    public void setText(String text) {
        input.setText(text);
        if (text == null || text.isEmpty()) {
            if (!input.isFocused()) {
                blurAnim(); // 触发字符回到原位
            }
        } else {
            if (!input.isFocused()) {
                // 如果有文本且未聚焦，字符应该在上方
                // HBox 整体Y位置也应是焦点调整后的位置
                // if (labelBox.getTranslateY() != LABEL_BOX_Y_BLURRED_INITIAL + LABEL_BOX_Y_FOCUSED_ADJUST) {
                //    labelBox.setTranslateY(LABEL_BOX_Y_BLURRED_INITIAL + LABEL_BOX_Y_FOCUSED_ADJUST);
                // }
                for (int i = 0; i < labelBox.getChildren().size(); i++) {
                    Node charNode = labelBox.getChildren().get(i);
                    charNode.setTranslateY(CHAR_TRANSLATE_Y_FOCUSED); // 直接设置到目标位置
                }
                subtleBlurAnim(); // 触发下划线动画
            } else {
                // 有文本且聚焦，focusAnim 已经处理或正在处理
            }
        }
    }
}