package com.apicloud.activity.timely;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import com.apicloud.util.Configure;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;
/**
 * 
 * ClassName: CameraDialog <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON(可选). <br/>
 * date: 2015-4-25 下午5:03:20 <br/>
 * blog:http://blog.csdn.net/qq718799510?viewmode=contents
 * @author zhuxiaohao
 * @version  拍照对话框
 * @since JDK 1.6
 */
public class CameraDialog {

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
	public Button text_pictures;// 拍照
	public Button text_album;// 相册选取
	/** 取消 */
	public TextView txt_share__cancel;

	public CameraDialog(Context context) {
		this.context = context;
		initView();
	}

	public void initView() {
		shareDialog = new Dialog(context,UZResourcesIDFinder.getResStyleID("custom_dialog"));
		shareDialog.setContentView(UZResourcesIDFinder.getResLayoutID("layout_ui_myresume_camera_dialog"));
		Window window = shareDialog.getWindow();
		window.setWindowAnimations(UZResourcesIDFinder.getResStyleID("dialog_in_and_out"));
		window.setGravity(Gravity.BOTTOM);
		text_pictures = (Button) shareDialog.findViewById(UZResourcesIDFinder.getResIdID("text_pictures"));
		text_album = (Button) shareDialog.findViewById(UZResourcesIDFinder.getResIdID("text_album"));
		txt_share__cancel = (TextView) shareDialog.findViewById(UZResourcesIDFinder.getResIdID("txt_share__cancel"));
		txt_share__cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}

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

	public void hide() {
		if (status && !ishide) {
			shareDialog.hide();
			ishide = true;
		}
	}

	public void dismiss() {
		if (status) {
			shareDialog.dismiss();
			status = false;
		}
	}

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
		txt_share__cancel.setOnClickListener(shareListener);
		text_pictures.setOnClickListener(shareListener);
		text_album.setOnClickListener(shareListener);
	}

}
