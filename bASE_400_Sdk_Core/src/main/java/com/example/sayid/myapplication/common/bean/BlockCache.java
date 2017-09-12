package com.example.sayid.myapplication.common.bean;

import java.util.Vector;

import com.example.sayid.myapplication.common.util.Logs;
import com.example.sayid.myapplication.common.util.StoreBeanUtil;


/**
 * 二次确认拦截，SP运营商拦截类
 *
 * @author luozhi
 */
public class BlockCache {
    /**
     * 运营商二次确认拦截
     */
    public final static String TYPE_OP_SECOND_BLOCK = "second_400.dat";
    /**
     * SP,运营商拦截
     */
    public final static String TYPE_SP_OP_BLOCK = "cp_400.dat";
    /**
     * 最大保存数
     */
    public final static int MAX_SIZE = 30;

    /**
     * 单例类
     */
    private volatile static BlockCache instance = null;

    /**
     * 私有构造函数
     */
    private BlockCache() {
    }

    /**
     * 获取BlockCache实例
     *
     * @return
     */
    public static BlockCache getInstance() {
        // 先检查实例是否存在，如果不存在才进入下面的同步块
        if (instance == null) {
            // 同步块，线程安全的创建实例
            synchronized (BlockCache.class) {
                // 再次检查实例是否存在，如果不存在才真正的创建实例
                if (instance == null) {
                    instance = new BlockCache();
                }
            }
        }
        return instance;
    }

    /**
     * 获取BlockBean集合
     *
     * @param type 类型名
     * @return
     */
    @SuppressWarnings("unchecked")
    public Vector<BlockBean> gets(String type) {
        return StoreBeanUtil.readAllBean(type);
    }

    /**
     * 增加BlockBean
     *
     * @param type
     * @param bean
     */
    public void add(String type, BlockBean bean) {
        StoreBeanUtil.addBean(type, bean, MAX_SIZE);
    }

    /**
     * 保存BlockBean
     *
     * @param type
     * @param beanInfo
     */
    public void put(String type, BlockBean beanInfo) {
        Vector<BlockBean> v_Bean = gets(type);

        //.d("BlockCache", "get vector " + v_Bean);
        for (BlockBean bean : v_Bean) {
            if (bean.pay_order_id.equals(beanInfo.pay_order_id)) {
                int index = v_Bean.indexOf(bean);
                v_Bean.setElementAt(beanInfo, index);
                break;
            }
        }

        //.d("BlockCache", "write vector " + v_Bean);

        StoreBeanUtil.writeAllBean(type, v_Bean);
    }

}
