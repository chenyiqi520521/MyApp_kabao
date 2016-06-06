/**
 * Project Name:CardPay
 * File Name:TimelyAccountTopUpActivity.java
 * Package Name:com.apicloud.activity
 * Date:2015-4-23下午7:44:24
 * Copyright (c) 2015, zhuxiaohao All Rights Reserved.
 *
 */

package com.apicloud.activity.timely;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import com.apicloud.activity.BaseActivity;
import com.apicloud.activity.topup.OrderPaymentsAdapter2;
import com.apicloud.activity.topup.TopUpActivity;
import com.apicloud.activity.topup.TopUpDialog;
import com.apicloud.activity.write.HandwritingActivity;
import com.apicloud.adapter.TimeListAdapter;
import com.apicloud.common.Common;
import com.apicloud.common.PinSecurityImpl;
import com.apicloud.controller.Controller;
import com.apicloud.controller.DeviceListener;
import com.apicloud.controller.TransferListener;
import com.apicloud.landy.LandyTackMsg;
import com.apicloud.module.BankCrad;
import com.apicloud.module.CardBean;
import com.apicloud.module.CardBean.DataBean;
import com.apicloud.module.CreditCardBean;
import com.apicloud.module.MsgBean;
import com.apicloud.module.RechargeItemBean;
import com.apicloud.swip.SwipApi;
import com.apicloud.util.Configure;
import com.apicloud.util.Constant;
import com.apicloud.util.LibImageLoader;
import com.apicloud.util.ListScrollUtil;
import com.apicloud.util.UICommon;
import com.apicloud.view.LoadingDialog;
import com.newland.me.DeviceManager.DeviceConnState;
import com.newland.mtype.BatteryInfoResult;
import com.newland.mtype.ConnectionCloseEvent;
import com.newland.mtype.DeviceInfo;
import com.newland.mtype.ModuleType;
import com.newland.mtype.conn.DeviceConnParams;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.emv.EmvTransController;
import com.newland.mtype.module.common.emv.EmvTransInfo;
import com.newland.mtype.module.common.emv.SecondIssuanceRequest;
import com.newland.mtype.module.common.swiper.SwipResult;
import com.newland.mtype.module.external.me11.ME11SwipResult;
import com.newland.mtype.tlv.TLVPackage;
import com.newland.mtype.util.Dump;
import com.newland.mtype.util.ISOUtils;
import com.newland.mtypex.audioport.AudioPortV100ConnParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.squareup.picasso.Picasso;
import com.umeng.analytics.MobclickAgent;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

import android.animation.TimeAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * ClassName:TimelyAccountTopUpActivity <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2015-4-23 下午7:44:24 <br/>
 * 
 * @author zhuxiaohao blog:http://blog.csdn.net/qq718799510?viewmode=contents
 * @version 1.0.0
 * @since JDK 1.6
 * @see 即时到帐充值
 */
public class TimelyAccountTopUpActivity extends BaseActivity implements OnClickListener {

	private String uid;
	private String posno;
	private String cardid;
	private String cardno;
	private String uname;
	private boolean needResult;

	private CardBean cardBean;
	Handler handler = new Handler();
	ListView cardNameList;
	private TextView tv_code;
	private Button btn_refresh;
	private String messareStr = "";
	private EditText et_code;

	RelativeLayout layout_card_select;// 选择信用卡
	private PopupWindow mPopupWindow;// 选择信用卡的PopupWindow
	private ImageView iv_icon;
	private List<Map<String, String>> items = new ArrayList<Map<String, String>>();// 绑定银行卡列表
	private View popupView;
	LinearLayout layout_add_card;// 添加信用卡
	TextView tv_pay_one;// 选择收款类型
	TextView tv_pay_two;
	TextView tv_pay_three;
	boolean isOneSelected;
	boolean isTwoSelected;
	boolean isThreeSelected;

	ImageButton ib_return;// 返回
	ImageView iv_crad_show;// 银行所属图片
	TextView txt_crad_name;// 所属银行
	TextView txt_crad_number;// 银行卡号
	TextView txt_cash_crad;// 转入储蓄卡银行卡号
	EditText ed_crad_amount;// 转入储蓄卡金额
	TextView btn_ok;// 确认

	boolean processing = false;// 时候进行中
	// DeviceController controller = DeviceControllerImpl.getInstance();// 卡头控制器
	boolean closed = false;// 关闭
	int cancel = 2;// 如果取消
	int connect = 4;// 如果连接失败
	int isStand = 0;// 如果出现不支持卡状态
	int swiper = 3;// 刷卡状态
	Handler iHandler, sonHandler;
	BankCrad bankCrad;// 银行卡刷出信息
	String csn;// 这个又称 KSN
	DeviceInfo deviceInfo;// 设备信息
	DisplayImageOptions options;
	TopUpDialog topUpDialog;// 输入密码对话框
	String pass;// 密码
	// NotCardHeadDialog cardHeadDialog;// 检测卡头的对话框
	CreditCardBean creditCardBean;// 请求对象
	Controller controller2;// 控制器
	MsgBean msgBean;// 返回对象
	String account;// 获取的卡号
	ProgressDialog pd;
	String price;
	ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
	TextView tv_word;
	boolean isFirstAdd = true;
	ImageView iv_card_1;

