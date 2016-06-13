package com.apicloud.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apicloud.activity.timely.TimelyAccountTopUpActivity;
import com.apicloud.activity.topup.TopUpDialog;
import com.apicloud.activity.write.HandwritingActivity;
import com.apicloud.adapter.MyCreditCardAdapter;
import com.apicloud.common.Common;
import com.apicloud.common.PinSecurityImpl;
import com.apicloud.controller.Controller;
import com.apicloud.controller.DeviceListener;
import com.apicloud.controller.MyController;
import com.apicloud.controller.TransferListener;
import com.apicloud.landy.LandyTackMsg;
import com.apicloud.module.BankCrad;
import com.apicloud.module.CardBean;
import com.apicloud.module.CardBean.DataBean;
import com.apicloud.module.CreditCardBean;
import com.apicloud.module.LoginBean;
import com.apicloud.module.MsgBean;
import com.apicloud.swip.SwipApi;
import com.apicloud.util.Configure;
import com.apicloud.util.Constant;
import com.apicloud.util.UICommon;
import com.apicloud.view.LoadingDialog;
import com.newland.me.DeviceManager.DeviceConnState;
import com.newland.mtype.BatteryInfoResult;
import com.newland.mtype.ConnectionCloseEvent;
import com.newland.mtype.DeviceInfo;
import com.newland.mtype.conn.DeviceConnParams;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.emv.EmvTransController;
import com.newland.mtype.module.common.emv.EmvTransInfo;
import com.newland.mtype.module.common.emv.SecondIssuanceRequest;
import com.newland.mtype.module.common.swiper.SwipResult;
import com.newland.mtype.tlv.TLVPackage;
import com.newland.mtype.util.Dump;
import com.newland.mtype.util.ISOUtils;
import com.newland.mtypex.audioport.AudioPortV100ConnParams;
import com.squareup.picasso.Picasso;
import com.umeng.analytics.MobclickAgent;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

