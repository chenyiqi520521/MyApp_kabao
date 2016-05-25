/**
 * Project Name:CardPay
 * File Name:CardPaymentsRules.java
 * Package Name:com.apicloud.activity
 * Date:2015-4-23下午3:17:57
 * Copyright (c) 2015, zhuxiaohao All Rights Reserved.
 *
 */

package com.apicloud.activity.card;

import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

/**
 * ClassName:CardPaymentsRules <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2015-4-23 下午3:17:57 <br/>
 * 
 * @author zhuxiaohao blog:http://blog.csdn.net/qq718799510?viewmode=contents
 * @version 1.0.0
 * @since JDK 1.6
 * @see 信用卡还款规则
 */
public class CardPaymentsRules extends Activity {
	ImageButton ib_return;//返回

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("card_payments_rules"));
		ib_return = (ImageButton) findViewById(UZResourcesIDFinder.getResIdID("ib_return"));
		ib_return.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

}
