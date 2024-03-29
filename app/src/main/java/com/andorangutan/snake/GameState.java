package com.andorangutan.snake;

import android.content.Context;
import android.view.MotionEvent;
import android.graphics.Point;
import android.util.Log;
import android.graphics.Color;
import android.graphics.Canvas;

import java.util.ArrayList;

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

    public GameState(Context context, Graphics graphics) {

        this.graphics = graphics;
        this.audio = new Audio();

        // Setup Board
        this.board = new Board(40, graphics.getHorizontalPixels(), graphics.getVerticalPixels());


        // TODO: Move to snake and apple
        this.eatID = this.audio.load(context, "get_apple.ogg");
        this.crashID = this.audio.load(context ,"snake_death.ogg");

        this.apple = new Apple(context,
                new Point(board.blocksWide,
                        board.blocksHigh),
                board.blockSize);

        this.snake = new Snake(context,
                new Point(board.blocksWide,
                        board.blocksHigh),
                board.blockSize);

        this.hud = new HUD(new Point(graphics.getHorizontalPixels(),graphics.getVerticalPixels()));
        uiController = new UIController(this);
    }

    public void addObserver(InputObserver o) {
        inputObservers.add(o);
    }
    public void newGame() {

        this.snake.reset(graphics.board.blocksWide, graphics.board.blocksHigh);

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

                this.paused = true;
                this.gameOver = true;
            }
        }
    }

    private boolean updateRequired() {

        // Run at 10 frames per second
        final long TARGET_FPS = 10;
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

            // Fill the screen with a color
           graphics.canvas.drawColor(Color.argb(255, 26, 128, 182));

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
            if(this.paused && this.gameOver){

                // Set the size and color of the mPaint for the text
                graphics.paint.setColor(Color.argb(255, 255, 255, 255));
                graphics.paint.setTextSize(250);

                // Draw the message
                // We will give this an international upgrade soon
                //mCanvas.drawText("Tap To Play!", 200, 700, mPaint);
                graphics.canvas.drawText(context.getResources().
                                getString(R.string.tap_to_play),
                        200, 700, graphics.paint);
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
