package com.suneee.localsocket.util;

/**
 * Created by QueRenJie on ${date}
 */
public class StringOperUtil {
    /**
     * 如果字符串的长度小于fixedLength的话就在左边填充filledStr的内容
     * @param originStr
     * @param fixedLength
     * @param filledStr
     * @return
     */
    public static String padLeft(String originStr, int fixedLength, String filledStr) {
        String str = originStr;
        int lengthOfStr = originStr.length();
        if (lengthOfStr < fixedLength) {
            for (int i = 0; i < fixedLength - lengthOfStr; i++) {
                str = filledStr + str;
            }
        }
        return str;
    }
}
