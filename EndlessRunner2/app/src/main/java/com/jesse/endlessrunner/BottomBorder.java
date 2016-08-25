package com.jesse.endlessrunner;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by jesse on 3/4/2016.
 * This is the bottom border, the growing and shrinking object you see at the bottom of the screen
 * This will kill you if you hit it.
 * It grows and shrinks based on players score.
 */
public class BottomBorder extends GameObject {
    private Bitmap image;

    /*
        Used to create the bitmap.
     */
    public BottomBorder(Bitmap res, int x, int y) {
        height = 200;
        width = 20;

        this.x = x;
        this.y = y;
        dx = GamePanel.MOVESPEED;

        image = Bitmap.createBitmap(res, 0, 0, width, height);
    }
    /*
        Update the position
     */
    public void update() {
        x += dx;
    }
    /*
        Draw the image.
     */
    public void draw(Canvas canvas) {
        canvas.drawBitmap(image, x, y, null);
    }
}
