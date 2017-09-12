package com.example.sayid.myapplication.common.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;


/**
 * 内存缓存工具类
 *
 * @author luozhi
 */
public class MemoryCache<T> {

    private final static String TAG = "MemoryCache";

    /**
     * 放入缓存时是个同步操作
     * LinkedHashMap构造方法的最后一个参数false代表这个map里的元素将按照插入顺序排列。
     */
    private Map<String, T> cache = Collections.synchronizedMap(new LinkedHashMap<String, T>(10, 1.5f, false));

    /**
     * 缓存只能占用的最大个数
     */
    private int maxSize = 30;// max memory in size

    /**
     * @param new_maxSize
     */
    public void setMaxSize(int new_maxSize) {
        maxSize = new_maxSize;
        //.d(TAG, "MemoryCache will use max size to " + maxSize);
    }

    /**
     * 获取指定对象
     *
     * @param id
     * @return
     */
    public T get(String id) {
        return cache.get(id);
    }

    /**
     * 删除指定对象
     *
     * @param id
     * @return
     */
    public T remove(String id) {
        return cache.remove(id);
    }

    /**
     * 设置指定对象
     *
     * @param id
     * @param obj
     */
    public void put(String id, T obj) {
        try {
            cache.put(id, obj);
            checkSize();
        } catch (Throwable th) {
            //.e(TAG, th, "put error:");
        }
    }

    /**
     * 严格控制堆内存，如果超过将首先替换最新插入的缓存
     */
    private void checkSize() {
        int size = cache.size();
        //.d(TAG, "cache size=" + size + " length=" + maxSize);
        if (size > maxSize) {
            // 先遍历最新插入使用的元素
            Iterator<Entry<String, T>> iter = cache.entrySet().iterator();
            while (iter.hasNext()) {
                iter.remove();
                break;
            }
            //.d(TAG, "Clean cache. New size " + cache.size());
        }
    }

    public void clear() {
        cache.clear();
    }
}
