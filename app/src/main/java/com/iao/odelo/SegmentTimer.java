package com.iao.odelo;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.iao.wrapper.Wrapper;

/**
 * Created by iao on 15. 6. 17.
 */
public class SegmentTimer {
    private boolean isTiming;
    private int time;

    Wrapper wrapper;

    public SegmentTimer() {
       wrapper = new Wrapper();
    }

    public void start() {
        time = 0;
        isTiming = true;
        wrapper.openSegment();
        timeHandler.sendEmptyMessageDelayed(0, 1000);
        segmentHandler.sendEmptyMessageDelayed(0, 1);
    }

    public void restart() {
        isTiming = true;
        wrapper.openSegment();
        timeHandler.sendEmptyMessageDelayed(0, 1000);
        segmentHandler.sendEmptyMessageDelayed(0, 1);
    }

    public void pause() {
        isTiming = false;
    }

    public void stop() {
        isTiming = false;
        time = 0;
        timeHandler.removeMessages(0);
        wrapper.closeSegment();
    }

    public boolean isTiming() {
        return isTiming;
    }

    public String getTime() {
        int [] times = wrapper.getTime();
        String time = "";
        for (int i = 0; i < times.length; ++i){
            if (i % 2 == 0 || i == times.length-1)
                time += times[i] + "";
            else
                time += times[i] + ":";
        }

        return time;
    }

    Handler timeHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (isTiming) {
                ++time;
                Log.d("time", "time " + time + " " + isTiming);
                timeHandler.sendEmptyMessageDelayed(0, 1000);
            }
            return false;
        }
    });

    Handler segmentHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (isTiming) {
                wrapper.setSegment(time);
                segmentHandler.sendEmptyMessageDelayed(0, 1);
            }
            return false;
        }
    });
}
