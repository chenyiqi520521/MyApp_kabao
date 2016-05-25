/**
 * Project Name:CardPay
 * File Name:WritePadActivity.java
 * Package Name:com.apicloud.view
 * Date:2015-4-29下午3:29:39
 * Copyright (c) 2015, zhuxiaohao All Rights Reserved.
 *
 */

package com.apicloud.activity.write;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.apicloud.activity.BaseActivity;
import com.apicloud.activity.topup.TopUpTwoActivity;
import com.apicloud.controller.HttpPostFile;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;



/**
 * ClassName:WritePadActivity <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2015-4-29 下午3:29:39 <br/>
 * 
 * @author zhuxiaohao blog:http://blog.csdn.net/qq718799510?viewmode=contents
 * @version 1.0.0
 * @since JDK 1.6
 * @see 签名
 */
public class WritePadActivity extends Activity implements OnClickListener {
	ImageButton ib_return;// 返回
	Button tablet_ok;// 提交
	Button tablet_cancle;//取消前面
	Button tablet_reset;//重签 
	Context context;// 上下文

	PaintView mView;// 画笔
	FrameLayout frameLayout;// 画图所显示的 View

	static final int BACKGROUND_COLOR = Color.WHITE;// 颜色
	static final int BRUSH_COLOR = Color.BLACK;// 颜色
	LayoutParams p;
	String path = "";// 图片地址
	String updatePath;
	HttpPostFile httpPostFile;
	Dialog successDialog=null;
	TextView tv_back=null;
	TextView tv_amount=null;
	private String url;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(UZResourcesIDFinder.getResLayoutID("write_pad"));
		p = getWindow().getAttributes();
		Display mDisplay = ((Activity) context).getWindowManager().getDefaultDisplay();
		p.height = mDisplay.getHeight();
		p.width = mDisplay.getWidth();
		getWindow().setAttributes(p);
		mView = new PaintView(context);
		frameLayout = (FrameLayout) findViewById(UZResourcesIDFinder.getResIdID("tablet_view"));
		frameLayout.addView(mView);
		mView.requestFocus();
		tablet_ok = (Button) findViewById(UZResourcesIDFinder.getResIdID("tablet_ok"));
		tablet_cancle=(Button) findViewById(UZResourcesIDFinder.getResIdID("tablet_cancle"));
		tablet_reset=(Button) findViewById(UZResourcesIDFinder.getResIdID("tablet_reset"));
		ib_return = (ImageButton) findViewById(UZResourcesIDFinder.getResIdID("ib_return"));
		tv_amount=(TextView) findViewById(UZResourcesIDFinder.getResIdID("tv_amount"));
		String amount=getIntent().getStringExtra("amount");
		if(amount!=null&&amount.length()>0){
			tv_amount.setText("您本次消费:"+amount+".00");
		}
		ib_return.setOnClickListener(this);
		tablet_ok.setOnClickListener(this);
		tablet_cancle.setOnClickListener(this);
		tablet_reset.setOnClickListener(this);
		httpPostFile = new HttpPostFile(getApplicationContext());
		//createSuccessDialog();

	}

	
	public static final int WRITING_RESULT_CODE=400;
	@Override
	public void onClick(View v) {
		if (v == ib_return) {
			/*Intent i=new Intent(WritePadActivity.this,HandwritingActivity.class);
			startActivity(i);*/
			finish();
		}
		if(v==tablet_reset){
			if (mView.getCachebBitmap() == null) {
				Toast.makeText(getApplicationContext(), "请签名", Toast.LENGTH_LONG).show();
				return;
			}
			mView.clear();
		}
		if (v == tablet_ok) {
			if (mView.getCachebBitmap() == null) {
				Toast.makeText(getApplicationContext(), "请签名", Toast.LENGTH_LONG).show();
				return;
			}
			Bitmap savedBmp=mView.getCachebBitmap();
			if(savedBmp!=null){
				path = saveBimap(savedBmp);
				
				Intent result=new Intent(WritePadActivity.this,BaseActivity.class);
				result.putExtra("sign_path",path);
				setResult(WRITING_RESULT_CODE, result);
				finish();
			 }else{
				finish();
			}
			
		}
		
		if(v==tablet_cancle){
			
			finish();
		}
	}

	void createSuccessDialog(){
		View v=WritePadActivity.this.getLayoutInflater().inflate(UZResourcesIDFinder.getResLayoutID("dialog_success_tip"), null);
		successDialog=new Dialog(WritePadActivity.this,UZResourcesIDFinder.getResStyleID("my_dialog"));
		successDialog.setCancelable(false);
		successDialog.setContentView(v);
		tv_back=(TextView) successDialog.findViewById(UZResourcesIDFinder.getResIdID("btn_index"));
		tv_back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				successDialog.dismiss();
			    WritePadActivity.this.finish();
				
			}
		});
	    Window dialogWindow = successDialog.getWindow();
	    WindowManager.LayoutParams lp = dialogWindow.getAttributes();
	    dialogWindow.setGravity(Gravity.CENTER);
	    lp.width =400; // 宽度
	    lp.height=500;
	    dialogWindow.setAttributes(lp);
	}
	/**
	 * 
	 * mergeBitmap:(A 图片插入 B 图片). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param firstBitmap
	 * @param secondBitmap
	 * @return
	 * @since JDK 1.6
	 */
	@SuppressWarnings("unused")
	private Bitmap mergeBitmap(Bitmap firstBitmap, Bitmap secondBitmap) {
//		Bitmap bitmap=Bitmap.createScaledBitmap(firstBitmap, firstBitmap.getWidth(), firstBitmap.getHeight(), true).copy(Bitmap.Config.ARGB_4444, true);
		 Bitmap bitmap = Bitmap.createBitmap(secondBitmap.getWidth(), secondBitmap.getHeight(),secondBitmap.getConfig());
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(secondBitmap, new Matrix(), null);
		canvas.drawBitmap(firstBitmap, secondBitmap.getWidth()/2, secondBitmap.getHeight()-250, null);
		return bitmap;
	}

	/**
	 * 
	 * comp:(压缩图片). <br/>
	 * @author zhuxiaohao
	 * @param image
	 * @return
	 * @since JDK 1.6
	 */
	private Bitmap comp(Bitmap image) {  
		 Bitmap bitmap=null;
	    ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
	    if(baos!=null){
	    	image.compress(Bitmap.CompressFormat.JPEG, 100, baos);  
		    if( baos.toByteArray().length / 1024>1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出    
		        baos.reset();//重置baos即清空baos  
		        image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中  
		    }  
		    ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());  
		    BitmapFactory.Options newOpts = new BitmapFactory.Options();  
		    //开始读入图片，此时把options.inJustDecodeBounds 设回true了  
		    newOpts.inJustDecodeBounds = true;  
		    bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);  
		    newOpts.inJustDecodeBounds = false;  
		    int w = newOpts.outWidth;  
		    int h = newOpts.outHeight;  
		    //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为  
		    float hh = 100f;//这里设置高度为800f  
		    float ww = 240f;//这里设置宽度为480f  
		    //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可  
		    int be = 1;//be=1表示不缩放  
		    if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放  
		        be = (int) (newOpts.outWidth / ww);  
		    } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放  
		        be = (int) (newOpts.outHeight / hh);  
		    }  
		    if (be <= 0)  
		        be = 1;  
		    newOpts.inSampleSize = be;//设置缩放比例  
		    //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了  
		    isBm = new ByteArrayInputStream(baos.toByteArray());  
		    bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
	    }
	     
	   Bitmap result=null;
	   if(bitmap!=null){
		   result=compressImage(bitmap);
	   }
	    return result;//压缩好比例大小后再进行质量压缩  
	}  
	/**
	 * 将两张位图拼接成一张(横向拼接)
	 * 
	 * @param first
	 * @param second
	 * @return
	 */
	private Bitmap add2Bitmap(Bitmap first, Bitmap second) {
		// int width = first.getWidth() + second.getWidth();
		int width = second.getWidth();
		// int height = Math.max(first.getHeight(), second.getHeight());
		int height = first.getHeight() + second.getHeight();
		Bitmap result = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		canvas.drawBitmap(first, 0, second.getHeight(), null);
		canvas.drawBitmap(second, 0, 0, null);
		return result;
	}

	/**
	 * 
	 * toConformBitmap:(合并图片). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param background
	 * @param foreground
	 * @return
	 * @since JDK 1.6
	 */
	public static Bitmap toConformBitmap(Bitmap background, Bitmap foreground) {
		if (background == null) {
			return null;
		}
		int bgWidth = background.getWidth();
		int bgHeight = background.getHeight();
		int fgWidth = foreground.getWidth();
		int fgHeight = foreground.getHeight();
		// create the new blank bitmap 创建一个新的和SRC长度宽度一样的位图
		Bitmap newbmp = Bitmap.createBitmap(bgWidth, bgHeight + fgHeight, Config.ARGB_8888);
		Canvas cv = new Canvas(newbmp);
		// draw bg into
		cv.drawBitmap(background, 0, 0, null);// 在 0，0坐标开始画入bg
		// draw fg into
		cv.drawBitmap(foreground, bgWidth, 0, null);// 在 0，0坐标开始画入fg ，可以从任意位置画入
		// save all clip
		cv.save(Canvas.ALL_SAVE_FLAG);// 保存
		// store
		cv.restore();// 存储
		return newbmp;
	}

	/**
	 * 压缩图片
	 * 
	 * @param image
	 * @return
	 */
	public static Bitmap compressImage(Bitmap image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		int options = 100;
		// 循环判断如果压缩后图片是否大于100kb,大于继续压缩
		while (baos.toByteArray().length / 1024 > 100) {
			// 重置baos
			baos.reset();
			// 这里压缩options%，把压缩后的数据存放到baos中
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);
			// 每次都减少10
			options -= 10;
		}
		// 把压缩后的数据baos存放到ByteArrayInputStream中
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
		// 把ByteArrayInputStream数据生成图片
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
		return bitmap;
	}
	
	/**
	 * 保存到sdcard
	 * 
	 * @param b
	 * @return
	 */
	@SuppressLint("SdCardPath")
	public  String savePic(Bitmap b) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
		
		String saveDir="";
		if(Environment.getExternalStorageState()==Environment.MEDIA_MOUNTED){
			saveDir = "/mnt/sdcard/kalai";
		}else{
			saveDir=WritePadActivity.this.getFilesDir().getAbsolutePath();
			//Toast.makeText(WritePadActivity.this, "请插入SD卡,内存不足", Toast.LENGTH_SHORT).show();
			
		}
		File outfile = new File(saveDir);
		// 如果文件不存在，则创建一个新文件
		if (!outfile.isDirectory()) {
			try {
				outfile.mkdir();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String fname = outfile + "/" + sdf.format(new Date()) + ".png";
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(fname);
			if (null != fos) {
				b.compress(Bitmap.CompressFormat.PNG, 90, fos);
				fos.flush();
				fos.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fname;
	}
	/**
	 * 
	 * setIntent:(返回传参). <br/>
	 * 
	 * @author zhuxiaohao
	 * @since JDK 1.6
	 */
	private void setIntent() {
		Intent intent = new Intent();
		intent.putExtra("url", path);
		setResult(1, intent);
	}

	
	/**
	 * 
	 * saveBimap:(图片保存指定路径). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param bitmap
	 * @return
	 * @since JDK 1.6
	 */
	private String saveBimap(Bitmap bitmap) {
		FileOutputStream file = null;
		String filename = null;
		try {
			filename = getFileName();
			file = new FileOutputStream(filename);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if(file!=null){
					file.flush();
					file.close();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//System.out.println("file" + filename);
		return filename;

	}

	/**
	 * 
	 * getFileName:(生成文件路径和文件名). <br/>
	 * 
	 * @author zhuxiaohao
	 * @return
	 * @since JDK 1.6
	 */
	@SuppressLint({ "SimpleDateFormat", "SdCardPath" })
	private String getFileName() {
		// String saveDir = Environment.getExternalStorageDirectory() +
		// "/kalai/image";
		String saveDir="";
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			saveDir = "/mnt/sdcard/kalai";
		}else{
			saveDir=WritePadActivity.this.getFilesDir().getAbsolutePath();
			//Toast.makeText(WritePadActivity.this, "请插入SD卡,内存不足", Toast.LENGTH_SHORT).show();
			
		}
		
		File dir = new File(saveDir);
		if (!dir.exists()) {
			dir.mkdir(); // 创建文件夹
		}
		// 用日期作为文件名，确保唯一性
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String fileName = saveDir + "/" + formatter.format(date) + ".jpg";
		return fileName;
	}

	/**
	 * 
	 * doUpdateImage:(上传图片). <br/>
	 * 
	 * @author zhuxiaohao
	 * @since JDK 1.6
	 */
	private void doUpdateImage() {
		new Thread() {
			public void run() {
				File file = new File(updatePath);
				//httpPostFile.postFile(file, getIntent().getStringExtra("uid"), getIntent().getStringExtra("ReferNO"), "http://121.40.107.136/kabao/B2CPay/SignImg");
			};
		}.start();
	}

	/**
	 * 
	 * saveToSD:(保存当前截图后的图片). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param bmp
	 * @param dirName
	 * @param fileName
	 * @throws IOException
	 * @since JDK 1.6
	 */
	private String saveToSD(Bitmap bmp, String dirName, String fileName) throws IOException {
		String path = "";
		// 判断sd卡是否存在
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File dir = new File(dirName);
			// 判断文件夹是否存在，不存在则创建
			if (!dir.exists()) {
				dir.mkdir();
			}

			File file = new File(dirName + fileName);
			// 判断文件是否存在，不存在则创建
			if (!file.exists()) {
				file.createNewFile();
			}
			path = dirName + fileName;
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file);
				if (fos != null) {
					// 第一参数是图片格式，第二个是图片质量，第三个是输出流
					bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
					// 用完关闭
					fos.flush();
					fos.close();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return path;
	}

	/**
	 * 这个视图实现绘图画布。
	 * 
	 * 它处理所有的输入事件和绘图功能。
	 */
	class PaintView extends View {
		private Paint paint;
		private Canvas cacheCanvas;
		private Bitmap cachebBitmap;
		private Path path;
		private boolean isboolean = false;

		public Bitmap getCachebBitmap() {
			if (isboolean == false) {
				return null;
			} else {
				return cachebBitmap;
			}
		}

		public Canvas getCanvas() {
			return cacheCanvas;
		}

		public PaintView(Context context) {
			super(context);
			init();
		}

		private void init() {
			paint = new Paint();
			paint.setAntiAlias(true);
			paint.setStrokeWidth(3);
			paint.setStyle(Paint.Style.STROKE);
			paint.setColor(Color.BLACK);
			paint.setFakeBoldText(true);
			path = new Path();
			cachebBitmap = Bitmap.createBitmap(p.width, (int) (p.height * 0.8), Config.ARGB_8888);
			cacheCanvas = new Canvas(cachebBitmap);
			cacheCanvas.drawColor(Color.WHITE);
		}

		public void clear() {
			if (cacheCanvas != null) {
				paint.setColor(BACKGROUND_COLOR);
				cacheCanvas.drawPaint(paint);
				paint.setColor(Color.BLACK);
				cacheCanvas.drawColor(Color.WHITE);
				invalidate();
			}
		}

		@Override
		protected void onDraw(Canvas canvas) {
			// canvas.drawColor(BRUSH_COLOR);
			canvas.drawBitmap(cachebBitmap, 0, 0, null);
			canvas.drawPath(path, paint);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			int curW = cachebBitmap != null ? cachebBitmap.getWidth() : 0;
			int curH = cachebBitmap != null ? cachebBitmap.getHeight() : 0;
			if (curW >= w && curH >= h) {
				return;
			}
			if (curW < w)
				curW = w;
			if (curH < h)
				curH = h;
			Bitmap newBitmap = Bitmap.createBitmap(curW, curH, Bitmap.Config.ARGB_8888);
			Canvas newCanvas = new Canvas();
			newCanvas.setBitmap(newBitmap);
			if (cachebBitmap != null) {
				newCanvas.drawBitmap(cachebBitmap, 0, 0, null);
			}
			cachebBitmap = newBitmap;
			cacheCanvas = newCanvas;
		}

		private float cur_x, cur_y;

		@SuppressLint("ClickableViewAccessibility")
		@Override
		public boolean onTouchEvent(MotionEvent event) {

			float x = event.getX();
			float y = event.getY();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				cur_x = x;
				cur_y = y;
				path.moveTo(cur_x, cur_y);
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				path.quadTo(cur_x, cur_y, x, y);
				cur_x = x;
				cur_y = y;
				break;
			}
			case MotionEvent.ACTION_UP: {
				cacheCanvas.drawPath(path, paint);
				path.reset();
				isboolean = true;
				break;
			}
			}
			invalidate();
			return true;
		}
	}

}
