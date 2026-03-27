package com.example.gamesplatform.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import com.example.gamesplatform.services.DatabaseService;

import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    Button btn_logout, play_btn;
    TextView btn_to_player_info, btn_to_main, btn_to_group, btn_to_shop, tv_nick_name;
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
                }
                else{
                    Intent intent = new Intent(MainActivity.this, GroupsActivity.class);
                    startActivity(intent);
                }
            }
        });
        btn_to_shop = findViewById(R.id.btn_main_shop);
        btn_to_shop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ShopActivity.class);
                startActivity(intent);
            }
        });

        play_btn = findViewById(R.id.play_button);
        play_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CarEscapeActivity.class);
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