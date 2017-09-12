package com.example.sayid.myapplication.common.thread;

import com.example.sayid.myapplication.common.listener.OnActionListener;

/**
 * 计时器，超时触发指定监听器
 *
 * @author luozhi
 */
public class TimeJudge extends Thread {
    private final static String TAG = "TimeJudge";

    /**
     * 计时单位
     */
    private final static int m_rate = 300;

    private int timeoutMilli;
    private boolean isRun;
    private long startTime;

    private OnActionListener onActionListener;
    private int actionCode;

    public TimeJudge(int timeoutMilli, OnActionListener onActionListener, int actionCode) {
        this.timeoutMilli = timeoutMilli;
        this.onActionListener = onActionListener;
        this.actionCode = actionCode;
        startTime = System.currentTimeMillis();
        isRun = true;
    }

    public void run() {
        while (isRun) {
            try {
                Thread.sleep(m_rate);
            } catch (InterruptedException ioe) {
//				Logs.e(TAG, "TimeJudge：001:" + ioe.toString());
                continue;
            }

            synchronized (this) {
                if (System.currentTimeMillis() - startTime > timeoutMilli) {
                    isRun = false;
                    if (onActionListener != null) {
//						Logs.d(TAG, " timeout, timeoutMilli=" + timeoutMilli);
                        onActionListener.onActionFinished(actionCode, 1, null);
                        onActionListener = null;
                    }
                    break;
                }
            }
        }
    }

    /**
     * 关闭计时器
     */
    public void close() {
//		Logs.d(TAG, " close... set isRun = false");
        isRun = false;
    }
}
