package com.example.gamesplatform.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamesplatform.R;
import com.example.gamesplatform.adapters.UsersAdapter;
import com.example.gamesplatform.models.Group;
import com.example.gamesplatform.models.User;
import com.example.gamesplatform.services.DatabaseService;
import com.example.gamesplatform.utils.ImageUtil;
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

    private TextView btn_to_player_info, btn_to_main, btn_to_group;

    private ImageView imgBanner;
    private Button btnLeaveGroup, btnUploadBanner;

    private User currentUser;

    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_group);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseService = DatabaseService.getInstance();


        Intent intent = getIntent();
        currentGroupId = intent.getStringExtra("GROUP_ID");

        if (currentGroupId == null || currentGroupId.isEmpty()) {
            Toast.makeText(this, "Group ID not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser = SharedPreferencesUtil.getUser(this);
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupButtons();
        setupRecyclerView();
        loadGroupMembers();

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                // מזהה החלקה אופקית
                if (Math.abs(diffX) > Math.abs(diffY)) {

                    if (Math.abs(diffX) > SWIPE_THRESHOLD &&
                            Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {

                        if (diffX < 0) {
                            // 👈 החלקה שמאלה → LeaderBoard
                            Intent intent = new Intent(MyGroupActivity.this, LeaderBordActivity.class);
                            startActivity(intent);

                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }

                        return true;
                    }
                }

                return false;
            }
        });

        btn_to_main = findViewById(R.id.btn_mygroup_home);
        btn_to_main.setOnClickListener(v -> {
            startActivity(new Intent(MyGroupActivity.this, MainActivity.class));
        });

        btn_to_player_info = findViewById(R.id.btn_mygroup_info);
        btn_to_player_info.setOnClickListener(v -> {
            startActivity(new Intent(MyGroupActivity.this, PlayerInfoActivity.class));
        });

        btn_to_group = findViewById(R.id.btn_mygroup);
        btn_to_group.setOnClickListener(v -> {
            User currentUser = SharedPreferencesUtil.getUser(MyGroupActivity.this);
            if (currentUser == null) return;

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
            } else {
                startActivity(new Intent(MyGroupActivity.this, GroupsActivity.class));
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private void setupButtons() {

        // יציאה מהקבוצה
        btnLeaveGroup.setOnClickListener(v -> {
            if (currentUser == null) return;

            databaseService.updateGroup(currentGroupId, group -> {
                if (group == null) return null;

                group.removeMember(currentUser.getId());
                currentUser.setInGroup(false);

                return group;

            }, new DatabaseService.DatabaseCallback<Group>() {
                @Override
                public void onCompleted(Group object) {
                    Toast.makeText(MyGroupActivity.this, "Left group", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(MyGroupActivity.this, "Failed to leave group", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // העלאת באנר (רק למנהל)
        btnUploadBanner.setOnClickListener(v -> {
            if (currentUser == null) return;

            databaseService.getGroup(currentGroupId, new DatabaseService.DatabaseCallback<Group>() {
                @Override
                public void onCompleted(Group group) {
                    if (group == null) return;

                    if (!group.isAdmin(currentUser.getId())) {
                        Toast.makeText(MyGroupActivity.this, "Only admin can upload banner", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    pickImageFromGallery();
                }

                @Override
                public void onFailed(Exception e) {
                }
            });
        });
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {

            try {
                android.net.Uri uri = data.getData();
                ImageView tempImage = new ImageView(this);
                tempImage.setImageURI(uri);

                final String base64 = ImageUtil.toBase64(tempImage);

                databaseService.updateGroup(currentGroupId, group -> {
                    if (group == null) return null;

                    group.setBannerImageBase64(base64);
                    return group;

                }, new DatabaseService.DatabaseCallback<Group>() {

                    @Override
                    public void onCompleted(Group object) {
                        Toast.makeText(MyGroupActivity.this, "Banner updated", Toast.LENGTH_SHORT).show();
                        loadGroupBanner(object);
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(MyGroupActivity.this, "Failed to upload banner", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadGroupBanner(Group group) {
        if (group != null) {
            if (group.getBannerImageBase64() != null) {
                android.graphics.Bitmap bitmap = ImageUtil.fromBase64(group.getBannerImageBase64());
                imgBanner.setImageBitmap(bitmap);
            } else {
                imgBanner.setImageResource(R.drawable.group_banner);
            }
        }
    }

    private void initViews() {
        rvGMembers = findViewById(R.id.rv_my_group_users);
        tvUserCount = findViewById(R.id.tv_user_count_my_group);
        tvGroupName = findViewById(R.id.tv_group_name_my_group);

        imgBanner = findViewById(R.id.img_group_banner);
        btnLeaveGroup = findViewById(R.id.btn_leave_group);
        btnUploadBanner = findViewById(R.id.btn_upload_banner);
    }

    private void setupRecyclerView() {
        rvGMembers.setLayoutManager(new LinearLayoutManager(this));
        usersAdapter = new UsersAdapter(user -> {
            Log.d(TAG, "User clicked: " + user.getUsername());
        });
        rvGMembers.setAdapter(usersAdapter);
    }

    private void loadGroupMembers() {
        databaseService.getGroup(currentGroupId, new DatabaseService.DatabaseCallback<Group>() {
            @Override
            public void onCompleted(Group group) {

                if (group == null || group.members == null || group.members.isEmpty()) {
                    Toast.makeText(MyGroupActivity.this, "Could not load group members.", Toast.LENGTH_SHORT).show();
                    return;
                }

                tvGroupName.setText(group.getGroupName());
                loadGroupBanner(group);

                Set<String> memberIds = group.getUserIds();
                List<User> membersList = new ArrayList<>();
                AtomicInteger usersToLoad = new AtomicInteger(memberIds.size());

                for (String userId : memberIds) {
                    databaseService.getUser(userId, new DatabaseService.DatabaseCallback<User>() {
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