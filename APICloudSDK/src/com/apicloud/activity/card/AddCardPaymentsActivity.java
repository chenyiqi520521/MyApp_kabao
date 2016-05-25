/**
 * Project Name:CardPay
 * File Name:CardPayments.java
 * Package Name:com.apicloud.activity
 * Date:2015-4-23下午2:51:31
 * Copyright (c) 2015, zhuxiaohao All Rights Reserved.
 *
 */

package com.apicloud.activity.card;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.apicloud.activity.AddCradActivity;
import com.apicloud.activity.BaseActivity;
import com.apicloud.activity.topup.NotCardHeadDialog;
import com.apicloud.activity.topup.TopUpTwoActivity;
import com.apicloud.common.Common;
import com.apicloud.controller.Controller;
import com.apicloud.controller.DeviceController;
import com.apicloud.controller.TransferListener;
import com.apicloud.impl.DeviceControllerImpl;
import com.apicloud.landy.LandyTackMsg;
import com.apicloud.module.BankCrad;
import com.apicloud.module.MsgPush;
import com.apicloud.module.MsgValidation;
import com.apicloud.swip.SwipApi;
import com.apicloud.util.Configure;
import com.apicloud.util.UICommon;
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
 * ClassName:CardPayments <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2015-4-23 下午2:51:31 <br/>
 * 
 * @author zhuxiaohao blog:http://blog.csdn.net/qq718799510?viewmode=contents
 * @version 1.0.0
 * @since JDK 1.6
 * @see 添加信用卡
 */
public class AddCardPaymentsActivity extends BaseActivity implements OnClickListener {
	ImageButton ib_return;// 返回
	EditText ed_crad;// 卡号
	EditText ed_crad_name;// 持卡姓名
	CheckBox cbx;// 业务规则
	TextView txt_crad;// 获取卡号
	TextView bank_crad;// 银行标签
	Button btn_ok;// 确定
	boolean b = false;// 时候阅读业务规则
	int ix = -1;// 业务规则是否同意
	TextView txt;// 业务规则

	boolean processing = false;// 时候进行中
	
	boolean closed = false;// 关闭
	int cancel = 2;// 如果取消
	int connect = 4;// 如果连接失败
	int isStand = 0;// 如果出现不支持卡状态
	int swiper = 3;// 刷卡状态
	Handler iHandler, resultHandler,sonHandler;
	String price = "0.00";// 金额
	BankCrad bankCrad;// 银行卡刷出信息
	String csn;// 这个又称 KSN
	DeviceInfo deviceInfo;// 设备信息
	Controller controller2;// 控制器
	MsgValidation msgValidation;// 银行名
	
