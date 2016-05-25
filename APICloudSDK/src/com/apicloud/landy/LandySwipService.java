package com.apicloud.landy;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.apicloud.module.BluetoothDeviceContext;
import com.apicloud.swip.SwipActivity;
import com.apicloud.swip.SwipApi;
import com.apicloud.swip.SwipUiHandler;
import com.apicloud.util.UICommon;
import com.landicorp.android.mpos.reader.LandiMPos;
import com.landicorp.android.mpos.reader.PBOCStopListener;
import com.landicorp.android.mpos.reader.model.StartPBOCParam;
import com.landicorp.android.mpos.reader.model.StartPBOCResult;
import com.landicorp.mpos.reader.BasicReaderListeners.CardType;
import com.landicorp.mpos.reader.BasicReaderListeners.WaitCardType;
import com.landicorp.mpos.reader.model.EncryptPinData;
import com.landicorp.mpos.reader.model.MPosDeviceInfo;
import com.landicorp.mpos.reader.model.MPosEMVProcessResult;
import com.landicorp.mpos.util.StringUtil;
import com.landicorp.robert.comm.api.DeviceInfo;
import com.landicorp.robert.comm.api.CommunicationManagerBase.CommunicationMode;
import com.landicorp.robert.comm.api.CommunicationManagerBase.DeviceCommunicationChannel;
import com.newland.mtype.util.Dump;
/**
 * 
 * @author myp
 * 联迪的刷卡api
 *
 */

public class LandySwipService extends SwipApi{
	 private Activity activity;
	 private SwipUiHandler swipUIHandler;
	 private Handler controllerHanlder;//Listener和刷卡Service的交互
	 private Handler parentHandler;//用来返回给主界面信息
	 private int whatDo;//需要服务类型
	 private int fromAct;// 从什么界面来
	 private String  amount="";//金额
	 private String orignal_amount="";//初始未转化前的金额
	 private String cardNo="";//卡号
	 private String ksn="";//序列号
	 private LandyTackMsg trackMsg=new LandyTackMsg();//磁道信息
	 private BluetoothDeviceContext  bluetoothDevice=null;
	 private DeviceInfo deviceInfo;
	 private LandiMPos reader;
	 private LandyListener landyListener=null;
	 private int IC_CARD=1;
	 private int MANGTIC_CAED=2;
	 private int CUR_CARD_TYPE=-1;//当前数卡类型
	 
	 
	 public static  String READY_CONNECT="尝试连接......";
	 public static String  CONNECTED_SUCCESS="连接成功";
	 public static String CONNECTED_FAIL="连接失败";
	 public static String  PLEASE_SWIP="请刷卡或插卡";
	 public static String  SWIPING="正在获取卡片信息......";
	 public static String  SWIP_SUCCESS="获取信息成功";
	 public static String SWIP_FAIL="获取信息失败";
	 public static String GET_TRACHING="正在获取磁道信息";
	 
	 public static String GETING_DEVICE_INFO="正在获取设备信息";
	 public static String LOADING_KEY="正在导入秘钥";
	 public static String LOAD_MASTER_KEY_SUCCESS="导入主秘钥成功";
	 public static String LOAD_MASTER_KEY_FAIL="导入主秘钥失败";
	 public static String LOAD_PIN_KEY_SUCCESS="导入Pin秘钥成功";
	 public static String LOAD_PIN_KEY_FAIL="导入Pin秘钥失败";
	 public static String LOAD_TRACK_KEY_SUCCESS="导入磁道秘钥成功";
	 public static String LOAD_TRACK_KEY_FAIL="导入磁道秘钥失败";
	 
	 public static String ENCRY_PIN_SUCCESS="加密Pin成功";
	 public static String ENCRY_PIN_FAIL="加密Pin失败";
			
	 
	 
	 
	 /**
	  * 
	  * @param activity上下文
	  * @param swipUIHandler UI信息传递
	  * @param whatDo  判断只是激活还是刷卡
	  * @param params  初始化的一些参数
	  */
	 
     public LandySwipService(Activity activity,SwipUiHandler swipUIHandler,Handler mainHandler,int whatDo,Map<String, Object> params) {
		this.activity=activity;
		this.swipUIHandler=swipUIHandler;
		this.parentHandler=mainHandler;
	    this.whatDo=whatDo;
		reader = LandiMPos.getInstance(activity);
		initControllerHanlder();
		landyListener=new LandyListener(controllerHanlder);
		if(params!=null){
			initParam(params);
		}
		
	 }
	
   
    /**
     * 
     * TODO  初始化刷卡等需要的一些参数
       2015年12月9日
       void
     */
     
