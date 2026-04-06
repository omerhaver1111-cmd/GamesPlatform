package com.example.gamesplatform.screens;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.gamesplatform.R;
import com.example.gamesplatform.models.Car;
import com.example.gamesplatform.models.GameObject;
import com.example.gamesplatform.models.Coin;
import com.example.gamesplatform.models.Obstacle;
import com.example.gamesplatform.models.User;
import com.example.gamesplatform.services.DatabaseService;
import com.example.gamesplatform.utils.SharedPreferencesUtil;

import android.graphics.Bitmap;
import java.util.ArrayList;
import java.util.Random;

public class CarEscapeActivity extends AppCompatActivity {

    private TextView timer_text_view;
    private FrameLayout game_container;

    private LinearLayout game_over_layout;
    private TextView time_text;
    private TextView record_text;
    private TextView coins_text;
    private Button restart_btn;
    private Button home_btn;

    private DatabaseService databaseService;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_car_escape);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseService = DatabaseService.getInstance();
        loadUser();

        game_over_layout = findViewById(R.id.game_over_layout);
        time_text = findViewById(R.id.time_text);
        record_text = findViewById(R.id.record_text);
        coins_text = findViewById(R.id.coins_text);

        restart_btn = findViewById(R.id.restart_btn);
        home_btn = findViewById(R.id.home_btn);

        game_over_layout.setVisibility(View.GONE);

        restart_btn.setOnClickListener(v -> recreate());
        home_btn.setOnClickListener(v -> finish());

        timer_text_view = findViewById(R.id.timer_text_view);
        game_container = findViewById(R.id.game_container);

        game_container.post(new Runnable() {
            @Override
            public void run() {
                int width = game_container.getWidth();
                int height = game_container.getHeight();
                GameView gameView = new GameView(CarEscapeActivity.this, width, height, timer_text_view);
                game_container.addView(gameView);
            }
        });
    }

    private void loadUser(){
        User savedUser = SharedPreferencesUtil.getUser(this);
        if(savedUser == null) return;

        databaseService.getUser(savedUser.getId(), new DatabaseService.DatabaseCallback<User>() {
            @Override
            public void onCompleted(User user) {
                if(user != null) currentUser = user;
            }
            @Override
            public void onFailed(Exception e) {}
        });
    }

    public void showGameOver(int time,int coins){

        game_over_layout.setVisibility(View.VISIBLE);

        if(currentUser == null){
            time_text.setText("Time: " + time + " sec");
            record_text.setText("Coins: " + coins);
            return;
        }

        int record = currentUser.getCarGameRecord();
        boolean isNewRecord = false;

        if(time > record){
            record = time;
            isNewRecord = true;
        }

        if(isNewRecord){
            time_text.setText("NEW RECORD! " + time + " sec");
            time_text.setTextColor(Color.GREEN);
        }
        else{
            time_text.setText("Time: " + time + " sec");
        }
        record_text.setText("Record: " + record);
        coins_text.setText("Coins: " + coins);

        int finalRecord = record;

        databaseService.updateUser(currentUser.getId(), user -> {

            if(user == null) return null;

            int newMoney = user.getMoney() + coins;
            int newexp = user.getExp() + (time - time %10);

            user.setMoney(newMoney);
            user.setExp(newexp);
            user.setCarGameRecord(finalRecord);
            currentUser.setMoney(newMoney);

            return user;

        }, null);
    }

    public class GameView extends View {

        private int screenWidth, screenHeight;
        private Car car;
        private Paint paint;

        private int lives = 3;
        private int coins = 0;
        private int seconds = 0;

        private TextView timerView;
        private CountDownTimer timer;
        private boolean gameOver = false;

        private ArrayList<GameObject> objects = new ArrayList<>();
        private Random random = new Random();

        private Bitmap coinBmp;
        private Bitmap obstacleBmp;

        private int spawnCounter = 0;

        public GameView(Context context,int width,int height,TextView timerView) {
            super(context);
            this.screenWidth = width;
            this.screenHeight = height;
            this.timerView = timerView;

            paint = new Paint();

            car = new Car(screenWidth,screenHeight,
                    BitmapFactory.decodeResource(getResources(),R.drawable.car_1));

            coinBmp = BitmapFactory.decodeResource(getResources(),R.drawable.coin);
            obstacleBmp = null;

            startTimer();
        }

        private void startTimer() {
            timer = new CountDownTimer(Long.MAX_VALUE,1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    seconds++;
                    int min = seconds / 60;
                    int sec = seconds % 60;
                    timerView.setText(String.format("%02d:%02d",min,sec));
                }
                @Override
                public void onFinish() {}
            };
            timer.start();
        }

        private void spawnObject(){
            int x = random.nextInt(screenWidth - 150);
            int r = random.nextInt(4);

            if(r == 0){
                objects.add(new Coin(x,0,coinBmp));
            }
            else
            {   String carName = "car_"+(random.nextInt(7)+1);
                int resId = getResources().getIdentifier(carName,"drawable",getContext().getPackageName());

                if(resId != 0){ //לא קורס אם אין תמונה
                    obstacleBmp = BitmapFactory.decodeResource(getResources(),resId);
                }
                else{
                    obstacleBmp = null;
                }

                objects.add(new Obstacle(x,0,obstacleBmp));
            }
        }

        private void update(){
            if(gameOver) return;

            spawnCounter++;

            if(spawnCounter > 40){
                spawnObject();
                spawnCounter = 0;
            }

            for(int i=0; i < objects.size(); i++){
                GameObject obj = objects.get(i);
                obj.update();

                if(Rect.intersects(obj.getRect(), car.getRect())){

                    if(obj instanceof Coin){
                        coins++;
                    }

                    if(obj instanceof Obstacle){
                        lives--;
                    }

                    obj.deactivate();

                    if(lives <= 0){
                        gameOver = true;
                        timer.cancel();
                        ((CarEscapeActivity)getContext()).showGameOver(seconds,coins);
                        return;
                    }
                }

                if(!obj.isActive()){
                    objects.remove(i);
                    i--;
                }
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            canvas.drawColor(Color.GRAY);

            update();

            car.draw(canvas);

            for(int i=0;i<objects.size();i++){
                objects.get(i).draw(canvas);
            }

            drawUI(canvas);

            if(!gameOver){
                invalidate();
            }
        }

        private void drawUI(Canvas canvas){
            paint.setColor(Color.WHITE);
            paint.setTextSize(50);

            canvas.drawText("Lives: " + lives,20,60,paint);
            canvas.drawText("Coins: " + coins,20,120,paint);

            if(currentUser != null){
                canvas.drawText("Money: " + currentUser.getMoney(),20,180,paint);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if(event.getAction()==MotionEvent.ACTION_MOVE){
                car.move((int)event.getX(),screenWidth);
            }
            return true;
        }
    }
}