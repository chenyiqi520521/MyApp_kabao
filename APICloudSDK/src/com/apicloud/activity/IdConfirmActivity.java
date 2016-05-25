package com.apicloud.activity;

import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;

public class IdConfirmActivity extends Activity implements OnClickListener{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("id_confirm"));
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
