package com.apicloud.swip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.apicloud.activity.BasicActivity;
import com.apicloud.activity.WebViewActivity;
import com.apicloud.activity.topup.TopUpDialog;
import com.apicloud.af.afSwipService;
import com.apicloud.bbpos_ic.MybbosIcService;
import com.apicloud.cft.myCsSwipService;
import com.apicloud.landy.NewLandySwipService;
import com.apicloud.landy.LandyTackMsg;

import com.apicloud.module.BluetoothDeviceContext;
import com.apicloud.util.UICommon;
import com.apicloud.wft.wftService;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

/**
 * 
 * @author myp
 * 刷卡的类
 *
 */

public class NewSwipActivity extends Activity implements OnClickListener{
	
	TextView tv_tip;
	TextView tv_title,btn_search,btn_sure;
	ImageView im_shuaka;
	private int fromAct = -1;
	private AnimationDrawable animationDrawable;
	private SwipApi api;
	private BluetoothDeviceContext  bluetoothDevice=null;
	private String chose_device="";
	private int whatToDo=-1;
	private String amount="";
	private String password="";
	private Handler mainHandler=null;
	private  LandyTackMsg lanyTrackMsg=null;//返回的数据封装 
	
	public static final int FINISH_ENCRY_KEY_SUCCESS=303;//加密piN成功
	public static final int FINISH_ENCRY_KEY_FAIL=305;//加密PIN失败
	public static final int FINISH_WORK_SUCCESS=300;//任务成功完成
	public static final int FINISH_WORK_FAIL=301;//任务失败
	public static final int FINISH_GET_CARD_NO_SUCCESS=302;//获取卡号成功
    private TopUpDialog ModelTopUpDialog;// 模块输入密码对话框
  
