package com.example.sayid.myapplication.common.util;

import com.example.sayid.myapplication.parseStep.ParseStep;
import com.example.sayid.myapplication.parseStep.ParseStepBean;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FutureUtil {

    private final static String TAG = "FutureUtil";

    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void execute(ParseStep parseStep) {
        Future<ParseStepBean> future = executor.submit(parseStep);
        try {
            //.d(TAG, "execute Started..");
            // 设置基地通道执行5分钟超时
            ParseStepBean bean = future.get(5, TimeUnit.MINUTES);
            if (bean == null) {
                Logs.d(TAG, "execute result is null!");
            } else {
                Logs.d(TAG, "execute result current index = " + bean.stepIndex);
            }
            Logs.d(TAG, "execute Finished!");
        } catch (InterruptedException e) {
            Logs.e(TAG, "execute Terminated! error:" + e);
        } catch (ExecutionException e) {
            Logs.e(TAG, "execute Terminated! error:" + e);
        } catch (TimeoutException e) {
            Logs.e(TAG, "execute Terminated! error:" + e);
        }
    }
}
