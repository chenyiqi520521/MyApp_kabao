/**
 * Project Name:CardPay
 * File Name:CradPay.java
 * Package Name:com.apicloud.activity
 * Date:2015-4-23下午5:26:54
 * Copyright (c) 2015, zhuxiaohao All Rights Reserved.
 *
 */

package com.apicloud.activity.card;

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
import android.graphics.SumPathEffect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apicloud.activity.BaseActivity;
import com.apicloud.activity.timely.TimelyAccountTopUpActivity;
import com.apicloud.activity.topup.NotCardHeadDialog;
import com.apicloud.activity.topup.TopUpTwoActivity;
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
import com.apicloud.module.MsgValidation;
import com.apicloud.swip.SwipApi;
import com.apicloud.util.Configure;
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
import com.newland.mtype.tlv.TLVPackage;
import com.newland.mtype.util.Dump;
import com.newland.mtype.util.ISOUtils;
import com.newland.mtypex.audioport.AudioPortV100ConnParams;
import com.umeng.analytics.MobclickAgent;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

/**
 * ClassName:CradPay <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2015-4-23 下午5:26:54 <br/>
 * 
 * @author zhuxiaohao blog:http://blog.csdn.net/qq718799510?viewmode=contents
 * @version 1.0.0
 * @since JDK 1.6
 * @see 信用卡还款支付界面
 */
public class CradPayActivity extends BaseActivity implements OnClickListener {
	ImageButton ib_return;// 返回
	TextView txt_crad_number;// 卡号
	TextView txt_crad_name;// 持卡人姓名
	TextView txt_crad_moery;// 金额
	EditText txt_crad_pay;// 需要支付的金额
	EditText ed_crad;// 卡号
	TextView txt_crad_get;// 获取卡号
	TextView bank_crad;// 所属银行
	EditText ed_crad_pass;// 密码
	Button btn_ok;// 确认转账

	boolean processing = false;// 设备进行中
	
