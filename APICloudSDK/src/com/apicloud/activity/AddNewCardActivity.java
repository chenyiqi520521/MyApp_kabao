package com.apicloud.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apicloud.common.Common;
import com.apicloud.controller.Controller;
import com.apicloud.controller.MyController;
import com.apicloud.landy.LandyTackMsg;
import com.apicloud.module.BankBean;
import com.apicloud.module.LoginBean;
import com.apicloud.module.MsgPush;
import com.apicloud.module.MsgValidation;
import com.apicloud.swip.SwipApi;
import com.apicloud.util.UICommon;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

public class AddNewCardActivity extends BasicActivity implements OnClickListener{
	private ImageButton ib_return;
	private TextView getCardNo;
	private EditText ed_card;
	private EditText ed_search;
	private Button btn_search;
	private Button btn_ok;
	private RelativeLayout rl_parentBank;
	private EditText ed_card_name;
	private EditText ed_idcard_num;
	private EditText ed_phone_num;
	private BankBean parent_bb;
	private EditText tv_parentbankname;
	private TextView bank_crad;
	public static final int GET_PARENT_REQUEST_CODE = 100;
	public static final int GET_CHILD_REQUEST_CODE = 101;
	public static final String CHOOSE_SEARCH = "search";
	public static final String CHOOSE_PARENT = "parent";
	public static final String CHOOSE_CHILDREN = "children";
	public static final String BRANCH = "branch";
	public static final String PARENT_CODE = "parent_code";
	private BankBean child_bb;
	private MsgValidation msgValidation;
	private ProgressDialog pg;
	private MsgPush stat;
	private String lkey;
	public static Handler resultHandler;
	private LoginBean loginBean;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("add_newcard"));
		initView();
		initResultHandler();
	}
	
	public void initView(){
		ib_return = (ImageButton) findViewById(UZResourcesIDFinder.getResIdID("ib_return"));
		getCardNo = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_crad"));
		tv_parentbankname = (EditText) findViewById(UZResourcesIDFinder.getResIdID("tv_parentbankname"));
		btn_search = (Button) findViewById(UZResourcesIDFinder.getResIdID("btn_search"));
		btn_ok = (Button) findViewById(UZResourcesIDFinder.getResIdID("btn_ok"));
		rl_parentBank = (RelativeLayout) findViewById(UZResourcesIDFinder.getResIdID("rl_parentbank"));	
		ed_card = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_crad"));
		ed_search = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_search"));
		ed_card_name = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_crad_name"));
		ed_idcard_num = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_idcard_num"));
		ed_phone_num = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_phone_num"));
		bank_crad = (TextView) findViewById(UZResourcesIDFinder.getResIdID("bank_crad"));
		lkey = getIntent().getStringExtra("key");
		setGetCardNum(getCardNo);
		getCardNo.setOnClickListener(this);
		btn_search.setOnClickListener(this);
		rl_parentBank.setOnClickListener(this);
		btn_ok.setOnClickListener(this);
		ib_return.setOnClickListener(this);
		initParentView();
		initSwipParam();
	}
	public void initSwipParam(){
		fromAct = UICommon.AddNewCardActivity;
		whatDo = SwipApi.WHATDO_GET_CARDNO;
		cur_Ac=this;
	}
	@Override
	public void onClick(View v) {
		if(v==ib_return){
			finish();
		}
		if(v ==getCardNo){
			
		}
		if(v==btn_search){
			if(MyController.isEmpty(tv_parentbankname.getText().toString().trim())){
				Toast.makeText(AddNewCardActivity.this, "请选择主行", Toast.LENGTH_SHORT).show();
				return;
			}
			if(MyController.isEmpty(ed_search.getText().toString().trim())){
				Toast.makeText(AddNewCardActivity.this, "请输入关键字", Toast.LENGTH_SHORT).show();
			}
			if(MyController.checkNetworkAvailable(AddNewCardActivity.this)){
				Intent intent = new Intent(AddNewCardActivity.this,BankNameActivity.class);
				intent.putExtra(CHOOSE_SEARCH,CHOOSE_CHILDREN);
				intent.putExtra(BRANCH, ed_search.getText().toString());
				intent.putExtra(PARENT_CODE,parent_bb.getCode());
				startActivityForResult(intent, GET_CHILD_REQUEST_CODE);
			}else{
				Toast.makeText(AddNewCardActivity.this, "请检查网络设置", Toast.LENGTH_SHORT).show();
				return;
			}
		}
		if(v==rl_parentBank){
			if(MyController.checkNetworkAvailable(AddNewCardActivity.this)){
				Intent intent = new Intent(AddNewCardActivity.this,BankNameActivity.class);
				intent.putExtra(CHOOSE_SEARCH,CHOOSE_PARENT);
				startActivityForResult(intent, GET_PARENT_REQUEST_CODE);
			}else{
				Toast.makeText(AddNewCardActivity.this, "请检查网络", Toast.LENGTH_SHORT).show();
			}
		}
		if(v==btn_ok){
			if(tv_parentbankname.getText().toString().isEmpty()){
				Toast.makeText(getApplicationContext(), "未选择主行", Toast.LENGTH_LONG).show();
				return;
			}
			if(ed_card.getText().toString().isEmpty()){
				Toast.makeText(getApplicationContext(), "未刷卡取号", Toast.LENGTH_LONG).show();
				return;
			}
			if(ed_card_name.getText().toString().isEmpty()){
				Toast.makeText(getApplicationContext(), "未填写姓名", Toast.LENGTH_LONG).show();
				return;
			}
			if(ed_idcard_num.getText().toString().isEmpty()){
				Toast.makeText(getApplicationContext(), "未填写身份证号", Toast.LENGTH_LONG).show();
				return;
			}
			if(ed_search.getText().toString().isEmpty()){
				Toast.makeText(getApplicationContext(), "未选择支行", Toast.LENGTH_LONG).show();
				return;
			}
			if(ed_phone_num.getText().toString().isEmpty()){
				Toast.makeText(getApplicationContext(), "未填写手机号", Toast.LENGTH_LONG).show();
				return;
			}
			doOk();
		}
	}
	public void doOk(){
		if(pg!=null){
			pg.setMessage("正在处理,请稍后");
			pg.setCancelable(false);
			pg.show();
		}else{
			pg = new ProgressDialog(AddNewCardActivity.this);
			pg.setMessage("正在处理,请稍后");
			pg.setCancelable(false);
			pg.show();
		}
		new Thread(){
			@Override
			public void run() {
				super.run();
				String cardNo = ed_card.getText().toString().trim();
				String idNo = ed_idcard_num.getText().toString().trim();
				String person = ed_card_name.getText().toString().trim();
				String phoneNo = ed_phone_num.getText().toString().trim();
				//模拟个登陆
				
				stat =  controller.pushCard(AddNewCardActivity.this, cardNo, person, Controller.NOT_CREADIT_CARD, lkey, idNo, phoneNo, null, child_bb.getId()+"", resultHandler);
				//验证返回结果
				confirmOk(stat);
			}
		}.start();
			
	}
	
	private void confirmOk(final MsgPush s){
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if(pg!=null){
					pg.dismiss();
				}
				if(s.error.equals("0")){
					Toast.makeText(getApplicationContext(), "添加成功", Toast.LENGTH_LONG).show();
					
						finish();
					
				}else if(s.error.equals("3")){
					Toast.makeText(AddNewCardActivity.this, "该卡已存在", Toast.LENGTH_SHORT).show();
				}else if(s.error.equals("1")){
					Toast.makeText(AddNewCardActivity.this, "添加失败--》"+s.RspMsg, Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(AddNewCardActivity.this, "添加失败--》"+s.RspMsg, Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		
		
	}
	//刷新卡号
	public void setCardNo(Object obj){
		if(CHOSE_DEVICE.equals(UICommon.WFT_DEVICE)||CHOSE_DEVICE.equals(UICommon.LANDY_DEVICE)||CHOSE_DEVICE.equals(UICommon.AF_DEVICE)||CHOSE_DEVICE.equals(UICommon.CFT_DEVICE)||CHOSE_DEVICE.equals(UICommon.BBPOS_IC_DEVICE)){
			LandyTackMsg landyTackMsg = (LandyTackMsg)obj;
			handBack(landyTackMsg.cardNo+"");
		}
	}
	public void handBack(final String msg){
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				ed_card.setText(msg.replaceAll(".{4}(?!$)", "$0"));
				
			}
		});
		//请求服务器是否支持该卡
		handleGetBankSurport(msg);
	}
	//请求服务器验证该卡号是否正确
	public void handleGetBankSurport(final String msg){
		if(!Common.checkNetWork(getApplicationContext())){
			Toast.makeText(getApplicationContext(), "未连接网络", Toast.LENGTH_SHORT).show();
			return;
		}
		new Thread(){
			@Override
			public void run() {
				super.run();
				msgValidation = controller.isValidation(msg);
				if(msgValidation!=null){
					handCardName(msgValidation);
				}else{
					Tip("请检查网络");
				}
			}
		}.start();
		
	}
	
	//刷新页面银行名
	public void handCardName(final MsgValidation s){
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if(msgValidation==null){
					Toast.makeText(getApplicationContext(), "该卡不支持", Toast.LENGTH_LONG).show();
				}else{
					bank_crad.setText(msgValidation.name);
					Log.i("msgValidation.name->银行名", msgValidation.name);
				}
				
			}
		});
	}
	void initResultHandler(){
		resultHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				
				super.handleMessage(msg);
			}
		};
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);
		if(data!=null){
			if(requestCode==GET_PARENT_REQUEST_CODE){
				parent_bb=(BankBean) data.getExtras().getSerializable("bank_data");
				tv_parentbankname.setText(parent_bb.getName());
				
			}else if(requestCode==GET_CHILD_REQUEST_CODE){
				child_bb = (BankBean) data.getExtras().get("bank_data");
				ed_search.setText(child_bb.getName());
			}
		}
	}
}
