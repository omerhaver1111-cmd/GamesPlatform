package com.example.gamesplatform.screens;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
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

public class LeaderBordActivity extends AppCompatActivity {
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
        databaseService = new DatabaseService();

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                // בדיקה שמדובר בהחלקה אופקית
                if (Math.abs(diffX) > Math.abs(diffY)) {

                    if (Math.abs(diffX) > SWIPE_THRESHOLD &&
                            Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {

                        if (diffX > 0) {
                            //  החלקה ימינה → MyGroupActivity
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
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }
}