    Button btn_help;
    TextView txt_title;
    ImageView iv_head;
    ProgressDialog pg;//进度圈
    TextView tv_search;//搜索蓝牙
    ImageButton iv_back;
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
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("layout_for_swip"));
		initView();
		
		
	}
	
	void initView(){
		ModelTopUpDialog = new TopUpDialog(NewSwipActivity.this);
		ModelTopUpDialog.setOnclickListener(this);
		ModelTopUpDialog.ed_crad_pass.setVisibility(View.VISIBLE);
		
		pg=new ProgressDialog(NewSwipActivity.this);
		pg.setCancelable(false);
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(discoveryReciever, filter);
		tv_title=(TextView) this.findViewById(UZResourcesIDFinder.getResIdID("tv_title"));
		tv_title.setText("刷卡");
		tv_search=(TextView) this.findViewById(UZResourcesIDFinder.getResIdID("tv_search_bl"));
		tv_search.setOnClickListener(this);
		//tv_tip=(TextView) findViewById(UZResourcesIDFinder.getResIdID("tv_tip"));
		//im_shuaka=(ImageView) findViewById(UZResourcesIDFinder.getResIdID("img_shuaka"));
		
		btn_help=(Button) findViewById(UZResourcesIDFinder.getResIdID("btn_help"));
		btn_help.setOnClickListener(this);
		iv_back=(ImageButton) findViewById(UZResourcesIDFinder.getResIdID("btn_back2"));
		iv_back.setOnClickListener(this);
		
		tv_tip=(TextView) this.findViewById(UZResourcesIDFinder.getResIdID("txt_title"));
		im_shuaka=(ImageView) findViewById(UZResourcesIDFinder.getResIdID("iv_head"));
		//获取刷卡的参数
		if(this.getIntent()!=null){
			Bundle bundle=this.getIntent().getExtras();
			if(bundle!=null){
				fromAct=bundle.getInt("fromAct");
				amount=bundle.getString("amount")+"";
				bluetoothDevice=(BluetoothDeviceContext) bundle.getSerializable("bluetooth");
			    chose_device=bundle.getString("chose_device")+"";
			    if(bluetoothDevice!=null){
			    	 logv("device", bluetoothDevice.name+"---address-->"+bluetoothDevice.address);
			    }
			    whatToDo=bundle.getInt("whatDo");
			    logv("fromAc", fromAct+"");
			    logv("whatDO", whatToDo+"");
			    logv("amount", amount+"");
			}
		}
		initMainHanlder();
		//控制对话框
		//controlCardDialog();
		//如果是联迪设备
		/*if(chose_device.equals(UICommon.LANDY_DEVICE)){
		  im_shuaka.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("ly_cash_landy"));
		}*/
		/*if(chose_device.equals(UICommon.AF_DEVICE)){
			  tv_tip.setText("尝试连接蓝牙");
			  im_shuaka.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("af_1"));
		}*/
		if(chose_device.equals(UICommon.CFT_DEVICE)){
			  im_shuaka.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("cft_1"));
		}
		if(chose_device.equals(UICommon.LANDY_DEVICE)||chose_device.equals(UICommon.AF_DEVICE)){
			  tv_tip.setText("尝试连接蓝牙");
			  im_shuaka.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("ly_cash_landy"));
		}
		
		if(chose_device.equals(UICommon.BBPOS_IC_DEVICE)){
			 im_shuaka.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("cft_1"));
		}
		if(chose_device.equals(UICommon.WFT_DEVICE)){
			  tv_tip.setText("尝试连接蓝牙");
			  im_shuaka.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("ly_cash_wft"));
		}
		checkBlueTooth();
		
		
		if(chose_device.equals(UICommon.CFT_DEVICE)){
			loadCFT(mainHandler);
		}
		if(chose_device.equals(UICommon.BBPOS_IC_DEVICE)){
			loadBBPOS_IC(mainHandler);
		}
		if(chose_device.equals(UICommon.CFT_DEVICE)||chose_device.equals(UICommon.BBPOS_IC_DEVICE)){
			
		}else{
			tv_tip.setText("请确认蓝牙设备开启，尝试连接中......");
		}
	}
	
	//检查蓝牙设备是否连接过
	void checkBlueTooth(){
		//如果是卡头设备
		if(chose_device.equals(UICommon.BBPOS_IC_DEVICE)||chose_device.equals(UICommon.CFT_DEVICE)){
			tv_search.setVisibility(View.GONE);
			//清空地址
			NewSwipActivity.this.getSharedPreferences(BasicActivity.KALAI_COMMON_SAVE,0).edit().putString(BasicActivity.CHOSED_BLUETOOTH_ADDRESS,"").commit();
			NewSwipActivity.this.getSharedPreferences(BasicActivity.KALAI_COMMON_SAVE,0).edit().putString(BasicActivity.CHOSED_LAST_DEVICE_NAME,"").commit();
			//更新最新设备
			NewSwipActivity.this.getSharedPreferences(BasicActivity.KALAI_COMMON_SAVE,0).edit().putString(BasicActivity.CHOSED_LAST_DEVICE_TYPE, chose_device).commit();
	    //如果不是卡头设备
		}else{
			//如果上个设备不同
			String last_device=NewSwipActivity.this.getSharedPreferences(BasicActivity.KALAI_COMMON_SAVE,0).getString(BasicActivity.CHOSED_LAST_DEVICE_TYPE, "");
			if(!last_device.equals(chose_device)){
				//清空地址
				NewSwipActivity.this.getSharedPreferences(BasicActivity.KALAI_COMMON_SAVE,0).edit().putString(BasicActivity.CHOSED_BLUETOOTH_ADDRESS,"").commit();
				NewSwipActivity.this.getSharedPreferences(BasicActivity.KALAI_COMMON_SAVE,0).edit().putString(BasicActivity.CHOSED_LAST_DEVICE_NAME,"").commit();
			}
			//更新最新设备
			NewSwipActivity.this.getSharedPreferences(BasicActivity.KALAI_COMMON_SAVE,0).edit().putString(BasicActivity.CHOSED_LAST_DEVICE_TYPE, chose_device).commit();
			String address=NewSwipActivity.this.getSharedPreferences(BasicActivity.KALAI_COMMON_SAVE,0).getString(BasicActivity.CHOSED_BLUETOOTH_ADDRESS,"");
		    String bluename=NewSwipActivity.this.getSharedPreferences(BasicActivity.KALAI_COMMON_SAVE,0).getString(BasicActivity.CHOSED_LAST_DEVICE_NAME,"");
			//如果地址大于0，就初始化设备
			if(address.length()>0){
				bluetoothDevice=new BluetoothDeviceContext(bluename, address);
				if(chose_device.equals(UICommon.LANDY_DEVICE)){
					if(bluename.startsWith("M18")){
						tv_tip.setText("尝试连接蓝牙");
						im_shuaka.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("ly_cash_landy"));
						loadLandySDK(mainHandler);
					}
					if(bluename.startsWith("L")){
						tv_tip.setText("尝试连接蓝牙");
						im_shuaka.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("ly_cash_landy"));
						loadAfSDK(mainHandler);
					}
				}
				if(chose_device.equals(UICommon.WFT_DEVICE)){
					tv_tip.setText("尝试连接蓝牙");
					im_shuaka.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("ly_cash_wft"));
					loadWftSDK(mainHandler);
				}
				
			}else{
				//搜索设备
				startDiscovery();
			}
			
		}
	}
	
	/**
	 * 启动蓝牙搜索
	 */
	public  List<BluetoothDeviceContext> discoveredDevices = new ArrayList<BluetoothDeviceContext>();
	public BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private void startDiscovery() {
		if (bluetoothAdapter.isEnabled()) {
			 if(pg!=null){
				    runOnUiThread(new Runnable(){
				    	public void run() {
				    		try {
				    			pg.setMessage("正在搜索可配对蓝牙");
				    			pg.show();
							} catch (Exception e) {
								pg=new ProgressDialog(NewSwipActivity.this);
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
							selectBtAddrToInit();
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
	void logv(String key,String value){
		Log.v("wft1", key+"--->"+value);
		
	}
	 public static final int RESPONSE_SWIP_SUCCESS=602;//刷卡成功返回
     public static final int RESPONSE_WORK_FAIL=603;//任务失败返回
     public static final int RESPONSE_GET_CARD_NO_SUCESS=604;//获取卡号成功
   
     /**
      * 
      * TODO  中央处理器，处理刷卡的各种返回结果
        2015年12月15日
        void
      */
	void initMainHanlder(){
		mainHandler=new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				//加密成功
				if(msg.what==FINISH_ENCRY_KEY_SUCCESS){
					lanyTrackMsg.pinBlock=(String) msg.obj+"";
					logv("pinblock", lanyTrackMsg.pinBlock+"");
				    api.closeDev();
				    //返回给上个页面
				    Intent responseIntent=new Intent(NewSwipActivity.this,BasicActivity.class);
				    responseIntent.putExtra("data", lanyTrackMsg);
				    responseIntent.putExtra("fromAct", fromAct);
					setResult(RESPONSE_SWIP_SUCCESS, responseIntent);
					finish();
				}
				//卡信息获取成功
				if(msg.what==FINISH_WORK_SUCCESS){
					
					lanyTrackMsg=(LandyTackMsg) msg.obj;
					if(chose_device.equals(UICommon.WFT_DEVICE)){
						api.closeDev();
					    //返回给上个页面
					    Intent responseIntent=new Intent(NewSwipActivity.this,BasicActivity.class);
					    responseIntent.putExtra("data", lanyTrackMsg);
					    responseIntent.putExtra("fromAct", fromAct);
						setResult(RESPONSE_SWIP_SUCCESS, responseIntent);
						finish();
					}else{
						ModelTopUpDialog.txt_crad_moery.setText(lanyTrackMsg.amount+"");
						ModelTopUpDialog.txt_cradNumber.setText(lanyTrackMsg.cardNo+"");
						ModelTopUpDialog.show();
					}
					
					//打印返回的信息
				    logv("cardno",lanyTrackMsg.cardNo);
				    logv("ksn", lanyTrackMsg.ksn);
				    logv("track1",lanyTrackMsg.track1);
				    logv("track2",lanyTrackMsg.track2);
				    logv("track3",lanyTrackMsg.track3);
				    logv("expirdate", lanyTrackMsg.expireDate);
				    logv("55data", lanyTrackMsg.Data55);
				   
					
				}
				//失败
				if(msg.what==FINISH_WORK_FAIL){
					
					
					Intent responseIntent=new Intent(NewSwipActivity.this,BasicActivity.class);
					setResult(RESPONSE_WORK_FAIL, responseIntent);
					finish();
					api.closeDev();
				}
				
				
				if(msg.what==FINISH_GET_CARD_NO_SUCCESS){//获取卡号成功
					 lanyTrackMsg=(LandyTackMsg) msg.obj;
					 logv("cardno-get",lanyTrackMsg.cardNo);
					 
					 Intent responseIntent=new Intent(NewSwipActivity.this,BasicActivity.class);
					 responseIntent.putExtra("data", lanyTrackMsg);
					 responseIntent.putExtra("fromAct", fromAct);
				     setResult(RESPONSE_GET_CARD_NO_SUCESS, responseIntent);
				     finish();
				     api.closeDev();
				}
			}
		};
	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//如果选择的是联迪设备
		         
		        
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode==KeyEvent.KEYCODE_BACK){
			try {
				api.closeDev();
				Intent responseIntent=new Intent(NewSwipActivity.this,BlueToothSearchActivity.class);
				setResult(RESPONSE_WORK_FAIL, responseIntent);
				finish();
			} catch (Exception e) {
				// TODO: handle exception
			}
			
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void loadBBPOS_IC(Handler handler){
		tv_tip.setText("请插入卡头");
		Map<String, Object> params = new HashMap<String, Object>();
		//设备刷卡参数
		params.put("fromAct", fromAct);
		params.put("bluetooth", bluetoothDevice);
		params.put("whatDo", whatToDo);
		params.put("amount", amount+"");
		api=new MybbosIcService(getApplicationContext(), new SwipUiHandler(NewSwipActivity.this){
			@Override
			protected void showText(Message msg) {
				// TODO Auto-generated method stub
				super.showText(msg);
				String tip=msg.obj+"";
				tv_tip.setText(msg.obj+"");
				if(tip.equals("连接成功")){
					im_shuaka.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("cft_2"));
				}
				if(tip.equals("等待刷卡")){
					//im_shuaka.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("cft_sk"));
				}
				
			}
		}, handler, whatToDo, params);
		api.openDev();
	}
	
	private void loadCFT(Handler handler){
		tv_tip.setText("请插入卡头");
		Map<String, Object> params = new HashMap<String, Object>();
		//设备刷卡参数
		params.put("fromAct", fromAct);
		params.put("bluetooth", bluetoothDevice);
		params.put("whatDo", whatToDo);
		params.put("amount", amount+"");
		
		api=new myCsSwipService(getApplicationContext(), new SwipUiHandler(NewSwipActivity.this){
			
			@SuppressLint("NewApi")
			@Override
			protected void showText(Message msg) {
				// TODO Auto-generated method stub
				super.showText(msg);
				String tip=msg.obj+"";
				tv_tip.setText(msg.obj+"");
				//cardHeadDialog.txt_title.setText(msg.obj+"");
				if(tip.equals("连接成功")){
					 //cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("cft_2")));
					im_shuaka.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("cft_2"));
				}
				if(tip.equals("等待刷卡")){
					 //cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("cft_sk")));
					//im_shuaka.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("cft_sk"));
				}
			}
			
			@Override
			protected void showTip(Message msg) {
				// TODO Auto-generated method stub
				super.showTip(msg);
			}
			
			@Override
			protected void showAnimation(Message msg) {
				// TODO Auto-generated method stub
				super.showAnimation(msg);
				
			}
			
		}, handler, whatToDo, params);
		api.openDev();
	}
	private void loadAfSDK(Handler handler){
		Map<String, Object> params = new HashMap<String, Object>();
		//设备刷卡参数
		params.put("fromAct", fromAct);
		params.put("bluetooth", bluetoothDevice);
		params.put("whatDo", whatToDo);
		params.put("amount", amount+"");
		
		api=new afSwipService(getApplicationContext(), new SwipUiHandler(NewSwipActivity.this){
			
			@Override
			protected void showText(Message msg) {
				// TODO Auto-generated method stub
				super.showText(msg);
				String tip=msg.obj+"";
				tv_tip.setText(msg.obj+"");
				//cardHeadDialog.txt_title.setText(msg.obj+"");
				if(tip.equals("连接成功")){
					im_shuaka.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("ly_cash2_landy"));
				}
				if(tip.equals("连接失败")){
					startDiscovery();
				}
				if(tip.equals("请刷卡")){
					//im_shuaka.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("af_3"));
				}
			}
			
			@Override
			protected void showTip(Message msg) {
				// TODO Auto-generated method stub
				super.showTip(msg);
			}
			
		}, handler, whatToDo, params);
		api.openDev();
	}
	
	private void loadWftSDK(Handler handler){
		Map<String, Object> params = new HashMap<String, Object>();
		//设备刷卡参数
		params.put("fromAct", fromAct);
		params.put("bluetooth", bluetoothDevice);
		params.put("whatDo", whatToDo);
		params.put("amount", amount+"");
		api=new wftService(NewSwipActivity.this, new SwipUiHandler(NewSwipActivity.this){
			@Override
			protected void showText(Message msg) {
				// TODO Auto-generated method stub
				super.showText(msg);
				String tip=msg.obj+"";
				tv_tip.setText(msg.obj+"");
				if(tip.equals("连接成功")){
					im_shuaka.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("ly_cash2_wft"));
				}
			}
		}, handler,whatToDo, params);
		api.openDev();
	}
	
	
	
	/**
	 * 
	 * TODO启动联迪设备
	   2015年12月9日
	   void
	 */
	private void loadLandySDK(Handler handler){
		Map<String, Object> params = new HashMap<String, Object>();
		//设备刷卡参数
		params.put("fromAct", fromAct);
		params.put("bluetooth", bluetoothDevice);
		params.put("whatDo", whatToDo);
		params.put("amount", amount+"");
		api=new NewLandySwipService(NewSwipActivity.this,new SwipUiHandler(NewSwipActivity.this){
			
			@Override
			protected void showText(Message msg) {
				// TODO Auto-generated method stub
				super.showText(msg);
				tv_tip.setText(msg.obj+"");
				String tipMsg=(String) msg.obj;
				 Log.v("landy1",tipMsg+"");
				//如果是成功连接就切换背景
				if(tipMsg.equals(NewLandySwipService.CONNECTED_SUCCESS)){
					im_shuaka.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("ly_cash2_landy"));
				}
				if(tipMsg.equals("连接失败1")){
					startDiscovery();
				}
				
				
			}
			@Override
			protected void showTip(Message msg) {
				// TODO Auto-generated method stub
				super.showTip(msg);
				//Tip(msg.obj+"");
				
			}
			
		}, handler,whatToDo, params);
		api.openDev();
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		//api.closeDev();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
	}
	
	
	/**
	 * 
	 * TODO  TOAST 提示
	   @param msg
	   2015年12月9日
	   void
	 */
	private void Tip(final String msg){
		runOnUiThread(new Thread(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				Toast.makeText(NewSwipActivity.this, msg, Toast.LENGTH_SHORT).show();
			}
		});
	
	}

	@Override
	public void onClick(View v) {
		//点击密码输入框确定按钮
		if(v == ModelTopUpDialog.txt_ok){
			password=ModelTopUpDialog.ed_crad_pass.getText().toString().trim();
			lanyTrackMsg.password=password;
			if(password==null||password.length()<=0){
				Toast.makeText(NewSwipActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
				return;
			}
			ModelTopUpDialog.dismiss();
			//联迪此处加密
			if(chose_device.equals(UICommon.LANDY_DEVICE)){
				api.encrtyPinkey(lanyTrackMsg);
			}
		   //安付加密
			
			if(chose_device.equals(UICommon.AF_DEVICE)){
				api.encrtyPinkey(password);
			}
			//财付通加密
			if(chose_device.equals(UICommon.CFT_DEVICE)){
				api.encrtyPinkey(password);
			}
			//bbposiIc加密
			if(chose_device.equals(UICommon.BBPOS_IC_DEVICE)){
				api.encrtyPinkey(password);
			}
		}
		
		if(v==btn_help){
			this.startActivity(new Intent(NewSwipActivity.this, WebViewActivity.class));
		}
		if(v==iv_back){
			finish();
		}
		if(v==tv_search){
			try {
				startDiscovery();
			} catch (Exception e) {
				// TODO: handle exception
			}
			
		}
		
		
	}
	
	// 弹出已配对蓝牙对话框,点击链接相应设备
			public void selectBtAddrToInit() {

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
							dialog.dismiss();
							String address = discoveredDevices.get(which).address;
							String bluename=discoveredDevices.get(which).name;
							bluetoothDevice=new BluetoothDeviceContext(bluename, address);
							if(chose_device.equals(UICommon.LANDY_DEVICE)||chose_device.equals(UICommon.AF_DEVICE)){
								  tv_tip.setText("尝试连接蓝牙");
								  im_shuaka.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("ly_cash_landy"));
							}
							if(chose_device.equals(UICommon.LANDY_DEVICE)){
								if(bluename.startsWith("M18")){
									loadLandySDK(mainHandler);
								}
								if(bluename.startsWith("L")){
									loadAfSDK(mainHandler);
								}
							}
							if(chose_device.equals(UICommon.WFT_DEVICE)){
								loadWftSDK(mainHandler);
							}
							
							NewSwipActivity.this.getSharedPreferences(BasicActivity.KALAI_COMMON_SAVE,0).edit().putString(BasicActivity.CHOSED_BLUETOOTH_ADDRESS,address).commit();
							NewSwipActivity.this.getSharedPreferences(BasicActivity.KALAI_COMMON_SAVE,0).edit().putString(BasicActivity.CHOSED_LAST_DEVICE_NAME,bluename).commit();
							} catch (Exception e) {
							//appendInteractiveInfoAndShow("控制器初始化失败!");
							Tip("未连接正确的蓝牙设备，控制器初始化失败");
						} finally {
							
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
								dialog.show();}
						   });
				
			}

}
