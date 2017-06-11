package lx.own.util;

import android.support.annotation.NonNull;

import java.security.MessageDigest;

/**
 * <p>文件相关的工具类</p><br/>
 *
 * @author Lx
 * @date 2017/2/7
 */
public class FileUtil {

    /**
     * 获取String摘要计算为MD5值的方法
     *
     * @param string 需要进行计算的字符串
     * @return MD5值
     */
    @NonNull
    public static String encodeMD5(@NonNull String string) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            MessageDigest digest = MessageDigest.getInstance("md5");
            byte[] bytes = digest.digest(string.getBytes());
            String s;
            for (byte b : bytes) {
                s = Integer.toHexString(b & 0xFF);
                if (s.length() == 1) {
                    stringBuilder.append("0" + s);
                } else {
                    stringBuilder.append(s);
                }
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
