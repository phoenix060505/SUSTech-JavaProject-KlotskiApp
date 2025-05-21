// KlotskiApp.java
package ui;
import game.*;
import game.AboutGame;
import javafx.animation.*;
import javafx.application.Platform; // 导入 Platform
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task; // 导入 Task
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.FontPosture;
import model.Block;
import model.Board;
import model.GameState;
import model.Level;
import ui.controls.WavePasswordConfirm;
import user.UserManager;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import javafx.animation.TranslateTransition;

import static game.AboutGame.applyFadeTransition;
public class KlotskiApp extends Application {
  // Game components
  private Image LoginBackground = new Image("LoginBackground.png");
  private GameLogic gameLogic;
  private GameFileManager gameFileManager;
  private UserManager userManager;
  private Stage primaryStage;
  private Block selectedBlock;
  private long startTime;
  private long elapsedTime;
  private Timeline timer;
  private int currentLevel = 1;
  //Restart
  private Button restartButton; // 添加重启按钮字段
  //Hint
  private LevelManager levelManager; // Add LevelManager field
  private Button hintButton; // 添加提示按钮字段
  private ProgressIndicator solverProgress; // 添加求解器进度指示器
  // UI components
  private GridPane boardGrid;
  private Label moveCountLabel;
  private Label timeLabel;
  //Auto-solve
  private AutoSolve autoSolver;
  private Button autoSolveButton;
  private boolean isAutoSolving = false;
  private static final double AUTO_SOLVE_STEP_DELAY_SECONDS = 0.001;
  //AboutGame
  private final AboutGame aboutGame = new AboutGame();
  private StackPane rootContainer;
  private Scene mainScene;
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

