package com.example.gamesplatform.screens;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamesplatform.R;
import com.example.gamesplatform.adapters.UsersLeaderBordAdapter;
import com.example.gamesplatform.models.Group;
import com.example.gamesplatform.models.User;
import com.example.gamesplatform.services.DatabaseService;
import com.example.gamesplatform.utils.SharedPreferencesUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class LeaderBordActivity extends AppCompatActivity {
    RecyclerView carRecycler, snakeRecycler, pianoRecycler;
    UsersLeaderBordAdapter carAdapter, snakeAdapter, pianoAdapter;
    Button btnCar, btnSnake, btnPiano;
    TextView tvBoardTitle;
    private GestureDetector gestureDetector;
    private DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_leader_bord);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        full_screen();
        databaseService = new DatabaseService();

        carRecycler = findViewById(R.id.recycler_car);
        snakeRecycler = findViewById(R.id.recycler_snake);
        pianoRecycler = findViewById(R.id.recycler_piano);

        carRecycler.setLayoutManager(new LinearLayoutManager(this));
        snakeRecycler.setLayoutManager(new LinearLayoutManager(this));
        pianoRecycler.setLayoutManager(new LinearLayoutManager(this));

        carAdapter = new UsersLeaderBordAdapter("car");
        snakeAdapter = new UsersLeaderBordAdapter("snake");
        pianoAdapter = new UsersLeaderBordAdapter("piano");

        carRecycler.setAdapter(carAdapter);
        snakeRecycler.setAdapter(snakeAdapter);
        pianoRecycler.setAdapter(pianoAdapter);

        tvBoardTitle = findViewById(R.id.tv_leader_bord_title);
        btnCar = findViewById(R.id.btn_leader_bord_car_escape);
        btnCar.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvBoardTitle.setText("Car Escape");
                carRecycler.setVisibility(View.VISIBLE);
                snakeRecycler.setVisibility(View.GONE);
                pianoRecycler.setVisibility(View.GONE);
            }
        });
        btnSnake = findViewById(R.id.btn_leader_bord_snake);
        btnSnake.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvBoardTitle.setText("Snake");
                carRecycler.setVisibility(View.GONE);
                snakeRecycler.setVisibility(View.VISIBLE);
                pianoRecycler.setVisibility(View.GONE);
            }
        });
        btnPiano = findViewById(R.id.btn_leader_bord_piano);
        btnPiano.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvBoardTitle.setText("Piano");
                carRecycler.setVisibility(View.GONE);
                snakeRecycler.setVisibility(View.GONE);
                pianoRecycler.setVisibility(View.VISIBLE);
            }
        });

        loadLeaderboards();

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                /// בדיקה שמדובר בהחלקה אופקית
                if (Math.abs(diffX) > Math.abs(diffY)) {

                    if (Math.abs(diffX) > SWIPE_THRESHOLD &&
                            Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {

                        if (diffX > 0) {
                            /// החלקה ימינה
                            User currentUser = SharedPreferencesUtil.getUser(LeaderBordActivity.this);
                            if (currentUser == null) {
                                Log.e(TAG, "No logged in user");
                                Intent intent = new Intent(LeaderBordActivity.this, MainActivity.class);
                                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                startActivity(intent);
                            }
                            if (currentUser.isInGroup()) {
                                databaseService.getGroupMap(new DatabaseService.DatabaseCallback<Map<String, Group>>() {
                                    @Override
                                    public void onCompleted(Map<String, Group> groupMap) {
                                        Group group = currentUser.getMyGroup(groupMap);
                                        if (group == null) {
                                            Toast.makeText(LeaderBordActivity.this, "לא נמצאה קבוצה", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        Intent intent = new Intent(LeaderBordActivity.this, MyGroupActivity.class);
                                        intent.putExtra("GROUP_ID", group.getGroupId());
                                        startActivity(intent);
                                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                    }

                                    @Override
                                    public void onFailed(Exception e) {
                                        Toast.makeText(LeaderBordActivity.this, "שגיאה בטעינת קבוצות", Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "getGroupMap failed", e);
                                        Intent intent = new Intent(LeaderBordActivity.this, MainActivity.class);
                                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                        startActivity(intent);

                                    }
                                });
                            }


                        }

                        return true;
                    }
                }
                return false;
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


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private List<User> getTop10(List<User> users, Comparator<User> comparator) {
        return users.stream()
                .sorted(comparator)
                .limit(10)
                .collect(java.util.stream.Collectors.toList());
    }

    private void loadLeaderboards() {

        databaseService.getGroupMap(new DatabaseService.DatabaseCallback<Map<String, Group>>() {
            @Override
            public void onCompleted(Map<String, Group> groupMap) {

                databaseService.getUserList(new DatabaseService.DatabaseCallback<List<User>>() {

                    @Override
                    public void onCompleted(List<User> users) {

                        List<User> topCar = getTop10(users,
                                (u1, u2) -> Integer.compare(u2.carGameRecord, u1.carGameRecord));

                        List<User> topSnake = getTop10(users,
                                (u1, u2) -> Integer.compare(u2.snakeRecord, u1.snakeRecord));

                        List<User> topPiano = getTop10(users,
                                (u1, u2) -> Integer.compare(u2.pianoRecord, u1.pianoRecord));

                        carAdapter.setGroupsMap(groupMap);
                        snakeAdapter.setGroupsMap(groupMap);
                        pianoAdapter.setGroupsMap(groupMap);

                        carAdapter.setUsers(topCar);
                        snakeAdapter.setUsers(topSnake);
                        pianoAdapter.setUsers(topPiano);
                    }

                    @Override
                    public void onFailed(Exception e) {
                        Toast.makeText(LeaderBordActivity.this, "שגיאה", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(LeaderBordActivity.this, "שגיאה בטעינת קבוצות", Toast.LENGTH_SHORT).show();
            }
        });
    }
}