/**
 * Project Name:CardPay
 * File Name:AddCrad.java
 * Package Name:com.apicloud.moduleDemo
 * Date:2015-4-22下午2:27:38
 * Copyright (c) 2015, zhuxiaohao All Rights Reserved.
 *
 */

package com.apicloud.activity;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apicloud.activity.card.AddCardPaymentsActivity;
import com.apicloud.activity.card.CradPayActivity;
import com.apicloud.activity.timely.AddTimelyAccountActivity;
import com.apicloud.activity.topup.NotCardHeadDialog;
import com.apicloud.activity.topup.TopUpTwoActivity;
import com.apicloud.activity.write.HandwritingActivity;
import com.apicloud.common.Common;
import com.apicloud.controller.Controller;
import com.apicloud.controller.DeviceController;
import com.apicloud.controller.TransferListener;
import com.apicloud.impl.DeviceControllerImpl;
import com.apicloud.landy.LandyTackMsg;
import com.apicloud.module.BankBean;
import com.apicloud.module.BankCrad;
import com.apicloud.module.CreditCardBean;
import com.apicloud.module.MsgPush;
import com.apicloud.module.MsgValidation;
import com.apicloud.swip.SwipApi;
import com.apicloud.util.Configure;
import com.apicloud.util.UICommon;
import com.newland.me.DeviceManager.DeviceConnState;
import com.newland.mtype.BatteryInfoResult;
import com.newland.mtype.ConnectionCloseEvent;
import com.newland.mtype.DeviceInfo;
import com.newland.mtype.DeviceRTException;
import com.newland.mtype.ModuleType;
import com.newland.mtype.conn.DeviceConnParams;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.cardreader.CardReader;
import com.newland.mtype.module.common.cardreader.OpenCardReaderEvent;
import com.newland.mtype.module.common.emv.EmvModule;
import com.newland.mtype.module.common.emv.EmvTransController;
import com.newland.mtype.module.common.emv.EmvTransInfo;
import com.newland.mtype.module.common.emv.SecondIssuanceRequest;
import com.newland.mtype.module.common.pin.PinInputEvent;
import com.newland.mtype.module.common.swiper.SwipResult;
import com.newland.mtype.module.common.swiper.SwipResultType;
import com.newland.mtype.module.external.me11.ME11SwipResult;
import com.newland.mtype.util.Dump;
import com.newland.mtypex.audioport.AudioPortV100ConnParams;
import com.umeng.analytics.MobclickAgent;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

/**
 * ClassName:AddCrad <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2015-4-22 下午2:27:38 <br/>
 * 
 * @author zhuxiaohao blog:http://blog.csdn.net/qq718799510?viewmode=contents
 * @version 1.0.0
 * @since JDK 1.6
 * @see 添加银行卡
 */
public class AddCradActivity extends BaseActivity implements OnClickListener {
	ImageButton ib_return;// 返回按钮
	EditText ed_crad;// 卡号
	EditText ed_crad_name;// 持卡人姓名
	TextView txt_crad;// 获取卡号
	TextView bank_crad;// 持卡所属银行
	Button btn_ok;// 确认
	EditText ed_crad_institutions;// 开户行
	String account = "";// 银行卡号

	boolean processing = false;// 时候进行中
	//DeviceController controller = DeviceControllerImpl.getInstance();// 卡头控制器
	boolean closed = false;// 关闭
	int cancel = 2;// 如果取消
	int connect = 4;// 如果连接失败
	int isStand = 0;// 如果出现不支持卡状态
	int swiper = 3;// 刷卡状态
	public static Handler iHandler,resultHanlder,sonHandler;
	
	String price = "0.00";// 金额
	BankCrad bankCrad;// 银行卡刷出信息
	String csn;// 这个又称 KSN
	DeviceInfo deviceInfo;// 设备信息
	
	MsgValidation msgValidation;// 银行名
	
	MsgPush state;// 返回状态
	CreditCardBean creditCardBean;// 请求对象
	ScheduledThreadPoolExecutor exec=new ScheduledThreadPoolExecutor(1);  
	
