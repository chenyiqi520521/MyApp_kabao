package com.apicloud.eventdemo;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.apicloud.util.commonUtil;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.uzmap.pkg.uzcore.uzmodule.ApplicationDelegate;



public class EasDelegate extends ApplicationDelegate {

	/**
	 * 继承自ApplicationDelegate的类，APICloud引擎在应用初始化之初就会将该类初始化一次（即new一个出来），并保持引用，
	 * 在应用整个运行期间，会将生命周期事件通过该对象通知出来。<br>
	 * 该类需要在module.json中配置，其中name字段可以任意配置，因为该字段将被忽略，请参考module.
	 * json中EasDelegate的配置
	 */
	private static Context instance;
	public static Handler handler;
	public EasDelegate() {
		Log.d("APICloud", "EasDelegate : instance");
		// 应用运行期间，该对象只会初始化一个出来
	}
	public static Context getInstance() {
		return instance;
	}
	@Override
	public void onApplicationCreate(Context context) {
		Log.d("APICloud", "EasDelegate : onApplicationCreate");
		// 请在这个函数中初始化模块中需要在ApplicationCreate中初始化的东西
		instance=context;
		initImageLoader(context);
        initHandler();
	}

	void initHandler(){
		handler=new Handler(){
			/* (non Javadoc) 
			 * @Title: handleMessage
			 * @Description: TODO
			 * @param msg 
			 * @see android.os.Handler#handleMessage(android.os.Message) 
			 */
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
			}
		};
	}
	/**
	 * 异步下载初始化 initImageLoader:(这里用一句话描述这个方法的作用). <br/>
	 * 
	 * @author chenhao
	 * @param context
	 * @since JDK 1.6
	 */
	public void initImageLoader(Context context) {
		String cathPath=Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"kalai1";
		DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true)
				.cacheOnDisk(true)
				.imageScaleType(ImageScaleType.EXACTLY)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.build();
		Point size = commonUtil.getScreenSize(context);
        File cacheDir = new File(cathPath);
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
				.diskCacheSize(200 * 1024 * 1024)
				.diskCacheFileCount(500)
				.memoryCache(new LruMemoryCache(1024 * 1024))
				.memoryCacheSize(1 * 1024 * 1024)
				.diskCache(new UnlimitedDiscCache(cacheDir))
				.imageDownloader(new BaseImageDownloader(context)) // default  
                .imageDecoder(new BaseImageDecoder(false)) // default  
                .defaultDisplayImageOptions(imageOptions) // default  
				//.writeDebugLogs()
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}

	@Override
	public void onActivityResume(Activity activity) {
		Log.d("APICloud", "EasDelegate : onActivityResume");
	}

	@Override
	public void onActivityPause(Activity activity) {
		Log.d("APICloud", "EasDelegate : onActivityPause");
	}

	@Override
	public void onActivityFinish(Activity activity) {
		Log.d("APICloud", "EasDelegate : onActivityFinish");
		
	}
	
	
 

}
