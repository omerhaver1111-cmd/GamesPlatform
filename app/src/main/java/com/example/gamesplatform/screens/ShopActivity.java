package com.example.gamesplatform.screens;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gamesplatform.R;
import com.example.gamesplatform.models.Upgrade;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ShopActivity extends AppCompatActivity {

    private GridLayout upgradesContainer;
    private TextView timerText;

    private List<Upgrade> upgrades;
    private int playerCoins = 1000;

    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        upgradesContainer = findViewById(R.id.upgradesContainer);
        timerText = findViewById(R.id.timerText);

        createUpgrades();
        loadSavedUpgrades();
        loadUpgrades();
        startTimer();
    }

    private void createUpgrades() {
        upgrades = new ArrayList<>();
        upgrades.add(new Upgrade("Speed Boost", "Increase speed by 20%", 200));
        upgrades.add(new Upgrade("Coin Magnet", "Attract coins", 350));
        upgrades.add(new Upgrade("Shield", "Block one hit", 500));
        upgrades.add(new Upgrade("Double Coins", "x2 coins", 800));
    }

    private void loadUpgrades() {
        upgradesContainer.removeAllViews();

        for (Upgrade upgrade : upgrades) {
            View view = LayoutInflater.from(this)
                    .inflate(R.layout.upgrade_item, upgradesContainer, false);

            TextView title = view.findViewById(R.id.title);
            TextView desc = view.findViewById(R.id.desc);
            Button btn = view.findViewById(R.id.buyBtn);

            title.setText(upgrade.getName());
            desc.setText(upgrade.getDescription());

            if (upgrade.isPurchased()) {
                btn.setText("Owned");
                btn.setEnabled(false);
            } else {
                btn.setText("Buy - " + upgrade.getPrice());
                btn.setEnabled(true);
            }

            btn.setOnClickListener(v -> buyUpgrade(upgrade));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(16, 16, 16, 16);

            view.setLayoutParams(params);
            upgradesContainer.addView(view);
        }
    }

    private void buyUpgrade(Upgrade upgrade) {
        if (upgrade.isPurchased()) return;

        if (playerCoins >= upgrade.getPrice()) {
            playerCoins -= upgrade.getPrice();
            upgrade.setPurchased(true);

            saveUpgrades();
            loadUpgrades();
        }
    }

    private void saveUpgrades() {
        SharedPreferences prefs = getSharedPreferences("shop", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        for (int i = 0; i < upgrades.size(); i++) {
            editor.putBoolean("upgrade_" + i, upgrades.get(i).isPurchased());
        }

        editor.apply();
    }

    private void loadSavedUpgrades() {
        SharedPreferences prefs = getSharedPreferences("shop", MODE_PRIVATE);

        for (int i = 0; i < upgrades.size(); i++) {
            boolean purchased = prefs.getBoolean("upgrade_" + i, false);
            upgrades.get(i).setPurchased(purchased);
        }
    }

    private long getTimeUntilReset() {
        Calendar now = Calendar.getInstance();

        Calendar reset = Calendar.getInstance();
        reset.set(Calendar.HOUR_OF_DAY, 13);
        reset.set(Calendar.MINUTE, 0);
        reset.set(Calendar.SECOND, 0);
        reset.set(Calendar.MILLISECOND, 0);

        if (now.after(reset)) {
            reset.add(Calendar.DAY_OF_YEAR, 1);
        }

        return reset.getTimeInMillis() - now.getTimeInMillis();
    }

    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }

        timer = new CountDownTimer(getTimeUntilReset(), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int hours = (int) (millisUntilFinished / (1000 * 60 * 60));
                int minutes = (int) (millisUntilFinished / (1000 * 60)) % 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;

                String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                timerText.setText(time);
            }

            @Override
            public void onFinish() {
                resetUpgrades();
                startTimer();
            }
        }.start();
    }

    private void resetUpgrades() {
        for (Upgrade upgrade : upgrades) {
            upgrade.setPurchased(false);
        }

        saveUpgrades();
        loadUpgrades();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}