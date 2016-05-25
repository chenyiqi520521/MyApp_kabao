package com.apicloud.util;

import android.app.Activity;
import android.util.DisplayMetrics;

/**
 * 取屏幕宽高
 * 
 * @author 作者 :zhuxiaohao
 * @version 创建时间：2014年8月12日 下午1:45:30
 */
public class Configure {

   public static final int KT_CONNECTED=1;
   public static final int LY_CONNECTED=3;
   public static final int KT_DISCONNECTED=2;
   public static final int LY_DISCONNECTED=4;
   public static final int ME15_DISCONNECTED=6;
   public static final int ME15_CONNECTED=5;
   public static final int DH_DISCONNECTED=8;
   public static final  int DH_CONNECTED=7;
	/** 是否移动 */
	public static boolean isMove = false;
	/** 是否换页 */
	public static boolean isChangingPage = false;
	/** 删除按钮变暗 */
	public static boolean isDelDark = false;
	/** 屏幕高度 */
	public static int screenHeight = 0;
	/** 屏幕宽度 */
	public static int screenWidth = 0;
	/** 屏幕密度 */
	public static float screenDensity = 0;
	/** 当前页 */
	public static int curentPage = 0;
	/** 总的页数 */
	public static int countPages = 0;
	/** 删除的页 */
	public static int removeItem = 0;
	/** 是否显示删除按钮 */
	public static boolean isdeleteShow = false;
	/** 交换item */
	public static boolean isChangeItem = false;

	public static void init(Activity context) {
		if (screenDensity == 0 || screenWidth == 0 || screenHeight == 0) {
			DisplayMetrics dm = new DisplayMetrics();
			context.getWindowManager().getDefaultDisplay().getMetrics(dm);
			Configure.screenDensity = dm.density;
			Configure.screenHeight = dm.heightPixels;
			Configure.screenWidth = dm.widthPixels;
		}
		curentPage = 0;
		countPages = 0;
	}

	public int[] ret(int[] intArray) {
		int size = intArray.length;
		for (int i = size - 1; i >= 0; i--)
			for (int j = 0; j < i; j++)
				if (intArray[j] > intArray[j + 1]) {
					int t = intArray[j];
					intArray[j] = intArray[j + 1];
					intArray[j + 1] = t;
				}
		return intArray;
	}

	public static int getScreenHeight(Activity context) {
		if (screenWidth == 0 || screenHeight == 0) {
			DisplayMetrics dm = new DisplayMetrics();
			context.getWindowManager().getDefaultDisplay().getMetrics(dm);
			Configure.screenDensity = dm.density;
			Configure.screenHeight = dm.heightPixels;
			Configure.screenWidth = dm.widthPixels;
		}
		return screenHeight;
	}

	public static int getScreenWidth(Activity context) {
		if (screenWidth == 0 || screenHeight == 0) {
			DisplayMetrics dm = new DisplayMetrics();
			context.getWindowManager().getDefaultDisplay().getMetrics(dm);
			Configure.screenDensity = dm.density;
			Configure.screenHeight = dm.heightPixels;
			Configure.screenWidth = dm.widthPixels;
		}
		return screenWidth;
	}
}