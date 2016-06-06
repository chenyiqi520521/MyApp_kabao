/**
 * Project Name:CardPay
 * File Name:Controller.java
 * Package Name:com.apicloud.moduleDemo
 * Date:2015-4-22下午6:18:52
 * Copyright (c) 2015, zhuxiaohao All Rights Reserved.
 *
 */

package com.apicloud.controller;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.apicloud.activity.BankNameActivity;
import com.apicloud.common.Common;
import com.apicloud.module.BankBean;
import com.apicloud.module.CardBean;
import com.apicloud.module.CardBean.DataBean;
import com.apicloud.module.CreditCardBean;
import com.apicloud.module.LoginBean;
import com.apicloud.module.MainBean;
import com.apicloud.module.MsgBean;
import com.apicloud.module.MsgPush;
import com.apicloud.module.MsgValidation;
import com.apicloud.module.PersonalBean;
import com.apicloud.module.ReChargeStyleBean;
import com.apicloud.module.RechargeItemBean;
import com.apicloud.util.HttpTools;

/**
 * ClassName:Controller <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2015-4-22 下午6:18:52 <br/>
 * 
 * @author zhuxiaohao blog:http://blog.csdn.net/qq718799510?viewmode=contents
 * @version 1.0.0
 * @since JDK 1.6
 * @see
 */
public class MyController {
	Context context;

	public MyController() {
	}

	public MyController(Context context) {
		this.context = context;
	}

