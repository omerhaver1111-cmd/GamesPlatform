package com.example.gamesplatform.screens;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gamesplatform.services.DatabaseService;
import com.google.firebase.FirebaseApp;


public class BaseActivity extends AppCompatActivity {

    protected DatabaseService databaseService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this);

        /// get the instance of the database service
        databaseService = DatabaseService.getInstance();
    }


}