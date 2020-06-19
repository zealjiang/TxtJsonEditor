package com.example.zealjiang.util;

import android.annotation.SuppressLint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zealjiang.MyApplication;
import com.example.zealjiang.txtjsoneditor.R;


/**
 * @author jinemng
 * 2015.4.20
 * 修改下Toast，避免一直弹不消失
 */
public class ToastUtil {

	private ToastUtil() {
		throw new UnsupportedOperationException("cannot be instantiated");
	}

	/**
	 * 场景：当UI线程崩溃会导致toast一直显示不消失，部分手机（Coolpad 8675-FHD Android4.4.4、三星 SM-C1158 Android4.4.2）子线程崩溃也会导致toast一直显示不消失；
	 *
	 * 另外：如果使用Dialog套用Toast的window主题代替Toast显示，在部分手机（如：小米）上需要开启“悬浮窗权限”才能显示；
	 */

	private static ToastCompat toast;
	@SuppressLint("StaticFieldLeak")
	private static ViewGroup toastLayout;
	@SuppressLint("StaticFieldLeak")
	private static TextView txtToast;

	public static void showToastCenter(String message){
		showToastCenter(message , Toast.LENGTH_SHORT);
	}

	public static void showToastCenterLong(String message){
		showToastCenter(message , Toast.LENGTH_LONG);
	}

	public static void showToastCenterDuration(String message , int duration){
		showToastCenter(message , duration);
	}

	private static void showToastCenter(String message , int toastLength){
		try {
			if (toast == null) {
				toast = new ToastCompat(MyApplication.getContext());
			}
			toast.setDuration(toastLength);
			toast.setGravity(Gravity.CENTER, 0, 0);
//			if (toastLayout == null) {
				toastLayout = (ViewGroup) LayoutInflater.from(MyApplication.getContext()).inflate(R.layout.toast_layout, null);
//			}
			if(toastLayout.getParent() != null && toastLayout.getParent() instanceof WindowManager){
				((WindowManager)toastLayout.getParent()).removeView(toastLayout);
			}
//			if (txtToast == null && toastLayout != null) {
				txtToast = toastLayout.findViewById(R.id.txt_toast);
//			}
			if (txtToast != null) {
				txtToast.setText(message);
			}
			if (toastLayout != null) {
				toast.setView(toastLayout);
			}
			toast.show();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public static void clear() {
		toast = null;
		toastLayout = null;
		txtToast = null;
	}

}