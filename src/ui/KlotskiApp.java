// KlotskiApp.java
package ui;
import game.Direction;
import game.GameLogic;
import javafx.animation.*;
import javafx.application.Platform; // 导入 Platform
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task; // 导入 Task
import javafx.scene.input.KeyEvent;
import javafx.scene.text.FontPosture;
import model.Block;
import model.Board;
import model.GameState;
import ui.controls.WavePasswordConfirm;
import user.UserManager;
import game.LevelManager; // Import LevelManager
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import util.GameFileManager;
import ui.controls.WaveTextField;
import ui.controls.WavePasswordField;
import java.util.*;
import java.util.stream.Stream;

public class KlotskiApp extends Application {
    private GameLogic gameLogic;
    private GameFileManager gameFileManager;
    private UserManager userManager;
    private Stage primaryStage;
    private Block selectedBlock;
    private long startTime;
    private long elapsedTime;
    private Timeline timer;
    private int currentLevel = 1;

    private LevelManager levelManager; // Add LevelManager field
    private Button hintButton; // 添加提示按钮字段
    private ProgressIndicator solverProgress; // 添加求解器进度指示器

    // UI components
    private GridPane boardGrid;
    private Label moveCountLabel;
    private Label timeLabel;

    private void applyFadeTransition(Button label) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.5), label);
        fadeTransition.setFromValue(0.0);    // 起始透明度
        fadeTransition.setToValue(1.0);     // 结束透明度
        fadeTransition.setCycleCount(1);     // 只播放一次
        fadeTransition.setInterpolator(Interpolator.EASE_IN); // 缓动效果
        // 保证Label可见性
        label.setVisible(true);
        fadeTransition.play();
    }

    private void applyFadeTransition(Label label) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.1), label);
        fadeTransition.setFromValue(0.0);    // 起始透明度
        fadeTransition.setToValue(1.0);     // 结束透明度
        fadeTransition.setCycleCount(1);     // 只播放一次
        fadeTransition.setInterpolator(Interpolator.EASE_IN); // 缓动效果
        //动画参数定制化
//    fadeTransition.setDelay(Duration.seconds(0.5)); // 添加0.5秒延迟出现
        fadeTransition.setRate(0.8); // 调节播放速度

        // 保证Label可见性
        label.setVisible(true);
        fadeTransition.play();
    }
















    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        gameLogic = new GameLogic();
        userManager = new UserManager();
        gameFileManager = new GameFileManager(); // Initialize the GameFileManager
        levelManager = new LevelManager();

        primaryStage.setTitle("Klotski Puzzle");
        showLoginScene();
        primaryStage.show();

        // Add a shutdown hook to clean up resources when the application closes
        primaryStage.setOnCloseRequest(event -> {
            if (gameFileManager != null) {
                gameFileManager.shutdown();
            }
            if (timer != null) {
                timer.stop();
            }
        });
    }
    private StackPane rootContainer; // 用于动画的根容器
    private Scene mainScene;         // 保持场景引用

    // Login scene
    private void showLoginScene() {
//    HBox hBox = new HBox();
        VBox loginBox = new VBox(15);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(20));
        loginBox.setStyle("-fx-background-color: #f0f0f0;");

        Text title = new Text("Klotski Puzzle");
        title.setFont(Font.font("Helvetica", FontWeight.BOLD, 36));

        Button guestButton = new Button("Play as Guest");
        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");
        Button aboutGame = new Button("About Game");

        guestButton.setMinWidth(200);
        loginButton.setMinWidth(200);
        registerButton.setMinWidth(200);
        aboutGame.setMinWidth(200);

