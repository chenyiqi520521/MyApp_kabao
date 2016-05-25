/**
 * Project Name:CardPay
 * File Name:TopUpActivitu.java
 * Package Name:com.apicloud.activity
 * Date:2015-4-23上午9:35:51
 * Copyright (c) 2015, zhuxiaohao All Rights Reserved.
 *
 */

package com.apicloud.activity;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.apicloud.activity.BaseActivity;
import com.apicloud.activity.timely.TimelyAccountTopUpActivity;
import com.apicloud.activity.topup.NotCardHeadDialog;
import com.apicloud.activity.topup.TopUpDialog;
import com.apicloud.activity.write.HandwritingActivity;
import com.apicloud.common.Common;
import com.apicloud.common.PinSecurityImpl;
import com.apicloud.controller.Controller;
import com.apicloud.controller.DeviceController;
import com.apicloud.controller.DeviceListener;
import com.apicloud.controller.TransferListener;
import com.apicloud.impl.DeviceControllerImpl;
import com.apicloud.landy.LandyTackMsg;
import com.apicloud.module.BankCrad;
import com.apicloud.module.CreditCardBean;
import com.apicloud.module.MsgBean;
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
import com.newland.mtype.module.common.pin.AccountInputType;
import com.newland.mtype.module.common.swiper.SwipResult;
import com.newland.mtype.module.external.me11.ME11SwipResult;
import com.newland.mtype.tlv.TLVPackage;
import com.newland.mtype.util.Dump;
import com.newland.mtype.util.ISOUtils;
import com.newland.mtypex.audioport.AudioPortV100ConnParams;
import com.umeng.analytics.MobclickAgent;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

/**
 * ClassName:TopUpActivitu <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2015-4-23 上午9:35:51 <br/>
 * o
 * 
 * @author zhuxiaohao blog:http://blog.csdn.net/qq718799510?viewmode=contents
 * @version 1.0.0
 * @since JDK 1.6
 * @see 钱包充值界面
 */
public class KuaiLainActivity extends BaseActivity implements OnClickListener {
	ImageButton ib_return;// 返回按钮
	TextView ed_crad;// 卡号
	EditText ed_crad_amount;// 金额
	RadioGroup radiogroup;// 单选组
	RadioButton rad_one, rad_two, rad_three, rad_four, rad_five;// rad
	TextView btn_ok;// 立即充值
	protected String info = "A";// 选中数据
	TopUpDialog topUpDialog;// 输入密码对话框
	// NotCardHeadDialog cardHeadDialog;// 检测卡头的对话框
	String code;

	boolean processing = false;// 时候进行中
	// DeviceController controller = DeviceControllerImpl.getInstance();// 卡头控制器
	boolean closed = false;// 关闭
	int cancel = 2;// 如果取消
	int connect = 4;// 如果连接失败
	int isStand = 0;// 如果出现不支持卡状态
	int swiper = 3;// 刷卡状态
	Handler iHandler, sonHandler, swipHanlder, dhSonHandler;
	String price = "1";// 金额
	BankCrad bankCrad;// 银行卡刷出信息
	String csn;// 这个又称 KSN
	String pass;
	DeviceInfo deviceInfo;// 设备信息
	Controller controller2;// 控制器
	MsgBean msgBean;// 返回对象
	protected CreditCardBean creditCardBean;// 请求对象
	// ProgressDialog pd;
	ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
	//快联输入框
	EditText et_kuailian_account,et_kuailian_psd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int layoutId = UZResourcesIDFinder.getResLayoutID("kuai_lian");
		if (layoutId > 0) {
			setContentView(layoutId);
		}
		initView();
		//code = getIntent().getStringExtra("code");
		topUpDialog = new TopUpDialog(KuaiLainActivity.this);
		topUpDialog.setOnclickListener(this);
		controller2 = new Controller(getApplicationContext());
		addMessageHandler();
		initSonHandler();
		initSwipHandler();
		try {
			startLocation();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}
	/**
	 * 
	 * TODO模块化以后获取数据完毕的提交处理
	   @param ob  //不同模块不同的bean
	   2015年12月14日
	   void
	 */
	public void ModelSubmit(Object ob){
		//如果选择的是联迪设备
		if(CHOSE_DEVICE.equals(UICommon.WFT_DEVICE)||CHOSE_DEVICE.equals(UICommon.LANDY_DEVICE)||CHOSE_DEVICE.equals(UICommon.AF_DEVICE)||CHOSE_DEVICE.equals(UICommon.CFT_DEVICE)||CHOSE_DEVICE.equals(UICommon.BBPOS_IC_DEVICE)){
			LandyTackMsg landybean=(LandyTackMsg)ob;
			Log.v("landy1", "cardno--tp"+landybean.cardNo);
			ed_crad.setText(landybean.cardNo+"");
			//设备实例
			landySetBean(landybean);
			/*topUpDialog.show();
			topUpDialog.setOnclickListener(this);
			handler.sendEmptyMessage(1);
			topUpDialog.ed_crad_pass.setVisibility(View.VISIBLE);*/
			goToHanwriting(creditCardBean, msgBean,ed_crad_amount.getText().toString().trim());
		}
		
		
	}
	
