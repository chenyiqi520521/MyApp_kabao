package com.apicloud.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.apicloud.controller.Controller;
import com.apicloud.controller.MyController;
import com.apicloud.landy.LandyTackMsg;
import com.apicloud.module.LoginBean;
import com.apicloud.module.MsgPush;
import com.apicloud.swip.SwipApi;
import com.apicloud.util.UICommon;
import com.apicloud.view.LoadingDialog;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

public class AddNewCreditActivity extends BasicActivity implements OnClickListener{
	private TextView txt_card_get;
	private ImageButton ib_return;
	private String chose_device = "E";
	private EditText ed_crad;
	private EditText ed_crad_name;
	private EditText ed_id_card_num;
	private EditText ed_phone_num;
	private EditText ed_sms_num;
	private Button btn_getsms;
	public Handler iHandler,resultHandler;
	private String phoneNo;
	private Button btn_ok;
	private String codeResult="";
	private String cardNo = "";
	private String cardName = "";
	private String idNo = "";
	private String smsNo = "";
	private ProgressDialog pd; 
	/** 如果取消 */
	int cancel = 2;
	/** 如果连接失败 */
	int connect = 4;
	/** 刷卡状态 */
	int swiper = 3;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("realtime_account_credit"));
		addMessageHandler();//添加ihandler处理事件
		initHanlder();
		initView();
		controller = new MyController();
	}
	private void initView(){
		txt_card_get = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_card_get"));
		ib_return = (ImageButton) findViewById(UZResourcesIDFinder.getResIdID("ib_return"));
		ed_crad = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_crad"));
		ed_crad_name = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_crad_name"));
		ed_id_card_num = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_id_card_num"));
		ed_phone_num = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_phone_num"));
		ed_sms_num = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_sms_num"));
		btn_getsms = (Button)findViewById(UZResourcesIDFinder.getResIdID("btn_getsms"));
		btn_ok = (Button)findViewById(UZResourcesIDFinder.getResIdID("btn_ok"));
		setGetCardNum(txt_card_get);
		btn_getsms.setOnClickListener(this);
		txt_card_get.setOnClickListener(this);
		ib_return.setOnClickListener(this);
		btn_ok.setOnClickListener(this);
		CHOSE_DEVICE = chose_device;
		initParentView();
		initSwipParam();
	}
	void initHanlder() {
		resultHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
			}

		};
	}
	public void addMessageHandler(){
		iHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
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
	
	private void initSwipParam(){
		fromAct = UICommon.AddNewCreditActivity;
		whatDo = SwipApi.WHATDO_GET_CARDNO;
		cur_Ac = this;
	}
	
	public void setCardNo(Object obj){
		if(CHOSE_DEVICE.equals(UICommon.LANDY_DEVICE)||CHOSE_DEVICE.equals(UICommon.AF_DEVICE)||CHOSE_DEVICE.equals(UICommon.CFT_DEVICE)){
			LandyTackMsg msg =(LandyTackMsg)obj;
			handleBack(msg.cardNo+"");
		}
	}
	/**
	 * 刷新卡号
	 * @param msg
	 */
	private void handleBack(final String msg){
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				ed_crad.setText(msg);
				
			}
		});
	}
	int count = 60;
	boolean stop = true;
	Runnable timer = new Runnable(){
		@Override
		public void run() {
			if(count!=0){
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						stop=false;
						btn_getsms.setText(count+"");
						count--;
						iHandler.postDelayed(timer, 1000);
					}
				});
			}else{
				btn_getsms.setClickable(true);
				btn_getsms.setText("获取验证码");
				stop=true;
				count=60;
			}
			
		}
	};
	
	@Override
	public void onClick(View v) {
		if(v==ib_return){
			finish();
		}
		if(v==txt_card_get){
			
		}
		if(v==btn_getsms){
			if(MyController.isEmpty(ed_phone_num.getText().toString().trim())){
				Toast.makeText(AddNewCreditActivity.this, "请输入手机号码", Toast.LENGTH_SHORT).show();
				return;
			}else{
			    phoneNo = ed_phone_num.getText().toString().trim();
				if(Controller.checkNetworkAvailable(getApplicationContext())){
					if(stop){
						btn_getsms.setClickable(false);
						new Thread(){
							@Override
							public void run() {
								// TODO Auto-generated method stub
								super.run();
								LoginBean loginBean = controller.doLogin("18602123569", "65727647", "10010001");
								String key = loginBean.error;
							    codeResult = controller.getSms(AddNewCreditActivity.this, phoneNo, key, iHandler);
							}
						}.start();
					}
					iHandler.post(timer);
				}else{
					Toast.makeText(AddNewCreditActivity.this, "请检查网络", Toast.LENGTH_SHORT).show();
					return;
				}
				
				
			}
			
		}
		if(v==btn_ok){
			if(MyController.isEmpty(ed_crad.getText().toString())){
				Toast.makeText(AddNewCreditActivity.this, "请刷卡取号", Toast.LENGTH_SHORT).show();
				return;
			}else if(MyController.isEmpty(ed_crad_name.getText().toString().trim())){
				Toast.makeText(AddNewCreditActivity.this, "请输入持卡人姓名", Toast.LENGTH_SHORT).show();
				return;
			}else if(MyController.isEmpty(ed_id_card_num.getText().toString().trim())){
				Toast.makeText(AddNewCreditActivity.this, "请输入持卡人身份证号", Toast.LENGTH_SHORT).show();
				return;
			}else if(MyController.isEmpty(ed_sms_num.getText().toString().trim())){
				Toast.makeText(AddNewCreditActivity.this, "请输入验证码", Toast.LENGTH_SHORT).show();
				return;
			}else if(codeResult.equals(ed_sms_num)){
				Toast.makeText(AddNewCreditActivity.this, "输入的验证码不正确", Toast.LENGTH_SHORT).show();
				return;
			}
			cardNo = ed_crad.getText().toString();
			cardName = ed_crad_name.getText().toString().trim();
			idNo = ed_id_card_num.getText().toString().trim();
			smsNo = ed_sms_num.getText().toString().trim();
			pd = new LoadingDialog(AddNewCreditActivity.this);
			pd.setCancelable(true);
			pd.setCanceledOnTouchOutside(false);
			pd.show();
				new Thread(){
					@Override
					public void run() {
						// TODO Auto-generated method stub
						super.run();
						LoginBean loginBean = controller.doLogin("18602123569", "65727647", "10010001");
						final String key = loginBean.error;
						MsgPush msg = controller.pushCard(AddNewCreditActivity.this, cardNo, cardName, MyController.CREDIT_CARD, key, idNo, phoneNo, smsNo, null, resultHandler);
						handleAddCreBack(msg);
					}
				}.start();
				
			
		}
	}
	private void handleAddCreBack(final MsgPush msg){
		pd.dismiss();
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if(msg.error.equals("0")){
					pd.dismiss();
					Toast.makeText(AddNewCreditActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
					finish();
				}else{
					pd.dismiss();
					Toast.makeText(AddNewCreditActivity.this, "添加失败--->"+msg.RspMsg, Toast.LENGTH_SHORT).show();
					return;
				}
				
			}
		});
	}
}
