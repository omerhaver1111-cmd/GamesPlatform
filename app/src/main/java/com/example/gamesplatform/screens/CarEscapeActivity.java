//package com.example.gamesplatform.screens;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.Rect;
//import android.os.Bundle;
//import android.view.MotionEvent;
//import android.view.View;
//import android.widget.FrameLayout;
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//import com.example.gamesplatform.R;
//import com.example.gamesplatform.models.Car;
//import com.example.gamesplatform.models.Obstacle;
//
//import java.util.ArrayList;
//import java.util.Random;
//
//public class CarEscapeActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_car_escape);
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//
//        final FrameLayout container = findViewById(R.id.game_container);
//        container.post(() -> {
//            int width = container.getWidth();
//            int height = container.getHeight();
//            GameView gameView = new GameView(CarEscapeActivity.this, width, height);
//            container.addView(gameView);
//        });
//    }
//
//    public class GameView extends View {
//        private Car car;
//        private ArrayList<Obstacle> obstacles;
//        private int screenWidth, screenHeight;
//        private float speed = 15f;
//        private final float MAX_SPEED = 90f;
//        private long lastObstacleTime = 0;
//        private Paint obstaclePaint;
//        private boolean isGameOver = false;
//
//        public GameView(Context context, int width, int height) {
//            super(context);
//            this.screenWidth = width;
//            this.screenHeight = height;
//
//            // טעינת התמונה
//            Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.car_1);
//            this.car = new Car(width, height, originalBitmap);
//
//            this.obstacles = new ArrayList<>();
//            obstaclePaint = new Paint();
//            obstaclePaint.setColor(Color.RED);
//        }
//
//        @Override
//        public boolean onTouchEvent(MotionEvent event) {
//            if (!isGameOver && (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE)) {
//                car.move((int) event.getX(), screenWidth);
//            }
//            return true;
//        }
//
//        @Override
//        protected void onDraw(Canvas canvas) {
//            super.onDraw(canvas);
//            if (isGameOver) {
//                Paint textPaint = new Paint();
//                textPaint.setColor(Color.WHITE);
//                textPaint.setTextSize(100);
//                textPaint.setTextAlign(Paint.Align.CENTER);
//                canvas.drawText("GAME OVER", screenWidth / 2f, screenHeight / 2f, textPaint);
//                return;
//            }
//
//            if (speed < MAX_SPEED) {
//                speed += 0.01f;
//            }
//
//            if (System.currentTimeMillis() - lastObstacleTime > 1000) {
//                obstacles.add(new Obstacle(screenWidth, (int)speed));
//                lastObstacleTime = System.currentTimeMillis();
//            }
//
//            // ציור המכונית (התמונה)
//            car.draw(canvas);
//
//            for (int i = 0; i < obstacles.size(); i++) {
//                Obstacle o = obstacles.get(i);
//                o.update();
//                canvas.drawRect(o.getRect(), obstaclePaint);
//
//                // בדיקת התנגשות מדויקת
//                if (Rect.intersects(car.getRect(), o.getRect())) {
//                    isGameOver = true;
//                }
//
//                if (o.y > screenHeight) {
//                    obstacles.remove(i);
//                    i--;
//                }
//            }
//            postInvalidateDelayed(16);
//        }
//    }
//}

package com.example.gamesplatform.screens;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.BitmapFactory;
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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.ArrayList;
import java.util.Random;

import com.example.gamesplatform.models.GameObject;
import com.example.gamesplatform.models.Coin;
import com.example.gamesplatform.models.Obstacle;

public class CarEscapeActivity extends AppCompatActivity {

    private TextView timer_text_view;
    private FrameLayout game_container;

    private LinearLayout game_over_layout;
    private TextView time_text;
    private TextView record_text;
    private Button restart_btn;
    private Button home_btn;

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

        game_over_layout = findViewById(R.id.game_over_layout);
        time_text = findViewById(R.id.time_text);
        record_text = findViewById(R.id.record_text);
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
    public void showGameOver(int time,int record){
        timer_text_view.setText("0");
        game_over_layout.setVisibility(View.VISIBLE);

        time_text.setText("Time: " + time);
        record_text.setText("Record: " + record);
    }

    public class GameView extends View {

        private int screenWidth;
        private int screenHeight;
        private Car car;
        private Paint paint;
        private int lives = 3;
        private int score = 0;
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
            //coinBmp = BitmapFactory.decodeResource(getResources(),R.drawable.coin);
            //obstacleBmp = BitmapFactory.decodeResource(getResources(),R.drawable.obstacle);
            coinBmp = null;
            obstacleBmp = null;
            startTimer();
        }

        private void startTimer() {
            timer = new CountDownTimer(60000,1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long sec = millisUntilFinished / 1000;
                    timerView.setText(String.valueOf(sec));
                }
                @Override
                public void onFinish() {
                    timerView.setText("0");
                }
            };
            timer.start();
        }

        private void spawnObject(){
            int x = random.nextInt(screenWidth-150);

            if(random.nextBoolean()){
                objects.add(new Coin(x,0,coinBmp));}
            else{
                objects.add(new Obstacle(x,0,obstacleBmp));}
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

                if(obj.getRect().intersect(car.getRect())){
                    if(obj instanceof Coin){
                        score += 10;}
                    if(obj instanceof Obstacle){
                        lives--;}
                    obj.deactivate();

                    if(lives <= 0){
                        gameOver = true;
                        timer.cancel();
                        CarEscapeActivity act =
                                (CarEscapeActivity)getContext();
                        act.showGameOver(score,score);
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

            drawLives(canvas);
            if(!gameOver){
                invalidate();
            }
        }

        private void drawLives(Canvas canvas) {
            paint.setColor(Color.WHITE);
            paint.setTextSize(50);
            canvas.drawText("Lives: " + lives,20,60,paint);
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