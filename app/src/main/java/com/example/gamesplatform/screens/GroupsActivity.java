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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamesplatform.adapters.GroupsAdapter;
import com.example.gamesplatform.adapters.UsersAdapter;
import com.example.gamesplatform.models.Group;
import com.example.gamesplatform.models.User;
import com.example.gamesplatform.services.DatabaseService;
import com.example.gamesplatform.utils.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class GroupsActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "GroupsActivity";

    // UI Components
    private RecyclerView rvGroups, rvUsers;
    private GroupsAdapter group_adapter;
    private UsersAdapter usersAdapter; // אדפטר ייעודי למשתמשים
    private TextView btn_to_player_info, btn_to_main;
    private EditText etGroupName, etGroupId;
    private Button btnCreateGroup, btnJoinGroup;

    // Data
    private String currentUserId;
    private User currentUser;
    // למעלה עם שאר המשתנים
    private Map<String, Group> groupsMap = new HashMap<>(); // מפה לחיפוש מהיר של שמות קבוצות
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

        LinearLayout createGwin, my_group;

        if (currentUser.getInGroup()){
            // If the user is in a group he can't create a new one
            createGwin = findViewById(R.id.create_group_window);
            createGwin.setVisibility(View.GONE);

            my_group = findViewById(R.id.my_group_card);
            my_group.setVisibility(View.VISIBLE);
        }
        else{
            createGwin = findViewById(R.id.create_group_window);
            createGwin.setVisibility(View.VISIBLE);

            my_group = findViewById(R.id.my_group_card);
            my_group.setVisibility(View.GONE);

        }

        btn_to_main = findViewById((R.id.btn_group_home));
        btn_to_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        btn_to_player_info = findViewById(R.id.btn_group_info);
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
        rvGroups = findViewById(R.id.rv_users_admin_page);
        rvUsers = findViewById(R.id.rv_group_users);

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
        rvGroups.setLayoutManager(new LinearLayoutManager(this));
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
                String groupName = etGroupId.getText().toString();
                filterGroups(groupName);
            }
            @Override
            public void onFailed(Exception e)
            {

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
        databaseService.updateUser(user, new DatabaseService.DatabaseCallback<Void>() {
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

        Log.d(TAG, "Joining group: " + groupId);

        // Check if group exists
//        groupsRef.child(groupId).get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                DataSnapshot snapshot = task.getResult();
//                if (snapshot.exists()) {
//                    Group group = snapshot.getValue(Group.class);
//
//                    if (group != null) {
//                        if (group.isMember(currentUserId)) {
//                            Toast.makeText(this, "You are already in this group", Toast.LENGTH_SHORT).show();
//                        } else {
//                            // Add user to group
//                            group.addMember(currentUserId);
//                            groupsRef.child(groupId).setValue(group)
//                                    .addOnSuccessListener(aVoid -> {
//                                        Log.d(TAG, "Joined group successfully");
//                                        Toast.makeText(GroupsActivity.this,
//                                                "Joined group: " + group.groupName,
//                                                Toast.LENGTH_SHORT).show();
//                                        etGroupId.setText("");
//                                    })
//                                    .addOnFailureListener(e -> {
//                                        Log.e(TAG, "Error joining group", e);
//                                        Toast.makeText(GroupsActivity.this,
//                                                "Failed to join group",
//                                                Toast.LENGTH_SHORT).show();
//                                    });
//                        }
//                    }
//                } else {
//                    Toast.makeText(this, "Group not found", Toast.LENGTH_SHORT).show();
//                }
//            } else {
//                Log.e(TAG, "Error checking group", task.getException());
//                Toast.makeText(this, "Error checking group", Toast.LENGTH_SHORT).show();
//            }
//        });
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