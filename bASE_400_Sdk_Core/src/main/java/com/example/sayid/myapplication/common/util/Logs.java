package com.example.sayid.myapplication.common.util;

import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;

import com.example.sayid.myapplication.common.data.ConfigConst;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 日志记录工具类 可以通过修改是否启用调试模式、日志级别、是否记录日志到文件, 灵活的记录日志。
 * 记录日志到文件需要配置权限：android.permission.WRITE_EXTERNAL_STORAGE
 *
 * @author kjxu
 * @version [1.1, 2012-10-15]
 */
public class Logs {
    /**
     * 当前类记录日志的tag
     */
    private final static String TAG = "Logs";

    /**
     * 是否启用调试模式, 如果为false不记录任何日志
     */
    private final static boolean ADB = ConfigConst.IS_ADB;

    /**
     * 是否需要记录日志到文件。默认需要输出到日志文件，是否真正的输出到了日志文件，取决于LOGFILENAME变量的目录是否存在。
     */
    private static boolean IS_NEED_FILELOG = ConfigConst.IS_NEED_FILELOG;

    /**
     * 日志级别
     */
    private final static int LOG_DEGREE = Log.DEBUG;

    /**
     * 日志文件名称绝对路径
     */
    private final static String LOGFILENAME = Environment.getExternalStorageDirectory().getPath() + "/" + ConfigConst.APP_FILES_LOG_DIR + "/app.log";

    /**
     * 对于sd卡未准备好, 允许缓存的最大日志条数
     */
    private final static int MAX_CACHE_SIZE = 128;

    /**
     * 锁对象, 在同步模块中使用
     */
    private static Object lock = new Object();

    /**
     * 待记录到文件的日志队列, 需要支持同步
     */
    private static Queue<String> queue = new ConcurrentLinkedQueue<String>();

    /**
     * 日志级别与其对应的字符标签
     */
    private static SparseArray<String> degreeLabel = new SparseArray<String>();

    /**
     * 日志线程对象
     */
    private static LogThread logThread = null;

    private static long i = 0;

    public static String getLinkId() {
        return "gprstest, " + (i++) + ", ";
    }

    /**
     * 初始化日志级别与其对应的字符标签
     */
    static {
        degreeLabel.put(Log.VERBOSE, "V");
        degreeLabel.put(Log.DEBUG, "D");
        degreeLabel.put(Log.INFO, "I");
        degreeLabel.put(Log.WARN, "W");
        degreeLabel.put(Log.ERROR, "E");

        logThread = new LogThread();
        logThread.start();
    }

    /**
     * 记录V级别日志 在记录V级别日志时调用, 如果日志配置为不记录日志或日志级别高于V, 不记录日志
     *
     * @param tag 日志tag
     * @param msg 日志信息, 支持动态传参可以是一个或多个(避免日志信息的+操作过早的执行)
     * @author kjxu
     */
    @SuppressWarnings("unused")
    public static void v(String tag, String... msg) {
        if (ADB && LOG_DEGREE <= Log.VERBOSE) {
            String msgStr = combineLogMsg(msg);

            Log.v(tag, msgStr);

            writeLogToFile(Log.VERBOSE, tag, msgStr, null);
        }
    }

    /**
     * 记录D级别日志 在记录D级别日志时调用, 如果日志配置为不记录日志或日志级别高于D, 不记录日志
     *
     * @param tag 日志tag
     * @param msg 日志信息, 支持动态传参可以是一个或多个(避免日志信息的+操作过早的执行)
     * @author kjxu
     */
    public static void d(String tag, String... msg) {
        if (ADB && LOG_DEGREE <= Log.DEBUG) {
            String msgStr = combineLogMsg(msg);

            Log.d(tag, msgStr);

            writeLogToFile(Log.DEBUG, tag, msgStr, null);
        }
    }

