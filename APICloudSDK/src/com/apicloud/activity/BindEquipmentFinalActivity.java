package com.apicloud.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.apicloud.controller.MyController;
import com.apicloud.module.LoginBean;
import com.apicloud.module.MsgBean;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

public class BindEquipmentFinalActivity extends Activity implements OnClickListener{
	private ImageView ib_return;
	private TextView tv_deviceNo;
	private TextView tv_bind;
	private MyController controller;
	private String device_num = "";
	
	private String shopno = "";
	private String chose_device="";
	private String text_bind_button="";
	private ProgressDialog pg;
	public static final int RESULT_BIND_SUCCESS = 201;
	public static final int RESULT_BIND_FAILURE = 202;
	public static final String SAVED_BIND_FINAL = "saved_bind_final";
	public static final String KALAI_SAVE = "kalai_save";
	public static final String BIND_ADDRESS = "bind_address";
	
	public static final String BANK_SHOPNO = "10010001";
	private LoginBean loginBean;
	public static final String WELCOME = "welcomeIndex";
	private String lkey="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("bind_equipment_final"));
		initView();
	}
	
	private void initView(){
		pg = new ProgressDialog(BindEquipmentFinalActivity.this);
		pg.setCancelable(false);
		controller = new MyController(getApplicationContext());
		ib_return = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("ib_return"));
		tv_deviceNo =(TextView) findViewById(UZResourcesIDFinder.getResIdID("tv_deviceid"));
		tv_bind = (TextView) findViewById(UZResourcesIDFinder.getResIdID("tv_bind"));
		ib_return.setOnClickListener(this);
		tv_bind.setOnClickListener(this);
		if(this.getIntent()!=null){
			if(this.getIntent().getStringExtra("fromActivity").equals(WELCOME)){
				text_bind_button = getIntent().getStringExtra("text_bind_button");
			    tv_bind.setText(text_bind_button);
			}
			lkey = getIntent().getStringExtra("lkey");
			device_num = getIntent().getStringExtra("device_num");
		    chose_device = getIntent().getStringExtra("chose_device");
		    tv_deviceNo.setText("设备编号:"+device_num);
		}
	}
	private void handleResult(final MsgBean mb){
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					if(mb.RspCd.equals("0")){
						Toast.makeText(BindEquipmentFinalActivity.this, "绑定成功", Toast.LENGTH_SHORT).show();
						BindEquipmentFinalActivity.this.getSharedPreferences(KALAI_SAVE, 0).edit().putString(SAVED_BIND_FINAL, BIND_ADDRESS).commit();
						Intent intent = new Intent(BindEquipmentFinalActivity.this,BindEquipmentActivity.class);
						setResult(RESULT_BIND_SUCCESS, intent);
						finish();
					}else{
						Toast.makeText(BindEquipmentFinalActivity.this, "绑定失败:"+mb.RspMsg, Toast.LENGTH_SHORT).show();
						Intent intent = new Intent(BindEquipmentFinalActivity.this,BindEquipmentActivity.class);
						setResult(RESULT_BIND_FAILURE, intent);
						finish();
					}
					
				}
			});
		
	}
	@Override
	public void onClick(View v) {
		if(v==ib_return){
			finish();
		}
		if(v==tv_bind){
			pg.show();
			pg.setMessage("绑定请求发送中...请稍后");
			new Thread(){
				@Override
				public void run() {
					
					super.run();
					
					MsgBean msg  = controller.bindDevice(device_num, BANK_SHOPNO, lkey);
					//处理返回的数据
					handleResult(msg);
				}
			}.start();
			
		}
	}
}
