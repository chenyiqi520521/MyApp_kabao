/**   
 * Copyright © 2015 公司名. All rights reserved.
 * 
 * @Title: SuccessDialogActivity.java 
 * @Prject: APICloudSDK
 * @Package: com.apicloud.activity.write 
 * @Description: TODO
 * @author: user20   
 * @date: 2015年8月28日 上午11:56:38 
 * @version: V1.0   
 */
package com.apicloud.activity.write;

import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/** 
 * @ClassName: SuccessDialogActivity 
 * @Description: TODO
 * @author: user20
 * @date: 2015年8月28日 上午11:56:38  
 */
public class SuccessDialogActivity extends Activity{
	
	/* (non Javadoc) 
	 * @Title: onCreate
	 * @Description: TODO
	 * @param savedInstanceState 
	 * @see android.app.Activity#onCreate(android.os.Bundle) 
	 */
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("dialog_success_tip"));
		TextView tv_back=(TextView)findViewById(UZResourcesIDFinder.getResIdID("btn_index"));
		tv_back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
			  SuccessDialogActivity.this.finish();
				
			}
		});
	}
	
	

}