    /**
     * 记录I级别日志 在记录I级别日志时调用, 如果日志配置为不记录日志或日志级别高于I, 不记录日志
     *
     * @param tag 日志tag
     * @param msg 日志信息, 支持动态传参可以是一个或多个(避免日志信息的+操作过早的执行)
     * @author kjxu
     */
    public static void i(String tag, String... msg) {
        if (ADB && LOG_DEGREE <= Log.INFO) {
            String msgStr = combineLogMsg(msg);

            Log.i(tag, msgStr);

            writeLogToFile(Log.INFO, tag, msgStr, null);
        }
    }

    /**
     * 记录W级别日志 在记录W级别日志时调用, 如果日志配置为不记录日志或日志级别高于W, 不记录日志
     *
     * @param tag 日志tag
     * @param msg 日志信息, 支持动态传参可以是一个或多个(避免日志信息的+操作过早的执行)
     * @author kjxu
     */
    public static void w(String tag, String... msg) {
        if (ADB && LOG_DEGREE <= Log.WARN) {
            String msgStr = combineLogMsg(msg);

            Log.w(tag, msgStr);

            writeLogToFile(Log.WARN, tag, msgStr, null);
        }
    }

    /**
     * 记录E级别日志 在记录E级别日志时调用, 如果日志配置为不记录日志或日志级别高于E, 不记录日志
     *
     * @param tag 日志tag
     * @param msg 日志信息, 支持动态传参可以是一个或多个(避免日志信息的+操作过早的执行)
     * @author kjxu
     */
    public static void e(String tag, String... msg) {
        if (ADB && LOG_DEGREE <= Log.ERROR) {
            String msgStr = combineLogMsg(msg);

            Log.e(tag, msgStr);

            writeLogToFile(Log.ERROR, tag, msgStr, null);
        }
    }

    /**
     * 记录E级别日志 在记录E级别日志时调用, 如果日志配置为不记录日志或日志级别高于E, 不记录日志
     *
     * @param tag 日志tag
     * @param tr  异常对象
     * @param msg 日志信息, 支持动态传参可以是一个或多个(避免日志信息的+操作过早的执行)
     * @author kjxu
     */
    public static void e(String tag, Throwable tr, String... msg) {
        if (ADB && LOG_DEGREE <= Log.ERROR) {
            String msgStr = combineLogMsg(msg);

            Log.e(tag, msgStr, tr);

            writeLogToFile(Log.ERROR, tag, msgStr, tr);
        }
    }

