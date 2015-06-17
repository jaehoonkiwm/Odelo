package com.iao.wrapper;

import android.util.Log;

/**
 * Created by iao on 15. 6. 17.
 */
public class Wrapper {
    private static final String TAG = "Wrapper";
    private boolean isconnectedSegment;
    char h1, h2, m1, m2, s1, s2;

    static {
        System.loadLibrary("segment");
    }

    public native int open(String path);
    public native void close();
    public native void setSegment(char min1, char min2, char sec1, char sec2, char msec1, char msec2);

    public void openSegment() {
        if (isconnectedSegment)
            return;

        int re = open("/dev/mySegment");

        if (re == 1)
            isconnectedSegment = true;
        else
            Log.d(TAG, "segment open failed");
    }

    public void closeSegment() {
        if (!isconnectedSegment)
            return;
        isconnectedSegment = false;
        close();
    }

    public void setSegment(int time) {

        int sec = time % 60;
        int min = time / 60;
        int hour = min / 60;
        min %= 60;

        if (hour == 24)
            hour = 0;

        h1 = (char) (hour / 10);
        h2 = (char) (hour % 10);
        m1 = (char) (min / 10);
        m2 = (char) (min % 10);
        s1 = (char) (sec / 10);
        s2 = (char) (sec % 10);

        setSegment(h1, h2, m1, m2, s1, s2);
    }

    public int[] getTime() {
        int [] times = {h1, h2, m1, m2, s1, s2};
        return times;
    }
}
