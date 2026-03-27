package com.example.gamesplatform.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamesplatform.R;
import com.example.gamesplatform.adapters.UsersAdapter;
import com.example.gamesplatform.models.Group;
import com.example.gamesplatform.models.User;
import com.example.gamesplatform.services.DatabaseService;
import com.example.gamesplatform.utils.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class MyGroupActivity extends AppCompatActivity {

    private static final String TAG = "MyGroupActivity";

    private TextView tvUserCount, tvGroupName;
    private RecyclerView rvGMembers;
    private UsersAdapter usersAdapter;
    private String currentGroupId;
    private DatabaseService databaseService;
    TextView btn_to_player_info, btn_to_main, btn_to_group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_group);

        Intent intent = getIntent();
        currentGroupId = intent.getStringExtra("GROUP_ID");

        if (currentGroupId == null || currentGroupId.isEmpty()) {
            Toast.makeText(this, "Group ID not found.", Toast.LENGTH_SHORT).show();
            //finish();
            return;
        }


        btn_to_main = findViewById((R.id.btn_mygroup_home));
        btn_to_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyGroupActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        btn_to_player_info = findViewById(R.id.btn_mygroup_info);
        btn_to_player_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyGroupActivity.this, PlayerInfoActivity.class);
                startActivity(intent);
            }
        });

        btn_to_group = findViewById(R.id.btn_mygroup);
        btn_to_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User currentUser = SharedPreferencesUtil.getUser(MyGroupActivity.this);
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
                                Toast.makeText(MyGroupActivity.this, "לא נמצאה קבוצה", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Intent intent = new Intent(MyGroupActivity.this, MyGroupActivity.class);
                            intent.putExtra("GROUP_ID", group.getGroupId());
                            startActivity(intent);
                        }
                        @Override
                        public void onFailed(Exception e) {
                            Toast.makeText(MyGroupActivity.this, "שגיאה בטעינת קבוצות", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "getGroupMap failed", e);
                        }
                    });
                }
                else{
                    Intent intent = new Intent(MyGroupActivity.this, GroupsActivity.class);
                    startActivity(intent);
                }
            }
        });

        initViews();
        setupRecyclerView();
        loadGroupMembers();
    }

    private void initViews() {
        rvGMembers = findViewById(R.id.rv_my_group_users);
        tvUserCount = findViewById(R.id.tv_user_count_my_group);
        tvGroupName = findViewById(R.id.tv_group_name_my_group);
    }

    private void setupRecyclerView() {
        rvGMembers.setLayoutManager(new LinearLayoutManager(this));
        usersAdapter = new UsersAdapter(user -> {
            Log.d(TAG, "User clicked: " + user.getUsername());
            // כאן אפשר להוסיף לוגיקה למעבר לפרופיל המשתמש
        });
        rvGMembers.setAdapter(usersAdapter);
    }

    private void loadGroupMembers() {
        DatabaseService.getInstance().getGroup(currentGroupId, new DatabaseService.DatabaseCallback<Group>() {
            @Override
            public void onCompleted(Group group) {
                if (group == null || group.members == null || group.members.isEmpty()) {
                    Toast.makeText(MyGroupActivity.this, "Could not load group members or group is empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                tvGroupName.setText(group.getGroupName());

                Set<String> memberIds = group.getUserIds();
                List<User> membersList = new ArrayList<>();
                AtomicInteger usersToLoad = new AtomicInteger(memberIds.size());

                for (String userId : memberIds) {
                    DatabaseService.getInstance().getUser(userId, new DatabaseService.DatabaseCallback<User>() {
                        @Override
                        public void onCompleted(User user) {
                            if (user != null) {
                                membersList.add(user);
                            }
                            if (usersToLoad.decrementAndGet() == 0) {
                                usersAdapter.setUsers(membersList);
                                tvUserCount.setText("Members: " + membersList.size());
                            }
                        }

                        @Override
                        public void onFailed(Exception e) {
                            Log.e(TAG, "Failed to load user: " + userId, e);
                            if (usersToLoad.decrementAndGet() == 0) {
                                usersAdapter.setUsers(membersList);
                                tvUserCount.setText("Members: " + membersList.size());
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(MyGroupActivity.this, "Error loading group details.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to get group", e);
            }
        });
    }
}