    /**
     * 组装动态传参的字符串 将动态参数的字符串拼接成一个字符串
     *
     * @param msg 动态参数
     * @return 拼接后的字符串
     * @author kjxu
     */
    private static String combineLogMsg(String... msg) {
        if (null == msg)
            return null;

        StringBuilder sb = new StringBuilder(getLinkId());
        for (String s : msg) {
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * 记录日志到文件 如果配置成需要记录日志到文件, 需要将日志数据拼接成一条日志记录, 首先放入到待记录的日志队列中, 再由异步线程记录到文件中。
     * 日志格式定义为：yyyy-MM-dd HH:mm:ss.SSS, <D>degree, <T>tag, <M>message,
     * <E>exception info
     *
     * @param degree 日志级别
     * @param tag    日志标签
     * @param msg    日志信息
     * @param tr     异常
     * @author kjxu
     */
    private static void writeLogToFile(int degree, String tag, String msg, Throwable tr) {
        if (IS_NEED_FILELOG) {
            StringBuffer sb = new StringBuffer();

            // 拼接时间、日志级别、标签、信息
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            sb.append(df.format(Calendar.getInstance().getTime())).append(", <P>").append(Thread.currentThread().getId())
                    .append(", <D>").append(degreeLabel.get(degree)).append(", <T>").append(tag).append(", <M>").append(msg);

            // 如果有异常信息, 需要拼接异常信息, 拼接所有的堆栈信息
            if (null != tr) {
                StackTraceElement[] stacks = tr.getStackTrace();
                if (null != stacks) {
                    sb.append(", <E>").append(tr.getMessage()).append("\r\n");
                    for (int i = 0; i < stacks.length; i++) {
                        sb.append("\t\tat ").append(stacks[i].getClassName()).append(".")
                                .append(stacks[i].getMethodName()).append("(")
                                .append(stacks[i].getClassName()).append(".java").append(" ")
                                .append(stacks[i].getLineNumber()).append(")").append("\r\n");
                    }
                }
            }

            // 将日志信息增加到队列中
            queue.add(sb.toString());

            // 通知日志线程写文件
            synchronized (lock) {
                lock.notify();
            }

            // TODO 线程如何终止, 资源如何回收
        }
    }

    /**
     * 日志线程类 用于记录日志到文件的线程类
     *
     * @author kjxu
     * @version [1.0.0.0, 2011-3-16]
     */
    private static class LogThread extends Thread {

        /**
         * 线程是否运行的标识位, 在start线程时置为true, 在终止线程(halt方法)时置为false
         */
        boolean isRunning = false;

        /**
         * 日志文件的输出流对象
         */
        private BufferedWriter bw;

        @Override
        public synchronized void start() {
            isRunning = true;

            // 启动日志线程时初始化文件输出流
            initWriter();

            super.start();
        }

        @Override
        public void run() {
            while (isRunning) {
                String log = null;

                if (null == bw) {
                    initWriter();
                    // writer初始化好了才记录日志
                } else {

                    // 循环从日志队列中取出日志, 记录到文件中
                    while (null != (log = queue.poll())) {
                        try {
                            bw.write(log);
                            bw.write("\r\n");
                            bw.flush();
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage(), e);

                            // 如果发生异常, 可能是sd卡不可用, 并尝试重新初始化输出流
                            initWriter();
                        }
                    }
                }

                // 处理完所有队列中的日志后, wait, 在有日志进入队列时被唤醒
                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            }


        }

        /**
         * 终止线程时调用 用于终止线程, 将运行状态置为false
         *
         * @author kjxu
         */
        @SuppressWarnings("unused")
        public synchronized void halt() {
            isRunning = false;
        }


        /**
         * 初始化文件输出流对象 如果文件输出流已经初始化过, 需要先关闭输出流, 再创建新的输出流对象; 否则直接创建
         *
         * @author kjxu
         */
        private void initWriter() {
            // 只缓存允许的日志数目
            if (queue.size() > MAX_CACHE_SIZE) {
                queue.clear();
            }

            // 如果输出流初始化过, 需要先关闭输出流, 释放资源
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
            /*
            try {
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/storage/emulated/legacy/app.log", true), "UTF-8"));
//            	bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/storage/sdcard0/app.log", true), "UTF-8"));
			}  catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
            */


            try {
                @SuppressWarnings("unused")
                boolean mExternalStorageAvailable = false;
                boolean mExternalStorageWriteable = false;
                String state = Environment.getExternalStorageState();

                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    // We can read and write the media
                    mExternalStorageAvailable = mExternalStorageWriteable = true;
                } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                    // We can only read the media
                    mExternalStorageAvailable = true;
                    mExternalStorageWriteable = false;
                } else {
                    // Something else is wrong. It may be one of many other states,
                    // but all we need
                    // to know is we can neither read nor write
                    mExternalStorageAvailable = mExternalStorageWriteable = false;
                }

                if (mExternalStorageWriteable) {
                    File dir = new File(LOGFILENAME.substring(0, LOGFILENAME.lastIndexOf("/")));
                    if (!dir.exists()) {
                        IS_NEED_FILELOG = false;
                        return;
                    }

                    // 创建文件输出流
                    bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(LOGFILENAME, true), "UTF-8"));
                }
            } catch (Exception e) {
                Log.e(TAG, "initWriter error", e);
            }
        }
    }

}
