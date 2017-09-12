package com.example.sayid.myapplication.parseStep;

import com.example.sayid.myapplication.common.data.ErrorCode;
import com.example.sayid.myapplication.model.ChannelInfo;
import com.example.sayid.myapplication.parseStep.step.action.Step;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 基地解析实体类
 *
 * @author zorro
 */
public class ParseStepBean implements Cloneable, Serializable {

    /**
     *
     */
    private final static long serialVersionUID = 1L;

    private final static String TAG = "ParseStepBean";

    public String key_id;
    /**
     * 通道信息
     */
    public ChannelInfo channelInfo;
    /**
     * 当前步骤
     */
    public int stepIndex = 0;

    /**
     * 总步骤数
     */
    public int stepTotal = -1;

    /**
     * 保存返回的二进制数据
     */
    public Map<String, Object> map_object;

    /**
     * 保存计费过程中产生的参数的map
     */
    public Map<String, String> map;

    /**
     * 充值是否成功
     */
    public boolean isPaySuccess = false;

    /**
     * 计费失败 原因ID
     */
    public String errorCode;

    /**
     * 计费请求日志
     */
    public StringBuffer sbLog;

    /**
     * 构造函数
     *
     * @param channelInfo
     */
    public ParseStepBean(ChannelInfo channelInfo) {
        this.channelInfo = channelInfo;
        map = new HashMap<String, String>();
        map_object = new HashMap<String, Object>();
        this.sbLog = new StringBuffer("init ");

        initExecute();
    }

    /**
     * 初始化
     */
    private void initExecute() {
        stepIndex = 0;

        // 初始化步骤总数
        if (channelInfo != null && channelInfo.list_action != null) {
            stepTotal = channelInfo.list_action.size();
        } else {
            stepTotal = 0;
        }

        if (channelInfo != null) {
            append("pay_order_id=").append(channelInfo.pay_order_id);
        }
        append(", stepIndex=").append(stepIndex).append(", totalStep=").append(stepTotal);
    }

    /**
     * 设置执行步骤
     *
     * @param index
     * @return
     */
    public void setStepIndex(int index) {
        this.stepIndex = index;
    }

    /**
     * 获取当前执行步骤
     *
     * @return
     */
    public Step getCurrentStep() {
        return getStep(stepIndex);
    }

    /**
     * 获取指定执行步骤
     *
     * @param index
     * @return
     */
    public Step getStep(int index) {
        // 步骤下标越界 返回空
        if (index < 0 || stepTotal == 0 || index > (stepTotal - 1)) {
            return null;
        }

        // 通道 通道动作集不为空
        if (channelInfo != null && channelInfo.list_action != null) {
            return channelInfo.list_action.get(index);
        } else {
            return null;
        }
    }

    /**
     * 支付完成事件通知
     */
    public void onPayEvent() {
        // 短信读取等待 不通知
        if (!ErrorCode.CODE_114014.equals(errorCode)) {
            if (channelInfo != null) {
                channelInfo.onPayEvent(isPaySuccess, errorCode);
            }
        }
    }

    @Override
    public Object clone() {
        ParseStepBean obj = null;
        try {
            obj = (ParseStepBean) super.clone();

            obj.map = new HashMap<String, String>(this.map);

            obj.map_object = new HashMap<String, Object>();
            for (Iterator<String> it = this.map_object.keySet().iterator(); it.hasNext(); ) {
                String key = it.next();
                Object value = this.map_object.get(key);

                if (value != null && value instanceof byte[]) {
                    byte[] temp = (byte[]) value;
                    byte[] clone = new byte[temp.length];

                    System.arraycopy(clone, 0, temp, 0, temp.length);
                    obj.map_object.put(key, clone);
                }
            }
        } catch (CloneNotSupportedException e) {
            //.e(TAG, e, "clone not support error:");
        }
        return obj;
    }

    public ParseStepBean append(int i) {
        sbLog.append(i);

        return this;
    }

    public ParseStepBean append(String content) {
        sbLog.append(content);
        return this;
    }
}
