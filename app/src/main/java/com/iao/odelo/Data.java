package com.iao.odelo;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by iao on 15. 6. 15.
 */
public class Data {
    public static final String TAG = "Data";
    int positionX;
    int positionY;
    int [] directions;

    public Data(int x, int y, int [] directions) {
        this.directions = new int[8];
        positionX = x;
        positionY = y;
        for (int i = 0; i < directions.length; ++i)
            this.directions[i] = directions[i];
    }

    public void printData() {
        Log.d(TAG, positionX + " " + positionY);
        for (int i = 0; i < directions.length; ++i)
            Log.d(TAG, directions[i]+"");
    }
}
