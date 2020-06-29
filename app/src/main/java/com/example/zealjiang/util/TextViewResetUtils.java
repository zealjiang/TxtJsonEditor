package com.example.zealjiang.util;

import android.graphics.Paint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.TextView;

public class TextViewResetUtils {
    /**
     * 外边转换好px宽度
     * @param textView
     * @param text
     * @param maxWidth
     */
    public static void reSizeTextView(TextView textView, String text, float maxWidth){
        if (textView == null || TextUtils.isEmpty(text)) {
            return;
        }
        Paint paint = textView.getPaint();
        if (paint == null) {
            return;
        }
        float textWidth = paint.measureText(text);
        int textSizeInDp = 30;

        if(textWidth > maxWidth){
            for(;textSizeInDp > 0; textSizeInDp--){
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSizeInDp);
                paint = textView.getPaint();
                textWidth = paint.measureText(text);
                if(textWidth <= maxWidth){
                    break;
                }
            }
        }
        textView.invalidate();
    }

    public static int getReTextViewSize(TextView textView, String text, float maxWidth){
        if (textView == null || TextUtils.isEmpty(text)) {
            return -1;
        }
        Paint paint = textView.getPaint();
        if (paint == null) {
            return -1;
        }
        float textWidth = paint.measureText(text);
        int textSizeInDp = 30;

        if(textWidth > maxWidth){
            for(;textSizeInDp > 0; textSizeInDp--){
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSizeInDp);
                paint = textView.getPaint();
                textWidth = paint.measureText(text);
                if(textWidth <= maxWidth){
                    return textSizeInDp;
                }
            }
        }
        textView.invalidate();
        return textSizeInDp;
    }

    /**
     * 传入dp宽度
     * @param textView
     * @param text
     * @param dp_Width
     */
    public static void reSizeTextView(TextView textView, String text, int dp_Width){
        if (textView == null || TextUtils.isEmpty(text)) {
            return;
        }
        float maxWidth = ScreenUtils.dipConvertPx(dp_Width);
        Paint paint = textView.getPaint();
        if (paint == null) {
            return;
        }
        float textWidth = paint.measureText(text);
        int textSizeInDp = 30;

        if(textWidth > maxWidth){
            for(;textSizeInDp > 0; textSizeInDp--){
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSizeInDp);
                paint = textView.getPaint();
                textWidth = paint.measureText(text);
                if(textWidth <= maxWidth){
                    break;
                }
            }
        }
        textView.invalidate();
    }
}