	TextView tv_delete;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("timely_accoun_top_up"));
		controller2 = new Controller(getApplicationContext());
		initView();
		initGasPopupWindow();
		getCode(uid);
		topUpDialog = new TopUpDialog(TimelyAccountTopUpActivity.this);
		topUpDialog.setOnclickListener(this);
		// cardHeadDialog = new
		// NotCardHeadDialog(TimelyAccountTopUpActivity.this);
		// cardHeadDialog.show();
		addMessageHandler();

		initSonHandler();
		try {
			startLocation();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	/**
	 * 
	 * TODO模块化以后获取数据完毕的提交处理
	 * 
	 * @param ob
	 *           
	 */
	public void ModelSubmit(Object ob) {
		// 如果选择的是联迪设备
		if (CHOSE_DEVICE.equals(UICommon.WFT_DEVICE) || CHOSE_DEVICE.equals(UICommon.LANDY_DEVICE) || CHOSE_DEVICE.equals(UICommon.CFT_DEVICE) || CHOSE_DEVICE.equals(UICommon.BBPOS_IC_DEVICE)) {
			LandyTackMsg landybean = (LandyTackMsg) ob;
			Log.v("landy1", "cardno--tp" + landybean.cardNo);
			// ed_crad.setText(landybean.cardNo+"");
			// 设备实例
			landySetBean(landybean);
			/*
			 * topUpDialog.show(); topUpDialog.setOnclickListener(this);
			 * handler.sendEmptyMessage(1);
			 * topUpDialog.ed_crad_pass.setVisibility(View.VISIBLE);
			 */
			String getNo = txt_crad_number.getText().toString().replaceAll(".{4}(?!$)", "$0 ");
			getNo = getNo.replace(" ", "");
			String swipcardno = landybean.cardNo;
			swipcardno = swipcardno.replace(" ", "");
			// String
			// get_account=(getIntent().getStringExtra("acctNo")).replaceAll(".{4}(?!$)",
			// "$0 ");
			if (!swipcardno.equals(getNo)) {
				Toast.makeText(TimelyAccountTopUpActivity.this, "请刷上面认证过的信用卡", Toast.LENGTH_SHORT).show();
			} else {
				btn_ok.setText("确定");
				goToHanwriting(creditCardBean, msgBean, ed_crad_amount.getText().toString().trim());
			}

		}

	}

	void landySetBean(LandyTackMsg landybean) {
		creditCardBean = new CreditCardBean();
		creditCardBean.ksn = landybean.ksn;
		creditCardBean.encWorkingKey = landybean.enworkingKey + "";
		creditCardBean.acctNoT2 = landybean.track2 + "";
		creditCardBean.acctNoT3 = landybean.track3 + "";
		creditCardBean.ic = landybean.Data55 + "";
		creditCardBean.pointService = landybean.pointService + "";
		creditCardBean.acctNo = landybean.cardNo + "";
		creditCardBean.cardEXPDate = landybean.expireDate + "";
		creditCardBean.cardSN = "1";
		if (CHOSE_DEVICE.equals(UICommon.BBPOS_IC_DEVICE) || CHOSE_DEVICE.equals(UICommon.WFT_DEVICE)) {
			creditCardBean.cardSN = landybean.cardSn + "";
		}
		creditCardBean.pin = landybean.pinBlock + "";
		creditCardBean.transAmt = landybean.amount + "";

	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		if (Constant.isNeedRefreshCardInfo) {
			items.clear();
			initGasPopupWindow();
		}
	}

	/**
	 * 获取验证码
	 */
	public void getCode(final String lkey) {
		new Thread() {
			public void run() {
				final String code = controller2.getCode(lkey);

				handler.post(new Runnable() {

					@Override
					public void run() {
						tv_code.setText(code);
					}
				});

			};

		}.start();
	}

	void initSonHandler() {
		sonHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String content = (String) msg.obj;
				if (content != null && content.equals("签名完毕")) {
					// Toast.makeText(TopUpActivity.this,"签名完毕",Toast.LENGTH_SHORT).show();
					try {
						topUpDialog.dismiss();
					} catch (Exception e) {
						// TODO: handle exception
					}
					ed_crad_amount.setEnabled(false);
					submit();

					return;
				}
				if (content != null && content.equals("sure")) {// 点击确定
					String amount1 = ed_crad_amount.getText().toString().trim();

					if (CHOSE_DEVICE.equals("B") || CHOSE_DEVICE.equals("C")) {
						topUpDialog.ed_crad_pass.setVisibility(View.GONE);
					}

					if (CHOSE_DEVICE.equals("A") || CHOSE_DEVICE.equals("D")) {
						topUpDialog.ed_crad_pass.setVisibility(View.VISIBLE);
					}
					topUpDialog.show();
					topUpDialog.txt_crad_moery.setText("¥" + ed_crad_amount.getText().toString().trim());
					topUpDialog.txt_cradNumber.setText(bankCradParent.account.replaceAll(".{4}(?!$)", "$0 "));
					topUpDialog.txt_crad_t.setText("¥" + ed_crad_amount.getText().toString().trim());
				}

				if (CHOSE_DEVICE.equals("B") || CHOSE_DEVICE.equals("D")) {
					int type = msg.what;
					operation_stay(type);
				}

			}
		};
		setParentHandler(sonHandler);
	}

	/**
	 * 
	 * initView:(初始化). <br/>
	 * 
	 * @author zhuxiaohao
	 * @since JDK 1.6
	 */
	@SuppressLint("NewApi")
	private void initView() {

		uid = getIntent().getStringExtra("uid");
		posno = getIntent().getStringExtra("posno");
		cardid = getIntent().getStringExtra("cardid");
		cardno = getIntent().getStringExtra("cardNo");
		uname = getIntent().getStringExtra("name");
		needResult = getIntent().getBooleanExtra("needResult", false);
		iv_card_1 = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("iv_crad_1"));
		iv_icon = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("iv_icon"));
		tv_delete = (TextView) findViewById(UZResourcesIDFinder.getResIdID("tv_delete"));
		tv_delete.setOnClickListener(this);
		// tv_pay_one = (TextView)
		// findViewById(UZResourcesIDFinder.getResIdID("tv_pay_one"));
		// tv_pay_one.setOnClickListener(this);
		// tv_pay_two = (TextView)
		// findViewById(UZResourcesIDFinder.getResIdID("tv_pay_two"));
		// tv_pay_two.setOnClickListener(this);
		// tv_pay_three = (TextView)
		// findViewById(UZResourcesIDFinder.getResIdID("tv_pay_three"));
		// tv_pay_three.setOnClickListener(this);
		layout_add_card = (LinearLayout) findViewById(UZResourcesIDFinder.getResIdID("layout_add_card"));
		layout_add_card.setOnClickListener(this);
		ib_return = (ImageButton) findViewById(UZResourcesIDFinder.getResIdID("ib_return"));
		iv_crad_show = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("iv_crad_show"));
		txt_crad_name = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_crad_name"));
		txt_crad_number = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_crad_number"));
		txt_cash_crad = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_cash_crad"));
		ed_crad_amount = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_crad_amount"));
		tv_word = (TextView) findViewById(UZResourcesIDFinder.getResIdID("tv_word"));
		tv_code = (TextView) findViewById(UZResourcesIDFinder.getResIdID("tv_code"));
		btn_refresh = (Button) findViewById(UZResourcesIDFinder.getResIdID("btn_refresh"));
		btn_refresh.setOnClickListener(this);
		et_code = (EditText) findViewById(UZResourcesIDFinder.getResIdID("et_code"));
		setEtCodeParent(et_code, uid);
		layout_card_select = (RelativeLayout) findViewById(UZResourcesIDFinder.getResIdID("layout_card_select"));
		layout_card_select.setOnClickListener(this);

		layout_card_select.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				if (items != null && items.size() > 0) {
					Toast.makeText(TimelyAccountTopUpActivity.this, "选择的是" + txt_crad_number.getText().toString(), Toast.LENGTH_SHORT).show();
					deleteCard(txt_crad_number.getText().toString());
				}
				return false;
			}
		});
		String tip = getIntent().getStringExtra("word").toString();
		if (tip != null && tip.length() > 0) {
			tip = tip.replace("#", "\r\n");
			tv_word.setText(tip);
		}
		btn_ok = (TextView) findViewById(UZResourcesIDFinder.getResIdID("btn_ok"));
		setGetCardNumTv(btn_ok);
		ib_return.setOnClickListener(this);
		// 判断是卡头还是蓝牙
		String chose_device = getIntent().getStringExtra("posno");
		if (chose_device != null && chose_device.length() > 0) {
			CHOSE_DEVICE = chose_device;

		}
		// CHOSE_DEVICE="A";
		if (CHOSE_DEVICE.equals("A")) {
			btn_ok.setOnClickListener(this);
		}

		// txt_crad_number.setText(replaceString(getIntent().getStringExtra("acctNo")).replaceAll(".{4}(?!$)",
		// "$0 "));
		txt_crad_name.setText(getIntent().getStringExtra("name"));
		txt_cash_crad.setText(getIntent().getStringExtra("cardNo").replaceAll(".{4}(?!$)", "$0 "));
		// String url = getIntent().getStringExtra("url");
		// if (url == null) {
		// url = "bank_yl";
		// }
		// Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
		// UZResourcesIDFinder.getResDrawableID(url));
		// iv_crad.setImageBitmap(bitmap);
		pd = new LoadingDialog(TimelyAccountTopUpActivity.this);
		pd.setCancelable(false);// 设置进度条是否可以按退回键取消
		pd.setCanceledOnTouchOutside(false);// 设置点击进度对话框外的区域对话框不消失
		// pd.show();
		needShowMoeny = true;
		amount_et = ed_crad_amount;
		initParentView();
		initHeadDialog();
		initSwipParam();
	}

	void initSwipParam() {
		fromAct = UICommon.TimelyAaccountTopUp;
		whatDo = SwipApi.WHATDO_SWIPER;
		cur_Ac = this;
	}

	private List<DataBean> testData() {
		List<DataBean> list = new ArrayList<CardBean.DataBean>();
		for (int i = 0; i < 5; i++) {
			DataBean dataBean = new DataBean();
			dataBean.setCardname("Test" + i);
			dataBean.setCardno("*************" + i);
			dataBean.setCardimg("");
			list.add(dataBean);
		}
		return list;
	}

	void filterMap() {
		List<Map<String, String>> tempList = new ArrayList<Map<String, String>>();
		Map<String, String> tempMap = new HashMap<String, String>();
		for (int i = 0; i < items.size(); i++) {
			Map<String, String> item = items.get(i);
			String cardNo1 = item.get("card_no") + "";
			Log.v("param2", "88-->" + cardNo1);
			if (!tempMap.containsKey(cardNo1)) {
				tempMap.put(cardNo1, cardNo1);
				tempList.add(item);
			}
		}
		items = tempList;

	}

	private void addCardName() {
		// List<DataBean> list = testData();
		List<DataBean> list = cardBean.getData();
		if (list != null && list.size() > 0) {
			layout_card_select.setVisibility(View.VISIBLE);
			for (int i = 0; i < list.size(); i++) {
				Map<String, String> item = new HashMap<String, String>();
				item.put("card_img", list.get(i).getCardimg());
				item.put("card_name", list.get(i).getCardname());
				item.put("card_no", list.get(i).getCardno());
				item.put("uname", list.get(i).getUname());
				item.put("id", list.get(i).getId() + "");
				Log.v("param2", "url0-->" + list.get(i).getCardimg());
				items.add(item);

			}
			if (isFirstAdd) {
				Log.v("param2", "3--->first");
				txt_crad_name.setText(items.get(0).get("uname"));
				txt_crad_number.setText(items.get(0).get("card_no"));
				txt_crad_number.setTag(items.get(0).get("id"));
				if (!TextUtils.isEmpty(items.get(0).get("card_img"))) {
					// new Thread
					Picasso.with(TimelyAccountTopUpActivity.this).load(items.get(0).get("card_img")).into(iv_crad_show);

				}
				isFirstAdd = false;
			}

			for (int i = 0; i < items.size(); i++) {
				if (txt_crad_number.getText().toString().trim().equals(items.get(i).get("card_no"))) {
					if (!TextUtils.isEmpty(items.get(i).get("card_img"))) {
						Log.v("param2", "url-->" + items.get(i).get("card_img"));
						Log.v("param2", "url-->" + items.get(i).get("card_no"));

					}
					items.remove(i);
					break;
				}
			}
		} else {
			layout_card_select.setVisibility(View.GONE);

		}
	}

	private void getPaymentsData() {
		new Thread() {
			public void run() {
				cardBean = controller2.getCardInfo(TimelyAccountTopUpActivity.this, uid, "rcard");

				// if (items != null && items.size() > 0) {
				handler.post(new Runnable() {

					@Override
					public void run() {
						try {
							// pd.dismiss();
							if (cardBean == null) {
								layout_card_select.setVisibility(View.GONE);
								Log.v("param2", "null--->size" + items.size());
							} else {
								addCardName();
								Log.v("param2", "2--->size" + items.size());
								if (cardBean != null && cardBean.getZp() != null) {
									txt_cash_crad.setText(cardBean.getZp().replaceAll(".{4}(?!$)", "$0 "));
								}

								Picasso.with(TimelyAccountTopUpActivity.this).load(cardBean.getZp_img()).into(iv_card_1);
								if (items != null && items.size() > 0) {
									filterMap();
									Log.v("param2", "url4-->" + items.size());
									/*
									 * final SimpleAdapter adapter = new
									 * SimpleAdapter
									 * (TimelyAccountTopUpActivity.this, items,
									 * UZResourcesIDFinder
									 * .getResLayoutID("item_card"), new
									 * String[] { "card_img", "card_name",
									 * "card_no" }, new int[] {
									 * UZResourcesIDFinder
									 * .getResIdID("iv_crad"),
									 * UZResourcesIDFinder
									 * .getResIdID("txt_crad_name"),
									 * UZResourcesIDFinder
									 * .getResIdID("txt_crad_number") });
									 * cardNameList.setAdapter(adapter);
									 */
									TimeListAdapter adapter = new TimeListAdapter(TimelyAccountTopUpActivity.this, items);
									cardNameList.setAdapter(adapter);
									cardNameList.setOnItemClickListener(new OnItemClickListener() {

										@Override
										public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

											String url = items.get(position).get("card_img");
											txt_crad_name.setText(items.get(position).get("uname"));
											txt_crad_number.setText(items.get(position).get("card_no"));
											txt_crad_number.setTag(items.get(position).get("id"));
											Log.v("param2", "click-->" + items.get(position).get("card_img"));
											if (!TextUtils.isEmpty(url)) {
												Picasso.with(TimelyAccountTopUpActivity.this).load(url).into(iv_crad_show);
											}

											items.clear();
											mPopupWindow.dismiss();
											addCardName();

										}
									});

								}
								mPopupWindow = new PopupWindow(popupView, AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT, true);
								mPopupWindow.setTouchable(true);
								mPopupWindow.setOutsideTouchable(true);
								mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));

							}
						} catch (Exception e) {
							// TODO: handle exception
							Log.v("param2", "url5 error-->" + e.getMessage());
						}

					}//
				});

			};

		}.start();
	}

	/**
	 * 删除银行卡
	 */
	private void deleteCard(final String cardId) {
		// 通过AlertDialog.Builder这个类来实例化我们的一个AlertDialog的对象
		AlertDialog.Builder builder = new AlertDialog.Builder(TimelyAccountTopUpActivity.this);
		// 设置Title的图标
		// builder.setIcon(R.drawable.ic_launcher);
		// 设置Title的内容
		builder.setTitle("提示");
		// 设置Content来显示一个信息
		builder.setMessage("确定删除吗？");
		// 设置一个PositiveButton
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				new Thread() {
					public void run() {

						final MsgBean msgBean = controller2.delcard(uid, cardId);
						handler.post(new Runnable() {

							@Override
							public void run() {
								if (msgBean != null && msgBean.RspCd != null) {
									if (msgBean.RspCd.equals("0")) {// 删除成功
										Toast.makeText(TimelyAccountTopUpActivity.this, "删除成功!", Toast.LENGTH_SHORT).show();
										items.clear();
										isFirstAdd = true;
										getPaymentsData();
									} else {
										Toast.makeText(TimelyAccountTopUpActivity.this, "删除失败！" + msgBean.RspMsg, Toast.LENGTH_SHORT).show();
									}
								}
							}
						});
					};
				}.start();

			}
		});
		// 设置一个NegativeButton
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		// 显示出该对话框
		builder.show();
	}

	/**
	 * 初始化信用卡的PopupWindow
	 */
	private void initGasPopupWindow() {
		try {
			Log.v("param2", "1");
			/*
			 * pd=new ProgressDialog(TimelyAccountTopUpActivity.this);
			 * pd.setMessage("正在请求数据..."); pd.setCancelable(false);
			 */
			// pd.show();
			popupView = getLayoutInflater().inflate(UZResourcesIDFinder.getResLayoutID("ppw_card_name"), null);
			cardNameList = (ListView) popupView.findViewById(UZResourcesIDFinder.getResIdID("watchView"));
			getPaymentsData();
		} catch (Exception e) {
			Log.v("param2", "poperror-->" + e.getMessage());
		}

	}

	/**
	 * 第二种方法
	 * 
	 * @param org
	 * @param n
	 * @return
	 */
	public static String replaceString(String org) {
		char[] cs = org.toCharArray();
		int len = org.length();
		for (int i = 0; i < (len - 4); i++) {
			cs[len - i - 5] = '*';
		}
		return String.valueOf(cs);
	}

	@SuppressWarnings("static-access")
	@Override
	public void onClick(View v) {
		if (v == tv_delete) {
			String deleteCradId = txt_crad_number.getTag().toString().trim() + "";
			if (deleteCradId.length() > 0) {
				deleteCard(deleteCradId);
			}

		}
		// 返回
		if (v == ib_return) {
			final boolean needResult = getIntent().getBooleanExtra("needResult", false);
			if (needResult) {
				Intent resultData = new Intent();
				JSONObject json = new JSONObject();
				try {
					json.put("callback", 0);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				resultData.putExtra("callback", json.toString());
				setResult(RESULT_OK, resultData);
			}
			;
			finish();
		}
		// 确定
		if (v == btn_ok) {
			final String code1 = et_code_parent.getText().toString() + "";
			if (code1.length() >= 0) {
				new Thread() {
					public void run() {
						if (!controller2.validateCode(code1, uid)) {
							Tip("验证码有误");
							return;
						}
					};
				}.start();
			}
			if (btn_ok.getText().toString().contains("请刷卡") || btn_ok.getText().toString().contains("确认支付")) {
				if (Controller.isEmpty(ed_crad_amount.getText().toString().trim())) {
					Toast.makeText(getApplicationContext(), "金额不能为空!", Toast.LENGTH_LONG).show();
					return;
				}
				String amount1 = ed_crad_amount.getText().toString().trim();
				try {
					if (Integer.parseInt(amount1) < 1) {
						Toast.makeText(getApplicationContext(), "金额要大于1元", Toast.LENGTH_LONG).show();
						return;
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
				initMe3xDeviceController(new AudioPortV100ConnParams());// 初始化设备
				operation_stay(Common.FETCH_DEVICE_INFO);
				addMessageHandler();// 添加初始化一系列的事件
				cardHeadDialog.show();
			} else if (btn_ok.getText().toString().contains("确定")) {
				String getNo = (getIntent().getStringExtra("acctNo")).replaceAll(".{4}(?!$)", "$0 ");
				getNo = getNo.replace(" ", "");

				if (!getNo.equals(account.replace(" ", ""))) {
					Toast.makeText(getApplicationContext(), "请刷认上面展示认证过的信用卡!", Toast.LENGTH_LONG).show();
					return;
				}
				topUpDialog.show();
				topUpDialog.txt_crad_moery.setText("¥" + ed_crad_amount.getText().toString().trim());
				topUpDialog.txt_cradNumber.setText(account);
				topUpDialog.txt_crad_t.setText("¥" + ed_crad_amount.getText().toString().trim());
			}

		}
		// 密码对话框确定
		if (v == topUpDialog.txt_ok) {

			doOk();
		}

		// 添加银行卡
		if (v == layout_add_card) {
			Intent intent = new Intent();
			intent.putExtra("uid", uid);
			intent.putExtra("posno", posno);
			intent.putExtra("cardid", cardid);
			intent.putExtra("cardno", cardno);
			intent.putExtra("uname", uname);
			intent.putExtra("needResult", needResult);
			intent.setClass(this, AddTimelyAccountActivity.class);
			startActivity(intent);
		}

		// 绑定的银行卡列表弹出
		if (v == layout_card_select) {
			try {
				mPopupWindow.showAsDropDown(v);
			} catch (Exception e) {
				// TODO: handle exception
			}

		}

		if (v == btn_refresh) {
			getCode(uid);
		}
	}

	/**
	 * 清空按钮的选择状态
	 */
	private void clearBtnBackground() {
		isOneSelected = false;
		isTwoSelected = false;
		isThreeSelected = false;
		tv_pay_one.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("_bg_pay_way_normal"));
		tv_pay_one.setTextColor(Color.parseColor("#000000"));
		tv_pay_two.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("_bg_pay_way_normal"));
		tv_pay_two.setTextColor(Color.parseColor("#000000"));
		tv_pay_three.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("_bg_pay_way_normal"));
		tv_pay_three.setTextColor(Color.parseColor("#000000"));

	}

	/**
	 * 设置按钮选中
	 */
	private void setBtnSelect(TextView v) {
		v.setTextColor(Color.parseColor("#ffffff"));
		v.setBackgroundResource(UZResourcesIDFinder.getResDrawableID("_bg_pay_way_selected"));
	}

	/**
	 * 
	 * doOk:(请求服务器支付). <br/>
	 * 
	 * @author zhuxiaohao
	 * @since JDK 1.6
	 */
	@SuppressWarnings("static-access")
	private void doOk() {
		
		if ((CHOSE_DEVICE.equals("A") || CHOSE_DEVICE.equals("D")) && Controller.isEmpty(topUpDialog.ed_crad_pass.getText().toString().trim())) {
			Toast.makeText(TimelyAccountTopUpActivity.this, "密码不能为空", Toast.LENGTH_LONG).show();
			return;
		} else {
			topUpDialog.txt_ok.setFocusable(false);
			topUpDialog.txt_ok.setEnabled(false);
			if (!Common.checkNetWork(getApplicationContext())) {
				Toast.makeText(getApplicationContext(), "当前没有网络", Toast.LENGTH_LONG).show();
				return;
			}
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					pd.show();

				}

			});
			btn_ok.setClickable(false);
			if (CHOSE_DEVICE.equals("C")) {
				price = amount_et.getText().toString().trim();
				bankCradParent.transAmt = price;
				creditCardBean = setCreditCardBean(bankCradParent);
			}
			if (locationInfo != null && locationInfo.length() > 0) {
				creditCardBean.gps = locationInfo;
				submit();
				goToHanwriting(creditCardBean, msgBean, ed_crad_amount.getText().toString().trim());
			} else {
				Tip("定位失败，请检查网络等，重新进入页面定位");
			}

		}
	}

	void submit() {
		new Thread() {
			@SuppressWarnings("static-access")
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();

				Log.v("cz1", "location->" + locationInfo);
				if (locationInfo != null && locationInfo.length() > 0) {
					creditCardBean.gps = locationInfo;
					// submit();

				} else {
					Tip("定位失败，请检查网络等，重新进入页面定位");
					return;

				}

				PinSecurityImpl impl = new PinSecurityImpl();
				String pin = "";
				if (CHOSE_DEVICE.equals("A") || CHOSE_DEVICE.equals("D")) {
					pin = topUpDialog.ed_crad_pass.getText().toString().trim();
				}
				if (CHOSE_DEVICE.equals("B")) {
					pin = BaseActivity.BlueToothPsd;
				}
				if (CHOSE_DEVICE.equals("A") || CHOSE_DEVICE.equals("B") || CHOSE_DEVICE.equals("D")) {
					if (pin != null && pin.length() >= 0 && !pin.equals("null")) {
						try {
							creditCardBean.pin = impl.desSecurity(bankCrad.account, pin, bankCrad.EncWorkingKey);
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
				}
				creditCardBean.lkey = getIntent().getStringExtra("uid");
				if (CHOSE_DEVICE.equals("A") || CHOSE_DEVICE.equals("B") || CHOSE_DEVICE.equals("C") || CHOSE_DEVICE.equals("D")) {
					creditCardBean.transAmt = Common.conversionPrice(ed_crad_amount.getText().toString().trim());
				}
				if (CHOSE_DEVICE.equals(UICommon.WFT_DEVICE) || CHOSE_DEVICE.equals(UICommon.LANDY_DEVICE) || CHOSE_DEVICE.equals(UICommon.CFT_DEVICE) || CHOSE_DEVICE.equals(UICommon.AF_DEVICE) || CHOSE_DEVICE.equals(UICommon.BBPOS_IC_DEVICE)) {
					creditCardBean.transAmt = Common.conversionPrice(creditCardBean.transAmt + "");
				}
				// creditCardBean.transAmt =
				// Common.conversionPrice(ed_crad_amount.getText().toString().trim());
				creditCardBean.cardNo = txt_cash_crad.getText().toString() + "";// 收款账户
				/*
				 * String pin=""; if(CHOSE_DEVICE.equals("A")){
				 * pin=topUpDialog.ed_crad_pass.getText().toString().trim();
				 * }else{ pin=BaseActivity.BlueToothPsd; }
				 * 
				 * if(pin!=null&&pin.length()>=0&&!pin.equals("null")){
				 */
				/*
				 * try {
				 * 
				 * creditCardBean.pin = impl.desSecurity(bankCrad.account, pin,
				 * bankCrad.EncWorkingKey); } catch (Exception e) {
				 * Log.v("pw2",e.getMessage()+""); }
				 */
				// 付款账户
				creditCardBean.acctNo = txt_crad_number.getText().toString().replaceAll(".{4}(?!$)", "$0 ").replace(" ", "");
				creditCardBean.type = "1";
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (pg != null) {
							pg.setCancelable(false);
							pg.setMessage("正在交易请稍后");
							// Tip("开始交易");
							pg.show();
						}

					}

				});

				String codeStr = et_code.getText().toString();
				msgBean = controller2.account(creditCardBean, codeStr + "");
				BaseActivity.BlueToothPsd = "";
				handleMsg(msgBean);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (pg != null) {
							pg.setCancelable(true);
							pg.dismiss();
						}

					}

				});
				/*
				 * }else{ Tip("输入密码为空"); }
				 */

			}
		}.start();
	}

	/**
	 * 
	 * handleMsg:(返回数据). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param msgBean
	 * @since JDK 1.6
	 */
	private void handleMsg(final MsgBean msgBean) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				pd.dismiss();
				try {
					topUpDialog.txt_ok.setFocusable(true);
					topUpDialog.txt_ok.setEnabled(true);
					topUpDialog.dismiss();
				} catch (Exception e) {
					// TODO: handle exception
				}

				if (msgBean == null) {
					Toast.makeText(getApplicationContext(), "网络不给", Toast.LENGTH_SHORT).show();
					TimelyAccountTopUpActivity.this.finish();
					return;
				}
				if (msgBean.RspCd.contains("00")) {
					// Toast.makeText(getApplicationContext(), msgBean.RspMsg,
					// Toast.LENGTH_LONG).show();
					final boolean needResult = getIntent().getBooleanExtra("needResult", false);
					if (needResult) {
						Intent resultData = new Intent();
						JSONObject json = new JSONObject();
						try {
							json.put("callback", 1);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						resultData.putExtra("callback", json.toString());
						setResult(RESULT_OK, resultData);
						Intent intent = new Intent();
						intent.setClass(getApplicationContext(), HandwritingActivity.class);
						Bundle bundle = new Bundle();
						bundle.putSerializable("msgBean", msgBean);
						intent.putExtra("uid", getIntent().getStringExtra("uid"));
						intent.putExtra("name", "");
						intent.putExtra("merchant", "");
						intent.putExtra("terminal", "");
						intent.putExtra("cardNo", creditCardBean.acctNo);
						intent.putExtra("iss", "");
						intent.putExtra("amount", ed_crad_amount.getText().toString().trim());
						intent.putExtra("ReferNO", msgBean.ReferNO);
						intent.putExtra("TransDate", msgBean.TransDate);
						intent.putExtra("TransTime", msgBean.TransTime);
						intent.putExtra("orderno", msgBean.orderno);
						intent.putExtra("sign_path", sign_path);// 待合成的图片
						intent.putExtras(bundle);
						startActivity(intent);
						topUpDialog.dismiss();
					}
					;
					finish();
				} else {

					Toast.makeText(getApplicationContext(), msgBean.RspMsg, Toast.LENGTH_LONG).show();
					finish();
				}

			}
		});

	}

	/**
	 * 
	 * handleBack:(刷新卡号). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param s
	 * @since JDK 1.6
	 */
	private void handleBack(final String s) {

		/*
		 * if(CHOSE_DEVICE.equals("A")){
		 * if(BaseActivity.BlueToothPsd.equals("")||BaseActivity.BlueToothPsd.
		 * equals("null")){// myFinish(); return; } }
		 */
		if (BaseActivity.BlueToothPsd.equals("null") && CHOSE_DEVICE.equals("B")) {
			myFinish();
			Tip("退出了交易");
			return;
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String getNo = txt_crad_number.getText().toString().replaceAll(".{4}(?!$)", "$0 ");
				if (getNo == null || getNo.length() <= 0) {
					Tip("还未绑定信用卡");
					return;
				}
				getNo = getNo.replace(" ", "");
				String swipcardno = s;
				swipcardno = swipcardno.replace(" ", "");
				// String
				// get_account=(getIntent().getStringExtra("acctNo")).replaceAll(".{4}(?!$)",
				// "$0 ");
				Log.v("param2", "swip-->" + swipcardno);
				Log.v("param2", "txt-->" + getNo);
				/*
				 * Tip("swip-->"+swipcardno); Tip("txt-->"+getNo);
				 */
				if (!swipcardno.equals(getNo)) {
					Toast.makeText(TimelyAccountTopUpActivity.this, "请刷上面认证过的信用卡", Toast.LENGTH_SHORT).show();
					// 重新刷卡
					operation_stay(Common.FETCH_DEVICE_INFO);
				} else {
					btn_ok.setText("确定");
					if (CHOSE_DEVICE.equals("C") || CHOSE_DEVICE.equals("B")) {
						try {
							controller.reset();
						} catch (Exception e) {
							// TODO: handle exception
						}

						// submit();
						goToHanwriting(creditCardBean, msgBean, ed_crad_amount.getText().toString().trim());
					} else {
						topUpDialog.ed_crad_pass.setVisibility(View.VISIBLE);
						topUpDialog.show();
						topUpDialog.txt_crad_moery.setText("¥" + ed_crad_amount.getText().toString().trim());
						// topUpDialog.txt_cradNumber.setText(txt_cash_crad.getText().toString().trim()+"");
						topUpDialog.txt_cradNumber.setText(swipcardno + "");
						topUpDialog.txt_crad_t.setText("¥" + ed_crad_amount.getText().toString().trim());
					}
				}

			}
		});
	}

	/**
	 * 
	 * operation_stay:(执行事件). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param type
	 * @since JDK 1.6
	 */
	private void operation_stay(int type) {
		switch (type) {
		// 取消
		case Common.CANCEL:
			new Thread(new Runnable() {
				@Override
				public void run() {
					if (DeviceConnState.CONNECTED == controller.getDeviceConnState()) {
						try {
							controller.reset();
						} catch (Exception e) {
							Message msg = new Message();
							msg.obj = "撤消指令执行失败!" + e.getMessage();
							iHandler.sendMessage(msg);
						}
					} else {
						Message msg = new Message();
						msg.obj = "设备未连接!";
						iHandler.sendMessage(msg);

					}
					processing = false;
					// 重新交易
					operation_stay(Common.FETCH_DEVICE_INFO);
				};
			}).start();
			break;

		// 取得设备信息
		case Common.FETCH_DEVICE_INFO:
			// if (processing) {
			// operation_stay(Common.CANCEL);
			// } else {
			// new Thread(new Runnable() {
			// @Override
			// public void run() {
			// getDeviceinfo();
			//
			// }
			// }).start();
			// }
			new Thread(new Runnable() {
				@Override
				public void run() {
					getDeviceinfo();

				}
			}).start();
			break;

		// 刷卡或者插卡
		case Common.SWIPCARD_ME11:

			new Thread(new Runnable() {
				@Override
				public void run() {
					if (CHOSE_DEVICE.equals("A")) {
						swiperCard();
					} else {// 蓝牙
						swipBlueTooth();
					}
				}
			}).start();
		}
	}

	// 蓝牙刷卡流程
	void swipBlueTooth() {
		swipShow();
		BaseActivity.BlueToothPsd = "";
		String p = ed_crad_amount.getText().toString().trim();
		try {
			if (Integer.parseInt(p) < 1) {
				Tip("请输入有效金额");
				cardHeadDialog.dismiss();
				return;
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
		final SwipResult swipResult = controller.swipCardMe3X("交易金额为" + p + "\n元请刷卡/插卡", p, new SimpleTransferListener(), 30000L, TimeUnit.MILLISECONDS, needTime);
		if (swipResult != null) {
			final String account = swipResult.getAccount().getAcctNo().replaceAll("(?i)F", "");

			Log.e("sean", account);
			bankCrad = new BankCrad();
			bankCrad.ksn = csn;
			bankCrad.account = account;
			bankCrad.pointService = "021";
			if (swipResult.getSecondTrackData() != null) {
				bankCrad.accNoT2 = SetTrackData(UnpackTrack(swipResult.getSecondTrackData())).toUpperCase();
			} else {
				cardHeadDialog.dismiss();
				Tip("获取磁道信息失败，请重新刷卡");
				return;
			}
			if (swipResult.getThirdTrackData() != null) {
				bankCrad.accNoT3 = SetTrackData(UnpackTrack(swipResult.getThirdTrackData())).toUpperCase();
			} /*
			 * else{ cardHeadDialog.dismiss(); Tip("获取磁道信息失败，请重新刷卡"); return; }
			 */
			// controller.startReadingPwd("请输入密码", new DeviceListener());
			bankCrad.ic = "0";
			bankCrad.EncWorkingKey = getHex_workkey();
			new Thread() {
				public void run() {
					String p = ed_crad_amount.getText().toString().trim();

					controller.startReadingPwd("交易金额为" + p + "元\n请输入密码", new DeviceListener());
					handleBack(account);
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							cardHeadDialog.dismiss();

						}

					});

					creditCardBean = setCreditCardBean(bankCrad);
				};
			}.start();

		} else {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					cardHeadDialog.txt_title.setText("正在读取卡的信息");
					cardHeadDialog.tip_pb.setVisibility(View.VISIBLE);
				}

			});
		}

		swipHidden();

	}

	/**
	 * 初始化设备
	 * 
	 * @since ver1.0
	 * @param params
	 *            设备连接参数
	 */
	private void initMe3xDeviceController(DeviceConnParams params) {
		controller.init(TimelyAccountTopUpActivity.this, Common.ME11_DRIVER_NAME, params, new DeviceEventListener<ConnectionCloseEvent>() {
			@SuppressLint("NewApi")
			@Override
			public void onEvent(ConnectionCloseEvent event, Handler handler) {
				if (event.isSuccess()) {// 设备被客户主动断开！

				}
				if (event.isFailed()) {// 设备链接异常断开！
					Message msg = new Message();
					msg.obj = "设备已经拔出,请插入刷卡器!!!";
					msg.what = connect;
					iHandler.sendMessage(msg);
					DealDisConnectedMessage(dialogHandler);

					initMe3xDeviceController(new AudioPortV100ConnParams());
					operation_stay(Common.FETCH_DEVICE_INFO);
				}
			}

			@Override
			public Handler getUIHandler() {
				return null;
			}
		});

		processing = false;

	}

	/**
	 * getDeviceinfo:(获取设备信息). <br/>
	 * 
	 * @author zhuxiaohao
	 * @since JDK 1.6
	 */
	@SuppressWarnings("unused")
	@SuppressLint("NewApi")
	private void getDeviceinfo() {
		processing = true;
		try {
			connectDevice();

			csn = deviceInfo.getCSN();
			if (deviceInfo == null) {
				return;
			} else if (deviceInfo != null) {
				operation_stay(Common.SWIPCARD_ME11);
				DealConnectedMessage(dialogHandle);

			}
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	/**
	 * 设备连接
	 */
	@SuppressLint("NewApi")
	public void connectDevice() {
		try {
			runOnUiThread(new Runnable() {
				public void run() {
					if (CHOSE_DEVICE.equals("A")) {
						cardHeadDialog.txt_title.setText("请插入卡头");
					} else {
						cardHeadDialog.txt_title.setText("正在连接蓝牙设备");
					}

				};
			});

			try {
				controller.connect();
			} catch (Exception e) {
				Tip("连接失败，未配对正确设备");
			}

			runOnUiThread(new Runnable() {
				public void run() {
					cardHeadDialog.tip_pb.setVisibility(View.VISIBLE);
					cardHeadDialog.txt_title.setText("正在连接设备......");
				};
			});
			exec.scheduleAtFixedRate(new Runnable() {

				@Override
				public void run() {
					try {
						BatteryInfoResult bfr = controller.getPowerLevel();
						if (bfr == null & Controller.isEmpty(txt_cash_crad.getText().toString().trim())) {

							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(TimelyAccountTopUpActivity.this, "连接中断,请稍后重试", Toast.LENGTH_SHORT).show();

								}

							});
							TimelyAccountTopUpActivity.this.finish();
							exec.isShutdown();
							exec = null;
						}
					} catch (Exception e) {
						BatteryInfoResult bfr = controller.getPowerLevel();
						if (bfr == null & Controller.isEmpty(txt_cash_crad.getText().toString().trim())) {
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(TimelyAccountTopUpActivity.this, "连接中断,请稍后重试", Toast.LENGTH_SHORT).show();

								}

							});
							TimelyAccountTopUpActivity.this.finish();
							exec.isShutdown();
							exec = null;

						}

					}
					/**/

				}

			}, 30, 300, TimeUnit.MILLISECONDS);
			Looper.prepare();
			deviceInfo = controller.getDeviceInfo();
			if (deviceInfo != null) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {

						Message msgs = new Message();
						msgs.obj = "请刷卡或者插卡";
						iHandler.sendMessage(msgs);
						Toast.makeText(TimelyAccountTopUpActivity.this, "请刷卡或者插卡", Toast.LENGTH_SHORT).show();
						DealConnectedMessage(dialogHandler);

						cardHeadDialog.tip_pb.setVisibility(View.GONE);
					}

				});
				operation_stay(Common.SWIPCARD_ME11);
			}
		} catch (

		Exception e1) {
			e1.printStackTrace();

		}
	}

	/**
	 * 
	 * addMessageHandler:(刷新对话框的事件). <br/>
	 * 
	 * @author zhuxiaohao
	 * @since JDK 1.6
	 */
	private void addMessageHandler() {
		iHandler = new Handler() {
			public void handleMessage(Message msg) {
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

	/**
	 * 
	 * setCreditCardBean:(组装请求数据). <br/>
	 * 
	 * @author zhuxiaohao
	 * @return 组装数据对象
	 * @since JDK 1.6
	 */
	@SuppressWarnings("unused")
	private CreditCardBean setCreditCardBean(BankCrad bankCrad) {
		CreditCardBean creditCardBean = null;
		if (bankCrad != null) {
			creditCardBean = new CreditCardBean();
			creditCardBean.pointService = bankCrad.pointService;
			creditCardBean.acctNo = bankCrad.acctNo;
			creditCardBean.transAmt = bankCrad.transAmt;
			// creditCardBean.trackdatas = bankCrad.trackdatas;
			creditCardBean.pin = bankCrad.pin;
			creditCardBean.tranxSN = bankCrad.tranxSN;
			creditCardBean.ic = bankCrad.ic;
			creditCardBean.cardEXPDate = bankCrad.cardEXPDate;
			creditCardBean.cardSN = bankCrad.cardSN;
			creditCardBean.ksn = bankCrad.ksn;
			creditCardBean.encWorkingKey = bankCrad.EncWorkingKey;
			creditCardBean.acctNoT2 = bankCrad.accNoT2;
			creditCardBean.acctNoT3 = bankCrad.accNoT3;
			String gps = getIntent().getStringExtra("GPS");
			if (gps != null) {
				creditCardBean.gps = gps;
			}
		}
		return creditCardBean;
	}

	/**
	 * 
	 * swiperCard:(刷卡或者插卡). <br/>
	 * 
	 * @author zhuxiaohao
	 * @since JDK 1.6
	 */
	@SuppressLint("NewApi")
	private void swiperCard() {
		try {
			swipShow();
			ME11SwipResult swipRslt = controller.swipCard("1", 30000L, TimeUnit.MILLISECONDS);
			if (swipRslt == null) {
				return;
			} else {

				ModuleType[] moduleType = swipRslt.getReadModels();
				if (moduleType[0] == ModuleType.COMMON_ICCARD) {// ic卡插卡
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							cardHeadDialog.txt_title.setText("正在读取卡的信息");
							cardHeadDialog.tip_pb.setVisibility(View.VISIBLE);
							swipHidden();
						}

					});
					controller.startEmv(new BigDecimal(ed_crad_amount.getText().toString().trim()), new SimpleTransferListener());
				} else if (moduleType[0] == ModuleType.COMMON_SWIPER) {// 刷卡
					String kzinfoTrack = Dump.getHexDump(swipRslt.getExtInfo()).replaceAll(" ", "");
					account = swipRslt.getAccount().getAcctNo().replaceAll("(?i)F", "");
					bankCrad = new BankCrad();
					bankCrad.ksn = csn;
					bankCrad.account = account;
					bankCrad.pointService = "021";
					bankCrad.accNoT2 = kzinfoTrack.substring(16, kzinfoTrack.length());
					bankCrad.accNoT3 = "";
					// bankCrad.trackdatas = kzinfoTrack.substring(16,
					// kzinfoTrack.length());
					bankCrad.ic = "0";
					bankCrad.EncWorkingKey = kzinfoTrack.substring(0, 16);
					creditCardBean = setCreditCardBean(bankCrad);
					handleBack(account);
					cardHeadDialog.dismiss();
				}
			}

		} catch (Exception e) {
			if (DeviceConnState.CONNECTED == controller.getDeviceConnState()) {
				Message msg = new Message();
				msg.obj = "刷卡或者插卡失败" + e.getMessage() + ",请重新刷卡或者插卡";
				msg.what = cancel;
				iHandler.sendMessage(msg);
				if (closed) {
					return;
				}
				swiperCard();
			}
		}
	}

	/**
	 * 
	 * ClassName: SimpleTransferListener <br/>
	 * Function: TODO ADD FUNCTION. <br/>
	 * Reason: TODO ADD REASON(可选). <br/>
	 * date: 2015-4-24 下午2:31:21 <br/>
	 * blog:http://blog.csdn.net/qq718799510?viewmode=contents
	 * 
	 * @author zhuxiaohao
	 * @version TopUpActivity
	 * @since JDK 1.6 芯片卡监听
	 */
	boolean hasBeanReaded = false;

	private class SimpleTransferListener implements TransferListener {
		@Override
		public void onEmvFinished(boolean arg0, EmvTransInfo context) throws Exception {
			if (closed) {
				return;
			}
			if (isStand == 1) {
				Message msg = new Message();
				msg.obj = "IC卡暂不支持转账汇款和信用卡还款的功能!!!";
				iHandler.sendMessage(msg);
				return;
			}
			if (bankCrad == null) {
				Message msg = new Message();
				msg.obj = "插卡失败,请重插...";
				iHandler.sendMessage(msg);
				operation_stay(Common.SWIPCARD_ME11);
				return;
			}
			closed = true;
		}

		@Override
		public void onError(EmvTransController arg0, Exception arg1) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onFallback(EmvTransInfo arg0) throws Exception {
			// TODO Auto-generated method stub
		}

		// 芯片卡 55域
		@Override
		public void onRequestOnline(EmvTransController arg0, EmvTransInfo context) throws Exception {
			if (!hasBeanReaded) {
				hasBeanReaded = true;

				List<Integer> L_55TAGS = new ArrayList<Integer>();

				L_55TAGS.add(0x9F26);// "9F26:"
				L_55TAGS.add(0x9F27);
				L_55TAGS.add(0x9F10);
				L_55TAGS.add(0x9F37);
				L_55TAGS.add(0x9F36);
				L_55TAGS.add(0x95);
				L_55TAGS.add(0x9A);
				L_55TAGS.add(0x9C);
				L_55TAGS.add(0x9F02);
				L_55TAGS.add(0x5F2A);
				L_55TAGS.add(0x82);

				L_55TAGS.add(0x9F1A);
				L_55TAGS.add(0x9F03);
				L_55TAGS.add(0x9F33);
				L_55TAGS.add(0x9F34);
				L_55TAGS.add(0x9F35);

				L_55TAGS.add(0x9F1E);
				L_55TAGS.add(0x84);
				L_55TAGS.add(0x9F09);
				L_55TAGS.add(0x9F41);
				// L_55TAGS.add(0x9F63);

				TLVPackage tlvPackage = context.setExternalInfoPackage(L_55TAGS);
				String y55 = ISOUtils.hexString(tlvPackage.pack());
				account = context.getCardNo();
				String CardSequenceNumber = context.getCardSequenceNumber();
				String kzinfoTrack = Dump.getHexDump(context.getTrack_2_eqv_data()).replaceAll(" ", "");
				bankCrad = new BankCrad();
				bankCrad.ksn = csn;
				bankCrad.account = account;
				// bankCrad.trackdatas = kzinfoTrack.substring(16,
				// kzinfoTrack.length());
				bankCrad.ic = y55;
				bankCrad.EncWorkingKey = kzinfoTrack.substring(0, 16);
				bankCrad.cardSN = CardSequenceNumber;
				bankCrad.pointService = "051";
				bankCrad.cardEXPDate = context.getCardExpirationDate().substring(0, 4);
				if (CHOSE_DEVICE.equals("A")) {// 音频
					bankCrad.accNoT2 = kzinfoTrack.substring(16, kzinfoTrack.length());
					bankCrad.accNoT3 = "";
					bankCrad.EncWorkingKey = kzinfoTrack.substring(0, 16);
				} else {// 蓝牙
					bankCrad.accNoT2 = Dump.getHexDump(context.getTrack_2_eqv_data()).replace("F", "").replace(">", "").replace("<", "").replace("=", "");
					bankCrad.accNoT3 = "";
					bankCrad.EncWorkingKey = getHex_workkey();
				}
				if (CHOSE_DEVICE.equals("B")) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							if (CHOSE_DEVICE.equals("B")) {
								cardHeadDialog.txt_title.setText("请输入密码");
								cardHeadDialog.tip_pb.setVisibility(View.VISIBLE);
							}

						}
					});
					String p = ed_crad_amount.getText().toString().trim();
					controller.startReadingPwd("交易金额为" + p + "元\n请输入密码", new DeviceListener());
				}
				SecondIssuanceRequest request = new SecondIssuanceRequest();
				request.setAuthorisationResponseCode("00");
				arg0.secondIssuance(request);
				cardHeadDialog.dismiss();
				creditCardBean = setCreditCardBean(bankCrad);
				handleBack(account);
			}

		}

		@Override
		public void onRequestPinEntry(EmvTransController arg0, EmvTransInfo arg1) throws Exception {
			Message msg = new Message();
			msg.obj = "错误的事件返回，不可能要求密码输入";
			iHandler.sendMessage(msg);
			arg0.cancelEmv();
		}

		@Override
		public void onRequestSelectApplication(EmvTransController arg0, EmvTransInfo arg1) throws Exception {
			Message msg = new Message();
			msg.obj = "错误的事件返回，不可能要求应用选择！";
			iHandler.sendMessage(msg);
			arg0.cancelEmv();
		}

		@Override
		public void onRequestTransferConfirm(EmvTransController arg0, EmvTransInfo arg1) throws Exception {
		}

		@Override
		public void onSwipMagneticCard(SwipResult swipRslt) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onOpenCardreaderCanceled() {
			// TODO Auto-generated method stub
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			controller.destroy(); // 终端后的相关状态处理会通过事件完成,此处不需要处理
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	void myFinish() {
		final boolean needResult = getIntent().getBooleanExtra("needResult", false);
		if (needResult) {
			Intent resultData = new Intent();
			JSONObject json = new JSONObject();
			try {
				json.put("callback", 0);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			resultData.putExtra("callback", json.toString());
			setResult(RESULT_OK, resultData);
		}
		;
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			closed = true;
			controller.destroy();
			myFinish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private Handler dialogHandle = new Handler() {
		@SuppressLint("NewApi")
		public void handleMessage(Message msg) {
			// Tip(msg.what+"");
			switch (msg.what) {
			case 1:// 卡头连接成功
				cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("cash2")));
				break;
			case 2:
				cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("cash")));
				break;
			case 3:// 连接成功
				cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("ly_cash2")));// 蓝牙连上
				break;
			case 4:
				cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("ly_cash")));// 蓝牙未连
				break;
			case Configure.ME15_DISCONNECTED:
				cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("ly_cash_me15")));// 蓝牙未连上
				break;
			case Configure.ME15_CONNECTED:
				cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("ly_cash2_me15")));// 蓝牙连
				break;
			case Configure.DH_DISCONNECTED:
				cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("ly_cash_dh")));//
				break;
			case Configure.DH_CONNECTED:
				cardHeadDialog.iv_head.setBackground(getResources().getDrawable(UZResourcesIDFinder.getResDrawableID("ly_cash2_dh")));//
				break;

			}
		};
	};

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
