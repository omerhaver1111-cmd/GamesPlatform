package com.example.gamesplatform.screens;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gamesplatform.services.AudioService;

import java.util.ArrayList;
import java.util.Random;

public class PianoActivity extends AppCompatActivity {

    private AudioService audioService;
    private boolean isBound = false;
    private FrameLayout gameContainer;
    private GameView gameView;

    // ניהול החיבור לשירות האודיו
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioService.AudioBinder binder = (AudioService.AudioBinder) service;
            audioService = binder.getService();
            isBound = true;

            // רק לאחר שהשירות מחובר, נציג את המשחק
            setupGame();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // יצירת קונטיינר ראשי
        gameContainer = new FrameLayout(this);
        gameContainer.setBackgroundColor(Color.WHITE);
        setContentView(gameContainer);

        // חיבור לשירות (Bind)
        Intent intent = new Intent(this, AudioService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    private void setupGame() {
        gameView = new GameView(this);
        gameContainer.addView(gameView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }

    // --- מחלקת המשחק הפנימית ---
    class GameView extends View {

        class Row {
            int correctIndex;
            Row(int correctIndex) { this.correctIndex = correctIndex; }
        }

        private ArrayList<Row> rows = new ArrayList<>();
        private Random random = new Random();
        private Paint blackPaint, whitePaint, borderPaint, textPaint;
        private int score = 0;
        private boolean isGameOver = false;

        private Handler handler = new Handler();
        private Runnable timerRunnable;
        private int timeLimit = 1500;

        public GameView(Context context) {
            super(context);
            initPaints();
            startGame();
        }

        private void initPaints() {
            blackPaint = new Paint();
            blackPaint.setColor(Color.BLACK);

            whitePaint = new Paint();
            whitePaint.setColor(Color.WHITE);

            borderPaint = new Paint();
            borderPaint.setColor(Color.GRAY);
            borderPaint.setStrokeWidth(3);
            borderPaint.setStyle(Paint.Style.STROKE);

            textPaint = new Paint();
            textPaint.setColor(Color.RED);
            textPaint.setTextSize(80);
            textPaint.setFakeBoldText(true);
        }

        private void startGame() {
            rows.clear();
            score = 0;
            isGameOver = false;
            for (int i = 0; i < 6; i++) {
                rows.add(new Row(random.nextInt(4)));
            }
            startTimer();
        }

        private void startTimer() {
            handler.removeCallbacks(timerRunnable);
            timerRunnable = () -> {
                if (!isGameOver) {
                    isGameOver = true;
                    invalidate();
                    Toast.makeText(getContext(), "Too slow!", Toast.LENGTH_SHORT).show();
                }
            };
            handler.postDelayed(timerRunnable, timeLimit);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (rows.isEmpty()) return;

            int tileWidth = getWidth() / 4;
            int tileHeight = getHeight() / rows.size();

            // ציור הקלידים
            for (int i = 0; i < rows.size(); i++) {
                Row row = rows.get(i);
                for (int j = 0; j < 4; j++) {
                    Paint paint = (j == row.correctIndex) ? blackPaint : whitePaint;

                    Rect rect = new Rect(
                            j * tileWidth,
                            i * tileHeight,
                            (j + 1) * tileWidth,
                            (i + 1) * tileHeight
                    );

                    canvas.drawRect(rect, paint);
                    canvas.drawRect(rect, borderPaint);
                }
            }

            // ציור הניקוד
            canvas.drawText("Score: " + score, 50, 120, textPaint);

            // מסך Game Over
            if (isGameOver) {
                canvas.drawColor(Color.argb(150, 0, 0, 0)); // רקע חצי שקוף
                textPaint.setColor(Color.WHITE);
                canvas.drawText("GAME OVER", getWidth() / 6f, getHeight() / 2f, textPaint);
                textPaint.setColor(Color.RED); // החזרה לצבע מקורי לציור הבא
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (isGameOver) {
                // לחיצה לאחר הפסד תתחיל משחק חדש
                if (event.getAction() == MotionEvent.ACTION_DOWN) startGame();
                return true;
            }

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                int column = (int) (event.getX() / (getWidth() / 4));
                int bottomIndex = rows.size() - 1;
                Row bottomRow = rows.get(bottomIndex);

                if (column == bottomRow.correctIndex) {
                    // הצלחה - השמעת צליל דרך השירות
                    if (isBound && audioService != null) {
                        audioService.playPianoNote();
                    }

                    score++;
                    if (timeLimit > 500) timeLimit -= 10; // האצת המשחק ככל שמתקדמים

                    rows.remove(bottomIndex);
                    rows.add(0, new Row(random.nextInt(4)));

                    startTimer(); // איפוס הטיימר
                } else {
                    // טעות בקליד
                    isGameOver = true;
                    handler.removeCallbacks(timerRunnable);
                }
                invalidate();
            }
            return true;
        }
    }
}