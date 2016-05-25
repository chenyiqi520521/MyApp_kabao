package com.apicloud.swip;

import java.util.ArrayList;
















import com.apicloud.activity.BaseActivity;
import com.apicloud.adapter.SearchBlueAdapter;
import com.apicloud.landy.LandyTackMsg;
import com.apicloud.module.BluetoothDeviceContext;
import com.apicloud.util.UICommon;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class BlueToothSearchActivity extends Activity  implements OnClickListener {
	TextView tv_title,btn_search,btn_sure;
	ListView lv_device;
	ProgressBar pb;
	private int fromAct = -1;
	int device_index=-1;//选择的蓝牙设备的序号
	String chose_device="";//选择设备的类型
	private String amount="";
	private int whatDo=-1;
	ArrayList<BluetoothDeviceContext> blueToothList=new ArrayList<BluetoothDeviceContext>();
	@SuppressLint("NewApi")
	public BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	SearchBlueAdapter adapter=null;
	private final BroadcastReceiver discoveryReciever = new BroadcastReceiver() {
		@SuppressLint("NewApi")
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {

				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (ifAddressExist(device.getAddress())) {
					return;
				}
                BluetoothDeviceContext 	btContext = new BluetoothDeviceContext(device.getName() == null ? device.getAddress() : device.getName(), device.getAddress());
				blueToothList.add(btContext);
				adapter.notifyDataSetChanged();
				

			}
		}
	};
	/**
	 * 检查是蓝牙地址是否已经存在
	 * 
	 * @return
	 */
	private boolean ifAddressExist(String addr) {
		for (BluetoothDeviceContext devcie :blueToothList) {
			if (addr.equals(devcie.address))
				return true;
		}
		return false;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.bluetoothsearch);
		setContentView(UZResourcesIDFinder.getResLayoutID("bluetoothsearch"));
		initView();
	}
	
	void initView(){
		
		if(this.getIntent()!=null){
			fromAct=this.getIntent().getIntExtra("fromAct", -1);
			amount=this.getIntent().getStringExtra("amount")+"";
			whatDo=this.getIntent().getIntExtra("whatDo", SwipApi.WHATDO_SWIPER);
			chose_device=this.getIntent().getStringExtra("chose_device")+"";
		}
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(discoveryReciever, filter);
		
		tv_title=(TextView) this.findViewById(UZResourcesIDFinder.getResIdID("tv_title"));
		tv_title.setText("设备绑定");
		pb=(ProgressBar) this.findViewById(UZResourcesIDFinder.getResIdID("pg"));
		btn_search=(TextView) this.findViewById(UZResourcesIDFinder.getResIdID("btnSearch"));
		btn_sure=(TextView) this.findViewById(UZResourcesIDFinder.getResIdID("btnConfirm"));
		btn_search.setOnClickListener(this);
		btn_sure.setOnClickListener(this);
		lv_device=(ListView) this.findViewById(UZResourcesIDFinder.getResIdID("lv_device"));
		adapter=new SearchBlueAdapter(BlueToothSearchActivity.this,blueToothList,lv_device);
		lv_device.setAdapter(adapter);
		
		lv_device.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long l) {
				device_index=position;
				
				ListView lv=(ListView) parent;
				int count=lv.getChildCount();
				if(count>0){
					for(int i=0;i<count;i++){
						View v=lv.getChildAt(i);
						if(v!=null){
						  LinearLayout ll=(LinearLayout) v.findViewById(UZResourcesIDFinder.getResIdID("ll"));
						  ll.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("bluetooth_normal"));
						  TextView tv=(TextView) v.findViewById(UZResourcesIDFinder.getResIdID("blt"));
						  tv.setTextColor(UZResourcesIDFinder.getResColorID("black"));
						   
						}
						
					}
				}
				
				int children=position-lv.getFirstVisiblePosition();
				View v=lv.getChildAt(children);
				if(v!=null){
					 LinearLayout ll=(LinearLayout) v.findViewById(UZResourcesIDFinder.getResIdID("ll"));
					 ll.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("bluetooth_chosed"));
					  TextView tv=(TextView) v.findViewById(UZResourcesIDFinder.getResIdID("blt"));
					  tv.setTextColor(UZResourcesIDFinder.getResColorID("white"));
				}
				
				
				
			}
		});
		}
	/**
	 * 启动蓝牙搜索
	 */
	@SuppressLint("NewApi")
	private void startDiscovery() {
		if (bluetoothAdapter.isEnabled()) {
			bluetoothAdapter.startDiscovery();
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(1000*30);
					} catch (InterruptedException e) {
					} finally {
						
						bluetoothAdapter.cancelDiscovery();
					}
					try {
						
						
					} catch (Exception e) {
						
					}
				}
			}).start();
		} else {
			
			Toast.makeText(BlueToothSearchActivity.this,"您还未开启蓝牙",Toast.LENGTH_SHORT).show();
		}

	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		//Toast.makeText(BlueToothSearchActivity.this, "request-->"+requestCode+"--response--"+resultCode, Toast.LENGTH_SHORT).show();
		//刷卡交易成功
		if(requestCode==SWIP_REQUEST&&resultCode==SwipActivity.RESPONSE_SWIP_SUCCESS){
			    LandyTackMsg lanyTrackMsg=(LandyTackMsg) data.getSerializableExtra("data");
			    Log.v("landy1", "cardno--blu"+lanyTrackMsg.cardNo);
			    int fromAct1=data.getIntExtra("fromAct", -1);
			    Intent responseIntent=new Intent(BlueToothSearchActivity.this,BaseActivity.class);
			    responseIntent.putExtra("data", lanyTrackMsg);
			    responseIntent.putExtra("fromAct",fromAct1);
				setResult(resultCode, responseIntent);
				finish();
		}
		//获取卡号成功
		if(requestCode==SWIP_REQUEST&&resultCode==SwipActivity.RESPONSE_GET_CARD_NO_SUCESS){
				LandyTackMsg lanyTrackMsg=(LandyTackMsg) data.getSerializableExtra("data");
				int fromAct1=data.getIntExtra("fromAct", -1);
			    Intent responseIntent=new Intent(BlueToothSearchActivity.this,BaseActivity.class);
			    responseIntent.putExtra("data", lanyTrackMsg);
			    responseIntent.putExtra("fromAct",fromAct1);
				setResult(resultCode, responseIntent);
				finish();
		}
		//任务失败
		if(requestCode==SWIP_REQUEST&&resultCode==SwipActivity.RESPONSE_WORK_FAIL){
			    Intent responseIntent=new Intent(BlueToothSearchActivity.this,BaseActivity.class);
			   
				setResult(resultCode, responseIntent);
				finish();
		}
	}
	public static int SWIP_REQUEST=500;
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v==btn_search){
			pb.setVisibility(View.VISIBLE);
			startDiscovery();
		}else if(v==btn_sure){
			Bundle b = new Bundle();
			b.putInt("fromAct", fromAct);
			b.putString("chose_device", chose_device);
			b.putString("amount",amount);
			b.putInt("whatDo", whatDo);
			BluetoothDeviceContext bdc=blueToothList.get(device_index);
			b.putSerializable("bluetooth", bdc);
			Intent intent=new Intent(BlueToothSearchActivity.this,SwipActivity.class);
			intent.putExtras(b);
			startActivityForResult(intent, SWIP_REQUEST);
			
			
		}
		
	}
	/*@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			UiCommon.INSTANCE.showActivity(UiCommon.ACTIVITY_IDX_HOME, null);
		    finish();
			
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}*/

}