      void initParam(Map<String, Object> params){
    	  
    	  if(params.containsKey("bluetooth")){
    		  bluetoothDevice=(BluetoothDeviceContext) params.get("bluetooth");
    		  //初始化连接参数
    		  deviceInfo=new DeviceInfo();
    		  deviceInfo.setDevChannel(DeviceCommunicationChannel.BLUETOOTH);
    		  deviceInfo.setIdentifier(bluetoothDevice.address);
    		  deviceInfo.setName(bluetoothDevice.name);
    		  
    	  }
    	  
    	  if(params.containsKey("whatDo")){
    		  whatDo=(Integer) params.get("whatDo");
    		  
    	  }
    	  if(params.containsKey("fromAct")){
			  fromAct=(Integer) params.get("fromAct");
		 }
    	  if(params.containsKey("amount")){
    		  amount=params.get("amount")+"";
    		  try {
				
			} catch (Exception e) {
				// TODO: handle exception
			}
    		  amount=amount.replace("¥","");
    		  orignal_amount=amount+"";
    		  if(amount.length()>0){
    			  //转化金额的形式
    			  amount=StringUtil.formatTransAmountToFen(amount)+"";
    			  Log.v("landy1", "amount1-->"+amount);

    		  }
    	  }
    	  
      }
      
      
     