public class RealTimeActivity extends BasicActivity implements OnClickListener {
	private LinearLayout layout_add_card;
	private ImageButton ib_return;
	private EditText ed_crad_amount;
	private EditText et_code;
	private TextView tv_code;
	private Button btn_refresh;
	Handler handler = new Handler();
	public TextView btn_ok;
	private CardBean cardBean;
	private String key;
	private View popupView;
	private ListView cardNameLv;
	private RelativeLayout layout_card_select;
	public static final int REQUEST_ADD_CREDIT = 103;
	private PopupWindow mPopupWindow;
	private TextView txt_card_name;// 付款人姓名
	private TextView txt_card_number;// 付款人卡号
	private List<Map<String, String>> items = new ArrayList<Map<String, String>>();
	boolean isFirstAdd = true;
	private ImageView iv_card_show;// 信用卡银行所属图片
	private TextView txt_cash_card;// 主卡卡号
	private ImageView iv_card_ico;// 主卡图标
	private String uid = "";
	private TextView tv_delete;
	public Handler iHandler,sonHandler;
	boolean processing = false;
	private String csn;
	int cancel = 2;// 如果取消
	int connect = 4;// 如果连接失败
	int isStand = 0;// 如果出现不支持卡状态
	int swiper = 3;// 刷卡状态
	DeviceInfo deviceInfo;// 设备信息
	ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
	BankCrad bankCrad;
	boolean closed = false;
	String account;// 获取的卡号
	CreditCardBean creditCardBean;// 请求的信用卡对象
	TopUpDialog topUpDialog;// 输入密码对话框
	MsgBean msgBean;
	ProgressDialog pd;
	String price;
	private String chose_device="E";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("realtime_account"));
		controller = new MyController();
		initView();
		initPopupWindow();
		
		
		topUpDialog = new TopUpDialog(RealTimeActivity.this);
		topUpDialog.setOnclickListener(this);
		addMessageHandler();

		initSonHandler();
	}

	private void initView() {
		layout_add_card = (LinearLayout) findViewById(UZResourcesIDFinder.getResIdID("layout_add_card"));
		ib_return = (ImageButton) findViewById(UZResourcesIDFinder.getResIdID("ib_return"));
		ed_crad_amount = (EditText) findViewById(UZResourcesIDFinder.getResIdID("ed_crad_amount"));// 金额
		et_code = (EditText) findViewById(UZResourcesIDFinder.getResIdID("et_code"));// 输入的验证码
		tv_code = (TextView) findViewById(UZResourcesIDFinder.getResIdID("tv_code"));// 动态显示的验证码
		btn_refresh = (Button) findViewById(UZResourcesIDFinder.getResIdID("btn_refresh"));// 验证码刷新按钮
		btn_ok = (TextView) findViewById(UZResourcesIDFinder.getResIdID("btn_ok"));// 确认支付
		layout_card_select = (RelativeLayout) findViewById(UZResourcesIDFinder.getResIdID("layout_card_select"));
		txt_card_name = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_crad_name"));
		txt_card_number = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_crad_number"));
		iv_card_show = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("iv_crad_show"));
		txt_cash_card = (TextView) findViewById(UZResourcesIDFinder.getResIdID("txt_cash_crad"));
		iv_card_ico = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("iv_card_ico"));
		tv_delete = (TextView) findViewById(UZResourcesIDFinder.getResIdID("tv_delete"));
		setGetCardNum(btn_ok);
		
		ib_return.setOnClickListener(this);
		layout_add_card.setOnClickListener(this);
		btn_refresh.setOnClickListener(this);
		btn_ok.setOnClickListener(this);
		layout_card_select.setOnClickListener(this);
		tv_delete.setOnClickListener(this);
		CHOSE_DEVICE=chose_device;
		
		pd = new LoadingDialog(RealTimeActivity.this);
		pd.setCancelable(false);// 设置进度条是否可以按退回键取消
		pd.setCanceledOnTouchOutside(false);// 设置点击进度对话框外的区域对话框不消失
		pd.show();
		needShowMoeny = true;
		amount_et = ed_crad_amount;
		initParentView();
		initSwipParam();

	}
	void initSwipParam() {
		fromAct = UICommon.RealTimeActivity;
		whatDo = SwipApi.WHATDO_SWIPER;
		cur_Ac = this;
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
	 * 获取验证码
	 * 
	 * @param uid
	 */
	private void getCode(final String uid) {
		new Thread() {
			@Override
			public void run() {

				super.run();
				final String code = controller.getCode(uid);
				handler.post(new Runnable() {

					@Override
					public void run() {
						tv_code.setText(code);

					}
				});
			}
		}.start();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
		if (Constant.isNeedRefreshCardInfo) {
			items.clear();
			initPopupWindow();
		}
	}
	private void initPopupWindow() {
		popupView = getLayoutInflater().inflate(UZResourcesIDFinder.getResLayoutID("popwindow_creditcard"), null);
		cardNameLv = (ListView) popupView.findViewById(UZResourcesIDFinder.getResIdID("watchView"));
		getPaymentsData();
		pd.dismiss();
		
	}

	private void getPaymentsData() {
		new Thread() {
			@Override
			public void run() {

				super.run();

				final LoginBean loginBean = controller.doLogin("18602123569", "65727647", "10010001");
				key = loginBean.error;
				uid = key;
				getCode(uid);
				setParentCode(et_code, uid);
				cardBean = controller.getCardInfo(RealTimeActivity.this, key, "rcard");
				handler.post(new Runnable() {

					@Override
					public void run() {
						if (cardBean == null) {
							layout_card_select.setVisibility(View.GONE);
						} else {
							addCardName();
							if (cardBean != null && cardBean.getZp() != null) {
								txt_cash_card.setText(cardBean.getZp().replaceAll(".{4}(?!$)", "$0"));
							}
							Picasso.with(RealTimeActivity.this).load(cardBean.getZp_img()).into(iv_card_ico);
							if (items != null && items.size() > 0) {
								filterMap();
								MyCreditCardAdapter myCreditAdapter = new MyCreditCardAdapter(RealTimeActivity.this, items);
								cardNameLv.setAdapter(myCreditAdapter);
								cardNameLv.setOnItemClickListener(new OnItemClickListener() {

									@Override
									public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
										String url = items.get(position).get("card_img");
										txt_card_name.setText(items.get(position).get("uname"));
										txt_card_number.setText(items.get(position).get("card_no"));
										txt_card_number.setTag(items.get(position).get("id"));
										if (!TextUtils.isEmpty(url)) {
											Picasso.with(RealTimeActivity.this).load(url).into(iv_card_show);
										}
										items.clear();
										mPopupWindow.dismiss();

										addCardName();
									}

								});
							}
							mPopupWindow = new PopupWindow(popupView, AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT, true);
							mPopupWindow.setTouchable(true);
							mPopupWindow.setOutsideTouchable(false);
							mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));
						}

					}
				});
			}
		}.start();
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
		List<DataBean> list = cardBean.getData();
		if (list != null && list.size() > 0) {
			layout_card_select.setVisibility(View.VISIBLE);
			for (int i = 0; i < list.size(); i++) {
				Map<String, String> map = new HashMap<String, String>();
				map.put("card_img", list.get(i).getCardimg());
				map.put("card_name", list.get(i).getCardname());
				map.put("card_no", list.get(i).getCardno());
				map.put("uname", list.get(i).getUname());
				map.put("id", list.get(i).getId());
				items.add(map);
			}
			if (isFirstAdd) {
				txt_card_name.setText(items.get(0).get("uname"));
				txt_card_number.setText(items.get(0).get("card_no"));
				txt_card_number.setTag(items.get(0).get("id"));
				if (!TextUtils.isEmpty(items.get(0).get("card_img"))) {
					Picasso.with(RealTimeActivity.this).load(items.get(0).get("card_img")).into(iv_card_show);
				}
				isFirstAdd = false;
			}
			for (int i = 0; i < list.size(); i++) {
				if (txt_card_number.getText().toString().equals(items.get(i).get("card_no"))) {
					if (!TextUtils.isEmpty(items.get(i).get("card_img"))) {
						Log.v("param2", "card_img-->" + items.get(i).get("card_img"));
						Log.v("param2", "card_no-->" + items.get(i).get("card_no"));
					}
					items.remove(i);
					break;
				}
			}
		} else {
			layout_card_select.setVisibility(View.GONE);
		}

	}

	@Override
	public void onClick(View v) {
		if (v == layout_add_card) {
			Intent intent = new Intent(RealTimeActivity.this, AddNewCreditActivity.class);
			startActivityForResult(intent, REQUEST_ADD_CREDIT);
		}
		if (v == ib_return) {
			finish();
		}
		if (v == layout_card_select) {
			try {
				mPopupWindow.showAsDropDown(v);
			} catch (Exception e) {

				Log.v("click_card_select", e.getMessage());
			}
		}
		if (v == btn_refresh) {
			new Thread() {
				@Override
				public void run() {

					super.run();
					final String code2 = controller.getCode(uid);
					handler.post(new Runnable() {

						@Override
						public void run() {

							tv_code.setText(code2);

						}
					});
				}
			}.start();
		}

		if (v == tv_delete) {
			String deleteId = txt_card_number.getTag().toString().trim();
			if (deleteId.length() > 0) {
				delCreditCard(deleteId);
			}
		}
		// 确定
		if (v == btn_ok) {
			final String code1 = et_code_parent.getText().toString() + "";
			if (code1.length() >= 0) {
				new Thread() {
					public void run() {
						if (!controller.validateCode(code1, uid)) {
							Log.e("输入的验证码", code1.toString());
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
			Toast.makeText(RealTimeActivity.this, "点击确定", Toast.LENGTH_LONG).show();
			doOk();
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
	 * doOk:(请求服务器支付). 
	 * 
	 * 
	 * 
	 */
	@SuppressWarnings("static-access")
	private void doOk() {
		
		if ((CHOSE_DEVICE.equals("A") || CHOSE_DEVICE.equals("D")) && Controller.isEmpty(topUpDialog.ed_crad_pass.getText().toString().trim())) {
			Toast.makeText(RealTimeActivity.this, "密码不能为空", Toast.LENGTH_LONG).show();
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
					Log.v("what is the problem", "i don't know");
					pd.show();

				}

			});
			btn_ok.setClickable(false);
			if (CHOSE_DEVICE.equals("C")) {
				price = amount_et.getText().toString().trim();
				bankCradParent.transAmt = price;
				creditCardBean = setCreditCardBean(bankCradParent);
			}
			Log.v("locationinfo------", locationInfo.toString());
			if (locationInfo != null && locationInfo.length() > 0) {
				creditCardBean.gps = locationInfo;
				//submit();
				goToHanwriting(creditCardBean, msgBean, ed_crad_amount.getText().toString().trim());
			} else {
				Tip("定位失败，请检查网络等，重新进入页面定位");
			}

		}
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
					if (DeviceConnState.CONNECTED == deviceController.getDeviceConnState()) {
						try {
							deviceController.reset();
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
						// 建设中
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
		BasicActivity.BlueToothPsd = "";
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
		final SwipResult swipResult = deviceController.swipCardMe3X("交易金额为" + p + "\n元请刷卡/插卡", p, new SimpleTransferListener(), 30000L, TimeUnit.MILLISECONDS, needTime);
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

					deviceController.startReadingPwd("交易金额为" + p + "元\n请输入密码", new DeviceListener());
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
		if (BasicActivity.BlueToothPsd.equals("null") && CHOSE_DEVICE.equals("B")) {
			myFinish();
			Tip("退出了交易");
			return;
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String getNo = txt_card_number.getText().toString().replaceAll(".{4}(?!$)", "$0 ");
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
					Toast.makeText(RealTimeActivity.this, "请刷上面认证过的信用卡", Toast.LENGTH_SHORT).show();
					// 重新刷卡
					operation_stay(Common.FETCH_DEVICE_INFO);
				} else {
					btn_ok.setText("确定");
					if (CHOSE_DEVICE.equals("C") || CHOSE_DEVICE.equals("B")) {
						try {
							deviceController.reset();
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
					deviceController.startReadingPwd("交易金额为" + p + "元\n请输入密码", new DeviceListener());
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

	/**
	 * 初始化设备
	 * 
	 * @since ver1.0
	 * @param params
	 *            设备连接参数
	 */
	private void initMe3xDeviceController(DeviceConnParams params) {
		deviceController.init(RealTimeActivity.this, Common.ME11_DRIVER_NAME, params, new DeviceEventListener<ConnectionCloseEvent>() {
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
				deviceController.connect();
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
						BatteryInfoResult bfr = deviceController.getPowerLevel();
						if (bfr == null & Controller.isEmpty(txt_cash_card.getText().toString().trim())) {

							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(RealTimeActivity.this, "连接中断,请稍后重试", Toast.LENGTH_SHORT).show();

								}

							});
							RealTimeActivity.this.finish();
							exec.isShutdown();
							exec = null;
						}
					} catch (Exception e) {
						BatteryInfoResult bfr = deviceController.getPowerLevel();
						if (bfr == null & Controller.isEmpty(txt_cash_card.getText().toString().trim())) {
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(RealTimeActivity.this, "连接中断,请稍后重试", Toast.LENGTH_SHORT).show();

								}

							});
							RealTimeActivity.this.finish();
							exec.isShutdown();
							exec = null;

						}

					}
					/**/

				}

			}, 30, 300, TimeUnit.MILLISECONDS);
			Looper.prepare();
			deviceInfo = deviceController.getDeviceInfo();
			if (deviceInfo != null) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {

						Message msgs = new Message();
						msgs.obj = "请刷卡或者插卡";
						iHandler.sendMessage(msgs);
						Toast.makeText(RealTimeActivity.this, "请刷卡或者插卡", Toast.LENGTH_SHORT).show();
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
			String getNo = txt_card_number.getText().toString().replaceAll(".{4}(?!$)", "$0 ");
			getNo = getNo.replace(" ", "");
			String swipcardno = landybean.cardNo;
			swipcardno = swipcardno.replace(" ", "");
			// String
			// get_account=(getIntent().getStringExtra("acctNo")).replaceAll(".{4}(?!$)",
			// "$0 ");
			if (!swipcardno.equals(getNo)) {
				Toast.makeText(RealTimeActivity.this, "请刷上面认证过的信用卡", Toast.LENGTH_SHORT).show();
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
	void submit() {
		new Thread() {
			@SuppressWarnings("static-access")
			@Override
			public void run() {
				
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
				creditCardBean.lkey = key;
				if (CHOSE_DEVICE.equals("A") || CHOSE_DEVICE.equals("B") || CHOSE_DEVICE.equals("C") || CHOSE_DEVICE.equals("D")) {
					creditCardBean.transAmt = Common.conversionPrice(ed_crad_amount.getText().toString().trim());
				}
				if (CHOSE_DEVICE.equals(UICommon.WFT_DEVICE) || CHOSE_DEVICE.equals(UICommon.LANDY_DEVICE) || CHOSE_DEVICE.equals(UICommon.CFT_DEVICE) || CHOSE_DEVICE.equals(UICommon.AF_DEVICE) || CHOSE_DEVICE.equals(UICommon.BBPOS_IC_DEVICE)) {
					creditCardBean.transAmt = Common.conversionPrice(creditCardBean.transAmt + "");
				}
				// creditCardBean.transAmt =
				// Common.conversionPrice(ed_crad_amount.getText().toString().trim());
				creditCardBean.cardNo = txt_cash_card.getText().toString() + "";// 收款账户
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
				creditCardBean.acctNo = txt_card_number.getText().toString().replaceAll(".{4}(?!$)", "$0 ").replace(" ", "");
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
				Log.e("lkey------->", creditCardBean.lkey.toString().trim());
				msgBean = controller.account(creditCardBean, codeStr + "");
				Log.e("code", msgBean.toString());
				BasicActivity.BlueToothPsd = "";
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
					RealTimeActivity.this.finish();
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
						intent.putExtra("uid", key);
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
	private void delCreditCard(final String deleteId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(RealTimeActivity.this);
		builder.setTitle("提示");
		builder.setMessage("确定删除吗");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				new Thread() {
					@Override
					public void run() {

						super.run();
						final MsgBean msg = controller.delcard(uid, deleteId);
						handler.post(new Runnable() {

							@Override
							public void run() {
								if (msg != null && msg.RspMsg != null) {
									if (msg.RspMsg.equals("0")) {
										Toast.makeText(RealTimeActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
										items.clear();
										isFirstAdd = true;
										getPaymentsData();
									} else {
										Toast.makeText(RealTimeActivity.this, "删除失败-->" + msg.RspMsg, Toast.LENGTH_SHORT).show();
									}
								}

							}
						});

					}
				}.start();

			}
		});
		builder.setNegativeButton("取消", null);
		builder.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}
}
