package ui.controls;

import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

/** 带浮动标签的密码框 */
public class WavePasswordConfirm extends StackPane {

    private final PasswordField input;
    private final HBox labelBox;
    private final Rectangle leftBar, rightBar;

    public WavePasswordConfirm(String label) {

        /* 输入框 */
        input = new PasswordField();
        input.getStyleClass().add("wave-input");
        input.setPrefWidth(200);

        /* 标签 */
        labelBox = new HBox();
        labelBox.getStyleClass().add("wave-label");
        for (int i = 0; i < label.length(); i++) {
            Text t = new Text(String.valueOf(label.charAt(i)));
            t.getStyleClass().add("wave-label-char");
            t.setStyle(String.format("-fx-transition-delay: %.2fs;", i * 0.05));
            labelBox.getChildren().add(t);
        }
        labelBox.setMouseTransparent(true);

        /* 下划线 */
        Pane barPane = new Pane();
        barPane.getStyleClass().add("wave-bar");
        barPane.setPrefWidth(200);
        barPane.setMouseTransparent(true);

        leftBar = new Rectangle(0, 2, Color.web("#5264AE"));
        rightBar = new Rectangle(0, 2, Color.web("#5264AE"));
        rightBar.setTranslateX(200);
        rightBar.setScaleX(-1);
        barPane.getChildren().addAll(leftBar, rightBar);

        /* 布局 */
        getChildren().addAll(input, barPane, labelBox);
        StackPane.setAlignment(labelBox, Pos.TOP_LEFT);
        labelBox.setTranslateX(5);
        labelBox.setTranslateY(10);
        barPane.translateYProperty().bind(input.heightProperty().subtract(1));

        /* 动画 */
        input.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) focusAnim();
            else if (input.getText().isEmpty()) blurAnim();
        });
    }

    private void focusAnim() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(200), labelBox);
        tt.setToY(-20);
        tt.play();
        leftBar.setWidth(100);
        rightBar.setWidth(100);
    }

    private void blurAnim() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(200), labelBox);
        tt.setToY(0);
        tt.play();
        leftBar.setWidth(0);
        rightBar.setWidth(0);
    }

    /* 代理 */
    public String getText() { return input.getText(); }
    public void clear() { input.clear(); }
}