package com.andorangutan.snake;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import java.util.ArrayList;
public class UIController implements InputObserver {

    public UIController(SnakeGameBroadcaster b) {
        b.addObserver(this);
    }

    @Override
    public void handleInput(MotionEvent event,
                            GameState gs, ArrayList<Rect> buttons) {
        int i = event.getActionIndex();
        int x = (int) event.getX(i);
        int y = (int) event.getY(i);
        int eventType = event.getAction() &
                MotionEvent.ACTION_MASK;
        if(eventType == MotionEvent.ACTION_UP ||
                eventType ==
                        MotionEvent.ACTION_POINTER_UP) {
            if (buttons.get(HUD.PAUSE).contains(x, y)) {
                // Player pressed the pause button
                // Respond differently depending
                // upon the game's state
                // If the game is not paused
                if (!gs.isPaused()) {
                    // Pause the game
                    gs.setPaused(true);
                }
                // Paused and not game over
                else if (gs.isPaused()
                && !gs.isGameOver()) {
                    gs.setPaused(false);
                }
            }
        }
    }


}
