package com.apicloud.wft;

import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.anfu.anf01.lib.inter.CAFSwiperController;
import com.apicloud.af.myAFswipListener;
import com.apicloud.landy.LandyTackMsg;
import com.apicloud.module.BluetoothDeviceContext;
import com.apicloud.swip.SwipActivity;
import com.apicloud.swip.SwipApi;
import com.apicloud.swip.SwipUiHandler;
import com.apicloud.util.UICommon;
import com.landicorp.mpos.util.StringUtil;
import com.pax.commonlib.convert.Convert;
import com.pax.kalai.d180.api.KalaiD180PaxMpos;
import com.pax.kalai.d180.mis.Enum.CardType;
import com.pax.kalai.d180.mis.Enum.KeyType;
import com.pax.kalai.d180.mis.MagProcessResult;
import com.pax.kalai.d180.mis.MposDeviceInfo;
import com.pax.kalai.d180.mis.StartPBOCParam;
import com.pax.kalai.d180.mis.StartPBOCResult;


/**
 * 万富通刷卡
 * @author Administrator
 *
 */
public class wftService extends SwipApi {
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
	 private String track2="";//二磁道
	 private String track3="";//三磁道信息
	 private String expireDate="";//过期日期
	 private String pointService="";//支付费率渠道
	 private String pin="";//加密
	 private String data55="";//55域
	 private String cardSn="";//卡片序列号
	 private BluetoothDeviceContext  bluetoothDevice=null;
	 
	 //主秘钥，磁道秘钥，pin秘钥
	 private final String KEY_MASTEER_KET="737fc7087c0e2fd08c0b912ce91cdab3";
	 private final String KEY_TDK="6209e14062fa355aa8b93f176372801a5f5216c3e226c269";
	 private final String KEY_PINK="25fd542765920828ba7a828753b1bc7cf227b3180592e70f";
	 
