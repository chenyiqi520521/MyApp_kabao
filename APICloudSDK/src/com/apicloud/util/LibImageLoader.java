package com.apicloud.util;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;





import com.apicloud.eventdemo.EasDelegate;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;


/***
 * 图片加载器
 * @author zhangqy
 *
 */
public class LibImageLoader {
	
	private ImageLoader imageLoader;
	private static LibImageLoader libImageLoader;
	private DisplayImageOptions mDisplayImageOptions;
		private LibImageLoader(){
		imageLoader = ImageLoader.getInstance();
	}
	
	public static LibImageLoader instance(){
		if(libImageLoader == null){
			synchronized (LibImageLoader.class) {
				if(libImageLoader == null){
					libImageLoader = new LibImageLoader();
				}
			}
		}
		return libImageLoader;
	}
	
	private DisplayImageOptions getDisplayOptions(Drawable loadingImg, Drawable failImg){
		return this.getDisplayOptions(loadingImg, failImg, true);
	}
	
	private DisplayImageOptions getDisplayOptionsNoCache(Drawable loadingImg, Drawable failImg){
		return this.getDisplayOptions(loadingImg, failImg, false);
	}
	
	private DisplayImageOptions getDisplayOptions(Drawable loadingImg, Drawable failImg, boolean isCache){
		mDisplayImageOptions = new DisplayImageOptions.Builder()
		.showImageForEmptyUri(loadingImg)
		.showImageOnFail(failImg)
		.showImageOnLoading(loadingImg)
		.resetViewBeforeLoading(true)
		.cacheOnDisk(isCache)
		.cacheInMemory(true)
		
		.imageScaleType(ImageScaleType.EXACTLY)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.considerExifParams(true)
		.displayer(new SimpleBitmapDisplayer())
		.build();
		return mDisplayImageOptions;
	}
	
	private DisplayImageOptions getDisplayOptions(int loadingImg, int failImg){
		return getDisplayOptions(EasDelegate.getInstance().getResources().getDrawable(loadingImg), 
				EasDelegate .getInstance().getResources().getDrawable(failImg));
	}
	
	private DisplayImageOptions getDisplayOptionsNoCache(int loadingImg, int failImg){
		return getDisplayOptionsNoCache(EasDelegate .getInstance().getResources().getDrawable(loadingImg), 
				EasDelegate .getInstance().getResources().getDrawable(failImg));
	}
	
	private DisplayImageOptions getDisplayOptions(int loadingImg){
		return getDisplayOptions(loadingImg, loadingImg);
	}
	
	private DisplayImageOptions getDisplayOptionsNoCache(int loadingImg){
		return getDisplayOptionsNoCache(loadingImg, loadingImg);
	}
	
	private DisplayImageOptions getDisplayOptions(Drawable loadingImg){
		return getDisplayOptions(loadingImg, loadingImg);
	}
	
	private DisplayImageOptions getDisplayOptionsNoCache(Drawable loadingImg){
		return getDisplayOptionsNoCache(loadingImg, loadingImg);
	}
	
	public void loadImg(String url, ImageView imgView, DisplayImageOptions options, 
			SimpleImageLoadingListener listener, ImageLoadingProgressListener lis){
		imageLoader.displayImage(url, imgView, options, listener, lis);
	}
	
	public void loadImg(String url, ImageView imgView, DisplayImageOptions options, 
			SimpleImageLoadingListener listener){
		imageLoader.displayImage(url, imgView, options, listener);
	}
	
	public void loadImg(String url, ImageView imgView, DisplayImageOptions options){
		imageLoader.displayImage(url, imgView, options, null, new DefaultLoadingLoading());
	}
	
	public void loadImg(String url, ImageView imgView){
		imageLoader.displayImage(url, imgView, null, 
				null, new DefaultLoadingLoading());
	}
	/*public void loadImg(String url, ImageView imgView, int loadingImg){
		imageLoader.displayImage(url, imgView, getDisplayOptions(loadingImg), 
				null, new DefaultLoadingLoading());
	}*/
	public void loadImgNoCache(String url, ImageView imgView, int loadingImg){
		imageLoader.displayImage(url, imgView, getDisplayOptionsNoCache(loadingImg), 
				null, new DefaultLoadingLoading());
	}
	
	public void loadImg(String url, ImageView imgView, Bitmap loadingImg){
		imageLoader.displayImage(url, imgView, getDisplayOptions(new BitmapDrawable(loadingImg)), 
				null, new DefaultLoadingLoading());
	}
	
	public void loadImgNoCache(String url, ImageView imgView, Bitmap loadingImg){
		imageLoader.displayImage(url, imgView, getDisplayOptionsNoCache(new BitmapDrawable(loadingImg)), 
				null, new DefaultLoadingLoading());
	}
	
	public void loadImg(String url, ImageView imgView, Bitmap loadingImg, SimpleImageLoadingListener listener){
		imageLoader.displayImage(url, imgView, getDisplayOptions(new BitmapDrawable(loadingImg)), 
				listener, new DefaultLoadingLoading());
	}
	
	public void loadImgNoCache(String url, ImageView imgView, Bitmap loadingImg, SimpleImageLoadingListener listener){
		imageLoader.displayImage(url, imgView, getDisplayOptionsNoCache(new BitmapDrawable(loadingImg)), 
				listener, new DefaultLoadingLoading());
	}
	
	public void loadImg(String url, ImageView imgView, int loadingImg, SimpleImageLoadingListener listener){
		imageLoader.displayImage(url, imgView, getDisplayOptions(loadingImg), 
				listener, new DefaultLoadingLoading());
	}
	
	public void setPauseStrategy(AbsListView lv, boolean pauseOnScroll, boolean pauseOnFling){
		lv.setOnScrollListener(new PauseOnScrollListener(imageLoader, pauseOnScroll, pauseOnFling));
	}
	
	public void clearCache(){
		clearDiskCache();
		clearMemoryCache();
	}
	
	public void clearDiskCache(){
		imageLoader.clearDiskCache();
	}
	
	public void clearMemoryCache(){
		imageLoader.clearMemoryCache();
	}
	
	private class DefaultLoadingLoading implements ImageLoadingProgressListener{

		@Override
		public void onProgressUpdate(String imageUri, View view, int current,
				int total) {
//			if(total > 0 && current >= total){
//				FadeInBitmapDisplayer.animate((ImageView)view, 300);
//			}
		}
		
	}
	
}
