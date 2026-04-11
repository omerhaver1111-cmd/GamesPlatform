package com.example.gamesplatform.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.example.gamesplatform.R;
import com.example.gamesplatform.models.Car;
import com.example.gamesplatform.models.Coin;
import com.example.gamesplatform.models.GameObject;
import com.example.gamesplatform.models.Obstacle;

import java.util.ArrayList;
import java.util.Random;

public class CarEscapeGameView extends View {

    // ממשק כדי להודיע ל-Activity שהמשחק נגמר
    public interface GameCallback {
        void onGameOver(int seconds, int coins);
        int getCurrentUserMoney(); // כדי לקבל את הכסף הנוכחי לתצוגה
    }

    private GameCallback callback;
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
    private Bitmap[] obstacleBmps;
    private int spawnCounter = 0;

    public CarEscapeGameView(Context context, int width, int height, TextView timerView, GameCallback callback) {
        super(context);
        this.screenWidth = width;
        this.screenHeight = height;
        this.timerView = timerView;
        this.callback = callback;

        paint = new Paint();

        car = new Car(screenWidth, screenHeight,
                BitmapFactory.decodeResource(getResources(), R.drawable.car_1));

        coinBmp = BitmapFactory.decodeResource(getResources(), R.drawable.coin);

        obstacleBmps = new Bitmap[7];
        for (int i = 0; i < 7; i++) {
            String carName = "car_" + (i + 1);
            int resId = getResources().getIdentifier(carName, "drawable", getContext().getPackageName());
            if (resId != 0) {
                obstacleBmps[i] = BitmapFactory.decodeResource(getResources(), resId);
            }
        }

        startTimer();
    }

    private void startTimer() {
        timer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                seconds++;
                int min = seconds / 60;
                int sec = seconds % 60;
                timerView.setText(String.format("%02d:%02d", min, sec));
            }

            @Override
            public void onFinish() {}
        };
        timer.start();
    }

    private void spawnObject() {
        int x = random.nextInt(screenWidth - 150);
        int r = random.nextInt(4);

        if (r == 0) {
            objects.add(new Coin(x, 0, coinBmp));
        } else {
            int index = random.nextInt(7);
            Bitmap selectedBmp = obstacleBmps[index];
            objects.add(new Obstacle(x, 0, selectedBmp));
        }
    }

    private void update() {
        if (gameOver) return;

        spawnCounter++;
        if (spawnCounter > 40) {
            spawnObject();
            spawnCounter = 0;
        }

        for (int i = 0; i < objects.size(); i++) {
            GameObject obj = objects.get(i);
            obj.update();

            if (Rect.intersects(obj.getRect(), car.getRect())) {
                if (obj instanceof Coin) coins++;
                if (obj instanceof Obstacle) lives--;

                obj.deactivate();

                if (lives <= 0) {
                    gameOver = true;
                    timer.cancel();
                    if (callback != null) {
                        callback.onGameOver(seconds, coins);
                    }
                    return;
                }
            }

            if (!obj.isActive()) {
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
        for (GameObject obj : objects) {
            obj.draw(canvas);
        }
        drawUI(canvas);
        if (!gameOver) invalidate();
    }

    private void drawUI(Canvas canvas) {
        paint.setColor(Color.WHITE);
        paint.setTextSize(50);
        canvas.drawText("Lives: " + lives, 20, 60, paint);
        canvas.drawText("Coins: " + coins, 20, 120, paint);

        if (callback != null) {
            canvas.drawText("Money: " + callback.getCurrentUserMoney(), 20, 180, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            car.move((int) event.getX(), screenWidth);
        }
        return true;
    }
}