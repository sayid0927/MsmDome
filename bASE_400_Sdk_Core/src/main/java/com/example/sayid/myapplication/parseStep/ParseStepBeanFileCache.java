package com.example.sayid.myapplication.parseStep;

import com.example.sayid.myapplication.common.util.StoreBeanUtil;
import com.example.sayid.myapplication.parseStep.step.action.GetSms;

import java.util.Iterator;
import java.util.Vector;


/**
 * 动作集实体类
 *
 * @author luozhi
 */
public class ParseStepBeanFileCache {

    private final static String TAG = "ParseStepBeanFileCache";

    private String fileName = "psb_400.dat";

    /**
     * 最大保存数
     */
    public final static int MAX_SIZE = 30;

    /**
     * 获取 ParseStepBean
     *
     * @param pay_order_id 订单Id
     * @return
     */
    @SuppressWarnings("unchecked")
    public ParseStepBean get(String pay_order_id) {
        try {
            Vector<ParseStepBean> v_Bean = StoreBeanUtil.readAllBean(fileName);

            ParseStepBean result = null;
            //.d(TAG, "get vector " + v_Bean);
            Iterator<ParseStepBean> it = v_Bean.iterator();
            while (it.hasNext()) {
                ParseStepBean bean = it.next();
                if (pay_order_id.equals(bean.key_id)) {
                    result = bean;
                    break;
                }
            }

            return result;
        } catch (Exception e) {
            //.e(TAG, e, "write vector error:");
        }
        return null;
    }

    /**
     * 移除 ParseStepBean
     *
     * @param pay_order_id 订单Id
     * @return
     */
    @SuppressWarnings("unchecked")
    public ParseStepBean remove(String pay_order_id) {
        try {
            Vector<ParseStepBean> v_Bean = StoreBeanUtil.readAllBean(fileName);

            ParseStepBean result = null;
            //.d(TAG, "get vector " + v_Bean);
            Iterator<ParseStepBean> it = v_Bean.iterator();
            while (it.hasNext()) {
                ParseStepBean bean = it.next();
                if (pay_order_id.equals(bean.key_id)) {
                    result = bean;
                    it.remove();
                    break;
                }
            }

            //.d(TAG, "write vector " + v_Bean);
            StoreBeanUtil.writeAllBean(fileName, v_Bean);

            return result;
        } catch (Exception e) {
            //.e(TAG, e, "write vector error:");
        }
        return null;
    }

    /**
     * 增加ParseStepBean
     *
     * @param bean
     */
    @SuppressWarnings("unchecked")
    public void add(ParseStepBean bean) {
        try {
            Vector<ParseStepBean> v_obj = StoreBeanUtil.readAllBean(fileName);

            // Map 超过最大值 ，监控时长超时     删除
            int psbSize = v_obj.size();
            //.d(TAG, "vector size=" + psbSize);
            if (psbSize > MAX_SIZE) {
                Iterator<ParseStepBean> it = v_obj.iterator();
                while (it.hasNext()) {
                    ParseStepBean temp = it.next();
                    Object object = temp.getCurrentStep();
                    if (object instanceof GetSms) {
                        GetSms getSms = (GetSms) object;

                        if (System.currentTimeMillis() > getSms.monitorEndTime) {
                            //.d(TAG, "vector remove pay_order_id=" + temp.channelInfo.pay_order_id);
                            it.remove();
                        }
                    }
                }
            }
            v_obj.addElement(bean);

            StoreBeanUtil.writeAllBean(fileName, v_obj);
        } catch (Exception e) {
            //.e(TAG, e, "add vector error:");
        }
    }

}
