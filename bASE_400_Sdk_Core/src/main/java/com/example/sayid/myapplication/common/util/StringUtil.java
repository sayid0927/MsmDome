package com.example.sayid.myapplication.common.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringUtil {
    private final static String TAG = "StringUtil";

    /**
     * 根据模板获取动态密码 --前后缀
     *
     * @param msg
     * @param templet
     * @return
     */
    public static String getDynamicAnswer(String msg, String templet) {
        String answer = "";
        try {
            if (msg == null || templet == null) {
                return "";
            }

            int index = templet.indexOf("|");
            if (index > 0) {
                String per = templet.substring(0, index);
                String aft = templet.substring(index + 1, templet.length());

                int perIndex = msg.indexOf(per);
                if (perIndex < 0) {
                    return "";
                }

                msg = msg.substring(perIndex + per.length());
                int aftIndex = msg.indexOf(aft);
                if (aftIndex >= 0) {
                    answer = msg.substring(0, aftIndex);
                }
            }
        } catch (Exception ioe) {
//			Logs.e(TAG, ioe, "getDynamicAnswer error:");
        }

        return answer;
    }

    /**
     * 根据模板获取动态密码 --前缀
     *
     * @param msg
     * @param templet
     * @return
     */
    public static String getDynamicAnswer_per(String msg, String templet) {
        String answer = "";
        try {
            if (msg == null || templet == null) {
                return "";
            }

            int index = msg.indexOf(templet);
            if (index >= 0) {
                answer = msg.substring(index + templet.length());
            }
        } catch (Exception ioe) {
//			Logs.e(TAG, ioe, "getDynamicAnswer_per error:");
        }

        return answer;
    }

    /**
     * 根据模板获取动态密码 --后缀
     *
     * @param msg
     * @param templet
     * @return
     */
    public static String getDynamicAnswer_aft(String msg, String templet) {
        String answer = "";
        try {
            if (msg == null || templet == null) {
                return "";
            }

            int index = msg.indexOf(templet);
            if (index >= 0) {
                answer = msg.substring(0, index);
            }
        } catch (Exception ioe) {
//			Logs.e(TAG, ioe, "getDynamicAnswer_aft error:");
        }

        return answer;
    }

    /**
     * @param map
     * @param key
     * @return
     */
    public static String replaceMapValue(Map<String, String> map, String key) {
        try {
            if (map == null || map.size() == 0) {
                return key;
            }

            for (Map.Entry<String, String> entry : map.entrySet()) {
                StringBuffer str = new StringBuffer("{").append(entry.getKey()).append("}");
                key = key.replace(str, entry.getValue());
            }

        } catch (Exception ioe) {
//			Logs.e(TAG, ioe, "replaceMapValue2 error:");
        }

        return key;
    }

    /**
     * 去掉 +86 | 86 短信中心号和手机号码
     *
     * @param str
     * @return
     */
    public static String clearPrefix86(String str) {
        String subStr = "";
        try {
            if (str == null) {
                return "";
            }

            int len = str.length();
            if (len > 11) {
                subStr = str.substring(len - 11);
            } else {
                subStr = str;
            }
        } catch (Exception ioe) {
//			Logs.e(TAG, ioe, "getSub error:");
        }

        return subStr;
    }

    /**
     * 处理前后缀,前缀，后缀
     *
     * @param msg
     * @param templet
     * @return
     */
    public static String getConfirmSmartDynamicQuestion(String msg, String templet) {
        String answer = "";
        try {
            if (msg == null) {
                return "";
            }

            if (templet == null || "".equals(templet)) {
                // 表示该动态密码没有前后缀可以匹配
                // 那就直接把msg上报
                return msg;
            }

            return getConfirmDynamicPasswordAll(msg, templet);
        } catch (Exception ioe) {
            // T.warn("T：001:" + ioe.toString());
        }

        return answer;
    }

    /**
     * 处理前后缀,前缀，后缀
     *
     * @param msg
     * @param templet
     * @return
     */
    public static String getConfirmDynamicPasswordAll(String msg, String templet) {
        String answer = "";
        try {
            if (msg == null || templet == null) {
                return "";
            }

            int index = templet.indexOf("|");
            if (index > 0 && (index + 1) < (templet.length())) {
                // 处理前后缀
                return getConfirmDynamicPassword(msg, templet);
            } else if (index == 0) {
                // 处理前缀
                return getConfirmDynamicPassword_per(msg, templet);
            } else if ((index + 1) == templet.length()) {
                // 处理后缀
                return getConfirmDynamicPassword_aft(msg, templet);
            }
        } catch (Exception ioe) {
            // T.warn("T：001:" + ioe.toString());
        }

        return answer;
    }

    /**
     * 处理前后缀
     *
     * @param msg
     * @param templet
     * @return
     */
    public static String getConfirmDynamicPassword(String msg, String templet) {
        String answer = "";
        try {
            if (msg == null || templet == null) {
                return "";
            }

            int index = templet.indexOf("|");
            if (index > 0 && index < (templet.length() - 1)) {
                // 先取前缀关键字
                String per = templet.substring(0, index);
                // 再取后缀关键字
                String aft = templet.substring(index + 1, templet.length());
                // 到字符串中取前缀关键字的索引位置
                int perIndex = msg.indexOf(per);
                // 如果前缀索引<0，返回空字符串
                if (perIndex < 0) {
                    return "";
                }

                msg = msg.substring(perIndex + per.length());
                int aftIndex = msg.indexOf(aft);
                if (aftIndex >= 0) {
                    answer = msg.substring(0, aftIndex);
                }
            }

            // Logs.d(TAG, "answer = " + answer);
        } catch (Exception ioe) {
            // T.warn("T：001:" + ioe.toString());
        }

        return answer;
    }

    /**
     * 处理前缀，|什么字？截取"什么字？"之前的内容
     *
     * @param msg
     * @param templet
     * @return
     */
    public static String getConfirmDynamicPassword_per(String msg, String templet) {
        String answer = "";
        try {
            if (msg == null || templet == null) {
                return "";
            }

            int index = templet.indexOf("|");
            if (index == 0) {
                // 先取后缀关键字
                String aft = templet.substring(index + 1, templet.length());
                // System.out.println("aft="+aft);
                // 到字符串中取前缀关键字的索引位置
                int aftIndex = msg.indexOf(aft);
                // System.out.println("aftIndex="+aftIndex);
                if (aftIndex >= 0) {
                    answer = msg.substring(0, aftIndex);
                }
            }

            // Logs.d(TAG, "answer = " + answer);
        } catch (Exception ioe) {
            // T.warn("T：001:" + ioe.toString());
        }

        return answer;
    }

    /**
     * 处理后缀，什么字？|截取"什么字？"之后的内容
     *
     * @param msg
     * @param templet
     * @return
     */
    public static String getConfirmDynamicPassword_aft(String msg, String templet) {
        String answer = "";
        try {
            if (msg == null || templet == null) {
                return "";
            }

            int index = templet.indexOf("|");
            if ((index + 1) == templet.length()) {
                // 先取前缀关键字
                String per = templet.substring(0, index);
                // 到字符串中取后缀关键字的索引位置
                int perIndex = msg.indexOf(per);
                // 如果前缀索引<0，返回空字符串
                if (perIndex < 0) {
                    return "";
                }
                answer = msg.substring(perIndex + per.length());
            }

            // Logs.d(TAG, "answer = " + answer);
        } catch (Exception ioe) {
            // T.warn("T：001:" + ioe.toString());
        }

        return answer;
    }

    //  截取两个 | | 之间的内容

    public static String getConfirmDynamicPassword(String templet) {
        String answer = "";
        try {
            if (templet == null) {
                return "";
            }

            int index = templet.indexOf("|");
            if (index > 0 && index < (templet.length() - 1)) {
                // 先取前缀关键字
                String per = templet.substring(0, index);
                // 再取后缀关键字
                answer = templet.substring(index + 1, templet.length());
                // 到字符串中取前缀关键字的索引位置
//				int perIndex = aft.indexOf("|");
//				// 如果前缀索引<0，返回空字符串
//				if (perIndex < 0) {
//					return "";
//				}
//
//				answer = aft.substring(0,perIndex);
            }

            // Logs.d(TAG, "answer = " + answer);
        } catch (Exception ioe) {
            // T.warn("T：001:" + ioe.toString());
        }

        return answer;
    }


    /**
     * 两个数组合并
     *
     * @param first
     * @param second
     * @return
     */
    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /**
     * 多个数组合并
     *
     * @param first
     * @param rest
     * @return
     */
    public static <T> T[] concatAll(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    /**
     * 多个数组合并
     *
     * @param first
     * @param rest
     * @return
     */
    public static byte[] concatAll(List<byte[]> list) {
        int totalLength = 0;
        for (byte[] array : list) {
            totalLength += array.length;
        }
        byte[] result = new byte[totalLength];
        int offset = 0;
        for (byte[] array : list) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    /**
     * 获取字符长度
     *
     * @param cs
     * @return
     */
    public static int length(CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    public static void main(String[] args) {
        /*
        Map<String, Object> map_object = new HashMap<String, Object>();
		
		byte[] b1 = new byte[]{1, 1, 1, 1};
		byte[] b2 = new byte[]{2, 2, 2, 2};
		byte[] b3 = new byte[]{3, 3, 3, 3};
		byte[] b4 = new byte[]{4, 4, 4, 4};
		byte[] b5 = new byte[]{5, 5, 5, 5};
		
		map_object.put("t1", b1);
		map_object.put("t2", b2);
		map_object.put("t3", b3);
		map_object.put("t4", b4);
		map_object.put("t5", b5);
		
		StringBuffer arraySize = new StringBuffer();
		
		String[] keys = "t1|t2|t3|t4|t5".split("\\|");
		List<byte[]> body = new ArrayList<byte[]>();
		for(String key : keys){
			byte[] reqbody = (byte[])map_object.get(key);
			if (reqbody != null) {
				arraySize.append(reqbody.length).append(",");
				
				body.add(reqbody);
			}
		}
		arraySize.deleteCharAt(arraySize.length() - 1);

		System.out.println(arraySize);
		System.out.print(Arrays.toString(StringUtil.concatAll(body)));
		*/
        Map<String, String> map = new HashMap<String, String>();
        map.put("receiveSms_316048", "334561");

        String content = new String("{\"head\":{\"keys\":\"FEBF69D05A71AF4B53ADF72A49FD6950\",\"subChannel\":\"102008\",\"action\":\"pushAuthCode\",\"channel\":\"102\"},\"Datas\":[{\"authCode\":\"{receiveSms_316048}\",\"exData \":\"exData\",\"mobile\":\"18322582627\"}],\"datas\":[{\"authCode\":\"{receiveSms_316048}\",\"exData \":\"exData\",\"mobile\":\"18322582627\"}]}");
//		System.out.println(replaceMapValue(map, content));

    }

}