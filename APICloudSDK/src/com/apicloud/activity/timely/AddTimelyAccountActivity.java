/**
 * Project Name:CardPay
 * File Name:TimelyAccountActivity.java
 * Package Name:com.apicloud.activity
 * Date:2015-4-23下午7:03:05
 * Copyright (c) 2015, zhuxiaohao All Rights Reserved.
 *
 */

package com.apicloud.activity.timely;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apicloud.activity.BaseActivity;
import com.apicloud.activity.TradnsferAccountsActivity;
import com.apicloud.activity.card.AddCardPaymentsActivity;
import com.apicloud.activity.card.CradPayActivity;
import com.apicloud.activity.telphone.TelPhoneActivity;
import com.apicloud.activity.topup.NotCardHeadDialog;
import com.apicloud.common.Common;
import com.apicloud.common.Image;
import com.apicloud.controller.Controller;
import com.apicloud.controller.DeviceController;
import com.apicloud.controller.HttpPostFile;
import com.apicloud.controller.TransferListener;
import com.apicloud.impl.DeviceControllerImpl;
import com.apicloud.landy.LandyTackMsg;
import com.apicloud.module.BankCrad;
import com.apicloud.module.CreditCardBean;
import com.apicloud.module.DeviceInfoString;
import com.apicloud.module.MsgPush;
import com.apicloud.module.MsgValidation;
import com.apicloud.moduleDemo.APIModuleDemo;
import com.apicloud.swip.SwipActivity;
import com.apicloud.swip.SwipApi;
import com.apicloud.util.Configure;
import com.apicloud.util.Constant;
import com.apicloud.util.UICommon;
import com.apicloud.view.LoadingDialog;
import com.newland.me.DeviceManager.DeviceConnState;
import com.newland.mtype.BatteryInfoResult;
import com.newland.mtype.ConnectionCloseEvent;
import com.newland.mtype.DeviceInfo;
import com.newland.mtype.ModuleType;
import com.newland.mtype.conn.DeviceConnParams;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.emv.EmvTransController;
import com.newland.mtype.module.common.emv.EmvTransInfo;
import com.newland.mtype.module.common.emv.SecondIssuanceRequest;
import com.newland.mtype.module.common.swiper.SwipResult;
import com.newland.mtype.module.external.me11.ME11SwipResult;
import com.newland.mtype.util.Dump;
import com.newland.mtypex.audioport.AudioPortV100ConnParams;
import com.umeng.analytics.MobclickAgent;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

/**
 * ClassName:TimelyAccountActivity <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2015-4-23 下午7:03:05 <br/>
 * 
 * @author zhuxiaohao blog:http://blog.csdn.net/qq718799510?viewmode=contents
 * @version 1.0.0
 * @since JDK 1.6
 * @see 即时到帐
 */
public class AddTimelyAccountActivity extends BaseActivity implements OnClickListener {
	/** 返回 */
	ImageButton ib_return;
	/** 银行卡号 */
	EditText ed_crad;
	/** 获取银行卡号 */
	TextView txt_crad_get;
	/** 持卡人姓名 */
	EditText ed_crad_name;
	/** 该卡所属银行 */
	TextView bank_crad;
	/** 身份证正面照片 */
	LinearLayout line_one;
	/** 第一个是+号,需要隐藏.第二个是获取到拍照或者选择相册的图片 */
	ImageView iv_one, iv_one_s;
	/** 身份证反面照片 */
	LinearLayout line_two;
	/** 第一个是+号,需要隐藏.第二个是获取到拍照或者选择相册的图片 */
	ImageView iv_two, iv_two_s;
	/** 身份证反面照片 */
	LinearLayout line_three;
	/** 第一个是+号,需要隐藏.第二个是获取到拍照或者选择相册的图片 */
	ImageView iv_three, iv_three_s;
	/** 身份证反面照片 */
	LinearLayout line_four;
	/** 第一个是+号,需要隐藏.第二个是获取到拍照或者选择相册的图片 */
	ImageView iv_four, iv_four_s;
	/** 提交 */
	Button btn_ok;

	/** TAG */
	String TAG = AddTimelyAccountActivity.class.getName();
	/** 时候进行中 */
	boolean processing = false;
	/** 卡头控制器 */
	// DeviceController controller = DeviceControllerImpl.getInstance();
	/** 关闭 */
	boolean closed = false;
	/** 如果出现不支持卡状态 */
	int isStand = 0;
	/** 如果取消 */
	int cancel = 2;
	/** 如果连接失败 */
	int connect = 4;
	/** 刷卡状态 */
	int swiper = 3;
	public Handler iHandler, sonHandler;
	public Handler resultHandler;
	/** 金额 */
	String transAmt = "0.00";
	/** 银行卡刷出信息 */
	BankCrad bankCrad;
	/** 这个又称 KSN */
	String csn;
	/** 设备信息 */
	DeviceInfo deviceInfo;
	/** 设备数据封装 */
	DeviceInfoString deviceInfoString;
	/** 密码 */
	String pin = "";

	/** 拍照对话框 */
	CameraDialog dialog;
	/** 拍照 */
	static final int PICTURES = 1;
	/** 相册 */
	static final int ALBUM = 2;

