package com.apicloud.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apicloud.controller.MyController;
import com.apicloud.module.CardBean;
import com.apicloud.module.LoginBean;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

public class RealTimeActivity extends BasicActivity implements OnClickListener{
	private LinearLayout layout_add_card;
	private ImageButton ib_return;
	private EditText ed_crad_amount;
	private EditText et_code;
	private TextView tv_code;
	private Button btn_refresh;
	private TextView btn_ok;
	private CardBean cardBean;
	private String key;
	public static final int REQUEST_ADD_CREDIT = 103;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("realtime_account"));
		controller = new MyController();
		initView();
	}
	
	public void initView(){
		layout_add_card = (LinearLayout) findViewById(UZResourcesIDFinder.getResIdID("layout_add_card"));
		ib_return = (ImageButton) findViewById(UZResourcesIDFinder.getResIdID("ib_return"));
		ed_crad_amount = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_crad_amount"));//金额
		et_code = (EditText) findViewById(UZResourcesIDFinder.getResIdID("et_code"));//输入的验证码
		tv_code = (TextView)findViewById(UZResourcesIDFinder.getResIdID("tv_code"));//动态显示的验证码
		btn_refresh =(Button)findViewById(UZResourcesIDFinder.getResIdID("btn_refresh"));//验证码刷新按钮
		btn_ok = (TextView)findViewById(UZResourcesIDFinder.getResIdID("btn_ok"));//确认支付
		setGetCardNum(btn_ok);
		ib_return.setOnClickListener(this);
		layout_add_card.setOnClickListener(this);
		btn_refresh.setOnClickListener(this);
		btn_ok.setOnClickListener(this);
		getPaymentsData();
		
	}
	private void getPaymentsData(){
		new Thread(){
			@Override
			public void run() {
				
				super.run();
				final LoginBean loginBean = controller.doLogin("18602123569", "65727647", "10131001");
				key=loginBean.error;
				cardBean = controller.getCardInfo(RealTimeActivity.this, key, "rcard");
			}
		}.start();
	}
	@Override
	public void onClick(View v) {
		if(v==layout_add_card){
			Intent intent = new Intent(RealTimeActivity.this,AddNewCreditActivity.class);
			startActivityForResult(intent, REQUEST_ADD_CREDIT);
		}
		if(v==ib_return){
			finish();
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}
}
