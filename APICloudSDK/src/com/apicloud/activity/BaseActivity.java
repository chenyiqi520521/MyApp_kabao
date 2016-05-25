/**   
 * Copyright © 2015 公司名. All rights reserved.
 * 
 * @Title: BaseActivity.java 
 * @Prject: APICloudSDK
 * @Package: com.apicloud.activity 
 * @Description: TODO
 * @author: user20   
 * @date: 2015年8月11日 上午10:23:58 
 * @version: V1.0   
 */
package com.apicloud.activity;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import com.apicloud.activity.card.AddCardPaymentsActivity;
import com.apicloud.activity.card.CradPayActivity;
import com.apicloud.activity.telphone.TelPhoneActivity;
import com.apicloud.activity.timely.AddTimelyAccountActivity;
import com.apicloud.activity.timely.TimelyAccountTopUpActivity;
import com.apicloud.activity.topup.NotCardHeadDialog;
import com.apicloud.activity.topup.TopUpActivity;
import com.apicloud.activity.topup.TopUpTwoActivity;
import com.apicloud.activity.write.HandwritingActivity;
import com.apicloud.activity.write.WritePadActivity;
import com.apicloud.common.Common;
import com.apicloud.controller.Controller;
import com.apicloud.controller.DHDeviceController;
import com.apicloud.controller.DeviceController;
import com.apicloud.impl.DHDeviceControllerImpl;
import com.apicloud.impl.DHListener;
import com.apicloud.impl.DeviceControllerImpl;
import com.apicloud.landy.LandyTackMsg;
import com.apicloud.module.BankCrad;
import com.apicloud.module.BluetoothDeviceContext;
import com.apicloud.module.CreditCardBean;
import com.apicloud.module.MsgBean;
import com.apicloud.swip.BlueToothSearchActivity;
import com.apicloud.swip.SwipActivity;
import com.apicloud.swip.SwipApi;
import com.apicloud.util.Configure;
import com.apicloud.util.Constant;
import com.apicloud.util.UICommon;
import com.apicloud.util.commonUtil;
import com.dspread.xpos.QPOSService.CommunicationMode;
import com.dspread.xpos.QPOSService.EmvOption;
import com.dspread.xpos.QPOSService.TransactionType;
import com.location.LocationSvc;
import com.newland.me.DeviceManager.DeviceConnState;
import com.newland.mtype.ConnectionCloseEvent;
import com.newland.mtype.DeviceInfo;
import com.newland.mtype.conn.DeviceConnParams;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtypex.bluetooth.BlueToothV100ConnParams;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnDismissListener;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/** 
 * @ClassName: BaseActivity 
 * @Description: TODO
 * @author: user20
 * @date: 2015年8月11日 上午10:23:58  
 */
public class BaseActivity extends Activity {
	private static final String ME3X_DRIVER_NAME = "com.newland.me.ME3xDriver";
	private static final String ME11_DRIVER_NAME = "com.newland.me.ME11Driver";
	private static final String K21_DRIVER_NAME = "com.newland.me.K21Driver";
	public  List<BluetoothDeviceContext> discoveredDevices = new ArrayList<BluetoothDeviceContext>();
	public BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	protected DeviceController controller = DeviceControllerImpl.getInstance();
	//protected DHDeviceController dhcontroller=DHDeviceControllerImpl.getInstance(CommunicationMode.BLUETOOTH_2Mode);
	protected DHDeviceController dhcontroller=null;
	protected Controller controller2;// 控制器
    protected TextView tv_getcard_num;//获取卡号
    protected ProgressDialog pg,pg1,pg_init,pg_getdata;//蓝牙搜索进度框
    protected String deviceToConnect;
    
    public static String BlueToothPsd="";//蓝牙时获取输入的密码
   public EditText et_code_parent;
    
