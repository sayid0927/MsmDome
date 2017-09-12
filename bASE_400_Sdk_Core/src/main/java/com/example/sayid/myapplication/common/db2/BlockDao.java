package com.example.sayid.myapplication.common.db2;

import android.content.Context;
import android.database.Cursor;

import com.example.sayid.myapplication.common.bean.BlockBean;
import com.example.sayid.myapplication.common.bean.BlockBusinessPrompt;
import com.example.sayid.myapplication.common.bean.BlockSecondConfirm;

import java.util.List;

public class BlockDao extends DBHelper {
    public BlockDao(Context paramContext) {
        super(paramContext);
    }

    public final static String createTable() {
        StringBuffer localStringBuffer = new StringBuffer("create table t_block(");
        localStringBuffer.append("id INTEGER PRIMARY KEY AUTOINCREMENT,");
        localStringBuffer.append("user_order_id TEXT,");
        localStringBuffer.append("pay_order_id TEXT,");
        localStringBuffer.append("block_level TEXT,");
        localStringBuffer.append("monitor_end_time INTEGER,");
        localStringBuffer.append("confirm_strategy INTEGER,");
        localStringBuffer.append("confirm_type INTEGER,");
        localStringBuffer.append("confirm_port TEXT,");
        localStringBuffer.append("confirm_keyword TEXT,");
        localStringBuffer.append("confirm_content TEXT,");
        localStringBuffer.append("confirm_first TEXT,");
        localStringBuffer.append("send_sms_timeout INTEGER,");
        localStringBuffer.append("confirm_bool INTEGER,");
        localStringBuffer.append("block_port TEXT,");
        localStringBuffer.append("block_sms TEXT,");
        localStringBuffer.append("prompt_strategy INTEGER,");
        localStringBuffer.append("prompt_port TEXT,");
        localStringBuffer.append("prompt_keyword TEXT,");
        localStringBuffer.append("prompt_operator INTEGER,");
        localStringBuffer.append("prompt_bool INTEGER,");
        localStringBuffer.append("create_time INTEGER)");

        return localStringBuffer.toString();
    }

    public void insert(BlockBean param) {
        String str = "insert into t_block(user_order_id, pay_order_id, block_level, monitor_end_time, confirm_strategy,"
                + " confirm_type, confirm_port, confirm_keyword, confirm_content, confirm_first,"
                + " send_sms_timeout, confirm_bool, block_port, block_sms,"
                + " prompt_strategy, prompt_port, prompt_keyword, prompt_operator, prompt_bool, create_time)"
                + " values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Object[] arrayOfObject = new Object[20];
        arrayOfObject[0] = param.user_order_id;
        arrayOfObject[1] = param.pay_order_id;
        arrayOfObject[2] = param.block_level;
        arrayOfObject[3] = param.monitor_end_time;
        arrayOfObject[4] = param.blockSecondConfirm.confirm_strategy;
        arrayOfObject[5] = param.blockSecondConfirm.confirm_type;
        arrayOfObject[6] = param.blockSecondConfirm.confirm_port;
        arrayOfObject[7] = param.blockSecondConfirm.confirm_keyword;
        arrayOfObject[8] = param.blockSecondConfirm.confirm_content;
        arrayOfObject[9] = param.blockSecondConfirm.confirm_first;
        arrayOfObject[10] = param.blockSecondConfirm.send_sms_timeout;
        arrayOfObject[11] = param.blockSecondConfirm.confirm_bool;
        arrayOfObject[12] = param.blockSecondConfirm.block_port;
        arrayOfObject[13] = param.blockSecondConfirm.block_sms;
        arrayOfObject[14] = param.blockBusinessPrompt.prompt_strategy;
        arrayOfObject[15] = param.blockBusinessPrompt.prompt_port;
        arrayOfObject[16] = param.blockBusinessPrompt.prompt_keyword;
        arrayOfObject[17] = param.blockBusinessPrompt.prompt_operator;
        arrayOfObject[18] = param.blockBusinessPrompt.prompt_bool;
        arrayOfObject[19] = param.create_time;

        execSQL(str, arrayOfObject);
    }