	boolean closed = false;// 关闭
	int cancel = 2;// 如果取消
	int connect = 4;// 如果连接失败
	int isStand = 0;// 如果出现不支持卡状态
	int swiper = 3;// 刷卡状态
	int states = 1;// 刷卡准备
	Handler iHandler,sonHandler;
	String price = "0.00";// 金额
	BankCrad bankCrad;// 银行卡刷出信息
	String csn;// 这个又称 KSN
	DeviceInfo deviceInfo;// 设备信息
	Controller controller2;// 控制器
	MsgBean msgBean;// 返回对象
	CreditCardBean creditCardBean;// 请求对象
	MsgValidation msgValidation;// 银行名
    ProgressDialog pd;
	int stuats = 1;
	LinearLayout ll_password;
	ScheduledThreadPoolExecutor exec=new ScheduledThreadPoolExecutor(1);  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("card_payments_pay"));
		initView();
		if(CHOSE_DEVICE.equals("A")||CHOSE_DEVICE.equals("D")){
			initMe3xDeviceController(new AudioPortV100ConnParams());// 初始化设备
			ll_password.setVisibility(View.VISIBLE);
		}else{
			 ll_password.setVisibility(View.GONE);
		}
		addMessageHandler();// 添加初始化一系列的事件
		/*operation_stay(Common.FETCH_DEVICE_INFO);
		cardHeadDialog = new NotCardHeadDialog(CradPayActivity.this);
		cardHeadDialog.show();*/
		controller2 = new Controller(getApplicationContext());
		initSonHandler();
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
		if(CHOSE_DEVICE.equals(UICommon.WFT_DEVICE)||CHOSE_DEVICE.equals(UICommon.LANDY_DEVICE)||CHOSE_DEVICE.equals(UICommon.CFT_DEVICE)||CHOSE_DEVICE.equals(UICommon.BBPOS_IC_DEVICE)){
			LandyTackMsg landybean=(LandyTackMsg)ob;
			
			//设备实例
			landySetBean(landybean);
			
			handleBack(landybean.cardNo+"");
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
	void initSonHandler(){
		sonHandler=new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String content=(String) msg.obj;
				if(content!=null&&content.equals("签名完毕")){
					try {
						
					} catch (Exception e) {
						// TODO: handle exception
					}
					submit();
					return;
				}
				if(content!=null&&content.equals("sure")&&(CHOSE_DEVICE.equals("B")||CHOSE_DEVICE.equals("C"))){//点击确定
					ed_crad.setText(bankCradParent.account.replaceAll(".{4}(?!$)", "$0 "));
					if (Controller.isEmpty(ed_crad.getText().toString().trim())) {
						Toast.makeText(getApplicationContext(), "卡号不能为空", Toast.LENGTH_LONG).show();
						return;
						}
					if (Controller.isEmpty(txt_crad_pay.getText().toString().trim())) {
					    Toast.makeText(getApplicationContext(), "输入金额不能为空", Toast.LENGTH_LONG).show();
						return;
					 }
					
					
					doOk();
				}
				
				if(CHOSE_DEVICE.equals("B")||CHOSE_DEVICE.equals("D")){
					try {
						int type=msg.what;
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
	 * initView:(初始化控件). <br/>
	 * 
	 * @author zhuxiaohao
	 * @since JDK 1.6
	 */
	@SuppressLint("NewApi")
	private void initView() {
		ib_return = (ImageButton) findViewById(UZResourcesIDFinder.getResIdID("ib_return"));
		txt_crad_number = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_crad_number"));
		txt_crad_name = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_crad_name"));
		txt_crad_moery = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_crad_moery"));
		txt_crad_pay = (EditText) findViewById(UZResourcesIDFinder.getResIdID("txt_crad_pay"));
		ed_crad = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_crad"));
		txt_crad_get = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_crad_get"));
		setGetCardNumTv(txt_crad_get);
		bank_crad = (TextView) findViewById(UZResourcesIDFinder.getResIdID("bank_crad"));
		ed_crad_pass = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_crad_pass"));
		btn_ok = (Button) findViewById(UZResourcesIDFinder.getResIdID("btn_ok"));
		txt_crad_number.setText(getIntent().getStringExtra("cardNo").replaceAll(".{4}(?!$)", "$0 "));
		txt_crad_moery.setText("¥" + getIntent().getStringExtra("money"));
		txt_crad_pay.setText("¥" + getIntent().getStringExtra("money"));
		ll_password=(LinearLayout) findViewById(UZResourcesIDFinder.getResIdID("ll_password"));
		ib_return.setOnClickListener(this);
		 //判断是卡头还是蓝牙
        String chose_device=getIntent().getStringExtra("posno");
        if(chose_device!=null&&chose_device.length()>0){
        	CHOSE_DEVICE=chose_device;
        	
        }
        //CHOSE_DEVICE="C";
        if(CHOSE_DEVICE.equals("A")){
        	txt_crad_get.setOnClickListener(this);
        }
		
		btn_ok.setOnClickListener(this);
		pd = new LoadingDialog(CradPayActivity.this);
		pd.setCancelable(true);// 设置进度条是否可以按退回键取消
		pd.setCanceledOnTouchOutside(false);// 设置点击进度对话框外的区域对话框不消失
		needShowMoeny=true;
		amount_et=txt_crad_pay;
		//初始化父控件
	    initParentView();
	    initHeadDialog();
	    try {
			startLocation();
		} catch (Exception e) {
			// TODO: handle exception
		}
	    initSwipParam();
		}
		void initSwipParam(){
			fromAct=UICommon.CardPayActivity;
			whatDo=SwipApi.WHATDO_SWIPER;
			cur_Ac=this;
		}
	@Override
	public void onClick(View v) {
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
		// 获取卡号
		if (v == txt_crad_get) {
			//cardHeadDialog = new NotCardHeadDialog(CradPayActivity.this);
			if(cardHeadDialog!=null){
				cardHeadDialog.show();
			}
			if (DeviceConnState.CONNECTED!= controller.getDeviceConnState()) {
			  if(CHOSE_DEVICE.equals("A")){
				  initMe3xDeviceController(new AudioPortV100ConnParams());// 初始化设备
			  }
			  operation_stay(Common.FETCH_DEVICE_INFO);
			}else{
             runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Message msgs = new Message();
						msgs.obj = "请刷卡或者插卡";
						iHandler.sendMessage(msgs);
						cardHeadDialog.tip_pb.setVisibility(View.GONE);
						
						
					}
				} );
			  operation_stay(Common.SWIPCARD_ME11);
			}
			
			
		}
		// 确认转账
		if (v == btn_ok) {
			doOk();
		}

	}

	/**
	 * 
	 * doOk:(确认转账处理数据). <br/>
	 * 
	 * @author zhuxiaohao
	 * @since JDK 1.6
	 */
	private void doOk() {
		if (Controller.isEmpty(ed_crad.getText().toString().trim())) {
			Toast.makeText(getApplicationContext(), "卡号不能为空", Toast.LENGTH_LONG).show();
			return;
		}
		if ((CHOSE_DEVICE.equals("A")||CHOSE_DEVICE.equals("D"))&&Controller.isEmpty(ed_crad_pass.getText().toString().trim())) {
			Toast.makeText(getApplicationContext(), "密码不能为空", Toast.LENGTH_LONG).show();
			return;
		}
		btn_ok.setFocusable(false);
		btn_ok.setEnabled(false);
		if (!Common.checkNetWork(getApplicationContext())) {
			Toast.makeText(getApplicationContext(), "当前没有网络", Toast.LENGTH_LONG).show();
			return;
		}
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				if(pd!=null){
					pd.show();
				}
				
			}
			
		});
		
		if(CHOSE_DEVICE.equals("C")){
			price = txt_crad_pay.getText().toString().trim();
			bankCradParent.transAmt=price;
			creditCardBean =setCreditCardBean(bankCradParent);
		}
		
		
		if(locationInfo!=null&&locationInfo.length()>0){
			creditCardBean.gps=locationInfo;
			//submit();
			goToHanwriting(creditCardBean, msgBean,getIntent().getStringExtra("money"));
		}else{
			Tip("定位失败，请检查网络等，重新进入页面定位");
		}

	}

	
	void submit(){
		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				PinSecurityImpl impl = new PinSecurityImpl();
				String pin="";
				if(CHOSE_DEVICE.equals("A")){
					pin=ed_crad_pass.getText().toString().trim();
				}
				if(CHOSE_DEVICE.equals("B"))
				{
					pin=BaseActivity.BlueToothPsd;
				}
				if(CHOSE_DEVICE.equals("A")||CHOSE_DEVICE.equals("B")||CHOSE_DEVICE.equals("D")){
					if(pin!=null&&pin.length()>=0&&!pin.equals("null")){
						try {
							Log.v("at1",bankCrad.account+"");
							creditCardBean.pin = impl.desSecurity(bankCrad.account,pin, bankCrad.EncWorkingKey);
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
				}
				creditCardBean.lkey = getIntent().getStringExtra("uid");// "1431051319395905056";
				creditCardBean.transAmt = Common.conversionPrice(getIntent().getStringExtra("money"));// "000000005000";
				creditCardBean.credit = getIntent().getStringExtra("cardNo");// "4392268307955824";
			/*	String pin="";
				if(CHOSE_DEVICE.equals("A")){
					pin=ed_crad_pass.getText().toString().trim();
				}else{
					pin=BaseActivity.BlueToothPsd;
				}
				if(pin!=null&&pin.length()>=0&&!pin.equals("null")){
					creditCardBean.pin = impl.desSecurity(bankCrad.account, pin, bankCrad.EncWorkingKey);*/
					creditCardBean.acctNo = ed_crad.getText().toString().trim();
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							if(pg!=null){
								pg.setMessage("正在交易请稍后");
								pg.show();
							}
							
						}
						
					});
					msgBean = controller2.creditCard(creditCardBean);
					BaseActivity.BlueToothPsd="";
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							if(pg!=null){
								pg.dismiss();
							}
							
						}
						
					});
					handleMsg(msgBean);
					
				/*}else{
					Tip("输入密码为空");
				}*/
				
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
				pd.dismiss();
				btn_ok.setFocusable(true);
				btn_ok.setEnabled(true);
				// TODO Auto-generated method stub
				if (msgBean == null) {
					Toast.makeText(getApplicationContext(), "网络不给力", Toast.LENGTH_SHORT).show();
					CradPayActivity.this.finish();
					return;
				}
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
						bundle.putSerializable("msgBean", msgBean);
						intent.putExtra("uid", getIntent().getStringExtra("uid"));
						intent.putExtra("name", "");
						intent.putExtra("merchant", "");
						intent.putExtra("terminal", "");
						intent.putExtra("cardNo", creditCardBean.acctNo);
						intent.putExtra("iss", "");
						intent.putExtra("amount", getIntent().getStringExtra("money"));
						intent.putExtra("ReferNO", msgBean.ReferNO);
						intent.putExtra("TransDate", msgBean.TransDate);
						intent.putExtra("TransTime", msgBean.TransTime);
						intent.putExtra("orderno", msgBean.orderno);
						intent.putExtra("sign_path", sign_path);//待合成的图片
						intent.putExtras(bundle);
						startActivity(intent);
					}
					;
					finish();
				} else {
					Toast.makeText(getApplicationContext(), msgBean.RspMsg, Toast.LENGTH_LONG).show();
				}

			}
		});

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
		if(BaseActivity.BlueToothPsd.equals("null")&&CHOSE_DEVICE.equals("B")){
			myFinish();
			return;
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ed_crad.setText(s);
				if (!Common.checkNetWork(getApplicationContext())) {
					Toast.makeText(getApplicationContext(), "当前没有网络", Toast.LENGTH_LONG).show();
					return;
				}
				new Thread() {
					@Override
					public void run() {
						super.run();
						msgValidation = controller2.isValidation(s);
						if (msgValidation != null) {
							handCradName(msgValidation);
						}else{
							Tip("请检查网络");
						  
						}
					}
				}.start();
			}
		});
	}

	/**
	 * 
	 * handCradName:(刷新银行名字). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param s
	 * @since JDK 1.6
	 */
	private void handCradName(final MsgValidation msgValidation) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (msgValidation == null) {
					Toast.makeText(getApplicationContext(), "卡号不符合规则", Toast.LENGTH_LONG).show();
					return;
				}
				if (Common.equals(msgValidation.cardtype, "借记卡")) {
					bank_crad.setText(msgValidation.name);
					cardHeadDialog.dismiss();
					
					if(CHOSE_DEVICE.equals("B")||CHOSE_DEVICE.equals("C")){
						//submit();
						goToHanwriting(creditCardBean, msgBean,getIntent().getStringExtra("money"));
					}
					
				} else {
					// Toast.makeText(getApplicationContext(), "请刷储蓄卡",
					// Toast.LENGTH_LONG).show();
					cardHeadDialog.txt_title.setText("请刷储蓄卡");
					stuats = 0;
					if(CHOSE_DEVICE.equals("A")||CHOSE_DEVICE.equals("B")){
						operation_stay(Common.SWIPCARD_ME11);
					}
					ed_crad.setText("");
					bank_crad.setText("");
				}
			}
		});
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
			//creditCardBean.trackdatas = bankCrad.trackdatas;
			creditCardBean.pin = bankCrad.pin;
			creditCardBean.tranxSN = bankCrad.tranxSN;
			creditCardBean.ic = bankCrad.ic;
			creditCardBean.cardEXPDate = bankCrad.cardEXPDate;
			creditCardBean.cardSN = bankCrad.cardSN;
			creditCardBean.ksn = bankCrad.ksn;
			creditCardBean.encWorkingKey = bankCrad.EncWorkingKey;
			creditCardBean.acctNoT2=bankCrad.accNoT2;
			creditCardBean.acctNoT3=bankCrad.accNoT3;
			String gps=getIntent().getStringExtra("GPS");
			if(gps!=null){
				creditCardBean.gps=gps;
			}
		}
		return creditCardBean;
	}

	/**
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
					//重新交易
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
					if(CHOSE_DEVICE.equals("A")){
						swiperCard();
					}else{//蓝牙
						swipBlueTooth();
					}
				}
			}).start();
		}
	}
	//蓝牙刷卡流程
	   void swipBlueTooth(){
		   swipShow();
		   String p=getIntent().getStringExtra("money");
		   if(p!=null&&p.length()>0){
			   
		   }else{
			   p="0.0";
		   }
		   SwipResult swipResult = controller.swipCardMe3X("请刷卡/插卡",p, new SimpleTransferListener(), 30000L, TimeUnit.MILLISECONDS,needTime);
			if (swipResult != null) {
				    final String account =swipResult.getAccount().getAcctNo().replaceAll("(?i)F", "");
					Log.e("sean", account);
					bankCrad = new BankCrad();
					bankCrad.ksn = csn;
					bankCrad.account = account;
					bankCrad.pointService = "021";
					/*bankCrad.trackdatas =Dump.getHexDump(swipResult.getTrackDatas()).replaceAll(" ", "");
					Log.v("track2",bankCrad.trackdatas);*/
					if(swipResult.getSecondTrackData()!=null){
						bankCrad.accNoT2=SetTrackData(UnpackTrack(swipResult.getSecondTrackData())).toUpperCase();
					}else{
						cardHeadDialog.dismiss();
						Tip("获取磁道信息失败，请重新刷卡");
						return;
					}
					if(swipResult.getThirdTrackData()!=null){
						bankCrad.accNoT3=SetTrackData(UnpackTrack(swipResult.getThirdTrackData())).toUpperCase();
					}/*else{
						cardHeadDialog.dismiss();
						Tip("获取磁道信息失败，请重新刷卡");
						return;
					}*/
					bankCrad.ic = "0";
					bankCrad.EncWorkingKey = getHex_workkey();
					new Thread(){
						public void run() {
							String p=getIntent().getStringExtra("money");
							controller.startReadingPwd("交易金额为"+p+"元\n请输入密码", new DeviceListener());
							handleBack(account);
							runOnUiThread(new Runnable(){

								@Override
								public void run() {
									cardHeadDialog.dismiss();
									
								}
								
							});
							
							creditCardBean = setCreditCardBean(bankCrad);
						};
					}.start();
					
				  
			}else{
				
			runOnUiThread(new Runnable(){
			
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
		controller.init(CradPayActivity.this, Common.ME11_DRIVER_NAME, params, new DeviceEventListener<ConnectionCloseEvent>() {
			@SuppressLint("NewApi")
			@Override
			public void onEvent(ConnectionCloseEvent event, Handler handler) {
				if (event.isSuccess()) {// 设备被客户主动断开！

				}
				if (event.isFailed()) {// 设备链接异常断开！
					DealDisConnectedMessage(dialogHandler);
					
					Message msg = new Message();
					msg.obj = "设备已经拔出,请插入刷卡器!!!";
					msg.what = connect;
					iHandler.sendMessage(msg);
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
		    csn = deviceInfo.getCSN();
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
			runOnUiThread(new Runnable(){
				public void run() {
					if(CHOSE_DEVICE.equals("A")){
						cardHeadDialog.txt_title.setText("请插入卡头");	
					}else{
						cardHeadDialog.txt_title.setText("正在连接蓝牙设备");	
					}
					
				};
			});
			
			try {
				controller.connect();
			} catch (Exception e) {
				Tip("连接失败，未配对正确设备");
			}
			
			runOnUiThread(new Runnable(){
				public void run() {
					cardHeadDialog.tip_pb.setVisibility(View.VISIBLE);
					cardHeadDialog.txt_title.setText("正在连接设备......");
				};
			});
			exec.scheduleAtFixedRate(new Runnable(){
				
				@Override
				public void run() {
					try {
						BatteryInfoResult bfr=controller.getPowerLevel();
						if(bfr==null&Controller.isEmpty(ed_crad.getText().toString().trim())){
							
							
							runOnUiThread(new Runnable(){

								@Override
								public void run() {
									Toast.makeText(CradPayActivity.this,"连接中断,请稍后重试",Toast.LENGTH_SHORT).show();
									
								}
								
							});
							CradPayActivity.this.finish();
							exec.isShutdown();
							exec=null;
						}
					} catch (Exception e) {
						BatteryInfoResult bfr=controller.getPowerLevel();
						if(bfr==null&Controller.isEmpty(ed_crad.getText().toString().trim())){
							 runOnUiThread(new Runnable(){

									@Override
									public void run() {
										Toast.makeText(CradPayActivity.this,"连接中断,请稍后重试",Toast.LENGTH_SHORT).show();
										
									}
									
								});
							    CradPayActivity.this.finish();
								exec.isShutdown();
								exec=null;
								
						}
						
					}
					/**/
					
				}
				
			}, 30, 300, TimeUnit.MILLISECONDS);
			 Looper.prepare();
			 deviceInfo = controller.getDeviceInfo();
             runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					Message msg = new Message();
					msg.obj = "设备连接成功,正在读取数据,请稍候...";
					iHandler.sendMessage(msg);
					cardHeadDialog.tip_pb.setVisibility(View.VISIBLE);
					
					
				}
			} );
			if (deviceInfo != null) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Message msgs = new Message();
						msgs.obj = "请刷卡或者插卡";
						iHandler.sendMessage(msgs);
						cardHeadDialog.tip_pb.setVisibility(View.GONE);
						
						
					}
				} );
				
				operation_stay(Common.SWIPCARD_ME11);
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
	private void addMessageHandler() {
		iHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == swiper) {// 刷卡过程
					cardHeadDialog.txt_title.setText((String) msg.obj);
				} else if (msg.what == connect) {// 异常断开
					cardHeadDialog.txt_title.setText((String) msg.obj);
				} else if (msg.what == cancel) {// 刷卡取消
					cardHeadDialog.txt_title.setText((String) msg.obj);
				} else if (msg.what == states) {// 刷卡准备
					cardHeadDialog.txt_title.setText((String) msg.obj);
				} else {
					cardHeadDialog.txt_title.setText((String) msg.obj);
				}
			}
		};
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
			ME11SwipResult swipRslt = controller.swipCard("1", 4*30000L, TimeUnit.MILLISECONDS);
			if (swipRslt == null) {
				return;
			} else {
				
				ModuleType[] moduleType = swipRslt.getReadModels();
				if (moduleType[0] == ModuleType.COMMON_ICCARD) {// ic卡插卡
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							cardHeadDialog.txt_title.setText("正在读取卡的信息");
							cardHeadDialog.tip_pb.setVisibility(View.VISIBLE);
							swipHidden();
							//Toast.makeText(TopUpActivity.this,"Ic插卡",Toast.LENGTH_SHORT).show();
							
						}
						
					});
					controller.startEmv(new BigDecimal(getIntent().getStringExtra("money")+""), new SimpleTransferListener());
				} else if (moduleType[0] == ModuleType.COMMON_SWIPER) {// 刷卡
					String kzinfoTrack = Dump.getHexDump(swipRslt.getExtInfo()).replaceAll(" ", "");
					String account = swipRslt.getAccount().getAcctNo().replaceAll("(?i)F", "");
					Log.e("sean", account);
					bankCrad = new BankCrad();
					bankCrad.ksn = csn;
					bankCrad.account = account;
					bankCrad.pointService = "021";
					//bankCrad.trackdatas = kzinfoTrack.substring(16, kzinfoTrack.length());
					bankCrad.accNoT2=kzinfoTrack.substring(16, kzinfoTrack.length());
					bankCrad.accNoT3="";
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
	boolean hasBeanReaded=false;
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
			if(!hasBeanReaded){
				hasBeanReaded=true;
				/*StringBuilder builder = new StringBuilder();
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
				if(CHOSE_DEVICE.equals("D")){
					builder.append(Common.getTLV2Str("9C:" +tag_9C));
				}else{
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
				builder.append(Common.getTLV2Str("9F41:" + tag_9F41));*/
				 List<Integer> L_55TAGS = new ArrayList<Integer>();
					
					L_55TAGS.add(0x9F26);//"9F26:"
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
					//L_55TAGS.add(0x9F63);
							
			      TLVPackage tlvPackage = context.setExternalInfoPackage(L_55TAGS);
			      String y55=ISOUtils.hexString(tlvPackage.pack());
				String cardNo = context.getCardNo();
				String CardSequenceNumber = context.getCardSequenceNumber();
				String kzinfoTrack = Dump.getHexDump(context.getTrack_2_eqv_data()).replaceAll(" ", "");
				bankCrad = new BankCrad();
				bankCrad.ksn = csn;
				//bankCrad.trackdatas = kzinfoTrack.substring(16, kzinfoTrack.length());
				bankCrad.ic = y55;
				//bankCrad.EncWorkingKey = kzinfoTrack.substring(0, 16);
				bankCrad.cardSN = CardSequenceNumber;
				bankCrad.account = cardNo;
				bankCrad.pointService = "051";
				bankCrad.cardEXPDate = context.getCardExpirationDate().substring(0, 4);
				if(CHOSE_DEVICE.equals("A")){//音频
					bankCrad.accNoT2=kzinfoTrack.substring(16, kzinfoTrack.length());
					bankCrad.accNoT3="";
					bankCrad.EncWorkingKey = kzinfoTrack.substring(0, 16);
				}else{//蓝牙
					bankCrad.accNoT2= Dump.getHexDump(context.getTrack_2_eqv_data()).replace("F","").replace(">","").replace("<","").replace("=","");
					bankCrad.accNoT3="";
					bankCrad.EncWorkingKey =getHex_workkey();
				}
				SecondIssuanceRequest request = new SecondIssuanceRequest();
				request.setAuthorisationResponseCode("00");
				arg0.secondIssuance(request);
				// cardHeadDialog.dismiss();
				if(CHOSE_DEVICE.equals("B")){
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							if(CHOSE_DEVICE.equals("B")){
								cardHeadDialog.txt_title.setText("请输入密码");
								cardHeadDialog.tip_pb.setVisibility(View.VISIBLE);
							}
							
							}
					 });
					String p=getIntent().getStringExtra("money");
					controller.startReadingPwd("交易金额为"+p+"元\n请输入密码", new DeviceListener());
				}
				//cardHeadDialog.dismiss();
				creditCardBean = setCreditCardBean(bankCrad);
				handleBack(cardNo);
			}
			
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

	@Override
	public void onDestroy() {
		super.onDestroy();
		controller.destroy(); // 终端后的相关状态处理会通过事件完成,此处不需要处理
	}

	void myFinish(){
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
			closed = true;
			controller.destroy();
			myFinish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private Handler dialogHandle = new Handler() {
		@SuppressLint("NewApi")
		public void handleMessage(Message msg) {
			//Tip(msg.what+"");
			switch (msg.what) {
			case 1://卡头连接成功
				cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("cash2")));
				break;
			case 2:
				cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("cash")));
				break;
			case 3://连接成功
				cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("ly_cash2")));//蓝牙连上
				break;
			case 4:
				cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("ly_cash")));//蓝牙未连
				break;
			case Configure.ME15_DISCONNECTED:
				cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("ly_cash_me15")));//蓝牙未连上
				break;
			case Configure.ME15_CONNECTED:
				cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("ly_cash2_me15")));//蓝牙连
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
