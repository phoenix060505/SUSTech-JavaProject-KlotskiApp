package ui.controls;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Node; // 需要导入 Node
import javafx.scene.control.TextField; // 类型为 TextField
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * 自定义文本框，模仿 Vue wave‑group 动画输入框 (标签字符逐个动画)
 */
public class WaveTextField extends StackPane {

    private final TextField input; // 类型为 TextField
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

    private static final Color BAR_FOCUSED_COLOR = Color.web("#E87A00"); // 例如: 暗橙色
    private static final Color BAR_DEFAULT_COLOR = Color.web("#8B4513"); // 例如: 鞍褐色


    public WaveTextField(String labelText) {
        this(labelText, 200.0);
    }

    public WaveTextField(String labelText, double prefWidth) {
        this.controlPrefWidth = prefWidth;

        input = new TextField(); // 使用 TextField
        input.getStyleClass().add("wave-input");
        input.setPrefWidth(this.controlPrefWidth);

        labelBox = new HBox();
        labelBox.getStyleClass().add("wave-label");
        for (int i = 0; i < labelText.length(); i++) {
            Text letter = new Text(String.valueOf(labelText.charAt(i)));
            letter.getStyleClass().add("wave-label-char");
            labelBox.getChildren().add(letter);
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

        input.focusedProperty().addListener((obs, oldV, foc) -> {
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

    public void setText(String txt) {
        input.setText(txt);
        if (txt == null || txt.isEmpty()) {
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
    public TextField getTextField() { return input; }
    public void clear() {
        setText("");
    }
}