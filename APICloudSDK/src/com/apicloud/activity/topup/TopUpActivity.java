
package com.apicloud.activity.topup;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.apicloud.activity.BaseActivity;
import com.apicloud.activity.timely.TimelyAccountTopUpActivity;
import com.apicloud.common.Common;
import com.apicloud.controller.Controller;
import com.apicloud.module.ReChargeStyleBean;
import com.apicloud.module.RechargeItemBean;
import com.apicloud.util.Constant;
import com.apicloud.util.ListScrollUtil;
import com.newland.mtypex.audioport.AudioPortV100ConnParams;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author wy521angel
 *
 */
public class TopUpActivity extends TopUpTwoActivity  {
	private ImageButton ib_return;// 返回
	private LinearLayout layout_ok;
	private LinearLayout layout_code;
	private Button btn_refresh;
	private int[] icons = new int[] { UZResourcesIDFinder.getResDrawableID("apple"), UZResourcesIDFinder.getResDrawableID("zhifubao"), UZResourcesIDFinder.getResDrawableID("weixin"), UZResourcesIDFinder.getResDrawableID("yinhangka"), UZResourcesIDFinder.getResDrawableID("yinhangka") };
	private String[] payWays = new String[] { "APPLE PAY", "支付宝支付", "微信支付", "银行卡小额支付（0.49）", "银行卡大额支付（35封顶）" };
	private ListView lv_payment;
	private Controller controller;// 控制器
	private ReChargeStyleBean reChargeStyleBean;
	private TextView tv_money;
	private List<Map<String, Object>> payList;
	private String smallPay;
	private String bigPay;
	private EditText ed_money;
	private EditText et_code;
	private TextView tv_info;
	private TextView tv_code;
	private boolean isFinished = true;
	private int payNum;// 付款方式的下标 ，1表示小额，2表示大额，0表示其他
	private OrderPaymentsAdapter2 orderPaymentsAdapter2;

