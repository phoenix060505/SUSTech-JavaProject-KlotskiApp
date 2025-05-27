package game;

import model.Board;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.util.Duration;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

public class AutoSolve {
    private static final double AUTO_SOLVE_STEP_DELAY_SECONDS = 0.001;
    private boolean isAutoSolving = false;
    private GameLogic gameLogic;
    private Button autoSolveButton;
    private Button hintButton;
    private ProgressIndicator solverProgress;
    private  Runnable onUpdateBoard;
    private  Runnable onShowVictory;
    private  BiConsumer<String,String> onShowAlert;
    private  LongSupplier getElapsedTime;
    private  Runnable onSaveGame;
    private Runnable onAutoSolveCompletedAndWon; // 新增：胜利并完成后调用的保存回调

    public AutoSolve(GameLogic gameLogic,
                     Button autoSolveButton,
                     Button hintButton,
                     ProgressIndicator solverProgress,
                     Runnable onUpdateBoard,
                     Runnable onShowVictory,
                     BiConsumer<String, String> onShowAlert,
                     Runnable onAutoSolveCompletedAndWon, // 新增参数
                     LongSupplier getElapsedTime) {
        this.gameLogic = gameLogic;
        this.autoSolveButton = autoSolveButton;
        this.hintButton = hintButton;
        this.solverProgress = solverProgress;
        this.onUpdateBoard = onUpdateBoard;
        this.onShowVictory = onShowVictory;
        this.getElapsedTime = getElapsedTime;
        this.onShowAlert = onShowAlert;
        this.onAutoSolveCompletedAndWon = onAutoSolveCompletedAndWon; // 新增赋值

    }

    public void toggleAutoSolve() {
        if (isAutoSolving) {
            stopAutoSolving();
        } else {
            if (gameLogic == null || gameLogic.getBoard() == null) {
                onShowAlert.accept("Auto-Solve" ,"Please start or load a game first.");
                return;
            }
            if (gameLogic.isGameWon()) {
                onShowAlert.accept("Auto-Solve", "The game is already won!");
                return;
            }
            startAutoSolving();

        }
    }

    private void startAutoSolving() {
        isAutoSolving = true;
        autoSolveButton.setText("Stop");
        if (hintButton != null) {
            hintButton.setDisable(true);
        }
        if (solverProgress != null) {
            solverProgress.setVisible(true);
        }
        performAutoSolveStep();
    }

    private void stopAutoSolving() {
        isAutoSolving = false;
        if (autoSolveButton != null) {
            autoSolveButton.setText("Auto-Solve");
        }
        if (hintButton != null && gameLogic != null) {
            hintButton.setDisable(gameLogic.isGameWon());
        }
        if (solverProgress != null) {
            solverProgress.setVisible(false);
        }
    }

    private void performAutoSolveStep() {
        if (!isAutoSolving || (gameLogic != null && gameLogic.isGameWon())) { // [vite: 16]
            // 如果在开始此步骤时游戏已经胜利 (可能是上一步导致的)，也应该停止并尝试保存
            if (gameLogic != null && gameLogic.isGameWon() && isAutoSolving) { // 确保是自动求解中赢得的
                // isAutoSolving is true here, will be set to false in stopAutoSolving
                boolean wasWinningDuringAutoSolve = isAutoSolving;
                onShowVictory.run(); // 显示胜利场景
                stopAutoSolving();   // 停止自动求解状态
                if (wasWinningDuringAutoSolve && onAutoSolveCompletedAndWon != null) {
                    onAutoSolveCompletedAndWon.run(); // 执行保存回调
                }
            } else {
                stopAutoSolving(); // [vite: 16]
            }
            return; // [vite: 16]
        }


        if (solverProgress != null) { // [vite: 16]
            solverProgress.setVisible(true); // [vite: 16]
        }

        Task<Board> solverTask = new Task<>() {
            @Override
            protected Board call() {
                if (gameLogic == null) return null;
                return gameLogic.getHint();
            }
        };

        solverTask.setOnSucceeded(event -> {
            Board nextBoard = solverTask.getValue();

            if (!isAutoSolving) {
                stopAutoSolving();
                return;
            }

            if (nextBoard != null) {
                gameLogic.setBoard(nextBoard.copy());

                Deque<Board> currentHistory = gameLogic.getMoveHistory();
                if (currentHistory == null) currentHistory = new ArrayDeque<>();
                currentHistory.push(gameLogic.getBoard().copy());
                gameLogic.setMoveHistory(currentHistory);
                onUpdateBoard.run();
                gameLogic.setMoveHistory(currentHistory);
                onUpdateBoard.run();
                if (gameLogic.isGameWon()) {
                    boolean wasWinningDuringAutoSolve = isAutoSolving; // 捕获当前状态
                    onShowVictory.run();
                    stopAutoSolving();   // 这会将 isAutoSolving 设为 false
                    if (wasWinningDuringAutoSolve && onAutoSolveCompletedAndWon != null) {
                        onAutoSolveCompletedAndWon.run(); // 调用保存回调
                    }
                } else if (isAutoSolving) {
                    PauseTransition pause = new PauseTransition(Duration.seconds(AUTO_SOLVE_STEP_DELAY_SECONDS));
                    pause.setOnFinished(e -> performAutoSolveStep());
                    pause.play();
                }
            } else {
                stopAutoSolving();
                if (gameLogic != null && !gameLogic.isGameWon()) {
                    onShowAlert.accept("Auto-Solve",
                            "No further hints available or the puzzle is unsolvable from this state.");
                }
            }
        });

        solverTask.setOnFailed(event -> {
            if (!isAutoSolving && solverTask.getException() instanceof InterruptedException) {
                System.out.println("Auto-solve task interrupted, possibly due to application shutdown.");
            } else {
                onShowAlert.accept("Auto-Solve Error",
                        "Solver failed: " + event.getSource().getException().getMessage());
                event.getSource().getException().printStackTrace();
            }
            stopAutoSolving();
        });

        new Thread(solverTask).start();
    }

    public boolean isAutoSolving() {
        return isAutoSolving;
    }

    public LongSupplier getGetElapsedTime() {
        return getElapsedTime;
    }
}