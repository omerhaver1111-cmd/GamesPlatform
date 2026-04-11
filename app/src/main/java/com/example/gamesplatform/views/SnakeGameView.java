package com.example.gamesplatform.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class SnakeGameView extends View {

    public interface SnakeGameListener {
        void onScoreUpdated(int score);
        void onGameOver(int score);
    }

    private SnakeGameListener listener;
    private int width, height;
    private int grid = 40;

    private ArrayList<Point> snake = new ArrayList<>();
    private Point food;
    private int dx = 1, dy = 0;

    private Handler handler = new Handler();
    private boolean gameOver = false;
    private int score = 0;
    private int speed = 150;

    private Paint paint = new Paint();
    private Random random = new Random();

    public SnakeGameView(Context context, int w, int h, SnakeGameListener listener) {
        super(context);
        this.width = w;
        this.height = h;
        this.listener = listener;

        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();

        /// אתחול נחש
        snake.add(new Point(5, 5));
        snake.add(new Point(4, 5));

        spawnFood();
        handler.post(loop);
    }

    private void spawnFood() {
        int gridW = width / grid;
        int gridH = height / grid;
        if (gridW > 0 && gridH > 0) {
            food = new Point(random.nextInt(gridW), random.nextInt(gridH));
        }
    }

    private Runnable loop = new Runnable() {
        @Override
        public void run() {
            if (!gameOver) {
                update();
                invalidate();
                handler.postDelayed(this, speed);
            }
        }
    };

    public void setDirection(int x, int y) {
        if (x == -dx && y == -dy) return; /// מניעת פנייה של 180 מעלות על עצמו
        dx = x;
        dy = y;
    }

    private void update() {
        if (gameOver) return;

        Point head = snake.get(0);
        int nx = head.x + dx;
        int ny = head.y + dy;

        /// בדיקת התנגשות בקירות
        if (nx < 0 || ny < 0 || nx >= width / grid || ny >= height / grid) {
            endGame();
            return;
        }

        /// בדיקת התנגשות בעצמו
        for (Point p : snake) {
            if (p.x == nx && p.y == ny) {
                endGame();
                return;
            }
        }

        snake.add(0, new Point(nx, ny));

        /// בדיקת אכילת אוכל
        if (food != null && nx == food.x && ny == food.y) {
            score++;
            if (listener != null) listener.onScoreUpdated(score);
            if (speed > 60) speed -= 5;
            spawnFood();
        } else {
            snake.remove(snake.size() - 1);
        }
    }

    private void endGame() {
        gameOver = true;
        handler.removeCallbacks(loop);
        if (listener != null) listener.onGameOver(score);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.argb(30, 0, 0, 0));

        /// ציור אוכל
        if (food != null) {
            paint.setColor(Color.RED);
            canvas.drawRect(food.x * grid, food.y * grid,
                    (food.x + 1) * grid, (food.y + 1) * grid, paint);
        }

        /// ציור נחש
        paint.setColor(Color.GREEN);
        for (Point p : snake) {
            canvas.drawRect(p.x * grid, p.y * grid,
                    (p.x + 1) * grid, (p.y + 1) * grid, paint);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP: setDirection(0, -1); return true;
            case KeyEvent.KEYCODE_DPAD_DOWN: setDirection(0, 1); return true;
            case KeyEvent.KEYCODE_DPAD_LEFT: setDirection(-1, 0); return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT: setDirection(1, 0); return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}