      /**
       * 中央控制的Handler,作为该Service和各种Listener的通信
       */
     void initControllerHanlder(){
    	 controllerHanlder=new Handler(){
    		 @Override
    		public void handleMessage(Message msg) {
    			// TODO Auto-generated method stub
    			super.handleMessage(msg);
    			int resultCode=msg.what;
    			switch (resultCode) {
    			
				case LandyListener.OPEN_DEVICE_SUCCESS://打开成功
					isConnected=true;
					//swipUIHandler.sendMessage(swipUIHandler.getTipMessage("打开设备成功"));
					swipUIHandler.sendMessage(swipUIHandler.getTextMessage(CONNECTED_SUCCESS));
					//加载主秘钥
					swipUIHandler.sendMessage(swipUIHandler.getTextMessage(LOADING_KEY));
					 //如果只是获取卡号，结束
               	 if(whatDo==SwipApi.WHATDO_GET_CARDNO){
               		swipUIHandler.sendMessage(swipUIHandler.getTextMessage(GETING_DEVICE_INFO));
                	//读取设备信息
					getDeviceInfo();
               		 
               	 }
               	 //如果是交易，开始交易流程
               	 if(whatDo==SwipApi.WHATDO_SWIPER){
               		loadMasterKey();
               	 }
					//getDeviceInfo();
					break;
					
				case LandyListener.OPEN_DEVICE_FAIL://打开失败
					//swipUIHandler.sendMessage(swipUIHandler.getTipMessage("打开设备失败"));
					swipUIHandler.sendMessage(swipUIHandler.getTextMessage(CONNECTED_FAIL+msg.obj));
					finishWorkFali();
					break;
				case LandyListener.LOAD_MATSTER_KEY_SUCCESS://导入主秘钥成功
					swipUIHandler.sendMessage(swipUIHandler.getTextMessage(LOAD_MASTER_KEY_SUCCESS));
					//导入pinKey
					loadPinKey();
					break;
                case LandyListener.LOAD_MATSTER_KEY_FAIL://导入主秘钥失败
                	swipUIHandler.sendMessage(swipUIHandler.getTextMessage(LOAD_MASTER_KEY_FAIL));
                	finishWorkFali();
					break;
                case LandyListener.LOAD_PIN_KEY_SUCCESS://导入pin秘钥成功
                	swipUIHandler.sendMessage(swipUIHandler.getTextMessage(LOAD_PIN_KEY_SUCCESS));
                	loadTrackKey();
                	break;
                case LandyListener.LOAD_PIN_KEY_FAIL://导入Pin秘钥失败
                	swipUIHandler.sendMessage(swipUIHandler.getTextMessage(LOAD_PIN_KEY_FAIL+msg.obj));
                	finishWorkFali();
                	break;
                case LandyListener.LOAD_TRACK_KEY_SUCCESS://导入磁道秘钥成功
                	swipUIHandler.sendMessage(swipUIHandler.getTextMessage(LOAD_TRACK_KEY_SUCCESS));
                	swipUIHandler.sendMessage(swipUIHandler.getTextMessage(GETING_DEVICE_INFO));
                	//读取设备信息
					getDeviceInfo();
                	
                	break;
                case LandyListener.LOAD_TRACK_KEY_FAIL://导入磁道秘钥失败
                	swipUIHandler.sendMessage(swipUIHandler.getTextMessage(LOAD_TRACK_KEY_FAIL+msg.obj));
                	finishWorkFali();
                	break;
				case LandyListener.GET_DEVICE_INFO_SUCCESS://获取信息成功
					MPosDeviceInfo info=(MPosDeviceInfo) msg.obj;
					ksn=info.clientSN+"";
					if(fromAct==UICommon.BindDeviceAactivity){
						LandyTackMsg landy=new LandyTackMsg();
						landy.ksn=ksn+"";
						finishGetCardNo(landy);
					}else{
						//开始刷卡
						//swipUIHandler.sendMessage(swipUIHandler.getTextMessage("设备序列号:"+info.clientSN+"硬件号:"+info.hardwareSN));
						swipUIHandler.sendMessage(swipUIHandler.getTextMessage(PLEASE_SWIP));
						SwipCard();
					}
					
					break;
                 case LandyListener.GET_DEVICE_INFO_FAIL://获取信息失败
                	 finishWorkFali();
                	 swipUIHandler.sendMessage(swipUIHandler.getTextMessage("获取信息失败-->"+msg.obj));
					break;
                 case LandyListener.SWIP_CARD_SUCCESS://刷卡成功
                	
                	String cardType=(String) msg.obj;
                	 Log.v("landy1",cardType+"");
                	 //如果刷的是磁条卡
                	 if(cardType.equals("MAGNETIC_CARD")){
                		 swipUIHandler.sendMessage(swipUIHandler.getTextMessage(SWIPING));
                		 //landySwipCDCrad();
                		 //设置当前类型为磁条卡
                		 CUR_CARD_TYPE=MANGTIC_CAED;
                		 
                	 }
                	 //如果刷的是IC卡
                	
                	 if (cardType.equals("IC_CARD") || cardType.equals("RF_CARD")) {
                		 //landySwipICcard();
                		 swipUIHandler.sendMessage(swipUIHandler.getTextMessage(SWIPING));
                		 //设备当前类型为IC卡
                		 CUR_CARD_TYPE=IC_CARD;
                	 }
                	 
                	 //获取卡号
                	 reader.getPANPlain(landyListener.getMypgetPanPlian());
  
                	 
                	 break;
                 case LandyListener.SWIP_CARD_FAIL://刷卡失败
                	 finishWorkFali();
                	 swipUIHandler.sendMessage(swipUIHandler.getTextMessage("刷卡失败-->"+msg.obj)); 
                	 break;
                 case LandyListener.GET_PAN_PLAIN_SUCCESS://获取卡号成功
                	 //swipUIHandler.sendMessage(swipUIHandler.getTextMessage("获取卡号-->"+msg.obj));
                	 //获取的卡号
                	 cardNo=msg.obj+"".replace(" ", "");
                	 trackMsg.cardNo=cardNo+"";
                	 Log.v("landy1", "cardno1-->"+cardNo);
                	 //如果只是获取卡号，结束
                	 if(whatDo==SwipApi.WHATDO_GET_CARDNO){
                		 swipUIHandler.sendMessage(swipUIHandler.getTextMessage(SWIP_SUCCESS));
                		 finishGetCardNo(trackMsg);
                		 
                	 }
                	 //如果是交易，开始交易流程
                	 if(whatDo==SwipApi.WHATDO_SWIPER){
                		 swipUIHandler.sendMessage(swipUIHandler.getTextMessage(GET_TRACHING));
                		 //获取磁道信息
                    	 if(CUR_CARD_TYPE==MANGTIC_CAED){
                    		 //获取磁条卡信息
                    		 getTrackData(); 
                    	 }else{
                    		 //获取IC卡的信息
                    		 landySwipICcard(); 
                    	 }
                	 }
                	
                	
                	 break;
                 case LandyListener.GET_PAN_PLAIN_FALI://获取卡号失败
                	 finishWorkFali();
                	 swipUIHandler.sendMessage(swipUIHandler.getTextMessage("获取卡号失败-->"+msg.obj)); 
                	 break;
                 case LandyListener.GET_TRACK_DATA_SUCCESS://磁条卡获取信息成功
                	 trackMsg=(LandyTackMsg) msg.obj;
                	 trackMsg.cardNo=cardNo;
                	 trackMsg.ksn=ksn;
                	 trackMsg.amount=orignal_amount;
                	 trackMsg.pointService="021";
                	 trackMsg.enworkingKey="0";
                	 swipUIHandler.sendMessage(swipUIHandler.getTextMessage(SWIP_SUCCESS));
                	 //swipUIHandler.sendMessage(swipUIHandler.getTextMessage("获取磁道信息成功,过期日期-->"+trackMsg.expireDate));
                	 finishWorkSuccess(trackMsg);
                	 break;
                 case LandyListener.GET_TRACK_DATA_FAIL://磁条卡获取信息失败
                	
                     swipUIHandler.sendMessage(swipUIHandler.getTextMessage("获取磁道信息失败-->"+msg.obj));
                	 finishWorkFali();
                	 break;
                 case LandyListener.EMV_SUCCESS://IC卡交易成功
                	 MPosEMVProcessResult result=(MPosEMVProcessResult) msg.obj;
                	 trackMsg.expireDate=result.getExpireData()+"";
                	 trackMsg.track2=result.getTrack2()+"";
                	 trackMsg.cardNo=cardNo;
                	 trackMsg.ksn=ksn;
                	 trackMsg.amount=orignal_amount;
                	 trackMsg.pointService="051";
                	 trackMsg.enworkingKey="0";
                	// swipUIHandler.sendMessage(swipUIHandler.getTextMessage("获取IC信息成功,过期日期-->"+trackMsg.track2));
                	 
                	 break;
                 case LandyListener.EMV_FAIL://IC卡交易失败
                	 finishWorkFali();
                	 swipUIHandler.sendMessage(swipUIHandler.getTextMessage("获取IC信息失败-->"+msg.obj)); 
                	 break;
                 case LandyListener.PBOC_START_SUCCESS://pboc55域数据获取成功
                	 StartPBOCResult result1=(StartPBOCResult) msg.obj;
                	 trackMsg.Data55=Dump.getHexDump(result1.getICCardData()).replace(" ","");
                	 finishWorkSuccess(trackMsg);
                	 Log.v("landy1", "data55->"+trackMsg.Data55);
                	 swipUIHandler.sendMessage(swipUIHandler.getTextMessage(SWIP_SUCCESS));
                	 //swipUIHandler.sendMessage(swipUIHandler.getTextMessage("获取IC信息成功,55域-->"+Dump.getHexDump(result1.getICCardData()).replace(" ","")));
                	 break;
                 case LandyListener.PBOC_START_FAIL:
                	 finishWorkFali();
                	 swipUIHandler.sendMessage(swipUIHandler.getTextMessage("获取IC信息失败,55域-->"+msg.obj));
                	 break;
                 case LandyListener.ENCRY_PIN_SUCCESS://加密成功
                	 swipUIHandler.sendMessage(swipUIHandler.getTextMessage(ENCRY_PIN_SUCCESS));
                	 String pinblock=(String) msg.obj;
                	 finishEncrykSuccess(pinblock);
                	 break;
                 case LandyListener.ENCRY_PIN_FAIL://加密失败
                	 swipUIHandler.sendMessage(swipUIHandler.getTextMessage(ENCRY_PIN_FAIL+msg.obj));
                	 finishWorkFali();
                	 break;
				default:
					break;
				}
    		}
    	 };
     }
   
