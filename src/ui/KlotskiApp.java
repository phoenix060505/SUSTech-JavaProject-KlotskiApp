package ui;

import game.*;
import game.AboutGame; // 如果 AboutGame 类在 game 包下
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.MediaPlayer;
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

import java.net.URL;
import java.util.*;

import static game.AboutGame.applyFadeTransition;

public class KlotskiApp extends Application {
  // Sound Players
  private MediaPlayer moveSoundPlayer;
  private MediaPlayer LoginBGM;
  private MediaPlayer GameBGM;
  private MediaPlayer VictoryBGM;

  // Image Resources
  private Image LoginBackground;
  private Image CaoCao, Guanyu, Soldier, General;

  // Game Logic and Management
  private GameLogic gameLogic;
  private GameFileManager gameFileManager;
  private UserManager userManager;
  private LevelManager levelManager;
  private Stage primaryStage;

  // Game State
  private Block selectedBlock;
  private long startTime;
  private long elapsedTime;
  private Timeline timer;
  private int currentLevel = 1;

  // UI Components
  private Button restartButton;
  private Button hintButton;
  private ProgressIndicator solverProgress;
  private GridPane boardGrid;
  private Label moveCountLabel;
  private Label timeLabel;

  // Auto-Solve
  private AutoSolve autoSolver;
  private Button autoSolveButton;
  private boolean isAutoSolving = false; // 已在 AutoSolve 类中管理，这里可能冗余，除非有独立逻辑

  // AboutGame (final field should be fine)
  private final AboutGame aboutGameInstance = new AboutGame(); // 实例化

  @Override
  public void start(Stage primaryStage) {
    this.primaryStage = primaryStage;
    gameLogic = new GameLogic();
    userManager = new UserManager();
    gameFileManager = new GameFileManager();
    levelManager = new LevelManager();

    try {
      // 确保字体只加载一次，或者如果需要不同大小，则分别加载
      Font.loadFont(getClass().getResourceAsStream("/fonts/Life Story Demo.ttf"), 16);
    } catch (Exception e) {
      System.err.println("加载自定义字体失败: " + e.getMessage());
    }
    primaryStage.setTitle("Klotski Puzzle");

    loadResources(); // 调用新的资源加载方法

    // 初始播放登录/主菜单BGM
    if (this.LoginBGM != null) {
      SoundManager.setVolume(this.LoginBGM, 0.4);
      this.LoginBGM.setCycleCount(MediaPlayer.INDEFINITE); // 循环播放
      SoundManager.playSound(this.LoginBGM);
    } else {
      System.err.println("LoginBGM 未能成功加载，无法在启动时播放。");
    }

    showLoginScene(); // 应用程序启动时显示登录界面

    // primaryStage.getScene() 此时可能为 null，因为 scene 是在 showLoginScene 中设置的
    // 将样式表加载移到各个 show...Scene 方法内部，确保 scene 对象已创建

    primaryStage.show();
    primaryStage.setOnCloseRequest(event -> {
      if (gameFileManager != null) {
        gameFileManager.shutdown();
      }
      if (timer != null) {
        timer.stop();
      }
      // 确保所有 MediaPlayer 在退出时被正确处理（如果需要）
      SoundManager.stopSound(LoginBGM);
      SoundManager.stopSound(GameBGM);
      SoundManager.stopSound(VictoryBGM);
      SoundManager.stopSound(moveSoundPlayer);
    });
  }

  private void loadResources() {
    // --- 加载声音 ---
    SoundManager sm = new SoundManager(); // SoundManager实例现在是局部的，如果方法都是静态的则不需要实例

    try {
      URL moveSoundUrl = getClass().getResource("/sounds/moveSound.wav");
      if (moveSoundUrl != null) this.moveSoundPlayer = sm.loadSound(moveSoundUrl.toExternalForm());
      else System.err.println("错误：声音 moveSound.wav 未在 /sounds/ 找到。");

      URL loginBgmUrl = getClass().getResource("/sounds/LoginBGM.wav");
      if (loginBgmUrl != null) this.LoginBGM = sm.loadSound(loginBgmUrl.toExternalForm());
      else System.err.println("错误：声音 LoginBGM.wav 未在 /sounds/ 找到。");

      URL gameBgmUrl = getClass().getResource("/sounds/GameBGM.wav");
      if (gameBgmUrl != null) this.GameBGM = sm.loadSound(gameBgmUrl.toExternalForm());
      else System.err.println("错误：声音 GameBGM.wav 未在 /sounds/ 找到。");

      // 确认 VictoryBGM 的实际文件名和扩展名大小写
      URL victoryBgmUrl = getClass().getResource("/sounds/VictoryBGM.WAV"); // 或 .wav
      if (victoryBgmUrl != null) this.VictoryBGM = sm.loadSound(victoryBgmUrl.toExternalForm());
      else System.err.println("错误：声音 VictoryBGM.WAV (或 .wav) 未在 /sounds/ 找到。");

    } catch (Exception e) {
      System.err.println("加载声音资源过程中发生一般性错误: " + e.getMessage());
      e.printStackTrace();
    }

    // --- 加载图片 ---
    try {
      URL loginBgUrl = getClass().getResource("/images/LoginBackground.png");
      if (loginBgUrl != null) LoginBackground = new Image(loginBgUrl.toExternalForm());
      else System.err.println("错误：图片 LoginBackground.png 未在 /images/ 找到。");

      URL caoCaoUrl = getClass().getResource("/images/CaoCao.png");
      if (caoCaoUrl != null) CaoCao = new Image(caoCaoUrl.toExternalForm());
      else System.err.println("错误：图片 CaoCao.png 未在 /images/ 找到。");

      URL guanyuUrl = getClass().getResource("/images/Guanyu.png");
      if (guanyuUrl != null) Guanyu = new Image(guanyuUrl.toExternalForm());
      else System.err.println("错误：图片 Guanyu.png 未在 /images/ 找到。");

      URL soldierUrl = getClass().getResource("/images/Soldier.png");
      if (soldierUrl != null) Soldier = new Image(soldierUrl.toExternalForm());
      else System.err.println("错误：图片 Soldier.png 未在 /images/ 找到。");

      URL generalUrl = getClass().getResource("/images/General.png");
      if (generalUrl != null) General = new Image(generalUrl.toExternalForm());
      else System.err.println("错误：图片 General.png 未在 /images/ 找到。");

    } catch (Exception e) {
      System.err.println("加载图片资源过程中发生一般性错误: " + e.getMessage());
      e.printStackTrace();
    }
  }


