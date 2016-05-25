package com.apicloud.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class commonUtil {
	
	/**
	 * 
	 * TODO强制倒开GPS
	   @param context
	   2015年12月7日
	   void
	 */
	public static final void openGPS(Activity context){
		Intent GPSintent=new Intent();
		GPSintent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
		GPSintent.addCategory("android.intent.category.ALTERNATIVE");
		GPSintent.setData(Uri.parse("custom:3"));
		try {
			PendingIntent.getBroadcast(context, 0,GPSintent,0);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	static final Point screenSize = new Point();
	@SuppressLint("NewApi")
	public static Point getScreenSize(Context ctt) {
		if (ctt == null) {
			return screenSize;
		}
		WindowManager wm = (WindowManager) ctt
				.getSystemService(Context.WINDOW_SERVICE);
		if (wm != null) {
			DisplayMetrics mDisplayMetrics = new DisplayMetrics();
			Display diplay = wm.getDefaultDisplay();
			if (diplay != null) {
				if (Build.VERSION.SDK_INT > 16)// Build.VERSION_CODES.JELLY_BEAN
				{
					diplay.getRealMetrics(mDisplayMetrics);
				} else {
					diplay.getMetrics(mDisplayMetrics);
				}
				int W = mDisplayMetrics.widthPixels;
				int H = mDisplayMetrics.heightPixels;
				if (W * H > 0 && (W > screenSize.x || H > screenSize.y)) {
					screenSize.set(W, H);
					// Log.i(TAG, "screen size:" + screenSize.toString());
				}
			}
		}
		/*if (StoreConfig.DEBUG) {
			Log.i(TAG, screenSize.toString());
		}*/
		return screenSize;
	}
	/**
	 * 4.0以后强制打开gps
	 */
	public static final void openGPS40(Activity context){
		
		    Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE"); 
		    intent.putExtra("enabled", true); 
		    context.sendBroadcast(intent); 
		 
		    String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED); 
		    if(!provider.contains("gps")){ //if gps is disabled  
		       final Intent poke = new Intent(); 
		       poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");  
		       poke.addCategory(Intent.CATEGORY_ALTERNATIVE); 
		       poke.setData(Uri.parse("3"));  
		       context.sendBroadcast(poke); 
		   } 
		
	}
	/**
	 * 判断GPS是否可用
	 */
	
	// Gps是否可用  
	   public static  boolean isGpsEnable(Activity context) {  
	        LocationManager locationManager =   
	               ((LocationManager) context.getSystemService(Context.LOCATION_SERVICE));  
	        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);  
	   }  

	/**
	 * 进入GPS设置界面
	 */
	
	public static void setOpenGPS(Activity context){
		  Intent intent = new Intent();
	        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        try 
	        {
	           context.startActivity(intent);
	                    
	            
	        } catch(ActivityNotFoundException ex) 
	        {
	            
	            // The Android SDK doc says that the location settings activity
	            // may not be found. In that case show the general settings.
	            
	            // General settings activity
	            intent.setAction(Settings.ACTION_SETTINGS);
	            try {
	                   context.startActivity(intent);
	            } catch (Exception e) {
	            }
	        }

	
	}

}
