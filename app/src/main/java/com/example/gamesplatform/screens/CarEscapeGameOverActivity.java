package com.example.gamesplatform.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gamesplatform.R;

public class CarEscapeGameOverActivity extends AppCompatActivity {

    private TextView time_text;
    private TextView record_text;
    private Button restart_btn;
    private Button home_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_escape_game_over);

        time_text = findViewById(R.id.time_text);
        record_text = findViewById(R.id.record_text);
        restart_btn = findViewById(R.id.restart_btn);
        home_btn = findViewById(R.id.home_btn);

        int time = getIntent().getIntExtra("time",0);
        int record = getIntent().getIntExtra("record",0);

        time_text.setText("Time: " + time);
        record_text.setText("Record: " + record);
        restart_btn.setOnClickListener(v -> {
            Intent i =
                    new Intent(
                            CarEscapeGameOverActivity.this,
                            CarEscapeActivity.class
                    );
            startActivity(i);
            finish();
        });

        home_btn.setOnClickListener(v -> {
            finish();
        });
    }
}