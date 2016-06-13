package com.apicloud.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

public class IdConfirmActivity extends Activity implements OnClickListener{
	private ImageView iv_return;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("id_confirm"));
		iv_return  = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("iv_return"));
		iv_return.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v==iv_return){
			finish();
		}
		
	}
}
