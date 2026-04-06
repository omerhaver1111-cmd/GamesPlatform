package com.example.gamesplatform.screens;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gamesplatform.R;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;

public class SnakeGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_snake_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        FrameLayout gameContainer = findViewById(R.id.game_container);

        gameContainer.post(() -> {
            int width = gameContainer.getWidth();
            int height = gameContainer.getHeight();

            GameView gameView = new GameView(this, width, height);
            gameContainer.addView(gameView);
        });
    }
    public class GameView extends View {

        private int screenWidth, screenHeight;
        private Paint paint;

        private int gridSize = 50;

        private ArrayList<Point> snake = new ArrayList<>();
        private Point food;

        private int dirX = 1;
        private int dirY = 0;

        private Handler handler = new Handler();
        private Runnable runnable;

        private boolean gameOver = false;

        public GameView(Context context, int width, int height) {
            super(context);

            this.screenWidth = width;
            this.screenHeight = height;

            paint = new Paint();

            // נחש התחלתי
            snake.add(new Point(5,5));
            snake.add(new Point(4,5));
            snake.add(new Point(3,5));

            spawnFood();

            runnable = new Runnable() {
                @Override
                public void run() {
                    update();
                    invalidate();
                    handler.postDelayed(this, 150);
                }
            };

            handler.post(runnable);
        }

        private void spawnFood() {
            int x = (int)(Math.random() * (screenWidth / gridSize));
            int y = (int)(Math.random() * (screenHeight / gridSize));
            food = new Point(x, y);
        }

        private void update() {
            if(gameOver) return;

            Point head = snake.get(0);

            int newX = head.x + dirX;
            int newY = head.y + dirY;

            // קירות
            if(newX < 0 || newY < 0 ||
                    newX >= screenWidth / gridSize ||
                    newY >= screenHeight / gridSize){
                gameOver = true;
                return;
            }

            // עצמו
            for(Point p : snake){
                if(p.x == newX && p.y == newY){
                    gameOver = true;
                    return;
                }
            }

            snake.add(0, new Point(newX, newY));

            // אוכל
            if(newX == food.x && newY == food.y){
                spawnFood();
            } else {
                snake.remove(snake.size() - 1);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            canvas.drawColor(Color.BLACK);

            // אוכל
            paint.setColor(Color.RED);
            canvas.drawRect(
                    food.x * gridSize,
                    food.y * gridSize,
                    (food.x + 1) * gridSize,
                    (food.y + 1) * gridSize,
                    paint
            );

            // נחש
            paint.setColor(Color.GREEN);
            for(Point p : snake){
                canvas.drawRect(
                        p.x * gridSize,
                        p.y * gridSize,
                        (p.x + 1) * gridSize,
                        (p.y + 1) * gridSize,
                        paint
                );
            }

            // Game Over
            if(gameOver){
                paint.setColor(Color.WHITE);
                paint.setTextSize(80);
                canvas.drawText("GAME OVER", screenWidth/4f, screenHeight/2f, paint);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    return true;

                case MotionEvent.ACTION_UP:
                    float x = event.getX();
                    float y = event.getY();

                    Point head = snake.get(0);

                    float centerX = screenWidth / 2f;
                    float centerY = screenHeight / 2f;

                    if(Math.abs(x - centerX) > Math.abs(y - centerY)){
                        if(x > centerX){
                            dirX = 1; dirY = 0;
                        } else {
                            dirX = -1; dirY = 0;
                        }
                    } else {
                        if(y > centerY){
                            dirX = 0; dirY = 1;
                        } else {
                            dirX = 0; dirY = -1;
                        }
                    }
                    return true;
            }

            return super.onTouchEvent(event);
        }
    }
}