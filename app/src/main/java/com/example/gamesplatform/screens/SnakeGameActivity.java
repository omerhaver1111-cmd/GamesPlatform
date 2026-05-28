package com.example.gamesplatform.screens;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gamesplatform.R;
import com.example.gamesplatform.models.User;
import com.example.gamesplatform.services.DatabaseService;
import com.example.gamesplatform.utils.SharedPreferencesUtil;
import com.example.gamesplatform.views.SnakeGameView;

public class SnakeGameActivity extends AppCompatActivity implements SnakeGameView.SnakeGameListener {

    private FrameLayout game_container;
    private TextView score_text;
    private LinearLayout game_over_layout;
    private TextView final_score, record_text;
    private Button restart_btn, home_btn;

    private DatabaseService databaseService;
    private User currentUser;
    private SnakeGameView gameView;

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

        initUI();
        full_screen();
        databaseService = DatabaseService.getInstance();
        loadUser();

        game_container.post(() -> {
            gameView = new SnakeGameView(this, game_container.getWidth(), game_container.getHeight(), this);
            game_container.addView(gameView);
        });

        setupControls();
    }

    private void full_screen() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            androidx.core.view.WindowInsetsControllerCompat controller =
                    WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

            // הסתרת סרגלי המערכת
            controller.hide(WindowInsetsCompat.Type.systemBars());
            // הגדרה שהסרגלים יופיעו רק במשיכה קלה מהקצה וייעלמו שוב
            controller.setSystemBarsBehavior(androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        } else {
            // תמיכה בגרסאות אנדרואיד ישנות
            getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private void initUI() {
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
    }

    private void setupControls() {
        findViewById(R.id.btn_up).setOnClickListener(v -> { if(gameView != null) gameView.setDirection(0, -1); });
        findViewById(R.id.btn_down).setOnClickListener(v -> { if(gameView != null) gameView.setDirection(0, 1); });
        findViewById(R.id.btn_left).setOnClickListener(v -> { if(gameView != null) gameView.setDirection(-1, 0); });
        findViewById(R.id.btn_right).setOnClickListener(v -> { if(gameView != null) gameView.setDirection(1, 0); });
    }

    private void loadUser() {
        User saved = SharedPreferencesUtil.getUser(this);
        if (saved == null) return;
        databaseService.getUser(saved.getId(), new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) { currentUser = user; }
            @Override public void onFailed(Exception e) {}
        });
    }

    @Override
    public void onScoreUpdated(int score) {
        runOnUiThread(() -> score_text.setText("Score: " + score));
    }

    @Override
    public void onGameOver(int score) {
        runOnUiThread(() -> showGameOverUI(score));
    }

    private void showGameOverUI(int score) {
        game_over_layout.setVisibility(View.VISIBLE);
        int record = (currentUser != null) ? currentUser.getSnakeRecord() : 0;
        final_score.setText("Score: " + score);

        if (score > record) {
            record = score;
            record_text.setText("NEW RECORD: " + record);
        } else {
            record_text.setText("Record: " + record);
        }

        if (currentUser != null) {
            final int finalRecord = record;
            databaseService.updateUser(currentUser.getId(), user -> {
                if (user == null) return null;
                user.setSnakeRecord(finalRecord);
                user.setMoney(user.getMoney() + score);
                user.setExp(user.getExp() + score*2);
                return user;
            }, null);
        }
    }
}