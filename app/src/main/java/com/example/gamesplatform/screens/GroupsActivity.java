package com.example.gamesplatform.screens;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gamesplatform.R;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamesplatform.adapters.GroupsAdapter;
import com.example.gamesplatform.models.Group;
import com.example.gamesplatform.models.User;
import com.example.gamesplatform.services.DatabaseService;
import com.example.gamesplatform.utils.SharedPreferencesUtil;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class GroupsActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "GroupsActivity";

    // UI Components
    private RecyclerView rvGroups;
    private GroupsAdapter group_adapter;
    private TextView btn_to_player_info, btn_to_main;
    private EditText etGroupName, etGroupId;
    private Button btnCreateGroup, btnJoinGroup;

    // Data
    private String currentUserId;
    private User currentUser;
    // למעלה עם שאר המשתנים
    private List<Group> allGroups = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_groups_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get current user
        currentUser = SharedPreferencesUtil.getUser(this);
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = currentUser.getId();

        btn_to_main = findViewById((R.id.btn_mygroup_home));
        btn_to_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        btn_to_player_info = findViewById(R.id.btn_mygroup_info);
        btn_to_player_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupsActivity.this, PlayerInfoActivity.class);
                startActivity(intent);
            }
        });

        btnCreateGroup = findViewById(R.id.btn_create_group);
        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroup();
            }
        });

        // Initialize views
        initViews();
        // Setup RecyclerView
        setupRecyclerView();
        // Setup button listeners
        setupButtons();
        // Load groups
        loadUsers();
        loadGroups();

        etGroupId.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "ahhhhhhhhhhhhhhhhhhhhhhh " + s);
                filterGroups(s.toString());
            }
        });
    }

    private void initViews() {
        // RecyclerView components
        rvGroups = findViewById(R.id.rv_groups);
        rvGroups.setLayoutManager(new LinearLayoutManager(this));

        // Create group components
        etGroupName = findViewById(R.id.et_group_name);
        btnCreateGroup = findViewById(R.id.btn_create_group);

        // Join group components
        etGroupId = findViewById(R.id.et_group_id);
        btnJoinGroup = findViewById(R.id.btn_join_group);
    }

    private void setupButtons() {
        btnCreateGroup.setOnClickListener(this);
        btnJoinGroup.setOnClickListener(this);
    }

    private void setupRecyclerView() {
        group_adapter = new GroupsAdapter(new GroupsAdapter.OnGroupListener() {
            @Override
            public void onClick(Group group) {
                // Handle group click - open group details
                Toast.makeText(GroupsActivity.this,
                        "Clicked: " + group.groupName,
                        Toast.LENGTH_SHORT).show();
            }
        });
        rvGroups.setAdapter(group_adapter);

//        rvUsers.setLayoutManager(new LinearLayoutManager(this));
//        usersAdapter = new GroupsAdapter(new UsersAdapter.OnUserClickListener() {
//            @Override
//            public void onClick(User user) {
//                // Handle group click - open group details
//                Toast.makeText(GroupsActivity.this,
//                        "Clicked: " + user.groupName,
//                        Toast.LENGTH_SHORT).show();
//            }
//        });
//        rvUsers.setAdapter(usersAdapter);
    }

    private void loadUsers() {
        DatabaseService.getInstance().getUserList(new DatabaseService.DatabaseCallback<List<User>>() {
            @Override
            public void onCompleted(List<User> users) {
                Toast.makeText(GroupsActivity.this, "users: "+users.size(), Toast.LENGTH_LONG).show();
                group_adapter.setUsers(users);
            }

            @Override
            public void onFailed(Exception e) {

            }
        });

    }

    private void loadGroups(){
        DatabaseService.getInstance().getGroupList(new DatabaseService.DatabaseCallback<List<Group>>() {
            @Override
            public void onCompleted(List<Group> groups) {
                GroupsActivity.this.allGroups.clear();
                GroupsActivity.this.allGroups.addAll(groups);
                group_adapter.setGroups(groups);
                String groupName = etGroupId.getText().toString();
                filterGroups(groupName);
            }
            @Override
            public void onFailed(Exception e)
            {
                Log.e(TAG, "Failed to load groups", e);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_create_group) {
            createGroup();
        } else if (v.getId() == R.id.btn_join_group) {
            joinGroup();
        }
    }

    private void createGroup() {
        String groupName = etGroupName.getText().toString().trim();
        if (groupName.isEmpty()) {
            etGroupName.setError("Group name is required");
            etGroupName.requestFocus();
            return;
        }

        Log.d(TAG, "Creating group: " + groupName);

        // Create new group
        Group newGroup = new Group(groupName, currentUserId);

        createGroupInDatabase(newGroup);
        currentUser.setInGroup(true);
        SharedPreferencesUtil.saveUser(GroupsActivity.this, currentUser);
        updateUserInDatabase(currentUser);
    }

    private void createGroupInDatabase(Group group) {
        Log.d(TAG, "createUserInDatabase: creating user");
        databaseService.createNewGroup(group, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                Log.d(TAG, "createGroupInDatabase: Group created successfully");
                /// save the user to shared preferences
                SharedPreferencesUtil.saveGroup(GroupsActivity.this, group);
                Log.d(TAG, "createUserInDatabase: Redirecting to HomePageActivity");
                /// Redirect to MainActivity and clear back stack to prevent user from going back to register screen
                Intent mainIntent = new Intent(GroupsActivity.this, MainActivity.class);
                /// clear the back stack (clear history) and start the HomePageActivity
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "createUserInDatabase: Failed to create group", e);
                /// show error message to user
                Toast.makeText(GroupsActivity.this, "Failed to create the group", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserInDatabase(User user) {
        Log.d(TAG, "Updating user in database: " + user.getId());
        databaseService.updateUser(user.id, new UnaryOperator<User>() {
            @Override
            public User apply(User serverUser) {
                if (serverUser != null) {
                    serverUser.setInGroup(true);
                }
                return serverUser;
            }
        }, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void result) {
                Log.d(TAG, "User profile updated successfully");
                Toast.makeText(GroupsActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Error updating user profile", e);
                Toast.makeText(GroupsActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void joinGroup() {
        String groupId = etGroupId.getText().toString().trim();

        if (groupId.isEmpty()) {
            etGroupId.setError("Group ID is required");
            etGroupId.requestFocus();
            return;
        }

        // חיפוש הקבוצה ברשימה allGroups
        Group targetGroup = null;
        for (Group g : allGroups) {
            if (g.id != null && g.id.equals(groupId)) {
                targetGroup = g;
                break;
            }
        }

        if (targetGroup == null) {
            Toast.makeText(this, "Group not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (targetGroup.isMember(currentUserId)) {
            Toast.makeText(this, "You are already in this group", Toast.LENGTH_SHORT).show();
            return;
        }

        // הוספת המשתמש לאובייקט הקבוצה
        targetGroup.addMember(currentUserId);

        // עדכון ב-Firebase דרך ה-Service
        DatabaseService.getInstance().updateGroup(targetGroup.getGroupId(), new UnaryOperator<Group>() {
            @Override
            public Group apply(Group serverGroup) {
                if (serverGroup != null)
                {
                    serverGroup.addMember(currentUserId);
                }
                return serverGroup;
            }
        }, new DatabaseService.DatabaseCallback<Group>() {
            @Override
            public void onCompleted(Group updatedServerGroup) {
                Log.d(TAG, "Joined group successfully");
                Toast.makeText(GroupsActivity.this, "Joined group!", Toast.LENGTH_SHORT).show();
                etGroupId.setText("");

                // עדכון המשתמש שהצטרף לקבוצה
                currentUser.setInGroup(true);
                SharedPreferencesUtil.saveUser(GroupsActivity.this, currentUser);
                updateUserInDatabase(currentUser);

                Intent intent = new Intent(GroupsActivity.this, MyGroupActivity.class);
                startActivity(intent);

            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Error joining group", e);
                Toast.makeText(GroupsActivity.this, "Failed to join group", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void filterGroups(String text) {
        List<Group> filterGroups = new ArrayList<>(allGroups);
        filterGroups.removeIf(new Predicate<Group>() {
            @Override
            public boolean test(Group group) {
                return !group.getGroupName().contains(text);
            }
        });
        group_adapter.setGroups(filterGroups);
    }


}