package com.jesse.endlessrunner;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by jesse on 3/4/2016.
 * Same as Bottom border but now top
 */
public class TopBorder extends GameObject {
    private Bitmap image;

    /*
        Used to create the bitmap.
     */
    public TopBorder(Bitmap res, int x, int y, int h) {
        height = h;
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
        try {
            canvas.drawBitmap(image, x, y, null);
        } catch (Exception e) {

        }
    }
}
