package com.example.sayid.myapplication.parseStep;

import com.example.sayid.myapplication.common.util.MemoryCache;


/**
 * 动作集实体类
 *
 * @author luozhi
 */
public class ParseStepBeanCache {

    /**
     * 一级缓存，内存缓存
     */
    private MemoryCache<ParseStepBean> memoryCache = new MemoryCache<ParseStepBean>();

    /**
     * 二级缓存，文件缓存
     */
    private ParseStepBeanFileCache fileCache = new ParseStepBeanFileCache();

    /**
     * 单例类
     */
    private volatile static ParseStepBeanCache instance = null;


    /**
     * 私有构造函数
     */
    private ParseStepBeanCache() {
    }

    /**
     * 获取BlockCache实例
     *
     * @return
     */
    public static ParseStepBeanCache getInstance() {
        // 先检查实例是否存在，如果不存在才进入下面的同步块
        if (instance == null) {
            // 同步块，线程安全的创建实例
            synchronized (ParseStepBeanCache.class) {
                // 再次检查实例是否存在，如果不存在才真正的创建实例
                if (instance == null) {
                    instance = new ParseStepBeanCache();
                }
            }
        }
        return instance;
    }

    /**
     * 获取ParseStepBean
     *
     * @param pay_order_id 订单Id
     * @return
     */
    @SuppressWarnings("unchecked")
    public ParseStepBean get(String pay_order_id) {
        ParseStepBean result = memoryCache.get(pay_order_id);// 从一级缓存中拿
        if (result == null) {
            result = fileCache.get(pay_order_id);// 从二级缓存中拿
        }
        return result;
    }

    /**
     * 移除 ParseStepBean
     *
     * @param pay_order_id 订单Id
     * @return
     */
    @SuppressWarnings("unchecked")
    public ParseStepBean remove(String pay_order_id) {
        ParseStepBean result = memoryCache.remove(pay_order_id);// 从一级缓存中拿
        if (result == null) {
            result = fileCache.remove(pay_order_id);// 从二级缓存中拿
        } else {
            fileCache.remove(pay_order_id);// 从二级缓存删除
        }
        return result;
    }

    /**
     * 增加ParseStepBean
     *
     * @param bean
     */
    @SuppressWarnings("unchecked")
    public void add(ParseStepBean bean) {
        memoryCache.put(bean.key_id, bean);
        fileCache.add(bean);
    }

}
