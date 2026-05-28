package com.example.gamesplatform.models;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

public class Coin extends GameObject {

    private int value = 10;

    public Coin(int x, int y, Bitmap bmp) {
        super(x, y, 100, 100, 8, bmp);
    }

    public int getValue() {
        return value;
    }

    @Override
    protected void drawFallback(Canvas canvas) {
        paint.setColor(Color.YELLOW);
        canvas.drawRect(x, y, x + width, y + height, paint);
    }

    @Override
    public void onCollision() {
        active = false;
    }
}