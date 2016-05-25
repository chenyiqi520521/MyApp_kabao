package com.apicloud.swip;


import android.content.Intent;

/**
 * 
 * @ClassName: Api
 * @author:Fan
 * @date: 2014-10-14
 * @Description: 设备API框架
 * 
 */
public abstract class SwipApi {

	private static final String TAG = "Api";

	public abstract void openDev();//打开设备
	
	public abstract void configuration();//配置

	public abstract void pushPinKey();//灌秘钥
	public abstract  boolean isConnect();//判断是不是连接

	public abstract void connectDev();//连接设备
	public abstract void disConnect();//断开连接

	public abstract void nofitySystemSetting();

	public abstract  void activityResult(int requestCode, int resultCode, Intent data);//给页面反馈
	public abstract void getDeviceInfo();//获取设备信息

	public abstract void SwipCard();//刷卡
	public abstract void enterPin();//输入pin
	public abstract void encrtyPinkey(Object obj);//加密pin
	public abstract void closeDev();//关闭设备
	public static final int WHATDO_GET_CARDNO = 1;// 只是获取卡号
	public static final int WHATDO_SWIPER = 2;// 刷卡

}
