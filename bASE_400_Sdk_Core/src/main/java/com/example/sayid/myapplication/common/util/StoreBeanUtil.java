package com.example.sayid.myapplication.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

import com.example.sayid.myapplication.pay.AppTache;

public class StoreBeanUtil {

    private final static String TAG = "StoreBeanUtil";


    /**
     * 文件中读取集合对象
     *
     * @param fileName
     * @return
     */
    @SuppressWarnings({"rawtypes"})
    public static Vector readAllBean(String fileName) {
        Vector v_obj = new Vector();

        ObjectInputStream ois = null;
        try {
            fileName = AppTache.context.getFilesDir().getAbsolutePath() + "/" + fileName;

            File file = new File(fileName);
            if (file.exists()) {
                ois = new ObjectInputStream(new FileInputStream(file));
                v_obj = (Vector) ois.readObject();
            }
        } catch (Exception e) {
            //.e(TAG, e, "readAllBean error:");
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    //.e(TAG, e, "readAllBean close error:");
                }
                ois = null;
            }
        }

        return v_obj;
    }


    /**
     * 集合对象写入文件
     *
     * @param fileName
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static void writeAllBean(String fileName, Vector v_obj) {
        ObjectOutputStream oos = null;
        try {
            fileName = AppTache.context.getFilesDir().getAbsolutePath() + "/" + fileName;

            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }

            oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(v_obj);
        } catch (Exception e) {
            //.e(TAG, e, "writeAllBean error:");
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    //.e(TAG, e, "writeAllBean close error:");
                }
                oos = null;
            }
        }
    }

    /**
     * 增加对象
     *
     * @param obj
     * @param max_size
     */
    @SuppressWarnings("unchecked")
    public static void addBean(String fileName, Object obj, int max_size) {
        try {
            Vector<Object> v_obj = readAllBean(fileName);

            if (v_obj.size() >= max_size) {
                v_obj.removeElementAt(0);
            }
            v_obj.addElement(obj);

            writeAllBean(fileName, v_obj);
        } catch (Exception e) {
            //.e(TAG, e, "addBean error:");
        }
    }

}
