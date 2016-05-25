package com.apicloud.bbpos_ic;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.apicloud.common.PinSecurityImpl;
import com.apicloud.landy.LandyTackMsg;
import com.apicloud.swip.SwipActivity;
import com.apicloud.swip.SwipApi;
import com.apicloud.swip.SwipUiHandler;
import com.apicloud.util.UICommon;
import com.bbpos.emvswipe.EmvSwipeController;
import com.bbpos.emvswipe.EmvSwipeController.AutoConfigError;
import com.bbpos.emvswipe.EmvSwipeController.BatteryStatus;
import com.bbpos.emvswipe.EmvSwipeController.CheckCardMode;
import com.bbpos.emvswipe.EmvSwipeController.CheckCardResult;
import com.bbpos.emvswipe.EmvSwipeController.DisplayText;
import com.bbpos.emvswipe.EmvSwipeController.EmvSwipeControllerListener;
import com.bbpos.emvswipe.EmvSwipeController.Error;
import com.bbpos.emvswipe.EmvSwipeController.NfcDataExchangeStatus;
import com.bbpos.emvswipe.EmvSwipeController.StartEmvResult;
import com.bbpos.emvswipe.EmvSwipeController.TerminalSettingStatus;
import com.bbpos.emvswipe.EmvSwipeController.TransactionResult;
import com.bbpos.emvswipe.EmvSwipeController.TransactionType;

/*
 * BBpos带IC刷卡设备
 */

public class MybbosIcService extends SwipApi {
	 private Context activity;
	 private SwipUiHandler swipUIHandler;
	 private Handler controllerHanlder;//Listener和刷卡Service的交互
	 private Handler parentHandler;//用来返回给主界面信息
	 private int whatDo;//需要服务类型
	 private int fromAct;// 从什么界面来
	 private String  amount="";//金额
	 private String orignal_amount="";//初始未转化前的金额
	 private String cardNo="";//卡号
	 private String ksn="";//序列号
	 private String track2=""; //2磁道
	 private String track3="";  //3磁道
	 private String expireDate=""; //有效期
	 private String pointService="";  //设备类型
	 private String pin="";  //加密字段
	 private String data55=""; //55域数据
	 private String cardSn=""; 
	 private String enworkingKey=""; //加密关键字
	 
	 private  EmvSwipeController emvSwipeController;// 刷卡器控制者BBPOS
	 EmvSwipeControllerListener emvlistener;
	 
	 /**
	  * 
	  * @param activity 上下文
	  * @param swipUIHandler UI交互处理Handler
	  * @param mainHandler 和主界面通信的Hanlder
	  * @param whatDo  请求做什么的
	  * @param params  数卡参数
	  */ 
	 public MybbosIcService(Context activity,SwipUiHandler swipUIHandler,Handler mainHandler,int whatDo,Map<String, Object> params) {
			this.activity=activity;
			this.swipUIHandler=swipUIHandler;
			this.parentHandler=mainHandler;
		    this.whatDo=whatDo;
		    if(params!=null){
				initParam(params);
			}
			
		 }

	 /**
	  * 
	  * TODO
	    @param params 初始化刷卡参数
	    2016年3月7日
	    void
	  */
	  void initParam(Map<String, Object> params){
	 	 
		  //做什么的
		   if(params.containsKey("whatDo")){
			  whatDo=(Integer) params.get("whatDo");
		   }
		   //金额
		   if(params.containsKey("amount")){
			  amount=params.get("amount")+"";
			  amount=amount.replace("¥","");
			  Log.v("cft1", "1-->"+amount+"");
		   }
		   if(whatDo==SwipApi.WHATDO_GET_CARDNO){
			   amount="1";
		   }
		   //来自哪个界面
		   if(params.containsKey("fromAct")){
				  fromAct=(Integer) params.get("fromAct");
			 }
		   /*if(fromAct==UICommon.BindDeviceAactivity){
			   amount="1";
		   }
		   */
		   
	   }
	  //结束刷卡服务，返回数据给主界面
	 	public void finishWorkSuccess(LandyTackMsg lanyTrackMsg){
	 		Message msg=parentHandler.obtainMessage();
	 		msg.what=SwipActivity.FINISH_WORK_SUCCESS;
	 		msg.obj=lanyTrackMsg;
	 		parentHandler.sendMessage(msg);
	 		
	 	}
	 	
