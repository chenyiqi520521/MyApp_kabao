package com.apicloud.activity.write;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.apicloud.common.Common;
import com.apicloud.controller.HttpPostFile;
import com.apicloud.module.MsgBean;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

/**
 * 
 * ClassName: HandwritingActivity <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON(可选). <br/>
 * date: 2015-4-30 上午9:58:23 <br/>
 * blog:http://blog.csdn.net/qq718799510?viewmode=contents
 * 
 * @author zhuxiaohao
 * @version
 * @since JDK 1.6
 */
public class HandwritingActivity extends Activity implements OnClickListener {
	private Bitmap mSignBitmap;// 签名图片
	private ImageView ivSign;// 图片
	private TextView txSign;
	Bitmap foreground;// 将合成的图片
	LinearLayout line;// 跳转
	// LinearLayout scrollview;// view
	ScrollView scrollview;
	String path = "";
	HttpPostFile httpPostFile;

	/** 以下是需要填充的数据 **/
	TextView txt_sign_merchant_name;// 商户名称
	TextView txt_sign_merchant_no;// 商户编号
	TextView txt_sign_terminal_no;// 终端编号
	TextView txt_sign_operator;// 操作员编号
	TextView txt_sign_crad_number;// 卡号
	TextView txt_sign_iss_no;// 发卡行卡号
	TextView txt_sign_acq_no;// 收单行号
	TextView txt_sign_txn_type;// 交易类型
	TextView txt_sign_exp_date;// 有效期
	TextView txt_sign_batch_no;// 交易批次号
	TextView txt_sign_voucher_no;// 凭证号
	TextView tx_sign_outh;// 授权码
	TextView txt_sign_date;// 日期
	TextView txt_sign_reference;// 参考号
	TextView txt_sign_time;// 时间
	TextView txt_sign_amount;// 金额
	TextView txt_orderno;//订单号
	MsgBean msgBean;
	Handler hanlder;
	ImageView iv_qm;
	ScheduledThreadPoolExecutor exec=new ScheduledThreadPoolExecutor(1); 

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int layoutId = UZResourcesIDFinder.getResLayoutID("signature");
		if (layoutId > 0) {
			setContentView(layoutId);
		}
		httpPostFile = new HttpPostFile(getApplicationContext());
		initView();
		initHandler();
		msgBean = (MsgBean) getIntent().getSerializableExtra("msgBean");
		exec.schedule(new Runnable(){

			@Override
			public void run() {
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						txSign.setVisibility(View.GONE);
						Bitmap second=getBitmapByView(scrollview);
						//合成图片
						updatePath=saveBimap(second);
						File f=new File(updatePath);
						if(f.exists()){
							Toast.makeText(HandwritingActivity.this,"图片大小:"+f.length(),Toast.LENGTH_SHORT).show();
						}
						//上传图片
					    UploadImage(hanlder);
					    //交易成功界面
					    Intent in=new Intent(HandwritingActivity.this,SuccessDialogActivity.class);
						startActivity(in);
						finish();
						
					}
					
				});
				
				
			}
			
		}, 2, TimeUnit.SECONDS);
	}

	void initHandler(){
		hanlder=new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String info=(String) msg.obj;
				Log.v("param",info+"");
				//Toast.makeText(HandwritingActivity.this, info+"",Toast.LENGTH_SHORT).show();
			}
		};//
	}
	/**
	 * 
	 * initView:(初始化). <br/>
	 * 
	 * @author zhuxiaohao
	 * @since JDK 1.6
	 */
	private void initView() {
		scrollview = (ScrollView) findViewById(UZResourcesIDFinder.getResIdID("scrollview"));
		ivSign = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("iv_sign"));
		txSign = (TextView) findViewById(UZResourcesIDFinder.getResIdID("tx_sign"));
		line = (LinearLayout) findViewById(UZResourcesIDFinder.getResIdID("line"));
		txt_sign_merchant_name = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_sign_merchant_name"));
		txt_sign_merchant_no = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_sign_merchant_no"));
		txt_sign_terminal_no = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_sign_terminal_no"));
		txt_sign_operator = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_sign_operator"));
		txt_sign_crad_number = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_sign_crad_number"));
		txt_sign_iss_no = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_sign_iss_no"));
		txt_sign_acq_no = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_sign_acq_no"));
		txt_sign_txn_type = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_sign_txn_type"));
		txt_sign_exp_date = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_sign_exp_date"));
		txt_sign_batch_no = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_sign_batch_no"));
		txt_sign_voucher_no = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_sign_voucher_no"));
		tx_sign_outh = (TextView) findViewById(UZResourcesIDFinder.getResIdID("tx_sign_outh"));
		txt_sign_date = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_sign_date"));
		txt_sign_reference = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_sign_reference"));
		txt_sign_time = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_sign_time"));
		txt_sign_amount = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_sign_amount"));
		txt_orderno=(TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_orderno"));
		iv_qm=(ImageView) findViewById(UZResourcesIDFinder.getResIdID("iv_qm"));
		line.setOnClickListener(this);
		initData();
//		txt_sign_date.setText("2015");
//		txt_sign_time.setText("518");
	}

	/**
	 * 
	 * initData:(填充数据). <br/>
	 * 
	 * @author zhuxiaohao
	 * @since JDK 1.6
	 */
	private void initData() {
		String name=getIntent().getStringExtra("name");
		if(name!=null&&name.length()>0){
			txt_sign_merchant_name.setText(name);
		}
		String merchant=getIntent().getStringExtra("merchant");
		if(merchant!=null&&merchant.length()>0){
			txt_sign_merchant_no.setText(merchant);
		}
		String terminal=getIntent().getStringExtra("terminal");
		if(terminal!=null&&terminal.length()>0){
			txt_sign_terminal_no.setText(terminal);
		}
		
		txt_sign_operator.setText("001");
		String cardNo=getIntent().getStringExtra("cardNo");
		//String cardNo="6222620170004131889";
		if(cardNo!=null&&cardNo.length()>0){
			String before=cardNo.substring(6,cardNo.length()-4);
			String newStr="";
			for(int i=0;i<before.length();i++){
				newStr+="*";
			}
		    cardNo=cardNo.replace(before,newStr);
		    
			txt_sign_crad_number.setText(cardNo);
		}
		
		txt_sign_iss_no.setText("");
		txt_sign_acq_no.setText("");
		txt_sign_txn_type.setText("");
		txt_sign_exp_date.setText("");
		txt_sign_batch_no.setText("");
		txt_sign_voucher_no.setText("");
		tx_sign_outh.setText("");
		String referNo=getIntent().getStringExtra("ReferNO");
		if(referNo!=null&&referNo.length()>0){
			txt_sign_reference.setText(referNo);
		}
		String amount=getIntent().getStringExtra("amount");
		
		if(amount!=null&&amount.length()>0){
			try {
				if(amount.contains(".")){
					String temp=amount.substring(amount.indexOf(".")+1,amount.length());
					if(temp.length()==1){
						amount=amount+"0";
					}else{
						amount=amount.substring(0,amount.indexOf(".")+3);
					}
					
					txt_sign_amount.setText("RMB"+amount);
				}else{
					txt_sign_amount.setText("RMB"+amount+".00");
				}
				/*double am1=Double.parseDouble(amount);
				DecimalFormat df=new DecimalFormat("0.00");
				df.format(am1);*/
				
			} catch (Exception e) {
				// TODO: handle exception
			}
			
		}
		SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		txt_sign_date.setText(formatter1.format(new Date()));
		
		/*String transDate=getIntent().getStringExtra("TransDate");
		if(transDate!=null&&transDate.length()>0){
			txt_sign_date.setText(Common.StrToDateTime(new Date()).toString());
		}*/
	    String time=getIntent().getStringExtra("TransTime");
	    if(time!=null&&time.length()>0){
	    	txt_sign_time.setText(Common.StrToDate(time).toString());
	    }
	    String orderno=getIntent().getStringExtra("orderno");
	    if(orderno!=null&&orderno.length()>0){
	    	txt_orderno.setText(orderno);
	    }
	    
	    String sign_path=getIntent().getStringExtra("sign_path");
		Bitmap first = BitmapFactory.decodeFile(sign_path);
		iv_qm.setImageBitmap(first);
		
	}

	/**
	 * 
	 * TODO 简单描述该方法的实现功能（获取图片宽高）.
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 *      android.content.Intent)
	 */
	@SuppressLint("SdCardPath")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 1:
			if (data != null) {
				path = data.getExtras().getString("url");
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 2;// 图片宽高都为原来的二分之1
				Bitmap abitmap = BitmapFactory.decodeFile(path, options);// 这里得到的事签名字迹图片
				mSignBitmap = abitmap;
				ivSign.setImageBitmap(mSignBitmap);
				txSign.setVisibility(View.GONE);
				ivSign.setDrawingCacheEnabled(true);
				scrollview.setDrawingCacheEnabled(true);

				// path=savePic(getBitmapByView(scrollview));
				path = savePic(getBitmapByView(scrollview));
				doUpdateImage();
			}
			break;
		}
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
				File file = new File(path);
				httpPostFile.postFile(file, getIntent().getStringExtra("uid"), getIntent().getStringExtra("ReferNO"), "http://121.43.231.170/klapi/B2CPay/SignImg",hanlder);
			};
		}.start();
	}

	/**
	 * 
	 * shotBitmap:(保存 view). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param v
	 * @return
	 * @since JDK 1.6
	 */
	private Bitmap shotBitmap(View v) {
		Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		v.draw(canvas);
		return bitmap;
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
	 * 截取scrollview的屏幕
	 * 
	 * @param scrollView
	 * @return
	 */
	public static Bitmap getBitmapByView(ScrollView scrollView) {
		int h = 0;
		Bitmap bitmap = null;
		// 获取scrollview实际高度
		for (int i = 0; i < scrollView.getChildCount(); i++) {
			h += scrollView.getChildAt(i).getHeight();
			scrollView.getChildAt(i).setBackgroundColor(Color.parseColor("#ffffff"));
		}
		// 创建对应大小的bitmap
		bitmap = Bitmap.createBitmap(scrollView.getWidth(), h, Bitmap.Config.RGB_565);
		final Canvas canvas = new Canvas(bitmap);
		scrollView.draw(canvas);
		return bitmap;
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
			saveDir=HandwritingActivity.this.getFilesDir().getAbsolutePath();
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
	@SuppressLint({ "SimpleDateFormat", "SdCardPath" })
	private String getFileName() {
		// String saveDir = Environment.getExternalStorageDirectory() +
		// "/kalai/image";
		String saveDir="";
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			saveDir = "/mnt/sdcard/kalai";
		}else{
			saveDir=HandwritingActivity.this.getFilesDir().getAbsolutePath();
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
	
	String updatePath="";
	@Override
	public void onClick(View v) {
		if (v == line) {
			txSign.setVisibility(View.GONE);
			//path = savePic(getBitmapByView(scrollview));
			Bitmap second=getBitmapByView(scrollview);
			/*String sign_path=getIntent().getStringExtra("sign_path");
			Bitmap first = BitmapFactory.decodeFile(sign_path);
			Bitmap firstb=null;
			if(first!=null){
				firstb=comp(first);
			}*/
			//合成图片
			updatePath=saveBimap(second);
			//上传图片
		    UploadImage(hanlder);
		    //交易成功界面
		    Intent in=new Intent(HandwritingActivity.this,SuccessDialogActivity.class);
			startActivity(in);
			finish();
			
			/*Intent intent = new Intent();
			intent.setClass(getApplicationContext(), WritePadActivity.class);
			intent.putExtra("url", path);
			intent.putExtra("uid", getIntent().getStringExtra("uid"));
			intent.putExtra("ReferNO", getIntent().getStringExtra("ReferNO"));
			startActivity(intent);
			finish();*/
			
		}
	}
	private void UploadImage(final Handler hanlder) {
		new Thread() {
			public void run() {
				File file = new File(updatePath);
				httpPostFile.postFile(file, getIntent().getStringExtra("uid"), getIntent().getStringExtra("ReferNO"), "http://121.43.231.170/klapi2/B2CPay/SignImg",hanlder);
			};
		}.start();
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.onKeyDown(keyCode, event);

	}
}