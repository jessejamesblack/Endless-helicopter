package com.jesse.endlessrunner;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by jesse on 3/4/2016.
 * This is the primary game class.
 */
public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;
    public static final int MOVESPEED = -5;
    SharedPreferences gamePrefs = getContext().getSharedPreferences("HighScoreList",
            Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = gamePrefs.edit();
    private long smokeStartTime;
    private long missileStartTime;
    private MainThread thread;
    private Background bg;
    private Player player;
    private ArrayList<SmokePuff> smoke;
    private ArrayList<Missile> missiles;
    private ArrayList<TopBorder> topBorder;
    private ArrayList<BottomBorder> bottomBorder;
    private Random rand = new Random();
    private int maxBorderHeight;
    private int minBorderHeight;
    private int progessDenom = 20;      //difficulty
    private boolean topDown = true;     //borders up or down? default both move downwards. later reverse.
    private boolean botDown = true;
    private boolean newGameCreated;
    private Explosion explosion;
    private long startReset;
    private boolean reset;
    private boolean disappear;
    private boolean started;
    //play death sound
    MediaPlayer deathSound = MediaPlayer.create(getContext(), R.raw.death);
    //play helicopter sound
    MediaPlayer helicopterSound = MediaPlayer.create(getContext(), R.raw.helicopter);

    /*
    Gets the context for the game.
     */
    public GamePanel(Context context) {
        super(context);
        //add the callback to the shareholder to intercept events
        getHolder().addCallback(this);
        //make gamePanel focusable so it can handle events
        setFocusable(true);
    }

    /*
        Creates the game and starts the game loop.
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.grassbg1));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.helicopter), 65, 25, 3);
        smoke = new ArrayList<SmokePuff>();
        missiles = new ArrayList<Missile>();
        topBorder = new ArrayList<TopBorder>();
        bottomBorder = new ArrayList<BottomBorder>();
        smokeStartTime = System.nanoTime();
        missileStartTime = System.nanoTime();

        thread = new MainThread(getHolder(), this);

        //we can safely start the game loop
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /*
        If the player is destroyed automatically retry.
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        int counter = 0;
        while (retry && counter < 1000) {
            counter++;
            try {
                thread.setRunning(false);
                thread.join();
                retry = false;
                //set null so garbage collector can pick up the object
                thread = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /*
        onTouchEvent start the game
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //if player pressed down on the screen start the game
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!player.getPlaying() && newGameCreated && reset) {
                player.setPlaying(true);
                player.setUp(true);
            }
            if (player.getPlaying()) {
                if (!started) started = true;
                reset = false;
                player.setUp(true);
            }
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            player.setUp(false);
            return true;
        }
        return super.onTouchEvent(event);
    }

    /*
        Update all the things!
     */
    public void update() {
        //if player is playing spawn entities
        if (player.getPlaying()) {
            //play helicopter sound
            helicopterSound.start();
            if (bottomBorder.isEmpty()) {
                player.setPlaying(false);
                return;
            }
            if (topBorder.isEmpty()) {
                player.setPlaying(false);
                return;
            }
            bg.update();
            player.update();
            // calculate height of borders based on score
            //max and min boarder and the boarder switch direction when max or min is met

            maxBorderHeight = 30 + player.getScore() / progessDenom;
            //cap max border height so that borders can only take ip a total of 1/2 screen
            if (maxBorderHeight > HEIGHT / 4)
                maxBorderHeight = HEIGHT / 4;
            minBorderHeight = 5 + player.getScore() / progessDenom;

            //check bottom border collision
            for (int i = 0; i < bottomBorder.size(); i++) {
                if (collision(bottomBorder.get(i), player)) {
                    player.setPlaying(false);
                }
            }
            //check top border collision
            for (int i = 0; i < topBorder.size(); i++) {
                if (collision(topBorder.get(i), player)) {
                    player.setPlaying(false);
                }
            }

            //update borders
            this.updateTopBorder();
            this.updateBottomBorder();
            //add missiles on timer
            long missileElapsed = (System.nanoTime() - missileStartTime) / 1000000;
            //the higher player score the higher chance of missiles
            if (missileElapsed > (2000 - player.getScore() / 4)) {
                //first missile goes middle
                if (missiles.size() == 0) {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),
                            R.drawable.missile), WIDTH + 10, HEIGHT / 2,
                            45, 15, player.getScore(), 13));
                    //rest are random
                } else {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),
                            R.drawable.missile), WIDTH + 10,
                            (int) (rand.nextDouble() * (HEIGHT - (maxBorderHeight * 2)) + maxBorderHeight),
                            45, 15, player.getScore(), 13));
                }
                //reset timer
                missileStartTime = System.nanoTime();
            }
            //loop through every missile and check if collision
            for (int i = 0; i < missiles.size(); i++) {
                missiles.get(i).update();
                if (collision(missiles.get(i), player)) {
                    //update missile
                    missiles.remove(i);
                    player.setPlaying(false);
                    break;
                }
                //if missile off screen remove it
                if (missiles.get(i).getX() < -100) {
                    missiles.remove(i);
                    break;
                }
            }
            //add smoke puffs on timer
            long elapsed = (System.nanoTime() - smokeStartTime) / 1000000;
            if (elapsed > 120) {
                smoke.add(new SmokePuff(player.getX(), player.getY() + 10));
                smokeStartTime = System.nanoTime();
            }
            //get rid of smoke
            for (int i = 0; i < smoke.size(); i++) {
                smoke.get(i).update();
                if (smoke.get(i).getX() < -10) {
                    smoke.remove(i);
                }
            }
        } else {
            player.resetDY();
            if (!reset) {
                newGameCreated = false;
                startReset = System.nanoTime();
                reset = true;
                disappear = true;
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(),
                        R.drawable.explosion), player.getX(), player.getY() - 30, 100, 100, 25);
            }
            explosion.update();

            long resetElapsed = (System.nanoTime() - startReset) / 1000000;
            if (resetElapsed > 2500 && !newGameCreated) {
                newGame();
            }
        }
    }

    /*
        Detect collisions and save scores when a player collides with something.
     */
    public boolean collision(GameObject a, GameObject b) {
        //Detect if player has collided with something.
        if (Rect.intersects(a.getRectangle(), b.getRectangle())) {

            helicopterSound.pause();
            deathSound.start();
            editor.clear();
            //initialize highScore and score.
            int highScore = gamePrefs.getInt("HighScore", 0);
            int score = player.getScore();
            //if a collision happen and highScore is equal to zero, update it with current score.
            if (highScore == 0) {
                editor.putInt("HighScore", score);
                editor.commit();
                System.out.println("Score updated: " + gamePrefs.getInt("HighScore", 0));
                //if highScore is less than score update it with the new score.
            } else if (highScore < score) {
                editor.putInt("HighScore", score);
                editor.commit();
                //Debug statement   System.out.println("Score updated: " + gamePrefs.getInt("HighScore", 0));
                //if highScore is greater than score do nothing.
            } else if (highScore > score)
                //Debug statement.
                System.out.print("Nothing to do.");
            return true;
        }
        return false;
    }

    /*
        Draw canvas and save the state
     */
    public void draw(Canvas canvas) {
        final float scaleFactorX = getWidth() / (WIDTH * 1.f);
        final float scaleFactorY = getHeight() / (HEIGHT * 1.f);
        if (canvas != null) {
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            if (!disappear) {
                player.draw(canvas);
            }
            //draw smoke
            for (SmokePuff sp : smoke) {
                sp.draw(canvas);
            }
            //draw missiles
            for (Missile m : missiles) {
                m.draw(canvas);
            }

            //draw borders
            for (TopBorder tb : topBorder) {
                tb.draw(canvas);
            }
            for (BottomBorder bt : bottomBorder) {
                bt.draw(canvas);
            }
            //draw explosion
            if (started) {
                explosion.draw(canvas);
            }
            drawText(canvas);
            canvas.restoreToCount(savedState);
        }
    }

    /*
        Update the top border
     */
    public void updateTopBorder() {
        //every 50 points. insert randomly places top blocks that break the pattern
        if (player.getScore() % 50 == 0) {
            //update top border but use brick2
            topBorder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                    R.drawable.brick2), topBorder.get(topBorder.size() - 1).getX() + 20,
                    0, (int) ((rand.nextDouble() * (maxBorderHeight)) + 1)));
        }
        for (int i = 0; i < topBorder.size(); i++) {
            topBorder.get(i).update();
            if (topBorder.get(i).getX() < -20) {
                topBorder.remove(i);
                //remove and replace

                //calc top down which determines if the border is moving up or down
                if (topBorder.get(topBorder.size() - 1).getHeight() >= maxBorderHeight) {
                    topDown = false;
                }
                if (topBorder.get(topBorder.size() - 1).getHeight() <= minBorderHeight) {
                    topDown = true;
                }
                //new border will have larger height
                if (topDown) {
                    topBorder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick2), topBorder.get(topBorder.size() - 1).getX() + 20,
                            0, topBorder.get(topBorder.size() - 1).getHeight() + 1));
                } else {
                    topBorder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick2), topBorder.get(topBorder.size() - 1).getX() + 20,
                            0, topBorder.get(topBorder.size() - 1).getHeight() - 1));
                }
            }
        }
    }

    /*
        Update the bottom border.
     */
    public void updateBottomBorder() {
        //every 40 points insert randomly placed bottom blocks
        if (player.getScore() % 40 == 0) {
            bottomBorder.add(new BottomBorder(BitmapFactory.decodeResource(getResources(),
                    R.drawable.brick), bottomBorder.get(bottomBorder.size() - 1).getX() + 20,
                    (int) ((rand.nextDouble() * maxBorderHeight) + (HEIGHT - maxBorderHeight))));
        }
        //update bottom border
        for (int i = 0; i < bottomBorder.size(); i++) {
            bottomBorder.get(i).update();

            //if border is moving off screen remove and add new one
            if (bottomBorder.get(i).getX() < -20) {
                bottomBorder.remove(i);

                //calc top down which determines if the border is moving up or down
                if (bottomBorder.get(bottomBorder.size() - 1).getHeight() >= maxBorderHeight) {
                    botDown = false;
                }
                if (bottomBorder.get(bottomBorder.size() - 1).getHeight() <= minBorderHeight) {
                    botDown = true;
                }
                if (botDown) {
                    bottomBorder.add(new BottomBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick), bottomBorder.get(bottomBorder.size() - 1).getX() + 20,
                            bottomBorder.get(bottomBorder.size() - 1).getY() + 1));
                } else {
                    bottomBorder.add(new BottomBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick), bottomBorder.get(bottomBorder.size() - 1).getX() + 20,
                            bottomBorder.get(bottomBorder.size() - 1).getY() - 1));
                }
            }

        }
    }

    /*
        Create a new game
     */
    public void newGame() {
        //clear everything reset player position and score
        disappear = false;

        bottomBorder.clear();
        topBorder.clear();

        missiles.clear();
        smoke.clear();

        minBorderHeight = 5;
        maxBorderHeight = 30;

        player.resetDY();
        player.resetScore();
        player.setY(HEIGHT / 2);


        //create initial borders
        //initial top border
        for (int i = 0; i * 20 < WIDTH + 40; i++) {
            //first border created
            if (i == 0) {
                topBorder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                        R.drawable.brick2), i * 20, 0, 10));
            } else {
                topBorder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                        R.drawable.brick2), i * 20, 0, topBorder.get(i - 1).getHeight() + 1));
            }
        }
        //initial bottom border
        for (int i = 0; i * 20 < WIDTH + 40; i++) {
            if (i == 0) {
                bottomBorder.add(new BottomBorder(BitmapFactory.decodeResource(getResources(),
                        R.drawable.brick), i * 20, HEIGHT - minBorderHeight));
            }
            //adding borders until the initial screen is filled
            else {
                bottomBorder.add(new BottomBorder(BitmapFactory.decodeResource(getResources(),
                        R.drawable.brick), i * 20, bottomBorder.get(i - 1).getY() - 1));
            }
        }
        newGameCreated = true;
    }

    /*
        Draw instructions, current score, and best score.
     */
    public void drawText(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(20.0f);
        //draw the text for current and best distance
        canvas.drawText("Current Distance: " + player.getScore(), 10, 30, paint);
        //gamePrefs takes the Int from "HighScore" and displays it.
        canvas.drawText("Best Distance: " + gamePrefs.getInt("HighScore", 0), WIDTH - 600, 30, paint);

        //if a new game is created show the controls
        if (!player.getPlaying() && newGameCreated && reset) {
            Paint paint1 = new Paint();
            paint1.setColor(Color.WHITE);
            paint1.setTextSize(40);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("Press to begin", WIDTH / 2 - 50, HEIGHT / 2, paint1);

            paint1.setTextSize(20);
            paint.setColor(Color.WHITE);
            canvas.drawText("Press and hold to move up", WIDTH / 2 - 50, HEIGHT / 2 + 20, paint1);
            canvas.drawText("Release to go down", WIDTH / 2 - 50, HEIGHT / 2 + 40, paint1);
        }
    }
}