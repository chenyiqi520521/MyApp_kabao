package com.apicloud.landy;

import com.landicorp.android.mpos.reader.PBOCStartListener;
import com.landicorp.android.mpos.reader.model.StartPBOCResult;
import com.landicorp.mpos.reader.BasicReaderListeners.CardType;
import com.landicorp.mpos.reader.BasicReaderListeners.CloseDeviceListener;
import com.landicorp.mpos.reader.BasicReaderListeners.EMVProcessListener;
import com.landicorp.mpos.reader.BasicReaderListeners.EncryptPinDataListener;
import com.landicorp.mpos.reader.BasicReaderListeners.GetDeviceInfoListener;
import com.landicorp.mpos.reader.BasicReaderListeners.GetPANListener;
import com.landicorp.mpos.reader.BasicReaderListeners.GetTaximeterDataListener;
import com.landicorp.mpos.reader.BasicReaderListeners.GetTrackDataCipherListener;
import com.landicorp.mpos.reader.BasicReaderListeners.GetTrackDataPlainListener;
import com.landicorp.mpos.reader.BasicReaderListeners.LoadMasterKeyListener;
import com.landicorp.mpos.reader.BasicReaderListeners.LoadPinKeyListener;
import com.landicorp.mpos.reader.BasicReaderListeners.LoadTrackKeyListener;
import com.landicorp.mpos.reader.BasicReaderListeners.OpenDeviceListener;
import com.landicorp.mpos.reader.BasicReaderListeners.WaitingCardListener;
import com.landicorp.mpos.reader.model.MPosDeviceInfo;
import com.landicorp.mpos.reader.model.MPosEMVProcessResult;

import android.os.Handler;
import android.os.Message;
import android.util.Log;


/**
 * 
 * @author myp
 * 所有联迪的监听类都统一放这里
 *
 */
public class LandyListener {
	
	public static final int OPEN_DEVICE_SUCCESS=100;//打开设备成功
	public static final int OPEN_DEVICE_FAIL=101;//打开设备失败
	
	public static final int GET_DEVICE_INFO_SUCCESS=102;//获取设备信息成功
	public static final int GET_DEVICE_INFO_FAIL=103;//获取设备信息失败
	
	public static final int SWIP_CARD_SUCCESS=104;//刷卡成功
	public static final int SWIP_CARD_FAIL=105;//刷卡失败
	
	
	public static final int GET_PAN_PLAIN_SUCCESS=106;//获取卡号明文成功
	public static final int GET_PAN_PLAIN_FALI=107;//获取卡号明文失败
	
	public static final int GET_TRACK_DATA_SUCCESS=108;//获取磁道成功
	public static final int GET_TRACK_DATA_FAIL=109;//获取磁道失败
	
	public static final int EMV_SUCCESS=110;//获取EMV交易成功
	public static final int EMV_FAIL=111;//获取EMV交易失败
	
	public static final int PBOC_START_SUCCESS=112;//pboc开始成功
	public static final int PBOC_START_FAIL=113;//pboc开始失败
	
	
	public static final int LOAD_MATSTER_KEY_SUCCESS=114;//导入主钥成功
	public static final int LOAD_MATSTER_KEY_FAIL=115;//导入主钥失败
	
	public static final int LOAD_PIN_KEY_SUCCESS=116;//导入pinkey成功
	public static final int LOAD_PIN_KEY_FAIL=117;//导入pinkey失败
	
	public static final int LOAD_TRACK_KEY_SUCCESS=118;//导入磁道秘钥成功
	public static final int LOAD_TRACK_KEY_FAIL=119;//导入磁道秘钥失败
	
	
	public static final int ENCRY_PIN_SUCCESS=120;//加密pin成功
	public static final int ENCRY_PIN_FAIL=121;//加密pin失败
	
	
	
	
	private Handler handler;//外界通信
	
	private mypOpenDeviceListener mypOpenlistener;//设备打开监听
	private mypCloseDeviceListener mypCloselistener;//设备关闭监听
	private mypGetDeviceInfoListener  mypDeviceInfoListener;//获取设备信息监听
	private mypWaitCardListener mypWaitcardlistener;//设备刷卡监听
	private mypGetPanPalin mypGetpanplainListener;//设备获取卡号监听
	private mypGetTrackDataCipherListener mypGettrackDatalistener;//设备获取磁道信息监听
	private mypEmvProcessListener mypEMVprocesslistener;//设备EMV交易监听
	private mypPBOStartListener mypPBOCstartlistener;//设备pboc监听
	private mypTrackDataPalinListener  mypTrackdataPlainlistener;//获取磁条卡明文 监听
	
