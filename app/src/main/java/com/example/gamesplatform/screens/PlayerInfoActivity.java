package com.example.gamesplatform.screens;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gamesplatform.R;
import com.example.gamesplatform.models.Group;
import com.example.gamesplatform.models.User;
import com.example.gamesplatform.services.DatabaseService;
import com.example.gamesplatform.utils.SharedPreferencesUtil;
import com.example.gamesplatform.utils.Validator;

import java.util.Map;
import java.util.function.UnaryOperator;

public class PlayerInfoActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "PlayerInfoActivity";
    String selectedUid;
    User selectedUser;
    boolean isCurrentUser = false;
    private EditText etUserFirstName, etUserNickName, etUserEmail, etUserPassword;
    private TextView tvUserDisplayEmail, btn_to_player_info, btn_to_group, btn_to_main, tv_username, tv_nick_name, tv_level, tv_money;
    private Button btnUpdateProfile, btn_logout, btn_to_admin_page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.player_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.player_profile), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        selectedUid = getIntent().getStringExtra("USER_UID");
        User currentUser = SharedPreferencesUtil.getUser(this);
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (selectedUid == null) {
            selectedUid = currentUser.getId();
        }
        isCurrentUser = selectedUid.equals(currentUser.getId());
        if (!isCurrentUser && !currentUser.isAdmin()) {
            // If the user is not an admin and the selected user is not the current user
            // then finish the activity
            Toast.makeText(this, "You are not authorized to view this profile", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        tv_username = findViewById(R.id.tv_user_display_username);
        tv_nick_name = findViewById(R.id.tv_user_display_nikname);
        tv_level = findViewById(R.id.tv_user_display_level);
        tv_money = findViewById(R.id.tv_user_display_cash);
        set_tv(tv_username, tv_nick_name, tv_level, tv_money);

        btn_to_admin_page = findViewById(R.id.btn_player_profile_to_admin_page);
        if (currentUser.isAdmin) {
            btn_to_admin_page.setVisibility(View.VISIBLE);
        } else {
            btn_to_admin_page.setVisibility(View.GONE);
        }


        Log.d(TAG, "Selected user: " + selectedUid);

        // Initialize the EditText fields
        etUserFirstName = findViewById(R.id.et_user_first_name);
        etUserNickName = findViewById(R.id.et_user_nick_name);
        etUserEmail = findViewById(R.id.et_user_email);
        etUserPassword = findViewById(R.id.et_user_password);
        tvUserDisplayEmail = findViewById(R.id.tv_user_display_email);

        btnUpdateProfile = findViewById(R.id.btn_edit_profile);
        btnUpdateProfile.setOnClickListener(this);

        showUserProfile();
        btn_to_main = findViewById((R.id.btn_main_home));
        btn_to_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PlayerInfoActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        btn_to_player_info = findViewById(R.id.btn_main_info);
        btn_to_player_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PlayerInfoActivity.this, PlayerInfoActivity.class);
                startActivity(intent);
            }
        });

        btn_to_group = findViewById(R.id.btn_info_to_group);
        btn_to_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User currentUser = SharedPreferencesUtil.getUser(PlayerInfoActivity.this);
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
                                Toast.makeText(PlayerInfoActivity.this, "לא נמצאה קבוצה", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Intent intent = new Intent(PlayerInfoActivity.this, MyGroupActivity.class);
                            intent.putExtra("GROUP_ID", group.getGroupId());
                            startActivity(intent);
                        }

                        @Override
                        public void onFailed(Exception e) {
                            Toast.makeText(PlayerInfoActivity.this, "שגיאה בטעינת קבוצות", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "getGroupMap failed", e);
                        }
                    });
                } else {
                    Intent intent = new Intent(PlayerInfoActivity.this, GroupsActivity.class);
                    startActivity(intent);
                }
            }
        });

        btn_logout = findViewById(R.id.btn_player_profile_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(PlayerInfoActivity.this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                SharedPreferencesUtil.signOutUser(PlayerInfoActivity.this);

                                Intent intent = new Intent(PlayerInfoActivity.this, LandingActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();

            }
        });

        btn_to_admin_page.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlayerInfoActivity.this, AdminPageActivity.class);
                startActivity(intent);
            }
        }));
    }

    private void set_tv(TextView tvUsername, TextView tvNickname, TextView tvLevel, TextView tvMoney) {
        // Get the user data from database
        databaseService.getUser(selectedUid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                selectedUser = user;
                // Set the user data to the EditText fields
                tvUsername.setText(user.getUsername());
                tvNickname.setText(user.getNickname());
                tvLevel.setText(String.valueOf(user.getExp()));
                tvMoney.setText(String.valueOf(user.getMoney()));
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Error getting user profile", e);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_edit_profile) {
            updateUserProfile();
            return;
        }
    }

    private void showUserProfile() {
        // Get the user data from database
        databaseService.getUser(selectedUid, new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                selectedUser = user;
                // Set the user data to the EditText fields
                etUserFirstName.setText(user.getUsername());
                etUserNickName.setText(user.getNickname());
                etUserEmail.setText(user.getEmail());
                etUserPassword.setText(user.getPassword());

                // Update display fields
                String displayName = user.getUsername() + "|" + user.getNickname();
                tvUserDisplayEmail.setText(user.getEmail());
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Error getting user profile", e);
            }
        });

        // disable the EditText fields if the user is not the current user
        if (!isCurrentUser) {
            etUserEmail.setEnabled(false);
            etUserPassword.setEnabled(false);
        } else {
            etUserEmail.setEnabled(true);
            etUserPassword.setEnabled(true);
            btnUpdateProfile.setVisibility(View.VISIBLE);
        }
    }

    private void updateUserProfile() {
        if (selectedUser == null) {
            Log.e(TAG, "User not found");
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }
        // Get the updated user data from the EditText fields
        String firstName = etUserFirstName.getText().toString();
        String lastName = etUserNickName.getText().toString();
        String email = etUserEmail.getText().toString();
        String password = etUserPassword.getText().toString();

        if (!isValid(firstName, lastName, email, password)) {
            Log.e(TAG, "Invalid input");
            return;
        }
        // Update the user object
        selectedUser.setUsername(firstName);
        selectedUser.setNickname(lastName);
        selectedUser.setEmail(email);
        selectedUser.setPassword(password);

        // Update the user data in the authentication
        Log.d(TAG, "Updating user profile");
        Log.d(TAG, "Selected user UID: " + selectedUser.getId());
        Log.d(TAG, "Is current user: " + isCurrentUser);
        Log.d(TAG, "User email: " + selectedUser.getEmail());
        Log.d(TAG, "User password: " + selectedUser.getPassword());


        if (!isCurrentUser && !selectedUser.isAdmin()) {
            Log.e(TAG, "Only the current user can update their profile");
            Toast.makeText(this, "You can only update your own profile", Toast.LENGTH_SHORT).show();
            return;
        } else if (isCurrentUser) {
            updateUserInDatabase(selectedUser);
        } else if (selectedUser.isAdmin()) {
            // update the user in the database
            updateUserInDatabase(selectedUser);
        }
    }

    private void updateUserInDatabase(User user) {
        Log.d(TAG, "Updating user in database: " + user.getId());
        databaseService.updateUser(user.id, new UnaryOperator<User>() {
            @Override
            public User apply(User serverUser) {
                if (serverUser != null) {
                    serverUser.setUsername(user.getUsername());
                    serverUser.setNickname(user.getNickname());
                    serverUser.setEmail(user.getEmail());
                    serverUser.setPassword(user.getPassword());
                }
                return serverUser;
            }
        }, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void result) {
                Log.d(TAG, "User profile updated successfully");
                Toast.makeText(PlayerInfoActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                showUserProfile(); // Refresh the profile view
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Error updating user profile", e);
                Toast.makeText(PlayerInfoActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValid(String firstName, String lastName, String email, String password) {
        if (!Validator.isNameValid(firstName)) {
            etUserFirstName.setError("First name is required");
            etUserFirstName.requestFocus();
            return false;
        }
        if (!Validator.isNameValid(lastName)) {
            etUserNickName.setError("Last name is required");
            etUserNickName.requestFocus();
            return false;
        }
        if (!Validator.isEmailValid(email)) {
            etUserEmail.setError("Email is required");
            etUserEmail.requestFocus();
            return false;
        }
        if (!Validator.isPasswordValid(password)) {
            etUserPassword.setError("Password is required");
            etUserPassword.requestFocus();
            return false;
        }
        return true;
    }
}