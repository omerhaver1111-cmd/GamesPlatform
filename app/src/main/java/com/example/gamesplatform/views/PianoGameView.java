package com.example.gamesplatform.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;

import com.example.gamesplatform.models.PianoRow;
import com.example.gamesplatform.services.AudioService;

import java.util.ArrayList;
import java.util.Random;

public class PianoGameView extends View {

    private ArrayList<PianoRow> rows = new ArrayList<>();
    private Random random = new Random();

    private Paint blackPaint, whitePaint, bluePaint, borderPaint, textPaint;

    private int score = 0;
    private int record = 0;

    private long timeLeftInMillis = 60000;
    private boolean isGameOver = false;
    private boolean showGameOverOverlay = false;

    private AudioService audioService;
    private CountDownTimer gameTimer;

    // ✅ listener
    private GameOverListener gameOverListener;

    public interface GameOverListener {
        void onGameOver(int score);
    }

    public void setGameOverListener(GameOverListener listener) {
        this.gameOverListener = listener;
    }

    public PianoGameView(Context context, AudioService service) {
        super(context);
        this.audioService = service;
        initPaints();
        startGame();
    }

    private void initPaints() {
        blackPaint = new Paint();
        blackPaint.setColor(Color.BLACK);

        whitePaint = new Paint();
        whitePaint.setColor(Color.WHITE);

        bluePaint = new Paint();
        bluePaint.setColor(Color.BLUE);

        borderPaint = new Paint();
        borderPaint.setColor(Color.GRAY);
        borderPaint.setStrokeWidth(3);
        borderPaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(60);
        textPaint.setFakeBoldText(true);
    }

    public void startGame() {
        rows.clear();
        score = 0;
        isGameOver = false;
        showGameOverOverlay = false;
        timeLeftInMillis = 60000;

        for (int i = 0; i < 5; i++) {
            rows.add(generateRandomRow());
        }

        startNewTimer(timeLeftInMillis);
        invalidate();
    }

    private PianoRow generateRandomRow() {
        int index = random.nextInt(4);
        boolean isBonus = random.nextInt(80) == 0;
        return new PianoRow(index, isBonus);
    }

    private void startNewTimer(long millis) {
        if (gameTimer != null) {
            gameTimer.cancel();
        }

        gameTimer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                invalidate();
            }

            @Override
            public void onFinish() {
                triggerGameOver();
            }
        }.start();
    }

    private void triggerGameOver() {
        if (isGameOver) return;

        isGameOver = true;

        if (gameTimer != null) {
            gameTimer.cancel();
        }

        if (score > record) {
            record = score;
        }

        showGameOverOverlay = true;

        // ✅ קריאה ל-Activity
        if (gameOverListener != null) {
            gameOverListener.onGameOver(score);
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int rowCount = 6;
        int tileWidth = getWidth() / 4;
        int tileHeight = getHeight() / rowCount;

        float textY = tileHeight / 1.5f;

        canvas.drawText("Score: " + score, 50, textY, textPaint);

        long seconds = timeLeftInMillis / 1000;
        canvas.drawText("Time: " + seconds + "s", getWidth() - 350, textY, textPaint);

        for (int i = 0; i < rows.size(); i++) {
            PianoRow row = rows.get(i);

            int currentRowTop = (i + 1) * tileHeight;

            for (int j = 0; j < 4; j++) {

                Paint p = (j == row.correctIndex)
                        ? (row.isBonus ? bluePaint : blackPaint)
                        : whitePaint;

                Rect rect = new Rect(
                        j * tileWidth,
                        currentRowTop,
                        (j + 1) * tileWidth,
                        currentRowTop + tileHeight
                );

                canvas.drawRect(rect, p);
                canvas.drawRect(rect, borderPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            int tileHeight = getHeight() / 6;
            int rowClicked = (int) (event.getY() / tileHeight);

            if (rowClicked == 5 && !rows.isEmpty()) {

                int column = (int) (event.getX() / (getWidth() / 4));
                PianoRow bottomRow = rows.get(rows.size() - 1);

                if (column == bottomRow.correctIndex) {

                    if (audioService != null) {
                        audioService.playPianoNote();
                    }

                    score++;

                    if (bottomRow.isBonus) {
                        timeLeftInMillis += 5000;
                        startNewTimer(timeLeftInMillis);
                    }

                    rows.remove(bottomRow);
                    rows.add(0, generateRandomRow());

                } else {
                    triggerGameOver();
                }

                invalidate();
            }
        }

        return true;
    }
}