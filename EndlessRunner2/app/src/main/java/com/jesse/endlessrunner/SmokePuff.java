package com.jesse.endlessrunner;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by jesse on 3/4/2016.
 * The smoke puff created behind the moving player object
 * Completely aesthetic has no effect on game play
 */
public class SmokePuff extends GameObject {
    public int r;

    /*
        Updates the position based on player position
     */
    public SmokePuff(int x, int y) {
        r = 5;
        super.x = x;
        super.y = y;
    }
    /*
        updates the position at which the smoke puffs should be drawn
    */
    public void update() {
        x -= 10;
    }
    /*
        Draw them
     */
    public void draw(Canvas canvas) {
        //sets the color
        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.FILL);

        //draws three circles behind the player object.
        canvas.drawCircle(x - r, y - r, r, paint);
        canvas.drawCircle(x - r + 2, y - r - 2, r, paint);
        canvas.drawCircle(x - r + 4, y - r + 1, r, paint);
    }
}
