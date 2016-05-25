package com.apicloud.cft;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.anfu.anf01.lib.inter.CAFSwiperController;
import com.apicloud.af.myAFswipListener;
import com.apicloud.common.PinSecurityImpl;
import com.apicloud.landy.LandyTackMsg;
import com.apicloud.module.BluetoothDeviceContext;
import com.apicloud.swip.SwipActivity;
import com.apicloud.swip.SwipApi;
import com.apicloud.swip.SwipUiHandler;
import com.apicloud.util.UICommon;
import com.bbpos.cswiper.CSwiperController;
import com.bbpos.cswiper.CSwiperController.DecodeResult;
import com.bbpos.cswiper.CSwiperController.PINKey;

public class myCsSwipService extends SwipApi {
	
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
	 private String track2="";
	 private String track3="";
	 private String expireDate="";
	 private String pointService="";
	 private String pin="";
	 private String data55="";
	 private String cardSn="";
	 private String enworkingKey="";
	 
	 private com.bbpos.cswiper.CSwiperController cswiperController;// 刷卡器控制者BBPOS
	 private com.bbpos.cswiper.CSwiperController.CSwiperStateChangedListener stateChangedListener;// 监听刷卡器BBPOS
	 
	 public myCsSwipService(Context activity,SwipUiHandler swipUIHandler,Handler mainHandler,int whatDo,Map<String, Object> params) {
			this.activity=activity;
			this.swipUIHandler=swipUIHandler;
			this.parentHandler=mainHandler;
		    this.whatDo=whatDo;
		    if(params!=null){
				initParam(params);
			}
			
		 }

