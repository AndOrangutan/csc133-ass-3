package com.andorangutan.snake;

import android.content.Context;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.ArrayList;

public class Game extends SurfaceView implements Runnable,SnakeGameBroadcaster {

    final GameState gs;
    private HUD hud;

    private ArrayList<InputObserver>
            inputObservers = new ArrayList<>();
    private UIController uiController;

    public Game(Context context, Graphics graphics) {
        super(context);

        this.gs = new GameState(context, graphics);

        graphics.setSurfaceHolder(getHolder());


        this.hud = new HUD(new Point(graphics.getHorizontalPixels(),graphics.getVerticalPixels()));
        uiController = new UIController(this);
    }

    public void addObserver(InputObserver o) {
        inputObservers.add(o);
    }
    
    // Handles the game loop
    @Override
    public void run() {
        while (gs.isPlaying()) {
            if(!gs.isPaused() && !gs.isGameOver()) {
                gs.update();
            }

            gs.draw();
        }

    }

    
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (gs.isPaused() && gs.isGameOver()) {
                    gs.setPaused(false);
                    gs.setGameOver(false);
                    gs.newGame();

                    // Don't want to process snake direction for this tap
                    return true;
                }

                // Let the Snake class handle the input
                gs.onTouchPassthrough(motionEvent);
                break;

            default:
                break;

        }
        for (InputObserver o : inputObservers) {
            o.handleInput(motionEvent, this.gs,
                    hud.getControls());
        }
        return true;
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
