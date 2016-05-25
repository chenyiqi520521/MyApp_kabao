package com.apicloud.moduleDemo;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.apicloud.activity.AddCradActivity;
import com.apicloud.activity.BindDeviceActivity;
import com.apicloud.activity.KuaiLainActivity;
import com.apicloud.activity.QueryMoneyActivity;
import com.apicloud.activity.TradnsferAccountsActivity;
import com.apicloud.activity.WelcomeIndexActivity;
import com.apicloud.activity.card.AddCardPaymentsActivity;
import com.apicloud.activity.card.CradPayActivity;
import com.apicloud.activity.telphone.TelPhoneActivity;
import com.apicloud.activity.timely.AddTimelyAccountActivity;
import com.apicloud.activity.timely.TimelyAccountTopUpActivity;
import com.apicloud.activity.topup.TopUpActivity;
import com.apicloud.activity.topup.TopUpTwoActivity;
import com.apicloud.activity.write.HandwritingActivity;
import com.apicloud.activity.write.WritePadActivity;
import com.apicloud.module.BluetoothDeviceContext;
import com.apicloud.util.Configure;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.annotation.UzJavascriptMethod;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

/**
 * 该类映射至Javascript中moduleDemo对象<br>
 * <br>
 * <strong>Js Example:</strong><br>
 * var module = api.require('moduleDemo');<br>
 * module.xxx();
 * 
 * @author APICloud
 * 
 */
public class APIModuleDemo extends UZModule {

	/** 回调参数 */
	private UZModuleContext mJsCallback;
	/** 回调 code */
	public static final int ACTIVITY_REQUEST_CODE_A = 100;

	public APIModuleDemo(UZWebView webView) {
		super(webView);
		Configure.init(getContext());
	}
	@UzJavascriptMethod
	public void jsmethod_bindDevice(UZModuleContext moduleContext) {
		
		mJsCallback = moduleContext;
		Intent intent = new Intent();
		intent.putExtra("lkey", moduleContext.optString("lkey"));
		intent.putExtra("shopno", moduleContext.optString("shopno"));
		intent.putExtra("posno", moduleContext.optString("pos"));
		intent.setClass(getContext(), BindDeviceActivity.class);
		intent.putExtra("needResult", true);
		startActivityForResult(intent, ACTIVITY_REQUEST_CODE_A);
	}
	/**
	 * 
	 * jsmethod_signature:(调用画板). <br/>
	 * 
	 * 
	 * @author zhuxiaohao
	 * @param moduleContext
	 * @since JDK 1.6
	 */
	@UzJavascriptMethod
	public void jsmethod_signature(UZModuleContext moduleContext) {
		
		startActivity(new Intent(getContext(), WritePadActivity.class));
	}
 
	@UzJavascriptMethod
	public void jsmethod_querymoney(UZModuleContext moduleContext) {
		
		mJsCallback = moduleContext;
		Intent intent = new Intent();
		intent.putExtra("uid", moduleContext.optString("uid"));
		intent.putExtra("posno", moduleContext.optString("pos"));
		intent.setClass(getContext(), QueryMoneyActivity.class);
		startActivity(intent);
	}
	/**
	 * 
	 * jsmethod_addCrad:(添加银行卡信息). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param moduleContext
	 * @since JDK 1.6
	 */
	@UzJavascriptMethod
	public void jsmethod_addCard(UZModuleContext moduleContext) {
		mJsCallback = moduleContext;
		Intent intent = new Intent();
		intent.putExtra("uid", moduleContext.optString("uid"));
		intent.putExtra("posno", moduleContext.optString("pos"));
		intent.putExtra("person", moduleContext.optString("person"));
		intent.putExtra("idnum", moduleContext.optString("idnum"));
		intent.putExtra("needResult", true);
		intent.setClass(getContext(), AddCradActivity.class);
		startActivityForResult(intent,ACTIVITY_REQUEST_CODE_A);
	}