	  void initParam(Map<String, Object> params){
	 	 
		   if(params.containsKey("whatDo")){
			  whatDo=(Integer) params.get("whatDo");
		   }
		   if(params.containsKey("amount")){
			  amount=params.get("amount")+"";
			  amount=amount.replace("¥","");
			  Log.v("cft1", "1-->"+amount+"");
		   }
		   if(params.containsKey("fromAct")){
				  fromAct=(Integer) params.get("fromAct");
			 }
		   
		   
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
		 * 刷卡器监听（BBPOS）
		 */
		private class StateChangedListener implements
				com.bbpos.cswiper.CSwiperController.CSwiperStateChangedListener {

		@Override
		public void onEPBDetected() {
			Log.v("cft1", "onEPBDetected");
			
		}

		@Override
		public void onPinEntryDetected(PINKey arg0) {
			Log.v("cft1", "onPinEntryDetected");
		}

		@Override
		public void onWaitingForPinEntry() {
			Log.v("cft1", "onWaitingForPinEntry");
			
		}

		@Override
		public void onCardSwipeDetected() {
			Log.v("cft1", "onCardSwipeDetected");
			
		}

		@Override
		public void onDecodeCompleted(HashMap<String, String> decodeData) {
			swipUIHandler.sendMessage(swipUIHandler.getTextMessage("获取卡片信息成功"));
			for (HashMap.Entry<String, String> entry : decodeData.entrySet()) {
				//sb.append(entry.getKey() + ": " + entry.getValue() + separator);
				Log.v("cft1", entry.getKey()+"--value-->"+entry.getValue());
			}
			String track2length=decodeData.get("track2Length")+"";
			String track3length=decodeData.get("track3Length")+"";
			LandyTackMsg landy=new LandyTackMsg();
			if(track2length.equals("255")){
				finishWorkFali();
			}
			if(track3length.equals("255")){
				finishWorkFali();
			}
			
			landy.amount=amount+"";
			landy.cardNo=decodeData.get("maskedPAN")+"";
			landy.track2=decodeData.get("track2Length")+"@"+decodeData.get("encTrack2")+"";
			landy.track3=decodeData.get("track3Length")+"@"+decodeData.get("encTrack3")+"";
			landy.ksn=ksn+"";
			landy.expireDate=decodeData.get("expiryDate")+"";
		    landy.pointService="021";
			landy.enworkingKey=decodeData.get("encWorkingKey")+"";
			enworkingKey=decodeData.get("encWorkingKey")+"";
			cardNo=landy.cardNo+"";
			
			if(whatDo==SwipApi.WHATDO_SWIPER){
				finishWorkSuccess(landy);
			}
			if(whatDo==SwipApi.WHATDO_GET_CARDNO){
				finishGetCardNo(landy);
			}
			
			
		}

		@Override
		public void onDecodeError(DecodeResult decodeEr) {
			Log.v("cft1", "onDecodeError"+decodeEr.toString());
			Toast.makeText(activity, "磁道信息获取失败", Toast.LENGTH_SHORT).show();
			finishWorkFali();
			
		}

		@Override
		public void onWaitingForCardSwipe() {
			Log.v("cft1", "onWaitingForCardSwipe");
			swipUIHandler.sendMessage(swipUIHandler.getTextMessage("等待刷卡"));
			
		}

		@Override
		public void onDevicePlugged() {
			Log.v("cft1", "onDevicePlugged");
			swipUIHandler.sendMessage(swipUIHandler.getTextMessage("连接成功"));
			//获取ksn
			getKsn();
			
		}

		@Override
		public void onDeviceUnplugged() {
			Log.v("cft1", "onDeviceUnplugged");
			Toast.makeText(activity, "设备连接中断", Toast.LENGTH_SHORT).show();
			finishWorkFali();
			
		}

		@Override
		public void onError(int arg0, String arg1) {
			Log.v("cft1", "onError");
			Toast.makeText(activity, "连接失败", Toast.LENGTH_SHORT).show();
			finishWorkFali();
			
		}

		@Override
		public void onInterrupted() {
			Log.v("cft1", "onInterrupted");
			finishWorkFali();
			
		}

		@Override
		public void onNoDeviceDetected() {
			// TODO Auto-generated method stub
			Log.v("cft1", "onNoDeviceDetected");
		}

		@Override
		public void onTimeout() {
			Log.v("cft1", "onTimeout");
		  Toast.makeText(activity, "刷卡超时", Toast.LENGTH_SHORT).show();
		  finishWorkFali();
			
		}

		@Override
		public void onWaitingForDevice() {
			// TODO Auto-generated method stub
			Log.v("cft1", "onWaitingForDevice");
		}

		@Override
		public void onGetKsnCompleted(String arg0) {
			swipUIHandler.sendMessage(swipUIHandler.getTextMessage("获取设备信息成功"));
			ksn=arg0+"";
			ksn=ksn.substring(0, 14)+"";
			Log.v("cft1", "ksn-->"+arg0);
			Log.v("cft1", "from-->"+fromAct);
			if(fromAct==UICommon.BindDeviceAactivity){
				LandyTackMsg landy=new LandyTackMsg();
				landy.ksn=ksn+"";
				finishGetCardNo(landy);
			}else{
				//开始刷卡
				swipUIHandler.sendMessage(swipUIHandler.getTextMessage("请刷卡"));
				try {
					SwipCard();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
			
			
		}
			
		}

	
	@Override
	public void openDev() {
		stateChangedListener = new StateChangedListener();
		cswiperController = CSwiperController.createInstance(activity,
				stateChangedListener);
		cswiperController.setDetectDeviceChange(true);
		
		if (cswiperController.isDevicePresent()) {// 
			getKsn();
			
		} else {
			
		}
		
		
		

	}
	
	private void getKsn(){
		try {
			if (cswiperController.getCSwiperState() == CSwiperController.CSwiperControllerState.STATE_IDLE) {
				cswiperController.getCSwiperKsn();
				
			}
		} catch (IllegalStateException ex) {
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
		cswiperController.deleteCSwiper();

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
		try {
			if (cswiperController.getCSwiperState() == CSwiperController.CSwiperControllerState.STATE_IDLE) {
				Log.v("cft1", "swip STATE_IDLE");
				cswiperController.startCSwiper();
			}
			else {
				Log.v("cft1", "swip not STATE_IDLE");
				cswiperController.stopCSwiper();
			}
		}
		catch (IllegalStateException ex) {
		
		}

	}

	@Override
	public void enterPin() {
		// TODO Auto-generated method stub

	}

	@Override
	public void encrtyPinkey(Object obj) {
		String psw=obj+"";
		PinSecurityImpl impl = new PinSecurityImpl();
		pin=impl.desSecurity(cardNo,psw,enworkingKey);
		Log.v("cft1","cardN-->"+cardNo+"--psw--"+psw+"-enwork-->"+enworkingKey);
		finishEncrykSuccess(pin);

	}

	@Override
	public void closeDev() {
		cswiperController.deleteCSwiper();
		//cswiperController.startCSwiper();

	}

}