	/**
	 * 
	 * isValidation:(验证卡号是否正确). <br/>
	 * 
	 * @author zhuxiaohao
	 * @return 如果成功就显示,查不到没显示
	 * @since JDK 1.6
	 */
	public MsgValidation isValidation(String cardno) {
		MsgValidation msgValidation = null;
		List<NameValuePair> nv = new ArrayList<NameValuePair>();
		nv.add(new BasicNameValuePair("cardno", cardno.replace(" ", "")));
		String strResult = HttpTools.getHttpRequestString(nv, Common.URL + "B2CPay/CardName");
		// String strResult=HttpTools.getHttpGetRequestString(Common.URL +
		// "B2CPay/CardName?"+"cardno="+cardno);

		if (!isEmpty(strResult)) {
			JSONObject jsonObject;
			try {
				msgValidation = new MsgValidation();
				jsonObject = new JSONObject(strResult);
				msgValidation.name = jsonObject.getString("name");
				msgValidation.cardtype = jsonObject.getString("cardtype");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return msgValidation;

	}
	/**
	 * 设备绑定接口
	 */
	public MsgBean bindDevice(String ksn,String shopno,String lkey) {
		MsgBean msgBean = null;
		
		List<NameValuePair> nv = new ArrayList<NameValuePair>();
		
		nv.add(new BasicNameValuePair("ksn",ksn+""));
		nv.add(new BasicNameValuePair("shopno", shopno+""));
		nv.add(new BasicNameValuePair("lkey", lkey+""));
		nv.add(new BasicNameValuePair("V",2+""));
		
		String strResult = HttpTools.getHttpRequestString(nv, Common.URL + "B2CPay/inksn");
		Log.v("push1", strResult+"");
		if (!isEmpty(strResult)) {
			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(strResult);
				msgBean = new MsgBean();
				try {
					msgBean.RspCd = jsonObject.getString("RspCd");
					msgBean.RspMsg = jsonObject.getString("RspMsg");
				} catch (Exception e) {
					// TODO: handle exception
				}
				} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return msgBean;

	}
	public static final int RESPONSE_NORMAL = 0;
	public static final int RESPONSE_FAIL = 1;

	// 获取支行接口
	public ArrayList<BankBean> getChilrenBankList(final Activity mContext, final String code, final String branch, String pageNum) {
		ArrayList<BankBean> temp = new ArrayList<BankBean>();
		/*
		 * List<NameValuePair> nv = new ArrayList<NameValuePair>(); nv.add(new
		 * BasicNameValuePair("code", code)); nv.add(new
		 * BasicNameValuePair("branch",branch)); nv.add(new
		 * BasicNameValuePair("p", pageNum));
		 */
		String finalBranch = branch.replace(" ", "");
		String uri = Common.URL + "B2CPay/CardBank?" + "branch=" + finalBranch + "&code=" + code + "&p=" + pageNum;
		String strResult = HttpTools.getHttpGetRequestString(uri);
		// String strResult = HttpTools.getHttpRequestString(nv, Common.URL +
		// "B2CPay/CardBank");

		if (!isEmpty(strResult)) {
			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(strResult);
				int error = jsonObject.getInt("error");
				if (error == RESPONSE_NORMAL) {
					int now_page = jsonObject.getInt("now_page");
					int page_num = jsonObject.getInt("page_num");
					JSONArray jarry = jsonObject.getJSONArray("data");
					if (jarry != null && jarry.length() > 0) {
						for (int i = 0; i < jarry.length(); i++) {
							JSONObject item = jarry.getJSONObject(i);
							BankBean bb = new BankBean();
							bb.setId(item.getString("ID"));
							bb.setChildrenBankCode(item.getString("BANK_CODE"));
							bb.setProvinceCode(item.getString("PROVINCE_CODE"));
							bb.setName(item.getString("NAME"));
							temp.add(bb);
							bb = null;

						}
					}
					if (now_page == page_num) {
						BankNameActivity.handler.post(new Runnable() {

							@Override
							public void run() {
								Toast.makeText(mContext, "暂无更多数据", Toast.LENGTH_SHORT).show();

							}

						});

					}
				} else {
					BankNameActivity.handler.post(new Runnable() {

						@Override
						public void run() {
							Toast.makeText(mContext, "请求失败", Toast.LENGTH_SHORT).show();

						}

					});
				}

			} catch (JSONException e) {

				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return temp;
	}

	// 获取主行接口
	public ArrayList<BankBean> getParetBankList(final Activity mContext) {
		ArrayList<BankBean> temp = new ArrayList<BankBean>();
		String strResult = HttpTools.getHttpRequestString(null, Common.URL + "B2CPay/Bank");
		if (!isEmpty(strResult)) {
			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(strResult);
				int error = jsonObject.getInt("error");
				if (error == RESPONSE_NORMAL) {
					JSONArray jarry = jsonObject.getJSONArray("data");
					if (jarry != null && jarry.length() > 0) {
						for (int i = 0; i < jarry.length(); i++) {
							JSONObject item = jarry.getJSONObject(i);
							BankBean bb = new BankBean();
							bb.setId(item.getString("ID"));
							bb.setCode(item.getString("CODE"));
							bb.setName(item.getString("NAME"));
							temp.add(bb);
							bb = null;

						}
					}
				} else {
					BankNameActivity.handler.post(new Runnable() {

						@Override
						public void run() {
							Toast.makeText(mContext, "请求失败", Toast.LENGTH_SHORT).show();

						}

					});
				}

			} catch (JSONException e) {

				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return temp;
	}

	public void tip(final Activity mContext, final String msg, Handler handler) {
		if (handler != null) {
			handler.post(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();

				}

			});
		}

	}

	/**
	 * 获取短信验证码
	 * 
	 */

	public String getSms(final Activity mContext, String phoneNum, String key, Handler handler) {
		String result_code = "-1";
		List<NameValuePair> nv = new ArrayList<NameValuePair>();
		nv.add(new BasicNameValuePair("phonenum", phoneNum));
		nv.add(new BasicNameValuePair("lkey", key));
		String strResult = HttpTools.getHttpRequestString(nv, Common.URL + "B2CPay/SendMsg");
		Log.v("param1", strResult + "");
		if (!isEmpty(strResult)) {
			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(strResult);
				String error = jsonObject.getString("error");
				if (error.equals("0")) {
					tip(mContext, "获取成功，请接受到后填写", handler);
					// 获取的验证码
					String code = jsonObject.getString("code");
					result_code = code;

				} else {
					tip(mContext, "获取失败", handler);
				}

			} catch (JSONException e) {

				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result_code;
	}

	public static final String CREDIT_CARD = 1 + "";// 信用卡认证
	public static final String NOT_CREADIT_CARD = 0 + "";// 其他卡
	public static final String COMMON_ADD_CREADIT_CARD = 2 + "";// 一般添加信用卡

	/**
	 * 
	 * pushCard:(添加成功). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param cardno
	 * @return 是否成功
	 * @since JDK 1.6
	 */
	public MsgPush pushCard(final Activity mContext, String cardno, String person, String type, String key, String idnum, String phoneNum, String smsNum, String branchild, Handler handler) {
		MsgPush stuate = null;
		List<NameValuePair> nv = new ArrayList<NameValuePair>();
		nv.add(new BasicNameValuePair("cardno", cardno.replace(" ", "")));
		nv.add(new BasicNameValuePair("person", person));
		if (!type.equals(COMMON_ADD_CREADIT_CARD)) {
			nv.add(new BasicNameValuePair("idnum", idnum));
			nv.add(new BasicNameValuePair("phonenum", phoneNum));
		}
		nv.add(new BasicNameValuePair("type", type));
		nv.add(new BasicNameValuePair("lkey", key));
		if (type.equals(CREDIT_CARD)) {
			nv.add(new BasicNameValuePair("smsnum", smsNum));
		} else if (type.equals(NOT_CREADIT_CARD)) {
			nv.add(new BasicNameValuePair("branchid", branchild));
		}
		nv.add(new BasicNameValuePair("V", 2 + ""));
		final String strResult = HttpTools.getHttpRequestString(nv, Common.URL + "B2CPay/PushCard2");

		Log.v("push1", "result->" + strResult + "");
		if (!isEmpty(strResult)) {
			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(strResult);
				stuate = new MsgPush();
				stuate.error = jsonObject.getString("error");

				if(jsonObject.has("RspMsg")){
					stuate.RspMsg=jsonObject.getString("RspMsg");
				}
				if (jsonObject.has("mainCard")) {
					Log.v("push1", "has");
					stuate.mainCard = jsonObject.getString("mainCard") + "";
				} else {
					Log.v("push1", "nohas");
				}

				/*
				 * String msg1=jsonObject.getString("RspMsg");
				 * if(msg1!=null&&msg1.length()>0){ stuate.RspMsg=msg1; }
				 */

				// final String error=stuate.error;
				Message msg = handler.obtainMessage();
				msg.obj = stuate;
				handler.sendMessage(msg);
				/*
				 * if(stuate.error.equals("0")){ tip(mContext, "添加成功", handler);
				 * }else{ tip(mContext, "添加失败", handler); }
				 */
				// stuate.cardid = jsonObject.getString("cardid");
				// stuate.type = jsonObject.getString("type");
			} catch (final JSONException e) {

				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return stuate;
	}

	/**
	 * creditCard:(信用卡还款). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param CreditCardBean
	 *            参数对象
	 * @return MsgBean 返回数据对象
	 * @throws UnsupportedEncodingException
	 * @since JDK 1.6
	 */
	public MsgBean creditCard(CreditCardBean cardBean) {
		MsgBean msgBean = null;
		String track2 = cardBean.acctNoT2;
		if (track2 != null) {
			track2 = track2.replace(" ", "");
		}

		String track3 = cardBean.acctNoT3;
		if (track3 != null) {
			track3 = track3.replace(" ", "");
		}
		List<NameValuePair> nv = new ArrayList<NameValuePair>();
		nv.add(new BasicNameValuePair("pointService", cardBean.pointService));
		nv.add(new BasicNameValuePair("acctNo", cardBean.acctNo.replace(" ", "")));
		nv.add(new BasicNameValuePair("transAmt", cardBean.transAmt));
		// nv.add(new BasicNameValuePair("trackdatas", cardBean.trackdatas));
		nv.add(new BasicNameValuePair("acctNoT2", track2 + ""));
		nv.add(new BasicNameValuePair("acctNoT3", track3 + ""));
		nv.add(new BasicNameValuePair("GPS", cardBean.gps + ""));
		nv.add(new BasicNameValuePair("pin", cardBean.pin));
		nv.add(new BasicNameValuePair("credit", cardBean.credit.replace(" ", "")));
		nv.add(new BasicNameValuePair("ic", cardBean.ic));
		nv.add(new BasicNameValuePair("cardEXPDate", cardBean.cardEXPDate));
		nv.add(new BasicNameValuePair("cardSN", cardBean.cardSN));
		nv.add(new BasicNameValuePair("ksn", cardBean.ksn));
		nv.add(new BasicNameValuePair("encWorkingKey", cardBean.encWorkingKey));
		nv.add(new BasicNameValuePair("lkey", cardBean.lkey));
		nv.add(new BasicNameValuePair("V", 2 + ""));
		String strResult = HttpTools.getHttpRequestString(nv, Common.URL + "B2CPay/CreditCard");
		if (!isEmpty(strResult)) {
			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(strResult);
				msgBean = new MsgBean();
				try {
					msgBean.RspCd = jsonObject.getString("RspCd");
					msgBean.ReferNO = jsonObject.getString("ReferNO");
				} catch (Exception e) {
					// TODO: handle exception
				}

				msgBean.RspMsg = jsonObject.getString("RspMsg");
				if (jsonObject.has("TransDate")) {
					msgBean.TransDate = jsonObject.getString("TransDate");
				}
				if (jsonObject.has("TransTime")) {
					msgBean.TransTime = jsonObject.getString("TransTime");
				}
				if (jsonObject.has("SettDate")) {
					msgBean.SettDate = jsonObject.getString("SettDate");
				}
				if (jsonObject.has("orderno")) {
					msgBean.orderno = jsonObject.getString("orderno");
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return msgBean;

	}

	/**
	 * amtTransfer:(AMT转账接口). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param CreditCardBean
	 *            参数对象
	 * @return MsgBean 返回数据对象
	 * @since JDK 1.6
	 */
	public MsgBean amtTransfer(CreditCardBean cardBean) {
		MsgBean msgBean = null;
		String track2 = cardBean.acctNoT2;
		if (track2 != null) {
			track2 = track2.replace(" ", "");
		}

		String track3 = cardBean.acctNoT3;
		if (track3 != null) {
			track3 = track3.replace(" ", "");
		}
		List<NameValuePair> nv = new ArrayList<NameValuePair>();
		nv.add(new BasicNameValuePair("pointService", cardBean.pointService));
		nv.add(new BasicNameValuePair("acctNo", cardBean.acctNo.replace(" ", "")));
		nv.add(new BasicNameValuePair("transAmt", cardBean.transAmt));
		// nv.add(new BasicNameValuePair("trackdatas", cardBean.trackdatas));
		nv.add(new BasicNameValuePair("acctNoT2", track2 + ""));
		nv.add(new BasicNameValuePair("acctNoT3", track3 + ""));
		nv.add(new BasicNameValuePair("GPS", cardBean.gps + ""));
		nv.add(new BasicNameValuePair("pin", cardBean.pin));
		nv.add(new BasicNameValuePair("cardNo", cardBean.cardNo));
		nv.add(new BasicNameValuePair("ic", cardBean.ic));
		nv.add(new BasicNameValuePair("cardEXPDate", cardBean.cardEXPDate));
		nv.add(new BasicNameValuePair("cardSN", cardBean.cardSN));
		nv.add(new BasicNameValuePair("ksn", cardBean.ksn));
		nv.add(new BasicNameValuePair("lkey", cardBean.lkey));
		nv.add(new BasicNameValuePair("encWorkingKey", cardBean.encWorkingKey));
		nv.add(new BasicNameValuePair("type", cardBean.type));
		nv.add(new BasicNameValuePair("idCard", cardBean.idCard));
		nv.add(new BasicNameValuePair("userName", cardBean.userName));
		nv.add(new BasicNameValuePair("V", 2 + ""));

		Log.v("param", "pointService->" + cardBean.pointService);
		Log.v("param", "acctNo->" + cardBean.acctNo.replace(" ", ""));
		Log.v("param", "transAmt->" + cardBean.transAmt);
		Log.v("param", "acctNoT2->" + track2 + "");
		Log.v("param", "acctNoT3->" + track3 + "");
		Log.v("param", "GPS->" + cardBean.gps);
		Log.v("param", "pin->" + cardBean.pin + "");
		Log.v("param", "cardNo->" + cardBean.cardNo + "");
		Log.v("param", "ic->" + cardBean.ic);
		Log.v("param", "cardEXPDate->" + cardBean.cardEXPDate);
		Log.v("param", "cardSN->" + cardBean.cardSN);
		Log.v("param", "ksn->" + cardBean.ksn);
		Log.v("param", "lkey->" + cardBean.lkey);
		Log.v("param", "encWorkingKey->" + cardBean.encWorkingKey);
		Log.v("param", "type->" + cardBean.type);
		Log.v("param", "idCard->" + cardBean.idCard);
		Log.v("param", "userName->" + cardBean.userName);
		Log.v("param", "requesturl->" + Common.URL + "B2CPay/AMTTransfer");

		/*
		 * Log.v("param", "bizCode->"+cardBean.bizCode); Log.v("param",
		 * "gps->"+cardBean.gps+"");
		 */

		String strResult = HttpTools.getHttpRequestString(nv, Common.URL + "B2CPay/AMTTransfer");
		if (!isEmpty(strResult)) {
			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(strResult);
				msgBean = new MsgBean();
				try {
					msgBean.RspCd = jsonObject.getString("RspCd");
					msgBean.ReferNO = jsonObject.getString("ReferNO");
				} catch (Exception e) {
					// TODO: handle exception
				}

				msgBean.RspMsg = jsonObject.getString("RspMsg");
				if (jsonObject.has("TransDate")) {
					msgBean.TransDate = jsonObject.getString("TransDate");
				}
				if (jsonObject.has("TransTime")) {
					msgBean.TransTime = jsonObject.getString("TransTime");
				}
				if (jsonObject.has("SettDate")) {
					msgBean.SettDate = jsonObject.getString("SettDate");
				}
				if (jsonObject.has("orderno")) {
					msgBean.orderno = jsonObject.getString("orderno");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return msgBean;

	}

	/**
	 * account:(即时到帐). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param CreditCardBean
	 *            参数对象
	 * @return MsgBean 返回数据对象
	 * @since JDK 1.6
	 */
	public MsgBean account(CreditCardBean cardBean, String code) {
		MsgBean msgBean = null;
		String track2 = cardBean.acctNoT2;
		if (track2 != null) {
			track2 = track2.replace(" ", "");
		}

		String track3 = cardBean.acctNoT3;
		if (track3 != null) {
			track3 = track3.replace(" ", "");
		}
		Log.v("param", "pointService->" + cardBean.pointService);
		Log.v("param", "acctNo->" + cardBean.acctNo.replace(" ", ""));
		Log.v("param", "transAmt->" + cardBean.transAmt);
		Log.v("param", "acctNoT2->" + track2 + "");
		Log.v("param", "acctNoT3->" + track3 + "");
		Log.v("param", "cardSN->" + cardBean.cardSN);
		Log.v("param", "ic->" + cardBean.ic);
		Log.v("param", "cardEXPDate->" + cardBean.cardEXPDate);
		Log.v("param", "ksn->" + cardBean.ksn);
		Log.v("param", "lkey->" + cardBean.lkey);
		Log.v("param", "encWorkingKey->" + cardBean.encWorkingKey);
		Log.v("param", "bizCode->" + cardBean.bizCode);
		Log.v("param", "gps->" + cardBean.gps + "");
		Log.v("param", "pin->" + cardBean.pin + "");
		List<NameValuePair> nv = new ArrayList<NameValuePair>();
		nv.add(new BasicNameValuePair("pointService", cardBean.pointService));
		nv.add(new BasicNameValuePair("acctNo", cardBean.acctNo.replace(" ", "")));
		nv.add(new BasicNameValuePair("transAmt", cardBean.transAmt));
		// nv.add(new BasicNameValuePair("trackdatas", cardBean.trackdatas));
		nv.add(new BasicNameValuePair("acctNoT2", track2 + ""));
		nv.add(new BasicNameValuePair("acctNoT3", track3 + ""));
		nv.add(new BasicNameValuePair("cardNo", cardBean.cardNo));
		nv.add(new BasicNameValuePair("pin", cardBean.pin));
		nv.add(new BasicNameValuePair("cardSN", cardBean.cardSN));
		nv.add(new BasicNameValuePair("ic", cardBean.ic));
		nv.add(new BasicNameValuePair("cardEXPDate", cardBean.cardEXPDate));
		nv.add(new BasicNameValuePair("ksn", cardBean.ksn));
		nv.add(new BasicNameValuePair("lkey", cardBean.lkey));
		nv.add(new BasicNameValuePair("encWorkingKey", cardBean.encWorkingKey));
		nv.add(new BasicNameValuePair("bizCode", cardBean.bizCode));
		nv.add(new BasicNameValuePair("GPS", cardBean.gps + ""));
		nv.add(new BasicNameValuePair("V", 2 + ""));
		nv.add(new BasicNameValuePair("code", code));
		String strResult = HttpTools.getHttpRequestString(nv, Common.URL + "B2CPay/Consume");
		Log.v("param", "response-->" + strResult);
		if (!isEmpty(strResult)) {
			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(strResult);
				msgBean = new MsgBean();
				try {
					msgBean.RspCd = jsonObject.getString("RspCd");
					msgBean.ReferNO = jsonObject.getString("ReferNO");
				} catch (Exception e) {
					// TODO: handle exception
				}

				msgBean.RspMsg = jsonObject.getString("RspMsg");
				if (jsonObject.has("TransDate")) {
					msgBean.TransDate = jsonObject.getString("TransDate");
				}
				if (jsonObject.has("TransTime")) {
					msgBean.TransTime = jsonObject.getString("TransTime");
				}
				if (jsonObject.has("SettDate")) {
					msgBean.SettDate = jsonObject.getString("SettDate");
				}
				if (jsonObject.has("orderno")) {
					msgBean.orderno = jsonObject.getString("orderno");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return msgBean;

	}

	/**
	 * amtTransfer:(余额查询接口). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param CreditCardBean
	 *            参数对象
	 * @return MsgBean 返回数据对象
	 * @since JDK 1.6
	 */
	public MsgBean getBalance(CreditCardBean cardBean) {
		MsgBean msgBean = null;
		List<NameValuePair> nv = new ArrayList<NameValuePair>();
		nv.add(new BasicNameValuePair("pointService", cardBean.pointService));
		nv.add(new BasicNameValuePair("acctNo", cardBean.acctNo.replace(" ", "")));
		nv.add(new BasicNameValuePair("transAmt", cardBean.transAmt));
		nv.add(new BasicNameValuePair("trackdatas", cardBean.trackdatas));
		nv.add(new BasicNameValuePair("pin", cardBean.pin));
		nv.add(new BasicNameValuePair("ic", cardBean.ic));
		nv.add(new BasicNameValuePair("cardEXPDate", cardBean.cardEXPDate));
		nv.add(new BasicNameValuePair("cardSN", cardBean.cardSN));
		nv.add(new BasicNameValuePair("ksn", cardBean.ksn));
		nv.add(new BasicNameValuePair("lkey", cardBean.lkey));
		nv.add(new BasicNameValuePair("encWorkingKey", cardBean.encWorkingKey));
		String strResult = HttpTools.getHttpRequestString(nv, Common.URL + "B2CPay/GetBalance");
		if (!isEmpty(strResult)) {
			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(strResult);
				msgBean = new MsgBean();
				msgBean.RspCd = jsonObject.getString("RspCd");
				msgBean.ReferNO = jsonObject.getString("ReferNO");
				msgBean.RspMsg = jsonObject.getString("RspMsg");
				if (jsonObject.has("TransDate")) {
					msgBean.TransDate = jsonObject.getString("TransDate");
				}
				if (jsonObject.has("TransTime")) {
					msgBean.TransTime = jsonObject.getString("TransTime");
				}
				if (jsonObject.has("SettDate")) {
					msgBean.SettDate = jsonObject.getString("SettDate");
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return msgBean;
	}

	/**
	 * mobileRecharge:(手机充值接口). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param CreditCardBean
	 *            参数对象
	 * @return MsgBean 返回数据对象
	 * @since JDK 1.6
	 */
	public MsgBean mobileRecharge(CreditCardBean cardBean) {
		MsgBean msgBean = null;
		String track2 = cardBean.acctNoT2;
		if (track2 != null) {
			track2 = track2.replace(" ", "");
		}

		String track3 = cardBean.acctNoT3;
		if (track3 != null) {
			track3 = track3.replace(" ", "");
		}
		List<NameValuePair> nv = new ArrayList<NameValuePair>();
		nv.add(new BasicNameValuePair("pointService", cardBean.pointService));
		nv.add(new BasicNameValuePair("acctNo", cardBean.acctNo.replace(" ", "")));
		nv.add(new BasicNameValuePair("transAmt", cardBean.transAmt));
		// nv.add(new BasicNameValuePair("trackdatas", cardBean.trackdatas));
		nv.add(new BasicNameValuePair("acctNoT2", track2 + ""));
		nv.add(new BasicNameValuePair("acctNoT3", track3 + ""));
		nv.add(new BasicNameValuePair("GPS", cardBean.gps + ""));
		nv.add(new BasicNameValuePair("pin", cardBean.pin));
		nv.add(new BasicNameValuePair("operator", cardBean.operator));
		nv.add(new BasicNameValuePair("mobile", cardBean.mobile));
		Log.v("phone1", cardBean.mobile);
		nv.add(new BasicNameValuePair("ic", cardBean.ic));
		nv.add(new BasicNameValuePair("cardEXPDate", cardBean.cardEXPDate));
		nv.add(new BasicNameValuePair("cardSN", cardBean.cardSN));
		nv.add(new BasicNameValuePair("ksn", cardBean.ksn));
		nv.add(new BasicNameValuePair("lkey", cardBean.lkey));
		nv.add(new BasicNameValuePair("encWorkingKey", cardBean.encWorkingKey));
		nv.add(new BasicNameValuePair("V", 2 + ""));

		Log.v("param", "pointService->" + cardBean.pointService);
		Log.v("param", "acctNo->" + cardBean.acctNo.replace(" ", ""));
		Log.v("param", "transAmt->" + cardBean.transAmt);
		Log.v("param", "acctNoT2->" + track2 + "");
		Log.v("param", "acctNoT3->" + track3 + "");
		Log.v("param", "GPS->" + cardBean.gps);
		Log.v("param", "pin->" + cardBean.pin + "");
		Log.v("param", "operator->" + cardBean.operator + "");
		Log.v("param", "mobile->" + cardBean.mobile + "");

		Log.v("param", "ic->" + cardBean.ic);
		Log.v("param", "cardEXPDate->" + cardBean.cardEXPDate);
		Log.v("param", "cardSN->" + cardBean.cardSN);
		Log.v("param", "ksn->" + cardBean.ksn);
		Log.v("param", "lkey->" + cardBean.lkey);
		Log.v("param", "encWorkingKey->" + cardBean.encWorkingKey);

		Log.v("param", "requesturl->" + Common.URL + "B2CPay/MobileRecharge");
		String strResult = HttpTools.getHttpRequestString(nv, Common.URL + "B2CPay/MobileRecharge");
		if (!isEmpty(strResult)) {
			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(strResult);
				msgBean = new MsgBean();
				try {
					msgBean.RspCd = jsonObject.getString("RspCd");
					msgBean.ReferNO = jsonObject.getString("ReferNO");
				} catch (Exception e) {
					// TODO: handle exception
				}

				msgBean.RspMsg = jsonObject.getString("RspMsg");
				if (jsonObject.has("TransDate")) {
					msgBean.TransDate = jsonObject.getString("TransDate");
				}
				if (jsonObject.has("TransTime")) {
					msgBean.TransTime = jsonObject.getString("TransTime");
				}
				if (jsonObject.has("SettDate")) {
					msgBean.SettDate = jsonObject.getString("SettDate");
				}
				if (jsonObject.has("orderno")) {
					msgBean.orderno = jsonObject.getString("orderno");
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return msgBean;
	}

	/**
	 * consume:(消费/支付接口). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param CreditCardBean
	 *            参数对象
	 * @return MsgBean 返回数据对象
	 * @since JDK 1.6
	 */
	public MsgBean consume_kuailian(CreditCardBean cardBean, String code) {
		MsgBean msgBean = null;
		String track2 = cardBean.acctNoT2;
		if (track2 != null) {
			track2 = track2.replace(" ", "");
		}

		String track3 = cardBean.acctNoT3;
		if (track3 != null) {
			track3 = track3.replace(" ", "");
		}
		Log.v("param", "pointService->" + cardBean.pointService);
		Log.v("param", "acctNo->" + cardBean.acctNo.replace(" ", ""));
		Log.v("param", "transAmt->" + cardBean.transAmt);
		Log.v("param", "acctNoT2->" + track2 + "");
		Log.v("param", "acctNoT3->" + track3 + "");
		Log.v("param", "cardSN->" + cardBean.cardSN);
		Log.v("param", "ic->" + cardBean.ic);
		Log.v("param", "cardEXPDate->" + cardBean.cardEXPDate);
		Log.v("param", "ksn->" + cardBean.ksn);
		Log.v("param", "lkey->" + cardBean.lkey);
		Log.v("param", "encWorkingKey->" + cardBean.encWorkingKey);
		Log.v("param", "bizCode->" + cardBean.bizCode);
		Log.v("param", "gps->" + cardBean.gps + "");
		Log.v("param", "pin->" + cardBean.pin + "");
		Log.v("param", "code->" + code + "");
		Log.v("param", "kualkian_username->" + cardBean.kuailian_username + "");
		Log.v("param", "kuailian_pwd->" + cardBean.kuailian_pwd + "");
		List<NameValuePair> nv = new ArrayList<NameValuePair>();
		nv.add(new BasicNameValuePair("pointService", cardBean.pointService));
		nv.add(new BasicNameValuePair("acctNo", cardBean.acctNo.replace(" ", "")));
		nv.add(new BasicNameValuePair("transAmt", cardBean.transAmt));
		nv.add(new BasicNameValuePair("acctNoT2", track2 + ""));
		nv.add(new BasicNameValuePair("acctNoT3", track3 + ""));
		nv.add(new BasicNameValuePair("GPS", cardBean.gps + ""));
		nv.add(new BasicNameValuePair("cardSN", cardBean.cardSN));
		nv.add(new BasicNameValuePair("ic", cardBean.ic));
		nv.add(new BasicNameValuePair("cardEXPDate", cardBean.cardEXPDate));
		nv.add(new BasicNameValuePair("ksn", cardBean.ksn));
		nv.add(new BasicNameValuePair("lkey", cardBean.lkey));
		nv.add(new BasicNameValuePair("encWorkingKey", cardBean.encWorkingKey));
		nv.add(new BasicNameValuePair("bizCode", cardBean.bizCode));
		nv.add(new BasicNameValuePair("pin", cardBean.pin));
		nv.add(new BasicNameValuePair("V", 2 + ""));
		nv.add(new BasicNameValuePair("code", code));
		nv.add(new BasicNameValuePair("username", cardBean.kuailian_username+""));
		nv.add(new BasicNameValuePair("pwd", cardBean.kuailian_pwd+""));
		String strResult = HttpTools.getHttpRequestString(nv, "http://121.43.231.170/klapi2/Seercf/Rech");
		Log.v("param", "response>" + strResult + "");
		if (!isEmpty(strResult)) {
			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(strResult);
				msgBean = new MsgBean();
				try {
					if(jsonObject.has("msg")){
						msgBean.RspMsg = jsonObject.getString("msg");
					}
					if(jsonObject.has("RspMsg")){
						msgBean.RspMsg = jsonObject.getString("RspMsg");
					}
					if(jsonObject.has("RspCd")){
						msgBean.RspCd = jsonObject.getString("RspCd");
					}
					if(jsonObject.has("ReferNO")){
						msgBean.ReferNO = jsonObject.getString("ReferNO");
					}
				
				} catch (Exception e) {

				}

				if (jsonObject.has("TransDate")) {
					msgBean.TransDate = jsonObject.getString("TransDate");
				}
				if (jsonObject.has("TransTime")) {
					msgBean.TransTime = jsonObject.getString("TransTime");
				}
				if (jsonObject.has("SettDate")) {
					msgBean.SettDate = jsonObject.getString("SettDate");
				}
				if (jsonObject.has("orderno")) {
					msgBean.orderno = jsonObject.getString("orderno");
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return msgBean;
	}
	/**
	 * consume:(消费/支付接口). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param CreditCardBean
	 *            参数对象
	 * @return MsgBean 返回数据对象
	 * @since JDK 1.6
	 */
	public MsgBean consume(CreditCardBean cardBean, String code) {
		MsgBean msgBean = null;
		String track2 = cardBean.acctNoT2;
		if (track2 != null) {
			track2 = track2.replace(" ", "");
		}

		String track3 = cardBean.acctNoT3;
		if (track3 != null) {
			track3 = track3.replace(" ", "");
		}
		Log.v("param", "pointService->" + cardBean.pointService);
		Log.v("param", "acctNo->" + cardBean.acctNo.replace(" ", ""));
		Log.v("param", "transAmt->" + cardBean.transAmt);
		Log.v("param", "acctNoT2->" + track2 + "");
		Log.v("param", "acctNoT3->" + track3 + "");
		Log.v("param", "cardSN->" + cardBean.cardSN);
		Log.v("param", "ic->" + cardBean.ic);
		Log.v("param", "cardEXPDate->" + cardBean.cardEXPDate);
		Log.v("param", "ksn->" + cardBean.ksn);
		Log.v("param", "lkey->" + cardBean.lkey);
		Log.v("param", "encWorkingKey->" + cardBean.encWorkingKey);
		Log.v("param", "bizCode->" + cardBean.bizCode);
		Log.v("param", "gps->" + cardBean.gps + "");
		Log.v("param", "pin->" + cardBean.pin + "");
		Log.v("param", "code->" + code + "");
		
		List<NameValuePair> nv = new ArrayList<NameValuePair>();
		nv.add(new BasicNameValuePair("pointService", cardBean.pointService));
		nv.add(new BasicNameValuePair("acctNo", cardBean.acctNo.replace(" ", "")));
		nv.add(new BasicNameValuePair("transAmt", cardBean.transAmt));
		nv.add(new BasicNameValuePair("acctNoT2", track2 + ""));
		nv.add(new BasicNameValuePair("acctNoT3", track3 + ""));
		nv.add(new BasicNameValuePair("GPS", cardBean.gps + ""));
		nv.add(new BasicNameValuePair("cardSN", cardBean.cardSN));
		nv.add(new BasicNameValuePair("ic", cardBean.ic));
		nv.add(new BasicNameValuePair("cardEXPDate", cardBean.cardEXPDate));
		nv.add(new BasicNameValuePair("ksn", cardBean.ksn));
		nv.add(new BasicNameValuePair("lkey", cardBean.lkey));
		nv.add(new BasicNameValuePair("encWorkingKey", cardBean.encWorkingKey));
		nv.add(new BasicNameValuePair("bizCode", cardBean.bizCode));
		nv.add(new BasicNameValuePair("pin", cardBean.pin));
		nv.add(new BasicNameValuePair("V", 2 + ""));
		nv.add(new BasicNameValuePair("code", code));
		
		String strResult = HttpTools.getHttpRequestString(nv, Common.URL + "B2CPay/Consume");
		Log.v("param", "response>" + strResult + "");
		if (!isEmpty(strResult)) {
			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(strResult);
				msgBean = new MsgBean();
				try {
					if(jsonObject.has("msg")){
						msgBean.RspMsg = jsonObject.getString("msg");
					}
					if(jsonObject.has("RspMsg")){
						msgBean.RspMsg = jsonObject.getString("RspMsg");
					}
					if(jsonObject.has("RspCd")){
						msgBean.RspCd = jsonObject.getString("RspCd");
					}
					if(jsonObject.has("ReferNO")){
						msgBean.ReferNO = jsonObject.getString("ReferNO");
					}
				
				} catch (Exception e) {

				}

				if (jsonObject.has("TransDate")) {
					msgBean.TransDate = jsonObject.getString("TransDate");
				}
				if (jsonObject.has("TransTime")) {
					msgBean.TransTime = jsonObject.getString("TransTime");
				}
				if (jsonObject.has("SettDate")) {
					msgBean.SettDate = jsonObject.getString("SettDate");
				}
				if (jsonObject.has("orderno")) {
					msgBean.orderno = jsonObject.getString("orderno");
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return msgBean;
	}
	/**
	 * consume:(消费/支付接口). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param CreditCardBean
	 *            参数对象
	 * @return MsgBean 返回数据对象
	 * @since JDK 1.6
	 */
	public MsgBean Query(CreditCardBean cardBean) {
		MsgBean msgBean = null;
		String track2 = cardBean.acctNoT2;
		if (track2 != null) {
			track2 = track2.replace(" ", "");
		}

		String track3 = cardBean.acctNoT3;
		if (track3 != null) {
			track3 = track3.replace(" ", "");
		}
		Log.v("param", "pointService->" + cardBean.pointService);
		Log.v("param", "acctNo->" + cardBean.acctNo.replace(" ", ""));
		Log.v("param", "transAmt->" + cardBean.transAmt);
		Log.v("param", "acctNoT2->" + track2 + "");
		Log.v("param", "acctNoT3->" + track3 + "");
		Log.v("param", "cardSN->" + cardBean.cardSN);
		Log.v("param", "ic->" + cardBean.ic);
		Log.v("param", "cardEXPDate->" + cardBean.cardEXPDate);
		Log.v("param", "ksn->" + cardBean.ksn);
		Log.v("param", "lkey->" + cardBean.lkey);
		Log.v("param", "encWorkingKey->" + cardBean.encWorkingKey);
		Log.v("param", "bizCode->" + cardBean.bizCode);
		// Log.v("param", "gps->"+cardBean.gps+"");
		Log.v("param", "pin->" + cardBean.pin + "");

		List<NameValuePair> nv = new ArrayList<NameValuePair>();
		nv.add(new BasicNameValuePair("pointService", cardBean.pointService));
		nv.add(new BasicNameValuePair("acctNo", cardBean.acctNo.replace(" ", "")));
		nv.add(new BasicNameValuePair("transAmt", cardBean.transAmt));
		nv.add(new BasicNameValuePair("acctNoT2", track2 + ""));
		nv.add(new BasicNameValuePair("acctNoT3", track3 + ""));
		// nv.add(new BasicNameValuePair("GPS", cardBean.gps+""));
		nv.add(new BasicNameValuePair("cardSN", cardBean.cardSN));
		nv.add(new BasicNameValuePair("ic", cardBean.ic));
		nv.add(new BasicNameValuePair("cardEXPDate", cardBean.cardEXPDate));
		nv.add(new BasicNameValuePair("ksn", cardBean.ksn));
		nv.add(new BasicNameValuePair("lkey", cardBean.lkey));
		nv.add(new BasicNameValuePair("encWorkingKey", cardBean.encWorkingKey));
		// nv.add(new BasicNameValuePair("bizCode",cardBean.bizCode));
		nv.add(new BasicNameValuePair("pin", cardBean.pin));
		// nv.add(new BasicNameValuePair("V",2+""));

		String strResult = HttpTools.getHttpRequestString(nv, Common.URL + "B2CPay/getBalance");
		Log.v("param", "response>" + strResult + "");
		if (!isEmpty(strResult)) {
			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(strResult);
				msgBean = new MsgBean();
				try {
					msgBean.RspCd = jsonObject.getString("RspCd");
					msgBean.Balance = jsonObject.getString("Balance");
					msgBean.RspMsg = jsonObject.getString("RspMsg");

				} catch (Exception e) {

				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return msgBean;
	}

	/**
	 * isEmpty:(判断是否为空). <br/>
	 * 
	 * @author zhuxiaohao
	 * @param str
	 * @return
	 * @since JDK 1.6
	 */
	public static boolean isEmpty(CharSequence str) {
		if (str == null || str.length() == 0)
			return true;
		else
			return false;
	}

	public static String JSONTokeners(String in) {
		// consume an optional byte order mark (BOM) if it exists
		if (in != null && in.startsWith("\ufeff")) {
			in = in.substring(1);
		}
		return in;
	}

	/**
	 * 检测网络是否可用
	 */
	public static boolean isNetConnective = false;

	public static boolean checkNetworkAvailable(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		if (networkInfo == null || !networkInfo.isAvailable()) {
			isNetConnective = false;
		} else {
			isNetConnective = true;
		}
		return isNetConnective;
	}

	/**
	 * 获取界面充值方式接口
	 * 
	 */
	public ReChargeStyleBean rechargeMethod(final Activity mContext, String key) {
		ReChargeStyleBean reChargeStyleBean = new ReChargeStyleBean();
		List<NameValuePair> nv = new ArrayList<NameValuePair>();
		nv.add(new BasicNameValuePair("lkey", key));
		nv.add(new BasicNameValuePair("system", "android"));
		String strResult = HttpTools.getHttpRequestString(nv, Common.URL + "Api/T1Menu");
		Log.v("param1", strResult + "");
		if (!isEmpty(strResult)) {
			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(strResult);
				JSONArray jsonArray = jsonObject.getJSONArray("list");
				List<RechargeItemBean> itemList = new ArrayList<RechargeItemBean>();
				for (int i = 0; i < jsonArray.length(); i++) {
					RechargeItemBean rechargeItemBean = new RechargeItemBean();
					String name = new JSONObject(jsonArray.getString(i).toString()).getString("name")+"";
					String p_link = new JSONObject(jsonArray.getString(i).toString()).getString("p_link")+"";
					String open = new JSONObject(jsonArray.getString(i).toString()).getString("open")+"";
					String bizCode=new JSONObject(jsonArray.getString(i).toString()).getString("bizCode")+"";
					rechargeItemBean.setName(name);
					rechargeItemBean.setP_link(p_link);
					rechargeItemBean.setOpen(open);
					rechargeItemBean.setBizCode(bizCode);
					itemList.add(rechargeItemBean);
				}
				reChargeStyleBean.setList(itemList);
				String fee_A = new JSONObject((jsonObject.getString("fee"))).getString("A");
				String fee_B_fee = new JSONObject(new JSONObject((jsonObject.getString("fee"))).getString("B")).getString("fee");
				String fee_B_top = new JSONObject(new JSONObject((jsonObject.getString("fee"))).getString("B")).getString("top");
				reChargeStyleBean.setA_fee(fee_A);
				reChargeStyleBean.setB_fee(fee_B_fee);
				reChargeStyleBean.setB_top(fee_B_top);
				String word = jsonObject.getString("word");
				reChargeStyleBean.setWord(word);

			} catch (JSONException e) {

				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return reChargeStyleBean;
	}

	/**
	 * 获取用户认证信用卡列表
	 * 
	 */
	public CardBean getCardInfo(final Activity mContext, String key, String type) {
		CardBean cardBean = new CardBean();
		List<NameValuePair> nv = new ArrayList<NameValuePair>();
		nv.add(new BasicNameValuePair("lkey", key));
		nv.add(new BasicNameValuePair("type", type));
		String strResult = HttpTools.getHttpRequestString(nv, Common.URL + "Api/xcard");
	    Log.v("param1", strResult + "");
		if (!isEmpty(strResult)) {
			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(strResult);
				int error = jsonObject.getInt("error");
				cardBean.setError(error);
				if (error == 0) {
					String zp = jsonObject.getString("zp")+"";
					cardBean.setZp(zp);
					String zp_img=jsonObject.getString("zp_name")+"";
					cardBean.setZp_img(Common.URL+"Tpl/Public/image/"+zp_img);
					
					JSONArray jsonArray = jsonObject.getJSONArray("data");
					List<DataBean> itemList = new ArrayList<DataBean>();
					for (int i = 0; i < jsonArray.length(); i++) {
						DataBean dataBean = new DataBean();
						String id = new JSONObject(jsonArray.getString(i).toString()).getString("id");
						String userno = new JSONObject(jsonArray.getString(i).toString()).getString("userno");
						String cardno = new JSONObject(jsonArray.getString(i).toString()).getString("cardno");
						String cardname = new JSONObject(jsonArray.getString(i).toString()).getString("cardname");
						String main = new JSONObject(jsonArray.getString(i).toString()).getString("main");
						String province = new JSONObject(jsonArray.getString(i).toString()).getString("province");
						String city = new JSONObject(jsonArray.getString(i).toString()).getString("city");
						String branch = new JSONObject(jsonArray.getString(i).toString()).getString("branch");
						String branchno = new JSONObject(jsonArray.getString(i).toString()).getString("branchno");
						String typename = new JSONObject(jsonArray.getString(i).toString()).getString("typename");
						String cname = new JSONObject(jsonArray.getString(i).toString()).getString("cname");
						String tel = new JSONObject(jsonArray.getString(i).toString()).getString("tel");
						String ctime = new JSONObject(jsonArray.getString(i).toString()).getString("ctime");
						String utime = new JSONObject(jsonArray.getString(i).toString()).getString("utime");
						String status = new JSONObject(jsonArray.getString(i).toString()).getString("status");
						String cardImg=new JSONObject(jsonArray.getString(i).toString()).getString("cardimg");
						String uName=new JSONObject(jsonArray.getString(i).toString()).getString("uname");
						dataBean.setId(id);
						dataBean.setUserno(userno);
						dataBean.setCardno(cardno);
						dataBean.setCardname(cardname);
						dataBean.setMain(main);
						dataBean.setProvince(province);
						dataBean.setCity(city);
						dataBean.setBranch(branch);
						dataBean.setBranchno(branchno);
						dataBean.setTypename(typename);
						dataBean.setCname(cname);
						dataBean.setTel(tel);
						dataBean.setCtime(ctime);
						dataBean.setUtime(utime);
						dataBean.setStatus(status);
						dataBean.setUname(uName);
						dataBean.setCardimg(Common.URL+"Tpl/Public/image/"+cardImg);
						itemList.add(dataBean);
					}
					cardBean.setData(itemList);
				}else{
					cardBean=null;
				}

			} catch (JSONException e) {

				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return cardBean;
	}

	/**
	 * 获取验证码
	 * 
	 */

	public String getCode(String uid) {
		String result_code = "-1";
		List<NameValuePair> nv = new ArrayList<NameValuePair>();
		nv.add(new BasicNameValuePair("lkey", uid));
		Log.v("param1",  "lkey-->get-"+uid);
		//"Api/Validationcode"
		String strResult = HttpTools.getHttpRequestString(nv, Common.URL + "Api/Validationcode");
		Log.v("param1", strResult + "");
		if (!isEmpty(strResult)) {
			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(strResult);
				String code = jsonObject.getString("code");
				result_code = code;
			} catch (JSONException e) {

				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result_code;
	}

	/**
	 * 验证验证码
	 */
	public boolean validateCode(String code,String uid){
		boolean flag=false;
		List<NameValuePair> nv = new ArrayList<NameValuePair>();
		nv.add(new BasicNameValuePair("lkey", uid));
		nv.add(new BasicNameValuePair("code", code));
		Log.v("param1",  "lkey-->v-"+uid);
		Log.v("param1",  "code-->v-"+code);
		String strResult = HttpTools.getHttpRequestString(nv, Common.URL + "Api/validate");
		Log.v("param1", strResult + "");
		
		if (!isEmpty(strResult)) {
			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(strResult);
				if(jsonObject.has("RspCd")){
					String rspCode=jsonObject.getString("RspCd")+"";
					Log.e("验证验证码", jsonObject.getString("RspCd")+"");
					if(rspCode.equals("0")){
						flag=true;
					}
				}
				
			} catch (JSONException e) {

				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return flag;
	}
	/**
	 * 删除银行卡
	 * 
	 */
	public MsgBean delcard(String key, String delids) {
		MsgBean msgBean = new MsgBean();
		List<NameValuePair> nv = new ArrayList<NameValuePair>();
		nv.add(new BasicNameValuePair("lkey", key));
		nv.add(new BasicNameValuePair("delids", delids));
		String strResult = HttpTools.getHttpRequestString(nv, Common.URL + "Api/delcard");
		Log.v("param1", strResult + "");
		if (!isEmpty(strResult)) {
			JSONObject jsonObject = null;
			try {
				jsonObject = new JSONObject(strResult);
				msgBean.RspCd = jsonObject.getString("error");
				msgBean.RspMsg = jsonObject.getString("msg");

			} catch (JSONException e) {

				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return msgBean;
	}
	/**
	 * 登陆请求
	 */
	public LoginBean doLogin(String mobile,String psd,String shopno){
		LoginBean loginBean = new LoginBean();
		List<NameValuePair> nv = new ArrayList<NameValuePair>();
		nv.add(new BasicNameValuePair("mobile",mobile+""));
		nv.add(new BasicNameValuePair("zpwd", psd));
		nv.add(new BasicNameValuePair("shopno", shopno));
		String result = HttpTools.getHttpRequestString(nv,Common.URL +"Api/LoginSetShop");
		Log.v("login----->",result +"");
		if(!isEmpty(result)){
			try {
				JSONObject json = new JSONObject(result);
				loginBean.error=json.getString("error");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return loginBean;
	}
	/**
	 * 请求设为主卡请求
	 */
	public MainBean doRequestMain(String lkey,String cardId){
		MainBean mainBean = new MainBean();
		List<NameValuePair> nv = new ArrayList<NameValuePair>();
		nv.add(new BasicNameValuePair("lkey", lkey));
		nv.add(new BasicNameValuePair("delids", cardId));
		String resultStr = HttpTools.getHttpRequestString(nv, Common.URL +"Api/setcard");
		if(!isEmpty(resultStr)){
			try {
				JSONObject json = new JSONObject(resultStr);
				mainBean.error = json.getString("error");
				mainBean.msg = json.getString("msg");
				Log.v("setCard------error",json.getString("error"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return mainBean;
	}
	/**
	 * 请求个人信息(例余额)
	 * @param lkey
	 * @return
	 */
	public PersonalBean requestMember(String lkey){
		PersonalBean pb = new PersonalBean();
		List<NameValuePair> nv = new ArrayList<NameValuePair>();
		nv.add(new BasicNameValuePair("lkey", lkey));
		String resultStr = HttpTools.getHttpRequestString(nv, Common.URL +"Api/member");
		Log.e("个人信息返回数据", "---->"+resultStr);
		if(!isEmpty(resultStr)){
			try {
				JSONObject json = new JSONObject(resultStr);
				JSONObject json2 = json.getJSONObject("data");
				pb.balance = json2.getString("balance");
				Log.v("banlance------>",json2.getString("balance")+"");
				pb.verify = json2.optString("verify");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.v("banlance------error-->",e.getMessage()+"");
				e.printStackTrace();
			}
		}
		return pb;
	}
	
	public LoginBean doWithDraw(String lkey,String cardId,String amount){
		LoginBean loginBean = new LoginBean();
		List<NameValuePair> nv = new ArrayList<NameValuePair>();
		nv.add(new BasicNameValuePair("lkey", lkey));
		nv.add(new BasicNameValuePair("cardid", cardId));
		nv.add(new BasicNameValuePair("amount", amount));
		Log.v("提现请求参数_--->", lkey+","+cardId+","+amount);
		String resultStr = HttpTools.getHttpRequestString(nv, Common.URL+"Api/postduration");
		if(!isEmpty(resultStr)){
			try {
				JSONObject json = new JSONObject(resultStr);
				
				loginBean.error = json.getString("error");
				loginBean.msg = json.getString("msg");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return loginBean;
	}
}