	/**
	 * 
	 * jsmethod_topUp:(我的钱包里的充值). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param moduleContext
	 * @since JDK 1.6
	 */
	@UzJavascriptMethod
	public void jsmethod_topUpPay(UZModuleContext moduleContext) {
		mJsCallback = moduleContext;
		Intent intent = new Intent(getContext(), TopUpActivity.class);
		intent.putExtra("uid", moduleContext.optString("uid"));
		intent.putExtra("posno", moduleContext.optString("pos"));
		intent.putExtra("GPS", moduleContext.optString("GPS"));
		intent.putExtra("A", moduleContext.optString("A"));
		intent.putExtra("B", moduleContext.optString("B"));
		intent.putExtra("C", moduleContext.optString("C"));
		intent.putExtra("D", moduleContext.optString("D"));
		intent.putExtra("word", moduleContext.optString("word"));
		intent.putExtra("needResult", true);
		startActivityForResult(intent, ACTIVITY_REQUEST_CODE_A);
	}
	
	@UzJavascriptMethod
	public void jsmethod_kuailian(UZModuleContext moduleContext) {
		mJsCallback = moduleContext;
		Intent intent = new Intent(getContext(), KuaiLainActivity.class);
		intent.putExtra("uid", moduleContext.optString("uid"));
		intent.putExtra("posno", moduleContext.optString("pos"));
		intent.putExtra("GPS", moduleContext.optString("GPS"));
		intent.putExtra("A", moduleContext.optString("A"));
		intent.putExtra("B", moduleContext.optString("B"));
		intent.putExtra("C", moduleContext.optString("C"));
		intent.putExtra("D", moduleContext.optString("D"));
		intent.putExtra("word", moduleContext.optString("word"));
		intent.putExtra("needResult", true);
		startActivityForResult(intent, ACTIVITY_REQUEST_CODE_A);
	}

	/**
	 * 
	 * jsmethod_CardPayments:(信用卡还款支付界面). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param moduleContext
	 * @since JDK 1.6
	 */
	@UzJavascriptMethod
	public void jsmethod_creditCardPayment(UZModuleContext moduleContext) {
		mJsCallback = moduleContext;
		Intent intent = new Intent();
		intent.putExtra("uid", moduleContext.optString("uid"));
		intent.putExtra("posno", moduleContext.optString("pos"));
		intent.putExtra("GPS", moduleContext.optString("GPS"));
		
		intent.putExtra("cardNo", moduleContext.optString("cardNo"));
		intent.putExtra("money", moduleContext.optString("money"));
		intent.putExtra("needResult", true);
		intent.setClass(getContext(), CradPayActivity.class);
		startActivityForResult(intent, ACTIVITY_REQUEST_CODE_A);
	}

	/**
	 * 
	 * jsmethod_CardPayments:(添加-->认证信用卡). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param moduleContext
	 * @since JDK 1.6
	 */
	@UzJavascriptMethod
	public void jsmethod_addCardPayments(UZModuleContext moduleContext) {
		mJsCallback = moduleContext;
		Intent intent = new Intent();
		intent.putExtra("uid", moduleContext.optString("uid"));
		intent.putExtra("posno", moduleContext.optString("pos"));
		
		intent.putExtra("needResult", true);
		intent.setClass(getContext(), AddCardPaymentsActivity.class);
		startActivityForResult(intent, ACTIVITY_REQUEST_CODE_A);
	}

	/**
	 * 
	 * jsmethod_TelPhone:(调用手机充值). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param moduleContext
	 * @since JDK 1.6
	 */
	public void jsmethod_telPhonePayment(UZModuleContext moduleContext) {
		mJsCallback = moduleContext;
		Intent intent = new Intent();
		intent.putExtra("uid", moduleContext.optString("uid"));
		intent.putExtra("posno", moduleContext.optString("pos"));
		intent.putExtra("GPS", moduleContext.optString("GPS"));
		intent.putExtra("phone",moduleContext.optString("phone"));
		intent.putExtra("money", moduleContext.optString("money"));
		intent.putExtra("operator", moduleContext.optString("operator"));
		intent.putExtra("needResult", true);
		intent.setClass(getContext(), TelPhoneActivity.class);
		startActivityForResult(intent, ACTIVITY_REQUEST_CODE_A);
	}

