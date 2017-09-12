package com.example.sayid.myapplication.common.thread;

import com.example.sayid.myapplication.common.listener.OnTimeCountListener;
import com.example.sayid.myapplication.common.util.Logs;

/**
 * 按指定的时间间隔，触发指定次数的监听器
 *
 * @author zorro
 */
public class TimeCountThread extends Thread {
    private final static String TAG = "TimeCountThread";

    private final static int m_rate = 300;

    private int times = 1;

    private int timeoutMilli;
    private boolean isRun = true;
    private long startTime;

    private OnTimeCountListener timeListener;

    /**
     * 构造函数
     *
     * @param times        次数
     * @param timeoutMilli 时间间隔，单位：毫秒
     * @param timeListener
     */
    public TimeCountThread(int times, int timeoutMilli, OnTimeCountListener timeListener) {
        this.times = times;
        this.timeoutMilli = timeoutMilli;
        this.timeListener = timeListener;
        isRun = true;
    }

    public synchronized void addTimes(int add_times) {
        times += add_times;
    }

    public int getLeaveTimes() {
        return times;
    }

    public void run() {
        while (isRun) {
            try {
                Thread.sleep(m_rate);
            } catch (InterruptedException ioe) {
//				Logs.e(TAG, "TimeCountThread：001:" + ioe.toString());
                continue;
            }

            synchronized (this) {
                if (System.currentTimeMillis() - startTime > timeoutMilli) {
                    startTime = System.currentTimeMillis();
                    if (timeListener != null) {
                        //有多条短信需要发送是，只有最后一条发送后才能清除 timeListener
                        timeListener.onTimeOut(times);
                    }
                    times--;
                    if (times <= 0) {
                        close();
                        break;
                    }
                }
            }
        }
    }

    /**
     * 关闭
     */
    public void close() {
        isRun = false;
        timeListener = null;
        startTime = 0;
    }
}
