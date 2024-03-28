package com.andorangutan.snake;

import android.util.Log;

public class Board {
    final int blocksWide;
    final int blocksHigh;
    final int blockSize;

    public Board(int blocksWide, int x, int y) {

        Log.d("Debugging", "x: " + x);
        Log.d("Debugging", "y: " + y);

        this.blocksWide = blocksWide;
        this.blockSize = x / this.blocksWide;
        this.blocksHigh = y / this.blockSize;

    }
}
