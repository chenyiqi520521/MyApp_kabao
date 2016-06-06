package com.apicloud.activity;

import java.io.File;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apicloud.activity.topup.TopUpThreeActivity;
import com.apicloud.index.BindCardActivity;
import com.apicloud.index.DataCleanManager;
import com.apicloud.library.CircleImageView;
import com.apicloud.module.LoginBean;
import com.apicloud.module.PersonalBean;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

public class WelcomeIndexActivity extends BasicActivity implements OnClickListener{
	private CircleImageView headImage;
	private static int CAMERA_SUCCESS = 1;
	private static int PHOTO_SUCCESS = 2;
	private static int PHOTO_REQUEST_CROP=3;
	private File tempFile;
	private static String PHOTO_NAME = "temp_photo.jpg";
	private Bitmap bitmap;
	private CircleImageView moneyImage;//充值
	private ImageView welcome_bottom_card;//卡管理
	private RelativeLayout about;//关于我们
	private RelativeLayout cleanCache;
	private ImageView welcome_bottom_user;//身份认证
	private RelativeLayout rl_realTime_account;
	private RelativeLayout rl_bindDevice;
	private CircleImageView cir_welcome_out;//提现
	public static final String TEXT_BIND_BUTTON = "更改绑定设备";
	public static final String WELCOME = "welcomeIndex";
	private TextView tx_welcome_leftmoney;
	private ImageView welcome_bottom_list;
	String key;
	PersonalBean personalBean;
	String balance;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("welcome_index"));
		initView();
	}
	
	public void initView(){
		headImage = (CircleImageView) findViewById(UZResourcesIDFinder.getResIdID("cir_welcome_default"));
		headImage.setOnClickListener(this);
		moneyImage = (CircleImageView) findViewById(UZResourcesIDFinder.getResIdID("cir_welcome_money"));
		moneyImage.setOnClickListener(this);
		welcome_bottom_card = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("welcome_bottom_card"));
		welcome_bottom_card.setOnClickListener(this);
		about = (RelativeLayout) findViewById(UZResourcesIDFinder.getResIdID("rl_aboutUs"));
		about.setOnClickListener(this);
		cleanCache = (RelativeLayout) findViewById(UZResourcesIDFinder.getResIdID("rl_cleanCache"));
		cleanCache.setOnClickListener(this);
		welcome_bottom_user = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("welcome_bottom_user"));
		welcome_bottom_user.setOnClickListener(this);
		rl_realTime_account = (RelativeLayout) findViewById(UZResourcesIDFinder.getResIdID("rl_realTime_account"));
		rl_realTime_account.setOnClickListener(this);
		rl_bindDevice = (RelativeLayout) findViewById(UZResourcesIDFinder.getResIdID("rl_bindDevice"));
		rl_bindDevice.setOnClickListener(this);
		cir_welcome_out = (CircleImageView)findViewById(UZResourcesIDFinder.getResIdID("cir_welcome_out"));
		cir_welcome_out.setOnClickListener(this);
		tx_welcome_leftmoney = (TextView) findViewById(UZResourcesIDFinder.getResIdID("tx_welcome_leftmoney"));
		welcome_bottom_list = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("welcome_bottom_list"));
		welcome_bottom_list.setOnClickListener(this);
		getBalance();
		
	}
	
	private void getBalance(){
		new Thread(){
			@Override
			public void run() {
				
				super.run();
				LoginBean loginBean = controller.doLogin("18602123569", "65727647", "10010001");
				key = loginBean.error;
				personalBean = controller.requestMember(key);
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						 balance = personalBean.balance;
						Log.e("balance------>", "余额是"+balance);
						tx_welcome_leftmoney.setText(balance);
						
					}
				});
				
			}
		}.start();
	}
	@Override
	public void onClick(View v) {
		if(v==headImage){
			AlertDialog.Builder builder = new Builder(WelcomeIndexActivity.this);
			builder.setTitle("请选择获取方式");
			builder.setItems(new String[]{"相册选取","拍照选取"}, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(which==1){
						Intent getPhoto = new Intent("android.media.action.IMAGE_CAPTURE");
						startActivityForResult(getPhoto,CAMERA_SUCCESS);
					}else{
						Intent getCamera = new Intent(Intent.ACTION_GET_CONTENT);
						getCamera.addCategory(Intent.CATEGORY_OPENABLE);
						getCamera.setType("image/*");
						startActivityForResult(getCamera, PHOTO_SUCCESS);
					}
				}
			});
			builder.create();
			builder.show();
		}
		//跳转充值
		if(v==moneyImage){
			Intent intent =new Intent(WelcomeIndexActivity.this,TopUpThreeActivity.class);
			startActivity(intent);
		}
		//跳转提现
		if(v==cir_welcome_out){
			Intent intent = new Intent(WelcomeIndexActivity.this,WithDrawActivity.class);
			intent.putExtra("balance", balance);
			intent.putExtra("key", key);
			startActivity(intent);
		}
		//卡管理
		if(v==welcome_bottom_card){
			Intent intent = new Intent(WelcomeIndexActivity.this,BindCardActivity.class);
			startActivity(intent);
		}
		//关于我们
		if(v==about){
			Intent intent = new Intent(WelcomeIndexActivity.this,AboutUsActivity.class);
			startActivity(intent);
		}
		//清除缓存
		if(v==cleanCache){
			String path = Environment.getExternalStorageDirectory().getAbsolutePath();
			DataCleanManager.cleanApplicationData(getApplicationContext(), path);
			Toast.makeText(getApplicationContext(), "清理缓存成功", Toast.LENGTH_SHORT).show();
		}
		//身份认证
		if(v==welcome_bottom_user){
			Intent intent = new Intent(WelcomeIndexActivity.this,IdConfirmActivity.class);
			startActivity(intent);
		}
		//实时收款
		if(v==rl_realTime_account){
			Intent intent = new Intent(WelcomeIndexActivity.this,RealTimeActivity.class);
			startActivity(intent);
		}
		//绑定设备
		if(v==rl_bindDevice){
			if(WelcomeIndexActivity.this.getSharedPreferences("kalai_save", 0).getString("saved_bind_final", "").isEmpty()){
				Intent intent = new Intent(WelcomeIndexActivity.this,BindEquipmentActivity.class);
				startActivity(intent);
			}else{
				Intent intent = new Intent(WelcomeIndexActivity.this,BindEquipmentFinalActivity.class);
				intent.putExtra("text_bind_button", TEXT_BIND_BUTTON);
				intent.putExtra("fromActivity", WELCOME);
				startActivity(intent);
			}
		}
		//交易记录
		if(v==welcome_bottom_list){
			Intent intent = new Intent(WelcomeIndexActivity.this,TradeDetailActivity.class);
			startActivity(intent);
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==CAMERA_SUCCESS){
			if(hasSdcard()){
			tempFile = new File(Environment.getExternalStorageDirectory(), PHOTO_NAME);
			crop(Uri.fromFile(tempFile));
			}else{
				Toast.makeText(WelcomeIndexActivity.this, "未找到存储卡", Toast.LENGTH_LONG).show();
			}
		}else if(requestCode==PHOTO_SUCCESS){
			if(data!=null){
				Uri uri = data.getData();
				crop(uri);
			}
			
		}else if(requestCode==PHOTO_REQUEST_CROP){
			try {
				bitmap = data.getParcelableExtra("data");
				this.headImage.setImageBitmap(bitmap);
				boolean delete = tempFile.delete();
				Log.v("delete=>>>>>", delete+"");
			} catch (Exception e) {
				System.out.println("裁剪过程中的错误信息是-》》"+e.getMessage());
				e.printStackTrace();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public void crop(Uri uri){
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop","true");
		//裁剪框的比例1:1
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		//输出比例
		intent.putExtra("outputX", 250);
		intent.putExtra("outputY", 250);
		//图片格式
		intent.putExtra("outputFormat", "JPEG");
		intent.putExtra("noFaceDetection", true);//取消人脸识别
		intent.putExtra("return-data",true);//true:不返回uri,false:返回uri
		startActivityForResult(intent, PHOTO_REQUEST_CROP);
	}
	public boolean hasSdcard(){
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			return true;
		}
		return false;
	}

}