  private void switchToMainMenuScene() {
    SoundManager.stopSound(GameBGM);
    SoundManager.stopSound(VictoryBGM); // 如果胜利界面有自己的BGM或效果音也应停止

    if (LoginBGM != null) {
      LoginBGM.setCycleCount(MediaPlayer.INDEFINITE);
      SoundManager.playSound(LoginBGM);
    }
    showMainMenu();
  }

  private void switchToGameSceneFromMenu() {
    SoundManager.stopSound(LoginBGM);
    // GameBGM 的播放在 showGameScene 方法内部处理，因为它可能根据新游戏或加载游戏而不同
    showGameScene();
    // 注意：startNewGame 或 loadGame 应该在 showGameScene 之后被调用，
    // showGameScene 仅设置UI，实际的游戏逻辑初始化（可能包括播放GameBGM）在它们里面。
  }


  private void showLoginScene() {
    // 确保LoginBGM在登录界面是播放的（如果它在start()中因某些原因未播放或被停止）
    // 通常在start()中已经开始播放了
    if (this.LoginBGM != null && this.LoginBGM.getStatus() != MediaPlayer.Status.PLAYING) {
      this.LoginBGM.setCycleCount(MediaPlayer.INDEFINITE);
      SoundManager.playSound(this.LoginBGM);
    } else if (this.LoginBGM == null) {
      System.err.println("LoginBGM为null，无法在showLoginScene中播放。");
    }

    VBox loginBox = new VBox(15);
    loginBox.getStyleClass().add("root");
    loginBox.getStyleClass().add("login-pane");
    loginBox.setAlignment(Pos.CENTER);
    loginBox.setPadding(new Insets(20));

    if (LoginBackground != null) {
      loginBox.setStyle("-fx-background-image: url('" + LoginBackground.getUrl() + "');" +
              "-fx-background-size: cover;" +
              "-fx-background-position: center;");
    } else {
      loginBox.setStyle("-fx-background-color: #F5EFE6;"); // Fallback
    }

    StackPane overlay = new StackPane();
    overlay.setStyle("-fx-background-color:transparent;");
    overlay.setMaxSize(400, 300);
    VBox contentBox = new VBox(15);
    contentBox.setAlignment(Pos.CENTER);

    Text title = new Text("Klotski Puzzle");
    title.getStyleClass().add("title-text");
    // ... (title gradient) ...
    LinearGradient gradient = new LinearGradient(
            0, 0, 1, 0, true, CycleMethod.REFLECT,
            new Stop(0.0, Color.DEEPPINK), new Stop(0.2, Color.BLUE),
            new Stop(0.4, Color.GREEN), new Stop(0.6, Color.ORANGE),
            new Stop(0.8, Color.RED), new Stop(1.0, Color.PURPLE)
    );
    title.setFill(gradient);


    Button guestButton = new Button("Play as Guest");
    Button loginButton = new Button("Login");
    Button registerButton = new Button("Register");
    Button aboutGameButton = new Button("About Game"); // Renamed for clarity
    guestButton.setMinWidth(200);
    loginButton.setMinWidth(200);
    registerButton.setMinWidth(200);
    aboutGameButton.setMinWidth(200);

    guestButton.setOnAction(e -> {
      applyFadeTransition(guestButton);
      userManager.loginAsGuest();
      // LoginBGM 应该继续播放，因为它也是主菜单的BGM
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
    aboutGameButton.setOnAction(e -> {
      applyFadeTransition(aboutGameButton);
      // 考虑在进入“关于游戏”时是否暂停LoginBGM
      // SoundManager.stopSound(LoginBGM); // 可选
      showGameIntroduction();
    });

    contentBox.getChildren().addAll(title, guestButton, loginButton, registerButton, aboutGameButton);
    overlay.getChildren().add(contentBox);
    loginBox.getChildren().add(overlay);

    Scene scene = new Scene(loginBox, 400, 300);
    applyStylesheetsToScene(scene, "/css/WarmTheme.css");
    primaryStage.setScene(scene);
    primaryStage.centerOnScreen();
  }

  private void showGameIntroduction() {
    // 当进入“关于游戏”界面时，可以考虑暂停LoginBGM
    // 如果希望LoginBGM继续播放，则不执行停止操作
    // SoundManager.stopSound(LoginBGM); // 示例：暂停

    VBox vbox = new VBox(20);
    vbox.setAlignment(Pos.BOTTOM_CENTER);
    vbox.setPadding(new Insets(20));
    vbox.setFillWidth(true);
    if (LoginBackground != null) {
      vbox.setStyle("-fx-background-image: url('" + LoginBackground.getUrl() + "');" +
              "-fx-background-size: cover;" +
              "-fx-background-position: center;");
    } else {
      vbox.setStyle("-fx-background-color: #F5EFE6;");
    }
    Label introduction = new Label();
    introduction.setWrapText(true);
    introduction.setMaxWidth(Double.MAX_VALUE);
    introduction.setStyle("-fx-font-size: 16px; -fx-line-spacing: 0.5em; -fx-text-fill: black;");

    ScrollPane scrollPane = new ScrollPane(introduction);
    scrollPane.setFitToWidth(true);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setVvalue(0);
    scrollPane.setPrefHeight(380);
    scrollPane.setStyle("-fx-background: rgba(255,255,255,0.7); -fx-background-color: rgba(255,255,255,0.7);");

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
                Will your strategy rival the brilliance of ancient tacticians? Sharpen your mind, honor the legends, and escape the past—one slide at a time.""";

    Button labelEffectButton = new Button("Can you master the puzzle… and rewrite history?");
    labelEffectButton.fontProperty().set(Font.font("System", FontPosture.ITALIC, 20));
    labelEffectButton.setWrapText(true);
    labelEffectButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #fd0001");
    labelEffectButton.setOpacity(0.0);
    labelEffectButton.setVisible(false);

    final IntegerProperty i = new SimpleIntegerProperty(0);
    Timeline timeline = new Timeline();

    Button backButton = new Button("Back");
    backButton.setMinWidth(200);

    Region spacer = new Region();
    VBox.setVgrow(spacer, Priority.ALWAYS);

    backButton.setOnAction(e -> {
      timeline.stop();
      applyFadeTransition(backButton);
      showLoginScene();
    });

    Button skipButton = new Button("Skip");
    skipButton.setOnAction(e -> {
      timeline.stop();
      introduction.setText(fullText);
      scrollPane.setVvalue(1.0);
      skipButton.setVisible(false);
      applyFadeTransition(labelEffectButton);
    });

    KeyFrame keyFrame = new KeyFrame(Duration.millis(20), event -> {
      if (i.get() >= fullText.length()) {
        timeline.stop();
        skipButton.setVisible(false);
        applyFadeTransition(labelEffectButton);
        return;
      }
      introduction.setText(fullText.substring(0, i.get()));
      scrollPane.setVvalue(1.0);
      i.set(i.get() + 1);
    });

    timeline.getKeyFrames().add(keyFrame);
    timeline.setCycleCount(fullText.length()); // Animate for the duration of the text
    Platform.runLater(timeline::play);

    vbox.getChildren().addAll(skipButton, scrollPane, spacer, labelEffectButton, backButton);
    Scene scene = new Scene(vbox, 600, 500);
    applyStylesheetsToScene(scene, "/css/WarmTheme.css");
    primaryStage.setScene(scene);
    primaryStage.centerOnScreen();
  }


  private void showLoginForm() {
    // LoginForm 通常不需要独立的BGM，会继承LoginScene的BGM（如果LoginBGM在播放）
    GridPane grid = new GridPane();
    // ... (grid setup from your code) ...
    grid.setAlignment(Pos.CENTER);
    grid.setHgap(20);
    grid.setVgap(20);
    grid.setPadding(new Insets(20));
    if (LoginBackground != null) {
      grid.setStyle("-fx-background-image: url('" + LoginBackground.getUrl() + "');" +
              "-fx-background-size: cover;" +
              "-fx-background-position: center;");
    } else {
      grid.setStyle("-fx-background-color: #f0f0f0;");
    }
    // ... (WaveTextField, WavePasswordField, buttons setup) ...
    Text title = new Text("Login");
    title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
    grid.add(title, 0, 0, 2, 1);

    WaveTextField usernameField = new WaveTextField("Username");
    grid.add(usernameField, 0,1,2, 1);

    WavePasswordField passwordField = new WavePasswordField("Password");
    grid.add(passwordField,0,2,2,1);

    Button loginBtnInternal = new Button("Login"); // Changed name to avoid conflict
    Button backBtnInternal = new Button("Back");   // Changed name

    HBox buttonBox = new HBox(10);
    buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
    buttonBox.getChildren().addAll(backBtnInternal, loginBtnInternal);
    grid.add(buttonBox, 1, 4);


    loginBtnInternal.setOnAction(e -> {
      String username = usernameField.getText();
      String password = passwordField.getText();
      if (username.isEmpty() || password.isEmpty()) {
        showAlert("Login Error", "Username and password cannot be empty.");
        return;
      }
      if (userManager.loginUser(username, password)) {
        // 成功登录后，LoginBGM 应该继续（或开始）播放，因为它也是主菜单的BGM
        showMainMenu(); // 这会显示主菜单，音乐应已在播放
      } else {
        showAlert("Login Error", "Invalid username or password.");
      }
    });
    backBtnInternal.setOnAction(e -> showLoginScene());

    Scene scene = new Scene(grid, 400, 300);
    applyStylesheetsToScene(scene, "/css/WarmTheme.css", "/css/wave.css");
    primaryStage.setScene(scene);
    primaryStage.centerOnScreen();
  }

  private void showRegisterForm() {
    GridPane grid = new GridPane();
    grid.setAlignment(Pos.CENTER);
    grid.setHgap(20);
    grid.setVgap(20);
    grid.setPadding(new Insets(20));
    if (LoginBackground != null) {
      grid.setStyle("-fx-background-image: url('" + LoginBackground.getUrl() + "');" +
              "-fx-background-size: cover;" +
              "-fx-background-position: center;");
    } else {
      grid.setStyle("-fx-background-color: #f0f0f0;");
    }
    Text title = new Text("Register");
    title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
    grid.add(title, 0, 0, 2, 1);

    WaveTextField usernameField = new WaveTextField("Username");
    grid.add(usernameField, 0,1,1,1);

    WavePasswordField passwordField = new WavePasswordField("Password");
    grid.add(passwordField,0,3,1,1);

    WavePasswordConfirm confirmField = new WavePasswordConfirm("Confirm Password");
    grid.add(confirmField, 0, 4, 1, 1);

    Button registerBtnInternal = new Button("Register");
    Button backBtnInternal = new Button("Back");

    HBox buttonBox = new HBox(10);
    buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
    buttonBox.getChildren().addAll(backBtnInternal, registerBtnInternal);
    grid.add(buttonBox, 1, 6);

    registerBtnInternal.setOnAction(e -> {
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
        showLoginScene(); // 返回登录界面，LoginBGM应该会继续播放
      } else {
        showAlert("Registration Error", "Username already exists.");
      }
    });
    backBtnInternal.setOnAction(e -> showLoginScene());


    Scene scene = new Scene(grid, 500, 400);
    applyStylesheetsToScene(scene, "/css/WarmTheme.css", "/css/wave.css");
    primaryStage.setScene(scene);
    primaryStage.centerOnScreen();
  }
  private void showMainMenu() {
    if (gameFileManager != null) {
      gameFileManager.stopAutoSave(); // 确保从游戏返回时停止自动保存
    }
    stopTimer(); // 停止游戏计时器

    // 确保 LoginBGM (主菜单音乐) 正在播放
    if (this.LoginBGM != null) {
      if (this.LoginBGM.getStatus() != MediaPlayer.Status.PLAYING) {
        this.LoginBGM.setCycleCount(MediaPlayer.INDEFINITE);
        SoundManager.playSound(this.LoginBGM);
      }
    } else {
      System.err.println("LoginBGM为null，无法在showMainMenu中播放。");
    }


    VBox menuBox = new VBox(15);
    menuBox.setAlignment(Pos.CENTER);
    menuBox.setPadding(new Insets(20));
    if (LoginBackground != null) {
      menuBox.setStyle("-fx-background-image: url('" + LoginBackground.getUrl() + "');" +
              "-fx-background-size: cover;" +
              "-fx-background-position: center;");
    } else {
      menuBox.setStyle("-fx-background-color: #f0f0f0;");
    }
    Text titleText = new Text("Klotski Puzzle"); // Renamed
    titleText.setFont(Font.font("Arial", FontWeight.BOLD, 24));

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
    loadGameButton.setDisable(userManager.isGuest());

    Label levelLabel = new Label("Select Level:");
    ComboBox<String> levelComboBox = new ComboBox<>();
    List<String> levelNames = new ArrayList<>();
    for (int i = 1; i <= levelManager.getLevelCount(); i++) {
      levelNames.add(levelManager.getLevel(i).getName());
    }
    levelComboBox.getItems().addAll(levelNames);
    levelComboBox.getSelectionModel().selectFirst();


    newGameButton.setOnAction(e -> {
      int levelNumber = levelComboBox.getSelectionModel().getSelectedIndex() + 1;
      switchToGameSceneFromMenu(); // 处理音乐切换并显示游戏场景
      startNewGame(levelNumber);   // 初始化新游戏逻辑
    });

    loadGameButton.setOnAction(e -> {
      if (userManager.isGuest() || userManager.getCurrentUser() == null || gameFileManager == null) {
        if (userManager.isGuest()) showAlert("Load Error", "Guests cannot load games. Please log in or register.");
        else if (userManager.getCurrentUser() == null) showAlert("Load Error", "No user logged in.");
        else showAlert("Load Error", "File manager not available.");
        return;
      }
      GameState state = gameFileManager.loadGame(userManager.getCurrentUser().getUsername());
      if (state != null) {
        if (state.isGameWon()) {
          showAlert("Load Error", "The loaded game is already won. Start a new game or load a different save.");
        } else {
          switchToGameSceneFromMenu(); // 处理音乐切换并显示游戏场景
          loadGame(state);             // 加载游戏状态
        }
      } else {
        showAlert("Load Error", "No saved game found or failed to load.");
      }
    });

    logoutButton.setOnAction(e -> {
      userManager.logout();
      showLoginScene();
    });

    menuBox.getChildren().addAll(titleText, welcomeText, levelLabel, levelComboBox, newGameButton, loadGameButton, logoutButton);

    Scene scene = new Scene(menuBox, 400, 400);
    applyStylesheetsToScene(scene, "/css/WarmTheme.css");
    // applyStylesheetsToScene(scene, "/css/wave.css"); // 如果主菜单也用wave控件
    primaryStage.setScene(scene);
    primaryStage.centerOnScreen();
  }


  private void showGameScene() {
    // GameBGM 的播放由调用此方法的地方（如 switchToGameSceneFromMenu 或 startNewGame/loadGame 内部）决定
    // 此方法仅负责UI构建
    // 确保在进入游戏场景时，登录/主菜单音乐已停止
    SoundManager.stopSound(LoginBGM);
    // 开始播放游戏音乐
    if (this.GameBGM != null) {
      this.GameBGM.setCycleCount(MediaPlayer.INDEFINITE);
      SoundManager.playSound(this.GameBGM);
    } else {
      System.err.println("GameBGM为null，无法在showGameScene中播放。");
    }

    BorderPane root = new BorderPane();
    root.setPadding(new Insets(10));
    if (LoginBackground != null) { // 游戏背景也可以用LoginBackground，或指定新的
      root.setStyle("-fx-background-image: url('" + LoginBackground.getUrl() + "');" +
              "-fx-background-size: cover;" +
              "-fx-background-position: center;");
    } else {
      root.setStyle("-fx-background-color: #F5EFE6;");
    }

    HBox topPanel = new HBox(20);
    topPanel.setPadding(new Insets(10));
    topPanel.setAlignment(Pos.CENTER);
    moveCountLabel = new Label("Moves: 0");
    timeLabel = new Label("Time: 00:00");
    topPanel.getChildren().addAll(moveCountLabel, timeLabel);
    root.setTop(topPanel);

    boardGrid = new GridPane();
    boardGrid.setAlignment(Pos.CENTER);
    boardGrid.setHgap(2);
    boardGrid.setVgap(2);
    boardGrid.setStyle("-fx-background-color: #333333;"); // 棋盘背景色
    boardGrid.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    boardGrid.setPrefSize(305, 375); // (4*73 + 3*2 approx) , (5*73 + 4*2 approx)
    StackPane boardPane = new StackPane(boardGrid);
    root.setCenter(boardPane);

    HBox controlPanel = new HBox(10);
    controlPanel.setPadding(new Insets(10));
    controlPanel.setAlignment(Pos.CENTER);

    Button upButton = new Button("Up");
    Button downButton = new Button("Down");
    Button leftButton = new Button("Left");
    Button rightButton = new Button("Right");
    Button undoButton = new Button("Undo");
    Button saveButton = new Button("Save");
    Button menuNavButton = new Button("Menu"); // Renamed to avoid conflict
    hintButton = new Button("Hint");
    autoSolveButton = new Button("Auto-Solve");
    restartButton = new Button("Restart");
    solverProgress = new ProgressIndicator(-1);
    solverProgress.setVisible(false);
    saveButton.setDisable(userManager.isGuest());

    autoSolver = new AutoSolve(
            gameLogic, autoSolveButton, hintButton, solverProgress,
            this::updateBoard, this::showVictoryScene, this::showAlert,
            this::saveGameOnAutoSolveWin, () -> elapsedTime
    );

    upButton.setOnAction(e -> moveSelected(Direction.UP));
    downButton.setOnAction(e -> moveSelected(Direction.DOWN));
    leftButton.setOnAction(e -> moveSelected(Direction.LEFT));
    rightButton.setOnAction(e -> moveSelected(Direction.RIGHT));
    hintButton.setOnAction(e -> getAndApplyHint());
    autoSolveButton.setOnAction(e -> {
      autoSolver.toggleAutoSolve();
      if (moveCountLabel != null && gameLogic.getBoard() != null) {
        moveCountLabel.setText("Moves: " + gameLogic.getBoard().getMoveCount());
      }
    });
    undoButton.setOnAction(e -> undo());
    saveButton.setOnAction(e -> saveGame());
    restartButton.setOnAction(e -> {
      if (gameLogic != null) {
        if (autoSolver != null && autoSolver.isAutoSolving()) {
          showAlert("Restart", "Cannot restart while auto-solving is in progress.");
          return;
        }
        // 重新开始游戏时，应该重新播放GameBGM（如果之前被停止了）
        if (this.GameBGM != null) {
          this.GameBGM.setCycleCount(MediaPlayer.INDEFINITE);
          SoundManager.playSound(this.GameBGM);
        }
        gameLogic.restartGame(currentLevel); // 这会重置棋盘和步数
        updateBoard(); // 更新显示
        // 重置计时器和标签
        elapsedTime = 0;
        startTime = System.currentTimeMillis(); // 重置开始时间
        if (timeLabel != null) timeLabel.setText("Time: 00:00");
        if (moveCountLabel != null && gameLogic.getBoard()!=null) moveCountLabel.setText("Moves: " + gameLogic.getBoard().getMoveCount());
        startTimer(); // 重新启动计时器
      }
    });
    menuNavButton.setOnAction(e -> { // 使用 menuNavButton
      if (autoSolver != null && autoSolver.isAutoSolving()) {
        autoSolver.toggleAutoSolve(); // 停止自动求解
      }
      stopTimer(); // 停止游戏计时器
      switchToMainMenuScene(); // 切换到主菜单并处理音乐
    });

    controlPanel.getChildren().addAll(
            upButton, downButton, leftButton, rightButton,
            undoButton, saveButton, hintButton, autoSolveButton, restartButton, menuNavButton, solverProgress);
    root.setBottom(controlPanel);
    // 防止按钮抢占键盘焦点
    for (Button b : List.of(upButton, downButton, leftButton, rightButton,
            undoButton, saveButton, hintButton, autoSolveButton, restartButton, menuNavButton)) {
      b.setFocusTraversable(false);
    }

    Scene scene = new Scene(root, 1100, 600); // 根据内容调整大小
    applyStylesheetsToScene(scene, "/css/WarmTheme.css");
    scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
      if (gameLogic == null || gameLogic.isGameWon() || (autoSolver !=null && autoSolver.isAutoSolving()) ) return;
      switch (e.getCode()) {
        case UP: moveSelected(Direction.UP); break;
        case DOWN: moveSelected(Direction.DOWN); break;
        case LEFT: moveSelected(Direction.LEFT); break;
        case RIGHT: moveSelected(Direction.RIGHT); break;
        case Z: if (e.isControlDown()) undo(); break;
        case S: if (e.isControlDown()) saveGame(); break;
        case H: if (e.isControlDown()) getAndApplyHint(); break;
        case A: if (e.isControlDown() && autoSolver!=null) autoSolver.toggleAutoSolve(); break;
        case R: if (e.isControlDown()) restartButton.fire(); break;
        case ESCAPE: menuNavButton.fire(); break; // 触发返回主菜单按钮的事件
      }
    });

    primaryStage.setScene(scene);
    primaryStage.centerOnScreen();
    root.requestFocus(); // 确保根节点获得焦点以响应键盘事件
    root.setOnMouseClicked(ev -> root.requestFocus()); // 点击棋盘区域也夺回焦点
  }
  private void showVictoryScene() {
    stopTimer(); // 停止游戏计时器
    SoundManager.stopSound(GameBGM); // 停止游戏背景音乐

    // 播放胜利音效
    if (VictoryBGM != null) {
      VictoryBGM.setCycleCount(1); // 胜利音效通常不循环
      SoundManager.playSound(VictoryBGM);
    } else {
      System.err.println("VictoryBGM为null，无法播放胜利音效。");
    }

    VBox victoryBox = new VBox(15);
    // ... (victoryBox setup as in your original code) ...
    victoryBox.setAlignment(Pos.CENTER);
    victoryBox.setPadding(new Insets(20));
    victoryBox.setStyle("-fx-background-color: #f0f0f0;"); // 可以自定义胜利背景

    Text title = new Text("Victory!");
    title.setFont(Font.font("Arial", FontWeight.BOLD, 24));

    Text moveText = new Text("Moves: " + (gameLogic != null && gameLogic.getBoard() != null ? gameLogic.getBoard().getMoveCount() : "N/A"));
    moveText.setFont(Font.font("Arial", FontWeight.NORMAL, 18));

    Text timeText = new Text("Time: " + formatTime(elapsedTime));
    timeText.setFont(Font.font("Arial", FontWeight.NORMAL, 18));

    Button newGameButton = new Button("New Game");
    Button menuButton = new Button("Menu"); // 这个 menuButton 实例与游戏场景中的不同

    newGameButton.setMinWidth(200);
    menuButton.setMinWidth(200);

    newGameButton.setOnAction(e -> {
      // 停止胜利音效（如果它还在播放或可能循环）
      SoundManager.stopSound(VictoryBGM);
      // 切换到游戏场景，并开始新游戏，GameBGM会在showGameScene或startNewGame中处理
      switchToGameSceneFromMenu(); // 确保LoginBGM停止, GameBGM会准备好
      startNewGame(currentLevel); // 开始新游戏（会播放GameBGM）
    });

    menuButton.setOnAction(e -> {
      // 停止胜利音效
      SoundManager.stopSound(VictoryBGM);
      switchToMainMenuScene(); // 返回主菜单并播放LoginBGM
    });

    victoryBox.getChildren().addAll(title, moveText, timeText, newGameButton, menuButton);

    Scene scene = new Scene(victoryBox, 400, 300);
    applyStylesheetsToScene(scene, "/css/WarmTheme.css");
    primaryStage.setScene(scene);
    primaryStage.centerOnScreen();
  }
  // 辅助方法，用于安全地加载样式表
  private void applyStylesheetsToScene(Scene scene, String... cssPaths) {
    if (scene == null) return;
    for (String cssPath : cssPaths) {
      try {
        URL cssUrl = getClass().getResource(cssPath);
        if (cssUrl != null) {
          scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
          System.err.println("错误: CSS 文件 " + cssPath + " 未在类路径找到。");
        }
      } catch (Exception e) {
        System.err.println("加载CSS (" + cssPath + ") 时出错: " + e.getMessage());
      }
    }
  }

  private void updateBoard() {
    if (boardGrid == null) {
      System.err.println("错误: boardGrid 在 updateBoard() 中为 null");
      return;
    }
    boardGrid.getChildren().clear(); // 清空棋盘现有内容

    Board currentBoard = gameLogic.getBoard();
    if (currentBoard == null) {
      System.err.println("错误: gameLogic.getBoard() 在 updateBoard() 中返回 null");
      return;
    }

    // 绘制棋盘格子背景
    for (int row = 0; row < currentBoard.getRows(); row++) {
      for (int col = 0; col < currentBoard.getCols(); col++) {
        Rectangle cell = new Rectangle(80, 80); // 假设格子大小
        cell.setFill(Color.LIGHTGRAY); // 格子背景色
        // cell.setStroke(Color.DIMGRAY); // 可选的格子边框
        boardGrid.add(cell, col, row);
      }
    }

    // 绘制棋子
    for (Block block : currentBoard.getBlocks()) {
      Rectangle rect = new Rectangle(
              80 * block.getWidth() , // 稍作调整以适应可能的边框或间隙
              80 * block.getHeight()
      );
      try {
        rect.setFill(Color.valueOf(block.getColorString()));
      } catch (Exception e) {
        System.err.println("设置棋子颜色时出错: " + block.getColorString() + " - " + e.getMessage());
        rect.setFill(Color.GRAY); // 备用颜色
      }
      rect.setStroke(Color.BLACK);
      rect.setStrokeWidth(0);
      rect.setUserData(block); // 将Block模型关联到Rectangle，用于点击事件

      ImageView imageView = new ImageView();
      Image imageToSet = null;
      String blockType = block.getType();

      if ("CaoCao".equals(blockType) && this.CaoCao != null) imageToSet = this.CaoCao;
      else if ("GuanYu".equals(blockType) && this.Guanyu != null) imageToSet = this.Guanyu;
      else if ("Soldier".equals(blockType) && this.Soldier != null) imageToSet = this.Soldier;
      else if ("General".equals(blockType) && this.General != null) imageToSet = this.General;

      if (imageToSet != null) {
        imageView.setImage(imageToSet);
        imageView.setFitWidth(rect.getWidth() ); // 图片比矩形略小，留出边框空间
        imageView.setFitHeight(rect.getHeight());
        imageView.setSmooth(true);
      } else {
        if (blockType != null && !blockType.isEmpty()) { // 只对已知类型但图片未加载的情况报错
          System.err.println("图片未加载或未知方块类型 (updateBoard): " + blockType);
        }
      }

      // Rectangle 作为背景和边框，ImageView 在其上
      StackPane blockContainer = new StackPane(rect, imageView);
      StackPane.setAlignment(imageView, Pos.CENTER);

      // 点击事件处理逻辑（与之前类似，但现在是对 StackPane）
      blockContainer.setOnMouseClicked(e -> {
        if (gameLogic.isGameWon() || (autoSolver != null && autoSolver.isAutoSolving())) {
          return;
        }
        // Block modelBlock = block; // 直接使用循环中的block变量，因为它代表当前处理的棋子
        if (this.selectedBlock == block) {
          this.selectedBlock = null; // 取消选中
        } else {
          this.selectedBlock = block; // 选中新的物块
        }
        updateSelectedBlockHighlight(); // 更新高亮
      });

      GridPane.setColumnIndex(blockContainer, block.getX());
      GridPane.setRowIndex(blockContainer, block.getY());
      GridPane.setColumnSpan(blockContainer, block.getWidth());
      GridPane.setRowSpan(blockContainer, block.getHeight());
      boardGrid.getChildren().add(blockContainer);
    }

    updateSelectedBlockHighlight(); // 更新选中棋子的高亮

    if (moveCountLabel != null) {
      moveCountLabel.setText("Moves: " + currentBoard.getMoveCount());
    }
  }

  private void updateSelectedBlockHighlight() {
    for (Node node : boardGrid.getChildren()) {
      if (node instanceof StackPane) { // 现在棋子是 StackPane
        StackPane container = (StackPane) node;
        // StackPane 的第一个子节点应该是 Rectangle (背景)
        if (!container.getChildren().isEmpty() && container.getChildren().get(0) instanceof Rectangle) {
          Rectangle rectNode = (Rectangle) container.getChildren().get(0);
          Block modelOfThisRect = (Block) rectNode.getUserData(); // UserData 存在 Rectangle 上

          if (modelOfThisRect != null && modelOfThisRect == this.selectedBlock) {
            DropShadow borderGlow = new DropShadow();
            borderGlow.setColor(Color.GOLD);
            borderGlow.setRadius(15); // 增加半径使效果更明显
            borderGlow.setSpread(0.7);
            // 将效果应用到整个 StackPane 或仅 Rectangle，取决于视觉需求
            // container.setEffect(borderGlow); // 应用到整个容器
            rectNode.setEffect(borderGlow); // 或者仅高亮矩形本身
          } else {
            // container.setEffect(null);
            rectNode.setEffect(null); // 移除高亮
          }
        }
      }
    }
  }

  private void moveSelected(Direction direction) {
    if (gameLogic == null || selectedBlock == null || gameLogic.isGameWon() || (autoSolver != null && autoSolver.isAutoSolving())) {
      return;
    }

    int oldGridX = selectedBlock.getX();
    int oldGridY = selectedBlock.getY();

    if (gameLogic.moveBlock(selectedBlock, direction)) { // moveBlock 现在应该返回 boolean
      if (this.moveSoundPlayer != null) SoundManager.playSound(this.moveSoundPlayer);
      StackPane blockContainerToAnimate = null;
      for(Node n : boardGrid.getChildren()){
        if(n instanceof StackPane){
          StackPane sp = (StackPane) n;
          if(!sp.getChildren().isEmpty() && sp.getChildren().get(0) instanceof Rectangle){
            Rectangle r = (Rectangle) sp.getChildren().get(0);
            if(r.getUserData() == selectedBlock){ // 假设selectedBlock是移动后的那个block实例
              blockContainerToAnimate = sp;
              break;
            }
          }
        }
      }

      if (blockContainerToAnimate != null) {
        double cellVisualWidth = boardGrid.getWidth() / gameLogic.getBoard().getCols();
        double cellVisualHeight = boardGrid.getHeight() / gameLogic.getBoard().getRows();
        if (boardGrid.getPrefWidth() > 0 && gameLogic.getBoard().getCols() > 0) {
          cellVisualWidth = boardGrid.getPrefWidth() / gameLogic.getBoard().getCols();
        }
        if (boardGrid.getPrefHeight() > 0 && gameLogic.getBoard().getRows() > 0) {
          cellVisualHeight = boardGrid.getPrefHeight() / gameLogic.getBoard().getRows();
        }


        double targetTranslateX = (selectedBlock.getX() - oldGridX) * cellVisualWidth;
        double targetTranslateY = (selectedBlock.getY() - oldGridY) * cellVisualHeight;

        TranslateTransition tt = new TranslateTransition(Duration.millis(150), blockContainerToAnimate);
        tt.setByX(targetTranslateX);
        tt.setByY(targetTranslateY);
        StackPane finalBlockContainerToAnimate = blockContainerToAnimate;
        tt.setOnFinished(event -> {
          finalBlockContainerToAnimate.setTranslateX(0); // 重置动画属性
          finalBlockContainerToAnimate.setTranslateY(0);
          updateBoard(); // 重新绘制整个棋盘以反映最终状态

          if (gameLogic.isGameWon()) {
            saveGame(); // 先保存游戏状态
            showVictoryScene();
          } else {
            // 非胜利状态下，移动后也保存（如果不是游客）
            if (!userManager.isGuest()) {
              saveGame();
            }
          }
          // 更新步数标签已在 updateBoard() 中
        });
        tt.play();
      } else {
        // 如果找不到动画目标，直接更新棋盘
        updateBoard();
        if (gameLogic.isGameWon()) {
          saveGame();
          showVictoryScene();
        } else {
          if (!userManager.isGuest()) {
            saveGame();
          }
        }
      }
    }
  }


  private void undo() {
    if (gameLogic == null || gameLogic.isGameWon() || (autoSolver != null && autoSolver.isAutoSolving())) {
      if (gameLogic != null && (gameLogic.isGameWon() || (autoSolver != null && autoSolver.isAutoSolving())))
        System.out.println("撤销操作被阻止：游戏已胜利或正在自动求解。");
      return;
    }

    if (gameLogic.undoMove()) {
      updateBoard(); // 更新棋盘显示
      // 撤销后通常也需要保存游戏状态（如果不是游客）
      if (!userManager.isGuest()) {
        saveGame(); // saveGame内部会获取最新状态
      }
    } else {
      showAlert("撤销错误", "没有可供撤销的步骤。");
    }
  }

  private void startNewGame(int levelNumber) {
    if (gameLogic == null) gameLogic = new GameLogic();

    gameLogic.initializeGame(levelNumber); // GameLogic内部应重置棋盘、步数、历史等
    this.currentLevel = levelNumber;
    this.selectedBlock = null; // 清除任何先前选中的块
    this.elapsedTime = 0;
    this.startTime = System.currentTimeMillis(); // 重置计时器起始时间

    updateBoard(); // 更新UI以显示新关卡
    startTimer();  // 启动计时器

    // 确保游戏音乐播放
    if (this.GameBGM != null) {
      this.GameBGM.setCycleCount(MediaPlayer.INDEFINITE);
      SoundManager.playSound(this.GameBGM);
    }

    if (!userManager.isGuest() && gameFileManager != null) {
      gameFileManager.startAutoSave(userManager.getCurrentUser(), () -> {
        if (gameLogic == null || gameLogic.getBoard() == null) return null;
        GameState autoSaveState = new GameState(
                gameLogic.getBoard().copy(),
                userManager.getCurrentUser().getUsername(),
                currentLevel,
                new ArrayDeque<>(gameLogic.getMoveHistory()),
                gameLogic.isGameWon()
        );
        long timeToSave = (timer != null && startTime != 0) ? (System.currentTimeMillis() - this.startTime) : this.elapsedTime;
        autoSaveState.setTimeElapsed(timeToSave);
        return autoSaveState;
      });
    }
  }

  private void loadGame(GameState state) {
    if (state == null) {
      showAlert("加载错误", "无法加载游戏：游戏状态为空。");
      return;
    }
    if (gameLogic == null) gameLogic = new GameLogic();

    gameLogic.setBoard(state.getBoard());
    gameLogic.setMoveHistory(new ArrayDeque<>(state.getMoveHistory())); // 确保是新的Deque实例
    gameLogic.setIsGameWon(state.isGameWon());

    this.currentLevel = state.getCurrentLevel();
    this.selectedBlock = null;
    this.elapsedTime = state.getTimeElapsed();
    this.startTime = System.currentTimeMillis() - this.elapsedTime; // 恢复计时器状态

    updateBoard(); // 更新UI

    if (state.isGameWon()) {
      showVictoryScene(); // 如果加载的游戏已经胜利，直接显示胜利场景
      if (gameFileManager != null) gameFileManager.stopAutoSave();
    } else {
      startTimer(); // 只有未胜利的游戏才启动计时器
      // 确保游戏音乐播放
      if (this.GameBGM != null) {
        this.GameBGM.setCycleCount(MediaPlayer.INDEFINITE);
        SoundManager.playSound(this.GameBGM);
      }
      if (!userManager.isGuest() && gameFileManager != null) {
        gameFileManager.startAutoSave(userManager.getCurrentUser(), () -> {
          if (gameLogic == null || gameLogic.getBoard() == null) return null;
          GameState autoSaveState = new GameState(
                  gameLogic.getBoard().copy(),
                  userManager.getCurrentUser().getUsername(),
                  currentLevel,
                  new ArrayDeque<>(gameLogic.getMoveHistory()),
                  gameLogic.isGameWon()
          );
          long currentElapsedTimeVal = (timer != null && startTime != 0) ? (System.currentTimeMillis() - this.startTime) : this.elapsedTime;
          autoSaveState.setTimeElapsed(currentElapsedTimeVal);
          return autoSaveState;
        });
      }
    }
  }
  private void saveGame() {
    if (userManager.isGuest()) {
      showAlert("保存错误", "游客无法保存游戏。");
      return;
    }
    if (gameLogic == null || gameLogic.getBoard() == null) {
      showAlert("保存错误", "没有游戏可供保存。");
      return;
    }
    if (autoSolver != null && autoSolver.isAutoSolving()) {
      showAlert("保存游戏", "正在自动求解时无法保存游戏。");
      return;
    }

    // 确保elapsedTime在保存时是最新的
    if (timer != null && startTime != 0) { // 如果计时器在运行 (游戏未胜利)
      this.elapsedTime = System.currentTimeMillis() - this.startTime;
    }
    // 如果游戏已胜利，elapsedTime应已在showVictoryScene或moveSelected中设置并停止计时器

    GameState stateToSave = new GameState(
            gameLogic.getBoard().copy(), // 保存棋盘的副本
            userManager.getCurrentUser().getUsername(),
            currentLevel,
            new ArrayDeque<>(gameLogic.getMoveHistory()), // 保存移动历史的副本
            gameLogic.isGameWon()
    );
    stateToSave.setTimeElapsed(this.elapsedTime); // 设置准确的已用时间

    if (gameFileManager.saveGame(userManager.getCurrentUser().getUsername(), stateToSave)) {
      if (!gameLogic.isGameWon()){ // 只有非胜利状态下的手动保存才提示
        showAlert("保存成功", "游戏已成功保存。");
      } else {
        System.out.println("游戏胜利状态已自动保存。");
      }
    } else {
      showAlert("保存错误", "保存游戏失败。");
    }
  }

  private void getAndApplyHint() {
    if (gameLogic == null || gameLogic.isGameWon() || (solverProgress != null && solverProgress.isVisible()) || (autoSolver != null && autoSolver.isAutoSolving())) {
      return;
    }
    if (solverProgress!=null) solverProgress.setVisible(true);
    if (hintButton!=null) hintButton.setDisable(true);

    Task<Board> solverTask = new Task<>() {
      @Override
      protected Board call() throws Exception {
        return gameLogic.getHint(); // getHint应该返回下一步的Board状态
      }
    };

    solverTask.setOnSucceeded(event -> {
      if (solverProgress!=null) solverProgress.setVisible(false);
      if (hintButton!=null) hintButton.setDisable(false);

      Board nextBoardState = solverTask.getValue();
      if (nextBoardState != null) {
        gameLogic.applyHintState(nextBoardState); // GameLogic应该负责更新其内部棋盘和历史
        // 并确保步数已在nextBoardState中更新
        updateBoard(); // 更新UI

        if (gameLogic.isGameWon()) {
          saveGame(); // 先保存
          showVictoryScene();
        } else {
          // 使用提示后也保存游戏
          if (!userManager.isGuest()) {
            saveGame();
          }
        }
      } else {
        showAlert("提示", "当前状态无解或已是最终状态。");
      }
    });

    solverTask.setOnFailed(event -> {
      if (solverProgress!=null) solverProgress.setVisible(false);
      if (hintButton!=null) hintButton.setDisable(false);
      showAlert("提示错误", "求解下一步失败：" + solverTask.getException().getMessage());
      solverTask.getException().printStackTrace();
    });
    new Thread(solverTask).start();
  }

  @Override
  public void stop() { // JavaFX Application stop method
    System.out.println("KlotskiApp is stopping...");
    if (gameFileManager != null) {
      gameFileManager.shutdown(); // 关闭自动保存的调度器
    }
    if (timer != null) {
      timer.stop(); // 停止游戏计时器
    }
    // 停止所有背景音乐和音效
    SoundManager.stopSound(LoginBGM);
    SoundManager.stopSound(GameBGM);
    SoundManager.stopSound(VictoryBGM);
    if (moveSoundPlayer != null) SoundManager.stopSound(moveSoundPlayer); // moveSoundPlayer不是循环的，但以防万一
  }

  private void startTimer() {
    stopTimer(); // 先停止任何可能正在运行的计时器
    // startTime 应该反映游戏段的开始，elapsedTime是该段之前累计的时间
    // 所以，当继续计时，新的startTime = 当前时间 - 已累计时间
    this.startTime = System.currentTimeMillis() - this.elapsedTime;

    this.timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
      if (gameLogic != null && !gameLogic.isGameWon()) { // 只有游戏未胜利时才更新时间
        this.elapsedTime = System.currentTimeMillis() - this.startTime;
        if (this.timeLabel != null &&
                this.timeLabel.getScene() != null && // 确保标签在场景中
                this.primaryStage.getScene() == this.timeLabel.getScene()) {
          this.timeLabel.setText("Time: " + formatTime(this.elapsedTime));
        }
      } else {
        stopTimer(); // 如果游戏胜利了，确保计时器停止
      }
    }));
    this.timer.setCycleCount(Timeline.INDEFINITE);
    this.timer.play();
  }

  private void stopTimer() {
    if (this.timer != null) {
      this.timer.stop();
      // elapsedTime 此时的值就是计时器停止时的总流逝时间
    }
  }

  private String formatTime(long millis) {
    long totalSeconds = millis / 1000;
    long minutes = totalSeconds / 60;
    long seconds = totalSeconds % 60;
    return String.format("%02d:%02d", minutes, seconds);
  }

  private void showAlert(String title, String message) {
    if (Platform.isFxApplicationThread()) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle(title);
      alert.setHeaderText(null);
      alert.setContentText(message);
      alert.showAndWait();
    } else {
      Platform.runLater(() -> {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
      });
    }
  }

  private void saveGameOnAutoSolveWin() {
    System.out.println("自动求解完成并胜利，尝试保存游戏...");
    if (userManager.isGuest()) {
      System.out.println("游客模式，自动求解胜利后不保存游戏。");
      return;
    }
    // elapsedTime 应该在 showVictoryScene -> stopTimer 中被正确设置
    // 或者在 autoSolver 中传递过来
    // 此处调用 saveGame 会使用当前的 elapsedTime
    saveGame(); // saveGame 内部会检查是否胜利并处理
  }


  public static void main(String[] args) {
    launch(args);
  }
}