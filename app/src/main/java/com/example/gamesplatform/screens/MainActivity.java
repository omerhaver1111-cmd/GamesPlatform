package com.example.gamesplatform.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gamesplatform.R;
import com.example.gamesplatform.models.Group;
import com.example.gamesplatform.models.User;
import com.example.gamesplatform.services.DatabaseService;
import com.example.gamesplatform.utils.SharedPreferencesUtil;

import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private final int CAR_LEVEL = 1;
    private final int SNAKE_LEVEL = 3;
    private final int PIANO_LEVEL = 5;
    Button btn_logout, play_car_btn, play_snake_btn, play_piano_btn;
    TextView btn_to_player_info, btn_to_main, btn_to_group, btn_to_shop;
    TextView tv_level, tv_nick_name, tv_coins;
    ProgressBar pbar_level;
    private DatabaseService databaseService;
    private int currentLevel = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        databaseService = new DatabaseService();

        setPlayerInfo();

        btn_logout = findViewById(R.id.btn_main_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferencesUtil.signOutUser(MainActivity.this);
                Intent intent = new Intent(MainActivity.this, LandingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }
        });

        btn_to_main = findViewById((R.id.btn_main_home));
        btn_to_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        btn_to_player_info = findViewById(R.id.btn_main_info);
        btn_to_player_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PlayerInfoActivity.class);
                startActivity(intent);
            }
        });

        btn_to_group = findViewById(R.id.btn_main_group);
        btn_to_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User currentUser = SharedPreferencesUtil.getUser(MainActivity.this);
                if (currentUser == null) {
                    Log.e(TAG, "No logged in user");
                    return;
                }
                if (currentUser.isInGroup()) {
                    databaseService.getGroupMap(new DatabaseService.DatabaseCallback<Map<String, Group>>() {
                        @Override
                        public void onCompleted(Map<String, Group> groupMap) {
                            Group group = currentUser.getMyGroup(groupMap);
                            if (group == null) {
                                Toast.makeText(MainActivity.this, "לא נמצאה קבוצה", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Intent intent = new Intent(MainActivity.this, MyGroupActivity.class);
                            intent.putExtra("GROUP_ID", group.getGroupId());
                            startActivity(intent);
                        }

                        @Override
                        public void onFailed(Exception e) {
                            Toast.makeText(MainActivity.this, "שגיאה בטעינת קבוצות", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "getGroupMap failed", e);
                        }
                    });
                } else {
                    Intent intent = new Intent(MainActivity.this, GroupsActivity.class);
                    startActivity(intent);
                }
            }
        });


        play_car_btn = findViewById(R.id.play_car_button);
        play_car_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CarEscapeActivity.class);
                startActivity(intent);
            }
        });

        play_snake_btn = findViewById(R.id.play_snake_button);
        play_snake_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel < SNAKE_LEVEL) {
                    Toast.makeText(MainActivity.this, "level needed " + SNAKE_LEVEL, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, SnakeGameActivity.class);
                startActivity(intent);
            }
        });
        play_piano_btn = findViewById(R.id.play_piano_button);
        play_piano_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel < PIANO_LEVEL) {
                    Toast.makeText(MainActivity.this, "level needed " + PIANO_LEVEL, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, RecordingActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setPlayerInfo() {
        tv_nick_name = findViewById(R.id.tv_main_nick_name);
        tv_level = findViewById(R.id.tv_main_level);
        pbar_level = findViewById(R.id.expProgressBar);
        tv_coins = findViewById(R.id.coins);

        User currentUser = SharedPreferencesUtil.getUser(this);
        if (currentUser == null) {
            Log.e(TAG, "No logged in user");
            return;
        }

        String uid = currentUser.getId();
        databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                if (user != null) {
                    if (user.getNickname() != null) {
                        tv_nick_name.setText(user.getNickname());
                    }
                    currentLevel = User.levelCalculate(user.getExp());
                    tv_level.setText(String.valueOf("Lv. " + currentLevel));

                    int remainingExp = User.getRemainingExp(user.getExp());
                    pbar_level.setProgress(remainingExp);

                    tv_coins.setText(user.getMoney() + "c");

                    updateGameAccess();
                }
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Error getting user profile", e);
            }
        });
    }

    private void updateGameAccess() {

        // Snake
        if (currentLevel < SNAKE_LEVEL) {
            play_snake_btn.setEnabled(false);
            play_snake_btn.setText("🔒Snake " + SNAKE_LEVEL);
            play_snake_btn.setAlpha(0.5f);
        } else {
            play_snake_btn.setEnabled(true);
            play_snake_btn.setText("Snake");
            play_snake_btn.setAlpha(1f);
        }

        // Piano
        if (currentLevel < PIANO_LEVEL) {
            play_piano_btn.setEnabled(false);
            play_piano_btn.setText("🔒Piano " + PIANO_LEVEL);
            play_piano_btn.setAlpha(0.5f);
        } else {
            play_piano_btn.setEnabled(true);
            play_piano_btn.setText("Piano");
            play_piano_btn.setAlpha(1f);
        }
    }
}