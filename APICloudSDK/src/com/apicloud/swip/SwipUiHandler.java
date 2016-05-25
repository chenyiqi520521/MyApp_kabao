package com.apicloud.swip;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SwipUiHandler extends Handler {

	public String TAG = "ApiUiHandler";

	public static final int SHOW_TIP = 1;// 显示气泡
	public static final int SHOW_TEXT = 2;// 显示文字
	public static final int SHOW_ANIM = 3;// 显示动画
	public static final int SHOW_CHANGE = 4;// 切换sdk

	public static final int ANIMATION_PLS_PLUGDEV = 1;// 请插入设备
	public static final int ANIMATION_PLS_SWIPER = 2;// 请刷卡
	public static final int ANIMATION_PLS_PWD = 3;// 请输入密码
	public static final int ANIMATION_PLS_OKDEV = 4;// 设备已插入
	public static final int ANIMATION_CAMCLE = 5;// 蓝牙按键取消
	public SwipUiHandler(Activity activity) {
		TAG = activity.getClass().getSimpleName();
	}

	public Message getTipMessage(String tip) {
		Message msg = this.obtainMessage();
		msg.what = SHOW_TIP;
		msg.obj = tip;
		return msg;
	}

	public Message getTextMessage(String text) {
		Message msg = this.obtainMessage();
		msg.what = SHOW_TEXT;
		msg.obj = text;
		return msg;
	}

	public Message getAnimMessage(int anim) {
		Message msg = this.obtainMessage();
		msg.what = SHOW_ANIM;
		msg.obj = anim;
		return msg;
	}
	
	public Message getchangeSdkMessage(int sdktype){
		Message msg = this.obtainMessage();
		msg.what = SHOW_CHANGE;
		msg.obj=sdktype;
		return msg;
	}

	@Override
	public void dispatchMessage(Message msg) {
		switch (msg.what) {
		case SHOW_TIP:
			showTip(msg);
			break;
		case SHOW_TEXT:
			showText(msg);
			break;
		case SHOW_ANIM:
			showAnimation(msg);
			break;
		case SHOW_CHANGE:
			showChangeSdk(msg);
		default:
			break;
		}
	}

	protected void showTip(Message msg) {
		Log.e(TAG, "no implements showTip");
	}

	protected void showText(Message msg) {
		Log.e(TAG, "no implements showText");
	}

	protected void showAnimation(Message msg) {
		Log.e(TAG, "no implements showAnimation");
	}
	
	
	protected void showChangeSdk(Message msg) {
		
	}
}