   protected boolean isBindPage=false;//是否是设备绑定页面
   protected String bind_lkey="";
   protected String bind_shopno="";
   protected BindDeviceActivity bindAc=null;
	/**
	 * 默认蓝牙接受处理器
	 */
    public  String CHOSE_DEVICE="E";//选择连接的设别类型  A表示卡头，B表示新大陆有键盘蓝牙，C表示鼎和蓝牙,D表示新大陆蓝牙无键盘
    protected Handler dialogHandler=null,parentHandler=null,dhParentHandler=null,pgHandler=null,blueToothHandler=null;
    protected NotCardHeadDialog cardHeadDialog;// 检测卡头的对话框
    DHListener dhlistener;
    protected BankCrad bankCradParent;// 银行卡刷出信息
    protected TextView amount_et=null;//输入钱的
    protected boolean needTime=false;
    
  
    @SuppressLint("NewApi")
	protected void initHeadDialog(){
    	if(CHOSE_DEVICE.equals("B")){
	    	  cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("ly_cash")));
	      }
		 
    	if(CHOSE_DEVICE.equals("C")){
    		 cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("ly_cash_dh")));
    	}
		 if(CHOSE_DEVICE.equals("D")){
			 cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("ly_cash_me15")));
		 }
    }
    protected void DealDisConnectedMessage(Handler handler){
    	
    	if(CHOSE_DEVICE.equals("A")){
    		handler.sendEmptyMessage(Configure.KT_DISCONNECTED);
    	}else if(CHOSE_DEVICE.equals("B")){
    		handler.sendEmptyMessage(Configure.LY_DISCONNECTED);
    	}else if(CHOSE_DEVICE.equals("C")){
    		handler.sendEmptyMessage(Configure.DH_DISCONNECTED);
    	}
    	else if(CHOSE_DEVICE.equals("D")){
    		handler.sendEmptyMessage(Configure.ME15_DISCONNECTED);
    	}
    	
    }
    
    protected void DealConnectedMessage(Handler handler){
        if(CHOSE_DEVICE.equals("A")){
    		handler.sendEmptyMessage(Configure.KT_CONNECTED);
    	}else if(CHOSE_DEVICE.equals("B")){
    		handler.sendEmptyMessage(Configure.LY_CONNECTED);
    	}else if(CHOSE_DEVICE.equals("C")){
    		handler.sendEmptyMessage(Configure.DH_CONNECTED);
    	}
    	else if(CHOSE_DEVICE.equals("D")){
    		handler.sendEmptyMessage(Configure.ME15_CONNECTED);
    	}
    }
	private final BroadcastReceiver discoveryReciever = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {

				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (ifAddressExist(device.getAddress())) {
					return;
				}

				BluetoothDeviceContext btContext = new BluetoothDeviceContext(device.getName() == null ? device.getAddress() : device.getName(), device.getAddress());
				discoveredDevices.add(btContext);
				

			}
		}
	};
	protected String locationInfo="30.290388-120.134746";
	private class LocationBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(com.location.Common1.LOCATION_ACTION)){ 
				locationInfo = intent.getStringExtra(com.location.Common1.LOCATION);
			    //Log.v("loc1",locationInfo+"");
			 }
				
			    
		}
	}
	
	public void setEtCodeParent(EditText etCode,String lkey){
		et_code_parent=etCode;
		lkey_parent=lkey;
	}
	protected void startLocation(){
		Intent intent = new Intent();
		intent.setClass(this, LocationSvc.class);
		startService(intent);
	}
	private final int READY_TO_CONNECT=100;//正在连接设备
	private final int CONNECT_SUCCESS=101;//连接成功
	private final int CONNECT_FAIL=102;//连接失败
	private final int GET_DEVICE_INFO=103;//获取设备信息
	private final int GET_DEVICE_INFO_SUCCESS=104;//获取设备信息成功
	private final int SWIP_CARD=105;//请刷卡
	private final int COMPLETETD=106;//刷卡完成
	@SuppressLint("NewApi")
	void dialogTip(int type){
		Message msg=dialogHandler.obtainMessage();
	     switch (type) {
		 case READY_TO_CONNECT:
				msg.obj="正在连接设备";
	     break;
		 case CONNECT_SUCCESS:
			 
			 if(CHOSE_DEVICE.equals("A")){
				 cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("cash2")));
			 }
			 if(CHOSE_DEVICE.equals("B")){
		    	  cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("ly_cash2")));
		      }
			 
	    	 if(CHOSE_DEVICE.equals("C")){
	    		 cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("ly_cash2_dh")));
	    	 }
			 if(CHOSE_DEVICE.equals("D")){
				 cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("ly_cash2_me15")));
			 }
				msg.obj="连接成功";
	     break;
		 case CONNECT_FAIL:
				msg.obj="连接失败";
		 break;
		 case GET_DEVICE_INFO:
			 
				msg.obj="正在读取设备信息";
		 break;
		 case GET_DEVICE_INFO_SUCCESS:
				msg.obj="获取成功";
		 break;
		 case SWIP_CARD:
			    swipShow();
				msg.obj="请刷卡或插卡";
		 break;
		 case COMPLETETD:
				//msg.obj="请刷卡或插卡";
			 if(cardHeadDialog!=null){
				 try {
					cardHeadDialog.dismiss();
				} catch (Exception e) {
					// TODO: handle exception
				}
			 }
		 break;

		}
	     dialogHandler.sendMessage(msg);
	}
	
   protected boolean needShowMoeny=false;
	void initDHparentHandler(){
		dhParentHandler=new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				if(msg.what==DHListener.CONNECT_SUCCESS){
					dialogTip(CONNECT_SUCCESS);
				
					dhcontroller.checkConnected();//获取posid
					dialogTip(GET_DEVICE_INFO);
					
				}	else if(msg.what==DHListener.CONNECT_FAIL){//连接失败
					 try {
						 cardHeadDialog.show();
						 startDiscovery(CHOSE_DEVICE);
					} catch (Exception e) {
						// TODO: handle exception
					}
					
					
				}
				else if(msg.what==DHListener.GET_POSID){
					String postId=(String) msg.obj;
					bankCradParent=new BankCrad();
					if(postId!=null&&postId.length()>0){//连接成功
						bankCradParent.ksn=postId;
						//判断是不是绑定设备页面
						if(isBindPage){
							Intent intent=new Intent(BaseActivity.this,BindDeviceFinalActivity.class);
							intent.putExtra("device_num", postId+"");
							intent.putExtra("lkey",bind_lkey+"");
							intent.putExtra("shopno", bind_shopno+"");
							intent.putExtra("chose_device",CHOSE_DEVICE+"");
							startActivityForResult(intent,BindDeviceActivity.REQUEST_FOR_BIND);
							return;
						}
						dhcontroller.UpdateWorkKey();
						}else{//没有获取到，重新连接
						blueToothSearchAndConnected(CHOSE_DEVICE);
					}
				}else if(msg.what==DHListener.UPDATE_WOEK_KEY_SUCCESS){
					dialogTip(GET_DEVICE_INFO_SUCCESS);
					//刷卡
					dialogTip(SWIP_CARD);
					
					if(needShowMoeny){
						
						dhcontroller.doTrade(30);
					 }else{
						dhcontroller.getCardNo();
					}
				
				}else if(msg.what==DHListener.GET_CARD_NO_SUCCESS){
					String cardNo=(String) msg.obj;
					//Tip(cardNo);
					dialogTip(COMPLETETD);//完毕
					dhcontroller.disConnected();
					bankCradParent.account=cardNo;//卡号
					//签名
					Message msg1=parentHandler.obtainMessage();
					msg1.obj="sure";
					parentHandler.sendMessage(msg1);
				}
				else if(msg.what==DHListener.CANCLE_TRANSACTIOPN){
					
					try {
						cardHeadDialog.dismiss();
						Tip("您退出了交易");
						new Thread(){
							public void run() {
								Instrumentation inst = new Instrumentation();
								inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
							};
						}.start();
					} catch (Exception e) {
						// TODO: handle exception
					}
					Tip("取消交易");
				}
				else if(msg.what==DHListener.SET_AMOUNT){//设置输入金额
					
					 if(needShowMoeny){
						 if(amount_et!=null){
							 try {
								 String money=amount_et.getText().toString().trim();
								  if(money.contains("¥")){
									  money=money.replace("¥", "");
								  }
								 //Tip(money);
								 int trac_moent=Integer.parseInt(money);
								 trac_moent=trac_moent*100;
								 if(trac_moent==0){
									 dhcontroller.setAmount("", "","156", TransactionType.INQUIRY);
								 }else{
									 dhcontroller.setAmount(trac_moent+"", "","156", TransactionType.GOODS);
								 }

								 
							} catch (Exception e) {
								Tip("输入金额无效");
							}
							 
						 }
						 
					 }else{
						 dhcontroller.setAmount("", "","156", TransactionType.INQUIRY);
					 }
					
					//
				}else if(msg.what==DHListener.IC_DO_EMV){
					 
					dhcontroller.doEmv(EmvOption.START);
				}else if(msg.what==DHListener.SEND_TIME){
					String time=(String) msg.obj;
					if(time!=null&&time.length()>0){
						dhcontroller.setTime(time);
						//Toast.makeText(BaseActivity.this, time, Toast.LENGTH_SHORT).show();
					}
					
				}else if(msg.what==DHListener.IS_SERVER_CONNECTED){
					dhcontroller.isServerConnected(true);
				}
				else if(msg.what==DHListener.GET_IC_INFO){//IC卡获取成功
					swipHidden();
					String str55=(String) msg.obj;
					if(str55!=null){
						Hashtable<String, String> decodeData=dhcontroller.getICInfo(str55);
						Enumeration<String> e=decodeData.keys();
						while(e.hasMoreElements()){
							String key=(String) e.nextElement();
							String value=decodeData.get(key);
							Log.v("up1", "key->"+key+"--value->"+value);
							
						}
						
						bankCradParent.pointService = "051";
						String encTack2=decodeData.get("encTrack2");
						if(encTack2!=null&&encTack2.length()>0){
							bankCradParent.accNoT2=encTack2;
						}
						
						String encTrack3=decodeData.get("encTrack3");
						if(encTrack3!=null&&encTrack3.length()>0){
							bankCradParent.accNoT3=encTrack3;
						}
						bankCradParent.cardEXPDate=decodeData.get("iccCardAppexpiryDate").substring(0,4);
						Log.v("up2",bankCradParent.cardEXPDate);
						bankCradParent.account=decodeData.get("maskedPAN");//卡号
						bankCradParent.ic = decodeData.get("iccdata");
						bankCradParent.pin=decodeData.get("pinBlock");
						bankCradParent.EncWorkingKey="0";
						bankCradParent.cardSN=decodeData.get("cardSquNo");//卡片序列号
					}
					dialogTip(COMPLETETD);//完毕
					dhcontroller.disConnected();
					//Tip(bankCradParent.account+"");
					Message msg1=parentHandler.obtainMessage();
					msg1.obj="sure";
					parentHandler.sendMessage(msg1);
					//Tip(1+"");
					
				}
				else if(msg.what==DHListener.GET_CT_SUCCESS){//获得磁条卡信息成功
					swipHidden();
					Hashtable<String, String> decodeData=(Hashtable<String, String>) msg.obj;
					if(decodeData!=null){
						bankCradParent.pointService = "021";
						String encTack2=decodeData.get("encTrack2");
						if(encTack2!=null&&encTack2.length()>0){
							bankCradParent.accNoT2=encTack2;
						}
						
						String encTrack3=decodeData.get("encTrack3");
						if(encTrack3!=null&&encTrack3.length()>0){
							bankCradParent.accNoT3=encTrack3;
						}
						bankCradParent.cardEXPDate=decodeData.get("expiryDate");
						bankCradParent.account=decodeData.get("maskedPAN");//卡号
						bankCradParent.ic = "0";
						bankCradParent.pin=decodeData.get("pinBlock");
						bankCradParent.EncWorkingKey="0";
						
						
						
					}
					dialogTip(COMPLETETD);//完毕
					dhcontroller.disConnected();
					
					//签名
					Message msg1=parentHandler.obtainMessage();
					msg1.obj="sure";
					parentHandler.sendMessage(msg1);
					
					
				}
			}
			
		};
	}
	/**
	 * 处理对话框信息提示
	 * @Title: initDialogHandler 
	 * @Description: TODO
	 * @return: void
	 */
	void initDialogHandler(){
		dialogHandler=new Handler(){
			
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String tip=(String) msg.obj;
				if(tip!=null&&tip.length()>0){
					cardHeadDialog.txt_title.setText(tip);
				}
				
			}
		};
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if(!commonUtil.isGpsEnable(BaseActivity.this)){
			Tip("请开启GPS");
			commonUtil.setOpenGPS(BaseActivity.this);
			finish();
		}
		controller2 = new Controller(getApplicationContext());
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		//DHDeviceController 
		dhcontroller=new DHDeviceControllerImpl(CommunicationMode.BLUETOOTH_2Mode);
		dhlistener=new DHListener();
		dhcontroller.init(getApplicationContext(), CommunicationMode.BLUETOOTH_2Mode,dhlistener);
		bankCradParent=new BankCrad();
		registerReceiver(discoveryReciever, filter);
		IntentFilter filter1 = new IntentFilter();
		filter1.addAction(com.location.Common1.LOCATION_ACTION);
		this.registerReceiver(new LocationBroadcastReceiver(), filter1);
		
		
		initDialogHandler();
		initDHparentHandler();
		
		
		
		
		
	}
	
	void initBlueToothHandler(){
		blueToothHandler=new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				if(msg.what==NotCardHeadDialog.RESTART_SEARCH_BLUETOOTH){
					startDiscovery(CHOSE_DEVICE);
				}
			}
		};
	}
	
	/* (non Javadoc) 
	 * @Title: onStop
	 * @Description: TODO 
	 * @see android.app.Activity#onStop() 
	 */
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		//Log.v("dhs", "stop");
		//dhcontroller.destory();
		//dhcontroller=null;
	}
	/* (non Javadoc) 
	 * @Title: onResume
	 * @Description: TODO 
	 * @see android.app.Activity#onResume() 
	 */
	
	void blueToothSearchAndConnected(String type){
		// Tip("show1");
		 if(cardHeadDialog!=null){
				cardHeadDialog.show();
		}
		 final String address=BaseActivity.this.getSharedPreferences(KALAI_COMMON_SAVE,0).getString(CHOSED_BLUETOOTH_ADDRESS,"");
		 connected=false;
		 //Tip("show1");
		
		 //Tip("show2");
		 if(address.length()>0){
			 
			 try {
				if(type.equals("C")){
					dhcontroller.connect(address);
				    dialogTip(READY_TO_CONNECT);
				    if(cardHeadDialog!=null){
						cardHeadDialog.show();
					}
				}
				
				if(type.equals("B")||type.equals("D")){
					 //final String address=BaseActivity.this.getSharedPreferences(KALAI_COMMON_SAVE,0).getString(CHOSED_BLUETOOTH_ADDRESS,"");
					try {
						/* if(cardHeadDialog!=null){
								cardHeadDialog.show();
						}*/
						 //Toast.makeText(BaseActivity.this, "尝试连接",Toast.LENGTH_LONG).show();
						 //initMe3xDeviceController(new BlueToothV100ConnParams(address),INIT_BLUETOOTH);
						 try { 
							 /*if(cardHeadDialog!=null){
									cardHeadDialog.show();
							}*/
							  //controller.connect();
							} catch (Exception e) {
								//Tip("连接失败，未配对正确设备");
							}
						    //pg1.dismiss();
						   
						 if(DeviceConnState.CONNECTED != controller.getDeviceConnState()){
							 
							
							 startDiscovery(type);
							 
						 }else{
							 Message msgMessage=parentHandler.obtainMessage();
						     msgMessage.what=Common.FETCH_DEVICE_INFO;
							 parentHandler.sendMessage(msgMessage);
							 if(cardHeadDialog!=null){
									cardHeadDialog.show();
							}
							
						 }
						 
					} catch (Exception e) {
						// TODO: handle exception
					}finally{
						
					}
					
					 
				}
				
			} catch (Exception e) {
				Tip("您默认的蓝牙连接失败，开启蓝牙搜索");
				//startDiscovery();
			}finally{
				
				
				
				
			}
		 }else{
			 
			 startDiscovery(type);
		 }
			
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		 
		
	}
	
	Runnable newLandConnect=new Runnable(){
		public void run() {
			if(CHOSE_DEVICE.equals("B")||CHOSE_DEVICE.equals("D")){
				try {
					    controller.connect();
					  //controller.disConnect();
					 } catch (Exception e) {
					// TODO: handle exception
				}
			}
		};
	};
	private boolean connected=false;
	protected int fromAct=-1;//来自哪个页面
	protected int whatDo=2;//刷卡
	public static final int SWIP_REQUEST=600;//刷卡请求
	public String lkey_parent="";
	
	void doWork(){
		if((tv_getcard_num.getText().toString().contains("确定")||tv_getcard_num.getText().toString().contains("查询"))&&(fromAct!=UICommon.KuailainActivity)){
			Message msg=parentHandler.obtainMessage();
			msg.obj="sure";
			parentHandler.sendMessage(msg);
		}else{
			if(needShowMoeny&&Controller.isEmpty(amount_et.getText().toString().trim())){
				Tip("交易金额不为空");
				return;
			}
			
			if(CHOSE_DEVICE.equals(UICommon.LANDY_DEVICE)||CHOSE_DEVICE.equals(UICommon.AF_DEVICE)||CHOSE_DEVICE.equals(UICommon.CFT_DEVICE)||CHOSE_DEVICE.equals(UICommon.BBPOS_IC_DEVICE)||CHOSE_DEVICE.equals(UICommon.WFT_DEVICE)){
				Intent intent=new Intent(BaseActivity.this,SwipActivity.class);
				intent.putExtra("fromAct", fromAct);
				if(whatDo==SwipApi.WHATDO_SWIPER){
					String amount=amount_et.getText().toString().trim()+"";
					if(amount.length()>0){
						intent.putExtra("amount",amount);
					}
				}
				
				intent.putExtra("whatDo", whatDo);
				intent.putExtra("chose_device", CHOSE_DEVICE);
				startActivityForResult(intent, SWIP_REQUEST);
				return;
			}
			blueToothSearchAndConnected(CHOSE_DEVICE);
		}
	}
	protected void initParentView(){
		cardHeadDialog = new NotCardHeadDialog(BaseActivity.this,CHOSE_DEVICE);
		initBlueToothHandler();
		cardHeadDialog.setHandler(blueToothHandler);
		
		if(CHOSE_DEVICE.equals("D")){
			needTime=true;
		}
		
		if(CHOSE_DEVICE.equals("B")||CHOSE_DEVICE.equals("D")){
			 final String address=BaseActivity.this.getSharedPreferences(KALAI_COMMON_SAVE,0).getString(CHOSED_BLUETOOTH_ADDRESS,"");
			 //Tip("ADDRESS->"+address);
			 if(address.length()>0){
				 initMe3xDeviceController(new BlueToothV100ConnParams(address),INIT_BLUETOOTH);
				 new Thread(){
						 public void run() {
							try {
								controller.connect();
							} catch (Exception e) {
								e.printStackTrace();
							} 
						 };
					 }.start();;
				}
			 
			pg_init=new ProgressDialog(BaseActivity.this);
			pg_init.setMessage("初始化设备...");
			pg_init.setCancelable(false);
			pg_init.show();
			new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					pg_init.dismiss();
					
				}
			}, 3000);
			 
		}
		dhlistener.setControllerHandeler(dhParentHandler);
		String last_device=BaseActivity.this.getSharedPreferences(KALAI_COMMON_SAVE,0).getString(CHOSED_LAST_DEVICE_TYPE, "");
		if(!last_device.equals(CHOSE_DEVICE)){
			BaseActivity.this.getSharedPreferences(KALAI_COMMON_SAVE,0).edit().putString(CHOSED_BLUETOOTH_ADDRESS,"").commit();
		}
		BaseActivity.this.getSharedPreferences(KALAI_COMMON_SAVE,0).edit().putString(CHOSED_LAST_DEVICE_TYPE, CHOSE_DEVICE).commit();
		//;
		if(!CHOSE_DEVICE.equals("A")){
			pg=new ProgressDialog(BaseActivity.this);
			pg.setMessage("正在搜索可配对蓝牙");
			tv_getcard_num.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if(et_code_parent!=null){
						final String code1=et_code_parent.getText().toString()+"";
						if(code1.length()>=0){
							new Thread(){
								public void run() {
									if(!controller2.validateCode(code1, lkey_parent)){
										Tip("验证码有误");
										return;
									}else{
										runOnUiThread(new Runnable(){

											@Override
											public void run() {
												// TODO Auto-generated method stub
												doWork();
											}
											
										});
										
									}
								};
							}.start();
						}
					}else{
						doWork();
					}
					
					
					
					
					
				}//
				
			});
		}else{
			
		}
	 }
	
	//随机生成
	public String getHex_workkey(){
		String workkey="";
		for(int i=0;i<16;i++){
			workkey+=Integer.toHexString(new Random().nextInt(16));
		}
		return workkey.toUpperCase();
	}

	protected void setGetCardNumTv(TextView tv){
		tv_getcard_num=tv;
	}
	protected void setParentHandler(Handler handler){
		parentHandler=handler;
	}
	/* (non Javadoc) 
	 * @Title: onDestroy
	 * @Description: TODO 
	 * @see android.app.Activity#onDestroy() 
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(discoveryReciever);
	}
	/**
	 * 检查是蓝牙地址是否已经存在
	 * 
	 * @return
	 */
	private boolean ifAddressExist(String addr) {
		for (BluetoothDeviceContext devcie : discoveredDevices) {
			if (addr.equals(devcie.address))
				return true;
		}
		return false;
	}
	/**
	 * 启动蓝牙搜索
	 */
	private void startDiscovery(final String type) {
		if (bluetoothAdapter.isEnabled()) {
			 if(pg!=null){
				    runOnUiThread(new Runnable(){
				    	public void run() {
				    		try {
				    			pg.show();
							} catch (Exception e) {
								pg=new ProgressDialog(BaseActivity.this);
								pg.setMessage("正在搜索可配对蓝牙");
								try {
									pg.show();
								} catch (Exception e2) {
									// TODO: handle exception
								}
								
							}
				    		
				    	};
				    });
					
			    }
			//btnStateToWaitingInitFinished();
			if (discoveredDevices != null) {
				discoveredDevices.clear();
			}
			bluetoothAdapter.startDiscovery();
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
					} finally {
						
						bluetoothAdapter.cancelDiscovery();
					}
					try {
						pg.dismiss();
						if(discoveredDevices.size()<=0){
							Tip("没有搜索到可用蓝牙，正在重新搜索");
							//startDiscovery(type);
						}else{
							selectBtAddrToInit(type);
						}
						
					} catch (Exception e) {
						
					}
				}
			}).start();
		} else {
			
			Tip("您还未开启蓝牙");
			//appendInteractiveInfoAndShow("蓝牙未打开");
		}

	}
	
	public static final String KALAI_COMMON_SAVE="common_save";
	public static final String CHOSED_BLUETOOTH_ADDRESS="chosed_bluetooth_address";//存储上次蓝牙连接地址
	public static final String CHOSED_LAST_DEVICE_TYPE="chosed_last_device_type";//存储上次设备型号
	public static final String CHOSED_LAST_DEVICE_NAME="chosed_bluetooth_name";//上次的名字
	
	boolean checkAddress(String address,DialogInterface dialog){
		boolean temp=true;
		try {
			if(CHOSE_DEVICE.equals("B")){
				if(!address.contains("ME30")){
					dialog.dismiss();
					Tip("选择了错误蓝牙设备");
					startDiscovery(CHOSE_DEVICE);
					temp=false;
					
				}
				
			}else if(CHOSE_DEVICE.equals("C")){
				if(!address.startsWith("MPOS")){
					dialog.dismiss();
					Tip("选择了错误蓝牙设备");
					startDiscovery(CHOSE_DEVICE);
					temp=false;
					
				}
				
			}else if(CHOSE_DEVICE.equals("D")){
				if(!address.contains("ME15")){
					dialog.dismiss();
					Tip("选择了错误蓝牙设备");
					startDiscovery(CHOSE_DEVICE);
					temp=false;
					
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return temp;
		
	}
	  
	 boolean connectTrue=true;
	// 弹出已配对蓝牙对话框,点击链接相应设备
		public void selectBtAddrToInit(final String type) {

			/** 收集扫描的蓝牙信息 **/
			int i = 0;
			final String[] bluetoothNames = new String[discoveredDevices.size()];
			for (BluetoothDeviceContext device : discoveredDevices) {
				bluetoothNames[i++] = device.name;
			}
			/** 弹出蓝牙对话框 **/
			
			final Builder builder = new android.app.AlertDialog.Builder(this);
			builder.setTitle("请选取已配对设备连接:");
			builder.setSingleChoiceItems(bluetoothNames, 0, new AlertDialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					
					try {
						
						deviceToConnect = discoveredDevices.get(which).address;
						//连接型号测试
						connectTrue=checkAddress(bluetoothNames[which],dialog);
						if(connectTrue){
							BaseActivity.this.getSharedPreferences(KALAI_COMMON_SAVE,0).edit().putString(CHOSED_BLUETOOTH_ADDRESS, discoveredDevices.get(which).address).commit();
							if(type.equals("C")){
								dhcontroller.connect(deviceToConnect);
								dialogTip(READY_TO_CONNECT);
							}
						}
						
						
						
					} catch (Exception e) {
						//appendInteractiveInfoAndShow("控制器初始化失败!");
						Tip("未连接正确的蓝牙设备，控制器初始化失败");
					} finally {
						if(connectTrue){
							dialog.dismiss();
							
							if(type.equals("B")||type.equals("D")){
								initMe3xDeviceController(new BlueToothV100ConnParams(deviceToConnect),INIT_BLUETOOTH);
								Message msgMessage=parentHandler.obtainMessage();
								msgMessage.what=Common.FETCH_DEVICE_INFO;
								parentHandler.sendMessage(msgMessage);
							}
							cardHeadDialog.show();
						}
						
						
						
					}//
				}
			});
		   runOnUiThread(new Runnable(){

			@Override
			public void run() {
				
					Dialog dialog = builder.create();
					dialog.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							//checkBtnState();
						}
					});
					dialog.show();
				
				
				
			}
			   
		   });
			
		}
		
		protected void Tip(final String msg){
			runOnUiThread(new Runnable(){
				
				@Override
				public void run() {
					Toast.makeText(BaseActivity.this,msg, Toast.LENGTH_SHORT).show();
					
				}
			});
			
		}
		/**
		 * 初始化设备
		 * 
		 * @since ver1.0
		 * @param params
		 *            设备连接参数
		 */
		private static final int INIT_VIDEO=0;
		private static final int INIT_BLUETOOTH=1;
		private void initMe3xDeviceController(DeviceConnParams params,int type) {
			String initPara="";
			switch (type) {
			case INIT_VIDEO:
				initPara=ME11_DRIVER_NAME;
			  break;
			case INIT_BLUETOOTH:
				initPara=ME3X_DRIVER_NAME;
			  break;
			 }
			
			controller.init(BaseActivity.this, initPara, params, new DeviceEventListener<ConnectionCloseEvent>() {
				@Override
				public void onEvent(ConnectionCloseEvent event, Handler handler) {
					if (event.isSuccess()) {
						connected=true;
					}
					if (event.isFailed()) {
						
					}
				}

				@Override
				public Handler getUIHandler() {
					return null;
				}
			});
		        
		}
		
		public  String UnpackTrack(byte[] track2) {
			String encodedTrack2 = new String(track2);
			return encodedTrack2.replace("=", "d");
		}

		public  String SetTrackData(final String data2) {
			// String data2 = track2Data;
			String datadata = data2.replaceAll(" ", "");
			String replaceData;
			
			if ('f' == datadata.charAt(datadata.length() - 1) ||
				'F' == datadata.charAt(datadata.length() - 1)) {
				replaceData = datadata.substring(0, datadata.length() - 1);
			} else {
				replaceData = datadata;
			}
			
			return replaceData;
		}
		
	
		public static final int WRITEPAD=300;
		protected void goToHanwriting(CreditCardBean creditCardBean,MsgBean msgBean,String amount){
			Intent intent = new Intent();
			intent.setClass(getApplicationContext(), WritePadActivity.class);
			intent.putExtra("amount",amount);
			/*Bundle bundle = new Bundle();
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
			intent.putExtras(bundle);*/
			startActivityForResult(intent, WRITEPAD);
		}
		
		public String sign_path="";//签名未合成的路径
		protected Activity cur_Ac=null;
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			// TODO Auto-generated method stub
			super.onActivityResult(requestCode, resultCode, data);
			Log.v("landy1", "req-->"+requestCode+"---result->"+resultCode);
			//从签名页面返回提交数据
			
			if(requestCode==WRITEPAD&&resultCode==WritePadActivity.WRITING_RESULT_CODE){
				if(data!=null){
					sign_path=data.getStringExtra("sign_path");
					//向后台发起请求
					Message msg=parentHandler.obtainMessage();
					msg.obj="签名完毕";
					parentHandler.sendMessage(msg);
				}
				
			}
			
			

			//刷卡交易成功
			if(requestCode==SWIP_REQUEST&&resultCode==SwipActivity.RESPONSE_SWIP_SUCCESS){
				LandyTackMsg lanyTrackMsg=(LandyTackMsg) data.getSerializableExtra("data");
				//打印信息
				logv("cardno", lanyTrackMsg.cardNo+"");
				logv("track2", lanyTrackMsg.track2+"");
				logv("expiredate", lanyTrackMsg.expireDate+"");
				logv("ksn", lanyTrackMsg.ksn+"");
				Log.v("55data",lanyTrackMsg.Data55+"");
				int cur_fromAct=data.getIntExtra("fromAct",-1);
				Log.v("fromact",cur_fromAct+"");
				
				//如果是账户充值
				if(cur_fromAct==UICommon.TopUpActivity){
					((TopUpTwoActivity)cur_Ac).ModelSubmit(lanyTrackMsg);
				}
				
				
				//如果是即时到账充值
				if(cur_fromAct==UICommon.TimelyAaccountTopUp){
					((TimelyAccountTopUpActivity)cur_Ac).ModelSubmit(lanyTrackMsg);
				}
				
				//如果是信用卡还款
				if(cur_fromAct==UICommon.CardPayActivity){
					((CradPayActivity)cur_Ac).ModelSubmit(lanyTrackMsg);
				}
				
				
				//如果是话费充值
				if(cur_fromAct==UICommon.TelPhoneActivity){
					((TelPhoneActivity)cur_Ac).ModelSubmit(lanyTrackMsg);
				}
				
				//如果是转账汇款
				if(cur_fromAct==UICommon.TradnsferAccountsActivity){
					((TradnsferAccountsActivity)cur_Ac).ModelSubmit(lanyTrackMsg);
				}
				
				//余额查询
				if(cur_fromAct==UICommon.QueryMoneyAactivity){
					
					((QueryMoneyActivity)cur_Ac).ModelSubmit(lanyTrackMsg);
				}
				//快联支付
                if(cur_fromAct==UICommon.KuailainActivity){
					
					((KuaiLainActivity)cur_Ac).ModelSubmit(lanyTrackMsg);
				}
			}
			//Log.v("", "");
			
			//获取卡号成功
			if(requestCode==SWIP_REQUEST&&resultCode==SwipActivity.RESPONSE_GET_CARD_NO_SUCESS){
				LandyTackMsg lanyTrackMsg=(LandyTackMsg) data.getSerializableExtra("data");
				logv("cardno-get-base", lanyTrackMsg.cardNo+"");
				int cur_fromAct=data.getIntExtra("fromAct",-1);
				
				if(cur_fromAct==UICommon.AddCardActivity){
					((AddCradActivity)cur_Ac).setCardNo(lanyTrackMsg);
				}
				
				if(cur_fromAct==UICommon.AddCardPaymentActivity){
					((AddCardPaymentsActivity)cur_Ac).setCradNo(lanyTrackMsg);
				}
				
				if(cur_fromAct==UICommon.AddTimelyAccountActivity){
					((AddTimelyAccountActivity)cur_Ac).setCardNo(lanyTrackMsg);
				}
				if(cur_fromAct==UICommon.BindDeviceAactivity){
					((BindDeviceActivity)cur_Ac).setCardNo(lanyTrackMsg);
				}
				
			}
			//任务失败
            if(requestCode==SWIP_REQUEST&&resultCode==SwipActivity.RESPONSE_WORK_FAIL){
            	if(!CHOSE_DEVICE.equals(UICommon.CFT_DEVICE)){
            		Tip("刷卡失败");
            	}
				
				finish();
			}
			
		}
		void logv(String key,String value){
			Log.v("landy1", "back-->"+key+"--value-->"+value);
			
		}
 
   protected void swipShow(){
	   runOnUiThread(new Runnable(){

		@Override
		public void run() {
			cardHeadDialog.swipShow();
			
		}
		   
	   });
	   
   }
		
   protected void swipHidden(){
	   runOnUiThread(new Runnable(){

		@Override
		public void run() {
			cardHeadDialog.swipHiden();
			
		}
		   
	   });
   }

}
