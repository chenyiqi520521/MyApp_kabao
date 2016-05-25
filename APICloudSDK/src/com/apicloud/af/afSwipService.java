package com.apicloud.af;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.anfu.anf01.lib.inter.CAFSwiperController;
import com.apicloud.landy.LandyListener;
import com.apicloud.landy.LandyTackMsg;
import com.apicloud.module.BluetoothDeviceContext;
import com.apicloud.swip.SwipActivity;
import com.apicloud.swip.SwipApi;
import com.apicloud.swip.SwipUiHandler;
import com.apicloud.util.UICommon;
import com.landicorp.android.mpos.reader.LandiMPos;
import com.landicorp.mpos.util.StringUtil;
import com.landicorp.robert.comm.api.DeviceInfo;
import com.landicorp.robert.comm.api.CommunicationManagerBase.DeviceCommunicationChannel;

public class afSwipService extends SwipApi{
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
	
	 CAFSwiperController m_swiperController = null;
	 myAFswipListener controllerListener=null;
	 private BluetoothDeviceContext  bluetoothDevice=null;
	 
	 public afSwipService(Context activity,SwipUiHandler swipUIHandler,Handler mainHandler,int whatDo,Map<String, Object> params) {
			this.activity=activity;
			this.swipUIHandler=swipUIHandler;
			this.parentHandler=mainHandler;
		    this.whatDo=whatDo;
		    initControllerHandler();
			
		    controllerListener=new myAFswipListener(controllerHanlder,activity);
		    m_swiperController=new CAFSwiperController(activity, controllerListener);
		    if(m_swiperController!=null){
		    	swipUIHandler.sendMessage(swipUIHandler.getTextMessage("初始化成功"));
		    }else{
		    	swipUIHandler.sendMessage(swipUIHandler.getTextMessage("初始化失败"));
		    }
			if(params!=null){
				initParam(params);
			}
			
		 }

