/**
 * Project Name:CardPay
 * File Name:TopUpDialog.java
 * Package Name:com.apicloud.activity
 * Date:2015-4-23上午10:46:43
 * Copyright (c) 2015, zhuxiaohao All Rights Reserved.
 *
 */

package com.apicloud.activity.topup;

import android.app.Dialog;
import android.app.Instrumentation;
import android.content.Context;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.apicloud.util.Configure;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

/**
 * ClassName:TopUpDialog <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2015-4-23 上午10:46:43 <br/>
 * 
 * @author zhuxiaohao blog:http://blog.csdn.net/qq718799510?viewmode=contents
 * @version 1.0.0
 * @since JDK 1.6
 * @see
 */
public class TopUpDialog implements android.view.View.OnClickListener {
	/**
	 * 上下文
	 */
	private Context context;
	/**
	 * 自定义对话框
	 */
	private Dialog shareDialog;
	/**
	 * 显示状态，true：已显示，false：没有显示
	 */
	private boolean status = false;
	/**
	 * 是否隐藏了。true：已隐藏，false：显示
	 */
	private boolean ishide = false;

	/**
	 * 实付金额
	 */
	public static TextView txt_crad_moery;
	/**
	 * 到账金额
	 */
	public static TextView txt_crad_t;

	/**
	 * 支付卡号
	 */
	public static TextView txt_cradNumber;
	/**
	 * 密码框
	 */
	public static EditText ed_crad_pass;
	/**
	 * 取消
	 */
	private TextView txt_clean;
	/**
	 * 确定
	 */
	public  TextView txt_ok;

	/**
	 * 
	 * Creates a new instance of TopUpDialog.
	 * 
	 * @param context
	 */
	public TopUpDialog(Context context) {
		this.context = context;
		initView();
	}

	/**
	 * 
	 * initView:(初始化). <br/>
	 * 
	 * @author zhuxiaohao
	 * @since JDK 1.6
	 */
	public void initView() {
		shareDialog = new Dialog(context, UZResourcesIDFinder.getResStyleID("custom_dialog"));
		shareDialog.setContentView(UZResourcesIDFinder.getResLayoutID("dialog"));
		Window window = shareDialog.getWindow();
		window.setWindowAnimations(UZResourcesIDFinder.getResStyleID("dialog_in_and_out"));
		window.setGravity(Gravity.CENTER);
		txt_crad_moery = (TextView) shareDialog.findViewById(UZResourcesIDFinder.getResIdID("txt_crad_moery"));
		txt_crad_t = (TextView) shareDialog.findViewById(UZResourcesIDFinder.getResIdID("txt_crad_t"));
		txt_cradNumber = (TextView) shareDialog.findViewById(UZResourcesIDFinder.getResIdID("txt_cradNumber"));
		ed_crad_pass = (EditText) shareDialog.findViewById(UZResourcesIDFinder.getResIdID("ed_crad_pass"));
		txt_clean = (TextView) shareDialog.findViewById(UZResourcesIDFinder.getResIdID("txt_clean"));
		txt_ok = (TextView) shareDialog.findViewById(UZResourcesIDFinder.getResIdID("txt_ok_dialog"));
		txt_clean.setOnClickListener(this);

	}

	/**
	 * 
	 * TODO 显示对话框.
	 * 
	 * @see android.app.Dialog#show()
	 */
	public void show() {
		if (shareDialog == null) {
			return;
		}
		status = shareDialog.isShowing();
		if (!status || ishide) {
			shareDialog.show();
			status = true;
			ishide = false;
			WindowManager.LayoutParams lp = shareDialog.getWindow().getAttributes();
			lp.width = Configure.screenWidth; // 设置宽度
			shareDialog.getWindow().setAttributes(lp);
		}
	}

	/**
	 * 
	 * TODO 简单描述该方法的实现功能（隐藏对话框）.
	 * 
	 * @see android.app.Dialog#hide()
	 */
	public void hide() {
		if (status && !ishide) {
			shareDialog.hide();
			ishide = true;
		}
	}

	/**
	 * 
	 * TODO 简单描述该方法的实现功能（销毁对话框）.
	 * 
	 * @see android.app.Dialog#dismiss()
	 */
	public void dismiss() {
		if (status) {
			shareDialog.dismiss();
			status = false;
		}
	}

	/**
	 * 
	 * TODO 简单描述该方法的实现功能（取消）.
	 * 
	 * @see android.app.Dialog#cancel()
	 */
	public void cancel() {
		if (status) {
			shareDialog.cancel();
			status = false;
		}
	}

	/**
	 * <pre>
	 * 设置监听
	 * </pre>
	 * 
	 * @param shareListener
	 */
	public void setOnclickListener(View.OnClickListener shareListener) {
		txt_ok.setOnClickListener(shareListener);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == txt_clean) {
			shareDialog.dismiss();
			new Thread(){
				public void run() {
					Instrumentation inst = new Instrumentation();
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
				};
			}.start();
		}

	}

}
