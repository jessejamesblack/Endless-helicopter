package com.jesse.endlessrunner;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by jesse on 3/4/2016.
 * The Background class is used to display the background image you see.
 */
public class Background {
    private Bitmap image;
    private int x, y, dx;

    public Background(Bitmap res) {
        image = res;
        dx = GamePanel.MOVESPEED;
    }
    //cycles through the image. You only ever see one image.
    public void update() {
        x += dx;
        if (x < -GamePanel.WIDTH) {
            x = 0;
        }
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(image, x, y, null);
        if (x < 0) {
            canvas.drawBitmap(image, x + GamePanel.WIDTH, y, null);
        }
    }
}