	private mypLoadMasterKeyListener myploadMasterKeylistener;//加载主钥监听
	
	private mypLoadPinKeyListener myploadPinkeyListener;//pin秘钥加载监听
	
	private mypLoadTrackKeyListener myploadTrackkeyListener;//磁道加密监听
	
	private mypEncryPinListener mypEncpinlistener;//pin加密监听
	
	public  LandyListener(Handler handler){
		this.handler=handler;
		mypOpenlistener=new mypOpenDeviceListener(handler);
		mypCloselistener=new mypCloseDeviceListener();
		mypDeviceInfoListener=new mypGetDeviceInfoListener(handler);
		mypWaitcardlistener=new mypWaitCardListener(handler);
		mypGetpanplainListener=new mypGetPanPalin(handler);
		mypGettrackDatalistener=new mypGetTrackDataCipherListener(handler);
		mypEMVprocesslistener=new mypEmvProcessListener(handler);
		mypPBOCstartlistener=new  mypPBOStartListener(handler);
		mypTrackdataPlainlistener=new mypTrackDataPalinListener(handler);
		myploadMasterKeylistener=new mypLoadMasterKeyListener(handler);
		myploadPinkeyListener=new mypLoadPinKeyListener(handler);
		myploadTrackkeyListener=new mypLoadTrackKeyListener(handler);
		mypEncpinlistener=new mypEncryPinListener(handler);

		
	}
	
	private void sendErrorMsg(Handler handler,int type,int errorCode,String errorMsg){
		  Message msg=handler.obtainMessage();
	      msg.what=type;
	      msg.obj="错误码:"+errorCode+"--提示:"+errorMsg;
	      Log.v("landy1", "loadpin--erro>"+msg.obj);
	      handler.sendMessage(msg);
		
	}
	
	
	
	//获取主钥加载监听
	public mypLoadMasterKeyListener getMyploadMasterKeylistener(){
		return myploadMasterKeylistener;
	}
	class mypLoadMasterKeyListener implements LoadMasterKeyListener{
		private Handler handler;
		public mypLoadMasterKeyListener(Handler handler){
			this.handler=handler;
			
		}
		@Override
		public void onError(int arg0, String arg1) {
			sendErrorMsg(handler, LOAD_MATSTER_KEY_FAIL,arg0 ,arg1);
			
		}
		@Override
		public void onLoadMasterKeySucc() {
			  Message msg=handler.obtainMessage();
		      msg.what=LOAD_MATSTER_KEY_SUCCESS;
		      handler.sendMessage(msg);
			
		}
	}
	
	//获取Pinkey加载监听
	public mypLoadPinKeyListener getMypLoadPinkeyListener(){
		return myploadPinkeyListener;
		
	}
	
	//pinkey加载监听器
	class mypLoadPinKeyListener implements LoadPinKeyListener{

		
		private Handler handler;
	    public mypLoadPinKeyListener(Handler handler) {
			// TODO Auto-generated constructor stub
	    	this.handler=handler;
		}
		@Override
		public void onError(int arg0, String arg1) {
			sendErrorMsg(handler, LOAD_PIN_KEY_FAIL,arg0 ,arg1);
			
		}

		@Override
		public void onLoadPinKeySucc() {
			  Message msg=handler.obtainMessage();
		      msg.what=LOAD_PIN_KEY_SUCCESS;
		      handler.sendMessage(msg);
			
		}
		
	}
	
	
	//获取磁道加密监听
	
	public mypLoadTrackKeyListener getMyploadTrackKeylistener(){
		return myploadTrackkeyListener;
	}
	
	//磁道加密监听
	class mypLoadTrackKeyListener implements LoadTrackKeyListener{
		private Handler hander;
		public mypLoadTrackKeyListener(Handler handler) {
			// TODO Auto-generated constructor stub
			this.hander=handler;
		}

		@Override
		public void onError(int arg0, String arg1) {
			sendErrorMsg(handler, LOAD_TRACK_KEY_FAIL,arg0 ,arg1);
			
		}

