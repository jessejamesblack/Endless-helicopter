package com.jesse.endlessrunner;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Random;

/**
 * Created by jesse on 3/4/2016.
 * The missile that you must avoid throughout the game
 */
public class Missile extends GameObject {
    private int score;
    private int speed;
    private Random rand = new Random();
    private Animation animation = new Animation();
    private Bitmap spritesheet;
    /*
        Create the missile and determine its speed based off of the players current score.
     */
    public Missile(Bitmap res, int x, int y, int w, int h, int s, int numFrames) {
        super.x = x;
        super.y = y;
        width = w;
        height = h;
        score = s;

        //speed of missiles increases as time goes
        speed = 7 + (int) (rand.nextDouble() * score / 30);

        //cap missiles speed
        if (speed >= 40)
            speed = 40;

        Bitmap[] image = new Bitmap[numFrames];

        spritesheet = res;

        //loop through image
        for (int i = 0; i < image.length; i++) {
            image[i] = Bitmap.createBitmap(spritesheet, 0, i * height, width, height);
        }

        //send array to animation class
        animation.setFrames(image);
        //spins faster means going faster
        animation.setDelay(100 - (speed));
    }

    public void update() {
        x -= speed;
        animation.update();
    }

    public void draw(Canvas canvas) {
        try {
            canvas.drawBitmap(animation.getImage(), x, y, null);
        } catch (Exception e) {

        }
    }

    @Override
    public int getWidth() {
        //offset slightly
        return width - 10;
    }
}
