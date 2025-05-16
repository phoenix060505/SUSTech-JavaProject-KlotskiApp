package ui.controls;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Node; // 需要导入 Node
import javafx.scene.control.PasswordField; // 类型为 PasswordField
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

/** 带浮动标签的密码确认框 (标签字符逐个动画) */
public class WavePasswordConfirm extends StackPane {

    private final PasswordField input; // 类型为 PasswordField
    private final HBox labelBox;
    private final Pane barPane;
    private final Rectangle centerBar;

    private double controlPrefWidth;

    // 动画常量
    private static final Duration BAR_ANIMATION_DURATION = Duration.millis(300);
    private static final Duration CHAR_ANIM_DURATION = Duration.millis(350);
    private static final double CHAR_ANIM_DELAY_INCREMENT = 50; // ms
    private static final double CHAR_TRANSLATE_Y_FOCUSED = -15;
    private static final double CHAR_TRANSLATE_Y_BLURRED = 0;

    private static final double LABEL_BOX_Y_BLURRED_INITIAL = 10;

    private static final Color BAR_FOCUSED_COLOR = Color.web("#5264AE");
    private static final Color BAR_DEFAULT_COLOR = Color.web("#5264AE");


    public WavePasswordConfirm(String label) {
        this(label, 200.0);
    }

    public WavePasswordConfirm(String label, double prefWidth) {
        this.controlPrefWidth = prefWidth;

        input = new PasswordField(); // 使用 PasswordField
        input.getStyleClass().add("wave-input");
        input.setPrefWidth(this.controlPrefWidth);

        labelBox = new HBox();
        labelBox.getStyleClass().add("wave-label");
        for (int i = 0; i < label.length(); i++) {
            Text t = new Text(String.valueOf(label.charAt(i)));
            t.getStyleClass().add("wave-label-char");
            labelBox.getChildren().add(t);
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
        StackPane.setAlignment(labelBox, Pos.TOP_LEFT);
        labelBox.setTranslateX(5);
        labelBox.setTranslateY(LABEL_BOX_Y_BLURRED_INITIAL);

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
        for (int i = 0; i < labelBox.getChildren().size(); i++) {
            Node charNode = labelBox.getChildren().get(i);
            TranslateTransition ttChar = new TranslateTransition(CHAR_ANIM_DURATION, charNode);
            ttChar.setToY(CHAR_TRANSLATE_Y_FOCUSED);
            ttChar.setDelay(Duration.millis(i * CHAR_ANIM_DELAY_INCREMENT));
            ttChar.setInterpolator(Interpolator.EASE_OUT);
            ttChar.play();
        }

        Timeline barTimeline = new Timeline();
        KeyValue kvBarWidth = new KeyValue(centerBar.widthProperty(), this.controlPrefWidth, Interpolator.EASE_OUT);
        KeyValue kvBarTranslateX = new KeyValue(centerBar.translateXProperty(), 0, Interpolator.EASE_OUT);
        KeyValue kvBarFill = new KeyValue(centerBar.fillProperty(), BAR_FOCUSED_COLOR, Interpolator.EASE_BOTH);
        KeyFrame kf = new KeyFrame(BAR_ANIMATION_DURATION, kvBarWidth, kvBarTranslateX, kvBarFill);
        barTimeline.getKeyFrames().add(kf);
        barTimeline.play();
    }

    private void blurAnim() {
        for (int i = 0; i < labelBox.getChildren().size(); i++) {
            Node charNode = labelBox.getChildren().get(i);
            TranslateTransition ttChar = new TranslateTransition(CHAR_ANIM_DURATION, charNode);
            ttChar.setToY(CHAR_TRANSLATE_Y_BLURRED);
            ttChar.setDelay(Duration.millis(i * CHAR_ANIM_DELAY_INCREMENT));
            ttChar.setInterpolator(Interpolator.EASE_IN);
            ttChar.play();
        }

        Timeline barTimeline = new Timeline();
        KeyValue kvBarWidth = new KeyValue(centerBar.widthProperty(), 0, Interpolator.EASE_IN);
        KeyValue kvBarTranslateX = new KeyValue(centerBar.translateXProperty(), this.controlPrefWidth / 2, Interpolator.EASE_IN);
        KeyValue kvBarFill = new KeyValue(centerBar.fillProperty(), BAR_DEFAULT_COLOR, Interpolator.EASE_BOTH);
        KeyFrame kf = new KeyFrame(BAR_ANIMATION_DURATION, kvBarWidth, kvBarTranslateX, kvBarFill);
        barTimeline.getKeyFrames().add(kf);
        barTimeline.play();
    }

    private void subtleBlurAnim() {
        for (int i = 0; i < labelBox.getChildren().size(); i++) {
            Node charNode = labelBox.getChildren().get(i);
            if (charNode.getTranslateY() != CHAR_TRANSLATE_Y_FOCUSED) {
                TranslateTransition ttChar = new TranslateTransition(Duration.millis(50), charNode);
                ttChar.setToY(CHAR_TRANSLATE_Y_FOCUSED);
                ttChar.setDelay(Duration.millis(i * CHAR_ANIM_DELAY_INCREMENT * 0.5));
                ttChar.play();
            }
        }

        Timeline barTimeline = new Timeline();
        KeyValue kvBarWidth = new KeyValue(centerBar.widthProperty(), this.controlPrefWidth, Interpolator.EASE_OUT);
        KeyValue kvBarTranslateX = new KeyValue(centerBar.translateXProperty(), 0, Interpolator.EASE_OUT);
        KeyValue kvBarFill = new KeyValue(centerBar.fillProperty(), BAR_DEFAULT_COLOR, Interpolator.EASE_BOTH);
        KeyFrame kf = new KeyFrame(BAR_ANIMATION_DURATION, kvBarWidth, kvBarTranslateX, kvBarFill);
        barTimeline.getKeyFrames().add(kf);
        barTimeline.play();
    }

    public String getText() { return input.getText(); }

    public void setText(String text) { // Added setText for consistency and better state handling
        input.setText(text);
        if (text == null || text.isEmpty()) {
            if (!input.isFocused()) {
                blurAnim();
            }
        } else {
            if (!input.isFocused()) {
                for (int i = 0; i < labelBox.getChildren().size(); i++) {
                    Node charNode = labelBox.getChildren().get(i);
                    charNode.setTranslateY(CHAR_TRANSLATE_Y_FOCUSED);
                }
                subtleBlurAnim();
            }
        }
    }

    public void clear() {
        // input.clear(); // Original
        // if (!input.isFocused() && input.getText().isEmpty()) { // Check if already empty to avoid redundant blur
        //     blurAnim();
        // }
        setText(""); // Use setText to handle state updates correctly
    }

    public PasswordField getInputField() { return input; } // Renamed from getPasswordField for consistency
}