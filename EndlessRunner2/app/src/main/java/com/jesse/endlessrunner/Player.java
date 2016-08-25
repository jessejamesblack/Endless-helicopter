package com.jesse.endlessrunner;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by jesse on 3/4/2016.
 * The player class
 * Used to determine players current score and position.
 * Also cycles through the image for the animation.
 */
public class Player extends GameObject {
    private Bitmap spritesheet;
    private int score;
    private boolean up;
    private boolean playing;
    private Animation animation = new Animation();
    private long startTime;

    /*
        Bitmap for the players image and animation
     */
    public Player(Bitmap res, int w, int h, int numFrames) {
        x = 100;
        y = GamePanel.HEIGHT / 2;
        dy = 0;
        score = 0;
        height = h;
        width = w;

        Bitmap[] image = new Bitmap[numFrames];
        spritesheet = res;

        for (int i = 0; i < image.length; i++) {
            image[i] = Bitmap.createBitmap(spritesheet, i + width, 0, width, height);
        }

        animation.setFrames(image);
        animation.setDelay(10);
        startTime = System.nanoTime();
    }
    /*
        Make the player go up
    */
    public void setUp(boolean b) {
        up = b;
    }

    /*
        Update position
    */
    public void update() {
        long elapsed = (System.nanoTime() - startTime) / 1000000;
        if (elapsed > 100) {
            score++;
            startTime = System.nanoTime();
        }
        animation.update();

        if (up) {
            dy -= 1;

        } else {
            dy += 1;
        }

        if (dy > 14)
            dy = 14;
        if (dy < -14)
            dy = -14;

        y += dy * 2;
    }

    /*
        draw the player
     */
    public void draw(Canvas canvas) {
        canvas.drawBitmap(animation.getImage(), x, y, null);
    }

    /*
        get the score used in missile calculations and others.
     */
    public int getScore() {
        return score;
    }

    /*
        Is the player playing?
     */
    public boolean getPlaying() {
        return playing;
    }

    /*
        Set playing to tru
     */
    public void setPlaying(boolean b) {
        playing = b;
    }

    /*
        Reset the player position used when creating a new game after player death
     */
    public void resetDY() {
        dy = 0;
    }

    /*
        Reset the player score used when creating a new game after player death
     */
    public void resetScore() {
        score = 0;
    }
}
