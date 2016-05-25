package com.apicloud.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.uzmap.pkg.uzcore.UZResourcesIDFinder;


public class AboutUsActivity extends Activity implements OnClickListener{
	private ImageView iv_aboutPhone;
	private Button btn_return;
	private TextView phoneNo;
	private String getPhone;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("layout_aboutus"));
		initView();
	}
	public void initView(){
		iv_aboutPhone = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("iv_aboutphone"));
		btn_return = (Button) findViewById(UZResourcesIDFinder.getResIdID("btn_return"));
		phoneNo = (TextView) findViewById(UZResourcesIDFinder.getResIdID("tv_phoneno"));
		iv_aboutPhone.setOnClickListener(this);
		btn_return.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		if(v==btn_return){
			finish();
		}
		if(v==iv_aboutPhone){
			getPhone = phoneNo.getText().toString();
			AlertDialog.Builder builder = new Builder(AboutUsActivity.this);
			builder.setTitle(getPhone);
			builder.setPositiveButton("呼叫", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+getPhone));
					startActivity(intent);
				}
			});
			builder.setNegativeButton("取消", null);
			builder.show();
		}
	}
}
