package com.apicloud.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

public class RealTimeActivity extends BasicActivity implements OnClickListener{
	private LinearLayout layout_add_card;
	private ImageButton ib_return;
	public static final int REQUEST_ADD_CREDIT = 103;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("realtime_account"));
		initView();
	}
	
	public void initView(){
		layout_add_card = (LinearLayout) findViewById(UZResourcesIDFinder.getResIdID("layout_add_card"));
		ib_return = (ImageButton) findViewById(UZResourcesIDFinder.getResIdID("ib_return"));
		layout_add_card.setOnClickListener(this);
		ib_return.setOnClickListener(this);
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
