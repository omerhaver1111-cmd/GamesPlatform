package com.example.gamesplatform.screens;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gamesplatform.R;
import com.example.gamesplatform.services.AudioService;

public class RecordingActivity extends AppCompatActivity {

    private AudioService audioService;
    private boolean isBound = false;
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioService.AudioBinder binder = (AudioService.AudioBinder) service;
            audioService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };
    private boolean isRecording = false;
    private Button btnRecord, btnPlay, btnStartGame, btnReturnToMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        initViews();
        full_screen();
        setupClickListeners();

        Intent intent = new Intent(this, AudioService.class);
        startService(intent); /// מוודא שהשירות ימשיך לרוץ גם אם ה-Activity תשתנה
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
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

    private void initViews() {
        btnRecord = findViewById(R.id.btnRecord);
        btnPlay = findViewById(R.id.btnPlay);
        btnStartGame = findViewById(R.id.btnStartGame);
        btnReturnToMain = findViewById(R.id.btnReturnToMain);
    }

    private void setupClickListeners() {
        btnRecord.setOnClickListener(v -> {
            if (!isBound) return;

            if (checkAudioPermission()) {
                if (!isRecording) {
                    audioService.startRecording();
                    btnRecord.setText("Stop Recording");
                    isRecording = true;
                } else {
                    audioService.stopRecording();
                    btnRecord.setText("Start Record");
                    isRecording = false;
                }
            } else {
                requestAudioPermission();
            }
        });

        btnPlay.setOnClickListener(v -> {
            if (isBound) {
                audioService.playAudio();
            }
        });

        btnStartGame.setOnClickListener(v -> {
            Intent intent = new Intent(this, PianoActivity.class);
            startActivity(intent);
        });

        btnReturnToMain.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

    }


    private boolean checkAudioPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO}, 200);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200 && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ///אם המשתמש יוצא ההקלטה תיעצר
        if (isRecording && isBound) {
            audioService.stopRecording();
            isRecording = false;
            btnRecord.setText("Start Record");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }
}