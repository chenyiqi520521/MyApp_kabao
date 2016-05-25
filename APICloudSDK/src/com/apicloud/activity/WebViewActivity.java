package com.apicloud.activity;

import com.apicloud.util.Constant;
import com.apicloud.view.ProgressWebView;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

public class WebViewActivity extends Activity implements OnClickListener {

	private final String TAG = getClass().getSimpleName();
	private ProgressWebView webView;
	ImageButton ib_return;// 返回按钮

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("activity_web"));

		initView();

	}

	private void initView() {
		ib_return = (ImageButton) findViewById(UZResourcesIDFinder.getResIdID("ib_return"));
		ib_return.setOnClickListener(this);
		String url = Constant.help;
		if (TextUtils.isEmpty(url)) {
			Log.i(TAG, "未获取到网页链接！");
		} else {
			webView = (ProgressWebView) findViewById(UZResourcesIDFinder.getResIdID("webview"));
			WebSettings webSettings = webView.getSettings();
			webSettings.setAllowFileAccess(true);
			webView.setWebViewClient(new MWebViewClient());
			loadUrl(url);
		}

	}

	@Override
	public void onClick(View v) {
		if (v == ib_return) {
			finish();
		}
	}

	public void loadUrl(String url) {
		if (webView != null) {
			webView.loadUrl(url);
			WebSettings webSettings = webView.getSettings();
			webSettings.setJavaScriptEnabled(true);
			webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
		}
	}

	private class MWebViewClient extends WebViewClient {
		// 重写webclient，从而截获跳转的地址
		public boolean shouldOverrideUrlLoading(WebView view, String url) {

			return false;
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
		}

		/**
		 * onPageFinished指页面加载完成,完成后取消计时器
		 */
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);

		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);

		}
	}

}
