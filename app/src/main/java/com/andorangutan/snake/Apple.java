package com.andorangutan.snake;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import java.util.Random;

class Apple implements GameObject{

    // The location of the apple on the grid
    // Not in pixels
    private Point location = new Point();

    // The range of values we can choose from
    // to spawn an apple

    final Board board;

    private Drawer drawer;

    // An image to represent the apple
    private Bitmap mBitmapApple;

    /// Set up the apple in the constructor
    Apple(Context context, Board board){

        // Hide the apple off-screen until the game starts
        location.x = -10;
        this.board = board;

        this.drawer = new Drawer(context, board);

        // Resize the bitmap
        mBitmapApple = drawer.bitmapScale(drawer.bitmapCreate(R.drawable.apple));
    }

    // This is called every time an apple is eaten
    void spawn(){
        // Choose two random values and place the apple
        Random random = new Random();
        location.x = random.nextInt(board.blocksWide) + 1;
        location.y = random.nextInt(board.blocksHigh - 1) + 1;
    }

    // Let SnakeGame know where the apple is
    // SnakeGame can share this with the snake
    Point getLocation(){
        return location;
    }

    // Draw the apple
    @Override
    public void draw(Canvas canvas, Paint paint){
        canvas.drawBitmap(mBitmapApple,
                location.x * board.blockSize, location.y * board.blockSize, paint);
    }
}