//    guestButton.setTextFill(Color.BLUE);

        guestButton.setOnAction(e -> {
            applyFadeTransition(guestButton);
            userManager.loginAsGuest();
            showMainMenu();
        });
        loginButton.setOnAction(e -> {
            applyFadeTransition(loginButton);
            showLoginForm();
        });
        registerButton.setOnAction(e -> {
            applyFadeTransition(registerButton);
            showRegisterForm();
        });
        aboutGame.setOnAction(e ->{
            applyFadeTransition(aboutGame);
            showGameIntroduction();
        });

        loginBox.getChildren().addAll(title, guestButton, loginButton, registerButton, aboutGame);

        Scene scene = new Scene(loginBox, 400, 300);
        primaryStage.setScene(scene);
    }
    private void showGameIntroduction() {
        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.BOTTOM_CENTER);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #ffffff;");
        vbox.setFillWidth(true);

        // 创建可滚动文本容器
        Label introduction = new Label();
        introduction.setWrapText(true);
        introduction.setMaxWidth(Double.MAX_VALUE);
        introduction.setStyle("-fx-font-size: 16px; -fx-line-spacing: 0.5em;");

        // 将Label包裹在ScrollPane中
        ScrollPane scrollPane = new ScrollPane(introduction);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // 禁用水平滚动条
        scrollPane.setVvalue(0); // 初始滚动位置在顶部

        // 设置固定高度确保滚动区域可见
        scrollPane.setPrefHeight(380);

        // 原始文本内容
        String fullText =
                """
                        Enter the strategic heart of ancient China, where history and logic intertwine. \
                    Chinese Klotski: Three Kingdoms Escape reinvents the classic sliding puzzle \
                    by weaving it into the epic saga of the Three Kingdoms. Guide Cao Cao, the ambitious \
                    warlord (a towering 2x2 block), to freedom through a labyrinth of allies \
                    and rivals—each piece a legend like the loyal Guanyu (1x2) or cunning Zhang Fei (1x1).
                        Every level is a battlefield of wits: maneuver intricately shaped blocks, each bearing \
                    the name and spirit of iconic heroes, to carve a path to victory. But tread carefully—one wrong move could trap Cao Cao forever.
                        Drenched in stunning classical Chinese art and haunting melodies, the game transforms each puzzle into a chapter of history. \
                    Will your strategy rival the brilliance of ancient tacticians? Sharpen your mind, honor the legends, and escape the past—one slide at a time."""; // 保持原有完整文本

        Label label = new Label("Can you master the puzzle… and rewrite history?");
        label.fontProperty().set(Font.font("System", FontPosture.ITALIC, 20));
        label.setWrapText(true);
        label.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #fd0001");
        label.setOpacity(0.0);
        label.setVisible(false);
        // 创建打字机动画效果
        final IntegerProperty i = new SimpleIntegerProperty(0);
        Timeline timeline = new Timeline();

        Button backButton = new Button("Back");
        backButton.setMinWidth(200);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        backButton.setOnAction(e -> {
            timeline.stop(); // 停止动画
            applyFadeTransition(backButton);
            showLoginScene();
        });

        Button skipButton = new Button("Skip");
        skipButton.setOnAction(e -> {
            timeline.stop();
            introduction.setText(fullText);
            scrollPane.setVvalue(1.0);
//      skipButton.setDisable(true);
            skipButton.setVisible(false);
            applyFadeTransition(label);
        });

        KeyFrame keyFrame = new KeyFrame(Duration.millis(20), event -> {
            if (i.get() >= fullText.length()) {
                timeline.stop();
                skipButton.setVisible(false);
                applyFadeTransition(label);
                return;
            }
            introduction.setText(fullText.substring(0, i.get()));
            scrollPane.setVvalue(1.0); // 自动滚动到底部
            i.set(i.get() + 1);
        });
        vbox.getChildren().add(skipButton);

        timeline.getKeyFrames().add(keyFrame);
        timeline.setCycleCount(Animation.INDEFINITE);

        // 在界面显示后启动动画
        Platform.runLater(() -> timeline.play());

        vbox.getChildren().addAll(scrollPane, spacer, label, backButton);
        Scene scene = new Scene(vbox, 600, 500); // 适当增加窗口高度
        primaryStage.setScene(scene);
    }

        // Login form
    private void showLoginForm() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #f0f0f0;");

        Text title = new Text("Login");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        grid.add(title, 0, 0, 2, 1);


        WaveTextField usernameField = new WaveTextField("Username");
        grid.add(usernameField, 0,1,2, 1);


        WavePasswordField passwordField = new WavePasswordField("Password");
        grid.add(passwordField,0,2,2,1);

        Button loginButton = new Button("Login");
        Button backButton = new Button("Back");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.getChildren().addAll(backButton, loginButton);
        grid.add(buttonBox, 1, 4);

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                showAlert("Login Error", "Username and password cannot be empty.");
                return;
            }

            if (userManager.loginUser(username, password)) {
                showMainMenu();
            } else {
                showAlert("Login Error", "Invalid username or password.");
            }
        });

        backButton.setOnAction(e -> showLoginScene());

        Scene scene = new Scene(grid, 400, 300);
        scene.getStylesheets().add(getClass().getResource("/css/wave.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    // Register form
    private void showRegisterForm() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #f0f0f0;");

        Text title = new Text("Register");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        grid.add(title, 0, 0, 2, 1);

        WaveTextField usernameField = new WaveTextField("Username");
        grid.add(usernameField, 0,1,1,1);

        WavePasswordField passwordField = new WavePasswordField("Password");
        grid.add(passwordField,0,3,1,1);

        WavePasswordConfirm confirmField = new WavePasswordConfirm("Confirm Password");
        grid.add(confirmField, 0, 4, 1, 1);

        Button registerButton = new Button("Register");
        Button backButton = new Button("Back");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.getChildren().addAll(backButton, registerButton);
        grid.add(buttonBox, 1, 6);

        registerButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String confirm = confirmField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                showAlert("Registration Error", "Username and password cannot be empty.");
                return;
            }

            if (!password.equals(confirm)) {
                showAlert("Registration Error", "Passwords do not match.");
                return;
            }

            if (userManager.registerUser(username, password)) {
                showAlert("Registration Success", "Account created successfully.");
                showLoginScene();
            } else {
                showAlert("Registration Error", "Username already exists.");
            }
        });

        backButton.setOnAction(e -> showLoginScene());

        Scene scene = new Scene(grid, 400, 300);
        scene.getStylesheets().add(getClass().getResource("/css/wave.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    // Main menu
    private void showMainMenu() {
        if (gameFileManager != null) {
            gameFileManager.stopAutoSave();
        }

        // Stop the timer
        stopTimer();
        VBox menuBox = new VBox(15);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(20));
        menuBox.setStyle("-fx-background-color: #f0f0f0;");

        Text title = new Text("Klotski Puzzle");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Text welcomeText;
        if (userManager.isGuest()) {
            welcomeText = new Text("Playing as Guest");
        } else {
            welcomeText = new Text("Welcome, " + userManager.getCurrentUser().getUsername() + "!");
        }
        welcomeText.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

        Button newGameButton = new Button("New Game");
        Button loadGameButton = new Button("Load Game");
        Button logoutButton = new Button("Logout");

        newGameButton.setMinWidth(200);
        loadGameButton.setMinWidth(200);
        logoutButton.setMinWidth(200);

        // Disable load game button for guests
        loadGameButton.setDisable(userManager.isGuest());

        // --- Level Selection UI ---
        Label levelLabel = new Label("Select Level:");
        ComboBox<String> levelComboBox = new ComboBox<>();
        // Assuming LevelManager has a method to get level names, e.g., getLevelNames()
        // For now, let's add some placeholder level names based on your LevelManager snippet
        // Get level names from LevelManager
        List<String> levelNames = new ArrayList<>();
        for (int i = 1; i <= levelManager.getLevelCount(); i++) {
            levelNames.add(levelManager.getLevel(i).getName());
        }
        levelComboBox.getItems().addAll(levelNames); // Add your actual level names
        levelComboBox.getSelectionModel().selectFirst(); // Select the first level by default
        // --- End Level Selection UI ---


        newGameButton.setOnAction(e -> {
            // Get selected level name and find the corresponding level number
            String selectedLevelName = levelComboBox.getSelectionModel().getSelectedItem();
            int levelNumber = levelComboBox.getSelectionModel().getSelectedIndex() + 1; // Use index + 1 as level number


            showGameScene(); // <-- 先调用 showGameScene 初始化 UI
            startNewGame(levelNumber); // <-- 再调用 startNewGame 进行游戏逻辑初始化和UI更新
        });

        loadGameButton.setOnAction(e -> {
            GameState state = userManager.loadGameForCurrentUser();
            if (state != null) {
                showGameScene(); // <-- 先调用 showGameScene 初始化 UI
                loadGame(state); // <-- 再调用 loadGame 加载状态并更新 UI
            } else {
                showAlert("Load Error", "No saved game found.");
            }
        });

        logoutButton.setOnAction(e -> {
            userManager.logout();
            showLoginScene();
        });

        // Add level selection controls to the menu box
        menuBox.getChildren().addAll(title, welcomeText, levelLabel, levelComboBox, newGameButton, loadGameButton, logoutButton); // Add level controls

        Scene scene = new Scene(menuBox, 400, 400); // Adjust scene size if needed
        scene.getStylesheets().add(getClass().getResource("/css/wave.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    // Game scene
// KlotskiApp.java ── 完整替换原来的 showGameScene()
    private void showGameScene() {
        /* ---------- 根布局 ---------- */
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #f0f0f0;");

        /* ---------- 顶部信息栏 ---------- */
        HBox topPanel = new HBox(20);
        topPanel.setPadding(new Insets(10));
        topPanel.setAlignment(Pos.CENTER);

        moveCountLabel = new Label("Moves: 0");
        timeLabel = new Label("Time: 00:00");
        topPanel.getChildren().addAll(moveCountLabel, timeLabel);
        root.setTop(topPanel);

        /* ---------- 棋盘 ---------- */
        boardGrid = new GridPane();
        boardGrid.setAlignment(Pos.CENTER);
        boardGrid.setHgap(2);
        boardGrid.setVgap(2);
        boardGrid.setStyle("-fx-background-color: #333333;");

        StackPane boardPane = new StackPane(boardGrid);
        root.setCenter(boardPane);

        /* ---------- 控制按钮 ---------- */
        HBox controlPanel = new HBox(10);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setAlignment(Pos.CENTER);

        Button upButton    = new Button("Up");
        Button downButton  = new Button("Down");
        Button leftButton  = new Button("Left");
        Button rightButton = new Button("Right");
        Button undoButton  = new Button("Undo");
        Button saveButton  = new Button("Save");
        Button menuButton  = new Button("Menu");
        hintButton = new Button("Hint"); // 初始化提示按钮
        solverProgress = new ProgressIndicator(-1); // 初始化进度指示器
        solverProgress.setVisible(false); // 默认隐藏

        // 禁用游客存档
        saveButton.setDisable(userManager.isGuest());

        /* --- 按钮事件 --- */
        upButton.setOnAction(e    -> moveSelected(Direction.UP));
        downButton.setOnAction(e  -> moveSelected(Direction.DOWN));
        leftButton.setOnAction(e  -> moveSelected(Direction.LEFT));
        rightButton.setOnAction(e -> moveSelected(Direction.RIGHT));
        undoButton.setOnAction(e  -> undo());
        saveButton.setOnAction(e  -> saveGame());
        menuButton.setOnAction(e  -> {
            stopTimer();
            showMainMenu();
        });
        hintButton.setOnAction(e -> getAndApplyHint()); // 添加提示按钮事件处理

        controlPanel.getChildren().addAll(
                upButton, downButton, leftButton, rightButton,
                undoButton, saveButton, hintButton, menuButton, solverProgress); // 添加提示按钮和进度指示器
        root.setBottom(controlPanel);

        /* ---------- 防止按钮抢键盘 ---------- */
        for (Button b : List.of(upButton, downButton, leftButton, rightButton,
                undoButton, saveButton, hintButton, menuButton)) { // 将提示按钮添加到列表中
            b.setFocusTraversable(false);
        }

        /* ---------- 创建场景并注册键盘事件 ---------- */
        Scene scene = new Scene(root, 600, 700);
        scene.getStylesheets().add(getClass().getResource("/css/wave.css").toExternalForm());
        // 事件过滤器：始终能收到键盘（不受焦点限制）
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (gameLogic.isGameWon()) return; // 游戏胜利后禁用键盘控制

            switch (e.getCode()) {
                case UP    -> moveSelected(Direction.UP);
                case DOWN  -> moveSelected(Direction.DOWN);
                case LEFT  -> moveSelected(Direction.LEFT);
                case RIGHT -> moveSelected(Direction.RIGHT);
                case Z     -> { if (e.isControlDown()) undo(); }
                case S     -> { if (e.isControlDown()) saveGame(); }
                case H     -> { if (e.isControlDown()) getAndApplyHint(); } // Ctrl+H 触发提示
            }
        });

        // ---------- 设置场景并抢焦点 ----------
        primaryStage.setScene(scene);

        root.requestFocus();                              // 首帧立即可用方向键
        root.setOnMouseClicked(ev -> root.requestFocus()); // 鼠标点击后再夺回焦点

    }


    // Victory scene
    private void showVictoryScene() {
        stopTimer();

        VBox victoryBox = new VBox(15);
        victoryBox.setAlignment(Pos.CENTER);
        victoryBox.setPadding(new Insets(20));
        victoryBox.setStyle("-fx-background-color: #f0f0f0;");

        Text title = new Text("Victory!");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Text moveText = new Text("Moves: " + gameLogic.getBoard().getMoveCount());
        moveText.setFont(Font.font("Arial", FontWeight.NORMAL, 18));

        Text timeText = new Text("Time: " + formatTime(elapsedTime));
        timeText.setFont(Font.font("Arial", FontWeight.NORMAL, 18));

        Button newGameButton = new Button("New Game");
        Button menuButton = new Button("Menu");

        newGameButton.setMinWidth(200);
        menuButton.setMinWidth(200);

        newGameButton.setOnAction(e -> {
            // When starting a new game from victory, ideally use the selected level from main menu
            // For now, let's start with the last played level or level 1
            startNewGame(currentLevel); // Start a new game with the current level
            showGameScene();
        });

        menuButton.setOnAction(e -> showMainMenu());

        victoryBox.getChildren().addAll(title, moveText, timeText, newGameButton, menuButton);

        Scene scene = new Scene(victoryBox, 400, 300);
        scene.getStylesheets().add(getClass().getResource("/css/wave.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    // Update the game board display
    private void updateBoard() {
        // Ensure boardGrid is not null before accessing its children
        if (boardGrid == null) {
            System.err.println("Error: boardGrid is null in updateBoard()");
            return; // Or throw an exception
        }
        boardGrid.getChildren().clear();

        Board board = gameLogic.getBoard();

        // Create board cells (5x4)
        for (int row = 0; row < board.getRows(); row++) {
            for (int col = 0; col < board.getCols(); col++) {
                Rectangle cell = new Rectangle(70, 70);
                cell.setFill(Color.LIGHTGRAY);
                boardGrid.add(cell, col, row);
            }
        }

        // Add blocks
        for (Block block : board.getBlocks()) {
            Rectangle rect = new Rectangle(
                    70 * block.getWidth() - 4,
                    70 * block.getHeight() - 4
            );

            // Convert color string back to Color for UI
            // Ensure model.Block has getColorString() and javafx.scene.paint.Color is imported
            rect.setFill(javafx.scene.paint.Color.valueOf(block.getColorString()));
            rect.setStroke(javafx.scene.paint.Color.BLACK);
            rect.setStrokeWidth(2);

            // Make blocks interactive
            rect.setOnMouseClicked(e -> {
                // 游戏胜利后禁用块选择
                if (gameLogic.isGameWon()) return;
                selectedBlock = block;
                updateSelectedBlockHighlight();
            });

            // Add block to grid
            GridPane.setColumnIndex(rect, block.getX());
            GridPane.setRowIndex(rect, block.getY());
            GridPane.setColumnSpan(rect, block.getWidth());
            GridPane.setRowSpan(rect, block.getHeight());

            boardGrid.getChildren().add(rect);
        }

        // Update move count label
        moveCountLabel.setText("Moves: " + board.getMoveCount());

        // Highlight the selected block
        updateSelectedBlockHighlight();
    }

    // Highlight the selected block
    private void updateSelectedBlockHighlight() {
        // Ensure boardGrid is not null
        if (boardGrid == null) {
            // This case should ideally not happen if updateBoard is called correctly after showGameScene
            return;
        }

        // Remove highlight from previously selected block
        for (int i = 0; i < boardGrid.getChildren().size(); i++) {
            if (boardGrid.getChildren().get(i) instanceof Rectangle) {
                Rectangle rect = (Rectangle) boardGrid.getChildren().get(i);
                // Check if this rectangle represents a block (not an empty cell)
                // Assuming cells are exactly 70x70, blocks are larger or different dimensions
                if (rect.getWidth() > 0 && rect.getHeight() > 0 && (rect.getWidth() != 70 || rect.getHeight() != 70)) {
                    // Reset stroke
                    rect.setStroke(Color.BLACK);
                    rect.setStrokeWidth(2);
                }
            }
        }


        // Add highlight to the currently selected block
        if (selectedBlock != null) {
            for (int i = 0; i < boardGrid.getChildren().size(); i++) {
                if (boardGrid.getChildren().get(i) instanceof Rectangle) {
                    Rectangle rect = (Rectangle) boardGrid.getChildren().get(i);

                    Integer col = GridPane.getColumnIndex(rect);
                    Integer row = GridPane.getRowIndex(rect);

                    if (col != null && row != null) {
                        // Find the block at this rectangle's position and dimensions
                        // Need to iterate through blocks to find the one matching this rectangle
                        // This might be inefficient, consider storing block reference in Rectangle's user data
                        for (Block block : gameLogic.getBoard().getBlocks()) {
                            if (block.getX() == col && block.getY() == row &&
                                    block.getWidth() == GridPane.getColumnSpan(rect) &&
                                    block.getHeight() == GridPane.getRowSpan(rect)) {

                                if (block == selectedBlock) {
                                    rect.setStroke(Color.WHITE);
                                    rect.setStrokeWidth(3);
                                    return; // Found the selected block, exit
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Move the selected block
    private void moveSelected(Direction direction) {
        // 游戏胜利后禁用移动
        if (gameLogic != null && gameLogic.isGameWon()) return;


        if (selectedBlock != null && gameLogic != null) { // Add null check for gameLogic
            if (gameLogic.moveBlock(selectedBlock, direction)) {
                updateBoard();

                if (gameLogic.isGameWon()) {
                    showVictoryScene();
                }
                // Auto-save only if user is logged in
                if (!userManager.isGuest()) {
                    GameState gameState = new GameState(
                            gameLogic.getBoard(),
                            userManager.getCurrentUser().getUsername(),
                            currentLevel,
                            new ArrayDeque<>(gameLogic.getMoveHistory()), // Copy history
                            gameLogic.isGameWon()
                    );
                    gameState.setTimeElapsed(System.currentTimeMillis() - startTime);  // 更新时间

                    gameFileManager.saveGame(userManager.getCurrentUser().getUsername(), gameState);  // 手动保存
                }

                // Update move count display after a successful move
                moveCountLabel.setText("Moves: " + gameLogic.getBoard().getMoveCount());
            }
        } else if (gameLogic != null && gameLogic.getBoard() != null) { // Add null checks
            // If no block is selected, try to find one at the center
            // This part might be less intuitive, consider removing or improving block selection
            /*
            Block centerBlock = gameLogic.getBoard().getBlockAt(
                    gameLogic.getBoard().getCols() / 2,
                    gameLogic.getBoard().getRows() / 2
            );

            if (centerBlock != null) {
                selectedBlock = centerBlock;
                updateSelectedBlockHighlight();
            }
            */
        }
    }

    // Undo last move
    private void undo() {
        // 游戏胜利后禁用撤销
        if (gameLogic != null && gameLogic.isGameWon()) return;

        if (gameLogic != null && gameLogic.undoMove()) { // Add null check for gameLogic
            updateBoard();
            // Update move count display after undo
            moveCountLabel.setText("Moves: " + gameLogic.getBoard().getMoveCount());

            // Auto-save after undo if user is logged in
            if (!userManager.isGuest()) {
                GameState gameState = new GameState(
                        gameLogic.getBoard(),
                        userManager.getCurrentUser().getUsername(),
                        currentLevel,
                        new ArrayDeque<>(gameLogic.getMoveHistory()), // Copy history
                        gameLogic.isGameWon() // isGameWon getter handles the check
                );
                gameState.setTimeElapsed(System.currentTimeMillis() - startTime); // Keep elapsed time
                gameFileManager.saveGame(userManager.getCurrentUser().getUsername(), gameState);
            }
        } else {
            showAlert("Undo Error", "No moves to undo."); // Add feedback for no undo
        }
    }

    // Start a new game with a specific level - Modified to accept levelNumber
    private void startNewGame(int levelNumber) {
        model.Level level = levelManager.getLevel(levelNumber);

        if (level != null) {
            /* ---------- 创建棋盘并应用布局 ---------- */
            Board newBoard = new Board();
            level.applyToBoard(newBoard);

            /* ---------- 设置到 gameLogic ---------- */
            if (gameLogic == null) {
                gameLogic = new GameLogic();
            }
            gameLogic.setBoard(newBoard);
            gameLogic.setIsGameWon(false);


            /* ---------- 重新初始化历史栈 ---------- */
            Deque<Board> history = new ArrayDeque<>(); // 创建新的历史栈
            history.push(newBoard.copy());    // ★ 压入初始状态的副本
            gameLogic.setMoveHistory(history); // 设置新的历史栈

            /* ---------- 其余 UI/计时器处理 ---------- */
            currentLevel = levelNumber;
            selectedBlock = null; // 清除选中的块
            startTime = System.currentTimeMillis();
            elapsedTime = 0;

            updateBoard();
            moveCountLabel.setText("Moves: " + gameLogic.getBoard().getMoveCount());
            timeLabel.setText("Time: " + formatTime(elapsedTime));
            startTimer();

            // Only start auto-save if the user is not a guest
            if (!userManager.isGuest()) {
                // Use a supplier to always get the current game state
                gameFileManager.startAutoSave(userManager.getCurrentUser(), () -> {
                    // 在 Supplier 中创建一个新的 GameState 副本，包含当前状态
                    return new GameState(
                            gameLogic.getBoard().copy(), // 确保保存的是当前 Board 的副本
                            userManager.getCurrentUser().getUsername(),
                            currentLevel,
                            new ArrayDeque<>(gameLogic.getMoveHistory()), // 复制移动历史
                            gameLogic.isGameWon()
                    );
                });
            }
        } else {
            showAlert("Error", "Could not load level " + levelNumber);
        }
    }


    // Load a saved game
    private void loadGame(GameState state) {
        // Restore the game state using the loaded GameState object
        // Do NOT create a new GameLogic() here if you want to update the current one.
        // Assuming gameLogic field in KlotskiApp is the instance to update:

        // Ensure gameLogic is not null before setting its state
        if (gameLogic == null) {
            gameLogic = new GameLogic(); // Should be initialized in start, but as a fallback
        }

        // 使用加载的状态更新 gameLogic
        gameLogic.setBoard(state.getBoard()); // 设置棋盘状态
        // Ensure GameLogic has setMoveHistory and setIsGameWon methods
        if (state.getMoveHistory() != null) {
            // 创建一个新的 Deque 并复制加载的历史
            gameLogic.setMoveHistory(new ArrayDeque<>(state.getMoveHistory())); // 设置移动历史
        } else {
            // If move history was not saved, initialize a new one with the current board state
            Deque<Board> history = new ArrayDeque<>();
            history.push(gameLogic.getBoard().copy());
            gameLogic.setMoveHistory(history);
        }
        gameLogic.setIsGameWon(state.isGameWon());     // 设置游戏胜利状态


        // Restore elapsed time
        elapsedTime = state.getTimeElapsed();
        startTime = System.currentTimeMillis() - elapsedTime; // Calculate startTime based on loaded elapsed time

        // Restore level
        currentLevel = state.getCurrentLevel(); // Assuming level is saved in GameState

        // Clear selection as the board state has changed
        selectedBlock = null;

        // Update UI based on the loaded state
        updateBoard(); // This will redraw the board based on gameLogic.getBoard()
        moveCountLabel.setText("Moves: " + gameLogic.getBoard().getMoveCount()); // Update move count display
        timeLabel.setText("Time: " + formatTime(elapsedTime)); // Update time display

        // Restart timer from the loaded elapsed time
        startTimer();
        // Only start auto-save if user is logged in
        if (!userManager.isGuest()) {
            gameFileManager.startAutoSave(userManager.getCurrentUser(), () -> {
                // 在 Supplier 中创建一个新的 GameState 副本，包含当前状态
                return new GameState(
                        gameLogic.getBoard().copy(), // 确保保存的是当前 Board 的副本
                        userManager.getCurrentUser().getUsername(),
                        currentLevel,
                        new ArrayDeque<>(gameLogic.getMoveHistory()), // 复制移动历史
                        gameLogic.isGameWon()
                );
            });
        }
    }

    // Save the current game
    private void saveGame() {
        if (userManager.isGuest()) {
            showAlert("Save Error", "Guests cannot save games.");
            return;
        }

        // Ensure gameLogic is not null before saving its state
        if (gameLogic == null || gameLogic.getBoard() == null) {
            showAlert("Save Error", "No game to save.");
            return;
        }

        elapsedTime = System.currentTimeMillis() - startTime;
        // Create GameState with all necessary state information
        // Ensure GameState constructor accepts moveHistory and isGameWon
        GameState state = new GameState(
                gameLogic.getBoard().copy(), // 保存当前 Board 的副本
                userManager.getCurrentUser().getUsername(),
                currentLevel, // Save current level
                new ArrayDeque<>(gameLogic.getMoveHistory()), // 包含移动历史的副本
                gameLogic.isGameWon() // 包含游戏胜利状态
        );
        state.setTimeElapsed(elapsedTime); // 设置已经过去的时间

        if (gameFileManager.saveGame(userManager.getCurrentUser().getUsername(), state)) {
            showAlert("Save Success", "Game saved successfully.");
        } else {
            showAlert("Save Error", "Failed to save game.");
        }
    }

    /**
     * 获取并应用提示（下一步）
     */
    private void getAndApplyHint() {
        // 如果游戏已胜利或求解器正在运行，则不执行
        if (gameLogic.isGameWon() || solverProgress.isVisible()) {
            return;
        }

        // 显示进度指示器并禁用提示按钮
        solverProgress.setVisible(true);
        hintButton.setDisable(true);

        // 在单独的线程中运行求解器，避免阻塞 UI 线程
        Task<Board> solverTask = new Task<Board>() {
            @Override
            protected Board call() throws Exception {
                // 调用 GameLogic 的 getHint 方法，它内部会使用 Solver
                return gameLogic.getHint();
            }
        };

        // 任务完成时的处理
        solverTask.setOnSucceeded(event -> {
            // 隐藏进度指示器并启用提示按钮
            solverProgress.setVisible(false);
            hintButton.setDisable(false);

            Board nextBoard = solverTask.getValue(); // 获取求解结果 (下一步棋盘)

            if (nextBoard != null) {
                // 将游戏状态更新到提示的下一步
                // 这里不直接使用 moveBlock 方法，因为我们是直接设置到下一个状态
                // 需要确保将提示的状态正确加入历史，并更新 moveCount
                gameLogic.setBoard(nextBoard.copy()); // 设置为下一步棋盘的副本
                // 增加移动计数（提示也算一步）
                gameLogic.getBoard().incrementMoveCount();
                // 保存当前状态到历史（为了支持从提示后的撤销）
                Deque<Board> currentHistory = gameLogic.getMoveHistory();
                currentHistory.push(gameLogic.getBoard().copy()); // 压入新的状态副本
                gameLogic.setMoveHistory(currentHistory); // 更新 gameLogic 的历史引用（如果必要）

                updateBoard(); // 更新 UI

                // 检查提示是否导致胜利
                if (gameLogic.isGameWon()) {
                    showVictoryScene();
                }

                // 提示后也进行自动保存
                if (!userManager.isGuest()) {
                    GameState gameState = new GameState(
                            gameLogic.getBoard().copy(), // 保存当前 Board 的副本
                            userManager.getCurrentUser().getUsername(),
                            currentLevel,
                            new ArrayDeque<>(gameLogic.getMoveHistory()), // 复制移动历史
                            gameLogic.isGameWon()
                    );
                    gameState.setTimeElapsed(System.currentTimeMillis() - startTime);
                    gameFileManager.saveGame(userManager.getCurrentUser().getUsername(), gameState);
                }

            } else {
                // 如果 getHint 返回 null，表示无解或已是最终状态
                showAlert("提示", "当前状态无解或已是最终状态。");
            }
        });

        // 任务失败时的处理
        solverTask.setOnFailed(event -> {
            // 隐藏进度指示器并启用提示按钮
            solverProgress.setVisible(false);
            hintButton.setDisable(false);
            showAlert("提示", "求解失败：" + event.getSource().getException().getMessage());
            event.getSource().getException().printStackTrace(); // 打印异常信息
        });

        // 启动任务
        new Thread(solverTask).start();
    }


    @Override
    public void stop() {
        if (gameFileManager != null) {
            gameFileManager.shutdown();
        }
        if (timer != null) {
            timer.stop();
        }
    }
    private void startTimer() {
        stopTimer(); // Stop any existing timer first
        startTime = System.currentTimeMillis() - elapsedTime;

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            elapsedTime = System.currentTimeMillis() - startTime;
            timeLabel.setText("Time: " + formatTime(elapsedTime));
        }));

        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    // Stop the game timer
    private void stopTimer() {
        if (timer != null) {
            timer.stop();
            // elapsedTime is already updated in the timer's keyframe or when stopTimer is called elsewhere
        }
    }

    // Format time as mm:ss
    private String formatTime(long millis) {
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) (millis / 60000);
        return String.format("%02d:%02d", minutes, seconds);
    }

    // Show an alert dialog
    private void showAlert(String title, String message) {
        // 确保在 JavaFX Application 线程中显示 Alert
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // Main method
    public static void main(String[] args) {
        launch(args);
    }
}