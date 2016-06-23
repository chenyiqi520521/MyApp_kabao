package com.apicloud.activity.topup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.apicloud.common.Common;
import com.apicloud.controller.Controller;
import com.apicloud.controller.MyController;
import com.apicloud.module.LoginBean;
import com.apicloud.module.ReChargeStyleBean;
import com.apicloud.module.RechargeItemBean;
import com.apicloud.util.ListScrollUtil;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

public class TopUpThreeActivity extends TopUpNewActivity {
	private ImageButton top_ib_return;
	private MyController controller;
	private EditText top_ed_text;
	private ListView paymentListView;
	private LinearLayout top_ll_ok;
	private ReChargeStyleBean reChargeStyleBean;
	private String lkey = "";
	private List<Map<String, Object>> payList;
	private OrderPaymentsAdapter3 orderPaymentsAdapter3;
	private TextView top_tv_info;
	private boolean isFinished = true;
	private String chose_device = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("top_up_three"));
		controller=new MyController(TopUpThreeActivity.this);
		initView();
		// 获取支付方式列表
		getPaymentsList();
	}

	Handler handler = new Handler();
	int default_checked = -1;

	public void initView() {
		top_ib_return = (ImageButton) findViewById(UZResourcesIDFinder.getResIdID("top_ib_return"));
		paymentListView = (ListView) findViewById(UZResourcesIDFinder.getResIdID("top_lv_payChoice"));
		top_ll_ok = (LinearLayout) findViewById(UZResourcesIDFinder.getResIdID("top_ll_ok"));
		top_tv_info = (TextView) findViewById(UZResourcesIDFinder.getResIdID("top_tv_info"));
		top_ed_text = (EditText) findViewById(UZResourcesIDFinder.getResIdID("top_ed_amount"));
		
		top_ib_return.setOnClickListener(this);
		top_ll_ok.setOnClickListener(this);
		chose_device = CHOSE_DEVICE;
		lkey = getIntent().getStringExtra("key");
		
	}

	@Override
	public void onClick(View v) {

		super.onClick(v);
		if (v == top_ib_return) {
			finish();
		}
		if (v == top_ll_ok) {
			if (TextUtils.isEmpty(top_ed_text.getText().toString())) {
				Toast.makeText(TopUpThreeActivity.this, "请输入金额", Toast.LENGTH_SHORT).show();
			}
			
			int select = paymentListView.getCheckedItemPosition();
			if (paymentListView.INVALID_POSITION != select) {
				ed_crad_amount.setText(top_ed_text.getText().toString());
				if (chose_device.equals("A")) {
					// dealwithA();
				} else {
					tv_getCardNum.performClick();
				}
			} else {
				Toast.makeText(TopUpThreeActivity.this, "请选择一种支付方式", Toast.LENGTH_SHORT).show();
			}
		} else if (v == topUpDialog.txt_ok) {
			try {
				if (CHOSE_DEVICE.equals("A") && Controller.isEmpty(topUpDialog.ed_crad_pass.getText().toString().trim())) {
					Toast.makeText(TopUpThreeActivity.this, "密码不能为空", Toast.LENGTH_LONG).show();
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
	}

	public void getPaymentsList() {
		try {
			pg = new ProgressDialog(TopUpThreeActivity.this);
			pg.setMessage("正在刷新列表...");
			pg.setCancelable(false);
			pg.show();
		} catch (Exception e) {

		}
		new Thread() {
			public void run() {
				
				setParentCode(null, lkey);
				// 发起请求，获取银行列表接口
				Log.v("充值lkey", lkey+"");
				reChargeStyleBean = controller.rechargeMethod(TopUpThreeActivity.this, lkey);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						pg.dismiss();

					}

				});
				
				List<RechargeItemBean> list = reChargeStyleBean.getList();
				Log.e("返回的数据", list.get(0).getName() + "," + list.get(1).getName());
				if (list != null && list.size() > 0) {
					payList = new ArrayList<Map<String, Object>>();

					Log.i("size", list.size() + "");
					for (int i = 0; i < list.size(); i++) {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("icon", list.get(i).getP_link());
						map.put("payNames", list.get(i).getName());
						map.put("open", list.get(i).getOpen());
						map.put("bizCode", list.get(i).getBizCode() + "");// 支付通道
						if (list.get(i).getBizCode().equals("A")) {
							default_checked = i;
						}
						payList.add(map);
					}

					handler.post(new Runnable() {

						@Override
						public void run() {
							// 填充列表
							orderPaymentsAdapter3 = new OrderPaymentsAdapter3(TopUpThreeActivity.this, payList);
							paymentListView.setAdapter(orderPaymentsAdapter3);
							ListScrollUtil.setListViewHeightBasedOnChildren(paymentListView);
							if (default_checked != -1) {
								try {
									paymentListView.setItemChecked(default_checked, true);
								} catch (Exception e) {
									paymentListView.setItemChecked(payList.size() - 1, true);
								}
							}
							top_tv_info.setText(reChargeStyleBean.getWord());
							paymentListView.setOnItemClickListener(new OnItemClickListener() {

								@Override
								public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
									try {
										((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
									} catch (Exception e) {

										e.printStackTrace();
									}

									if (payList.get(position).get("open").equals("0")) {
										isFinished = false;
										Toast.makeText(TopUpThreeActivity.this, "建设中", Toast.LENGTH_SHORT).show();
									} else if (payList.get(position).get("open").equals("1")) {
										isFinished = true;
										String money = top_ed_text.getText().toString();
										if (TextUtils.isEmpty(money)) {
											Toast.makeText(TopUpThreeActivity.this, "请输入金额", Toast.LENGTH_LONG).show();

										}

									}

								}

							});
						}

					});
				}
			}
		}.start();
	}

}
