package com.example.gamesplatform.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import com.example.gamesplatform.services.DatabaseService;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    Button btn_logout;
    TextView btn_to_player_info, btn_to_main, tv_nick_name;
    private DatabaseService databaseService;

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

        tv_nick_name = findViewById(R.id.tv_main_nick_name);
        setPlayerInfo(tv_nick_name);

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

    }

    private void setPlayerInfo(TextView tvNickName) {
        User currentUser = SharedPreferencesUtil.getUser(this);
        if (currentUser == null) {
            Log.e(TAG, "No logged in user");
            return;
        }

        String uid = currentUser.getId();

        databaseService.getUser(uid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                if (user != null && user.getNickname() != null) {
                    tvNickName.setText(user.getNickname());
                }
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Error getting user profile", e);
            }
        });
    }
}