	/**
	 * 
	 * jsmethod_TradnsferAccounts:(调用转账汇款). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param moduleContext
	 * @since JDK 1.6
	 */
	public void jsmethod_tradnsferAccountsPayment(UZModuleContext moduleContext) {
		mJsCallback = moduleContext;
		Intent intent = new Intent();
		intent.putExtra("uid", moduleContext.optString("uid"));
		intent.putExtra("posno", moduleContext.optString("pos"));
		intent.putExtra("GPS", moduleContext.optString("GPS"));
		intent.putExtra("idCard", moduleContext.optString("idCard"));
		intent.putExtra("cardNo", moduleContext.optString("cardNo"));
		intent.putExtra("money", moduleContext.optString("money"));
		intent.putExtra("name", moduleContext.optString("name"));
		intent.putExtra("needResult", true);
		intent.setClass(getContext(), TradnsferAccountsActivity.class);
		startActivityForResult(intent, ACTIVITY_REQUEST_CODE_A);
	}

	/**
	 * 
	 * jsmethod_timelyAccount:(添加->即时到帐认证信用卡). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param moduleContext
	 * @since JDK 1.6
	 */
	public void jsmethod_addTimelyAccount(UZModuleContext moduleContext) {
		mJsCallback = moduleContext;
		Intent intent = new Intent();
		intent.putExtra("uid", moduleContext.optString("uid"));
		intent.putExtra("posno", moduleContext.optString("pos"));
	    intent.putExtra("cardid", moduleContext.optString("cardid"));
		intent.putExtra("cardno", moduleContext.optString("cardno"));
		intent.putExtra("uname", moduleContext.optString("uname"));
		intent.putExtra("needResult", true);
		intent.setClass(getContext(), AddTimelyAccountActivity.class);
		startActivityForResult(intent, ACTIVITY_REQUEST_CODE_A);
	}

	/**
	 * 
	 * jsmethod_timelyAccountTopUp:(即时到帐充值). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param moduleContext
	 * @since JDK 1.6
	 */
	public void jsmethod_timelyAccountTopUp(UZModuleContext moduleContext) {
		mJsCallback = moduleContext;
		Intent intent = new Intent();
		intent.putExtra("uid", moduleContext.optString("uid"));
		intent.putExtra("posno", moduleContext.optString("pos"));
		intent.putExtra("GPS", moduleContext.optString("GPS"));
		intent.putExtra("cardNo", moduleContext.optString("cardNo"));
		intent.putExtra("acctNo", moduleContext.optString("acctNo"));
		intent.putExtra("name", moduleContext.optString("name"));
		intent.putExtra("url", moduleContext.optString("url"));
		intent.putExtra("word", moduleContext.optString("word"));
		intent.putExtra("needResult", true);
		intent.setClass(getContext(), TimelyAccountTopUpActivity.class);
		startActivityForResult(intent, ACTIVITY_REQUEST_CODE_A);
	}
	public void jsmethod_welcomeIndex(UZModuleContext moduleContext){
		mJsCallback =moduleContext;
		Intent intent = new Intent();
		intent.putExtra("uid",moduleContext.optString("uid"));
		intent.putExtra("posno", moduleContext.optString("pos"));
		intent.putExtra("GPS", moduleContext.optString("GPS"));
		intent.putExtra("cardNo", moduleContext.optString("cardNo"));
		intent.putExtra("acctNo", moduleContext.optString("acctNo"));
		intent.putExtra("name", moduleContext.optString("name"));
		intent.putExtra("url", moduleContext.optString("url"));
		intent.putExtra("word", moduleContext.optString("word"));
		intent.putExtra("uname", moduleContext.optString("uname"));
		intent.putExtra("GPS", moduleContext.optString("GPS"));
		intent.putExtra("idnum", moduleContext.optString("idnum"));
		intent.putExtra("phone",moduleContext.optString("phone"));
		intent.setClass(getContext(),WelcomeIndexActivity.class);
		startActivityForResult(intent,ACTIVITY_REQUEST_CODE_A);
		
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK && requestCode == ACTIVITY_REQUEST_CODE_A) {
			String result = data.getStringExtra("callback");
			
			if (null != result && null != mJsCallback) {
				try {
					
					JSONObject ret = new JSONObject(result);
					
					
					Log.v("push1", "callback final-->"+ret.toString());
					mJsCallback.success(ret, true);
					mJsCallback = null;
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
