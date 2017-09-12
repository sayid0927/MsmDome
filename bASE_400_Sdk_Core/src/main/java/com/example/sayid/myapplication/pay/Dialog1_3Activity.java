package com.example.sayid.myapplication.pay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import com.example.sayid.myapplication.common.listener.OnDialogListener;
import com.example.sayid.myapplication.common.util.UI;
import com.example.sayid.myapplication.model.ChannelOrderResp;


public class Dialog1_3Activity extends Activity {
    private final static String TAG = "Dialog1_3Activity";


    int dealType;
    String pay_order_id;
    String user_order_id;
    String tip;
    String content;
    static AlertDialog dialog;
    Context myContext;
    public static Dialog1_3Activity inst;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.dialog1);

//		Logs.d(TAG, "onCreate---->");

        try {
//			requestWindowFeature(Window.FEATURE_NO_TITLE); //隐藏标题栏

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
            //	initDialog(tip);

        } catch (Exception e) {
//			Logs.e(TAG, "Dialog1_3Activity：001:" + e.toString());
        }


        inst = this;
    }


    public void creatDialog(String content, String tip) {
        try {
            dialog = UI.showDialog(this, content, tip, new OnDialogListener() {
                public void onDialogClicked(int keyBack) {
                    switch (keyBack) {
                        case 1:                         //确定
//						Logs.d(TAG, "creatDialog : 确定");
                            define();
                            break;

                        case 2:                         //取消
//						Logs.d(TAG, "creatDialog : 取消");
                            quit();
                            break;
                    }
                }
            });

            if (dialog != null) {
                try {
                    //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                } catch (Exception e) {
//					Logs.e(TAG, "D1_001");
                }
                dialog.show();

//				dialog.setOwnerActivity(Dialog1_3Activity.this);
            }
        } catch (Exception e) {
//			Logs.e(TAG, "Dialog1_3Activity：002:" + e.toString());
        }
    }

    /**
     * 确定
     */
    public void define() {

        switch (dealType) {
            case UI.DEAL_TYPE_1:
                if (ChannelOrderResp.instance != null) {
                    ChannelOrderResp.instance.orderPrompt(user_order_id, pay_order_id);
                }
//			this.dismiss();
                finish();
                break;

            case UI.DEAL_TYPE_3:
                if (ChannelOrderResp.instance != null) {
                    ChannelOrderResp.instance.secondConfirmPrompt(myContext, user_order_id, pay_order_id);
                }

//			this.dismiss();
                finish();
                break;
        }
    }

    /**
     * 取消
     */
    private void quit() {
        switch (dealType) {
            case UI.DEAL_TYPE_1:
//			Logs.d(TAG, "user_order_id:" + user_order_id);
                if (ChannelOrderResp.instance != null) {
                    ChannelOrderResp.instance.quitOrderPrompt(user_order_id, pay_order_id);
                }
                break;

            case UI.DEAL_TYPE_3:
                break;
        }
//		this.dismiss();
        finish();
    }

}