	 private KalaiD180PaxMpos manager;//管理器
	 private wftListener myWftListener;//流程监听器
	 public wftService(Context activity,SwipUiHandler swipUIHandler,Handler mainHandler,int whatDo,Map<String, Object> params) {
		   this.activity=activity;
			this.swipUIHandler=swipUIHandler;
			this.parentHandler=mainHandler;
		    this.whatDo=whatDo;
		    manager=KalaiD180PaxMpos.getInstance(activity);
		   
		    initControllerHandler();
		    myWftListener=new wftListener(controllerHanlder);
			if(params!=null){
				initParam(params);
			}
	 }
	 /**
	  * 
	  * TODO 初始化刷卡参数
	    @param params
	    2016年4月7日
	    void
	  */
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
   		  orignal_amount=amount;
   		  if(amount.length()>0){
   			amount=StringUtil.formatTransAmountToFen(amount)+"";
   		  }
   	   }
   	   
      }
	 
	 /**
	  * 
	  * TODO 刷卡流程控制
	    2016年4月7日
	    void
	  */
	 void initControllerHandler(){
    	 controllerHanlder=new Handler(){
    		 @Override
    		public void handleMessage(Message msg) {
    			// TODO Auto-generated method stub
    			super.handleMessage(msg);
    			//报错
    			if(msg.what==100){
    				String error_msg=(String) msg.obj;
    				swipUIHandler.sendMessage(swipUIHandler.getTextMessage("错误信息->"+error_msg));
    				
    			}
    			//连接成功
    			if(msg.what==wftListener.MSG_OPEN_SUCCESS){
    				swipUIHandler.sendMessage(swipUIHandler.getTextMessage("连接成功"));
    				swipUIHandler.sendMessage(swipUIHandler.getTextMessage("正在导入秘钥"));
    				//导入秘钥
    				
    				manager.loadMasterKey(Convert.str2Bcd(KEY_MASTEER_KET),myWftListener.MyLoadmianKeyListener);
    				
    			}
    			//导入主秘钥成功
    			if(msg.what==wftListener.MSG_LOAD_MIAN_KEY_SUCCESS){
    				//导入磁道秘钥
    				
    				manager.loadWorkKey(KeyType.TDK, Convert.str2Bcd(KEY_TDK),myWftListener.getLoadWorklistener(0));
    			}
    			//导入磁道秘钥成功
    			if(msg.what==wftListener.MSG_LOAD_WORK_TRACK_KEY_SUCCESS){
    				
    				manager.loadWorkKey(KeyType.TPK, Convert.str2Bcd(KEY_PINK),myWftListener.getLoadWorklistener(1));
    			}
    			//导入Pin秘钥成功
    			if(msg.what==wftListener.MSG_LOAD_WORK_PIN_KEY_SUCCESS){
    				swipUIHandler.sendMessage(swipUIHandler.getTextMessage("正在获取设备信息"));
    				//获取设备信息
    				manager.getDeviceInfo(myWftListener.MygetDeviceInfoListener);
    			}
    			//获取设备信息成功
    			if(msg.what==wftListener.MSG_GET_DEVICE_INFO_SUCCESS){
    				MposDeviceInfo devInfo=(MposDeviceInfo) msg.obj;
    				ksn=devInfo.getCustomerSN()+"";
    				Log.v("wft1", "ksn-->"+ksn);
    				Log.v("wft1", "ksn1-->"+devInfo.getProductSN());
    				//如果是绑定设备
    				if(fromAct==UICommon.BindDeviceAactivity){
    					LandyTackMsg landy=new LandyTackMsg();
						landy.ksn=ksn+"";
						finishGetCardNosuccess(landy);
    				}else{//刷卡
    					
    					swipUIHandler.sendMessage(swipUIHandler.getTextMessage("请刷卡"));
    					int timeout = 60;//60s
    					manager.checkCard(CardType.MAGNETIC_IC_CARD, timeout, myWftListener.MycheckcardListener);
    				}
    			}
    			
    			//检查卡片类型成功
    			if(msg.what==wftListener.MSG_CHECK_CARD_SUCCESS){
    				swipUIHandler.sendMessage(swipUIHandler.getTextMessage("正在获取卡片信息"));
    				CardType cardType=(CardType) msg.obj;
    				//刷的是磁条卡
    				if(cardType == CardType.MAGNETIC_CARD){
    					int timeout = 60; //60s
    					manager.magProcess(amount, timeout,myWftListener.MymagprocessListener);
    					
    					
    				}
    				//刷的是IC卡
    				if(cardType == CardType.IC_CARD){
    					StartPBOCParam param = new StartPBOCParam();
					    param.setTransType((byte)0x00);
					    param.setDateTime("150504163400");
					    param.setAuthAmount(amount);
					    param.setOtherAmount("000000000000");
					    manager.startPBOC(param, myWftListener.MystartPbocListener);
    					
    					    
    				}
    				
    			}
    			//获取卡号成功
    			if(msg.what==wftListener.MSG_GET_CARDNUM_SUCCESS){
    				cardNo=(String) msg.obj;
    				LandyTackMsg landy=new LandyTackMsg();
    				landy.cardNo=cardNo;
    				
    				if(whatDo==SwipApi.WHATDO_GET_CARDNO){
						finishGetCardNosuccess(landy);
					}
    			}
    			
    			//磁条卡刷卡成功
    			if(msg.what==wftListener.MSG_MAG_SUCCESS){
    				swipUIHandler.sendMessage(swipUIHandler.getTextMessage("获取卡片信息成功"));
    				MagProcessResult result=(MagProcessResult) msg.obj;
    				cardNo=result.getPan()+"";
    				track2= Convert.bcd2Str(result.getCipherTrack2());
    				track2=hexStr2Str(track2);
    				track2=track2.replace("=","D");
    				if(result.getCipherTrack3()!=null){
    					track3= Convert.bcd2Str(result.getCipherTrack3());
        				track3=hexStr2Str(track3);
        				track3=track3.replace("=","D");
    				}
    				pin=Convert.bcd2Str(result.getPinblock());
    				pointService="021";
    				expireDate="";
    				
    				Log.v("wft1", "mag-->trac2-->"+track2+"---trac3-->"+track3+"---pin--"+pin);
    				
    				LandyTackMsg landy=new LandyTackMsg();
    				landy.amount=orignal_amount;
    				landy.cardNo=cardNo;
    		        landy.expireDate=expireDate+"";
					landy.ksn=""+ksn+"";
					landy.pointService=pointService+"";
					landy.track2=track2+"";
					landy.track3=track3+"";
					landy.enworkingKey="0";
					landy.pinBlock=pin;
					
					if(whatDo==SwipApi.WHATDO_GET_CARDNO){
						finishGetCardNosuccess(landy);
					}
					if(whatDo==SwipApi.WHATDO_SWIPER){
						finishWorkSuccess(landy);
					}
    			}
    			//IC卡刷卡成功
    			if(msg.what==wftListener.MSG_IC_SUCCESS){
    				swipUIHandler.sendMessage(swipUIHandler.getTextMessage("获取卡片信息成功"));
    				StartPBOCResult result=(StartPBOCResult) msg.obj;
    				cardNo=result.getPan()+"";
    				cardNo=cardNo.replace("F","");
    				track2= Convert.bcd2Str(result.getTrack2Cipher());
    				track2=hexStr2Str(track2);
    				track2=track2.replace("F","");
    				track2=track2.replace("=","D");
    				data55=Convert.bcd2Str(result.getIccData());
    				expireDate=result.getExpiry().substring(0, 4)+"";
    				cardSn=result.getCardSeq()+"";
    				pointService="051";
    				pin=Convert.bcd2Str(result.getPinBlock())+"";
    				LandyTackMsg landy=new LandyTackMsg();
					landy.amount=orignal_amount+"";
					landy.cardNo=cardNo+"";
					landy.track2=track2+"";
					landy.track3=track3+"";
					landy.ksn=""+ksn+"";
					landy.expireDate=expireDate+"";
					landy.Data55=data55+"";
					landy.pointService=pointService+"";
					landy.enworkingKey="0";
					landy.cardSn=cardSn+"";
					landy.pinBlock=pin;
					
					if(whatDo==SwipApi.WHATDO_GET_CARDNO){
						finishGetCardNosuccess(landy);
					}
					if(whatDo==SwipApi.WHATDO_SWIPER){
						finishWorkSuccess(landy);
					}
					
    				Log.v("wft1", "ic-->trac2-->"+track2+"---data-->"+data55);
    				
    			}
    			
    			
    		}
    	 };
   }
	@Override
	public void openDev() {
		// TODO Auto-generated method stub
		if(manager==null){
			manager=KalaiD180PaxMpos.getInstance(activity);
		}
		swipUIHandler.sendMessage(swipUIHandler.getTextMessage("尝试连接设备"));
        manager.connect(bluetoothDevice.address, myWftListener.MyopenDeviceListener);
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
		// TODO Auto-generated method stub

	}

	@Override
	public void enterPin() {
		// TODO Auto-generated method stub

	}

	@Override
	public void encrtyPinkey(Object obj) {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeDev() {
		if(manager!=null){
			manager.closeDevice();
		}
	}
	/**
	 2016年4月7日
	 create by mengyupeng
	 TODO
	 **/
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
	public void finishGetCardNosuccess(LandyTackMsg landy){
		Message msg=parentHandler.obtainMessage();
		msg.what=SwipActivity.FINISH_GET_CARD_NO_SUCCESS;
		msg.obj=landy;
		parentHandler.sendMessage(msg);
			}
	/**
	 * 十六进制转换字符串
	 * 
	 * @param String
	 *            str Byte字符串(Byte之间无分隔符 如:[616C6B])
	 * @return String 对应的字符串
	 */
	public String hexStr2Str(String hexStr) {
		hexStr = hexStr.replaceAll("0x", "");
		hexStr = hexStr.replaceAll(" ", "");
		String str = "0123456789ABCDEF";
		char[] hexs = hexStr.toCharArray();
		byte[] bytes = new byte[hexStr.length() / 2];
		int n;

		for (int i = 0; i < bytes.length; i++) {
			n = str.indexOf(hexs[2 * i]) * 16;
			n += str.indexOf(hexs[2 * i + 1]);
			bytes[i] = (byte) (n & 0xff);
		}
		return new String(bytes);
	}
}
