package com.example.gamesplatform.screens;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gamesplatform.R;
import com.example.gamesplatform.models.User;
import com.example.gamesplatform.services.DatabaseService;
import com.example.gamesplatform.utils.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.Random;

public class SnakeGameActivity extends AppCompatActivity {

    private FrameLayout game_container;
    private TextView score_text;

    private LinearLayout game_over_layout;
    private TextView final_score, record_text;
    private Button restart_btn, home_btn;

    private DatabaseService databaseService;
    private User currentUser;

    private GameView gameView;

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

        databaseService = DatabaseService.getInstance();
        loadUser();

        game_container = findViewById(R.id.game_container);
        score_text = findViewById(R.id.score_text);

        game_over_layout = findViewById(R.id.game_over_layout);
        final_score = findViewById(R.id.final_score);
        record_text = findViewById(R.id.record_text);

        restart_btn = findViewById(R.id.restart_btn);
        home_btn = findViewById(R.id.home_btn);

        game_over_layout.setVisibility(View.GONE);

        restart_btn.setOnClickListener(v -> recreate());
        home_btn.setOnClickListener(v -> finish());

        game_container.post(() -> {
            gameView = new GameView(this, game_container.getWidth(), game_container.getHeight());
            game_container.addView(gameView);
        });

        findViewById(R.id.btn_up).setOnClickListener(v -> gameView.setDirection(0, -1));
        findViewById(R.id.btn_down).setOnClickListener(v -> gameView.setDirection(0, 1));
        findViewById(R.id.btn_left).setOnClickListener(v -> gameView.setDirection(-1, 0));
        findViewById(R.id.btn_right).setOnClickListener(v -> gameView.setDirection(1, 0));
    }

    private void loadUser() {
        User saved = SharedPreferencesUtil.getUser(this);
        if (saved == null) return;

        databaseService.getUser(saved.getId(), new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                currentUser = user;
            }

            @Override
            public void onFailed(Exception e) {
            }
        });
    }

    public void showGameOver(int score) {

        runOnUiThread(() -> {

            game_over_layout.setVisibility(View.VISIBLE);

            int record = 0;

            if (currentUser != null) {
                record = currentUser.getSnakeRecord();
            }

            final_score.setText("Score: " + score);

            if (score > record) {
                record = score;
                record_text.setText("NEW RECORD: " + record);
            } else {
                record_text.setText("Record: " + record);
            }

            if (currentUser != null) {
                int finalRecord = record;
                databaseService.updateUser(currentUser.getId(), user -> {
                    if (user == null) return null;
                    user.setSnakeRecord(finalRecord);
                    user.setMoney(user.getMoney() + score);
                    user.setExp(user.getExp() + score);
                    return user;

                }, null);
            }
        });
    }


    public class GameView extends View {

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

        public GameView(Context context, int w, int h) {
            super(context);

            this.width = w;
            this.height = h;

            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus();

            snake.add(new Point(5, 5));
            snake.add(new Point(4, 5));

            spawnFood();
            handler.post(loop);
        }

        private void spawnFood() {
            food = new Point(random.nextInt(width / grid), random.nextInt(height / grid));
        }        private Runnable loop = new Runnable() {
            @Override
            public void run() {
                update();
                invalidate();
                handler.postDelayed(this, speed);
            }
        };

        public void setDirection(int x, int y) {
            if (x == -dx && y == -dy) return;
            dx = x;
            dy = y;
        }

        private void update() {
            if (gameOver) return;

            Point head = snake.get(0);
            int nx = head.x + dx;
            int ny = head.y + dy;

            if (nx < 0 || ny < 0 || nx >= width / grid || ny >= height / grid) {
                endGame();
                return;
            }

            for (Point p : snake) {
                if (p.x == nx && p.y == ny) {
                    endGame();
                    return;
                }
            }

            snake.add(0, new Point(nx, ny));

            if (nx == food.x && ny == food.y) {
                score++;
                score_text.setText("Score: " + score);

                if (speed > 60) speed -= 5;

                spawnFood();
            } else {
                snake.remove(snake.size() - 1);
            }
        }

        private void endGame() {
            gameOver = true;
            handler.removeCallbacks(loop);
            showGameOver(score);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.argb(30, 0, 0, 0));

            paint.setColor(Color.RED);
            canvas.drawRect(food.x * grid, food.y * grid,
                    (food.x + 1) * grid, (food.y + 1) * grid, paint);

            paint.setColor(Color.GREEN);
            for (Point p : snake) {
                canvas.drawRect(p.x * grid, p.y * grid,
                        (p.x + 1) * grid, (p.y + 1) * grid, paint);
            }
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    setDirection(0, -1);
                    return true;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    setDirection(0, 1);
                    return true;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    setDirection(-1, 0);
                    return true;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    setDirection(1, 0);
                    return true;
            }
            return super.onKeyDown(keyCode, event);
        }


    }
}