	/** 第一张图片 */
	int image_one = 0x0003;
	/** 第一张图片 */
	int image_two = 0x0004;
	/** 第3张图片 */
	int image_three = 0x0005;
	/** 第4张图片 */
	int image_four = 0x0006;
	/** 类型 */
	int type = -1;
	/** 图片 */
	Map<Integer, String> maplist = new HashMap<Integer, String>();
	/** 拍照的图片路径 */
	private String filePath;
	Controller controller2;// 控制器
	List<Image> list = null;// 数据
	// NotCardHeadDialog cardHeadDialog;// 检测卡头的对话框
	HttpPostFile httpPostFile;// 上传文件夹
	boolean Stuats;
	File file = null;// 上传图片
	MsgValidation msgValidation;// 银行名
	MsgPush state;// 返回对象
	int isboolean = 0;
	ProgressDialog pd;
	ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);

	/**
	 * 新加的控件
	 */
	Button btn_getsms;
	EditText et_ed_sms_num, ed_id_card_num, ed_phone_num;
	boolean cardNochecked = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("timely_account"));
		// operation_stay(Common.FETCH_DEVICE_INFO);
		addMessageHandler();// 添加初始化一系列的事件
		initHanlder();
		initView();
		if (CHOSE_DEVICE.equals("A")) {
			initMe3xDeviceController(new AudioPortV100ConnParams());// 初始化设备
		}
		dialog = new CameraDialog(AddTimelyAccountActivity.this);
		dialog.setOnclickListener(dialogOnClickListener);
		// cardHeadDialog = new
		// NotCardHeadDialog(AddTimelyAccountActivity.this);
		// cardHeadDialog.show();
		controller2 = new Controller(AddTimelyAccountActivity.this);
		httpPostFile = new HttpPostFile(getApplicationContext());
		initHanlder();
		initSonHandler();
		try {
			startLocation();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	void initSonHandler() {
		sonHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String content = (String) msg.obj;
				// Tip(content+"");
				if (content != null && content.equals("sure") && CHOSE_DEVICE.equals("B")) {// 点击确定
					ed_crad.setText(bankCradParent.account.replaceAll(".{4}(?!$)", "$0 "));

				}
				if (content != null && content.equals("sure") && CHOSE_DEVICE.equals("C")) {// 点击确定
					ed_crad.setText(bankCradParent.account.replaceAll(".{4}(?!$)", "$0 "));

				}
				if (CHOSE_DEVICE.equals("B") || CHOSE_DEVICE.equals("D")) {
					try {
						int type = msg.what;
						operation_stay(type);
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			}
		};
		setParentHandler(sonHandler);
	}

	/**
	 * 
	 * initView:(初始化). <br/>
	 * 
	 * @author zhuxiaohao
	 * @since JDK 1.6
	 */
	@SuppressLint("NewApi")
	private void initView() {
		ib_return = (ImageButton) findViewById(UZResourcesIDFinder.getResIdID("ib_return"));
		ed_crad = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_crad"));
		txt_crad_get = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_crad_get"));
		setGetCardNumTv(txt_crad_get);
		ed_crad_name = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_crad_name"));

		line_one = (LinearLayout) findViewById(UZResourcesIDFinder.getResIdID("line_one"));
		iv_one = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("iv_one"));
		iv_one_s = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("iv_one_s"));

		line_two = (LinearLayout) findViewById(UZResourcesIDFinder.getResIdID("line_two"));
		iv_two = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("iv_two"));
		iv_two_s = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("iv_two_s"));

		line_three = (LinearLayout) findViewById(UZResourcesIDFinder.getResIdID("line_three"));
		iv_three = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("iv_three"));
		iv_three_s = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("iv_three_s"));

		line_four = (LinearLayout) findViewById(UZResourcesIDFinder.getResIdID("line_four"));
		iv_four = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("iv_four"));
		iv_four_s = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("iv_four_s"));
		bank_crad = (TextView) findViewById(UZResourcesIDFinder.getResIdID("bank_crad"));
		btn_ok = (Button) findViewById(UZResourcesIDFinder.getResIdID("btn_ok"));
		btn_getsms = (Button) findViewById(UZResourcesIDFinder.getResIdID("btn_getsms"));
		btn_getsms.setOnClickListener(this);
		et_ed_sms_num = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_sms_num"));
		ed_id_card_num = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_id_card_num"));
		ed_phone_num = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_phone_num"));

		btn_ok.setOnClickListener(this);
		// 判断是卡头还是蓝牙
		String chose_device = getIntent().getStringExtra("posno");
		if (chose_device != null && chose_device.length() > 0) {
			CHOSE_DEVICE = chose_device;

		}
		if (CHOSE_DEVICE.equals("A")) {
			txt_crad_get.setOnClickListener(this);
		}

		ib_return.setOnClickListener(this);
		line_one.setOnClickListener(this);
		line_two.setOnClickListener(this);
		line_three.setOnClickListener(this);
		line_four.setOnClickListener(this);
		String cardid = getIntent().getStringExtra("cardid");
		// if (cardid != "" || cardid != null) {
		// handleText();
		// }
		ed_crad.addTextChangedListener(textWatcher);
		initParentView();
		initHeadDialog();
		initSwipParam();

	}
	void initSwipParam(){
		fromAct=UICommon.AddTimelyAccountActivity;
		whatDo=SwipApi.WHATDO_GET_CARDNO;
		cur_Ac=this;
	}
	public void setCardNo(Object obj){
		//如果选择的是联迪设备
		if(CHOSE_DEVICE.equals(UICommon.WFT_DEVICE)||CHOSE_DEVICE.equals(UICommon.LANDY_DEVICE)||CHOSE_DEVICE.equals(UICommon.AF_DEVICE)||CHOSE_DEVICE.equals(UICommon.CFT_DEVICE)||CHOSE_DEVICE.equals(UICommon.BBPOS_IC_DEVICE)){
			LandyTackMsg landybean=(LandyTackMsg)obj;
			//Tip(landybean.cardNo+"");
			handleBack(landybean.cardNo+"");
		}
	 	
	}

	void initHanlder() {
		resultHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
			}

		};
	}

	int count = 60;
	boolean stop = true;// 停止计时
	Runnable timer = new Runnable() {

		@Override
		public void run() {

			if (count != 0) {

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						stop = false;
						// Tip(count+"");
						btn_getsms.setText(count + "");
						count--;
						iHandler.postDelayed(timer, 1000);
					}

				});
			} else {
				btn_getsms.setClickable(true);
				btn_getsms.setText("获取验证码");
				stop = true;

				count = 60;
			}

		}
	};
	/**
	 * 
	 * TODO 简单描述该方法的实现功能（点击事件）.
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	private String result_smsm_code = "-1";

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// 返回
		if (v == ib_return) {
			final boolean needResult = getIntent().getBooleanExtra("needResult", false);
			if (needResult) {
				Intent resultData = new Intent();
				JSONObject json = new JSONObject();
				try {
					json.put("callback", 0);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				resultData.putExtra("callback", json.toString());
				setResult(RESULT_OK, resultData);
			}
			;
			finish();
		}

		if (v == btn_getsms) {// 短信获取验证码
			if (Controller.isEmpty(ed_phone_num.getText().toString().trim())) {
				Toast.makeText(getApplicationContext(), "请先填写手机号", Toast.LENGTH_LONG).show();
				return;
			}
			if (controller2.checkNetworkAvailable(AddTimelyAccountActivity.this)) {

				if (stop) {
					btn_getsms.setClickable(false);
					new Thread() {
						public void run() {
							// 开启倒计时
							// iHandler.pos
							result_smsm_code = controller2.getSms(AddTimelyAccountActivity.this, ed_phone_num.getText().toString().trim(), getIntent().getStringExtra("uid"), iHandler);
						};
					}.start();
				}

				iHandler.post(timer);
			} else {
				Toast.makeText(AddTimelyAccountActivity.this, "请检查网络", Toast.LENGTH_SHORT).show();
			}

		}
		// 提交
		if (v == btn_ok) {

			if (Controller.isEmpty(ed_crad.getText().toString().trim())) {

				Toast.makeText(getApplicationContext(), "卡号不能为空", Toast.LENGTH_LONG).show();
				return;
			}
			if (Controller.isEmpty(ed_crad_name.getText().toString().trim())) {

				Toast.makeText(getApplicationContext(), "持卡人姓名不能为空", Toast.LENGTH_LONG).show();
				return;
			}
			if (Controller.isEmpty(ed_id_card_num.getText().toString().trim())) {

				Toast.makeText(getApplicationContext(), "身份证号不能为空", Toast.LENGTH_LONG).show();
				return;
			}
			if (Controller.isEmpty(ed_phone_num.getText().toString().trim())) {

				Toast.makeText(getApplicationContext(), "手机号不能为空", Toast.LENGTH_LONG).show();
				return;
			}
			if (Controller.isEmpty(et_ed_sms_num.getText().toString().trim())) {

				Toast.makeText(getApplicationContext(), "验证码不能为空", Toast.LENGTH_LONG).show();
				return;
			}

			String sms_code = result_smsm_code + "";
			if (!et_ed_sms_num.getText().toString().trim().equals(sms_code)) {
				Toast.makeText(getApplicationContext(), "输入的验证码不正确！", Toast.LENGTH_LONG).show();
				return;
			}
			/*
			 * if (maplist.size() < 4) { isboolean = 0;
			 * Toast.makeText(getApplicationContext(), "图片不能少于4张,必须填满.",
			 * Toast.LENGTH_LONG).show(); return; }
			 */

			if (controller2.checkNetworkAvailable(AddTimelyAccountActivity.this)) {
				if (!cardNochecked) {
					final String cardNo = ed_crad.getText().toString().trim().replace(" ", "");
					new Thread() {
						@Override
						public void run() {
							super.run();
							msgValidation = controller2.isValidation(cardNo);
							if (msgValidation != null) {
								handCradName(msgValidation, true);
							} else {
								Tip("请检查网络");
							}

						}
					}.start();
				} else {
					doOk();
				}

			} else {
				Toast.makeText(getApplicationContext(), "请检查网络", Toast.LENGTH_LONG).show();
			}

		}

		// 获取卡号
		if (v == txt_crad_get) {
			// cardHeadDialog = new
			// NotCardHeadDialog(AddTimelyAccountActivity.this);
			cardHeadDialog.show();
			operation_stay(Common.FETCH_DEVICE_INFO);

		}
		// 点击第一个选择拍照
		if (v == line_one) {
			dialog.show();
			type = image_one;
		}
		// 点击第2个选择拍照
		if (v == line_two) {
			dialog.show();
			type = image_two;
		}
		// 点击第3个选择拍照
		if (v == line_three) {
			dialog.show();
			type = image_three;
		}
		// 点击第4个选择拍照
		if (v == line_four) {
			dialog.show();
			type = image_four;
		}
	}

	/**
	 * 
	 * doOk:(添加信用卡认证). <br/>
	 * 
	 * @author zhuxiaohao
	 * @since JDK 1.6
	 */
	private void doOk() {
		if (!Common.checkNetWork(getApplicationContext())) {
			Toast.makeText(getApplicationContext(), "当前没有网络", Toast.LENGTH_LONG).show();
			return;
		}
		pd = new LoadingDialog(AddTimelyAccountActivity.this);
		pd.setCancelable(true);// 设置进度条是否可以按退回键取消
		pd.setCanceledOnTouchOutside(false);// 设置点击进度对话框外的区域对话框不消失
		pd.show();
		new Thread() {
			public void run() {
				String cardno = ed_crad.getText().toString().trim();
				String person = ed_crad_name.getText().toString().trim();
				String idnum = ed_id_card_num.getText().toString().trim();
				String phoneNum = ed_phone_num.getText().toString().trim();
				String smsNum = et_ed_sms_num.getText().toString().trim();
				state = controller2.pushCard(AddTimelyAccountActivity.this, cardno, person, Controller.CREDIT_CARD, getIntent().getStringExtra("uid"), idnum, phoneNum, smsNum, null, resultHandler);
				// state =
				// controller2.pushCard(AddTimelyAccountActivity.this,ed_crad.getText().toString().trim(),
				// ed_crad_name.getText().toString().trim(),
				// bank_crad.getText().toString().trim(), 1 + "",
				// getIntent().getStringExtra("uid"),
				// getIntent().getStringExtra("cardid"));
				if (state.error.contains("0")) {
					pd.dismiss();
					controller2.tip(AddTimelyAccountActivity.this, "认证成功", iHandler);
					final boolean needResult = getIntent().getBooleanExtra("needResult", false);
					if (needResult) {
						Intent resultData = new Intent();
						JSONObject json = new JSONObject();
						try {
							json.put("callback", 1);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						resultData.putExtra("callback", json.toString());
						setResult(RESULT_OK, resultData);
					}
					Constant.isNeedRefreshCardInfo = true;
					finish();
				} else {
					pd.dismiss();
					controller2.tip(AddTimelyAccountActivity.this, "认证失败" + state.error+"--"+state.RspMsg, iHandler);
				}

			};
		}.start();

	}

	private void uiTherad() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Toast.makeText(AddTimelyAccountActivity.this, "该卡已存在", Toast.LENGTH_SHORT).show();
				pd.dismiss();
			}
		});
	}

	/**
	 * 
	 * statusStr:(如果添加失败). <br/>
	 * 
	 * @author zhuxiaohao
	 * @since JDK 1.6
	 */
	private void statusStr() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), "添加失败", Toast.LENGTH_LONG).show();
				isboolean = 0;
				pd.dismiss();
			}
		});
	}

	/**
	 * 
	 * doUpadateImage:(异步上传图片). <br/>
	 * 
	 * @author zhuxiaohao
	 * @since JDK 1.6
	 */
	private void doUpadateImage() {
		UpdateTextTask updateTextTask = new UpdateTextTask(this);
		updateTextTask.execute();
	}

	/**
	 * 对话框点击事件
	 */
	private OnClickListener dialogOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = null;
			// 相册选取
			if (v == dialog.text_album) {
				// 选择照片的时候也一样，我们用Action为Intent.ACTION_GET_CONTENT，有些人使用其他的Action但我发现在有些机子中会出问题，所以优先选择这个
				intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(intent, 2);
			}
			// 拍照
			if (v == dialog.text_pictures) {
				// 拍照我们用Action为MediaStore.ACTION_IMAGE_CAPTURE，有些人使用其他的Action但我发现在有些机子中会出问题，所以优先选择这个
				String state = Environment.getExternalStorageState(); // 判断是否存在sd卡
				if (state.equals(Environment.MEDIA_MOUNTED)) { // 直接调用系统的照相机
					intent = new Intent("android.media.action.IMAGE_CAPTURE");
					filePath = getFileName();
					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(filePath)));
					startActivityForResult(intent, 1);
				} else {
					Toast.makeText(AddTimelyAccountActivity.this, "请检查手机是否有SD卡!", Toast.LENGTH_LONG).show();
				}
			}
			if (v == dialog.txt_share__cancel) {
				dialog.dismiss();
			}

		}
	};

	/**
	 * 
	 * TODO 简单描述该方法的实现功能（处理拍照之后图片）.
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 *      android.content.Intent)
	 */
	@SuppressWarnings("unused")
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 选择完或者拍完照后会在这里处理，然后我们继续使用setResult返回Intent以便可以传递数据和调用
		// if (data.getExtras() != null && data.getData() != null) {
		Log.v("landy1", "req1-->"+requestCode+"---result->"+resultCode);
		if(requestCode==SWIP_REQUEST&&resultCode==SwipActivity.RESPONSE_GET_CARD_NO_SUCESS){
			LandyTackMsg lanyTrackMsg=(LandyTackMsg) data.getSerializableExtra("data");
			setCardNo(lanyTrackMsg);
		}
		
		if (resultCode != Activity.RESULT_OK) {

			return;
		} else {
			switch (requestCode) {
			case 1:
				Log.i(TAG, filePath);

				int degree = readPictureDegree(filePath);
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 2;// 图片宽高都为原来的二分之1
				Bitmap abitmap = BitmapFactory.decodeFile(filePath, options);
				Bitmap newbitmap = rotaingImageView(degree, abitmap);
				if (newbitmap != null) {
					if (type == image_one) {
						iv_one_s.setImageBitmap(newbitmap);
						iv_one.setVisibility(View.GONE);
						maplist.put(1, filePath);
					}
					if (type == image_two) {
						iv_two_s.setImageBitmap(newbitmap);
						iv_two.setVisibility(View.GONE);
						maplist.put(2, filePath);
					}
					if (type == image_three) {
						iv_three_s.setImageBitmap(newbitmap);
						iv_three.setVisibility(View.GONE);
						maplist.put(3, filePath);
					}
					if (type == image_four) {
						iv_four_s.setImageBitmap(newbitmap);
						iv_four.setVisibility(View.GONE);
						maplist.put(4, filePath);
					}
				}

				break;
			case 2:
				if (data != null) {
					// 取得返回的Uri,基本上选择照片的时候返回的是以Uri形式，但是在拍照中有得机子呢Uri是空的，所以要特别注意
					Uri mImageCaptureUri = data.getData();
					// 返回的Uri不为空时，那么图片信息数据都会在Uri中获得。如果为空，那么我们就进行下面的方式获取
					if (mImageCaptureUri != null) {
						// 当选取的是图片的时候
						if (mImageCaptureUri.toString().contains("images")) {
							// 返回的Uri不为空时，那么图片信息数据都会在Uri中获得。如果为空，那么我们就进行下面的方式获取
							if (mImageCaptureUri != null) {
								Bitmap image;
								try {
									// 这个方法是根据Uri获取Bitmap图片的静态方法
									image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageCaptureUri);
									if (image != null) {
										String[] proj = { MediaStore.Images.Media.DATA };
										// android多媒体数据库的封装接口
										@SuppressWarnings("deprecation")
										Cursor cursor = managedQuery(mImageCaptureUri, proj, null, null, null);
										// 获得用户选择的图片的索引值
										int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
										cursor.moveToFirst();
										// 最后根据索引值获取图片路径
										String path = cursor.getString(column_index);
										BitmapFactory.Options boptions = new BitmapFactory.Options();
										boptions.inSampleSize = 4;// 图片宽高都为原来的二分之1
										Bitmap bbitmap = BitmapFactory.decodeFile(path, boptions);
										if (type == image_one) {
											iv_one_s.setImageBitmap(bbitmap);
											iv_one.setVisibility(View.GONE);
											maplist.put(1, path);
										}
										if (type == image_two) {
											iv_two_s.setImageBitmap(bbitmap);
											iv_two.setVisibility(View.GONE);
											maplist.put(2, path);
										}
										if (type == image_three) {
											iv_three_s.setImageBitmap(bbitmap);
											iv_three.setVisibility(View.GONE);
											maplist.put(3, path);
										}
										if (type == image_four) {
											iv_four_s.setImageBitmap(bbitmap);
											iv_four.setVisibility(View.GONE);
											maplist.put(4, path);
										}

									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							} else {
								Bundle extras = data.getExtras();
								if (extras != null) {
									// 这里是有些拍照后的图片是直接存放到Bundle中的所以我们可以从这里面获取Bitmap图片
									Bitmap cbitmap = extras.getParcelable("data");
									if (cbitmap != null) {
										if (type == image_one) {
											iv_one_s.setImageBitmap(cbitmap);
											iv_one.setVisibility(View.GONE);
											maplist.put(1, saveBimap(cbitmap));
										}
										if (type == image_two) {
											iv_two_s.setImageBitmap(cbitmap);
											iv_two.setVisibility(View.GONE);
											maplist.put(2, saveBimap(cbitmap));
										}
										if (type == image_three) {
											iv_three_s.setImageBitmap(cbitmap);
											iv_three.setVisibility(View.GONE);
											maplist.put(3, saveBimap(cbitmap));
										}
										if (type == image_four) {
											iv_four_s.setImageBitmap(cbitmap);
											iv_four.setVisibility(View.GONE);
											maplist.put(4, saveBimap(cbitmap));
										}

									}
								}
							}
						}
					}

				}
				break;
			}

		}
		dialog.dismiss();

	};

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
				file.flush();
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("file" + filename);
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
		String saveDir = "/mnt/sdcard/kalai";
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
	 * 读取图片属性：旋转的角度
	 * 
	 * @param path
	 *            图片绝对路径
	 * @return degree旋转的角度
	 */
	@SuppressLint("NewApi")
	public static int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/**
	 * 
	 * rotaingImageView:(旋转图片). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param angle
	 * @param bitmap
	 * @return
	 * @since JDK 1.6
	 */
	public Bitmap rotaingImageView(int angle, Bitmap bitmap) {
		// 旋转图片 动作
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		// 创建新的图片
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		return resizedBitmap;
	}

	/**
	 * 
	 * setCreditCardBean:(组装请求数据). <br/>
	 * 
	 * @author zhuxiaohao
	 * @return 组装数据对象
	 * @since JDK 1.6
	 */
	@SuppressWarnings("unused")
	private CreditCardBean setCreditCardBean(BankCrad bankCrad) {
		CreditCardBean creditCardBean = null;
		if (bankCrad != null) {
			creditCardBean = new CreditCardBean();
			creditCardBean.pointService = bankCrad.pointService;
			creditCardBean.acctNo = bankCrad.acctNo;
			creditCardBean.transAmt = bankCrad.transAmt;
			creditCardBean.trackdatas = bankCrad.trackdatas;
			creditCardBean.pin = bankCrad.pin;
			creditCardBean.tranxSN = bankCrad.tranxSN;
			creditCardBean.ic = bankCrad.ic;
			creditCardBean.cardEXPDate = bankCrad.cardEXPDate;
			creditCardBean.cardSN = bankCrad.cardSN;
			creditCardBean.ksn = bankCrad.ksn;
			creditCardBean.encWorkingKey = bankCrad.EncWorkingKey;
		}

		return creditCardBean;
	}

	/**
	 * 
	 * setDeviceinfo:(把设备信息封装成对象). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param deviceInfo
	 * @since JDK 1.6
	 */
	public DeviceInfoString setDeviceinfo(DeviceInfo deviceInfo) {
		DeviceInfoString ds = new DeviceInfoString();
		ds.sn = deviceInfo.getSN();
		ds.isFactoryModel = deviceInfo.isFactoryModel();
		ds.isMainkeyLoaded = deviceInfo.isMainkeyLoaded();
		ds.isWorkingkeyLoaded = deviceInfo.isWorkingkeyLoaded();
		ds.isDUKPTkeyLoadedfalse = deviceInfo.isDUKPTkeyLoaded();
		ds.udid = deviceInfo.getUdid();
		ds.appVer = deviceInfo.getAppVer();
		ds.csn = deviceInfo.getCSN();
		ds.ksn = deviceInfo.getKSN();
		ds.pid = deviceInfo.getPID();
		ds.vid = deviceInfo.getVID();
		ds.customSN = deviceInfo.getCustomSN();
		ds.isSupportAudio = deviceInfo.isSupportAudio();
		ds.isSupportBlueTooth = deviceInfo.isSupportBlueTooth();
		ds.isSupportUSB = deviceInfo.isSupportUSB();
		ds.isSupportMagCard = deviceInfo.isSupportMagCard();
		ds.isSupportICCard = deviceInfo.isSupportICCard();
		ds.isSupportQuickPass = deviceInfo.isSupportQuickPass();
		ds.isSupportPrint = deviceInfo.isSupportPrint();
		ds.isSupportLCD = deviceInfo.isSupportLCD();
		ds.firmwareVer = deviceInfo.getFirmwareVer();
		return ds;

	}

	/**
	 * 
	 * handleBack:(刷新卡号). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param s
	 * @since JDK 1.6
	 */
	private void handleBack(final String s) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ed_crad.setText(s.replaceAll(".{4}(?!$)", "$0 "));
			}
		});
		handleGetCardName(s);
	}

	/**
	 * 
	 * handleGetCardName:(请求服务器验证时候支持该卡). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param s
	 * @since JDK 1.6
	 */
	private void handleGetCardName(final String s) {
		if (!Common.checkNetWork(getApplicationContext())) {
			Toast.makeText(getApplicationContext(), "当前没有网络", Toast.LENGTH_LONG).show();
			return;
		}
		new Thread() {
			@Override
			public void run() {
				super.run();
				msgValidation = controller2.isValidation(s);
				handCradName(msgValidation, false);
			}
		}.start();
	}

	/**
	 * 
	 * handCradName:(刷新银行名字). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param s
	 * @since JDK 1.6
	 */
	private void handCradName(final MsgValidation msgValidation, final boolean isSubmit) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (msgValidation == null) {
					Toast.makeText(getApplicationContext(), "该卡不支持", Toast.LENGTH_LONG).show();
					ed_crad.setText("");
					return;
				}
				if (msgValidation.cardtype.contains("借记卡")) {
					Toast.makeText(getApplicationContext(), "不支持借记卡", Toast.LENGTH_LONG).show();
					ed_crad.setText("");
					return;
				}
				cardNochecked = true;
				bank_crad.setText(msgValidation.name);
				if (isSubmit) {
					doOk();
				}
			}
		});
	}

	/**
	 * 
	 * operation_stay:(执行事件). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param type
	 * @since JDK 1.6
	 */
	private void operation_stay(int type) {
		switch (type) {
		// 取消
		case Common.CANCEL:
			new Thread(new Runnable() {
				@Override
				public void run() {
					if (DeviceConnState.CONNECTED == controller.getDeviceConnState()) {
						try {
							controller.reset();
						} catch (Exception e) {
							Message msg = new Message();
							msg.obj = "撤消指令执行失败!" + e.getMessage();
							iHandler.sendMessage(msg);
						}
					} else {
						Message msg = new Message();
						msg.obj = "设备未连接!";
						iHandler.sendMessage(msg);

					}
					processing = false;
					// 重新交易
					operation_stay(Common.FETCH_DEVICE_INFO);
				};
			}).start();
			break;

		// 取得设备信息
		case Common.FETCH_DEVICE_INFO:

			new Thread(new Runnable() {
				@Override
				public void run() {
					getDeviceinfo();

				}
			}).start();
			break;

		// 刷卡或者插卡
		case Common.SWIPCARD_ME11:

			new Thread(new Runnable() {
				@Override
				public void run() {

					if (CHOSE_DEVICE.equals("A")) {
						swiperCard();
					} else {// 蓝牙
						swipBlueTooth();
					}
				}
			}).start();
		}
	}

	// 蓝牙刷卡流程
	void swipBlueTooth() {
		swipShow();
		final SwipResult swipResult = controller.swipCardMe3X("请刷卡/插卡", "0.0", new SimpleTransferListener(), 30000L, TimeUnit.MILLISECONDS, needTime);
		if (swipResult != null) {
			byte[] secondTrack = swipResult.getSecondTrackData();
			byte[] thirdTrack = swipResult.getThirdTrackData();
			String account = swipResult.getAccount().getAcctNo();
			Log.v("info", "ino-->" + account + "");
			Log.v("info", "ino-->" + Dump.getHexDump(secondTrack) + "");
			handleBack(account);
			cardHeadDialog.dismiss();

		} else {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {

					cardHeadDialog.txt_title.setText("正在读取卡的信息");
					cardHeadDialog.tip_pb.setVisibility(View.VISIBLE);
				}
			});
		}
		swipHidden();
	}

	/**
	 * 初始化设备
	 * 
	 * @since ver1.0
	 * @param params
	 *            设备连接参数
	 */
	private void initMe3xDeviceController(DeviceConnParams params) {
		controller.init(AddTimelyAccountActivity.this, Common.ME11_DRIVER_NAME, params, new DeviceEventListener<ConnectionCloseEvent>() {
			@SuppressLint("NewApi")
			@Override
			public void onEvent(ConnectionCloseEvent event, Handler handler) {
				if (event.isSuccess()) {// 设备被客户主动断开！
				}
				if (event.isFailed()) {// 设备链接异常断开！
					Message msg = new Message();
					msg.obj = "设备已经拔出,请插入刷卡器!";
					msg.what = connect;
					iHandler.sendMessage(msg);
					DealDisConnectedMessage(dialogHandler);

					initMe3xDeviceController(new AudioPortV100ConnParams());
					operation_stay(Common.FETCH_DEVICE_INFO);

				}
			}

			@Override
			public Handler getUIHandler() {
				return null;
			}
		});
		// processing = false;
	}

	/**
	 * getDeviceinfo:(获取设备信息). <br/>
	 * 
	 * @author zhuxiaohao
	 * @since JDK 1.6
	 */
	@SuppressWarnings("unused")
	@SuppressLint("NewApi")
	private void getDeviceinfo() {
		processing = true;
		try {
			connectDevice();

			csn = deviceInfo.getCSN() + "";
			if (deviceInfo == null) {
				deviceInfoString = setDeviceinfo(deviceInfo);
				return;
			} else if (deviceInfo != null) {
				operation_stay(Common.SWIPCARD_ME11);
				DealConnectedMessage(dialogHandle);

			}
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	/**
	 * connectDevice:(设备连接). <br/>
	 * 
	 * @author zhuxiaohao
	 * @since JDK 1.6
	 */
	@SuppressLint("NewApi")
	public void connectDevice() {
		try {
			runOnUiThread(new Runnable() {
				public void run() {
					if (CHOSE_DEVICE.equals("A")) {
						cardHeadDialog.txt_title.setText("请插入卡头");
					} else {
						cardHeadDialog.txt_title.setText("正在连接蓝牙设备");
					}

				};
			});

			try {
				controller.connect();
			} catch (Exception e) {
				Tip("连接失败，未配对正确设备");
			}
			runOnUiThread(new Runnable() {
				public void run() {
					cardHeadDialog.tip_pb.setVisibility(View.VISIBLE);
					cardHeadDialog.txt_title.setText("正在连接设备......");
				};
			});
			exec.scheduleAtFixedRate(new Runnable() {

				@Override
				public void run() {
					try {

						if (DeviceConnState.CONNECTED != controller.getDeviceConnState() && Controller.isEmpty(ed_crad.getText().toString().trim())) {

							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(AddTimelyAccountActivity.this, "连接中断,请稍后重试", Toast.LENGTH_SHORT).show();

								}

							});
							AddTimelyAccountActivity.this.finish();
							exec.isShutdown();
							exec = null;
						}
					} catch (Exception e) {

						if (DeviceConnState.CONNECTED != controller.getDeviceConnState() && Controller.isEmpty(ed_crad.getText().toString().trim())) {
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(AddTimelyAccountActivity.this, "连接中断,请稍后重试", Toast.LENGTH_SHORT).show();

								}

							});
							AddTimelyAccountActivity.this.finish();
							exec.isShutdown();
							exec = null;

						}

					}
					/**/

				}

			}, 30, 300, TimeUnit.MILLISECONDS);
			Looper.prepare();
			deviceInfo = controller.getDeviceInfo();
			if (deviceInfo != null) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {

						Message msgs = new Message();
						msgs.obj = "请刷卡或者插卡";
						iHandler.sendMessage(msgs);
						cardHeadDialog.txt_title.setText("请刷卡或者插卡......");
						Toast.makeText(AddTimelyAccountActivity.this, "请刷卡或者插卡", Toast.LENGTH_SHORT).show();
						DealConnectedMessage(dialogHandler);

						cardHeadDialog.tip_pb.setVisibility(View.GONE);
					}

				});
				operation_stay(Common.SWIPCARD_ME11);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * addMessageHandler:(刷新对话框的事件). <br/>
	 * 
	 * @author zhuxiaohao
	 * @since JDK 1.6
	 */
	private void addMessageHandler() {
		iHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == swiper) {// 刷卡过程
					cardHeadDialog.txt_title.setText((String) msg.obj);
				} else if (msg.what == connect) {// 异常断开
					cardHeadDialog.txt_title.setText((String) msg.obj);
				} else if (msg.what == cancel) {// 刷卡取消
					cardHeadDialog.txt_title.setText((String) msg.obj);
				} else {
					cardHeadDialog.txt_title.setText((String) msg.obj);
				}
			}
		};
	}

	/**
	 * swiperCard:(刷卡或者插卡). <br/>
	 * 
	 * @author zhuxiaohao
	 * @since JDK 1.6
	 */
	@SuppressLint("NewApi")
	private void swiperCard() {
		try {
			swipShow();
			ME11SwipResult swipRslt = controller.swipCard("", 30000L, TimeUnit.MILLISECONDS);
			if (swipRslt == null) {
				return;
			} else {

				ModuleType[] moduleType = swipRslt.getReadModels();
				if (moduleType[0] == ModuleType.COMMON_ICCARD) {// ic卡插卡
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							cardHeadDialog.txt_title.setText("正在读取卡的信息");
							cardHeadDialog.tip_pb.setVisibility(View.VISIBLE);
							swipHidden();
						}

					});
					controller.startEmv(new BigDecimal(transAmt), new SimpleTransferListener());
				} else if (moduleType[0] == ModuleType.COMMON_SWIPER) {// 刷卡
					String kzinfoTrack = Dump.getHexDump(swipRslt.getExtInfo()).replaceAll(" ", "");
					String account = swipRslt.getAccount().getAcctNo().replaceAll("(?i)F", "");
					Log.e("sean", account);
					bankCrad = new BankCrad();
					bankCrad.ksn = csn;// 设备 KSN
					bankCrad.account = account;// 账号
					bankCrad.pointService = "021";// 磁条卡
					bankCrad.trackdatas = kzinfoTrack.substring(16, kzinfoTrack.length());
					bankCrad.ic = "0";
					bankCrad.EncWorkingKey = kzinfoTrack.substring(0, 16);
					handleBack(account);
					cardHeadDialog.dismiss();

				}
			}
		} catch (Exception e) {
			if (DeviceConnState.CONNECTED == controller.getDeviceConnState()) {
				Message msg = new Message();
				msg.obj = "刷卡或者插卡失败" + e.getMessage() + ",请重新刷卡或者插卡";
				msg.what = cancel;
				iHandler.sendMessage(msg);
				if (closed) {
					return;
				}
				swiperCard();
			}
		}
	}

	/**
	 * 
	 * ClassName: SimpleTransferListener <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-4-24 下午2:31:21 <br/>
	 * blog:http://blog.csdn.net/qq718799510?viewmode=contents
	 * 
	 * @author zhuxiaohao
	 * @version TopUpActivity
	 * @since JDK 1.6 芯片卡监听
	 */
	private class SimpleTransferListener implements TransferListener {
		@Override
		public void onEmvFinished(boolean arg0, EmvTransInfo context) throws Exception {
			if (closed) {
				return;
			}
			if (isStand == 1) {
				Message msg = new Message();
				msg.obj = "IC卡暂不支持转账汇款和信用卡还款的功能!!!";
				iHandler.sendMessage(msg);
				return;
			}
			if (bankCrad == null) {
				Message msg = new Message();
				msg.obj = "插卡失败,请重插...";
				iHandler.sendMessage(msg);
				operation_stay(Common.SWIPCARD_ME11);
				return;
			}
			closed = true;
		}

		@Override
		public void onError(EmvTransController arg0, Exception arg1) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onFallback(EmvTransInfo arg0) throws Exception {
			// TODO Auto-generated method stub
		}

		// 芯片卡 55域
		@Override
		public void onRequestOnline(EmvTransController arg0, EmvTransInfo context) throws Exception {
			StringBuilder builder = new StringBuilder();
			String tag_9F26 = Dump.getHexDump(context.getAppCryptogram()).replaceAll(" ", "");
			builder.append(Common.getTLV2Str("9F26:" + tag_9F26));
			byte tag_9F27 = context.getCryptogramInformationData();
			builder.append(Common.getTLV2Str("9F27:" + Integer.toHexString(tag_9F27 & 0xFF)));
			Log.e("sean", "tag_9F27:" + tag_9F27);
			String tag_9F10 = Dump.getHexDump(context.getIssuerApplicationData()).replaceAll(" ", "");
			builder.append(Common.getTLV2Str("9F10:" + tag_9F10));
			String tag_9F37 = Dump.getHexDump(context.getUnpredictableNumber()).replaceAll(" ", "");
			builder.append(Common.getTLV2Str("9F37:" + tag_9F37));
			String tag_9F36 = Dump.getHexDump(context.getAppTransactionCounter()).replaceAll(" ", "");
			builder.append(Common.getTLV2Str("9F36:" + tag_9F36));
			String tag_95 = Dump.getHexDump(context.getTerminalVerificationResults()).replaceAll(" ", "");
			builder.append(Common.getTLV2Str("95:" + tag_95));
			String tag_9A = context.getTransactionDate().replaceAll(" ", "");
			Log.e("info", "tag_9A=" + tag_9A);
			builder.append(Common.getTLV2Str("9A:" + tag_9A));
			int tag_9C = context.getTransactionType();
			builder.append(Common.getTLV2Str("9C:" + "0" + tag_9C));
			String tag_9F02 = context.getAmountAuthorisedNumeric().replaceAll(" ", "");
			while (true) {
				tag_9F02 = "0" + tag_9F02;
				if (tag_9F02.length() >= 12) {
					break;
				}
			}
			builder.append(Common.getTLV2Str("9F02:" + tag_9F02));
			String tag_5F2A = context.getTransactionCurrencyCode().replaceAll(" ", "");
			builder.append(Common.getTLV2Str("5F2A:" + "0" + tag_5F2A));
			String tag_82 = Dump.getHexDump(context.getApplicationInterchangeProfile()).replaceAll(" ", "");
			builder.append(Common.getTLV2Str("82:" + tag_82));
			String tag_9F1A = context.getTerminalCountryCode().replaceAll(" ", "");
			builder.append(Common.getTLV2Str("9F1A:" + "0" + tag_9F1A));
			String tag_9F03 = context.getAmountOtherNumeric().replaceAll(" ", "");
			while (true) {
				tag_9F03 = "0" + tag_9F03;
				if (tag_9F03.length() >= 12) {
					break;
				}
			}
			builder.append(Common.getTLV2Str("9F03:" + tag_9F03));
			String tag_9F33 = Dump.getHexDump(context.getTerminal_capabilities()).replaceAll(" ", "");
			builder.append(Common.getTLV2Str("9F33:" + tag_9F33));
			String tag_9F34 = Dump.getHexDump(context.getCvmRslt()).replaceAll(" ", "");
			builder.append(Common.getTLV2Str("9F34:" + tag_9F34));
			String tag_9F35 = context.getTerminalType().replaceAll(" ", "");
			builder.append(Common.getTLV2Str("9F35:" + tag_9F35));
			String tag_9F1E = context.getInterface_device_serial_number().replaceAll(" ", "");
			for (int i = 0; i < tag_9F1E.length(); i++) {
				char c = tag_9F1E.charAt(i);
				if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
					tag_9F1E = tag_9F1E.replace(c, '0');
				}
			}
			builder.append(Common.getTLV2Str("9F1E:" + "3030303030303031"));
			String tag_84 = Dump.getHexDump(context.getDedicatedFileName()).replaceAll(" ", "");
			Log.e("sean", "tag_84:" + tag_84);
			builder.append(Common.getTLV2Str("84:" + tag_84));
			String tag_9F09 = Dump.getHexDump(context.getAppVersionNumberTerminal()).replaceAll(" ", "");
			builder.append(Common.getTLV2Str("9F09:" + tag_9F09));
			String tag_9F41 = Dump.getHexDump(context.getTransactionSequenceCounter()).replaceAll(" ", "");
			builder.append(Common.getTLV2Str("9F41:" + tag_9F41));
			String cardNo = context.getCardNo();
			String CardSequenceNumber = context.getCardSequenceNumber();
			String kzinfoTrack = Dump.getHexDump(context.getTrack_2_eqv_data()).replaceAll(" ", "");
			bankCrad = new BankCrad();
			bankCrad.ksn = csn;
			bankCrad.trackdatas = kzinfoTrack.substring(16, kzinfoTrack.length());
			bankCrad.ic = builder + "@" + context.getCardExpirationDate().substring(0, 4) + "@" + CardSequenceNumber;
			bankCrad.EncWorkingKey = kzinfoTrack.substring(0, 16);
			bankCrad.account = cardNo;
			bankCrad.pointService = "051";

			SecondIssuanceRequest request = new SecondIssuanceRequest();
			request.setAuthorisationResponseCode("00");
			arg0.secondIssuance(request);
			handleBack(cardNo);
			cardHeadDialog.dismiss();
		}

		@Override
		public void onRequestPinEntry(EmvTransController arg0, EmvTransInfo arg1) throws Exception {
			Message msg = new Message();
			msg.obj = "错误的事件返回，不可能要求密码输入";
			iHandler.sendMessage(msg);
			arg0.cancelEmv();
		}

		@Override
		public void onRequestSelectApplication(EmvTransController arg0, EmvTransInfo arg1) throws Exception {
			Message msg = new Message();
			msg.obj = "错误的事件返回，不可能要求应用选择！";
			iHandler.sendMessage(msg);
			arg0.cancelEmv();
		}

		@Override
		public void onRequestTransferConfirm(EmvTransController arg0, EmvTransInfo arg1) throws Exception {
		}

		@Override
		public void onSwipMagneticCard(SwipResult swipRslt) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onOpenCardreaderCanceled() {
			// TODO Auto-generated method stub
		}
	}

	/**
	 * 
	 * TODO 简单描述该方法的实现功能（销毁控制器）.
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		controller.destroy(); // 终端后的相关状态处理会通过事件完成,此处不需要处理
	}

	/**
	 * 
	 * TODO 简单描述该方法的实现功能（监听返回按钮,销毁控制器）.
	 * 
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			closed = true;
			controller.destroy();
			final boolean needResult = getIntent().getBooleanExtra("needResult", false);
			if (needResult) {
				Intent resultData = new Intent();
				JSONObject json = new JSONObject();
				try {
					json.put("callback", 0);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				resultData.putExtra("callback", json.toString());
				setResult(RESULT_OK, resultData);
			}
			;
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 
	 * ClassName:  <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-5-7 上午11:47:07 <br/>
	 * blog:http://blog.csdn.net/qq718799510?viewmode=contents
	 * 
	 * @author zhuxiaohao
	 * @version TimelyAccountActivity
	 * @since JDK 1.6
	 */
	class UpdateTextTask extends AsyncTask<Void, Integer, Integer> {
		private Context context;

		UpdateTextTask(Context context) {
			this.context = context;
		}

		/**
		 * 运行在UI线程中，在调用doInBackground()之前执行
		 */
		@Override
		protected void onPreExecute() {
			// Toast.makeText(context, "正在上传图片..", Toast.LENGTH_SHORT).show();
		}

		/**
		 * 后台运行的方法，可以运行非UI线程，可以执行耗时的方法
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		protected Integer doInBackground(Void... params) {

			Stuats = httpPostFile.postFiles(list, state.cardid, "http://121.43.231.170/klapi/B2CPay/CardImg");
			return null;
		}

		/**
		 * 运行在ui线程中，在doInBackground()执行完毕后执行
		 */
		@Override
		protected void onPostExecute(Integer integer) {
			startActivity(new Intent(AddTimelyAccountActivity.this, TimelyAccountOverActivity.class));
			isboolean = 0;
			final boolean needResult = getIntent().getBooleanExtra("needResult", false);
			if (needResult) {
				Intent resultData = new Intent();
				JSONObject json = new JSONObject();
				try {
					json.put("callback", 1);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				resultData.putExtra("callback", json.toString());
				setResult(RESULT_OK, resultData);
			}
			finish();
		}

		/**
		 * 在publishProgress()被调用以后执行，publishProgress()用于更新进度
		 */
		@Override
		protected void onProgressUpdate(Integer... values) {
			pd.dismiss();
		}
	}

	private void handleText() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String no = getIntent().getStringExtra("cardno");
				String nn = getIntent().getStringExtra("uname");
				ed_crad.setText(no.replaceAll(".{4}(?!$)", "$0 "));
				ed_crad_name.setText(nn);

			}
		});
	}

	private Handler dialogHandle = new Handler() {
		@SuppressLint("NewApi")
		public void handleMessage(Message msg) {
			// Tip(msg.what+"");
			switch (msg.what) {
			case 1:// 卡头连接成功
				cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("cash2")));
				break;
			case 2:
				cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("cash")));
				break;
			case 3:// 连接成功
				cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("ly_cash2")));// 蓝牙连上
				break;
			case 4:
				cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("ly_cash")));// 蓝牙未连
				break;
			case Configure.ME15_DISCONNECTED:
				cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("ly_cash_me15")));// 蓝牙未连上
				break;
			case Configure.ME15_CONNECTED:
				cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("ly_cash2_me15")));// 蓝牙连
				break;
			case Configure.DH_DISCONNECTED:
				cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("ly_cash_dh")));//
				break;
			case Configure.DH_CONNECTED:
				cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("ly_cash2_dh")));//
				break;

			}
		};
	};

	/**
	 * edit 监听事件
	 */
	private TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (ed_crad.length() >= 16) {
				handleGetCardName(ed_crad.getText().toString().trim());
			}

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

}
