package com.apicloud.index;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.apicloud.activity.AddNewCardActivity;
import com.apicloud.adapter.MyCardAdapter;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

public class BindCardActivity extends Activity implements OnClickListener{
	private ImageButton ib_return;
	private RelativeLayout rl_addCard;
	private ListView showCardList;
	private MyCardAdapter myCardAdapter;
	public static final int REQUEST_ADD_CODE = 100;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("bindcard_list"));
		initView();
	}
	public void initView(){
		ib_return = (ImageButton) findViewById(UZResourcesIDFinder.getResIdID("bcib_return"));
		rl_addCard = (RelativeLayout) findViewById(UZResourcesIDFinder.getResIdID("rl_bcbutton"));
		showCardList = (ListView) findViewById(UZResourcesIDFinder.getResIdID("lv_showCardList"));
		ib_return.setOnClickListener(this);
		rl_addCard.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		if(v==ib_return){
			finish();
		}
		if(v==rl_addCard){
			Intent intent = new Intent(BindCardActivity.this,AddNewCardActivity.class);
			startActivityForResult(intent, REQUEST_ADD_CODE);
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {


		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==REQUEST_ADD_CODE&&resultCode==REQUEST_ADD_CODE){
			//建设中...刷新卡列表
		}
	}
}
