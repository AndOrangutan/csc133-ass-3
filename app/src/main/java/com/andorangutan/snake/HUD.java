package com.andorangutan.snake;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import java.util.ArrayList;
public class HUD {
    private int mTextFormatting;
    private int mScreenHeight;
    private int mScreenWidth;
    private ArrayList<Rect> controls;

    static int PAUSE = 0;
    HUD(Point size) {
        mScreenHeight = size.y;
        mScreenWidth = size.x;
        mTextFormatting = size.x / 50;
        prepareControls();

    }
    private void prepareControls() {
        int buttonWidth = mScreenWidth / 14;
        int buttonHeight = mScreenHeight / 12;
        int buttonPadding = mScreenWidth / 90;

        Rect pause = new Rect(
                mScreenWidth - buttonPadding -
                        buttonWidth,
                buttonPadding,
                mScreenWidth - buttonPadding,
                buttonPadding + buttonHeight);

        controls = new ArrayList<>();
        controls.add(PAUSE,pause);
    }
    void draw(Canvas c, Paint p, GameState gs) {
        // Draw the HUD
        p.setColor(Color.argb(255,255,255,255));
        p.setTextSize(mTextFormatting);
        if(gs.isPaused() && !gs.isGameOver()){
            p.setTextSize(mTextFormatting * 5);
            c.drawText("PAUSED",
                    mScreenWidth /3, mScreenHeight /2
                    ,p);
        }
        drawControls(c, p);
    }
    private void drawControls(Canvas c, Paint p){
        p.setColor(Color.argb(100,255,255,255));
        for(Rect r : controls){
            c.drawRect(r.left, r.top, r.right,
                    r.bottom, p);
        }
// Set the colors back
        p.setColor(Color.argb(255,255,255,255));
    }
    ArrayList<Rect> getControls(){
        return controls;
    }

}
