package com.andorangutan.snake;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

public class Main extends Activity {

    // Declare an instance of SnakeGame
    SnakeGame mSnakeGame;

    // Set the game up
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Graphics graphics = new Graphics(this);

        // Create a new instance of the SnakeEngine class
        mSnakeGame = new SnakeGame(this, graphics);

        // Make snakeEngine the view of the Activity
        setContentView(mSnakeGame);
    }

    // Start the thread in snakeEngine
    @Override
    protected void onResume() {
        super.onResume();
        mSnakeGame.resume();
    }

    // Stop the thread in snakeEngine
    @Override
    protected void onPause() {
        super.onPause();
        mSnakeGame.pause();
    }
    
}
