package com.apicloud.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.apicloud.activity.topup.NotCardHeadDialog;
import com.apicloud.controller.Controller;
import com.apicloud.controller.MyController;
import com.apicloud.landy.LandyTackMsg;
import com.apicloud.module.BluetoothDeviceContext;
import com.apicloud.swip.NewSwipActivity;
import com.apicloud.swip.SwipActivity;
import com.apicloud.swip.SwipApi;
import com.apicloud.util.UICommon;

public class BasicActivity extends Activity{
	public static final String KALAI_COMMON_SAVE="common_save";//sharedpreference存储位置
	public static final String CHOSED_BLUETOOTH_ADDRESS="chosed_bluetooth_address";//存储上次蓝牙连接地址
	public static final String CHOSED_LAST_DEVICE_TYPE = "chosed_last_device_type";//存储上次设备类型
	public static final String CHOSED_LAST_DEVICE_NAME = "chosed_bluetooth_name";//上次的名字
	protected MyController controller;
	protected Handler blueToothHandler;
	protected TextView tv_getCardNum;
	protected String CHOSE_DEVICE = "E";//表示蓝迪设备
	public BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); 
	public List<BluetoothDeviceContext> discoveredDevices = new ArrayList<BluetoothDeviceContext>();
	private ProgressDialog pg;
	protected NotCardHeadDialog cardHeadDialog;
	protected String deviceToConnect;
	public EditText et_parent_code;
	protected int fromAct = -1;//来自哪个页面
	protected int whatDo = 2;//刷卡
	protected Activity cur_Ac = null;
	public TextView amount_et;
	protected boolean needShowMoney=false;
	public static final int SWIP_REQUEST = 500;//插刷请求
	private boolean connected = false;
	protected String bind_lkey="";
	protected String bind_shopno="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		controller = new MyController(BasicActivity.this);
	}
	
	protected void setGetCardNum(TextView tv){
		this.tv_getCardNum=tv;
	}
	void initBlueToothHandler(){
		blueToothHandler = new Handler(){
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
	private void startDiscovery(final String type){
		if(bluetoothAdapter.isEnabled()){
			if(pg!=null){
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						try{
							pg.show();
						}catch(Exception e){
							pg = new ProgressDialog(BasicActivity.this);
							pg.setMessage("正在寻找蓝牙设备");
							try {
								pg.show();
							} catch (Exception e1) {
								
								e1.printStackTrace();
							}
						}
						
					}
				});
				
			}
			if(discoveredDevices!=null){
				discoveredDevices.clear();
			}
			bluetoothAdapter.startDiscovery();
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally{
						bluetoothAdapter.cancelDiscovery();
					}
					try {
						pg.dismiss();
						if(discoveredDevices.size()<=0){
							Tip("未找到匹配蓝牙");
						}else{
							selectBtAddrToInit(type);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
	}
	boolean connectTrue = true;
	public void selectBtAddrToInit(final String type){
		//收集蓝牙搜索到的信息
		int i = 0;
		final String[] blueNames = new String[discoveredDevices.size()];
		for(BluetoothDeviceContext device :discoveredDevices){
			blueNames[i++]=device.name;
		}
		//弹出对话框
		final Builder builder = new AlertDialog.Builder(BasicActivity.this);
		builder.setTitle("请选择蓝牙设备");
		builder.setSingleChoiceItems(blueNames, 0, new AlertDialog.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					deviceToConnect = discoveredDevices.get(which).address;
					//连接型号判定
					connectTrue = checkAddress(blueNames[which],dialog);
					if(connectTrue){
						BasicActivity.this.getSharedPreferences(KALAI_COMMON_SAVE, 0).edit().putString(CHOSED_BLUETOOTH_ADDRESS,discoveredDevices.get(which).address).commit();
						if(type.equals("C")){
							//建设中..
						}
					}
				} catch (Exception e) {
					Tip("未连接正确的蓝牙设备，控制器初始化失败");
					
				}finally{
					if(connectTrue){
						dialog.dismiss();
						if(type.equals("B")||type.equals("C")){
							//建设中..
						}
					}
					cardHeadDialog.show();
				}
			}
		} );
		//show