	private String uid;
	private String posno;
	private String GPS;
	private String A;
	private String B;
	private String C;
	private String D;
	private String word;
	private boolean needResult;
	private String chose_device="";//当前设备类型

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("top_up"));
		controller = new Controller(TopUpActivity.this);
		initView();
		//获取验证码
		getCode(uid);
		//获取支付方式列表
		getPaymentsData();
		
	}

	Handler handler = new Handler();
	int default_checked=-1;
	private void getPaymentsData() {
		try {
			pg=new ProgressDialog(TopUpActivity.this);
			pg.setMessage("正在刷新支付列表...");
			pg.setCancelable(false);
			pg.show();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		new Thread() {
			public void run() {
				//发起获取支付方式请求
				reChargeStyleBean = controller.rechargeMethod(TopUpActivity.this, getIntent().getStringExtra("uid"));
				 runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						pg.dismiss();
						
					}
				});
				//拼接数据
				List<RechargeItemBean> list = reChargeStyleBean.getList();
				
				if (list != null && list.size() > 0) {
					payList = new ArrayList<Map<String, Object>>();
					for (int i = 0; i < list.size(); i++) {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("icon", list.get(i).getP_link());//图标
						map.put("payNames", list.get(i).getName());//支付方式名称
						map.put("open", list.get(i).getOpen());//打开方式
						map.put("bizCode", list.get(i).getBizCode()+"");//支付通道
						if(list.get(i).getBizCode().equals("A")){
							default_checked=i;
						}
						payList.add(map);
					}
					handler.post(new Runnable() {

						@Override
						public void run() {
							//填充列表
							orderPaymentsAdapter2 = new OrderPaymentsAdapter2(TopUpActivity.this, payList);
							lv_payment.setAdapter(orderPaymentsAdapter2);
							ListScrollUtil.setListViewHeightBasedOnChildren(lv_payment);
							if(default_checked!=-1){
								try {
									lv_payment.setItemChecked(default_checked, true);
								} catch (Exception e) {
									// TODO: handle exception
									lv_payment.setItemChecked(payList.size()-1, true);
								}
								
							}
							
                            
							tv_info.setText(reChargeStyleBean.getWord());
                            //列表单击事件
							lv_payment.setOnItemClickListener(new OnItemClickListener() {

								@Override
								public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
									try {
										//隐藏软键盘
										((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))  
						                .hideSoftInputFromWindow(getCurrentFocus()  
						                        .getWindowToken(),  
						                        InputMethodManager.HIDE_NOT_ALWAYS);  
									} catch (Exception e) {
										// TODO: handle exception
									}
									 
									if (payList.get(arg2).get("open").equals("0")) {
										isFinished = false;
										Toast.makeText(TopUpActivity.this, "建设中……", Toast.LENGTH_SHORT).show();
										layout_code.setVisibility(View.GONE);
										// tv_money.setText("");
									} else if (payList.get(arg2).get("open").equals("1")) {
										isFinished = true;
										String money = ed_money.getText().toString();
										if (TextUtils.isEmpty(money)) {
											Toast.makeText(TopUpActivity.this, "请输入充值金额！", Toast.LENGTH_SHORT).show();
										}
										Map<String, Object> temp = payList.get(arg2);
										if(temp!=null){
											//判断是A通道
											String bizeCode=temp.get("bizCode")+"";
											if(bizeCode.equals("A")){
												info="A";
												layout_code.setVisibility(View.VISIBLE);
												 setEtCodeParent(et_code,uid);
											}
											//判断B通道
											if(bizeCode.equals("B")){
												info="B";
												layout_code.setVisibility(View.GONE);
												setEtCodeParent(null,uid);
											}
										}
										
										
									}
								}
							});
						}
					});

				}
			};

		}.start();
	}

	

	/**
	 * 价钱额度计算
	 * 
	 * @param inputPay
	 * @param isSmall
	 * @return
	 */
	private String getPayNum(String input, boolean isSmall) {
		double inputPay = Double.parseDouble(input);
		double newMoney;
		if (isSmall) {
			newMoney = (inputPay - inputPay * Double.parseDouble((reChargeStyleBean.getA_fee())));
		} else {
			double bif = inputPay * Double.parseDouble(reChargeStyleBean.getB_fee());
			int top = Integer.parseInt(reChargeStyleBean.getB_top());
			newMoney = bif > top ? (inputPay - top) : (inputPay - bif);
		}

		BigDecimal b = new BigDecimal(newMoney);
		double f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		return f1 + "";
	}

	/**
	 * 初始化界面并填充必要数据
	 */
	private void initView() {
		uid = getIntent().getStringExtra("uid");
		posno = getIntent().getStringExtra("posno");
		GPS = getIntent().getStringExtra("GPS");
		A = getIntent().getStringExtra("A");
		B = getIntent().getStringExtra("B");
		C = getIntent().getStringExtra("C");
		D = getIntent().getStringExtra("D");
		word = getIntent().getStringExtra("word");
		needResult = getIntent().getBooleanExtra("needResult", false);

		tv_info = (TextView) findViewById(UZResourcesIDFinder.getResIdID("tv_info"));
		tv_money = (TextView) findViewById(UZResourcesIDFinder.getResIdID("tv_money"));
		ed_money = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_money"));
		ib_return = (ImageButton) findViewById(UZResourcesIDFinder.getResIdID("ib_return1"));
		ib_return.setOnClickListener(this);
		lv_payment = (ListView) findViewById(UZResourcesIDFinder.getResIdID("lv_payment"));
		layout_code = (LinearLayout) findViewById(UZResourcesIDFinder.getResIdID("layout_code"));
		et_code = (EditText) findViewById(UZResourcesIDFinder.getResIdID("et_code"));
		setEtCodeParent(et_code,uid);
        
		tv_code = (TextView) findViewById(UZResourcesIDFinder.getResIdID("tv_code"));

		layout_ok = (LinearLayout) findViewById(UZResourcesIDFinder.getResIdID("layout_ok"));
		layout_ok.setOnClickListener(this);
		btn_refresh = (Button) findViewById(UZResourcesIDFinder.getResIdID("btn_refresh"));
		btn_refresh.setOnClickListener(this);
		// priceTextChanged();
        chose_device=CHOSE_DEVICE;
	}

	/**
	 * 获取验证码
	 */
	public void getCode(final String uid) {
		new Thread() {
			public void run() {
				final String code = controller2.getCode(uid);

				handler.post(new Runnable() {

					@Override
					public void run() {
						tv_code.setText(code);
					}
				});

			};

		}.start();
	}

	private void dealwithA(){
		if (btn_ok.getText().toString().trim().contains("获取卡号")) {

			cardHeadDialog = new NotCardHeadDialog(TopUpActivity.this, CHOSE_DEVICE);
			cardHeadDialog.show();
			if (CHOSE_DEVICE.equals("A")) {
				initMe3xDeviceController(new AudioPortV100ConnParams());// 初始化设备
			}
			operation_stay(Common.FETCH_DEVICE_INFO);
			addMessageHandler();// 添加初始化一系列的事件
			// cardHeadDialog.txt_title.setText("请插入卡头");
		} else if (btn_ok.getText().toString().trim().contains("确定")) {
			if (Controller.isEmpty(ed_crad.getText().toString().trim())) {
				Toast.makeText(getApplicationContext(), "卡号不能为空", Toast.LENGTH_LONG).show();
				return;

			}
			price = ed_crad_amount.getText().toString().trim();
			doOk();
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// 返回
		if (v == ib_return) {
			finish();
		} else if (v == btn_refresh) {
			getCode(uid);
		} else if (v == layout_ok) {
			
			if (TextUtils.isEmpty(ed_money.getText().toString())) {
				Toast.makeText(this, "请输入充值金额！", Toast.LENGTH_SHORT).show();
				return;
			}
			
			if (!isFinished) {
				Toast.makeText(TopUpActivity.this, "建设中……", Toast.LENGTH_SHORT).show();
				return;
			}
			int select = lv_payment.getCheckedItemPosition();
			Map<String, Object> temp = payList.get(select);
			if(temp!=null){
				String bizeCode=temp.get("bizCode")+"";
				if(bizeCode.equals("A")){
					/*if (TextUtils.isEmpty(et_code.getText().toString())) {
						Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
						return;
					}*/

					final String code1=et_code_parent.getText().toString()+"";
					if(code1.length()>0){
						new Thread(){
							public void run() {
								if(!controller2.validateCode(code1, uid)){
									Tip("验证码有误");
									return;
								}
							};
						}.start();
					}
					
				}
				
			}
			
			// INVALID_POSITION 代表无效的位置。有效值的范围是 0 到当前适配器项目数减 1 。
			if (ListView.INVALID_POSITION != select) {
				
				
				code = et_code.getText().toString()+"";
			    ed_crad_amount.setText(ed_money.getText().toString());
				if(chose_device.equals("A")){
					dealwithA();
				}else{
					tv_getcard_num.performClick();
				}
				
			} else {
				Toast.makeText(this, "请选择一种支付方式！", Toast.LENGTH_SHORT).show();
			}

		}else if (v == topUpDialog.txt_ok) {
			try {
				if (CHOSE_DEVICE.equals("A") && Controller.isEmpty(topUpDialog.ed_crad_pass.getText().toString().trim())) {
					Toast.makeText(TopUpActivity.this, "密码不能为空", Toast.LENGTH_LONG).show();
					return;
				}
				topUpDialog.txt_ok.setFocusable(false);
				topUpDialog.txt_ok.setEnabled(false);
				if (!Common.checkNetWork(getApplicationContext())) {
					Toast.makeText(getApplicationContext(), "当前没有网络", Toast.LENGTH_LONG).show();
					return;
				}

				topUpDialog.dismiss();
				goToHanwriting(creditCardBean, msgBean, ed_crad_amount.getText().toString().trim());
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

	}//
	
	
}
