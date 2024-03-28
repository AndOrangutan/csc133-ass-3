package com.andorangutan.snake;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.util.ArrayList;

class SnakeGame extends SurfaceView implements Runnable,SnakeGameBroadcaster{

    // Objects for the game loop/thread
    private Thread mThread = null;

    private ArrayList<InputObserver>
            inputObservers = new ArrayList<>();
    UIController mUIController;
    // Control pausing between updates
    private long mNextFrameTime;
    // Is the game currently playing and or paused?
    private volatile boolean mPlaying = false;
    private volatile boolean mPaused = true;
    private volatile boolean gameOver = true;

    // for playing sound effects
//    private SoundPool mSP;
    private int eatID = -1;
    private int crashID = -1;

    // // The size in segments of the playable area
    // private final int NUM_BLOCKS_WIDE = 40;
    // private int mNumBlocksHigh;

    // How many points does the player have
    private int mScore;

    // // Objects for drawing
    // private Canvas mCanvas;
    // private SurfaceHolder mSurfaceHolder;
    // private Paint mPaint;

    // A snake ssss
    private Snake mSnake;
    // And an apple
    private Apple mApple;

    private Graphics graphics;

    private Audio audio;
    private HUD hud;

    // This is the constructor method that gets called
    // from SnakeActivity
    public SnakeGame(Context context, Graphics graphics) {
        super(context);

        this.graphics = graphics;
        // // Work out how many pixels each block is
        // int blockSize = size.x / NUM_BLOCKS_WIDE;
        // // How many blocks of the same size will fit into the height
        // mNumBlocksHigh = size.y / blockSize;

        // // Initialize the SoundPool
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //     AudioAttributes audioAttributes = new AudioAttributes.Builder()
        //             .setUsage(AudioAttributes.USAGE_MEDIA)
        //             .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        //             .build();
        //
        //     mSP = new SoundPool.Builder()
        //             .setMaxStreams(5)
        //             .setAudioAttributes(audioAttributes)
        //             .build();
        // } else {
        //     mSP = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        // }
        // try {
        //     AssetManager assetManager = context.getAssets();
        //     AssetFileDescriptor descriptor;
        //
        //     // Prepare the sounds in memory
        //     descriptor = assetManager.openFd("get_apple.ogg");
        //     mEat_ID = mSP.load(descriptor, 0);
        //
        //     descriptor = assetManager.openFd("snake_death.ogg");
        //     mCrashID = mSP.load(descriptor, 0);
        //
        // } catch (IOException e) {
        //     // Error
        // }

        this.audio = new Audio();

        // TODO: Move to snake and apple
        this.eatID = this.audio.load(context, "get_apple.ogg");
        this.crashID = this.audio.load(context ,"snake_death.ogg");

        // // Initialize the drawing objects
        // mSurfaceHolder = getHolder();
        // mPaint = new Paint();

        graphics.setSurfaceHolder(getHolder());

        // Call the constructors of our two game objects
        mApple = new Apple(context,
                new Point(graphics.board.blocksWide,
                        graphics.board.blocksHigh),
                graphics.board.blockSize);

        mSnake = new Snake(context,
                new Point(graphics.board.blocksWide,
                        graphics.board.blocksHigh),
                graphics.board.blockSize);
        hud = new HUD(new Point(graphics.getHorizontalPixels(),graphics.getVerticalPixels()));
        mUIController = new UIController(this);

    }

    public void addObserver(InputObserver o) {
        inputObservers.add(o);
    }


    // Called to start a new game
    public void newGame() {

        // reset the snake
        mSnake.reset(graphics.board.blocksWide, graphics.board.blocksHigh);

        // Get the apple ready for dinner
        mApple.spawn();

        // Reset the mScore
        mScore = 0;

        // Setup mNextFrameTime so an update can triggered
        mNextFrameTime = System.currentTimeMillis();
    }


    // Handles the game loop
    @Override
    public void run() {
        while (mPlaying) {
            if(!mPaused && !gameOver) {
                // Update 10 times a second
                if (updateRequired()) {
                    update();
                }
            }

            draw();
        }
    }


    // Check to see if it is time for an update
    public boolean updateRequired() {

        // Run at 10 frames per second
        final long TARGET_FPS = 10;
        // There are 1000 milliseconds in a second
        final long MILLIS_PER_SECOND = 1000;

        // Are we due to update the frame
        if(mNextFrameTime <= System.currentTimeMillis()){
            // Tenth of a second has passed

            // Setup when the next update will be triggered
            mNextFrameTime =System.currentTimeMillis()
                    + MILLIS_PER_SECOND / TARGET_FPS;

            // Return true so that the update and draw
            // methods are executed
            return true;
        }

        return false;
    }


    // Update all the game objects
    public void update() {

        // Move the snake
        mSnake.move();

        // Did the head of the snake eat the apple?
        if(mSnake.checkDinner(mApple.getLocation())){
            // This reminds me of Edge of Tomorrow.
            // One day the apple will be ready!
            mApple.spawn();

            // Add to  mScore
            mScore = mScore + 1;

            // Play a sound
            audio.play(this.eatID);
        }

        // Did the snake die?
        if (mSnake.detectDeath()) {
            // Pause the game ready to start again
            audio.play(crashID);

            mPaused = true;
            gameOver=true;
        }

    }


    // Do all the drawing
    public void draw() {
        // Get a lock on the mCanvas
        if (graphics.getSurfaceHolder().getSurface().isValid()) {
            graphics.canvas = graphics.getSurfaceHolder().lockCanvas();

            // Fill the screen with a color
           graphics.canvas.drawColor(Color.argb(255, 26, 128, 182));

            // Set the size and color of the mPaint for the text
            graphics.paint.setColor(Color.argb(255, 255, 255, 255));
            graphics.paint.setTextSize(120);

            // Draw the score
            graphics.canvas.drawText("" + mScore, 20, 120, graphics.paint);

            // Draw the apple and the snake
            mApple.draw(graphics.canvas, graphics.paint);
            mSnake.draw(graphics.canvas, graphics.paint);
            hud.draw(graphics.canvas, graphics.paint,this);

            // Draw some text while paused
            if(mPaused && gameOver){

                // Set the size and color of the mPaint for the text
                graphics.paint.setColor(Color.argb(255, 255, 255, 255));
                graphics.paint.setTextSize(250);

                // Draw the message
                // We will give this an international upgrade soon
                //mCanvas.drawText("Tap To Play!", 200, 700, mPaint);
                graphics.canvas.drawText(getResources().
                                getString(R.string.tap_to_play),
                        200, 700, graphics.paint);
            }


            // Unlock the mCanvas and reveal the graphics for this frame
            graphics.getSurfaceHolder().unlockCanvasAndPost(graphics.canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (mPaused && gameOver) {
                    mPaused = false;
                    gameOver = false;
                    newGame();

                    // Don't want to process snake direction for this tap
                    return true;
                }

                // Let the Snake class handle the input
                mSnake.switchHeading(motionEvent);
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


    // Stop the thread
    public void pause() {
        mPlaying = false;
        try {
            mThread.join();
        } catch (InterruptedException e) {
            // Error
        }
    }


    // Start the thread
    public void resume() {
        mPlaying = true;
        mThread = new Thread(this);
        mThread.start();
    }
    public Boolean getPaused(){
        return mPaused;
    }

    public boolean isGameOver() {
        return gameOver;
    }
    public void newPause(){
        mPaused = true;
    }
    public void newResume() {
        mPaused = false;
    }
}

