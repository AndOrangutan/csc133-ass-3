package com.andorangutan.snake;

import android.view.Display;
import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.content.Context;
import android.app.Activity;
// import android.view.SurfaceView;

public class Graphics {
    private int horizontalPixels;
    private int verticalPixels;

    public Board board;

    public Canvas canvas;
    private SurfaceHolder surfaceHolder;
    public Paint paint;

    public Graphics(Context context) {
        Log.d("Debugging", "Initializing graphics...");

        // Get screen dimensions
        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        // Initialize our size based variables based on the screen resolution
        this.setHorizontalPixels(size.x);
        this.setVerticalPixels(size.y);

        // Setup Board
        this.board = new Board(40, this.getHorizontalPixels(), this.getVerticalPixels());

        // Initialize the drawing objects
        // this.surfaceHolder = getHolder();
        this.paint = new Paint();

        

        Log.d("Debugging", "Initialized graphics.");
    }

    public int getHorizontalPixels() {
        return this.horizontalPixels;
    }

    public void setHorizontalPixels(int pixels) {
        this.horizontalPixels = pixels;
    }

    public int getVerticalPixels() {
        return this.verticalPixels;
    }

    public void setVerticalPixels(int pixels) {
        this.verticalPixels = pixels;
    }

    public SurfaceHolder getSurfaceHolder() {
        return this.surfaceHolder;
    }
    public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
    }
}
