package com.andorangutan.snake;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Build;
import android.view.MotionEvent;
import android.graphics.Point;
import android.util.Log;
import android.graphics.Color;
import android.graphics.Canvas;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GameState implements SnakeGameBroadcaster {

    private Snake snake;
    private Apple apple;
    private int score;

    private long nextFrameTime;

    private int eatID = -1;
    private int crashID = -1;
    private Audio audio;


    private HUD hud;
    private ArrayList<InputObserver>
            inputObservers = new ArrayList<>();
    private UIController uiController;

    private Thread thread = null;

    public Board board;
    public Graphics graphics;

    private volatile boolean playing = false;
    private volatile boolean paused = true;
    private volatile boolean gameOver = true;

    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("Scores");
    private int[] scoreArray = new int[5];
    private String[] dateArray = new String[5];

    public GameState(Context context, Graphics graphics) {

        this.graphics = graphics;
        this.audio = new Audio();

        // Setup Board
        this.board = new Board(40, graphics.getHorizontalPixels(), graphics.getVerticalPixels());


        // TODO: Move to snake and apple
        this.eatID = this.audio.load(context, "get_apple.ogg");
        this.crashID = this.audio.load(context ,"snake_death.ogg");

        this.apple = new Apple(context, board);

        this.snake = new Snake(context, board);

        this.hud = new HUD(new Point(graphics.getHorizontalPixels(),graphics.getVerticalPixels()));
        uiController = new UIController(this);
    }

    public void addObserver(InputObserver o) {
        inputObservers.add(o);
    }
    public void newGame() {

        this.snake.reset();

        // Get the apple ready for dinner
        this.apple.spawn();

        // Reset the mScore
        this.score = 0;

        // Setup mNextFrameTime so an update can triggered
        this.nextFrameTime = System.currentTimeMillis();
    }

    public void update() {
        if (this.updateRequired()) {

            // Move the snake
            this.snake.move();

            // Did the head of the snake eat the apple?
            if(this.snake.checkDinner(this.apple.getLocation())){
                // This reminds me of Edge of Tomorrow.
                // One day the apple will be ready!
                this.apple.spawn();

                // Add to  mScore
                this.score = this.score + 1;

                // Play a sound
                this.audio.play(this.eatID);
            }

            // Did the snake die?
            if (this.snake.detectDeath()) {
                // Pause the game ready to start again
                this.audio.play(this.crashID);

                writeScores(this.score);
                readScores();

                this.paused = true;
                this.gameOver = true;
            }
        }
    }

    private boolean updateRequired() {

        // Run at 10 frames per second
        long TARGET_FPS = 10;
        TARGET_FPS = TARGET_FPS + (this.score / 3);
        // There are 1000 milliseconds in a second
        final long MILLIS_PER_SECOND = 1000;

        // Are we due to update the frame
        if(this.nextFrameTime <= System.currentTimeMillis()){
            // Tenth of a second has passed

            // Setup when the next update will be triggered
            this.nextFrameTime =System.currentTimeMillis()
                    + MILLIS_PER_SECOND / TARGET_FPS;

            // Return true so that the update and draw
            // methods are executed
            return true;
        }

        return false;
    }

    public void pause() {
        this.playing = false;
        try {
            this.thread.join();
        } catch (InterruptedException e) {
            // Error
            Log.e("Thread", "InterruptedException occurred while waiting in join()", e);
        }
    }

    public void resusme(Runnable target) {
        this.playing = true;
        this.thread = new Thread(target);
        this.thread.start();
    }

    // Do all the drawing
    public void draw(Context context) {
        // Get a lock on the mCanvas
        if (graphics.getSurfaceHolder().getSurface().isValid()) {
            graphics.canvas = graphics.getSurfaceHolder().lockCanvas();

            Bitmap bitmapBackground = BitmapFactory.decodeResource(context.getResources(), R.drawable.background);
            graphics.canvas.drawBitmap(bitmapBackground, 0, 0, null);

            Typeface comicFont = context.getResources().getFont(R.font.comicsansms);
            graphics.paint.setTypeface(comicFont);

            // Fill the screen with a color
           //graphics.canvas.drawColor(Color.argb(255, 26, 128, 182));

            // Set the size and color of the mPaint for the text
            graphics.paint.setColor(Color.argb(255, 255, 255, 255));
            graphics.paint.setTextSize(120);

            // Draw the score
            graphics.canvas.drawText("" + this.score, 20, 120, graphics.paint);

            // Draw the apple and the snake
            this.apple.draw(graphics.canvas, graphics.paint);
            this.snake.draw(graphics.canvas, graphics.paint);
            hud.draw(graphics.canvas, graphics.paint,this);

            // Draw some text while paused
            //if(this.paused && this.gameOver){
            if (this.paused && (apple.getLocation().x == -10)) {
                // Set the size and color of the mPaint for the text
                graphics.paint.setColor(Color.argb(255, 255, 255, 255));
                graphics.paint.setTextSize(250);

                // Draw the message
                // We will give this an international upgrade soon
                //mCanvas.drawText("Tap To Play!", 200, 700, mPaint);
                graphics.canvas.drawText(context.getResources().
                                getString(R.string.tap_to_play),
                        450, 700, graphics.paint);
            }
            else if (this.paused && this.gameOver)
            {
                graphics.paint.setColor(Color.argb(255, 255, 255, 255));
                graphics.paint.setTextSize(250);

                Typeface deathFont = context.getResources().getFont(R.font.optimusprincepssemibold);

                graphics.paint.setTypeface(deathFont);
                //graphics.canvas.drawText(context.getResources().
                //                getString(R.string.you_died),
                 //       500, 700, graphics.paint);

                graphics.paint.setTextSize(180);
                graphics.canvas.drawText("High Scores: ", 520, 280, graphics.paint);
                graphics.paint.setTextSize(100);

                for (int i = 0; i < 5; i++) {
                    int y = 450 + (i * 100);
                    if (scoreArray[i] == 0) {
                        graphics.canvas.drawText("#" + (i + 1) + ": N/A", 500, y, graphics.paint);
                    }
                    else {
                        graphics.canvas.drawText("#" + (i + 1) + ": " + scoreArray[i] + "pts - " +
                                dateArray[i], 500, y, graphics.paint);
                    }
                }
            }


            // Unlock the mCanvas and reveal the graphics for this frame
            graphics.getSurfaceHolder().unlockCanvasAndPost(graphics.canvas);
        }
    }


    public Thread getThread() {
        return this.thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public boolean onTouchPassthrough(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (this.isPaused() && this.isGameOver()) {
                    this.setPaused(false);
                    this.setGameOver(false);
                    this.newGame();

                    // Don't want to process snake direction for this tap
                    return true;
                }

                // Let the Snake class handle the input
                this.snake.switchHeading(motionEvent);
                break;

            default:
                break;

        }
        for (InputObserver o : inputObservers) {
            o.handleInput(motionEvent, this,
                    hud.getControls());
        }
        return true;
    }

    public void writeScores(int score) {
        String date = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
        String device = Build.MANUFACTURER;
        Scores playerScore = new Scores(device, score);


        myRef.child(date).setValue(playerScore);
    }

    public void readScores() {
        myRef.orderByValue().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Map.Entry<String, Integer>> highScores = new ArrayList<>();
                for (DataSnapshot scoreSnapshot : dataSnapshot.getChildren()) {
                    String dateString = scoreSnapshot.getKey();
                    int score = scoreSnapshot.child("score").getValue(Integer.class);
                    highScores.add(new AbstractMap.SimpleEntry<>(dateString, score));
                }

                Collections.sort(highScores, (entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

                for (int i = 0; i < Math.min(5, highScores.size()); i++) {
                    Map.Entry<String, Integer> entry = highScores.get(i);
                    scoreArray[i] = entry.getValue();
                    dateArray[i] = entry.getKey();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("Failed to get scores - " + error.getMessage());
            }
        });
    }
    public boolean isGameOver() {
        return this.gameOver;
    }

    public void setGameOver(boolean flag) {
        this.gameOver = flag;
    }

    public boolean isPlaying() {
        return this.playing;
    }

    public void setPlaying(boolean flag) {
        this.playing = flag;
    }


    public boolean isPaused() {
        return this.paused;
    }

    public void setPaused(boolean flag) {
        this.paused = flag;
    }
}