//		runOnUiThread(new Runnable(){
//
//			@Override
//			public void run() {
//				
//					Dialog dialog = builder.create();
//					dialog.setOnDismissListener(new OnDismissListener() {
//						@Override
//						public void onDismiss(DialogInterface dialog) {
//							//checkBtnState();
//						}
//					});
//					dialog.show();
//				
//				
//				
//			}
//			   
//		   });
	}
	
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
	protected void Tip(final String msg){
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(BasicActivity.this, msg, Toast.LENGTH_LONG).show();
				
			}
		});
	}
	//
	protected void initParentView(){
		cardHeadDialog = new NotCardHeadDialog(BasicActivity.this, CHOSE_DEVICE);
		initBlueToothHandler();
		cardHeadDialog.setHandler(blueToothHandler);
		if(CHOSE_DEVICE.equals("D")){
			//建设中...
		}
		if(CHOSE_DEVICE.equals("B")||CHOSE_DEVICE.equals("D")){
			//建设中
		}
		//获取上次设备地址
		String last_device = BasicActivity.this.getSharedPreferences(KALAI_COMMON_SAVE, 0).getString(CHOSE_DEVICE, CHOSED_LAST_DEVICE_TYPE);
		if(!last_device.equals(CHOSE_DEVICE)){
			BasicActivity.this.getSharedPreferences(KALAI_COMMON_SAVE, 0).edit().putString(CHOSED_BLUETOOTH_ADDRESS, "").commit();
		}
		BasicActivity.this.getSharedPreferences(KALAI_COMMON_SAVE, 0).edit().putString(CHOSED_LAST_DEVICE_TYPE, CHOSE_DEVICE).commit();
		
		if(!CHOSE_DEVICE.equals("A")){
			pg=new ProgressDialog(BasicActivity.this);
			pg.setMessage("正在搜索可配对蓝牙");
			tv_getCardNum.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(et_parent_code!=null){
						//建设中...
					}else{
						doWork();
					}
					
				}
			});
			
		}else{
			
		}
	}

	void doWork() {
		if (tv_getCardNum.getText().toString().contains("确定") || tv_getCardNum.getText().toString().contains("查询") && (fromAct != UICommon.KuailainActivity)) {
			// 建设中
		} else {
			if (needShowMoney && Controller.isEmpty(amount_et.getText().toString().trim())) {
				// 建设中
			}
			if (CHOSE_DEVICE.equals(UICommon.LANDY_DEVICE) || CHOSE_DEVICE.equals(UICommon.AF_DEVICE) || CHOSE_DEVICE.equals(UICommon.CFT_DEVICE) || CHOSE_DEVICE.equals(UICommon.BBPOS_IC_DEVICE) || CHOSE_DEVICE.equals(UICommon.WFT_DEVICE)) {
				Intent intent = new Intent(BasicActivity.this, NewSwipActivity.class);
				intent.putExtra("fromAct", fromAct);
				if (whatDo == SwipApi.WHATDO_SWIPER) {
					// 建设中
				}
				intent.putExtra("whatDo", whatDo);
				intent.putExtra("chose_device", CHOSE_DEVICE);
				startActivityForResult(intent, SWIP_REQUEST);
				return;
			}
			blueToothSearchAndConnected(CHOSE_DEVICE);
		}

	}
	void blueToothSearchAndConnected(String type){
		// Tip("show1");
		 if(cardHeadDialog!=null){
				cardHeadDialog.show();
		}
		 final String address=BasicActivity.this.getSharedPreferences(KALAI_COMMON_SAVE,0).getString(CHOSED_BLUETOOTH_ADDRESS,"");
		 connected=false;
		 //Tip("show1");
		
		 //Tip("show2");
		 if(address.length()>0){
			 //建设中
//			 
//			 try {
//				if(type.equals("C")){
//					dhcontroller.connect(address);
//				    dialogTip(READY_TO_CONNECT);
//				    if(cardHeadDialog!=null){
//						cardHeadDialog.show();
//					}
//				}
//				
//				if(type.equals("B")||type.equals("D")){
//					 //final String address=BaseActivity.this.getSharedPreferences(KALAI_COMMON_SAVE,0).getString(CHOSED_BLUETOOTH_ADDRESS,"");
//					try {
//						/* if(cardHeadDialog!=null){
//								cardHeadDialog.show();
//						}*/
//						 //Toast.makeText(BaseActivity.this, "尝试连接",Toast.LENGTH_LONG).show();
//						 //initMe3xDeviceController(new BlueToothV100ConnParams(address),INIT_BLUETOOTH);
//						 try { 
//							 /*if(cardHeadDialog!=null){
//									cardHeadDialog.show();
//							}*/
//							  //controller.connect();
//							} catch (Exception e) {
//								//Tip("连接失败，未配对正确设备");
//							}
//						    //pg1.dismiss();
//						   
//						 if(DeviceConnState.CONNECTED != controller.getDeviceConnState()){
//							 
//							
//							 startDiscovery(type);
//							 
//						 }else{
//							 Message msgMessage=parentHandler.obtainMessage();
//						     msgMessage.what=Common.FETCH_DEVICE_INFO;
//							 parentHandler.sendMessage(msgMessage);
//							 if(cardHeadDialog!=null){
//									cardHeadDialog.show();
//							}
//							
//						 }
//						 
//					} catch (Exception e) {
//						// TODO: handle exception
//					}finally{
//						
//					}
//					
//					 
//				}
//				
//			} catch (Exception e) {
//				Tip("您默认的蓝牙连接失败，开启蓝牙搜索");
//				//startDiscovery();
//			}finally{
//				
//				
//				
//				
//			}
		 }else{
			 
			 startDiscovery(type);
		 }
			
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		//获取卡号成功
		if(requestCode==SWIP_REQUEST&&resultCode==NewSwipActivity.RESPONSE_GET_CARD_NO_SUCESS){
			LandyTackMsg landyTrackMsg = (LandyTackMsg) data.getSerializableExtra("data");
			int cur_fromAct = data.getIntExtra("fromAct", -1);
			if(cur_fromAct==UICommon.AddNewCardActivity){
				((AddNewCardActivity)cur_Ac).setCardNo(landyTrackMsg);
			}
			if(cur_fromAct==UICommon.BindEquipmentActivity){
				((BindEquipmentActivity)cur_Ac).setCardNo(landyTrackMsg);
			}
			if(cur_fromAct==UICommon.AddNewCreditActivity){
				((AddNewCreditActivity)cur_Ac).setCardNo(landyTrackMsg);
			}
		}
	}
}
