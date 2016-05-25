package com.apicloud.activity;

import com.apicloud.controller.Controller;
import com.apicloud.module.MsgBean;
import com.apicloud.util.UICommon;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

//最后绑定设备的页面
public class BindDeviceFinalActivity extends Activity  implements OnClickListener{
	ImageButton ib_return;// 返回
	TextView tv_device_num;
	TextView tv_binddevice;
	
	String lkey="";
	String shopno="";
	String device_num="";
	String chose_device="";
	Controller controller2;// 控制器
	protected ProgressDialog loadDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("bind_device_final"));
		initView();
		
	}
	/**
	 * 
	 * TODO初始化控件
	   2015年12月7日
	   void
	 */
	void initView(){
		loadDialog=new ProgressDialog(BindDeviceFinalActivity.this);
		loadDialog.setCancelable(false);
		controller2 = new Controller(getApplicationContext());
		tv_device_num=(TextView) findViewById(UZResourcesIDFinder.getResIdID("tv_deviceid"));
		tv_binddevice=(TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_bind"));
		tv_binddevice.setOnClickListener(this);
		ib_return = (ImageButton) findViewById(UZResourcesIDFinder.getResIdID("ib_return"));
		ib_return.setOnClickListener(this);
		if(this.getIntent()!=null){
			device_num=this.getIntent().getStringExtra("device_num");
			lkey=this.getIntent().getStringExtra("lkey");
			shopno=this.getIntent().getStringExtra("shopno");
			chose_device=this.getIntent().getStringExtra("chose_device");
				tv_device_num.setText("设备编号:"+device_num);
				if(chose_device.equals(UICommon.CFT_DEVICE)){
					device_num=device_num.substring(0, 14);
					tv_device_num.setText(device_num+"");
				
		
				
			}
		}
	}
	
	public static final int RESPONSE_FOR_BIND_SUCCESS=205;//设备绑定成功
	public static final int RESPONSE_FOR_BIND_FAIL=206;//设备绑定失败
	//对http请求的返回做出处理
	void handle(final MsgBean msgbean){
		runOnUiThread(new Thread(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				loadDialog.dismiss();
				
				//绑定成功
				if(msgbean.RspCd.equals("0")){
					
					Toast.makeText(BindDeviceFinalActivity.this, "绑定成功", Toast.LENGTH_LONG).show();
					Intent intent=new Intent(BindDeviceFinalActivity.this,BindDeviceActivity.class);
					setResult(RESPONSE_FOR_BIND_SUCCESS, intent);
					finish();
				}else{//绑定失败,提示错误原因
					Intent intent=new Intent(BindDeviceFinalActivity.this,BindDeviceActivity.class);
					setResult(RESPONSE_FOR_BIND_FAIL, intent);
					finish();
					Toast.makeText(BindDeviceFinalActivity.this,"绑定失败:"+msgbean.RspMsg,Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	/**
	 * 点击处理
	 */
	@Override
	public void onClick(View v) {
		//如果点击绑定设备
		if(v==tv_binddevice){
		    loadDialog.setMessage("绑定中，请稍后");
		    loadDialog.show();
			//子线程发起绑定请求
			new Thread(){
				public void run() {
					MsgBean msgbean=controller2.bindDevice(device_num, shopno, lkey);
					handle(msgbean);
					
				};
			}.start();
		}
		//点击返回键
		if(v==ib_return){
			finish();
		}
		
	}

}
