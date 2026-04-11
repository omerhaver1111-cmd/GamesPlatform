package com.example.gamesplatform.models;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public abstract class GameObject {

    protected int x, y, width, height;
    protected int speed;
    protected Bitmap image;
    protected boolean active = true;

    protected Paint paint = new Paint();

    public GameObject(int x, int y, int w, int h, int speed, Bitmap bmp) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        this.speed = speed;

        if (bmp != null) {
            image = Bitmap.createScaledBitmap(bmp, w, h, false);
        }
    }

    public void update() {
        y += speed;
    }

    public void draw(Canvas canvas) {

        if (!active) return;

        if (image != null) {
            canvas.drawBitmap(image, x, y, null);
        } else {
            drawFallback(canvas);
        }
    }

    protected abstract void drawFallback(Canvas canvas);

    public Rect getRect() {
        return new Rect(x, y, x + width, y + height);
    }

    public void deactivate() {
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    public abstract void onCollision();
}