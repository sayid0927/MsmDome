package com.example.sayid.myapplication.common.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sayid.myapplication.common.data.Strings;
import com.example.sayid.myapplication.common.listener.OnDialogListener;
import com.example.sayid.myapplication.pay.Dialog1_3Activity;
import com.example.sayid.myapplication.pay.Dialog2Activity;

public class UI {
    private final static String TAG = "UI";

    public final static int DIALOG_1 = 1;
    public final static int DIALOG_2 = 2;
    public final static int DIALOG_3 = 3;

    /**
     * 第一次确认,订单确认
     */
    public final static int DEAL_TYPE_1 = 1;
    /**
     * 首条短信确认
     */
    public final static int DEAL_TYPE_2 = 2;
    /**
     * 二次确认
     */
    public final static int DEAL_TYPE_3 = 3;
    /**
     * 提示用户apn设置
     */
    public final static int DEAL_TYPE_4 = 4;
    /**
     * 提示用户apn切换回去
     */
    public final static int DEAL_TYPE_5 = 5;
    private static Dialog1_3Activity dialog;


    public static void showTip(Context context, String text) {
        if (context == null) {
            return;
        }
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    public static void showAlertDialog(int type, Context context, String user_order_id, String pay_order_id,
                                       String content, String tip, int dealType) {
        try {
            if (context == null) {
                return;
            }

            Intent intent = null;
            if (type == DIALOG_1 || type == DIALOG_3) {
                intent = new Intent(context, Dialog1_3Activity.class);


            } else {
                intent = new Intent(context, Dialog2Activity.class);
            }

            if (tip == null) {
                tip = "";
            }
            if (content == null) {
                content = "";
            }

            Bundle b = new Bundle();
            b.putString("user_order_id", user_order_id);       //用户订单号
            b.putString("pay_order_id", pay_order_id);       //订单号
            b.putString("content", content);                 //对话框内容
            b.putString("tip", tip);                         //对话框内容
            b.putInt("dealType", dealType);                  //对话框 按键响应方式
            intent.putExtras(b);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);


        } catch (Exception e) {
            //.e(TAG, "UI：001:" + e.toString());
        }
    }

    public static AlertDialog showDialog(Context context, String message, String tip, final OnDialogListener listener) {
        if (context == null) {
            return null;
        }

        TextView tvTip = new TextView(context);
        tvTip.setText(tip);
        tvTip.setTextColor(0xFFAAAAAA);
        tvTip.setTextSize(12f);
        tvTip.setGravity(Gravity.CENTER);
        tvTip.setPadding(0, 0, 0, 10);

        AlertDialog.Builder build = new AlertDialog.Builder(context);
        build.setMessage(message)
                .setView(tvTip)
                .setPositiveButton(Strings.SURE, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null) {
                            listener.onDialogClicked(1);
                        }
                    }
                })
                .setNegativeButton(Strings.CANCEL, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null) {
                            listener.onDialogClicked(2);
                        }
                    }
                })
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                            if (listener != null) {
                                listener.onDialogClicked(2);
                            }
                            return true;
                        } else if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_MENU) {

                            return true;
                        }

                        return false;
                    }
                });
        AlertDialog dialog = build.create();
        dialog.setCanceledOnTouchOutside(false);         //屏蔽点击dialog边缘使得dialog消失

        return dialog;
    }

}
