package com.apicloud.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.apicloud.controller.Controller;
import com.apicloud.controller.MyController;
import com.apicloud.module.BankBean;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

public class BankNameActivity extends Activity implements OnClickListener, OnScrollListener {
	private ImageButton ib_return;
	private int pageNum = 1;//请求第一页
	private TextView tv_title;
	private ListView bank_listView;
	private MyBankAdapter bankAdapter;
	private ArrayList<BankBean> bankData;
	private MyController controller;
	private String choseWhat = "";// 来自主行or支行
	private MyBankAdapter myAdapter;
	private ProgressDialog pd;
	public static Handler handler;
	private String branch;
	private String parentCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("lay_banklist"));
		controller = new MyController(getApplicationContext());
		// handler = new Handler();
		pd = new ProgressDialog(BankNameActivity.this);
		pd.setMessage("初始化列表");
		initView();
		initParam();
		initHandler();
		initAdapter();
	}

	public void initView() {
		ib_return = (ImageButton) findViewById(UZResourcesIDFinder.getResIdID("ib_return"));
		tv_title = (TextView) findViewById(UZResourcesIDFinder.getResIdID("tv_title"));
		bank_listView = (ListView) findViewById(UZResourcesIDFinder.getResIdID("lv_bank"));
		ib_return.setOnClickListener(this);

	}
	public void initParam(){
		choseWhat = getIntent().getStringExtra(AddNewCardActivity.CHOOSE_SEARCH);
		if(choseWhat.equals(AddNewCardActivity.CHOOSE_CHILDREN)){
			branch = getIntent().getStringExtra(AddNewCardActivity.BRANCH);
			parentCode=getIntent().getStringExtra(AddNewCardActivity.PARENT_CODE);
			tv_title.setText("银行网点选择");
		}
	}

	@Override
	public void onClick(View v) {
		finish();
	}

	public void initHandler() {
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				super.handleMessage(msg);
			}
		};
	}

	public void initAdapter() {
		if (pd != null) {
			pd.show();
		}
		new Thread() {
			public void run() {

				if (choseWhat.equals(AddNewCardActivity.CHOOSE_PARENT)) {
					bankData = controller.getParetBankList(BankNameActivity.this);

				} else {
					// 选择的是支行
					bankData = controller.getChilrenBankList(BankNameActivity.this, parentCode, branch, pageNum+"");
					pageNum++;
				}
				if (bankData != null) {
					handler.post(new Runnable() {

						@Override
						public void run() {
							myAdapter = new MyBankAdapter(BankNameActivity.this, bankData, choseWhat);
							if (myAdapter != null) {
								bank_listView.setAdapter(myAdapter);
							}
							if (pd != null) {
								pd.dismiss();
							}
						}
					});
				} else {
					handler.post(new Runnable() {

						@Override
						public void run() {
							Toast.makeText(BankNameActivity.this, "请更换搜索关键字", Toast.LENGTH_SHORT).show();
							if (pd != null) {
								pd.dismiss();
							}
						}
					});
				}
			}

		}.start();

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub

	}
}