     void initParam(Map<String, Object> params){
    	 if(params.containsKey("bluetooth")){
   		  bluetoothDevice=(BluetoothDeviceContext) params.get("bluetooth");
   		
   	  }
    	 if(params.containsKey("fromAct")){
			  fromAct=(Integer) params.get("fromAct");
		 }
   	   if(params.containsKey("whatDo")){
   		  whatDo=(Integer) params.get("whatDo");
   	   }
   	   if(params.containsKey("amount")){
   		  amount=params.get("amount")+"";
   		amount=amount.replace("¥","");
   	   }
   	   
      }
     
     
     void initControllerHandler(){
    	 controllerHanlder=new Handler(){
    		 @Override
    		public void handleMessage(Message msg) {
    			// TODO Auto-generated method stub
    			super.handleMessage(msg);
    			
    			if(msg.what==myAFswipListener.ERROR){
    				swipUIHandler.sendMessage(swipUIHandler.getTextMessage(msg.obj+""));
    				Log.v("af1", msg.obj+"");
    				
    				finishWorkFali();
    			}
    			if(msg.what==myAFswipListener.CONNECTED_FAILED){
    				swipUIHandler.sendMessage(swipUIHandler.getTextMessage("连接失败"));
    				Log.v("af1", "connected fail");
    				//finishWorkFali();
    			}
    			if(msg.what==myAFswipListener.CONNECTED_SUCCESS){
    				swipUIHandler.sendMessage(swipUIHandler.getTextMessage("连接成功"));
    				//导秘钥
    				String desKey = "8C8346A829493B408C8346A829493B4082E13665";
    				String pinKey = "A25C9D507FFEE034A25C9D507FFEE03400962B60";
    				boolean bret = m_swiperController.importWorkingKey(pinKey, desKey);
    				if(bret){
    					Log.v("af1", "loadpin success");
    					//获取设备信息
        				getDeviceInfo();
    				}else{
    					Log.v("af1", "loadpin fail");
    					swipUIHandler.sendMessage(swipUIHandler.getTextMessage("导入秘钥失败"));
    					//finishWorkFali();
    				}
    				
    			}
    			if(msg.what==myAFswipListener.WAITE_SWIP){
    				swipUIHandler.sendMessage(swipUIHandler.getTextMessage("等待刷卡....."));
    				
    			}
    			if(msg.what==myAFswipListener.GET_DEVICE_INFO_SUCCESS){
    				swipUIHandler.sendMessage(swipUIHandler.getTextMessage("获取设备信息成功"));
    				Map<String, String> map=(Map<String, String>) msg.obj;
    				if(map!=null){
    					Iterator<Entry<String, String>> ite=map.entrySet().iterator();
    					while (ite.hasNext()) {
    						 Map.Entry<String,String> entry = ite.next();  
							 Log.v("af1", "key-->"+entry.getKey()+"---value--"+entry.getValue());
						}
    					
    				}
    				
    				ksn=map.get("KSN")+"";
    				
    				if(fromAct==UICommon.BindDeviceAactivity){
						LandyTackMsg landy=new LandyTackMsg();
						landy.ksn=ksn+"";
						finishGetCardNosuccess(landy);
					}else{
						//请刷卡
	    				swipUIHandler.sendMessage(swipUIHandler.getTextMessage("请刷卡"));
	    				m_swiperController.setSwiperParameters(1, new Integer(2));
	    				m_swiperController.startSwiper("", 30);
					}
    			
    				
    				
    			}
    			
    			if(msg.what==myAFswipListener.GET_CARD_INFO_SUCCESS){
    				swipUIHandler.sendMessage(swipUIHandler.getTextMessage("获取卡片信息成功"));
    				Map<String, String> map=(Map<String, String>) msg.obj;
    				if(map!=null){
    					Iterator<Entry<String, String>> ite=map.entrySet().iterator();
    					while (ite.hasNext()) {
    						 Map.Entry<String,String> entry = ite.next();  
							 Log.v("af1", "card->"+"key-->"+entry.getKey()+"---value--"+entry.getValue());
						}
    					
    				}
    				
    				String isIC = map.get(m_swiperController.AFSWIPER_RETURN_MAP_KEY_ICCARDFLAG);
    				if(isIC != null && isIC.equals("01")){//Ic卡
    					cardNo=map.get(m_swiperController.AFSWIPER_RETURN_MAP_KEY_CARDNUMBER);
    					//statusEditText.setText(statusEditText.getText() + "\n" + "track2length: " + cardInfos.get(m_swiperController.AFSWIPER_RETURN_MAP_KEY_TRACK2LENGTH));
    					track2=map.get(m_swiperController.AFSWIPER_RETURN_MAP_KEY_TRACK2);
    					//statusEditText.setText(statusEditText.getText() + "\n" + "track3length: " + cardInfos.get(m_swiperController.AFSWIPER_RETURN_MAP_KEY_TRACK3LENGTH));
    					track3=map.get(m_swiperController.AFSWIPER_RETURN_MAP_KEY_TRACK3);
    					//statusEditText.setText(statusEditText.getText() + "\n" + "needpin: " + cardInfos.get(m_swiperController.AFSWIPER_RETURN_MAP_KEY_NEEDPIN));
    					data55=map.get(m_swiperController.AFSWIPER_RETURN_MAP_KEY_ICDATA);
    					cardSn=map.get(m_swiperController.AFSWIPER_RETURN_MAP_KEY_CRDSQN);
    					expireDate=map.get(m_swiperController.AFSWIPER_RETURN_MAP_KEY_EXPIRED);
    					expireDate=expireDate.substring(0,4);
    					pointService="051";
    					
    					LandyTackMsg landy=new LandyTackMsg();
    					landy.amount=amount+"";
    					landy.cardNo=cardNo+"";
    					landy.track2=track2+"";
    					landy.track3=track3+"";
    					landy.ksn="J"+ksn+"";
    					landy.expireDate=expireDate+"";
    					landy.Data55=data55+"";
    					landy.pointService=pointService+"";
    					landy.enworkingKey="0";
    					if(whatDo==SwipApi.WHATDO_GET_CARDNO){
    						finishGetCardNosuccess(landy);
    					}
    					if(whatDo==SwipApi.WHATDO_SWIPER){
    						finishWorkSuccess(landy);
    					}
    					
    				}else{//磁条卡
    					cardNo=map.get(m_swiperController.AFSWIPER_RETURN_MAP_KEY_CARDNUMBER);
    					track2=map.get(m_swiperController.AFSWIPER_RETURN_MAP_KEY_TRACK2);
    				    track3=map.get(m_swiperController.AFSWIPER_RETURN_MAP_KEY_TRACK3);
    					expireDate=map.get(m_swiperController.AFSWIPER_RETURN_MAP_KEY_EXPIRED);
    					expireDate=expireDate.substring(0,4);
    					pointService="021";
    					LandyTackMsg landy=new LandyTackMsg();
    					landy.amount=amount+"";
    					landy.cardNo=cardNo+"";
    					landy.enworkingKey="";
    					landy.expireDate=expireDate+"";
    					landy.ksn="J"+ksn+"";
    					landy.pointService=pointService+"";
    					landy.track2=track2+"";
    					landy.track3=track3+"";
    					landy.enworkingKey="0";
    					if(whatDo==SwipApi.WHATDO_GET_CARDNO){
    						finishGetCardNosuccess(landy);
    					}
    					if(whatDo==SwipApi.WHATDO_SWIPER){
    						finishWorkSuccess(landy);
    					}
    					
    					
    					
    				}
    			}
    		}
    	 };
     }
   //结束刷卡服务，返回数据给主界面
 	public void finishWorkSuccess(LandyTackMsg lanyTrackMsg){
 		Message msg=parentHandler.obtainMessage();
 		msg.what=SwipActivity.FINISH_WORK_SUCCESS;
 		msg.obj=lanyTrackMsg;
 		parentHandler.sendMessage(msg);
 		
 	}
	@Override
	public void openDev() {
		
		new Handler().postDelayed(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(!m_swiperController.isConnected()) {

					  m_swiperController.connectDevice(bluetoothDevice.address, 20);
					}
			}
		}, 1000);
		
		Log.v("af1", "address->"+bluetoothDevice.address+"");
	}

	@Override
	public void configuration() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pushPinKey() {
		
		
		
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
		m_swiperController.disconnectDevice();
		
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
		m_swiperController.getDeviceInfo();
	}

	@Override
	public void SwipCard() {
		m_swiperController.setSwiperParameters(1, new Integer(2));
		m_swiperController.startSwiper(amount, 30);
	}

	@Override
	public void enterPin() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void encrtyPinkey(Object obj) {
		String psw=obj+"";
		
		if(psw.length()>0){
			pin=m_swiperController.encryptPin(psw);
		}
		
		Log.v("af1", "pin-->"+pin);
		finishEncrykSuccess(pin);
		
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
		public void finishGetCardNosuccess(LandyTackMsg landy){
			Message msg=parentHandler.obtainMessage();
			msg.what=SwipActivity.FINISH_GET_CARD_NO_SUCCESS;
			msg.obj=landy;
			parentHandler.sendMessage(msg);
		}
	@Override
	public void closeDev() {
		// TODO Auto-generated method stub
		m_swiperController.disconnectDevice();
	}

}
