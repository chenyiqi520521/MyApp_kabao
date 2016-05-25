package com.apicloud.wft;



import com.pax.kalai.d180.listener.CheckCardListener;
import com.pax.kalai.d180.listener.ConnectListener;
import com.pax.kalai.d180.listener.GetCardNumberListener;
import com.pax.kalai.d180.listener.GetDeviceInfoListener;
import com.pax.kalai.d180.listener.LoadMasterKeyListener;
import com.pax.kalai.d180.listener.LoadWorkKeyListener;
import com.pax.kalai.d180.listener.MagProcessListener;
import com.pax.kalai.d180.listener.StartPBOCListener;
import com.pax.kalai.d180.mis.Enum.CardType;
import com.pax.kalai.d180.mis.Enum.KeyType;
import com.pax.kalai.d180.mis.MagProcessResult;
import com.pax.kalai.d180.mis.MposDeviceInfo;
import com.pax.kalai.d180.mis.StartPBOCResult;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class wftListener {
	/**
	 2016年4月7日
	 create by mengyupeng
	 TODO
	 **/
	
	private Handler hanlder;//和外界通信
	
	public wftListener(Handler handler){
		this.hanlder=handler;
		MyopenDeviceListener=new myOpenDeviceListener(hanlder);
		MyLoadmianKeyListener=new myLoadMainkeyListener(hanlder);
		MygetDeviceInfoListener=new myGetDeviceInfoListener(hanlder);
		MycheckcardListener=new myCheckCardListener(hanlder);
		MymagprocessListener=new myMagProcessListener(hanlder);
	    MystartPbocListener=new myStartPbocListener(hanlder);
	    MygetCardNumlistener=new myGetCardNumListener(hanlder);
	}
	
	private void sendErrorMsg(Handler handler,int type,int errorCode,String errorMsg){
		  Message msg=handler.obtainMessage();
	      msg.what=100;
	      msg.arg1=type;
	      msg.obj="错误码:"+errorCode+"--提示:"+errorMsg;
	      Log.v("wft1", "type--"+type+"---erromsg>"+msg.obj+"---errorCode"+errorCode);
	      handler.sendMessage(msg);
		
	}
	/**
	 * 设备打开监听器
	 */
	public static final int MSG_OPEN_SUCCESS=600;
	public static final int MSG_OPEN_FAIL=601;
	public myOpenDeviceListener MyopenDeviceListener;
	class myOpenDeviceListener implements ConnectListener{

		private  Handler handler1;
		public myOpenDeviceListener(Handler handler){
			this.handler1=handler;
		}
		@Override
		public void onError(int arg0, String arg1) {
			// TODO Auto-generated method stub
			sendErrorMsg(handler1, MSG_OPEN_FAIL, arg0, arg1);
			
		}

		@Override
		public void onSucc() {
			Message msg=handler1.obtainMessage();
			msg.what=MSG_OPEN_SUCCESS;
			handler1.sendMessage(msg);
			
		}
		
	}
	/**
	 * 导入主秘钥监听
	 */
	public myLoadMainkeyListener MyLoadmianKeyListener;
	public static final int MSG_LOAD_MIAN_KEY_SUCCESS=602;
	public static final int MSG_LOAD_MIAN_KEY_FAIL=603;
	class myLoadMainkeyListener implements LoadMasterKeyListener{

		private Handler handler1;
		public myLoadMainkeyListener(Handler handler){
			this.handler1=handler;
		}
		@Override
		public void onError(int arg0, String arg1) {
			sendErrorMsg(handler1, MSG_LOAD_MIAN_KEY_FAIL, arg0, arg1);
			
		}

		@Override
		public void onSucc() {
			// TODO Auto-generated method stub
			Message msg=handler1.obtainMessage();
			msg.what=MSG_LOAD_MIAN_KEY_SUCCESS;
			handler1.sendMessage(msg);
		}
		
	}
	
	/**
	 * 导入工作秘钥监听
	 */
	public myLoadWorkKeyListener MyLoadWorkkeyListener;
	public static final int MSG_LOAD_WORK_PIN_KEY_SUCCESS=604;
	public static final int  MSG_LOAD_WORK_PIN_KEY_FAIL=605;
	public static final int MSG_LOAD_WORK_TRACK_KEY_SUCCESS=606;
	public static final int  MSG_LOAD_WORK_TRACK_KEY_FAIL=607;
	public myLoadWorkKeyListener getLoadWorklistener(int loadwitch){
		MyLoadWorkkeyListener=new myLoadWorkKeyListener(hanlder, loadwitch);
		return MyLoadWorkkeyListener;
	}
	class myLoadWorkKeyListener implements LoadWorkKeyListener{

		private Handler handler1;
		private int witch;
		public myLoadWorkKeyListener(Handler handler,int loadwitch){
			this.handler1=handler;
			this.witch=loadwitch;
		}
		@Override
		public void onError(int arg0, String arg1) {
			// TODO Auto-generated method stub
			int type=-1;
			if(witch==0){
				type=MSG_LOAD_WORK_TRACK_KEY_FAIL;
			}else{
				type=MSG_LOAD_WORK_PIN_KEY_FAIL;
			}
			sendErrorMsg(handler1, type, arg0, arg1);
		}

		
		@Override
		public void onSucc(KeyType arg0) {
			// TODO Auto-generated method stub
            Message msg=handler1.obtainMessage();
			
			if(arg0==KeyType.TDK){
				msg.what=MSG_LOAD_WORK_TRACK_KEY_SUCCESS;
			}
			if(arg0==KeyType.TPK){
				msg.what=MSG_LOAD_WORK_PIN_KEY_SUCCESS;
			}
			handler1.sendMessage(msg);
		}
		
	}
	
	/**
	 * 获取设备监听
	 */
	public myGetDeviceInfoListener MygetDeviceInfoListener;
	public static final int MSG_GET_DEVICE_INFO_SUCCESS=608;
	public static final int MSG_GET_DEVICE_INFO_FAIL=609;
	class myGetDeviceInfoListener implements GetDeviceInfoListener{

		Handler handler1;
		public myGetDeviceInfoListener(Handler handler) {
			// TODO Auto-generated constructor stub
			this.handler1=handler;
		}
		@Override
		public void onError(int arg0, String arg1) {
			// TODO Auto-generated method stub
			sendErrorMsg(handler1, MSG_GET_DEVICE_INFO_FAIL, arg0, arg1);
			
		}

		@Override
		public void onSucc(MposDeviceInfo arg0) {
			// TODO Auto-generated method stub
			Message msg=handler1.obtainMessage();
			msg.what=MSG_GET_DEVICE_INFO_SUCCESS;
			msg.obj=arg0;
			handler1.sendMessage(msg);
		}
		
	}
	
	
	/**
	 * 刷卡检测监听
	 */
	public myCheckCardListener MycheckcardListener;
	public static final int MSG_CHECK_CARD_SUCCESS=610;
	public static final int MSG_CHECK_CARD_FAIL=611;
	class myCheckCardListener implements CheckCardListener{
		private Handler handler1;
		public myCheckCardListener(Handler handler) {
			// TODO Auto-generated constructor stub
			this.handler1=handler;
		}

		@Override
		public void onError(int arg0, String arg1) {
			// TODO Auto-generated method stub
			sendErrorMsg(handler1, MSG_CHECK_CARD_FAIL, arg0, arg1);
		}

		@Override
		public void onSucc(CardType arg0) {
		    
			Message msg=handler1.obtainMessage();
			msg.what=MSG_CHECK_CARD_SUCCESS;
			msg.obj=arg0;
			handler1.sendMessage(msg);
		}
		
	}
	
	
	public myGetCardNumListener MygetCardNumlistener;
	public static final int MSG_GET_CARDNUM_SUCCESS=616;
	public static final int MSG_GET_CARDNUM_FAIL=617;
	class myGetCardNumListener implements GetCardNumberListener{

		private Handler handler1;
		public myGetCardNumListener(Handler handler) {
			// TODO Auto-generated constructor stub
			handler1=handler;
		}
		@Override
		public void onError(int arg0, String arg1) {
			sendErrorMsg(handler1, MSG_GET_CARDNUM_FAIL, arg0, arg1);
			
		}

		@Override
		public void onSucc(String arg0) {
			// TODO Auto-generated method stub
			Message msg=handler1.obtainMessage();
			msg.what=MSG_GET_CARDNUM_SUCCESS;
			msg.obj=arg0;
			handler1.sendMessage(msg);
			
		}
		
	}
	/**
	 * 磁条卡监听
	 */
	public myMagProcessListener MymagprocessListener ;
	public static final int MSG_MAG_SUCCESS=612;
	public static final int MSG_MAG_FAIL=613;
	class myMagProcessListener implements MagProcessListener{
		private Handler handler1;
		public myMagProcessListener(Handler handler) {
			// TODO Auto-generated constructor stub
			this.handler1=handler;
		}

		@Override
		public void onError(int arg0, String arg1) {
			// TODO Auto-generated method stub
			sendErrorMsg(handler1, MSG_MAG_FAIL, arg0, arg1);
			
		}

		@Override
		public void onSucc(MagProcessResult arg0) {
			// TODO Auto-generated method stub
			Message msg=handler1.obtainMessage();
			msg.what=MSG_MAG_SUCCESS;
			msg.obj=arg0;
			handler1.sendMessage(msg);
			
		}
		
	}
	
	/**
	 * Ic卡刷卡监听
	 */
	public myStartPbocListener MystartPbocListener;
	public static final int MSG_IC_SUCCESS=614;
	public static final int MSG_IC_FAIL=615;
	class myStartPbocListener implements StartPBOCListener{
		
		private Handler handler1;
		public myStartPbocListener(Handler hanlder) {
			// TODO Auto-generated constructor stub
			this.handler1=hanlder;
		}

		@Override
		public void onError(int arg0, String arg1) {
			// TODO Auto-generated method stub
			sendErrorMsg(handler1, MSG_IC_FAIL, arg0, arg1);
		}

		@Override
		public void onSucc(StartPBOCResult arg0) {
			// TODO Auto-generated method stub
			Message msg=handler1.obtainMessage();
			msg.what=MSG_IC_SUCCESS;
			msg.obj=arg0;
			handler1.sendMessage(msg);
		}
		
	}
	
}
