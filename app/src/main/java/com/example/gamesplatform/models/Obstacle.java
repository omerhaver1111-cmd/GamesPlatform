package com.example.gamesplatform.models;

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