/**
 * Project Name:CardPay
 * File Name:Common.java
 * Package Name:com.apicloud.common
 * Date:2015-4-23上午10:05:52
 * Copyright (c) 2015, zhuxiaohao All Rights Reserved.
 *
 */

package com.apicloud.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * ClassName:Common <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2015-4-23 上午10:05:52 <br/>
 * 
 * @author zhuxiaohao blog:http://blog.csdn.net/qq718799510?viewmode=contents
 * @version 1.0.0
 * @since JDK 1.6
 * @see 一些参数配置
 */
public class Common {

	/** 音频驱动 */
	public static final String ME11_DRIVER_NAME = "com.newland.me.ME11Driver";

	/** 刷卡或者插卡 */
	public static final int SWIPCARD_ME11 = 0x0001;
	/** 取消状态 */
	public static final int CANCEL = 0x0002;
	/** 取得设备信息 */
	public static final int FETCH_DEVICE_INFO = 0x0003;
	// public static String URL ="http://app.fxscloudos.com/";
	// public static String URL = "http://121.43.231.170/klapi/";
	public static String URL = "http://121.43.231.170/klapi2/";

	// public static String URL = "http://183.129.207.156:7008/B2CPay_V2/";
	/** 55 域拼装 */
	public static String getTLV2Str(String tlv) {
		String[] tlvs = tlv.split(":");
		String tlvStr = tlvs[0] + getLen(tlvs[1].length()) + tlvs[1];
		return tlvStr;
	}

	private static String getLen(int valueLen) {
		valueLen = valueLen / 2;
		String ret = Integer.toHexString(valueLen).toUpperCase();
		if (ret.length() == 1) {
			ret = "0" + ret;
		}
		return ret;
	}

	/**
	 * 
	 * matchesPhoneNumber:(电话号码). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param phone_number
	 * @return
	 * @since JDK 1.6
	 */
	public static int matchesPhoneNumber(String phone_number) {
		String cm = "^((13[4-9])|(147)|(15[0-2,7-9])|(18[2-3,7-8]))\\d{8}$";
		String cu = "^((13[0-2])|(145)|(15[5-6])|(186))\\d{8}$";
		String ct = "^((133)|(153)|(18[0,9]))\\d{8}$";
		int flag = 0;
		if (phone_number.matches(cm)) {
			flag = 1;
		} else if (phone_number.matches(cu)) {
			flag = 2;
		} else if (phone_number.matches(ct)) {
			flag = 3;
		} else {
			flag = 4;
		}
		return flag;

	}

	/**
	 * 
	 * whichOperator:(电话归属地). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param x
	 * @return
	 * @since JDK 1.6
	 */
	public static String whichOperator(int x) {
		String telString = "";
		switch (x) {
		case 1:
			telString = "移动号码";
			System.out.println("移动号码");
			break;
		case 2:
			telString = "联通号码";
			System.out.println("联通号码");
			break;
		case 3:
			telString = "电信号码";
			System.out.println("电信号码");
			break;
		case 4:
			telString = "输入有误";
			System.out.println("输入有误");
			break;
		}

		return telString;
	}

	public static void main(String[] args) {
		String e = "14561198278";
		whichOperator(matchesPhoneNumber(e));

	}

	/**
	 * 
	 * conversionPrice:(价格换算). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param lenght
	 * @param price
	 * @return
	 * @since JDK 1.6
	 */
	public static String conversionPrice(String price) {
		String f = "%0" + 12 + "d";
		int number = (int) (Float.parseFloat(price) * 100);
		return String.format(f, number);
	}

	/**
	 * 判断两个字符串是否相等
	 * 
	 * @param cs1
	 * @param cs2
	 * @return
	 */
	public static boolean equals(CharSequence cs1, CharSequence cs2) {
		return cs1 == null ? cs2 == null : cs1.equals(cs2);
	}

	/**
	 * 判断网络是否连接
	 * 
	 * @param context
	 *            场景
	 * @return true:网络已经连接;false:网络断开
	 */
	public static boolean checkNetWork(Context context) {
		// 获得手机所有连接管理对象（包括对wi-fi等连接的管理）
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				// 获得网络连接管理的对象
				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null && info.isConnected()) {
					return true;
				}
			}
		} catch (Exception e) {
		}
		return false;
	}

	/**
	 * 字符串转换成日期
	 * 
	 * @param str
	 * @return date
	 * @throws java.text.ParseException
	 */
	@SuppressLint("SimpleDateFormat")
	public static String StrToDate(String str) {
		String time = "";
		try {
			SimpleDateFormat formatter1 = new SimpleDateFormat("HH:mm:ss");
			SimpleDateFormat formatter2 = new SimpleDateFormat("HHmmss");
			time = formatter1.format(formatter2.parse(str));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return time;
	}

	/**
	 * 字符串转换成日期
	 * 
	 * @param str
	 * @return date
	 * @throws java.text.ParseException
	 */
	@SuppressLint("SimpleDateFormat")
	public static String StrToDateTime(String str) {
		String time = "";
		try {
			if (str != null) {
				SimpleDateFormat formatter1 = new SimpleDateFormat("HH-dd");
				SimpleDateFormat formatter2 = new SimpleDateFormat("HHdd");
				time = formatter1.format(formatter2.parse(str));
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return time;
	}

	@SuppressLint("SimpleDateFormat")
	public static int toDate() {
		SimpleDateFormat formatter1 = new SimpleDateFormat("yyMMDDHHmmss");
		Date cruDate = new Date();
		String cruDateStr = formatter1.format(cruDate);
		// int yy = Integer.parseInt(cruDateStr.substring(0, 2));
		// int mm = Integer.parseInt(cruDateStr.substring(2, 4));
		// int dd = Integer.parseInt(cruDateStr.substring(4, 6));
		// int hh = Integer.parseInt(cruDateStr.substring(6, 8));
		// int mms = Integer.parseInt(cruDateStr.substring(8, 10));
		// int ss = Integer.parseInt(cruDateStr.substring(10, 12));
		return Integer.parseInt(cruDateStr.substring(0, 4));
	}

	/* int -> byte[] */
	public static byte[] intToBytes(int num) {
		byte[] b = new byte[4];
		for (int i = 0; i < 4; i++) {
			b[i] = (byte) (num >>> (24 - i * 8));
		}
		return b;
	}

}
