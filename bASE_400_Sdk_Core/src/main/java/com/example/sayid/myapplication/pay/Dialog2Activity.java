package com.example.sayid.myapplication.pay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.example.sayid.myapplication.common.listener.OnDialogListener;
import com.example.sayid.myapplication.common.util.UI;
import com.example.sayid.myapplication.model.ChannelOrderResp;

public class Dialog2Activity extends Activity {
    private final static String TAG = "Dialog2Activity";

    int dealType;
    String user_order_id;
    String pay_order_id;
    AlertDialog dialog;
    final static int APN_SETTING = 1;
    int setting;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//		Logs.d(TAG, "Dialog2Activity : onCreate--->");
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE); //隐藏标题栏

            Bundle b = this.getIntent().getExtras();
            if (b == null) {
                return;
            }
            user_order_id = b.getString("user_order_id");
            pay_order_id = b.getString("pay_order_id");
            String content = b.getString("content");
            dealType = b.getInt("dealType");
            String tip = b.getString("tip");

            creatDialog(content, tip);
        } catch (Exception e) {
//			Logs.e(TAG, "onCreate error:" + e.toString());
        }
    }

    private void creatDialog(String content, String tip) {
        try {
            dialog = UI.showDialog(this, content, tip, new OnDialogListener() {
                public void onDialogClicked(int keyBack) {
                    switch (keyBack) {
                        case 1:                         //确定
                            define();
                            break;

                        case 2:                         //取消
                            quit();
                            break;
                    }
                }
            });

            if (dialog != null) {
                try {
                    dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                } catch (Exception e) {
//					Logs.e(TAG, "D2_001");
                }
                dialog.show();
            }
        } catch (Exception e) {
//			Logs.e(TAG, "createDialog error:" + e.toString());
        }
    }

    public void onResume() {
        super.onResume();
//		Logs.d(TAG, "Dialog2Activity : onResume--->dealType = " + dealType);

        switch (dealType) {
            case UI.DEAL_TYPE_4:
//			Logs.d(TAG, "setting = " + setting);

                if (setting == 1) {
                    setting = 2;
                } else if (setting == 2) {
//				boolean bool = Apn.getInstance(this).isCurrApnWap();   //当前apn是否选择 wap
//				if(bool){
                    //TODO
//					ParseStep.getInstanse().paySuccess();
//					Apn.getInstance(this).changeToWap(true);
//				}else {
                    //TODO 没有设置，放弃计费
//					ParseStep.getInstanse().end(false, ErrorCode.CODE_113010, "");
//				}
                    setting = 0;
                    finish();
                }
                break;
            case UI.DEAL_TYPE_5:
//			Logs.d(TAG, "setting = " + setting);
                if (setting == 1) {
                    setting = 2;
                } else if (setting == 2) {
//				Apn.getInstance(this).backToApn();   //从setting界面返回后，程序默认切回wifi等状态
                    setting = 0;
                    finish();
                }
                break;
        }
    }


    /**
     * 确定
     */
    private void define() {
        switch (dealType) {
            case UI.DEAL_TYPE_2:
                if (ChannelOrderResp.instance != null) {
                    ChannelOrderResp.instance.channelPrompt(user_order_id, pay_order_id);
                }

                finish();
                break;

            case UI.DEAL_TYPE_4:       //提示用户apn设置
            case UI.DEAL_TYPE_5:       //提示用户apn切换回去
                Intent intent = new Intent(android.provider.Settings.ACTION_APN_SETTINGS);
                startActivityForResult(intent, APN_SETTING);
                break;
        }
    }

    /**
     * 取消
     */
    private void quit() {
        switch (dealType) {
            case UI.DEAL_TYPE_2:
                if (ChannelOrderResp.instance != null) {
                    ChannelOrderResp.instance.quitChannelPrompt(user_order_id, pay_order_id);
                }
                break;

            case UI.DEAL_TYPE_4:    //用户取消设置wap，返回结果
                //放弃计费
//			ParseStep.getInstanse().end(false, ErrorCode.CODE_113009, "");
                setting = 0;
                break;

            case UI.DEAL_TYPE_5:
//			Apn.getInstance(this).backToApn();   //用户取消设置apn，切回到之前的设置，程序默认切回
                setting = 0;
                break;
        }

        finish();
    }

    /**
     * 系统apn设置界面 返回响应
     * (non-Javadoc)
     *
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case APN_SETTING:
                setting = 2;
                break;
        }
    }

}
