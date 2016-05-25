/**
 * Project Name:CardPay
 * File Name:TimelyAccountOverActivity.java
 * Package Name:com.apicloud.activity
 * Date:2015-4-23下午8:03:28
 * Copyright (c) 2015, zhuxiaohao All Rights Reserved.
 *
 */

package com.apicloud.activity.timely;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

/**
 * ClassName:TimelyAccountOverActivity <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2015-4-23 下午8:03:28 <br/>
 * 
 * @author zhuxiaohao blog:http://blog.csdn.net/qq718799510?viewmode=contents
 * @version 1.0.0
 * @since JDK 1.6
 * @see 即时到帐结束界面
 */
public class TimelyAccountOverActivity extends Activity implements OnClickListener {
	ImageButton ib_return;// 返回
	Button btn_ok;// 确定

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("timely_account_over"));
		ib_return = (ImageButton) findViewById(UZResourcesIDFinder.getResIdID("ib_return"));
		btn_ok = (Button) findViewById(UZResourcesIDFinder.getResIdID("btn_ok"));
		ib_return.setOnClickListener(this);
		btn_ok.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		// 确认
		if (v == btn_ok) {
			finish();
		}
		// 返回
		if (v == ib_return) {
			finish();
		}

	}

}