	/**
	 * 新加的控价
	 */
	RelativeLayout rl_parentbank;
	EditText et_parentbankname;
	Button btn_search;
	EditText et_childBank;
	EditText ed_idcard_num,ed_phone_num;
	String id_card_num;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//controller2 = new Controller(getApplicationContext());
		setContentView(UZResourcesIDFinder.getResLayoutID("add_crad"));
		initView();
		addMessageHandler();// 添加初始化一系列的事件
		initResultHandler();
		if(CHOSE_DEVICE.equals("A")){
			initMe3xDeviceController(new AudioPortV100ConnParams());// 初始化设备
		}
		initSonHandler();
	}

	  
	/**
	 * 
	 * initView:(初始化界面控件). <br/>
	 * 
	 * @author zhuxiaohao
	 * @since JDK 1.6
	 */
	@SuppressLint("NewApi")
	private void initView() {
		ib_return = (ImageButton) findViewById(UZResourcesIDFinder.getResIdID("ib_return"));
		ed_crad = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_crad"));
		txt_crad = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_crad"));
		setGetCardNumTv(txt_crad);
		ed_crad_name = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_crad_name"));
		bank_crad = (TextView) findViewById(UZResourcesIDFinder.getResIdID("bank_crad"));
		btn_ok = (Button) findViewById(UZResourcesIDFinder.getResIdID("btn_ok"));
		ed_crad_institutions = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_crad_institutions"));
		btn_search= (Button) findViewById(UZResourcesIDFinder.getResIdID("btn_search"));
		rl_parentbank=(RelativeLayout) findViewById(UZResourcesIDFinder.getResIdID("rl_parentbank"));
		et_parentbankname=(EditText) findViewById(UZResourcesIDFinder.getResIdID("tv_parentbankname"));
        et_childBank=(EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_search"));
        ed_idcard_num=(EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_idcard_num"));
        ed_phone_num=(EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_phone_num"));
        String card_name=getIntent().getStringExtra("person");
        id_card_num=getIntent().getStringExtra("idnum");
        Log.v("push1", "id-->"+id_card_num);
        if(card_name!=null){
        	ed_crad_name.setText(card_name+"");
        }
        try {
        	 if(id_card_num!=null){
             	//id_card_num="140121199004210611";
             	String t1=id_card_num.substring(0,10);
             	String t2=id_card_num.substring(10,11+3);
             	String t3=id_card_num.substring(id_card_num.length()-4,id_card_num.length());
             	ed_idcard_num.setText(t1+"****"+t3);
             }
		} catch (Exception e) {
			// TODO: handle exception
		}
       
        //判断是卡头还是蓝牙
        String chose_device=getIntent().getStringExtra("posno");
        if(chose_device!=null&&chose_device.length()>0){
        	CHOSE_DEVICE=chose_device;
        	
        }
        Log.v("push1", "chosedevice-->"+CHOSE_DEVICE);
        //CHOSE_DEVICE="B";
        ib_return.setOnClickListener(this);
        if(CHOSE_DEVICE.equals("A")){
        	txt_crad.setOnClickListener(this);
        }
		btn_ok.setOnClickListener(this);
		btn_search.setOnClickListener(this);
		rl_parentbank.setOnClickListener(this);
		ed_crad.addTextChangedListener(textWatcher);
		needShowMoeny=false;
		//初始化父控件
		initParentView();
		initHeadDialog();
		initSwipParam();
		
		}
		void initSwipParam(){
			fromAct=UICommon.AddCardActivity;
			whatDo=SwipApi.WHATDO_GET_CARDNO;
			cur_Ac=this;
		}
		
		public void setCardNo(Object obj){
			//如果选择的是联迪设备
			if(CHOSE_DEVICE.equals(UICommon.WFT_DEVICE)||CHOSE_DEVICE.equals(UICommon.LANDY_DEVICE)||CHOSE_DEVICE.equals(UICommon.AF_DEVICE)||CHOSE_DEVICE.equals(UICommon.CFT_DEVICE)||CHOSE_DEVICE.equals(UICommon.BBPOS_IC_DEVICE)){
				LandyTackMsg landybean=(LandyTackMsg)obj;
				handleBack(landybean.cardNo+"");
			}
		 	
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
		// 输入卡号
		if (v == ed_crad) {

		}
		// 获取卡号
		if (v == txt_crad) {
			
			cardHeadDialog.show();
			operation_stay(Common.FETCH_DEVICE_INFO);
		}
		// 获取持卡人姓名
		if (v == ed_crad_name) {

		}
		// 确定
		if (v == btn_ok) {
			/*MsgPush stuate = new MsgPush();
			stuate.error="0";
			stuate.mainCard="1";
			
			handeOk(stuate);*/
			if (Controller.isEmpty(ed_crad.getText().toString().trim())) {
				Toast.makeText(AddCradActivity.this, "银行卡号不能为空!", Toast.LENGTH_LONG).show();
				return;

			}
			if (Controller.isEmpty(ed_crad_name.getText().toString().trim())) {
				Toast.makeText(AddCradActivity.this, "银行姓名不能为空", Toast.LENGTH_LONG).show();
				return;
			}
			if (Controller.isEmpty(ed_idcard_num.getText().toString().trim())) {
				Toast.makeText(AddCradActivity.this, "身份证号不能为空", Toast.LENGTH_LONG).show();
				return;

			}
			if (Controller.isEmpty(ed_phone_num.getText().toString().trim())) {
				Toast.makeText(AddCradActivity.this, "手机号不能为空", Toast.LENGTH_LONG).show();
				return;

			}
			if(children_bb==null){
				Toast.makeText(AddCradActivity.this, "开户支行不能为空", Toast.LENGTH_LONG).show();
				return;
			}
			doOk();

		}
		//选择主行
		if(v==rl_parentbank){
			if(controller2.checkNetworkAvailable(AddCradActivity.this)){
				Intent it=new Intent(AddCradActivity.this,BankListActivity.class);
				it.putExtra(CHOSEED_SEARCH,CHOSE_PARENT);
				startActivityForResult(it,GET_PARENT_BANK_REQUESTCODE);
			}else{
				Toast.makeText(AddCradActivity.this,"请检查网络", Toast.LENGTH_SHORT).show();
			}
			
		}
		//检索支行
		if(v==btn_search){
			if (Controller.isEmpty(et_parentbankname.getText().toString().trim())) {
				Toast.makeText(AddCradActivity.this, "请先选择主行!", Toast.LENGTH_LONG).show();
				return;

			}
			if (Controller.isEmpty(et_childBank.getText().toString().trim())) {
				Toast.makeText(AddCradActivity.this, "请输入搜索关键词", Toast.LENGTH_LONG).show();
				return;

			}
			if(controller2.checkNetworkAvailable(AddCradActivity.this)){
				BankListAdapter.check_map.clear();
				Intent it=new Intent(AddCradActivity.this,BankListActivity.class);
				it.putExtra(CHOSEED_SEARCH,CHOSE_CHILDREN);
				it.putExtra(PARENT_BANK_CODE, parent_bb.getCode());
				it.putExtra(BRANCH,et_childBank.getText().toString().trim());
				startActivityForResult(it, GET_CHILDREN_BANK_REQUESTCODE);
			}else{
				Toast.makeText(AddCradActivity.this,"请检查网络", Toast.LENGTH_SHORT).show();
			}
			
		}
	}

	public static final String CHOSEED_SEARCH="search";
	public static final String PARENT_BANK_CODE="parent_bank_code";
	public static final String BRANCH="branch";
	public static final String CHOSE_PARENT="parent";
	public static final String CHOSE_CHILDREN="child";
	public static final int GET_PARENT_BANK_REQUESTCODE=100;
	public static final int GET_CHILDREN_BANK_REQUESTCODE=101;
	
	private BankBean parent_bb=null,children_bb=null;
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(data!=null){
			if(requestCode==GET_PARENT_BANK_REQUESTCODE){
				parent_bb=null;
				parent_bb=(BankBean) data.getExtras().getSerializable("bank_data");
				et_parentbankname.setText(parent_bb.getName());
				et_childBank.setText("");
				children_bb=null;
			}else if(requestCode==GET_CHILDREN_BANK_REQUESTCODE){
				children_bb=null;
				children_bb=(BankBean) data.getExtras().getSerializable("bank_data");
				et_childBank.setText(children_bb.getName());
			}
		}
	}
	/**
	 * 
	 * doOk:(添加银行卡). <br/>
	 * 
	 * @author zhuxiaohao
	 * @since JDK 1.6
	 */
	private void doOk() {
		if (!Common.checkNetWork(getApplicationContext())) {
			Toast.makeText(getApplicationContext(), "当前没有网络", Toast.LENGTH_LONG).show();
			return;
		}else{
			if(pg!=null){
				pg.setMessage("处理中，请稍后");
				pg.setCancelable(false);
				pg.show();
			}else{
				pg=new ProgressDialog(AddCradActivity.this);
				pg.setMessage("处理中，请稍后");
				pg.setCancelable(false);
				pg.show();
			}
			
			new Thread() {
				@Override
				public void run() {
					super.run();
					String cardno=ed_crad.getText().toString().trim();
					String person=ed_crad_name.getText().toString().trim();
					String idnum= id_card_num.trim();	
					String phoneNum=ed_phone_num.getText().toString().trim();
					state=controller2.pushCard(AddCradActivity.this, cardno, person,Controller.NOT_CREADIT_CARD, getIntent().getStringExtra("uid"),idnum, phoneNum,null,children_bb.getId()+"",resultHanlder);
					//state = controller2.pushCard(ed_crad.getText().toString().trim(), ed_crad_name.getText().toString().trim(),bank_crad.getText().toString().trim(),  0 + "", getIntent().getStringExtra("uid"),"");
					handeOk(state);
				}
			}.start();
		}
		
	}

	/**
	 * 刷新 UI
	 */
	@SuppressWarnings("unused")
	private Handler handlerMSG = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				ed_crad.setText(account);
				break;
			case 2:
				bank_crad.setText(msgValidation.name);
				break;
			case 3:
				finish();
				break;

			}
		};
	};
	void initResultHandler(){
		resultHanlder=new Handler(){
			
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				if(msg.obj!=null){
				
					/*MsgPush s=(MsgPush) msg.obj;
					//Toast.makeText(AddCradActivity.this,s.error,Toast.LENGTH_SHORT).show();
					if(s.error.equals("0")){
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
						;
						
						finish();
					}*/
				}//
			}
			
		};
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
				if(pg!=null){
					pg.dismiss();
				}
				
				// TODO Auto-generated method stub
				if (s.error.contains("0")) {
					Toast.makeText(AddCradActivity.this, "添加成功", Toast.LENGTH_LONG).show();
					final boolean needResult = getIntent().getBooleanExtra("needResult", false);
					if (needResult) {
						Intent resultData = new Intent();
						JSONObject json = new JSONObject();
						//boolean needCallback2=false;
						try {
							Log.v("push1", "callback ismain0--->"+s.mainCard);
							if(s.mainCard.equals("1")){//是主卡
								Log.v("push1", "callback ismain-->"+s.mainCard);
								json.put("callback", 2);
								//needCallback2=true;
								
							}else{
								json.put("callback", 1);
							}
							
							
						} catch (JSONException e) {
							e.printStackTrace();
						}
						resultData.putExtra("callback", json.toString());
						/*if(needCallback2){
							resultData.putExtra("callback1", "2");
						}*/
						
						Log.v("push1", "callback-->"+json.toString());
						setResult(RESULT_OK, resultData);
					}
					;
					
					finish();
				} else if(s.error.contains("3")){
					Toast.makeText(AddCradActivity.this, "该卡已存在", Toast.LENGTH_SHORT).show();
				}else if(s.error.contains("1")){
					Toast.makeText(AddCradActivity.this, "添加失败-->"+s.RspMsg, Toast.LENGTH_LONG).show();
				}else{
					Toast.makeText(AddCradActivity.this, "添加失败,"+s.RspMsg, Toast.LENGTH_LONG).show();
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
	 * @author zhuxiaohao
	 * @param s
	 * @since JDK 1.6
	 */
	private void handleGetCardName(final String s){
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
					handCradName(msgValidation);
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
	private void handCradName(final MsgValidation s) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (msgValidation == null) {
					Toast.makeText(getApplicationContext(), "该卡不支持", Toast.LENGTH_LONG).show();
					return;
				}
				bank_crad.setText(msgValidation.name);
			}
		});
	}

	void initSonHandler(){
		sonHandler=new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				/*int type=msg.what;
				operation_stay(type);*/
				String content=(String) msg.obj;
				Log.v("push1", "content-->"+content);
				//Tip(content+"");
				//Toast.makeText(AddCardPaymentsActivity.this,bankCradParent.account+"",Toast.LENGTH_SHORT).show();
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
	/**
	 * 
	 * operation_stay:(执行事件). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param type
	 * @since JDK 1.6
	 */
	private void operation_stay(int type) {
		Log.v("push1", "type-->"+type);
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
	
	

	/**
	 * 初始化设备
	 * 
	 * @since ver1.0
	 * @param params
	 *            设备连接参数
	 */
	private void initMe3xDeviceController(DeviceConnParams params) {
		controller.init(AddCradActivity.this, Common.ME11_DRIVER_NAME, params, new DeviceEventListener<ConnectionCloseEvent>() {
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
									Toast.makeText(AddCradActivity.this,"连接中断,请稍后重试",Toast.LENGTH_SHORT).show();
									
								}
								
							});
							AddCradActivity.this.finish();
							exec.isShutdown();
							exec=null;
						}
					} catch (Exception e) {
						
						if(DeviceConnState.CONNECTED != controller.getDeviceConnState()&&Controller.isEmpty(ed_crad.getText().toString().trim())){
							 runOnUiThread(new Runnable(){

									@Override
									public void run() {
										Toast.makeText(AddCradActivity.this,"连接中断,请稍后重试",Toast.LENGTH_SHORT).show();
										
									}
									
								});
							    AddCradActivity.this.finish();
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
					cardHeadDialog.tip_pb.setVisibility(View.VISIBLE);
					
					
				}
			} );
			
			Looper.prepare();
			deviceInfo = controller.getDeviceInfo();
			if (deviceInfo != null) {
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						cardHeadDialog.tip_pb.setVisibility(View.GONE);
						cardHeadDialog.txt_title.setText("请插卡或者刷卡");
						
					}
					
				});
				operation_stay(Common.SWIPCARD_ME11);
				DealConnectedMessage(dialogHandle);
				
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			//connectDevice();
		}
	}

	/**
	 * f\
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
			ME11SwipResult swipRslt =null;
			swipRslt=controller.swipCard("", 30000L, TimeUnit.MILLISECONDS);
			
			  if (swipRslt == null) {
				  runOnUiThread(new Runnable(){

						@Override
						public void run() {
							Toast.makeText(AddCradActivity.this, "swip为空", Toast.LENGTH_SHORT).show();
							}
						});
				return;
			} else {
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						cardHeadDialog.txt_title.setText("正在读取卡的信息");
						cardHeadDialog.tip_pb.setVisibility(View.VISIBLE);
						swipHidden();
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
			cardHeadDialog.tip_pb.setVisibility(View.GONE);
		}

		@Override
		public void onRequestPinEntry(EmvTransController arg0, EmvTransInfo arg1) throws Exception {
			Message msg = new Message();
			msg.obj = "错误的事件返回，不可能要求密码输入";
			iHandler.sendMessage(msg);
			arg0.cancelEmv();
			cardHeadDialog.dismiss();
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
			if (ed_crad.length() >=16) {
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