     //加载主秘钥
     public void loadMasterKey(){
    	 reader.loadMasterKey(new Byte("0"), hexStringToBytes("945BD7B6A0DA6454B4FA8AE83E6388EB73F9A660"),landyListener.getMyploadMasterKeylistener());
     }
     
     //加载Pin
     public void loadPinKey(){
    	 reader.loadPinKey(new Byte("0"),hexStringToBytes("92A083587845173C92A083587845173C00962B60"), landyListener.getMypLoadPinkeyListener());
    	
    	 
     }
     
     //加载磁道秘钥
     
     public void loadTrackKey(){
    	 reader.loadTrackKey(new Byte("0"),hexStringToBytes("C21734DB8EB1FA9FC21734DB8EB1FA9FE2F24340"),landyListener.getMyploadTrackKeylistener());
     }
     
    
     public  byte[] hexStringToBytes(String hexString) {  
    	     if (hexString == null || hexString.equals("")) {  
    	       return null;  
    	     }  
    	    hexString = hexString.toUpperCase();  
    	   int length = hexString.length() / 2;  
    	     char[] hexChars = hexString.toCharArray();  
    	     byte[] d = new byte[length];  
    	     for (int i = 0; i < length; i++) {  
    	         int pos = i * 2;  
    	         d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));  
    	     }  
    	    return d;  
    	 } 
     private byte charToByte(char c) {  
    	    return (byte) "0123456789ABCDEF".indexOf(c);  
    	 }  

   //随机生成一个EnworkingKey
 	public String getHex_workkey(){
 		String workkey="";
 		for(int i=0;i<16;i++){
 			workkey+=Integer.toHexString(new Random().nextInt(16));
 		}
 		return workkey.toUpperCase();
 	}
 	boolean isConnected=false;
	@Override
	public void openDev() {
		
		//打开设备
		reader.openDevice(CommunicationMode.MODE_DUPLEX, deviceInfo,landyListener.getMypOpenDeviceListener());
		Log.v("landy1", "open");
		controllerHanlder.postDelayed(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(!isConnected){
					swipUIHandler.sendMessage(swipUIHandler.getTextMessage("连接失败1"));
				}
			}
			
		}, 8000);
		
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
		return reader.isConnected();
	}

	@Override
	public void connectDev() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disConnect() {
	
		
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
		reader.getDeviceInfo(landyListener.getMypDeviceInfoLister());
		
	}

	@Override
	public void SwipCard() {
		reader.waitingCard(WaitCardType.MAGNETIC_IC_CARD_RFCARD, amount,"请刷卡", 2000,landyListener.getMypWaitCardListener());
		
	}
	
	
	//刷IC卡
	public void landySwipICcard(){
		final StartPBOCParam startPBOCParam = new StartPBOCParam();
		//获取当前交易日期
		SimpleDateFormat dateFormate=new SimpleDateFormat("yyMMdd-HHmmss");
		String DateStr=dateFormate.format(new Date());
		String[] temp=DateStr.split("-");
		Log.v("landy1", "dataStr-->"+DateStr);
		Log.v("landy1", "day-->"+temp[0]);
		Log.v("landy1", "time-->"+temp[1]);
		Log.v("landy1", "amount-->"+amount);
		//设备交易参数
		byte emvTradeType = 0x00;
		startPBOCParam.setTransactionType(emvTradeType);
		startPBOCParam.setAuthorizedAmount(amount);
		startPBOCParam.setOtherAmount("000000000000");
		startPBOCParam.setDate(temp[0]+"");
		startPBOCParam.setTime(temp[1]+""); // "pos_time":
		startPBOCParam.setForbidContactCard(false);
		startPBOCParam.setForceOnline(true);
		startPBOCParam.setForbidMagicCard(false);
		startPBOCParam.setForbidContactlessCard(false);
		reader.startPBOC(startPBOCParam, landyListener.getEmvListener(), landyListener.getMyppboclistener());
	}
	//关闭emv交易
	public void stopEMV(){
		reader.PBOCStop(new PBOCStopListener() {

			@Override
			public void onError(int errCode, String errDesc) {
				
			}

			@Override
			public void onPBOCStopSuccess() {
				
			}
		});
	}
	//获取磁道信息
	
	public void getTrackData(){
		reader.getTrackDataCipher(landyListener.getMypTackDataListener());
		//reader.getTrackDataPlain(landyListener.getMyptrackDataPalinlistener());
	}
	
	//获取卡号成功返回
	public void finishGetCardNo(LandyTackMsg lanyTrackMsg){
		Message msg=parentHandler.obtainMessage();
		msg.what=SwipActivity.FINISH_GET_CARD_NO_SUCCESS;
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
	//结束刷卡服务，返回数据给主界面
	public void finishWorkSuccess(LandyTackMsg lanyTrackMsg){
		Message msg=parentHandler.obtainMessage();
		msg.what=SwipActivity.FINISH_WORK_SUCCESS;
		msg.obj=lanyTrackMsg;
		parentHandler.sendMessage(msg);
		
	}
	//结束刷卡，返回错误
	public void finishWorkFali(){
		Message msg=parentHandler.obtainMessage();
		msg.what=SwipActivity.FINISH_WORK_FAIL;
		parentHandler.sendMessage(msg);
	}
	


	@Override
	public void enterPin() {
		// TODO Auto-generated method stub
		
	}

	//关闭设备
	@Override
	public void closeDev() {
		try {
			stopEMV();
			reader.closeDevice(landyListener.getMypCloseDeviceListener());
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
	}


	@Override
	public void encrtyPinkey(Object obj) {
     LandyTackMsg landyMsg=(LandyTackMsg)obj;
	 EncryptPinData data=new EncryptPinData();
	 data.cardNO=landyMsg.cardNo;
	 if(landyMsg.cardNo.length()>16){
		 data.cardNO=landyMsg.cardNo.substring(landyMsg.cardNo.length()-16,landyMsg.cardNo.length());
	 }
   	
   	 data.pinData=landyMsg.password;
   	 data.pinKeyIndex=new Byte("0");
   	 reader.encryptPin(data, landyListener.getMypencpinListener());
		
	}

}