  // Login scene
  private void showLoginScene() {
    //HBox hBox = new HBox();
    VBox loginBox = new VBox(15);
    loginBox.setAlignment(Pos.CENTER);
    loginBox.setPadding(new Insets(20));
    // 设置背景图片（关键修改）
    loginBox.setStyle("-fx-background-image: url('" + LoginBackground.getUrl() + "');" +
        "-fx-background-size: cover;" +
        "-fx-background-position: center;");
    // 创建半透明遮罩层保证文字可读性

    StackPane overlay = new StackPane();
    overlay.setStyle("-fx-background-color: rgba(255,255,255,0.7);");
    overlay.setMaxSize(400, 300);
    // 原有控件容器改为嵌套结构
    VBox contentBox = new VBox(15);
    contentBox.setAlignment(Pos.CENTER);

    Text title = new Text("Klotski Puzzle");
    title.setFont(Font.font("System", FontWeight.BOLD, 36));
    LinearGradient gradient = new LinearGradient(//颜色渐变
        0,    // startX
        0,    // startY
        1,    // endX (1表示水平方向100%)
        0,    // endY
        true, // 启用比例坐标
        CycleMethod.REFLECT, // 渐变循环模式
        new Stop(0.0, Color.DEEPPINK),
        new Stop(0.2, Color.BLUE),
        new Stop(0.4, Color.GREEN),
        new Stop(0.6, Color.ORANGE),
        new Stop(0.8, Color.RED),
        new Stop(1.0, Color.PURPLE)
    );//彩色标题

    title.setFill(gradient);

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

    ImageView imageView = new ImageView(LoginBackground);
    imageView.setFitWidth(100);
    imageView.setFitHeight(100);
    imageView.setPreserveRatio(true);

    loginBox.getChildren().addAll(title, guestButton, loginButton, registerButton, aboutGame);
    overlay.getChildren().add(contentBox);
    loginBox.getChildren().add(overlay);

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

    Button label = new Button("Can you master the puzzle… and rewrite history?");
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
    List<String> levelNames = new ArrayList<>();
    for (int i = 1; i <= levelManager.getLevelCount(); i++) {
      levelNames.add(levelManager.getLevel(i).getName());
    }
    levelComboBox.getItems().addAll(levelNames); // Add your actual level names
    levelComboBox.getSelectionModel().selectFirst(); // Select the first level by default



    newGameButton.setOnAction(e -> {
      // Get selected level name and find the corresponding level number
      String selectedLevelName = levelComboBox.getSelectionModel().getSelectedItem();
      int levelNumber = levelComboBox.getSelectionModel().getSelectedIndex() + 1; // Use index + 1 as level number


      showGameScene(); // <-- 先调用 showGameScene 初始化 UI
      startNewGame(levelNumber); // <-- 再调用 startNewGame 进行游戏逻辑初始化和UI更新
    });
    loadGameButton.setOnAction(e -> {
      if (userManager.isGuest()) {
        showAlert("Load Error", "Guests cannot load games. Please log in or register.");
        return;
      }
      if (userManager.getCurrentUser() == null) {
        showAlert("Load Error", "No user logged in.");
        return;
      }
      if (gameFileManager == null) {
        showAlert("Load Error", "File manager not available.");
        return;
      }

      GameState state = gameFileManager.loadGame(userManager.getCurrentUser().getUsername());

      if (state != null) {
        System.out.println("[MAIN_MENU_LOAD_ACTION] Loaded state.isGameWon(): " + state.isGameWon()); // DEBUG
        if (state.isGameWon()) {
          showAlert("Load Error", "You are already winning the game.");
          // Optionally, you might want to stop any ongoing game logic or timers here if any were active
          // stopTimer(); // Good practice if a timer could be running from another context
        } else {
          showGameScene();
          loadGame(state);
        }
      } else {
        showAlert("Load Error", "No saved game found or failed to load.");
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
    hintButton = new Button("Hint");// 初始化提示按钮
    autoSolveButton = new Button("Auto-Solve");//初始化自动求解按钮
    restartButton = new Button("Restart");
    solverProgress = new ProgressIndicator(-1); // 初始化进度指示器
    solverProgress.setVisible(false); // 默认隐藏
    saveButton.setDisable(userManager.isGuest());

    autoSolver = new AutoSolve(
        gameLogic,
        autoSolveButton,
        hintButton,
        solverProgress,
        this::updateBoard,
        this::showVictoryScene,
        this::showAlert,
        () -> elapsedTime
    );
    // 禁用游客存档

    /* --- 按钮事件 --- */
    upButton.setOnAction(e    -> moveSelected(Direction.UP));
    downButton.setOnAction(e  -> moveSelected(Direction.DOWN));
    leftButton.setOnAction(e  -> moveSelected(Direction.LEFT));
    rightButton.setOnAction(e -> moveSelected(Direction.RIGHT));
    hintButton.setOnAction(e -> getAndApplyHint()); // 添加提示按钮事件处理
    autoSolveButton.setOnAction(e -> {
          autoSolver.toggleAutoSolve();
          if (moveCountLabel != null && gameLogic.getBoard() != null) {
            moveCountLabel.setText("Moves: " + gameLogic.getBoard().getMoveCount());
          }
        }

    ); // Assign action
    undoButton.setOnAction(e  -> undo());
    saveButton.setOnAction(e  -> saveGame());
    restartButton.setOnAction(e -> {
      if (gameLogic != null) {
        gameLogic.restartGame();
        updateBoard();
        if (moveCountLabel != null && gameLogic.getBoard() != null) {
          moveCountLabel.setText("Moves: " + gameLogic.getBoard().getMoveCount());
        }
      }
    });
    menuButton.setOnAction(e  -> {
      if (autoSolver != null && autoSolver.isAutoSolving()) {
        autoSolver.toggleAutoSolve(); // 停止自动求解
      }
      stopTimer();
      showMainMenu();
    });

    controlPanel.getChildren().addAll(
        upButton, downButton, leftButton, rightButton,
        undoButton, saveButton, hintButton,autoSolveButton, restartButton ,menuButton, solverProgress); // 添加提示按钮和进度指示器
    root.setBottom(controlPanel);

    /* ---------- 防止按钮抢键盘 ---------- */
    for (Button b : List.of(upButton, downButton, leftButton, rightButton,
        undoButton, saveButton, hintButton,autoSolveButton,restartButton, menuButton)) { // 将提示按钮添加到列表中
      b.setFocusTraversable(false);
    }
    /* ---------- 创建场景并注册键盘事件 ---------- */
    Scene scene = new Scene(root, 700, 600);
    scene.getStylesheets().add(getClass().getResource("/css/wave.css").toExternalForm());
    // 事件过滤器：始终能收到键盘（不受焦点限制）
    scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
      if (gameLogic.isGameWon()|| isAutoSolving) return; // 游戏胜利后禁用键盘控制
      switch (e.getCode()) {
        case UP    -> moveSelected(Direction.UP);
        case DOWN  -> moveSelected(Direction.DOWN);
        case LEFT  -> moveSelected(Direction.LEFT);
        case RIGHT -> moveSelected(Direction.RIGHT);
        case Z     -> { if (e.isControlDown()) undo(); }
        case S     -> { if (e.isControlDown()) saveGame(); }
        case H     -> { if (e.isControlDown()) getAndApplyHint(); } // Ctrl+H 触发提示
        case A     -> { if (e.isControlDown()) autoSolver.toggleAutoSolve(); } // Ctrl+A 触发自动求解
        case R     -> { if (e.isControlDown()) restartButton.fire(); } // Ctrl+R 触发重启
        case ESCAPE -> showMainMenu();
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
      showGameScene();
      startNewGame(currentLevel);

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
    for (Block block : board.getBlocks()) {
      Rectangle rect = new Rectangle(
          73 * block.getWidth(), // 物块矩形的宽度
          73 * block.getHeight()     // 物块矩形的高度
      );
      rect.setFill(Color.valueOf(block.getColorString()));
      rect.setStroke(Color.BLACK); // 默认边框颜色
      rect.setStrokeWidth(2);                         // 默认边框宽度
      // 这是关键步骤，用于之后从Rectangle中识别出具体的Block对象
      rect.setUserData(block);
      rect.setOnMouseClicked(e -> {
        // 游戏胜利后或自动演示时禁用块选择
        if (gameLogic.isGameWon() || isAutoSolving) {
          return;
        }

        // 从被点击的 Rectangle 的 userData 中获取其对应的 Block 模型实例
        Block clickedModelBlock = (Block) ((Rectangle) e.getSource()).getUserData();

        // 更新 selectedBlock 的引用
        // 如果用户再次点击当前已经选中的物块，则取消选中 (实现切换选中/取消选中效果)
        if (this.selectedBlock == clickedModelBlock) {
          this.selectedBlock = null; // 取消选中
        } else {
          this.selectedBlock = clickedModelBlock; // 选中新的物块
        }

        // 调用方法来更新所有物块的高亮状态
        updateSelectedBlockHighlight();
      });

      // 将物块矩形添加到 GridPane 中的正确位置
      GridPane.setColumnIndex(rect, block.getX());
      GridPane.setRowIndex(rect, block.getY());
      GridPane.setColumnSpan(rect, block.getWidth());
      GridPane.setRowSpan(rect, block.getHeight());

      boardGrid.getChildren().add(rect);
    }
    updateSelectedBlockHighlight();
    if (moveCountLabel != null && gameLogic != null && gameLogic.getBoard() != null) {
      moveCountLabel.setText("Moves: " + gameLogic.getBoard().getMoveCount());
    }
  }
  private void updateSelectedBlockHighlight() {
    for (Node node : boardGrid.getChildren()) {
      if (node instanceof Rectangle) {
        Rectangle rectNode = (Rectangle) node;
        // 从 Rectangle 的 userData 中获取其对应的 Block 模型
        Block modelOfThisRect = (Block) rectNode.getUserData();

        if (modelOfThisRect != null && modelOfThisRect == this.selectedBlock) {

          DropShadow borderGlow = new DropShadow();
          borderGlow.setColor(Color.GOLD); // 高亮颜色
          borderGlow.setRadius(12);      // 光晕半径
          borderGlow.setSpread(0.8);     // 光晕扩散度
          rectNode.setEffect(borderGlow);

        } else {
          rectNode.setEffect(null); // 移除所有效果
          rectNode.setStroke(Color.BLACK); // 恢复默认边框颜色
          rectNode.setStrokeWidth(2);      // 恢复默认边框宽度
        }
      }
    }


















//        // Add blocks
//        for (Block block : board.getBlocks()) {
//            Rectangle rect = new Rectangle(
//                    70 * block.getWidth()+2 ,
//                    70 * block.getHeight()
//            );
//            rect.setFill(javafx.scene.paint.Color.valueOf(block.getColorString()));
//            rect.setStroke(javafx.scene.paint.Color.BLACK);
//            rect.setStrokeWidth(2);
//
//            rect.setUserData(block);
//
//            // Make blocks interactive
//            rect.setOnMouseClicked(e -> {
//                // 游戏胜利后禁用块选择
//                if (gameLogic.isGameWon()|| isAutoSolving) return;
//                Block clickedModelBlock = (Block) ((Rectangle) e.getSource()).getUserData();
//                if (this.selectedBlock == clickedModelBlock) {
//                    this.selectedBlock = null; // 取消选中
//                } else {
//                    this.selectedBlock = clickedModelBlock; // 选中新的物块
//                }
//                updateSelectedBlockHighlight();
//            });
//
//            // Add block to grid
//            GridPane.setColumnIndex(rect, block.getX());
//            GridPane.setRowIndex(rect, block.getY());
//            GridPane.setColumnSpan(rect, block.getWidth());
//            GridPane.setRowSpan(rect, block.getHeight());
//
//            boardGrid.getChildren().add(rect);
//        }
//
//        // Update move count label
//        moveCountLabel.setText("Moves: " + board.getMoveCount());
//
//        // Highlight the selected block
//        updateSelectedBlockHighlight();
//    }
//    // Highlight the selected block
//    private void updateSelectedBlockHighlight() {
//        if (boardGrid == null) return;
//        // Clear old highlights and apply type-specific colors
//        for (Node node : boardGrid.getChildren()) {
//            if (node instanceof Rectangle rect && isBlock(rect)) {
//                if (rect.getWidth() > 0 && rect.getHeight() > 0
//                        && (rect.getWidth() != 70 || rect.getHeight() != 70)) {
//                rect.setStroke(Color.BLACK);
//                rect.setStrokeWidth(2);
//                rect.setEffect(null);
//                }
//            }
//        }
//        // Highlight selected block
//        if (selectedBlock == null) return;
//        for (Node node : boardGrid.getChildren()) {
//            if (!(node instanceof Rectangle rect)) continue;
//            int col = GridPane.getColumnIndex(rect) == null ? 0 : GridPane.getColumnIndex(rect);
//            int row = GridPane.getRowIndex(rect) == null ? 0 : GridPane.getRowIndex(rect);
//            int spanX = GridPane.getColumnSpan(rect) == null ? 1 : GridPane.getColumnSpan(rect);
//            int spanY = GridPane.getRowSpan(rect) == null ? 1 : GridPane.getRowSpan(rect);
//
//            if (selectedBlock.getX() == col && selectedBlock.getY() == row
//                    && selectedBlock.getWidth() == spanX
//                    && selectedBlock.getHeight() == spanY) {
//                // Create bright glow effect
//                DropShadow glow = new DropShadow();
//                glow.setColor(Color.GOLD);
//                glow.setRadius(10);
//                glow.setSpread(0.8);
//                rect.setEffect(glow);
//                // Set distinctive border
//                rect.setStroke(Color.YELLOW);
//                rect.setStrokeWidth(4);
//                rect.setStroke(Color.YELLOW);
//                rect.setStrokeWidth(4);
//                rect.getStrokeDashArray().clear();
//                break;
//            }
//        }
  }
  // Move the selected block
  private void moveSelected(Direction direction) {
    // 游戏胜利后或自动演示时禁用移动
    if (gameLogic != null && (gameLogic.isGameWon() || isAutoSolving)) {
      return;
    }

    if (selectedBlock != null && gameLogic != null) {
      // 记录移动前选定块的逻辑位置，用于计算动画的相对位移
      int oldGridX = selectedBlock.getX();
      int oldGridY = selectedBlock.getY();

      // 尝试在游戏逻辑中移动块
      if (gameLogic.moveBlock(selectedBlock, direction)) {
        // 移动成功后，selectedBlock 的 x, y 已经是新位置

        // 查找与 selectedBlock 对应的 Rectangle 节点以进行动画
        Rectangle blockRectangleToAnimate = (Rectangle) boardGrid.getChildren().stream().filter(node -> node.getUserData() == selectedBlock && node instanceof Rectangle).findFirst().orElse(null);
        // 通过比较 UserData 来找到对应的 Rectangle

        if (blockRectangleToAnimate != null) {
          // 计算动画需要平移的像素距离
          // 假设每个格子的宽度和高度为 70 像素 (与 updateBoard 中的设置一致)
          double cellWidth = 70.0;
          double cellHeight = 70.0;

          // 计算X和Y方向上的目标平移量
          // blockRectangleToAnimate 的当前视觉位置是基于 oldGridX, oldGridY
          // selectedBlock.getX() 和 selectedBlock.getY() 是移动后的新逻辑位置
          double targetTranslateX = (selectedBlock.getX() - oldGridX) * cellWidth;
          double targetTranslateY = (selectedBlock.getY() - oldGridY) * cellHeight;

          // 创建平移动画
          TranslateTransition tt = new TranslateTransition(Duration.millis(200), blockRectangleToAnimate); // 动画持续200毫秒
          tt.setByX(targetTranslateX); // 设置X方向的相对平移量
          tt.setByY(targetTranslateY); // 设置Y方向的相对平移量
          tt.setCycleCount(1);        // 动画只播放一次
          tt.setAutoReverse(false);   // 不自动反向播放

          // 动画播放完毕后的操作
          tt.setOnFinished(event -> {
            // 动画完成后，重置Rectangle的translate属性，因为它已经被TranslateTransition修改了
            // 这样可以避免影响下一次布局或动画
            blockRectangleToAnimate.setTranslateX(0);
            blockRectangleToAnimate.setTranslateY(0);

            // 现在可以安全地更新整个棋盘的UI了
            // updateBoard() 会根据新的逻辑位置重新绘制所有块
            updateBoard();

            // 检查游戏是否胜利
            if (gameLogic.isGameWon()) {
              showVictoryScene();
            }

            // 如果用户不是游客，则自动保存游戏
            if (!userManager.isGuest()) {
              GameState gs = new GameState(
                  gameLogic.getBoard(),
                  userManager.getCurrentUser().getUsername(),
                  currentLevel,
                  gameLogic.getMoveHistory(),
                  gameLogic.isGameWon());
              gs.setTimeElapsed(System.currentTimeMillis() - startTime);
              gameFileManager.saveGame(userManager.getCurrentUser().getUsername(), gs);
            }

            // 更新移动步数标签
            moveCountLabel.setText("Moves: " + gameLogic.getBoard().getMoveCount());
          });

          // 播放动画
          tt.play();

        } else {
          // 如果由于某种原因没有找到对应的Rectangle (理论上不应该发生)
          // 则回退到立即更新棋盘，没有动画
          updateBoard();
          if (gameLogic.isGameWon()) {
            showVictoryScene();
          }
          if (!userManager.isGuest()) {
            GameState gs = new GameState(
                gameLogic.getBoard(),
                userManager.getCurrentUser().getUsername(),
                currentLevel,
                gameLogic.getMoveHistory(),
                gameLogic.isGameWon());
            gs.setTimeElapsed(System.currentTimeMillis() - startTime);
            gameFileManager.saveGame(userManager.getCurrentUser().getUsername(), gs);
          }
          moveCountLabel.setText("Moves: " + gameLogic.getBoard().getMoveCount());
        }
      }
    } else if (gameLogic != null && gameLogic.getBoard() != null) {
      // 如果没有块被选中 (这部分逻辑保持不变)
      // Block centerBlock = gameLogic.getBoard().getBlockAt(
      // gameLogic.getBoard().getCols() / 2,
      // gameLogic.getBoard().getRows() / 2
      // );
      // if (centerBlock != null) {
      // selectedBlock = centerBlock;
      // updateSelectedBlockHighlight();
      // }
    }
  }
  // undo() 方法
  private void undo() {
    System.out.println("[APP_UNDO] Undo button clicked. isAutoSolving: " + isAutoSolving);
    if (gameLogic != null && (gameLogic.isGameWon() || isAutoSolving)) {
      System.out.println("  Undo blocked: game won or auto-solving.");
      return;
    }

    if (gameLogic != null) {
      System.out.println("  Before gameLogic.undoMove(), history size: " + (gameLogic.getMoveHistory() != null ? gameLogic.getMoveHistory().size() : "null"));
      boolean undoSuccess = gameLogic.undoMove();
      System.out.println("  gameLogic.undoMove() returned: " + undoSuccess);

      if (undoSuccess) {
        updateBoard();
        if (moveCountLabel != null && gameLogic.getBoard() != null) {
          moveCountLabel.setText("Moves: " + gameLogic.getBoard().getMoveCount());
        }
        System.out.println("  After undo, history size: " + (gameLogic.getMoveHistory() != null ? gameLogic.getMoveHistory().size() : "null"));
        if (gameLogic.getMoveHistory() != null && !gameLogic.getMoveHistory().isEmpty() && gameLogic.getBoard() != null) {
          System.out.println("  Current board moveCount after undo: " + gameLogic.getBoard().getMoveCount() +
              ". History top moveCount: " + gameLogic.getMoveHistory().peek().getMoveCount());
        }


        // Auto-save after undo if user is logged in (保持不变)
        if (!userManager.isGuest()) {
          GameState gameState = new GameState(
              gameLogic.getBoard(), // 使用当前 gameLogic 中的 board
              userManager.getCurrentUser().getUsername(),
              currentLevel,
              new ArrayDeque<>(gameLogic.getMoveHistory()), // 复制当前历史
              gameLogic.isGameWon()
          );
          gameState.setTimeElapsed(System.currentTimeMillis() - startTime);
          gameFileManager.saveGame(userManager.getCurrentUser().getUsername(), gameState);
        }
      } else {
        showAlert("Undo Error", "No moves to undo.");
      }
    } else {
      showAlert("Undo Error", "Game logic not available.");
    }
  }
  // Start a new game with a specific level - Modified to accept levelNumber
  private void startNewGame(int levelNumber) {
    Level level = levelManager.getLevel(levelNumber);
    this.selectedBlock = null;
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
      if (moveCountLabel != null) moveCountLabel.setText("Moves: " + gameLogic.getBoard().getMoveCount());
      if (timeLabel != null) timeLabel.setText("Time: " + formatTime(elapsedTime));
      startTimer();

      // Only start auto-save if the user is not a guest
      if (!userManager.isGuest()) {
        gameFileManager.startAutoSave(userManager.getCurrentUser(), () -> {
          GameState autoSaveState = new GameState(
              gameLogic.getBoard().copy(),
              userManager.getCurrentUser().getUsername(),
              currentLevel,
              new ArrayDeque<>(gameLogic.getMoveHistory()),
              gameLogic.isGameWon() // false at start of new game
          );
          // 确保使用 this.startTime 和 this.elapsedTime 的当前值
          // 如果计时器正在运行，startTime 是基准；如果已停止，elapsedTime 是最终值
          long timeToSave;
          if (timer != null) { // 假设 timer != null 表示正在计时或刚停止
            timeToSave = System.currentTimeMillis() - this.startTime;
          } else { // 如果计时器完全停止并且没有有效的startTime（例如，在菜单界面）
            timeToSave = this.elapsedTime; // 使用已累计的时间
          }
          autoSaveState.setTimeElapsed(timeToSave);
          System.out.println("[AUTOSAVE from startNewGame] Supplier Time: " + timeToSave + ", Won: " + autoSaveState.isGameWon());
          return autoSaveState;
        });
      }
    } else {
      showAlert("Error", "Could not load level " + levelNumber);
    }
  }
  private void loadGame(GameState state) {
    System.out.println("[LOADGAME_METHOD] Loading ongoing game state for level: " + state.getCurrentLevel()); // DEBUG

    this.selectedBlock = null;
    if (gameLogic == null) {
      gameLogic = new GameLogic();
    }
    gameLogic.setBoard(state.getBoard());
    if (state.getMoveHistory() != null) {
      gameLogic.setMoveHistory(state.getMoveHistory());
    } else {
      Deque<Board> history = new ArrayDeque<>();
      if (gameLogic.getBoard() != null) {
        history.push(gameLogic.getBoard().copy());
      }
      gameLogic.setMoveHistory(history);
    }
    gameLogic.setIsGameWon(false); // 明确是未胜利的游戏

    this.elapsedTime = state.getTimeElapsed();
    this.currentLevel = state.getCurrentLevel();

    updateBoard();
    if (moveCountLabel != null && gameLogic.getBoard() != null) {
      moveCountLabel.setText("Moves: " + gameLogic.getBoard().getMoveCount());
    }
    if (timeLabel != null) {
      timeLabel.setText("Time: " + formatTime(this.elapsedTime));
    }

    startTimer();

    if (!userManager.isGuest()) {
      gameFileManager.startAutoSave(userManager.getCurrentUser(), () -> {
        GameState autoSaveState = new GameState(
            gameLogic.getBoard().copy(),
            userManager.getCurrentUser().getUsername(),
            currentLevel,
            new ArrayDeque<>(gameLogic.getMoveHistory()),
            gameLogic.isGameWon() // Should be false here
        );
        long currentElapsedTime = (timer != null && startTime != 0) ? (System.currentTimeMillis() - this.startTime) : this.elapsedTime;
        autoSaveState.setTimeElapsed(currentElapsedTime);
        return autoSaveState;
      });
    }
  }
  // KlotskiApp.java
  private void saveGame() {
    if (userManager.isGuest()) {
      showAlert("Save Error", "Guests cannot save games.");
      return;
    }

    if (gameLogic == null || gameLogic.getBoard() == null) {
      showAlert("Save Error", "No game to save.");
      return;
    }
    if (isAutoSolving) {
      showAlert("Save Game", "Cannot save while auto-solving is in progress.");
      return;
    }

    // Recalculate elapsedTime for precision at the moment of saving
    this.elapsedTime = System.currentTimeMillis() - this.startTime; // Use this. for clarity
    System.out.println("[SAVE DEBUG] KlotskiApp.saveGame():");
    System.out.println("  - Current System.currentTimeMillis(): " + System.currentTimeMillis());
    System.out.println("  - this.startTime: " + this.startTime);
    System.out.println("  - Calculated this.elapsedTime for saving: " + this.elapsedTime + " ms (" + formatTime(this.elapsedTime) + ")");

    GameState state = new GameState(
        gameLogic.getBoard().copy(),
        userManager.getCurrentUser().getUsername(),
        currentLevel,
        new ArrayDeque<>(gameLogic.getMoveHistory()),
        gameLogic.isGameWon()
    );
    state.setTimeElapsed(this.elapsedTime);
    System.out.println("  - GameState.timeElapsed set to: " + state.getTimeElapsed() + " ms (" + formatTime(state.getTimeElapsed()) + ")");

    if (gameFileManager.saveGame(userManager.getCurrentUser().getUsername(), state)) {
      showAlert("Save Success", "Game saved successfully.");
    } else {
      showAlert("Save Error", "Failed to save game.");
    }
  }
  //Hint
  private void getAndApplyHint() {
    // 如果游戏已胜利或求解器正在运行，则不执行
    if (gameLogic.isGameWon() || (solverProgress != null && solverProgress.isVisible()) || isAutoSolving) {
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
        gameLogic.setBoard(nextBoard.copy());
        // 保存当前状态到历史（为了支持从提示后的撤销）
        Deque<Board> currentHistory = gameLogic.getMoveHistory();
        currentHistory.push(gameLogic.getBoard().copy()); // 压入新的状态副本
        gameLogic.setMoveHistory(currentHistory); // 更新 gameLogic 的历史引用（如果必要）

        updateBoard(); // 更新 UI
        moveCountLabel.setText("Moves: " + gameLogic.getBoard().getMoveCount()); // 更新步数显示
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
  // Initialize the game file manager
  @Override
  public void stop() {
    if (gameFileManager != null) {
      gameFileManager.shutdown();
    }
    if (timer != null) {
      timer.stop();
    }
  }
  // In KlotskiApp.java

  private void startTimer() {
    stopTimer(); // Stop any existing timer first
    this.startTime = System.currentTimeMillis() - this.elapsedTime; // Ensure using member fields

    this.timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
      this.elapsedTime = System.currentTimeMillis() - this.startTime;

      // Robust condition to update timeLabel:
      // 1. The timeLabel object itself must exist.
      // 2. The timeLabel must currently be part of *some* scene.
      // 3. That scene must be the one currently displayed on the primaryStage.
      if (this.timeLabel != null &&
          this.timeLabel.getScene() != null &&
          this.primaryStage.getScene() == this.timeLabel.getScene()) {
        this.timeLabel.setText("Time: " + formatTime(this.elapsedTime));
      }
    }));

    this.timer.setCycleCount(Timeline.INDEFINITE);
    this.timer.play();
  }
  // Stop the game timer
  private void stopTimer() {
    if (timer != null) {
      timer.stop();
      timer = null;
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