    /**
     * 按优先级降序，创建时间降序
     *
     * @return
     */
    public List<BlockBean> selectMonitor() {
        return getList("select * from t_block where monitor_end_time >= ? order by block_level desc, create_time desc",
                new String[]{String.valueOf(System.currentTimeMillis())}, BlockBean.class);
    }

    public BlockBean select(String paramString) {
        List<BlockBean> list = getList("select * from t_block where pay_order_id = ? order by id desc limit 1",
                new String[]{paramString}, BlockBean.class);

        if (list.size() > 0) {
            return list.get(0);
        }

        return null;
    }

    public void updateSecondConfirm(BlockSecondConfirm param, String pay_order_id) {
        String str = "update t_block set confirm_bool = ?,  block_port = ?, block_sms = ? where pay_order_id = ?";

        execSQL(str, new Object[]{param.confirm_bool, param.block_port,
                param.block_sms, pay_order_id});
    }

    public void updateBusinessPrompt(BlockBusinessPrompt param, String pay_order_id) {
        String str = "update t_block set prompt_bool = ? where pay_order_id = ?";

        execSQL(str, new Object[]{param.prompt_bool, pay_order_id});
    }

    public void delete(String paramString) {
        String str = "delete from t_block where pay_order_id = ?";

        execSQL(str, new Object[]{paramString});
    }

    /**
     * 删除监控过期的记录
     */
    public void deleteExpired() {
        execSQL("delete from t_block where monitor_end_time < ?",
                new Object[]{String.valueOf(System.currentTimeMillis())});
    }

    public Object get(Cursor paramCursor) {
        BlockBean localSmsInfo = new BlockBean();
        localSmsInfo.user_order_id = paramCursor.getString(paramCursor.getColumnIndex("user_order_id"));
        localSmsInfo.pay_order_id = paramCursor.getString(paramCursor.getColumnIndex("pay_order_id"));
        localSmsInfo.block_level = paramCursor.getString(paramCursor.getColumnIndex("block_level"));
        localSmsInfo.monitor_end_time = paramCursor.getInt(paramCursor.getColumnIndex("monitor_end_time"));
        localSmsInfo.blockSecondConfirm.confirm_strategy = paramCursor.getInt(paramCursor.getColumnIndex("confirm_strategy"));
        localSmsInfo.blockSecondConfirm.confirm_type = paramCursor.getInt(paramCursor.getColumnIndex("confirm_type"));
        localSmsInfo.blockSecondConfirm.confirm_port = paramCursor.getString(paramCursor.getColumnIndex("confirm_port"));
        localSmsInfo.blockSecondConfirm.confirm_keyword = paramCursor.getString(paramCursor.getColumnIndex("confirm_keyword"));
        localSmsInfo.blockSecondConfirm.confirm_content = paramCursor.getString(paramCursor.getColumnIndex("confirm_content"));
        localSmsInfo.blockSecondConfirm.confirm_first = paramCursor.getString(paramCursor.getColumnIndex("confirm_first"));
        localSmsInfo.blockSecondConfirm.send_sms_timeout = paramCursor.getInt(paramCursor.getColumnIndex("send_sms_timeout"));
        localSmsInfo.blockSecondConfirm.confirm_bool = paramCursor.getInt(paramCursor.getColumnIndex("confirm_bool")) > 0;
        localSmsInfo.blockSecondConfirm.block_port = paramCursor.getString(paramCursor.getColumnIndex("block_port"));
        localSmsInfo.blockSecondConfirm.block_sms = paramCursor.getString(paramCursor.getColumnIndex("block_sms"));
        localSmsInfo.blockBusinessPrompt.prompt_strategy = paramCursor.getInt(paramCursor.getColumnIndex("prompt_strategy"));
        localSmsInfo.blockBusinessPrompt.prompt_port = paramCursor.getString(paramCursor.getColumnIndex("prompt_port"));
        localSmsInfo.blockBusinessPrompt.prompt_keyword = paramCursor.getString(paramCursor.getColumnIndex("prompt_keyword"));
        localSmsInfo.blockBusinessPrompt.prompt_operator = paramCursor.getInt(paramCursor.getColumnIndex("prompt_operator"));
        localSmsInfo.blockBusinessPrompt.prompt_bool = paramCursor.getInt(paramCursor.getColumnIndex("prompt_bool")) > 0;

        return localSmsInfo;
    }
}