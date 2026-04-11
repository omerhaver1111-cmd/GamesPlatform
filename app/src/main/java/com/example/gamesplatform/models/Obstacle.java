package com.example.gamesplatform.models;
//
//import android.graphics.Rect;
//
//import java.util.Random;
//
//public class Obstacle {
//    public int x, y, width, height, speed;
//
//    public Obstacle(int screenWidth, int currentSpeed) {
//        width = 150;
//        height = 150;
//        speed = currentSpeed;
//        Random random = new Random();
//        // הגרלת מיקום אופקי התחלתי
//        x = random.nextInt(screenWidth - width);
//        // התחלה מעל המסך
//        y = -height;
//    }
//
//    public void update() {
//        y += speed; // המכשול נופל למטה
//    }
//
//    public Rect getRect() {
//        return new Rect(x, y, x + width, y + height);
//    }
//}

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

public class Obstacle extends GameObject {

    private int damage = 1;

    public Obstacle(int x, int y, Bitmap bmp) {
        super(x, y, 140, 240, 12, bmp);
    }

    public int getDamage() {
        return damage;
    }

    @Override
    protected void drawFallback(Canvas canvas) {
        paint.setColor(Color.RED);
        canvas.drawRect(x, y, x + width, y + height, paint);
    }

    @Override
    public void onCollision() {
        active = false;
    }
}