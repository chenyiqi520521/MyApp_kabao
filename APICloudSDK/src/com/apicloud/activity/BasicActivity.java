package com.apicloud.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.apicloud.activity.topup.NotCardHeadDialog;
import com.apicloud.activity.topup.TopUpNewActivity;
import com.apicloud.activity.topup.TopUpTwoActivity;
import com.apicloud.activity.write.WritePadActivity;
import com.apicloud.controller.Controller;
import com.apicloud.controller.DeviceController;
import com.apicloud.controller.MyController;
import com.apicloud.impl.DeviceControllerImpl;
import com.apicloud.landy.LandyTackMsg;
import com.apicloud.module.BankCrad;
import com.apicloud.module.BluetoothDeviceContext;
import com.apicloud.module.CreditCardBean;
import com.apicloud.module.MsgBean;
import com.apicloud.swip.NewSwipActivity;
import com.apicloud.swip.SwipActivity;
import com.apicloud.swip.SwipApi;
import com.apicloud.util.Configure;
import com.apicloud.util.UICommon;
import com.apicloud.util.commonUtil;
import com.location.LocationSvc;

public class BasicActivity extends Activity {
	public static final String KALAI_COMMON_SAVE = "common_save";// sharedpreference存储位置
	public static final String CHOSED_BLUETOOTH_ADDRESS = "chosed_bluetooth_address";// 存储上次蓝牙连接地址
	public static final String CHOSED_LAST_DEVICE_TYPE = "chosed_last_device_type";// 存储上次设备类型
	public static final String CHOSED_LAST_DEVICE_NAME = "chosed_bluetooth_name";// 上次的名字
	protected MyController controller;
	protected Handler dialogHandler, blueToothHandler, parentHandler;
	protected TextView tv_getCardNum;
	protected String CHOSE_DEVICE = "E";// 表示蓝迪设备
	public BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	public List<BluetoothDeviceContext> discoveredDevices = new ArrayList<BluetoothDeviceContext>();
	protected ProgressDialog pg;
	protected NotCardHeadDialog cardHeadDialog;
	protected String deviceToConnect;
	public EditText et_parent_code;
	protected int fromAct = -1;// 来自哪个页面
	protected int whatDo = 2;// 刷卡
	protected Activity cur_Ac = null;
	public TextView amount_et;
	protected boolean needShowMoney = false;
	public static final int SWIP_REQUEST = 500;// 插刷请求
	private boolean connected = false;
	protected String bind_lkey = "";
	protected String bind_shopno = "";
	public EditText et_code_parent;
	public String parent_lkey = "";
	protected boolean needTime = false;
	protected DeviceController deviceController = DeviceControllerImpl.getInstance();
	public static String BlueToothPsd = "";// 蓝牙时获取输入的密码
	public static final int WRITEPAD = 300;// 签名请求
	protected String locationInfo = "30.290388-120.134746";
	public String sign_path = "";// 签名未合成的路径
	protected BankCrad bankCradParent;// 银行卡刷出信息
	protected boolean needShowMoeny = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		if (!commonUtil.isGpsEnable(BasicActivity.this)) {
			Tip("请开启GPS");
			commonUtil.setOpenGPS(BasicActivity.this);
			finish();
		}
		controller = new MyController(BasicActivity.this);
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(discoveryReciever, filter);
		IntentFilter filter1 = new IntentFilter();
		filter1.addAction(com.location.Common1.LOCATION_ACTION);
		this.registerReceiver(new LocationBroadcastReceiver(), filter1);
	}

	private class LocationBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(com.location.Common1.LOCATION_ACTION)) {
				locationInfo = intent.getStringExtra(com.location.Common1.LOCATION);
				// Log.v("loc1",locationInfo+"");
			}

		}
	}
	protected void startLocation() {
		Intent intent = new Intent();
		intent.setClass(this, LocationSvc.class);
		startService(intent);
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

	protected void setGetCardNum(TextView tv) {
		this.tv_getCardNum = tv;
	}

	protected void setParentHandler(Handler handler) {
		this.parentHandler = handler;
	}

	void initBlueToothHandler() {
		blueToothHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				if (msg.what == NotCardHeadDialog.RESTART_SEARCH_BLUETOOTH) {
					startDiscovery(CHOSE_DEVICE);
				}
			}
		};
	}

	protected void DealConnectedMessage(Handler handler) {
		if (CHOSE_DEVICE.equals("A")) {
			handler.sendEmptyMessage(Configure.KT_CONNECTED);
		} else if (CHOSE_DEVICE.equals("B")) {
			handler.sendEmptyMessage(Configure.LY_CONNECTED);
		} else if (CHOSE_DEVICE.equals("C")) {
			handler.sendEmptyMessage(Configure.DH_CONNECTED);
		} else if (CHOSE_DEVICE.equals("D")) {
			handler.sendEmptyMessage(Configure.ME15_CONNECTED);
		}
	}

	protected void DealDisConnectedMessage(Handler handler) {

		if (CHOSE_DEVICE.equals("A")) {
			handler.sendEmptyMessage(Configure.KT_DISCONNECTED);
		} else if (CHOSE_DEVICE.equals("B")) {
			handler.sendEmptyMessage(Configure.LY_DISCONNECTED);
		} else if (CHOSE_DEVICE.equals("C")) {
			handler.sendEmptyMessage(Configure.DH_DISCONNECTED);
		} else if (CHOSE_DEVICE.equals("D")) {
			handler.sendEmptyMessage(Configure.ME15_DISCONNECTED);
		}

	}

	public String UnpackTrack(byte[] track2) {
		String encodedTrack2 = new String(track2);
		return encodedTrack2.replace("=", "d");
	}

	public String SetTrackData(final String data2) {
		// String data2 = track2Data;
		String datadata = data2.replaceAll(" ", "");
		String replaceData;

		if ('f' == datadata.charAt(datadata.length() - 1) || 'F' == datadata.charAt(datadata.length() - 1)) {
			replaceData = datadata.substring(0, datadata.length() - 1);
		} else {
			replaceData = datadata;
		}

		return replaceData;
	}

	// 随机生成
	public String getHex_workkey() {
		String workkey = "";
		for (int i = 0; i < 16; i++) {
			workkey += Integer.toHexString(new Random().nextInt(16));
		}
		return workkey.toUpperCase();
	}

	protected void swipShow() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				cardHeadDialog.swipShow();

			}

		});

	}

	private void startDiscovery(final String type) {
		if (bluetoothAdapter.isEnabled()) {
			if (pg != null) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						try {
							pg.show();
						} catch (Exception e) {
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
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						bluetoothAdapter.cancelDiscovery();
					}
					try {
						pg.dismiss();
						if (discoveredDevices.size() <= 0) {
							Tip("未找到匹配蓝牙");
						} else {
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

	public void selectBtAddrToInit(final String type) {
		// 收集蓝牙搜索到的信息
		int i = 0;
		final String[] blueNames = new String[discoveredDevices.size()];
		for (BluetoothDeviceContext device : discoveredDevices) {
			blueNames[i++] = device.name;
		}
		// 弹出对话框
		final Builder builder = new AlertDialog.Builder(BasicActivity.this);
		builder.setTitle("请选择蓝牙设备");
		builder.setSingleChoiceItems(blueNames, 0, new AlertDialog.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					deviceToConnect = discoveredDevices.get(which).address;
					// 连接型号判定
					connectTrue = checkAddress(blueNames[which], dialog);
					if (connectTrue) {
						BasicActivity.this.getSharedPreferences(KALAI_COMMON_SAVE, 0).edit().putString(CHOSED_BLUETOOTH_ADDRESS, discoveredDevices.get(which).address).commit();
						if (type.equals("C")) {
							// 建设中..
						}
					}
				} catch (Exception e) {
					Tip("未连接正确的蓝牙设备，控制器初始化失败");

				} finally {
					if (connectTrue) {
						dialog.dismiss();
						if (type.equals("B") || type.equals("C")) {
							// 建设中..
						}
					}
					cardHeadDialog.show();
				}
			}
		});
		// show
		// runOnUiThread(new Runnable(){
		//
		// @Override
		// public void run() {
		//
		// Dialog dialog = builder.create();
		// dialog.setOnDismissListener(new OnDismissListener() {
		// @Override
		// public void onDismiss(DialogInterface dialog) {
		// //checkBtnState();
		// }
		// });
		// dialog.show();
		//
		//
		//
		// }
		//
		// });
	}

	boolean checkAddress(String address, DialogInterface dialog) {
		boolean temp = true;
		try {
			if (CHOSE_DEVICE.equals("B")) {
				if (!address.contains("ME30")) {
					dialog.dismiss();
					Tip("选择了错误蓝牙设备");
					startDiscovery(CHOSE_DEVICE);
					temp = false;

				}

			} else if (CHOSE_DEVICE.equals("C")) {
				if (!address.startsWith("MPOS")) {
					dialog.dismiss();
					Tip("选择了错误蓝牙设备");
					startDiscovery(CHOSE_DEVICE);
					temp = false;

				}

			} else if (CHOSE_DEVICE.equals("D")) {
				if (!address.contains("ME15")) {
					dialog.dismiss();
					Tip("选择了错误蓝牙设备");
					startDiscovery(CHOSE_DEVICE);
					temp = false;

				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return temp;

	}

	protected void Tip(final String msg) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(BasicActivity.this, msg, Toast.LENGTH_LONG).show();

			}
		});
	}

	//
	protected void initParentView() {
		cardHeadDialog = new NotCardHeadDialog(BasicActivity.this, CHOSE_DEVICE);
		initBlueToothHandler();
		cardHeadDialog.setHandler(blueToothHandler);
		if (CHOSE_DEVICE.equals("D")) {
			// 建设中...
		}
		if (CHOSE_DEVICE.equals("B") || CHOSE_DEVICE.equals("D")) {
			// 建设中
		}
		// 获取上次设备地址
		String last_device = BasicActivity.this.getSharedPreferences(KALAI_COMMON_SAVE, 0).getString(CHOSE_DEVICE, CHOSED_LAST_DEVICE_TYPE);
		if (!last_device.equals(CHOSE_DEVICE)) {
			BasicActivity.this.getSharedPreferences(KALAI_COMMON_SAVE, 0).edit().putString(CHOSED_BLUETOOTH_ADDRESS, "").commit();
		}
		BasicActivity.this.getSharedPreferences(KALAI_COMMON_SAVE, 0).edit().putString(CHOSED_LAST_DEVICE_TYPE, CHOSE_DEVICE).commit();

		if (!CHOSE_DEVICE.equals("A")) {
			pg = new ProgressDialog(BasicActivity.this);
			pg.setMessage("正在搜索可配对蓝牙");
			tv_getCardNum.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					if (et_code_parent != null) {
						final String code1 = et_code_parent.getText().toString() + "";
						if (code1.length() >= 0) {
							new Thread() {
								public void run() {
									if (!controller.validateCode(code1, parent_lkey)) {
										Tip("验证码有误");
										return;
									} else {
										runOnUiThread(new Runnable() {

											@Override
											public void run() {
												// TODO Auto-generated method
												// stub
												doWork();
											}

										});

									}
								};
							}.start();
						}
					} else {
						doWork();
					}

				}
			});

		} else {

		}
	}

	protected void goToHanwriting(CreditCardBean creditCardBean, MsgBean msgBean, String amount) {
		Intent intent = new Intent();
		intent.setClass(getApplicationContext(), WritePadActivity.class);
		intent.putExtra("amount", amount);
		/*
		 * Bundle bundle = new Bundle(); intent.putExtra("uid",
		 * getIntent().getStringExtra("uid")); intent.putExtra("name", "");
		 * intent.putExtra("merchant", ""); intent.putExtra("terminal", "");
		 * intent.putExtra("cardNo", creditCardBean.acctNo);
		 * intent.putExtra("iss", ""); intent.putExtra("amount",
		 * getIntent().getStringExtra("money")); intent.putExtra("ReferNO",
		 * msgBean.ReferNO); intent.putExtra("TransDate", msgBean.TransDate);
		 * intent.putExtra("TransTime", msgBean.TransTime);
		 * intent.putExtras(bundle);
		 */
		startActivityForResult(intent, WRITEPAD);
	}

	public void setParentCode(EditText parentCode, String lkey) {
		parent_lkey = lkey;
		et_code_parent = parentCode;
	}

	void doWork() {
		if (tv_getCardNum.getText().toString().contains("确定") || tv_getCardNum.getText().toString().contains("查询") && (fromAct != UICommon.KuailainActivity)) {
			Message msg = parentHandler.obtainMessage();
			msg.obj = "sure";
			parentHandler.sendMessage(msg);
		} else {
			if (needShowMoney && Controller.isEmpty(amount_et.getText().toString().trim())) {
				Tip("交易金额不为空");
				return;
			}
			if (CHOSE_DEVICE.equals(UICommon.LANDY_DEVICE) || CHOSE_DEVICE.equals(UICommon.AF_DEVICE) || CHOSE_DEVICE.equals(UICommon.CFT_DEVICE) || CHOSE_DEVICE.equals(UICommon.BBPOS_IC_DEVICE) || CHOSE_DEVICE.equals(UICommon.WFT_DEVICE)) {
				Intent intent = new Intent(BasicActivity.this, NewSwipActivity.class);
				intent.putExtra("fromAct", fromAct);
				if (whatDo == SwipApi.WHATDO_SWIPER) {
					String amount = amount_et.getText().toString().trim() + "";
					if (amount.length() > 0) {
						intent.putExtra("amount", amount);
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

	void blueToothSearchAndConnected(String type) {
		// Tip("show1");
		if (cardHeadDialog != null) {
			cardHeadDialog.show();
		}
		final String address = BasicActivity.this.getSharedPreferences(KALAI_COMMON_SAVE, 0).getString(CHOSED_BLUETOOTH_ADDRESS, "");
		connected = false;
		// Tip("show1");

		// Tip("show2");
		if (address.length() > 0) {
			// 建设中
			//
			// try {
			// if(type.equals("C")){
			// dhcontroller.connect(address);
			// dialogTip(READY_TO_CONNECT);
			// if(cardHeadDialog!=null){
			// cardHeadDialog.show();
			// }
			// }
			//
			// if(type.equals("B")||type.equals("D")){
			// //final String
			// address=BaseActivity.this.getSharedPreferences(KALAI_COMMON_SAVE,0).getString(CHOSED_BLUETOOTH_ADDRESS,"");
			// try {
			// /* if(cardHeadDialog!=null){
			// cardHeadDialog.show();
			// }*/
			// //Toast.makeText(BaseActivity.this,
			// "尝试连接",Toast.LENGTH_LONG).show();
			// //initMe3xDeviceController(new
			// BlueToothV100ConnParams(address),INIT_BLUETOOTH);
			// try {
			// /*if(cardHeadDialog!=null){
			// cardHeadDialog.show();
			// }*/
			// //controller.connect();
			// } catch (Exception e) {
			// //Tip("连接失败，未配对正确设备");
			// }
			// //pg1.dismiss();
			//
			// if(DeviceConnState.CONNECTED != controller.getDeviceConnState()){
			//
			//
			// startDiscovery(type);
			//
			// }else{
			// Message msgMessage=parentHandler.obtainMessage();
			// msgMessage.what=Common.FETCH_DEVICE_INFO;
			// parentHandler.sendMessage(msgMessage);
			// if(cardHeadDialog!=null){
			// cardHeadDialog.show();
			// }
			//
			// }
			//
			// } catch (Exception e) {
			// // TODO: handle exception
			// }finally{
			//
			// }
			//
			//
			// }
			//
			// } catch (Exception e) {
			// Tip("您默认的蓝牙连接失败，开启蓝牙搜索");
			// //startDiscovery();
			// }finally{
			//
			//
			//
			//
			// }
		} else {

			startDiscovery(type);
		}

	}

	protected void swipHidden() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				cardHeadDialog.swipHiden();

			}

		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		// 从签名页面返回提交数据

		if (requestCode == WRITEPAD && resultCode == WritePadActivity.WRITING_RESULT_CODE) {
			if (data != null) {
				sign_path = data.getStringExtra("sign_path");
				// 向后台发起请求
				Message msg = parentHandler.obtainMessage();
				msg.obj = "签名完毕";
				parentHandler.sendMessage(msg);
			}

		}
		if (requestCode == SWIP_REQUEST && resultCode == SwipActivity.RESPONSE_SWIP_SUCCESS) {
			LandyTackMsg lanyTrackMsg = (LandyTackMsg) data.getSerializableExtra("data");
			int cur_fromAct = data.getIntExtra("fromAct", -1);
			//如果是充值
			if(cur_fromAct == UICommon.TopUpThreeActivity){
				((TopUpNewActivity)cur_Ac).ModelSubmit(lanyTrackMsg);
			}
			// 如果是实时收款
			if (cur_fromAct == UICommon.RealTimeActivity) {
				((RealTimeActivity) cur_Ac).ModelSubmit(lanyTrackMsg);
			}
		}
		// 获取卡号成功
		if (requestCode == SWIP_REQUEST && resultCode == NewSwipActivity.RESPONSE_GET_CARD_NO_SUCESS) {
			LandyTackMsg landyTrackMsg = (LandyTackMsg) data.getSerializableExtra("data");
			int cur_fromAct = data.getIntExtra("fromAct", -1);
			if (cur_fromAct == UICommon.AddNewCardActivity) {
				((AddNewCardActivity) cur_Ac).setCardNo(landyTrackMsg);
			}
			if (cur_fromAct == UICommon.BindEquipmentActivity) {
				((BindEquipmentActivity) cur_Ac).setCardNo(landyTrackMsg);
			}
			if (cur_fromAct == UICommon.AddNewCreditActivity) {
				((AddNewCreditActivity) cur_Ac).setCardNo(landyTrackMsg);
			}
		}
	}
}
