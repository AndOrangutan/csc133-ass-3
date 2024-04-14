package com.andorangutan.snake;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

class Drawer {
    private Context context;
    final Board board;
    public Drawer(Context context, Board board) {
        this.context = context;
        this.board = board;

    }

    public Bitmap bitmapCreate(int imgId) {
        return BitmapFactory.decodeResource(context.getResources(), imgId);
    }


    public Bitmap bitmapScale(Bitmap bitmap) {
        return Bitmap.createScaledBitmap(bitmap, board.blockSize, board.blockSize, false);
    }

    public Bitmap bitmapCreateScaleRotate(int imgId, Matrix matrix) {
       Bitmap bitmap = bitmapScale(bitmapCreate(imgId));
       return Bitmap.createBitmap(bitmap, 0, 0, board.blockSize, board.blockSize, matrix, false);
    }

}
