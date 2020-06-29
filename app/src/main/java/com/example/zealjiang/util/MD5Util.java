
package com.example.zealjiang.util;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MD5Util {

    private static final String TAG = "MD5Util";

    public static final String ALGORITHM = "MD5";

    public static final String DEFAULT_CHARSET = "UTF-8";

    public static String md5LowerCase(String string) {
        if (!TextUtils.isEmpty(string)) {
            try {
                byte[] buffer = string.getBytes(DEFAULT_CHARSET);
                if (buffer != null && buffer.length > 0) {
                    MessageDigest digester = MessageDigest.getInstance(ALGORITHM);
                    digester.update(buffer);
                    buffer = digester.digest();
                    string = toLowerCaseHex(buffer);
                }
            } catch (NoSuchAlgorithmException e) {
            } catch (UnsupportedEncodingException e) {
            } catch (Exception e) {
            }
        }

        return string;
    }

    private static String toLowerCaseHex(byte[] b) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            int v = b[i];
            builder.append(HEX_LOWER_CASE[(0xF0 & v) >> 4]);
            builder.append(HEX_LOWER_CASE[0x0F & v]);
        }
        return builder.toString();
    }

    private static final char[] HEX_LOWER_CASE = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

}
