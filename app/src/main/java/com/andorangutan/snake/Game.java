package com.andorangutan.snake;

import android.content.Context;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.SurfaceView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class Game extends SurfaceView implements Runnable {

    final GameState gs;

    private Context context;

    public Game(Context context, Graphics graphics) {
        super(context);

        this.context = context;
        this.gs = new GameState(context, graphics);

        graphics.setSurfaceHolder(getHolder());


    }


    // Handles the game loop
    @Override
    public void run() {
        while (gs.isPlaying()) {
            if(!gs.isPaused() && !gs.isGameOver()) {
                gs.update();
            }

            gs.draw(this.context);
        }

    }

    
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return gs.onTouchPassthrough(motionEvent);
    }


    public void onResume() {
        this.gs.setPlaying(true);
        this.gs.setThread(new Thread(this));
        this.gs.getThread().start();
    }

    public void onPause() {
        this.gs.pause();
    }


}