	void landySetBean(LandyTackMsg landybean){
		creditCardBean=new CreditCardBean();
		creditCardBean.ksn=landybean.ksn;
		creditCardBean.encWorkingKey=landybean.enworkingKey+"";
		creditCardBean.acctNoT2=landybean.track2+"";
		creditCardBean.acctNoT3=landybean.track3+"";
		creditCardBean.ic=landybean.Data55+"";
		creditCardBean.pointService=landybean.pointService+"";
		creditCardBean.acctNo=landybean.cardNo+"";
		creditCardBean.cardEXPDate=landybean.expireDate+"";
		creditCardBean.cardSN="1";
		if(CHOSE_DEVICE.equals(UICommon.BBPOS_IC_DEVICE)||CHOSE_DEVICE.equals(UICommon.WFT_DEVICE)){
			creditCardBean.cardSN=landybean.cardSn+"";
		}
		
		creditCardBean.pin=landybean.pinBlock+"";
		creditCardBean.transAmt=landybean.amount+"";
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
        et_kuailian_account=(EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_kuailian_account"));
        et_kuailian_psd=(EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_kuailian_psd"));
        
		ed_crad = (TextView) findViewById(UZResourcesIDFinder.getResIdID("ed_crad"));
		ib_return = (ImageButton) findViewById(UZResourcesIDFinder.getResIdID("ib_return"));
		ed_crad_amount = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_crad_amount"));
		String moneyStr = getIntent().getStringExtra("money");

		if (!TextUtils.isEmpty(moneyStr)) {
			ed_crad_amount.setText(moneyStr);
		}

		radiogroup = (RadioGroup) findViewById(UZResourcesIDFinder.getResIdID("radiogroup"));
		rad_one = (RadioButton) findViewById(UZResourcesIDFinder.getResIdID("rad_one"));
		rad_two = (RadioButton) findViewById(UZResourcesIDFinder.getResIdID("rad_two"));
		rad_three = (RadioButton) findViewById(UZResourcesIDFinder.getResIdID("rad_three"));
		rad_four = (RadioButton) findViewById(UZResourcesIDFinder.getResIdID("rad_four"));
		rad_five = (RadioButton) findViewById(UZResourcesIDFinder.getResIdID("rad_five"));
		btn_ok = (TextView) findViewById(UZResourcesIDFinder.getResIdID("btn_ok"));
		ib_return.setOnClickListener(this);

		// 判断是卡头还是蓝牙
		String chose_device = getIntent().getStringExtra("posno");
		// Tip(chose_device);
		if (chose_device != null && chose_device.length() > 0) {
			CHOSE_DEVICE = chose_device;

		}
        //CHOSE_DEVICE="D";
		Log.v("push1", "chosedevice-->" + CHOSE_DEVICE);
		setGetCardNumTv(btn_ok);
		if (CHOSE_DEVICE.equals("A")) {
			btn_ok.setOnClickListener(this);
		}

		rad_one.setTag("A");
		rad_two.setTag("B");
		rad_three.setTag("C");
		rad_four.setTag("D");
		
		radiogroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				int id = group.getCheckedRadioButtonId();
				RadioButton button = (RadioButton) findViewById(id);
				info = button.getTag().toString();
			}
		});
		needShowMoeny = true;
		amount_et = ed_crad_amount;
		// 初始化父控件
		initParentView();
		initHeadDialog();
		initSwipParam();
		

	}
	void initSwipParam(){
		fromAct=UICommon.KuailainActivity;
		whatDo=SwipApi.WHATDO_SWIPER;
		cur_Ac=this;
	}
	void initSonHandler() {
		sonHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String content = (String) msg.obj;
				if (content != null && content.equals("签名完毕")) {
					ed_crad_amount.setEnabled(false);
					submit();
					return;
				}
				if (content != null && content.equals("sure")) {// 点击确定
					ed_crad.setText(bankCradParent.account.replaceAll(".{4}(?!$)", "$0 "));
					if (Controller.isEmpty(et_kuailian_account.getText().toString().trim())) {
						Toast.makeText(getApplicationContext(), "快联账户不能为空", Toast.LENGTH_LONG).show();
						return;
					}
					if (Controller.isEmpty(et_kuailian_psd.getText().toString().trim())) {
						Toast.makeText(getApplicationContext(), "快联密码不能为空", Toast.LENGTH_LONG).show();
						return;
					}
					if (Controller.isEmpty(ed_crad_amount.getText().toString().trim())) {
						Toast.makeText(getApplicationContext(), "输入金额不能为空", Toast.LENGTH_LONG).show();
						return;
					}

					price = ed_crad_amount.getText().toString().trim();
				    bankCradParent.transAmt = price;
					creditCardBean = setCreditCardBean(bankCradParent);
					doOk();
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

	
	@SuppressWarnings("static-access")
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// 立即充值
		if (v == btn_ok) {
			Log.v("", "");
			if (Controller.isEmpty(ed_crad_amount.getText().toString().trim())) {
				Toast.makeText(getApplicationContext(), "金额不能为空", Toast.LENGTH_LONG).show();
				return;

			}

			try {
				if (Integer.parseInt(ed_crad_amount.getText().toString().trim()) < 1) {
					Tip("请输入有效金额");
					return;
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			if (btn_ok.getText().toString().trim().contains("获取卡号")) {

				cardHeadDialog = new NotCardHeadDialog(KuaiLainActivity.this, CHOSE_DEVICE);
				cardHeadDialog.show();
				if (CHOSE_DEVICE.equals("A")) {
					initMe3xDeviceController(new AudioPortV100ConnParams());// 初始化设备
				}
				operation_stay(Common.FETCH_DEVICE_INFO);
				addMessageHandler();// 添加初始化一系列的事件
				// cardHeadDialog.txt_title.setText("请插入卡头");
			} else if (btn_ok.getText().toString().trim().contains("确定")) {
				/*if (Controller.isEmpty(ed_crad.getText().toString().trim())) {
					Toast.makeText(getApplicationContext(), "卡号不能为空", Toast.LENGTH_LONG).show();
					return;

				}*/
				price = ed_crad_amount.getText().toString().trim();
				doOk();
			}

		}
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
		// 如果输入密码框点击了确定
		if (v == topUpDialog.txt_ok) {
			btn_ok.setClickable(false);
			if (CHOSE_DEVICE.equals("A") && Controller.isEmpty(topUpDialog.ed_crad_pass.getText().toString().trim())) {
				Toast.makeText(KuaiLainActivity.this, "密码不能为空", Toast.LENGTH_LONG).show();
				return;
			}
			topUpDialog.txt_ok.setFocusable(false);
			topUpDialog.txt_ok.setEnabled(false);
			if (!Common.checkNetWork(getApplicationContext())) {
				Toast.makeText(getApplicationContext(), "当前没有网络", Toast.LENGTH_LONG).show();
				return;
			}

			topUpDialog.dismiss();
			goToHanwriting(creditCardBean, msgBean, ed_crad_amount.getText().toString().trim());

		}//
	}

	void submit() {

		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				
				if (locationInfo != null && locationInfo.length() > 0) {
					creditCardBean.gps = locationInfo;
					// submit();

				} else {
					Tip("定位失败，请检查网络等，重新进入页面定位");
					return;

				}
				PinSecurityImpl impl = new PinSecurityImpl();
				String pin = "";
				if (CHOSE_DEVICE.equals("A") || CHOSE_DEVICE.equals("D")) {
					pin = topUpDialog.ed_crad_pass.getText().toString().trim();
				}
				if (CHOSE_DEVICE.equals("B")) {
					pin = BaseActivity.BlueToothPsd;
				}
				Log.v("param", "pw-->" + pin + "");
				if (CHOSE_DEVICE.equals("A") || CHOSE_DEVICE.equals("B") || CHOSE_DEVICE.equals("D")) {
					if (pin != null && pin.length() >= 0 && !pin.equals("null")) {
						try {
							Log.v("at1", bankCrad.account + "");

							creditCardBean.pin = impl.desSecurity(bankCrad.account, pin, bankCrad.EncWorkingKey);

						} catch (Exception e) {
							// TODO: handle exception
						}
					}
				}

				creditCardBean.lkey = getIntent().getStringExtra("uid") + "";// 1435906091215330211
				
				if(CHOSE_DEVICE.equals("A")||CHOSE_DEVICE.equals("B")||CHOSE_DEVICE.equals("C")||CHOSE_DEVICE.equals("D")){
					Log.v("cft1", "4--"+ed_crad_amount.getText().toString().trim());
					creditCardBean.transAmt = Common.conversionPrice(ed_crad_amount.getText().toString().trim());//"000000000001";//
					creditCardBean.acctNo = ed_crad.getText().toString().trim();
				}
				if(CHOSE_DEVICE.equals(UICommon.WFT_DEVICE)||CHOSE_DEVICE.equals(UICommon.LANDY_DEVICE)||CHOSE_DEVICE.equals(UICommon.CFT_DEVICE)||CHOSE_DEVICE.equals(UICommon.AF_DEVICE)||CHOSE_DEVICE.equals(UICommon.BBPOS_IC_DEVICE)){
					creditCardBean.transAmt=Common.conversionPrice(creditCardBean.transAmt+"");
					Log.v("cft1", "3--"+creditCardBean.transAmt);
				}
				
				creditCardBean.kuailian_username=et_kuailian_account.getText().toString()+"";
				creditCardBean.kuailian_pwd=et_kuailian_psd.getText().toString()+"";
				creditCardBean.bizCode ="A";
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (pg != null) {
							pg.setCancelable(false);
							pg.setMessage("正在交易请稍后");
							pg.show();
						}

					}

				});

				msgBean = controller2.consume_kuailian(creditCardBean, code);
				handleMsg(msgBean);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (pg != null) {
							pg.dismiss();
							pg.setCancelable(true);
						}

					}

				});

			}
		}.start();
	}

	/**
	 * 
	 * handleMsg:(返回数据). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param msgBean
	 * @since JDK 1.6
	 */
	private void handleMsg(final MsgBean msgBean) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				topUpDialog.txt_ok.setFocusable(true);
				topUpDialog.txt_ok.setEnabled(true);
				// pd.dismiss();
				if (msgBean == null) {
					Toast.makeText(getApplicationContext(), "网络不给力", Toast.LENGTH_SHORT).show();
					KuaiLainActivity.this.finish();
					return;
				}
				// Tip("交易返回-->"+msgBean.toString()+"");
				if (msgBean.RspCd.contains("00")) {

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
						Intent intent = new Intent();
						intent.setClass(getApplicationContext(), HandwritingActivity.class);
						Bundle bundle = new Bundle();
						intent.putExtra("uid", getIntent().getStringExtra("uid"));
						intent.putExtra("name", "");
						intent.putExtra("merchant", "");
						intent.putExtra("terminal", "");
						intent.putExtra("cardNo", creditCardBean.acctNo);
						intent.putExtra("iss", "");
						intent.putExtra("amount", ed_crad_amount.getText().toString().trim());
						intent.putExtra("ReferNO", msgBean.ReferNO);
						intent.putExtra("TransDate", msgBean.TransDate);
						intent.putExtra("TransTime", msgBean.TransTime);
						intent.putExtra("orderno", msgBean.orderno + "");
						intent.putExtra("sign_path", sign_path);// 待合成的图片
						intent.putExtras(bundle);
						startActivity(intent);
						cardHeadDialog.dismiss();
					}
					;
					finish();
				} else {
					closed = true;
					controller.destroy();
					myFinish();
					Toast.makeText(getApplicationContext(), msgBean.RspMsg + "" + "错误码:" + msgBean.RspCd, Toast.LENGTH_LONG).show();
				}

			}
		});

	}

	/**
	 * 
	 * doOk:(立即充值). <br/>
	 * 
	 * @author zhuxiaohao
	 * @since JDK 1.6
	 */
	@SuppressWarnings("static-access")
	public void doOk() {
		if (CHOSE_DEVICE.equals("B") || CHOSE_DEVICE.equals("C")) {
			topUpDialog.ed_crad_pass.setVisibility(View.GONE);
			// submit();
			goToHanwriting(creditCardBean, msgBean, ed_crad_amount.getText().toString().trim());

		} else {
			topUpDialog.show();
			topUpDialog.setOnclickListener(this);
			handler.sendEmptyMessage(1);
			topUpDialog.ed_crad_pass.setVisibility(View.VISIBLE);
		}

	}

	/**
	 * 刷新对话框数据
	 */
	private Handler handler = new Handler() {
		@SuppressWarnings("static-access")
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				price = ed_crad_amount.getText().toString().trim() + "";
				topUpDialog.txt_crad_moery.setText("¥" + price);
				topUpDialog.txt_crad_t.setText("¥" + price);
				topUpDialog.txt_cradNumber.setText(ed_crad.getText().toString().trim());
				topUpDialog.ed_crad_pass.setText("");
				break;
			}
		};
	};

	/**
	 * 
	 * handleBack:(刷新卡号). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param s
	 * @since JDK 1.6
	 */
	private void handleBack(final String s) {

		if (BaseActivity.BlueToothPsd.equals("null") && CHOSE_DEVICE.equals("B")) {
			myFinish();
			Tip("退出了交易");
			return;
		}

		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				ed_crad.setText(s.replaceAll(".{4}(?!$)", "$0 "));
				btn_ok.setText("确定");

				doOk();
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
	public void operation_stay(int type) {
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
			if (processing) {
				operation_stay(Common.CANCEL);
			} else {
				new Thread(new Runnable() {
					@Override
					public void run() {
						getDeviceinfo();

					}
				}).start();
			}
			break;

		// 刷卡或者插卡
		case Common.SWIPCARD_ME11:
			new Thread(new Runnable() {
				@Override
				public void run() {

					if (CHOSE_DEVICE.equals("A")) {
						swiperCard();
					} else {// 蓝牙

						if (controller != null) {
							swipBlueTooth();
						}

					}
				}
			}).start();
		}
	}

	void initSwipHandler() {
		swipHanlder = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				SwipResult swipResult = (SwipResult) msg.obj;
				swipHidden();
				if (swipResult != null) {

					// kzinfoTrack =
					// Dump.getHexDump(swipResult.getExtInfo()).replaceAll(" ",
					// "");
					final String account = swipResult.getAccount().getAcctNo().replaceAll("(?i)F", "");
					Log.e("sean", account);
					bankCrad = new BankCrad();
					bankCrad.ksn = csn;
					bankCrad.account = account;
					bankCrad.pointService = "021";
					/*
					 * bankCrad.trackdatas
					 * =Dump.getHexDump(swipResult.getTrackDatas()).replaceAll(
					 * " ", ""); Log.v("track2",bankCrad.trackdatas);
					 */
					if (swipResult.getSecondTrackData() != null) {
						bankCrad.accNoT2 = SetTrackData(UnpackTrack(swipResult.getSecondTrackData())).toUpperCase();
					} else {
						cardHeadDialog.dismiss();
						Tip("获取磁道信息失败，请重新刷卡");
						return;
					}
					if (swipResult.getThirdTrackData() != null) {
						bankCrad.accNoT3 = SetTrackData(UnpackTrack(swipResult.getThirdTrackData())).toUpperCase();
					} /*
						 * else{ cardHeadDialog.dismiss();
						 * Tip("获取磁道信息失败，请重新刷卡"); return; }
						 */

					bankCrad.ic = "0";
					bankCrad.EncWorkingKey = getHex_workkey();
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							if (CHOSE_DEVICE.equals("B")) {
								cardHeadDialog.txt_title.setText("请输入密码");
								cardHeadDialog.tip_pb.setVisibility(View.VISIBLE);
							}

						}
					});

					new Thread() {
						public void run() {
							String p = ed_crad_amount.getText().toString().trim();
							String tip = "交易金额为" + p + "元\n请输入密码";
							controller.startReadingPwd(tip, new DeviceListener());
							handleBack(account);
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									cardHeadDialog.dismiss();

								}

							});

							creditCardBean = setCreditCardBean(bankCrad);
						};
					}.start();

				} else {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							cardHeadDialog.txt_title.setText("正在读取卡的信息");
							cardHeadDialog.tip_pb.setVisibility(View.VISIBLE);
						}
					});
				}
			}
		};
	}

	// 蓝牙刷卡流程
	void swipBlueTooth() {
		swipShow();
		String p = ed_crad_amount.getText().toString().trim();
		try {
			if (Integer.parseInt(p) < 1) {
				Tip("请输入有效金额");
				return;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		final SwipResult swipResult = controller.swipCardMe3X("交易金额:" + p + "元\n请刷卡/插卡", p, new SimpleTransferListener(), 4 * 30000L, TimeUnit.MILLISECONDS, swipHanlder, needTime);

	}

	/**
	 * 初始化设备
	 * 
	 * @since ver1.0
	 * @param params
	 *            设备连接参数
	 */
	public void initMe3xDeviceController(DeviceConnParams params) {
		controller.init(KuaiLainActivity.this, Common.ME11_DRIVER_NAME, params, new DeviceEventListener<ConnectionCloseEvent>() {
			@SuppressLint("NewApi")
			@Override
			public void onEvent(ConnectionCloseEvent event, Handler handler) {
				if (event.isSuccess()) {// 设备被客户主动断开！

				}
				if (event.isFailed()) {// 设备链接异常断开！
					Message msg = new Message();
					msg.obj = "设备已经拔出,请插入刷卡器!!!";
					msg.what = connect;
					iHandler.sendMessage(msg);
					DealDisConnectedMessage(dialogHandler);
					if (CHOSE_DEVICE.equals("A")) {
						initMe3xDeviceController(new AudioPortV100ConnParams());
					}

					operation_stay(Common.FETCH_DEVICE_INFO);
				}
			}

			@Override
			public Handler getUIHandler() {
				return null;
			}
		});

		processing = false;

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
			Log.v("", "");
			csn = deviceInfo.getCSN();
			// deviceInfo.getSN();
			/*
			 * if(csn.length()>14){ csn =csn.substring(0, 14); }
			 */

			if (deviceInfo == null) {
				return;
			} else if (deviceInfo != null) {
				DealConnectedMessage(dialogHandle);

				operation_stay(Common.SWIPCARD_ME11);
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	/**
	 * 设备连接
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

						if (DeviceConnState.CONNECTED != controller.getDeviceConnState() && Controller.isEmpty(ed_crad_amount.getText().toString().trim())) {

							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(KuaiLainActivity.this, "连接中断,请稍后重试", Toast.LENGTH_SHORT).show();

								}

							});
							KuaiLainActivity.this.finish();
							exec.isShutdown();
							exec = null;
						}
					} catch (Exception e) {

						if (DeviceConnState.CONNECTED != controller.getDeviceConnState() && Controller.isEmpty(ed_crad_amount.getText().toString().trim())) {
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(KuaiLainActivity.this, "连接中断,请稍后重试", Toast.LENGTH_SHORT).show();

								}

							});
							KuaiLainActivity.this.finish();
							exec.isShutdown();
							exec = null;

						}

					}
					/**/

				}

			}, 30, 300, TimeUnit.MILLISECONDS);
			deviceInfo = controller.getDeviceInfo();
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Message msg = new Message();
					msg.obj = "设备连接成功,正在读取数据,请稍候...";
					iHandler.sendMessage(msg);
					cardHeadDialog.tip_pb.setVisibility(View.GONE);

				}
			});

			// Looper.prepare();
			if (deviceInfo != null) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {

						Message msgs = new Message();
						msgs.obj = "请刷卡或者插卡";
						iHandler.sendMessage(msgs);
						Toast.makeText(KuaiLainActivity.this, "请刷卡或者插卡", Toast.LENGTH_SHORT).show();
						DealConnectedMessage(dialogHandler);

					}

				});

				operation_stay(Common.SWIPCARD_ME11);
			} else {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {

						Toast.makeText(KuaiLainActivity.this, "获取信息失败", Toast.LENGTH_SHORT).show();

					}

				});
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * 
	 * addMessageHandler:(刷新对话框的事件). <br/>
	 * 
	 * @author zhuxiaohao
	 * @since JDK 1.6
	 */
	public void addMessageHandler() {
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
			// creditCardBean.trackdatas = bankCrad.trackdatas;
			creditCardBean.pin = bankCrad.pin;
			creditCardBean.tranxSN = bankCrad.tranxSN;
			creditCardBean.ic = bankCrad.ic;
			creditCardBean.cardEXPDate = bankCrad.cardEXPDate;
			creditCardBean.cardSN = bankCrad.cardSN;
			creditCardBean.ksn = bankCrad.ksn;
			creditCardBean.encWorkingKey = bankCrad.EncWorkingKey;
			creditCardBean.acctNoT2 = bankCrad.accNoT2;
			creditCardBean.acctNoT3 = bankCrad.accNoT3;
			String gps = getIntent().getStringExtra("GPS");
			if (gps != null) {
				creditCardBean.gps = gps;
			}

		}
		return creditCardBean;
	}

	/**
	 * 
	 * swiperCard:(刷卡或者插卡). <br/>
	 * 
	 * @author zhuxiaohao
	 * @since JDK 1.6
	 */
	@SuppressLint("NewApi")
	private void swiperCard() {
		try {
			swipShow();
			String p = ed_crad_amount.getText().toString().trim();
			try {
				if (Integer.parseInt(p) < 1) {
					Tip("请输入有效金额");
					return;
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			ME11SwipResult swipRslt = controller.swipCard(p, 4 * 30000L, TimeUnit.MILLISECONDS);
			// cardHeadDialog.swipHiden();
			swipHidden();
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
							// Toast.makeText(TopUpActivity.this,"Ic插卡",Toast.LENGTH_SHORT).show();
							swipHidden();
						}

					});

					controller.startEmv(new BigDecimal(ed_crad_amount.getText().toString().trim()), new SimpleTransferListener());
				} else if (moduleType[0] == ModuleType.COMMON_SWIPER) {// 刷卡

					String kzinfoTrack = Dump.getHexDump(swipRslt.getExtInfo()).replaceAll(" ", "");
					String account = swipRslt.getAccount().getAcctNo().replaceAll("(?i)F", "");
					Log.e("sean", account);
					bankCrad = new BankCrad();
					bankCrad.ksn = csn;
					bankCrad.account = account;
					bankCrad.pointService = "021";
					// bankCrad.trackdatas = kzinfoTrack.substring(16,
					// kzinfoTrack.length());
					bankCrad.accNoT2 = kzinfoTrack.substring(16, kzinfoTrack.length());
					bankCrad.accNoT3 = "";
					// Log.v("track1", kzinfoTrack.substring(16,
					// kzinfoTrack.length()));
					bankCrad.ic = "0";
					bankCrad.EncWorkingKey = kzinfoTrack.substring(0, 16);
					handleBack(account);
					cardHeadDialog.dismiss();
					creditCardBean = setCreditCardBean(bankCrad);

				}

			}

		} catch (Exception e) {
			if (DeviceConnState.CONNECTED == controller.getDeviceConnState()) {
				Message msg = new Message();
				msg.obj = "刷卡或者插卡失败" + e.getMessage() + ",请重新刷卡或者插卡";
				msg.what = cancel;
				iHandler.sendMessage(msg);
				DealDisConnectedMessage(dialogHandle);

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
	boolean hasBeanReaded = false;

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
			Toast.makeText(KuaiLainActivity.this, "onEmvFinished", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onError(EmvTransController arg0, Exception arg1) {

		}

		@Override
		public void onFallback(EmvTransInfo arg0) throws Exception {
			// Toast.makeText(TopUpActivity.this,"onFallback",Toast.LENGTH_SHORT).show();
		}

		// 芯片卡 55域
		@Override
		public void onRequestOnline(EmvTransController arg0, EmvTransInfo context) throws Exception {

			if (!hasBeanReaded) {
				hasBeanReaded = true;
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
				if (CHOSE_DEVICE.equals("D")) {
					builder.append(Common.getTLV2Str("9C:" + tag_9C));
				} else {
					builder.append(Common.getTLV2Str("9C:" + "0" + tag_9C));
				}

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

				List<Integer> L_55TAGS = new ArrayList<Integer>();

				L_55TAGS.add(0x9F26);// "9F26:"
				L_55TAGS.add(0x9F27);
				L_55TAGS.add(0x9F10);
				L_55TAGS.add(0x9F37);
				L_55TAGS.add(0x9F36);
				L_55TAGS.add(0x95);
				L_55TAGS.add(0x9A);
				L_55TAGS.add(0x9C);
				L_55TAGS.add(0x9F02);
				L_55TAGS.add(0x5F2A);
				L_55TAGS.add(0x82);

				L_55TAGS.add(0x9F1A);
				L_55TAGS.add(0x9F03);
				L_55TAGS.add(0x9F33);
				L_55TAGS.add(0x9F34);
				L_55TAGS.add(0x9F35);

				L_55TAGS.add(0x9F1E);
				L_55TAGS.add(0x84);
				L_55TAGS.add(0x9F09);
				L_55TAGS.add(0x9F41);
				// L_55TAGS.add(0x9F63);

				TLVPackage tlvPackage = context.setExternalInfoPackage(L_55TAGS);
				String y55 = ISOUtils.hexString(tlvPackage.pack());
				// System.out.println(">>>>55域打包集合:"+
				// ISOUtils.hexString(tlvPackage.pack()));

				String cardNo = context.getCardNo();
				String CardSequenceNumber = context.getCardSequenceNumber();
				String kzinfoTrack = Dump.getHexDump(context.getTrack_2_eqv_data()).replaceAll(" ", "");
				bankCrad = new BankCrad();
				bankCrad.ksn = csn;
				// bankCrad.trackdatas = kzinfoTrack.substring(16,
				// kzinfoTrack.length());
				// bankCrad.ic = builder.toString();
				bankCrad.ic = y55;
				Log.v("y55", y55);
				bankCrad.cardSN = CardSequenceNumber;
				bankCrad.account = cardNo;
				bankCrad.cardEXPDate = context.getCardExpirationDate().substring(0, 4);
				bankCrad.pointService = "051";
				if (CHOSE_DEVICE.equals("A")) {// 音频
					bankCrad.accNoT2 = kzinfoTrack.substring(16, kzinfoTrack.length());
					bankCrad.accNoT3 = "";
					bankCrad.EncWorkingKey = kzinfoTrack.substring(0, 16);
				} else {// 蓝牙

					bankCrad.accNoT2 = Dump.getHexDump(context.getTrack_2_eqv_data()).replace("F", "").replace(">", "").replace("<", "").replace("=", "");
					// bankCrad.accNoT2=SetTrackData(UnpackTrack(context.getTrack_2_eqv_data())).toUpperCase();
					bankCrad.accNoT3 = "";
					bankCrad.EncWorkingKey = getHex_workkey();
				}
				SecondIssuanceRequest request = new SecondIssuanceRequest();
				request.setAuthorisationResponseCode("00");
				arg0.secondIssuance(request);
				if (CHOSE_DEVICE.equals("B")) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							if (CHOSE_DEVICE.equals("B")) {
								cardHeadDialog.txt_title.setText("请输入密码");
								cardHeadDialog.tip_pb.setVisibility(View.VISIBLE);
							}

						}
					});
					String p = ed_crad_amount.getText().toString().trim();
					controller.startReadingPwd("交易金额为:" + p + "元\n请输入密码", new DeviceListener());
				}

				// controller.startPininput(AccountInputType.USE_ACCT_HASH,
				// swipRslt.getAccount().getAcctHashId(), 6, true, "消费金额为:" +
				// amt.toString() + "\n请输入交易密码:", 60000);
				cardHeadDialog.dismiss();

				creditCardBean = setCreditCardBean(bankCrad);
				handleBack(cardNo);
				cardHeadDialog.tip_pb.setVisibility(View.VISIBLE);

			}

		}

		@Override
		public void onRequestPinEntry(EmvTransController arg0, EmvTransInfo arg1) throws Exception {
			Message msg = new Message();
			msg.obj = "错误的事件返回，不可能要求密码输入";
			iHandler.sendMessage(msg);
			arg0.cancelEmv();
			Toast.makeText(KuaiLainActivity.this, "onRequestPinEntry", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onRequestSelectApplication(EmvTransController arg0, EmvTransInfo arg1) throws Exception {
			Message msg = new Message();
			msg.obj = "错误的事件返回，不可能要求应用选择！";
			iHandler.sendMessage(msg);
			arg0.cancelEmv();
			Toast.makeText(KuaiLainActivity.this, "onRequestSelectApplication", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onRequestTransferConfirm(EmvTransController arg0, EmvTransInfo arg1) throws Exception {
			Toast.makeText(KuaiLainActivity.this, "onRequestTransferConfirm", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onSwipMagneticCard(SwipResult swipRslt) {
			Toast.makeText(KuaiLainActivity.this, "onSwipMagneticCard", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onOpenCardreaderCanceled() {
			Toast.makeText(KuaiLainActivity.this, "onOpenCardreaderCanceled", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// controller.destroy(); // 终端后的相关状态处理会通过事件完成,此处不需要处理

		if (CHOSE_DEVICE.equals("C")) {
			dhcontroller.destory();
		}

	}

	void myFinish() {
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// Tip("返回");
			closed = true;
			dhcontroller.destory();
			myFinish();

		}
		return super.onKeyDown(keyCode, event);
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

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

}
