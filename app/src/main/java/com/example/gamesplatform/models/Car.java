package com.example.gamesplatform.models;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class Car {
    public int x, y, width, height;
    private Bitmap carIcon;

    public Car(int screenWidth, int screenHeight, Bitmap originalBitmap) {
        width = 140; height = 240; // גודל המכונית
        x = screenWidth / 2 - width / 2;
        y = screenHeight - height - 50;
        // יצירת גרסה מוקטנת של התמונה לגודל שקבענו
        carIcon = Bitmap.createScaledBitmap(originalBitmap, width, height, false);
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(carIcon, x, y, null);
    }

    public void move(int newX, int screenWidth) {
        x = newX - width / 2;
        if (x < 0) x = 0;
        if (x > screenWidth - width) x = screenWidth - width;
    }

    public Rect getRect() {
        return new Rect(x, y, x + width, y + height);
    }
}