	 	//结束加密，返回
		public void finishEncrykSuccess(String pinBlock){
			Message msg=parentHandler.obtainMessage();
			msg.what=SwipActivity.FINISH_ENCRY_KEY_SUCCESS;
			msg.obj=pinBlock;
			parentHandler.sendMessage(msg);
			
		}
		//结束刷卡，返回错误
		public void finishWorkFali(){
			Message msg=parentHandler.obtainMessage();
			msg.what=SwipActivity.FINISH_WORK_FAIL;
			parentHandler.sendMessage(msg);
		}
		//获取卡号成功返回
		public void finishGetCardNo(LandyTackMsg lanyTrackMsg){
			Message msg=parentHandler.obtainMessage();
			msg.what=SwipActivity.FINISH_GET_CARD_NO_SUCCESS;
			msg.obj=lanyTrackMsg;
			parentHandler.sendMessage(msg);
		}
		
		
		/**
		 * 
		 * @author Administrator
		 * 设备监听器，回调函数作用参考厂家文档
		 */
		private class emvListener implements EmvSwipeControllerListener{

			@Override
			public void onAutoConfigCompleted(boolean arg0, String arg1) {
				Log.v("bbpos", "onAutoConfigCompleted");
				
				getKsn();
				
			}

			@Override
			public void onAutoConfigError(AutoConfigError arg0) {
				Log.v("bbpos","onAutoConfigErro");
				Toast.makeText(activity, "配置失败"+arg0.toString(), Toast.LENGTH_SHORT).show();
				//swipUIHandler.sendMessage(swipUIHandler.getTextMessage("正在获取卡片信息"));
				finishWorkFali();
			}

			@Override
			public void onAutoConfigProgressUpdate(double arg0) {
				swipUIHandler.sendMessage(swipUIHandler.getTextMessage("连接成功"));
				Log.v("bbpos","onAutoConfigProgressUpdate--->"+arg0);
				DecimalFormat    df   = new DecimalFormat("######0.00");   
				swipUIHandler.sendMessage(swipUIHandler.getTextMessage("正在重新配置"+df.format(arg0)+"%"));
				
			}

			@Override
			public void onBatteryLow(BatteryStatus arg0) {
				Log.v("bbpos", "onBatteryLow");
				
			}

			@Override
			public void onDeviceHere(boolean arg0) {
				Log.v("bbpos", "onDeviceHere");
				
			}
           //设备连接成功
			@Override
			public void onDevicePlugged() {
				swipUIHandler.sendMessage(swipUIHandler.getTextMessage("连接成功"));
				Log.v("bbpos", "onDevicePlugged");
				//获取ksn
				//getKsn();
				emvSwipeController.startAutoConfig();
			}
            
			@Override
			public void onDeviceUnplugged() {
				Log.v("bbpos", "onDeviceUnplugged");
				//Log.v("cft1", "onDeviceUnplugged");
				Toast.makeText(activity, "设备连接中断", Toast.LENGTH_SHORT).show();
				//finishWorkFali();
				
			}

			@Override
			public void onError(Error arg0) {
				
				Log.v("bbpos", "onError"+arg0.toString());
				Toast.makeText(activity, "连接失败"+arg0.toString(), Toast.LENGTH_SHORT).show();
				//finishWorkFali();
				
			}

			@Override
			public void onNoDeviceDetected() {
				Log.v("bbpos", "onNoDeviceDetected");
				
			}

			@Override
			public void onPowerDown() {
				Log.v("bbpos", "onPowerDown");
				
			}

			@Override
			public void onRequestAdviceProcess(String arg0) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onRequestAdviceProcess");
			}

			@Override
			public void onRequestCheckServerConnectivity() {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onRequestCheckServerConnectivity");
			}

			@Override
			public void onRequestClearDisplay() {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onRequestClearDisplay");
			}

