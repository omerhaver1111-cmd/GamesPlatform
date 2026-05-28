package com.example.gamesplatform.screens;

import android.graphics.Color;
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
import com.example.gamesplatform.views.CarEscapeGameView;

public class CarEscapeActivity extends AppCompatActivity implements CarEscapeGameView.GameCallback {

    private TextView timer_text_view;
    private FrameLayout game_container;
    private LinearLayout game_over_layout;
    private TextView time_text, record_text, coins_text;
    private Button restart_btn, home_btn;

    private DatabaseService databaseService;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_car_escape);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setContentView(R.layout.activity_car_escape);

        full_screen();
        initUI();
        databaseService = DatabaseService.getInstance();
        loadUser();

        game_container.post(() -> {
            int width = game_container.getWidth();
            int height = game_container.getHeight();
            CarEscapeGameView gameView = new CarEscapeGameView(this, width, height, timer_text_view, this);
            game_container.addView(gameView);
        });
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
        game_over_layout = findViewById(R.id.game_over_layout);
        time_text = findViewById(R.id.time_text);
        record_text = findViewById(R.id.record_text);
        coins_text = findViewById(R.id.coins_text);
        restart_btn = findViewById(R.id.restart_btn);
        home_btn = findViewById(R.id.home_btn);
        timer_text_view = findViewById(R.id.timer_text_view);
        game_container = findViewById(R.id.game_container);

        game_over_layout.setVisibility(View.GONE);
        restart_btn.setOnClickListener(v -> recreate());
        home_btn.setOnClickListener(v -> finish());
    }

    private void loadUser() {
        User savedUser = SharedPreferencesUtil.getUser(this);
        if (savedUser == null) return;

        databaseService.getUser(savedUser.getId(), new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                if (user != null) currentUser = user;
            }
            @Override public void onFailed(Exception e) {}
        });
    }


    @Override
    public void onGameOver(int time, int coins) {
        runOnUiThread(() -> showGameOverUI(time, coins));
    }

    @Override
    public int getCurrentUserMoney() {
        return (currentUser != null) ? currentUser.getMoney() : 0;
    }

    private void showGameOverUI(int time, int coins) {
        game_over_layout.setVisibility(View.VISIBLE);
        if (currentUser == null) {
            time_text.setText("Time: " + time + " sec");
            coins_text.setText("Coins: " + coins);
            return;
        }

        int record = currentUser.getCarGameRecord();
        boolean isNewRecord = time > record;
        if (isNewRecord) record = time;

        if (isNewRecord) {
            time_text.setText("NEW RECORD! " + time + " sec");
            time_text.setTextColor(Color.GREEN);
        } else {
            time_text.setText("Time: " + time + " sec");
        }

        record_text.setText("Record: " + record);
        coins_text.setText("Coins: " + coins);

        final int finalRecord = record;
        databaseService.updateUser(currentUser.getId(), user -> {
            if (user == null) return null;
            user.setMoney(user.getMoney() + coins);
            user.setExp(user.getExp() + (time /3 )+ coins);
            user.setCarGameRecord(finalRecord);
            currentUser = user; /// עדכון המשתמש מקומי
            return user;
        }, null);
    }
}