	MsgPush state;// 返回状态
	ScheduledThreadPoolExecutor exec=new ScheduledThreadPoolExecutor(1);
	boolean cardNoChecked=false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("card_payments"));
		initView();
		initHanlder();
		if(CHOSE_DEVICE.equals("A")){
			initMe3xDeviceController(new AudioPortV100ConnParams());// 初始化设备
		}
		
		//operation_stay(Common.FETCH_DEVICE_INFO);
		addMessageHandler();// 添加初始化一系列的事件
		controller2 = new Controller(AddCardPaymentsActivity.this);

		initSonHandler();

	}
	void initSonHandler(){
		sonHandler=new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String content=(String) msg.obj;
				//Tip(content+"");
				if(content!=null&&content.equals("sure")&&(CHOSE_DEVICE.equals("B")||CHOSE_DEVICE.equals("C"))){//点击确定
					ed_crad.setText(bankCradParent.account.replaceAll(".{4}(?!$)", "$0 "));
					
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
	@SuppressLint("NewApi")
	private void initView() {
		
		// TODO Auto-generated method stub
		ib_return = (ImageButton) findViewById(UZResourcesIDFinder.getResIdID("ib_return"));
		ed_crad = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_crad"));
		ed_crad_name = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_crad_name"));
		cbx = (CheckBox) findViewById(UZResourcesIDFinder.getResIdID("cbx"));
		txt_crad = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_crad"));
		bank_crad = (TextView) findViewById(UZResourcesIDFinder.getResIdID("bank_crad"));
		btn_ok = (Button) findViewById(UZResourcesIDFinder.getResIdID("btn_ok"));
		txt = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt"));
		ib_return.setOnClickListener(this);
		setGetCardNumTv(txt_crad);
		//判断是卡头还是蓝牙
        String chose_device=getIntent().getStringExtra("posno");
        if(chose_device!=null&&chose_device.length()>0){
        	CHOSE_DEVICE=chose_device;
        	
        }
        if(CHOSE_DEVICE.equals("A")){
        	txt_crad.setOnClickListener(this);
        }
		
        //CHOSE_DEVICE="C";
		btn_ok.setOnClickListener(this);
		txt.setOnClickListener(this);
		cbx.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					b = isChecked;
				}else{
					b = isChecked;
				}

			}
		});
		ed_crad.addTextChangedListener(textWatcher);
		needShowMoeny=false;
		initParentView();
		initHeadDialog();

		initSwipParam();

	}
	void initSwipParam(){
		fromAct=UICommon.AddCardPaymentActivity;
		whatDo=SwipApi.WHATDO_GET_CARDNO;
		cur_Ac=this;
	}
	public void setCradNo(Object obj){
		if(CHOSE_DEVICE.equals(UICommon.WFT_DEVICE)||CHOSE_DEVICE.equals(UICommon.LANDY_DEVICE)||CHOSE_DEVICE.equals(UICommon.AF_DEVICE)||CHOSE_DEVICE.equals(UICommon.CFT_DEVICE)||CHOSE_DEVICE.equals(UICommon.BBPOS_IC_DEVICE)){
			LandyTackMsg landybean=(LandyTackMsg)obj;
			handleBack(landybean.cardNo+"");
		}
		
	}
	void initHanlder(){
		  resultHandler=new Handler(){
		    @Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				// TODO Auto-generated method stub
				if(msg.obj!=null){
					MsgPush s=(MsgPush) msg.obj;
					if (s.error.contains("0")) {
						Toast.makeText(AddCardPaymentsActivity.this, "添加成功", Toast.LENGTH_LONG).show();
						boolean needResult = getIntent().getBooleanExtra("needResult", false);
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
						};
						finish();
					} else if(s.error.contains("3")){
						Toast.makeText(AddCardPaymentsActivity.this, "该卡已存在", Toast.LENGTH_SHORT).show();
					}else  if(s.error.contains("1")){
						Toast.makeText(AddCardPaymentsActivity.this, "添加失败", Toast.LENGTH_LONG).show();
					}else{
						Tip(s.error);
					}
				}
				
			}
			
		};
	}
	@SuppressWarnings("static-access")
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
		// 业务规则跳转
		if (v == txt) {
			startActivity(new Intent(AddCardPaymentsActivity.this, CardPaymentsRules.class));
		}
		// 获取卡号
		if (v == txt_crad) {
			cardHeadDialog = new NotCardHeadDialog(AddCardPaymentsActivity.this,CHOSE_DEVICE);
			cardHeadDialog.show();
			operation_stay(Common.FETCH_DEVICE_INFO);
		}
		// 确定
		if (v == btn_ok) {
			if (Controller.isEmpty(ed_crad.getText().toString().trim())) {
				Toast.makeText(AddCardPaymentsActivity.this, "卡号不能为空", Toast.LENGTH_LONG).show();
				return;
			}
			if (Controller.isEmpty(ed_crad_name.getText().toString().trim())) {
				Toast.makeText(AddCardPaymentsActivity.this, "持卡人姓名不能为空", Toast.LENGTH_LONG).show();
				return;
			}
			if (b == false) {
				Toast.makeText(AddCardPaymentsActivity.this, "还款业务规则必须同意", Toast.LENGTH_LONG).show();
				return;
			}
			
			if(!cardNoChecked){
				pg.setTitle("请稍后");
				pg.show();
				final String cardno=ed_crad.getText().toString().trim().replace(" ","");
				new Thread() {
					@Override
					public void run() {
						super.run();
						msgValidation = controller2.isValidation(cardno);
						if(msgValidation!=null){
							handCradName(msgValidation,true);
						}else{
							Tip("请检查网络");
						}
						
					}
				}.start();
			}else{
				doOk();
			}
			
			
			

		}

	}

	/**
	 * 
	 * doOk:(请求). <br/>
	 * 
	 * @author zhuxiaohao
	 * @since JDK 1.6
	 */
	private void doOk() {
		if (!Common.checkNetWork(getApplicationContext())) {
			Toast.makeText(getApplicationContext(), "当前没有网络", Toast.LENGTH_LONG).show();
			return;
		}
		new Thread() {
			public void run() {
				state=controller2.pushCard(AddCardPaymentsActivity.this, ed_crad.getText().toString().trim(), ed_crad_name.getText().toString().trim(),Controller.COMMON_ADD_CREADIT_CARD, getIntent().getStringExtra("uid"),null,null,null,null,resultHandler);
				//state = controller2.pushCard(ed_crad.getText().toString().trim(),ed_crad_name.getText().toString().trim(), bank_crad.getText().toString().trim(),  0 + "", getIntent().getStringExtra("uid"),"");
				//handeOk(state);
			};
		}.start();
	}

	/**
	 * 
	 * handeOk:( 验证返回结果). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param s
	 * @since JDK 1.6
	 */
	private void handeOk(final MsgPush s) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (state.error.contains("0")) {
					Toast.makeText(AddCardPaymentsActivity.this, "添加成功", Toast.LENGTH_LONG).show();
					boolean needResult = getIntent().getBooleanExtra("needResult", false);
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
					};
					finish();
				} else if(state.error.contains("3")){
					Toast.makeText(AddCardPaymentsActivity.this, "该卡已存在", Toast.LENGTH_SHORT).show();
				}else  if(state.error.contains("1")){
					Toast.makeText(AddCardPaymentsActivity.this, "添加失败", Toast.LENGTH_LONG).show();
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
				if(msgValidation!=null){
					handCradName(msgValidation,false);
				}else{
					Tip("请检查网络");
				}
				
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
	private void handCradName(final MsgValidation msgValidation,final boolean isDoOk) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (msgValidation == null) {
					Toast.makeText(getApplicationContext(), "该卡不支持", Toast.LENGTH_LONG).show();
					// ed_crad.setText("");
					return;
				}
				cardNoChecked=true;
				bank_crad.setText(msgValidation.name);
				if(isDoOk){
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
					//重新交易
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
		   final SwipResult swipResult = controller.swipCardMe3X("请刷卡/插卡", "0.0"
					, new SimpleTransferListener(), 30000L, TimeUnit.MILLISECONDS,needTime);
			if (swipResult != null) {
				byte[] secondTrack = swipResult.getSecondTrackData();
				byte[] thirdTrack = swipResult.getThirdTrackData();
				String account=swipResult.getAccount().getAcctNo();
				Log.v("info", "ino-->"+account+"");
				Log.v("info", "ino-->"+Dump.getHexDump(secondTrack)+"");
				handleBack(account);
				cardHeadDialog.dismiss();
				
				
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
		controller.init(AddCardPaymentsActivity.this, Common.ME11_DRIVER_NAME, params, new DeviceEventListener<ConnectionCloseEvent>() {
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
					
					initMe3xDeviceController(new AudioPortV100ConnParams());
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
			
			csn = deviceInfo.getCSN()+"";
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
						
						if(DeviceConnState.CONNECTED != controller.getDeviceConnState()&&Controller.isEmpty(ed_crad.getText().toString().trim())){
							
							
							runOnUiThread(new Runnable(){

								@Override
								public void run() {
									Toast.makeText(AddCardPaymentsActivity.this,"连接中断,请稍后重试",Toast.LENGTH_SHORT).show();
									
								}
								
							});
							AddCardPaymentsActivity.this.finish();
							exec.isShutdown();
							exec=null;
						}
					} catch (Exception e) {
						
						if(DeviceConnState.CONNECTED != controller.getDeviceConnState()&&Controller.isEmpty(ed_crad.getText().toString().trim())){
							 runOnUiThread(new Runnable(){

									@Override
									public void run() {
										Toast.makeText(AddCardPaymentsActivity.this,"连接中断,请稍后重试",Toast.LENGTH_SHORT).show();
										
									}
									
								});
								AddCardPaymentsActivity.this.finish();
								exec.isShutdown();
								exec=null;
								
						}
						
					}
					/**/
					
				}
				
			}, 30, 300, TimeUnit.MILLISECONDS);
             runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					Message msg = new Message();
					msg.obj = "设备连接成功,正在读取数据,请稍候...";
					iHandler.sendMessage(msg);
					cardHeadDialog.tip_pb.setVisibility(View.GONE);
					
					
				}
			} );
			deviceInfo = controller.getDeviceInfo();
			if (deviceInfo != null) {
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						
						Message msgs = new Message();
						msgs.obj = "请刷卡或者插卡";
						iHandler.sendMessage(msgs);
						Toast.makeText(AddCardPaymentsActivity.this,"请刷卡或者插卡",Toast.LENGTH_SHORT).show();
						DealConnectedMessage(dialogHandler);
						
					}
					
				});
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
			Message msg = new Message();
			msg.what = swiper;
			msg.obj = "请刷卡或者插卡";
			iHandler.sendMessage(msg);
			ME11SwipResult swipRslt = controller.swipCard("", 30000L, TimeUnit.MILLISECONDS);
			if (swipRslt == null) {
				return;
			} else {
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						cardHeadDialog.txt_title.setText("正在读取卡的信息");
						cardHeadDialog.tip_pb.setVisibility(View.VISIBLE);
						swipHidden();
						//Toast.makeText(TopUpActivity.this,"Ic插卡",Toast.LENGTH_SHORT).show();
						
					}
					
				});
				ModuleType[] moduleType = swipRslt.getReadModels();
				if (moduleType[0] == ModuleType.COMMON_ICCARD) {// ic卡插卡
					controller.startEmv(new BigDecimal(price), new SimpleTransferListener());
				} else if (moduleType[0] == ModuleType.COMMON_SWIPER) {// 刷卡
					String kzinfoTrack = Dump.getHexDump(swipRslt.getExtInfo()).replaceAll(" ", "");
					String account = swipRslt.getAccount().getAcctNo().replaceAll("(?i)F", "");
					Log.i("account", account);
					bankCrad = new BankCrad();
					bankCrad.ksn = csn;
					bankCrad.account = account;
					bankCrad.pointService = "021";
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
			cardHeadDialog.dismiss();
			handleBack(cardNo);
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
