package com.apicloud.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apicloud.activity.AddNewCardActivity;
import com.apicloud.activity.BaseActivity;
import com.apicloud.controller.MyController;
import com.apicloud.module.CardBean;
import com.apicloud.module.CardBean.DataBean;
import com.apicloud.module.LoginBean;
import com.apicloud.module.MainBean;
import com.apicloud.module.MsgBean;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

public class BindCardActivity extends Activity implements OnClickListener {
	private ImageButton ib_return;
	private RelativeLayout rl_addCard;
	private ListView showCardList;
	private MyCardAdapter myCardAdapter;
	public static final int REQUEST_ADD_CODE = 100;
	private MyController controller;
	private CardBean cardBean;
	private String key = "";
	private Handler handler = new Handler();
	Handler mainHandler;// 处理设定主卡的handler
	public List<Map<String, String>> items = new ArrayList<Map<String, String>>();
	private ProgressDialog pd;
	private MainBean mainBean;
	int setCard = 1;
	public static TextView tv_setMainCard;
	String cardId;
	MsgBean msgBean;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		controller = new MyController();
		pd = new ProgressDialog(BindCardActivity.this);
		pd.setMessage("加载主卡列表，请稍后....");
		pd.show();

		setContentView(UZResourcesIDFinder.getResLayoutID("bindcard_list"));
		initView();
		initMainHandler();

	}

	private void initMainHandler() {
		mainHandler = new Handler() {
			@Override
			public void handleMessage(final Message msg) {
				
				super.handleMessage(msg);

				Log.e("viewPosition", "值是:" + msg.what);
				if (msg.arg1 == 5) {
					if(msg.what==0){
						cardId=(String) msg.obj;
						new Thread(){
							@Override
							public void run() {
								
								super.run();
								msgBean = controller.delcard(key, cardId);
								if(msgBean.RspCd.equals("0")){
									Tip("删除成功");
								}else{
									Tip("删除失败--->"+msgBean.RspMsg);
									return;
								}
							}
						}.start();
					}else{
						cardId=(String) msg.obj;
						new Thread(){
							@Override
							public void run() {
								
								super.run();
								msgBean = controller.delcard(key, cardId);
								if(msgBean.RspCd.equals("0")){
									Tip("删除成功");
								}else{
									Tip("删除失败--->"+msgBean.RspMsg);
									return;
								}
							}
						}.start();
					}
				} else {
					if (msg.what == 0) {
						cardId = (String) msg.obj;
						new Thread() {

							@Override
							public void run() {

								super.run();

								mainBean = controller.doRequestMain(key, cardId);
								if (mainBean.error.equals("0")) {

									Tip("设置主卡成功");

								} else {
									Tip("设置主卡失败" + mainBean.msg);
									return;
								}
							}
						}.start();

					} else {
						cardId = (String) msg.obj;
						new Thread() {

							@Override
							public void run() {

								super.run();
								Log.e("initMainHandler", "进了");
								mainBean = controller.doRequestMain(key, cardId);
								if (mainBean.error.equals("0")) {

									Tip("设置主卡成功");

								} else {
									Tip("设置主卡失败" + mainBean.msg);
									return;
								}
							}
						}.start();
					}
				}

			}
		};
	}

	private void Tip(final String msg) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(BindCardActivity.this, msg, Toast.LENGTH_SHORT).show();

			}
		});

	}



	private void initView() {
		ib_return = (ImageButton) findViewById(UZResourcesIDFinder.getResIdID("bcib_return"));
		rl_addCard = (RelativeLayout) findViewById(UZResourcesIDFinder.getResIdID("rl_bcbutton"));
		showCardList = (ListView) findViewById(UZResourcesIDFinder.getResIdID("lv_showCardList"));
		key = getIntent().getStringExtra("key");
		getDataList();
		pd.dismiss();
		ib_return.setOnClickListener(this);
		rl_addCard.setOnClickListener(this);
	}

	private void getDataList() {
		new Thread() {
			@Override
			public void run() {

				super.run();
				
				cardBean = controller.getCardInfo(BindCardActivity.this, key, "card");
				handler.post(new Runnable() {

					@Override
					public void run() {
						if (cardBean == null) {
							showCardList.setVisibility(View.GONE);
						} else {
							showCardList.setVisibility(View.VISIBLE);
							List<DataBean> list = cardBean.getData();
							for (int i = 0; i < list.size(); i++) {
								Map<String, String> map = new HashMap<String, String>();
								map.put("card_img", list.get(i).getCardimg());
								map.put("card_num", list.get(i).getCardno());
								map.put("card_name", list.get(i).getCardname());
								map.put("card_id", list.get(i).getId());
								map.put("main_card", list.get(i).getMain());

								items.add(map);
							}
							myCardAdapter = new MyCardAdapter(BindCardActivity.this, items, mainHandler);
							myCardAdapter.notifyDataSetChanged();
							showCardList.setAdapter(myCardAdapter);
						}

					}
				});
			}
		}.start();
	}

	@Override
	public void onClick(View v) {
		if (v == ib_return) {
			finish();
		}
		if (v == rl_addCard) {
			Intent intent = new Intent(BindCardActivity.this, AddNewCardActivity.class);
			intent.putExtra("key", key);
			startActivity(intent);
		}

	}

}
