package com.example.gamesplatform.screens;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamesplatform.R;
import com.example.gamesplatform.adapters.UsersAdapter;
import com.example.gamesplatform.models.Group;
import com.example.gamesplatform.models.User;
import com.example.gamesplatform.services.DatabaseService;
import com.example.gamesplatform.utils.SharedPreferencesUtil;

import java.util.List;
import java.util.Map;


public class AdminPageActivity extends BaseActivity {
    private static final String TAG = "AdminPageActivity";
    private UsersAdapter userAdapter;
    private TextView tvUserCount;
    private Button btn_to_player_info, btn_to_main, btn_to_group, btn_to_shop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        full_screen();
        RecyclerView usersList = findViewById(R.id.rv_users_admin_page);
        tvUserCount = findViewById(R.id.tv_user_count);
        usersList.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UsersAdapter(new UsersAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user, int position) {
                Log.d(TAG, "User clicked: " + user);
                Intent intent = new Intent(AdminPageActivity.this, PlayerInfoActivity.class);
                intent.putExtra("USER_UID", user.getId());
                startActivity(intent);
            }
            @Override
            public void onLongUserClick(User user, int position) {
                showPromoteDialog(user, position);
            }
        });
        usersList.setAdapter(userAdapter);

        btn_to_main = findViewById((R.id.btn_admin_home));
        btn_to_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminPageActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        btn_to_player_info = findViewById(R.id.btn_admin_info);
        btn_to_player_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminPageActivity.this, PlayerInfoActivity.class);
                startActivity(intent);
            }
        });

        btn_to_group = findViewById(R.id.btn_admin_group);
        btn_to_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User currentUser = SharedPreferencesUtil.getUser(AdminPageActivity.this);
                if (currentUser == null) {
                    Log.e(TAG, "No logged in user");
                    return;
                }
                if (currentUser.isInGroup()) {
                    databaseService.getGroupList(new DatabaseService.DatabaseCallback<List<Group>>() {
                        @Override
                        public void onCompleted(List<Group> groups) {
                            Group group = currentUser.getMyGroup(groups);
                            if (group == null) {
                                Toast.makeText(AdminPageActivity.this, "לא נמצאה קבוצה", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Intent intent = new Intent(AdminPageActivity.this, MyGroupActivity.class);
                            intent.putExtra("GROUP_ID", group.getGroupId());
                            startActivity(intent);
                        }

                        @Override
                        public void onFailed(Exception e) {
                            Toast.makeText(AdminPageActivity.this, "שגיאה בטעינת קבוצות", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "getGroupMap failed", e);
                        }
                    });
                } else {
                    Intent intent = new Intent(AdminPageActivity.this, GroupsActivity.class);
                    startActivity(intent);
                }
            }

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

    private void showPromoteDialog(User user, int position) {

        new AlertDialog.Builder(this)
                .setTitle("Admin Promotion")
                .setMessage("Do you want to make " + user.getUsername() + " an admin?")
                .setPositiveButton("Yes", (dialog, which) -> {

                    databaseService.updateUser(user.getId(), serverUser -> {
                        if (serverUser == null) return null;

                        serverUser.setIsAdmin(true);
                        return serverUser;

                    }, new DatabaseService.DatabaseCallback<User>() {
                        @Override
                        public void onCompleted(User serverUser) {
                            Toast.makeText(AdminPageActivity.this,
                                    user.getUsername() + " is now admin",
                                    Toast.LENGTH_SHORT).show();
                            userAdapter.setUserAtPosition(position, serverUser);
                        }

                        @Override
                        public void onFailed(Exception e) {
                            Toast.makeText(AdminPageActivity.this,
                                    "Failed to update user",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        databaseService.getUserList(new DatabaseService.DatabaseCallback<>() {
            @Override
            public void onCompleted(List<User> users) {
                userAdapter.setUserList(users);
                tvUserCount.setText("Total users: " + users.size());
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Failed to get users list", e);
            }
        });
    }

}