package com.example.sayid.myapplication.common.db2;

import android.content.Context;
import android.database.Cursor;

import com.example.sayid.myapplication.common.bean.ReportBean;

import java.util.List;

public class ReportDao extends DBHelper {
    public ReportDao(Context paramContext) {
        super(paramContext);
    }

    public final static String createTable() {
        StringBuffer localStringBuffer = new StringBuffer("create table t_report(");
        localStringBuffer.append("id INTEGER PRIMARY KEY AUTOINCREMENT,");
        localStringBuffer.append("pay_order_id VARCHAR(128),");
        localStringBuffer.append("url text,");
        localStringBuffer.append("json_data text)");

        return localStringBuffer.toString();
    }

    public void insert(ReportBean paramReport) {
        String str = "insert into t_report(pay_order_id, url, json_data) values(?,?,?)";

        Object[] arrayOfObject = new Object[9];
        arrayOfObject[0] = paramReport.pay_order_id;
        arrayOfObject[1] = paramReport.url;
        arrayOfObject[2] = paramReport.jsonData;

        execSQL(str, arrayOfObject);
    }

    public List<ReportBean> selectAll() {
        return getList("select * from t_report order by id desc", null, ReportBean.class);
    }

    public void delete(int paramString) {
        String str = "delete from t_report where id = ?";

        execSQL(str, new Object[]{paramString});
    }

    public void delete() {
        execSQL("delete from t_report");
    }

    public Object get(Cursor paramCursor) {
        ReportBean localSmsInfo = new ReportBean();
        localSmsInfo.id = paramCursor.getInt(paramCursor.getColumnIndex("id"));
        localSmsInfo.pay_order_id = paramCursor.getString(paramCursor.getColumnIndex("pay_order_id"));
        localSmsInfo.url = paramCursor.getString(paramCursor.getColumnIndex("url"));
        localSmsInfo.jsonData = paramCursor.getString(paramCursor.getColumnIndex("json_data"));

        return localSmsInfo;
    }
}