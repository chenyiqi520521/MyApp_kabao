package com.apicloud.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.apicloud.landy.LandyTackMsg;
import com.apicloud.swip.SwipApi;
import com.apicloud.util.UICommon;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

public class BindEquipmentActivity extends BasicActivity implements OnClickListener{
	private ImageButton ib_return;
	private TextView tv_bindDev;
	String chose_device="E";
	String csn;//设备序列号
	private static final String BIND_EQUI = "bind_equi";
	public static final int REQUEST_AS_BIND=200;
	private String key;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("bind_equipment"));
		initView();
		initParentView();
		initSwipParam();
	}
	void initSwipParam(){
		fromAct = UICommon.BindEquipmentActivity;
		whatDo = SwipApi.WHATDO_GET_CARDNO;
		cur_Ac = this;
	}
	private void initView(){
		ib_return = (ImageButton) findViewById(UZResourcesIDFinder.getResIdID("ib_return"));
		tv_bindDev = (TextView) findViewById(UZResourcesIDFinder.getResIdID("tv_getDev"));
		key = getIntent().getStringExtra("key");
		ib_return.setOnClickListener(this);
		tv_bindDev.setOnClickListener(this);
		setGetCardNum(tv_bindDev);
		CHOSE_DEVICE=chose_device;
	}
	public void setCardNo(Object obj){
		if(CHOSE_DEVICE.equals(UICommon.LANDY_DEVICE)||CHOSE_DEVICE.equals(UICommon.WFT_DEVICE)){
			LandyTackMsg ltm=(LandyTackMsg)obj;
			Intent intent = new Intent(BindEquipmentActivity.this,BindEquipmentFinalActivity.class);
			csn=ltm.ksn+"";
			intent.putExtra("chose_device", CHOSE_DEVICE+"");
			intent.putExtra("device_num", csn+"");
			intent.putExtra("lkey",key);
			intent.putExtra("shopno", bind_shopno+"");
			intent.putExtra("fromActivity", BIND_EQUI);
			startActivityForResult(intent,REQUEST_AS_BIND);
		}
	}
	@Override
	public void onClick(View v) {
		if(v==ib_return){
			finish();
		}
		if(v==tv_bindDev){
			
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode==BindEquipmentFinalActivity.RESULT_BIND_SUCCESS||resultCode==BindEquipmentFinalActivity.RESULT_BIND_FAILURE){
			finish();
		}
	}
}
