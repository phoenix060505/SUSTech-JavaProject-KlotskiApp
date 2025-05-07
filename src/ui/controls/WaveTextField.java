package ui.controls;

import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * 自定义文本框，模仿 Vue wave‑group 动画输入框
 */
public class WaveTextField extends StackPane {

    private final TextField input;
    private final HBox       labelBox;
    private final Rectangle  leftBar;
    private final Rectangle  rightBar;

    public WaveTextField(String labelText) {

        /* -------- 输入框 -------- */
        input = new TextField();
        input.getStyleClass().add("wave-input");
        input.setPrefWidth(200);

        /* -------- 标签（逐字延迟动画） -------- */
        labelBox = new HBox();
        labelBox.getStyleClass().add("wave-label");
        labelBox.setMouseTransparent(true);
        for (int i = 0; i < labelText.length(); i++) {
            Text letter = new Text(String.valueOf(labelText.charAt(i)));
            letter.getStyleClass().add("wave-label-char");
            letter.setStyle(String.format("-fx-transition-delay: %.2fs;", i * 0.05));
            labelBox.getChildren().add(letter);
        }

        /* -------- 下划线动画条 -------- */
        Pane barPane = new Pane();
        barPane.getStyleClass().add("wave-bar");
        barPane.setMouseTransparent(true);
        barPane.setPrefWidth(200);

        leftBar  = new Rectangle(0, 2, Color.web("#5264AE"));
        rightBar = new Rectangle(0, 2, Color.web("#5264AE"));
        rightBar.setTranslateX(200);   // 右侧从右向左伸展
        rightBar.setScaleX(-1);
        barPane.getChildren().addAll(leftBar, rightBar);

        /* -------- 布局 -------- */
        getChildren().addAll(input, barPane, labelBox);
        StackPane.setAlignment(labelBox, Pos.TOP_LEFT);
        labelBox.setTranslateX(5);
        labelBox.setTranslateY(10);
        barPane.translateYProperty().bind(
                Bindings.createDoubleBinding(() -> input.getHeight() - 1, input.heightProperty()));

        /* -------- 交互动画 -------- */
        input.focusedProperty().addListener((obs, oldV, foc) -> {
            if (foc) focusAnim();          // 获得焦点
            else if (input.getText().isEmpty()) blurAnim(); // 失焦且无内容
        });
    }

    /* ========== 动画 ========== */
    private void focusAnim() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(200), labelBox);
        tt.setToY(-20);  // 标签上移
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

    /* ========== 常用代理 ========== */
    public String getText()           { return input.getText(); }
    public void   setText(String txt) { input.setText(txt);     }
    public TextField getTextField()   { return input;           }
}
