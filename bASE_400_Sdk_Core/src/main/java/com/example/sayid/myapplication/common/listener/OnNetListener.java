package com.example.sayid.myapplication.common.listener;

import org.apache.http.HttpEntity;

public interface OnNetListener {
    public void onSuccess(HttpEntity entity, boolean IsEnc);

    public void onFailed(String exceptionId, String exceptionText);

    public void onTimeout();
}
