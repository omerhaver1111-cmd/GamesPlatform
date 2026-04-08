package com.example.gamesplatform.screens;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gamesplatform.R;
import com.example.gamesplatform.models.User;
import com.example.gamesplatform.services.AudioService;
import com.example.gamesplatform.services.DatabaseService;
import com.example.gamesplatform.views.PianoGameView;
import com.example.gamesplatform.utils.SharedPreferencesUtil;

public class PianoActivity extends AppCompatActivity {

    private AudioService audioService;
    private boolean isBound = false;

    private FrameLayout gameContainer;
    private LinearLayout gameOverLayout;

    private TextView scoreText, recordText;
    private Button restartBtn, goMainBtn, goRecordingBtn;

    private PianoGameView gameView;

    private User currentUser;
    private DatabaseService databaseService;

    private static final String TAG = "PianoActivity";


    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            audioService = ((AudioService.AudioBinder) service).getService();
            isBound = true;

            databaseService = DatabaseService.getInstance();

            initGame();
            loadCurrentUser();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piano);

        initViews();
        initButtons();

        Intent intent = new Intent(this, AudioService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }


    private void initViews() {
        gameContainer = findViewById(R.id.game_container);
        gameOverLayout = findViewById(R.id.game_over_layout);

        scoreText = findViewById(R.id.score_text);
        recordText = findViewById(R.id.record_text);

        restartBtn = findViewById(R.id.restart_btn);
        goMainBtn = findViewById(R.id.go_main_btn);
        goRecordingBtn = findViewById(R.id.go_recording_btn);
    }

    private void initButtons() {

        goMainBtn.setOnClickListener(v -> goToMain());
        goRecordingBtn.setOnClickListener(v -> goToRecording());

        restartBtn.setOnClickListener(v -> restartGame());
    }


    private void goToMain() {
        Intent intent = new Intent(PianoActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToRecording() {
        Intent intent = new Intent(PianoActivity.this, RecordingActivity.class);
        startActivity(intent);
        finish();
    }


    private void initGame() {
        gameView = new PianoGameView(PianoActivity.this, audioService);
        gameContainer.addView(gameView);

        gameView.setGameOverListener(score ->
                runOnUiThread(() -> showGameOver(score))
        );
    }

    private void restartGame() {
        gameOverLayout.setVisibility(View.GONE);
        if (gameView != null) {
            gameView.startGame();
        }
    }

    private void loadCurrentUser() {

        User savedUser = SharedPreferencesUtil.getUser(this);

        if (savedUser == null) {
            Log.e(TAG, "No user in SharedPreferences");
            return;
        }

        String uid = savedUser.getId();

        databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                currentUser = user;

                runOnUiThread(() -> {
                    if (currentUser != null) {
                        recordText.setText("Record: " + currentUser.getPianoRecord());
                    } else {
                        recordText.setText("Record: N/A");
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Failed to load user", e);
            }
        });
    }

    private void showGameOver(int score) {

        gameOverLayout.setVisibility(View.VISIBLE);

        if (currentUser == null) {
            scoreText.setText("Score: " + score);
            recordText.setText("Record: N/A");
            return;
        }

        int record = currentUser.getPianoRecord();
        boolean isNewRecord = false;

        if (score > record) {
            record = score;
            isNewRecord = true;
        }

        if (isNewRecord) {
            scoreText.setText("NEW RECORD! " + score);
            scoreText.setTextColor(Color.GREEN);
        } else {
            scoreText.setText("Score: " + score);
            scoreText.setTextColor(Color.WHITE);
        }

        recordText.setText("Record: " + record);

        int finalRecord = record;

        databaseService.updateUser(currentUser.getId(), user -> {

            if (user == null) return null;

            user.setExp(user.getExp() + (score / 2));
            user.setPianoRecord(finalRecord);

            currentUser.setPianoRecord(finalRecord);

            return user;

        }, null);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }
}