		@Override
		public void onLoadTrackKeySucc() {
			  Message msg=handler.obtainMessage();
		      msg.what=LOAD_TRACK_KEY_SUCCESS;
		      handler.sendMessage(msg);
			
		}
		
	}
	
	public mypEncryPinListener getMypencpinListener(){
		return mypEncpinlistener;
	}
	
	//加密Pin监听
	
	class mypEncryPinListener implements EncryptPinDataListener{
		private Handler handler;
		public mypEncryPinListener(Handler handler) {
			// TODO Auto-generated constructor stub
			this.handler=handler;
		}

		@Override
		public void onError(int arg0, String arg1) {
			sendErrorMsg(handler, ENCRY_PIN_FAIL,arg0 ,arg1);
			Log.v("landy1", "pinEnc-->errCode"+arg0+"errMsg-->"+arg1);
			
		}

		@Override
		public void onEncryptPinSucc(String arg0) {
			  Message msg=handler.obtainMessage();
		      msg.what=ENCRY_PIN_SUCCESS;
		      msg.obj=arg0+"";
		      handler.sendMessage(msg);
			
		}
		
	}
	//获取设备打开监听
	public mypOpenDeviceListener getMypOpenDeviceListener(){
		return mypOpenlistener;
	}
	
	//打开设备的内部类
	
	class mypOpenDeviceListener implements OpenDeviceListener{

		private Handler handler;
		public mypOpenDeviceListener(Handler handler) {
			this.handler=handler;
		}
		@Override
		public void openFail() {
			  Log.v("landy1", "openfail");
			  Message msg=handler.obtainMessage();
		      msg.what=OPEN_DEVICE_FAIL;
		      handler.sendMessage(msg);
			
		}

		@Override
		public void openSucc() {
		      Message msg=handler.obtainMessage();
		      msg.what=OPEN_DEVICE_SUCCESS;
		      handler.sendMessage(msg);
			
		}
		
	}
	
	
	//获取断开设备内部类
	public mypCloseDeviceListener getMypCloseDeviceListener(){
		return mypCloselistener;
	}
	//断开设备内部类
	
	class mypCloseDeviceListener implements CloseDeviceListener{
         
		
		@Override
		public void closeSucc() {
			Log.v("landy1", "closesucce");
			
		}
		
	}
	
	
	public mypGetDeviceInfoListener getMypDeviceInfoLister(){
		return mypDeviceInfoListener;
	}
	
	//获取设备监听
	
	class mypGetDeviceInfoListener implements GetDeviceInfoListener{
		private Handler handler;
		public mypGetDeviceInfoListener(Handler handler) {
		 	this.handler=handler;
		}

		@Override
		public void onError(int errCode, String errMsg) {
			 sendErrorMsg(handler, GET_DEVICE_INFO_FAIL,errCode, errMsg);
			
		}

		@Override
		public void onGetDeviceInfoSucc(MPosDeviceInfo info) {
			  Message msg=handler.obtainMessage();
		      msg.what=GET_DEVICE_INFO_SUCCESS;
		      msg.obj=info;
		      handler.sendMessage(msg);
			
		}
		
	}
	
	public mypWaitCardListener getMypWaitCardListener(){
		return mypWaitcardlistener;
	}
	
	//刷卡监听类
	
	class mypWaitCardListener implements WaitingCardListener{
		private Handler handler;
		public mypWaitCardListener(Handler handler) {
			this.handler=handler;
		}

		@Override
		public void onError(int arg0, String errMsg) {
			
			sendErrorMsg(handler, SWIP_CARD_FAIL, arg0, errMsg);
			
		}

		@Override
		public void onProgressMsg(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onWaitingCardSucc(CardType type) {
			
			Message msg=handler.obtainMessage();
			msg.what=SWIP_CARD_SUCCESS;
			String result=type+"";
			msg.obj=result+"";
			handler.sendMessage(msg);
		}
		
	} 
	
	public mypGetPanPalin getMypgetPanPlian(){
		return mypGetpanplainListener;
	}
	//获取卡号明文监听
	
	class mypGetPanPalin implements GetPANListener{
		
		private Handler handler;
		
		public mypGetPanPalin(Handler handler) {
			this.handler=handler;
		}

		@Override
		public void onError(int arg0, String errMsg) {
			sendErrorMsg(handler, GET_PAN_PLAIN_FALI, arg0, errMsg);
			
		}

