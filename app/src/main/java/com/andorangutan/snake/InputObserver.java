package com.andorangutan.snake;

import android.graphics.Rect;
import android.view.MotionEvent;
import java.util.ArrayList;

public interface InputObserver {
    void handleInput(MotionEvent event,
                     SnakeGame sg,
                     ArrayList<Rect> controls);
}
