/**
 * Project Name:CardPay
 * File Name:NotCardHead.java
 * Package Name:com.apicloud.activity.topup
 * Date:2015-4-24下午1:36:23
 * Copyright (c) 2015, zhuxiaohao All Rights Reserved.
 *
 */

package com.apicloud.activity.topup;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.apicloud.activity.WebViewActivity;
import com.apicloud.util.Configure;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

/**
 * ClassName:NotCardHead <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2015-4-24 下午1:36:23 <br/>
 * 
 * @author zhuxiaohao blog:http://blog.csdn.net/qq718799510?viewmode=contents
 * @version 1.0.0
 * @since JDK 1.6
 * @see
 */
public class NotCardHeadDialog implements OnClickListener {
	Context context;// 上下文
	Dialog dialog;// 对话框
	boolean status = false;// 是否已经显示
	boolean ishide = false;// 时候已经显示
	public TextView txt_title;// 标题栏
	public ImageView iv_head;// 图片
	public ProgressBar tip_pb;
	public TextView tv_searchbl;// 搜索蓝牙
	public Button btn_help;
	ImageButton iv_back;
	public TextView tv_tip, tv_1, tv_2, tv_3, tv_4;

	private Handler handler;
	private String choseDevice = "";

	public NotCardHeadDialog(Context context, String choseDevice) {
		this.context = context;
		initView(choseDevice);
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
		this.choseDevice = choseDevice;

	}

	private void initView(String choseDevice) {
		dialog = new Dialog(context, UZResourcesIDFinder.getResStyleID("processDialog"));
		// dialog=new Dialog(context);
		dialog.setContentView(UZResourcesIDFinder.getResLayoutID("top_up_not_head"));
		/*
		 * Window window = dialog.getWindow();
		 * window.setWindowAnimations(UZResourcesIDFinder.getResStyleID(
		 * "dialog_in_and_out")); window.setGravity(Gravity.CENTER);
		 */
		txt_title = (TextView) dialog.findViewById(UZResourcesIDFinder.getResIdID("txt_title"));
		tv_searchbl = (TextView) dialog.findViewById(UZResourcesIDFinder.getResIdID("tv_search_bl"));
		tv_searchbl.setOnClickListener(this);
		btn_help = (Button) dialog.findViewById(UZResourcesIDFinder.getResIdID("btn_help"));
		btn_help.setOnClickListener(this);
		iv_back=(ImageButton) dialog.findViewById(UZResourcesIDFinder.getResIdID("btn_back2"));
		iv_back.setOnClickListener(this);
		if (choseDevice.equals("A")) {
			tv_searchbl.setVisibility(View.GONE);
		} else {
			txt_title.setText("尝试连接蓝牙设备");
		}

		tv_tip = (TextView) dialog.findViewById(UZResourcesIDFinder.getResIdID("txt_tip"));
		/*
		 * tv_1 = (TextView)
		 * dialog.findViewById(UZResourcesIDFinder.getResIdID("tv_1")); tv_2 =
		 * (TextView)
		 * dialog.findViewById(UZResourcesIDFinder.getResIdID("tv_2")); tv_3=
		 * (TextView)
		 * dialog.findViewById(UZResourcesIDFinder.getResIdID("tv_3")); tv_4=
		 * (TextView)
		 * dialog.findViewById(UZResourcesIDFinder.getResIdID("tv_4"));
		 */
		iv_head = (ImageView) dialog.findViewById(UZResourcesIDFinder.getResIdID("iv_head"));
		tip_pb = (ProgressBar) dialog.findViewById(UZResourcesIDFinder.getResIdID("pb"));
		// WindowManager.LayoutParams lp=dialog.getWindow().getAttributes();
		// lp.alpha=0.8f;
		// dialog.getWindow().setAttributes(lp);
	}

	public void swipShow() {
		try {
			tv_tip.setVisibility(View.VISIBLE);
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public static final int RESTART_SEARCH_BLUETOOTH = 1;// 重新搜索蓝牙

	public void reStartSearchBlueTooth() {
		if (handler != null) {
			this.dismiss();
			Message msg = handler.obtainMessage();
			msg.what = RESTART_SEARCH_BLUETOOTH;
			handler.sendMessage(msg);
		}

	}

	public void swipHiden() {
		try {
			tv_tip.setVisibility(View.GONE);
			// tv_searchbl.setVisibility(View.GONE);
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public void HiddenBlueToothSearch() {

		try {
			tv_searchbl.setVisibility(View.GONE);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/*
	 * public void blueToothTipShow(){ try { tv_1.setVisibility(View.VISIBLE);
	 * tv_2.setVisibility(View.VISIBLE); tv_3.setVisibility(View.VISIBLE);
	 * tv_4.setVisibility(View.VISIBLE); } catch (Exception e) { // TODO: handle
	 * exception }
	 * 
	 * } public void blueToothTipHidden(){ try { tv_1.setVisibility(View.GONE);
	 * tv_2.setVisibility(View.GONE); tv_3.setVisibility(View.GONE);
	 * tv_4.setVisibility(View.GONE); } catch (Exception e) { // TODO: handle
	 * exception }
	 * 
	 * }
	 */
	/**
	 * 
	 * TODO 显示对话框.
	 * 
	 * @see android.app.Dialog#show()
	 */
	public void show() {
		if (dialog == null) {
			return;
		}
		status = dialog.isShowing();
		if (!status || ishide) {
			dialog.show();
			status = true;
			ishide = false;
			WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
			lp.width = Configure.screenWidth; // 设置宽度
			lp.alpha = 0.8f;
			dialog.getWindow().setAttributes(lp);
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
			dialog.hide();
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
			dialog.dismiss();
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
			dialog.cancel();
			status = false;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == tv_searchbl) {
			reStartSearchBlueTooth();
		}
		if (v == btn_help) {
			context.startActivity(new Intent(context, WebViewActivity.class));
		}
		if(v==iv_back){
			try {
				dialog.dismiss();
			} catch (Exception e) {
				// TODO: handle exception
			}
			
		}

	}

}
