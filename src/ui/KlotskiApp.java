// KlotskiApp.java
package ui;
import game.Direction;
import game.GameLogic;
import javafx.scene.input.KeyEvent;
import model.Block;
import model.Board;
import model.GameState;
import ui.controls.WavePasswordConfirm;
import user.UserManager;
import game.LevelManager; // Import LevelManager
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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

    // UI components
    private GridPane boardGrid;
    private Label moveCountLabel;
    private Label timeLabel;

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
        VBox loginBox = new VBox(15);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(20));
        loginBox.setStyle("-fx-background-color: #f0f0f0;");

        Text title = new Text("Klotski Puzzle");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Button guestButton = new Button("Play as Guest");
        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");

        guestButton.setMinWidth(200);
        loginButton.setMinWidth(200);
        registerButton.setMinWidth(200);

        guestButton.setOnAction(e -> {
            userManager.loginAsGuest();
            showMainMenu();
        });

        loginButton.setOnAction(e -> showLoginForm());
        registerButton.setOnAction(e -> showRegisterForm());

        loginBox.getChildren().addAll(title, guestButton, loginButton, registerButton);

        Scene scene = new Scene(loginBox, 400, 300);
        scene.getStylesheets().add(getClass().getResource("/css/wave.css").toExternalForm());
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
        levelComboBox.getItems().addAll("Classic", "Advanced", "Expert"); // Add your actual level names
        levelComboBox.getSelectionModel().selectFirst(); // Select the first level by default
        // --- End Level Selection UI ---


        newGameButton.setOnAction(e -> {
            // Get selected level name and find the corresponding level number
            String selectedLevelName = levelComboBox.getSelectionModel().getSelectedItem();
            int levelNumber = -1; // Determine level number based on name or index
            // A better approach is to store levels with their numbers or directly use Level objects in ComboBox

            // For this example, let's use the selected index + 1 as the level number
            levelNumber = levelComboBox.getSelectionModel().getSelectedIndex() + 1;


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

        controlPanel.getChildren().addAll(
                upButton, downButton, leftButton, rightButton,
                undoButton, saveButton, menuButton);
        root.setBottom(controlPanel);

        /* ---------- 防止按钮抢键盘 ---------- */
        for (Button b : List.of(upButton, downButton, leftButton, rightButton,
                undoButton, saveButton, menuButton)) {
            b.setFocusTraversable(false);
        }

        /* ---------- 创建场景并注册键盘事件 ---------- */
        Scene scene = new Scene(root, 600, 700);
        scene.getStylesheets().add(getClass().getResource("/css/wave.css").toExternalForm());
        // 事件过滤器：始终能收到键盘（不受焦点限制）
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case UP    -> moveSelected(Direction.UP);
                case DOWN  -> moveSelected(Direction.DOWN);
                case LEFT  -> moveSelected(Direction.LEFT);
                case RIGHT -> moveSelected(Direction.RIGHT);
                case Z     -> { if (e.isControlDown()) undo(); }
                case S     -> { if (e.isControlDown()) saveGame(); }
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

        for (int i = 0; i < boardGrid.getChildren().size(); i++) {
            if (boardGrid.getChildren().get(i) instanceof Rectangle) {
                Rectangle rect = (Rectangle) boardGrid.getChildren().get(i);

                // Skip the empty cells
                if (rect.getWidth() < 70) { // Assuming cells are exactly 70 wide
                    continue;
                }

                // Get the block associated with this rectangle
                // Need to find the block at the rectangle's grid position
                Integer col = GridPane.getColumnIndex(rect);
                Integer row = GridPane.getRowIndex(rect);

                if (col != null && row != null) {
                    Block blockAtPos = gameLogic.getBoard().getBlockAt(col, row);

                    if (blockAtPos == selectedBlock) {
                        rect.setStroke(Color.WHITE);
                        rect.setStrokeWidth(3);
                    } else {
                        // Reset stroke for non-selected blocks that are actual blocks
                        if (blockAtPos != null && blockAtPos.getWidth() > 0 && blockAtPos.getHeight() > 0) {
                            rect.setStroke(Color.BLACK);
                            rect.setStrokeWidth(2);
                        }
                    }
                }
            }
        }
    }

    // Move the selected block
    private void moveSelected(Direction direction) {
        if (selectedBlock != null && gameLogic != null) { // Add null check for gameLogic
            if (gameLogic.moveBlock(selectedBlock, direction)) {
                updateBoard();

                if (gameLogic.isGameWon()) {
                    showVictoryScene();
                }
                GameState gameState = new GameState(
                        gameLogic.getBoard(),
                        userManager.getCurrentUser().getUsername(),
                        currentLevel,
                        gameLogic.getMoveHistory(),
                        gameLogic.isGameWon()
                );
                gameState.setTimeElapsed(System.currentTimeMillis() - startTime);  // 更新时间

                gameFileManager.saveGame(userManager.getCurrentUser().getUsername(), gameState);  // 手动保存
                // Update move count display after a successful move
                moveCountLabel.setText("Moves: " + gameLogic.getBoard().getMoveCount());
            }
        } else if (gameLogic != null && gameLogic.getBoard() != null) { // Add null checks
            // If no block is selected, try to find one at the center
            Block centerBlock = gameLogic.getBoard().getBlockAt(
                    gameLogic.getBoard().getCols() / 2,
                    gameLogic.getBoard().getRows() / 2
            );

            if (centerBlock != null) {
                selectedBlock = centerBlock;
                updateSelectedBlockHighlight();
            }
        }
    }

    // Undo last move
    private void undo() {
        if (gameLogic != null && gameLogic.undoMove()) { // Add null check for gameLogic
            updateBoard();
            // Update move count display after undo
            moveCountLabel.setText("Moves: " + gameLogic.getBoard().getMoveCount());
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
            Deque<Board> history = gameLogic.getMoveHistory();
            history.clear();                  // 清空旧记录
            history.push(newBoard.copy());    // ★ 压入初始状态

            /* ---------- 其余 UI/计时器处理 ---------- */
            currentLevel = levelNumber;
            selectedBlock = null;
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
                    return new GameState(
                            gameLogic.getBoard(),
                            userManager.getCurrentUser().getUsername(),
                            currentLevel,
                            gameLogic.getMoveHistory(),
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

        gameLogic.setBoard(state.getBoard()); // 设置棋盘状态
        // Ensure GameLogic has setMoveHistory and setIsGameWon methods
        if (state.getMoveHistory() != null) {
            gameLogic.setMoveHistory(state.getMoveHistory()); // 设置移动历史
        } else {
            // If move history was not saved, initialize a new one
            gameLogic.setMoveHistory(new Deque<Board>() {
                @Override
                public void addFirst(Board board) {

                }

                @Override
                public void addLast(Board board) {

                }

                @Override
                public boolean offerFirst(Board board) {
                    return false;
                }

                @Override
                public boolean offerLast(Board board) {
                    return false;
                }

                @Override
                public Board removeFirst() {
                    return null;
                }

                @Override
                public Board removeLast() {
                    return null;
                }

                @Override
                public Board pollFirst() {
                    return null;
                }

                @Override
                public Board pollLast() {
                    return null;
                }

                @Override
                public Board getFirst() {
                    return null;
                }

                @Override
                public Board getLast() {
                    return null;
                }

                @Override
                public Board peekFirst() {
                    return null;
                }

                @Override
                public Board peekLast() {
                    return null;
                }

                @Override
                public boolean removeFirstOccurrence(Object o) {
                    return false;
                }

                @Override
                public boolean removeLastOccurrence(Object o) {
                    return false;
                }

                @Override
                public boolean add(Board board) {
                    return false;
                }

                @Override
                public boolean offer(Board board) {
                    return false;
                }

                @Override
                public Board remove() {
                    return null;
                }

                @Override
                public Board poll() {
                    return null;
                }

                @Override
                public Board element() {
                    return null;
                }

                @Override
                public Board peek() {
                    return null;
                }

                @Override
                public boolean addAll(Collection<? extends Board> c) {
                    return false;
                }

                @Override
                public boolean removeAll(Collection<?> c) {
                    return false;
                }

                @Override
                public boolean retainAll(Collection<?> c) {
                    return false;
                }

                @Override
                public void clear() {

                }

                @Override
                public void push(Board board) {

                }

                @Override
                public Board pop() {
                    return null;
                }

                @Override
                public boolean remove(Object o) {
                    return false;
                }

                @Override
                public boolean containsAll(Collection<?> c) {
                    return false;
                }

                @Override
                public boolean contains(Object o) {
                    return false;
                }

                @Override
                public int size() {
                    return 0;
                }

                @Override
                public boolean isEmpty() {
                    return false;
                }

                @Override
                public Iterator<Board> iterator() {
                    return null;
                }

                @Override
                public Object[] toArray() {
                    return new Object[0];
                }

                @Override
                public <T> T[] toArray(T[] a) {
                    return null;
                }

                @Override
                public Iterator<Board> descendingIterator() {
                    return null;
                }
            });
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
        if (!userManager.isGuest()) {
            gameFileManager.startAutoSave(userManager.getCurrentUser(), () -> {
                return new GameState(
                        gameLogic.getBoard(),
                        userManager.getCurrentUser().getUsername(),
                        currentLevel,
                        gameLogic.getMoveHistory(),
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
                gameLogic.getBoard(),
                userManager.getCurrentUser().getUsername(),
                currentLevel, // Save current level
                gameLogic.getMoveHistory(), // 包含移动历史
                gameLogic.isGameWon() // 包含游戏胜利状态
        );
        state.setTimeElapsed(elapsedTime); // 设置已经过去的时间

        if (gameFileManager.saveGame(userManager.getCurrentUser().getUsername(), state)) {
            showAlert("Save Success", "Game saved successfully.");
        } else {
            showAlert("Save Error", "Failed to save game.");
        }
    }

    // Start the game timer

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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Main method
    public static void main(String[] args) {
        launch(args);
    }
}