			@Override
			public void onRequestDisplayText(DisplayText arg0) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onRequestDisplayText");
				swipUIHandler.sendMessage(swipUIHandler.getTextMessage("正在获取卡片信息"));
			}

			@Override
			public void onRequestFinalConfirm() {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onRequestFinalConfirm");
				emvSwipeController.sendFinalConfirmResult(true);
			}

			
			//连接交易结束
			@Override
			public void onRequestOnlineProcess(String arg0) {
				Log.v("bbpos", "onRequestOnlineProcess");
				swipUIHandler.sendMessage(swipUIHandler.getTextMessage("获取卡片信息成功"));
				//打印返回的刷卡信息
				Hashtable<String, String> decodeData =EmvSwipeController.decodeTlv(arg0);
				for (HashMap.Entry<String, String> entry : decodeData.entrySet()) {
					//sb.append(entry.getKey() + ": " + entry.getValue() + separator);
					Log.v("bbpos", "ic-->"+entry.getKey()+"--value-->"+entry.getValue());
				}
				LandyTackMsg landy=new LandyTackMsg();
				landy.amount=amount+"";
				landy.cardNo=decodeData.get("maskedPAN")+"";
				landy.track2=decodeData.get("encTrack2Eq")+"";
				//landy.track3=decodeData.get("encTrack3")+"";
				landy.ksn=ksn+"";
				landy.expireDate=decodeData.get("5F24").substring(0,4)+"";
			    landy.pointService="051";
				landy.Data55=decodeData.get("encOnlineMessage")+"";
				landy.cardSn=decodeData.get("5F34")+"";
				landy.enworkingKey=decodeData.get("encTrack2EqRandomNumber")+"";
			
				enworkingKey=decodeData.get("encTrack2EqRandomNumber")+"";
				cardNo=landy.cardNo+"";
				
				if(whatDo==SwipApi.WHATDO_SWIPER){
					finishWorkSuccess(landy);
				}
				if(whatDo==SwipApi.WHATDO_GET_CARDNO){
					finishGetCardNo(landy);
				}
				/*String maskedPAN=decodeData.get("maskedPAN");//卡号
				String batchKsn=decodeData.get("batchKsn");
				String icdata=decodeData.get("encOnlineMessage");//55域数据
				String cardSeqNo=decodeData.get("5F34") == null ? "" : decodeData.get("5F34");
				cardSeqNo = cardSeqNo.substring(cardSeqNo.length()-1, cardSeqNo.length());
				String expiryDate = decodeData.get("5F24")==null ? "" : decodeData.get("5F24").substring(0, 4);//过期日期
				String encWorkingKey=decodeData.get("encTrack2EqRandomNumber");//加密
				String encTracks=decodeData.get("encTrack2Eq");//2磁道信息
*/				
			}

			@Override
			public void onRequestPinEntry() {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onRequestPinEntry");
				emvSwipeController.sendPinEntryResult("123456");
			}

			@Override
			public void onRequestReferProcess(String arg0) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onRequestReferProcess");
			}

			@Override
			public void onRequestSelectApplication(ArrayList<String> arg0) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onRequestSelectApplication");
			}

			@Override
			public void onRequestSetAmount() {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onRequestSetAmount");
				if(emvSwipeController.setAmount(amount, "", "156", TransactionType.GOODS)){
					
				}
			}

			@Override
			public void onRequestTerminalTime() {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onRequestTerminalTime");
			}

			@Override
			public void onRequestVerifyID(String arg0) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onRequestVerifyID");
			}

			@Override
			public void onReturnApduResult(boolean arg0, String arg1, int arg2) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onReturnApduResult");
			}

			@Override
			public void onReturnApduResultWithPkcs7Padding(boolean arg0, String arg1) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onReturnApduResultWithPkcs7Padding");
			}

			@Override
			public void onReturnBatchData(String arg0) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onReturnBatchData");
			}

			@Override
			public void onReturnCancelCheckCardResult(boolean arg0) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onReturnCancelCheckCardResult");
			}
            //返回检查卡片结果
			@Override
			public void onReturnCheckCardResult(CheckCardResult checkCardResult,
					Hashtable<String, String> decodeData) {
				Log.v("bbpos", "onReturnCheckCardResult");
				if(checkCardResult == CheckCardResult.NONE) {//刷卡或插卡已超时\n请按开始再试一次
					Log.v("bbpos", "onReturnCheckCardResult+CheckCardResult.NONE");
				    SwipCard();
				}else if(checkCardResult == CheckCardResult.ICC) {//ICC卡已插入
					String terminalTime = new SimpleDateFormat("yyMMddHHmmss").format(Calendar.getInstance().getTime());
					Hashtable<String, Object> data = new Hashtable<String, Object>();
					//设置Ic卡参数
					data.put("terminalTime", terminalTime);
					data.put("checkCardTimeout", "120");
					data.put("setAmountTimeout", "120");
					data.put("selectApplicationTimeout", "120");
					data.put("finalConfirmTimeout", "120");
					data.put("onlineProcessTimeout", "120");
					data.put("pinEntryTimeout", "120");
					data.put("emvOption", "START");
					data.put("checkCardMode", CheckCardMode.SWIPE_OR_INSERT);
					emvSwipeController.startEmv(data);
				}else if(checkCardResult == CheckCardResult.MCR) {//磁条卡刷卡成功
					for (HashMap.Entry<String, String> entry : decodeData.entrySet()) {
						//sb.append(entry.getKey() + ": " + entry.getValue() + separator);
						Log.v("bbpos", "mag-->"+entry.getKey()+"--value-->"+entry.getValue());
					}
					LandyTackMsg landy=new LandyTackMsg();
					landy.amount=amount+"";
					landy.cardNo=decodeData.get("PAN")+"";
					landy.track2=decodeData.get("encTrack2")+"";
					landy.track3=decodeData.get("encTrack3")+"";
					landy.ksn=ksn+"";
					landy.expireDate=decodeData.get("expiryDate")+"";
				    landy.pointService="021";
					landy.enworkingKey=decodeData.get("randomNumber")+"";
					enworkingKey=decodeData.get("randomNumber")+"";
					cardNo=landy.cardNo+"";
					
					if(whatDo==SwipApi.WHATDO_SWIPER){
						finishWorkSuccess(landy);
					}
					if(whatDo==SwipApi.WHATDO_GET_CARDNO){
						finishGetCardNo(landy);
					}
					
					/*String formatID = decodeData.get("formatID");
					String maskedPAN = decodeData.get("maskedPAN");//卡号
					String PAN = decodeData.get("PAN");
					String expiryDate = decodeData.get("expiryDate");//过期日期
					String cardHolderName = decodeData.get("cardholderName");
					String ksn = decodeData.get("ksn");//设备序列号
					String serviceCode = decodeData.get("serviceCode");
					String track1Length = decodeData.get("track1Length");
					String track2Length = decodeData.get("track2Length");
					String track3Length = decodeData.get("track3Length");
					String encTracks = decodeData.get("encTracks")==null ? "" :decodeData.get("encTracks");
					String encTrack1 = decodeData.get("encTrack1")==null ? "" :decodeData.get("encTrack1");
					String encTrack2 = decodeData.get("encTrack2")==null ? "" :decodeData.get("encTrack2");
					String encTrack3 = decodeData.get("encTrack3")==null ? "" :decodeData.get("encTrack3");
					String partialTrack = decodeData.get("partialTrack");
					String trackEncoding = decodeData.get("trackEncoding");
					String finalMessage = decodeData.get("finalMessage");
					String randomNumber = decodeData.get("randomNumber");*/
					
				}
				
			}

			@Override
			public void onReturnDeviceInfo(Hashtable<String, String> arg0) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onReturnDeviceInfo");
			}

			@Override
			public void onReturnEmvCardBalance(boolean arg0, String arg1) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onReturnEmvCardBalance");
			}

			@Override
			public void onReturnEmvCardDataResult(boolean arg0, String arg1) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onReturnEmvCardDataResult");
			}

			@Override
			public void onReturnEmvCardNumber(String arg0) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onReturnEmvCardNumber");
			}

			@Override
			public void onReturnEmvLoadLog(String[] arg0) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onReturnEmvLoadLog");
			}

			@Override
			public void onReturnEmvTransactionLog(String[] arg0) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onReturnEmvTransactionLog");
			}

			@Override
			public void onReturnEncryptDataResult(String arg0, String arg1) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onReturnEncryptDataResult");
			}

			@Override
			public void onReturnEncryptPinResult(Hashtable<String, String> arg0) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onReturnEncryptPinResult");
			}

			@Override
			public void onReturnKsn(Hashtable<String, String> arg0) {
				Log.v("bbpos", "onReturnKsn");
				// TODO Auto-generated method stub
				 ksn= arg0.get("csn") == null? "" : arg0.get("csn");
				 ksn=ksn.substring(0, 14);
				 Log.v("bbpos", "ksn-->"+ksn);
				 if(fromAct==UICommon.BindDeviceAactivity){
						LandyTackMsg landy=new LandyTackMsg();
						landy.ksn=ksn+"";
						finishGetCardNo(landy);
					}else{
						//开始刷卡
						swipUIHandler.sendMessage(swipUIHandler.getTextMessage("请刷卡或插卡"));
						try {
							SwipCard();
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
			}

			@Override
			public void onReturnNfcDataResult(NfcDataExchangeStatus arg0, String arg1, int arg2) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onReturnNfcDataResult");
			}

			@Override
			public void onReturnPowerOffIccResult(boolean arg0) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onReturnPowerOffIccResult");
			}

			@Override
			public void onReturnPowerOffNfcResult(boolean arg0) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onReturnPowerOffNfcResult");
			}

			@Override
			public void onReturnPowerOnIccResult(boolean arg0, String arg1, String arg2, int arg3) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onReturnPowerOnIccResult");
			}

			@Override
			public void onReturnPowerOnNfcResult(boolean arg0, String arg1, int arg2) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onReturnPowerOnNfcResult");
			}

			@Override
			public void onReturnReadTerminalSettingResult(TerminalSettingStatus arg0, String arg1) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onReturnReadTerminalSettingResult");
			}

			@Override
			public void onReturnReversalData(String arg0) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onReturnReversalData");
			}

			@Override
			public void onReturnStartEmvResult(StartEmvResult arg0, String arg1) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onReturnStartEmvResult");
			}

			@Override
			public void onReturnTransactionLog(String arg0) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onReturnTransactionLog");
			}

			@Override
			public void onReturnTransactionResult(TransactionResult arg0) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onReturnTransactionResult");
			}

			@Override
			public void onReturnTransactionResult(TransactionResult arg0, Hashtable<String, String> arg1) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onReturnTransactionResult");
			}

			@Override
			public void onReturnUpdateTerminalSettingResult(TerminalSettingStatus arg0) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onReturnUpdateTerminalSettingResult");
			}

			@Override
			public void onReturnViposBatchExchangeApduResult(Hashtable<Integer, String> arg0) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onReturnViposBatchExchangeApduResult");
			}

			@Override
			public void onReturnViposExchangeApduResult(String arg0) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onReturnViposExchangeApduResult");
			}

			@Override
			public void onWaitingForCard(CheckCardMode arg0) {
				// TODO Auto-generated method stub
				Log.v("bbpos", "onWaitingForCard");
			}
			
		}
	@Override
	public void openDev() {
		//初始化
		emvlistener=new emvListener();
		emvSwipeController=EmvSwipeController.getInstance(activity, emvlistener);
		emvSwipeController.startAudio();
		
		emvSwipeController.setDetectDeviceChange(true);
		
		if(emvSwipeController.isDevicePresent()){
			//getKsn();
			emvSwipeController.startAutoConfig();
		}

	}
	//获取设备序列号
	private void getKsn(){
		if(emvSwipeController==null){
			Log.v("bbpos", "null");
		}
		
		if(emvSwipeController!=null){
			
			parentHandler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					Log.v("bbpos", "getksn");
					emvSwipeController.getKsn();
				}
			}, 1000);
			
		}
	}

	@Override
	public void configuration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pushPinKey() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isConnect() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void connectDev() {
		// TODO Auto-generated method stub

	}

	@Override
	public void disConnect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void nofitySystemSetting() {
		// TODO Auto-generated method stub

	}

	@Override
	public void activityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void getDeviceInfo() {
		// TODO Auto-generated method stub

	}

	@Override
	public void SwipCard() {
		Hashtable<String, Object> data = new Hashtable<String, Object>();
		data.put("checkCardTimeout", "120");
		data.put("checkCardMode", CheckCardMode.SWIPE_OR_INSERT);
		emvSwipeController.checkCard(data);

	}

	@Override
	public void enterPin() {
		// TODO Auto-generated method stub

	}

	@Override
	public void encrtyPinkey(Object obj) {
		// TODO Auto-generated method stub
		String psw=obj+"";
		Log.v("cft1","cardN-->"+cardNo+"--psw--"+psw+"-enwork-->"+enworkingKey);
		PinSecurityImpl impl = new PinSecurityImpl();
		pin=impl.desSecurity(cardNo,psw,enworkingKey);
		//Log.v("cft1","cardN-->"+cardNo+"--psw--"+psw+"-enwork-->"+enworkingKey);
		finishEncrykSuccess(pin);
	}

	@Override
	public void closeDev() {
		if(emvSwipeController!=null){
			emvSwipeController.stopAudio();
			emvSwipeController.resetEmvSwipeController();
    		emvSwipeController = null;
		}

	}

}