		@Override
		public void onGetPANSucc(String arg0) {
			Message msg=handler.obtainMessage();
			msg.what=GET_PAN_PLAIN_SUCCESS;
			msg.obj=arg0;
			handler.sendMessage(msg);
			
		}
		
	}
	
	public mypTrackDataPalinListener getMyptrackDataPalinlistener(){
		return mypTrackdataPlainlistener;
	}
	
	//获取磁道卡信息，明文
	
	class mypTrackDataPalinListener implements GetTrackDataPlainListener{
       private Handler handler;
       public mypTrackDataPalinListener(Handler handler) {
		// TODO Auto-generated constructor stub
    	   this.handler=handler;
    	   
	   }
		@Override
		public void onError(int arg0, String arg1) {
			sendErrorMsg(handler, GET_TRACK_DATA_FAIL, arg0,arg1);
			
		}

		@Override
		public void onGetTrackDataPlainSucc(String arg0, String arg1, String arg2) {
			Message msg=handler.obtainMessage();
			LandyTackMsg trackMsg=new LandyTackMsg();
			String[] temp=null;
			String expireDate="";
			trackMsg.track3=arg2.replace("=","")+"";
			if(arg1!=null&&arg1.length()>0){
				temp=arg1.split("=");
				expireDate=temp[1].substring(0, 4)+"";
				
			}
			
			trackMsg.track1=arg0.replace("=","")+"";
			trackMsg.track2=arg1.replace("=","")+"";
			
			trackMsg.expireDate=expireDate;
			Log.v("landy1", "mag--track1>"+arg0);
		    Log.v("landy1", "mag--track2>"+arg1);
		    Log.v("landy1", "mag--track3>"+arg2);
		    Log.v("landy1", "mag--expiredate>"+expireDate);
			msg.what=GET_TRACK_DATA_SUCCESS;
			msg.obj=trackMsg;
			handler.sendMessage(msg);
			
			
		}
		
	}
	
	public mypGetTrackDataCipherListener getMypTackDataListener(){
		return mypGettrackDatalistener;
	}
	
	//获取磁道信息监听,密文
	
	class mypGetTrackDataCipherListener implements GetTrackDataCipherListener{
       private Handler handler;
       public mypGetTrackDataCipherListener(Handler handler) {
		this.handler=handler;
	   }
		@Override
		public void onError(int arg0, String arg1) {
			sendErrorMsg(handler, GET_TRACK_DATA_FAIL, arg0,arg1);
			
		}

		@Override
		public void onGetTrackDataCipherSucc(String arg0, String arg1, String arg2, String arg3) {
			Message msg=handler.obtainMessage();
			LandyTackMsg trackMsg=new LandyTackMsg();
			trackMsg.track1=arg0+"";
			trackMsg.track2=arg1+"";
			trackMsg.track3=arg2+"";
			trackMsg.expireDate=arg3+"";
			msg.what=GET_TRACK_DATA_SUCCESS;
			msg.obj=trackMsg;
			handler.sendMessage(msg);
			
		}
		
	}
	
	
	public mypEmvProcessListener getEmvListener(){
		return mypEMVprocesslistener;
	}
	//IC卡交易监听
	
	class mypEmvProcessListener implements EMVProcessListener{
		
		private Handler handler;
		
		public mypEmvProcessListener(Handler handler) {
			this.handler=handler;
		}

		@Override
		public void onError(int arg0, String arg1) {
			sendErrorMsg(handler, EMV_FAIL, arg0, arg1);
			
		}

		@Override
		public void onEMVProcessSucc(MPosEMVProcessResult result) {
			
			Message msg=handler.obtainMessage();
			msg.what=EMV_SUCCESS;
			msg.obj=result;
			handler.sendMessage(msg);
		}
		
	}
	
	
	public mypPBOStartListener getMyppboclistener(){
		return mypPBOCstartlistener;
	}
	
	//PBOC
	
	class mypPBOStartListener implements PBOCStartListener{
		
		private Handler hander;
	    public mypPBOStartListener(Handler handler) {

          this.hander=handler;
		}

		@Override
		public void onError(int arg0, String arg1) {
			sendErrorMsg(hander,PBOC_START_FAIL,arg0, arg1);
			
		}

		@Override
		public void onPBOCStartSuccess(StartPBOCResult result) {
			Message msg=hander.obtainMessage();
			msg.obj=result;
			msg.what=PBOC_START_SUCCESS;
			hander.sendMessage(msg);
			
		}
		
	}

}
